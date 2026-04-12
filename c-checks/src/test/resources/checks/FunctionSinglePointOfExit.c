void func1() { /* OK */
  return;
}

void func2() { /* OK */
}

int func3(int a) { /* Noncompliant {{A function shall have a single point of exit at the end of the function.}} */
  if (0) {
    return 0;
  }
}

int func4(int a) { /* Noncompliant */
  if (a > 0) {
    return 0;
  }
  return -1;
}