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

import java.util.Arrays;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S5263")
public class NoBitwiseInBooleanContextCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Arrays.asList(
            CGrammar.CONTROL_STATEMENT,
            CGrammar.ITERATION_STATEMENT
        );
    }

    @Override
    public void visitNode(AstNode statement) {
        AstNode firstChild = statement.getFirstChild();
        if (firstChild == null) {
            return;
        }

        if (firstChild.is(CKeyword.SWITCH)) {
            return;
        }

        AstNode condition = getConditionExpression(statement);
        if (condition == null) {
            return;
        }

        checkForBitwiseOperators(condition);
    }

    private AstNode getConditionExpression(AstNode statement) {
        AstNode firstChild = statement.getFirstChild();
        if (firstChild == null) {
            return null;
        }

        if (firstChild.is(CKeyword.IF)
                || firstChild.is(CKeyword.WHILE)
                || firstChild.is(CKeyword.DO)) {
            return statement.getFirstChild(CGrammar.EXPRESSION);
        }

        if (firstChild.is(CKeyword.FOR)) {
            return getForConditionExpression(statement);
        }

        return null;
    }

    private AstNode getForConditionExpression(AstNode forStatement) {
        int semicolonCount = 0;
        for (AstNode child : forStatement.getChildren()) {
            if (child.is(CPunctuator.SEMICOLON)) {
                semicolonCount++;
                continue;
            }
            if (semicolonCount == 1 && child.is(CGrammar.EXPRESSION)) {
                return child;
            }
            if (semicolonCount >= 2) {
                break;
            }
        }
        return null;
    }

    private void checkForBitwiseOperators(AstNode node) {
        if (node == null) {
            return;
        }

        if (node.is(CGrammar.AND_EXPRESSION)) {
            AstNode andOp = getDirectAndOperator(node);
            if (andOp != null) {
                addIssue(
                    "Bitwise operator \"&\" should not be used in a boolean"
                    + " context — use \"&&\" instead.",
                    andOp
                );
                return;
            }
        }

        if (node.is(CGrammar.INCLUSIVE_OR_EXPRESSION)) {
            AstNode orOp = getDirectOrOperator(node);
            if (orOp != null) {
                addIssue(
                    "Bitwise operator \"|\" should not be used in a boolean"
                    + " context — use \"||\" instead.",
                    orOp
                );
                return;
            }
        }

        if (node.is(CGrammar.EXCLUSIVE_OR_EXPRESSION)) {
            AstNode xorOp = getDirectXorOperator(node);
            if (xorOp != null) {
                addIssue(
                    "Bitwise operator \"^\" should not be used in a boolean"
                    + " context — use logical operators instead.",
                    xorOp
                );
                return;
            }
        }

        for (AstNode child : node.getChildren()) {
            if (child.is(CPunctuator.ANDAND)
                    || child.is(CPunctuator.OROR)) {
                continue;
            }
            checkForBitwiseOperators(child);
        }
    }

    private AstNode getDirectAndOperator(AstNode andExpr) {
        for (AstNode child : andExpr.getChildren()) {
            if (child.is(CPunctuator.AND)) {
                return child;
            }
        }
        return null;
    }

    private AstNode getDirectOrOperator(AstNode orExpr) {
        for (AstNode child : orExpr.getChildren()) {
            if (child.is(CPunctuator.OR)) {
                return child;
            }
        }
        return null;
    }

    private AstNode getDirectXorOperator(AstNode xorExpr) {
        for (AstNode child : xorExpr.getChildren()) {
            if (child.is(CPunctuator.XOR)) {
                return child;
            }
        }
        return null;
    }
}