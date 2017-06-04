# Tests for dining_phil_live
#
# @Author: Christopher Brooks
#
# @Version: $Id: dining_phil_live.tcl,v 1.10 2005/11/22 20:02:56 allenh Exp $
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
test dining_phil_live-1.1 {Create run.x and run it} {
    # Note that if the simulation runs too fast, 
    # then we might get a java.lang.OutOfMemoryError 

    # runExecutable is defined in $METRO/util/testDefs.tcl
    # and runs run.x for 2 second and ignores runtime errors

    set results [runExecutable run.x 2 true]

    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    regsub -all {cps.*$} $results3 {cpsXXX} results4
    regsub -all {Phil[0-4]} $results4 {PhilN} results5
    regsub -all {Chopstick[0-4]} $results5 {ChopstickN} results6

    # Under Cygwin with SystemC 2.1, the output is non-det, so we 
    # just check for strings
    list [regexp {PhilN: Start thinking 1} $results6] \
	[regexp {PhilN: Finished thinking 1} $results6] \
	[regexp {PhilN try to use the ChopstickN} $results6] \
	[regexp {PhilN: got ChopstickN} $results6] \
	[regexp {PhilN: Got both  cpsXXX} $results6] \
	[regexp {PhilN: Start eating} $results6] \
	[regexp {PhilN try to release the ChopstickN} $results6] \
	[regexp {PhilNreleased Chopstick} $results6]
} {1 1 1 1 1 1 1 1}

