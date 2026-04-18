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

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.checks.utils.Clazz;
import org.sonar.c.checks.utils.MetadataTag;
import org.sonar.c.checks.utils.Variable;
import org.sonar.check.Rule;

@Rule(key = "S1469")
public class ArrayFieldElementTypeCheck extends CCheck {

  @Override
  public List<AstNodeType> subscribedTo() {
    return Collections.singletonList(CGrammar.CLASS_DEF);
  }

  @Override
  public void visitNode(AstNode astNode) {
    for (AstNode directive : Clazz.getDirectives(astNode)) {

      if (Variable.isVariable(directive)) {
        AstNode varBindingList = directive
          .getFirstChild(CGrammar.ANNOTABLE_DIRECTIVE)
          .getFirstChild(CGrammar.VARIABLE_DECLARATION_STATEMENT)
          .getFirstChild(CGrammar.VARIABLE_DEF)
          .getFirstChild(CGrammar.VARIABLE_BINDING_LIST);

        checkVarBindingList(varBindingList, directive);
      }

    }
  }

  private void checkVarBindingList(AstNode varBindingList, AstNode directive) {
    for (AstNode varBinding : varBindingList.getChildren(CGrammar.VARIABLE_BINDING)) {

      if (!hasInitialisation(varBinding) && isArray(varBinding) && !hasArrayTypeTag(directive)) {
        String message = MessageFormat.format(
          "Define the element type for this ''{0}'' array",
          varBinding.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.IDENTIFIER).getTokenValue());
        addIssue(message, varBinding);
      }
    }
  }

  private static boolean hasInitialisation(AstNode varBinding) {
    return varBinding.getFirstChild(CGrammar.VARIABLE_INITIALISATION) != null;
  }

  private static boolean isArray(AstNode varBinding) {
    AstNode typeExpr = varBinding.getFirstChild(CGrammar.TYPED_IDENTIFIER).getFirstChild(CGrammar.TYPE_EXPR);

    return typeExpr != null
      && typeExpr.getNumberOfChildren() == 1
      && "Array".equals(typeExpr.getFirstChild().getTokenValue());
  }

  private static boolean hasArrayTypeTag(AstNode directive) {
    AstNode previousDirective = directive.getPreviousAstNode();

    while (previousDirective != null && MetadataTag.isMetadataTag(previousDirective)) {
      if (MetadataTag.isTag(previousDirective.getFirstChild().getFirstChild(CGrammar.METADATA_STATEMENT), "ArrayElementType")) {
        return true;
      }
      previousDirective = previousDirective.getPreviousAstNode();
    }
    return false;
  }
}
