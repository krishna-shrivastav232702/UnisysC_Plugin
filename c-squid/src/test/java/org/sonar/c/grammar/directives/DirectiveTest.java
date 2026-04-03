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
package org.sonar.c.grammar.directives;

import org.junit.jupiter.api.Test;
import org.sonar.c.CGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.tests.Assertions;

public class DirectiveTest {

  private final LexerlessGrammar g = CGrammar.createGrammar();

  @Test
  public void test() {
    Assertions.assertThat(g.rule(CGrammar.DIRECTIVE))
        // .matches(";")
        // .matches("{}")
        // .matches("var a;")
        // .matches("default xml namespace = a")

        // .matches("attribute var a;")
        // .notMatches("attribute \n var a;")

        // .matches("include \"String\";")
        // .matches("include \"String\"")

        // .matches("import a;")
        // .matches("import a")

        // .matches("use namespace a;")
        // .matches("use namespace a")

        // .matches("public namespace ns = \"...\";")

        .matches("CONFIG::debug { }")
        .matches("#include <stdio.h>");
    // .matches("#define PI 3.14")
    // .matches("#ifdef DEBUG")
    // .matches("#endif");
  }

}
