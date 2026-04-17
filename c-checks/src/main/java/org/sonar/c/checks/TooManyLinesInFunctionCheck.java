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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.metrics.FileLinesVisitor;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "S138")
public class TooManyLinesInFunctionCheck extends CCheck {

  private static final int DEFAULT = 100;

  @RuleProperty(key = "max", description = "Maximum authorized lines in a function", defaultValue = "" + DEFAULT)
  int max = DEFAULT;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
        CGrammar.FUNCTION_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    FileLinesVisitor linesVisitor = new FileLinesVisitor();
    linesVisitor.scanNode(astNode);
    int nbLines = linesVisitor.linesOfCode().size();
    if (nbLines > max) {
      addIssue(
          MessageFormat.format(
              "This function has {0} lines of code, which is greater than the {1} lines authorized. Split it into smaller functions.",
              nbLines, max),
          astNode);
    }
  }
}
