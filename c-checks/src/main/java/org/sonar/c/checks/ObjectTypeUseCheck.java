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
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S1434")
public class ObjectTypeUseCheck extends CCheck {

  private static final String OBJECT_TYPE = "Object";

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.VARIABLE_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    for (AstNode varBinding : astNode.getFirstChild(CGrammar.VARIABLE_BINDING_LIST).getChildren(CGrammar.VARIABLE_BINDING)) {
      if (isDeclareAsObject(varBinding) || isInitialisedAsObject(varBinding.getFirstChild(CGrammar.VARIABLE_INITIALISATION))) {

        String variableName = varBinding.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.IDENTIFIER).getTokenValue();
        addIssue(MessageFormat.format("Clearly define the type of this ''{0}'' variable", variableName), astNode);
      }
    }
  }

  private static boolean isDeclareAsObject(AstNode varBinding) {
    AstNode typeExpr = varBinding.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.TYPE_EXPR);
    return typeExpr != null && OBJECT_TYPE.equals(typeExpr.getTokenValue());
  }

  private static boolean isInitialisedAsObject(@Nullable AstNode varInitialisation) {
    if (varInitialisation != null) {
      AstNode assignmentExpr = varInitialisation.getFirstChild(CGrammar.VARIABLE_INITIALISER).getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION);

      if (assignmentExpr != null && assignmentExpr.getNumberOfChildren() == 1 && assignmentExpr.getFirstChild().is(CGrammar.POSTFIX_EXPRESSION)) {
        AstNode postfixExprChild = assignmentExpr.getFirstChild(CGrammar.POSTFIX_EXPRESSION).getFirstChild();

        // Check for object initialiser, e.g {attr1:Type, attr2:Type}
        if (postfixExprChild.is(CGrammar.PRIMARY_EXPRESSION)) {
          return postfixExprChild.getFirstChild().is(CGrammar.OBJECT_INITIALISER);

          // Check for instantiation of Object, e.g new Object()
        } else if (postfixExprChild.is(CGrammar.FULL_NEW_EXPR, CGrammar.SHORT_NEW_EXPR)) {
          AstNode subExpr = postfixExprChild.getFirstChild(CGrammar.FULL_NEW_SUB_EXPR, CGrammar.SHORT_NEW_SUB_EXPR);
          return subExpr != null && OBJECT_TYPE.equals(subExpr.getTokenValue());
        }
      }
    }
    return false;
  }
}
