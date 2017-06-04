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
#ifndef MMSCS_MEDIUM_H
#define MMSCS_MEDIUM_H

#include "global.h"
#include "portmap.h"
#include "string.h"
#include "programcounter.h"
#include <list>
#include <map>
#include <set>

class portMap;
class StateMediumSched;

class medium : public sc_channel {
    protected:
typedef std::list <portMap *> portMapList_t;
    portMapList_t _portMapList;

    public:
    SC_HAS_PROCESS(medium);
    medium(sc_module_name name) : sc_channel(name) {}
    ~medium() {}

    int _id;                        //medium ID
    interfaceUsers_t _intfcUsed;
    interfaceUsers_t _intfcPrevented;
    PCSet_t _potentialUserPCs;

    // return if an interface is being used by a ProgramCounter other than pc
    bool isIntfcUsed(const char* intfcName, ProgramCounter* pc);

    // return if an interface is being prevented by a ProgramCounter other than pc
    bool isIntfcPrevented(const char* intfcName, ProgramCounter* pc);

    // add one interface that is used by pc
    void addUsedIntfc(const char* intfcName, ProgramCounter* pc);

    // release one interface that is used by pc
    void releaseUsedIntfc(const char* intfcName, ProgramCounter* pc);

    // add one interface that is prevented by pc
    void addPreventedIntfc(const char* intfcName, ProgramCounter* pc);

    // release one interface that is prevented by pc
    void releasePreventedIntfc(const char* intfcName, ProgramCounter* pc);

    void add2portMapList (const char *portName, sc_object * mediumPtr);

    void addPortIntfcEntry(simpleList_t * list, const char *portName, const char *interfaceName, ProgramCounter* pc);

};


#endif

