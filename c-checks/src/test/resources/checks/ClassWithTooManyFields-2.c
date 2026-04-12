void test() {
  struct C { /* Noncompliant {{Refactor this struct so it has no more than 2 public fields, rather than the 3 it currently has.}} */
    int a;
    int b;
    int c;
    int d;
    int e;
    int f;
  };

  struct D {
    int a;
    int b;
  };
}