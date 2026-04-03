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
package org.sonar.c.grammar.statements;

import org.junit.jupiter.api.Test;
import org.sonar.c.CGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.tests.Assertions;

public class ForStatementTest {

  private final LexerlessGrammar g = CGrammar.createGrammar();

  @Test
  public void test() {
    // Assertions.assertThat(g.rule(CGrammar.FOR_INITIALISER))
    // // list expression
    // .matches("a = 1, b = 2")
    // // variable definition
    // .matches("var a = 1, b = 2");
    Assertions.assertThat(g.rule(CGrammar.FOR_INIT))
        // expression
        .matches("i = 1");

    // Assertions.assertThat(g.rule(CGrammar.FOR_IN_BINDING))
    // // variable binding
    // .matches("var i")
    // postfix expression
    // .matches("i");

    Assertions.assertThat(g.rule(CGrammar.FOR_STATEMENT))
        // for
        .matches("for ( ; ; ) { }")
        .matches("for (i = 0; i < 5; i++) { }");
  }

}
