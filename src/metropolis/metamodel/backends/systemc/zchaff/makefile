# Makefile for the Metropolis metamodel frontend lexer
#
# @Version: $Id: makefile,v 1.13 2005/02/18 16:46:33 cxh Exp $
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2003-2004 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software and that appropriate acknowledgments are made
# to the research of the Metropolis group.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						METRO_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		src/metropolis/metamodel/backends/systemc/util

# Root of the Metropolis directory
ROOT =		../../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =

# Under Solaris, in $METRO/mk/metro.mk, $METRO/config defines
# $RAND_MAX_FLAG to be equal to -D_NEED_REDEFINE_RAND_MAX_
# Thus, we avoid needing GNU make here
CXX_MAKEFILE_FLAGS = $(RAND_MAX_FLAG)

.SUFFIXES: .o .cpp 

HEADERS = \
	zchaff_base.h \
	zchaff_clsgen.h \
	zchaff_header.h \
	zchaff_dbase.h \
	zchaff_solver.h

SOLVER_SRCS = sat_solver.cpp 
SOLVER_OBJS = $(SOLVER_SRCS:.cpp=.o)

LIB_SRCS =  zchaff_utils.cpp \
	    zchaff_solver.cpp\
	    zchaff_base.cpp \
	    zchaff_dbase.cpp \
	    zchaff_c_wrapper.cpp \
	    zchaff_cpp_wrapper.cpp \

LIB_OBJS = $(LIB_SRCS:.cpp=.o)

# 'make fast' builds $(METRO_LIBS)
METRO_LIBS = libsat.a

CCSRCS = $(LIB_SRCS) $(SOLVER_SRCS) \
	zminimal.cpp

# Some .cpp files do not compile under Cygwin/gcc-3.3.1
SRCS = $(CCSRCS) \
	zverify_bf.cpp \
	zverify_df.cpp \
	zcore_extract.cpp \
	cnf_stats.cpp

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	README.txt \
	SAT.h \
	$(HEADERS) \
	mystl_hash.h \
	run_till_fix \
	test1.cnf \
	test2.cnf \
	zchaff_parallel.cpp \
	zchaff_probe.cpp \
	zchaff_version.h \
	zchaff_wrapper.wrp \
	zchaff.dsp \
	zchaff.dsw

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(LIB_OBJS)

all: $(METRO_LIBS) zchaff zminimal
install: all
jclass: all

# These targets do not compile under Cygwin/gcc-3.3.1
other: zverify_bf zverify_df zcore cnf_stats

zchaff:   $(SOLVER_OBJS) libsat.a SAT_C.h
	  $(METRO_CXX) $(LINKFLAGS) $(C_FLAGS) $(SOLVER_OBJS) libsat.a -o zchaff 

zverify_bf: zverify_bf.cpp
	  $(METRO_CXX) $(LINKFLAGS) $(C_FLAGS) zverify_bf.cpp -o zverify_bf

zverify_df: zverify_df.cpp
	  $(METRO_CXX) $(LINKFLAGS) $(C_FLAGS) zverify_df.cpp -o zverify_df

zcore: zcore_extract.cpp
	  $(METRO_CXX) $(LINKFLAGS) $(C_FLAGS) zcore_extract.cpp -o zcore

zminimal: zminimal.o libsat.a
	  $(METRO_CXX) $(LINKFLAGS) $(C_FLAGS) zminimal.o libsat.a -o zminimal

cnf_stats: cnf_stats.o
	  $(METRO_CXX) $(LINKFLAGS) $(C_FLAGS) cnf_stats.o -o cnf_stats

zchaff_c_wrapper.cpp:	zchaff_wrapper.wrp
	sed 's/EXTERN/extern \"C\"/' zchaff_wrapper.wrp > zchaff_c_wrapper.cpp

zchaff_cpp_wrapper.cpp:	zchaff_wrapper.wrp
	sed 's/EXTERN//' zchaff_wrapper.wrp > zchaff_cpp_wrapper.cpp

SAT_C.h:
	sed 's/gid = 0/gid/' SAT.h > SAT_C.h

$(LIB_OBJS): $(HEADERS) makefile
libsat.a:   $(LIB_OBJS)
	@rm -f libsat.a
	$(METRO_AR) cr libsat.a $(LIB_OBJS)
	$(METRO_RANLIB) libsat.a

# 'make clean' will remove $(KRUFT)
KRUFT = libsat.a zchaff *wrapper.cpp zminimal zcore zverify_bf zverify_df cnf_stats SAT_C.h

zchaff_utils.o: zchaff_utils.cpp
zchaff_solver.o: zchaff_solver.cpp zchaff_solver.h zchaff_version.h \
  zchaff_dbase.h zchaff_base.h zchaff_header.h
zchaff_base.o: zchaff_base.cpp zchaff_base.h zchaff_header.h
zchaff_dbase.o: zchaff_dbase.cpp zchaff_dbase.h zchaff_base.h \
  zchaff_header.h
zchaff_c_wrapper.o: zchaff_c_wrapper.cpp zchaff_solver.h zchaff_version.h \
  zchaff_dbase.h zchaff_base.h zchaff_header.h zchaff_clsgen.h
zchaff_cpp_wrapper.o: zchaff_cpp_wrapper.cpp zchaff_solver.h \
  zchaff_version.h zchaff_dbase.h zchaff_base.h zchaff_header.h \
  zchaff_clsgen.h
sat_solver.o: sat_solver.cpp SAT.h
zverify_bf.o: zverify_bf.cpp mystl_hash.h
zverify_df.o: zverify_df.cpp
zcore_extract.o: zcore_extract.cpp mystl_hash.h
zminimal.o: zminimal.cpp SAT.h
cnf_stats.o: cnf_stats.cpp

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
