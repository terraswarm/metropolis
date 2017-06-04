# Metropolis makefile
#
# @Version: $Id: makefile,v 1.24 2004/09/18 00:10:29 cxh Exp $
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
ME =		.

# Order matters here.
# Compile bin first so that the metroshell script is created first
# Compile examples last
DIRS = bin src lib doc util examples

# Root of Metro directory
ROOT =		.

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

EXTRA_SRCS = \
	.classpath.in \
	.eclipse.epf \
	README.txt \
	copyright.txt \
	configure.in \
	configure

# Files to be removed by 'make clean'
KRUFT =


# Files to be removed by 'make distclean'
DISTCLEAN_STUFF = \
	mk/metro.mk config.log config.status config.cache


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS) \
	bin \
	config \
	lib \
	mk

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	.classpath \
	adm \
	config.log \
	config.status \
	config.cache \
	confTest.class \
	logs \
	public_html \
	publications \
	vendors

# In the default, have 'make' run 'make fast'
all: mk/metro.mk fast

install: subinstall

docs:
	(cd doc; $(MAKE))

doccheck:
	(cd doc; $(MAKE) $@)

# Glimpse is a tool that prepares an index of a directory tree.
# glimpse is not included with Metropolis, see http://glimpse.cs.arizona.edu
GLIMPSEINDEX =	/usr/local/bin/glimpseindex
glimpse: .glimpse_exclude
	@echo "Saving .glimpse_exclude, removing the .glimpse* files"
	rm -f glimpse_exclude
	cp .glimpse_exclude glimpse_exclude
	rm -f .glimpse*
	cp  glimpse_exclude .glimpse_exclude
	$(GLIMPSEINDEX) -n -H `pwd` `pwd`
	chmod a+r .glimpse_*
	rm -f glimpse_exclude

# Generate metro.mk by running configure
mk/metro.mk: configure mk/metro.mk.in
	./configure

configure: configure.in
	@echo "configure.in is newer than configure, so we run"
	@echo "autoconf to update the configure file"
	@echo "This may occur if you do a cvs update, and the mod time"
	@echo "of configure.in is newer than that of configure"
	@echo "even though the configure script in the repository"
	@echo "was modified after configure.in was modified."
	@echo "Note that if you don't have GNU autoconf installed,"
	@echo "you can try running 'touch configure' to work around"
	@echo "this problem."
	autoconf

# Arguments for cvs2cl.pl, which is used to generate a ChangeLog
# from the CVS logs.  
# -W 3600 means unify entries that are within 3600 seconds or 1 hr.
CVS2CL_ARGS = -W 3600

# Generate a ChangeLog file from the CVS logs
# This requires that the CVS directory be present and takes
# quite awhile to update
ChangeLog:
	@if [ -d CVS ]; then \
		echo "Running ./util/testsuite/cvs2cl.pl"; \
		echo " This could take several minutes"; \
		./util/testsuite/cvs2cl.pl -W 3600; \
	else \
		echo "CVS directory not present, so we can't update $@"; \
	fi

update:
	-cvs update -P -d
	$(MAKE) -k clean fast

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
