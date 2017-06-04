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
#ifndef MMSCS_NONDETERMINISM_H
#define MMSCS_NONDETERMINISM_H

#include <set>

#include "global.h"
#include "object.h"
#include "process.h"
#include "MetroString.h"
#ifdef FIRST1
#define NONDET false
#else
#define NONDET true
#endif

int * intArrayGen(int, bool);
int nondeterminism(unsigned short size);

//should use template here for type of 'data' -GY
class Nondet : public Object {
public:
    int data;
    bool nondet;

    Nondet() {
        data = 0;
        nondet = true;
    }

    Nondet(int i) {
        data = i;
        nondet = false;
    }

    Nondet(DUMMY_CTOR_ARG _arg, int *index) {}

    void set(process *caller, int i) {
        data = i;
        nondet = false;
    }

    void set(int i) {
        data = i;
        nondet = false;
    }

    void setAny(process *caller) {
        data = nondeterminism(sizeof(int));
        nondet = true;
    }

    int get(process *caller) {
        return data;
    }

    int get() {
        return data;
    }

    bool isNondet(process *caller) {
        return nondet;
    }

    bool isNondet() {
        return nondet;
    }

};
#endif

