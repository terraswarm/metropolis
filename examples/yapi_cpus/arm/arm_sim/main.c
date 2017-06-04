/*  main.c -- top level of ARMulator:  ARM6 Instruction Emulator.
 */

#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>                // TCM
#include <string.h>
//#include <io.h>     // TCM
#include "bfd.h"
#include "armdefs.h"
#include "armemu.h"

/* Added by TCM */
int do_trace = 0;
int do_dump = 0;
FILE *trace_file = NULL;
FILE *dump_file = NULL;
char trace_file_name[100];

static char info[] =
  "ARMulator, based on GDB 5.0 version, Sauer's v0.1 build\n";
static char usage[] = "Usage: %s obj-filename\n";

struct ARMul_State *arm1;        /* ARMulator state              */
static int mem_size = (1 << 21);        /* Memory size in bytes. TCM uncommented */
static int verbosity = 1;        /* start up banner, maybe other */
int big_endian = 0;                /* Non-zero:set big endian mode */
int stop_simulator;                /* somehow used in armemu.c     */

/* prototypes (added by TCM for c++ compilation) */
void sim_load (ARMul_State * state, const char *prog);


// allocate and initilize a new armulator instance
static ARMul_State *
new_armulator (void)
{

  ARMul_State *state;

  ARMul_EmulateInit ();
  state = ARMul_NewState ();
  state->bigendSig = (big_endian ? HIGH : LOW);
  ARMul_MemoryInit (state, mem_size);        // TCM uncommented
  ARMul_OSInit (state);
  ARMul_CoProInit (state);
  state->verbose = verbosity;

  ARMul_SelectProcessor (state, ARM_XScale_Prop);
  ARMul_SetCPSR (state, USER32MODE);
  ARMul_Reset (state);

  /* added by TCM */
  prev_pc = -1;
  prev_inst_count = -1;

  return (state);
}

/**************************************************************/
/*                                                            */
/**************************************************************/

int
main (int argc, char *argv[])
{
  int i;                        // TCM

  printf (info);

  // handle arguments if any
  if (argc < 2)
    {                                // TCM
      printf ("Invalid commandline\n");
      printf (usage, argv[0]);
      return 1;
    }
  /*** Begin: Added by TCM
       process non-program arguments ***/
  for (i = 2; i < argc; i++)
    {
      if (strcmp (argv[i], "dump") == 0)
        {
          i++;
          do_dump = 1;
          if (i >= argc)
            {
              printf ("Must specify a dump file\n");
              return 1;
            }
          dump_file = fopen (argv[i], "w");
          printf ("Doing Dump\n");
        }
      else if (strcmp (argv[i], "trace") == 0)
        {
          i++;
          do_trace = 1;
          if (i >= argc)
            {
              printf ("Must specify a trace file\n");
              return 1;
            }
          strcpy (trace_file_name, argv[i]);
          trace_file = fopen (trace_file_name, "w");
          printf ("Doing Trace: %s\n", argv[i]);
          //fprintf(trace_file, "XXXX\n");
          //fclose(trace_file);
        }
      else if (strcmp (argv[i], "memtrace") == 0)
        {
          i++;
          do_trace = 2;
          if (i >= argc)
            {
              printf ("Must specify a trace file\n");
              return 1;
            }
          strcpy (trace_file_name, argv[i]);
          trace_file = fopen (trace_file_name, "w");
          printf ("Doing Trace: %s\n", argv[i]);
          //fprintf(trace_file, "XXXX\n");
          //fclose(trace_file);
        }
    }
  /*** End: Added by TCM ***/

  verbosity = 1;



  arm1 = new_armulator ();
  sim_load (arm1, argv[1]);

  /*** Begin: TCM ***/
  if (do_dump)
    {
      fclose (dump_file);
      // continue execution
      if (!do_trace)
        {
          return -1;
        }
    }
  /*** End: TCM ***/
  //return -1; // TCM, to exit for debug

  while (1)
    {
      int pc = ARMul_DoProg (arm1);
      arm1->Reg[15] = pc;
      arm1->NextInstr |= PRIMEPIPE;
    }
}


void
ARMul_ConsolePrint (ARMul_State * state, const char *format, ...)
{
  va_list ap;
  if (state->verbose)
    {
      va_start (ap, format);
      vprintf (format, ap);
      va_end (ap);
    }
}

ARMword
ARMul_Debug (ARMul_State * state, ARMword pc, ARMword instr)
{
  // printf( "ARMul_Debug\n");
  // state->Emulate = STOP;
  // stop_simulator = 1;
  return instr;
}

int
sim_write (ARMul_State * state, ARMword addr, unsigned char *buffer, int size)
{
  int i = 0;
  int j;
  char dump_buff[4];
  unsigned long utemp;

  printf ("SIM_WRITE: size: %d\n", size);        // TCM
  while (size--)
    {
      ARMul_WriteByte (state, addr, buffer[i]);
    /*** BEGIN: TCM ***/
      //printf("i: %d  buff:%d\n", i-1, buffer[i-1]); // TCM
      if (do_dump)
        {
          dump_buff[i % 4] = buffer[i];
          if ((i % 4) == 3)
            {
              utemp = 0;
              for (j = 0; j < 4; j++)
                {
                  // little endian shift?
                  utemp =
                    utemp | (((unsigned long) dump_buff[j] & 0xff) <<
                             (j * 8));
                  //utemp = utemp << 8;
                }
        /********************* OLD WAY (big endian?)
        for (j=0; j<3; j++) {
          utemp = utemp|((unsigned long)dump_buff[j]&0xff);
          utemp = utemp << 8;
        }
        utemp = utemp|((unsigned long)dump_buff[3]&0xff);
        ****************************/
              fprintf (dump_file, "%x %x\n", (addr - 3), utemp);
              //fprintf(dump_file, "%08x %08x | %02x %02x %02x %02x\n", (addr-3), utemp, dump_buff[0], dump_buff[1], dump_buff[2], dump_buff[3]);
            }
          //fputc(buffer[i], dump_file);
          // do something with this here
        }
      i++;
    /*** END: TCM ***/
      addr++;
    }
  return size;
}

bfd *
sim_load_file (ARMul_State * state, const char *prog, int verbose_p)
{
  asection *s;
  bfd *result_bfd;
  unsigned long data_count = 0;        /* Number of bytes transferred to memory */
  int found_loadable_section;

  result_bfd = bfd_openr (prog, 0);
  if (result_bfd == NULL)
    {
      fprintf (stderr, "sim_load_file: can't open \"%s\": %s\n", prog,
               bfd_errmsg (bfd_get_error ()));
      return NULL;
    }

  if (!bfd_check_format (result_bfd, bfd_object))
    {
      fprintf (stderr, "sim_load_file: \"%s\" is not an object file: %s\n",
               prog, bfd_errmsg (bfd_get_error ()));
      bfd_close (result_bfd);
      return NULL;
    }

  found_loadable_section = 0;
  for (s = result_bfd->sections; s; s = s->next)
    {
      //fprintf(stderr,"XXX..\n");
      if (s->flags & SEC_LOAD)
        {
          bfd_size_type size;
          size = bfd_get_section_size_before_reloc (s);
          fprintf (stderr, "SIZE: %d\n", size);
          if (size > 0)
            {
              char *buffer;
              bfd_vma lma;
              buffer = (char *) malloc (size);
              if (buffer == NULL)
                {
                  fprintf (stderr,
                           "sim_load_file: insufficient memory to load \"%s\"\n",
                           prog);
                  bfd_close (result_bfd);
                  return NULL;
                }
              lma = bfd_section_vma (result_bfd, s);
              if (verbose_p)
                {
                  printf ("Loading section %s, size 0x%08lx vma ",
                          bfd_get_section_name (result_bfd, s),
                          (unsigned long) size);
                  printf ("0x%lx", (unsigned long) lma);
                  printf ("\n");
                }
              data_count += size;
              bfd_get_section_contents (result_bfd, s, buffer, 0, size);
              sim_write (state, lma, (unsigned char *) buffer, size);
              found_loadable_section = 1;
              free (buffer);
            }                        // size>0
        }                        // SEC_LOAD
    }                                // all sections
  if (!found_loadable_section)
    {
      fprintf (stderr, "sim_load_file: no loadable sections \"%s\"\n", prog);
      bfd_close (result_bfd);
      return NULL;
    }

  if (verbose_p)
    {
      printf ("Start address ");
      printf ("0x%lx", (unsigned long) bfd_get_start_address (result_bfd));
      printf ("\n");
    }

  return result_bfd;
}

void
sim_load (ARMul_State * state, const char *prog)
{
  bfd *prog_bfd;
  prog_bfd = sim_load_file (state, prog, 1);

  if (prog_bfd == NULL)
    {
      fprintf (stderr, "Unable to load %s\n");
      return;
    }
  ARMul_SetPC (state, bfd_get_start_address (prog_bfd));
  bfd_close (prog_bfd);
  return;
}
