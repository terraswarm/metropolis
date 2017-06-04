/*
  Copyright (c) 2003-2005 The Regents of the University of California.
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
#ifndef MMSCS_GLOBAL_H
#define MMSCS_GLOBAL_H

#ifdef _MSC_VER
/* Under Microsoft Visual C, print the
   'identifier was truncated to '255' characters in the debug information'
   only once. */
#pragma warning( once : 4786 )

/* MSVC 6.0 does not have random()*/
#define random() rand()
#define srandom srand
#endif

#include <systemc.h>
#include <vector>
#include <map>
#include <set>
#include "ltl2ba-1.0/ltl2ba.h"
#include "zchaff/SAT.h"

//xichen_loc_beg
#include <fstream.h>
//xichen_loc_end

class ProgramCounter;
class node;
class event;
class String;
class medium;
class Variable;
class LTLSynchImply;

/****************** Parameters used in compilation *******************/
/** Time step during simulation **/
#define DeltaCycle SC_ZERO_TIME


/******************************* Types *******************************/
typedef class quantity Quantity ;
typedef class ProgramCounter SchedProgramCounter;
typedef std::vector<node *> simpleList_t;
typedef std::vector<simpleList_t *> _2DList_t;
typedef std::vector<_2DList_t *> _2DListStack_t;
typedef std::vector<int *> intList_t;
typedef std::vector<String *> strList_t;
typedef std::vector<strList_t *> _2DStrList_t;
typedef std::vector<_2DStrList_t *> _2DStrListStack_t;
typedef std::vector<event *> eventList_t;
typedef std::map <event *, int> eventMap_t;
typedef std::vector<int> intVect_t;
typedef std::vector<ProgramCounter *> pcList_t;
typedef std::map<event*, int> eventVarIndexMap_t;
typedef std::pair<event *, event *> conflictRelation_t;
typedef std::set<conflictRelation_t> conflictRelationSet_t;
typedef std::set<medium*> MediaSet_t;
typedef std::vector<MediaSet_t *> MediaSetList_t;
typedef std::multiset<ProgramCounter*> userPCs_t;
struct ltstr {
    bool operator() (const char* s1, const char* s2) const {
        return strcmp(s1, s2) < 0;
    }
};
typedef std::map<const char*, userPCs_t*, ltstr> interfaceUsers_t;
typedef std::set<ProgramCounter*> PCSet_t;

enum ActionState { ACTION_STATE_BEGIN=0, ACTION_STATE_END,
                   ACTION_STATE_NONE, ACTION_STATE_OTHER};

class EventMap {
    public:
EventMap(event *e) : _e(e) { _refCount = 0; }
    event *_e;
    short _refCount;
};



/** Constants declaration **/
class RunType {
    public:
#if _MSC_VER == 1200
// MSVC 6.0 fix, see
// http://dcplusplus.sourceforge.net/forum/viewtopic.php?t=5528&sid=5235155655ad2cfd93dd7462750cb3d9
enum {EXEC = 0};
    enum {TRY = 0};
#else
    const static int EXEC = 0;
    const static int TRY  = 1;

#endif // _MSC_VER == 1200


};

class SchedStateVal {
    public:
#if _MSC_VER == 1200
enum {RUN     = 1};
    enum {DONTRUN = 2};
    enum {RUNNING = 3};
    enum {UNKNOW = 4};

    enum {EVALUATE= 5};
    enum {FINISHED= 6};
    enum {TESTSCHEDULING= 7};
#else
    const static int RUN     = 1;
    const static int DONTRUN = 2;
    const static int RUNNING = 3;
    const static int UNKNOW = 4;

    const static int EVALUATE= 5;
    const static int FINISHED= 6;
    const static int TESTSCHEDULING= 7;
#endif // _MSC_VER == 1200
};

class BuiltInLOCType {
    public:
#if _MSC_VER == 1200
    enum {MAXRATE = 1};
    enum {MINRATE = 2};
    enum {PERIOD  = 3};
    enum {MAXDELTA = 4};
    enum {MINDELTA = 5};
#else
    const static int MAXRATE = 1;
    const static int MINRATE = 2;
    const static int PERIOD  = 3;
    const static int MAXDELTA = 4;
    const static int MINDELTA = 5;
#endif // _MSC_VER == 1200
};

class FunctionType {
    public:
#if _MSC_VER == 1200
enum {AWAIT                        = 0x0001};
    enum {LABEL                        = 0x0002};
    enum {INTFCFUNC                        = 0x0004};
    enum {ANNOTATION                = 0x0008};
#else
    const static int AWAIT                        = 0x0001;
    const static int LABEL                        = 0x0002;
    const static int INTFCFUNC                = 0x0004;
    const static int ANNOTATION                = 0x0008;
#endif // _MSC_VER == 1200
};

class VarType {
    public:
#if _MSC_VER == 1200
    enum { BOOLTYPE   = 0};
    enum { CHARTYPE   = 1};
    enum { DOUBLETYPE = 2};
    enum { FLOATTYPE  = 3};
    enum { INTTYPE    = 4};
    enum { LONGTYPE   = 5};
    enum { STRINGTYPE = 6};
    enum { BYTETYPE   = 7};
    enum { SHORTTYPE  = 8};
#else
    const static int BOOLTYPE   = 0;
    const static int CHARTYPE   = 1;
    const static int DOUBLETYPE = 2;
    const static int FLOATTYPE  = 3;
    const static int INTTYPE    = 4;
    const static int LONGTYPE   = 5;
    const static int STRINGTYPE = 6;
    const static int BYTETYPE   = 7;
    const static int SHORTTYPE  = 8;
#endif // _MSC_VER == 1200
};

/************************ Supporting classes ************************/

/** Dummy classes for exceptions and arguments **/
class STUCK {};
class SIMPANIC {};
class DUMMY_CTOR_ARG {};

/******************************* Includes *******************************/

//#define Empty_String ""
//#define Keep_Unchanged_String "__KEEP_UNCHANGED__"

#define SEG(i) ((int)((i)/(8*sizeof(int))))
#define BIT(i)  ((i)%(8*sizeof(int)))
#define POSLIT(i)    (2*(i))
#define NEGLIT(i)  (2*(i)+1)

#define LAST -1

extern bool ANNOTATION_ENABLED;
extern int * procorder;
extern int * schorder;
extern int argc;
extern char ** argv;
extern int mode;
extern long NumSMSchedulingRounds;
extern long NumRunProcesses;
extern long NumIterH3;
extern short heuristic;
extern long SMSchedulingTime;
extern long SimTotalTime;
extern bool timestat;
extern bool debug_flag;
extern short debug_level;
extern unsigned long gxi;
extern unsigned long evgxi;
extern bool dumpev;
extern bool showmedia;
extern bool synchonly;
extern bool ltlonly;
extern bool interleaving;
extern short maxCycle;
extern short minCycle;
extern bool testSchedFlag;
extern bool allProcStuck;
extern bool requestQA;
extern short maxQAProc;
extern long gxiindex;
extern long numqaproc;
extern long numQR;
extern long numGTQR;
extern PCSet_t reqPCs;
class manager;
extern manager _mng;
class process;
extern process * caller;
class scoreboard;
extern scoreboard  _sb;
extern const char * Empty_String;
extern const char * Keep_Unchanged_String;
extern const char * intfcName;
extern LTLSynchImply ** ltlsi;
extern bool* debugmask;

//xichen_loc_beg
extern char * trace_file_name;
extern ofstream trace_file;
//xichen_loc_end

class GlobalTime;
//extern GlobalTime *GTime;

void sc_sim_stop();
void sc_sim_start(double);
void wait_debug(const sc_event&);
extern void process_equalvars_in_synch();

#ifdef MSC_VER
/* MSVC 6.0 does not have various function calls, so we ifdef them here */
#define METRO_NO_RANDOM
#include <c:/mingprocess.h>
#define getpid _getpid

#endif

#ifdef __MINGW32__
/* FIXME: if we do not fully specify process.h, then we end up with
   the local process.h by mistake */
#include <c:/mingw/include/process.h>
#define getpid _getpid

#define METRO_NO_RANDOM
#endif

#ifdef METRO_NO_RANDOM
#define random() rand()
#define srandom srand
#endif /*METRO_NO_RANDOM*/

#endif
