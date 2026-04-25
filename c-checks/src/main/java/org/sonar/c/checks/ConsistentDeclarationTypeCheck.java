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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "M23_050")
public class ConsistentDeclarationTypeCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.PROGRAM);
    }

    @Override
    public void visitNode(AstNode program) {
        Map<String, String> firstTypeMap = new HashMap<>();
        Map<String, AstNode> firstNodeMap = new HashMap<>();

        for (AstNode extDef : program.getChildren(CGrammar.EXTERNAL_DEFINITION)) {

            AstNode declaration = extDef.getFirstChild(CGrammar.DECLARATION);
            AstNode functionDef = extDef.getFirstChild(CGrammar.FUNCTION_DEF);

            if (declaration != null) {
                processDeclaration(declaration, firstTypeMap, firstNodeMap);
            } else if (functionDef != null) {
                processFunctionDef(functionDef, firstTypeMap, firstNodeMap);
            }
        }
    }

    private void processDeclaration(
            AstNode declaration,
            Map<String, String> firstTypeMap,
            Map<String, AstNode> firstNodeMap) {

        String typeSignature = buildTypeSignature(declaration);
        if (typeSignature == null || typeSignature.isEmpty()) {
            return;
        }

        List<AstNode> nameNodes = getDeclaredNameNodes(declaration);
        for (AstNode nameNode : nameNodes) {
            String name = nameNode.getTokenValue();
            if (name == null) {
                continue;
            }
            checkAndRegister(name, typeSignature, nameNode,
                    firstTypeMap, firstNodeMap);
        }
    }

    private void processFunctionDef(
            AstNode functionDef,
            Map<String, String> firstTypeMap,
            Map<String, AstNode> firstNodeMap) {

        String typeSignature = buildTypeSignatureFromSpecifiers(
                functionDef.getFirstChild(CGrammar.DECLARATION_SPECIFIERS));
        if (typeSignature == null || typeSignature.isEmpty()) {
            return;
        }

        AstNode declarator = functionDef.getFirstChild(CGrammar.DECLARATOR);
        if (declarator == null) {
            return;
        }
        AstNode directDecl = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
        if (directDecl == null) {
            return;
        }
        AstNode identifier = directDecl.getFirstChild(CGrammar.IDENTIFIER);
        if (identifier == null) {
            return;
        }

        String name = identifier.getTokenValue();
        if (name == null) {
            return;
        }

        checkAndRegister(name, typeSignature, identifier,
                firstTypeMap, firstNodeMap);
    }

    private void checkAndRegister(
            String name,
            String typeSignature,
            AstNode reportNode,
            Map<String, String> firstTypeMap,
            Map<String, AstNode> firstNodeMap) {

        if (!firstTypeMap.containsKey(name)) {
            firstTypeMap.put(name, typeSignature);
            firstNodeMap.put(name, reportNode);
        } else {
            String previousType = firstTypeMap.get(name);
            if (!previousType.equals(typeSignature)) {
                addIssue(
                    "All declarations of \"" + name + "\" shall have the same type."
                    + " Previously declared as \"" + previousType + "\","
                    + " now declared as \"" + typeSignature + "\".",
                    reportNode
                );
            }
        }
    }

    private String buildTypeSignature(AstNode declaration) {
        AstNode declSpecs = declaration.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
        return buildTypeSignatureFromSpecifiers(declSpecs);
    }

    
    private String buildTypeSignatureFromSpecifiers(AstNode declSpecs) {
        if (declSpecs == null) {
            return null;
        }

        List<String> typeParts = new ArrayList<>();
        for (AstNode ts : declSpecs.getChildren(CGrammar.TYPE_SPECIFIER)) {
            String val = ts.getTokenValue();
            if (val != null && !val.isEmpty()) {
                typeParts.add(val.toLowerCase());
            }
        }

        if (typeParts.isEmpty()) {
            return null;
        }

        Collections.sort(typeParts);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < typeParts.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(typeParts.get(i));
        }
        return sb.toString();
    }

    
    private List<AstNode> getDeclaredNameNodes(AstNode declaration) {
        List<AstNode> result = new ArrayList<>();
        AstNode initDeclList = declaration.getFirstChild(CGrammar.INIT_DECLARATOR_LIST);
        if (initDeclList == null) {
            return result;
        }

        for (AstNode initDecl : initDeclList.getChildren(CGrammar.INIT_DECLARATOR)) {
            AstNode declarator = initDecl.getFirstChild(CGrammar.DECLARATOR);
            if (declarator == null) {
                continue;
            }
            AstNode directDecl = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
            if (directDecl == null) {
                continue;
            }
            AstNode identifier = directDecl.getFirstChild(CGrammar.IDENTIFIER);
            if (identifier != null) {
                result.add(identifier);
            }
        }
        return result;
    }
}