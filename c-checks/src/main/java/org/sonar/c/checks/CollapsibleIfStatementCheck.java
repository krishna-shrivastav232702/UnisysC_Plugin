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
import com.sonar.sslr.api.AstNodeType;
import java.util.Collections;
import java.util.List;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.api.CKeyword;
import org.sonar.check.Rule;

@Rule(key = "S1066")
public class CollapsibleIfStatementCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    // Subscribe to CONTROL_STATEMENT as it contains the IF rule
    return Collections.singletonList(CGrammar.CONTROL_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    // Ensure this is an IF statement (not SWITCH)
    if (!astNode.hasDirectChildren(CKeyword.IF)) {
      return;
    }

    // Only collapsible if there is no 'else' clause
    if (!hasElseClause(astNode)) {
      // According to CGrammar: IF, LPAREN, EXPR, RPAREN, STATEMENT
      AstNode bodyStatement = astNode.getFirstChild(CGrammar.STATEMENT);

      if (bodyStatement != null) {
        AstNode nestedIf = getNestedIfCollapsible(bodyStatement);
        if (nestedIf != null) {
          addIssue("Merge this if statement with the enclosing one.", nestedIf);
        }
      }
    }
  }

  private static AstNode getNestedIfCollapsible(AstNode statementNode) {
    // A STATEMENT node in your grammar wraps the actual logic
    AstNode actualContent = statementNode.getFirstChild();
    if (actualContent == null)
      return null;

    // Case 1: Direct nesting (if (a) if (b) ...)
    if (isIfStatement(actualContent)) {
      return !hasElseClause(actualContent) ? actualContent : null;
    }

    // Case 2: Nesting inside a block (if (a) { if (b) ... })
    if (actualContent.is(CGrammar.COMPOUND_STATEMENT)) {
      // If the block contains declarations (e.g. int x;), it's not collapsible
      if (actualContent.hasDirectChildren(CGrammar.DECLARATION_LIST)) {
        return null;
      }

      AstNode stmtList = actualContent.getFirstChild(CGrammar.STATEMENT_LIST);
      // Must contain exactly one statement
      if (stmtList != null && stmtList.getChildren().size() == 1) {
        AstNode singleStmt = stmtList.getFirstChild(CGrammar.STATEMENT);
        AstNode innerControl = (singleStmt != null) ? singleStmt.getFirstChild() : null;

        if (innerControl != null && isIfStatement(innerControl)) {
          return !hasElseClause(innerControl) ? innerControl : null;
        }
      }
    }
    return null;
  }

  private static boolean isIfStatement(AstNode node) {
    return node.is(CGrammar.CONTROL_STATEMENT) && node.hasDirectChildren(CKeyword.IF);
  }

  private static boolean hasElseClause(AstNode controlNode) {
    return controlNode.hasDirectChildren(CKeyword.ELSE);
  }
}