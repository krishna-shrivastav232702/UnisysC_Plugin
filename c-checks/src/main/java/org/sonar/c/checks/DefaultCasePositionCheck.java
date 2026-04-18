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
import org.sonar.c.CKeyword;
import org.sonar.check.Rule;

@Rule(key = "S4524")
public class DefaultCasePositionCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.SWITCH_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode previous = null;
    int nbCase = 0;
    for (AstNode caseElement : astNode.getChildren(CGrammar.CASE_ELEMENT)) {
      for (AstNode caseLabel : caseElement.getChildren(CGrammar.CASE_LABEL)) {
        if (previous != null && nbCase > 1 && previous.getFirstChild().is(CKeyword.DEFAULT)) {
          addIssue("Move this \"default\" clause to the beginning or end of this \"switch\" statement.", previous);
          return;
        }
        previous = caseLabel;
        ++nbCase;
      }
    }
  }

}
