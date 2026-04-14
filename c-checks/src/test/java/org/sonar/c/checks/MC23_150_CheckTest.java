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

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.c.checks.LocalVarShadowsFieldCheck;

public class MC23_150_CheckTest {

    private MC23_150_Check check = new MC23_150_Check();

    @Test
    public void test() {
        CVerifier.verify(new File("src/test/resources/checks/MC23_150.ccc_m"), check);
    }
}