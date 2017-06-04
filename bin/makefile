# Makefile for scripts that invoke Metropolis Java classes
#
# @Author: Christopher Brooks (Makefile only)
#
# @Version: $Id: makefile,v 1.16 2004/09/14 02:01:02 cxh Exp $
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		bin

DIRS =

# Root of the Java directory
ROOT =		..


# Get configuration info
CONFIG =	$(METRO)/mk/metro.mk
include $(CONFIG)

# Bourne Shell scripts for Unix and Cygwin
# $(METRO_JAVASIM) is set to "javasim" in $METRO/mk/metro.mk by configure if 
# src/metropolis/metamodel/backends/javasim/ exists
SHELL_FILES = \
	$(METRO_JAVASIM) \
	mddd \
	metacomp \
	mgdb \
	metroshell \
	prometheus \
	simulator \
	systemc

EXTRA_SRCS = \
	metroinvoke.in

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = $(SHELL_FILES) \
	locgen \
	metroinvoke

EXTRA_TARGETS = metroinvoke mgdb mddd $(METRO_JAVASIM)

all: $(EXTRA_TARGETS)
	-chmod a+x metroinvoke
	rm -f metacomp
	ln -s metroinvoke metacomp
	rm -f metroshell
	ln -s metroinvoke metroshell
	rm -f prometheus
	ln -s metroinvoke prometheus
	rm -f simulator
	ln -s metroinvoke simulator
	rm -f systemc
	ln -s metroinvoke systemc

install: all
	-chmod a+x $(SHELL_FILES)

fast: install
demo: all

metroinvoke: metroinvoke.in ../config.status
	(cd ..; ./config.status)

# javasim sources are not shipped in the release.
javasim: metroinvoke
	rm -f $@
	ln -s metroinvoke $@

mgdb:
	rm -f $@
	ln -s ../util/mgdb/$@ .

mddd:
	rm -f $@
	ln -s mgdb $@

# zip might not get the permissions of configure right, so we run /bin/sh
../config.status: ../configure
	(cd ..; /bin/sh ./configure)

# 'make clean' removes KRUFT
KRUFT = locgen $(EXTRA_TARGETS) $(SHELL_FILES)

# Get the rest of the rules
include $(METRO)/mk/metrocommon.mk
