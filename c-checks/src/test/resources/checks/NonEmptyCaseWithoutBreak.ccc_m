void foo(int param) {
  switch (param) {
    case 0: /* OK */
    case 1: /* OK */
      break;
    case 2: /* OK */
      return;
    case 3: /* OK */
      exit(1);
    case 4: /* OK */
    {
      doSomething();
      break;
    }
    default: /* OK */
      break;
  }

  switch (param) {
    case 0:
    case 1: /* Noncompliant {{Last statement in this switch-clause should be an unconditional break.}} */
      doSomething();
    case 2: /* Noncompliant */
      if (1) {
        break;
      }
    case 3: /* Noncompliant */
      break;
      doSomething();
    default: /* OK */
      doSomethingElse();
  }
}