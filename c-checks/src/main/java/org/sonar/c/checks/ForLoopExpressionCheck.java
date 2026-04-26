/*
 * SonarQube Unisys C Plugin
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S886")
public class ForLoopExpressionCheck extends CCheck {

  private static final String MESSAGE =
      "This loop's controlling expressions should only be concerned with loop control.";

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.ITERATION_STATEMENT);
  }

  @Override
  public void visitNode(AstNode node) {
    AstNode forKeyword = node.getFirstChild(CKeyword.FOR);
    if (forKeyword == null) {
      return;
    }

    // Parse the for-loop structure: for ( init ; condition ; update ) body
    AstNode initExpr = null;
    AstNode conditionExpr = null;
    AstNode updateExpr = null;
    AstNode bodyStmt = null;

    int semicolonCount = 0;
    boolean pastLParen = false;

    for (AstNode child : node.getChildren()) {
      if (child.is(CPunctuator.LPARENTHESIS)) {
        pastLParen = true;
        continue;
      }
      if (!pastLParen) {
        continue;
      }
      if (child.is(CPunctuator.SEMICOLON)) {
        semicolonCount++;
        continue;
      }
      if (child.is(CPunctuator.RPARENTHESIS)) {
        continue;
      }
      if (child.is(CGrammar.EXPRESSION)) {
        if (semicolonCount == 0) {
          initExpr = child;
        } else if (semicolonCount == 1) {
          conditionExpr = child;
        } else if (semicolonCount == 2) {
          updateExpr = child;
        }
      }
      if (child.is(CGrammar.STATEMENT)) {
        bodyStmt = child;
      }
    }

    if (hasViolation(initExpr, conditionExpr, updateExpr, bodyStmt)) {
      addIssue(MESSAGE, forKeyword);
    }
  }

  private boolean hasViolation(AstNode initExpr, AstNode conditionExpr,
      AstNode updateExpr, AstNode bodyStmt) {
    // Check 1: Condition contains side effects (++, --, or assignment)
    if (conditionExpr != null && hasSideEffect(conditionExpr)) {
      return true;
    }

    // Check 2: Init has comma expression (multiple initializations)
    if (initExpr != null && hasCommaOperator(initExpr)) {
      return true;
    }

    // Check 3: Loop counter modified in the body
    if (initExpr != null && bodyStmt != null) {
      String counterName = extractCounterName(initExpr);
      if (counterName != null && isVariableModifiedIn(counterName, bodyStmt)) {
        return true;
      }
    }

    // Check 4: Update uses variables that are modified in the body
    if (updateExpr != null && bodyStmt != null) {
      Set<String> updateVars = collectIdentifiers(updateExpr);
      for (String var : updateVars) {
        if (isVariableModifiedIn(var, bodyStmt)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Checks if a node contains side effects: ++, --, or assignment operators.
   */
  private boolean hasSideEffect(AstNode node) {
    if (node.hasDescendant(CPunctuator.DOUBLE_PLUS) || node.hasDescendant(CPunctuator.DOUBLE_MINUS)) {
      return true;
    }
    // Check for assignment operators within the condition
    for (AstNode assignExpr : node.getDescendants(CGrammar.ASSIGNMENT_EXPRESSION)) {
      if (assignExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR) != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the expression contains a comma operator (multiple sub-expressions).
   */
  private boolean hasCommaOperator(AstNode expression) {
    return expression.hasDirectChildren(CPunctuator.COMMA);
  }

  /**
   * Extracts the loop counter variable name from the init expression.
   * Looks for the left-hand side identifier of the first assignment.
   */
  private String extractCounterName(AstNode expression) {
    AstNode assignExpr = findFirstAssignment(expression);
    if (assignExpr == null) {
      return null;
    }
    AstNode leftSide = assignExpr.getFirstChild();
    if (leftSide == null) {
      return null;
    }
    return extractIdentifierName(leftSide);
  }

  private AstNode findFirstAssignment(AstNode node) {
    if (node.is(CGrammar.ASSIGNMENT_EXPRESSION)
        && node.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR) != null) {
      return node;
    }
    for (AstNode child : node.getChildren()) {
      AstNode found = findFirstAssignment(child);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private String extractIdentifierName(AstNode node) {
    if (node.is(CGrammar.IDENTIFIER)) {
      return node.getTokenValue();
    }
    for (AstNode child : node.getChildren()) {
      String name = extractIdentifierName(child);
      if (name != null) {
        return name;
      }
    }
    return null;
  }

  /**
   * Collects all identifier names used in a node.
   */
  private Set<String> collectIdentifiers(AstNode node) {
    Set<String> names = new HashSet<>();
    for (AstNode id : node.getDescendants(CGrammar.IDENTIFIER)) {
      names.add(id.getTokenValue());
    }
    // Also check if the node itself is an identifier
    if (node.is(CGrammar.IDENTIFIER)) {
      names.add(node.getTokenValue());
    }
    return names;
  }

  /**
   * Checks if a variable is modified (assigned, incremented, or decremented)
   * within the given node subtree.
   */
  private boolean isVariableModifiedIn(String varName, AstNode node) {
    // Check for assignment to the variable: varName = ..., varName += ..., etc.
    for (AstNode assignExpr : node.getDescendants(CGrammar.ASSIGNMENT_EXPRESSION)) {
      if (assignExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR) != null) {
        AstNode leftSide = assignExpr.getFirstChild();
        String name = extractIdentifierName(leftSide);
        if (varName.equals(name)) {
          return true;
        }
      }
    }

    // Check for prefix ++/-- on the variable
    for (AstNode unary : node.getDescendants(CGrammar.UNARY_EXPR)) {
      if (unary.hasDirectChildren(CPunctuator.DOUBLE_PLUS) || unary.hasDirectChildren(CPunctuator.DOUBLE_MINUS)) {
        String name = extractIdentifierName(unary);
        if (varName.equals(name)) {
          return true;
        }
      }
    }

    // Check for postfix ++/-- on the variable
    for (AstNode postfix : node.getDescendants(CGrammar.POSTFIX_EXPRESSION)) {
      if (postfix.hasDirectChildren(CPunctuator.DOUBLE_PLUS) || postfix.hasDirectChildren(CPunctuator.DOUBLE_MINUS)) {
        String name = extractIdentifierName(postfix);
        if (varName.equals(name)) {
          return true;
        }
      }
    }

    return false;
  }
}