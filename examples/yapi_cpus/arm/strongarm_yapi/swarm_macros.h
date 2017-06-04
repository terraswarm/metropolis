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
// file   swarm_macros.h
// author Michael Dales (michael@dcs.gla.ac.uk)
// header n/a
// info   Some useful macro definitions.
//
///////////////////////////////////////////////////////////////////////////////

#ifndef __SWARM_MACROS_H__
#define __SWARM_MACROS_H__

#include <stdio.h>
#include <signal.h>

#ifdef DEBUG
#define ASSERT(_x)         if (!(_x)) {\
                     fprintf(stderr, "ASSERT at line %d of %s : %s\n", \
                       __LINE__, __FILE__, #_x);\
                     raise(SIGTRAP);}
#else
#define ASSERT(_x)
#endif

#undef DELETE
#ifdef DEBUG_MEM
#define NEW(_t)             ({\
                              void* _p = new _t;\
                              fprintf(stderr, "creating %s at %p in %s:%d\n", #_t, _p, __FILE__, __LINE__);\
                              /*(_t*)*/_p;\
                            })
#define TNEW(_t)             ({\
                              void* _p = new _t;\
                              fprintf(stderr, "creating %s at %p in %s:%d\n", #_t, _p, __FILE__, __LINE__);\
                              _p;\
                            })

#define DELETE(_c)          {fprintf(stderr, "deleting %s at %p in %s:%d\n", #_c, _c, __FILE__, __LINE__);\
                             delete _c;\
                             _c = NULL;}
#define TDELETE(_c)         {fprintf(stderr, "deleting[] %s at %p in %s:%d\n", #_c, _c, __FILE__, __LINE__);\
                             delete _c;\
                             _c = NULL;}
#else
#define NEW(_t)             new _t;
#define TNEW(_t)            new _t;
#define DELETE(_c)          { delete _c; _c = NULL;}
#define TDELETE(_c)          { delete[] _c; _c = NULL;}
#endif

#endif /* __SWARM_MACROS_H__ */
