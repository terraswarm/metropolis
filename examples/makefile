# Metropolis makefile for examples
#
# @Version: $Id: makefile,v 1.23 2005/03/14 20:32:49 cxh Exp $
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
ME =		examples

# Subdirectories that contain source code and makefiles
# In general, keep these alphabetical.  However, pip relies on other
# directories, so it is last.
DIRS = \
	builtinloc \
	constraint_elaboration \
	dining_phil \
	dining_phil_live \
	gtime \
	loc_checker \
	mgdb \
	plt \
	producers_consumer \
	promela \
	runtime \
	simpleArchitecture \
	simpleMapping \
	sync \
	template_resolution \
	yapi_cpus \
	pip


# These tests work with an older version of the simulator
# If you rename $(METRO)/src/metropolis/metamodel/backends/systemc_v0
# $(METRO)/src/metropolis/metamodel/backends/systemc
# and build, then these tests might work. 
SYSTEMC_V0_DIRS = \
	test \
	test_util

OTHER_DIRS = \
	javasim \
	prometheus \

# Root of Metro directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)


# Source Java files
JSRCS    = 

# Extra rules that should be generated
EXTRA_TARGETS =

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

all: suball
install: all subinstall

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
