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
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "M23_099")
public class NoUnaryMinusOnUnsignedCheck extends CCheck {

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

        if (!"-".equals(operatorNode.getTokenValue())) {
            return;
        }

        AstNode operand = operatorNode.getNextSibling();
        if (operand == null) {
            return;
        }

        if (isUnsignedOperand(unaryExpr, operand)) {
            addIssue(
                "The built-in unary '-' operator should not be applied to an expression of unsigned type.",
                operatorNode
            );
        }
    }

    private boolean isUnsignedOperand(AstNode scope, AstNode operand) {
        return isUnsignedInSubtree(scope, operand);
    }

    private boolean isUnsignedInSubtree(AstNode scope, AstNode node) {
        String tokenValue = node.getTokenValue();
        if (tokenValue != null) {
            if (isUnsignedLiteral(tokenValue)) {
                return true;
            }
            if (tokenValue.matches("[a-zA-Z][a-zA-Z0-9]*")) {
                if (isVariableDeclaredUnsigned(scope, tokenValue)) {
                    return true;
                }
            }
        }

        for (AstNode child : node.getChildren()) {
            if (isUnsignedInSubtree(scope, child)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUnsignedLiteral(String token) {
        String upper = token.toUpperCase();
        return upper.endsWith("U")
            || upper.endsWith("UL")
            || upper.endsWith("ULL")
            || upper.endsWith("LU")
            || upper.endsWith("LLU");
    }

    
    private boolean isVariableDeclaredUnsigned(AstNode startNode, String varName) {
        AstNode scopeRoot = startNode.getParent();
        while (scopeRoot != null
                && !scopeRoot.is(CGrammar.FUNCTION_DEF)
                && !scopeRoot.is(CGrammar.PROGRAM)) {
            scopeRoot = scopeRoot.getParent();
        }
        if (scopeRoot == null) {
            return false;
        }

        for (AstNode decl : scopeRoot.getDescendants(CGrammar.DECLARATION)) {
            if (declarationDefinesName(decl, varName) && declarationHasUnsigned(decl)) {
                return true;
            }
        }
        return false;
    }


    private boolean declarationDefinesName(AstNode decl, String varName) {
        for (AstNode dd : decl.getDescendants(CGrammar.DIRECT_DECLARATOR)) {
            if (varName.equals(dd.getTokenValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean declarationHasUnsigned(AstNode decl) {
        for (AstNode ts : decl.getDescendants(CGrammar.TYPE_SPECIFIER)) {
            String val = ts.getTokenValue();
            if ("unsigned".equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }
}