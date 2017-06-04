/*
  Copyright (c) 2003-2006 The Regents of the University of California.
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
#include "scoreboard.h"
#include "port_rusage.h"
#include "node.h"
#include "programcounter.h"
#include "manager.h"
#include "process.h"
#include "GlobalTime.h"
#include "global.h"
#include "statemediumDeclaration.h"
#include "netlist.h"
#include "medium.h"
#include "quantitymanager.h"
#include "action.h"
#include "event.h"
#include "argsutil.h"
#include "nondeterminism.h"
#include "ltlsynchimply.h"
#include <algorithm>
//#include "malloc.h"
#include "stdlib.h"
#include "sat_hook.h"

// include behaviors
#include "behavior.h"
#include "DefaultBehavior.h"


scoreboard::scoreboard(sc_module_name name) : sc_channel(name) {
    _pBehavior = new DefaultBehavior();
    end_module();
}

void scoreboard::registerProcess(ProgramCounter * pc) {
    procList.push_back(pc);
    pc->setScoreboard(this);
}

void scoreboard::registerMedium(medium * m) {
    mediaList.push_back(m);
}

void scoreboard::initialize() {
    std::vector<ProgramCounter *>::iterator procListIter = procList.begin();
    size = procList.size();
    maxQAProc = size;
    maxCycle = 20;
    minCycle = 10;
/*    maxCycle = (size > 10) ? 15 : 7;
    minCycle = (size > 10) ? 6 : 3;*/
    reqPCs.clear();
    pcVector = new ProgramCounter *[size];
    currentLineList = new strList_t;
    eventTagList = new strList_t;
    schedStateList = new strList_t;
    int i=0;
    for (; procListIter!=procList.end(); procListIter++) {
        pcVector[i] = (*procListIter);
        pcVector[i]->setScoreboard(this);
        i++;
    }
    _mng.pc->setScoreboard(this);

    std::vector<medium *>::iterator mediaListIter = mediaList.begin();
    msize = mediaList.size();
    mediaVector = new medium *[msize];
    i = 0;
    for (; mediaListIter!=mediaList.end(); mediaListIter++) {
        mediaVector[i] = (*mediaListIter);
        i++;
    }

    SatMgr = NULL;

    topNetlist->synch(0);
    if (ltl) {
        topNetlist->build_buchi();
        for (int k=0; k<topNetlist->num_event; k++) {
            ltlPC.insert(topNetlist->ltlEvents[k]->getProcess()->pc);
            if (ltlonly)
                caredPC.insert(topNetlist->ltlEvents[k]->getProcess()->pc);
        }//end for
    }//end if ltl

    setupSynchImplicationInfo();

    if (dumpev || evgxi>0) {
        cerr<<"**********["<<endl;
        cerr<<"1. Number of simulation cycles"<<endl;
        cerr<<"2. Can processes update quantity annotation requests?"<<endl;
        cerr<<"3. Are all processes that did not make quantity annotation requests stuck?"<<endl;
        cerr<<"4. Number of simulation cycles before next quantity resolution?"<<endl;
        cerr<<"5. Maximum number of processes that may request quantity annotations?"<<endl;
        cerr<<"6. Number of processes that made quantity annotation requests?"<<endl;
        cerr<<"] **********"<<endl;
    }

    //show synchEventGroup
    if (debug_flag || gxi>0) {
        if (synchEventGroup.size() > 0) {
            cerr<<"**************** synchEventGroup ****************"<<endl;
            cerr<<"Total number of synchEvent is "<<synchEventGroup.size()<<endl;
            event * e;
            for (eventGroups::iterator it = synchEventGroup.begin(); it!=synchEventGroup.end(); it++) {
                e = it->first;
                intpair p = it->second;
                cerr<<"("<<e->getProcess()->name()<<", ";
                cerr<<e->getAction()->getObject()->name()<<", ";
                cerr<<e->getAction()->getName()<<") <";
                cerr<<p.first<<", "<<p.second<<">"<<endl;
            }
        }
    }

        debugmask = new bool[22];
        char * dmenv = getenv("MMMDEBUGMASK");
        if (dmenv != NULL) {
                for (i=0; i<22; i++)
                        debugmask[i] = false;
                for (i=0; dmenv[i]!='\0'; i++) {
                        if (dmenv[i] < 'a' || dmenv[i] > 'v') continue;
                        debugmask[ (int)(dmenv[i]-'a') ] = true;
                }
        } else {
                for (i=0; i<22; i++)
                        debugmask[i] = true;
                cerr<<"Finer control on debugging information is possible."<<endl;
                cerr<<"Refer to metro/src/metropolis/metamodel/backends/systemc/debug.txt"<<endl;
        }

} // end initialize

// process command line arguments
bool scoreboard::processArgs() {
    //default configuration of the simulator
    SchedAlg = &scoreboard::heuristic0;        // heuristic 0
    heuristic = 0;
    interleaving = false;        // use interleaving concurrent simulation
    ltl = false;                 // consider any LTL constraints
    ANNOTATION_ENABLED = true;   //annotate quantities in simulation
    timestat = false;            // no report on time statistics of the simulation
    debug_flag = false;          // no debugging info
    debug_level = 0;  // level-0 debugging info
    gxi = 0;
    evgxi = 0;
    dumpev = false;  // do not dump event vector
    //maxCycle = 10;    //maximum number of cycles before resolving quantities
    showmedia = false; // no debugging info
    synchonly = false; // no debugging info
    ltlonly = false;   // no debugging info

    if (getArgc()>1) {
        for (int i=1; i<getArgc(); i++)
            if (!strcmp(getArgv(i), "-d")) {
                debug_flag = true;
                char n;
                ++i;
                if (i==getArgc()) n='E';
                else n=getArgv(i)[0];
                switch(n) {
                case '0':
                    debug_level = 0;
                    break;
                case '1':
                    debug_level = 1;
                    break;
                case '2':
                    debug_level = 2;
                    break;
                case '3':
                    debug_level = 3;
                    break;
                case '4':
                    debug_level = 4;
                    break;
                case '-':
                    --i;
                default:
                    cerr<<"Incorrect argument for -d. Should be '-d <0-4> [gxi]'."<<endl;
                    cerr<<"Use the default value -d 0."<<endl;
                    debug_level = 0;
                } // end switch
                /*
                  } else if (!strcmp(getArgv(i), "-mc")) {
                  ++i;
                  char n = getArgv(i)[0];
                  if (n=='-' || n<'1' || n>'9') {
                  --i;
                  cerr<<"Incorrect argument for -mc. Should be '-mc <number>'."<<endl;
                  cerr<<"Use the default value -mc 10."<<endl;
                  maxCycle = 10;
                  } else {
                  maxCycle = atoi(getArgv(i));
                  }
                */
                if (i+1 >= getArgc()) continue;
                if (getArgv(i+1)[0] != '-') {
                    gxi = atol(getArgv(++i));
                    if (gxi>0)
                        debug_flag = false;
                }
            } else if (!strcmp(getArgv(i), "-dumpev")) {
                dumpev = true;
                if (i+1 >= getArgc()) continue;
                if (getArgv(i+1)[0] != '-') {
                    evgxi = atol(getArgv(++i));
                    if (evgxi>0)
                        dumpev = false;
                }
            } else if (!strcmp(getArgv(i), "-synchp")) {
                synchonly = true;
            } else if (!strcmp(getArgv(i), "-ltlp")) {
                ltlonly = true;
            } else if (!strcmp(getArgv(i), "-m")) {
                showmedia = true;
            } else if (!strcmp(getArgv(i), "-ic")) {
                interleaving = true;
            } else if (!strcmp(getArgv(i), "-noqa")) {
                ANNOTATION_ENABLED = false;
            } else if (!strcmp(getArgv(i), "-r")) {
                srandom(getpid());
            } else if (!strcmp(getArgv(i), "-ltl")) {
                ltl = true;
            } else if (!strcmp(getArgv(i), "-t")) {
                timestat = true;
                /*
                  } else if (!strcmp(getArgv(i), "-h")) {
                  char n;
                  ++i;
                  if (i==getArgc()) n='E';
                  else n=getArgv(i)[0];
                  switch(n) {
                  case '0':
                  SchedAlg = &scoreboard::heuristic0;
                  heuristic = 0;
                  break;
                  case '1':
                  SchedAlg = &scoreboard::heuristic1;
                  heuristic = 1;
                  break;
                  case '2':
                  SchedAlg = &scoreboard::heuristic2;
                  heuristic = 2;
                  break;
                  case '4':
                  SchedAlg = &scoreboard::heuristic2;
                  heuristic = 4;
                  break;
                  case '3':
                  SchedAlg = &scoreboard::heuristic3;
                  heuristic = 3;
                  break;
                  case '-':
                  --i;
                  default:
                  cerr<<"Incorrect argument for -h. Should be '-h <0-3>'."<<endl;
                  cerr<<"Use the default heuristic 0."<<endl;
                  SchedAlg = &scoreboard::heuristic0;
                  } // end switch
                */
            } else if (!strcmp(getArgv(i), "-help")) {
                cout<<endl;
                //cout<<getArgv(0)<<"   [-help]  [-r]  [-t]  [-h <0-3>]  [-ltl]  [-ic] [-noqa]"<<endl;
                //cout<<getArgv(0)<<"   [-help]  [-r]  [-t]  [-ltl]  [-ic] [-noqa] [-mc <number>]"<<endl;
                cout<<getArgv(0)<<"   [-help]  [-r]  [-t]  [-ltl]  [-ic] [-noqa]"<<endl;
                cout<<"\t[-dumpev [gxi]]  [-d <0-4> [gxi] [-synchp]  [-ltlp]  [-m] ]"<<endl;
                cout<<"\t-help\t\tShow this message"<<endl;
                cout<<"\t-r\t\tExhibit true (not pseudo) randomness during simulation"<<endl;
                cout<<"\t-t\t\tReport time statistics on simulation"<<endl;
                /*
                  cout<<"\t-h <0-4>\tChoose different scheduling heuristics"<<endl;
                  cout<<"\t    0\t\tThe default scheduling heuristic (the original one)"<<endl;
                  cout<<"\t    1\t\tRandomly choose one runnable process"<<endl;
                  cout<<"\t    2\t\tChoose a (sub)set of compatible and runnable processes"<<endl;
                  cout<<"\t    3\t\tChoose a maxium set of compatible and runnable processes"<<endl;
                  cout<<"\t    4\t\tChoose heuristic 2 with hook function to guide SAT solver"<<endl;
                */
                cout<<"\t-ltl\t\tEnforce LTL constraints during simulation"<<endl;
                cout<<"\t-ic\t\tUse interleaving concurrent feature in simulation"<<endl;
                cout<<"\t-noqa\t\tDo not annotate quantities during simulation"<<endl;
                /*
                  cout<<"\t-mc <number>\tMaximum number of cycles between quantity resolutions"<<endl;
                  cout<<"\t\t\tThe higher the number, the less frequently quantities get resolved"<<endl;
                */
                cout<<"\t-dumpev [gxi]\tDump event vectors during simulation"<<endl;
                cout<<"\t\t\tIf gxi exists, start dumping after gxi event vectors."<<endl;
                cout<<"\t-d <0-4> [gxi]\tShow scoreboard for each step during simulation."<<endl;
                cout<<"\t\t\tA larger number gives more detailed information."<<endl;
                cout<<"\t\t\tIf gxi exists, start showing after gxi event vectors."<<endl;
                cout<<"\t  -synchp\tShow only the processes present in synch."<<endl;
                cout<<"\t  -ltlp  \tShow only the processes present in ltl constraints."<<endl;
                cout<<"\t  -m\t\tShow all media."<<endl;
                return true;
            } //end if
    } // end for
    return false;
} // end processArg

//Find out if there are any await statements waiting to evaulate conditions.
bool scoreboard::eval() {
    bool flag = false;
    for (int i=0; i<size; i++) {
        if (pcVector[i]->SchedState == SchedStateVal::EVALUATE) {
            notify(DeltaCycle, pcVector[i]->invoke);
            flag = true;
            wait(_mng.pc->invoke);
        }
    }
    return flag;
}

//Before restart of manager cycle, change DONTRUN to UNKNOW for
//interface function, block or label
//For await statements, create csOrder[].
void scoreboard::preSet() {
    testSchedFlag = false;

    for (int i=0; i<size; i++) {
        pcVector[i]->savePendingEvents();
        pcVector[i]->_pcBlocked.clear();

        if (pcVector[i]->SchedState == SchedStateVal::TESTSCHEDULING)
            testSchedFlag = true;

        if (pcVector[i]->SchedState == SchedStateVal::DONTRUN)
            pcVector[i]->SchedState = SchedStateVal::UNKNOW;
    } // end for

/*
    if (reqPCs.size() < maxQAProc) {
        if (!allProcStuck) {
            for (PCSet_t::iterator it=reqPCs.begin(); it!=reqPCs.end(); it++)
                set2dontrun(*it);
        }
    } else {
        allProcStuck = true;
        cyclesB4QA = 0;
    }
*/

    if (numSynchEventGroup>0) {
        collect_ref_count();
        process_equalvars_in_synch();
    }

    for (int i=0; i<numSynchEventGroup; i++) {
        if (enabledSynchEventEachGroup[i] < numSynchEventEachGroup[i]) {
            for (int j=0; j<enabledSynchEventEachGroup[i]; j++) {
                enabledSynchEventPC[i][j]->freeAllPendingEvents();
                enabledSynchEventPC[i][j]->SchedState = SchedStateVal::DONTRUN;
                enabledSynchEventPC[i][j] = NULL;
            } // end for
            enabledSynchEventEachGroup[i] = 0;
        } // end if
    } // end for
} // end preSet

//Adjust pc->selected to actual choice stored in csOrder[]
//Clean up pc->csOrder[] created in preSet()
void scoreboard::postSet() {
    for (int i = 0; i < size; i++) {
        ProgramCounter *pc = pcVector[i];

        if (pc->SchedState == SchedStateVal::FINISHED) continue;

        if (((!testSchedFlag) &&
                    ((pc->funcType & FunctionType::ANNOTATION) == 0) &&
                    (!pc->_inSynch) &&
                    (pc->SchedState == SchedStateVal::DONTRUN)) //||
                //(testSchedFlag && pc->mode==RunType::EXEC)
            ) {
            pc->loadPendingEvents();
        }
    } // end for

    for (int i=0; i<numSynchEventGroup; i++) {
        enabledSynchEventEachGroup[i] = 0;
    } // end for
} // end postSet

/*
 * pass1
 * Check first deterministic processes (the one with 1 pending event).
 * Update the reference count value in the eventSynchMap_t to order the
 * critical section selection next.
 * Then checks each process with all the others in order to detect any
 * conflict in test list.
 */
bool scoreboard::pass1() {
    bool rtn = false;

    for (eventSynchMap_t::iterator it = _eventSynchMap.begin();
         it != _eventSynchMap.end();
         it++) {
        it->first->_refCount = 0;
        it->second->_refCount = 0;
    } // end for

    // nPendingEvents == 1 --> check deterministic critical sections selection
    // nPendingEvents == 2 --> check non deterministic critical sections selection
    for (int nPendingEvents = 1; nPendingEvents <= 2; nPendingEvents++) {
        for (int i = 0; i < size; i++) {
            ProgramCounter * pc = pcVector[i];

            if (testSchedFlag) {
                if (pc->SchedState != SchedStateVal::TESTSCHEDULING) continue;
            } else {
                if (pc->SchedState != SchedStateVal::UNKNOW) continue;
            } // end if

            if (pc->_pendingEvents.size() == 0) continue;

            if (nPendingEvents == 1) {
                if (pc->_pendingEvents.size() == 1) resolveSynch0(pc);                // deterministic => _pendingEvents.size() == 1
            } else {
                if (pc->_pendingEvents.size() > 1) resolveSynch1(pc);                // non deterministic => _pendingEvents.size() > 1

                // suppose all label or block could run
                if (pc->funcType & FunctionType::LABEL) {
                    rtn = true;
                    continue;
                } // end if

                // medium-centric approach
                rtn = couldRun(pc);

                /* process-centric approach
                   int j;

                   do {
                   //                                        bool first = true;
                   for (j = 1; j < size; j++) {
                   if (i == j) continue;
                   ProgramCounter * cmp = pcVector[j];

                   if (cmp->SchedState != SchedStateVal::UNKNOW) continue;
                   if (cmp->_pendingEvents.empty()) continue;

                   if (pc->funcType & FunctionType::AWAIT) {
                   if (!couldRunAwait(pc, cmp)) {
                   break;
                   } // end if
                   } else if (pc->funcType & FunctionType::INTFCFUNC) {
                   if (!couldRunIntfc(pc, cmp)) {
                   break;
                   } // end if
                   } // end if
                   } // end for
                   } while ((j < size) && (pc->_pendingEvents.size() > 0));
                   if (j == size) rtn = true;
                */
            } // end if
        } // end for
    } // end for

    return (rtn || testSchedFlag);
}



// Second scan. Find out wether there is any inconsistence if all processes from pass1 begin to run.
bool scoreboard::pass2() {
    bool bFound = false;

    // mode == 0 => collect blocking process relations
    // mode == 1 => perform necessary updates (setMustDo / setMustNotDo)
    for (int mode = 0; mode < 2; mode++) {
        for (int k=0; k<msize; k++) { // iterate over all media
            medium * m = mediaVector[k];
            for (PCSet_t::iterator i = m->_potentialUserPCs.begin(); i != m->_potentialUserPCs.end(); i++) {
                ProgramCounter *pc = (*i);

                if (testSchedFlag) {
                    if (pc->SchedState != SchedStateVal::TESTSCHEDULING) continue;
                } else {
                    if (pc->SchedState != SchedStateVal::UNKNOW) continue;
                } // end if

                if (pc->_pendingEvents.empty()) continue;

                bool runBothFlag = true;
                PCSet_t::iterator j = i;
                for (j++; j != m->_potentialUserPCs.end(); j++) {
                    ProgramCounter *cmp = (*j);

                    if (testSchedFlag) {
                        if (cmp->SchedState != SchedStateVal::TESTSCHEDULING) continue;
                    } else {
                        if (cmp->SchedState != SchedStateVal::UNKNOW) continue;
                    } // end if

                    if (cmp->_pendingEvents.empty()) continue;

                    if (!couldRunBoth(mode, pc, cmp)) {
                        //runBothFlag = false;
                        if (mode == 0) {        // collect information on blocking process relations
                            set2blocked(pc, cmp);
                            set2blocked(cmp, pc);
                        } else                                // perform event/process selection according to user specific simulator plugin
                            _pBehavior->resolve(pc, cmp);
                    } // end if
                } // end for j
                //if (runBothFlag) bFound = true;
            } // end for i
        } // end for k
    } // end for mode

    for (int i=0; i<size; i++) {
        ProgramCounter * pc = pcVector[i];
        if (testSchedFlag) {
            if (pc->SchedState != SchedStateVal::TESTSCHEDULING) continue;
        } else {
            if (pc->SchedState != SchedStateVal::UNKNOW) continue;
        } // end if
        if (! pc->_pendingEvents.empty()) {
            bFound = true;
            break;
        }
    }//end for i

    return (bFound || testSchedFlag);
} // end pass2

bool scoreboard::resolveSynch0(ProgramCounter *pPC) {
    // prepare reference count number
    maxRefCount = 0;
    // prepare critical section selection order
    pPC->_csOrder.clear();

    // pPC->_pendingEvents.size() == 1
    for (eventMap_t::iterator it = pPC->_pendingEvents.begin(); it != pPC->_pendingEvents.end(); it++) {
        event *pEvent = (*it).first;        // event
        short nCs = (*it).second;                // critical section number

        pPC->_csOrder.push_back(nCs);

        // get the corresponding synch event in the map
        EventMap *pFoundEM = getEventSynchMap(pEvent, true);

        if (pFoundEM) {
            // count how many times this event is pending in all non deterministic processes (pending events > 1)
            for (int i = 0; i < size; i++) {
                ProgramCounter *pc = pcVector[i];


                if (pc->SchedState != SchedStateVal::UNKNOW) continue;
                if (pc->_pendingEvents.empty()) continue;

                for (eventMap_t::iterator it = pc->_pendingEvents.begin(); it != pc->_pendingEvents.end(); it++) {
                    if (it->first == pFoundEM->_e && pc->_pendingEvents.size() > 1) {

                        pFoundEM->_refCount++;
                        if (pFoundEM->_refCount > maxRefCount) maxRefCount = pFoundEM->_refCount;
                        break;
                    } // end if
                } // end for
            } // end for
        } // end if
    } // end for

    return true;

} // end resolveSynch0

bool scoreboard::resolveSynch1(ProgramCounter *pPC) {
    // prepare critical section selection order
    pPC->_csOrder.clear();

    // start considering pending events corresponding to the highest
    // value of the reference count
    for (int i = maxRefCount; i >= 0; --i) {
        // store first position of the group of critical sections
        // having the same reference count in order to randomize the
        // order among the critical sections with the same reference count
        // value
        int posInsCsOrder = pPC->_csOrder.size();
        for (eventMap_t::iterator it = pPC->_pendingEvents.begin(); it != pPC->_pendingEvents.end(); it++) {
            event *pEvent = it->first;        // event
            short nCs = it->second;                // critical section number

            // get pEvent position in the event synch map
            EventMap *pFoundEM = getEventSynchMap(pEvent, false);

            bool bInsertCS = false;

            if (pFoundEM) {
                if (pFoundEM->_refCount == i) bInsertCS = true;
            } else
                bInsertCS = true;

            if (bInsertCS) {
                // check if the critical section has already been inserted into _csOrder
                intVect_t::iterator it = std::find(pPC->_csOrder.begin(), pPC->_csOrder.end(), nCs);
                if (it == pPC->_csOrder.end()) {
                    // randomize critical section order
                    int dim = pPC->_csOrder.size();

                    int pos = (dim - posInsCsOrder == 0) ? 0 : (random() % (dim - posInsCsOrder));
                    if (posInsCsOrder + pos >= dim)
                        pPC->_csOrder.push_back(nCs);
                    else {
                        short oldCs = pPC->_csOrder[posInsCsOrder + pos];
                        pPC->_csOrder[posInsCsOrder + pos] = nCs;
                        pPC->_csOrder.push_back(oldCs);
                    } // end if
                } // end if

            } // end if
        } // end if
    } // end for

    return true;
} // end resolveSynch1

//Third scan. Constraints check. Not yet implemented.
bool scoreboard::pass3() {

    return true;
}

//Forth scan. Do user specified _doScheduling.
//bool pass4(){} = _doScheduling in Scheduler

//A little like pass2(). Collect all conflicts among all pending events
void scoreboard::collectEventConflicts() {
    conflictRSet.clear();
    eventVarIndex.clear();
    eventMap_t  pcBackup, cmpBackup;

    for (int i = 0; i < size; i++) {
        ProgramCounter *pc = pcVector[i];

        if (testSchedFlag) {
            if (pc->SchedState != SchedStateVal::TESTSCHEDULING) continue;
        } else {
            if (pc->SchedState != SchedStateVal::UNKNOW) continue;
        } // end if

        if (pc->_pendingEvents.empty()) continue;

        for (eventMap_t::iterator it=pc->_pendingEvents.begin(); it!=pc->_pendingEvents.end(); it++) {
            eventVarIndex[it->first] = 0;
            if (pc->funcType & FunctionType::AWAIT) {
                ++it;
                eventMap_t::iterator it2=it;
                --it;
                for (; it2!=pc->_pendingEvents.end(); it2++)
                    conflictRSet.insert(std::make_pair(it->first, it2->first));
            } // end if
        } // end for

        if (pc->funcType & FunctionType::LABEL) continue;

        pcBackup   = pc->_pendingEvents;
        for (int j = i + 1; j < size; j++) {
            ProgramCounter *cmp = pcVector[j];

            if (testSchedFlag) {
                if (cmp->SchedState != SchedStateVal::TESTSCHEDULING) continue;
            } else {
                if (cmp->SchedState != SchedStateVal::UNKNOW) continue;
            } // end if

            if (cmp->_pendingEvents.empty()) continue;

            if (cmp->funcType & FunctionType::LABEL) continue;

            if ((pc->funcType & FunctionType::INTFCFUNC) &&
                    (cmp->funcType & FunctionType::INTFCFUNC)) continue;

            cmpBackup= cmp->_pendingEvents;

            for (eventMap_t::iterator itpc=pcBackup.begin(); itpc!=pcBackup.end(); itpc++)
                for (eventMap_t::iterator itcmp=cmpBackup.begin(); itcmp!=cmpBackup.end(); itcmp++) {
                    pc->_pendingEvents.clear();
                    cmp->_pendingEvents.clear();
                    pc->_pendingEvents.insert(*itpc);
                    cmp->_pendingEvents.insert(*itcmp);
                    if (!couldRunBoth(0, pc, cmp))
                        conflictRSet.insert(std::make_pair(itpc->first, itcmp->first));
                } // end for

            cmp->_pendingEvents = cmpBackup;
        } // end for j
        pc->_pendingEvents = pcBackup;
    } // end for i
} // end collectEventConflicts


//self-check done by individual process
// if ANNOTATION exists, it won't get here.
void scoreboard::check(ProgramCounter *pc) {
    /*
      if (!interleaving) {
      // original non-interleaving concurrent version
      if (pc->SchedState == SchedStateVal::UNKNOW)
      pc->SchedState = SchedStateVal::DONTRUN;
      else // pc->SchedState == SchedStateVal::TESTSCHEDULING
      pc->SchedState = SchedStateVal::RUN;
      } else
    */
    {
        // interleaving concurrent version
        switch (pc->funcType) {
        case FunctionType::LABEL:
            if (getEventSynchMap( pc->_pendingEvents.begin()->first, false)) {
                pc->SchedState =SchedStateVal::DONTRUN;
            } else {
                pc->SchedState =SchedStateVal::RUN;
            } // end if
            break;
        case FunctionType::INTFCFUNC:
            if (getEventSynchMap( pc->_pendingEvents.begin()->first, false)) {
                pc->SchedState =SchedStateVal::DONTRUN;
            } else {
                for (int j = 1; j < size; j++) {
                    if (pc == pcVector[j]) continue;
                    if (!couldRunIntfc(pc, pcVector[j])) {
                        pc->SchedState =SchedStateVal::DONTRUN;
                        return;
                    } // end if
                } // end for

                pc->SchedState =SchedStateVal::RUN;
            } // end if
            break;
        case FunctionType::AWAIT:
            //favor synched CS over non-synched CS by defering
            // the scheduling to (*SchedAlg)()
            for (eventMap_t::iterator it = pc->_pendingEvents.begin();
                 it != pc->_pendingEvents.end();
                 it++) {
                if (getEventSynchMap( it->first, false)) {
                    pc->SchedState =SchedStateVal::DONTRUN;
                    return;
                } // end if
            } //end for

            for (int j = 1; j < size; j++) {
                if (pc == pcVector[j]) continue;
                if (!couldRunAwait(pc, pcVector[j])) {
                    pc->SchedState =SchedStateVal::DONTRUN;
                    return;
                } // end if
            } // end for

            if (pc->_pendingEvents.size() == 1) {
                // only 1 pending event => RUN the corresponding process
                pc->selected = pc->_pendingEvents.begin()->second;
            } else if (pc->_pendingEvents.size() > 1) {
                // more than 1 pending event => choose a random critical
                // section and RUN it
                int choice = (int)(random() % (pc->_pendingEvents.size()));
                eventMap_t::iterator it = pc->_pendingEvents.begin();
                for (int i = 0 ;
                     it != pc->_pendingEvents.end() && i < choice;
                     it++, i++);
                pc->selected = it->second;
            } // end if
            pc->SchedState =SchedStateVal::RUN;

            break;
        case FunctionType::ANNOTATION: // should never happen
        default:
            cerr<<"Internal Error! Exit."<<endl;
            sc_stop();
        } // end switch
    } // end if
} // end check

//Update corresponding synch event info
void scoreboard::updateSynchEventInfo(int eventGroupID, ProgramCounter* eventPC) {
    enabledSynchEventPC[eventGroupID][enabledSynchEventEachGroup[eventGroupID]] = eventPC;
    enabledSynchEventEachGroup[eventGroupID]++;
    eventPC->_inSynch = true;
} // end updateSynchEventInfo

//Return whether all events in the ith synch event group are enabled
bool scoreboard::isSynchEventGroupEnabled(int ith) {
    return (enabledSynchEventEachGroup[ith]==numSynchEventEachGroup[ith]);
} // end isSynchEventGroupEnabled

//Due to the equal variables comparison, enable/disable this group
//of synched events
void scoreboard::setSynchEventGroupStatus(int i, bool satisfied) {
    if (!satisfied) {
        for (int j=0; j<enabledSynchEventEachGroup[i]; j++) {
            enabledSynchEventPC[i][j]->freeAllPendingEvents();
            enabledSynchEventPC[i][j]->SchedState = SchedStateVal::DONTRUN;
            enabledSynchEventPC[i][j] = NULL;
        } // end for
        enabledSynchEventEachGroup[i] = 0;
    } // end if
} // end setSynchEventGroupStatus

//add (synch event, <beg group, end group>) info to scoreboard
void scoreboard::addSynchEventGroupInfo(process *p, sc_object *m, const char *c, enum ActionState as, int groupNum) {
    event * key = getEvent(p, m, c, ACTION_STATE_BEGIN);
    int beg, end;
    beg = end = -1;
    if (as == ACTION_STATE_BEGIN)
        beg = groupNum;
    else
        end = groupNum;
    eventGroups::iterator it = synchEventGroup.find(key);
    if (it == synchEventGroup.end()) {
        bool inserted;
        pair<eventGroups::iterator, bool>(it, inserted) = synchEventGroup.insert(eventGroups::value_type(key, intpair(beg, end)));
    } else {
        if (as == ACTION_STATE_BEGIN)
            it->second.first = groupNum;
        else
            it->second.second = groupNum;
    }

    synchPC.insert(p->pc);
    if (synchonly) caredPC.insert(p->pc);
} // end addSynchEventGroupInfo

//Collect the reference count of a CS in await,
//which is involved in synch statements
void scoreboard::collect_ref_count() {
    int beg, end; //total # synch events - # enabled synch events
    bool mapAwait;

    //Check for 2-way synch
    for ( PCSet_t::iterator pcit = synchPC.begin();
          pcit!=synchPC.end(); pcit++) {
        ProgramCounter * pc = (*pcit);
        mapAwait = true;
        if ((pc->funcType & FunctionType::AWAIT) != 0) {
            if (pc->_pendingEvents.size()==0) continue;
            for (int k=0; k<2; k++) {
                mapAwait = false;
                eventMap_t::iterator it = pc->_pendingEvents.begin();
                for (; it!=pc->_pendingEvents.end(); it++) {
                    eventGroups::iterator eit = synchEventGroup.find(it->first);
                    if (k==0) {
                        if (eit == synchEventGroup.end()) {
                            it->first->setRefCount(INT_MAX);
                            continue;
                        }
                        mapAwait = true;
                        break;
                    } else {
                        if (eit == synchEventGroup.end()) {
                            it->first->inPendingList = false;
                            pc->_pendingEvents.erase(it);
                            continue;
                        }
                        intpair p = eit->second;
                        if (p.first >= 0) {
                            beg = numSynchEventEachGroup[p.first]-enabledSynchEventEachGroup[p.first];
                            if (beg>1) {
                                it->first->inPendingList = false;
                                pc->_pendingEvents.erase(it);
                            }
                        } else {
                            beg = INT_MAX;
                            it->first->setRefCount(beg);
                        }

                        /*
                          if (p.second >= 0)
                          end = numSynchEventEachGroup[p.second]-enabledSynchEventEachGroup[p.second];
                          else
                          end = INT_MAX;
                        */
                    }
                    //it->first->setRefCount((beg<end) ? beg : end);
                } // end for it
                if (!mapAwait) break;
            } // end for k
        } // end if pc->funcType
    } // end for pcit
} // end collect_ref_count

bool scoreboard::resolveSynch() {
    bool change = false;
    for (int i=0; i<numSynchEventGroup; i++) {
        if (numSynchEventEachGroup[i] == enabledSynchEventEachGroup[i]) {
            bool enabled = true;
            for (int j=0; (j<enabledSynchEventEachGroup[i]) & enabled; j++) {
                if (enabledSynchEventPC[i][j]->_pendingEvents.size()==0) enabled = false;
            } // end for
            if (!enabled) {
                change = true;
                for (int j=0; j<enabledSynchEventEachGroup[i]; j++) {
                    enabledSynchEventPC[i][j]->freeAllPendingEvents();
                    enabledSynchEventPC[i][j]->SchedState = SchedStateVal::DONTRUN;
                    enabledSynchEventPC[i][j] = NULL;
                } // end for
                enabledSynchEventEachGroup[i] = 0;
            } //end if
        } // end if
    } // end for

    return change;
} // end resolveSynch

void scoreboard::setupSynchImplicationInfo() {
    EqualVar * eqv;
    Variable * v0, * v1;
    for (int i=0; i<numSynchImplyGroup; i++) {
        //Collect Nondet variable info
        for (int eqvi=0; eqvi<ltlsi[i]->eqvarsNum; eqvi++) {
            eqv = ltlsi[i]->eqvars[eqvi];
            v0 = eqv->v[0];
            v1 = eqv->v[1];
            if (v0->nondet) {
                ndVarMap_t::iterator it = ndVars.find(v0);
                equalVarSet_t * eqvs;
                if (it == ndVars.end())
                    eqvs = NULL;
                else
                    eqvs = it->second;
                if (eqvs == NULL) {
                    eqvs = new equalVarSet_t();
                    ndVars.insert(ndVarMap_t::value_type(v0, eqvs));
                }
                eqvs->insert(v1);
            }//end if v0
            if (v1->nondet) {
                ndVarMap_t::iterator it = ndVars.find(v1);
                equalVarSet_t * eqvs;
                if (it == ndVars.end())
                    eqvs = NULL;
                else
                    eqvs = it->second;
                if (eqvs == NULL) {
                    eqvs = new equalVarSet_t();
                    ndVars.insert(ndVarMap_t::value_type(v1, eqvs));
                }
                eqvs->insert(v0);
            }//end if v1
        }//end for eqvi

        //Collect program counters that have events in the
        //right hand side of 1-way synch statements
        for (int j=0; j<ltlsi[i]->rhsNum; j++)
            synchImplPC.insert(ltlsi[i]->rhs[j]->getProcess()->pc);

        //Setup debugging info
        if (synchonly) {
            for (int j=0; j<ltlsi[i]->lhsNum; j++)
                caredPC.insert(ltlsi[i]->lhs[j]->getProcess()->pc);
            for (int j=0; j<ltlsi[i]->rhsNum; j++)
                caredPC.insert(ltlsi[i]->rhs[j]->getProcess()->pc);
        }
    }//end for i
}//end scoreboard::setupSynchImplicationInfo

bool scoreboard::resolveSynchImply() {
    bool change = false;
    int lhs, rhs;
    EqualVar * eqv;
    Variable * v0, * v1;
    Nondet * n;

    for (int i=0; i<numSynchImplyGroup; i++) {
        for (int j=0; j<ltlsi[i]->rhsNum; j++) {
            ltlsi[i]->rhs[j]->setImplCount(-1);
        }
    }

    for (int i=0; i<numSynchImplyGroup; i++) {
        //if (rhs==false) disable all lhs events
        lhs = rhs = 0;
        for (int j=0; j<ltlsi[i]->lhsNum; j++) {
            if (ltlsi[i]->lhs[j]->isEnabled()) {
                if (! ltlsi[i]->lhs[j]->isInNamedEvent(FunctionType::AWAIT))
                    lhs ++;
            }
        }//end for j, lhs
        for (int j=0; j<ltlsi[i]->rhsNum; j++) {
            if (ltlsi[i]->rhs[j]->isEnabled()) {
                if (ltlsi[i]->rhs[j]->isInNamedEvent(FunctionType::AWAIT))
                    ltlsi[i]->rhs[j]->promoteImplCount(lhs);
                else
                    rhs ++;
            }
        }//end for j, rhs

        if (lhs>0 && rhs==0) {
            //Must disable all lhs events
            change = true;
            for (int j=0; j<ltlsi[i]->lhsNum; j++) {
                if (! ltlsi[i]->lhs[j]->isInNamedEvent(FunctionType::AWAIT))
                    ltlsi[i]->lhs[j]->setMustNotDo();
            }
            continue;
        }

        //Check equality comparisons and disable those not satisfied
        for (int eqvi=0; eqvi<ltlsi[i]->eqvarsNum; eqvi++) {
            EqualVar * eqv = ltlsi[i]->eqvars[eqvi];
            if (!(eqv->isSatisfied())) {
                if (eqv->v[0]->e != NULL) {
                    if (eqv->v[0]->e->isInNamedEvent(FunctionType::AWAIT)) {
                        if (eqv->v[1]->e != NULL) {
                            if (! eqv->v[1]->e->isInNamedEvent(FunctionType::AWAIT)) {
                                change |= eqv->v[1]->e->setMustNotDo();
                                continue;
                            }
                        }
                    }
                }
                if (eqv->v[1]->e != NULL) {
                    if (eqv->v[1]->e->isInNamedEvent(FunctionType::AWAIT)) {
                        if (eqv->v[0]->e != NULL) {
                            if (! eqv->v[0]->e->isInNamedEvent(FunctionType::AWAIT)) {
                                change |= eqv->v[0]->e->setMustNotDo();
                                continue;
                            }
                        }
                    }
                }

                //Disable the event on which side there are more
                //enabled events. In the case there are equal number
                //of the enabled events on both sides, disable the
                //event on the left hand side.
                event * e;
                event ** events;
                int numEvents;
                if (lhs >= rhs) {
                    events = ltlsi[i]->lhs;
                    numEvents = ltlsi[i]->lhsNum;
                } else {
                    events = ltlsi[i]->rhs;
                    numEvents = ltlsi[i]->rhsNum;
                }
                for (int j=0; j<numEvents; j++) {
                    e = events[j];
                    if (e->equals(eqv->v[0]->e)) {
                        change |= e->setMustNotDo();
                        break;
                    } else if (e->equals(eqv->v[1]->e)) {
                        change |= e->setMustNotDo();
                        break;
                    }
                }//end for j
            }//end if eqv->isSatisfied()
        }//end for eqvi
    } // end for i

    //Resolve Nondet info
    void *val0, *val1;
    int type0, type1;
    Variable * ndVar;
    equalVarSet_t * eqvars;
    for (ndVarMap_t::iterator it = ndVars.begin();
         it != ndVars.end();
         it ++) {
        ndVar = it->first;
        eqvars = it->second;

        if (! ndVar->e->isEnabled())
            continue;
        if (ndVar->e->isInNamedEvent(FunctionType::AWAIT))
            continue;

        //Choose a base value in the group of variables/constants
        //in the same group that are compared to the same Nondet variable

        //If there is a constant, choose it.
        v0 = NULL;
        equalVarSet_t::iterator evit = eqvars->begin();
        for (evit = eqvars->begin(); evit != eqvars->end(); evit++) {
            v1 = (*evit);
            if (v1->e == NULL) {//constant
                v0 = v1;
                break;
            }
        }

        //No constants exist. Choose one variable.
        if (v0 == NULL) {
            evit = eqvars->begin();
            for (int j=0; j<eqvars->size(); j++) {
                v0 = (*evit);
                if (v0->e->isEnabled())
                    break;
                evit++;
            }
        }
        if (v0 == NULL)
            continue;
        val0 = v0->v;
        type0 = v0->type;

        //Make sure that the one chosen is a deterministic one,
        //otherwise choose the next deterministic one.
        if (v0->nondet) {
            n = *((Nondet**) v0->v);
            val0 = &(n->data);
            if (n->isNondet()) {
                int j;
                for (j=0; j<eqvars->size(); j++) {
                    evit++;
                    if (evit == eqvars->end())
                        evit = eqvars->begin();
                    v0 = (*evit);
                    val0 = v0->v;
                    type0 = v0->type;
                    if (! v0->nondet)
                        break;
                    else {
                        n = *((Nondet**) v0->v);
                        if (! n->isNondet()) {
                            val0 = &(n->data);
                            break;
                        }
                    }
                }//end for j
                if (j == eqvars->size())
                    continue;
            }//end if n->isNondet()
        }//end if v0->nondet

        //Disable those that are not equal to the base variable
        //equalVarSet_t::iterator evit2;
        for (evit = eqvars->begin();
             evit != eqvars->end();
             ) {
            v1 = (*evit);
            if (v0 == v1) {
                evit ++;
                continue;
            }
            if (v1->nondet) {
                n = *((Nondet**) v1->v);
                val1 = &(n->data);
            } else {
                val1 = v1->v;
            }
            type1 = v1->type;
            if (!EqualVar::equal(type0, val0, type1, val1)) {
                if (v1->e == NULL) {//constant
                    change |= ndVar->e->setMustNotDo();
                    break;
                } else {
                    change |= v1->e->setMustNotDo();
                    evit++;
                }
            } else {
                evit ++;
            }
        }//end for ndVars iterator
    }//end for i

    //Disable those events that are
    //1. in an await and at least one event is implied in 1-way synch
    //2. not implied by other events in 1-way synch
    ProgramCounter * pc;
    bool implAwait;
    for ( PCSet_t::iterator pcit = synchImplPC.begin();
          pcit!=synchImplPC.end(); pcit++) {
        pc = (*pcit);
        if ((pc->funcType & FunctionType::AWAIT) != 0) {
            if (pc->_pendingEvents.size()<2) continue;
            implAwait = false;
            for (int k=0; k<2; k++) {
                eventMap_t::iterator it = pc->_pendingEvents.begin();
                for (; it!=pc->_pendingEvents.end(); it++) {
                    if (k==0) {
                        if (it->first->getImplCount() >= 0) {
                            implAwait = true;
                            break;
                        }
                    } else {
                        if (it->first->getImplCount() <= 0) {
                            it->first->inPendingList = false;
                            pc->_pendingEvents.erase(it);
                            change = true;
                        }
                    }
                } // end for it
                if (!implAwait) break;
            } // end for k
        } // end if pc->funcType
    }

    return change;
} // end resolveSynchImply

/*
 * Set values to Nondet variables in synch implication constraints
 */
void scoreboard::finalizeNondetVars() {
    equalVarSet_t *eqvars;
    Nondet *n, *t;
    Variable *v0, *v1, *vtmp, *ndVar;
    void *val0;
    int type0;
    for (ndVarMap_t::iterator it = ndVars.begin();
         it != ndVars.end();
         it ++) {
        ndVar = it->first;
        if (! ndVar->e->isEnabled())
            continue;
        if (ndVar->e->isInNamedEvent(FunctionType::AWAIT))
            continue;
        n = * ((Nondet**) ndVar->v);
        if (! n->isNondet())
            continue;
        eqvars = it->second;

        //Choose a base value in the group of variables/constants
        //in the same group that are compared to the same Nondet variable

        //If there is a constant, choose it.
        v0 = v1 = NULL;
        equalVarSet_t::iterator evit;
        for (evit = eqvars->begin(); evit != eqvars->end(); evit++) {
            vtmp = (*evit);
            if (vtmp->e == NULL) {//constant
                v0 = vtmp;
                break;
            } else if (vtmp->e->isEnabled()) {
                if (v1 == NULL) {
                    if (vtmp->nondet) {
                        t = *((Nondet**) vtmp->v);
                        if (! t->isNondet())
                            v1 = vtmp;
                    } else {
                        v1 = vtmp;
                    }
                }
            }
        }

        //No constants exist. Choose one variable.
        if (v0 == NULL)
            v0 = v1;
        if (v0 == NULL)
            continue;
        val0 = v0->v;
        type0 = v0->type;

        //Make sure that the one chosen is a deterministic one,
        //otherwise choose the next deterministic one.
        if (v0->nondet) {
            t = *((Nondet**) v0->v);
            val0 = &(t->data);
            type0 = v0->type;

        }//end if v0->nondet

        switch(type0) {
        case VarType::BYTETYPE:
            n->set( (*((unsigned char*)val0)));
            break;
        case VarType::CHARTYPE:
            n->set( (*((char*)val0)));
            break;
        case VarType::SHORTTYPE:
            n->set( (*((short*)val0)));
            break;
        case VarType::DOUBLETYPE:
            n->set((int) (*((double*)val0)));
            break;
        case VarType::FLOATTYPE:
            n->set((int) (*((float*)val0)));
            break;
        case VarType::INTTYPE:
            n->set( (*((int*)val0)));
            break;
        case VarType::LONGTYPE:
            n->set( (*((long*)val0)));
            break;
        case VarType::STRINGTYPE:
        case VarType::BOOLTYPE:
        default:
            std::cerr<<"Cannot assign a boolean or String value to a Nondet variable."<<endl;
        }//end switch
    }//end for it
}//end socreboard::finalizeNondetVars

//Call postpostcond function for all GlobalTime quantity manager instances
void scoreboard::postpostcond() {
    bool anyoneResolved = false;
    for (gtimeSet_t::iterator it = gtimeSet.begin(); it != gtimeSet.end(); it ++) {
        if (!anyoneResolved) {
            if ((*it)->isResolved()) {
                numGTQR++;
                anyoneResolved = true;
            }
        }
        (*it)->postpostcond();
    }
}

//Remove all built-in LOC registries from quantity managers
void scoreboard::clearBuiltInLOC(process * p) {
    for (std::map<int, QuantityManagerLOC*>::iterator it=BILOCQuantities.begin();
         it != BILOCQuantities.end();
         it ++) {
        QuantityManagerLOC *qm = it->second;
        qm->unregisterLOC(p, p);
    }

}//end scoreboard::clearBuiltInLOC

/*
 * set2run
 * set the schedule state of processes to RUN according to the rules specified
 * in the function
 */
bool scoreboard::set2run() {
    bool bContinue = false;

    for (int i = 0; i < size; i++) {
        ProgramCounter *pc = pcVector[i];
        if (pc->SchedState == SchedStateVal::RUN) bContinue = true;

        // if test scheduling and the process is in test scheduling
        // or
        // if not test scheduling and the process has UNKNOW schedule state
        if ((testSchedFlag && pc->SchedState == SchedStateVal::TESTSCHEDULING) ||
                (!testSchedFlag && pc->SchedState == SchedStateVal::UNKNOW)) {
            if (pc->_pendingEvents.size() == 0) {                        // no pending events => DONTRUN
                pc->SchedState = SchedStateVal::DONTRUN;
            } else if (pc->_pendingEvents.size() == 1) {        // only 1 pending event => RUN the corresponding process
                pc->SchedState = SchedStateVal::RUN;
                pc->selected = pc->_pendingEvents.begin()->second;
                bool bCon = bContinue;
                bContinue = true;
                if ((pc->funcType & FunctionType::ANNOTATION) != 0) {
                    if (testSchedFlag || !allProcStuck) {
                        pc->freeAllPendingEvents();
                        pc->SchedState = SchedStateVal::DONTRUN;
                        bContinue = bCon;
                    }
                }
            } else if (pc->_pendingEvents.size() > 1) {                // more than 1 pending event => choose a random critical section and RUN it
                /*
                  std::vector<event*> tmp;
                  std::vector<int> tmpsel;
                  std::vector<event*> orig;
                  std::vector<int> origsel;
                  eventMap_t::iterator it = pc->_pendingEvents.begin();
                  int min = INT_MAX;
                  int refc;
                  for (; it != pc->_pendingEvents.end(); it++) {
                  refc = it->first->getRefCount();
                  orig.push_back(it->first);
                  origsel.push_back(it->second);
                  if (refc > min) continue;
                  if (refc < min) {
                  tmp.clear();
                  tmpsel.clear();
                  min = refc;
                  }
                  tmp.push_back(it->first);
                  tmpsel.push_back(it->second);
                  }

                  pc->SchedState = SchedStateVal::RUN;
                  bContinue = true;
                  int choice;
                  event* selectedEvent;
                  if (tmp.size() > 0) {
                  choice = (int)(random() % (tmp.size()));
                  selectedEvent = tmp[choice];
                  pc->selected = tmpsel[choice];
                  } else {
                  choice = (int)(random() % (pc->_pendingEvents.size()));
                  selectedEvent = orig[choice];
                  pc->selected = origsel[choice];
                  }
                */
                int choice = (int)(random() % (pc->_pendingEvents.size()));
                eventMap_t::iterator it = pc->_pendingEvents.begin();
                for (int i=0; i<choice; i++) it++;
                pc->SchedState = SchedStateVal::RUN;
                event * selectedEvent = it->first;
                int selectedCS = it->second;
                pc->selected = selectedCS;
                bContinue = true;

                pc->freeAllPendingEvents();
                pc->addPendingEvent(selectedEvent, pc->selected);
            } // end if
        } //else
        //pc->SchedState = SchedStateVal::DONTRUN;
    } // end for

    return (bContinue || testSchedFlag);
} // end set2run

/*
 * set2dontrun
 * set the schedule state of processes to DONTRUN performing some necessary cleanup steps
 */
void scoreboard::set2dontrun(ProgramCounter * pc) {
    // look if this process is in synch with another one => set to DONTRUN the corresponding process
    for (eventMap_t::iterator it = pc->_pendingEvents.begin(); it != pc->_pendingEvents.end(); it++) {
        event *pEvent = it->first;

        for (eventSynchMap_t::iterator it_map = _eventSynchMap.begin(); it_map != _eventSynchMap.end(); it_map++) {
            EventMap *pEventMap = NULL;
            if (pEvent == it_map->first->_e) pEventMap = it_map->second;
            if (pEvent == it_map->second->_e) pEventMap = it_map->first;
            if (pEventMap) {
                if (pEventMap->_refCount != -1) {
                    pEventMap->_refCount = -1;
                    set2dontrun(pEventMap->_e->getProcess()->pc);
                }
            }
        } // end for

    } // end for

    // delete any pending event
    pc->freeAllPendingEvents();
    // set to DONTRUN
    pc->SchedState = SchedStateVal::DONTRUN;
} // end set2dontrun

/*
 * set2blocked
 * fill the vector of _pcBlocked according to synchs
 */
void scoreboard::set2blocked(ProgramCounter * pc, ProgramCounter * pc_blocked) {
    // search if the pc_blocked is already present in the list
    pcList_t::iterator it = std::find(pc->_pcBlocked.begin(), pc->_pcBlocked.end(), pc_blocked);
    if (it != pc->_pcBlocked.end()) return;

    // add the blocked pc "pc_blocked" to pc
    pc->_pcBlocked.push_back(pc_blocked);

    // fill the blocked queue according to synchs
    for (eventMap_t::iterator it = pc_blocked->_pendingEvents.begin(); it != pc_blocked->_pendingEvents.end(); it++) {
        event *pEvent = it->first;

        for (eventSynchMap_t::iterator it_map = _eventSynchMap.begin(); it_map != _eventSynchMap.end(); it_map++) {
            EventMap *pEventMap = NULL;
            if (pEvent == it_map->first->_e) pEventMap = it_map->second;
            if (pEvent == it_map->second->_e) pEventMap = it_map->first;
            if (pEventMap)
                set2blocked(pc, pEventMap->_e->getProcess()->pc);

        } // end for
    } // end for
} // end set2blocked

//Could pc run?
bool scoreboard::couldRun(ProgramCounter * pc) {
    if (pc->funcType == FunctionType::AWAIT) {
        for (eventMap_t::iterator it = pc->_pendingEvents.begin(); it != pc->_pendingEvents.end(); ) {

            bool flag = false;

            _2DList_t *pSetListAll_pc = pc->setList.back();
            simpleList_t *pSetList_pc = (*pSetListAll_pc)[it->second];

            //check if pc has setList intersecting with existing prevented interface list
            for (int i = 0; i < pSetList_pc->size(); i++) {
                node * n = (*pSetList_pc)[i];
                flag = ((medium*)n->object)->isIntfcPrevented(n->intfcName, pc);
                if (flag) break;
            } // end for

            //check if pc has testList intersecting with interfaces being used
            if (!flag) {
                simpleList_t::iterator it_nl;
                intList_t::iterator it_ssl;

                _2DList_t *pTestListAll_pc = pc->testList.back();
                simpleList_t *pTestList_pc = (*pTestListAll_pc)[it->second];
                for (int i = 0; i < pTestList_pc->size(); i++) {
                    node * n = (*pTestList_pc)[i];
                    flag = ((medium*)n->object)->isIntfcUsed(n->intfcName, pc);
                    if (flag) break;
                } // end for
            } //end if

            if (flag) {
                ++it;
                if (it != pc->_pendingEvents.end()) {
                    event * pKey = it->first;
                    it --;
                    it->first->inPendingList = false;
                    pc->_pendingEvents.erase(it);
                    it = pc->_pendingEvents.find(pKey);
                } else {
                    it --;
                    it->first->inPendingList = false;
                    pc->_pendingEvents.erase(it);
                    it = pc->_pendingEvents.end();
                }// end if
            } else
                it++;
        } // end for

        return (pc->_pendingEvents.size() > 0);
    } else if (pc->funcType == FunctionType::INTFCFUNC) {
        bool flag = false;

        //check if interface is being prevented
        flag = ((medium*)pc->obj.object)->isIntfcPrevented(pc->obj.intfcName, pc);

        if (flag) pc->_pendingEvents.clear();

        return (!flag);
    } else {
        return true;
    } // end if

} // end couldRun

//Could pc(await statement) run under the influence of cmp(DONTRUN/UNKNOW/RUNNING)?
bool scoreboard::couldRunAwait(ProgramCounter * pc, ProgramCounter * cmp) {
    for (eventMap_t::iterator it = pc->_pendingEvents.begin(); it != pc->_pendingEvents.end(); ) {

        bool flag = false;

        _2DList_t *pSetListAll_pc = pc->setList.back();
        simpleList_t *pSetList_pc = (*pSetListAll_pc)[it->second];

        //check if pc has a setList intersecting with a cmp preventList
        for (int i = 0; i < cmp->preventList.size(); i++) {
            simpleList_t *pSetList_cmp = cmp->preventList[i];
            //                        flag = contained(&(pc->obj), pSetList_cmp);
            if (!flag) flag = intersect(pSetList_cmp, pSetList_pc);
            if (flag) break;
        } // end for

        //                //check if cmp has any running program that pc is waiting for finishing
        //                if (flag) {
        //                        it++;
        //                        continue;
        //                } // end if

        if (!flag) {
            simpleList_t::iterator it_nl;
            intList_t::iterator it_ssl;

            _2DList_t *pTestListAll_pc = pc->testList.back();
            simpleList_t *pTestList_pc = (*pTestListAll_pc)[it->second];
            for (it_nl = cmp->nodeList.begin(), it_ssl = cmp->SchedStateList.begin(); it_nl != cmp->nodeList.end() && it_ssl != cmp->SchedStateList.end(); it_nl++, it_ssl++) {
                int state = *(*it_ssl);
                if (state != SchedStateVal::RUNNING) continue;
                flag = contained(*it_nl, pTestList_pc);
                if (flag) break;
            } // end for
        } //end if

        if (flag) {
            ++it;
            if (it != pc->_pendingEvents.end()) {
                event * pKey = it->first;
                it--;
                it->first->inPendingList = false;
                pc->_pendingEvents.erase(it);
                it = pc->_pendingEvents.find(pKey);
            } else {
                it--;
                it->first->inPendingList = false;
                pc->_pendingEvents.erase(it);
                it = pc->_pendingEvents.end();
            }// end if
        } else
            it++;
    } // end for

    return (pc->_pendingEvents.size() > 0);
}

//Could pc(interface function) run under the influence of cmp(DONTRUN/UNKNOW/RUNNING)?
bool scoreboard::couldRunIntfc(ProgramCounter * pc, ProgramCounter * cmp) {
    bool flag = false;

    //check if cmp(->preventList) prevents pc(->node) from running
    for (int j = 0; j < cmp->preventList.size(); j++) {
        simpleList_t *pPreventList = cmp->preventList[j];
        flag = contained(&(pc->obj), pPreventList);
        if (flag) break;
    } // end for

    if (flag) pc->_pendingEvents.clear();

    return (!flag);
}

//Could pc run together with cmp
bool scoreboard::couldRunBoth(int mode, ProgramCounter * pc, ProgramCounter * cmp) {

    event *pEvent_pc = NULL, *pEvent_cmp = NULL;

    for (intVect_t::iterator it_cs_pc = pc->_csOrder.begin(); (!pEvent_pc && !pEvent_cmp) && it_cs_pc != pc->_csOrder.end(); it_cs_pc++) {
        for (intVect_t::iterator it_cs_cmp = cmp->_csOrder.begin(); (!pEvent_pc && !pEvent_cmp) && it_cs_cmp != cmp->_csOrder.end(); it_cs_cmp++) {
            if (cmp->funcType & FunctionType::AWAIT) {
                _2DList_t *pSetListAll_cmp = cmp->setList.back();
                _2DList_t *pTestListAll_cmp = cmp->testList.back();

                simpleList_t *pSetList_cmp = (*pSetListAll_cmp)[*it_cs_cmp];
                simpleList_t *pTestList_cmp = (*pTestListAll_cmp)[*it_cs_cmp];
                if (contained(&(pc->obj), pSetList_cmp)) continue;

                if (pc->funcType & FunctionType::AWAIT) {
                    _2DList_t *pSetListAll_pc = pc->setList.back();
                    _2DList_t *pTestListAll_pc = pc->testList.back();

                    simpleList_t *pSetList_pc = (*pSetListAll_pc)[*it_cs_pc];
                    simpleList_t *pTestList_pc = (*pTestListAll_pc)[*it_cs_pc];

                    if (contained(&(cmp->obj), pSetList_pc)) continue;

                    //check if cmp(->setList[selected]) and pc(->setList[selected]) intersect
                    if (intersect(pSetList_cmp, pSetList_pc)) continue;


                    //check if cmp(->setList[selected]) and pc(->testList[selected]) intersect
                    if (intersect(pSetList_cmp, pTestList_pc)) continue;

                    //check if cmp(->testList[selected]) and pc(->setList[selected]) intersect
                    if (intersect(pTestList_cmp, pSetList_pc)) continue;
                } // end if
            } else {
                if (pc->funcType & FunctionType::AWAIT) {
                    _2DList_t *pSetListAll_pc = pc->setList.back();
                    _2DList_t *pTestListAll_pc = pc->testList.back();

                    simpleList_t *pSetList_pc = (*pSetListAll_pc)[*it_cs_pc];
                    simpleList_t *pTestList_pc = (*pTestListAll_pc)[*it_cs_pc];

                    if (contained(&(cmp->obj), pSetList_pc)) continue;
                } // end if
            } // end if

            // search the events in the pending event list of the corresponding processes
            if (pc->funcType & FunctionType::AWAIT) {
                for (eventMap_t::iterator it = pc->_pendingEvents.begin(); !pEvent_pc && it != pc->_pendingEvents.end(); it++) {
                    if (it->second == *it_cs_pc) pEvent_pc = it->first;
                } // end for
            } else {
                if (pc->_pendingEvents.size() == 1) pEvent_pc = pc->_pendingEvents.begin()->first;
            } // end if

            if (cmp->funcType & FunctionType::AWAIT) {
                for (eventMap_t::iterator it = cmp->_pendingEvents.begin(); !pEvent_cmp && it != cmp->_pendingEvents.end(); it++) {
                    if (it->second == *it_cs_cmp) pEvent_cmp = it->first;
                } // end for
            } else {
                if (cmp->_pendingEvents.size() == 1) pEvent_cmp = cmp->_pendingEvents.begin()->first;
            } // end if

            // in case both processes pc and cmp are nondeterministic, check if the critical sections
            // of the selected events prevent them to be executed according to the synch map

            if (pEvent_pc && pEvent_cmp) {
                // get corresponding synch event for pc
                EventMap *pEvent_pc_map = getEventSynchMap(pEvent_pc, true);
                // get corresponding synch event for cmp
                EventMap *pEvent_cmp_map = getEventSynchMap(pEvent_cmp, true);

                if (((pEvent_pc_map) &&
                            // found and cmp contains the requested event (pEvent_pc_map->_e) but it has not been selected
                            // (pEvent_cmp != pEvent_pc_map->_e)
                            (cmp->containsPendingEvent(pEvent_pc_map->_e) && pEvent_cmp != pEvent_pc_map->_e)) ||
                        ((pEvent_cmp_map) &&
                                // found and pc contains the requested event (pEvent_cmp_map->_e) but it has not been selected
                                // (pEvent_pc != pEvent_cmp_map->_e)
                                (pc->containsPendingEvent(pEvent_cmp_map->_e) && pEvent_pc != pEvent_cmp_map->_e))) {
                    pEvent_pc = NULL;        // abort

                    pEvent_cmp = NULL;        // abort
                } // end if
            } // end if
        } // end for
    } // end for

    // update
    if (mode == 1) {
        if (pEvent_pc && pEvent_cmp) {
            pc->setMustDo(pEvent_pc);
            cmp->setMustDo(pEvent_cmp);
        } // end if
    } // end if

    return (pEvent_pc && pEvent_cmp);
} // end couldRunBoth

//check if one node is in a set of nodes
bool scoreboard::contained(node * n, simpleList_t *pList) {

    bool rtn = false;
    for (int i = 0; i < pList->size(); i++) {
        node *pNode = (*pList)[i];
        if (n->object != pNode->object) continue;
        if ((pNode->intfcName != "all") && (n->intfcName != "all") && (pNode->intfcName != n->intfcName) ) continue;
        else {
            rtn = true;
            break;
        } // end if
    } // end for
    return rtn;
} // end contained

//check if two sets of nodes intersects
bool scoreboard::intersect(simpleList_t * pList1, simpleList_t * pList2) {
    bool rtn = false;
    for (int i = 0; i < pList1->size(); i++) {
        node *pNode = (*pList1)[i];
        if (contained(pNode, pList2)) {
            rtn = true;
            break;
        } // end if
    } // end for
    return rtn;
} // end intersect

//print out all scoreboard
void scoreboard::showSB() {
    ProgramCounter * pc;

    static String state[8]={"", "RUN", "DONTRUN", "RUNNING", "UNKNOW", "EVALUATE", "FINISHED", "TESTSCHEDULING"};
    static String ftype[5]={"", "AWAIT", "LABEL", "INTFCFUNC", "ANNOTATION"};
    static String astate[2]={"BEGIN", "END"};
    static String runMode[2]={"EXEC", "TRY"};

    cerr<<"************************Scoreboard************************"<<endl;
    bool allPCDone;
    PCSet_t::iterator PCit;
    int i = 0;
    if (synchonly | ltlonly) {
        PCit = caredPC.begin();
        allPCDone = (PCit != caredPC.end());
        if (allPCDone) pc = (*PCit);
    } else {
        allPCDone = (i < size);
        if (allPCDone) pc = pcVector[i];
    }

    while (allPCDone) {

        cerr<<"ProgramCounter #"<<i+1<<"\t"<<pc<<endl;
        if (debugmask[0]) cerr<<"process * p="<<(pc->p)<<endl;
        if (debugmask[1]) cerr<<"process name="<<pc->p->name()<<endl;
        if (debugmask[2]) cerr<<"process id="<<pc->p->_id<<endl;
        if (debugmask[3]) cerr<<"int mode="<<runMode[pc->mode]<<endl;
        if (debugmask[4]) cerr<<"node obj={"<<pc->obj.object->name()<<", "<<pc->obj.intfcName<<"}"<<endl;
        if (debugmask[5]) cerr<<"String stLabel="<<pc->stLabel<<endl;
        if (debugmask[6]) cerr<<"int SchedState="<<state[pc->SchedState]<<endl;

        if (debugmask[7]) {
            cerr<<"pending events={"<<endl;
            for (eventMap_t::iterator it = pc->_pendingEvents.begin(); it != pc->_pendingEvents.end(); it++) {
                event *pEvent = it->first;
                cerr<<pEvent->getProcess()->name();
                cerr<<", ("<<pEvent->getAction()->getObject()->name();
                cerr<<" :: "<<pEvent->getAction()->getName()<<"), ";
                cerr<<astate[pEvent->getActionState()]<<" CS#="<<it->second;
                cerr<<" RefCount="<<pEvent->getRefCount();
                cerr<<" ImplCount="<<pEvent->getImplCount()<<endl;
            } // end for
            cerr<<"}"<<endl;
        }
        if (debugmask[8]) {
            cerr<<"in synch=";
            if (pc->_inSynch) cerr<<"yes"<<endl;
            else cerr<<"no"<<endl;
        }

        if (debugmask[9]) {
            cerr<<"int funcType="<<pc->funcType<<" [ ";
            for (int j=1, k=1; j<=4; j++, k=k<<1) if (pc->funcType & k) cerr<<ftype[j]<<" ";
            cerr<<"]"<<endl;
        }

        if (debugmask[10] && (pc->funcType & FunctionType::AWAIT) != 0) {
            cerr<<"bool * cond={";
            if (!pc->testList.empty()) {
                if (pc->cond) {
                    _2DList_t *p = pc->testList.back();
                    for (int j = 0; j < p->size(); j++) cerr<<pc->cond[j]<<" ";
                } // end if
            }
            cerr<<"}"<<endl;
        }

        if (debugmask[11] && (pc->funcType & FunctionType::AWAIT) != 0)
            cerr<<"int selected="<<pc->selected<<endl;
        if (debugmask[12] && (pc->funcType & FunctionType::AWAIT) != 0) {
            cerr<<"csOrder={ ";
            for (int j = 0; j < pc->_csOrder.size(); j++) cerr<<pc->_csOrder[j]<<" ";
            cerr<<"}"<<endl;
        }

        if (debugmask[13] && (pc->funcType & FunctionType::AWAIT) != 0) {
            cerr<<"testList={ ";
            for (int j = 0; j < pc->testList.size(); j++) {
                _2DList_t *pTestListStack = pc->testList[j];

                cerr<<"#";
                for (int k = 0; k < pTestListStack->size(); k++) {
                    simpleList_t *pTestList = (*pTestListStack)[k];

                    cerr<<"[";
                    for (int m = 0; m < pTestList->size(); m++) {

                        node *pNode = (*pTestList)[m];
                        cerr<<"("<<pNode->object->name()<<","<<pNode->intfcName<<")";
                    } // end for
                    cerr<<"]";
                } // end for
                cerr<<"# ";
            } // end for
            cerr<<"}"<<endl;
        }

        if (debugmask[14] && (pc->funcType & FunctionType::AWAIT) != 0) {
            cerr<<"setList ={ ";
            for (int j = 0; j < pc->setList.size(); j++) {
                _2DList_t *pSetListStack = pc->setList[j];

                cerr<<"#";
                for (int k = 0; k < pSetListStack->size(); k++) {
                    simpleList_t *pSetList = (*pSetListStack)[k];

                    cerr<<"[";
                    for (int m = 0; m < pSetList->size(); m++) {
                        node *pNode = (*pSetList)[m];

                        cerr<<"("<<pNode->object->name()<<","<<pNode->intfcName<<")";
                    } // end for
                    cerr<<"]";
                } // end for
                cerr<<"# ";
            } // end for
            cerr<<"}"<<endl;
        }

        if (debugmask[15]) {
            cerr<<"preventList={ ";
            for (int k = 0; k < pc->preventList.size(); k++) {
                simpleList_t *pPreventList = pc->preventList[k];

                cerr<<"[";
                for (int m = 0; m < pPreventList->size(); m++) {
                    node *pNode = (*pPreventList)[m];
                    cerr<<"("<<pNode->object->name()<<","<<pNode->intfcName<<")";
                } // end for
                cerr<<"]";
            } // end for
            cerr<<"}"<<endl;
        }

        if (debugmask[16]) {
            cerr<<"nodeList=[ ";
            for (int m = 0; m < pc->nodeList.size(); m++) {
                node *pNode = pc->nodeList[m];
                cerr<<"("<<pNode->object->name()<<","<<pNode->intfcName<<")";
            } // end for
            cerr<<"]"<<endl;
        }

        if (debugmask[17]) {
            cerr<<"labelList=[ ";
            for (int m = 0; m < pc->labelList.size(); m++)
                cerr<<"("<<pc->labelList[m]->c_str()<<")";
            cerr<<"]"<<endl;
        }

        if (debugmask[18]) {
            cerr<<"SchedStateList=[ ";
            for (int m = 0; m < pc->SchedStateList.size(); m++)
                cerr<<"("<<state[*(pc->SchedStateList[m])]<<")";
            cerr<<"]"<<endl;
        }

        if (debugmask[19]) {
            cerr<<"funcTypeList=[ ";
            for (int m = 0; m < pc->funcTypeList.size(); m++) {
                int ft = *(pc->funcTypeList[m]);
                cerr<<"(";
                if (ft & FunctionType::ANNOTATION) cerr<<"ANNOTATION:";
                cerr<<ftype[ft & (~FunctionType::ANNOTATION)]<<")";
            }
            cerr<<"]"<<endl;
        }

        if (debugmask[20]) {
            cerr<<"potentialUsedMedia=[ ";
            for (MediaSet_t::iterator it = pc->_potentialUsedMedia->begin(); it!=pc->_potentialUsedMedia->end(); it++)
                cerr<<(*it)->name()<<"("<<(*it)<<") ";
            cerr<<"]"<<endl;
        }

        if (debugmask[21]) {
            cerr<<"potentialUsedMediaList={ ";
            for (int m = 0; m<pc->_potentialUsedMediaList.size(); m++) {
                cerr<<"#[";
                MediaSet_t * p = pc->_potentialUsedMediaList[m];
                for (MediaSet_t::iterator it = p->begin(); it!=p->end(); it++)
                    cerr<<(*it)->name()<<"("<<(*it)<<") ";
                cerr<<"]# ";
            }
            cerr<<"}"<<endl;
        }

        cerr<<endl;
        if (synchonly | ltlonly) {
            PCit++;
            allPCDone = (PCit != caredPC.end());
            if (allPCDone) pc = (*PCit);
            i++;
        } else {
            i++;
            allPCDone = (i < size);
            if (allPCDone) pc = pcVector[i];
        }

    }

    if (showmedia) {
        cerr<<"------ Media Information ------"<<endl;
        for (int i = 0; i < msize; i++) {
            medium *m = mediaVector[i];

            cerr<<"Medium #"<<i+1<<"\t"<<m<<endl;
            cerr<<"medium name="<<m->name()<<endl;
            cerr<<"medium id="<<m->_id<<endl;

            cerr<<"used interfaces:"<<endl;
            int idx = 1;
            for (interfaceUsers_t::iterator it=m->_intfcUsed.begin(); it!=m->_intfcUsed.end(); it++) {
                const char * intfcName = it->first;
                userPCs_t * pcs = it->second;
                cerr<<idx<<". interface name:\t"<<intfcName<<endl;
                idx++;
                for (userPCs_t::iterator uit=pcs->begin(); uit!=pcs->end(); uit++) {
                    ProgramCounter * pc = *uit;
                    cerr<<"\t"<<pc->p->name()<<"("<<pc<<")"<<endl;
                }
            }

            cerr<<"prevented interfaces:"<<endl;
            idx = 1;
            for (interfaceUsers_t::iterator it=m->_intfcPrevented.begin(); it!=m->_intfcPrevented.end(); it++) {
                const char * intfcName = it->first;
                userPCs_t * pcs = it->second;
                cerr<<idx<<". interface name:\t"<<intfcName<<endl;
                idx++;
                for (userPCs_t::iterator uit=pcs->begin(); uit!=pcs->end(); uit++) {
                    ProgramCounter * pc = *uit;
                    cerr<<"\t"<<pc->p->name()<<"("<<pc<<")"<<endl;
                }
            }

            cerr<<"potential users:"<<endl;
            for (PCSet_t::iterator it=m->_potentialUserPCs.begin(); it!=m->_potentialUserPCs.end(); it++) {
                ProgramCounter * pc = *it;
                cerr<<"\t"<<pc->p->name()<<"("<<pc<<")"<<endl;
            }

            cerr<<endl;
        }
    }

    if (numSynchEventGroup > 0) {
        cerr<<"------ Synch Events Information ------"<<endl;
        cerr<<"Number of synch event groups = "<<numSynchEventGroup<<endl;
        for (int i=0; i<numSynchEventGroup; i++) {
            cerr<<"Group "<<i<<":"<<endl;
            cerr<<"\tNumber of synch events "<<numSynchEventEachGroup[i]<<endl;
            cerr<<"\tEnabled synch events "<<enabledSynchEventEachGroup[i]<<endl;
            cerr<<"\tEnabled synch events PCs"<<endl;
            for (int j=0; j<enabledSynchEventEachGroup[i]; j++) {
                ProgramCounter *pc = enabledSynchEventPC[i][j];
                cerr<<"\t\t"<<pc->p->name()<<"("<<pc<<")"<<endl;
            }
        }
        cerr<<endl;
    }

#ifdef DEBUG_INTERACTIVE
    char c;
    cin >> c;
#endif
}

//Print out the event(s) in the pending event list of a process
void scoreboard::dumpEvent(ProgramCounter *pc) {
    cerr<<pc->p->name()<<": \t";
    if (pc->funcType & FunctionType::ANNOTATION)
        cerr<<"QA ";
    if (pc->_inSynch)
        cerr<<"synch ";
    eventMap_t::iterator it;
    eventMap_t::iterator endit;
    if (pc->_pendingEvents.size() > 0) {
        it = pc->_pendingEvents.begin();
        endit = pc->_pendingEvents.end();
    } else {
        it = pc->_oldPendingEvents.begin();
        endit = pc->_oldPendingEvents.end();
    }
    for (; it != endit; it++) {
        it->first->show();
        if ((pc->funcType & FunctionType::AWAIT) != 0)
            cerr<<" CS#"<<it->second;
    }
    switch (pc->SchedState) {
    case SchedStateVal::RUN:
        cerr<<" RUN";
        break;
    case SchedStateVal::DONTRUN:
        cerr<<" DONTRUN";
        break;
    case SchedStateVal::RUNNING:
        cerr<<" RUNNING";
        break;
    case SchedStateVal::UNKNOW:
        cerr<<" UNKNOWN";
        break;
    case SchedStateVal::EVALUATE:
        cerr<<" EVALUATE";
        break;
    case SchedStateVal::FINISHED:
        cerr<<" FINISHED";
        break;
    case SchedStateVal::TESTSCHEDULING:
        cerr<<" TESTSCHEDULING";
        break;
    default:
        cerr<<" ???";
        break;
    }
    cerr<<endl;
}

//Invoke those processes that are scheduled to run
void scoreboard::invoke() {
    static short _maxQAProc = 0;
    static short _allTimeMaxQAProc = 0;
    bool flag = false;
    bool preAllProcStuck;
    bool preResolvedQA;
    bool resolvedQA;
    short reqPCsize;

    if (!debug_flag && gxi>0) {
        if (gxiindex >= gxi) {
            debug_flag = true;
        }
    }
    if (!dumpev && evgxi>0)
        if (gxiindex >= evgxi)
            dumpev = true;

    //if (!testSchedFlag)
    preAllProcStuck = allProcStuck;
    preResolvedQA = allProcStuck && !testSchedFlag;
    reqPCsize = reqPCs.size();
    if (reqPCsize>0) numqaproc++;

    if (dumpev) {
        if (testSchedFlag) {
            cerr<<"**********Evaluating Guard ["<<gxiindex<<",";
            cerr<<requestQA<<","<<allProcStuck<<","<<cyclesB4QA<<",";
            cerr<<maxQAProc<<","<<reqPCsize<<"] **********"<<endl;
        } else {
            cerr<<"********************** Executing ["<<gxiindex<<",";
            cerr<<requestQA<<","<<allProcStuck<<","<<cyclesB4QA<<",";
            cerr<<maxQAProc<<","<<reqPCsize<<"] *************************"<<endl;
        }
    }

    if (testSchedFlag) {
        for (int i = 0; i < size; i++) {
            if (pcVector[i]->SchedState == SchedStateVal::RUN ||
                    pcVector[i]->mode == RunType::TRY ||
                    pcVector[i]->_inSynch ) {
                flag = true;
                if (timestat) NumRunProcesses++;
                notify(DeltaCycle, pcVector[i]->invoke);
                if (dumpev)
                    dumpEvent(pcVector[i]);
                if (debug_flag) {
                    cerr<<"Invoke process "<<pcVector[i]->p->name()<<" ("<<pcVector[i]<<")"<<endl;
                }
            }
        }
        requestQA = false;
        //cyclesB4QA ++;
    } else {
        allProcStuck = true;
        for (int i = 0; i < size; i++) {
            if (!preResolvedQA)
                if (reqPCs.find(pcVector[i]) != reqPCs.end())
                    continue;

            if ((pcVector[i]->funcType & FunctionType::AWAIT) ||
                    (pcVector[i]->funcType & FunctionType::ANNOTATION) ||
                    (pcVector[i]->SchedState == SchedStateVal::RUN) ||
                    (pcVector[i]->_inSynch) )

                {
                    flag = true;
                    if (timestat)
                        if (pcVector[i]->SchedState == SchedStateVal::RUN)
                            NumRunProcesses++;
                    notify(DeltaCycle, pcVector[i]->invoke);
                    if (dumpev)
                        dumpEvent(pcVector[i]);
                    if (debug_flag) {
                        cerr<<"Invoke process "<<pcVector[i]->p->name()<<" ("<<pcVector[i]<<")"<<endl;
                    }
                }
            if (pcVector[i]->SchedState == SchedStateVal::RUN
                && ((pcVector[i]->funcType & FunctionType::AWAIT)==0 ||
                    !pcVector[i]->p->mappingProcess)) {
                allProcStuck = false;
            }
        }// end for

        if (reqPCsize>0) { //there are QAs
            if (reqPCsize > _maxQAProc) {
                _maxQAProc = reqPCsize;
            }
            if (reqPCsize > _allTimeMaxQAProc) {
                _allTimeMaxQAProc = reqPCsize;
            }

            if (!preResolvedQA) {
                if (allProcStuck || cyclesB4QA > maxCycle) {
                    if (!allProcStuck) {
                        _maxQAProc--;
                    }

                    allProcStuck = true;
                    requestQA = true;
                    cyclesB4QA = 0;
                } else {
                    if (reqPCsize >= maxQAProc) {
                        if (_maxQAProc < _allTimeMaxQAProc && cyclesB4QA < minCycle)
                            _maxQAProc++;
                        allProcStuck = true;
                        requestQA = true;
                        cyclesB4QA = 0;
                    } else {
                        requestQA = false;
                    }
                } //end if
            } else {
                requestQA = allProcStuck;
            }//end if(!preResolvedQA)

            if (gxiindex > size*5) {
                maxQAProc = _maxQAProc;
                //maxCycle = (size - maxQAProc); //GY: adjust maxCycle more clever
                if (dumpev)
                    cerr<<"QA maxQAProc="<<maxQAProc<<endl;
            }

            if (allProcStuck)
                cyclesB4QA = 0;
            else
                cyclesB4QA ++;

            if (requestQA) {
                flag = true;
                for (PCSet_t::iterator it=reqPCs.begin(); it!=reqPCs.end(); it++) {
                    (*it)->SchedState == SchedStateVal::DONTRUN;
                    notify(DeltaCycle, (*it)->invoke);
                    if (dumpev)
                        dumpEvent(*it);
                    if (debug_flag) {
                        cerr<<"Invoke process "<<(*it)->p->name()<<" ("<<(*it)<<")"<<endl;
                    }
                }
           }
        } else {    //no QAs
            requestQA = allProcStuck;
            if (resolvedQA)
                cyclesB4QA = 0;
        }
    } // end if

    if (preResolvedQA) {
        reqPCs.clear();
    }

    if (dumpev) {
        cerr<<"********************** ["<<gxiindex<<",";
        cerr<<requestQA<<","<<allProcStuck<<","<<cyclesB4QA<<",";
        cerr<<maxQAProc<<","<<reqPCs.size()<<"] *************************";
        cerr<<endl<<endl;
    }

    gxiindex++;

    if (!flag) {
        cerr<<"Finished or Deadlocked!"<<endl;
        //showSB();
        sc_sim_stop();
    } // end if

    wait(_mng.pc->invoke);

}

//Fix: should support more than two synched events
void scoreboard::registerSynchEvents(event *e1, event *e2) {
    _eventSynchMap.insert(eventSynchMap_t::value_type(new EventMap(e1), new EventMap(e2)));
} // end registerSynchEvents

EventMap *scoreboard::getEventSynchMap(event *e, bool pair) {
    EventMap *pResult = NULL;

    // search this event in the synch map
    for (eventSynchMap_t::iterator it = _eventSynchMap.begin(); !pResult && it != _eventSynchMap.end(); it++) {
        EventMap *pEM_1 = it->first;
        EventMap *pEM_2 = it->second;
        if (pEM_1->_e == e) pResult = pair ? pEM_2 : pEM_1;
        if (pEM_2->_e == e) pResult = pair ? pEM_1 : pEM_2;
    } // end for

    return pResult;
} // end getEventSynchMap

//bool scoreboard::atLeastOneRun() {
//          for (int i = 0; i < size; i++)
//        if (pcVector[i]->SchedState == SchedStateVal::RUN) return true;
//     return false;
//} // end atLeastOneRun

// Initialize SAT solver ZChaff
void scoreboard::initSATSolver() {
    SatMgr = SAT_InitManager();
    if (heuristic == 4) SAT_AddHookFun(SatMgr, favor_1_sat_hook, 1);

} // end initSATSolver

// Free SAT solver ZChaff
void scoreboard::freeSATSolver() {
    SAT_ReleaseManager(SatMgr);
} // end freeSATSolver

// Create variables in and get their indices from ZChaff for all events in eventVarIndex
void scoreboard::createVars(eventVarIndexMap_t& evi) {
    if (evi.empty()) return;
    int index;
    for (eventVarIndexMap_t::iterator it=evi.begin(); it!=evi.end(); it++) {
        index = SAT_AddVariable(SatMgr);
        it->second = index;
    } // end for
} // end createVars

// Add synch constraints as CNF's to ZChaff
void scoreboard::addSynchClauses(eventVarIndexMap_t& evi, eventSynchMap_t& esm) {
    int clause[2];
    for (eventSynchMap_t::iterator it=esm.begin(); it!=esm.end(); it++) {
        clause[0] = POSLIT(evi[it->first->_e]);
        clause[1] = NEGLIT(evi[it->second->_e]);
        SAT_AddClause(SatMgr, clause, 2);
        clause[0] ++; //change to negative literal
        clause[1] --;   //change to positive literal
        SAT_AddClause(SatMgr, clause, 2);
    } // end for
} // end addSynchClauses

// Add conflict constraints stored in conflictRSet as CNF's to ZChaff
void scoreboard::addConflictClauses(eventVarIndexMap_t& evi, conflictRelationSet_t& crs) {
    int clause[2];
    for (conflictRelationSet_t::iterator it=crs.begin(); it!=crs.end(); it++) {
        clause[0] = NEGLIT(evi[it->first]);
        clause[1] = NEGLIT(evi[it->second]);
        SAT_AddClause(SatMgr, clause, 2);
    } // end for
} // end addConflictClauses

// Add number (lower bound) of 1-value variable constraints as CNF's to ZChaff
void scoreboard::addNumberOfOnesClauses(eventVarIndexMap_t::iterator it,
        int size, int need, intVect_t& result) {
    if (need == 0) {
        int ss = result.size();
        if (ss==0) return;
        int *clause = new int[ss];
        for (int i=0; i<ss; i++) clause[i] = result[i];
        SAT_AddClause(SatMgr, clause, ss);
        //    SAT_AddClause(SatMgr, result.begin(), ss);
        delete clause;
        return;
    } // end if

    result.push_back(POSLIT(it->second));
    addNumberOfOnesClauses(++it, size-1, need-1, result);
    --it;
    result.pop_back();
    if (size>need) addNumberOfOnesClauses(++it, size-1, need, result);
    --it;
} // end addNumberOfOnesClauses

// Add LTL constraints coming from transition relations in Buchi Automaton
void scoreboard::addLTLConstraintClauses(eventVarIndexMap_t& evi) {
    topNetlist->addLTLConstraintsToSAT(SatMgr, evi);
} // end addLTLConstraintClauses


// Solve the SAT problem by calling ZChaff
enum SAT_StatusT  scoreboard::solveSAT() {
    return (enum SAT_StatusT) SAT_Solve(SatMgr);
} // end solveSAT

// Get the result from SAT solver
void scoreboard::getSATResult(eventVarIndexMap_t& evi) {
    for (eventVarIndexMap_t::iterator it=evi.begin(); it!=evi.end(); it++) {
        evi[it->first] = SAT_GetVarAsgnment(SatMgr, it->second);
    } //end for
} // end getSATResult

// Apply SAT result to the scheduling
bool scoreboard::applySATResult(eventVarIndexMap_t& evi) {
    bool flag = false;
    for (eventVarIndexMap_t::iterator it=evi.begin(); it!=evi.end(); it++) {
        if (it->second==0) // variable is 0 => corresponding event is disabled
            it->first->getProcess()->pc->setMustNotDo(it->first);
        else
            flag = true;
    } //end for
    return flag;
} // end applySATResult

// Count the number of 1-value variables
int scoreboard::countOnes(eventVarIndexMap_t& evi) {
    int numZeros = 0;
    for (eventVarIndexMap_t::iterator it=evi.begin(); it!=evi.end(); it++) {
        if (it->second==0) numZeros++; // variable is 0
    } //end for
    return (evi.size() - numZeros);
} // end countOnes

// Randomly choose one event and set it (and its synched event, if any) to run if possible
bool scoreboard::pickOneEventRandomly() {
    int * pickOrder;
    pickOrder = intArrayGen(size, true);
    bool picked = false;
    ProgramCounter *pc, *mpc;
    pc = mpc = NULL;

    for (int i=0; i<size && !picked; i++) {
        pc = pcVector[pickOrder[i]];
        if (testSchedFlag) {
            if (pc->SchedState != SchedStateVal::TESTSCHEDULING) continue;
        } else {
            if (pc->SchedState != SchedStateVal::UNKNOW) continue;
        } // end if

        int psize;
        if ((psize=pc->_pendingEvents.size()) == 0) continue;

        for (; psize>0; psize--) {
            int choice = (int)(random() % psize);
            eventMap_t::iterator it = pc->_pendingEvents.begin();
            for (int ii = 0 ; it != pc->_pendingEvents.end() && ii < choice; it++, ii++);
            event * e = it->first;
            pc->selected = it->second;

            EventMap * emap = getEventSynchMap(e, true);
            if (emap==NULL) {
                picked = true;
                break;
            } else {
                event *em = emap->_e;
                mpc = em->getProcess()->pc;
                if ((testSchedFlag && mpc->SchedState != SchedStateVal::TESTSCHEDULING) ||
                        (!testSchedFlag && mpc->SchedState != SchedStateVal::UNKNOW)) {
                    pc->setMustNotDo(e);
                    continue;
                } else {
                    pc->setMustDo(e);
                    mpc->setMustDo(em);
                    picked = true;
                    break;
                } // end if
            } // end if
        } // end for
    } // end for

    // make all other processes not to run
    for (int i=0; i<size; i++) {
        if ((pcVector[i]==pc) || (pcVector[i]==mpc)) continue;
        pcVector[i]->_pendingEvents.clear();
    } // end for


    return picked;
} // end pickOneEventRandomly

bool scoreboard::schedule() {
    long tmp;
    struct rusage ru;
    if (timestat) {
        NumSMSchedulingRounds ++;
        getrusage(RUSAGE_SELF, &ru);
        tmp = ru.ru_utime.tv_sec*1000000+ru.ru_utime.tv_usec+
            ru.ru_stime.tv_sec*1000000+ru.ru_stime.tv_usec;
    } // end if

    bool flag = (this->*SchedAlg)();

    if (timestat) {
        getrusage(RUSAGE_SELF, &ru);
        tmp = ru.ru_utime.tv_sec*1000000+ru.ru_utime.tv_usec+
            ru.ru_stime.tv_sec*1000000+ru.ru_stime.tv_usec - tmp;
        SMSchedulingTime += tmp;
    } // end if

    return flag;
}

// Scheduling algorithm 0: the original pass1-2-3... algorithm
// This one is the default algorithm used.
bool scoreboard::heuristic0() {
    bool flag;

    if (ltl && !testSchedFlag) {
        bool occur = topNetlist->checkOccuredLTLEvents();
        if (debug_flag && debug_level>3) {
            if (!occur)
                cerr<<"No event(s) in LTL formula occuring."<<endl;
        }
    }

    if (!testSchedFlag && ANNOTATION_ENABLED && allProcStuck) {
        topNetlist->resolve((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after resolving quantity managers"<<endl;
            showSB();
        }
        numQR ++;
    }

    if (ltl && !testSchedFlag) {
        topNetlist->preGuideLTL();
        if (debug_flag && debug_level>3) {
            cerr<<endl<<"after preGuideLTL"<<endl;
            showSB();
        }
    }

    flag = pass1();
    if (debug_flag && debug_level>1) {
        cerr<<endl<<"after pass1"<<endl;
        showSB();
    }

    if (flag) {
        flag = pass2();
        if (debug_flag && debug_level>1) {
            cerr<<endl<<"after pass2"<<endl;
            showSB();
        }
    }

    /*
      if (flag) {
      flag = pass3();
      if (debug_flag && debug_level>4) {
      cerr<<endl<<"after pass3"<<endl;
      showSB();
      }
      }
    */

    if (!testSchedFlag && flag) {
        do {
            resolveSynch();
        } while (resolveSynchImply());

        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after resolveSynch and resolveSynchImply"<<endl;
            showSB();
        }
    }

    if (!testSchedFlag && ANNOTATION_ENABLED && allProcStuck) {
        topNetlist->postcond((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after running postcond of quantity managers"<<endl;
            showSB();
        }
    }

    event * passLTL = ((event*) 1); // NULL:  passed; -1: violated; other: need to iterate
    flag = true;
    if (ltl && !testSchedFlag) {
        //while ((passLTL!=NULL) && (passLTL!=((event*)-1)) && flag ) {
            //flag = set2run();
            passLTL = topNetlist->postCheckLTL(true);
        //} // end while
    } //else {
        flag = set2run();
    //} // end if

    if (!testSchedFlag)
        finalizeNondetVars();

    if (debug_flag && debug_level>3) {
        cerr<<endl<<"after set2run";
        if (ltl) cerr<<" and postCheckLTL";
        cerr<<endl;
        showSB();
    }

    if (!testSchedFlag && ANNOTATION_ENABLED && allProcStuck) {
        postpostcond();
    }

    return flag;
} // end heuristic0

// Scheduling algorithm 1: randomly choose one process to run
bool scoreboard::heuristic1() {
    bool flag;

    if (ANNOTATION_ENABLED) {
        topNetlist->resolve((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after resolving quantity managers"<<endl;
            showSB();
        }
    }

    if (ltl) {
        topNetlist->preGuideLTL();
        if (debug_flag && debug_level>3) {
            cerr<<endl<<"after preGuideLTL"<<endl;
            showSB();
        }
    }

    flag = pass1();
    if (debug_flag && debug_level>1) {
        cerr<<endl<<"after pass1"<<endl;
        showSB();
    }

    event * passLTL = ((event*) 1); // NULL:  passed; -1: violated; other: need to iterate
    if (flag && ltl) {
        while ((passLTL!=NULL) && (passLTL!=((event*)-1))) {
            passLTL = topNetlist->postCheckLTL(false);
        } // end while
    } // end if

    if (flag) {
        flag = pickOneEventRandomly();
        if (debug_flag && debug_level>1) {
            cerr<<endl<<"after randomly pick one event"<<endl;
            showSB();
        }
    }

    if (ANNOTATION_ENABLED) {
        topNetlist->postcond((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after running postcond of quantity managers"<<endl;
            showSB();
        }
    }

    passLTL = ((event*) 1);
    if (ltl && flag) {
        while ((passLTL!=NULL) && (passLTL!=((event*)-1)) && flag ) {
            flag = set2run();
            passLTL = topNetlist->postCheckLTL(true);
        } // end while
    } else {
        flag = set2run();
    } // end if

    if (debug_flag && debug_level>3) {
        cerr<<endl<<"after set2run";
        if (ltl) cerr<<" and postCheckLTL";
        cerr<<endl;
        showSB();
    }

    return flag;
} // end heuristic1

// Scheduling algorithm 2: call zchaff to find a set of consistent processes to run
bool scoreboard::heuristic2() {
    bool flag;

    if (ANNOTATION_ENABLED) {
        topNetlist->resolve((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after resolving quantity managers"<<endl;
            showSB();
        }
    }

    if (ltl) {
        topNetlist->preGuideLTL();
        if (debug_flag && debug_level>3) {
            cerr<<endl<<"after preGuideLTL"<<endl;
            showSB();
        }
    }

    flag = pass1();
    if (debug_flag && debug_level>1) {
        cerr<<endl<<"after pass1"<<endl;
        showSB();
    }

    if (flag) {
        // collect all pending events, and the confliction among them
        collectEventConflicts();

        // Initialize SAT solver ZChaff
        initSATSolver();

        // Create variables in and get their indices from ZChaff for all events in eventVarIndex
        createVars(eventVarIndex);

        // Add synch constraints as CNF's to ZChaff
        addSynchClauses(eventVarIndex, _eventSynchMap);

        // Add conflict constraints stored in conflictRSet as CNF's to ZChaff
        addConflictClauses(eventVarIndex, conflictRSet);

        // At least one event should run. Add this constraints as CNF's to ZChaff
        intVect_t dummy;
        addNumberOfOnesClauses(eventVarIndex.begin(), (int)eventVarIndex.size(), (int)eventVarIndex.size(), dummy);

        // Add LTL constraints coming from transition relations in Buchi Automaton
        if (ltl) addLTLConstraintClauses(eventVarIndex);

        // Solve the SAT problem by calling ZChaff
        switch(solveSAT()) {
        case UNSATISFIABLE:
            cerr<<"Unable to finish simulation, because your design has internal conflicts."<<endl;
            sc_stop();
            break;
        case SATISFIABLE:
            break;
        default:
            cerr<<"Unable to finish simulation, because the underline SAT engine cannot handle the problem."<<endl;
            sc_stop();
        } // end switch

        // Get the result from SAT solver
        getSATResult(eventVarIndex);

        // Apply SAT result to the scheduling
        flag = applySATResult(eventVarIndex);

        // Free SAT solver ZChaff
        freeSATSolver();

        if (debug_flag && debug_level>1) {
            cerr<<endl<<"after applySATResult"<<endl;
            showSB();
        }

    } // end if

    if (ANNOTATION_ENABLED) {
        topNetlist->postcond((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after running postcond of quantity managers"<<endl;
            showSB();
        }
    }

    event * passLTL = ((event*) 1); // NULL:  passed; -1: violated; other: need to iterate
    flag = true;
    if (ltl) {
        while ((passLTL!=NULL) && (passLTL!=((event*)-1)) && flag ) {
            flag = set2run();
            passLTL = topNetlist->postCheckLTL(true);
        } // end while
    } else {
        flag = set2run();
    } // end if

    if (debug_flag && debug_level>3) {
        cerr<<endl<<"after set2run";
        if (ltl) cerr<<" and postCheckLTL";
        cerr<<endl;
        showSB();
    }

    return flag;
} // end heuristic2

// Scheduling algorithm 3: iteratively call zchaff to find a MAX set of consistent processes to run
bool scoreboard::heuristic3() {
    bool flag;

    if (ANNOTATION_ENABLED) {
        topNetlist->resolve((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after resolving quantity managers"<<endl;
            showSB();
        }
    }

    if (ltl) {
        topNetlist->preGuideLTL();
        if (debug_flag && debug_level>3) {
            cerr<<endl<<"after preGuideLTL"<<endl;
            showSB();
        }
    }

    flag = pass1();
    if (debug_flag && debug_level>1) {
        cerr<<endl<<"after pass1"<<endl;
        showSB();
    }

    if (flag) {
        // collect all pending events, and the confliction among them
        collectEventConflicts();

        bool foundMaxOnes = false;
        int numOfOnes = 0;
        eventVarIndexMap_t result;

        while (!foundMaxOnes) {

            // Initialize SAT solver ZChaff
            initSATSolver();

            // Create variables in and get their indices from ZChaff for all events in eventVarIndex
            createVars(eventVarIndex);

            // Add synch constraints as CNF's to ZChaff
            addSynchClauses(eventVarIndex, _eventSynchMap);

            // Add conflict constraints stored in conflictRSet as CNF's to ZChaff
            addConflictClauses(eventVarIndex, conflictRSet);

            // Add number (lower bound) of 1-value variable constraints as CNF's to ZChaff
            intVect_t dummy;
            addNumberOfOnesClauses(eventVarIndex.begin(), (int)eventVarIndex.size(), eventVarIndex.size()-numOfOnes, dummy);

            // Add LTL constraints coming from transition relations in Buchi Automaton
            if (ltl) addLTLConstraintClauses(eventVarIndex);

            // Solve the SAT problem by calling ZChaff
            NumIterH3 ++;
            switch(solveSAT()) {
            case UNSATISFIABLE:
                if (numOfOnes == 0) {
                    cerr<<"Unable to finish simulation, because your design has internal conflicts."<<endl;
                    sc_stop();
                } else
                    foundMaxOnes = true;
                break;
            case SATISFIABLE:
                // Get SAT result from SAT solver
                getSATResult(eventVarIndex);

                // Count the number of 1-value variables
                int tmp;
                tmp = countOnes(eventVarIndex);

                if (tmp>numOfOnes) {
                    numOfOnes = tmp;
                    result = eventVarIndex; // Save current SAT result
                    if (numOfOnes+1>result.size()) foundMaxOnes = true;
                } else
                    foundMaxOnes = true;
                break;
            default:
                if (numOfOnes == -1) {
                    cerr<<"Unable to finish simulation, because the underline SAT engine cannot handle the problem."<<endl;
                    sc_stop();
                } else
                    foundMaxOnes = true;
            } // end switch

            // Free SAT solver ZChaff
            freeSATSolver();

        } // end while

        // Apply SAT result to the scheduling
        flag = applySATResult(result);
        if (debug_flag && debug_level>1) cerr<<endl<<"Total number of scheduled events is "<<countOnes(result)<<endl;

        if (debug_flag && debug_level>1) {
            cerr<<endl<<"after applySATResult"<<endl;
            showSB();
        }

    } // end if

    if (ANNOTATION_ENABLED) {
        topNetlist->postcond((process*)&_mng);
        if (debug_flag && debug_level>2) {
            cerr<<endl<<"after running postcond of quantity managers"<<endl;
            showSB();
        }
    }

    event * passLTL = ((event*) 1); // NULL:  passed; -1: violated; other: need to iterate
    flag = true;
    if (ltl) {
        while ((passLTL!=NULL) && (passLTL!=((event*)-1)) && flag ) {
            flag = set2run();
            passLTL = topNetlist->postCheckLTL(true);
        } // end while
    } else {
        flag = set2run();
    } // end if

    if (debug_flag && debug_level>3) {
        cerr<<endl<<"after set2run";
        if (ltl) cerr<<" and postCheckLTL";
        cerr<<endl;
        showSB();
    }

    return flag;
} // end heuristic3

// Used by the metamodel debugger.
// Uses a std::set to eliminate duplicate strings.
//
// FIXME: Have to free memory allocated here somehow!
//
strList_t scoreboard::getAllNextLines() {
    strList_t* nextLineList = new strList_t;
    std::set<String> lineSet;
    std::set<String>::iterator setIter;

    for (int i=0; i<size; i++) {
        ProgramCounter* pc = pcVector[i];
        for (int j=0; j<pc->nextMMLines.size(); j++) {
            lineSet.insert(pc->nextMMLines[j]);
        }
    }

    for (setIter = lineSet.begin(); setIter != lineSet.end(); setIter++) {
        String* s = new String(*setIter);
        nextLineList->push_back(s);
    }
    return *nextLineList;
}

// Used by the metamodel debugger.
//
strList_t scoreboard::getAllCurrentLines() {
    updateCurrentLines();
    return *currentLineList;
}

// Used by the metamodel debugger.
//
strList_t scoreboard::getAllEventTags() {
    strList_t::iterator tagIter = eventTagList->begin();
    while (tagIter != eventTagList->end()) {
        delete *tagIter;
        tagIter++;
    }
    eventTagList->clear();

    for (int i = 0; i < size; i++) {
        ProgramCounter* pc = pcVector[i];
        String *pTagString = new String(pc->p->name());
        if (pTagString != (String*)0) {
            String tagString = *pTagString;
            if (pc->eventTag != (String *)0) {
                tagString += ":";
                tagString += *pc->eventTag;
                eventTagList->push_back(new String(tagString));
            }
        }
        delete pTagString;
    }
    return *eventTagList;
}


// Used by the metamodel debugger.
//
strList_t scoreboard::getAllSchedStates() {
    strList_t::iterator tagIter = schedStateList->begin();
    while (tagIter != schedStateList->end()) {
        delete *tagIter;
        tagIter++;
    }
    schedStateList->clear();

    for (int i = 0; i < size; i++) {
        ProgramCounter* pc = pcVector[i];
        String *pStateString = new String(pc->p->name());
        if (pStateString != (String*)0) {
            String stateString = *pStateString;
            stateString += ":";
            switch (pc->SchedState) {
            case SchedStateVal::RUN:
                stateString += "RUN";
                break;
            case SchedStateVal::DONTRUN:
                stateString += "DONTRUN";
                break;
            case SchedStateVal::RUNNING:
                stateString += "RUNNING";
                break;
            case SchedStateVal::UNKNOW:
                stateString += "UNKNOWN";
                break;
            case SchedStateVal::EVALUATE:
                stateString += "EVALUATE";
                break;
            case SchedStateVal::FINISHED:
                stateString += "FINISHED";
                break;
            case SchedStateVal::TESTSCHEDULING:
                stateString += "TESTSCHEDULING";
                break;
            default:
                stateString += "???";
                break;
            }
            schedStateList->push_back(new String(stateString));
        }
        delete pStateString;
    }
    return *schedStateList;
}

//
void scoreboard::updateCurrentLines() {
    strList_t::iterator clIter = currentLineList->begin();
    while (clIter != currentLineList->end()) {
        delete *clIter;
        clIter++;
    }
    currentLineList->clear();

    for (int i = 0; i < size; i++) {
        ProgramCounter* pc = pcVector[i];
        String *pCurrentLineString = new String(pc->p->name());
        if (pCurrentLineString != (String*)0) {
            String currentLineString = *pCurrentLineString;
            if (pc->currentLineNumber != (String *)0) {
                currentLineString += ":";
                currentLineString += *pc->currentLineNumber;
                currentLineList->push_back(new String(currentLineString));
            }
            delete pCurrentLineString;
        }
    }
}
