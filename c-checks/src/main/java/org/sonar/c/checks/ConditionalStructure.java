/*
 * SonarQube Flex Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.c.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;

import static java.util.Collections.singletonList;

class ConditionalStructure {
  static class BranchAndContent {
    AstNode branch;
    List<AstNode> content;
    boolean oneLiner;
    BranchAndContent(AstNode branch, List<AstNode> content, boolean oneLiner) {
      this.branch = branch;
      this.content = content;
      this.oneLiner = oneLiner;
    }
  }
  static BranchAndContent branchAndContentIf(AstNode branch, AstNode statement) {
    return new BranchAndContent(branch, singletonList(statement), isOnelinerStatement(statement));
  }
  static BranchAndContent branchAndContentSwitch(AstNode branch, List<AstNode> statements) {
    return new BranchAndContent(branch, statements, isOnelinerStatements(statements));
  }

  final List<BranchAndContent> branches;
  final boolean allBranchesArePresent;
  final AstNode node;

  ConditionalStructure(AstNode node, List<BranchAndContent> branches, boolean allBranchesArePresent) {
    this.node = node;
    this.branches = branches;
    this.allBranchesArePresent = allBranchesArePresent;
  }

  boolean areAllEquivalentBranches() {
    if (branches.isEmpty()) {
      return false;
    }
    BranchAndContent first = branches.get(0);
    return branches.stream().skip(1).allMatch(next -> SyntacticEquivalence.areEquivalent(first.content, next.content));
  }

  @FunctionalInterface
  interface DuplicatedBranchCallback extends BiConsumer<AstNode, AstNode> {
    void accept(AstNode branchFirstNode1, AstNode branchFirstNode2);
  }

  void forEachBranchDuplication(DuplicatedBranchCallback callback) {
    boolean allEquivalentBranches = areAllEquivalentBranches();
    if (allBranchesArePresent && allEquivalentBranches) {
      return;
    }

    for (BranchAndContent branch1 : branches) {
      if (!branch1.oneLiner || allEquivalentBranches) {
        for (BranchAndContent branch2 : branches) {
          if (branch1 == branch2) {
            break;
          }
          if (SyntacticEquivalence.areEquivalent(branch1.content, branch2.content)) {
            callback.accept(branch1.branch, branch2.branch);
          }
        }
      }
    }
  }

  static boolean isOnelinerNonCompound(AstNode statement) {
    List<Token> tokens = statement.getTokens();
    if (!tokens.isEmpty()) {
      return tokens.get(0).isOnSameLineThan(tokens.get(tokens.size() - 1));
    }
    return false;
  }

  static boolean isOnelinerStatement(AstNode statement) {
    AstNode compoundStatement = statement.getFirstChild(CGrammar.COMPOUND_STATEMENT);
    if (compoundStatement != null) {
      AstNode statementList = compoundStatement.getFirstChild(CGrammar.STATEMENT_LIST);
      if (statementList == null) {
        return true;
      }
      List<AstNode> statements = statementList.getChildren(CGrammar.STATEMENT);
      return isOnelinerStatements(statements);
    }
    return isOnelinerNonCompound(statement);
  }

  static boolean isOnelinerStatements(List<AstNode> statements) {
    if (statements.isEmpty()) {
      return true;
    }
    Token firstToken = statements.get(0).getTokens().get(0);
    Token lastToken = statements.get(statements.size() - 1).getLastToken();
    return firstToken.isOnSameLineThan(lastToken);
  }

  static ConditionalStructure ifStatement(AstNode node, Set<AstNode> visitedIfStatements) {
    List<BranchAndContent> branches = new ArrayList<>();
    boolean allBranchesArePresent = false;

    List<AstNode> statements = node.getChildren(CGrammar.STATEMENT);
    branches.add(branchAndContentIf(node, statements.get(0)));

    AstNode currentNode = node;

    while (currentNode.hasDirectChildren(CKeyword.ELSE)) {
      List<AstNode> currentStatements = currentNode.getChildren(CGrammar.STATEMENT);
      AstNode elseStatement = currentStatements.get(currentStatements.size() - 1);
      AstNode nestedControl = elseStatement.getFirstChild(CGrammar.CONTROL_STATEMENT);
      if (nestedControl != null && nestedControl.hasDirectChildren(CKeyword.IF)) {
        visitedIfStatements.add(nestedControl);
        List<AstNode> nestedStatements = nestedControl.getChildren(CGrammar.STATEMENT);
        branches.add(branchAndContentIf(nestedControl, nestedStatements.get(0)));
        currentNode = nestedControl;
      } else {
        AstNode theElse = currentNode.getFirstChild(CKeyword.ELSE);
        if (theElse != null) {
          branches.add(branchAndContentIf(theElse, elseStatement));
        }
        allBranchesArePresent = true;
        break;
      }
    }

    return new ConditionalStructure(node, branches, allBranchesArePresent);
  }

static ConditionalStructure switchStatement(AstNode node) {
    List<BranchAndContent> branches = new ArrayList<>();
    boolean allBranchesArePresent = false;

    AstNode body = node.getFirstChild(CGrammar.STATEMENT);
    if (body == null) {
        return new ConditionalStructure(node, branches, false);
    }

    AstNode compoundStatement = body.getFirstChild(CGrammar.COMPOUND_STATEMENT);
    if (compoundStatement == null) {
        return new ConditionalStructure(node, branches, false);
    }

    AstNode statementList = compoundStatement.getFirstChild(CGrammar.STATEMENT_LIST);
    if (statementList == null) {
        return new ConditionalStructure(node, branches, false);
    }

    List<AstNode> statements = statementList.getChildren(CGrammar.STATEMENT);

    AstNode currentLabel = null;
    List<AstNode> currentContent = new ArrayList<>();

    for (AstNode statement : statements) {
        AstNode labeledStatement = statement.getFirstChild(CGrammar.LABELED_STATEMENT);

        if (labeledStatement != null &&
            (labeledStatement.hasDirectChildren(CKeyword.CASE) ||
             labeledStatement.hasDirectChildren(CKeyword.DEFAULT))) {

            // Save previous branch
            if (currentLabel != null) {
                List<AstNode> content = stripTrailingBreak(currentContent);
                // oneliner: check if ALL content nodes are on one line
                boolean oneLiner = isOnelinerStatements(content);
                branches.add(new BranchAndContent(currentLabel, content, oneLiner));
            }

            currentLabel = labeledStatement;
            currentContent = new ArrayList<>();

            if (labeledStatement.hasDirectChildren(CKeyword.DEFAULT)) {
                allBranchesArePresent = true;
            }
            AstNode labeledContent = labeledStatement.getFirstChild(CGrammar.STATEMENT);
            if (labeledContent != null) {
                currentContent.add(labeledContent);
            }

        } else {
            if (currentLabel != null) {
                currentContent.add(statement);
            }
        }
    }

    if (currentLabel != null) {
        List<AstNode> content = stripTrailingBreak(currentContent);
        boolean oneLiner = isOnelinerStatements(content);
        branches.add(new BranchAndContent(currentLabel, content, oneLiner));
    }

    return new ConditionalStructure(node, branches, allBranchesArePresent);
}

private static List<AstNode> stripTrailingBreak(List<AstNode> content) {
    if (!content.isEmpty() && isBreakStatement(content.get(content.size() - 1))) {
        return new ArrayList<>(content.subList(0, content.size() - 1));
    }
    return content;
}

private static boolean isBreakStatement(AstNode statement) {
    return statement.is(CGrammar.STATEMENT) &&
        statement.hasDirectChildren(CGrammar.JUMP_STATEMENT) &&
        statement.getFirstChild(CGrammar.JUMP_STATEMENT).hasDirectChildren(CKeyword.BREAK);
}

  AstNode getNode() {
    return node;
  }
}