int main() {
  int x[10], i;

  asm("movnv     r0, r0");
  x[0]=x[1]=1;

  for (i=2; i<10; i++) {
    x[i] = x[i-1] + x[i+2];
  }

  asm("movnv     r0, r0");

  return 0;
}
