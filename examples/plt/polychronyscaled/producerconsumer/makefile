# Metropolis makefile
#
# @Version: $Id: makefile,v 1.15 2005/02/17 15:20:08 cxh Exp $
#
# Copyright (c) 2004-2005 The Regents of the University of California.
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
ME =		examples/plt/polychronyscaled/producerconsumer

# Root of Metro directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

DIRS =		test

MMM_SRCS = \
	Consumer.mmm \
	ProdConsScheduled.mmm \
	Producer.mmm \
	ProducerConsumer.mmm

EXTRA_SRCS = $(MMM_SRCS) README.txt

# Run 'make DEBUG=-debug' to debug the Metropolis frontend.
# See $METRO/doc/coding/debugging.htm 
# DEBUG = -debug

# Run 'make MMDEBUG=-mmdebug' to debug the metamodel.
# MMDEBUG = -mmdebug

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to report an error on them.
MISC_FILES = \
	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

all: sc_main.cpp systemc_sim.mk
	make -f systemc_sim.mk

install: all

mmdebug: clean FORCE
	make MMDEBUG="-mmdebug -w" all

# AUXCLASSPATH is used by the code coverage tools
sc_main.cpp systemc_sim.mk: $(MM_SRCS)
	METRO_CLASSPATH=$(METRO)/examples/plt/polychronyscaled \
	CLASSPATH="$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
	$(ROOT)/bin/metacomp -systemc $(MMDEBUG) -top \
		producerconsumer.ProducerConsumer ProducerConsumer.mmm

# 'make clean' removes KRUFT
KRUFT =	run.x data metro*.tmp *.cpp *.o .*.ast *.h systemc_sim.mk bkpts.cmd \

# Run the default example, see README.txt
demo1:
	if [ -f ProdConsScheduled.mmm_1 ]; then \
	    cp ProdConsScheduled.mmm_1 ProdConsScheduled.mmm; \
        fi 
	$(MAKE) clean all
	@echo "Next, we run the demo for 5 seconds, which will produce"
	@echo "  \"Producer is executing\""
	@echo "  \"Consumer is executing\""
	@echo "over and over"  
	$(ROOT)/util/testsuite/run-watchdog -n 2 run.x > run.out 2>&1
	head -60 run.out


demo2: ProdConsScheduled.mmm_1
	@echo "Test rate priority and de-synchronization"
	sed 's@^\([ 	]*\)desynchNetlist();@\1//desynchNetlist();@' \
		ProdConsScheduled.mmm_1 > ProdConsScheduled.mmm
	@echo "Differences between old and new:"
	-diff ProdConsScheduled.mmm_1 ProdConsScheduled.mmm
	$(MAKE) clean all
	$(ROOT)/util/testsuite/run-watchdog -n 2 run.x > run.out 2>&1
	head -60 run.out

demo3: ProdConsScheduled.mmm_1
	sed -e 's@^\([ 	]*\)desynchNetlist();@\1//desynchNetlist();@' \
	    -e 's@^\([ 	]*\)Producer p = new Producer( "P" , 1 , 1 );@\1Producer p = new Producer( "P" , 5 , 1 );@' \
		ProdConsScheduled.mmm_1 > ProdConsScheduled.mmm
	@echo "Differences between old and new:"
	-diff ProdConsScheduled.mmm_1 ProdConsScheduled.mmm
	$(MAKE) clean all
	$(ROOT)/util/testsuite/run-watchdog -n 2 run.x > run.out 2>&1
	head -60 run.out

demo4: ProdConsScheduled.mmm_1
	sed -e 's@^\([ 	]*\)desynchNetlist();@\1//desynchNetlist();@' \
	    -e 's@^\([ 	]*\)Producer p = new Producer( "P" , 1 , 1 );@\1Producer p = new Producer( "P" , 5 , 2 );@' \
		ProdConsScheduled.mmm_1 > ProdConsScheduled.mmm
	@echo "Differences between old and new:"
	-diff ProdConsScheduled.mmm_1 ProdConsScheduled.mmm
	$(MAKE) clean all
	$(ROOT)/util/testsuite/run-watchdog -n 2 run.x > run.out 2>&1
	head -60 run.out

#demo5: ProdConsScheduled.mmm_1
#	sed 's@^\([ 	]*\)Producer p = new Producer( "Producer" , 1 , 1 );@\1Producer p = new Producer( "Producer" , 5 , 2 );@' \
#		ProdConsScheduled.mmm_1 > ProdConsScheduled.mmm
#	$(MAKE) clean all


# Master copy of file used for other demos
ProdConsScheduled.mmm_1:
	cp ProdConsScheduled.mmm ProdConsScheduled.mmm_1

#test: clean all
#	@./run.x
#	$(MAKE) clean

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk

FORCE:
