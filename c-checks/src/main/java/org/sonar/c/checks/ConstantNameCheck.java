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
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.checks.utils.Variable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;


@Rule(key = "S115")
public class ConstantNameCheck extends CCheck {

  private static final String DEFAULT = "^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the constant names against",
    defaultValue = DEFAULT)
  String format = DEFAULT;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.VARIABLE_DECLARATION_STATEMENT);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    if (pattern == null) {
      pattern = Pattern.compile(format);
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.getFirstChild(CGrammar.VARIABLE_DEF).getFirstChild(CGrammar.VARIABLE_DEF_KIND).getFirstChild(CKeyword.CONST) != null) {

      for (AstNode identifier : Variable.getDeclaredIdentifiers(astNode)) {
        String varName = identifier.getTokenValue();

        if (!pattern.matcher(varName).matches()) {
          addIssue("Rename this constant '" + varName + "' to match the regular expression " + format + "",
            identifier);
        }
      }
    }
  }
}
