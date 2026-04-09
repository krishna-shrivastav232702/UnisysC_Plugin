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
import org.sonar.c.CKeyword;
import org.sonar.check.Rule;

@Rule(key = "S1066")
public class CollapsibleIfStatementCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.IF_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!hasElseClause(astNode)) {

      AstNode childtSatementNode = astNode.getFirstChild(CGrammar.SUB_STATEMENT).getFirstChild(CGrammar.STATEMENT);
      if (childtSatementNode != null) {

        AstNode nestedCollapsibleIf = getNestedIfCollapsible(childtSatementNode.getFirstChild());
        if (nestedCollapsibleIf != null) {
          addIssue("Merge this if statement with the enclosing one.", nestedCollapsibleIf);
        }

      }
    }
  }

  private static AstNode getNestedIfCollapsible(AstNode statementNode) {
    if (statementNode.is(CGrammar.IF_STATEMENT)) {
      return statementNode;
    }

    if (statementNode.is(CGrammar.BLOCK) && statementNode.getFirstChild(CGrammar.DIRECTIVES).getChildren().size() == 1) {
      AstNode singleStatementChild = statementNode.getFirstChild(CGrammar.DIRECTIVES).getFirstChild(CGrammar.DIRECTIVE).getFirstChild();

      if (singleStatementChild.is(CGrammar.STATEMENT) && singleStatementChild.getFirstChild().is(CGrammar.IF_STATEMENT)) {
        AstNode ifNode = singleStatementChild.getFirstChild();
        return !hasElseClause(ifNode) ? ifNode : null;
      }
    }
    return null;
  }

  private static boolean hasElseClause(AstNode astNode) {
    return astNode.hasDirectChildren(CKeyword.ELSE);
  }
}
