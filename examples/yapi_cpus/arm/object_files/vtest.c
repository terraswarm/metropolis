#include <unistd.h>
#include <stdarg.h>
#include <inttypes.h>
#include <profile.h>

void vfoo(int count, va_list ap)
{
  int i;
  char* str;
  int len;

  for (i = 0; i < count; i++)
    {
      str = va_arg(ap, char*);
      len = va_arg(ap, int);
      write(2, str, len);
    }
}

void foo(int count, ...)
{
  va_list ap;

  va_start(ap, count);

  vfoo(count, ap);

  va_end(ap);
}

int main()
{
  uint32_t cc1, cc2;
  uint32_t cm1, cm2;
  uint32_t ch1, ch2;

  cc1 = rpcc();
  ch1 = rpch();
  cm1 = rpcm();
  foo(1, "wibble\n", 8);
  cc2 = rpcc();
  ch2 = rpch();
  cm2 = rpcm();

  printf("cc = %d\tch = %d\tcm = %d\n", cc2 - cc1, ch2 - ch1, cm2 - cm1);

  return 0;
}
