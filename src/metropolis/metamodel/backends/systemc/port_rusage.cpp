/*
  Copyright (c) 2003-2004 The Regents of the University of California.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the
  above copyright notice and the following two paragraphs appear in all
  copies of this software and that appropriate acknowledgments are made
  to the research of the Metropolis group.

  IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
  FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
  ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
  THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
  SUCH DAMAGE.

  THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
  PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.


  METROPOLIS_COPYRIGHT_VERSION_1
  COPYRIGHTENDKEY

*/

/*
  Windows does not have a native getrusage(), but psapi.dll and psapi.lib
  provide this functionality.

  http://www.microsoft.com/msdownload/platformsdk/sdkupdate/psdkredist.htm

  psapi.h, psapi.lib and psapi.dll can be found at
  http://d0.phys.washington.edu/Projects/l3/auto_start/building/Default.htm

  This source code is from:
  http://forum.java.sun.com/thread.jsp?thread=303055&forum=52&message=1211261
*/
#include "port_rusage.h"

#ifdef _MSC_VER
#include <windows.h>
#include "psapi.h"
#include <process.h>
#else
#include <sys/resource.h>
#endif

#include <stdio.h>

#ifdef WIN32
int WIN32_getrusage(int who, struct rusage *usage) {
    HANDLE hProcess;
    PROCESS_MEMORY_COUNTERS pmc;
    hProcess = OpenProcess(PROCESS_QUERY_INFORMATION |
            PROCESS_VM_READ,
            FALSE, GetCurrentProcessId());
    {
        FILETIME ftCreate, ftExit, ftKernel, ftUser;
        if (GetProcessTimes(hProcess, &ftCreate, &ftExit, &ftKernel, &ftUser)) {
            LONGLONG tUser64 = (*(LONGLONG *)&ftUser / 10);
            LONGLONG tKernel64 = (*(LONGLONG *)&ftKernel / 10);
            usage->ru_utime.tv_sec =(long)(tUser64 / 1000000);
            usage->ru_stime.tv_sec =(long)(tKernel64 / 1000000);
            usage->ru_utime.tv_usec =(long)(tUser64 % 1000000);
            usage->ru_stime.tv_usec =(long)(tKernel64 % 1000000);
        } else {
            CloseHandle( hProcess );
            return -1;
        }
    }
    if (GetProcessMemoryInfo( hProcess, &pmc, sizeof(pmc))) {
        usage->ru_maxrss=(DWORD)(pmc.WorkingSetSize /1024);
        usage->ru_majflt=pmc.PageFaultCount;
    } else {
        CloseHandle( hProcess );
        return -1;
    }
    CloseHandle( hProcess );
    return 0;
}
#endif

// void util_getrusage(struct rusage *r) {
//     memset(r, '\0', sizeof(struct rusage));
//     getrusage(RUSAGE_SELF, r);
// }

// double rusage_cputime(struct rusage *r) {
//     return (double) r->ru_stime.tv_sec +
//         (double) r->ru_utime.tv_sec +
//         (double) r->ru_stime.tv_usec / 1000000.0 +
//         (double) r->ru_utime.tv_usec / 1000000.0;
// }

// void PrintRusage(void) {
//     struct rusage rusage;
//     util_getrusage(&rusage);
//     fprintf(stderr, "CPU Usage: %.3f seconds = %.3f user + %.3f sys\n",
//             rusage_cputime(&rusage),
//             rusage.ru_utime.tv_sec + ((double) rusage.ru_utime.tv_usec / 1000000.0),
//             rusage.ru_stime.tv_sec + ((double) rusage.ru_stime.tv_usec / 1000000.0));
//     printf("Memory in use: %ld\n",rusage.ru_maxrss*1024);
// }

// int main (int argc, char **argv) {
//     for (;;) {
//             // simulate leakage
//             void *x = malloc(1024);
//             PrintRusage();
//             Sleep(0);
//         }
//     return 1;
// }
