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
// name   core.h
// author Michael Dales (michael@dcs.gla.ac.uk)
// header n/a
// info
//
///////////////////////////////////////////////////////////////////////////////

#ifndef __DISARM_H__
#define __DISARM_H__

class CDisarm
{
 public:
  static void Decode(uint32_t inst, char* str);

 private:
  static void decode_branch(uint32_t i, char* str);
  static void decode_swi(uint32_t i, char* str);
  static void decode_dpi(uint32_t i, char* str);
  static void decode_mult(uint32_t i, char* str);
  static void decode_swt(uint32_t i, char* str);
  static void decode_hwt(uint32_t i, char* str);
  static void decode_mrt(uint32_t i, char* str);
  static void decode_swp(uint32_t i, char* str);
  static void decode_sgr(uint32_t i, char* str);
  static void decode_gsr(uint32_t i, char* str);
  static void decode_cdo(uint32_t i, char* str);
  static void decode_cdt(uint32_t i, char* str);
  static void decode_crt(uint32_t i, char* str);
};

#endif // __DSIARM_H__
