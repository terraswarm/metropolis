# Makefile for the Metropolis metamodel frontend lexer
#
# @Version: $Id: makefile,v 1.11 2004/09/13 15:44:59 cxh Exp $
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2003-2004 The Regents of the University of California.
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

ME =		src/metropolis/metamodel/backends/systemc/util

# Root of the Metropolis directory
ROOT =		../../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =

LIB_SRCS = \
	ArrayList.cpp \
	HashMap.cpp \
	LinkedList.cpp \
	Math.cpp

LIB_OBJS = $(LIB_SRCS:.cpp=.o)

# 'make fast' builds $(METRO_LIBS)
METRO_LIBS = libutil.a

CCSRCS = $(LIB_SRCS)

SRCS =	$(CCSRCS)

INC_DIR = .
BASE_DIR = .
LIBRARY_DIR = ../
CXX_INCLUDES = -I $(INC_DIR) -I $(SYSTEMC)/include -I $(LIBRARY_DIR)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	ArrayList.h \
	HashMap.h \
	LinkedList.h \
	MetroMath.h \
	Util.h \
	util.dsp \
	util.dsw

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

all: $(METRO_LIBS)
install: all
jclass: all

libutil.a: $(LIB_OBJS)
	$(AR) -rs libutil.a $(LIB_OBJS)
	$(METRO_RANLIB) libutil.a

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
