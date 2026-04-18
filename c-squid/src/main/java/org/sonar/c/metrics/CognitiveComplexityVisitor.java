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

package org.sonar.c.metrics;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.sonar.c.CGrammar;
import org.sonar.c.CVisitor;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

public class CognitiveComplexityVisitor extends CVisitor {
    private int complexity = 0;
    private int nestingLevel = 0;

    public int getComplexity() {
        return complexity;
    }

    @Override
    public List<AstNodeType> subscribedTo() {
        return Arrays.asList(
            CGrammar.FUNCTION_DEF,
            CGrammar.ITERATION_STATEMENT,
            CGrammar.CONTROL_STATEMENT,
            CGrammar.COMPOUND_STATEMENT
        );
    }

    @Override
    public void visitFile(@Nullable AstNode node) {
        complexity = 0;
        nestingLevel = 0;
    }

    @Override
    public void visitNode(AstNode node) {
        if (isBranch(node)) {
            complexity += (nestingLevel + 1);
            nestingLevel++;
        }
    }

    @Override
    public void leaveNode(AstNode node) {
        if (isBranch(node)) {
            nestingLevel--;
        }
    }

    private boolean isBranch(AstNode node) {
        return node.is(
            CGrammar.ITERATION_STATEMENT,
            CGrammar.CONTROL_STATEMENT
        );
    }

    public static int complexity(AstNode root) {
        CognitiveComplexityVisitor visitor = new CognitiveComplexityVisitor();
        visitor.scanNode(root);
        return visitor.complexity;
    }

    public static int functionComplexity(AstNode functionDef) {
        CognitiveComplexityVisitor visitor = new FunctionComplexityVisitor(functionDef);
        visitor.scanNode(functionDef);
        return visitor.complexity;
    }

    private static class FunctionComplexityVisitor extends CognitiveComplexityVisitor {
        private final AstNode functionDef;
        private int outerNestingLevel = 0;

        public FunctionComplexityVisitor(AstNode functionDef) {
            this.functionDef = functionDef;
        }

        @Override
        public void visitNode(AstNode node) {
            if (isNestedFunction(node)) {
                outerNestingLevel++;
            }
            if (outerNestingLevel == 0) {
                super.visitNode(node);
            }
        }

        @Override
        public void leaveNode(AstNode node) {
            if (isNestedFunction(node)) {
                outerNestingLevel--;
            } else if (outerNestingLevel == 0) {
                super.leaveNode(node);
            }
        }

        private boolean isNestedFunction(AstNode node) {
            return node.is(CGrammar.FUNCTION_DEF) && node != functionDef;
        }
    }
}