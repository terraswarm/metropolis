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
#include "process.h"
#include "portmap.h"
#include "programcounter.h"
#include "scoreboard.h"

process::process(sc_module_name name): sc_module(name) {
    pc = new ProgramCounter(this);
    mappingProcess = false;
}

void process::add2portMapList (const char *portName, sc_object * mediumPtr) {
    _portMapList.push_back(new portMap(portName, mediumPtr));
} // end add2portMapList

void process::addPortIntfcEntry(simpleList_t *portList,
        const char   *portName,
        const char   *interfaceName) {
    if (!strcmp(portName, "this")) {
        cerr<<"Warning: Process "<<name()<<" does not implement any interface.";
        cerr<<"It is meaningless to use "<<portName<<"."<<interfaceName
            <<" in await in this process."<<endl;
    } else if (!strcmp(portName, "all")) {
        for (portMapList_t::iterator it = _portMapList.begin();
             it != _portMapList.end();
             it++) {
            portMap *p = *it;
            sc_object * scobj = (sc_object *) p->mediumPtr;
            node *n = new node(scobj, interfaceName); // interfaceName should be
                                                      // "all" in this case.
            portList->insert(portList->begin(), n);
            medium *m = dynamic_cast<medium*> (scobj);
            if (m) {
                //m->_potentialUserPCs.insert(pc);
                pc->_potentialUsedMedia->insert(m);
            }
        } // end for
    } else {
        for (portMapList_t::iterator it = _portMapList.begin();
             it != _portMapList.end();
             it++) {
            portMap *p = *it;
            if (strcmp(p->portName.c_str(), portName) == 0) {
                sc_object * scobj = (sc_object *) p->mediumPtr;
                node *n = new node(scobj, interfaceName);
                portList->insert(portList->begin(), n);
                medium *m = dynamic_cast<medium*> (scobj);
                if (m) {
                    //m->_potentialUserPCs.insert(pc);
                    pc->_potentialUsedMedia->insert(m);
                }
                return;
            } // end if
        } // end for
    } // end if

    cerr<<name()<<"::addPortIntfcEntry() error! Cannot find "<<portName<<"."
        <<interfaceName<<endl;
    throw SIMPANIC();
} // end addPortIntfcEntry
