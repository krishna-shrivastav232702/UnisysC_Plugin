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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S5350")
public class ConstPointerLocalVarCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.FUNCTION_DEF);
    }

    @Override
    public void visitNode(AstNode functionDef) {
        AstNode functionBody = functionDef.getFirstChild(CGrammar.FUNCTION_BODY);
        if (functionBody == null) {
            return;
        }

        AstNode compoundStmt = functionBody.getFirstChild(CGrammar.COMPOUND_STATEMENT);
        if (compoundStmt == null) {
            return;
        }

        AstNode declList = compoundStmt.getFirstChild(CGrammar.DECLARATION_LIST);
        if (declList == null) {
            return;
        }

        // Step 1: Collect all local non-const pointer variable names and nodes
        // A pointer declaration has POINTER as direct child of DECLARATOR
        // and does NOT have "const" in its TYPE_SPECIFIER list
        List<AstNode> nonConstPointers = collectNonConstLocalPointers(declList);
        if (nonConstPointers.isEmpty()) {
            return;
        }

        // Step 2: Collect all pointer names that are written to in the body
        // Either: *p = value  (write through dereference)
        // Or:      p = value  (pointer itself reassigned)
        Set<String> writtenPointers = collectWrittenPointers(compoundStmt);

        // Step 3: Flag any pointer that is never written to
        for (AstNode pointerIdentifier : nonConstPointers) {
            String name = pointerIdentifier.getTokenValue();
            if (name != null && !writtenPointers.contains(name)) {
                addIssue(
                    "Pointer local variable \"" + name
                    + "\" should be declared \"const\" since the"
                    + " pointed-to object is never modified.",
                    pointerIdentifier
                );
            }
        }
    }

    /**
     * Collects IDENTIFIER nodes of local pointer variables that are
     * NOT already declared with const.
     *
     * A non-const local pointer looks like:
     *   DECLARATION
     *     ├── DECLARATION_SPECIFIERS
     *     │     └── TYPE_SPECIFIER (int/float/etc — no const)
     *     ├── INIT_DECLARATOR_LIST
     *     │     └── INIT_DECLARATOR
     *     │           └── DECLARATOR
     *     │                 ├── POINTER         ← has POINTER child
     *     │                 └── DIRECT_DECLARATOR
     *     │                       └── IDENTIFIER
     *     └── SEMICOLON
     *
     * A const pointer looks like:
     *   DECLARATION_SPECIFIERS
     *     ├── TYPE_SPECIFIER (const)
     *     └── TYPE_SPECIFIER (int)
     */
    private List<AstNode> collectNonConstLocalPointers(AstNode declList) {
        List<AstNode> result = new ArrayList<>();

        for (AstNode decl : declList.getChildren(CGrammar.DECLARATION)) {
            // Skip if already const
            if (declarationHasConst(decl)) {
                continue;
            }

            // Look for pointer declarators
            AstNode initDeclList = decl.getFirstChild(CGrammar.INIT_DECLARATOR_LIST);
            if (initDeclList == null) {
                continue;
            }

            for (AstNode initDecl : initDeclList.getChildren(CGrammar.INIT_DECLARATOR)) {
                AstNode declarator = initDecl.getFirstChild(CGrammar.DECLARATOR);
                if (declarator == null) {
                    continue;
                }

                // Must have POINTER as direct child to be a pointer variable
                if (!declarator.hasDirectChildren(CGrammar.POINTER)) {
                    continue;
                }

                AstNode directDecl = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
                if (directDecl == null) {
                    continue;
                }

                AstNode identifier = directDecl.getFirstChild(CGrammar.IDENTIFIER);
                if (identifier != null) {
                    result.add(identifier);
                }
            }
        }

        return result;
    }

    /**
     * Returns true if the DECLARATION contains a "const" TYPE_SPECIFIER
     * in its DECLARATION_SPECIFIERS.
     *
     * e.g. const int *p  →  DECLARATION_SPECIFIERS has TYPE_SPECIFIER(const)
     */
    private boolean declarationHasConst(AstNode decl) {
        AstNode declSpecs = decl.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
        if (declSpecs == null) {
            return false;
        }
        for (AstNode ts : declSpecs.getChildren(CGrammar.TYPE_SPECIFIER)) {
            String val = ts.getTokenValue();
            if ("const".equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Scans the compound statement for any writes to pointer variables.
     * Returns a set of pointer variable names that are written to.
     *
     * Two kinds of writes:
     *
     * Kind 1 — pointer reassignment: p = &y
     *   ASSIGNMENT_EXPRESSION
     *     ├── UNARY_EXPR → ... → IDENTIFIER(p)   ← left side, no dereference
     *     ├── ASSIGNMENT_OPERATOR(=)
     *     └── ASSIGNMENT_EXPRESSION (right side)
     *
     * Kind 2 — write through dereference: *p = 99
     *   ASSIGNMENT_EXPRESSION
     *     ├── UNARY_EXPR
     *     │     ├── UNARY_OPERATOR(*)
     *     │     └── CAST_EXPRESSION → ... → IDENTIFIER(p)
     *     ├── ASSIGNMENT_OPERATOR(=)
     *     └── ASSIGNMENT_EXPRESSION (right side)
     */
    private Set<String> collectWrittenPointers(AstNode compoundStmt) {
        Set<String> written = new HashSet<>();

        for (AstNode assignExpr : compoundStmt.getDescendants(CGrammar.ASSIGNMENT_EXPRESSION)) {
            // Must have an ASSIGNMENT_OPERATOR child to be a real assignment
            AstNode assignOp = assignExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR);
            if (assignOp == null) {
                continue;
            }

            // Left side is the first child
            AstNode leftSide = assignExpr.getFirstChild();
            if (leftSide == null || leftSide.is(CGrammar.ASSIGNMENT_OPERATOR)) {
                continue;
            }

            // Kind 2: dereference write — *p = value
            // Left side is UNARY_EXPR with UNARY_OPERATOR(*) as first child
            String derefName = extractDereferencedPointerName(leftSide);
            if (derefName != null) {
                written.add(derefName);
                continue;
            }

            // Kind 1: pointer reassignment — p = value
            // Left side is a plain identifier (no dereference)
            String directName = extractDirectIdentifierName(leftSide);
            if (directName != null) {
                written.add(directName);
            }
        }

        return written;
    }

    /**
     * If the node is a UNARY_EXPR with a dereference operator '*',
     * returns the name of the dereferenced pointer identifier.
     *
     * *p = value  →  UNARY_EXPR
     *                  ├── UNARY_OPERATOR(*)
     *                  └── CAST_EXPRESSION → ... → IDENTIFIER(p)
     */
    private String extractDereferencedPointerName(AstNode node) {
        // Unwrap single-child wrappers to reach the UNARY_EXPR
        AstNode current = node;
        while (current != null
                && current.getChildren().size() == 1) {
            current = current.getChildren().get(0);
        }

        if (current == null || !current.is(CGrammar.UNARY_EXPR)) {
            return null;
        }

        AstNode unaryOp = current.getFirstChild(CGrammar.UNARY_OPERATOR);
        if (unaryOp == null || !"*".equals(unaryOp.getTokenValue())) {
            return null;
        }

        // The operand after the * operator
        AstNode operand = unaryOp.getNextSibling();
        if (operand == null) {
            return null;
        }

        return extractIdentifierName(operand);
    }

    /**
     * Extracts a plain identifier name from a node, but only if there
     * is NO dereference operator in the path — meaning it is a direct
     * variable reference, not a dereference expression.
     *
     * p = value   →  returns "p"
     * *p = value  →  returns null (handled by extractDereferencedPointerName)
     */
    private String extractDirectIdentifierName(AstNode node) {
        // If any UNARY_OPERATOR with '*' exists in subtree, it's a dereference
        for (AstNode uo : node.getDescendants(CGrammar.UNARY_OPERATOR)) {
            if ("*".equals(uo.getTokenValue())) {
                return null;
            }
        }
        return extractIdentifierName(node);
    }

    /**
     * Walks the subtree to find the first IDENTIFIER token value.
     */
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
}