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

@Rule(key = "M23_094")
public class NoCastPointerToIntegralCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.CAST_EXPRESSION);
    }

    @Override
    public void visitNode(AstNode castExpr) {
        AstNode firstChild = castExpr.getFirstChild();
        if (firstChild == null || !"(".equals(firstChild.getTokenValue())) {
            return;
        }

        AstNode typeName = castExpr.getFirstChild(CGrammar.TYPE_NAME);
        if (typeName == null) {
            return;
        }

        if (typeName.hasDescendant(CGrammar.POINTER)) {
            return; // target is a pointer type — not our rule
        }

        if (!isCastTargetIntegral(typeName)) {
            return; // target is not an integral type (e.g. float, double, void)
        }

        // The operand (4th child: LPAR TYPE_NAME RPAR operand) must be a pointer
        AstNode operand = castExpr.getLastChild();
        if (operand == null) {
            return;
        }

        if (operandIsPointer(castExpr, operand)) {
            addIssue(
                "A cast shall not convert a pointer type to an integral type.",
                firstChild
            );
        }
    }

    private boolean isCastTargetIntegral(AstNode typeName) {
        for (AstNode ts : typeName.getDescendants(CGrammar.TYPE_SPECIFIER)) {
            String val = ts.getTokenValue();
            if (val == null) continue;
            switch (val.toLowerCase()) {
                case "int":
                case "long":
                case "short":
                case "char":
                case "unsigned":
                case "signed":
                    return true;
                default:
                    break;
            }
        }
        return false;
    }


    private boolean operandIsPointer(AstNode scope, AstNode operand) {
        // Case 1: address-of expression — &x is always a pointer
        if (operand.hasDescendant(CGrammar.UNARY_OPERATOR)) {
            for (AstNode uo : operand.getDescendants(CGrammar.UNARY_OPERATOR)) {
                if ("&".equals(uo.getTokenValue())) {
                    return true;
                }
            }
        }

        // Case 2: identifier — look up its declaration and check for POINTER node
        String tokenVal = operand.getTokenValue();
        if (tokenVal != null && tokenVal.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
            if (isVariableDeclaredAsPointer(scope, tokenVal)) {
                return true;
            }
        }

        // Also walk children to find identifiers (handles parenthesized operands)
        for (AstNode child : operand.getChildren()) {
            if (operandIsPointer(scope, child)) {
                return true;
            }
        }

        return false;
    }

    private boolean isVariableDeclaredAsPointer(AstNode startNode, String varName) {
        // Walk up to enclosing function or file root
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
            if (declarationDefinesPointerName(decl, varName)) {
                return true;
            }
        }
        return false;
    }

    private boolean declarationDefinesPointerName(AstNode decl, String varName) {
        for (AstNode declarator : decl.getDescendants(CGrammar.DECLARATOR)) {
            if (!declarator.hasDirectChildren(CGrammar.POINTER)) {
                continue;
            }
            AstNode directDecl = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
            if (directDecl != null && varName.equals(directDecl.getTokenValue())) {
                return true;
            }
        }
        return false;
    }
}