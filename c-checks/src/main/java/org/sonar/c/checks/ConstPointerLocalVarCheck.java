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

       
        List<AstNode> nonConstPointers = collectNonConstLocalPointers(declList);
        if (nonConstPointers.isEmpty()) {
            return;
        }
        Set<String> writtenPointers = collectWrittenPointers(compoundStmt);

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

    private List<AstNode> collectNonConstLocalPointers(AstNode declList) {
        List<AstNode> result = new ArrayList<>();

        for (AstNode decl : declList.getChildren(CGrammar.DECLARATION)) {
            if (declarationHasConst(decl)) {
                continue;
            }

            AstNode initDeclList = decl.getFirstChild(CGrammar.INIT_DECLARATOR_LIST);
            if (initDeclList == null) {
                continue;
            }

            for (AstNode initDecl : initDeclList.getChildren(CGrammar.INIT_DECLARATOR)) {
                AstNode declarator = initDecl.getFirstChild(CGrammar.DECLARATOR);
                if (declarator == null) {
                    continue;
                }

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

    private Set<String> collectWrittenPointers(AstNode compoundStmt) {
        Set<String> written = new HashSet<>();

        for (AstNode assignExpr : compoundStmt.getDescendants(CGrammar.ASSIGNMENT_EXPRESSION)) {
            AstNode assignOp = assignExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR);
            if (assignOp == null) {
                continue;
            }

            AstNode leftSide = assignExpr.getFirstChild();
            if (leftSide == null || leftSide.is(CGrammar.ASSIGNMENT_OPERATOR)) {
                continue;
            }

            String derefName = extractDereferencedPointerName(leftSide);
            if (derefName != null) {
                written.add(derefName);
                continue;
            }

            String directName = extractDirectIdentifierName(leftSide);
            if (directName != null) {
                written.add(directName);
            }
        }

        return written;
    }

    private String extractDereferencedPointerName(AstNode node) {
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

        AstNode operand = unaryOp.getNextSibling();
        if (operand == null) {
            return null;
        }

        return extractIdentifierName(operand);
    }

    private String extractDirectIdentifierName(AstNode node) {
        for (AstNode uo : node.getDescendants(CGrammar.UNARY_OPERATOR)) {
            if ("*".equals(uo.getTokenValue())) {
                return null;
            }
        }
        return extractIdentifierName(node);
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
}