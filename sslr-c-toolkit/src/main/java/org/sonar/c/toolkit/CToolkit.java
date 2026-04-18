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
package org.sonar.c.toolkit;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonar.c.api.CKeyword;
import org.sonar.c.parser.CParser;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.JavadocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.StringTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.sslr.toolkit.Toolkit;

public final class CToolkit {

  public static final String SPAN_END_TAG = "</span>";

  private CToolkit() {
  }

  public static void main(String[] args) {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SSDK");
    new Toolkit(CParser.create(Charset.defaultCharset()), getTokenizers(), "SSLR Unisys C Toolkit").run();
  }

  // Visible for testing
  static List<Tokenizer> getTokenizers() {
    return Collections.unmodifiableList(Arrays.asList(
      new StringTokenizer("<span class=\"s\">", SPAN_END_TAG),
      new CDocTokenizer("<span class=\"cd\">", SPAN_END_TAG),
      new JavadocTokenizer("<span class=\"cppd\">", SPAN_END_TAG),
      new CppDocTokenizer("<span class=\"cppd\">", SPAN_END_TAG),
      new KeywordsTokenizer("<span class=\"k\">", SPAN_END_TAG, CKeyword.keywordValues())));
  }

}
