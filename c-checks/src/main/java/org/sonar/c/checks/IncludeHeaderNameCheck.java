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

@Rule(key = "M23_208")
public class IncludeHeaderNameCheck extends CCheck {

    private static final String MESSAGE =
        "The header file name shall not contain invalid characters or sequences.";

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.INCLUDE_DIRECTIVE);
    }

    @Override
    public void visitNode(AstNode node) {

        String header = null;

        AstNode stringNode = node.getFirstChild(CGrammar.STRING);
        if (stringNode != null) {
            header = stringNode.getTokenValue();
            header = header.substring(1, header.length() - 1); // remove quotes
        }

        if (header == null) {
            List<AstNode> tokens = node.getChildren();

            boolean foundLT = false;
            for (AstNode child : tokens) {
                if ("<".equals(child.getTokenValue())) {
                    foundLT = true;
                    continue;
                }
                if (foundLT && !">".equals(child.getTokenValue())) {
                    header = child.getTokenValue();
                    break;
                }
            }
        }

        if (header == null) {
            return;
        }

        if (header.contains("'") ||
            header.contains("\"") ||
            header.contains("\\") ||
            header.contains("/*")) {

            addIssue(MESSAGE, node);
        }
    }
}