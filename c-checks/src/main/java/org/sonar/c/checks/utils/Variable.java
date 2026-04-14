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

public final class Variable {

  private Variable() {
  }

  public static String getName(AstNode varDeclStatement) {
    Preconditions.checkState(varDeclStatement.is(CGrammar.VARIABLE_DECLARATION_STATEMENT));
    return varDeclStatement
      .getFirstChild(CGrammar.VARIABLE_DEF)
      .getFirstChild(CGrammar.VARIABLE_BINDING_LIST)
      .getFirstChild(CGrammar.VARIABLE_BINDING)
      .getFirstChild(CGrammar.TYPED_IDENTIFIER)
      .getFirstChild(CGrammar.IDENTIFIER).getTokenValue();
  }

  public static boolean isVariable(AstNode directive) {
    Preconditions.checkState(directive.is(CGrammar.DIRECTIVE));
    if (directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE) != null) {
      AstNode variableDecStmt = directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE).getFirstChild(CGrammar.VARIABLE_DECLARATION_STATEMENT);

      if (variableDecStmt != null) {
        return variableDecStmt
          .getFirstChild(CGrammar.VARIABLE_DEF)
          .getFirstChild(CGrammar.VARIABLE_DEF_KIND)
          .getFirstChild().is(CKeyword.VAR);
      }
    }
    return false;
  }

  public static boolean isConst(AstNode directive) {
    Preconditions.checkState(directive.is(CGrammar.DIRECTIVE));
    if (directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE) != null) {
      AstNode variableDecStmt = directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE).getFirstChild(CGrammar.VARIABLE_DECLARATION_STATEMENT);

      if (variableDecStmt != null) {
        return variableDecStmt
          .getFirstChild(CGrammar.VARIABLE_DEF)
          .getFirstChild(CGrammar.VARIABLE_DEF_KIND)
          .getFirstChild().is(CKeyword.CONST);
      }
    }
    return false;
  }

  public static List<AstNode> getDeclaredIdentifiers(AstNode varDeclStatement) {
    Preconditions.checkState(varDeclStatement.is(CGrammar.VARIABLE_DECLARATION_STATEMENT));
    List<AstNode> identifiers = new ArrayList<>();
    if (varDeclStatement.is(CGrammar.VARIABLE_DECLARATION_STATEMENT)) {
      AstNode varBindingList = varDeclStatement
        .getFirstChild(CGrammar.VARIABLE_DEF)
        .getFirstChild(CGrammar.VARIABLE_BINDING_LIST);

      for (AstNode varBinding : varBindingList.getChildren(CGrammar.VARIABLE_BINDING)) {
        identifiers.add(varBinding.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.IDENTIFIER));
      }
    }
    return identifiers;
  }

}
