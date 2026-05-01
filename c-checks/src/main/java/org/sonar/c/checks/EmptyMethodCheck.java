/*
 * SonarQube Unisys C Plugin
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

@Rule(key = "S1186")
public class EmptyMethodCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.FUNCTION_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode functionBody = astNode.getFirstChild(CGrammar.FUNCTION_BODY);
    if (functionBody == null) {
      return;
    }
    AstNode compoundStatement = functionBody.getFirstChild(CGrammar.COMPOUND_STATEMENT);
    if (compoundStatement != null && isEmpty(compoundStatement)) {
      addIssue(
          "Add a nested comment explaining why this method is empty, throw an NotSupportedException or complete the implementation.",
          astNode);
    }
  }

  private static boolean isEmpty(AstNode compoundStatement) {
    AstNode declarationList = compoundStatement.getFirstChild(CGrammar.DECLARATION_LIST);
    AstNode statementList = compoundStatement.getFirstChild(CGrammar.STATEMENT_LIST);
    return (declarationList == null || !declarationList.hasChildren())
        && (statementList == null || !statementList.hasChildren());
  }

}