# Tests for Picture in Picture simple architecture test
#
# @Author: Christopher Brooks
#
# @Version: $Id: simple.tcl,v 1.11 2005/12/09 19:01:37 allenh Exp $
#
# @Copyright (c) 2004 The Regents of the University of California.
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
test simple-1.1 {Create run.x and run it} {
    # runExecutable is defined in $METRO/util/testDefs.tcl
    # and runs run.x for 10 seconds	
    set results [runExecutable run.x 10]

    # Return the last few characters
    # Get rid of of the SystemC and Copyright messages, which are
    # printed on stderr, so appear last here because of a Jacl exec bug.
    set results2 \
	[string range $results [expr [string length $results] - 500] end]
    regsub {^ *SystemC 2\..*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3

    # Check for presense of some key things:
    set results4 [ regexp {SwTask0 running Cpu request beg} $results3 ]
    set results5 [ regexp {SwTask1 running Cpu request beg} $results3 ]
    set results6 [ regexp {SwTask2 running Cpu request beg} $results3 ]
    set results7 [ regexp {SwTask3 running Cpu request beg} $results3 ]
    set results8 [ regexp {Chose task SwTask0} $results3 ]
    set results9 [ regexp {Chose task SwTask1} $results3 ]
    set results10 [ regexp {Chose task SwTask2} $results3 ]
    set results11 [ regexp {Chose task SwTask3} $results3 ]

    list $results4 $results5 $results6 $results7 $results8 $results9 $results10 $results11
} {1 1 1 1 1 1 1 1}
