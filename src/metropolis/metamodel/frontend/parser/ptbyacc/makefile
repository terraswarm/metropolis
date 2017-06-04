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
#
# 						METROPOLIS_COPYRIGHT_VERSION_1
# 						COPYRIGHTENDKEY
#
#################################################
## Makefile for Byacc/Java
## Date: 01 Oct 97
## Author: Bob Jamison
## (Metropolis version: $Id: makefile,v 1.13 2004/08/30 18:55:03 cxh Exp $ )
#################################################

# Uncomment for debug output:
# PTBYACC_DEBUG_OPT = -tv

ME =		src/metropolis/metamodel/frontend/parser/ptbyacc

# Root of the Metropolis directory
ROOT =		../../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

CC     = $(METRO_CC)

CSRCS   = closure.c \
	error.c \
	lalr.c \
	lr0.c \
	main.c \
	mkpar.c \
	output.c \
	reader.c \
	skeleton.c \
	symtab.c \
	verbose.c \
	warshall.c

OBJS = $(CSRCS:.c=.o)

SRCS =	$(CSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	README.txt \
	byacc.ide \
	defs.h

# Extra rules that should be generated
EXTRA_TARGETS = ptbyacc 

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(EXTRA_TARGETS)

all: $(EXTRA_TARGETS)
install: all
jclass: all

# We rename the binary to ptbyacc so as to differentiate
# it from the regular yacc and byacc
$(EXTRA_TARGETS): $(OBJS)
	$(METRO_CC) -o $@ $(OBJS)
	@echo "Run 'make update_parser' to update the files in"
	@echo "metamodel/frontend/parser."

# This command will overwrite the following files in
# metropolis/metamodel/frontend/parser/ptolemy
# 	MetaModelParser.java
# 	MetaModelParserval.java
#	*.tbl
update_parser: $(EXTRA_TARGETS)
	(cd ".."; \
		"ptbyacc/ptbyacc" -j \
		$(PTBYACC_DEBUG_OPT) \
		-p metropolis.metamodel.frontend.parser \
                -f MetaModelParser Grammar.y ;  \
		rm -f *.bin; \
		$(MAKE) MetaModelParser.class)
	@echo "After running this rule, you might want to update"
	@echo "the .bin files in metropolis/metamodel/frontend/parser"
	@echo "by running"
	@echo "   (cd ..; make update_tables)"
	@echo "   (cd ..; cvs commit -m \"Modified byacc\")"


###
closure.o: closure.c defs.h
error.o: error.c defs.h
lalr.o: lalr.c defs.h
lr0.o: lr0.c defs.h
main.o: main.c defs.h
mkpar.o: mkpar.c defs.h
output.o: output.c defs.h
reader.o: reader.c defs.h
skeleton.o: skeleton.c defs.h
symtab.o: symtab.c defs.h
verbose.o: verbose.c defs.h
warshall.o: warshall.c defs.h

# make clean removes $(KRUFT) 
KRUFT = $(EXTRA_TARGETS)

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
