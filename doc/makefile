# Metropolis doc makefile
#
# @Version: $Id: makefile,v 1.12 2005/11/28 23:06:04 allenh Exp $
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
ME =		doc

# Order matters here.
# Compile examples last
DIRS =

# Root of Metro directory
ROOT =		..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

EXTRA_SRCS =

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = $(DIRS) \
	archRefinementMethodology.pdf \
	arm_cpus.pdf \
	coding \
	emsoft02 \
        FTlib \
        index.htm \
	metamodel \
	metropolisdesignguidelines \
	polychronyscaled \
        qm_framework \
        SystemCSim \
        TTL \
	TTLtemplate.pdf \
        yapi \
        yapitemplate \
	yapitemplate.pdf

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

# Files to be removed by 'make clean'
KRUFT = codeDoc doccheck

# Files to be removed by 'make distclean'
DISTCLEAN_STUFF =

# Make copyright.txt readonly so that when we open up the text editor
# we open up a readonly texteditor
all: codeDoc/index.html doccheck/index.html

install: subinstall docs

# Packages that are not currently shipped with the release
#OPTIONALPACKAGES =
#	metropolis.metamodel.backends.simulator \
#	metropolis.metamodel.backends.cpp


ALLPACKAGES = \
	$(OPTIONALPACKAGES) \
	metropolis.metamodel \
	metropolis.metamodel.shell \
	metropolis.metamodel.runtime \
	metropolis.metamodel.nodetypes \
	metropolis.metamodel.frontend \
	metropolis.metamodel.frontend.parser \
	metropolis.metamodel.frontend.lexer \
	metropolis.metamodel.backends \
	metropolis.metamodel.backends.metamodel \
	metropolis.metamodel.backends.systemc \
	metropolis.metamodel.backends.systemc.mmdebug \
	metropolis.metamodel.backends.promela \
	metropolis.metamodel.backends.elaborator \
	metropolis.metamodel.backends.elaborator.util

# Generation of Javadoc documentation
docs: 
	(cd ..; "$(JAVADOC)" $(JDOC_FLAGS) $(ALLPACKAGES))

codeDoc/index.html:
	(cd ..; "$(JAVADOC)" $(JDOC_FLAGS) $(ALLPACKAGES))

# Doccheck is a doclet that checks for bugs
# Location of the doccheck jar file, available from
# http://java.sun.com/developer/earlyAccess/doccheck/
DOCCHECKJAR =	$(METRO)/vendors/sun/doccheck1.2b2/doccheck.jar
JDOCCHECKDOCLET = -doclet com.sun.tools.doclets.doccheck.DocCheck  \
	-docletpath $(DOCCHECKJAR)

doccheck: doccheck/index.html
doccheck/index.html:
	if [ ! -d doccheck ]; then mkdir -p doccheck; fi
	if [ ! -f $(DOCCHECKJAR) ]; then \
		echo "$(DOCCHECKJAR) does not exist, so not running doccheck";\
		echo "doccheck is a doclet that checks for javadoc bugs."; \
	else \
	        echo "Running doccheck"; \
		(cd ..; CLASSPATH="src$(CLASSPATHSEPARATOR)$(PTJACL_JAR)" \
			"$(JAVADOC)" \
			$(JDOCCHECKFLAGS) \
			$(JDOCCHECKDOCLET) \
			-d doc/doccheck $(ALLPACKAGES)) \
	fi
# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
