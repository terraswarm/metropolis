# Metropolis metamodel test makefile
#
# @Version: $Id: makefile,v 1.26 2004/11/22 16:54:23 cxh Exp $
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
ME =		src/metropolis/metamodel/test

# Root of the Metropolis directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

JSRCS = \
        TestDecl.java \
	TestSingletonNode.java \
	TestStringNode.java \
	TestTreeNode.java \
	TestTwoListNode.java \
	TestVisitor.java

JACL_TESTS = \
	ChildList.tcl \
	Compiler.tcl \
	Decl.tcl \
	Effect.tcl \
	PropertyMap.tcl \
	MetaModelVisitor.tcl \
	Modifier.tcl \
	ModifierAuto.tcl \
	NullValue.tcl \
	TNLManip.tcl \
	TrackedPropertyMap.tcl \
	TreeNode.tcl \
	Pip.tcl \
	Scope.tcl \
	ScopeIter.tcl \
	StringManip.tcl

JCLASS = $(JSRCS:%.java=%.class)

EXTRA_SRCS = $(JSRCS) $(JACL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	alljtests.tcl \
	makeModifierAuto.tcl \
	testDefs.tcl \
	NullValue.serial

# Set METRO_CLASSPATH and METROBASE so we can compile PiP
tests:: alljtests.tcl jclass ModifierAuto.tcl
	METRO_CLASSPATH=`cd $(ROOT)/../metroworkspace/examples; pwd` \
	METROBASE=$(ROOT)/.. $(JTCLSH) alljtests.tcl

# 'make all' should not run the tests automatically
all:
	@echo "To run the tests, run 'make', or 'make tests'"

install: all

# Run "make jtclsh" to get a prompt and run the tests by hand
jtclsh:
	$(JTCLSH)

ModifierAuto.tcl: makeModifierAuto.tcl makefile
	CLASSPATH=$(AUXCLASSPATH) $(ROOT)/bin/metroshell -q -ni \
		makeModifierAuto.tcl | \
		grep -v 'Warning' | grep -v 'is discouraged' > ModifierAuto.tcl

# 'make clean' removes KRUFT
KRUFT =	NullValue.serial

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
