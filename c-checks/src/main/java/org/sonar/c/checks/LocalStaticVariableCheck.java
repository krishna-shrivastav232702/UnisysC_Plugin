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
import com.sonar.sslr.api.Token;

@Rule(key = "M23_233")
public class LocalStaticVariableCheck extends CCheck {

@Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.DECLARATION);
    }

    @Override
    public void visitNode(AstNode node) {
        if (node.hasAncestor(CGrammar.FUNCTION_DEF)) {
            
            AstNode specifiers = node.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
            if (specifiers != null) {
                
                boolean isStatic = false;
                boolean isConst = false;

                for (Token token : specifiers.getTokens()) {
                    String value = token.getValue();
                    if ("static".equals(value)) {
                        isStatic = true;
                    }
                    if ("const".equals(value)) {
                        isConst = true;
                    }
                }

                if (isStatic && !isConst) {
                    addIssue("Local variables shall not have static storage duration unless they are const.", node);
                }
            }
        }
    }
}