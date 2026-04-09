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

@Rule(key = "S108")
public class EmptyNestedBlockCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.BLOCK);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!astNode.getFirstChild(CGrammar.DIRECTIVES).hasChildren() && isNested(astNode) && !hasComment(astNode)) {
      addIssue("Either remove or fill this block of code.", astNode);
    }
  }

  private static boolean isNested(AstNode blockNode) {
    return !blockNode.getParent().is(
      CGrammar.CLASS_DEF,
      CGrammar.INTERFACE_DEF,
      CGrammar.PACKAGE_DEF,
      CGrammar.FUNCTION_COMMON);
  }

  private static boolean hasComment(AstNode blockNode) {
    return blockNode.getFirstChild(CPunctuator.RCURLYBRACE).getToken().hasTrivia();
  }

}
