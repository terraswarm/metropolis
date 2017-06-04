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
// file   alu.cpp
// author Michael Dales (michael@dcs.gla.ac.uk)
// header alu.h
// info   Defines the type of ALU operations.
//
///////////////////////////////////////////////////////////////////////////////

#include "swarm.h"
#include "alu.h"
#include <iostream.h>

#define CARRY_FROM(_a,_b,_r) ((_a >> 31) ? ((_b >> 31) | ((~_r) >> 31)) : ((_b >> 31) * ((~_r) >> 31)))

#define BORROWED_FROM(_a,_b,_r) ((_a >> 31) ? ((_b >> 31) & (_r >> 31)) : ((_b >> 31) | (_r >> 31)))


/******************************************************************************
 *
 */
uint32_t adc_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp;
    //int64_t result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int32_t)a;
    temp += (int32_t)b;
    if (c & C_FLAG)
        temp++;

    if (verbose)
        cout << "ADC_OP\n";

    //result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flags = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (short_res == 0)
        (*cond) |= Z_FLAG;

    // C Flag = CarryFrom(Rn + shifter_operand + C Flag)
    if (CARRY_FROM((uint32_t)a, (uint32_t)b, short_res))
        (*cond) |= C_FLAG;

    // V Flag = OverflowFrom(Rn + shifter_operand + C Flag)
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing ADC_OP(" << a << ", " << b
             << ")= " << (uint32_t)short_res << endl;
        //print out condition codes???
    }

    return (uint32_t)short_res;
}



/******************************************************************************
 *
 */
uint32_t add_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp;
    //int64_t result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int64_t)((int32_t)a);
    temp += (int64_t)((int32_t)b);

    //result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flags = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (short_res == 0)
        (*cond) |= Z_FLAG;

    // C Flag = CarryFrom(Rn + shifter_operand)
    if (CARRY_FROM((uint32_t)a, (uint32_t)b, short_res))
        (*cond) |= C_FLAG;

    // V Flag = OverflowFrom(Rn + shifter_operand)
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1)) {
        (*cond) |= V_FLAG;
    }

    if (verbose) {
        cout << "Executing ADD_OP(" << a << ", " << b
             << ")= " << (uint32_t)short_res << endl;
        //print out condition codes???
    }

    return (uint32_t)short_res;
}


/******************************************************************************
 *
 */
uint32_t and_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    uint32_t temp;

    temp = a & b;

    // Clear flags - V flag uneffected
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (temp == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing AND_OP(" << a << ", " << b
             << ")= " << temp << endl;
        //print out condition codes???
    }

    return temp;
}


/******************************************************************************
 *
 */
uint32_t bic_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    uint32_t temp;

    temp = a & ~b;

    // Clear flags
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (temp == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing BIC_OP(" << a << ", " << b
             << ")= " << temp << endl;
        //print out condition codes???
    }

    return temp;
}


/******************************************************************************
 *
 */
uint32_t cmn_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp;
    //int64_t result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int64_t)a;
    temp += (int64_t)b;

    //result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flags = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (short_res == 0)
        (*cond) |= Z_FLAG;

    // C Flag = CarryFrom(Rn + shifter_operand)
    if (CARRY_FROM((uint32_t)a, (uint32_t)b, short_res))
        (*cond) |= C_FLAG;

    temp = (int64_t)((int32_t)a);
    temp += (int32_t)b;

    // V Flag = OverflowFrom(Rn + shifter_operand)
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing CMN_OP(" << a << ", " << b
             << ")= " << endl;
        //print out condition codes???
    }

    return 0;
}


/******************************************************************************
 *
 */
uint32_t cmp_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp, result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int32_t)a;
    temp -= (int32_t)b;

    result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    uint64_t temp2 = (uint32_t)a;
    temp2 -= (uint32_t)b;

    uint64_t result2 = temp2 & 0x00000000FFFFFFFF;

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (short_res == 0)
        (*cond) |= Z_FLAG;

    // C Flag = NOT BorrowFrom(Rn - shifter_operand)
    //if (result2 == temp2)
    if (BORROWED_FROM((uint32_t)a, (uint32_t)b, short_res) == 0)
        (*cond) |= C_FLAG;

    // V Flag = OverFlowFrom (Rn - shifter_operand)
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing CMP_OP(" << a << ", " << b
             << ")= " << endl;
        //print out condition codes???
    }

    return 0;
}


/******************************************************************************
 *
 */
uint32_t eor_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    uint32_t temp;

    temp = a ^ b;

    // Clear flags (overflow unaffected)
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (temp == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing EOR_OP(" << a << ", " << b
             << ")= " << temp << endl;
        //print out condition codes???
    }

    return temp;
}


/******************************************************************************
 *
 */
uint32_t mov_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    // Clear flags (overflow unaffected)
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((b >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (b == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing MOV_OP(" << a << ", " << b
             << ")= " << b << endl;
        //print out condition codes???
    }

    return b;
}


/******************************************************************************
 *
 */
uint32_t mvn_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    // Clear flags (overflow unaffected)
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((b >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (~b == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing MVN_OP(" << a << ", " << b
             << ")= " << ~b << endl;
        //print out condition codes???
    }

    return ~b;
}


/******************************************************************************
 *
 */
uint32_t orr_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    uint32_t temp;

    temp = a | b;

    // Clear flags (overflow unaffected)
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((b >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = if Rd == 0 then 1 else 0
    if (temp == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing ORR_OP(" << a << ", " << b
             << ")= " << temp << endl;
        //print out condition codes???
    }

    return temp;
}


/******************************************************************************
 *
 */
uint32_t rsb_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp, result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int32_t)b;
    temp -= (int32_t)a;

    result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = If Rd == 0 then 1 else 0
    if (result == 0)
        (*cond) |= Z_FLAG;

    // C Flag = NOT BorrowedFrom(shifter_operand - Rn)
    if (BORROWED_FROM((uint32_t)b, (uint32_t)a, short_res) == 0)
        (*cond) |= C_FLAG;

    // V Flag = OverflowFrom(shifter_operand - Rn)
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing RSB_OP(" << a << ", " << b
             << ")= " << (uint32_t)result << endl;
        //print out condition codes???
    }

    return (uint32_t)result;
}


/******************************************************************************
 *
 */
uint32_t rsc_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp, result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int32_t)b;
    temp -= (int32_t)a;
    if (!(c & C_FLAG))
        temp--;

    result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = If Rd == 0 then 1 else 0
    if (result == 0)
        (*cond) |= Z_FLAG;

    // C Flag = NOT BorrowedFrom(shifter_operand - Rn - NOT(C Flag))
    if (BORROWED_FROM((uint32_t)b, (uint32_t)a, short_res) == 0)
        (*cond) |= C_FLAG;

    // V Flag = OverflowFrom(shifter_operand - Rn - NOT(C Flag))
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing RSC_OP(" << a << ", " << b
             << ")= " << (uint32_t)result << endl;
        //print out condition codes???
    }

    return (uint32_t)result;
}


/******************************************************************************
 *
 */
uint32_t sbc_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp, result;
    uint32_t short_res;
    uint32_t c = (*cond);

    temp = (int32_t)a;
    temp -= (int32_t)b;
    if (!(c & C_FLAG))
        temp--;

    result = temp & 0x00000000FFFFFFFFL;
    short_res = (uint32_t)((uint64_t)temp);

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = If Rd == 0 then 1 else 0
    if (result == 0)
        (*cond) |= Z_FLAG;

    // C Flag = NOT BorrowedFrom(Rn - shifter_operand - NOT(C Flag))
    if (BORROWED_FROM((uint32_t)a, (uint32_t)b, short_res) == 0)
        (*cond) |= C_FLAG;

    // V Flag = OverflowFrom(Rn - shifter_operand - NOT(C Flag))
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing SBC_OP(" << a << ", " << b
             << ")= " << (uint32_t)result << endl;
        //print out condition codes???
    }

    return (uint32_t)result;
}


/******************************************************************************
 *
 */
uint32_t sub_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int64_t temp, result, foo;
    uint32_t c = (*cond);
    uint32_t res;

    temp = (int32_t)a;
    temp -= (int32_t)b;

    result = temp & 0x00000000FFFFFFFFL;

    // Clear flags
    *cond &= 0x0FFFFFFF;

    // N Flag = Rd[31]
    if (((temp >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = If Rd == 0 then 1 else 0
    if (result == 0)
        (*cond) |= Z_FLAG;

    // C Flag = NOT BorrowedFrom(Rn - shifter_operand - NOT(C Flag))
#if 0
    if (result == temp)
        (*cond) |= C_FLAG;
#else
    foo = (uint32_t)((uint64_t)result);
    //cout.form("foo = 0x%08x\n", foo);
    if (BORROWED_FROM(a, b, foo) == 0)
        (*cond) |= C_FLAG;

#endif

    // V Flag = OverflowFrom(Rn - shifter_operand - NOT(C Flag))
    if (((temp >> 32) & 0x1) != ((temp >> 31) & 0x1))
        (*cond) |= V_FLAG;

    if (verbose) {
        cout << "Executing SUB_OP(" << a << ", " << b
             << ")= " << (uint32_t)result << endl;
        //print out condition codes???
    }

    return (uint32_t)result;
}


/******************************************************************************
 *
 */
uint32_t teq_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    uint32_t result;

    result = a ^ b;

    // Clear Flags (overflow unaffected)
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((result >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = If Rd == 0 then 1 else 0
    if (result == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing TEQ_OP(" << a << ", " << b
             << ")= " << 0 << endl;
        //print out condition codes???
    }

    return 0;
}


/******************************************************************************
 *
 */
uint32_t tst_op(uint32_t a, uint32_t b, uint32_t* cond)
{
    int32_t result;

    result = a & b;

    // Clear Flags (overflow unaffected)
    *cond &= (0x0FFFFFFF | V_FLAG);

    // N Flag = Rd[31]
    if (((result >> 31) & 0x1) == 1)
        (*cond) |= N_FLAG;

    // Z Flag = If Rd == 0 then 1 else 0
    if (result == 0)
        (*cond) |= Z_FLAG;

    // C Flag = shifter_carry_out
    // Done otherwhere

    // V Flag = unaffected

    if (verbose) {
        cout << "Executing TST_OP(" << a << ", " << b
             << ")= " << 0 << endl;
        //print out condition codes???
    }

    return 0;
}
