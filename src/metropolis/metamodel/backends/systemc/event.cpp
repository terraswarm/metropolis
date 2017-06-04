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
#include "event.h"
#include "process.h"
#include "statemediumDeclaration.h"
#include "action.h"
#include "requestclass.h"
#include "quantity.h"
#include "programcounter.h"

event::event(process *p, action *a, ActionState as) {
    _process = p;
    _action = a;
    _as = as;
    _refCount = 0;
    _implCount = -1;
}

event::~event() {
}

void event::addQuantityRequestClass(quantity *q, RequestClass *rc) {
    mapQuantities_t::iterator it = _mapQuantities.find(q);
    vecRequestClass_t *pRc;
    if (it == _mapQuantities.end()) {
        pRc = new vecRequestClass_t;
        _mapQuantities.insert(mapQuantities_t::value_type(q, pRc));
    } else
        pRc = it->second;

    if (pRc->size()> HISTORYLENGTH) {
        RequestClass *first = pRc->front();
        pRc->erase(pRc->begin());
        delete first;
    }
    pRc->push_back(rc);
}  // end addQuantityRequestClass

RequestClass *event::getQuantityRequestClass(quantity *q, int index) {
    if (index <= -2) return NULL;

    mapQuantities_t::iterator it = _mapQuantities.find(q);
    if (it == _mapQuantities.end()) return NULL;
    RequestClass *rc = NULL;
    vecRequestClass_t *pRc = it->second;
    if (index == -1) return pRc->back(); //LAST
    else return (*pRc)[index];
} // end getQuantityRequestClass

bool event::setMustDo() {
    ProgramCounter *pc = _process->pc;
    return pc->setMustDo(this);
} // end setMustDo

bool event::setMustNotDo() {
    ProgramCounter *pc = _process->pc;
    return pc->setMustNotDo(this);
} // end setMustNotDo

bool event::isEnabled() {
    return inPendingList;
    //ProgramCounter *pc = _process->pc;
    /*
      for (eventMap_t::iterator it=pc->_pendingEvents.begin();
      it != pc->_pendingEvents.end(); it++) {
      if (equals(it->first)) return true;
      }
    */
    //eventMap_t::iterator it = pc->_pendingEvents.find(this);
    //if (it != pc->_pendingEvents.end()) return true;
    //return false;
} // end isEnabled

bool event::isInNamedEvent(int type) {
    return (_process->pc->funcType & type);
} // end isInNamedEvent

bool event::equals(event *e) {
    if (_process == e->getProcess() &&
            ((*_action) == (*(e->getAction()))) &&
            _as == e->getActionState()) {
        return true;
    }
    else return false;
} // end equals

void event::show() {
    switch(_as) {
    case ACTION_STATE_BEGIN:
        cerr<<"beg";
        break;
    case ACTION_STATE_END:
        cerr<<"end";
        break;
    case ACTION_STATE_NONE:
        cerr<<"none";
        break;
    case ACTION_STATE_OTHER:
        cerr<<"other";
        break;
    default:
        cerr<<"wrong_event_type";
    }
    cerr<<"("<<_process->name()<<", ";
    _action->show();
    cerr<<")";
} // end show

event *beg(process *p, sc_object *obj, const char *name) {
    return getEvent(p, obj, name, ACTION_STATE_BEGIN);
} // end beg

event *end(process *p, sc_object *obj, const char *name) {
    return getEvent(p, obj, name, ACTION_STATE_END);
} // end end

event *getEvent(process *p, sc_object *obj, const char *name, ActionState as) {
    event *pEvent = NULL;
    for (eventList_t::iterator it = p->pc->_eventList.begin();
         it != p->pc->_eventList.end(); it++) {
        pEvent = *it;
        if (pEvent->getAction()->getObject() == obj &&
                strcmp(pEvent->getAction()->getName().c_str(), name) == 0 &&
                pEvent->getActionState() == as)
            return pEvent;
    } // end for

    pEvent = new event(p, new action(obj, name), as);
    p->pc->_eventList.push_back(pEvent);

    return pEvent;
} // end getEvent
