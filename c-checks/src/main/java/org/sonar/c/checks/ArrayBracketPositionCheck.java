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
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S3729")
public class ArrayBracketPositionCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.POSTFIX_EXPRESSION);
    }

    @Override
    public void visitNode(AstNode node) {
        if (node.hasDirectChildren(CPunctuator.LBRAKET)) {
            
            AstNode base = node.getFirstChild();
            AstNode index = node.getFirstChild(CGrammar.EXPRESSION);

            if (base != null && index != null) {
                if (isConstant(base) && isName(index)) {
                    addIssue("Array indices should be placed between brackets, not the array name.", node);
                }
            }
        }
    }

    private boolean isConstant(AstNode node) {
        return node.hasDescendant(CGrammar.I_CONSTANT);
    }

    private boolean isName(AstNode node) {
        return node.hasDescendant(CGrammar.IDENTIFIER);
    }
}