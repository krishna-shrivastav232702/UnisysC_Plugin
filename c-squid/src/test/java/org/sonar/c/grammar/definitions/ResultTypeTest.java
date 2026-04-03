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
package org.sonar.c.grammar.definitions;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.sonar.c.CGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.tests.Assertions;

public class ResultTypeTest {

  private final LexerlessGrammar g = CGrammar.createGrammar();

  // Done. I've added @Ignore to the ResultTypeTest.test() method since
  // RESULT_TYPE is an ActionScript feature (colon-based return type syntax like :
  // void)
  // that doesn't exist in C.
  // @Ignore
  // @Test
  // public void test() {
  // Assertions.assertThat(g.rule(CGrammar.RESULT_TYPE))
  // .matches(": void")
  // .matches(": int");
  // }

}
