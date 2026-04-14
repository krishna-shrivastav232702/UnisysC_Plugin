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
package org.sonar.c.grammar.expressions;

import org.junit.jupiter.api.Test;
import org.sonar.c.CGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.tests.Assertions;

public class PrimaryExpressionTest {

  private final LexerlessGrammar g = CGrammar.createGrammar();

  @Test
  public void string() {
    Assertions.assertThat(g.rule(CGrammar.PRIMARY_EXPRESSION))
      .matches("\"Sonar source\"");
  }

  @Test
  public void reservedNamespace() {
    Assertions.assertThat(g.rule(CGrammar.PRIMARY_EXPRESSION))
      .matches("internal")
      .matches("private::identifier");
  }

  @Test
  public void emptyArrayInitialiser() {
    Assertions.assertThat(g.rule(CGrammar.PRIMARY_EXPRESSION))
      .matches("[]")
      .matches("[   ]");
  }

  @Test
  public void emptyObjectInitialiser() {
    Assertions.assertThat(g.rule(CGrammar.PRIMARY_EXPRESSION))
      .matches("{}")
      .matches("{   }");
  }

  @Test
  public void filledObjectInitialiser() {
    Assertions.assertThat(g.rule(CGrammar.PRIMARY_EXPRESSION))
      .matches("{ FirstName : \"John\", LastName: \"Smith\"}")
      .matches("{ Age : 43}");
  }

}
