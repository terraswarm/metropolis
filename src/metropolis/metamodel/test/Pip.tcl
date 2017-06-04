# Tests for the Metroworkspace Picture in Picture example (if it exists)
#
# @Author: Christopher Hylands Brooks
#
# @Version: $Id: Pip.tcl,v 1.4 2005/11/22 20:05:26 allenh Exp $
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

if {[info procs jdkCapture] == "" } then {
    source [ file join $METRO util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set testDir [pwd]
######################################################################
####
#
set pipDirectory \
    [file join $METRO .. metroworkspace examples pip yapi videotop]
if [file isdirectory $pipDirectory] {
    test Pip-1.1a {Picture in Picture example exists} {
	cd $pipDirectory
	# Remove sc_main.cpp so that we can remake files in
	# metro/lib/metamodel/plt/TTL/ if necessary
	file delete -force sc_main.cpp
	puts "Pip-1.1a: running make in videotop . . ."
	set makeResults [exec make all]
	set r1 [file exists run.x]
	cd $testDir
	if { $r1 != 1 } {
	    set r1 $makeResults
	}
	list $r1
    } {1}
} else {
    test Pip-1.1a {Picture in Picture example does not exist} {
	file isdirectory $pipDirectory
    } {1} {Picture in Picture example does not exist} 
}
