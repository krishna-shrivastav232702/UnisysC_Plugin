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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.checks.utils.Clazz;
import org.sonar.c.checks.utils.Modifiers;
import org.sonar.c.checks.utils.Variable;
import org.sonar.check.Rule;

@Rule(key = "S1170")
public class PublicConstNotStaticCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.CLASS_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    for (AstNode directive : Clazz.getDirectives(astNode)) {
      if (Variable.isConst(directive)) {
        Set<AstNodeType> varModifiers = Modifiers.getModifiers(directive.getFirstChild(CGrammar.ATTRIBUTES));

        if (varModifiers.contains(CKeyword.PUBLIC) && !varModifiers.contains(CKeyword.STATIC)) {
          String name = Variable.getName(directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE).getFirstChild());
          addIssue(MessageFormat.format("Make this const field \"{0}\" static too", name), directive);
        }
      }
    }
  }
}
