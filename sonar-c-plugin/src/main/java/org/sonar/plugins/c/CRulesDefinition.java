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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.c.checks.CheckList;
import org.sonar.plugins.c.core.C;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public final class CRulesDefinition implements RulesDefinition {

  private static final String REPOSITORY_NAME = "Unisys_C_Analyzer";
  private static final String RESOURCE_BASE_PATH = "org/sonar/l10n/c/rules/c";
  private static final Set<String> TEMPLATE_RULE_KEYS = new HashSet<>();

  private final SonarRuntime sonarRuntime;

  public CRulesDefinition(SonarRuntime sonarRuntime) {
    this.sonarRuntime = sonarRuntime;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckList.REPOSITORY_KEY, C.KEY)
      .setName(REPOSITORY_NAME);

    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(RESOURCE_BASE_PATH, CProfile.SONAR_WAY_PROFILE_PATH, sonarRuntime);
    ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckList.getChecks());

    TEMPLATE_RULE_KEYS.forEach(key -> repository.rule(key).setTemplate(true));

    repository.done();
  }
}
