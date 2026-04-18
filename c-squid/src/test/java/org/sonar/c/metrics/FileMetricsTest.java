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
package org.sonar.c.metrics;

import java.io.File;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.c.TestVisitorContext;
import org.sonar.c.metrics.FileMetrics;

import static org.fest.assertions.Assertions.assertThat;

public class FileMetricsTest {

  @Test
  public void comments() {
    assertThat(metrics("comments.as").commentLines()).containsOnly(2, 6, 10);
    assertThat(metrics("comments.as").nosonarLines()).containsOnly(12);
  }

  @Test
  public void lines_of_code() {
    assertThat(metrics("lines_of_code.as").linesOfCode()).containsOnly(9, 14, 15);
  }

  @Test
  public void statements() {
    assertThat(metrics("statements.as").numberOfStatements()).isEqualTo(9);
  }

  @Test
  public void executable_lines() {
    assertThat(metrics("statements.as").executableLines())
        .isEqualTo("3=1;6=1;10=1;14=1;22=1;24=1;27=1;29=1;");
  }

  @Test
  public void functions() {
    assertThat(metrics("functions.as").numberOfFunctions()).isEqualTo(3);
  }

  @Disabled
  @Test
  public void classes() {
    assertThat(metrics("classes.as").numberOfClasses()).isEqualTo(0);
  }

  private FileMetrics metrics(String fileName) {
    File baseDir = new File("src/test/resources/metrics/");
    File file = new File(baseDir, fileName);
    return new FileMetrics(TestVisitorContext.create(file));
  }

}
