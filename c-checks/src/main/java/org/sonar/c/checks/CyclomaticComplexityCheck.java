/*
 * SonarQube Flex Plugin
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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.util.Arrays;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.metrics.CyclomaticComplexityVisitor;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "S1541")
public class CyclomaticComplexityCheck extends CCheck {

  private static final int DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD = 10;

  @RuleProperty(
    key = "maximumFunctionComplexityThreshold",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD)
  private int maximumFunctionComplexityThreshold = DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(CGrammar.FUNCTION_DEF);
  }

  @Override
  public void visitNode(AstNode node) {
    int functionComplexity = CyclomaticComplexityVisitor.functionComplexity(node);
    if (functionComplexity > maximumFunctionComplexityThreshold) {
      String message = String.format("Function has a complexity of %s which is greater than %s authorized.", functionComplexity, maximumFunctionComplexityThreshold);
      addIssueWithCost(message, node, (double)functionComplexity - maximumFunctionComplexityThreshold);
    }
  }

  public void setMaximumFunctionComplexityThreshold(int threshold) {
    this.maximumFunctionComplexityThreshold = threshold;
  }

}