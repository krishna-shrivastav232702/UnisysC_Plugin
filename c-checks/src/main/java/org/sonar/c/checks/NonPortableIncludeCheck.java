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

@Rule(key = "S4263")
public class NonPortableIncludeCheck extends CCheck {

    private static final String MESSAGE =
        "Replace the quoted include with an angle-bracket include \"<...>\" "
        + "to avoid relying on a non-portable search strategy.";

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.INCLUDE_DIRECTIVE);
    }

    @Override
    public void visitNode(AstNode includeDirective) {
        AstNode stringNode = includeDirective.getFirstChild(CGrammar.STRING);
        if (stringNode != null) {
            addIssue(MESSAGE, stringNode);
        }
    }
}