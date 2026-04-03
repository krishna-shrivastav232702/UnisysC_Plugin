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
package org.sonar.c;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CVisitorContext;
import org.sonar.c.Issue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CCheckTest {

  @Test
  public void lineIssue() {
    CCheck check = new CCheck() {
      @Override
      public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.ASSIGNMENT_OPERATOR);
      }

      @Override
      public void visitNode(AstNode node) {
        addIssue("message!", node);
      }
    };
    List<Issue> issues = check.scanFileForIssues(context());
    assertThat(issues).extracting("line", "message", "cost").containsExactly(tuple(2, "message!", null));
  }

  @Test
  public void lineIsueWithCost() {
    CCheck check = new CCheck() {
      @Override
      public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.ASSIGNMENT_OPERATOR);
      }

      @Override
      public void visitNode(AstNode node) {
        addIssueWithCost("message!", node, 42.);
      }
    };
    List<Issue> issues = check.scanFileForIssues(context());
    assertThat(issues).extracting("line", "message", "cost").containsExactly(tuple(2, "message!", 42.));
  }

  @Test
  public void fileIssue() {
    CCheck check = new CCheck() {
      @Override
      public List<AstNodeType> subscribedTo() {
        return Collections.emptyList();
      }

      @Override
      public void visitFile(AstNode node) {
        addFileIssue("bad file!");
      }
    };
    List<Issue> issues = check.scanFileForIssues(context());
    assertThat(issues).extracting("line", "message", "cost").containsExactly(tuple(null, "bad file!", null));
  }

  private CVisitorContext context() {
    return TestVisitorContext.create(new File("src/test/resources/org/sonar/c/ccheck.as"));
  }
}
