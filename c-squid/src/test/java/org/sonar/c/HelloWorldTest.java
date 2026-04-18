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
package org.sonar.c;

import org.junit.jupiter.api.Test;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.tests.Assertions;

public class HelloWorldTest {

  private final LexerlessGrammar g = CGrammar.createGrammar();

  @Test
  public void testUnisysCProgram() {
    Assertions.assertThat(g.rule(CGrammar.PROGRAM))
      .matches("#include <stdio.h>\n"
            + "#include \"unisys_lib.h\"\n"
            + "\n"
            + "void hello() {\n"
            + "    printf(\"Hello Unisys C\");\n"
            + "}\n"
            + "\n"
            + "int main() {\n"
            + "    hello();\n"
            + "    return 0;\n"
            + "}\n"
            + " ");
  }

  @Test
  public void testEmptyProgram() {
    Assertions.assertThat(g.rule(CGrammar.PROGRAM))
      .matches("   "); 
  }
}
