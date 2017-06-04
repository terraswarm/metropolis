# Makefile for src/util/locgen/examples directory
#
# @Version: $Id: makefile,v 1.16 2004/09/14 14:59:14 cxh Exp $
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

ME =		src/util/locgen/examples

# Root of the Metropolis directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
DIRS =	test

# Source Java files
JSRCS =

# Extra rules that should be generated
EXTRA_SRCS = fir_violation.loc fir_no_violation.loc equal_performance.loc

EXTRA_TARGETS =  $(EXTRA_SRCS:%.loc=%.check)

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	README.txt \
	fir_trace.trc \
	np1.trc \
	np2.trc \
	testFile.loc

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	test \
	fir_no_violation.cc \
	fir_violation.cc

all: suball $(EXTRA_SRCS) ../locgen $(EXTRA_TARGETS)
	@echo "For a demo, see either README.txt or run 'make demo'"

install: all subinstall

../locgen:
	(cd ..; $(MAKE) locgen)

%.check: %.cc
	$(METRO_CXX) $(CXX_FLAGS) -g -Wall -Wno-deprecated -o $@ $^

%.cc: %.loc
	../locgen $^ $@

demo:
	@echo "fir_violation.loc has a violation"
	@echo "fir_violation.loc contains the following lines"
	@cat fir_violation.loc
	@echo ""
	@echo "The run of fir_violation.check produces the following"
	fir_violation.check fir_trace.trc

	@echo "###############################################"
	@echo "fir_no_violation.loc does not have a violation"
	@echo "fir_no_violation.loc contains the following lines"
	@cat fir_no_violation.loc
	@echo ""
	@echo "The difference between fir_violation.loc and fir_no_violation.loc is:"
	-diff fir_violation.loc fir_no_violation.loc
	@echo ""
	@echo "The run of fir_no_violation.check produces the following"
	fir_no_violation.check fir_trace.trc

	@echo "###############################################"
	@echo "Test using two trace files with one formula"
	./equal_performance.check np1.trc np2.trc

# The .PRECIOUS dependency keeps the .cc files around for gcov
.PRECIOUS: fir_no_violation.cc fir_violation.cc

# make clean removes $(KRUFT)
KRUFT = *~ *o *.cc *.check

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

