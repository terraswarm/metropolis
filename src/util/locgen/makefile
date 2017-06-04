# Makefile for src/util/locgen directory
#
# @Version: $Id: makefile,v 1.8 2004/09/13 15:44:31 cxh Exp $
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

ME =		src/util/locgen

# Root of the Metropolis directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
DIRS = 		examples

# Source Java files
JSRCS =

# Extra rules that should be generated
EXTRA_TARGETS =  locgen

JCLASS = $(JSRCS:%.java=%.class)

PARSER_TAB = parser.tab.c parser.tab.h

# Disable -O2 optimization, it is too slow
CXX_OPTIMIZER = -g

# Give more verbose error messages
CXX_MAKEFILE_FLAGS = -DYYERROR_VERBOSE

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	README.txt

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	lex.yy.c \
	lex.yy.o \
	loc.flex \
	locgen \
	parser.tab.c \
	parser.tab.h \
	parser.tab.o \
	parser.y \
	term_type.h

all: suball $(EXTRA_SRCS) $(EXTRA_TARGETS)

install: all subinstall
	cp $(EXTRA_TARGETS) $(ROOT)/bin

locgen: lex.yy.o parser.tab.o
	$(METRO_CXX) $(CXX_FLAGS) -o locgen lex.yy.o parser.tab.o


lex.yy.o: lex.yy.c parser.tab.h term_type.h
	$(METRO_CXX) $(CXX_FLAGS) -Wall -c -o lex.yy.o lex.yy.c

parser.tab.o: parser.tab.c term_type.h
	$(METRO_CXX) $(CXX_FLAGS) -Wall -c -o parser.tab.o parser.tab.c

$(PARSER_TAB): parser.y
	$(METRO_BISON) -d parser.y

lex.yy.c: loc.flex
	$(METRO_FLEX) loc.flex

KRUFT= locgen $(PARSER_TAB) lex.yy.c

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

