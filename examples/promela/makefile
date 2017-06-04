# Metropolis makefile
#
# @Version: $Id: makefile,v 1.8 2004/05/27 19:32:34 cxh Exp $
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
ME =		examples/promela

# Root of Metro directory
ROOT =		../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =		test

MMM_SRCS = \
	prodcons_ttl.mmm \
	SimpleByte1.mmm \
	SimpleByte2.mmm \
	SimpleByte3.mmm \
	SimpleByte.mmm

LTL_SRCS = \
	prodcons_ttl.pml.ltl \
	SimpleByte1.pml.ltl \
	SimpleByte2.pml.ltl \
	SimpleByte3.pml.ltl

EXTRA_SRCS = $(MMM_SRCS) $(LTL_SRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	README.txt

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

# $(METACOMP) is defined in $METRO/mk/metro.mk
all:
	$(METACOMP) -classpath .. -promela SimpleByte.mmm
	$(METACOMP) -classpath .. -promela SimpleByte1.mmm
	$(METACOMP) -classpath .. -promela SimpleByte2.mmm
	$(METACOMP) -classpath .. -promela SimpleByte3.mmm
	$(METACOMP) -classpath .. -promela prodcons_ttl.mmm
	  
install: 
	@echo "Nothing to be installed here"

jclass: all
# 'make clean' will remove $(KRUFT)
KRUFT = *tmp *.pml .*.ast *~ pan*

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
