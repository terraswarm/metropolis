# Metropolis makefile
#
# @Version: $Id: makefile,v 1.7 2005/11/28 18:08:26 allenh Exp $
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
ME =		examples/loc_checker

# Root of Metro directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =		test

# Configure only sets METRO_LOCGEN_DIR if bison & flex are found.
# Tests require flex.
ifndef METRO_LOCGEN_DIR
SKIP_TESTS = skip
endif

MMM_SRCS = \
	M.mmm \
	Reader.mmm \
	system.mmm \
	Writer.mmm \
	XX.mmm

EXTRA_SRCS = $(MMM_SRCS)

CHECKERS = $(patsubst .%.loc, %.check, $(wildcard .*.loc))

# Run 'make DEBUG=-debug' to debug the Metropolis frontend.
# See $METRO/doc/coding/debugging.htm 
# DEBUG = -debug

# Run 'make MMDEBUG=-mmdebug' to debug the metamodel.
# MMDEBUG = -mmdebug

LOCGEN = $(ROOT)/bin/locgen

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	README.fig \
	README.ps \
	README.txt

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \

all: sc_main.cpp systemc_sim.mk
	make -f systemc_sim.mk
	@echo "To run the demo, run \"make demo\"" 

checker: $(LOCGEN) $(CHECKERS)

%.check: .%.cc
	g++ -g -Wall -Wno-deprecated -o $@ $^

.%.cc: .%.loc
	$(LOCGEN) $^ $@

install: all

elaborate:
	METRO_CLASSPATH=$(METRO)/examples \
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		$(METRO)/bin/metacomp -classpath .. \
			-runtimetest constraint loc_checker.IwIr Reader.mmm Writer.mmm M.mmm XX.mmm system.mmm

# Run the entire demo
demo: all run_runx checker run_checkers elaborate

run_runx:
	./run.x

run_checkers:
	./constr0.check .trace
	./constr1.check .trace
	./constr2.check .trace
	./constr3.check .trace

$(LOCGEN):
	if [ ! -f $(LOCGEN) ]; then \
	    (cd $(ROOT)/src/util/locgen; make install); \
	fi
mmdebug: clean FORCE
	make MMDEBUG=-mmdebug all

# AUXCLASSPATH is used by the code coverage tools
sc_main.cpp systemc_sim.mk: 
	METRO_CLASSPATH=$(METRO)/examples \
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		$(ROOT)/bin/systemc $(DEBUG) $(MMDEBUG) \
		    -top loc_checker.IwIr Reader.mmm Writer.mmm M.mmm XX.mmm system.mmm

system.cpp: system.mmm
	METRO_CLASSPATH=$(METRO)/examples \
		$(ROOT)/bin/systemc system.mmm

# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h systemc_sim.mk bkpts.cmd .trace .constr* *.check

#test: clean all
#	@./run.x
#	$(MAKE) clean

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

FORCE:
