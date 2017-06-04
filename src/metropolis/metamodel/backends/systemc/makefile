# Makefile for the Metropolis metamodel frontend lexer
#
# @Version: $Id: makefile,v 1.30 2004/10/18 19:30:29 guyang Exp $
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

# override CXX_OPTIMIZER += -ggdb

ME =		src/metropolis/metamodel/backends/systemc

# Root of the Metropolis directory
ROOT =		../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =	mmdebug ltl2ba-1.0 util zchaff test

JSRCS = \
	AutoLabel.java \
	CompileUnit.java \
	CompileUnitsCache.java \
	ExplicitCastVisitor.java \
	HeaderCodegen.java \
	MakefileCodegen.java \
	SystemCBackend.java \
	SystemCCodegenVisitor.java \
	ASTLookupVisitor.java \
	OneSynchEvent.java \
	SynchEventPool.java \
	VarAttribute.java \
	PortInterfaceCollectVisitor.java \
	TraverseStmtsVisitor.java \
	ICASymbolicFlag.java \
	ICAwaitOptimizeVisitor.java \
	ICAComputeVisitor.java \
	ICAwaitTestlistComputeVisitor.java \
    LTLCodegenVisitor.java

JCLASS = $(JSRCS:%.java=%.class)

LIB_SRCS = \
	DefaultBehavior.cpp \
	GlobalTime.cpp \
	GlobalTimeRequestClass.cpp \
	String.cpp \
	action.cpp \
	argsutil.cpp \
	behavior.cpp \
	buchiman.cpp \
	event.cpp \
	global.cpp \
	globaltimemanager.cpp \
	manager.cpp \
	medium.cpp \
	netlist.cpp \
	netlist_b.cpp \
	node.cpp \
	nondeterminism.cpp \
	object.cpp \
	port_rusage.cpp \
	portmap.cpp \
	process.cpp \
	programcounter.cpp \
	quantity.cpp \
	quantitymanager.cpp \
	requestclass.cpp \
	sat_hook.cpp \
	scheduler.cpp \
	schedulingnetlist.cpp \
	scoreboard.cpp \
	statemedium.cpp \
    ltlsynchimply.cpp

LIB_OBJS = $(LIB_SRCS:.cpp=.o)
LIB_HS = $(LIB_SRCS:.cpp=.h)

EXTRA_SRCS = \
	MetroString.h \
	README.txt \
	statemediumDeclaration.h \
	library.h \
	macros.h \
	systemc.dsp \
	systemc.dsw


# 'make fast' builds $(METRO_LIBS)
METRO_LIBS = liblibrary.a

CCSRCS = $(LIB_SRCS)

SRCS = 	$(CCSRCS) $(JSRCS) \
	algorithm.cpp

INC_DIR = .
BASE_DIR = .
LIBRARY_DIR = ../
CXX_INCLUDES = -I $(INC_DIR) -I $(SYSTEMC)/include -I $(LIBRARY_DIR)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	package.html

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(LIB_OBJS) \
	$(LIB_HS)

KRUFT = liblibrary.a

all: $(METRO_LIBS) jclass

install: all

liblibrary.a: $(LIB_OBJS) $(OPTIONAL_OBJS)
	$(METRO_AR) -rs liblibrary.a $(LIB_OBJS)

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
