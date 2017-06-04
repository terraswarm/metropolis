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
#ifndef MMSCS_GLOBALTIME_H
#define MMSCS_GLOBALTIME_H

#include "global.h"

#include "quantity.h"
#include "requestclass.h"
#include "event.h"
#include "globaltimemanager.h"

#include <vector>

#define later(x) ((x) > _gtime ? (x) : _gtime)
#define earlier(x) (_gtimeRequested > (x) ? (x) : _gtimeRequested)
#define RANDOMSCALE 3
#define DOUBLE_ERROR_TOLERANCE 1E-10

class GlobalTime : public quantity, public GlobalTimeManager {
    public:
    GlobalTime(sc_module_name name);
    GlobalTime(sc_module_name name, DUMMY_CTOR_ARG _arg, int* index);

    virtual ~GlobalTime();

    String *_name;

    double _gtime;

    private:
    double _gtimeRequested;
    bool _stable;
    bool _resolved;
    bool _otherQuantStuck;
    bool _timeQuantStuck;


    class request_t {
        public:
        request_t(event *e, GlobalTimeRequestClass *rc) { 
            _e = e; _rc = rc; 
        }
        event *_e;
        GlobalTimeRequestClass *_rc;
    };
    
    class biloc_request_t {
        public:
        biloc_request_t(int type, event *e1, event *e2, GlobalTimeRequestClass *rc) {
            _type = type;
            _e1 = e1;
            _e2 = e2;
            _rc = rc;
        }
        int _type;
        event *_e1;
        event *_e2;
        GlobalTimeRequestClass *_rc;
    };


    std::vector<request_t *> _requests;
    std::vector<biloc_request_t *> _builtInLOCRequests;
    std::set<ProgramCounter *> _timedPCs;
    std::set<ProgramCounter *> _requestingPCs;

    public:
    void request(process *caller, event *e, RequestClass *rc);
    void requestI(process *caller, event *e, GlobalTimeRequestClass *rc);
    void resolve(process *caller);
    bool stable(process *caller);
    void postcond(process *caller);
    void postpostcond();
    bool isResolved() { return _resolved; }
    //        void request(event *e, RequestClass *rc);
    //        void resolve();
    //        bool stable();
    //        void postcond();

    //        double sub(double t1, double t2) {
    double sub(process *caller, double t1, double t2) {
        return t1 - t2;
    }

    //  bool equal(double t1, double t2) {
    bool equal(process *caller, double t1, double t2) {
        return t1 == t2;
    }

    //        bool less(double t1, double t2) {
    bool less(process *caller, double t1, double t2) {
        return t1 < t2;
    }

    double A(process *caller, event *e, int i);
    //        double A(event *e, int i);

    double getCurrentTime(process *caller) {
        return _gtime;
    }

    quantity* getQuantity(process* caler, int id) {
        return this;
    }

    void registerLOC(process* caler, int type, event* e1, event* e2, RequestClass *r);

    void unregisterLOC(process* caler, process *p);

    void registerTimedProcess(process* proc);

    private:
    int requestIndex(event *e);
};

#endif

