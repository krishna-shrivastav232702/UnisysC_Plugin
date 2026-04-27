package org.sonar.c.checks;

import java.io.File;
import org.junit.Test;

public class NonPortableIncludeCheckTest {

    NonPortableIncludeCheck check = new NonPortableIncludeCheck();

    @Test
    public void test() {
        CVerifier.verify(
            new File("src/test/resources/checks/NonPortableInclude.ccc_m"),
            check
        );
    }
}