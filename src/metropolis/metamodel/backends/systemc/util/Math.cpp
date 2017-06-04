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
#include "./MetroMath.h"
#include <math.h>

const double Math::E = 2.71828182845904523536028747135266249775724709369995;
;
const double Math::PI = 3.14159265358979323846264338327950288419716939937510;
;





double Math::cos(process *caller, double a)
{
    return (double) ::cos(a);
};

double Math::exp(process *caller, double a)
{
    return (double) ::exp(a);
};

double Math::log(process *caller, double a)
{
    return (double) ::log(a);
};

double Math::pow(process *caller, double a, double b)
{
    return (double) ::pow(a, b);
};

double Math::sin(process *caller, double a)
{
    return (double) ::sin(a);
};

double Math::sqrt(process *caller, double a)
{
    return (double) ::sqrt(a);
};

double Math::tan(process *caller, double a)
{
    return (double) ::tan(a);
};


Math::Math() : Object() {
};

Math::Math(DUMMY_CTOR_ARG _arg, int *index): Object(_arg, &index[0]) {
}
