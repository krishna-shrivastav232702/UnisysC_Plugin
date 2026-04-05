void doSomething() {
  ;                                           /* Noncompliant {{Remove this empty statement.}} */
}

void doSomethingElse() {
  printf("Hello, world!");;                   /* Noncompliant */
  for (int i = 0; i < 3; i++);               /* Noncompliant */
}