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
#ifndef MMSCS_OBJECT_H
#define MMSCS_OBJECT_H

#include "global.h"

class Object {
    public:
Object(DUMMY_CTOR_ARG _arg, int *index) {}
    Object() {}
    virtual ~Object() {}

    public:
    //        virtual bool operator== (Object const &ob) { return false; }
    //        virtual bool operator!= (Object const &ob) { return false; }
    virtual bool operator> (const Object &ob) const { return false; }
    virtual bool operator< (const Object &ob) const { return false; }
    virtual bool operator>= (const Object &ob) const { return false; }
    virtual bool operator<= (const Object &ob) const { return false; }
    virtual bool equals(Object ob) { return false; }

    //virtual int hashCode(process * caller) { return (int)this; }
    virtual int hashCode(process * caller) { return *((int*)this); }

    // do not use this function to clone your derived class objects
    virtual Object* clone(process * caller) { return NULL; }
};

#endif

