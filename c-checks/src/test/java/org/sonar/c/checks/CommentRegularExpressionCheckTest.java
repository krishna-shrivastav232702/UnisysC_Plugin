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
package org.sonar.c.checks;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.c.checks.CommentRegularExpressionCheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CommentRegularExpressionCheckTest {

  @Test
  public void test() {
    CommentRegularExpressionCheck check = new CommentRegularExpressionCheck();

    check.regularExpression = "(?i).*TODO.*";
    check.message = "Avoid TODO";

    CVerifier.verify(new File("src/test/resources/checks/CommentRegularExpression.ccc_m"), check);
  }

  @Test
  public void test_default_regex() {
    CVerifier.verifyNoIssue(new File("src/test/resources/checks/CommentRegularExpressionDefault.ccc_m"), new CommentRegularExpressionCheck());
  }

  @Test
  public void test_bad_regex() {
    CommentRegularExpressionCheck check = new CommentRegularExpressionCheck();

    check.regularExpression = "[a-z";
    check.message = "Avoid TODO";

    final File file = new File("src/test/resources/checks/CommentRegularExpression.ccc_m");
    RuntimeException e = assertThrows(RuntimeException.class, () -> CVerifier.verify(file, check));
    assertEquals("Unable to compile regular expression: [a-z", e.getMessage());
  }

}
