# Metropolis shell tests makefile
#
# @Version: $Id: makefile,v 1.14 2004/06/11 01:09:35 cxh Exp $
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
ME =		src/metropolis/metamodel/shell/test

# Root of the Metropolis directory
ROOT =		../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

JACL_TESTS = \
	ClasspathCmd.tcl \
	ElaborateCmd.tcl \
	HelpCmd.tcl \
	MetroListCmd.tcl \
	MetroLoadCmd.tcl \
	NetworkCmd.tcl \
	Shell.tcl \
	SimulateCmd.tcl

EXTRA_SRCS = $(JACL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	testDefs.tcl

# Set CLASSPATH to include the JavaScope zip file
tests:: alljtests.tcl $(HOME)/.metroshrc interactiveTest
	rm -rf metro*.tmp
	CLASSPATH=$(AUXCLASSPATH) $(ROOT)/bin/metroshell alljtests.tcl

# 'make all' should not run the tests automatically
all:
	@echo "To run the tests, run 'make', or 'make tests'"

$(HOME)/.metroshrc:
	REALHOME=`echo "puts \"[java::call System getProperty user.home]\"; exit" | ${METRO}/bin/metroshell | tail -1 | awk '{printf("%s", $$2); for (i=3;i<=NF;i++) printf(" %s", $$i)}'`; \
	echo "Creating $$REALHOME/.metroshrc"; \
	echo "set sawHomeDirMetroshrc 1" > "$$REALHOME"/.metroshrc

# This test increases the code coverage
interactiveTest:
	echo "puts \"Interactive Test\"; exit" | \
			CLASSPATH=$(AUXCLASSPATH) $(ROOT)/bin/metroshell

# 'make clean' removed $(KRUFT)
KRUFT = *.tmp sc_main.cpp systemc_sim.mk

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
