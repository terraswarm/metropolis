# Tests for dining_phil
#
# @Author: Christopher Brooks
#
# @Version: $Id: dining_phil.tcl,v 1.7 2005/11/22 20:03:10 allenh Exp $
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
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

set TESTDIR [pwd]
######################################################################
####
#
test dining_phil-1.1 {Create run.x and constrN.check} {
    cd ..	
    set errMsg {}
    if [catch {exec -stderrok make -k} errMsg] {
	cd test
	error "dining_phil-1.1 failed: $errMsg"
    }

    if {! [ file exists .[java::field java.io.File separator]run.x]} {
	error ".[java::field java.io.File separator]run.x does not exist"
    }

    # Let it run for 10 seconds
    if [catch {set results \
		   [exec -stderrok -sh \
			[file join $METRO util testsuite run-watchdog] -n 10 \
			./run.x]} errMsg] {
	cd test
	error "dining_phil-1.1 failed: $errMsg"
    }
    cd $TESTDIR

    # Check to make sure the last line looks like
    #   Phil2: Got right cs 1

    #puts $results

    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    regsub -all {cps.*$} $results3 {cpsXXX} results4
    # Get rid of everything that doesn't start with a P
    regsub -all {^[^P]*$} $results4 {} results5
    set results6 [string trim $results5] 

    # Substitute xxx for right and left
    regsub -all {Got right cs} $results6 {Got xxx cs} results7
    regsub -all {Got left cp} $results7 {Got xxx cs} results8

    # Just return part of the last line
    string range $results8 [expr {[string length $results8] - 12}] [expr {[string length $results8] - 3}] 

} {Got xxx cs}

