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
#ifndef MMSCS_BUCHIMAN_H
#define MMSCS_BUCHIMAN_H

#include "global.h"
#include <set>

#define INIT_STATE 0x1
#define FINAL_STATE 0x2

typedef std::set<BState *> BState_set_t;
typedef std::set<BTrans *> BTrans_set_t;

void pre(BState *, BState_set_t&, BState_set_t&);
void post(BState *, BState_set_t&, BState_set_t&);
void transToPost(BState *, BState_set_t& , int *, int *, bool, BTrans_set_t&);
void conditionalPost(BState *, BState_set_t&, int *, int *, bool, BState_set_t&);
void strictPre(BState *, BState_set_t&, BState_set_t&);
void strictPost(BState *, BState_set_t&, BState_set_t&);

void annotateDist2FS(BState *);   // should be called first before the following functions
void outputBuchi(BState *);
void outputBuchiStates(BState_set_t& );
void getInitStates(BState *, BState_set_t&);
void getFinalStates(BState *, BState_set_t&);

#endif
