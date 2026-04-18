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
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

@Rule(key = "S1435")
public class StarTypeUseCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.VARIABLE_BINDING);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode typeExprNode = astNode.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.TYPE_EXPR);
    if (typeExprNode != null && typeExprNode.getFirstChild(CPunctuator.STAR) != null) {
      addIssue("Remove usage of this \"star\" type", typeExprNode);
    }
  }

}
