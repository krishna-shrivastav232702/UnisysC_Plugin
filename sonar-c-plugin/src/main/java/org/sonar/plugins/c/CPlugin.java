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
package org.sonar.plugins.c;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.c.cobertura.CoberturaSensor;
import org.sonar.plugins.c.core.C;

public class CPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.c.file.suffixes";
  public static final String COBERTURA_REPORT_PATHS = "sonar.c.cobertura.reportPaths";

  @Override
  public void define(Context context) {
    context.addExtensions(
      C.class,

      CSquidSensor.class,
      CoberturaSensor.class,

      CRulesDefinition.class,
      CProfile.class,

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(C.DEFAULT_FILE_SUFFIXES)
        .name("File suffixes")
        .description("List of suffixes for files to analyze. To not filter, leave the list empty.")
        .onConfigScopes(PropertyDefinition.ConfigScope.MODULE, PropertyDefinition.ConfigScope.PROJECT)
        .category(C.NAME)
        .multiValues(true)
        .build(),

      PropertyDefinition.builder(COBERTURA_REPORT_PATHS)
        .deprecatedKey("sonar.c.cobertura.reportPath")
        .name("Cobertura xml report paths")
        .description("Comma separated list of paths to the Cobertura coverage report file. The paths may be either absolute or relative to the project base directory.")
        .onConfigScopes(PropertyDefinition.ConfigScope.MODULE, PropertyDefinition.ConfigScope.PROJECT)
        .category(C.NAME)
        .multiValues(true)
        .build());
  }
}
