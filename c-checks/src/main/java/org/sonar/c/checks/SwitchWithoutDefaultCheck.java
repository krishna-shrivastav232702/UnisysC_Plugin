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

@Rule(key = "S131")
public class SwitchWithoutDefaultCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.CONTROL_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    // Only proceed if the first child of the control statement is 'switch'
    if (!astNode.hasDirectChildren(CKeyword.SWITCH)) {
      return;
    }

    // Find all labeled statements (case X: or default:) within this switch
    List<AstNode> labels = astNode.getDescendants(CGrammar.LABELED_STATEMENT);

    AstNode defaultLabel = null;

    for (AstNode label : labels) {
      if (label.hasDirectChildren(CKeyword.DEFAULT)) {
        defaultLabel = label;
        break;
      }
    }

    if (defaultLabel == null) {
      addIssue("Avoid switch statement without a \"default\" clause.", astNode);
    } else {
      // Check if default is the last labeled statement in the switch
      if (!labels.isEmpty() && labels.get(labels.size() - 1) != defaultLabel) {
        addIssue("The \"default\" clause should be the last one in a \"switch\" statement.", defaultLabel);
      }
    }
  }
}
