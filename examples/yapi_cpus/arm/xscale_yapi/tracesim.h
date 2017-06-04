/*
@Copyright (c) 2004-2005 The Regents of the University of California.
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

// OVERVIEW: A list of Trace Instructions for use with the Metropolis 
///          microarchitectural models of the Strongarm and XScale microprocessors
// 
// AUTHOR: Trevor Meyerowitz (tcm@eecs.berkeley.edu)
//

#ifndef TRACE_SIM
#define TRACE_SIM

#include <iostream.h>
#include <fstream.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "swarm.h"
#include "disarm.h"
#include "trace.h"

class TraceList{
 public:
  TraceList(const char *filename);
  ~TraceList();
  unsigned int getSize();
  CTraceInst* getInst(int i);
  //unsigned int getCurrInstNum();
  bool isFinished();
  void print();
 private:
  TraceList(); // undefined
  unsigned int getNumLines(const char *filename);
  // variables
  unsigned int size, curr_inst_num, temp;
  CTraceInst **trace_list;
};
#endif
