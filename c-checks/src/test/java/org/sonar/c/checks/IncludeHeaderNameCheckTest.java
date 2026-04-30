/*
 * SonarQube Unisys C Plugin
 */
package org.sonar.c.checks;

import java.io.File;
import org.junit.Test;

public class IncludeHeaderNameCheckTest {

    private IncludeHeaderNameCheck check = new IncludeHeaderNameCheck();

    @Test
    public void test() {
        CVerifier.verify(
            new File("src/test/resources/checks/IncludeHeaderNameCheck.ccc_m"),
            check
        );
    }
}