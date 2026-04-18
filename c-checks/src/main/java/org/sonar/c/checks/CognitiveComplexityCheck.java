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
import org.sonar.c.metrics.CognitiveComplexityVisitor;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S3776")
public class CognitiveComplexityCheck extends CCheck {
    
    private static final int DEFAULT_MAX = 15;

    @RuleProperty(
        key = "threshold",
        description = "The maximum authorized cognitive complexity.",
        defaultValue = "" + DEFAULT_MAX
    )
    private int threshold = DEFAULT_MAX;

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.FUNCTION_DEF);
    }

    @Override
    public void visitNode(AstNode node) {
        int functionComplexity = CognitiveComplexityVisitor.complexity(node);
        if (functionComplexity > threshold) {
            String message = String.format("Function has a complexity of %s which is greater than %s authorized.",
            functionComplexity, threshold);
            addIssueWithCost(message, node, (double) functionComplexity - threshold);
        }
    }

    public void setMaximumCognitiveComplexityThreshold(int threshold) {
        this.threshold = threshold;
    }

}
