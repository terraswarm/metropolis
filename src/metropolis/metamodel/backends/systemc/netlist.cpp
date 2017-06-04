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
#include "programcounter.h"
#include "process.h"
#include "netlist.h"
#include "schedulingnetlist.h"
#include "quantity.h"
#include "statemediumDeclaration.h"
#include "medium.h"
#include "action.h"
#include "nondeterminism.h"
#include <iostream>

netlist::netlist(sc_module_name name) : netlist_b(name) {
    buchi = NULL;
    ltlEvents = NULL;
    pos = neg = undecided = NULL;
    ltlEventOccur = false;
    num_event = 0;
    type = NormalNetlistType;
}

netlist::~netlist() {
}

void netlist::registerProcess(std::string name, process *p) {
    _p.insert(p_t::value_type(name, p));
} // end registerProcess

void netlist::registerMedium(std::string name, medium *m) {
    _m.insert(m_t::value_type(name, m));
} // end registerMedium

//void netlist::top() {   //changed from top to resolve

void netlist::resolve(process *caller) {
    bool bStable;

    do {
        bStable = true;

        for (eq_n_t::iterator it = _eq_r.begin(); it != _eq_r.end(); it++)
            ((netlist_b*)(*it))->resolve((process*)&_mng);

    } while (!bStable);

    if (!_childrenResolveCollected) {
        _eq_r.clear();

        for (nl_t::iterator it = _nl.begin(); it != _nl.end(); it++) {
            netlist_b * sn = ((netlist_b*)it->second);
            if (sn->type == NormalNetlistType) {
                if (sn->isChildrenCollected(NetlistResolve))
                    addEqualSubNetlists(NetlistResolve, sn->getEqualSubNetlists(NetlistResolve));
                else
                    addEqualSubNetlist(NetlistResolve, sn);
            } else { //sn is SchedulingNetlistType
                if (((SchedulingNetlist*)sn)->getNumQuantity() > 0)
                    addEqualSubNetlist(NetlistResolve, sn);
                else if (sn->isChildrenCollected(NetlistResolve))
                    addEqualSubNetlists(NetlistResolve, sn->getEqualSubNetlists(NetlistResolve));
                else
                    addEqualSubNetlist(NetlistResolve, sn);
            } // end if
        }
        _childrenResolveCollected = true;
    } // end if

} // end resolve

void netlist::postcond(process *caller) {
    for (eq_n_t::iterator it = _eq_p.begin(); it != _eq_p.end(); it++)
        ((netlist_b*)(*it))->postcond((process*)&_mng);

    if (!_childrenPostcondCollected) {
        _eq_p.clear();

        for (nl_t::iterator it = _nl.begin(); it != _nl.end(); it++) {
            netlist_b * sn = ((netlist_b*)it->second);
            if (sn->type == NormalNetlistType) {
                if (sn->isChildrenCollected(NetlistPostcond))
                    addEqualSubNetlists(NetlistPostcond, sn->getEqualSubNetlists(NetlistPostcond));
                else
                    addEqualSubNetlist(NetlistPostcond, sn);
            } else { //sn is SchedulingNetlistType
                if (((SchedulingNetlist*)sn)->getNumQuantity() > 0)
                    addEqualSubNetlist(NetlistPostcond, sn);
                else if (((SchedulingNetlist*)sn)->getNumStatemedium() > 0)
                    addEqualSubNetlist(NetlistPostcond, sn);
                else if (sn->isChildrenCollected(NetlistPostcond))
                    addEqualSubNetlists(NetlistPostcond, sn->getEqualSubNetlists(NetlistPostcond));
                else
                    addEqualSubNetlist(NetlistPostcond, sn);
            } // end if
        }
        _childrenPostcondCollected = true;
    } // end if

} // end postcond

bool netlist::resolveSynch() {
    for (nl_t::iterator it = _nl.begin(); it != _nl.end(); it++) {
        netlist * subnet = (netlist*)it->second;
        if (subnet->type == NormalNetlistType) subnet->resolveSynch();
    }

    synch(1);

    return true;
} // end resolveSynch

process *netlist::getProcess(std::string name) {
    p_t::iterator it = _p.find(name);
    if (it == _p.end()) return NULL;
    return it->second;
} // end getProcess

medium *netlist::getMedium(std::string name) {
    m_t::iterator it = _m.find(name);
    if (it == _m.end()) return NULL;
    return it->second;
} // end getMedium

sc_object *netlist::getcomponent(netlist *nl, std::string name) {
    sc_object *pResult = NULL;
    if (nl == this) {
        if ((pResult = getNetlist(name)))
            return pResult;
        else if ((pResult = getProcess(name)))
            return pResult;
        else if ((pResult = getMedium(name)))
            return pResult;
    } else {
        for (nl_t::iterator it = _nl.begin(); it != _nl.end(); it++) {
            pResult = ((netlist*)it->second)->getcomponent(nl, name);
            if (pResult) return pResult;
        } // end for
    } // end if

    return NULL;
} // end getcomponent

void netlist::registerLTLEvent(event * e, std::string name) {
    //static int eventID = 1;
    //char id[10];
    events_in_ltl_t::iterator it = _ltl_events.find(e);
    if (it == _ltl_events.end()) {
        //        std::string s;
        //        sprintf(id, "e%d", eventID++);
        //        s = id;
        _ltl_events.insert(events_in_ltl_t::value_type(e, name));
    } // enf if
} //end registerLTLEvent

std::string netlist::getLTLEventDummyName(event * e) {
    events_in_ltl_t::iterator it = _ltl_events.find(e);
    if (it == _ltl_events.end())
        return NULL;
    else
        return it->second;
} //end getLTLEventDummyName

void netlist::appendLTLFormula(std::string formula) {
    _ltl_formula += formula;
} //end appendLTLFormula

void netlist::build_buchi() {
    if (_ltl_events.size() == 0) return;

    strcpy(uform, _ltl_formula.c_str());
    hasuform = strlen(uform);
    tl_out = stdout;
    tl_parse();

    buchi = bstates;
    // MSVC has a slightly different syntax for the new operator
    // for details, go to msdn and look for the C++ Language Reference
    // http://msdn.microsoft.com/library/default.asp?url=/library/en-us/vclang/html/_pluslang_new_operator.asp
    //ltlEvents = new (event *)[sym_id];
    ltlEvents = new (event *[sym_id]);
    num_event = sym_id;
    pos = new int[SEG(num_event-1)+1];
    neg = new int[SEG(num_event-1)+1];
    undecided = new int[SEG(num_event-1)+1];
    //GY: old buggy one? undecided = new int[(int)((num_event-1)/sizeof(int))];

    annotateDist2FS(buchi);

    for (int i=0; i<num_event; i++) {
        for (events_in_ltl_t::iterator it = _ltl_events.begin();
             it != _ltl_events.end(); it++) {
            if (it->second == sym_table[i]) {
                ltlEvents[i] = it->first;
                break;
            } // end if
        } // end for
    } // end for
    if (debug_flag) {
        cerr<<"LTL formula="<<uform<<endl;
        for (int i=0; i<num_event; i++) {
            cerr<<sym_table[i]<<" <=> ";
            ltlEvents[i]->show();
            cerr<<endl;
        } // end for
        outputBuchi(buchi);
    }

    getInitStates(buchi, activeStatesInBuchi);
    getFinalStates(buchi, finalStatesInBuchi);
} // end build_buchi

bool netlist::checkOccuredLTLEvents() {
    ltlEventOccur = false;

    for (int i=0; i<num_event; i++)
        if (ltlEvents[i]->inPendingList) {
            ltlEventOccur = true;
            if (debug_flag) {
                cerr<<"LTL event "<<sym_table[i]<<" ";
                ltlEvents[i]->show();
                cerr<<endl;
            } else {
                break;
            }
        }

    return ltlEventOccur;
} // end checkOccuredLTLEvents

bool netlist::collectCurrentLTLEventsStatus() {
    if (!ltlEventOccur) return false;

    for (int i = SEG(num_event-1) ;i>=0; i--) pos[i]=neg[i]=undecided[i]=0;
    for (int i=0; i<num_event; i++) {
        ProgramCounter * pc = ltlEvents[i]->getProcess()->pc;
        //eventMap_t::iterator it = pc->_pendingEvents.find(ltlEvents[i]);
        //if (it == pc->_pendingEvents.end())
        if (! ltlEvents[i]->inPendingList)
            neg[SEG(i)] |= (1<<BIT(i));
        else {
            switch (pc->SchedState) {
            case  SchedStateVal::RUN:
                //if (pc->selected == it->second)
                    pos[SEG(i)] |= (1<<BIT(i));
                //else
                    //neg[SEG(i)] |= (1<<BIT(i));
                break;
            case  SchedStateVal::DONTRUN:
                neg[SEG(i)] |= (1<<BIT(i));
                break;
            case  SchedStateVal::UNKNOW:
            default:
                undecided[SEG(i)] |= (1<<BIT(i));
            } // end switch
        } // end if

    } // end for

/*    bool no_one = true;
    for (int i = SEG(num_event-1) ;i>=0 && no_one; i--)
        no_one = (undecided[i]==0 && pos[i]==0);

    return (!no_one);*/
    return true;
} // end collectCurrentEventsStatus

bool netlist::preGuideLTL() {
    bool ltlHasImpact = false;

    //for (nl_t::iterator it = _nl.begin(); it != _nl.end(); it++)
    //ltlHasImpact |= ((netlist*)it->second)->preGuideLTL();

    if (buchi == NULL) return ltlHasImpact;

    if (!collectCurrentLTLEventsStatus()) return ltlHasImpact;


    BTrans_set_t   tr;
    transToPost(buchi, activeStatesInBuchi, pos, neg, false, tr);

    int * posProd = new int[SEG(num_event-1)+1];
    int * negProd = new int[SEG(num_event-1)+1];
    for (int i = SEG(num_event-1) ;i>=0; i--) posProd[i]=negProd[i]=-1; // 0xFFFFFFFF
    for (BTrans_set_t::iterator it=tr.begin(); it!=tr.end(); it++) {
        for (int i = SEG(num_event-1) ;i>=0; i--) {
            posProd[i] &= (*it)->pos[i];
            negProd[i] &= (*it)->neg[i];
        }
    } // end for

    bool imply = false;
    for (int i = 0; i<num_event; i++) {
        if (posProd[SEG(i)] & (1<<BIT(i))) {
            imply = true;
            ltlEvents[i]->setMustDo();
        }
        if (negProd[SEG(i)] & (1<<BIT(i))) {
            imply = true;
            ltlEvents[i]->setMustNotDo();
        }
    } // end for

    delete posProd;
    delete negProd;

    return (ltlHasImpact | imply);
} // end preGuideLTL

event*  netlist::postCheckLTL(bool decided) {
    event * sat = NULL;
    int *posEvent;
    //for (nl_t::iterator it = _nl.begin(); it != _nl.end() && (!sat); it++)
    //sat = ((netlist*)it->second)->postCheckLTL(decided);
    if (sat) {
        //cerr<<"LTL constraints violated."<<endl;
        return sat;
    } // end if

    if (buchi == NULL) return NULL;

    if (!collectCurrentLTLEventsStatus()) return NULL;

    for(int i=0; i<num_event; i++) {
        if (undecided[SEG(i)] & (1<<BIT(i))) {
            if (ltlEvents[i]->getProcess()->pc->funcType & FunctionType::ANNOTATION) {
                undecided[SEG(i)] ^= (1<<BIT(i));       //set to 0
                if (!testSchedFlag && allProcStuck)
                    pos[SEG(i)] |= (1<<BIT(i));         //set to 1
                else
                    neg[SEG(i)] |= (1<<BIT(i));         //set to 1
            }
        }
    }

    BState_set_t   post;
    //posEvent = decided ? pos : undecided;
    posEvent = pos;
    conditionalPost(buchi, activeStatesInBuchi, posEvent, neg, false, post);
    if (post.size() == 0) { // LTL constraints violated
        //TODO: is it possible to disable some of the events with QA?
        return ((event*) -1);
    }
    if (debug_flag) {
        cerr<<"Post states:";
        outputBuchiStates(post);
        cerr<<endl;
    }
    //LTL constraints not violated, choose the best set of next states
//     if (decided) {
//         activeStatesInBuchi.clear();
//         activeStatesInBuchi = post;
//         if (debug_flag) cerr<<"# of active states in Buchi"
//             <<activeStatesInBuchi.size()<<endl;
//     }

//     bool anyPos = false;
//     for (int i = SEG(num_event-1) ;i>=0 && !anyPos; i--) anyPos = (posEvent[i] != 0);
//     if (!anyPos) {
//         cerr<<"LTL constraints violated."<<endl;
//         return ((event*)-1);
//     } // end if

    int minstep = INT_MAX;
    BState_set_t best;
    bool toFinal = false;

    //Min Step heuristic. Find the best next states in Buchi
/*    for (BState_set_t::iterator sit = post.begin(); sit != post.end(); sit++) {
        if (toFinal) {
            if ((*sit)->final & FINAL_STATE) best.insert(*sit);
        } else {
            if ((*sit)->final & FINAL_STATE) {
                best.clear();
                toFinal = true;
                best.insert(*sit);
            } else if ((*sit)->dist < minstep) {
                best.clear();
                minstep = (*sit)->dist;
                best.insert(*sit);
            } else if ((*sit)->dist == minstep) {
                best.insert(*sit);
            }
        }
    }
*/
    best = post;

    BTrans * maxPosTr = NULL;
    BTrans_set_t bestTr;
    int num1;
    int min1 = INT_MAX;
    //max events heuristic. Find the transitions with the max number of runnable events
    for (BState_set_t::iterator sit = activeStatesInBuchi.begin();
        sit != activeStatesInBuchi.end(); sit++) {
        for (BTrans *t=(*sit)->trans->nxt; t!=(*sit)->trans; t = t->nxt) {
            if (best.find(t->to) != best.end()) {
                bool trEnabled = true;
                for (int i=0; i<=SEG(num_event-1) && trEnabled; i++)
                    if (((~neg[i]) & t->pos[i]) != t->pos[i] ||
                        ((~pos[i]) & t->neg[i]) != t->neg[i])
                        trEnabled = false;
                if (!trEnabled) continue;
                num1 = 0;
                for (int i=0; i<num_event; i++)
                    if (t->neg[SEG(i)] & undecided[SEG(i)] & (1<<BIT(i)))
                        num1 ++;
                if (num1 < min1) {
                    min1 = num1;
                    bestTr.clear();
                    bestTr.insert(t);
                } else if (num1 == min1) {
                    bestTr.insert(t);
                }
            }
        } // end for
    }// end for

    //From the transitions with the max number of runnable events,
    //choose the destination states with the min step to accepting states
    //Remember the first such states, set pos, neg events accordingly
    min1 = INT_MAX;
    for(BTrans_set_t::iterator tit=bestTr.begin(); tit!=bestTr.end(); tit++) {
        if ((*tit)->to->dist < min1) {
            min1 = (*tit)->to->dist;
            maxPosTr = *tit;
        }
    }
    for(int i=0; i<num_event; i++) {
        if (undecided[SEG(i)] & (1<<BIT(i))) {
            if (maxPosTr->neg[SEG(i)] & (1<<BIT(i))) {
                neg[SEG(i)] |= (1<<BIT(i));
                ltlEvents[i]->setMustNotDo();
            } else {
                pos[SEG(i)] |= (1<<BIT(i));
            }
        }
    }

    post.clear();
    conditionalPost(buchi, activeStatesInBuchi, pos, neg, true, post);
    if(post.size() == 0) {
        cerr<<"LTL constraints violated. Exit!"<<endl;
        sc_stop();
    }
    if (debug_flag) {
        cerr<<activeStatesInBuchi.size()<<" active states(";
        outputBuchiStates(activeStatesInBuchi);
        cerr<<") in BA, moving to ";
        cerr<<post.size()<<" next states(";
        outputBuchiStates(post);
        cerr<<")"<<endl;
        for(int i=0; i<num_event; i++) {
            if (pos[SEG(i)] & (1<<BIT(i)))
                cerr<<" ";
            else
                cerr<<"!";
            cerr<<_ltl_events[ltlEvents[i]];
            cerr<<" ";
            ltlEvents[i]->show();
            cerr<<endl;
        }
    }
    activeStatesInBuchi = post;

/*
    BTrans_set_t  tr;
    int * none_pos = new int[SEG(num_event-1)+1];
    for (int i=0; i<=SEG(num_event-1); i++) none_pos[i]=0;
    transToPost(buchi, activeStatesInBuchi, none_pos, neg, tr);
    delete none_pos;
    int max1=0;
    for (BTrans_set_t::iterator it=tr.begin(); it!=tr.end(); it++) {
        num1 = 0;
        for (int i=0; i<num_event; i++)
            if ( (posEvent[SEG(i)] & (1<<BIT(i))) != 0  &&
                 ((*it)->neg[SEG(i)] & (1<<BIT(i))) == 0 )  num1++;
        if (num1>max1) {
            max1 = num1;
            maxPosTr = (*it);
        } else if (num1==max1 && num1>0) {
            if (maxPosTr==NULL) maxPosTr = (*it);
            else if (((*it)->to->final & FINAL_STATE) != 0) {
                if ((maxPosTr->to->final & FINAL_STATE) == 0) maxPosTr = (*it);
                else {
                    if (random() & 0x1) maxPosTr = (*it);
                } // end if
            } else if ((maxPosTr->to->final & FINAL_STATE) == 0) {
                if ((*it)->to->dist  < maxPosTr->to->dist ) maxPosTr = (*it);
                else if ((*it)->to->dist == maxPosTr->to->dist ) {
                    if (random() & 0x1) maxPosTr = (*it);
                } // end if
            } // end if
        } // end if
    } // end for

    if (maxPosTr == NULL) return ((event*) -1);
    event * synchE;
    EventMap * em;
    for (int i=0; i<num_event; i++) {
        if ( (posEvent[SEG(i)] & (1<<BIT(i))) != 0 &&
             (maxPosTr->neg[SEG(i)] & (1<<BIT(i))) != 0 )  {
            ltlEvents[i]->setMustNotDo();
            em = _sb.getEventSynchMap(ltlEvents[i], true);
            synchE = (em == NULL) ? NULL : (em->_e);
            if (synchE) synchE->setMustNotDo();
            return ltlEvents[i];
        } // end if
    } // end for
*/

    // Disable one event
    //    event * synchE;
    //    EventMap * em;
    //    for (int disableSynch = 0; disableSynch<=1; disableSynch++) {
    //       for (int i = 0; i<num_event; i++) {
    //         if (pos[SEG(i)] & (1<<BIT(i))) {
    //           em =_sb.getEventSynchMap(ltlEvents[i], true);
    //           synchE = (em == NULL) ? NULL : (em->_e);
    //           if (synchE) {
    //             if (!disableSynch) continue;
    //             else {
    //               ltlEvents[i]->getProcess()->pc->setMustNotDo(ltlEvents[i]);
    //               synchE->getProcess()->pc->setMustNotDo(synchE);
    //               return false;
    //             } // end if (!disableSynch)
    //           } else {
    //             ltlEvents[i]->getProcess()->pc->setMustNotDo(ltlEvents[i]);
    //             return false;
    //           } // end if
    //         } // end if
    //       } // end for i
    //    } // end for disableSynch


    // Randomly disable one event
    //    int * indx = intArrayGen(num_event, true);
    //    for (int disableSynch = 0; disableSynch<=1; disableSynch++) {
    //       for (int i = 0; i<num_event; i++) {
    //         if (pos[SEG(indx[i])] & (1<<BIT(indx[i]))) {
    //           em =_sb.getEventSynchMap(ltlEvents[indx[i]], true);
    //           synchE = (em == NULL) ? NULL : (em->_e);
    //           if (synchE) {
    //             if (!disableSynch) continue;
    //             else {
    //               ltlEvents[indx[i]]->getProcess()->pc->setMustNotDo(ltlEvents[indx[i]]);
    //               synchE->getProcess()->pc->setMustNotDo(synchE);
    //               delete indx;
    //               return false;
    //             } // end if (!disableSynch)
    //           } else {
    //             ltlEvents[indx[i]]->getProcess()->pc->setMustNotDo(ltlEvents[indx[i]]);
    //             delete indx;
    //             return false;
    //           } // end if
    //         } // end if
    //       } // end for i
    //    } // end for disableSynch
    //    delete indx;

    return ((event*) NULL);    // should never get here
} //end postCheckLTL


void netlist::addLTLConstraintsToSAT(SAT_Manager SatMgr, eventVarIndexMap_t& evi) {

} // end addLTLConstraintsToSAT
