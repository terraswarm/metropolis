# Metropolis test_new test makefile
#
# @Version: $Id: makefile,v 1.9 2004/08/30 18:47:02 cxh Exp $
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
ME =		src/metropolis/metamodel/backends/systemc/test

# Root of the Metropolis directory
ROOT =		../../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)



JACL_TESTS = \
	CompileUnit.tcl \
	HeaderCodegen.tcl \
	MakefileCodegen.tcl \
	VarAttribute.tcl

EXTRA_SRCS = $(JACL_TESTS) testDefs.tcl flags.mk

tests:: alljtests.tcl
	$(JTCLSH) alljtests.tcl

# We include flags.mk so that we can test MakefileCodegen
# This include must appear after tests:: so that the rule that
# is run in the default when we do 'make' is tests:: and not the
# rule in flags.mk
include flags.mk

# 'make all' should not run the tests automatically
all:
	@echo "To run the tests, run 'make', or 'make tests'"

# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h systemc_sim.mk bkpts.cmd .trace .constr* *.check

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
