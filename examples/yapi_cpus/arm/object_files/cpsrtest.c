#include <stdio.h>
#include <inttypes.h>
#include <swarm.h>

int main()
{
  uint32_t my_cpsr, my_ncpsr;

  __asm__ volatile ("mrs %0, cpsr" : "=r" (my_cpsr) :);
  my_ncpsr = my_cpsr | 0x000000C0;
  printf("We changed 0x%08X to 0x%08X\n", my_cpsr, my_ncpsr);
  __asm__ volatile ("msr cpsr, %0" : : "r" (my_ncpsr));

  _dump();

  return 0;
}
