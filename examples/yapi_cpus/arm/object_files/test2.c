#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

/******************************************************************************
 * Here are some headings for Windows' bitmaps.
 */
typedef unsigned long  DWORD;
typedef unsigned short WORD;
typedef long           LONG;
typedef unsigned char  BYTE;

typedef struct MY_BITMAPFILEHEADER {
        WORD    mbType;
        DWORD   mbSize;
        WORD    mbReserved1;
        WORD    mbReserved2;
        DWORD   mbOffBits;
} BITMAPFILEHEADER;
typedef BITMAPFILEHEADER *PBITMAPFILEHEADER;

typedef struct myBITMAPINFOHEADER{
        DWORD      miSize;
        LONG       miWidth;
        LONG       miHeight;
        WORD       miPlanes;
        WORD       miBitCount;
        DWORD      miCompression;
        DWORD      miSizeImage;
        LONG       miXPelsPerMeter;
        LONG       miYPelsPerMeter;
        DWORD      miClrUsed;
        DWORD      miClrImportant;
} BITMAPINFOHEADER;
typedef BITMAPINFOHEADER *PBITMAPINFOHEADER;

typedef struct myRGBTRIPLE {
        BYTE    rgbBlue;
        BYTE    rgbGreen;
        BYTE    rgbRed;
} RGBTRIPLE;


#define BUFFER 215094
#define SRC_FILE "../test_apps/data/coffee.bmp"
#define DEST_FILE "/tmp/coffee.bmp"
#define HEADER_SIZE 54

char src[BUFFER];
char dest[BUFFER];

#define LOC(_x,_y)   (_x) + ((_y) * pInfo->miWidth)

/* 3x3, left to right, top to bottom, values for edge detection matrix */
const static int CM_EDGE[] = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
const static int CM_TEST[] = {0, 0, 0, 0, 1, 0, 0, 0, 0};

/* Applies a Matrix filter to a pixel */
#define MUNG(_p,_c,_x,_y,_m) \
            (_p[LOC(_x-1,y-1)]._c * _m[0]) + \
            (_p[LOC(_x,y-1)]._c * _m[1]) + \
            (_p[LOC(_x+1,y-1)]._c * _m[2]) + \
            (_p[LOC(_x-1,y)]._c * _m[3]) + \
            (_p[LOC(_x,y)]._c * _m[4]) + \
            (_p[LOC(_x+1,y)]._c * _m[5]) + \
            (_p[LOC(_x-1,y+1)]._c * _m[6]) + \
            (_p[LOC(_x,y+1)]._c * _m[7]) + \
            (_p[LOC(_x+1,y+1)]._c * _m[8])

int main()
{
  int fd1, fd2;
  PBITMAPINFOHEADER pInfo;
  RGBTRIPLE* pDataSrc;
  RGBTRIPLE* pDataDest;
  int x, y;

  if ((fd1 = open(SRC_FILE, O_RDONLY)) == -1)
    {
      write(STDERR_FILENO, "Error on open 1\n", 16);
      return 0;
    }
  if (read(fd1, src, BUFFER) != BUFFER)
    {
      write(STDERR_FILENO, "Error on reading\n", 17);
      return 0;
    }
  close(fd1);

  memcpy(dest, src, HEADER_SIZE);

  pInfo = (PBITMAPINFOHEADER)((char*)src + 14);
  pDataSrc = (RGBTRIPLE*)((char*)src + HEADER_SIZE);
  pDataDest = (RGBTRIPLE*)((char*)dest + HEADER_SIZE);
  for (y = 1; y < (pInfo->miHeight - 1); y++)
    {
      for (x = 1; x < (pInfo->miWidth - 1); x++)
        {
          pDataDest[LOC(x,y)].rgbRed =
            MUNG(pDataSrc, rgbRed, x, y, CM_EDGE);
          pDataDest[LOC(x,y)].rgbGreen =
            MUNG(pDataSrc, rgbGreen, x, y, CM_EDGE);
          pDataDest[LOC(x,y)].rgbBlue =
            MUNG(pDataSrc, rgbBlue, x, y, CM_EDGE);
        }
    }

  if ((fd2 = creat(DEST_FILE, S_IRWXU | S_IRGRP | S_IROTH)) == -1)
    {
      write(STDERR_FILENO, "Error on open 2\n", 16);
      return 0;
    }
  if (write(fd2, dest, BUFFER) != BUFFER)
    {
      write(STDERR_FILENO, "Error on writing\n", 17);
      return 0;
    }
  close(fd2);

  write(STDERR_FILENO, "Done\n", 5);

  return 0;
}
