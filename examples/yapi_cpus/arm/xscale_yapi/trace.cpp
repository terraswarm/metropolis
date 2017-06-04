/*
  @Copyright (c) 2004 The Regents of the University of California.
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
// name   disarm.cpp
// author Michael Dales (michael@dcs.gla.ac.uk)
// header disarm.h
// info
//
///////////////////////////////////////////////////////////////////////////////

// OVERVIEW: Instruction trace-entry class cpp file for the XScale model.
//           Disassembles and creates the appropriate instances for each instruction.
//
// AUTHOR: Trevor Meyerowitz (tcm@eecs.berkeley.edu)
//

#include "swarm.h"
#include "isa.h"
#include "trace.h"
#include "disarm.h"
#include <string.h>
#include <iostream.h>
#include <stdlib.h>
#include <stdio.h>

/* Multiplication opcodes */
#define MUL   0
#define MLA   1
#define UMULL 4
#define UMLAL 5
#define SMULL 6
#define SMLAL 7

enum REGS {R_R0 = 0x00, R_R1 = 0x01, R_R2 = 0x02, R_R3 = 0x03,
           R_R4 = 0x04, R_R5 = 0x05, R_R6 = 0x06, R_R7 = 0x07,
           R_R8 = 0x08, R_R9 = 0x09, R_R10 = 0x0A, R_FP = 0x0B,
           R_IP = 0x0C, R_SP = 0x0D, R_LR = 0x0E, R_PC = 0x0F,
           R_CPSR = 0x10, R_SPSR = 0x11 /*HACK*/};


/* The condition codes */
const static char* cond[] = {"eq", "ne", "cs", "cc", "mi", "pl", "vs",
                             "vc", "hi", "ls", "ge", "lt", "gt", "le",
                             "", "nv"};

const static char* regs[] = {"r0", "r1", "r2", "r3", "r4", "r5", "r6",
                             "r7", "r8", "r9", "r10", "fp", "ip",
                             "sp", "lr", "pc"};

const static char* cregs[] = {"cr0", "cr1", "cr2", "cr3", "cr4", "cr5",
                              "cr6", "cr7", "cr8", "cr9", "cr10", "cr11",
                              "cr12", "cr13", "cr14", "cr15"};

const static char* dpiops[] = {"and", "eor", "sub", "rsb", "add", "adc",
                               "sbc", "rsc", "tst", "teq", "cmp", "cmn",
                               "orr", "mov", "bic", "mvn"};

const static char* shift[] = {"lsl", "lsr", "asr", "asl", "ror", "rrx"};

const static char* mult[] = {"mul", "mla", "???", "???",
                             "umull", "umla", "smull", "smlal"};


CTraceInst::CTraceInst(uint32_t _pc, uint32_t _inst, bool _exec)
{
    pc = _pc;
    inst = _inst;
    exec = _exec;

    num_op_regs = num_wr_regs = 0;
    //num_addresses = 0;
    writes_cond = reads_cond = false;

    uint32_t i = inst;

    extra_issue_cycles = 0;

    if ((i & BRANCH_MASK) == BRANCH_SIG)
        {
            trace_branch(i);
        }
    else if ((i & SWI_MASK) == SWI_SIG)
        {
            trace_swi(i);
        }
    else if ((i & MULT_MASK) == MULT_SIG)
        {
            trace_mult(i);
        }
    else if ((i & DPI_MASK) == DPI_SIG)
        {
            trace_dpi(i);
        }
    else if ((i & SWT_MASK) == SWT_SIG)
        {
            trace_swt(i);
        }
#if 0
    else if ((i & HWT_MASK) == HWT_SIG)
        {
            trace_hwt(i);
        }
#endif
    else if ((i & MRT_MASK) == MRT_SIG)
        {
            trace_mrt(i);
        }

    //#if 0

    else if ((i & SWP_MASK) == SWP_SIG)
        {
            trace_swp(i);
        }
    else if ((i & MRS_MASK) == MRS_SIG)
        {
            trace_sgr(i);
        }
    else if ((i & MSR_MASK) == MSR_SIG)
        {
            trace_gsr(i);
        }
    else if ((i & CDO_MASK) == CDO_SIG)
        {
            trace_cdo(i);
        }
    else if ((i & CDT_MASK) == CDT_SIG)
        {
            trace_cdt(i);
        }
    else if ((i & CRT_MASK) == CRT_SIG)
        {
            trace_crt(i);
        }
    else if ((i & UAI_MASK) == UAI_SIG)
        {
            //sprintf(op, "Unused arithmetic op\n");
        }
    else if ((i & UCI1_MASK) == UCI1_SIG)
        {
            //sprintf(op, "Unused control 1\n");
        }
    else if ((i & UCI2_MASK) == UCI2_SIG)
        {
            //sprintf(op, "Unused control 2\n");
        }
    else if ((i & UCI3_MASK) == UCI3_SIG)
        {
            //sprintf(op, "Unused control 3\n");
        }
    else if ((i & ULSI_MASK) == ULSI_SIG)
        {
            //sprintf(op, "Unused load/store\n");
        }
    else if ((i & UCPI_MASK) == UCPI_SIG)
        {
            //sprintf(op, "Unused CoPro\n");
        }
    else if ((i & UNDEF_MASK) == UNDEF_SIG)
        {
            //sprintf(op, "Undefined\n");
        }
    else
        {
            //sprintf(op, "Rubbish");
        }
    //#endif

    calculateExtraIssueCycles();
}

/*
  void CTraceInst::addAddress(int new_addr) {
  if (num_addresses < 16) {
  addresses[num_addresses] = new_addr;
  num_addresses++;
  } else {
  cerr << "CTRACEINST::addAddress tried to add too many addresses\n";
  exit(-1);
  }
  }

  int CTraceInst::getNumAddresses() {
  return num_addresses;
  }

  int CTraceInst::getAddress(int i) {
  if ((i >= 0) && (i < 16))
  return addresses[i];
  else
  return 0;
  }
*/

void CTraceInst::print() {
    char str[80];

    CDisarm::Decode(inst, str);
    cout << "----- " << str << endl;
    printf("pc:0x%08x inst:0x%08x inst_type:%d ex:%d\n",
            pc, inst, type, exec);
    printf("readcc:%d writecc:%d writer:%d opr:%d is_cyc:%d\n",
            reads_cond, writes_cond, num_wr_regs, num_op_regs, extra_issue_cycles);
    if (num_wr_regs > 0) {
        cout << "Write Regs:";
        for (int i=0; i<num_wr_regs; i++)
            cout << write_regs[i] << " ";
        cout << endl;
    }
    if (num_op_regs > 0) {
        cout << "Op Regs:";
        for (int i=0; i<num_op_regs; i++)
            cout << op_regs[i] << " ";
        cout << endl;
    }
    /*
      if (num_addresses > 0) {
      cout << "Addresses: ";
      for (int i=0; i<num_addresses; i++) {
      printf("0x%08x ", addresses[i]);
      }
      cout << endl;
      }
      cout << endl;
    */
}

uint32_t CTraceInst::getPC() {
    return pc;
}

uint32_t CTraceInst::getInst() {
    return inst;
}

uint32_t CTraceInst::getOpReg(int i) {
    return op_regs[i];
}

uint32_t CTraceInst::getNumOpRegs() {
    return num_op_regs;
}

uint32_t CTraceInst::getWriteReg(int i) {
    return write_regs[i];
}

uint32_t CTraceInst::getNumWriteRegs() {
    return num_wr_regs;
}

uint32_t CTraceInst::getImmediate() {
    return inst && 0xFF;
}

uint32_t CTraceInst::getBranchOffset() {
    return inst && 0xFFFFFF;
}


bool CTraceInst::getReadsCond() {
    return reads_cond;
}

bool CTraceInst::getWritesCond() {
    return writes_cond;
}

INST_TYPE CTraceInst::getInstType() {
    return type;
}

bool CTraceInst::executes() {
    return exec;
}

uint32_t CTraceInst::getExtraIssueCycles() {
    return extra_issue_cycles;
    //return 0; // for no issue cycle stalls
}

// calculates the number of extra issue cycles for instructions
// currently only for the sa-110 (need to change this)
void CTraceInst::calculateExtraIssueCycles() {
    INST temp_inst;
    temp_inst.raw = inst;

    extra_issue_cycles = 0;
    switch (type) {
    case tBRANCH:
        // resolved on the microarch-side of the model
        break;
    case tSWI:
        // estimate of interrupt delay time
        extra_issue_cycles = 5;
        break;
    case tDPI:
        if ((inst & DPI_MASK) == DPI_SIG) {
            if (temp_inst.dpi2.pad2 == 1)
                extra_issue_cycles = 1;  // extra cycle for register shift
        }
        else if ((inst & MRS_MASK) == MRS_SIG) {
            extra_issue_cycles = 0;
        }
        else if ((inst & MSR_MASK) == MSR_SIG) {
            extra_issue_cycles = 1;
        }
        else {
            cerr << "Invalid tDPI, exiting\n";
            exit(-1);
        }
        break;
    case tMULT:
        // not complete.. must be value dependent???
        switch (temp_inst.mult.opcode) {
        case MUL:
        case MLA:
            extra_issue_cycles = 1;
            break;
        case UMULL: case UMLAL: case SMULL: case SMLAL:
            extra_issue_cycles = 2;
            break;
        default:
            cerr << "Invalid multiply instruction: " << temp_inst.mult.opcode << ", exiting\n";
            exit(-1);
            break;
        }
        break;
    case tLOAD:
    case tSTORE:
        //if (load/store mult);
        if ((inst & MRT_MASK) == MRT_SIG) {
            if (type == tLOAD) {
                // 1 + N if R15 not written to, 6 + N if written to
                extra_issue_cycles=1;
                for (int i=0; i<num_wr_regs; i++) {
                    if (write_regs[i] == 15) {
                        extra_issue_cycles = 6;
                        break;
                    }
                }
                extra_issue_cycles += num_wr_regs;
            } else if (type == tSTORE) {
                extra_issue_cycles = num_op_regs;
            } else {
                cerr << "Error: wrong type of inst, expecting ldm/stm, exiting\n";
                exit(-1);
            }
        }
        break;
    case tSWP:
        extra_issue_cycles = 4; // stall has issue latency of 2
        break;
    default:
        extra_issue_cycles=0;
        break;
    }
    /* FOR MSR to control
    // not sure about this one
    extra_issue_cycles = 2;
    */
}

/******************************************************************************
 * trace_branch - Decodes a branch inopuction.
 */
void CTraceInst::trace_branch(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type = tBRANCH;

    num_op_regs = 0;
    num_wr_regs = 1;
    write_regs[0] = R_PC;

    if (inst.branch.link != 0)
        {
            write_regs[num_wr_regs] = R_SP;
            num_wr_regs++;
        }

    if ( (inst.branch.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }
}


/******************************************************************************
 * trace_swi - traces the software interrupt (Does nothing except of cond codes here)
 */
void CTraceInst::trace_swi(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type = tSWI;
    num_op_regs = 0;
    num_wr_regs = 0;

    if ( (inst.swi.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }

    //sprintf(op, "swi%s   %x", cond[inst.swi.cond], inst.swi.val);
}


/******************************************************************************
 * trace_dpi - Decode a data processing inopuction. Note that for the most
 *              part we use DPI1 until we need to worry about specifics.
 */
void CTraceInst::trace_dpi(uint32_t i)
{
    INST inst;
    inst.raw = i;

    char str[80], temp[20];

    sprintf(str, "0x%08X: %s%s", pc, dpiops[inst.dpi1.opcode], cond[inst.dpi1.cond]);

    num_op_regs = num_wr_regs = 0;
    writes_cond = false;
    type = tDPI;

    if ( (inst.dpi1.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }

    if ((inst.dpi1.opcode >> 2) != 0x2)
        if (inst.dpi1.set != 0)
            {
                writes_cond = true;
                strcat(str, "s");
            }

    sprintf(str, "\t");
    strcat(str, "   ");

    if ((inst.dpi1.opcode != 0xA) && (inst.dpi1.opcode != 0xB)
            && (inst.dpi1.opcode != 0x8) && (inst.dpi1.opcode != 0x9))
        {
            write_regs[num_wr_regs] = inst.dpi1.rd;
            num_wr_regs++;
            sprintf(temp, "%s, ", regs[inst.dpi1.rd]);
            strcat(str, temp);
        }

    if ((inst.dpi1.opcode != 0xD) && (inst.dpi1.opcode != 0xF))
        {
            op_regs[num_op_regs]=inst.dpi1.rn;
            num_op_regs++;
            sprintf(temp, "%s, ", regs[inst.dpi1.rn]);
            strcat(str, temp);
        }

    if (inst.dpi1.hash != 0)
        {
            uint32_t t = inst.dpi1.imm >> (inst.dpi1.rot * 2);
            t |= (inst.dpi1.imm << (32 - (inst.dpi1.rot * 2)));

            sprintf(temp, "#%d\t; 0x%x", t, t);
            strcat(str, temp);
        }
    else
        {
            if (inst.dpi2.pad2 == 0)
                {
                    sprintf(temp, "%s", regs[inst.dpi2.rm]);
                    strcat(str, temp);
                    op_regs[num_op_regs]=inst.dpi3.rm;
                    num_op_regs++;
                    if (inst.dpi2.shift != 0)
                        {
                            sprintf(temp, ", %s #%d\t; 0x%x", shift[inst.dpi2.type],
                                    inst.dpi2.shift, inst.dpi2.shift);
                            strcat(str, temp);
                        }
                }
            else
                {
                    op_regs[num_op_regs]=inst.dpi3.rm;
                    num_op_regs++;
                    op_regs[num_op_regs]=inst.dpi3.rs;
                    num_op_regs++;
                    sprintf(str, "%s", regs[inst.dpi3.rm]);
                    sprintf(temp, "%s, %s %s", regs[inst.dpi3.rm], shift[inst.dpi3.type], regs[inst.dpi3.rs]);
                    strcat(str, temp);
                }
        }

    //printf("%s\n", str);
}


/******************************************************************************
 *
 */
void CTraceInst::trace_mult(uint32_t i)
{
    INST inst;
    inst.raw = i;
    char temp[20];

    //sprintf(op, "%s%s", mult[inst.mult.opcode], cond[inst.mult.cond]);

    num_op_regs = num_wr_regs = 0;
    type = tMULT;
    writes_cond = false;

    if ( (inst.mult.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }


    if (inst.mult.set != 0)
        {
            writes_cond = true;
            // opcat(op, "s");
        }

    switch (inst.mult.opcode)
        {
        case MUL:
            {
                num_op_regs = 2;
                op_regs[0] = inst.mult.rm;
                op_regs[1] = inst.mult.rs;
                num_wr_regs = 1;
                write_regs[0] = inst.mult.rd;
                //sprintf(temp, "   %s, %s, %s", regs[inst.mult.rd], regs[inst.mult.rm],
                //       regs[inst.mult.rs]);
            }
            break;
        case MLA:
            {
                num_op_regs = 3;
                op_regs[0] = inst.mult.rm;
                op_regs[1] = inst.mult.rs;
                op_regs[2] = inst.mult.rn;
                num_wr_regs = 1;
                write_regs[0] = inst.mult.rd;
                //sprintf(temp, "   %s, %s, %s, %s", regs[inst.mult.rd], regs[inst.mult.rm],
                //       regs[inst.mult.rs], regs[inst.mult.rn]);
            }
            break;
        case UMULL: case UMLAL: case SMULL: case SMLAL:
            {
                num_op_regs = 3;
                op_regs[0] = inst.mult.rm;
                op_regs[1] = inst.mult.rs;
                op_regs[2] = inst.mult.rn;
                num_wr_regs = 1;
                write_regs[0] = inst.mult.rd;
                //sprintf(temp, "   %s, %s, %s, %s", regs[inst.mult.rd], regs[inst.mult.rn],
                //       regs[inst.mult.rm], regs[inst.mult.rs]);
            }
            break;
        default:
            {
                cerr << "Invalid multiply instruction: " << inst.mult.opcode << ", exiting.\n";
                exit(-1);
                //sprintf(temp, "   ????");
            }
            break;
        }
    //opcat(op, temp);
}


/******************************************************************************
 * trace_swt - Decodes a single word transfer inopuction.
 * Notes: doesn't detect address dependencies currently...
 */
void CTraceInst::trace_swt(uint32_t i)
{
    INST inst;
    inst.raw = i;
    char* temp;

    writes_cond = false;

    if ( (inst.swt1.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }

    /* print the instruction name */
    if (inst.swt1.ls != 0) {
        type = tLOAD;
        num_wr_regs=1;
        write_regs[0] = inst.swt1.rd;
        num_op_regs = 1;
        op_regs[0] = inst.swt1.rn;
        //sprintf(op, "ldr%s", cond[inst.swt1.cond]);
    }
    else {
        type = tSTORE;
        num_wr_regs=0;
        num_op_regs=2;
        op_regs[0] = inst.swt1.rd;
        op_regs[1] = inst.swt1.rn;
        //sprintf(op, "str%s", cond[inst.swt1.cond]);
    }

    //temp = op + 3 + (inst.swt1.cond == 14 ? 0 : 2);

    if (inst.swt1.b != 0)
        {
            //sprintf(temp++, "b");
        }

    /* The T bit???? */

    //sprintf(temp, "   %s, [%s", regs[inst.swt1.rd], regs[inst.swt1.rn]);

    //temp += 10;
    //temp += inst.swt1.rd == 10 ? 1 : 0;
    //temp += inst.swt1.rn == 10 ? 1 : 0;

    if (inst.swt1.p == 0)
        {
            //sprintf(temp, "], ");
            //temp += 3;
        }
    else
        {
            //sprintf(temp, ", ");
            //temp += 2;
        }

    if (inst.swt1.hash == 0)
        {
            if (inst.swt1.u == 0) {
                //sprintf(temp++, "-");
                //sprintf(temp, "#%d", inst.swt1.imm);
            }
        }
    else
        {
            //sprintf(temp, "%s, %s #%x", regs[inst.swt2.rm], shift[inst.swt2.type],
            //     inst.swt2.shift);
            op_regs[num_op_regs]=inst.swt2.rm;
            num_op_regs++;
        }
    if (inst.swt1.p == 1)
        {
            //temp = op + strlen(str);
            //sprintf(temp++, "]");
            //if (inst.swt1.wb == 1)
            //sprintf(temp, "!");
        }

}


/******************************************************************************
 * trace_hwt - Decodes a half word transfer.
 */
void CTraceInst::trace_hwt(uint32_t i)
{
    INST inst;
    inst.raw = i;
    char* temp;

    writes_cond = false;

    if ( (inst.hwt.cond && 0xE) != 0xE) {
        // use condition codes in this case cond = 0xE or 0xF (always/never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }

    /* print the instruction name */
    if (inst.hwt.ls == 1) {
        type = tLOAD;
        num_wr_regs=1;
        write_regs[0] = inst.hwt.rd;
        num_op_regs = 1;
        op_regs[0] = inst.hwt.rn;
        //sprintf(op, "ldr%s", cond[inst.swt1.cond]);
    }
    else {
        type = tSTORE;
        num_wr_regs=0;
        num_op_regs=2;
        op_regs[0] = inst.hwt.rd;
        op_regs[1] = inst.hwt.rn;
        //sprintf(op, "str%s", cond[inst.swt1.cond]);
    }

    //temp = op + 3 + (inst.swt1.cond == 14 ? 0 : 2);

    /* The T bit???? */

    //sprintf(temp, "   %s, [%s", regs[inst.swt1.rd], regs[inst.swt1.rn]);

    //temp += 10;
    //temp += inst.swt1.rd == 10 ? 1 : 0;
    //temp += inst.swt1.rn == 10 ? 1 : 0;

    if (inst.hwt.p == 0)
        {
            //sprintf(temp, "], ");
            //temp += 3;
        }
    else
        {
            //sprintf(temp, ", ");
            //temp += 2;
        }

    if (inst.hwt.hash == 0)
        {
            op_regs[num_op_regs]=inst.hwt.rm;
            num_op_regs++;
        }
    else
        {
            //sprintf(temp, "%s, %s #%x", regs[inst.swt2.rm], shift[inst.swt2.type],
            //     inst.swt2.shift);
            op_regs[num_op_regs]=inst.hwt.rm;
            num_op_regs++;
        }
    if (inst.hwt.p == 1)
        {
            //temp = op + strlen(str);
            //sprintf(temp++, "]");
            //if (inst.swt1.wb == 1)
            //sprintf(temp, "!");
        }
}


/******************************************************************************
 * trace_mrt - Decodes the Multiple Register Transfer instructions.
 */
void CTraceInst::trace_mrt(uint32_t i)
{
    uint32_t rlist, cur;
    char* temp;

    INST inst;
    inst.raw = i;

    writes_cond = false;
    num_wr_regs = 0;
    num_op_regs = 0;

    if ( (inst.mrt.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    }
    else {
        reads_cond = false;
    }

    /* print the instruction name */
    if (inst.mrt.ls == 1) {
        type = tLOAD;
        //sprintf(str, "ldm%s", cond[inst.mrt.cond]);
    }
    else {
        type = tSTORE;
        //sprintf(str, "stm%s", cond[inst.mrt.cond]);
    }
    //temp = str + 3 + (inst.swt1.cond == 14 ? 0 : 2);

    if (inst.mrt.u == 0){}
    //sprintf(temp++, "d");
    else {}
    //sprintf(temp++, "i");

    if (inst.mrt.p == 0){}
    //sprintf(temp++, "a");
    else {}
    //sprintf(temp++, "b");

    /* base register */
    //sprintf(temp, "   %s", regs[inst.mrt.rn]);
    //temp += 5;
    //temp += (inst.mrt.rn == 10) ? 1 : 0;
    num_op_regs = 1;
    op_regs[0] = inst.mrt.rn;



    if (inst.mrt.wb == 1){}
    //sprintf(temp++, "!");

    //sprintf(temp, ", {");
    //temp += 3;

    rlist = inst.mrt.list;
    cur = 0;
    while (rlist != 0)
        {
            if ((rlist & 0x1) == 0x1)
                {
                    if (type==tLOAD) {
                        write_regs[num_wr_regs]=cur;
                        num_wr_regs++;
                    }
                    else if (type==tSTORE) {
                        op_regs[num_op_regs]=cur;
                        num_op_regs++;
                    }
                    else {
                        cerr << "Invalid LDM/STM instruction\n";
                        exit(-1);
                    }
                    //sprintf(temp, "%s", regs[cur]);
                    //temp += 2;
                    //temp += (cur == 10) ? 1 : 0;

                    if (rlist > 1)
                        {
                            //sprintf(temp, ", ");
                            //temp += 2;
                        }
                }
            cur++;
            rlist = rlist >> 1;
        }


    //sprintf(temp, "}");
}


/******************************************************************************
 * trace_swp - Decodes a swap instruction.
 */
void CTraceInst::trace_swp(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type = tSWP;
    writes_cond = false;
    //sprintf(str, "%x\tswp%s", i, cond[inst.swap.cond]);


    if ( (inst.swap.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    } else {
        reads_cond = false;
    }

    num_wr_regs = 1;
    num_op_regs = 2;

    write_regs[0] = inst.swap.rd;
    op_regs[0] = inst.swap.rm;
    op_regs[1] = inst.swap.rn;

    if (inst.swap.byte == 1){}
    //sprintf(str, "b");


    //sprintf(str, "\t%s, %s, [%s]\n", regs[inst.swap.rd], regs[inst.swap.rm],
    // regs[inst.swap.rn]);
}


/******************************************************************************
 * trace_sgr - Decodes a status to general register transfer.
 */
void CTraceInst::trace_sgr(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type=tDPI;
    writes_cond = false;


    if ( (inst.mrs.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    } else {
        reads_cond = false;
    }

    num_wr_regs=1;
    num_op_regs=1;

    write_regs[0]=inst.mrs.rd;

    //sprintf(str, "%x\tmsr%s\t%s, ", i, cond[inst.mrs.cond], regs[inst.mrs.rd]);

    if (inst.mrs.which == 1) {
        op_regs[0]=R_CPSR;
        //sprintf(str, " cpsr\n");
    }
    else {
        op_regs[0]=R_SPSR;
    }
    //sprintf(str, " spsr\n");
}


/******************************************************************************
 * trace_gsr - Decodes a general to status register transfer.
 */
void CTraceInst::trace_gsr(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type=tDPI;
    writes_cond = false;

    if ( (inst.msr1.cond && 0xE) != 0xE) {
        // use condition codes unless cond = 0xE or 0xF (Always/Never)
        reads_cond = true;
    } else {
        reads_cond = false;
    }

    num_wr_regs=1;
    num_op_regs=0;

    //sprintf(str, "%x\tmrs%s\t", i, cond[inst.msr1.cond]);

    if (inst.msr1.which == 1) {
        write_regs[0]=R_CPSR;
    }
    //sprintf(str, " cpsr_");
    else {
        write_regs[0]=R_SPSR;
    }
    //sprintf(str, " spsr_");

    if (inst.msr1.hash == 1)
        {
            //sprintf(str, "f, #%x\n", inst.msr1.imm << inst.msr1.rot);
        }
    else
        {
            if (inst.msr2.field & 0x1){}
            //sprintf(str, "c, ");
            else if (inst.msr2.field & 0x2){}
            //sprintf(str, "x, ");
            else if (inst.msr2.field & 0x4){}
            //sprintf(str, "s, ");
            else {}
            //sprintf(str, "f, ");

            //sprintf(str, "%s\n", regs[inst.msr2.rm]);
            op_regs[0]=inst.msr2.rm;
            num_op_regs=1;
        }
}


/******************************************************************************
 * trace_cdo - Decodes a CoPro data op.
 */
void CTraceInst::trace_cdo(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type = tCOPROC;

    num_op_regs = 0;
    num_wr_regs = 0;

    //sprintf(str, "%x\tcdp%s\t", i, cond[inst.cdo.cond]);

    //sprintf(str, "p%x, %x, %s, %s, %s", inst.cdo.cpn, inst.cdo.cop1,
    //         cregs[inst.cdo.crd], cregs[inst.cdo.crn], cregs[inst.cdo.crm]);

    if (inst.cdo.cop2 != 0){}
    //sprintf(str, ", %x", inst.cdo.cop2);

    //sprintf(str, "\n");
}


/******************************************************************************
 * trace_cdt - Decodes a CoPro data transfer.
 */
void CTraceInst::trace_cdt(uint32_t i)
{
    INST inst;
    inst.raw = i;

    type = tCOPROC;

    //sprintf(str, "%x\t", i);

    if (inst.cdt.ls == 1){}
    //sprintf(str, "ldc%s", cond[inst.cdt.cond]);
    else {}
    //sprintf(str, "stc%s", cond[inst.cdt.cond]);

    if (inst.cdt.n == 1){}
    //sprintf(str, "l");

    //sprintf(str, "\tp%x, %s, [%s", inst.cdt.cpn, cregs[inst.cdt.crd],
    //         regs[inst.cdt.rn]);

    if (inst.cdt.p == 0){}
    //sprintf(str, "]");

    if (inst.cdt.offset != 0){}
    //sprintf(str, ", #%d", inst.cdt.offset);

    if (inst.swt1.p == 1)
        {
            //sprintf(str, "]");
            if (inst.swt1.wb == 1){}
            //sprintf(str, "!");
        }

    //sprintf(str, "\n");
}


/******************************************************************************
 * trace_crt - Decode a CoPro register transfer.
 */
void CTraceInst::trace_crt(uint32_t i)
{
    INST inst;
    inst.raw = i;
    char temp[40];

    //  sprintf(str, "%x\t", i);

    type = tCOPROC;

    // XScale multi with internal accumulate
    if ( ( i & XS_MIA_MASK ) == XS_MIA_SIG ) {
        cout << "!!!!!!!!!!!MIA!!!!!!!!!!\n";
        num_op_regs = 2;
        num_wr_regs = 0;
        op_regs[0] = inst.crt.crm;
        op_regs[1] = inst.crt.rd;

    }
    // Default MRT instruction
    else {
        // load-store bit
        if (inst.crt.ls) {
            num_wr_regs = 1;
            num_op_regs = 0;

            write_regs[0] =  inst.crt.rd;
        } else {
            num_wr_regs = 0;
            num_op_regs = 1;

            op_regs[0] = inst.crt.rd;
        }
    }

}
