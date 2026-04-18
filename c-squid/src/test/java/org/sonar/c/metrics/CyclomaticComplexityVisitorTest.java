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

import com.sonar.sslr.impl.Parser;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sonar.c.CGrammar;
import org.sonar.c.parser.CParser;
import org.sonar.sslr.parser.LexerlessGrammar;

import static org.assertj.core.api.Assertions.assertThat;

public class CyclomaticComplexityVisitorTest {

  private Parser<LexerlessGrammar> parser = CParser.create(StandardCharsets.UTF_8);

  @Test
  public void simple_statement() {
    assertThat(functionComplexity("int main() { a = 1; return 0; }")).isEqualTo(1);
  }

  @Test
  public void and_operator() {
    assertThat(functionComplexity("int main() { a == x && y; return 0; }")).isEqualTo(2);
  }

  @Test
  public void or_operator() {
    assertThat(functionComplexity("int main() { a = x || y; return 0; }")).isEqualTo(2);
  }

  @Test
  public void if_statement() {
    assertThat(functionComplexity("int main() { if (x) { a = 1; } return 0; }")).isEqualTo(2);
    assertThat(functionComplexity("int main() { if (x) { a = 1; } return 0; }")).isEqualTo(2);
  }

  @Test
  public void while_statement() {
    assertThat(functionComplexity("int main() { while(x) { a = 1; } return 0; }")).isEqualTo(2);
    assertThat(functionComplexity("int main() { while(x) { a = 1; } return 0; }")).isEqualTo(2);
  }

  @Test
  public void for_statement() {
    assertThat(functionComplexity("int main() { for (i = 1; i < x; i++) {} return 0; }")).isEqualTo(2);
  }

  @Test
  public void ternary_operator() {
    assertThat(functionComplexity("int main() { int x = a ? b : c; }")).isEqualTo(2);
    assertThat(functionComplexity("int main() { for (i = 1; i < x; i++) {} return 0; }")).isEqualTo(2);
  }

  @Test
  public void function_definition() {
    assertThat(complexity("int f() {}")).isEqualTo(1);
  }

  @Test
  public void nested_logic_check() {
    assertThat(functionComplexity("int f() { if(x) { while(y) {} } }")).isEqualTo(3);
  }

  private int complexity(String source) {
    return CyclomaticComplexityVisitor.complexity(parser.parse(source));
  }

  private int functionComplexity(String source) {
    return CyclomaticComplexityVisitor.functionComplexity(parser.parse(source).getFirstDescendant(CGrammar.FUNCTION_DEF));
  }
}
