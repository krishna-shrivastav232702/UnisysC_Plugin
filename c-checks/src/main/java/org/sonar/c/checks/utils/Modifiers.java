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
package org.sonar.c.checks.utils;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import org.sonar.c.CGrammar;
import org.sonar.c.CKeyword;

public final class Modifiers {

  private Modifiers() {
  }

  public static Set<AstNodeType> getModifiers(@Nullable AstNode attributes) {
    Set<AstNodeType> modifiersSet = new HashSet<>();

    if (attributes != null && attributes.is(CGrammar.ATTRIBUTES)) {
      for (AstNode attribute : attributes.getChildren(CGrammar.ATTRIBUTE)) {

        if (attribute.getFirstChild().is(CGrammar.RESERVED_NAMESPACE)) {
          modifiersSet.add(attribute.getFirstChild(CGrammar.RESERVED_NAMESPACE).getFirstChild().getType());

        } else if (attribute.getFirstChild().is(CGrammar.ATTRIBUTE_EXPR) && attribute.getFirstChild().getNumberOfChildren() == 1) {
          modifiersSet.add(attribute.getFirstChild().getFirstChild(CGrammar.IDENTIFIER).getFirstChild().getType());
        }
      }
    }
    return modifiersSet;
  }

  public static boolean isNonPublic(Set<AstNodeType> modifiers) {
    for (AstNodeType modifier : modifiers) {
      if (modifier.equals(CKeyword.INTERNAL) || modifier.equals(CKeyword.PROTECTED) || modifier.equals(CKeyword.PRIVATE)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPublic(AstNode attriutes) {
    Set<AstNodeType> modifiers = getModifiers(attriutes);
    return modifiers.contains(CKeyword.PUBLIC) || !isNonPublic(modifiers);
  }

}
