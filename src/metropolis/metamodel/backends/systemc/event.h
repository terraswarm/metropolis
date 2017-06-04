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
#ifndef MMSCS_EVENT_H
#define MMSCS_EVENT_H

#include "global.h"
#include "process.h"
#include "action.h"
#include <vector>
#include <map>
#define HISTORYLENGTH 4

class statemedium;
class RequestClass;
class quantity;

class event {
    public:

event(process *p, action *a, ActionState as);
    event() {}
    ~event();

    bool inPendingList;

    private:
    process * _process;
    statemedium * _statemedium;
    action * _action;
    ActionState _as;

    int _refCount;
    int _implCount;

    typedef std::vector<RequestClass *> vecRequestClass_t;
    typedef std::map<quantity *, vecRequestClass_t *> mapQuantities_t;

    mapQuantities_t _mapQuantities;

    public:
    process *getProcess() { return _process; }
    void setProcess(process * p) { _process = p; }
    statemedium *getStatemedium() { return _statemedium; }
    void setStatemedium(statemedium * p) { _statemedium = p; }
    action *getAction() { return _action; }
    void setAction(action * a) { _action = a; }
    ActionState getActionState() { return _as; }
    void setActionState(ActionState as) { _as = as; }
    int getRefCount() { return _refCount; }
    void setRefCount(int rct) { _refCount = rct; }
    int getImplCount() { return _implCount; }
    void setImplCount(int imp) { _implCount  = imp; }
    void promoteImplCount(int imp) { 
        if (imp>_implCount) _implCount  = imp; }
    void addQuantityRequestClass(quantity *q, RequestClass *rc);
    RequestClass *getQuantityRequestClass(quantity *q, int index);
    bool isEnabled();
    bool isInNamedEvent(int type);
    bool setMustDo();
    bool setMustNotDo();
    bool equals(event *e);
    void show();
};

event *beg(process *p, sc_object *obj, const char *name);
event *end(process *p, sc_object *obj, const char *name);
event *getEvent(process *p, sc_object *obj, const char *name, ActionState as);

#endif
