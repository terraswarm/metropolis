# Makefile for the Metropolis metamodel frontend lexer
#
# @Version: $Id: makefile,v 1.15 2004/09/14 14:39:08 cxh Exp $
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

ME =		src/metropolis/metamodel/backends

# Root of the Metropolis directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
# elaborator before javasim because JavasimBackend extends ElaboratorBackEnd
# $(METRO_JAVASIM) is set to "javasim" in $METRO/mk/metro.mk by configure if 
# src/metropolis/metamodel/backends/javasim/ exists
DIRS = \
	metamodel \
	simulator \
	elaborator \
	$(METRO_JAVASIM) \
	systemc \
	cpp \
	promela \
	cfa \
	runtimetest
#	   prometheus

# Source Java files
JSRCS    =	Backend.java

# Extra rules that should be generated
EXTRA_TARGETS =  

JCLASS = $(JSRCS:%.java=%.class)

EXTRA_SRCS = $(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	metamodel \
	elaborator \
	$(METRO_JAVASIM) \
	systemc \
	promela \
	runtimetest \
	README.txt \
	mgdb \
	package.html

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	cfa \
	cpp \
	doc \
	metamodel_backend_howto.ppt \
	simulator \

all: suball $(EXTRA_SRCS) $(EXTRA_TARGETS)

install: subinstall

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
