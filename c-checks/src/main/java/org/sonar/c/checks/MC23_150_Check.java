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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.api.CKeyword;

@Rule(key = "M23_150")
public class MC23_150_Check extends CCheck {

    private static final String MESSAGE = "This non-void function does not return a value on all paths.";

    @Override
    public List<AstNodeType> subscribedTo() {
        List<AstNodeType> types = new ArrayList<>();
        types.add(CGrammar.FUNCTION_DEF);
        return types;
    }

    @Override
    public void visitFile(@Nullable AstNode astNode) {
    }

    @Override
    public void visitNode(AstNode functionDef) {
        if (isVoidFunction(functionDef))
            return;

        // Must have a body (COMPOUND_STATEMENT), not just a prototype
        AstNode body = functionDef.getFirstChild(CGrammar.COMPOUND_STATEMENT);
        if (body == null)
            return;

        AstNode statementList = body.getFirstChild(CGrammar.STATEMENT_LIST);
        if (statementList == null || statementList.getNumberOfChildren() == 0)
            return;

        if (!allPathsReturn(statementList)) {
            addIssue(MESSAGE, functionDef);
        }
    }

    @Override
    public void leaveNode(AstNode astNode) {
    }

    /**
     * Checks DECLARATION_SPECIFIERS tokens for "void".
     * FUNCTION_DEF → DECLARATION_SPECIFIERS DECLARATOR ...
     */
    private static boolean isVoidFunction(AstNode functionDef) {
        AstNode specifiers = functionDef.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
        if (specifiers == null)
            return false;
        for (Token token : specifiers.getTokens()) {
            if ("void".equals(token.getValue()))
                return true;
        }
        return false;
    }

    private static boolean allPathsReturn(AstNode statementList) {
        if (statementList == null)
            return false;
        List<AstNode> statements = statementList.getChildren(CGrammar.STATEMENT);
        for (AstNode statement : statements) {
            if (statementAlwaysReturns(statement))
                return true;
        }
        return false;
    }

    /**
     * In the new grammar:
     * - return is inside JUMP_STATEMENT
     * - if/switch is inside CONTROL_STATEMENT
     * - while/for/do is inside ITERATION_STATEMENT
     * - blocks are COMPOUND_STATEMENT
     */
    private static boolean statementAlwaysReturns(AstNode statement) {

        // ── JUMP_STATEMENT (return/break/continue/goto) ───────────────────────
        AstNode jumpStmt = statement.getFirstChild(CGrammar.JUMP_STATEMENT);
        if (jumpStmt != null) {
            // Only "return" guarantees a value — not break/continue/goto
            return jumpStmt.getFirstChild(CKeyword.RETURN) != null;
        }

        // ── CONTROL_STATEMENT (if / switch) ────────────────────────────────
        AstNode selectionStmt = statement.getFirstChild(CGrammar.CONTROL_STATEMENT);
        if (selectionStmt != null) {
            if (selectionStmt.getFirstChild(CKeyword.IF) != null) {
                return ifAlwaysReturns(selectionStmt);
            }
            if (selectionStmt.getFirstChild(CKeyword.SWITCH) != null) {
                return switchAlwaysReturns(selectionStmt);
            }
        }

        // ── COMPOUND_STATEMENT { } ────────────────────────────────────────────
        AstNode compoundStmt = statement.getFirstChild(CGrammar.COMPOUND_STATEMENT);
        if (compoundStmt != null) {
            AstNode innerList = compoundStmt.getFirstChild(CGrammar.STATEMENT_LIST);
            return allPathsReturn(innerList);
        }

        // ── ITERATION_STATEMENT (while/for/do) — conservative ────────────────
        if (statement.getFirstChild(CGrammar.ITERATION_STATEMENT) != null) {
            return false; // loop may not execute
        }

        return false;
    }

    /**
     * CONTROL_STATEMENT → IF ( EXPRESSION ) STATEMENT [ ELSE STATEMENT ]
     * Both branches must return for the if to always return.
     */
    private static boolean ifAlwaysReturns(AstNode selectionStmt) {
        List<AstNode> statements = selectionStmt.getChildren(CGrammar.STATEMENT);
        if (statements.size() < 2)
            return false; // no else branch
        return statementAlwaysReturns(statements.get(0))
                && statementAlwaysReturns(statements.get(1));
    }

    /**
     * CONTROL_STATEMENT → SWITCH ( EXPRESSION ) STATEMENT
    * Conservative: requires a COMPOUND_STATEMENT body where
    * all statements that are reachable end in a JUMP_STATEMENT(return).
     */
    private static boolean switchAlwaysReturns(AstNode selectionStmt) {
        AstNode bodyStmt = selectionStmt.getFirstChild(CGrammar.STATEMENT);
        if (bodyStmt == null)
            return false;
        AstNode compoundStmt = bodyStmt.getFirstChild(CGrammar.COMPOUND_STATEMENT);
        if (compoundStmt == null)
            return false;

        AstNode statementList = compoundStmt.getFirstChild(CGrammar.STATEMENT_LIST);
        if (statementList == null)
            return false;

        // Must have a default label and all items must return
        boolean hasDefault = false;
        for (AstNode stmt : statementList.getChildren(CGrammar.STATEMENT)) {
            // Check for default label via LABELED_STATEMENT
            AstNode labeledStmt = stmt.getFirstChild(CGrammar.LABELED_STATEMENT);
            if (labeledStmt != null && labeledStmt.getFirstChild(CKeyword.DEFAULT) != null) {
                hasDefault = true;
            }
            AstNode jumpStmt = stmt.getFirstChild(CGrammar.JUMP_STATEMENT);
            if (jumpStmt != null && jumpStmt.getFirstChild(CKeyword.RETURN) == null) {
                return false; // non-return jump (break)
            }
        }
        return hasDefault;
    }
}