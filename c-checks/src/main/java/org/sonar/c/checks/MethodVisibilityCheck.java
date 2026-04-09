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
import org.sonar.c.checks.utils.Function;
import org.sonar.c.checks.utils.Modifiers;
import org.sonar.check.Rule;

@Rule(key = "S1784")
public class MethodVisibilityCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.CLASS_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    List<AstNode> directives = astNode.getFirstChild(CGrammar.BLOCK).getFirstChild(CGrammar.DIRECTIVES).getChildren();
    if (directives == null) {
      return;
    }

    for (AstNode directive : directives) {
      AstNode annotableDirective = directive.getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE);

      if (annotableDirective != null) {
        AstNode annotableDirectiveChild = annotableDirective.getFirstChild();

        if (annotableDirectiveChild.is(CGrammar.FUNCTION_DEF) && !hasVisibility(annotableDirectiveChild)) {
          addIssue(
            MessageFormat.format("Explicitly declare the visibility of this method \"{0}\".", Function.getName(annotableDirectiveChild)),
            annotableDirectiveChild);
        }
      }
    }
  }

  private static boolean hasVisibility(AstNode functionDef) {
    Set<AstNodeType> modifiers = Modifiers.getModifiers(functionDef.getPreviousAstNode());

    for (AstNodeType modifier : modifiers) {
      if (isVisibility(modifier)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isVisibility(AstNodeType modifier) {
    return modifier.equals(CKeyword.PUBLIC) || modifier.equals(CKeyword.INTERNAL)
      || modifier.equals(CKeyword.PROTECTED) || modifier.equals(CKeyword.PRIVATE);
  }

}
