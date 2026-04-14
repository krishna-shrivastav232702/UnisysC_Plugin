void test() {
  int a;
  for (a = 0; a < 42; a++) {
      a = 0;                                /* Noncompliant {{Do not update the loop counter "a" within the loop body.}} */
  }

  int d, e;
  for (d = 0, e = 0; d < 42; d++) {
      d = 0;                                /* Noncompliant */
      e = 0;                                /* Noncompliant */
  }

  int f, g;
  for (f = 0; f < 42; f++) {
      f = 0;                                /* Noncompliant */
      g = 0;                                /* OK */
      for (g = 0; g < 42; g++) {
          g = 0;                            /* Noncompliant */
          f = 0;                            /* Noncompliant */
      }
      f = 0;                                /* Noncompliant */
      g = 0;                                /* OK */
  }

  g = 0;                                    /* OK */

  int h;
  for (h = 0; h < 42; h++) {
      h = 0;                                /* Noncompliant */
  }

  g++;                                      /* OK */
  ++g;                                      /* OK */
  g = 0;                                    /* OK */

  int i;
  for (i = 0; 0 < 42; i++) {
      i++;                                  /* Noncompliant */
      ++i;                                  /* Noncompliant */
      --i;                                  /* Noncompliant */
      i--;                                  /* Noncompliant */
  }

  int j, k;
  for (j = 0; j < 42; j++) {               /* OK */
      for (k = 0; j++ < 42; k++) {         /* Noncompliant */
      }
  }

  for (i = 0; i < 42; i++) {               /* OK */
  }

  for (i = 0; i < 10; i++) {
      for (k = 0; k < 20; i++) {           /* Noncompliant */
          doSomething(i = 0);               /* Noncompliant */
      }
  }

  int list;
  for (i = 0; list > 1; i++) {             /* Noncompliant {{Calculate the stop condition value outside the loop and set it to a variable.}} */
  }

  for (i = 0; doSomething() > 1; i++) {    /* Noncompliant */
  }

  for (; i > 0; i++) {
      i = 1;                                /* OK */
  }

  for (++i; i > 0; i++) {
      i = 1;                                /* Noncompliant */
  }
}