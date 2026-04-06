#include <stdbool.h>

void doSomething(void) {
    /* placeholder */
}

void A_f(void) {
    if (false) { // Compliant
    }

    if (false) { // Compliant
    } else {
    }

    if (false) { // Compliant
        if (false) { // Noncompliant
        }
    }

    if (false) { // Compliant
        if (false) { // Compliant
        }
        doSomething();
    }

    if (false) { // Compliant
        int a = 0;
        if (a) { // Compliant
        }
    }

    if (false) { // Compliant
        if (false) { // Compliant
        }
    } else {
    }

    if (false) { // Compliant
        if (false) { // Compliant
        } else {
        }
    }

    if (false) { // Compliant
    } else if (false) { // Compliant
        if (false) { // Noncompliant
        }
    }

    if (false) // Compliant
        if (true) { // Noncompliant
        }

    if (false) { // Compliant
        while (true) {
            if (true) { // Compliant
            }
        }

        while (true)
            if (true) { // Compliant
            }
    }
}
