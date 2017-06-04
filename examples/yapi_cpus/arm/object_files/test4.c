#include <stdio.h>


int main()
{
  /*fprintf(stderr, "Hello, %s!\n", "World");*/
  volatile int a, b;

  a = 1000;
  b = 15;

  fprintf(stderr, "Hello - %d\n", a * b);
  return 0;
}
