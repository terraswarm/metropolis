/******************************************************************************
 * Simple filter program.
 *
 *
 *****************************************************************************/

#include <stdlib.h>
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

typedef unsigned int uint32_t;

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

#if 0
char src[BUFFER];
char dest[BUFFER];
#endif

#define LOC(_x,_y)   (_x) + ((_y) * pInfo->miWidth)

/* 3x3, left to right, top to bottom, values for edge detection matrix */
const static int CM_EDGE[] = {-1, 0, 1, -1, 0, 1, -1, 0, 1};
const static int CM_TEST[] = {0, 0, 0, 0, 1, 0, 0, 0, 0};
const static int CM_EMBOSS[] = {-1, 0, 0, 0, 0, 0, 0, 0, 1};
const static int CM_SHARPEN[] = {0, -1, 0, -1, 5, -1, 0, -1, 0};


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

  char* src;
  char* dest;

  /* Allocate memory for the processing */
  if ((src = (char*)malloc(BUFFER)) == NULL)
    {
      write(STDERR_FILENO, "Failed to allocate src\n", 23);
      return 0;
    }
  if ((dest = (char*)malloc(BUFFER)) == NULL)
    {
      write(STDERR_FILENO, "Failed to allocate dest\n", 24);
      return 0;
    }

  /* Try to read the data from the file */
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
          uint32_t temp;

          temp = MUNG(pDataSrc, rgbRed, x, y, CM_SHARPEN);
               pDataDest[LOC(x,y)].rgbRed =
            ((temp & 0xFFFFFF00) != 0) ?
            (((temp >> 31) == 0) ? 255 : 0) : temp;

          temp = MUNG(pDataSrc, rgbGreen, x, y, CM_SHARPEN);
          pDataDest[LOC(x,y)].rgbGreen =
            ((temp & 0xFFFFFF00) != 0) ?
            (((temp >> 31) == 0) ? 255 : 0) : temp;

          temp = MUNG(pDataSrc, rgbBlue, x, y, CM_SHARPEN);
          pDataDest[LOC(x,y)].rgbBlue =
            ((temp & 0xFFFFFF00) != 0) ?
            (((temp >> 31) == 0) ? 255 : 0) : temp;
        }
    }

#ifdef ARM
  if ((fd2 = open(DEST_FILE, O_CREAT | O_RDWR, S_IRWXU | S_IRGRP | S_IROTH)) == -1)
#else
  if ((fd2 = creat(DEST_FILE, S_IRWXU | S_IRGRP | S_IROTH)) == -1)
#endif
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

  free(src);
  free(dest);

  return 0;
}
