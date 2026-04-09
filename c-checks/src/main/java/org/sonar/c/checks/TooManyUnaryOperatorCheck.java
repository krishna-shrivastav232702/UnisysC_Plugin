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
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;


@Rule(key = "S1454")
public class TooManyUnaryOperatorCheck extends CCheck {

  private boolean assignmentExpression;
  private int counter;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
      CGrammar.ASSIGNMENT_EXPR,
      CPunctuator.DOUBLE_MINUS,
      CPunctuator.DOUBLE_PLUS);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    assignmentExpression = false;
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.ASSIGNMENT_EXPR)) {
      assignmentExpression = true;
    } else if (assignmentExpression && (astNode.is(CPunctuator.DOUBLE_MINUS) || astNode.is(CPunctuator.DOUBLE_PLUS))) {
      counter++;
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CGrammar.ASSIGNMENT_EXPR)) {
      if (counter > 1) {
        addIssue("Split this expression into multiple expressions so that each one contains no more than a single \"++\" or \"--\" unary operator",
          astNode);
      }
      assignmentExpression = false;
      counter = 0;
    }
  }
}
