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
#include "medium.h"
#include "portmap.h"
#include "node.h"
#include "statemediumDeclaration.h"

// return if an interface is being used by a ProgramCounter other than pc
bool medium::isIntfcUsed(const char* intfcName, ProgramCounter* pc) {
    if (intfcName[0]=='\0') return false;
    interfaceUsers_t::iterator it = _intfcUsed.find(intfcName);
    if (it == _intfcUsed.end()) return false;
    userPCs_t* user = it->second;
    return (user->size()-user->count(pc)>0);
} // end isIntfcUsed

// return if an interface is being prevented by a ProgramCounter other than pc
bool medium::isIntfcPrevented(const char* intfcName, ProgramCounter* pc) {
    if (intfcName[0]=='\0') return false;
    interfaceUsers_t::iterator it = _intfcPrevented.find(intfcName);
    if (it == _intfcPrevented.end()) return false;
    userPCs_t* user = it->second;
    return (user->size() - user->count(pc)>0);
} // end isIntfcPrevented

// add one interface that is used by pc
void medium::addUsedIntfc(const char* intfcName, ProgramCounter* pc) {
    interfaceUsers_t::iterator it = _intfcUsed.find(intfcName);
    if (it == _intfcUsed.end()) {
        userPCs_t* user = new userPCs_t;
        user->insert(pc);
        _intfcUsed.insert(interfaceUsers_t::value_type(intfcName, user));
    } else {
        it->second->insert(pc);
    }
} // end addUsedIntfc

// release one interface that is used by pc
void medium::releaseUsedIntfc(const char* intfcName, ProgramCounter* pc) {
    interfaceUsers_t::iterator it = _intfcUsed.find(intfcName);
    if (it == _intfcUsed.end()) return;
    userPCs_t* user = it->second;
    userPCs_t::iterator itpc = user->find(pc);
    if (itpc == user->end()) return;
    user->erase(itpc);

    //The following if {} statement could be commented out
    //for better time performance. Because of finite (small)
    //number of interfaces, it has no impact on memory performance.
    if (user->empty()) {
        _intfcUsed.erase(it);
        delete user;
    }
} // end releaseUsedIntfc

// add one interface that is prevented by pc
void medium::addPreventedIntfc(const char* intfcName, ProgramCounter* pc) {
    interfaceUsers_t::iterator it = _intfcPrevented.find(intfcName);
    if (it == _intfcPrevented.end()) {
        userPCs_t* user = new userPCs_t;
        user->insert(pc);
        _intfcPrevented.insert(interfaceUsers_t::value_type(intfcName, user));
    } else {
        it->second->insert(pc);
    }
} // end addPreventedIntfc

// release one interface that is prevented by pc
void medium::releasePreventedIntfc(const char* intfcName, ProgramCounter* pc) {
    interfaceUsers_t::iterator it = _intfcPrevented.find(intfcName);
    if (it == _intfcPrevented.end()) return;
    userPCs_t* user = it->second;
    userPCs_t::iterator itpc = user->find(pc);
    if (itpc == user->end()) return;
    user->erase(itpc);

    //The following if {} statement could be commented out
    //for better time performance. Because of finite (small)
    //number of interfaces, it has no impact on memory performance.
    if (user->empty()) {
        _intfcPrevented.erase(it);
        delete user;
    }
} // end releasePreventedIntfc

void medium::add2portMapList(const char *portName, sc_object * mediumPtr) {
    _portMapList.push_back(new portMap(portName, mediumPtr));
} // end add2portMapList

void medium::addPortIntfcEntry(simpleList_t * portList, const char *portName, const char *interfaceName, ProgramCounter* pc) {
    if (!strcmp(portName, "this")) {
        node *n = new node((sc_object *)this, interfaceName);
        portList->insert(portList->begin(), n);
        //_potentialUserPCs.insert(pc);
        pc->_potentialUsedMedia->insert(this);
    } else if (!strcmp(portName, "all")) {
        for (portMapList_t::iterator it = _portMapList.begin(); it != _portMapList.end(); it++) {
            portMap *p = *it;
            sc_object * scobj = (sc_object *) p->mediumPtr;
            node *n = new node(scobj, interfaceName); //interfaceName should be "all" in this case.
            portList->insert(portList->begin(), n);
            medium *m = dynamic_cast<medium*> (scobj);
            if (m) {
                //m->_potentialUserPCs.insert(pc);
                pc->_potentialUsedMedia->insert(m);
            }
        } // end for
    } else {
        for (portMapList_t::iterator it = _portMapList.begin(); it != _portMapList.end(); it++) {
            portMap *p = *it;
            if (p->portName == portName) {
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

        cerr<<name()<<"::addPortIntfcEntry() error! Cannot find "<<portName<<"."<<interfaceName<<endl;
        throw SIMPANIC();
    } // end if
} // end addPortIntfcEntry
