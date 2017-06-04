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
// file   swarm.h
// author Michael Dales (michael@dcs.gla.ac.uk)
// header n/a
// info   Includes all the default bits.
//
///////////////////////////////////////////////////////////////////////////////

#ifndef __SWARM_H__
#define __SWARM_H__

/* typedefs */
#include "swarm_types.h"
#include "swarm_macros.h"

#ifdef __cplusplus
// Basic exception class

class CException
{
 public:
  CException();
  virtual ~CException();

 public:
  inline const char* StrError() { return m_strError; }

 protected:
  char* m_strError;
};
#endif /* __cplusplus */

#endif /* __SWARM_H__ */
