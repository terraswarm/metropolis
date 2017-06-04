# Tests for simpleArchitecture
#
# @Author: Christopher Brooks
#
# @Version: $Id: simpleArchitecture.tcl,v 1.7 2005/11/29 23:37:02 allenh Exp $
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
test simpleArchitecture-1.1 {Create run.x and run it} {

    # runExecutable is defined in $METRO/util/testDefs.tcl
    # and runs run.x for 2 seconds	
    set results [runExecutable]

    # Return the first 100 lines or so.
    set results2 [string range $results 0 4500]

    # Get rid of of the SystemC and Copyright messages, which are
    # printed on stderr, so appear last here because of a Jacl exec bug.
    regsub {^ *SystemC 2\..*$} $results2 {} results3
    regsub {^ *Copyright.*$} $results3 {} results4

    # Make sure it gets started by pringing stuff about processes
    set results5 [regexp {Process:- SwTask3} $results4]
    set results6 [regexp {Process:- SwTask2} $results4]
    set results7 [regexp {Process:- SwTask1} $results4]
    set results8 [regexp {Process:- SwTask0} $results4]

    set results9 [regexp {make request to GTime} $results4]
    set results10 [regexp {CpuScheduler. Resolve:} $results4]

    list $results5 $results6 $results7 $results8 $results9 $results10
} {1 1 1 1 1 1}
