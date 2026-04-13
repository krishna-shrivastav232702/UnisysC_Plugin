void if_statement(int a, int b) {
  if (a) {
    first();
    second();
  } else if (b) {
    foo();
  } else {
    first();
    second();
  }

  if (a) { /* Noncompliant {{Remove this conditional structure or edit its code blocks so that they're not all the same.}} */
    first();
  } else {
    first();
  }

  if (a) { /* Noncompliant */
    first();
    second();
  } else {
    first();
    second();
  }

  if (a) { /* Noncompliant */
    first();
    second();
  } else if (b) {
    first();
    second();
  } else {
    first();
    second();
  }
}

void switch_statement(int a) {
  switch (a) { /* OK - no default */
    case 1:
      first();
      break;
    case 2:
      second();
  }

  switch (a) { /* Noncompliant */
    case 1:
      first();
      second();
      break;
    default:
      first();
      second();
  }

  switch (a) { /* Noncompliant */
    case 1:
      first();
      break;
    case 2:
      first();
      break;
    default:
      first();
  }
}