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

#define BEGIN_AWAIT_STATEMENT(condNum) \
        simpleList_t * list; \
        _2DList_t * tslist; \
        bool flag = true; \
        bool * mask = new bool[condNum]; \
        memset(mask, 1, condNum * sizeof(bool)); \
        bool * condition = new bool[condNum]; \
        pc->initAwait(condNum, NONDET);


#define ASK_EVALUATE \
        if (pc->mode == RunType::EXEC) { \
                pc->setState(this, intfcName, Keep_Unchanged_String, SchedStateVal::EVALUATE); \
                notify(DeltaCycle, _mng.pc->invoke); \
                wait(pc->invoke); \
        } \
  int _previousExecMode = pc->mode; \
  pc->mode = RunType::TRY;

#define ASK_MANAGER \
     pc->mode = _previousExecMode; \
     pc->setState(this, intfcName, Keep_Unchanged_String, \
       (pc->mode == RunType::EXEC)?SchedStateVal::UNKNOW : SchedStateVal::TESTSCHEDULING); \
     pc->setPotentialUsedMedia(); \
     if ((pc->funcType & FunctionType::ANNOTATION)!=0 || !interleaving) { \
       notify(DeltaCycle, _mng.pc->invoke); \
       wait(pc->invoke); \
     } else { \
                                pc->savePendingEvents(); \
                                _sb.check(pc); \
                                if (pc->SchedState == SchedStateVal::DONTRUN) { \
                                        pc->loadPendingEvents(); \
                                        pc->SchedState = SchedStateVal::UNKNOW; \
                                        notify(DeltaCycle, _mng.pc->invoke); \
                                        wait(pc->invoke); \
                                } \
     } \
     pc->resetPotentialUsedMedia();

#define BEGIN_PREVENT_LIST \
  simpleList_t *_pSetList = (*tslist)[pc->selected];\
        pc->preventList.insert(pc->preventList.begin(), _pSetList);\
  for (int _sli = 0; _sli < _pSetList->size(); _sli++) {\
    node * n = (*_pSetList)[_sli];\
    ((medium*)n->object)->addPreventedIntfc(n->intfcName, pc);\
  }

#define END_PREVENT_LIST \
  _pSetList = pc->preventList[0];\
  for (int _sli = 0; _sli < _pSetList->size(); _sli++) {\
    node * n = (*_pSetList)[_sli];\
    ((medium*)n->object)->releasePreventedIntfc(n->intfcName, pc);\
  }\
  pc->preventList.erase(pc->preventList.begin());

#define END_AWAIT_STATEMENT \
        tslist = pc->setList.back(); \
        while (!tslist->empty()) { \
                list = tslist->front(); \
                while (!list->empty()) { \
                        delete list->front(); \
                        list->erase(list->begin()); \
                } \
                delete list; \
                tslist->erase(tslist->begin()); \
        } \
        delete tslist; \
        pc->setList.pop_back(); \
        tslist = pc->testList.back(); \
        while (!tslist->empty()) { \
                list = tslist->front(); \
                while (!list->empty()) { \
                        delete list->front(); \
                        list->erase(list->begin()); \
                } \
                delete list; \
                tslist->erase(tslist->begin()); \
        } \
        delete tslist; \
        pc->testList.pop_back(); \
        delete[] mask; \
        delete[] condition; \
        pc->restoreState(); \
        if (stuck_flag) throw STUCK();

#define sc_stop sc_sim_stop
#define sc_start sc_sim_start
#define updateSynchEventInfo(eventGroupID, pc) \
        _sb.updateSynchEventInfo(eventGroupID, pc)

////////////////////////////////////////////////////////////
//    New macros very slightly overlap with old ones.     //
//    The following macros actually show the code         //
//    sequence for labeled statement, labeled block,      //
//    interface function call with annotation.            //
//    The commented code will be generated by backend.    //
////////////////////////////////////////////////////////////

#define INIT(StmtType) \
        bool stuck_flag = false, return_flag = false, reSchedule = false;\
  \
        pc->saveState();\
        pc->funcType = FunctionType::StmtType;  //StmtType=LABEL INTFCFUNC AWAIT

//*        if (ANNOTATION_ENABLED) pc->funcType |= FunctionType::ANNOTATION;

#define ADD_PENDING_EVENT(ActionState, Name) \
        do {\
                pc->addPendingEvent(ActionState(pc->p, this, Name), 0); //ActionState=beg end

//could exist 0 or more SETUP_STATE
#define SETUP_STATE(IName) \
                pc->setState(this, IName, Keep_Unchanged_String, \
                    (pc->mode == RunType::EXEC)?SchedStateVal::UNKNOW : SchedStateVal::TESTSCHEDULING);\


//*                if (ANNOTATION_ENABLED) {
//*                        beg_statement;
//*                }

#define RESOLVE_EXEC \
    if (pc->funcType & FunctionType::ANNOTATION) {\
                        notify(DeltaCycle, _mng.pc->invoke);\
                        wait(pc->invoke);\
                        reSchedule = (pc->mode == RunType::EXEC) && (pc->SchedState != SchedStateVal::RUN);\
                } else if (!interleaving || pc->_inSynch) {\
                        notify(DeltaCycle, _mng.pc->invoke);\
                        wait(pc->invoke);\
                        reSchedule = (pc->SchedState!=SchedStateVal::RUN) && pc->_inSynch;\
                } else {\
                        pc->savePendingEvents();\
                        _sb.check(pc);\
                        if (pc->SchedState == SchedStateVal::DONTRUN) {\
                                pc->loadPendingEvents();\
                                pc->SchedState = (pc->mode == RunType::EXEC)?\
                                                SchedStateVal::UNKNOW : SchedStateVal::TESTSCHEDULING;\
                                notify(DeltaCycle, _mng.pc->invoke);\
                                wait(pc->invoke);\
                        }\
                        reSchedule = false;\
                }\
        } while (reSchedule); \
        pc->_inSynch = false; \
        pc->freeAllPendingEvents();

//                if (pc->SchedState == SchedStateVal::RUN) {
//                        pc->SchedState = SchedStateVal::RUNNING;
//      ((medium*)pc->obj.object)->addUsedIntfc(pc->obj.intfcName, pc);
//                        try {
//                                statement;
//                        } catch (STUCK) { stuck_flag = true; }
//                        catch (RETURN) { return_flag = true; }
//                }
//                else stuck_flag = true;

//if (!stuck_flag) {

//ADD_PENDING_EVENT(end, "NAME")

//SETUP_STATE("INAME")

//*                if (ANNOTATION_ENABLED) {
//*                        end_statement;
//*                }

//RESOLVE_EXEC

//}
//                if (pc->SchedState == SchedStateVal::RUN) {
//                        pc->SchedState = SchedStateVal::RUNNING;
//      ((medium*)pc->obj.object)->releaseUsedIntfc(pc->obj.intfcName, pc);
//                }
//                else stuck_flag = true;

#define FINISH(StmtType) \
        pc->restoreState(); \
        if (stuck_flag) throw STUCK();

