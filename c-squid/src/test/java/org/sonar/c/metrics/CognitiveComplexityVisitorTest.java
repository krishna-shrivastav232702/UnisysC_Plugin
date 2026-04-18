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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.sonar.c.CGrammar;
import org.sonar.c.parser.CParser;
import org.sonar.sslr.parser.LexerlessGrammar;

import com.sonar.sslr.impl.Parser;

public class CognitiveComplexityVisitorTest {
    private Parser<LexerlessGrammar> parser = CParser.create(StandardCharsets.UTF_8);

    @Test
    public void simple_statement() {
        assertThat(functionComplexity("int main() { a = 1; return 0; }")).isEqualTo(0);
    }

    @Test
    public void if_statement() {
        assertThat(functionComplexity("int main() { if (x) { a = 1; } return 0; }")).isEqualTo(1);
    }

    @Test
    public void while_statement() {
        assertThat(functionComplexity("int main() { while(x) { a = 1; } return 0; }")).isEqualTo(1);
    }

    @Test
    public void for_statement() {
        assertThat(functionComplexity("int main() { for (i = 1; i < x; i++) {} return 0; }")).isEqualTo(1);
    }

    @Test
    public void nested_if_in_while() {
        assertThat(functionComplexity("int main() { while(x) { if(y) { a = 1; } } return 0; }")).isEqualTo(3);
    }

    @Test
    public void nested_if_in_if() {
        assertThat(functionComplexity("int main() { if(x) { if(y) { a = 1; } } return 0; }")).isEqualTo(3);
    }

    @Test
    public void deeply_nested() {
        assertThat(functionComplexity("int main() { if(x) { while(y) { for(;;) {} } } }")).isEqualTo(6);
    }

    @Test
    public void sibling_branches_not_cumulative() {
        assertThat(functionComplexity("int main() { if(x) { a = 1; } if(y) { b = 2; } return 0; }")).isEqualTo(2);
    }

    @Test
    public void nested_function_excluded() {
        assertThat(functionComplexity("int f() { if(x) { } }")).isEqualTo(1);
    }

    @Test
    public void whole_file_complexity() {
        assertThat(complexity("int f() { if(x) {} } int g() { while(y) {} }")).isEqualTo(2);
    }

    private int complexity(String source) {
        return CognitiveComplexityVisitor.complexity(parser.parse(source));
    }

    private int functionComplexity(String source) {
        return CognitiveComplexityVisitor.functionComplexity(
            parser.parse(source).getFirstDescendant(CGrammar.FUNCTION_DEF)
        );
    }
}
