#include <stdio.h>
#include <inttypes.h>

void mul(uint32_t r1, uint32_t r2, uint32_t a, uint32_t b)
{
  uint32_t t1 = r1, t2 = r1;
  __asm__ ("smull %0, %1, %2, %3" : "=&r" (r1), "=&r" (r2) : "r" (a), "r" (b));
  printf("smull: 0x%08x 0x%08x 0x%08x 0x%08x\n", a, b, r1, r2);

  __asm__ ("umull %0, %1, %2, %3" : "=&r" (r1), "=&r" (r2) : "r" (a), "r" (b));
  printf("umull: 0x%08x 0x%08x 0x%08x 0x%08x\n", a, b, r1, r2);

  __asm__ ("smlal %0, %1, %2, %3" : "+&r" (r1), "+&r" (r2) : "r" (a), "r" (b));
  printf("smlal: 0x%08x 0x%08x 0x%08x 0x%08x\n", a, b, r1, r2);

  r1 = t1; r2 = t2;
  __asm__ ("umlal %0, %1, %2, %3" : "+&r" (r1), "+&r" (r2) : "r" (a), "r" (b));
  printf("umlal: 0x%08x 0x%08x 0x%08x 0x%08x\n", a, b, r1, r2);
}

int main()
{
  int i, j, k, l;

  for (i = 0; i < 4; i++)
    {
      for (j = 0; j < 16; j++)
        {
          for (k = 0; k < 4; k++)
            {
              for (l = 0; l < 16; l++)
                {
                  uint32_t a, b;
                  a = (uint32_t)(i << (j * 2));
                  b = (uint32_t)(k << (l * 2));
                  mul(0, 0, a, b);
                  printf("\n");
                }
            }
        }
    }

  return 0;
}
