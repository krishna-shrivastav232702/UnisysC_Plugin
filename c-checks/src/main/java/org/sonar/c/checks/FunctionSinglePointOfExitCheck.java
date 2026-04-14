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

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S1005")
public class FunctionSinglePointOfExitCheck extends CCheck {

  private int returnStatements;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(CGrammar.FUNCTION_DEF, CGrammar.RETURN_STATEMENT);
  }

  @Override
  public void visitNode(AstNode node) {
    if (node.is(CGrammar.FUNCTION_DEF)) {
      returnStatements = 0;
    } else if (node.is(CGrammar.RETURN_STATEMENT)) {
      returnStatements++;
    }
  }

  @Override
  public void leaveNode(AstNode node) {
    if (node.is(CGrammar.FUNCTION_DEF) && (returnStatements != 0) && (returnStatements > 1 || !hasReturnAtEnd(node))) {
      addIssue("A function shall have a single point of exit at the end of the function.", node);
    }
  }

  private static boolean hasReturnAtEnd(AstNode functionDefinitionNode) {
    AstNode lastDirectiveNode = functionDefinitionNode
      .getFirstChild(CGrammar.FUNCTION_COMMON)
      .getFirstChild(CGrammar.BLOCK)
      .getFirstChild(CGrammar.DIRECTIVES)
      .getLastChild();
    if (lastDirectiveNode != null) {
      AstNode statementNode = lastDirectiveNode.getFirstChild(CGrammar.STATEMENT);
      if (statementNode != null && statementNode.getFirstChild().is(CGrammar.RETURN_STATEMENT)) {
        return true;
      }
    }
    return false;
  }

}
