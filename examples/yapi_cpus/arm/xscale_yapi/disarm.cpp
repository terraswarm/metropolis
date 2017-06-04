///////////////////////////////////////////////////////////////////////////////
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

#include "swarm.h"
#include "isa.h"
#include "disarm.h"
#include <string.h>

/* Multiplication opcodes */
#define MUL   0
#define MLA   1
#define UMULL 4
#define UMLAL 5
#define SMULL 6
#define SMLAL 7

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


void CDisarm::Decode(uint32_t inst, char* str)
{
    uint32_t i = inst;

    if (str == NULL)
        {
            throw CException();
        }

    if ((i & BRANCH_MASK) == BRANCH_SIG)
        {
            decode_branch(i, str);
        }
    else if ((i & SWI_MASK) == SWI_SIG)
        {
            decode_swi(i, str);
        }
    else if ((i & MULT_MASK) == MULT_SIG)
        {
            decode_mult(i, str);
        }
    else if ((i & DPI_MASK) == DPI_SIG)
        {
            decode_dpi(i, str);
        }
    else if ((i & SWT_MASK) == SWT_SIG)
        {
            decode_swt(i, str);
        }
#if 0
    else if ((i & HWT_MASK) == HWT_SIG)
        {
            decode_hwt(i, str);
        }
#endif
    else if ((i & MRT_MASK) == MRT_SIG)
        {
            decode_mrt(i, str);
        }
    return;
#if 0

    else if ((i & SWP_MASK) == SWP_SIG)
        {
            decode_swp(i, str);
        }
    else if ((i & MRS_MASK) == MRS_SIG)
        {
            decode_sgr(i, str);
        }
    else if ((i & MSR_MASK) == MSR_SIG)
        {
            decode_gsr(i, str);
        }
    else if ((i & CDO_MASK) == CDO_SIG)
        {
            decode_cdo(i, str);
        }
    else if ((i & CDT_MASK) == CDT_SIG)
        {
            decode_cdt(i, str);
        }
    else if ((i & CRT_MASK) == CRT_SIG)
        {
            decode_crt(i, str);
        }
    else if ((i & UAI_MASK) == UAI_SIG)
        {
            sprintf(str, "Unused arithmetic op\n");
        }
    else if ((i & UCI1_MASK) == UCI1_SIG)
        {
            sprintf(str, "Unused control 1\n");
        }
    else if ((i & UCI2_MASK) == UCI2_SIG)
        {
            sprintf(str, "Unused control 2\n");
        }
    else if ((i & UCI3_MASK) == UCI3_SIG)
        {
            sprintf(str, "Unused control 3\n");
        }
    else if ((i & ULSI_MASK) == ULSI_SIG)
        {
            sprintf(str, "Unused load/store\n");
        }
    else if ((i & UCPI_MASK) == UCPI_SIG)
        {
            sprintf(str, "Unused CoPro\n");
        }
    else if ((i & UNDEF_MASK) == UNDEF_SIG)
        {
            sprintf(str, "Undefined\n");
        }
    else
        {
            sprintf(str, "Rubbish");
        }
#endif
}



/******************************************************************************
 * decode_branch - Decodes a branch instruction.
 */
void CDisarm::decode_branch(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;
    char temp[10];

    sprintf(str, "b", i);

    if (inst.branch.link != 0)
        {
            strcat(str, "l");
        }

    sprintf(temp, "%s\t%x", cond[inst.branch.cond], inst.branch.offset);
    strcat(str, temp);
}


/******************************************************************************
 * decode_swi - Decode the software interrupt.
 */
void CDisarm::decode_swi(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "swi%s   %x", cond[inst.swi.cond], inst.swi.val);
}


/******************************************************************************
 * decode_dpi - Decode a data processing instruction. Note that for the most
 *              part we use DPI1 until we need to worry about specifics.
 */
void CDisarm::decode_dpi(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;
    char temp[20];

    sprintf(str, "%s%s", dpiops[inst.dpi1.opcode], cond[inst.dpi1.cond]);

    if ((inst.dpi1.opcode >> 2) != 0x2)
        if (inst.dpi1.set != 0)
            {
                strcat(str, "s");
            }

    //sprintf(str, "\t");
    strcat(str, "   ");

    if ((inst.dpi1.opcode != 0xA) && (inst.dpi1.opcode != 0xB)
            && (inst.dpi1.opcode != 0x8) && (inst.dpi1.opcode != 0x9))
        {
            sprintf(temp, "%s, ", regs[inst.dpi1.rd]);
            strcat(str, temp);
        }

    if ((inst.dpi1.opcode != 0xD) && (inst.dpi1.opcode != 0xF))
        {
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

                    if (inst.dpi2.shift != 0)
                        {
                            sprintf(temp, ", %s #%d\t; 0x%x", shift[inst.dpi2.type],
                                    inst.dpi2.shift, inst.dpi2.shift);
                            strcat(str, temp);
                        }
                }
            else
                {
                    //          sprintf(str, "%s", regs[inst.dpi3.rm]);
                    sprintf(temp, "%s, %s %s", regs[inst.dpi3.rm], shift[inst.dpi3.type], regs[inst.dpi3.rs]);
                    strcat(str, temp);
                }
        }
}


/******************************************************************************
 *
 */
void CDisarm::decode_mult(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;
    char temp[20];

    sprintf(str, "%s%s", mult[inst.mult.opcode], cond[inst.mult.cond]);

    if (inst.mult.set != 0)
        {
            strcat(str, "s");
        }

    switch (inst.mult.opcode)
        {
        case MUL:
            {
                sprintf(temp, "   %s, %s, %s", regs[inst.mult.rd], regs[inst.mult.rm],
                        regs[inst.mult.rs]);
            }
            break;
        case MLA:
            {
                sprintf(temp, "   %s, %s, %s, %s", regs[inst.mult.rd], regs[inst.mult.rm],
                        regs[inst.mult.rs], regs[inst.mult.rn]);
            }
            break;
        case UMULL: case UMLAL: case SMULL: case SMLAL:
            {
                sprintf(temp, "   %s, %s, %s, %s", regs[inst.mult.rd], regs[inst.mult.rn],
                        regs[inst.mult.rm], regs[inst.mult.rs]);
            }
            break;
        default:
            {
                sprintf(temp, "   ????");
            }
            break;
        }
    strcat(str, temp);
}


/******************************************************************************
 * decode_swt - Decodes a single word transfer instruction.
 */
void CDisarm::decode_swt(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;
    char* temp;

    /* print the instruction name */
    if (inst.swt1.ls != 0)
        sprintf(str, "ldr%s", cond[inst.swt1.cond]);
    else
        sprintf(str, "str%s", cond[inst.swt1.cond]);

    temp = str + 3 + (inst.swt1.cond == 14 ? 0 : 2);

    if (inst.swt1.b != 0)
        {
            sprintf(temp++, "b");
        }

    /* The T bit???? */

    sprintf(temp, "   %s, [%s", regs[inst.swt1.rd], regs[inst.swt1.rn]);
    temp += 10;
    temp += inst.swt1.rd == 10 ? 1 : 0;
    temp += inst.swt1.rn == 10 ? 1 : 0;

    if (inst.swt1.p == 0)
        {
            sprintf(temp, "], ");
            temp += 3;
        }
    else
        {
            sprintf(temp, ", ");
            temp += 2;
        }

    if (inst.swt1.hash == 0)
        {
            if (inst.swt1.u == 0)
                sprintf(temp++, "-");
            sprintf(temp, "#%d", inst.swt1.imm);
        }
    else
        {
            sprintf(temp, "%s, %s #%x", regs[inst.swt2.rm], shift[inst.swt2.type],
                    inst.swt2.shift);
        }
    if (inst.swt1.p == 1)
        {
            temp = str + strlen(str);
            sprintf(temp++, "]");
            if (inst.swt1.wb == 1)
                sprintf(temp, "!");
        }

}


/******************************************************************************
 * decode_hwt - Decodes a half word transfer.
 */
void CDisarm::decode_hwt(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\t", i);

    /* print the instruction name */
    if (inst.hwt.ls == 1)
        sprintf(str, "ldr%s", cond[inst.hwt.cond]);
    else
        sprintf(str, "str%s", cond[inst.hwt.cond]);

    if (inst.hwt.s == 1)
        sprintf(str, "s");
    if (inst.hwt.h == 1)
        sprintf(str, "h\t");
    else
        sprintf(str, "b\t");

    sprintf(str, "\t%s, [%s", regs[inst.hwt.rd], regs[inst.hwt.rn]);
    if (inst.hwt.p == 0)
        sprintf(str, "], ");
    else
        sprintf(str, ", ");

    if (inst.hwt.u == 0)
        sprintf(str, "-");

    if (inst.hwt.hash == 0)
        {
            sprintf(str, "#%d", (inst.hwt.imm << 4) + inst.hwt.rm);
        }
    else
        {
            sprintf(str, "%s", regs[inst.hwt.rm]);
        }

    if (inst.hwt.p == 1)
        {
            sprintf(str, "]");
            if (inst.hwt.wb == 1)
                sprintf(str, "!");
        }

    sprintf(str, "\n");
}


/******************************************************************************
 * decode_mrt - Decodes the Multiple Register Transfer instructions.
 */
void CDisarm::decode_mrt(uint32_t i, char* str)
{
    uint32_t rlist, cur;
    char* temp;

    INST inst;
    inst.raw = i;

    /* print the instruction name */
    if (inst.mrt.ls == 1)
        sprintf(str, "ldm%s", cond[inst.mrt.cond]);
    else
        sprintf(str, "stm%s", cond[inst.mrt.cond]);

    temp = str + 3 + (inst.swt1.cond == 14 ? 0 : 2);

    if (inst.mrt.u == 0)
        sprintf(temp++, "d");
    else
        sprintf(temp++, "i");

    if (inst.mrt.p == 0)
        sprintf(temp++, "a");
    else
        sprintf(temp++, "b");

    /* base register */
    sprintf(temp, "   %s", regs[inst.mrt.rn]);
    temp += 5;
    temp += (inst.mrt.rn == 10) ? 1 : 0;

    if (inst.mrt.wb == 1)
        sprintf(temp++, "!");

    sprintf(temp, ", {");
    temp += 3;

    rlist = inst.mrt.list;
    cur = 0;
    while (rlist != 0)
        {
            if ((rlist & 0x1) == 0x1)
                {
                    sprintf(temp, "%s", regs[cur]);
                    temp += 2;
                    temp += (cur == 10) ? 1 : 0;

                    if (rlist > 1)
                        {
                            sprintf(temp, ", ");
                            temp += 2;
                        }
                }
            cur++;
            rlist = rlist >> 1;
        }

    sprintf(temp, "}");
}


/******************************************************************************
 * decode_swp - Decodes a swap instruction.
 */
void CDisarm::decode_swp(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\tswp%s", i, cond[inst.swap.cond]);

    if (inst.swap.byte == 1)
        sprintf(str, "b");

    sprintf(str, "\t%s, %s, [%s]\n", regs[inst.swap.rd], regs[inst.swap.rm],
            regs[inst.swap.rn]);
}


/******************************************************************************
 * decode_sgr - Decodes a status to general register transfer.
 */
void CDisarm::decode_sgr(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\tmsr%s\t%s, ", i, cond[inst.mrs.cond], regs[inst.mrs.rd]);

    if (inst.mrs.which == 1)
        sprintf(str, " cpsr\n");
    else
        sprintf(str, " spsr\n");
}


/******************************************************************************
 * decode_gsr - Decodes a general to status register transfer.
 */
void CDisarm::decode_gsr(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\tmrs%s\t", i, cond[inst.msr1.cond]);

    if (inst.msr1.which == 1)
        sprintf(str, " cpsr_");
    else
        sprintf(str, " spsr_");

    if (inst.msr1.hash == 1)
        {
            sprintf(str, "f, #%x\n", inst.msr1.imm << inst.msr1.rot);
        }
    else
        {
            if (inst.msr2.field & 0x1)
                sprintf(str, "c, ");
            else if (inst.msr2.field & 0x2)
                sprintf(str, "x, ");
            else if (inst.msr2.field & 0x4)
                sprintf(str, "s, ");
            else
                sprintf(str, "f, ");

            sprintf(str, "%s\n", regs[inst.msr2.rm]);
        }
}


/******************************************************************************
 * decode_cdo - Decodes a CoPro data op.
 */
void CDisarm::decode_cdo(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\tcdp%s\t", i, cond[inst.cdo.cond]);

    sprintf(str, "p%x, %x, %s, %s, %s", inst.cdo.cpn, inst.cdo.cop1,
            cregs[inst.cdo.crd], cregs[inst.cdo.crn], cregs[inst.cdo.crm]);

    if (inst.cdo.cop2 != 0)
        sprintf(str, ", %x", inst.cdo.cop2);

    sprintf(str, "\n");
}


/******************************************************************************
 * decode_cdt - Decodes a CoPro data transfer.
 */
void CDisarm::decode_cdt(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\t", i);

    if (inst.cdt.ls == 1)
        sprintf(str, "ldc%s", cond[inst.cdt.cond]);
    else
        sprintf(str, "stc%s", cond[inst.cdt.cond]);

    if (inst.cdt.n == 1)
        sprintf(str, "l");

    sprintf(str, "\tp%x, %s, [%s", inst.cdt.cpn, cregs[inst.cdt.crd],
            regs[inst.cdt.rn]);

    if (inst.cdt.p == 0)
        sprintf(str, "]");

    if (inst.cdt.offset != 0)
        sprintf(str, ", #%d", inst.cdt.offset);

    if (inst.swt1.p == 1)
        {
            sprintf(str, "]");
            if (inst.swt1.wb == 1)
                sprintf(str, "!");
        }

    sprintf(str, "\n");
}


/******************************************************************************
 * decode_crt - Decode a CoPro register transfer.
 */
void CDisarm::decode_crt(uint32_t i, char* str)
{
    INST inst;
    inst.raw = i;

    sprintf(str, "%x\t", i);

    if (inst.crt.ls == 1)
        sprintf(str, "mcr");
    else
        sprintf(str, "mrc");

    sprintf(str, "%s\t p%x, %x, %s, %s, %s", cond[inst.crt.cond], inst.crt.cpn,
            inst.crt.cop1, regs[inst.crt.rd], cregs[inst.crt.crn],
            cregs[inst.crt.crm]);

    if (inst.crt.cop2 != 0)
        sprintf(str, ", %x", inst.crt.cop2);

    sprintf(str, "\n");
}
