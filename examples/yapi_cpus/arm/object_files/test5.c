#include <stdio.h>
#include <inttypes.h>

int main()
{
  uint32_t foo = 0xDEADBEEF;
  uint32_t bar;

  __asm__ ("ldrh %0, [%1]" : "=r" (bar) : "r" (&foo));

  printf("0x%x\n", bar);

  __asm__ ("ldrsh %0, [%1]" : "=r" (bar) : "r" ((uint16_t*)(&foo) + 1));

  printf("0x%x\n", bar);

  bar = 0xbabe;
  __asm__ ("strh %1, [%0]" : : "r" (&foo), "r" (bar));
  printf("0x%x\n", foo);

  bar = 0xcafe;
  __asm__ ("strh %1, [%0]" : : "r" ((uint16_t*)(&foo) + 1), "r" (bar));
  printf("0x%x\n", foo);

  return 0;
}
