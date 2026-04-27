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
package org.sonar.c.metrics;

import com.sonar.sslr.api.AstNode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.sonar.c.CGrammar;
import org.sonar.c.CVisitorContext;

public class FileMetrics {

  private final int numberOfStatements;
  private final int numberOfClasses;
  private final int numberOfFunctions;
  private final FileLinesVisitor fileLinesVisitor = new FileLinesVisitor();
  private final String executableLines;

  public FileMetrics(CVisitorContext context) {
    AstNode rootTree = context.rootTree();
    Objects.requireNonNull(rootTree, "Cannot compute metrics without a root tree");
    List<AstNode> statements = rootTree.getDescendants(
      CGrammar.DEFAULT_XML_NAMESPACE_DIRECTIVE,
      CGrammar.VARIABLE_DECLARATION_STATEMENT,
      CGrammar.EXPRESSION_STATEMENT,
      CGrammar.CONTROL_STATEMENT,
      CGrammar.FOR_STATEMENT,
      CGrammar.WHILE_STATEMENT,
      CGrammar.DO_STATEMENT,
      CGrammar.JUMP_STATEMENT,
      CGrammar.RETURN_STATEMENT,
      CGrammar.EMPTY_STATEMENT);

    Set<Integer> alreadyMarked = new HashSet<>();
    StringBuilder sb = new StringBuilder();
    for (AstNode descendant : statements) {
      int line = descendant.getTokenLine();
      if (alreadyMarked.add(line)) {
        sb.append(line).append("=1;");
      }
    }
    executableLines = sb.toString();

    numberOfStatements = statements.size();
    numberOfClasses = rootTree.getDescendants(CGrammar.CLASS_DEF, CGrammar.INTERFACE_DEF).size();
    numberOfFunctions = rootTree.getDescendants(CGrammar.FUNCTION_DEF, CGrammar.FUNCTION_EXPR).size();
    fileLinesVisitor.scanFile(context);
  }

  public Set<Integer> linesOfCode() {
    return fileLinesVisitor.linesOfCode();
  }

  public Set<Integer> commentLines() {
    return fileLinesVisitor.linesOfComments();
  }

  public Set<Integer> nosonarLines() {
    return fileLinesVisitor.noSonarLines();
  }

  public int numberOfClasses() {
    return numberOfClasses;
  }

  public int numberOfFunctions() {
    return numberOfFunctions;
  }

  public int numberOfStatements() {
    return numberOfStatements;
  }

  public String executableLines() {
    return executableLines;
  }
}
