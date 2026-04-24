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

@Rule(key = "M23_081")
public class NoPointerSubtractionCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.ADDITIVE_EXPRESSION);
    }

    @Override
    public void visitNode(AstNode additiveExpr) {
        List<AstNode> children = additiveExpr.getChildren();
        if (children.size() < 3) {
            return;
        }

        for (int i = 1; i < children.size() - 1; i += 2) {
            AstNode op = children.get(i);

            if (!"-".equals(op.getTokenValue())) {
                continue;
            }

            AstNode left  = children.get(i - 1);
            AstNode right = children.get(i + 1);

            if (isPointerOperand(additiveExpr, left)
                    && isPointerOperand(additiveExpr, right)) {
                addIssue(
                    "Subtraction between pointers shall only be applied to pointers"
                    + " that address elements of the same array.",
                    op
                );
            }
        }
    }

    private boolean isPointerOperand(AstNode scope, AstNode operand) {
        if (containsDereference(operand)) {
            return false;
        }

        String name = extractIdentifierName(operand);
        if (name == null) {
            return false;
        }

        return isVariableDeclaredAsPointer(scope, name);
    }

    private boolean containsDereference(AstNode node) {
        for (AstNode uo : node.getDescendants(CGrammar.UNARY_OPERATOR)) {
            if ("*".equals(uo.getTokenValue())) {
                return true;
            }
        }
        return false;
    }

    private String extractIdentifierName(AstNode node) {
        if (node.is(CGrammar.IDENTIFIER)) {
            return node.getTokenValue();
        }
        for (AstNode child : node.getChildren()) {
            String name = extractIdentifierName(child);
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    private boolean isVariableDeclaredAsPointer(AstNode startNode, String varName) {
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