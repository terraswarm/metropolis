# Makefile for metamodel/lib/plt/TTLtemplate directory
#
# @Version: $Id: makefile,v 1.2 2004/09/08 01:33:48 cxh Exp $
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

ME =		lib/metamodel/plt/TTLtemplate

# Root of the Metropolis directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
DIRS =

# Source Meta Model files 
MMM_SRCS = \
	TTL.mmm \
	yapi2TTL.mmm \
	yapirefTTL.mmm

# Extra rules that should be generated
EXTRA_TARGETS =  

EXTRA_SRCS = $(MMM_SRCS)

JCLASS = $(JSRCS:%.java=%.class)

# make clean removes $(KRUFT) 
KRUFT = *.cpp *.h .*.ast

all: suball $(EXTRA_SRCS) $(EXTRA_TARGETS)

install: all subinstall

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

