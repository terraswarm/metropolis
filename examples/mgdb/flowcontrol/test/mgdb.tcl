# Tests for mgdb "next" command in flow-control constructs
#
# @Author: Christopher Brooks, Allen Hopkins
#
# @Version: $Id: mgdb.tcl,v 1.7 2005/11/22 20:02:30 allenh Exp $
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
test mgdb_flowcontrol-1.l {verify the version number of the underlying gdb} {
    set results [exec $METRO/bin/mgdb --version]
    set firstLine [string range $results 0 [expr {[string first {Copyright} [split $results]] -2 }]]
    if [regexp { 5.} $firstLine] {
	error "mgdb requires GDB 6.0 or later, you are running $firstLine"
    }
    
} {}

######################################################################
####
#
test mgdb_flowcontrol-2.l {Create run.x and constrN.check} {
    cd ..	
    puts "mgdb_flowcontrol-2.l: make started"
    set errMsg {}
    if [catch {exec -stderrok make -k} errMsg] {
	cd test
	error "mgdb_flowcontrol-2.l failed: $errMsg"
    }
    puts "mgdb_flowcontrol-2.l: make complete"
    set seconds 180
    puts "mgdb_flowcontrol-2.l: running test for $seconds seconds"
    if [catch {set results [exec -stderrok \
		[file join $METRO util testsuite run-watchdog] \
				-n $seconds \
				./testflowcontrol]} errMsg] {
	cd test
	error "mgdb_flowcontrol-2.l failed: $errMsg"
    }
    cd $TESTDIR
    list $results
} {{While-loop test:
Tests.mmm:51
Tests.mmm:52
Tests.mmm:53
Tests.mmm:55
Tests.mmm:56
Tests.mmm:57
Tests.mmm:58
Tests.mmm:59
Tests.mmm:56
Tests.mmm:57
Tests.mmm:58
Tests.mmm:59
Tests.mmm:56
Tests.mmm:61
Tests.mmm:44
Do-loop test:
Tests.mmm:64
Tests.mmm:65
Tests.mmm:67
Tests.mmm:69
Tests.mmm:70
Tests.mmm:71
Tests.mmm:69
Tests.mmm:70
Tests.mmm:71
Tests.mmm:69
Tests.mmm:70
Tests.mmm:71
Tests.mmm:72
Tests.mmm:45
For-loop test:
Tests.mmm:75
Tests.mmm:76
Tests.mmm:77
Tests.mmm:79
Tests.mmm:80
Tests.mmm:81
Tests.mmm:80
Tests.mmm:81
Tests.mmm:80
Tests.mmm:81
Tests.mmm:80
Tests.mmm:81
Tests.mmm:82
Tests.mmm:80
Tests.mmm:81
Tests.mmm:82
Tests.mmm:86
Tests.mmm:87
Tests.mmm:87
Tests.mmm:87
Tests.mmm:89
Tests.mmm:46
Switch test:
Tests.mmm:92
Tests.mmm:93
Tests.mmm:94
Tests.mmm:96
Tests.mmm:97
Tests.mmm:113
Tests.mmm:114
Tests.mmm:96
Tests.mmm:97
Tests.mmm:99
Tests.mmm:102
Tests.mmm:96
Tests.mmm:97
Tests.mmm:113
Tests.mmm:114
Tests.mmm:96
Tests.mmm:97
Tests.mmm:104
Tests.mmm:105
Tests.mmm:96
Tests.mmm:97
Tests.mmm:113
Tests.mmm:114
Tests.mmm:96
Tests.mmm:97
Tests.mmm:107
Tests.mmm:108
Tests.mmm:96
Tests.mmm:97
Tests.mmm:113
Tests.mmm:114
Tests.mmm:96
Tests.mmm:97
Tests.mmm:110
Tests.mmm:111
Tests.mmm:96
Tests.mmm:97
Tests.mmm:113
Tests.mmm:114
Tests.mmm:96
Tests.mmm:117
Tests.mmm:47
Tests.mmm:48
Runner.mmm:38
-
run-watchdog: Done}}
