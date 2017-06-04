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
#ifndef MMSCS_NETLIST_B_H
#define MMSCS_NETLIST_B_H

#include "global.h"
#include <map>
#include <string>

class quantity;
class statemedium;
class process;
class medium;
class netlist;
class netlist_b;

enum NetlistType {NormalNetlistType, SchedulingNetlistType};
enum NetlistOperation {NetlistResolve, NetlistPostcond};
typedef std::set<netlist_b *> eq_n_t;

class netlist_b : public sc_channel{
    public:
netlist_b(sc_module_name name);
    virtual ~netlist_b();

    int type;

    protected:
    typedef std::map<std::string, netlist_b *> nl_t;
    nl_t _nl;                // netlist

    bool _ifTop;

    eq_n_t _eq_r; // a equal set of sub-netlists whose resolve() needs to be called
    bool _childrenResolveCollected;
    eq_n_t _eq_p; // a equal set of sub-netlists whose postcond() needs to be called
    bool _childrenPostcondCollected;

    public:
    void registerNetlist(std::string name, netlist_b *nl);
    netlist_b *getNetlist(std::string name);

    eq_n_t getEqualSubNetlists(NetlistOperation op);
    void addEqualSubNetlist(NetlistOperation op, netlist_b * n);
    void addEqualSubNetlists(NetlistOperation op, eq_n_t eq);
    bool isChildrenCollected(NetlistOperation op);
    virtual void resolve(process * caller) = 0;
    virtual void postcond(process * caller) = 0;
};


#endif
