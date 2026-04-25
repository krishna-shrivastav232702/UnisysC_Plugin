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
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S5271")
public class NoReturnFunctionShouldBeNoreturnCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.FUNCTION_DEF);
    }

    @Override
    public void visitNode(AstNode functionDef) {

        AstNode declSpecs = functionDef.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
        if (declSpecs == null) {
            return;
        }

        if (isVoidFunction(declSpecs)) {
            return;
        }

        AstNode functionBody = functionDef.getFirstChild(CGrammar.FUNCTION_BODY);
        if (functionBody == null) {
            return;
        }

        if (!hasAnyReturn(functionBody)) {
            AstNode reportNode = getFunctionNameNode(functionDef);
            if (reportNode == null) {
                reportNode = functionDef;
            }
            addIssue(
                "Function \"" + reportNode.getTokenValue() + "\" never returns"
                + " and should be declared as \"noreturn\".",
                reportNode
            );
        }
    }

    private boolean isVoidFunction(AstNode declSpecs) {
        for (AstNode ts : declSpecs.getChildren(CGrammar.TYPE_SPECIFIER)) {
            if ("void".equalsIgnoreCase(ts.getTokenValue())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyReturn(AstNode functionBody) {
        for (AstNode jumpStmt : functionBody.getDescendants(CGrammar.JUMP_STATEMENT)) {
            AstNode firstChild = jumpStmt.getFirstChild();
            if (firstChild != null && firstChild.is(CKeyword.RETURN)) {
                return true;
            }
        }
        return false;
    }

    
    private AstNode getFunctionNameNode(AstNode functionDef) {
        AstNode declarator = functionDef.getFirstChild(CGrammar.DECLARATOR);
        if (declarator == null) {
            return null;
        }
        AstNode directDecl = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
        if (directDecl == null) {
            return null;
        }
        return directDecl.getFirstChild(CGrammar.IDENTIFIER);
    }
}