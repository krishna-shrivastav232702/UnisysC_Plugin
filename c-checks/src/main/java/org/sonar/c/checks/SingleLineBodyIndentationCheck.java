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

@Rule(key = "S3973")
public class SingleLineBodyIndentationCheck extends CCheck {

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

        if (firstChild.is(CKeyword.IF)) {
            checkIfStatement(statement, firstChild);
            return;
        }

        if (firstChild.is(CKeyword.WHILE)) {
            checkSingleBody(statement, firstChild,
                getWhileBody(statement));
            return;
        }

        if (firstChild.is(CKeyword.FOR)) {
            checkSingleBody(statement, firstChild,
                getForBody(statement));
            return;
        }

        if (firstChild.is(CKeyword.DO)) {
            checkSingleBody(statement, firstChild,
                getDoBody(statement));
        }
    }

    private void checkIfStatement(AstNode statement, AstNode ifKeyword) {
        List<AstNode> children = statement.getChildren();

        AstNode ifBody = null;
        AstNode elseKeyword = null;
        AstNode elseBody = null;

        boolean pastRParen = false;
        boolean pastElse = false;

        for (AstNode child : children) {
            if (child.is(CPunctuator.RPARENTHESIS)) {
                pastRParen = true;
                continue;
            }
            if (pastRParen && !pastElse && child.is(CGrammar.STATEMENT)) {
                ifBody = child;
                continue;
            }
            if (child.is(CKeyword.ELSE)) {
                elseKeyword = child;
                pastElse = true;
                continue;
            }
            if (pastElse && child.is(CGrammar.STATEMENT)) {
                elseBody = child;
            }
        }

        checkSingleBody(statement, ifKeyword, ifBody);

        if (elseKeyword != null && elseBody != null) {
            checkSingleBody(statement, elseKeyword, elseBody);
        }
    }

    private void checkSingleBody(AstNode statement,
            AstNode keyword, AstNode body) {
        if (body == null) {
            return;
        }

        AstNode actualBody = getActualStatement(body);
        if (actualBody == null) {
            return;
        }

        if (actualBody.is(CGrammar.COMPOUND_STATEMENT)) {
            return;
        }

        int keywordLine = keyword.getTokenLine();
        int bodyLine = actualBody.getTokenLine();

        if (keywordLine == bodyLine) {
            addIssue(
                "A conditionally executed single line should be"
                + " denoted by indentation, not placed on the same"
                + " line as the controlling statement.",
                keyword
            );
        }
    }

    private AstNode getActualStatement(AstNode statementNode) {
        if (statementNode == null) {
            return null;
        }
        List<AstNode> children = statementNode.getChildren();
        if (children.size() == 1) {
            return children.get(0);
        }
        return statementNode;
    }

    private AstNode getWhileBody(AstNode iterationStatement) {
        List<AstNode> children = iterationStatement.getChildren();
        if (children.isEmpty()) {
            return null;
        }
        AstNode last = children.get(children.size() - 1);
        if (last.is(CGrammar.STATEMENT)) {
            return last;
        }
        return null;
    }

    private AstNode getForBody(AstNode iterationStatement) {
        List<AstNode> children = iterationStatement.getChildren();
        if (children.isEmpty()) {
            return null;
        }
        AstNode last = children.get(children.size() - 1);
        if (last.is(CGrammar.STATEMENT)) {
            return last;
        }
        return null;
    }

    private AstNode getDoBody(AstNode iterationStatement) {
        List<AstNode> children = iterationStatement.getChildren();
        if (children.size() < 2) {
            return null;
        }
        AstNode second = children.get(1);
        if (second.is(CGrammar.STATEMENT)) {
            return second;
        }
        return null;
    }
}