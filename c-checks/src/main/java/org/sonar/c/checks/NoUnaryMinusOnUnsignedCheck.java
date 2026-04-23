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
        // UNARY_EXPR = UNARY_OPERATOR CAST_EXPRESSION  (among other alternatives)
        // We need the UNARY_OPERATOR child to be present and be MINUS
        AstNode operatorNode = unaryExpr.getFirstChild(CGrammar.UNARY_OPERATOR);
        if (operatorNode == null) {
            return;
        }

        // UNARY_OPERATOR is a single-token node; its token value is the operator char
        if (!"-".equals(operatorNode.getTokenValue())) {
            return;
        }

        // The operand is the CAST_EXPRESSION sibling after the operator
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
        // Walk the operand subtree collecting all tokens to check
        return isUnsignedInSubtree(scope, operand);
    }

    private boolean isUnsignedInSubtree(AstNode scope, AstNode node) {
        // Check this node's own token
        String tokenValue = node.getTokenValue();
        if (tokenValue != null) {
            // Case 1: Unsigned integer literal (10U, 10UL, 0xFFU, etc.)
            if (isUnsignedLiteral(tokenValue)) {
                return true;
            }
            // Case 2: Identifier — look up its declaration
            if (tokenValue.matches("[a-zA-Z][a-zA-Z0-9]*")) {
                if (isVariableDeclaredUnsigned(scope, tokenValue)) {
                    return true;
                }
            }
        }

        // Recurse into children (handles parenthesized expressions, casts, etc.)
        for (AstNode child : node.getChildren()) {
            if (isUnsignedInSubtree(scope, child)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Matches unsigned integer literal suffixes: 10U 10u 10UL 10ul 0xFFU etc.
     * The grammar rule I_CONSTANT ends with INTEGER_SUFFIX = [uU]([lL]+)? | ([lL]+)[uU]?
     */
    private boolean isUnsignedLiteral(String token) {
        String upper = token.toUpperCase();
        return upper.endsWith("U")
            || upper.endsWith("UL")
            || upper.endsWith("ULL")
            || upper.endsWith("LU")
            || upper.endsWith("LLU");
    }

    /**
     * Walk up from the current UNARY_EXPR node to the enclosing FUNCTION_DEF
     * (or PROGRAM if at file scope), then search all DECLARATION descendants
     * for one that declares varName with an UNSIGNED type specifier.
     *
     * CGrammar.FUNCTION_DEF is: optional(DECLARATION_SPECIFIERS) DECLARATOR FUNCTION_BODY
     * CGrammar.PROGRAM is the file root.
     */
    private boolean isVariableDeclaredUnsigned(AstNode startNode, String varName) {
        // Walk up to the nearest enclosing function or file root
        AstNode scopeRoot = startNode.getParent();
        while (scopeRoot != null
                && !scopeRoot.is(CGrammar.FUNCTION_DEF)
                && !scopeRoot.is(CGrammar.PROGRAM)) {
            scopeRoot = scopeRoot.getParent();
        }
        if (scopeRoot == null) {
            return false;
        }

        // Search every DECLARATION in the scope for one that:
        //   (a) has a DIRECT_DECLARATOR whose token is varName
        //   (b) has a TYPE_SPECIFIER whose token is "unsigned"
        for (AstNode decl : scopeRoot.getDescendants(CGrammar.DECLARATION)) {
            if (declarationDefinesName(decl, varName) && declarationHasUnsigned(decl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if any DIRECT_DECLARATOR descendant of decl has token value == varName.
     * DECLARATION -> INIT_DECLARATOR_LIST -> INIT_DECLARATOR -> DECLARATOR
     *             -> optional(POINTER) DIRECT_DECLARATOR
     * DIRECT_DECLARATOR starts with IDENTIFIER.
     */
    private boolean declarationDefinesName(AstNode decl, String varName) {
        for (AstNode dd : decl.getDescendants(CGrammar.DIRECT_DECLARATOR)) {
            if (varName.equals(dd.getTokenValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if any TYPE_SPECIFIER descendant of decl is the UNSIGNED keyword.
     * TYPE_SPECIFIER -> UNSIGNED (CKeyword), whose token value is "unsigned".
     */
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