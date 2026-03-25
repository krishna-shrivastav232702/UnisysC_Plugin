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
import org.sonar.check.Rule;

@Rule(key = "NonEmptyCaseWithoutBreak")
public class NonEmptyCaseWithoutBreakCheck extends CCheck {

  private static final AstNodeType[] JUMP_NODES = {CGrammar.BREAK_STATEMENT, CGrammar.RETURN_STATEMENT, CGrammar.THROW_STATEMENT, CGrammar.CONTINUE_STATEMENT};

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.CASE_ELEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (isLastCaseElement(astNode)) {
      return;
    }

    int jumpStmtNumber = astNode.getDescendants(JUMP_NODES).size();

    if (jumpStmtNumber < 2) {
      AstNode directive = astNode.getLastChild();
      visitLastDirective(astNode, directive);
    }
  }

  private static boolean isLastCaseElement(AstNode astNode) {
    return astNode.getNextSibling().isNot(CGrammar.CASE_ELEMENT);
  }

  private void visitLastDirective(AstNode astNode, AstNode directive) {
    if (isBlock(directive)) {
      visitLastDirective(astNode, directive.getFirstChild().getFirstChild().getLastChild());
      return;
    }
    if (directive.getFirstChild().is(CGrammar.STATEMENT)
      && directive.getFirstChild().getFirstChild().isNot(JUMP_NODES)) {
      List<AstNode> children = astNode.getChildren(CGrammar.CASE_LABEL);
      addIssue("Last statement in this switch-clause should be an unconditional break.",
        children.get(children.size() - 1));
    }
  }

  private static boolean isBlock(AstNode directive) {
    return directive.getNumberOfChildren() == 1
        && directive.getFirstChild().is(CGrammar.STATEMENT)
        && directive.getFirstChild().getNumberOfChildren() == 1
        && directive.getFirstChild().getFirstChild().is(CGrammar.BLOCK);
  }

}
