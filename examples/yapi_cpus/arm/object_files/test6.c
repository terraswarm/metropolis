#include <stdio.h>
#include <inttypes.h>

int main()
{
  uint32_t temp;

  __asm__ ("mrc p15, 0, %0, c1, c0, 0" : "=r" (temp) :);

  printf("0x%08x\n", temp);

  return 0;
}
