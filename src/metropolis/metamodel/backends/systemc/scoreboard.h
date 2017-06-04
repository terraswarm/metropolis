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
#ifndef MMSCS_SCOREBOARD_H
#define MMSCS_SCOREBOARD_H

#include "global.h"
#ifdef __MINGW32__
#define METRO_NO_RESOURCE_H
#define METRO_NO_UNISTD_H
#endif

#if (defined(__WIN32__) || defined(_WIN32)) && !defined(__CYGWIN32__)
/* Microsoft Visual C++ */
#define METRO_NO_RESOURCE_H
#define METRO_NO_UNISTD_H
#endif

#ifndef METRO_NO_RESOURCE_H
#include "sys/resource.h"
#endif

#ifndef METRO_NO_UNISTD_H
#include "unistd.h"
#endif

#include <map>
#include <set>
#include <vector>

class node;
class event;
class ProgramCounter;
class statemedium;
class netlist;
class Behavior;
class Nondet;
class QuantityManagerLOC;

class scoreboardManagerIntfc : virtual public sc_interface
{
    public:
virtual void registerProcess(ProgramCounter * pc) = 0;
    virtual bool eval() = 0;

    virtual bool schedule() = 0;
    //        virtual bool pass1() = 0;
    //        virtual bool pass2() = 0;
    //        virtual bool pass3() = 0;
    //        virtual bool resolveSynch() = 0;
    //        virtual bool set2run() = 0;
    virtual void postSet() = 0;
    virtual void preSet() = 0;
    virtual void invoke() = 0;
    virtual void initialize() = 0;
    virtual void showSB() = 0;
};

class scoreboard : public sc_channel, public scoreboardManagerIntfc {
    public:
    std::vector<ProgramCounter *> procList;  // scoreboard of the whole system
    std::vector<medium *> mediaList;
    std::map<int, QuantityManagerLOC*> BILOCQuantities;
    netlist *topNetlist;                                // top level netlist

    friend class Behavior;

    int size; //total number of processes
    ProgramCounter ** pcVector;
    int msize; //total number of media
    medium ** mediaVector;

    int numSynchEventGroup;
    int *numSynchEventEachGroup;
    int *enabledSynchEventEachGroup;
    ProgramCounter* ** enabledSynchEventPC;
    typedef std::pair<int, int> intpair;
    typedef std::map<event*, intpair> eventGroups;
    eventGroups synchEventGroup;
    PCSet_t synchPC;
    PCSet_t synchImplPC;
    PCSet_t ltlPC;
    PCSet_t caredPC;

    int numSynchImplyGroup;
    typedef std::set<Variable*> equalVarSet_t;
    typedef std::map<Variable*, equalVarSet_t*> ndVarMap_t;
    ndVarMap_t ndVars;

    short maxRefCount;
    short cyclesB4QA;

    typedef std::map<EventMap *, EventMap *> eventSynchMap_t;
    eventSynchMap_t _eventSynchMap;

    typedef std::set<GlobalTime *> gtimeSet_t;
    gtimeSet_t gtimeSet;

    // Choose whether or not LTL constraints are enforced by simulator.
    // Default option is YES.
    bool ltl;

    // Choose whether or not interleaving concurrency is used.
    // Default option is YES.
    //bool interleaving;

    SC_HAS_PROCESS(scoreboard);
    scoreboard(sc_module_name name);

    //Register all processes to scoreboard
    void registerProcess(ProgramCounter * pc);

    //Register all media to scoreboard
    void registerMedium(medium * m);

    //Initialize simulation
    void initialize();

    //Find out if there are any await statements waiting to evaulate
    //conditions.
    bool eval();

    //Before restart of manager cycle, change DONTRUN to UNKNOW for
    //interface function, block or label
    //For await statements, create csOrder[].
    void preSet();

    //Adjust pc->selected to actual choice stored in csOrder[]
    //Clean up pc->csOrder[] created in preSet()
    void postSet();

    //Do scheduling
    bool schedule();

    //Self-check done by individual process
    void check(ProgramCounter *pPC);

    //Update corresponding synch event info
    void updateSynchEventInfo(int eventGroupID, ProgramCounter* eventPC);

    //Return whether all events in the ith synch event group are enabled
    bool isSynchEventGroupEnabled(int ith);

    //Due to the equal variables comparison,
    //enable/disable the ith group of synched events
    void setSynchEventGroupStatus(int ith, bool satisfied);

    //add (synch event, <beg group, end group>) info to scoreboard
    void addSynchEventGroupInfo(process *p, sc_object *m, const char *c, ActionState as, int groupNum);

    //Collect the reference count of a CS in await
    void collect_ref_count();

    //Register events appearing in synch to scoreboard
    void registerSynchEvents(event *e1, event *e2);

    //Get the paired synch event
    EventMap *getEventSynchMap(event *e, bool pair);

    //Remove all built-in LOC registries from quantity managers
    void clearBuiltInLOC(process * p);

    //Print out all scoreboard
    void showSB();

    //Print out an event 
    void dumpEvent(ProgramCounter *pc);

    //Invoke all runnable processes after scheduling
    void invoke();

    //Process command line arguments
    bool processArgs();

    //Set processes from UNKNOW state to RUN state
    bool set2run();

    // set a process to DONTRUN performing some necessary cleanup steps
    void set2dontrun(ProgramCounter * pc);

    //Get breakpoint line numbers for all next possible critical sections
    strList_t getAllNextLines();

    // Get the current line numbers of all blocked processes
    strList_t getAllCurrentLines();

    // Get the eventTags from all the ProgramCounters.
    strList_t getAllEventTags();

    // Get the SchedStates from all the ProgramCounters.
    strList_t getAllSchedStates();

    // Update the list of all the ProgramCounters' current-line-number
    // strings.
    void updateCurrentLines();

    // A list of Strings identifying the current mmm source code lines
    // of all the processes.
    strList_t *currentLineList;

    // A list of the event-tag strings for all the processes.
    strList_t *eventTagList;

    // A list of the scheduling state strings for all the processes.
    strList_t *schedStateList;

    private:
    // ZChaff SAT solver manager
    SAT_Manager SatMgr; //=void * sbSatMgr;

    // Map each event to a ZChaff variable index
    eventVarIndexMap_t  eventVarIndex;

    // Map each event to a ZChaff variable index
    conflictRelationSet_t  conflictRSet;

    //A little like pass2(). Collect all conflicts among all pending events
    void collectEventConflicts();

    // Initialize SAT solver ZChaff
    void initSATSolver();

    // Free SAT solver ZChaff
    void freeSATSolver();

    // Create variables in and get their indices from ZChaff for all events in eventVarIndex
    void createVars(eventVarIndexMap_t&);

    // Add synch constraints as CNF's to ZChaff
    void addSynchClauses(eventVarIndexMap_t&, eventSynchMap_t&);

    // Add conflict constraints stored in conflictRSet as CNF's to ZChaff
    void addConflictClauses(eventVarIndexMap_t&, conflictRelationSet_t&);

    // Add number (lower bound) of 1-value variable constraints as CNF's to ZChaff
    void addNumberOfOnesClauses(eventVarIndexMap_t::iterator, int, int, intVect_t&);

    // Add LTL constraints coming from transition relations in Buchi Automaton
    void addLTLConstraintClauses(eventVarIndexMap_t&);

    // Solve the SAT problem by calling ZChaff
    enum SAT_StatusT  solveSAT();

    // Get the result from SAT solver
    void getSATResult(eventVarIndexMap_t&);

    // Apply SAT result to the scheduling
    bool applySATResult(eventVarIndexMap_t&);

    // Count the number of 1-value variables
    int countOnes(eventVarIndexMap_t&);

    // Randomly choose one event and set it (and its synched event, if any) to run if possible
    bool pickOneEventRandomly();

    // specify the process selection behavior
    Behavior *_pBehavior;

    // Choose scheduling algorithm among heuristicX(), X=0,1,2,3
    // by using the runtime argument "-heuristic X"
    bool (scoreboard::*SchedAlg)();

    // Scheduling algorithm 0: the original pass1-2-3... algorithm
    // This one is the default algorithm used.
    bool heuristic0();

    // Scheduling algorithm 1: randomly choose one process to run
    bool heuristic1();

    // Scheduling algorithm 2: call zchaff to find a set of consistent processes to run

    bool heuristic2();

    // Scheduling algorithm 3: iteratively call zchaff to find a MAX set of consistent processes to run
    bool heuristic3();

    //First scan. Find out which UNKNOW process could run.
    bool pass1();

    //Second scan. Find out wether there is any inconsistence if all processes from pass1 begin to run.
    bool pass2();

    //Third scan. Constraints check. Not yet implemented.
    bool pass3();

    //Forth scan. Do user specified _doScheduling.
    //bool pass4(){} = _doScheduling in Scheduler

    //Forth scan. Resolve synch constraints
    bool resolveSynch0(ProgramCounter *pPC);

    //Forth scan. Resolve synch constraints
    bool resolveSynch1(ProgramCounter *pPC);

    //Forth scan. Resolve synch constraints
    bool resolveSynch();

    //Collect Nondet variable information in synch imply constaints
    void setupSynchImplicationInfo();

    //Forth scan. Resolve synch imply constraints
    bool resolveSynchImply();

    //Set Nondet variables
    void finalizeNondetVars();

    //Call postpostcond for all GlobalTime instances
    void postpostcond();
    
    //Check there is at least one process is set to RUN
    //bool atLeastOneRun();

    // fill the vector of _pcBlocked according to synchs
    void set2blocked(ProgramCounter * pc, ProgramCounter * pc_blocked);

    //Could pc run?
    bool couldRun(ProgramCounter * pc);

    //Could pc(await statement) run under the influence of cmp(DONTRUN/UNKNOW/RUNNING)?
    bool couldRunAwait(ProgramCounter * pc, ProgramCounter * cmp);

    //Could pc(interface function) run under the influence of cmp(DONTRUN/UNKNOW/RUNNING)?
    bool couldRunIntfc(ProgramCounter * pc, ProgramCounter * cmp);

    //Could pc run together with cmp
    bool couldRunBoth(int mode, ProgramCounter * pc, ProgramCounter * cmp);

    //check if one node is in a set of nodes
    bool contained(node * n, simpleList_t *pList);

    //check if two sets of nodes intersects
    bool intersect(simpleList_t *pList1, simpleList_t *pList2);
};

#endif
