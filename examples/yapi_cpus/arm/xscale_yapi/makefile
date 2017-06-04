# Metropolis makefile
#
# @Version: $Id: makefile,v 1.7 2004/09/14 01:29:40 cxh Exp $
#
# Copyright (c) 2004 The Regents of the University of California.
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
#						METROPOLIS_COPYRIGHT_VERSION_2
#						COPYRIGHTENDKEY
##########################################################################

# Current directory relative to $METRO
ME =		examples/yapi_cpus/arm/xscale_yapi

# Root of Metro directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =		test

CPP_SRCS = \
	alu.cpp \
	disarm.cpp \
	swarm.cpp \
	trace.cpp \
	tracesim.cpp

H_SRCS = $(CPP_SRCS:%.cpp=%.h) \
	isa_be.h \
	isa.h \
	isa_le.h \
	swarm_macros.h \
	swarm_types.h

OBJS = $(CPP_SRCS:%.cpp=%.o)

# MyDataTypes.mmm needs to be first so we can find Instruction
MMM_SRCS = \
	MyDataTypes.mmm \
	Exec.mmm \
	Fetch.mmm \
	XScaleExec.mmm \
	XScaleFetch.mmm \
	xscale_netlist.mmm

EXTRA_SRCS = $(MMM_SRCS) $(CPP_SRCS) $(H_SRCS) \
	mmscs_xscale.mk \
	README.txt

# Run 'make DEBUG=-debug' to debug the Metropolis frontend.
# See $METRO/doc/coding/debugging.htm 
# DEBUG = -debug

# Run 'make MMDEBUG=-mmdebug' to debug the metamodel.
# Or, run 'make debug'.
# MMDEBUG = -mmdebug

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

all: xscale.x demo

install: all

xscale.x: sc_main.cpp $(OBJS)
	$(MAKE) -f mmscs_xscale.mk

demo: xscale.x
	./xscale.x ../object_files/arith2.tr
	./xscale.x ../object_files/arith-core.tr
	./xscale.x ../object_files/arith.tr
	./xscale.x ../object_files/fib-core.tr
	./xscale.x ../object_files/fib_mem.tr
	./xscale.x ../object_files/fib.tr
	./xscale.x ../object_files/mults-core.tr
	./xscale.x ../object_files/test1.tr

debug: clean
	$(MAKE) CXX_OPTIMIZER=-g C_OPTIMIZER=-g \
		CXX_MAKEFILE_FLAGS=-DCPU_DEBUG \
		xscale-debug.x

xscale-debug.x: sc_main.cpp $(OBJS)
	$(MAKE) CXX_OPTIMIZER=-g C_OPTIMIZER=-g \
		CXX_MAKEFILE_FLAGS=-DCPU_DEBUG \
		-f mmscs_xscale.mk
	mv xscale.x $@

mmdebug: clean
	$(MAKE)  MMDEBUG=-mmdebug all


alu.o: $(SBASIC) alu.cpp alu.h
	$(METRO_CXX) $(CXX_FLAGS) -c alu.cpp

disarm.o: $(SBASIC) disarm.h disarm.cpp
	$(METRO_CXX) $(CXX_FLAGS) -c disarm.cpp

tracesim.o: $(SBASIC) tracesim.cpp tracesim.h trace.h
	$(METRO_CXX) $(CXX_FLAGS) -c tracesim.cpp

swarm.o: $(SBASIC) swarm.cpp
	$(METRO_CXX) $(CXX_FLAGS) -c swarm.cpp

trace.o: $(SBASIC) trace.cpp trace.h
	$(METRO_CXX) $(CXX_FLAGS) -c trace.cpp

sc_main.cpp: 
	$(METACOMP) -systemc -top xscale_netlist $(MMDEBUG) \
		$(MMM_SRCS)


# 'make clean' removes KRUFT
KRUFT =	all.*  xscale*.x *Fetch.cpp *Fetch.h *Exec.cpp *Exec.h \
	xscale_netlist.cpp xscale_netlist.h MyDataTypes.cpp \
	sc_main.cpp MyDataTypes.h core gmon.out *.tmp .trace .*.ast \
	systemc_sim.mk

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
