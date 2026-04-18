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
import com.sonar.sslr.api.Token;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CPunctuator;
import org.sonar.c.checks.utils.Expression;
import org.sonar.check.Rule;

@Rule(key = "S127")
public class VariantStopConditionInForLoopCheck extends CCheck {

  Set<String> counters = new HashSet<>();
  Set<String> pendingCounters = new HashSet<>();

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
      CGrammar.FOR_STATEMENT,
      CGrammar.SUB_STATEMENT,

      CGrammar.ASSIGNMENT_EXPRESSION,
      CPunctuator.DOUBLE_PLUS,
      CPunctuator.DOUBLE_MINUS);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    counters.clear();
    pendingCounters.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.FOR_STATEMENT)) {
      pendingCounters.addAll(getLoopsCounters(astNode));
      checkLoopsCondition(astNode);

    } else if (astNode.is(CGrammar.SUB_STATEMENT) && !pendingCounters.isEmpty()) {
      counters.addAll(pendingCounters);
      pendingCounters.clear();

    } else if (!counters.isEmpty() && astNode.is(CGrammar.ASSIGNMENT_EXPRESSION, CPunctuator.DOUBLE_PLUS, CPunctuator.DOUBLE_MINUS)) {
      checkIfModifyingCounter(astNode);
    }
  }

  private void checkLoopsCondition(AstNode forStatement) {
    AstNode stopConditionExpr = getStopCondition(forStatement);
    if (stopConditionExpr == null) {
      return;
    }

    for (AstNode assignmentExpr : stopConditionExpr.getChildren(CGrammar.ASSIGNMENT_EXPRESSION)) {
      for (Token t : assignmentExpr.getTokens()) {

        String tokenValue = t.getValue();
        if (CPunctuator.LPARENTHESIS.getValue().equals(tokenValue) || CPunctuator.DOT.getValue().equals(tokenValue)) {
          addIssue("Calculate the stop condition value outside the loop and set it to a variable.", assignmentExpr);
          break;
        }
      }
    }
  }

  /**
   * Returns for statement stop condition, null if there is no stop condition.
   */
  @Nullable
  private static AstNode getStopCondition(AstNode forStatement) {
    AstNode semicolonNode = forStatement.getFirstChild(CPunctuator.SEMICOLON);

    if (semicolonNode != null) {
      AstNode stopConditionExpr = semicolonNode.getNextAstNode();
      return stopConditionExpr.is(CGrammar.LIST_EXPRESSION) ? stopConditionExpr : null;
    }
    return null;
  }

  private void checkIfModifyingCounter(AstNode expression) {
    AstNode varNode = null;
    if (expression.is(CGrammar.ASSIGNMENT_EXPRESSION) && expression.hasDirectChildren(CGrammar.ASSIGNMENT_OPERATOR)) {
      varNode = expression.getFirstChild();
    } else if (expression.is(CPunctuator.DOUBLE_PLUS, CPunctuator.DOUBLE_MINUS)) {
      AstNode exprParent = expression.getParent();
      varNode = exprParent.is(CGrammar.UNARY_EXPR) ? exprParent.getLastChild() : exprParent.getFirstChild();
    }

    if (varNode != null) {
      String varName = Expression.exprToString(varNode);
      if (counters.contains(varName)) {
        addIssue(MessageFormat.format("Do not update the loop counter \"{0}\" within the loop body.", varName), varNode);
      }
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CGrammar.FOR_STATEMENT)) {
      counters.removeAll(getLoopsCounters(astNode));
    }
  }

  private static Set<String> getLoopsCounters(AstNode forStatement) {
    Set<String> loopCounters = new HashSet<>();
    AstNode initialiser = forStatement.getFirstChild(CGrammar.FOR_INITIALISER);

    if (initialiser != null) {
      AstNode initialiserExpr = initialiser.getFirstChild();

      if (initialiserExpr.is(CGrammar.VARIABLE_DEF_NO_IN)) {
        getCountersFromVariableDef(loopCounters, initialiserExpr);

      } else {
        getCountersFromListExpression(loopCounters, initialiserExpr);
      }
    }
    return loopCounters;
  }

  private static void getCountersFromListExpression(Set<String> counters, AstNode initialiserExpr) {
    for (AstNode assignmentExpr : initialiserExpr.getChildren(CGrammar.ASSIGNMENT_EXPR_NO_IN)) {
      AstNode exprFirstChild = assignmentExpr.getFirstChild();

      if (assignmentExpr.hasDirectChildren(CGrammar.ASSIGNMENT_OPERATOR)) {
        counters.add(Expression.exprToString(exprFirstChild));
      } else if (exprFirstChild.is(CGrammar.UNARY_EXPR)) {
        counters.add(Expression.exprToString(exprFirstChild.getLastChild()));
      } else if (exprFirstChild.is(CGrammar.POSTFIX_EXPRESSION)) {
        counters.add(Expression.exprToString(exprFirstChild.getFirstChild()));
      }
    }
  }

  private static void getCountersFromVariableDef(Set<String> counters, AstNode initialiserExpr) {
    for (AstNode variableBinding : initialiserExpr.getFirstChild(CGrammar.VARIABLE_BINDING_LIST_NO_IN).getChildren(CGrammar.VARIABLE_BINDING_NO_IN)) {
      counters.add(Expression.exprToString(variableBinding.getFirstChild(CGrammar.TYPED_IDENTIFIER_NO_IN).getFirstChild(CGrammar.IDENTIFIER)));
    }
  }

}
