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
#ifndef MMSCS_MATH_H
#define MMSCS_MATH_H

// FIXME: This file is called MetroMath.h instead of Math.h
// because under gcc-3.3.1 under Windows, when we include various
// files, some of the system includes include <math.h>, and because
// Windows is case insensitive, case preserving, we end up including
// Math.h instead, which causes compilation problems.

#include "object.h"

class Math  : public Object {
    public:
const static double E;
    const static double PI;

    static double cos(process *caller, double a);

    static double exp(process *caller, double a);

    static double log(process *caller, double a);

    static double pow(process *caller, double a, double b);

    static double sin(process *caller, double a);

    static double sqrt(process *caller, double a);

    static double tan(process *caller, double a);

    Math();

    Math(DUMMY_CTOR_ARG _arg, int *index);
};

#endif
