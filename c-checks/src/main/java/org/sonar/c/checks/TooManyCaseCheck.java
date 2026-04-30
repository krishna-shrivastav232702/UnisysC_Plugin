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
import org.sonar.check.RuleProperty;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S1479")
public class TooManyCaseCheck extends CCheck {

    private static final int MAX_CASES = 30;

    @RuleProperty(key = "max", description = "Maximum allowed fields in a structure", defaultValue = "" + MAX_CASES)
    int max = MAX_CASES;

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.CONTROL_STATEMENT);
    }
    @Override
    public void visitNode(AstNode node) {
        boolean isSwitch = node.getChildren().stream()
                               .anyMatch(child -> child.is(CKeyword.SWITCH));
    
        if (isSwitch) {
            AstNode compoundStatement = node.getFirstDescendant(CGrammar.COMPOUND_STATEMENT);
            
            if (compoundStatement != null) {
                List<AstNode> cases = compoundStatement.getDescendants(CKeyword.CASE);
                
                if (cases.size() > max) {
                    addIssue(
                        String.format("This 'switch' statement has %d 'case' clauses, which exceeds the maximum of %d.", 
                        cases.size(), max), 
                        node
                    );
                }
            }
        }
    }
}

