# Tests for the producers_consumer Metropolis SystemC simulator 
#
# @Author: Christopher Brooks
#
# @Version: $Id: producers_consumer.tcl,v 1.16 2005/11/22 20:01:11 allenh Exp $
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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
#######################################################################

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Producers_Consumer-1.1 {} {
    set results [exec -stderrok ".[java::field java.io.File separator]run.x"]
    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # Substitute Producer? for Producer0 and Producer1
    regsub -all {Producer0} $results3 {Producer?} results4
    regsub -all {Producer1} $results4 {Producer?} results5

    # Get rid of all lines that do not start with m
    regsub -all {^[^m].*$} $results5 {} results6

    # Get rid of 'monitor> Producer?: write'
    regsub -all {^monitor> Producer.: write} $results6 {} results7

    # Get rid of 'monitor> Consumer: read'
    regsub -all {^monitor> Consumer: read} $results7 {} results8

    # This hack is necessary because of problems with crnl under windows
    set changes [regsub -all [java::call System getProperty "line.separator"] \
		     $results8 "\n" results9]
    list [string range $results9 0 2048]
} {{





 0 BEGIN_RT
 10 END_RT
 10 BEGIN_RT
 20 END_RT
 20 BEGIN_RT
 30 END_RT
 30 BEGIN_RT
 40 END_RT
 40 BEGIN_RT
 50 END_RT
 50 BEGIN_RT
 60 END_RT
 60 BEGIN_RT
 70 END_RT
 70 BEGIN_RT
 80 END_RT
 80 BEGIN_RT
 90 END_RT
 90 BEGIN_RT
 100 END_RT
 100 BEGIN_RT
 110 END_RT
 110 BEGIN_RT
 120 END_RT
 120 BEGIN_RT
 130 END_RT
 130 BEGIN_RT
 140 END_RT
 140 BEGIN_RT
 150 END_RT
 150 BEGIN_RT
 160 END_RT
 160 BEGIN_RT
 170 END_RT
 170 BEGIN_RT
 180 END_RT
 180 BEGIN_RT
 190 END_RT
 190 BEGIN_RT
 200 END_RT
 200 BEGIN_RT
 210 END_RT
 210 BEGIN_RT
 220 END_RT
 220 BEGIN_RT
 230 END_RT
 230 BEGIN_RT
 240 END_RT
 240 BEGIN_RT
 250 END_RT
 250 BEGIN_RT
 260 END_RT
 260 BEGIN_RT
 270 END_RT
 270 BEGIN_RT
 280 END_RT
 280 BEGIN_RT
 290 END_RT
 290 BEGIN_RT
 300 END_RT
 300 BEGIN_RT
 310 END_RT
 310 BEGIN_RT
 320 END_RT
 320 BEGIN_RT
 330 END_RT
 330 BEGIN_RT
 340 END_RT
 340 BEGIN_RT
 350 END_RT
 350 BEGIN_RT
 360 END_RT
 360 BEGIN_RT
 370 END_RT
 370 BEGIN_RT
 380 END_RT
 380 BEGIN_RT
 390 END_RT
 390 BEGIN_RT
 400 END_RT
 400 BEGIN_RT
 410 END_RT
 410 BEGIN_RT
 420 END_RT
 420 BEGIN_RT
 430 END_RT
 430 BEGIN_RT
 440 END_RT
 440 BEGIN_RT
 450 END_RT
 450 BEGIN_RT
 460 END_RT
 460 BEGIN_RT
 470 END_RT
 470 BEGIN_RT
 480 END_RT
 480 BEGIN_RT
 490 END_RT
 490 BEGIN_RT
 500 END_RT
 500 BEGIN_RT
 510 END_RT
 510 BEGIN_RT
 520 END_RT
 520 BEGIN_RT
 530 END_RT
 530 BEGIN_RT
 540 END_RT
 540 BEGIN_RT
 550 END_RT
 550 BEGIN_RT
 560 END_RT
 560 BEGIN_RT
 570 END_RT
 570 BEGIN_RT
 580 END_RT
 580 BEGIN_RT
 590 END_RT
 590 BEGIN_RT
 600 END_RT
 600 BEGIN_RT
 610 END_RT
 610 BEGIN_RT
 620 END_RT
 620 BEGIN_RT
 630 END_RT
 630 BEGIN_RT
 640 END_RT
 640 BEGIN_RT
 650 END_RT
 650 BEGIN_RT
 660 END_RT
 660 BEGIN_RT
 670 END_RT
 670 BEGIN_RT
 680 END_RT
 680 BEGIN_RT
 690 END_RT
 690 BEGIN_RT
 700 END_RT
 700 BEGIN_RT
 710 END_RT
 710 BEGIN_RT
 720 END_RT
 720 BEGIN_RT
 730 END_RT
 730 BEGIN_RT
 740 END_RT
 740 BEGIN_RT
 750 END_RT
 750 BEGIN_RT
 760 END_RT
 760 BEGIN_RT
 770 END_RT
 770 BEGIN_RT
 780 END_RT
 780 BEGIN_RT
 790 END_RT
 790 BEGI}}

