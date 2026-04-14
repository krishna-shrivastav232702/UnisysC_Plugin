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
import org.sonar.c.checks.utils.Clazz;
import org.sonar.c.checks.utils.Function;
import org.sonar.check.Rule;

@Rule(key = "S1467")
public class ConstructorCallsDispatchEventCheck extends CCheck {

  boolean isInClass;
  private Deque<ClassState> classStack = new ArrayDeque<>();

  private static class ClassState {
    String className;
    boolean isInConstructor;

    public ClassState(String className) {
      this.className = className;
    }
  }

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
      CGrammar.CLASS_DEF,
      CGrammar.FUNCTION_DEF,
      CGrammar.PRIMARY_EXPRESSION);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    classStack.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.CLASS_DEF)) {
      isInClass = true;
      String className = Clazz.getName(astNode);
      classStack.push(new ClassState(className));
    } else if (isConstructor(astNode)) {
      classStack.peek().isInConstructor = true;
    } else if (isCallToDispatchEventInConstructor(astNode)) {
      addIssue(MessageFormat.format("Remove this event dispatch from the \"{0}\" constructor", classStack.peek().className), astNode);
    }
  }

  private boolean isConstructor(AstNode astNode) {
    return isInClass && astNode.is(CGrammar.FUNCTION_DEF) && Function.isConstructor(astNode, classStack.peek().className);
  }

  private boolean isCallToDispatchEventInConstructor(AstNode astNode) {
    return isInClass && classStack.peek().isInConstructor && astNode.is(CGrammar.PRIMARY_EXPRESSION) && isCallToDispatchEvent(astNode);
  }

  private static boolean isCallToDispatchEvent(AstNode primaryExpr) {
    return "dispatchEvent".equals(primaryExpr.getTokenValue())
      && primaryExpr.getNextAstNode().is(CGrammar.ARGUMENTS)
      && primaryExpr.getNextAstNode().getFirstChild(CGrammar.LIST_EXPRESSION) != null;
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (isInClass && classStack.peek().isInConstructor && astNode.is(CGrammar.FUNCTION_DEF)) {
      classStack.peek().isInConstructor = false;
    } else if (isInClass && astNode.is(CGrammar.CLASS_DEF)) {
      classStack.pop();
      isInClass = !classStack.isEmpty();
    }
  }
}
