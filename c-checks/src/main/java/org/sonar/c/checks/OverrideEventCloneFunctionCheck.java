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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.checks.utils.Function;
import org.sonar.check.Rule;

@Rule(key = "S1470")
public class OverrideEventCloneFunctionCheck extends CCheck {

  private static final String EVENT_TYPE_NAME = "Event";

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.CLASS_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isExtendingEvent(astNode)) {
      return;
    }
    List<AstNode> classDirectives = astNode
      .getFirstChild(CGrammar.BLOCK)
      .getFirstChild(CGrammar.DIRECTIVES)
      .getChildren(CGrammar.DIRECTIVE);

    for (AstNode directive : classDirectives) {
      if (isOverridingFunction(directive) && isCloneFunction(directive)) {
        return;
      }
    }

    String className = astNode.getFirstChild(CGrammar.CLASS_NAME).getFirstChild(CGrammar.CLASS_IDENTIFIERS).getLastChild().getTokenValue();
    addIssue(MessageFormat.format("Make this class \"{0}\" override \"Event.clone()\" function.", className), astNode);
  }

  private static boolean isCloneFunction(AstNode directive) {
    AstNode functionDef = directive
      .getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE)
      .getFirstChild(CGrammar.FUNCTION_DEF);

    String functionName = Function.getName(functionDef);

    return "clone".equals(functionName) && EVENT_TYPE_NAME.equals(getResultType(functionDef));
  }

  private static String getResultType(AstNode functionDef) {
    AstNode resultType = functionDef
      .getFirstChild(CGrammar.FUNCTION_COMMON)
      .getFirstChild(CGrammar.FUNCTION_SIGNATURE)
      .getFirstChild(CGrammar.RESULT_TYPE);

    if (resultType != null && resultType.getFirstChild(CGrammar.TYPE_EXPR) != null) {
      return resultType.getFirstChild(CGrammar.TYPE_EXPR).getTokenValue();
    }
    return null;
  }


  private static boolean isExtendingEvent(AstNode classDef) {
    AstNode inheritenceNode = classDef.getFirstChild(CGrammar.INHERITENCE);

    if (inheritenceNode != null && inheritenceNode.getFirstChild(CKeyword.EXTENDS) != null) {
      AstNode qualifiedId = inheritenceNode.getFirstChild(CGrammar.TYPE_EXPR).getLastChild();
      if (qualifiedId.is(CGrammar.QUALIFIED_IDENTIFIER) && EVENT_TYPE_NAME.equals(qualifiedId.getTokenValue())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isOverridingFunction(AstNode directive) {
    return isFunctionWithAttributes(directive) && isOverriding(directive);
  }

  private static boolean isFunctionWithAttributes(AstNode directive) {
    return directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE) != null
      && directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE).getFirstChild().is(CGrammar.FUNCTION_DEF)
      && directive.getFirstChild(CGrammar.ATTRIBUTES) != null;
  }

  private static boolean isOverriding(AstNode directive) {
    for (AstNode attribute : directive.getFirstChild(CGrammar.ATTRIBUTES).getChildren()) {

      if (attribute.getFirstChild().is(CGrammar.ATTRIBUTE_EXPR)
        && attribute.getFirstChild().getNumberOfChildren() == 1
        && attribute.getFirstChild().getFirstChild(CGrammar.IDENTIFIER).getTokenValue().equals(CKeyword.OVERRIDE.getValue())) {
        return true;
      }
    }
    return false;
  }
}
