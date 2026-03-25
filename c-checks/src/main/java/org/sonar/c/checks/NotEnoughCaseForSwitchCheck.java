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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.util.Collections;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S1301")
public class NotEnoughCaseForSwitchCheck extends CCheck {

  private static final int MINIMUM_CASE = 3;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.SWITCH_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbCase = 0;
    for (AstNode caseElement : astNode.getChildren(CGrammar.CASE_ELEMENT)) {
      nbCase += caseElement.getChildren(CGrammar.CASE_LABEL).size();
    }

    if (nbCase < MINIMUM_CASE) {
      addIssue("Replace this \"switch\" statement with \"if\" statements to increase readability.", astNode);
    }
  }

}
