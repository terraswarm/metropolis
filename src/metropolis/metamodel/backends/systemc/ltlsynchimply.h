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
#ifndef MMSCS_LTLSYNCHIMPLY_H
#define MMSCS_LTLSYNCHIMPLY_H

#include "global.h"
#include "event.h"

class Variable {
public:
    Variable(event *evt, bool n, bool c, unsigned char t)
    {
        e = evt;
        v = NULL;
        nondet = n;
        constant = c;
        type = t;
    }

    event * e;
    void * v;
    bool nondet;
    bool constant;
    unsigned char type;
};

class EqualVar {
public:
    EqualVar(Variable *v1, Variable *v2)
    {
        v[0] = v1;
        v[1] = v2;
    }

    Variable * v[2];

    bool isSatisfied();
    static bool equal(int type0, void* v0, int type1, void* v1);
};

class LTLSynchImply {
public:
    LTLSynchImply() {}

    event ** lhs;
    unsigned char lhsNum;
    event ** rhs;
    unsigned char rhsNum;
    EqualVar ** eqvars;
    unsigned char eqvarsNum;
};

#endif
