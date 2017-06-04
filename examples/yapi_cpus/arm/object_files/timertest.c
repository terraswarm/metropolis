#include <stdio.h>
#include <inttypes.h>
#include <swarm.h>

#define IC_ICMR ((volatile uint32_t*)0x90050004)
#define IC_ICLR ((volatile uint32_t*)0x90050008)

#define OST_OSMR0 ((volatile uint32_t*)0x90000000)
#define OST_OSCR  ((volatile uint32_t*)0x90000010)
#define OST_OIER  ((volatile uint32_t*)0x9000001C)

int main()
{
  uint32_t temp;
  int i;

  /* Tell the interrupt controller that we want to have a IRQ occur
   * for the first OS timer */
  *IC_ICMR = 0x04000000;
  *IC_ICLR = 0x04000000;

  /* Setup the interrupt timer */
  temp = *OST_OSCR;
  _dump();
  temp += 1500;
  *OST_OSMR0 = temp;
  *OST_OIER = 0x00000001;

  for (i = 0; i < 100; i++)
    {
      fprintf(stderr, "Foo %d\n", i);
    }

  return 0;
}
