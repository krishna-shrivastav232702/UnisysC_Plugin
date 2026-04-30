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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Token;

@Rule(key = "S3972")
public class ConditionalsStartOnNewLineCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.PROGRAM);
    }

    @Override
    public void visitNode(AstNode programNode) {
        List<Token> allTokens = programNode.getTokens();
        if (allTokens == null || allTokens.isEmpty()) {
            return;
        }

        Map<Token, Integer> tokenIndexMap = new HashMap<>();
        for (int i = 0; i < allTokens.size(); i++) {
            tokenIndexMap.put(allTokens.get(i), i);
        }

        List<AstNode> controlStatements = programNode.getDescendants(CGrammar.CONTROL_STATEMENT);

        for (AstNode controlStmt : controlStatements) {
            Token controlToken = controlStmt.getToken(); 
            Integer index = tokenIndexMap.get(controlToken);

            if (index != null && index > 0) {
                Token prevToken = allTokens.get(index - 1);
                
                if (prevToken.getLine() == controlToken.getLine()) {
                    if (!"else".equals(prevToken.getValue())) {
                        addIssue("Conditionals should start on a new line.", controlStmt);
                    }
                }
            }
        }
    }
}