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
#ifndef MMSCS_NODE_H
#define MMSCS_NODE_H

#include "global.h"
#include "MetroString.h"

class node {
    public:
    sc_object * object;
    char *intfcName;    //think about using register_port to automatically
    //set up node information
    node() {
        object = NULL;
        intfcName = (char *) Empty_String;
    }

    node(node& obj) {
        object = obj.object;
        intfcName = obj.intfcName;
    }

/*    node(sc_object * o, String i) {
        object = o;
        intfcName = i.c_str();
    }
*/
    node(sc_object * o, const char *i) {
        object = o;
        intfcName = (char *) i;
    }

    // Explicit empty destructor.  Workaround for gcc bug affecting mgdb.
    ~node() {}

    void operator = ( node n) {
        object = n.object;
        intfcName = n.intfcName;
    }

    void operator = ( node * n) {
        object = n->object;
        intfcName = n->intfcName;
    }

    bool operator == (node n) {
        return (object==n.object && strcmp(intfcName, n.intfcName)==0);
    }

/*    void set(sc_object * o, String i) {
        object = o;
        intfcName = i.c_str();
    }
*/
};

#endif

