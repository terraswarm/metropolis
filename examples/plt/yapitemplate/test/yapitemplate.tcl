# Tests for the Metropolis yapitemplate
#
# @Author: Christopher Brooks
#
# @Version: $Id: yapitemplate.tcl,v 1.4 2005/11/22 20:01:52 allenh Exp $
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
test yapitemplate-1.1 {} {
    # runExecutable is defined in $METRO/util/testDefs.tcl
    # and runs run.x for 2 seconds	
    set results [runExecutable]

    # Return the last few characters
    # Get rid of of the SystemC and Copyright messages, which are
    # printed on stderr, so appear last here because of a Jacl exec bug.
    set results2 \
	[string range $results [expr [string length $results] - 500] end]
    regsub {^ *SystemC 2\..*$} $results2 {} results3
    regsub {^ *Copyright.*$} $results3 {} results4

    # This hack is necessary because of problems with crnl under windows
    set changes [regsub -all [java::call System getProperty "line.separator"] \
		     $results4 "\n" results5]

    set lastChar \
	[string range $results5 [expr [string length $results5] - 1] end]
    if {"$lastChar" != "1"} {
	# FIXME: This is necessary for Cygwin and SystemC-2.1
	puts "yapitemplate-1.1: Last char of results was not '1', (length [string length $results5]), so we are truncating."
	set results5 \
	    [string range $results5 0 [expr [string length $results5] - 2]]
    }


    list \
	[regexp {Finished or Deadlocked!} $results] \
	[regexp {SystemC: simulation stopped by user.} $results] \
	[regexp {run-watchdog: Done} $results] \
	[string range $results5 [expr [string length $results5] - 159] end]
} {1 1 1 {Reading: TheTTLRefinementds 7 1
Writing: TheTTLRefinementds 8 1
Reading: TheTTLRefinementds 8 1
Writing: TheTTLRefinementds 9 1
Reading: TheTTLRefinementds 9 1}}
