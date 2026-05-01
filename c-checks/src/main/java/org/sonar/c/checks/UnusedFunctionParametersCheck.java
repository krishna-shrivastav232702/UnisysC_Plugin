/*
 * SonarQube Unisys C Plugin
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S1172")
public class UnusedFunctionParametersCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.FUNCTION_DEF);
  }

  @Override
  public void visitNode(AstNode functionDef) {
    AstNode functionBody = functionDef.getFirstChild(CGrammar.FUNCTION_BODY);
    if (functionBody == null || isExcluded(functionDef, functionBody)) {
      return;
    }

    List<AstNode> parameterIdentifiers = getParameterIdentifiers(functionDef);
    if (parameterIdentifiers.isEmpty()) {
      return;
    }

    Set<String> usedIdentifiers = getUsedIdentifiers(functionBody);
    List<String> unusedParameters = new ArrayList<>();

    for (AstNode parameterIdentifier : parameterIdentifiers) {
      String parameterName = parameterIdentifier.getTokenValue();
      if (!usedIdentifiers.contains(parameterName)) {
        unusedParameters.add(parameterName);
      }
    }

    if (!unusedParameters.isEmpty()) {
      addIssue(buildMessage(unusedParameters), functionDef);
    }
  }

  private static List<AstNode> getParameterIdentifiers(AstNode functionDef) {
    List<AstNode> identifiers = new ArrayList<>();
    AstNode declarator = functionDef.getFirstChild(CGrammar.DECLARATOR);
    if (declarator == null) {
      return identifiers;
    }

    AstNode parameterTypeList = declarator.getFirstDescendant(CGrammar.PARAMETER_TYPE_LIST);
    if (parameterTypeList == null) {
      return identifiers;
    }

    for (AstNode parameterDeclaration : parameterTypeList.getDescendants(CGrammar.PARAMETER_DECLARATION)) {
      AstNode parameterDeclarator = parameterDeclaration.getFirstChild(CGrammar.DECLARATOR);
      AstNode parameterIdentifier = parameterDeclarator == null ? null : getDeclaredIdentifier(parameterDeclarator);
      if (parameterIdentifier != null) {
        identifiers.add(parameterIdentifier);
      }
    }

    return identifiers;
  }

  private static Set<String> getUsedIdentifiers(AstNode functionBody) {
    Set<String> usedIdentifiers = new LinkedHashSet<>();

    for (AstNode identifier : functionBody.getDescendants(CGrammar.IDENTIFIER)) {
      if (!isDeclarationIdentifier(identifier)) {
        usedIdentifiers.add(identifier.getTokenValue());
      }
    }

    return usedIdentifiers;
  }

  private static boolean isDeclarationIdentifier(AstNode identifier) {
    AstNode parent = identifier.getParent();
    return parent != null
        && parent.is(CGrammar.DIRECT_DECLARATOR)
        && parent.getParent() != null
        && parent.getParent().is(CGrammar.DECLARATOR);
  }

  private static AstNode getDeclaredIdentifier(AstNode declarator) {
    AstNode directDeclarator = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
    if (directDeclarator == null) {
      return null;
    }

    AstNode identifier = directDeclarator.getFirstChild(CGrammar.IDENTIFIER);
    if (identifier != null) {
      return identifier;
    }

    AstNode nestedDeclarator = directDeclarator.getFirstChild(CGrammar.DECLARATOR);
    return nestedDeclarator == null ? null : getDeclaredIdentifier(nestedDeclarator);
  }

  private static boolean isExcluded(AstNode functionDef, AstNode functionBody) {
    return isEmptyFunction(functionBody)
        || isEventHandler(functionDef)
        || isVoidFunctionTerminatedByExit(functionDef, functionBody);
  }

  private static boolean isEmptyFunction(AstNode functionBody) {
    return functionBody.getDescendants(CGrammar.STATEMENT).isEmpty();
  }

  private static boolean isEventHandler(AstNode functionDef) {
    String functionName = getFunctionName(functionDef);
    if (functionName == null) {
      return false;
    }

    String lowerCaseName = functionName.toLowerCase(Locale.ENGLISH);
    return lowerCaseName.contains("handle") || startsWithOnPreposition(functionName);
  }

  private static boolean startsWithOnPreposition(String name) {
    return name.startsWith("on")
        && (name.length() == 2 || Character.isUpperCase(name.charAt(2)));
  }

  private static boolean isVoidFunctionTerminatedByExit(AstNode functionDef, AstNode functionBody) {
    if (!isVoidFunction(functionDef)) {
      return false;
    }

    for (AstNode postfixExpression : functionBody.getDescendants(CGrammar.POSTFIX_EXPRESSION)) {
      AstNode calledIdentifier = getCalledIdentifier(postfixExpression);
      if (calledIdentifier != null) {
        String functionName = calledIdentifier.getTokenValue();
        if ("exit".equals(functionName) || "abort".equals(functionName)) {
          return true;
        }
      }
    }

    return false;
  }

  private static AstNode getCalledIdentifier(AstNode postfixExpression) {
    AstNode current = postfixExpression;
    while (current.getFirstChild(CGrammar.POSTFIX_EXPRESSION) != null) {
      current = current.getFirstChild(CGrammar.POSTFIX_EXPRESSION);
    }
    return current.getFirstDescendant(CGrammar.IDENTIFIER);
  }

  private static boolean isVoidFunction(AstNode functionDef) {
    AstNode declarationSpecifiers = functionDef.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
    if (declarationSpecifiers == null) {
      return false;
    }

    for (AstNode typeSpecifier : declarationSpecifiers.getChildren(CGrammar.TYPE_SPECIFIER)) {
      if ("void".equalsIgnoreCase(typeSpecifier.getTokenValue())) {
        return true;
      }
    }

    return false;
  }

  private static String getFunctionName(AstNode functionDef) {
    AstNode declarator = functionDef.getFirstChild(CGrammar.DECLARATOR);
    if (declarator == null) {
      return null;
    }

    AstNode functionIdentifier = getDeclaredIdentifier(declarator);
    return functionIdentifier == null ? null : functionIdentifier.getTokenValue();
  }

  private static String buildMessage(List<String> unusedParameters) {
    String parameterKind = unusedParameters.size() > 1 ? "parameters" : "parameter";
    return MessageFormat.format(
        "Remove the unused function {0} \"{1}\".",
        parameterKind,
        String.join(", ", unusedParameters));
  }
}
