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
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "M23_037")
public class NoLowercaseLSuffixCheck extends CCheck {
    
    @Override
    public List<AstNodeType> subscribedTo() {
        return Arrays.asList(CGrammar.INTEGER_SUFFIX, CGrammar.FLOATING_SUFFIX);
    }

    @Override
    public void visitNode(AstNode node) {
        if (node.is(CGrammar.INTEGER_SUFFIX)) {
            if (node.getFirstChild(CGrammar.LONG_SUFFIX) != null
                    && node.getTokenValue().charAt(0) == 'l') {
                addIssue("The lowercase form of 'L' shall not be used as the first character in a literal suffix", node);
            }
        } else {
            // FLOATING_SUFFIX
            if (node.getTokenValue().charAt(0) == 'l') {
                addIssue("The lowercase form of 'L' shall not be used as the first character in a literal suffix", node);
            }
        }
    }
}
