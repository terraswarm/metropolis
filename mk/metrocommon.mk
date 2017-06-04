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
#
# 						METROPOLIS_COPYRIGHT_VERSION_1
# 						COPYRIGHTENDKEY
#
# Common makefile of the Metropolis meta-model compiler.
# 
# To install, define the variables METRO and METRO_JDK
#          and use 'make install'.
# To compile , use 'make' 
#               or 'make all'.
# To get docs, use 'make docs'.
# To clean up, use 'make clean'.
# To test, use 'make test'.


####################################################
# Location of directories in the METRO tree

# Root directory of the Metropolis sources 
METRO_HOME =		$(METRO)/src/metropolis

# Meta-model compiler path
METAMODEL_HOME = 	$(METRO_HOME)/metamodel

# Documentation path
DOC_DIR =		$(METRO)/doc/codeDoc

# Location of the Metropolis SystemC backend
SYSTEMC_BACKEND =	$(METAMODEL_HOME)/backends/systemc

# Regression test directories
TESTDIR = 		$(METRO)/examples/test_new

####################################################
# Location of Java and the classpath
# Java JDK path
JDK_DIR    = $(METRO_JDK)

# Classpath
CLASSPATH  = $(METRO_HOME)/..$(CLASSPATHSEPARATOR).$(AUXCLASSPATH)$(LOCALCLASSPATH)

JTARGETS = $(JSRCS:.java=.class)

# Rule for compiling C++ files
.cpp.o:
	$(METRO_CXX) $(CXX_FLAGS) $(CXX_INCLUDES) -c $<

# Rule for compiling with cc
.c.o:
	$(METRO_CC) $(C_FLAGS) $(C_INCLUDES) -c $<

# Under no circumstances should this makefile include 'all', 'install'
# or 'depend' rules.  These rules should go in the makefile that
# includes this makefile, or into no-compile.mk
# The reason is that we want to avoid duplicate 'all', 'install'
# and 'depend' rules without using the possibly unportable double-colon
# makefile convention.

# Run make all in the subdirs
suball:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making all in $(ME)/$$x `date "+%h %e %T"`; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) all ;\
			) \
		    fi ; \
		done ; \
	fi

# Run make install in the subdirs
subinstall:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making install in $(ME)/$$x `date "+%h %e %T"` ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) install ;\
			) \
		    fi ; \
		done ; \
	fi


# Quickly attempt to build the tree
# 'make fast' is a hack.  If it does not work for you, either fix it or
# don't use it and use 'make' instead.
# Run make $(EXTRA_TARGETS) first so that we create
# metamodel.MetaModelVisitor before going in to nodetypes.
# $(EXTRA_TARGETS2) is used in metropolis/metamodel/frontend/parser/makefile
# so that we build MetaModelParser.java first before building the tables.
fast:
	@if [ "x$(EXTRA_TARGETS)" != "x" ]; then \
		$(MAKE) $(EXTRA_TARGETS); \
	fi
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making fast in $(ME)/$$x `date "+%h %e %T"`; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) fast ;\
			) \
		    fi ; \
		done ; \
	fi
	@if [ "x$(CCSRCS)" != "x" ]; then \
		echo "fast build with 'make sources' in `pwd`"; \
		$(MAKE) sources; \
		echo "fast build with '"$(METRO_CXX)" $(CXX_FLAGS) $(CXX_INCLUDES) -c $(CCSRCS)' in `pwd`"; \
		"$(METRO_CXX)" $(CXX_FLAGS) $(CXX_INCLUDES) -c $(CCSRCS); \
	fi
	@if [ "x$(CSRCS)" != "x" ]; then \
		echo "fast build with '"$(METRO_CC)" $(C_FLAGS) $(C_INCLUDES) -c $(CSRCS)' in `pwd`"; \
		"$(METRO_CC)" $(C_FLAGS) $(C_INCLUDES) -c $(CSRCS); \
		$(MAKE) all; \
	fi
	@if [ "x$(JSRCS)" != "x" ]; then \
		echo "fast build with 'CLASSPATH=\"$(CLASSPATH)$(AUXCLASSPATH)\" "$(JAVAC)" $(JFLAGS) *.java' in `pwd`"; \
		CLASSPATH="$(CLASSPATH)$(AUXCLASSPATH)" "$(JAVAC)" $(JFLAGS) *.java; \
	fi
	@if [ "x$(EXTRA_TARGETS2)" != "x" ]; then \
		$(MAKE) $(EXTRA_TARGETS2); \
	fi

	@if [ "x$(METRO_LIBS)" != "x" ]; then \
		$(MAKE) $(METRO_LIBS); \
	fi

# "make sources" will do SCCS get on anything where SCCS file is newer.
sources::	$(SRCS) $(EXTRA_SRCS) $(HDRS) $(MISC_FILES) makefile
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

##############
# Java rules
# JFLAGS is defined in metro.mk

.SUFFIXES: .class .java
.java.class:
	rm -f `basename $< .java`.class
	CLASSPATH="$(CLASSPATH)" "$(JAVAC)" $(JFLAGS) $<

# Build all the Java class files.
# Run in the subdirs first in case the subpackages need to be compiled first.

jclass:	$(DERIVED_JSRCS) $(JSRCS) subjclass $(JCLASS)

subjclass:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making jclass in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) jclass ;\
			) \
		    fi ; \
		done ; \
	fi


# Script used to find files that shold not be shipped
CHKEXTRA =	$(METRO)/util/testsuite/chkextra
checkjunk:
	@"$(CHKEXTRA)" $(SRCS) $(HDRS) $(EXTRA_SRCS) $(MISC_FILES) \
		$(OPTIONAL_FILES) $(JSRCS) alljtests.tcl makefile SCCS CVS \
		$(JCLASS) $(OBJS) $(LIBR) $(PTDISTS) \
		$(PTCLASSJAR) $(PTCLASSALLJAR) $(PTAUXALLJAR) \
		$(EXTRA_TARGETS)
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

##############
# Rules for cleaning

DERIVED_JAVA_SRCS =  $(MMM_SRCS:%.mmm=%.java)

CRUD=.*.ast .constr*.loc .trace *.o *.so core *~ *.bak ,* LOG* *.class \
	*.bb *.bbg *.bin *.da *.gcov \
	alljtests.tcl manifest.tmp .lastgen \
	$(DERIVED_JAVA_SRCS) \
	$(JCLASS) $(KRUFT)

clean:
	@if [ -f systemc_sim.mk ]; then \
	    $(MAKE) -f systemc_sim.mk sim_clean; \
	fi
	rm -rf $(CRUD)
	if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi

sim_clean:
	@rm -rf $(SYSTEMC_SIM_KRUFT)
	rm -f systemc_sim.mk

# Cleaner than 'make clean'
# Remove the stuff in the parent directory after processing
# the child directories incase something in the child depends on
# something we will be removing in the parent
realclean:
	@if [ "x$(DIRS)" != "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi
	rm -f $(CRUD) configure config.status config.log config.cache
	-rm -f doc/codeDoc/* $(OPTIONAL_FILES)


##############
# Testing rules

# Instrument Java code for use with JavaScope.
jsinstr:
	$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS)

# If the jsoriginal directory does not exist, then instrument the Java files.
# If JSSKIP is set, then we skip running JavaScope on them. 
# JSSKIP is used in mescal/domains/mescalPE/kernel/makefile
jsoriginal:
	@if [ ! -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS)"; \
		$(JSINSTR) $(JSINSTRFLAGS) $(JSRCS); \
		if [ "$(JSSKIP)" != "" ]; then \
			set $(JSSKIP); \
			for x do \
				echo "Restoring $$x so that JavaScope is not run on it"; \
				cp jsoriginal/$$x .; \
			done; \
		fi; \
	fi

# Back out the instrumentation.
jsrestore:
	if [ -d jsoriginal -a "$(JSRCS)" != "" ]; then \
		echo "Running jsrestore in `pwd`"; \
		$(JSRESTORE) $(JSRCS); \
		rm -f jsoriginal/README; \
		rmdir jsoriginal; \
		$(MAKE) clean; \
	else \
		echo "no jsoriginal directory, or no java sources"; \
	fi

# Compile the instrumented Java classes and include JavaScope.zip
jsbuild:
	$(MAKE) AUXCLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" JFLAGS="$(JFLAGS)" all

# Run the test_jsimple rule with the proper classpath
jstest_jsimple:
	$(MAKE) AUXCLASSPATH="$(CLASSPATHSEPARATOR)$(JSCLASSPATH)" \
		tests
	@echo "To view code coverage results, run javascope or jsreport"
	@echo "To get a summary, run jsreport or jsreport -HTML or"
	@echo "jssummary -HTML -PROGRESS -OUTFILE=\$$HOME/public_html/private/js/coverage.html"
	@echo "jsreport -HTML -PROGRESS -RECURSIVE -OUTDIR=\$$HOME/public_html/private/js"


# If necessary, instrument the classes, then rebuild, then run the tests
jsall: jsoriginal
	$(MAKE) clean
	$(MAKE) jsbuild
	if [ -w test ] ; then \
	   (cd test; $(MAKE) jstest_jsimple); \
	fi

# Run the tests in nightly mode so that the checks in NonStrictTest
# and TypeTest work.
nightly:
	$(MAKE) JTCLSHFLAGS=-Dptolemy.ptII.isRunningNightlyBuild=true tests

# Run all the tests
tests:: makefile
	@if [ "x$(DIRS)" != "x" -a "x$(SKIP_TESTS)" = "x" ]; then \
		set $(DIRS); \
		for x do \
		    if [ -w $$x ] ; then \
			( cd $$x ; \
			echo making $@ in $(ME)/$$x ; \
			$(MAKE) $(MFLAGS) $(MAKEVARS) $@ ;\
			) \
		    fi ; \
		done ; \
	fi


# alljtests.tcl is used to source all the tcl files that use Java
# that are listed in the JACL_TESTS makefile varialb
alljtests.tcl: makefile
	rm -f $@
	echo '# CAUTION: automatically generated file by a rule in metrocommon.mk' > $@
	echo '# This file will source all the Tcl files that use Java. ' >> $@
	echo '# This file will source the tcl files list in the' >> $@
	echo '# makefile JACL_TESTS variable' >> $@
	echo '# This file is different from all.itcl in that all.itcl' >> $@
	echo '# will source all the .itcl files in the current directory' >> $@
	echo '#' >> $@
	echo '# Set the following to avoid endless calls to exit' >> $@
	echo "if {![info exists reallyExit]} {set reallyExit 0}" >> $@
	echo '# If there is no update command, define a dummy proc.  Jacl needs this' >> $@
	echo 'if {[info command update] == ""} then { ' >> $@
	echo '    proc update {} {}' >> $@
	echo '}' >> $@
	echo "#Do an update so that we are sure tycho is done displaying" >> $@
	echo "update" >> $@
	echo "set savedir \"[pwd]\"" >> $@
	echo "if {\"$(JACL_TESTS)\" != \"\"} {foreach i [list $(JACL_TESTS)] {puts \$$i; cd \"\$$savedir\"; if [ file exists \$$i ] { if [ catch {source \$$i} msg] {puts \"Error: \$$msg\"; incr FAILED}}}}" >> $@
	echo "catch {doneTests}" >> $@
	echo "exit" >> $@

FORCE:
