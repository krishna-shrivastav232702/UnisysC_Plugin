int f1(int a, int b) {          /* Noncompliant {{Remove the unused function parameter "b".}} */
  return a;
}

int f2(int a, int b, int c) {   /* Noncompliant {{Remove the unused function parameters "b, c".}} */
  return a;
}

int f3(int a, int b) {          /* OK */
  return a + b;
}

int f4(int a) {                 /* OK */
  return a;
}

void f5(int a) {                /* OK - empty body stub */
}

void f6(int a) {                /* OK - single exit stub */
  exit(1);
}

void handleClick(int event) {   /* OK - event handler by name */
  process();
}

void onSomething(int event) {   /* OK - event handler by name */
  process();
}

void onlySomething(int event) { /* Noncompliant {{Remove the unused function parameter "event".}} */
  process();
}