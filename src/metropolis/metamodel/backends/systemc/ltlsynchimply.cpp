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
#include "ltlsynchimply.h"
#include "nondeterminism.h"

bool EqualVar::isSatisfied() {
    if (v[0]->e != NULL) {
        if (!(v[0]->e->isEnabled()))
            return true;
        else if (v[0]->e->isInNamedEvent(FunctionType::AWAIT))
            return false;
    }
    if (v[1]->e != NULL) {
        if (!(v[1]->e->isEnabled()))
            return true;
        else if (v[0]->e->isInNamedEvent(FunctionType::AWAIT))
            return false;
    }

    Nondet * n0 = NULL;
    Nondet * n1 = NULL;
    void *v0, *v1;
    int type0, type1;

    v0 = v[0]->v;
    if (v[0]->nondet) {
        n0 = * ((Nondet**) v[0]->v);
        if (n0->isNondet())
            return true;
        v0 = &(n0->data);
    }
    type0 = v[0]->type;

    v1 = v[1]->v;
    if (v[1]->nondet) {
        n1 = * ((Nondet**) v[1]->v);
        if (n1->isNondet())
            return true;
        v1 = &(n1->data);
        type1 = v[1]->type;
    }
    type1 = v[1]->type;

    return equal(type0, v0, type1, v1);
}//end EqualVar::satisfied

bool EqualVar::equal(int type0, void *v0, int type1, void *v1) {
    double val0, val1;

    if (type0 == VarType::BYTETYPE)
        val0 = (double) (*((unsigned char*)v0));
    else if (type0 == VarType::CHARTYPE)
        val0 = (double) (*((char*)v0));
    else if (type0 == VarType::SHORTTYPE)
        val0 = (double) (*((short*)v0));
    else if (type0 == VarType::DOUBLETYPE)
        val0 = (double) (*((double*)v0));
    else if (type0 == VarType::FLOATTYPE)
        val0 = (double) (*((float*)v0));
    else if (type0 == VarType::INTTYPE )
        val0 = (double) (*((int*)v0));
    else if (type0 == VarType::LONGTYPE)
        val0 = (double) (*((long*)v0));
    else if (type0 == VarType::STRINGTYPE) {
        if (type1 != VarType::STRINGTYPE) {
            std::cerr<<"Cannot compare a String to a non-String."<<endl;
            return false;
        } else
            return (* ((String**) v0))->equals(* ((String**) v1));
    } else if (type0 == VarType::BOOLTYPE) {
        if (type1 != VarType::BOOLTYPE) {
            std::cerr<<"Cannot compare a boolean variable to a non-boolean variable."<<endl;
            return false;
        } else
            return (* ((bool*) v0)) & (* ((bool*) v1));
    }

    if (type1 == VarType::STRINGTYPE) {
        std::cerr<<"Cannot compare a String to a non-String."<<endl;
        return false;
    } else if (type1 == VarType::BOOLTYPE) {
        std::cerr<<"Cannot compare a boolean variable to a non-boolean variable."<<endl;
        return false;
    } else if (type1 == VarType::CHARTYPE)
        val1 = (double) (*((char*)v1));
    else if (type1 == VarType::SHORTTYPE)
        val1 = (double) (*((short*)v1));
    else if (type1 == VarType::DOUBLETYPE)
        val1 = (double) (*((double*)v1));
    else if (type1 == VarType::FLOATTYPE)
        val1 = (double) (*((float*)v1));
    else if (type1 == VarType::INTTYPE )
        val1 = (double) (*((int*)v1));
    else if (type1 == VarType::LONGTYPE)
        val1 = (double) (*((long*)v1));

    return (val0 == val1);
}//end EqualVar::equal
