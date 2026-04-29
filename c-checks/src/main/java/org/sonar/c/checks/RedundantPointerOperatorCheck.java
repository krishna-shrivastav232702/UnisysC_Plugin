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

@Rule(key = "S3491")
public class RedundantPointerOperatorCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.UNARY_EXPR);
    }

    @Override
    public void visitNode(AstNode node) {
        String topOp = getUnaryOperator(node);
        if (topOp == null) return;

        AstNode operand = node.getFirstChild(CGrammar.CAST_EXPRESSION);
        if (operand == null) return;

        AstNode innerUnary = findInnerUnary(operand);

        if (innerUnary != null) {
            String innerOp = getUnaryOperator(innerUnary);
            
            if (("&".equals(topOp) && "*".equals(innerOp)) || 
                ("*".equals(topOp) && "&".equals(innerOp))) {
                addIssue("Redundant pointer operator sequences (&* or *&) should be removed.", node);
            }
        }
    }

    private String getUnaryOperator(AstNode unaryNode) {
        AstNode opNode = unaryNode.getFirstChild(CGrammar.UNARY_OPERATOR);
        if (opNode != null) {
            if (opNode.hasDirectChildren(CPunctuator.AND)) return "&";
            if (opNode.hasDirectChildren(CPunctuator.STAR)) return "*";
        }
        return null;
    }

    private AstNode findInnerUnary(AstNode node) {
        if (node.is(CGrammar.UNARY_EXPR)) {
            return node;
        }
        
        if (node.is(CGrammar.PRIMARY_EXPRESSION) && node.hasDirectChildren(CPunctuator.LPARENTHESIS)) {
            AstNode expr = node.getFirstChild(CGrammar.EXPRESSION);
            if (expr != null) return findInnerUnary(expr);
        }

        for (AstNode child : node.getChildren()) {
            AstNode found = findInnerUnary(child);
            if (found != null) return found;
        }
        
        return null;
    }
}
