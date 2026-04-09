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
import com.sonar.sslr.api.Token;
import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.checks.utils.Function;
import org.sonar.c.checks.utils.Preconditions;
import org.sonar.check.Rule;

@Rule(key = "S1172")
public class UnusedFunctionParametersCheck extends CCheck {

  private Deque<Boolean> classes = new ArrayDeque<>();

  private static class Scope {
    private final Scope outerScope;
    private final AstNode functionDec;
    private final Map<String, Integer> arguments;

    public Scope(Scope outerScope, AstNode functionDec) {
      this.outerScope = outerScope;
      this.functionDec = functionDec;
      this.arguments = new LinkedHashMap<>();
    }

    private void declare(AstNode astNode) {
      Preconditions.checkState(astNode.is(CGrammar.IDENTIFIER));
      String identifier = astNode.getTokenValue();
      arguments.put(identifier, 0);
    }

    private void use(String value) {
      Scope scope = this;
      while (scope != null) {
        Integer usage = scope.arguments.get(value);
        if (usage != null) {
          usage++;
          scope.arguments.put(value, usage);
          return;
        }
        scope = scope.outerScope;
      }
    }
  }

  private static final AstNodeType[] FUNCTION_NODES = {CGrammar.FUNCTION_DEF, CGrammar.FUNCTION_EXPR};
  private Scope currentScope;

  @Override
  public List<AstNodeType> subscribedTo() {
    List<AstNodeType> types = new ArrayList<>();
    types.add(CGrammar.POSTFIX_EXPR);
    types.add(CGrammar.PARAMETERS);
    types.add(CGrammar.CLASS_DEF);
    Collections.addAll(types, FUNCTION_NODES);
    return types;
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    currentScope = null;
    classes.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.CLASS_DEF)) {
      classes.push(implementsAnInterface(astNode));
    } else if (astNode.is(FUNCTION_NODES)) {
      // enter new scope
      currentScope = new Scope(currentScope, astNode);

    } else if (currentScope != null && astNode.is(CGrammar.PARAMETERS) && astNode.getParent().is(CGrammar.FUNCTION_SIGNATURE)) {
      declareInCurrentScope(Function.getParametersIdentifiers(currentScope.functionDec));

    } else if (currentScope != null && astNode.is(CGrammar.POSTFIX_EXPR)) {
      AstNode postfixExprChild = astNode.getFirstChild();
      // check if it is not a call to function with same name than the parameter
      if (postfixExprChild.is(CGrammar.PRIMARY_EXPR) && postfixExprChild.getNextAstNode().isNot(CGrammar.ARGUMENTS)) {
        currentScope.use(getPrimaryExpressionStringValue(postfixExprChild));
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(FUNCTION_NODES) && isNotAbstract(astNode)) {
      // leave scope
      if (!isExcluded(astNode)) {
        reportUnusedArgument();
      }
      currentScope = currentScope.outerScope;
    } else if (astNode.is(CGrammar.CLASS_DEF)) {
      classes.pop();
    }
  }

  private void reportUnusedArgument() {
    int nbUnusedArgs = 0;
    StringBuilder formatBuilder = new StringBuilder("Remove the unused function {0} \"");

    for (Map.Entry<String, Integer> entry : currentScope.arguments.entrySet()) {
      if (entry.getValue() == 0) {
        formatBuilder.append(entry.getKey()).append(", ");
        nbUnusedArgs++;
      }
    }

    if (nbUnusedArgs > 0) {
      formatBuilder.replace(formatBuilder.length() - 2, formatBuilder.length(), "\".");
      String message = MessageFormat.format(formatBuilder.toString(), nbUnusedArgs > 1 ? "parameters" : "parameter");
      addIssue(message, currentScope.functionDec);
    }
  }

  private boolean isExcluded(AstNode functionDec) {
    AstNode directives = functionDec
      .getFirstChild(CGrammar.FUNCTION_COMMON)
      .getFirstChild(CGrammar.BLOCK)
      .getFirstChild(CGrammar.DIRECTIVES);

    return isExcludedFunctionDeclaration(functionDec) || isEmpty(directives)
      || containsOnlyThrowStmt(directives) || isInClassImplementingInterface();
  }

  private static Boolean implementsAnInterface(AstNode classDef) {
    AstNode inheritenceNode = classDef.getFirstChild(CGrammar.INHERITENCE);
    return inheritenceNode != null && inheritenceNode.getFirstChild().is(CKeyword.IMPLEMENTS);
  }

  private boolean isInClassImplementingInterface() {
    return !classes.isEmpty() && classes.peek();
  }

  private static boolean containsOnlyThrowStmt(AstNode directives) {
    List<AstNode> directiveList = directives.getChildren();

    if (directiveList.size() == 1) {
      AstNode directiveKind = directiveList.get(0).getFirstChild().getFirstChild();
      return directiveKind.is(CGrammar.THROW_STATEMENT);
    }
    return false;
  }

  private static boolean isEmpty(AstNode directives) {
    return directives.getNumberOfChildren() == 0;
  }

  private void declareInCurrentScope(List<AstNode> identifiers) {
    for (AstNode identifier : identifiers) {
      currentScope.declare(identifier);
    }
  }

  private static boolean isExcludedFunctionDeclaration(AstNode functionDec) {
    return functionDec.is(CGrammar.FUNCTION_DEF) && (Function.isOverriding(functionDec) || isEventHandler(functionDec));
  }

  private static boolean isEventHandler(AstNode functionDec) {
    String functionName = functionDec.getFirstChild(CGrammar.FUNCTION_NAME).getTokenValue();

    if (functionName.toLowerCase(Locale.ENGLISH).contains("handle") || startsWithOnPreposition(functionName)) {
      AstNode parameters = functionDec
        .getFirstChild(CGrammar.FUNCTION_COMMON)
        .getFirstChild(CGrammar.FUNCTION_SIGNATURE)
        .getFirstChild(CGrammar.PARAMETERS);

      if (parameters != null) {
        AstNode firstParameter = parameters.getFirstChild(CGrammar.PARAMETER);

        if (firstParameter != null && firstParameter.getFirstChild(CGrammar.TYPED_IDENTIFIER) != null) {
          AstNode firstParameterType = firstParameter
            .getFirstChild(CGrammar.TYPED_IDENTIFIER)
            .getFirstChild(CGrammar.TYPE_EXPR);
          return firstParameterType != null && firstParameterType.getLastToken().getValue().endsWith("Event");
        }
      }
    }
    return false;
  }

  private static boolean startsWithOnPreposition(String name) {
    return name.startsWith("on") && (name.length() == 2 || name.substring(2, 3).matches("[A-Z]"));
  }

  private static boolean isNotAbstract(AstNode functionDef) {
    return functionDef.getFirstChild(CGrammar.FUNCTION_COMMON).getLastChild().is(CGrammar.BLOCK);
  }

  private static String getPrimaryExpressionStringValue(AstNode postfixExpr) {
    StringBuilder builder = new StringBuilder();
    for (Token t : postfixExpr.getTokens()) {
      builder.append(t.getValue());
    }
    return builder.toString();
  }

}
