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
#include "schedulingnetlist.h"

void SchedulingNetlist::registerStatemedium(std::string name, statemedium *sm) {
    _sm.insert(sm_t::value_type(name, sm));
} // end registerStatemedium

void SchedulingNetlist::registerQuantity(std::string name, quantity *q) {
    _q.insert(q_t::value_type(name, q));
} // end registerQuantity

void SchedulingNetlist::top(process *caller) {
    this->resolve((process*)&_mng);
} // end top

void SchedulingNetlist::postcond(process *caller) {
    for (eq_n_t::iterator it = _eq_p.begin(); it != _eq_p.end(); it++)
        ((SchedulingNetlist*)(*it))->postcond((process*)&_mng);

    for (q_t::iterator it = _q.begin(); it != _q.end(); it++)
        it->second->postcond((process*)&_mng);

    for (sm_t::iterator it = _sm.begin(); it != _sm.end(); it++)
        it->second->postcond((process*)&_mng);

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

void SchedulingNetlist::resolve(process *caller) {
    bool bStable;

    do {
        bStable = true;

        for (eq_n_t::iterator it = _eq_r.begin(); it != _eq_r.end(); it++)
            ((netlist_b*)(*it))->resolve((process*)&_mng);

        for (q_t::iterator it = _q.begin(); it != _q.end(); it++) {
            quantity *pQuantity = it->second;
            pQuantity->resolve((process*)&_mng);
            if (!pQuantity->stable((process*)&_mng)) bStable = false;
        } // end for

        //commented out by YW
        /*
          for (sm_t::iterator it = _sm.begin(); it != _sm.end(); it++) {
          statemedium *pStatemedium = it->second;
          pStatemedium->resolve((process*)&_mng, 0, Empty_String);
          if (!pStatemedium->stable((process*)&_mng, 0, Empty_String)) bStable = false;
          } // end for
        */
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

statemedium *SchedulingNetlist::getStatemedium(std::string name) {
    sm_t::iterator it = _sm.find(name);
    if (it == _sm.end()) return NULL;
    return it->second;
} // end getStatemedium

quantity *SchedulingNetlist::getQuantity(std::string name) {
    q_t::iterator it = _q.find(name);
    if (it == _q.end()) return NULL;
    return it->second;
} // end getQuantity

sc_object *SchedulingNetlist::getcomponent(SchedulingNetlist *nl, std::string name) {
    sc_object *pResult = NULL;
    if (nl == this) {
        if ((pResult = getNetlist(name)))
            return pResult;
        else if ((pResult = getStatemedium(name)))
            return pResult;
        else if ((pResult = getQuantity(name)))
            return pResult;
    } else {
        for (nl_t::iterator it = _nl.begin(); it != _nl.end(); it++) {
            pResult = ((SchedulingNetlist*)it->second)->getcomponent(nl, name);
            if (pResult) return pResult;
        } // end for
    } // end if

    return NULL;
} // end getcomponent
