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

///////////////////////////////////////////////////////////////////////////////
// Contains code from the SWARM emulator:
// Copyright 2000 Michael Dales
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// name   core.h
// author Michael Dales (michael@dcs.gla.ac.uk)
// header n/a
// info
//
///////////////////////////////////////////////////////////////////////////////


// OVERVIEW: Instruction trace-entry class header file
//
// AUTHOR: Trevor Meyerowitz (tcm@eecs.berkeley.edu)
//

#ifndef __TRACE_H__
#define __TRACE_H__

#include "memory.h"

typedef enum INST_TYPE{tBRANCH, tSWI, tDPI, tMULT,
                       tLOAD, tSTORE, tSWP, tCOPROC, tUNDEF};


// NOTE: none of the coprocessor stuff is implemented yet
//        the spsr isn't fully represented yet.

class CTraceInst
{
 public:
  CTraceInst(uint32_t _pc, uint32_t _inst, bool _exec);
 public:
  void print();
  uint32_t getPC();
  uint32_t getInst();
  uint32_t getOpReg(int i);
  uint32_t getNumOpRegs();
  uint32_t getWriteReg(int i);
  uint32_t getNumWriteRegs();
  uint32_t getImmediate();
  uint32_t getBranchOffset();
  bool getReadsCond();
  bool getWritesCond();
  INST_TYPE getInstType();
  bool executes();
  uint32_t getExtraIssueCycles();
 private:
  void calculateExtraIssueCycles();
  CTraceInst(); // not allowed
  void trace_branch(uint32_t i);
  void trace_swi(uint32_t i);
  void trace_dpi(uint32_t i);
  void trace_mult(uint32_t i);
  void trace_swt(uint32_t i);
  void trace_hwt(uint32_t i);
  void trace_mrt(uint32_t i);
  void trace_swp(uint32_t i);
  void trace_sgr(uint32_t i);
  void trace_gsr(uint32_t i);
  void trace_cdo(uint32_t i);
  void trace_cdt(uint32_t i);
  void trace_crt(uint32_t i);

  // private general data
  uint32_t pc, inst;
  bool exec;

  // private data about the instruction dependencies
  uint32_t op_regs[32];
  uint32_t write_regs[32];
  uint32_t num_op_regs;
  uint32_t num_wr_regs;
  uint32_t extra_issue_cycles;
  bool reads_cond, writes_cond;
  INST_TYPE type;
};

#endif // __TRACE_H__
