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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S1481")
public class UnusedLocalVariableCheck extends CCheck {

  private static class LocalVariable {
    final AstNode declaration;
    int usages;

    private LocalVariable(AstNode declaration, int usages) {
      this.declaration = declaration;
      this.usages = usages;
    }
  }

  private static class Scope {
    private final Scope outerScope;
    private final Map<String, LocalVariable> variables;

    public Scope(Scope outerScope) {
      this.outerScope = outerScope;
      this.variables = new HashMap<>();
    }

    private void declare(AstNode astNode) {
      String identifier = astNode.getTokenValue();
      variables.computeIfAbsent(identifier, key -> new LocalVariable(astNode, 0));
    }

    private void use(AstNode astNode) {
      String identifier = astNode.getTokenValue();
      Scope scope = this;

      while (scope != null) {
        LocalVariable variable = scope.variables.get(identifier);
        if (variable != null) {
          variable.usages++;
          return;
        }
        scope = scope.outerScope;
      }
    }
  }

  private Scope currentScope;

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
        CGrammar.COMPOUND_STATEMENT,
        CGrammar.DECLARATION,
        CGrammar.IDENTIFIER
    );
  }

  @Override
  public void visitNode(AstNode astNode) {
    if (astNode.is(CGrammar.COMPOUND_STATEMENT)) {
      currentScope = new Scope(currentScope);
    } 
    else if (currentScope != null && astNode.is(CGrammar.DECLARATION)) {
      List<AstNode> descantants = astNode.getDescendants(CGrammar.IDENTIFIER);
      
      for (AstNode id : descantants) {
        if (isWithinDeclarator(id)) {
          currentScope.declare(id);
        }
      }
    } 
    else if (currentScope != null && astNode.is(CGrammar.IDENTIFIER)) {
      if (!isWithinDeclarator(astNode)) {
        currentScope.use(astNode);
      }
    }
  }

  private boolean isWithinDeclarator(AstNode identifier) {
    return identifier.getParent().is(CGrammar.DIRECT_DECLARATOR) && identifier.getParent().getParent().is(CGrammar.DECLARATOR);
  }

  @Override
  public void leaveNode(AstNode astNode) {
    if (astNode.is(CGrammar.COMPOUND_STATEMENT) && currentScope != null) {
      reportUnusedVariable();
      currentScope = currentScope.outerScope;
    }
  }

  private void reportUnusedVariable() {
    for (Map.Entry<String, LocalVariable> entry : currentScope.variables.entrySet()) {
      if (entry.getValue().usages == 0) {
        addIssue(MessageFormat.format("Remove this unused ''{0}'' local variable.", entry.getKey()),
            entry.getValue().declaration);
      }
    }
  }
}
