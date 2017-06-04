# Makefile for examples/pip/map
#
# @Version: $Id: makefile,v 1.10 2004/10/12 22:34:43 bldmastr Exp $
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

ME =		examples/pip/map

# Root of the Metropolis directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
DIRS =

MMM_SRCS = \
	mapper.mmm

# Source Java files
JSRCS =

# Extra rules that should be generated.  'make fast' creates this file
EXTRA_TARGETS =  sc_main_map.cpp

EXTRA_SRCS = $(MMM_SRCS)

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	README.txt

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =	doc

all: $(EXTRA_SRCS) $(EXTRA_TARGETS)
	make -f systemc_sim.mk

install: all

# AUXCLASSPATH is used by the code coverage tools
sc_main_map.cpp: $(METRO)/lib/metamodel/plt/yapitemplate/yapidatatypes.mmm \
		$(METRO)/lib/metamodel/plt/yapitemplate/yapi.mmm  \
		$(METRO)/lib/metamodel/plt/TTLtemplate/TTL.mmm \
		$(METRO)/lib/metamodel/plt/TTLtemplate/yapi2TTL.mmm \
		$(METRO)/lib/metamodel/plt/TTLtemplate/yapirefTTL.mmm \
		$(METRO)/lib/metamodel/plt/storage/GenericStorage.mmm \
		mapper.mmm 
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		JAVAFLAGS=-Xmx256m \
		$(METRO)/bin/systemc $(DEBUG) $(MMDEBUG) \
		-top pip.map.Mapper -classpath ../.. \
		-w -noic mapper.mmm \
		$(METRO)/lib/metamodel/plt/yapitemplate/yapidatatypes.mmm \
		$(METRO)/lib/metamodel/plt/yapitemplate/yapi.mmm  \
		$(METRO)/lib/metamodel/plt/TTLtemplate/TTL.mmm \
		$(METRO)/lib/metamodel/plt/TTLtemplate/yapi2TTL.mmm \
		$(METRO)/lib/metamodel/plt/TTLtemplate/yapirefTTL.mmm \
		$(METRO)/lib/metamodel/plt/storage/GenericStorage.mmm


# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h systemc_sim.mk bkpts.cmd \
	$(METRO)/lib/metamodel/plt/yapitemplate/*.cpp \
	$(METRO)/lib/metamodel/plt/yapitemplate/*.h \
	$(METRO)/lib/metamodel/plt/yapitemplate/.*.ast \
	$(METRO)/lib/metamodel/plt/TTLtemplate/.*.ast \
	$(METRO)/lib/metamodel/plt/TTLtemplate/*.cpp \
	$(METRO)/lib/metamodel/plt/TTLtemplate/*.h 


# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

