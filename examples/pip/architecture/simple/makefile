# Metropolis makefile
#
# @Version: $Id: makefile,v 1.3 2004/09/08 04:54:42 cxh Exp $
#
# Copyright (c) 2003-2004 The Regents of the University of California.
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
ME =		$(METRO)/examples/pip/architecture/simple

# Root of Metro directory
ROOT =		$(METRO)

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =		test

MMM_SOURCES = \
	Architecture.mmm \
	ArchScheduledNetlist.mmm \
	ArchSchedulingNetlist.mmm \
	Bus.mmm \
	CpuRtos.mmm \
	InterfaceSched.mmm \
	InterfaceSchedReq.mmm \
	InterfaceScheduling.mmm \
	Mem.mmm \
	MyScheduler.mmm \
	ProcessAccount.mmm \
	ProcessRecord.mmm \
	SchedulerFIFO.mmm \
	SchedulerTimeSliceBased.mmm \
	SwTask.mmm \
	Top.mmm

EXTRA_SRCS = $(MMM_SOURCES)

METRO_CLASSPATH = $(ME)/..

# Run 'make DEBUG=-debug' to debug the Metropolis frontend.
# See $METRO/doc/coding/debugging.htm 
# DEBUG = -debug

# Run 'make MMDEBUG=-mmdebug' to debug the metamodel.
# MMDEBUG = -mmdebug

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

all: sc_main.cpp
	make -f systemc_sim.mk

install: all

mmdebug: clean FORCE
	make MMDEBUG=-mmdebug all

# AUXCLASSPATH is used by the code coverage tools
sc_main.cpp: $(MMM_SOURCES)
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		$(ROOT)/bin/systemc $(DEBUG) $(MMDEBUG) \
		    -top pip.architecture.simple.TopNet \
		    -classpath $(METRO)/examples \
		    $(MMM_SOURCES)	

system.cpp : system.mmm
	$(ROOT)/bin/systemc system.mmm

elaborate:
	$(METRO)/bin/metacomp -elaborator pip.architecture.simple.TopNet Top.mmm 


# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h systemc_sim.mk bkpts.cmd

#test: clean all
#	@./run.x
#	$(MAKE) clean

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

FORCE:
