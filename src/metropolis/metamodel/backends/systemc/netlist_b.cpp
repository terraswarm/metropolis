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
#include "netlist_b.h"

netlist_b::netlist_b(sc_module_name name) : sc_channel(name) {
    _ifTop = false;
    _childrenResolveCollected = false;
    _childrenPostcondCollected = false;
}

netlist_b::~netlist_b() {
}

void netlist_b::registerNetlist(std::string name, netlist_b *nl) {
    _nl.insert(nl_t::value_type(name, nl));
    _eq_r.insert(nl);
    _eq_p.insert(nl);
} // end registerNetlist

netlist_b *netlist_b::getNetlist(std::string name) {
    nl_t::iterator it = _nl.find(name);
    if (it == _nl.end()) return NULL;
    return it->second;
} // end getNetlist

eq_n_t netlist_b::getEqualSubNetlists(NetlistOperation op) {
    if (op == NetlistResolve)
        return _eq_r;
    else //if (op == NetlistPostcond)
        return _eq_p;
} // end getEqualSubNetlists

void netlist_b::addEqualSubNetlist(NetlistOperation op, netlist_b * n) {
    if (op == NetlistResolve)
        _eq_r.insert(n);
    else //if (op == NetlistPostcond)
        _eq_p.insert(n);
} // end addEqualSubNetlist

void netlist_b::addEqualSubNetlists(NetlistOperation op, eq_n_t eq) {
    if (op == NetlistResolve)
        for (eq_n_t::iterator it = eq.begin(); it != eq.end(); it++)
            _eq_r.insert((netlist_b*)(*it));
    else //if (op == NetlistPostcond)
        for (eq_n_t::iterator it = eq.begin(); it != eq.end(); it++)
            _eq_p.insert((netlist_b*)(*it));
} // end addEqualSubNetlists

bool netlist_b::isChildrenCollected(NetlistOperation op) {
    if (op == NetlistResolve)
        return _childrenResolveCollected;
    else //if (op == NetlistPostcond)
        return _childrenPostcondCollected;
} // end isChildrenCollected
