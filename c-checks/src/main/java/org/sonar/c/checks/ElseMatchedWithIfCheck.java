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
import org.sonar.c.CKeyword;
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S5261")
public class ElseMatchedWithIfCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.CONTROL_STATEMENT);
    }

    @Override
    public void visitNode(AstNode controlStatement) {
        AstNode firstChild = controlStatement.getFirstChild();
        if (firstChild == null || !firstChild.is(CKeyword.IF)) {
            return;
        }

        if (hasElse(controlStatement)) {
            return;
        }

        AstNode ifBody = getIfBody(controlStatement);
        if (ifBody == null) {
            return;
        }

        AstNode actualBody = unwrapStatement(ifBody);
        if (actualBody == null) {
            return;
        }

        if (actualBody.is(CGrammar.COMPOUND_STATEMENT)) {
            return;
        }

        if (actualBody.is(CGrammar.CONTROL_STATEMENT)) {
            AstNode innerFirst = actualBody.getFirstChild();
            if (innerFirst != null && innerFirst.is(CKeyword.IF)
                    && hasElse(actualBody)) {
                AstNode elseKeyword = getElseKeyword(actualBody);
                if (elseKeyword != null) {
                    addIssue(
                        "This \"else\" is ambiguously matched with the"
                        + " inner \"if\" — use braces to clarify intent.",
                        elseKeyword
                    );
                }
            }
        }
    }

    private boolean hasElse(AstNode controlStatement) {
        for (AstNode child : controlStatement.getChildren()) {
            if (child.is(CKeyword.ELSE)) {
                return true;
            }
        }
        return false;
    }

    private AstNode getIfBody(AstNode controlStatement) {
        boolean pastRParen = false;
        for (AstNode child : controlStatement.getChildren()) {
            if (child.is(CKeyword.ELSE)) {
                break;
            }
            if (child.is(CPunctuator.RPARENTHESIS)) {
                pastRParen = true;
                continue;
            }
            if (pastRParen && child.is(CGrammar.STATEMENT)) {
                return child;
            }
        }
        return null;
    }

    private AstNode getElseKeyword(AstNode controlStatement) {
        for (AstNode child : controlStatement.getChildren()) {
            if (child.is(CKeyword.ELSE)) {
                return child;
            }
        }
        return null;
    }

    private AstNode unwrapStatement(AstNode node) {
        if (node == null) {
            return null;
        }
        List<AstNode> children = node.getChildren();
        if (children.size() == 1) {
            return children.get(0);
        }
        return node;
    }
}