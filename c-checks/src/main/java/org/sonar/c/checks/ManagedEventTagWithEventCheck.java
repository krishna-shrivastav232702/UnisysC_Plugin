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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.checks.utils.MetadataTag;
import org.sonar.check.Rule;

@Rule(key = "S1464")
public class ManagedEventTagWithEventCheck extends CCheck {

  private Map<String, Boolean> isDeclaredInEventTag = new HashMap<>();

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.METADATA_STATEMENT);
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (!MetadataTag.isTag(astNode, "ManagedEvents")) {
      return;
    }

    initEventMap(MetadataTag.getSinglePropertyAsList(astNode));
    searchEventInPreviousTag(astNode);
    searchEventInNextTag(astNode);

    for (Map.Entry<String, Boolean> entry: isDeclaredInEventTag.entrySet()) {
      if (Boolean.FALSE.equals(entry.getValue())) {
        addIssue(MessageFormat.format("The managed event {0} is either misspelled or is missing a companion Event metadata tag", entry.getKey()), astNode);
      }
    }

    isDeclaredInEventTag.clear();
  }

  private void initEventMap(List<String> managedEventNames) {
    for (String eventName : managedEventNames) {
      isDeclaredInEventTag.put("\"" + eventName + "\"", false);
    }
  }

  private void searchEventInPreviousTag(AstNode metadata) {
    AstNode previousDirective = metadata.getParent().getParent().getPreviousAstNode();

    while (previousDirective != null && MetadataTag.isMetadataTag(previousDirective)) {
      checkEventTag(previousDirective);
      previousDirective = previousDirective.getPreviousAstNode();
    }
  }

  private void searchEventInNextTag(AstNode metadata) {
    AstNode nextDirective = metadata.getParent().getParent().getNextAstNode();

    while (nextDirective != null && MetadataTag.isMetadataTag(nextDirective)) {
      checkEventTag(nextDirective);
      nextDirective = nextDirective.getNextAstNode();
    }
  }


  private void checkEventTag(AstNode directive) {
    AstNode metadataStmt = directive.getFirstChild().getFirstChild(CGrammar.METADATA_STATEMENT);

    if (MetadataTag.isTag(metadataStmt, "Event")) {
      String eventName = MetadataTag.getTagPropertiesMap(metadataStmt).get("name");


      isDeclaredInEventTag.computeIfPresent(eventName, (key, value) -> true);
    }
  }
}
