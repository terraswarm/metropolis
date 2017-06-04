#include <stdio.h>
#include <swarm.h>

int main()
{
  uint32_t pid;

  /* Have a look at the processor before I begin */
  printf("Here's the initial dump:\n");
  _dump();

  /* Now try and set the current process ID */
  _setcurpid(0x42);

  /* See if we made a difference */
  printf("\n\nHaving set the pid:\n");
  _dump();

  /* Now let's read back that pid */
  pid = _getcurpid();
  printf("\n\nThe pid we read was 0x%X\n", pid);

  return 0;
}
