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

#include "global.h"
#include "manager.h"
#include "process.h"
#include "GlobalTime.h"

#include "port_rusage.h" // struct rusage

int * procorder = NULL;
int * schorder = NULL;
int argc;
char ** argv;
int mode = 0;
short heuristic;
long NumSMSchedulingRounds;
long NumRunProcesses;
long NumIterH3;
long SMSchedulingTime;
long SimTotalTime;
bool timestat;
bool debug_flag;
short debug_level;
unsigned long gxi;
unsigned long evgxi;
bool dumpev;
bool showmedia;
bool synchonly;
bool ltlonly;
bool interleaving;
short maxCycle;
short minCycle;
bool testSchedFlag;
bool allProcStuck = false;
bool requestQA = false;
short maxQAProc;
PCSet_t reqPCs;
bool ANNOTATION_ENABLED = true;
bool* debugmask;

long gxiindex = 1;
long numqaproc = 0;
long numQR = 0;
long numGTQR = 0;

//xichen_loc_beg
char * trace_file_name = ".trace";
ofstream trace_file;
//xichen_loc_end

#ifndef _MSC_VER
const int RunType::EXEC;
const int RunType::TRY;
const int SchedStateVal::RUN;
const int SchedStateVal::DONTRUN;
const int SchedStateVal::RUNNING;
const int SchedStateVal::UNKNOW;
const int SchedStateVal::EVALUATE;
const int SchedStateVal::FINISHED;
const int SchedStateVal::TESTSCHEDULING;
const int FunctionType::AWAIT;
const int FunctionType::LABEL;
const int FunctionType::INTFCFUNC;
const int FunctionType::ANNOTATION;
#endif

extern manager _mng;
process * caller = (process *) &_mng;
const char * Empty_String = "";
const char * Keep_Unchanged_String = "__KEEP_UNCHANGED__";
const char * intfcName = Keep_Unchanged_String;
LTLSynchImply ** ltlsi;
//GlobalTime *GTime = new GlobalTime("GTime");

void sc_sim_stop() {
    if (timestat) {
#ifndef __MINGW32__
        struct rusage ru;
        getrusage(RUSAGE_SELF, &ru);
        SimTotalTime = -SimTotalTime;
        SimTotalTime = ru.ru_utime.tv_sec*1000000+ru.ru_utime.tv_usec+
            ru.ru_stime.tv_sec*1000000+ru.ru_stime.tv_usec;
        cout<<endl<<"Total Simulation Time: "<<SimTotalTime<<" uS"<<endl;
        cout<<"Total Time Spent On Scheduling Rounds by Simulation Manager: "
            <<SMSchedulingTime<<" uS"<<endl;
        cout<<"Number of Scheduling Rounds Done by Simulation Manager: "
            <<NumSMSchedulingRounds<<endl;
        cout<<"Average Number of RUN Processes Scheduled per Scheduling Round: "
            <<(double)NumRunProcesses/(double)NumSMSchedulingRounds<<endl;
        if (heuristic == 3)
            cout<<"Average Number of Iterations to Find the MAX Number of RUN processes: "
                <<(double)NumIterH3/(double)NumSMSchedulingRounds;
        cout<<endl;
#endif // __MINGW32__
        cout<<"Total GXI "<< gxiindex <<endl;
        cout<<"GXIs with QA "<<numqaproc<<endl;
        cout<<"Total QR GXI "<<numQR<<endl;
        cout<<"Total GTQR GXI "<<numGTQR<<endl;
    } // end if
    sc_stop();
} // end sc_sim_stop

void sc_sim_start(double duration = -1) {
    if (timestat) {
#ifndef __MINGW32__
        struct rusage ru;
        getrusage(RUSAGE_SELF, &ru);
        SimTotalTime = ru.ru_utime.tv_sec*1000000+ru.ru_utime.tv_usec+
            ru.ru_stime.tv_sec*1000000+ru.ru_stime.tv_usec;
#endif // __MINGW32__

        NumSMSchedulingRounds = 0;
        NumRunProcesses = 0;
        NumIterH3 = 0;
        SMSchedulingTime = 0;
    }

    //sc_start( duration );
    sc_start();
} // end sc_sim_start
