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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.api.CKeyword;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "S1142")
public class TooManyReturnCheck extends CCheck {

  private static final int DEFAULT = 3;
  private final Deque<Integer> returnStatementCounter = new ArrayDeque<>();

  @RuleProperty(key = "max", description = "Maximum allowed return statements per function", defaultValue = ""
      + DEFAULT)
  int max = DEFAULT;

  @Override
  public List<AstNodeType> subscribedTo() {
    // In your CGrammar, FUNCTION_DEF is the rule for function definitions
    // JUMP_STATEMENT contains the return keyword
    return Arrays.asList(CGrammar.FUNCTION_DEF, CGrammar.JUMP_STATEMENT);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    returnStatementCounter.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.FUNCTION_DEF)) {
      // Start counting for a new function context
      returnStatementCounter.push(0);
    } else if (astNode.is(CGrammar.JUMP_STATEMENT) && isReturnStatement(astNode)) {
      // Only increment if we are currently inside a function
      if (!returnStatementCounter.isEmpty()) {
        setReturnStatementCounter(getReturnStatementCounter() + 1);
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CGrammar.FUNCTION_DEF)) {
      int count = getReturnStatementCounter();
      if (count > max) {
        addIssue(
            MessageFormat.format(
                "Reduce the number of returns of this function {0,number,integer}, down to the maximum allowed {1,number,integer}.",
                count,
                max),
            astNode);
      }
      returnStatementCounter.pop();
    }
  }

  private boolean isReturnStatement(AstNode jumpNode) {
    // In your JUMP_STATEMENT rule, return is one of the firstOf options
    return jumpNode.hasDirectChildren(CKeyword.RETURN);
  }

  private int getReturnStatementCounter() {
    return returnStatementCounter.peek();
  }

  private void setReturnStatementCounter(int value) {
    returnStatementCounter.pop();
    returnStatementCounter.push(value);
  }
}
