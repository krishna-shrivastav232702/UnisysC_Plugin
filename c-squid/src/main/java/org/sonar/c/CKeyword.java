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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.sonar.sslr.grammar.GrammarRuleKey;

public enum CKeyword implements GrammarRuleKey {

  /**
   * "as" is not keyword in ActionScript 2, so we treat it as syntactic keyword
   */
  AS(true),
  ASM,
  AUTO,
  BREAK,
  CASE,
  CATCH,
  CHAR,
  CLASS,
  CONST,
  CONTINUE,
  DEFAULT,
  DELETE,
  DO,
  DOUBLE,
  ELSE,
  ENUM,
  EXTENDS,
  EXTERN,
  FALSE,
  __FAR,
  FINALLY,
  FLOAT,
  FOR,
  FUNCTION,
  GOTO,
  IF,
  IMPLEMENTS,
  IMPORT,
  IN,
  INLINE,
  INSTANCEOF,
  INT,
  INTERFACE,
  INTERNAL,
  IS,
  LONG,
  __NEAR,
  NEW,
  NULL,
  PACKAGE,
  PRIVATE,
  PROTECTED,
  PUBLIC,
  REGISTER,
  RETURN,
  SHORT,
  SIGNED,
  SIZEOF,
  __STACK_NUMBER__,
  STRUCT,
  SUPER,
  SWITCH,
  THIS,
  THROW,
  TRUE,
  TRY,
  TYPEDEF,
  TYPEOF,
  UNION,
  UNSIGNED,
  USE,
  __USER_LOCK__,
  __USER_UNLOCK__,
  VAR,
  VOID,
  VOLATILE,
  WHILE,
  WITH,

  EACH(true),
  GET(true),
  SET(true),
  NAMESPACE(true),
  INCLUDE(true),
  DYNAMIC(true),
  FINAL(true),
  // "native" strangely appears in both the "keywords" and "syntactic keywords" lists of the language spec
  // It seems that "native" is accepted as a valid identifier by the compiler and should be considered as a "syntactic keyword"
  NATIVE(true),
  OVERRIDE(true),
  STATIC(true),
  XML(true);

  private final boolean syntactic;

  CKeyword() {
    this(false);
  }

  CKeyword(boolean syntactic) {
    this.syntactic = syntactic;
  }

  public static String[] keywordValues() {
    String[] keywordsValue = new String[CKeyword.values().length];
    int i = 0;
    for (CKeyword keyword : CKeyword.values()) {
      keywordsValue[i] = keyword.getValue();
      i++;
    }
    return keywordsValue;
  }

  public static List<CKeyword> keywords() {
    return Collections.unmodifiableList(Arrays.stream(values())
      .filter(CKeyword -> !CKeyword.syntactic)
      .collect(Collectors.toList()));
  }

  public String getValue() {
    return toString().toLowerCase(Locale.ENGLISH);
  }

}