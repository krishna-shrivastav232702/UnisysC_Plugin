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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.util.Collections;
import java.util.List;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S1066")
public class CollapsibleIfStatementCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    // Subscribe to CONTROL_STATEMENT as defined in CGrammar
    return Collections.singletonList(CGrammar.CONTROL_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isIfStatement(astNode)) {
      return;
    }

    if (hasElseClause(astNode)) {
      return;
    }

    AstNode bodyStatement = astNode.getFirstChild(CGrammar.STATEMENT);

    if (bodyStatement != null) {
      AstNode nestedIf = getNestedIfCollapsible(bodyStatement);
      if (nestedIf != null) {
        addIssue("Merge this if statement with the enclosing one.", nestedIf);
      }
    }
  }

  private static AstNode getNestedIfCollapsible(AstNode statementNode) {
    AstNode directIf = statementNode.getFirstChild(CGrammar.CONTROL_STATEMENT);
    if (isIfStatement(directIf) && !hasElseClause(directIf)) {
      return directIf;
    }
    AstNode block = statementNode.getFirstChild(CGrammar.COMPOUND_STATEMENT);
    if (block != null) {

      if (block.hasDirectChildren(CGrammar.DECLARATION_LIST)) {
        return null;
      }

      AstNode stmtList = block.getFirstChild(CGrammar.STATEMENT_LIST);
      if (stmtList != null && stmtList.getChildren().size() == 1) {

        AstNode innerStmt = stmtList.getFirstChild(CGrammar.STATEMENT);
        if (innerStmt != null) {
          AstNode innerIf = innerStmt.getFirstChild(CGrammar.CONTROL_STATEMENT);
          if (isIfStatement(innerIf) && !hasElseClause(innerIf)) {
            return innerIf;
          }
        }
      }
    }

    return null;
  }

  private static boolean isIfStatement(AstNode node) {
    return node != null && node.is(CGrammar.CONTROL_STATEMENT) &&
        node.getToken() != null && "if".equals(node.getToken().getValue());
  }

  private static boolean hasElseClause(AstNode controlNode) {
    if (controlNode == null) {
      return false;
    }
    for (AstNode child : controlNode.getChildren()) {
      if (child.getToken() != null && "else".equals(child.getToken().getValue())) {
        return true;
      }
    }
    return false;
  }
}
