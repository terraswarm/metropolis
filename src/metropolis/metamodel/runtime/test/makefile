# Metropolis src/metropolis/metamodel/runtime/test makefile
#
# @Version: $Id: makefile,v 1.21 2005/01/12 16:15:16 cxh Exp $
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
ME =		src/metropolis/metamodel/runtime/test/makefile

# Root of the Metropolis directory
ROOT =		../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

JSRCS = NetworkTest.java \
	NetworkTestChild.java

JCLASS = $(JSRCS:%.java=%.class)

JACL_TESTS = \
	BuiltInLOC.tcl \
	Connection.tcl \
	Constraint.tcl \
	EqualVars.tcl \
	Event.tcl \
	INetlist.tcl \
	INode.tcl \
	IPortArray.tcl \
	IPortElem.tcl \
	LTLSynch.tcl \
	LTLSynchImply.tcl \
	MMPort.tcl \
	MMType.tcl \
	Network.tcl

OTHER_SRCS = \
	makeINode.tcl \
	runtime.tcl \
	testDefs.tcl

EXTRA_SRCS = $(JACL_TESTS) $(OTHER_SRCS)

# Set CLASSPATH to include the JavaScope zip file
tests:: jclass alljtests.tcl
	rm -rf metro*.tmp
	$(METROSHELL) alljtests.tcl

# 'make all' should not run the tests automatically
all: jclass
	@echo "To run the tests, run 'make', or 'make tests'"

KRUFT = *.tmp

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
