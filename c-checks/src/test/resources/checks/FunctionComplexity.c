int foo = 1;

void ko() {
    switch (foo) {
        case 1:
            break;
        case 2:
            break;
        default:
            break;
    }
}

void empty() {  /* Noncompliant {{Function has a complexity of 1 which is greater than 0 authorized.}} [[effortToFix=1]] */
}

void withReturn() {   /* Noncompliant {{Function has a complexity of 1 which is greater than 0 authorized.}} [[effortToFix=1]] */
  return;
}

void withTernary(int i) {  /* Noncompliant {{Function has a complexity of 2 which is greater than 0 authorized.}} [[effortToFix=2]] */
  int result = (i > 0) ? 1 : 0;
}