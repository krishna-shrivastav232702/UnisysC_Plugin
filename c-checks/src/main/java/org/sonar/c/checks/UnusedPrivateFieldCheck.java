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
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;
import org.sonar.c.checks.utils.Clazz;
import org.sonar.c.checks.utils.Modifiers;
import org.sonar.c.checks.utils.Variable;
import org.sonar.check.Rule;

@Rule(key = "S1068")
public class UnusedPrivateFieldCheck extends CCheck {

  private static class PrivateField {
    final AstNode declaration;
    int usages;

    public PrivateField(AstNode declaration, int usages) {
      this.declaration = declaration;
      this.usages = usages;
    }
  }

  private static class ClassState {
    private final Map<String, PrivateField> privateFields;

    public ClassState(AstNode classDef) {
      this.privateFields = new HashMap<>();
      retrieveAllPrivateFields(classDef);
    }

    private void retrieveAllPrivateFields(AstNode classDef) {
      for (AstNode varDeclaration : Clazz.getFields(classDef)) {
        if (Modifiers.getModifiers(varDeclaration.getParent().getPreviousAstNode()).contains(CKeyword.PRIVATE)) {
          for (AstNode identifier : Variable.getDeclaredIdentifiers(varDeclaration)) {

            privateFields.put(identifier.getTokenValue(), new PrivateField(identifier, 0));
          }
        }
      }
    }

    private void use(AstNode astNode) {
      PrivateField field = privateFields.get(astNode.getTokenValue());

      if (field != null) {
        field.usages++;
      }
    }
  }


  private Deque<ClassState> classStack = new ArrayDeque<>();

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
      CGrammar.CLASS_DEF,
      CGrammar.QUALIFIED_IDENTIFIER);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    classStack.clear();
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.CLASS_DEF)) {
      classStack.push(new ClassState(astNode));
    } else if (!classStack.isEmpty() && astNode.is(CGrammar.QUALIFIED_IDENTIFIER)) {
      classStack.peek().use(astNode);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CGrammar.CLASS_DEF)) {
      reportUnusedPrivateField();
    }
  }

  private void reportUnusedPrivateField() {
    for (Map.Entry<String, PrivateField> entry : classStack.pop().privateFields.entrySet()) {
      if (entry.getValue().usages == 0) {
        addIssue(MessageFormat.format("Remove this unused ''{0}'' private field", entry.getKey()), entry.getValue().declaration);
      }
    }
  }
}
