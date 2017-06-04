# Metropolis makefile for arm_sim
#
# @Version: $Id: makefile,v 1.5 2004/09/07 16:04:09 cxh Exp $
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
ME =		examples/yapi_cpus/arm/arm_sim

# Root of Metro directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =

C_SRCS = \
	armcopro.c \
	arminit.c \
	armos.c \
	armsupp.c \
	armvirt.c \
	main.c \
	thumbemu.c \

# Source files that do not have directly corresponding .o files
# armemu.c is compiled to create armemu-32.0 and armemu-26.0
OTHER_C_SRCS = \
	armemu.c

H_SRCS = $(CPP_SRCS:%.cpp=%.h) \
	ansidecl.h \
	armdefs.h \
	armemu.h \
	armopts.h \
	armos.h \
	dbg_rdi.h

OBJS = $(C_SRCS:%.c=%.o) \
	armemu-32.o \
	armemu-26.o

EXTRA_SRCS = $(C_SRCS) $(OTHER_C_SRCS) $(H_SRCS) \
	COPYING \
	ChangeLog \
	ChangeLog.RedHat \
	README.Cygnus \
	README_Metropolis.txt

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

C_MAKEFILE_FLAGS = -DMODET -DNOOS

EXTRA_TARGETS = arm-sim

all: $(EXTRA_TARGETS)

install: all
	cp $(EXTRA_TARGETS) $(ROOT)/bin

arm-sim: $(OBJS) 
	$(METRO_CC) $^ -lbfd -liberty -o $@

armemu-32.o: armemu.c $(HFILES)
	$(METRO_CC) $(C_FLAGS) -c -DMODE32 armemu.c -o $@

armemu-26.o: armemu.c $(HFILES)
	$(METRO_CC) $(C_FLAGS) -c -DMODE26 armemu.c -o $@

# make clean removes $(KRUFT) 
KRUFT = $(EXTRA_TARGETS)

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
