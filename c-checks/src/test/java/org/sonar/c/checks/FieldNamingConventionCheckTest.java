package org.sonar.c.checks;

import org.junit.Test;
import java.io.File;

public class FieldNamingConventionCheckTest {

    @Test
    public void test() {
        CVerifier.verify(
            new File("src/test/resources/checks/FieldNamingConventionCheck.ccc_m"),
            new FieldNamingConventionCheck()
        );
    }
}