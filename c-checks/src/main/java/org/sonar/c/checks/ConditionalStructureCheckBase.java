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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;

public abstract class ConditionalStructureCheckBase extends CCheck {
  private Set<AstNode> visitedIfStatements = new HashSet<>();
  @Override
  public void visitFile(@Nullable AstNode node) {
    visitedIfStatements.clear();
  }
  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
      CGrammar.CONTROL_STATEMENT);
  }
  @Override
  public void visitNode(AstNode node) {
    if (isIfStatement(node)) {
      if (!visitedIfStatements.add(node)) {
        return;
      }
      visitConditionalStructure(ConditionalStructure.ifStatement(node, visitedIfStatements));
    } else if (isSwitchStatement(node)) {
      visitConditionalStructure(ConditionalStructure.switchStatement(node));
    }
  }

  private boolean isIfStatement(AstNode node) {
    return node.getFirstChild().is(CKeyword.IF);
  }

  private boolean isSwitchStatement(AstNode node) {
    return node.getFirstChild().is(CKeyword.SWITCH);
  }

  abstract void visitConditionalStructure(ConditionalStructure cond);

}
