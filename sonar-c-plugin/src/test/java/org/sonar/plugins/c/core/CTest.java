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
package org.sonar.plugins.c.core;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.c.CPlugin;

import static org.assertj.core.api.Assertions.assertThat;

public class CTest {

  @Test
  public void testGetFileSuffixes() {
    MapSettings settings = new MapSettings();
    Configuration config = new ConfigurationBridge(settings);
    C c = new C(config);

    assertThat(c.getFileSuffixes()).isEqualTo(new String[] { "ccc_m" });

    settings.setProperty(CPlugin.FILE_SUFFIXES_KEY, "");
    assertThat(c.getFileSuffixes()).isEqualTo(new String[] { "ccc_m" });

    settings.setProperty(CPlugin.FILE_SUFFIXES_KEY, "ccc_m");
    assertThat(c.getFileSuffixes()).isEqualTo(new String[] { "ccc_m" });

    settings.setProperty(CPlugin.FILE_SUFFIXES_KEY, ",ccc_m");
    assertThat(c.getFileSuffixes()).isEqualTo(new String[] { "ccc_m" });
  }

}
