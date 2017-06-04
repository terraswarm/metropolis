# Makefile to create tar files for distribution
# Version: $Id: makefile 3991 2005-11-23 22:06:15Z allenh $
# Copyright (c) 1994-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
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
#
# Programmer:  Christopher Brooks

# How this works:

# The bldmastr user on gigasource has a 
# cronjob (~bldmastr/adm/bldmastr_cron_gigasource.txt) that runs
# /home2/bldmastr/adm/bldmastr_nightly_gigasource.  Both of
# these files are checked in to the buildmaster cvs repository
# bldmastr_nightly_gigasource rotates the logs and then runs make
# using ~bldmastr/adm/makefile.  That makefile has a rule that
# calls make bldmastrcoverage using this makefile. 
#
# See $METRO/doc/coding/testing.htm

# Use the command below for building Metropolis under Windows:
# make USER=cxh METROHOME=/cygdrive/c/cxh/src/tmp/metro-1.1 clean all

# Use the command below for building Metropolis under Unix:
# make USER=cxh METROHOME=/tmp/cxh/metro clean all

# Use the command below for building and testing Metropolis:
# make  USER=cxh METROHOME=/tmp/cxh/metro METRO=/tmp/cxh/metro/adm/gen-1.0/remote1/metropolis-1.0.3 JSCLASSPATH=/users/cxh/JavaScope/JavaScope.zip JSOUTDIR=/users/cxh/public_html/nightly/js remote1 >& make.out &

# Variables.
# If you change the version number, update $METRO/README.txt 
MAJOR_VERSION = 	1.1
MINOR_VERSION =	        4

#VERSION=	$(MAJOR_VERSION).$(MINOR_VERSION)
VERSION=	$(MAJOR_VERSION)

# Version string for lame filesystems that don't like more than one .
WINDOWS_VERSION =	1_1_4

RELEASE_VERSION =	metropolis-$(VERSION)

# Location of utilities
# We use GNU tar, which allows us to exclude files
TAR = 		/bin/tar
SUFFIX =	gz

# GNU tar command line arguments to exclude files
TAR_EXCLUDES =--no-anchored --exclude "*.class" --exclude "*.exe" --exclude='*.tmp' --exclude="run.x" --exclude="*.o" --exclude="CVS" --exclude "systemc_v*" --no-anchored -X $(GENDIR)/src.ex 

USER =		metro
METROHOME =	/users/metro
METROADM =	$(METROHOME)/adm

# Should be this directory
GENDIR =	$(METROADM)/gen-$(MAJOR_VERSION)

# Temporary location of the distribution so that we can
# have the tar files have the version number in them
METRODIST = 	$(METROADM)/dists/$(RELEASE_VERSION)

# Directories and files in the top level metro directory that we should
# include in the tar file.  We list individual files and directories
# so as to avoid including directories like metro/adm/ and the extra
# kruft in metro/doc
METRO_TOPDIR= \
	        .classpath.in \
		.eclipse.epf \
		README.txt \
		bin \
		config \
		configure \
		configure.in \
		copyright.txt \
		doc/archRefinementMethodology.pdf \
		doc/arm_cpus.pdf \
		doc/coding \
		doc/makefile \
		doc/metamodel/metamodel.pdf \
		doc/metropolisdesignguidelines/metropolisdesignguidelines.pdf \
		doc/polychronyscaled/polychronyscaled.pdf \
		doc/TTLtemplate.pdf \
		doc/yapitemplate.pdf \
		examples \
		lib \
		makefile \
		mk \
	        src/.classpath.in \
	        src \
		util

##############################################################################
# Files to be produced

# Source only, no .class, .jar or javadoc
METRO_SRC =   $(GENDIR)/$(RELEASE_VERSION).src.tar.$(SUFFIX)

##############################################################################
# Targets
all: $(METRO_SRC)

clean:
	@echo "#"
	@echo "# Now cleaning old tar files"
	@echo "#"
	rm -f $(METRO_SRC)
	rm -rf $(METRODIST) src test

FINDCLEAN = $(GENDIR)/findclean

$(METROADM)/dists:
	@if [ $(METROADM)/dists ]; then \
		mkdir -p $(METROADM)/dists; \
	fi

# First we create a directory containing the distribution,
# then we modify the distribution slightly,
# then we tar it up.
metrodist: $(METRODIST)
$(METRODIST): $(METROADM)/dists
	@echo "#"
	@echo "# Now creating $(METRODIST)"
	@echo "#"
	@echo "$@ `date`"
	mkdir $@
	(cd $(METROHOME); \
		$(TAR) -c $(TAR_EXCLUDES) \
		--no-anchored -X $(GENDIR)/src1.ex \
		$ -f - $(METRO_TOPDIR) )| \
		(cd $@; $(TAR) -xpf -)
	@echo "Removing codeDoc directories and other documentation"
	@echo "  tar --no-anchored -X does not seem to work under Cygwin"
	-find $(METRODIST) -name codeDoc -exec rm -rf {} \;
	-find $(METRODIST) -name CVS -exec rm -rf {} \;
	rm -f $(METRODIST)/.glimpse*
	cp $(METROHOME)/.glimpse_exclude $(METRODIST)
	# Bump up the mod time of $$METRO/mk/metro.mk.in so that if
	# the user runs make, then configure will run 
	touch $(METRODIST)/mk/metro.mk.in

metro_src: $(METRO_SRC)
$(METRO_SRC): $(METRODIST)
	@echo "Creating $@"
	rm -rf src/$(RELEASE_VERSION)
	-mkdir src
	(cd src; \
		echo "## Copying from $(METRODIST)/.."; \
		(cd $(METRODIST)/..; \
			$(TAR) -c $(TAR_EXCLUDES) \
			-f - $(RELEASE_VERSION)) | $(TAR) -xf - ; \
		cd $(RELEASE_VERSION); \
		echo "##   Running configure and then make clean"; \
		METRO=`pwd` ./configure; make -k clean; \
		touch mk/metro.mk.in; \
		echo "##   Removing .class files, certain .jar files"; \
		find . -name "*.class" -ls -exec rm {} \; ;\
		cd src; find . -name "*.jar" -ls -exec rm {} \; ;\
		cd ..; \
		find doc -name "*.jar" -ls -exec rm {} \; ;\
		rm -rf doc/codeDoc; \
		echo "##   Removing files listed in src.ex"; \
		rm -rf `cat $(GENDIR)/src.ex`; \
		 $(FINDCLEAN); \
		cd ..; \
		echo "##   Starting tar of $@"; \
		$(TAR) -zcf $(METRO_SRC) -X $(GENDIR)/src.ex \
			$(RELEASE_VERSION));

####################################
# Simple test of the src file

test: $(METRO_SRC)
	mkdir test
	cd test; tar -zxf $(METRO_SRC)
	(cd test/$(RELEASE_VERSION); make checkjunk) 
	if [ `uname -o` = "Cygwin" ]; then \
	    genDir=`echo "$(GENDIR)" | \
                    sed 's!^/cygdrive/\(.\)/\(.*\)!\1:/\2!'`;\
	    (cd test/$(RELEASE_VERSION); \
		METRO=$${genDir}/test/$(RELEASE_VERSION) \
			./configure; \
		METRO=$${genDir}/test/$(RELEASE_VERSION) \
			make fast install tests); \
	else \
	    (cd test/$(RELEASE_VERSION); \
		METRO=$(GENDIR)/test/$(RELEASE_VERSION) \
			./configure; \
		METRO=$(GENDIR)/test/$(RELEASE_VERSION) \
			make fast install tests); \
	fi


####################################
# Build and generate test case coverage

# Log file to save the results in
REMOTE1LOG = $(GENLOGDIR)/remote1.out

JSHOME = $(HOME)

# JavaScope database, gets removed every night
# Note that this must be a directory called jsdatabase in the user's
# home directory.  Overriding this is tricky, it requires setting
# up a javascope.properties file that can be read at runtime
JSDATABASE=$(JSHOME)/jsdatabase

# Directory to place output in
JSOUTDIR=$(JSHOME)/public_html/nightly/js

# Directory to place output in
JSOUTDIR=/users/metro/public_html/nightly/js

REMOTE1PARENT = $(GENDIR)/remote1
REMOTE1HOME = $(REMOTE1PARENT)/$(RELEASE_VERSION)

# Location of JavaScope Zip file, used for instrumenting code coverage.
JSCLASSPATH=/usr/local/JavaScope/JavaScope.zip


# Build and test in a separate location $(REMOTE1HOME)
remote1:
	$(MAKE)\
		remote1nuke \
		remote1dist \
		remote1ptiidist \
		remote1all

# Build in situ at ~bldmastr 
# The nightly build runs this rule, see the top of this makefile for details.
bldmastrcoverage:
	$(MAKE) -k REMOTE1HOME=/home/bldmastr/metro \
		JSDATABASE=/home/bldmastr/metro/jsdatabase \
		JSOUTDIR=/home/www/cvswww/metro/nightly \
		JSCLASSPATH=/usr/local/JavaScope/JavaScope.zip \
		METRO=/home/bldmastr/metro \
		METRO_CLASSPATH=/home/bldmastr/metro:/home/bldmastr/metro/lib \
		METROHOME=/home/bldmastr/metro \
		TAR=/usr/local/bin/tar \
		VERSION= \
		remote1dist \
		remote1distclean \
		remote1all

remote1all:	remote1configure \
		remote1distnonjsall \
		remote1distjsall \
		remote1distjsrestore \
		remote1distcleanagain \
		remote1build \
		remote1doc \
		remote1ChangeLog

# Remove the remote dist
remote1nuke:
	rm -rf $(REMOTE1PTIIDIST)

remote1dist:
	@echo "#"
	@echo "# Now creating the tar files on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	$(MAKE) VERSION=$(MAJOR_VERSION).$(MINOR_VERSION) clean all

# Untar the distribution
remote1ptiidist: $(REMOTE1HOME)
$(REMOTE1HOME): $(PTII_FULL)
	@echo "#"
	@echo "# Now untaring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	if [ ! -d $(REMOTE1PARENT) ]; then \
		mkdir -p $(REMOTE1PARENT); \
	fi
	(cd $(REMOTE1PARENT); $(TAR) -zxf $(METRO_SRC))

# Clean the remote1 tree
# The line '#metronightly-mail#: Starting make clean at `date`'
# is special, the metronightly-mail script uses it
remote1distclean:
	@echo "#"
	@echo "# Now running make clean in $(REMOTE1HOME) on `hostname`"
	@echo "#"
	@echo "#metronightly-mail#: Starting make clean at `date`"
	(cd $(REMOTE1HOME); \
		$(MAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)

remote1configure:
	@echo "#"
	@echo "# Now configuring on `hostname`"
	@echo "#"
	@echo "$@ `date`"
	(cd $(REMOTE1HOME);	rm -f config.cache; METRO=$(REMOTE1HOME) ./configure)

# JSALLDIRS contains the directory that has the java files, not the test dir.
# * Don't include frontend/lexer because the body of yylex() will be over 65k
#   and we will get
#    Lexer.java:1062: code too large for try statement
# * parser/MetaModelDecl fails to compile if we run JavaScope on it
#   but we need to build it so that the parser gets built.
# * metamodel/shell is after the other src dirs so we increase coverage

# Run the essential Ptolemy tests
JSALLDIRS = \
	$(REMOTE1HOME)/src/util/testsuite \
	$(REMOTE1HOME)/src/util/locgen/examples \
	$(REMOTE1HOME)/src/metropolis/metamodel \
	$(REMOTE1HOME)/src/metropolis/metamodel/frontend \
	$(REMOTE1HOME)/src/metropolis/metamodel/runtime \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends/cpp \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends/elaborator \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends/elaborator/util \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends/promela \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends/runtimetest \
	$(REMOTE1HOME)/src/metropolis/metamodel/backends/systemc \
	$(REMOTE1HOME)/src/metropolis/metamodel/shell \
	$(REMOTE1HOME)/examples/constraint_elaboration \
	$(REMOTE1HOME)/examples/dining_phil/test \
	$(REMOTE1HOME)/examples/dining_phil_live/test \
	$(REMOTE1HOME)/examples/gtime \
	$(REMOTE1HOME)/examples/loc_checker \
	$(REMOTE1HOME)/examples/mgdb \
	$(REMOTE1HOME)/examples/mgdb/flowcontrol \
	$(REMOTE1HOME)/examples/producers_consumer \
	$(REMOTE1HOME)/examples/sync \
	$(REMOTE1HOME)/examples/pip/architecture/simple \
	$(REMOTE1HOME)/examples/pip/architecture/vertical \
	$(REMOTE1HOME)/examples/plt/polychronyscaled/leaderelection \
	$(REMOTE1HOME)/examples/plt/polychronyscaled/producerconsumer \
	$(REMOTE1HOME)/examples/plt/yapitemplate/test \
	$(REMOTE1HOME)/examples/promela \
	$(REMOTE1HOME)/examples/runtime \
	$(REMOTE1HOME)/examples/simpleArchitecture \
	$(REMOTE1HOME)/examples/simpleMapping \
	$(REMOTE1HOME)/examples/yapi_cpus/arm/strongarm_yapi \
	$(REMOTE1HOME)/examples/yapi_cpus/arm/xscale_yapi

# Directories we run make after running running make in the NONJSALLDIRS
# The reason to do this is that Tcl scripts might refer to classes
# defined but not yet compiled in these directories
JSALLOTHERDIRS =

# Directories we run make in before running make in the NONJSALLDIRS
NONJSALLCOREDIRS = \
	$(REMOTE1HOME)/bin \
	$(REMOTE1HOME)/src/metropolis/metamodel/frontend/lexer \
	$(REMOTE1HOME)/src/metropolis/metamodel/frontend/parser

# Directories to run tests in outside of JavaScope
NONJSALLDIRS = \


# We want the data to go into a separate directory
JSINSTRFLAGS="-IDATABASE=$(JSDATABASE) -RDATABASE=$(JSDATABASE)"

# Run tests outside of Javascope
remote1distnonjsall:
	@echo "#"
	@echo "# Now running make non Javascope tests in $(REMOTE1HOME)"
	@echo "# on  `hostname`"
	@echo "# To add to the list of directories, edit "
	@echo "# ~metro/adm/gen-latest/makefile"
	@echo "#"
	@echo "$@ `date`"
	@echo "# Run make in the non javascope core directories"
	@echo "# that need to be built before we build the other non javascope"
	-if [ "x$(NONJSALLCOREDIRS)" != "x" ]; then \
		set $(NONJSALLCOREDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making all  in $(METROHOME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
			   AUXCLASSPATH=$(JSCLASSPATH) \
				all \
			);  \
		    fi ; \
		done ; \
	fi
	-if [ "x$(NONJSALLDIRS)" != "x" ]; then \
		set $(NONJSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making tests in $(METROHOME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
			   AUXCLASSPATH=$(JSCLASSPATH) \
				tests \
			);  \
		    fi ; \
		done ; \
	fi

# Run JavaScope Code Coverage tool in the distribution tree
# For command line options, see
#http://www.gigascale.org/pubs/gsrc-downloads/JavaScope/docs/pdf/UGApxTools.pdf
remote1distjsall: remote1distjsall_1 remote1distjsall_3

remote1distjsall_1:
	@echo "#"
	@echo "# Now running make jsall in $(REMOTE1HOME)"
	@echo "# on  `hostname`"
	@echo "# Generating test coverage data for: "
	@echo "# $(JSALLDIRS)"
	@echo "# To add to the list of directories, edit "
	@echo "# ~metro/adm/gen-latest/makefile"
	@echo "#"
	@echo "$@ `date`"

	@echo "Removing $(JSDATABASE) in prep for $$x "
	-ls -ldg $(JSDATABASE) $(JSDATABASE)/*
	-rm -rf $(JSDATABASE)
	mkdir $(JSDATABASE)

	@-if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jsall in $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				JTCLSHFLAGS=-Dptolemy.metro.isRunningNightlyBuild=true \
				jsall JSINSTRFLAGS=$(JSINSTRFLAGS);\
			);  \
		    fi ; \
		done ; \
	fi
	@-if [ "x$(JSALLOTHERDIRS)" != "x" ]; then \
		set $(JSALLOTHERDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making alll in $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) \
				all; \
			);  \
		    fi ; \
		done ; \
	fi

remote1distjsall_3:
	jssummary -HTML -PROGRESS \
	        -IDATABASE=$(JSDATABASE) \
	        -RDATABASE=$(JSDATABASE) \
		-OUTFILE=$(JSOUTDIR)/coverage.html
	jsreport -HTML -PROGRESS -RECURSIVE \
	        -IDATABASE=$(JSDATABASE) \
	        -RDATABASE=$(JSDATABASE) \
		-OUTDIR=$(JSOUTDIR)
	# Add links and colorize the coverage.html page
	sh $(METROADM)/bin/coveragerating $(METRO)/src \
		$(JSOUTDIR)/coverage.html
	-chmod -R a+r $(JSOUTDIR)

# report directories that have java files in them, but are not
# in JSALLDIRS
jsalldirs_check:
	@sh $(METROHOME)/adm/bin/jsalldirs_check $(REMOTE1HOME) $(JSALLDIRS)

# Remove the JavaScope instrumentation
remote1distjsrestore:
	@echo "#"
	@echo "# Now running make jsrestore in $(REMOTE1HOME)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	@if [ "x$(JSALLDIRS)" != "x" ]; then \
		set $(JSALLDIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jsrestore in $$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jsrestore;\
			);  \
		    fi ; \
		done ; \
	fi
	#( cd $(REMOTE1HOME)/ptolemy/copernicus/applet; \
	#	echo making jsrestore in ptolemy/copernicus/applet ; \
	#	$(MAKE) $(MFLAGS) $(MAKEVARS) jsrestore)

# Clean the remote1 tree
# The line '#metronightly-mail#: Starting make clean at `date`'
# is special, the metronightly-mail script uses it
remote1distcleanagain:
	@echo "#"
	@echo "# Now running make clean again in $(REMOTE1HOME) on `hostname`"
	@echo "#"
	@echo "#metronightly-mail#: Starting make clean at `date`"
	(cd $(REMOTE1HOME); \
		$(MAKE) clean; \
		find . -name codeDoc -exec rm -rf {} \;)

# Remove the JavaScope instrumentation
remote1build:
	@echo "#"
	@echo "# Now running make in $(REMOTE1HOME)"
	@echo "# on  `hostname`"
	@echo "#"
	@echo "$@ `date`"
	(cd $(REMOTE1HOME);	\
		METRO=$(REMOTE1HOME) $(MAKE) -k sources install)

# Remove the JavaScope instrumentation
remote1doc:
	@echo "#"
	@echo "# Now running make in $(REMOTE1HOME)/doc"
	@echo "# on `hostname`"
	@echo "# We run make in the doc directory early so that"
	@echo "# the website link checker will find files in doc/codeDoc"
	@echo "#"
	@echo "$@ `date`"
	(cd $(REMOTE1HOME); METRO=$(REMOTE1HOME) $(MAKE) docs doccheck)

remote1ChangeLog:
	@echo "#"
	@echo "# Now generating ChangeLog file in $(REMOTE1HOME)"
	@echo "#"
	@echo "$@ `date`"
	(cd $(REMOTE1HOME);	rm -f ChangeLog; \
		METRO=$(REMOTE1HOME) $(MAKE) -k ChangeLog; \
		rm -f ChangeLog.txt; ln -s ChangeLog ChangeLog.txt)

