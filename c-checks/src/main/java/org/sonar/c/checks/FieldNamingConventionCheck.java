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
import java.util.regex.Pattern;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S116")
public class FieldNamingConventionCheck extends CCheck {

    private static final String DEFAULT_REGEX = "^[a-z][a-zA-Z0-9]*$";
    private final Pattern pattern = Pattern.compile(DEFAULT_REGEX);

    private static final String MESSAGE =
        "Field name does not comply with naming convention.";

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.STRUCT_DECLARATOR);
    }

    @Override
    public void visitNode(AstNode node) {

        AstNode identifier = node.getFirstDescendant(CGrammar.IDENTIFIER);

        if (identifier != null) {
            String name = identifier.getTokenValue();

            if (!pattern.matcher(name).matches()) {
                addIssue(MESSAGE, identifier);
            }
        }
    }
}