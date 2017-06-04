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
// file   alu.h
// author Michael Dales (michael@dcs.gla.ac.uk)
// header n/a
// info   Defines the type of ALU operations.
//
///////////////////////////////////////////////////////////////////////////////

#ifndef __ALU_H__
#define __ALU_H__

#include "swarm.h"
#include <fstream.h>

#define V_FLAG 0x10000000
#define C_FLAG 0x20000000
#define Z_FLAG 0x40000000
#define N_FLAG 0x80000000

#define CZ_FLAGS 0x60000000
#define NV_FLAGS 0x90000000

enum OPCODE {OP_AND = 0x00, OP_EOR = 0x01, OP_SUB = 0x02, OP_RSB = 0x03,
             OP_ADD = 0x04, OP_ADC = 0x05, OP_SBC = 0x06, OP_RSC = 0x07,
             OP_TST = 0x08, OP_TEQ = 0x09, OP_CMP = 0x0A, OP_CMN = 0x0B,
             OP_ORR = 0x0C, OP_MOV = 0x0D, OP_BIC = 0x0E, OP_MVN = 0x0F};

typedef uint32_t alu_fn (uint32_t a, uint32_t b, uint32_t* cont);

extern alu_fn and_op;
extern alu_fn eor_op;
extern alu_fn sub_op;
extern alu_fn rsb_op;
extern alu_fn add_op;
extern alu_fn adc_op;
extern alu_fn sbc_op;
extern alu_fn rsc_op;
extern alu_fn tst_op;
extern alu_fn teq_op;
extern alu_fn cmp_op;
extern alu_fn cmn_op;
extern alu_fn orr_op;
extern alu_fn mov_op;
extern alu_fn bic_op;
extern alu_fn mvn_op;

extern bool_t verbose, trace;
extern unsigned int TracePC;
extern ofstream tout;

#endif /* __ALU_H__ */
