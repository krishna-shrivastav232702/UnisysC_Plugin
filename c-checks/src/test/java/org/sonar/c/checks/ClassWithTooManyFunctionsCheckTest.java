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
import org.sonar.c.checks.ClassWithTooManyFunctionsCheck;

public class ClassWithTooManyFunctionsCheckTest {

  private ClassWithTooManyFunctionsCheck check = new ClassWithTooManyFunctionsCheck();

  @Test
  public void defaults() {
    CVerifier.verify(new File("src/test/resources/checks/ClassWithTooManyFunctions.ccc_m"), check);
  }

  @Test
  public void custom_maximum_function_threshold() {
    check.maximumFunctionThreshold = 1;

    CVerifier.verify(new File("src/test/resources/checks/ClassWithTooManyFunctions-1.ccc_m"), check);
  }

  @Test
  public void custom_count_non_publicMethods() {
    check.countNonpublicMethods = false;

    CVerifier.verifyNoIssueIgnoringExpected(new File("src/test/resources/checks/ClassWithTooManyFunctions.ccc_m"), check);
  }

  @Test
  public void custom_both_parameters() {
    check.maximumFunctionThreshold = 1;
    check.countNonpublicMethods = false;

    CVerifier.verify(new File("src/test/resources/checks/ClassWithTooManyFunctions-1-public.ccc_m"), check);
  }
}
