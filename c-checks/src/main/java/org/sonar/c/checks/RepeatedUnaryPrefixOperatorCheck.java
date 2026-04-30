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

import java.util.Collections;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S2761")
public class RepeatedUnaryPrefixOperatorCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.UNARY_EXPR);
    }

    @Override
    public void visitNode(AstNode unaryExpr) {
        AstNode operatorNode = unaryExpr.getFirstChild(CGrammar.UNARY_OPERATOR);
        if (operatorNode == null) {
            return;
        }

        String opValue = operatorNode.getTokenValue();
        
        if ("*".equals(opValue) || "&".equals(opValue)) {
            return;
        }

        AstNode castExpr = unaryExpr.getFirstChild(CGrammar.CAST_EXPRESSION);
        if (castExpr == null) {
            return;
        }

        AstNode innerUnary = castExpr.getFirstChild(CGrammar.UNARY_EXPR);
        if (innerUnary == null) {
            return;
        }

        AstNode innerOperatorNode = innerUnary.getFirstChild(CGrammar.UNARY_OPERATOR);
        
        if (innerOperatorNode != null && opValue.equals(innerOperatorNode.getTokenValue())) {
            addIssue("Unary prefix operator \"" + opValue + "\" should not be repeated.", operatorNode);
        }
    }
}