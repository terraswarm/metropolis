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
#include "GlobalTime.h"
#include "process.h"
#include "programcounter.h"
#include <math.h>  // Pick up decl for fabs() under Solaris/gcc3.4.2

GlobalTime::GlobalTime(sc_module_name name) : quantity(name)
{
    _gtime = 0;
    _gtimeRequested = 0;
    _stable = false;
    _resolved = false;
    _otherQuantStuck = false;
    _timeQuantStuck = false;
}

GlobalTime::GlobalTime(sc_module_name name, DUMMY_CTOR_ARG _arg, int* index) : quantity(name)
{
    _gtime = 0;
    _gtimeRequested = 0;
    _stable = false;
    _resolved = false;
    _otherQuantStuck = false;
    _timeQuantStuck = false;
}


GlobalTime::~GlobalTime() {
}

void GlobalTime::request(process *caller, event *e, RequestClass *rc) {
    reqPCs.insert(e->getProcess()->pc);
    if (!requestQA) return;

    _requestingPCs.insert(e->getProcess()->pc);
    event *pFound = NULL;

    int i;
    for (i = 0; i < _requests.size() && !pFound; i++) {
        //        request_t *pRequest = _requests[i];
        //        if (pRequest->_e->getProcess() == e->getProcess() &&
        //                *(pRequest->_e->getAction()) == *(e->getAction()))
        //            pFound = pRequest->_e;
        if (_requests[i]->_e->equals(e)) {
            pFound = _requests[i]->_e;
            break;
        }
    } // end for

    if (pFound) {
        if (_requests[i] ->_rc!= NULL) delete _requests[i] ->_rc;
        _requests[i]->_rc = (GlobalTimeRequestClass*) rc;
        //_requests.push_back(new request_t(pFound, (GlobalTimeRequestClass *)rc));
    } else
        _requests.push_back(new request_t(e, (GlobalTimeRequestClass *)rc));

    if (debug_flag && debug_level>2) {
        cerr<<"QA (GT): ";
        e->show();
        cerr<<" requesting "<<((GlobalTimeRequestClass*)rc)->_gtime<<endl;
    }
}

void GlobalTime::requestI(process *caller, event *e, GlobalTimeRequestClass *rc) {
    reqPCs.insert(e->getProcess()->pc);
    if (!requestQA) return;

    _requestingPCs.insert(e->getProcess()->pc);
    event *pFound = NULL;

    int i;
    for (i = 0; i < _requests.size() && !pFound; i++) {
        //        request_t *pRequest = _requests[i];
        //        if (pRequest->_e->getProcess() == e->getProcess() &&
        //                *(pRequest->_e->getAction()) == *(e->getAction()))
        //            pFound = pRequest->_e;
        if (_requests[i]->_e->equals(e)) {
            pFound = _requests[i]->_e;
            break;
        }
    } // end for

    //convert time interval to absolute time
    ((GlobalTimeRequestClass*)rc)->_gtime += _gtime;

    if (pFound) {
        if (_requests[i] ->_rc!= NULL) delete _requests[i] ->_rc;
        _requests[i]->_rc = (GlobalTimeRequestClass*) rc;
        //_requests.push_back(new request_t(pFound, (GlobalTimeRequestClass *)rc));
    } else
        _requests.push_back(new request_t(e, (GlobalTimeRequestClass *)rc));

    if (debug_flag && debug_level>2)
        cerr<<"QA (GT): "<<e->getProcess()->name()<<" requesting "<<rc->_gtime<<endl;
}

//void GlobalTime::resolve() {
void GlobalTime::resolve(process *caller) {
    if (debug_flag && debug_level>2) {
        cerr<<"QA (GT): *** Total # requests "<<_requests.size()<<endl;
        for (int i = 0; i < _requests.size(); i++) {
            request_t *pRequest = _requests[i];
            cerr<<"QA (GT): "<<pRequest->_e->getProcess()->name()<<" requests "<<pRequest->_rc->_gtime<<endl;
        } // end for
    }

    if (!_otherQuantStuck && !_timeQuantStuck) {
        for (int i=0; i<_builtInLOCRequests.size(); i++) {
            _requestingPCs.insert(_builtInLOCRequests[i]->_e1->getProcess()->pc);
        }

        _resolved = true;

        if (_timedPCs.size() > _requestingPCs.size() &&
                maxQAProc > _requestingPCs.size()) {
            std::set<ProgramCounter *>::iterator timedPCit;
            for(timedPCit = _timedPCs.begin(); timedPCit != _timedPCs.end() && _resolved; timedPCit++) {
                ProgramCounter * pc = *timedPCit;
                if (pc->SchedState == SchedStateVal::FINISHED)
                    _timedPCs.erase(timedPCit);
                else if (_requestingPCs.find(pc) == _requestingPCs.end())
                    _resolved = false;
            }
        }

        _stable = false;

        if (!_resolved) {
            if (!_stable) {
                for (int i = 0; i < _requests.size(); i++) {
                    request_t *pRequest = _requests[i];
                    pRequest->_e->getProcess()->pc->setMustNotDo(pRequest->_e);
                } // end for
            }
            _stable = true;
            _gtimeRequested = _gtime;
            return;
        }
    } else {
        _resolved = true;
    }

    _gtimeRequested = LONG_MAX;

    for (int i=0; i<_builtInLOCRequests.size(); i++) {
        biloc_request_t *req = _builtInLOCRequests[i];
        bool e1NeedRequest = false;
        bool e2NeedRequest = false;
        if (req->_e1 != NULL) {
            e1NeedRequest = req->_e1->isEnabled();
            e1NeedRequest &= (testSchedFlag == (req->_e1->getProcess()->pc->mode == RunType::TRY));
        }
        if (req->_e2 != NULL) {
            e2NeedRequest = req->_e2->isEnabled();
            e2NeedRequest &= (testSchedFlag == (req->_e2->getProcess()->pc->mode == RunType::TRY));
        }
        GlobalTimeRequestClass * last;
        switch(req->_type) {
        case BuiltInLOCType::MAXRATE:
            if (e1NeedRequest) {
                last = (GlobalTimeRequestClass*) req->_e1->getQuantityRequestClass(this, LAST);
                int reqIdx = requestIndex(req->_e1);
                if (last != NULL) {
                    if (reqIdx >= 0) {
                        double reqTime = _requests[reqIdx]->_rc->_gtime;
                        if (reqTime < last->_gtime + 1/(req->_rc->_gtime)) {
                            _requests.erase(_requests.begin() + reqIdx);
                            req->_e1->setMustNotDo();
                        }
                    } else {
                        _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(
                                                                  later(last->_gtime + (1+drand48()*RANDOMSCALE)/(req->_rc->_gtime)))));
                    }
                } else {
                    if (reqIdx < 0)
                        _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(_gtime)));
                }
            }
            break;
        case BuiltInLOCType::MINRATE:
            last = (GlobalTimeRequestClass*) req->_e1->getQuantityRequestClass(this, LAST);
            if (e1NeedRequest) {
                int reqIdx = requestIndex(req->_e1);
                if (last != NULL) {
                    if (reqIdx >= 0) {
                        double reqTime = _requests[reqIdx]->_rc->_gtime;
                        if (reqTime > last->_gtime + 1/(req->_rc->_gtime)) {
                            _requests.erase(_requests.begin() + reqIdx);
                            req->_e1->setMustNotDo();
                        }
                    } else {
                        _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(
                                                                  later(last->_gtime + drand48()/(req->_rc->_gtime)))));
                    }
                } else {
                    if (reqIdx < 0)
                        _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(_gtime)));
                }
            } else {
                if (last != NULL) {
                    _gtimeRequested = earlier(last->_gtime + 1.0/(req->_rc->_gtime));
                }
            }
            break;
        case BuiltInLOCType::PERIOD:
            last = (GlobalTimeRequestClass*) req->_e1->getQuantityRequestClass(this, LAST);
            if (e1NeedRequest) {
                int reqIdx = requestIndex(req->_e1);
                if (last != NULL) {
                    if (reqIdx >= 0) {
                        double reqTime = _requests[reqIdx]->_rc->_gtime;
                        if (fabs(reqTime - last->_gtime - req->_rc->_gtime) > DOUBLE_ERROR_TOLERANCE) {
                            _requests.erase(_requests.begin() + reqIdx);
                            req->_e1->setMustNotDo();
                        }
                    } else {
                        _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(
                                                                  last->_gtime + req->_rc->_gtime)));
                    }
                } else {
                    if (reqIdx < 0)
                        _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(_gtime)));
                }
            } else {
                if (last != NULL) {
                    _gtimeRequested = earlier(last->_gtime + req->_rc->_gtime);
                }
            }
            break;
        case BuiltInLOCType::MAXDELTA:
            if (req->_e1->isEnabled() && req->_e2->isEnabled()) {
                cerr<<"Internal error!"<<endl;
                req->_e1->show();
                cerr<<" and ";
                req->_e2->show();
                cerr<<" cannot be enabled at the same time."<<endl;
                exit(1);
            }
            if (e1NeedRequest) {
                int reqIdx = requestIndex(req->_e1);
                if (reqIdx < 0)
                    _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(
                                                              _gtime + drand48()*req->_rc->_gtime)));
            } else {
                last = (GlobalTimeRequestClass*) req->_e1->getQuantityRequestClass(this, LAST);
                if (e2NeedRequest) {
                    int reqIdx = requestIndex(req->_e2);
                    if (last != NULL) {
                        if (reqIdx >= 0) {
                            double reqTime = _requests[reqIdx]->_rc->_gtime;
                            if (reqTime > last->_gtime + req->_rc->_gtime) {
                                _requests.erase(_requests.begin() + reqIdx);
                                req->_e2->setMustNotDo();
                            }
                        } else {
                            _requests.push_back(new request_t(req->_e2, new GlobalTimeRequestClass(
                                                                      later(last->_gtime + drand48()*(req->_rc->_gtime)))));
                        }
                    } else {
                        if (reqIdx < 0)
                            _requests.push_back(new request_t(req->_e2, new GlobalTimeRequestClass(_gtime)));
                    }
                } else {
                    if (last != NULL) {
                        _gtimeRequested = earlier(last->_gtime + req->_rc->_gtime);
                    }
                }
            }
            break;
        case BuiltInLOCType::MINDELTA:
            if (req->_e1->isEnabled() && req->_e2->isEnabled()) {
                cerr<<"Internal error!"<<endl;
                req->_e1->show();
                cerr<<" and ";
                req->_e2->show();
                cerr<<" cannot be enabled at the same time."<<endl;
                exit(1);
            }
            if (e1NeedRequest) {
                int reqIdx = requestIndex(req->_e1);
                if (reqIdx < 0)
                    _requests.push_back(new request_t(req->_e1, new GlobalTimeRequestClass(
                                                              _gtime + drand48()*req->_rc->_gtime)));
            } else if (e2NeedRequest) {
                last = (GlobalTimeRequestClass*) req->_e1->getQuantityRequestClass(this, LAST);
                int reqIdx = requestIndex(req->_e2);
                if (last != NULL) {
                    if (reqIdx >= 0) {
                        double reqTime = _requests[reqIdx]->_rc->_gtime;
                        if (reqTime < last->_gtime + req->_rc->_gtime) {
                            _requests.erase(_requests.begin() + reqIdx);
                            req->_e2->setMustNotDo();
                        }
                    } else {
                        _requests.push_back(new request_t(req->_e2, new GlobalTimeRequestClass(
                                                                  later(last->_gtime + (1+drand48()*RANDOMSCALE)*(req->_rc->_gtime)))));
                    }
                } else {
                    if (reqIdx < 0)
                        _requests.push_back(new request_t(req->_e2, new GlobalTimeRequestClass(_gtime)));
                }
            }
            break;
        default:
            break;
        }
    }


    _stable = true;

    for (int i = 0; i < _requests.size(); i++) {
        request_t *pRequest = _requests[i];
        if (pRequest->_rc->_gtime < _gtimeRequested &&
                pRequest->_rc->_gtime >= _gtime)
            _gtimeRequested = pRequest->_rc->_gtime;
    } // end for

    for (int i = 0; i < _requests.size(); i++) {
        request_t *pRequest = _requests[i];
        if (pRequest->_rc->_gtime > _gtimeRequested || pRequest->_rc->_gtime < _gtime) {
            if (pRequest->_e->getProcess()->pc->setMustNotDo(pRequest->_e)) {
                _stable = false;
            } // end if
            //            if (pRequest->_e->getProcess()->pc->SchedState != SchedStateVal::DONTRUN) {
            //                _stable = false;
            //                pRequest->_e->getProcess()->pc->SchedState = SchedStateVal::DONTRUN;
            //            } // end if
        } // end if
    } // end for
}

//bool GlobalTime::stable() {
bool GlobalTime::stable(process *caller) {
    return _stable;
}

//void GlobalTime::postcond() {
void GlobalTime::postcond(process *caller) {
    //if (_gtimeRequested == LONG_MAX) return;

    //_gtime = _gtimeRequested;

    while (_requests.size() > 0) {
        request_t *pRequest = _requests[0];
        if (_resolved) {
            if (pRequest->_e->getProcess()->pc->setMustDo(pRequest->_e)) {
                _gtime = _gtimeRequested;
                GlobalTimeRequestClass *pRc = new GlobalTimeRequestClass(_gtime);
                pRequest->_e->addQuantityRequestClass((quantity *)this, pRc);
            }
        }
        if (_requests[0]->_rc!= NULL) delete _requests[0] ->_rc;
        _requests.erase(_requests.begin());
        delete pRequest;
    }

    //        if (dumpev)
    if (_resolved && debug_flag && debug_level>2)
        cerr<<"QA (GT): GlobalTime = "<<_gtime<<endl;
}

void GlobalTime::postpostcond() {
    if (!_resolved) {
        _otherQuantStuck = true;
        PCSet_t::iterator it;
        for(it = reqPCs.begin(); it != reqPCs.end() && _otherQuantStuck; it ++) {
            _otherQuantStuck = ((*it)->SchedState == SchedStateVal::DONTRUN);
            //|| (*it)->SchedState == SchedStateVal::FINISHED );
        }

        _timeQuantStuck = true;
        std::set<ProgramCounter *>::iterator timedPCit;
        for(timedPCit = _timedPCs.begin(); timedPCit != _timedPCs.end() && _timeQuantStuck; timedPCit++) {
            _timeQuantStuck = ((*timedPCit)->SchedState == SchedStateVal::DONTRUN);
            //|| (*timedPCit)->SchedState == SchedStateVal::FINISHED);
        }
    } else {
        _otherQuantStuck = false;
        _timeQuantStuck = false;
    } // end if
    if (debug_flag && debug_level>2) {
        cerr<<"QA (GT): _resolved, _otherQuantStuck, _timeQuantStuck, timedPCs.size(), reqPCs.size, _requestingPCs.size = ";
        cerr<<_resolved<<", "<< _otherQuantStuck<<", "<< _timeQuantStuck;
        cerr<<", "<< _timedPCs.size()<<", "<< reqPCs.size()<<", "<< _requestingPCs.size()<<endl;
    }
    _requestingPCs.clear();
}

//double GlobalTime::A(event *e, int i) {
double GlobalTime::A(process *caller, event *e, int i) {
    GlobalTimeRequestClass *pRc = (GlobalTimeRequestClass *)e->getQuantityRequestClass(this, i);
    if (!pRc) {
        if (debug_flag && debug_level>2) {
            cerr<<"QA (GT): ";
            e->show();
            cerr<<" annotated -1"<<endl;
        }
        return -1;
    }
    if (debug_flag && debug_level>2) {
        cerr<<"QA (GT): ";
        e->show();
        cerr<<" annotated "<<pRc->_gtime<<endl;
    }
    return pRc->_gtime;
}

void GlobalTime::registerLOC(process* caller, int type, event* e1, event* e2, RequestClass *r) {
    biloc_request_t *req = NULL;
    int i;
    for (i=0; i<_builtInLOCRequests.size(); i++) {
        req = _builtInLOCRequests[i];
        if (req->_type == type)
            if (req->_e1 == e1)
                if (req->_e2 == e2)
                    break;
    }

    if (i==_builtInLOCRequests.size()) { //No existing identical built-in LOC
        req = new biloc_request_t(type, e1, e2, (GlobalTimeRequestClass*) r);
        _builtInLOCRequests.push_back(req);
    } else {
        delete req->_rc;
        req->_rc = (GlobalTimeRequestClass*) r;
    }
}

void GlobalTime::unregisterLOC(process* caller, process *p) {
    for (int i=0; i<_builtInLOCRequests.size(); ) {
        if (_builtInLOCRequests[i]->_e1->getProcess() == p)
            _builtInLOCRequests.erase(_builtInLOCRequests.begin() + i);
        else
            i++;
    }
}

void GlobalTime::registerTimedProcess(process* proc){
    _timedPCs.insert(proc->pc);
}

int GlobalTime::requestIndex(event *e) {
    for (int i = 0; i < _requests.size(); i++) {
        if (_requests[i]->_e->equals(e))
            return i;
    }
    return -1;
}
