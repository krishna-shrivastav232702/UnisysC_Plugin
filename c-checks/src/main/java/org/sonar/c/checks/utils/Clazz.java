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
import javax.annotation.Nullable;

import org.sonar.c.CGrammar;

public final class Clazz {

  private Clazz() {
  }

  public static List<AstNode> getDirectives(AstNode classDefNode) {
    Preconditions.checkState(classDefNode.is(CGrammar.CLASS_DEF));
    return classDefNode
      .getFirstChild(CGrammar.BLOCK)
      .getFirstChild(CGrammar.DIRECTIVES)
      .getChildren(CGrammar.DIRECTIVE);
  }

  public static List<AstNode> getFields(AstNode classDefNode) {
    Preconditions.checkState(classDefNode.is(CGrammar.CLASS_DEF));
    List<AstNode> fields = new ArrayList<>();

    for (AstNode directive : classDefNode.getFirstChild(CGrammar.BLOCK).getFirstChild(CGrammar.DIRECTIVES).getChildren()) {
      AstNode fieldDef = getFieldDefinition(directive);
      if (fieldDef != null) {
        fields.add(fieldDef);
      }
    }
    return fields;
  }

  public static String getName(AstNode classDefNode) {
    Preconditions.checkState(classDefNode.is(CGrammar.CLASS_DEF));
    return classDefNode.getFirstChild(CGrammar.CLASS_NAME)
      .getFirstChild(CGrammar.CLASS_IDENTIFIERS)
      .getLastChild()
      .getTokenValue();
  }

  public static AstNode getConstructor(AstNode classDefNode) {
    Preconditions.checkState(classDefNode.is(CGrammar.CLASS_DEF));
    final String className = Clazz.getName(classDefNode);

    for (AstNode directive : classDefNode.getFirstChild(CGrammar.BLOCK).getFirstChild(CGrammar.DIRECTIVES).getChildren()) {
      AstNode functionDef = getFunctionDefinition(directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE));

      if (functionDef != null && Function.isConstructor(functionDef, className)) {
        return functionDef;
      }
    }

    return null;
  }

  private static AstNode getFieldDefinition(AstNode directive) {
    Preconditions.checkState(directive.is(CGrammar.DIRECTIVE));
    AstNode annotableDir = directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE);
    return annotableDir == null ? null : annotableDir.getFirstChild(CGrammar.VARIABLE_DECLARATION_STATEMENT);
  }

  private static AstNode getFunctionDefinition(@Nullable AstNode annotableDir) {
    return annotableDir != null
      && annotableDir.is(CGrammar.ANNOTABLE_DIRECTIVE)
      && annotableDir.getFirstChild().is(CGrammar.FUNCTION_DEF) ? annotableDir.getFirstChild() : null;
  }

  public static List<AstNode> getFunctions(AstNode classDefNode) {
    Preconditions.checkState(classDefNode.is(CGrammar.CLASS_DEF));
    List<AstNode> functions = new ArrayList<>();

    for (AstNode directive : classDefNode.getFirstChild(CGrammar.BLOCK).getFirstChild(CGrammar.DIRECTIVES).getChildren()) {
      AstNode functionDef = getFunctionDefinition(directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE));

      if (functionDef != null) {
        functions.add(functionDef);
      }
    }
    return functions;
  }

}
