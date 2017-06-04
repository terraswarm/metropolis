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
// file   isa_be.h
// author Michael Dales (michael@dcs.gla.ac.uk)
// header n/a
// info   Defines the types for ARM instructions. For each instruction I've
//        defined the bitfield typedef and a mask/signiture for testing if
//        an instruction is of that type.
//
//        For more detailed instructions see the Furber book.
//
//        This file defines the big endian versions.
//
///////////////////////////////////////////////////////////////////////////////

#ifndef __ISA_BE_H__
#define __ISA_BE_H__

#include "swarm.h"

/* Branch/Link encoding */
typedef struct BLTAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 3;
  bool_t   link   : 1;
  uint32_t offset : 24;
} BRANCH;
#define BRANCH_MASK 0x0E000000
#define BRANCH_SIG  0x0A000000


/* Software interrupt */
typedef struct SWITAG
{
  uint32_t cond : 4;
  uint32_t pad  : 4;
  uint32_t val  : 24;
} SWI;
#define SWI_MASK    0x0F000000
#define SWI_SIG     0x0F000000


/* Data processing instruction - there are 3 types (see Furber book) */
typedef struct DP1TAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 2;
  uint32_t hash   : 1;
  uint32_t opcode : 4;
  uint32_t set    : 1;
  uint32_t rn     : 4;
  uint32_t rd     : 4;
  uint32_t rot    : 4;
  uint32_t imm    : 8;
} DPI1;

typedef struct DP2TAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 2;
  uint32_t hash   : 1;
  uint32_t opcode : 4;
  bool_t   set    : 1;
  uint32_t rn     : 4;
  uint32_t rd     : 4;
  uint32_t shift  : 5;
  uint32_t type   : 2;
  uint32_t pad2   : 1;
  uint32_t rm     : 4;
} DPI2;

typedef struct DP3TAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 2;
  uint32_t hash   : 1;
  uint32_t opcode : 4;
  bool_t   set    : 1;
  uint32_t rn     : 4;
  uint32_t rd     : 4;
  uint32_t rs     : 4;
  uint32_t pad2   : 1;
  uint32_t type   : 2;
  uint32_t pad3   : 1;
  uint32_t rm     : 4;
} DPI3;
#define  DPI_MASK    0x0C000000
#define  DPI_SIG     0x00000000


/* Multiply instruction */
typedef struct MTAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 4;
  uint32_t opcode : 3;
  uint32_t set    : 1;
  uint32_t rd     : 4;
  uint32_t rn     : 4;
  uint32_t rs     : 4;
  uint32_t pad2   : 4;
  uint32_t rm     : 4;
} MULT;
#define  MULT_MASK   0x0F0000F0
#define  MULT_SIG    0x00000090


/* Single word and unsigned byte transfer - again, two formats*/
typedef struct SW1TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 2;
  uint32_t hash  : 1;
  uint32_t p     : 1;
  uint32_t u     : 1;
  uint32_t b     : 1;
  uint32_t wb    : 1;
  uint32_t ls    : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t imm   : 12;
} SWT1;

typedef struct SW2TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 2;
  uint32_t hash  : 1;
  uint32_t p     : 1;
  uint32_t u     : 1;
  uint32_t b     : 1;
  uint32_t wb    : 1;
  uint32_t ls    : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t shift : 5;
  uint32_t type  : 2;
  uint32_t pad2  : 1;
  uint32_t rm    : 4;
} SWT2;
#define  SWT_MASK    0x0C000000
#define  SWT_SIG     0x04000000


/* Half-word and signed bype transfer - two formats, but use same fields, so
   I've only defined it once. */
typedef struct HWTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 3;
  uint32_t p     : 1;
  uint32_t u     : 1;
  uint32_t hash  : 1;
  uint32_t wb    : 1;
  uint32_t ls    : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t imm   : 4;
  uint32_t pad2  : 1;
  uint32_t s     : 1;
  uint32_t h     : 1;
  uint32_t pad3  : 1;
  uint32_t rm    : 4;
} HWT;
#define  HWT_MASK    0x0E000090
#define  HWT_SIG     0x00000090


/* Multiple register transfer */
typedef struct MRTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 3;
  uint32_t p     : 1;
  uint32_t u     : 1;
  uint32_t psr   : 1;
  uint32_t wb    : 1;
  uint32_t ls    : 1;
  uint32_t rn    : 4;
  uint32_t list  : 16;
} MRT;
#define MRT_MASK     0x0E000000
#define MRT_SIG      0x08000000


/* Swap memory and register */
typedef struct SWPTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 5;
  uint32_t byte  : 1;
  uint32_t pad2  : 2;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t pad3  : 8;
  uint32_t rm    : 4;
} SWP;
#define SWP_MASK    0x0FB00FF0
#define SWP_SIG     0x01000090


/* Status register to general register transfer */
typedef struct MRSTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 5;
  uint32_t which : 1;
  uint32_t pad2  : 6;
  uint32_t rd    : 4;
  uint32_t pad3  : 12;
} MRS;
#define MRS_MASK    0x0FBF0FFF
#define MRS_SIG     0x010F0000


/* General register to status register transfer - two formats */
typedef struct MSR1TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 2;
  uint32_t hash  : 1;
  uint32_t pad2  : 2;
  uint32_t which : 1;
  uint32_t pad3  : 2;
  uint32_t field : 4;
  uint32_t pad4  : 4;
  uint32_t rot   : 4;
  uint32_t imm   : 8;
} MSR1;

typedef struct MSR2TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 2;
  uint32_t hash  : 1;
  uint32_t pad2  : 2;
  uint32_t which : 1;
  uint32_t pad3  : 2;
  uint32_t field : 4;
  uint32_t pad4  : 4;
  uint32_t pad5  : 8;
  uint32_t rm    : 4;
} MSR2;
#define MSR_MASK    0x0DB0F000
#define MSR_SIG     0x0120F000


/* Coprocessor data transfers */
typedef struct CDOTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 4;
  uint32_t cop1  : 4;
  uint32_t crn   : 4;
  uint32_t crd   : 4;
  uint32_t cpn   : 4;
  uint32_t cop2  : 3;
  uint32_t pad2  : 1;
  uint32_t crm   : 4;
} CDO;
#define CDO_MASK    0x0F000010
#define CDO_SIG     0x0E000000


/* Coprocessor data transfers */
typedef struct CDTAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 3;
  uint32_t p      : 1;
  uint32_t u      : 1;
  uint32_t n      : 1;
  uint32_t wb     : 1;
  uint32_t ls     : 1;
  uint32_t rn     : 4;
  uint32_t crd    : 4;
  uint32_t cpn    : 4;
  uint32_t offset : 8;
} CDT;
#define CDT_MASK     0x0E000000
#define CDT_SIG      0x0C000000


/* Coprocessor register transfers */
typedef struct CRTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 4;
  uint32_t cop1  : 3;
  uint32_t ls    : 1;
  uint32_t crn   : 4;
  uint32_t rd    : 4;
  uint32_t cpn   : 4;
  uint32_t cop2  : 3;
  uint32_t pad2  : 1;
  uint32_t crm   : 4;
} CRT;
#define CRT_MASK    0x0F000010
#define CRT_SIG     0x0E000010


/* Unused arithmetic instruction */
typedef struct UAITAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 6;
  uint32_t op    : 2;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t rs    : 4;
  uint32_t pad2  : 4;
  uint32_t rm    : 4;
} UAI;
#define UAI_MASK    0x0FC000F0
#define UAI_SIG     0x00700090


/* Unused control instructions - there are 3 of these */
typedef struct UC1TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 5;
  uint32_t op    : 2;
  uint32_t pad2  : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t rs    : 4;
  uint32_t op2   : 3;
  uint32_t pad3  : 1;
  uint32_t rm    : 4;
} UCI1;
#define UCI1_MASK    0x0F900010
#define UCI1_SIG     0x01000000

typedef struct UC2TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 5;
  uint32_t op    : 2;
  uint32_t pad2  : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t rs    : 4;
  uint32_t pad3  : 1;
  uint32_t op2   : 2;
  uint32_t pad4  : 1;
  uint32_t rm    : 4;
} UCI2;
#define UCI2_MASK    0x0F900090
#define UCI2_SIG     0x01000010

typedef struct UC3TAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 5;
  uint32_t op    : 2;
  uint32_t pad2  : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t rot   : 4;
  uint32_t imm   : 8;
} UCI3;
#define UCI3_MASK    0x0F900000
#define UCI3_SIG     0x03000000


/* Unused Load/Store Instruction */
typedef struct ULSTAG
{
  uint32_t cond  : 4;
  uint32_t pad   : 3;
  uint32_t p     : 1;
  uint32_t u     : 1;
  uint32_t b     : 1;
  uint32_t wb    : 1;
  uint32_t ls    : 1;
  uint32_t rn    : 4;
  uint32_t rd    : 4;
  uint32_t rs    : 4;
  uint32_t pad3  : 1;
  uint32_t op2   : 2;
  uint32_t pad4  : 1;
  uint32_t rm    : 4;
} ULSI;
#define ULSI_MASK    0x0E000090
#define ULSI_SIG     0x00000090


/* Unused Coprocessor Instruction */
typedef struct UCPITAG
{
  uint32_t cond   : 4;
  uint32_t pad    : 4;
  uint32_t op     : 2;
  uint32_t pad2   : 1;
  uint32_t x      : 1;
  uint32_t rn     : 4;
  uint32_t crd    : 4;
  uint32_t cpn    : 4;
  uint32_t offset : 8;
} UCPI;
#define UCPI_MASK    0x0F200000
#define UCPI_SIG     0x0C000000


/* Undefined instruction space */
typedef struct UDTAG
{
  uint32_t cond : 4;
  uint32_t pad  : 3;
  uint32_t raw  : 20;
  uint32_t pad2 : 1;
  uint32_t raw2 : 4;
} UNDEF;
#define UNDEF_MASK   0x0E000010
#define UNDEF_SIG    0x06000010


/* General instruction */
typedef union ITAG
{
  UNDEF    undefined;
  UCPI     ucpi;
  ULSI     ulsi;
  UCI3     uci3;
  UCI2     uci2;
  UCI1     uci1;
  UAI      uai;
  CRT      crt;
  CDT      cdt;
  CDO      cdo;
  MSR2     msr2;
  MSR1     msr1;
  MRS      mrs;
  SWP      swap;
  MRT      mrt;
  HWT      hwt;
  SWT2     swt2;
  SWT1     swt1;
  MULT     mult;
  DPI3     dpi3;
  DPI2     dpi2;
  DPI1     dpi1;
  SWI      swi;
  BRANCH   branch;
  uint32_t raw;
} INST;

/* XSCALE SPECIFIC INSTRUCTIONS: ADDED BY CXH */
#define XS_MIA_MASK    0x0FF00F10
#define XS_MIA_SIG     0x0E200010

#endif /* __ISA_BE_H__ */
