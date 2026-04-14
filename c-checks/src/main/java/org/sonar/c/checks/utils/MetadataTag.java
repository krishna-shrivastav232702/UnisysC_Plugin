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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.c.CGrammar;

public final class MetadataTag {

  private MetadataTag() {
  }

  public static boolean isTag(AstNode metadata, String tagName) {
    Preconditions.checkState(metadata.is(CGrammar.METADATA_STATEMENT));
    if (isNotEmpty(metadata)) {
      AstNode postfixExpr = metadata
        .getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION)
        .getFirstChild(CGrammar.POSTFIX_EXPRESSION);

      return postfixExpr != null && tagName.equals(postfixExpr.getTokenValue());
    }
    return false;
  }

  // [Metadata("property, in, one, string")] --> [property, in, one, string]
  // or
  // [Metadata(tag="property, in, one, string")] --> [property, in, one, string]
  public static List<String> getSinglePropertyAsList(AstNode metadata) {
    Preconditions.checkState(metadata.is(CGrammar.METADATA_STATEMENT));
    List<String> propertyList = new ArrayList<>();

    if (isNotEmpty(metadata) && hasProperty(metadata)) {
      AstNode properties = metadata
        .getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION)
        .getFirstChild(CGrammar.POSTFIX_EXPRESSION)
        .getFirstChild(CGrammar.ARGUMENTS)
        .getFirstChild(CGrammar.LIST_EXPRESSION);

      if (properties.getNumberOfChildren() == 1) {
        AstNode assignmentExpr = properties.getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION);
        if (assignmentExpr.getNumberOfChildren() > 1) {
          // Case where assignment expr contains an assignment operation
          assignmentExpr = assignmentExpr.getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION);
        }
        String singleProperty = assignmentExpr.getTokenValue();
        for (String property : singleProperty.substring(1, singleProperty.length() - 1).split(",")) {
          propertyList.add(property.trim());
        }
      }
    }
    return propertyList;
  }

  public static Map<String, String> getTagPropertiesMap(AstNode metadata) {
    Preconditions.checkState(metadata.is(CGrammar.METADATA_STATEMENT));
    if (isNotEmpty(metadata) && hasProperty(metadata)) {

      Map<String, String> properties = new HashMap<>();
      AstNode listExpr = metadata
        .getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION)
        .getFirstChild(CGrammar.POSTFIX_EXPRESSION)
        .getFirstChild(CGrammar.ARGUMENTS)
        .getFirstChild(CGrammar.LIST_EXPRESSION);

      for (AstNode assignmentExpr : listExpr.getChildren(CGrammar.ASSIGNMENT_EXPRESSION)) {
        if (assignmentExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR) != null) {
          properties.put(assignmentExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR).getPreviousSibling().getTokenValue(),
            assignmentExpr.getFirstChild(CGrammar.ASSIGNMENT_OPERATOR).getNextSibling().getTokenValue());
        }
      }
      return properties;
    }
    return null;
  }

  public static boolean isNotEmpty(AstNode metadata) {
    return metadata.getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION) != null
      && metadata.getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION).getFirstChild(CGrammar.POSTFIX_EXPRESSION) != null;
  }

  public static boolean hasProperty(AstNode metadata) {
    Preconditions.checkState(metadata.is(CGrammar.METADATA_STATEMENT));
    AstNode arguments = metadata
      .getFirstChild(CGrammar.ASSIGNMENT_EXPRESSION)
      .getFirstChild(CGrammar.POSTFIX_EXPRESSION)
      .getFirstChild(CGrammar.ARGUMENTS);
    return arguments != null && arguments.getFirstChild(CGrammar.LIST_EXPRESSION) != null;
  }

  public static boolean isMetadataTag(AstNode directive) {
    return directive.getFirstChild().is(CGrammar.STATEMENT)
      && directive.getFirstChild().getFirstChild().is(CGrammar.METADATA_STATEMENT);
  }

}
