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
#ifndef MMSCS_NETLIST_H
#define MMSCS_NETLIST_H

#include "global.h"
#include "netlist_b.h"
#include "buchiman.h"
#include "event.h"
#include <map>
#include <string>

class process;
class medium;

extern void tl_parse(void);

class netlistIntfc : virtual public sc_interface
{
    public:
virtual bool ifTop(process *caller) = 0;
    //virtual void top() = 0;   //commented out by YW
    virtual void resolve(process *caller) = 0; //added by YW
    virtual void postcond(process *caller) = 0;

    virtual sc_object *getcomponent(netlist *nl, std::string name) = 0;
    virtual void synch(int mode) = 0;
    virtual void ltl() = 0;
};

class netlist : public netlist_b, virtual public netlistIntfc {
    public:
    netlist(sc_module_name name);
    int num_event;  //number of events in LTL formula (=global variable sym_id)
    event ** ltlEvents;  //ordered events' references in LTL formula
    virtual ~netlist();

    protected:
    typedef std::map<std::string, process *> p_t;
    p_t _p;                        // process list
    typedef std::map<std::string, medium *> m_t;
    m_t _m;                        // medium list

    typedef std::map<event *, std::string> events_in_ltl_t;
    events_in_ltl_t     _ltl_events;
    std::string    _ltl_formula;
    int *pos, *neg, *undecided;   //representation of the presence/absence/undecided of ltlEvents
    BState* buchi;   //buchi automaton of LTL formula
    BState_set_t  activeStatesInBuchi;
    BState_set_t  finalStatesInBuchi;
    bool ltlEventOccur;

    public:
    void registerProcess(std::string name, process *p);
    void registerMedium(std::string name, medium *m);
    process *getProcess(std::string name);
    medium *getMedium(std::string name);

    virtual bool resolveSynch();

    void registerLTLEvent(event * e, std::string name);
    std::string getLTLEventDummyName(event * e);
                void appendLTLFormula(std::string formula);
    void build_buchi();
    bool checkOccuredLTLEvents();
    bool preGuideLTL();
    event* postCheckLTL(bool);
    bool collectCurrentLTLEventsStatus();
    void addLTLConstraintsToSAT(SAT_Manager, eventVarIndexMap_t&);

    bool ifTop(process *caller) { return _ifTop; }
    //virtual void top() = 0;   //commented out by YW
    virtual void resolve(process *caller);
    virtual void postcond(process *caller);
    sc_object *getcomponent(netlist *nl, std::string name);
    virtual void synch(int mode) { }
    virtual void ltl() { }
};

#endif
