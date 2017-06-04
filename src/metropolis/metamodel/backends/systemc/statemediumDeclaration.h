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

/* Note, this file is called statemediumDeclaration.h instead
 * of statemedium.h so as to avoid confusion with 
 * $METRO/lib/metamodel/lang/StateMedium.h under Windows.
 * The reason is that the Windows file system is case insensitive,
 * case preserving, so #include "statemedium.h" will include either
 * statemedium.h or StateMedium.h, depending which comes first in
 * the search path.
 */

#ifndef MMSCS_STATEMEDIUMDECLARATION_H
#define MMSCS_STATEMEDIUMDECLARATION_H

#include "global.h"
#include "medium.h"
#include "event.h"
#include "scoreboard.h"

/** StateMediumSched and StateMediumProc are two interfaces,
    which statemedium must implement.                    **/
class StateMediumSched : virtual public sc_interface {
    public:
virtual void registerObj(sc_object * o, bool bProcess = false) = 0;
    virtual process *getProcess(process *caller) = 0;
    virtual SchedProgramCounter *getProgramCounter(process *caller) = 0;
    virtual int getSchedState(process *caller) = 0;
    virtual bool setSchedState(process *caller, int newState) = 0;

    virtual event **getCanDo(process *caller) = 0;
    virtual bool setMustDo(process *caller, event *e) = 0;
    virtual bool setMustNotDo(process *caller, event *e) = 0;
    virtual void resolve(process *caller) = 0;
    virtual bool stable(process *caller) = 0;
    virtual void postcond(process *caller) = 0;
    virtual int getNumEnabledEvents(process *caller) = 0;
    virtual event *getEnabledEvent(process *caller, int i) = 0;
    virtual bool isEventEnabled(process *caller, event *e) = 0;
    virtual event *getMustDo(process *caller) = 0;

    /*
      virtual event **getCanDo() = 0;
      virtual void setMustDo(event *e) = 0;
      virtual void setMustNotDo(event *e) = 0;
      virtual void resolve() = 0;
      virtual bool stable() = 0;
      virtual void postcond() = 0;
      virtual int getNumEnabledEvents() = 0;
      virtual event *getEnabledEvent(int i) = 0;
      virtual bool isEventEnabled(event *e) = 0;
      virtual event *getMustDo() = 0;
    */
};

class StateMediumProc : virtual public sc_interface {};

class statemedium : public sc_channel,
      virtual public StateMediumSched, virtual public StateMediumProc {
    public:
sc_object *connectedObj;
    bool _bProcess;
    bool _bStable;

    SC_HAS_PROCESS(statemedium);
    statemedium(sc_module_name name) : sc_channel(name) { }
    void registerObj(sc_object * o, bool bProcess = false);
    process *getProcess(process *caller);
    SchedProgramCounter *getProgramCounter(process *caller);
    int getSchedState(process *caller);
    bool setSchedState(process *caller, int newState);

    virtual event **getCanDo(process *caller);
    virtual bool setMustDo(process *caller, event *e);
    virtual bool setMustNotDo(process *caller, event *e);
    virtual void resolve(process *caller);
    virtual bool stable(process *caller);
    virtual void postcond(process *caller);
    virtual int getNumEnabledEvents(process *caller);
    virtual event *getEnabledEvent(process *caller, int i);
    virtual bool isEventEnabled(process *caller, event *e);
    virtual event *getMustDo(process *caller);

    /*
      virtual event **getCanDo();
      virtual void setMustDo(event *e);
      virtual void setMustNotDo(event *e);
      virtual void resolve();
      virtual bool stable();
      virtual void postcond();
      virtual int getNumEnabledEvents();

      virtual event *getEnabledEvent(int i);
      virtual bool isEventEnabled(event *e);
      virtual event *getMustDo();
    */
};

#endif
