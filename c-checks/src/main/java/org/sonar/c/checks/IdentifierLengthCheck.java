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

@Rule(key = "S799")
public class IdentifierLengthCheck extends CCheck {

    private static final int MAX_LENGTH = 31;

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.IDENTIFIER);
    }

    @Override
    public void visitNode(AstNode identifierNode) {
        String name = identifierNode.getTokenValue();
        
        if (name != null && name.length() > MAX_LENGTH) {
            addIssue(
                "Identifier \"" + name + "\" is " + name.length() + 
                " characters long. Identifiers should not be longer than " + MAX_LENGTH + " characters.", 
                identifierNode
            );
        }
    }
}