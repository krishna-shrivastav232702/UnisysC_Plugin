int field = 0;

void f1() {
    int foo;                /* OK */
    int field = 1;          /* Noncompliant {{Rename "field" which hides the field declared at line 1.}} */
}

void f2() {
    int bar = field;        /* OK - using outer variable, not shadowing */
}

void nestedFunction() {
    int field;
}

void f3() {
    nestedFunction();
}