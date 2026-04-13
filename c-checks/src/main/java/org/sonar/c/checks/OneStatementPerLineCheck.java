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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S122")
public class OneStatementPerLineCheck extends CCheck {

  private final Map<Integer, Integer> statementsPerLine = new HashMap<>();

  @Override
  public List<AstNodeType> subscribedTo() {
    // In CGrammar.java, we need to track both general statements and C-style
    // declarations
    return Arrays.asList(CGrammar.STATEMENT, CGrammar.DECLARATION);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    statementsPerLine.clear();
  }

  @Override
  public void visitNode(AstNode node) {
    if (!isExcluded(node)) {
      int line = node.getTokenLine();
      statementsPerLine.compute(line, (k, v) -> v == null ? 1 : (v + 1));
    }
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    for (Map.Entry<Integer, Integer> entry : statementsPerLine.entrySet()) {
      if (entry.getValue() > 1) {
        addIssueAtLine(
            MessageFormat.format(
                "At most one statement is allowed per line, but {0} statements were found on this line.",
                entry.getValue()),
            entry.getKey());
      }
    }
  }

  private boolean isExcluded(AstNode node) {
    // 1. Exclude Compound Statements (Blocks) as they wrap other statements
    if (node.is(CGrammar.COMPOUND_STATEMENT) || node.hasDirectChildren(CGrammar.COMPOUND_STATEMENT)) {
      return true;
    }

    // 2. Exclude Labeled statements and empty semicolons
    if (node.is(CGrammar.LABELED_STATEMENT) || node.is(CGrammar.EMPTY_STATEMENT)) {
      return true;
    }

    // 3. Prevent double-counting: If a STATEMENT node is just a wrapper for
    // a more specific node we are already tracking (like another STATEMENT), skip
    // it.
    if (node.is(CGrammar.STATEMENT) && node.getNumberOfChildren() == 1) {
      AstNodeType childType = node.getFirstChild().getType();
      if (childType.equals(CGrammar.STATEMENT) || childType.equals(CGrammar.COMPOUND_STATEMENT)) {
        return true;
      }
    }

    return false;
  }
}