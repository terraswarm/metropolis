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
#include "statemediumDeclaration.h"
#include "process.h"
#include "programcounter.h"

void statemedium::registerObj(sc_object * o, bool bProcess) {
    connectedObj = o;
    _bProcess = bProcess;
    _bStable = true;
}

process *statemedium::getProcess(process *caller) {
    return _bProcess ? (process *)connectedObj : NULL;
}

SchedProgramCounter *statemedium::getProgramCounter(process *caller) {
    return _bProcess ? ((process*)connectedObj)->pc : NULL;
}

int statemedium::getSchedState(process *caller) {
    return _bProcess ? ((process*)connectedObj)->pc->SchedState : NULL;
}

bool statemedium::setSchedState(process *caller, int newState) {
    if (!_bProcess) return false;

    ProgramCounter * pc = ((process*)connectedObj)->pc;
    if (pc->SchedState != SchedStateVal::UNKNOW)
        return false;
    else {
        pc->SchedState = newState;
        return true;
    }
}

//event **statemedium::getCanDo() {
event **statemedium::getCanDo(process *caller) {
    ProgramCounter *pc = ((process*)connectedObj)->pc;
    event **result = new event *[pc->_pendingEvents.size()];
    int i = 0;
    for (eventMap_t::iterator it = pc->_pendingEvents.begin(); it != pc->_pendingEvents.end(); it++, i++)
        result[i] = it->first;

    return result;
} // end getCanDo

/* The following three functions evaluate an ordered set _pendingEvents.
   An event 'e' is in _pendingEvents at the state when the functions are
   called if and only if 'e' is an event of the process connected to this
   statemedium, 'e' is not the NOP event, and 'e' is currently enabled at the
   state.
*/
/* This function returns the cardinality of _pendingEvents */
int statemedium::getNumEnabledEvents(process *caller) {
    if (_bProcess) return ((process *)connectedObj)->pc->_pendingEvents.size();
    else {
        cerr<<"getNumEnabledEvents called for statemedium unconnected to a process."<<endl;
        sc_stop();
        return -1;
    }
}

/* This function returns the i-th element of _pendingEvents. */
event *statemedium::getEnabledEvent(process *caller, int idx) {
    int i=0;
    event *e = NULL;

    if (!_bProcess) {
        cerr<<"getEnabledEvent called for statemedium unconnected to a process."<<endl;
        sc_stop();
        return e;
    }

    ProgramCounter *pc = ((process *)connectedObj)->pc;
    for (eventMap_t::iterator it=pc->_pendingEvents.begin();
         it != pc->_pendingEvents.end(); it++, i++) {
        if (i == idx) return it->first;
    }
}

/* This function returns true if and only if an event 'e' is in
   _pendingEvents.
*/
bool statemedium::isEventEnabled(process *caller, event *e) {
    if ((process *)connectedObj != e->getProcess()) return false;

    return e->isEnabled();
}

/* This function first checks if _pendingEvents has only one event 'e' and
   if the NOP event has been disabled.  If this is the case, it returns
   'e'. Otherwise, it returns null.
*/
//NOP is not handled correctly.
event *statemedium::getMustDo(process *caller) {
    if (!_bProcess) {
        cerr<<"getNumEnabledEvents called for statemedium unconnected to a process."<<endl;
        sc_stop();
    }

    ProgramCounter *pc = ((process *)connectedObj)->pc;
    /*
      See the comment in registerObj() for _NOPenabled flag.
      if ((pc->_pendingEvents.size() == 1) && (_NOPenabled == false)) {
    */
    if (pc->_pendingEvents.size() == 1) {
        return (pc->_pendingEvents.begin())->first;
    }
    else return NULL;
}

//void statemedium::setMustDo(event *e) {
bool statemedium::setMustDo(process *caller, event *e) {

    if (!_bProcess) {
        cerr<<"setMustDo called for statemedium unconnected to a process."<<endl;
        sc_stop();
        return false;
    }

    ProgramCounter *pc = ((process *)connectedObj)->pc;
    return pc->setMustDo(e);

    // Disable the NOP event.
    // _NOPenabled = false;  See the comment at registerObj() of this class.

} // end setMustDo

//void statemedium::setMustNotDo(event *e) {
bool statemedium::setMustNotDo(process *caller, event *e) {

    if (!_bProcess) {
        cerr<<"setMustNotDo called for statemedium unconnected to a process."<<endl;
        sc_stop();
        return false;
    }

    ProgramCounter *pc = ((process *)connectedObj)->pc;
    return pc->setMustNotDo(e);
} // end setMustNotDo

//void statemedium::resolve() {
void statemedium::resolve(process *caller) {
} // end resolve

//bool statemedium::stable() {
bool statemedium::stable(process *caller) {
    return _bStable;
} // end stable


//void statemedium::postcond() {
void statemedium::postcond(process *caller) {
    _bStable = true;
} // end postcond
