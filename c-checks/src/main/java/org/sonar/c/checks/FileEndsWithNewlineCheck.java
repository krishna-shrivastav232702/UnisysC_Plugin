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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S113")
public class FileEndsWithNewlineCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.PROGRAM);
    }

    @Override
    public void visitNode(AstNode program) {
        if (program == null || program.getToken() == null || program.getToken().getURI() == null) {
            return;
        }

        Path filePath;
        try {
            filePath = Paths.get(program.getToken().getURI());
        } catch (Exception e) {
            return;
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return;
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            return;
        }

        if (bytes.length == 0) {
            return;
        }

        byte lastByte = bytes[bytes.length - 1];
        if (lastByte != '\n') {
            addIssue(
                "File should end with a newline.",
                program
            );
        }
    }
}