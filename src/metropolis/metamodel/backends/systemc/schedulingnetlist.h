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
#ifndef MMSCS_SCHEDULINGNETLIST_H
#define MMSCS_SCHEDULINGNETLIST_H

#include "global.h"
#include "netlist_b.h"
#include "quantity.h"
#include "process.h"
#include "statemediumDeclaration.h"

class SchedulingNetlistIntfc : virtual public sc_interface
{
    public:
    virtual bool ifTop(process *caller) = 0;
    virtual void top(process *caller) = 0;
    virtual void postcond(process *caller) = 0;
    virtual void resolve(process *caller) = 0;
};

class SchedulingNetlist :  public netlist_b, virtual public SchedulingNetlistIntfc {

    protected:
    typedef std::map<std::string, statemedium *> sm_t;
    sm_t _sm;                // state medium list
    typedef std::map<std::string, quantity *> q_t;
    q_t _q;                        // list of quantities

    public:
    SchedulingNetlist(sc_module_name name) : netlist_b(name) {
        type = SchedulingNetlistType;
        //_childrenResolveCollected = false;
    }
    virtual ~SchedulingNetlist() { }

    void registerStatemedium(std::string name, statemedium * sm);
    void registerQuantity(std::string name, quantity *q);
    statemedium *getStatemedium(std::string name);
    quantity *getQuantity(std::string name);
    virtual sc_object *getcomponent(SchedulingNetlist *nl, std::string name);

    bool ifTop(process *caller) { return _ifTop;        }
    virtual void top(process *caller);
    virtual void postcond(process *caller);
    virtual void resolve(process *caller);

    int getNumQuantity() { return _q.size(); }
    int getNumStatemedium() { return _sm.size(); }
};

#endif
