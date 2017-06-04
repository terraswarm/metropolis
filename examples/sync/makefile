# Metropolis makefile
#
# @Version: $Id: makefile,v 1.9 2004/08/29 09:25:36 guyang Exp $
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
ME =		examples/sync

# Root of Metro directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =		test

MMM_SRCS = \
	anet.mmm \
	architecture.mmm \
	behavior.mmm \
	bnet.mmm \
	wnet.mmm

EXTRA_SRCS = $(MMM_SRCS)

# Run 'make DEBUG=-debug' to debug the Metropolis frontend.
# See $METRO/doc/coding/debugging.htm 
# DEBUG = -debug

# Run 'make MMDEBUG=-mmdebug' to debug the metamodel.
# MMDEBUG = -mmdebug

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	README.txt

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

all: sc_main.cpp systemc_sim.mk
	make -f systemc_sim.mk

install: all

mmdebug: clean FORCE
	make MMDEBUG=-mmdebug all

undebug: FORCE
	make -f systemc_sim.mk unsubstitute

# AUXCLASSPATH is used by the code coverage tools
sc_main.cpp systemc_sim.mk: 
	METRO_CLASSPATH=$(METRO)/examples \
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		$(ROOT)/bin/systemc $(DEBUG) $(MMDEBUG) \
		    -top sync.whole -w\
				anet.mmm architecture.mmm behavior.mmm bnet.mmm wnet.mmm

# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h systemc_sim.mk bkpts.cmd

#test: clean all
#	@./run.x
#	$(MAKE) clean

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

FORCE:
