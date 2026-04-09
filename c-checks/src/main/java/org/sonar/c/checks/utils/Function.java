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
package org.sonar.c.checks.utils;

import com.sonar.sslr.api.AstNode;
import java.util.ArrayList;
import java.util.List;

import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;

public class Function {

  private Function() {
  }

  public static String getName(AstNode functionDef) {
    Preconditions.checkState(functionDef.is(CGrammar.FUNCTION_DEF));
    return functionDef.getFirstChild(CGrammar.FUNCTION_NAME).getFirstChild(CGrammar.IDENTIFIER).getTokenValue();
  }

  public static boolean isAccessor(AstNode functionDef) {
    Preconditions.checkState(functionDef.is(CGrammar.FUNCTION_DEF));
    return functionDef.getFirstChild(CGrammar.FUNCTION_NAME).getFirstChild(CKeyword.GET, CKeyword.SET) != null;
  }


  public static boolean isEmptyConstructor(AstNode functionDef, String className) {
    Preconditions.checkState(functionDef.is(CGrammar.FUNCTION_DEF));
    AstNode functionBlock = functionDef.getFirstChild(CGrammar.FUNCTION_COMMON).getFirstChild(CGrammar.BLOCK);

    return isConstructor(functionDef, className)
      && (functionBlock == null || functionBlock.getFirstChild(CGrammar.DIRECTIVES).getChildren().isEmpty());
  }

  public static boolean isConstructor(AstNode functionDef, String className) {
    Preconditions.checkState(functionDef.is(CGrammar.FUNCTION_DEF));
    return functionDef.getFirstChild(CGrammar.FUNCTION_NAME).getNumberOfChildren() == 1
      && functionDef.getFirstChild(CGrammar.FUNCTION_NAME).getFirstChild().getTokenValue().equals(className);
  }


  public static List<AstNode> getParametersIdentifiers(AstNode functionDef) {
    Preconditions.checkState(functionDef.is(CGrammar.FUNCTION_DEF, CGrammar.FUNCTION_EXPR));
    List<AstNode> paramIdentifier = new ArrayList<>();
    AstNode parameters = functionDef
      .getFirstChild(CGrammar.FUNCTION_COMMON)
      .getFirstChild(CGrammar.FUNCTION_SIGNATURE)
      .getFirstChild(CGrammar.PARAMETERS);

    if (parameters != null) {
      for (AstNode parameter : parameters.getChildren(CGrammar.PARAMETER, CGrammar.REST_PARAMETERS)) {
        if (parameter.getFirstChild(CGrammar.TYPED_IDENTIFIER) != null) {
          paramIdentifier.add(parameter.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.IDENTIFIER));
        }
      }
    }
    return paramIdentifier;
  }

  public static boolean isOverriding(AstNode functionDef) {
    Preconditions.checkState(functionDef.is(CGrammar.FUNCTION_DEF));
    AstNode attributesNode = functionDef.getPreviousAstNode();

    if (attributesNode != null && attributesNode.is(CGrammar.ATTRIBUTES)) {

      for (AstNode attribute : attributesNode.getChildren()) {
        if (attribute.getFirstChild().is(CGrammar.ATTRIBUTE_EXPR)
          && attribute.getFirstChild().getNumberOfChildren() == 1
          && attribute.getFirstChild().getFirstChild(CGrammar.IDENTIFIER).getTokenValue().equals(CKeyword.OVERRIDE.getValue())) {
          return true;
        }
      }
    }
    return false;
  }
}
