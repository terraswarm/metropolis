# Makefile for metamodel/lib/lang directory
#
# @Version: $Id: makefile,v 1.6 2005/03/14 20:52:21 cxh Exp $
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2004 The Regents of the University of California.
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

ME =		lib/metamodel/lang

# Root of the Metropolis directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
DIRS =

MMM_SRCS = \
	Action.mmm \
	Array.mmm \
	BuiltInLOCType.mmm \
	GlobalTimeManager.mmm \
	GlobalTime.mmm \
	GlobalTimeRequestClass.mmm \
	Interface.mmm \
	Math.mmm \
	Medium.mmm \
	Netlist.mmm \
	Node.mmm \
	Nondet.mmm \
	Object.mmm \
	ObjectType.mmm \
	Port.mmm \
	Process.mmm \
	QuantityManager.mmm \
	QuantityManagerLOC.mmm \
	Quantity.mmm \
	RequestClass.mmm \
	SchedHierarchyChild.mmm \
	SchedHierarchyParent.mmm \
	SchedProgramCounter.mmm \
	SchedStateVal.mmm \
	Scheduler.mmm \
	SchedulingNetlistIntfc.mmm \
	SchedulingNetlist.mmm \
	Scope.mmm \
	Sized.mmm \
	StateMedium.mmm \
	StateMediumProc.mmm \
	StateMediumSched.mmm \
	String.mmm

# Source Java files
JSRCS =

# Extra rules that should be generated
EXTRA_TARGETS =  

EXTRA_SRCS = $(MMM_SRCS)

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	doc

all: suball $(EXTRA_SRCS) $(EXTRA_TARGETS)

install: all subinstall

# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h *.pml systemc_sim.mk bkpts.cmd 

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

