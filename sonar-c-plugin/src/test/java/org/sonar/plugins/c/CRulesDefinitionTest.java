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

import org.junit.jupiter.api.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.utils.Version;
import org.sonar.c.checks.CheckList;
import org.sonar.plugins.c.CRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

public class CRulesDefinitionTest {

  private static final SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarLint(Version.create(9, 3));

  @Test
  public void test() {
    CRulesDefinition rulesDefinition = new CRulesDefinition(sonarRuntime);
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("c");

    assertThat(repository.name()).isEqualTo("Sonar");
    assertThat(repository.language()).isEqualTo("c");
    assertThat(repository.rules()).hasSize(CheckList.getChecks().size());

    Rule functionComplexityRule = repository.rule("FunctionComplexity");
    assertThat(functionComplexityRule).isNotNull();
    assertThat(functionComplexityRule.name()).isEqualTo("Cyclomatic Complexity of functions should not be too high");

    for (Rule rule : repository.rules()) {
      for (RulesDefinition.Param param : rule.params()) {
        assertThat(param.description()).as("description for " + param.key() + " of " + rule.key()).isNotEmpty();
      }
    }

    assertThat(repository.rules().stream().filter(Rule::template))
      .extracting(Rule::key)
      .containsOnly("XPath", "CommentRegularExpression");
  }
}
