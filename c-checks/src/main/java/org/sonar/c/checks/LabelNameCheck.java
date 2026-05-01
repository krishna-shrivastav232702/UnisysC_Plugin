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
import com.sonar.sslr.api.Token;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "S3222")
public class LabelNameCheck extends CCheck {

  private static final String DEFAULT = "^[A-Z][A-Z0-9_]*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the label names against",
    defaultValue = DEFAULT)
  String format = DEFAULT;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.LABELED_STATEMENT);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    if (pattern == null) {
      pattern = Pattern.compile(format);
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode nameNode = astNode.getFirstChild(CGrammar.IDENTIFIER);
    if (nameNode != null) {
      String labelIdentifier = nameNode.getTokenValue();
      if (!pattern.matcher(labelIdentifier).matches()) {
        addIssue(MessageFormat.format("Rename this label name to match the regular expression {0}", format), astNode);
      }
    }
  }
}
