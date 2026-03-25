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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.c.checks.CheckList;
import org.sonar.plugins.c.core.C;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class CProfile implements BuiltInQualityProfilesDefinition {

  public static final String SONAR_WAY_PROFILE_PATH = "org/sonar/l10n/c/rules/c/Sonar_way_profile.json";

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(CheckList.SONAR_WAY_PROFILE, C.KEY);
    BuiltInQualityProfileJsonLoader.load(profile, CheckList.REPOSITORY_KEY, SONAR_WAY_PROFILE_PATH);
    profile.done();
  }
}
