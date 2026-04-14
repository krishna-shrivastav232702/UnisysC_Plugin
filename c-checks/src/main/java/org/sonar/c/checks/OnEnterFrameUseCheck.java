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
import org.sonar.c.checks.utils.Expression;
import org.sonar.check.Rule;

@Rule(key = "S1982")
public class OnEnterFrameUseCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.ASSIGNMENT_EXPRESSION);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.getNumberOfChildren() > 1 && isOnEnterFrame(astNode.getFirstChild()) && isFunctionExpr(astNode.getLastChild())) {
      addIssue("Refactor this code to remove the use of \"onEnterFrame\" event handler.", astNode);
    }
  }

  private static boolean isFunctionExpr(AstNode assignementExpr) {
    AstNode assignmentExprChild = assignementExpr.getFirstChild();
    return assignmentExprChild.is(CGrammar.POSTFIX_EXPRESSION) && assignmentExprChild.getFirstChild().getFirstChild().is(CGrammar.FUNCTION_EXPR);
  }

  private static boolean isOnEnterFrame(AstNode postfixExpr) {
    return Expression.exprToString(postfixExpr).endsWith(".onEnterFrame");
  }

}
