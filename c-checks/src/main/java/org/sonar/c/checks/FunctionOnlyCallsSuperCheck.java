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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.checks.utils.Function;
import org.sonar.check.Rule;

@Rule(key = "S1185")
public class FunctionOnlyCallsSuperCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.FUNCTION_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!isOverridingParentFunction(astNode.getPreviousAstNode())) {
      return;
    }
    AstNode singleDirectiveNode = getSingleStatementBlock(astNode);

    if (singleDirectiveNode != null && isSuperOrReturnOfSuperReference(singleDirectiveNode.getFirstChild())) {
      List<String> parameters = getParametersName(astNode);
      String methodName = Function.getName(astNode);

      if (isUselessCallToSuper(singleDirectiveNode.getFirstChild(CGrammar.STATEMENT), methodName, parameters)
        && !hasMetadataTag(astNode.getParent().getParent().getPreviousAstNode())) {
        addIssue(MessageFormat.format("Remove this method \"{0}\" to simply inherit it.", methodName), astNode);
      }
    }
  }

  private static boolean hasMetadataTag(AstNode directive) {
    return directive.getFirstChild().is(CGrammar.STATEMENT)
      && directive.getFirstChild().getFirstChild().is(CGrammar.METADATA_STATEMENT);
  }

  private static AstNode getSingleStatementBlock(AstNode functionDef) {
    AstNode functionBlock = functionDef.getFirstChild(CGrammar.FUNCTION_COMMON).getFirstChild(CGrammar.BLOCK);

    if (functionBlock != null && functionBlock.getFirstChild(CGrammar.DIRECTIVES).getNumberOfChildren() == 1) {
      return functionBlock.getFirstChild(CGrammar.DIRECTIVES).getFirstChild();
    }
    return null;
  }

  private static boolean isSuperReference(AstNode statement) {
    return statement.getFirstChild(CGrammar.EXPRESSION_STATEMENT) != null
      && isSuperExpression(statement.getFirstChild().getFirstChild().getFirstChild());
  }

  private static boolean isSuperExpression(AstNode listExpression) {
    AstNode postfixExpr = listExpression.getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION).getFirstChild(CGrammar.POSTFIX_EXPRESSION);

    return postfixExpr != null && postfixExpr.getFirstChild(CGrammar.SUPER_EXPR) != null;
  }

  private static boolean isReturnOfSuperReference(AstNode statement) {
    return statement.getFirstChild().is(CGrammar.RETURN_STATEMENT)
      && statement.getFirstChild().getFirstChild(CGrammar.LIST_EXPRESSION) != null
      && isSuperExpression(statement.getFirstChild().getFirstChild(CGrammar.LIST_EXPRESSION));
  }

  private static boolean isSuperOrReturnOfSuperReference(AstNode directiveChild) {
    return directiveChild.is(CGrammar.STATEMENT) && (isSuperReference(directiveChild) || isReturnOfSuperReference(directiveChild));


  }

  private static List<String> getParametersName(AstNode functionDef) {
    List<String> parametersNames = new ArrayList<>();
    for (AstNode identifier : Function.getParametersIdentifiers(functionDef)) {
      parametersNames.add(identifier.getTokenValue());
    }
    return parametersNames;
  }

  private static boolean isOverridingParentFunction(@Nullable AstNode attributesNode) {
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

  private static boolean isUselessCallToSuper(AstNode singleStatement, String methodName, List<String> parameters) {
    StringBuilder sb = new StringBuilder();

    for (Token token : singleStatement.getFirstChild().getTokens()) {
      sb.append(token.getValue());
    }

    String actual = sb.toString();
    String expected = CKeyword.SUPER.getValue() + "." + methodName + "(" + String.join(",", parameters) + ");";

    return actual.equals(expected) || actual.equals(CKeyword.RETURN.getValue() + expected);
  }
}
