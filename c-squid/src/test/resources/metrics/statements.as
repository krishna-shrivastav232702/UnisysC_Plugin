int foo() {
  int a = 1; // +1 declaration statement
  a = a + 1; // +1 expression statement

  if (false) { // +1 if statement
    return 1; // +1 return statement
  }

  for (int i = 0; i < 10; i++) { // +1 for statement
    break; // +1 break statement
  }

  while (false) { // +1 while statement
    continue; // +1 continue statement
  }

  do { // +1 do-while statement
  } while (false);

  switch (1) { // +1 switch statement
    case 0:
      break;
    default:
      break;
  }

  a = a + 1; a = a + 1; // +2 expression statement

  return 0; // +1 return statement
}
