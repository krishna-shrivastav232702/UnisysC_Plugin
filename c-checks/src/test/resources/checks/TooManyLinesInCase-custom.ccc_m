void foo(int myVariable) {
  switch (myVariable) {
    case 0:                     /* Noncompliant */
      printf("");
      printf("");
      printf("");
      break;
    case 1:
    default:                    /* Noncompliant {{Reduce this switch case number of lines of code from 6 to at most 4, for example by extracting code into methods.}} */
      printf("");
      printf("");
      printf("");
      printf("");
      break;
  }
}