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
#ifndef MMSCS_GLOBALTIMEREQUESTCLASS_H
#define MMSCS_GLOBALTIMEREQUESTCLASS_H

#include "global.h"
#include "requestclass.h"

class GlobalTimeRequestClass : public RequestClass {
    public:
    GlobalTimeRequestClass(double gtime) { _gtime = gtime; }
    GlobalTimeRequestClass() { }
    GlobalTimeRequestClass(DUMMY_CTOR_ARG _arg, int* index) { }

    double _gtime;
    double _period;
    void setRequestTime(process *caller, double gtime) { _gtime = gtime; }
};

#endif

