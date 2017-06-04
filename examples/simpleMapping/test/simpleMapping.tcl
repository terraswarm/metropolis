# Tests for simpleMapping
#
# @Author: Christopher Hylands Brooks
#
# @Version: $Id: simpleMapping.tcl,v 1.9 2005/11/22 20:04:31 allenh Exp $
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


# Give this test more time
set timeOutSeconds 4800

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
test simpleMapping-1.1 {Create run_arch.x, run_func.x and run_map.x} {

    cd ..	
    set errMsg {}
    puts -nonewline "Running make, generating three binaries . . ."
    if [catch {exec -stderrok make -k} errMsg] {
    	cd test
    	error "simpleMapping-1.1 failed: $errMsg"
    }
    puts "Done" 

    if {! [ file exists .[java::field java.io.File separator]run_arch.x]} {
	error ".[java::field java.io.File separator]run_arch.x does not exist"
    }

    # Let it run for 1 second
    # FIXME: why does the catch throw an error?
    if [catch {set results \
		   [exec -stderrok -sh \
			[file join $METRO util testsuite run-watchdog] -n 1 \
			./run_arch.x]} results] {
	cd test
	puts "simpleMapping-1.1 warning: $errMsg"
    }
    cd $TESTDIR

    #puts $results

    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3

    # This hack is necessary because of problems with crnl under windows
    set changes [regsub -all [java::call System getProperty "line.separator"] \
		     $results3 "\n" results4]

    equalsOneOf [string range $results4 0 512] \
	[list {Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
Task9 read invoked
Task8 write invoked
Task7 read invoked
Task6 write invoked
Task5 write invoked
Task4 write invoked
Task3 read invoked
Task2 read invoked
Task1 read invoked
Task0 write invoked
Task9 read invoked
Task8 write invoked
Task7 read invoked
Task6 read invoked
Task5 write invoked
Task4 read invoked
Task3 write invoked
Task2 read invoked
Task1 write invoked
Task0 read invoke} {Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
Task9 read invoked
Task8 read invoked
Task7 read invoked
Task6 write invoked
Task5 write invoked
Task4 read invoked
Task3 read invoked
Task2 write invoked
Task1 write invoked
Task0 read invoked
Task9 write invoked
Task8 read invoked
Task7 read invoked
Task6 write invoked
Task5 write invoked
Task4 write invoked
Task3 read invoked
Task2 read invoked
Task1 read invoked
Task0 write invoke}]
} {1}


######################################################################
####
#
test simpleMapping-1.2 {Run run_func.x} {
    # runExecutable is defined in $METRO/util/testDefs.tcl
    # and runs run_func.x for 1 second, don't ignore runtime errors, skip make
    set results [runExecutable run_func.x 1 false true]

    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # This hack is necessary because of problems with crnl under windows
    set changes [regsub -all [java::call System getProperty "line.separator"] \
		     $results3 "\n" results4]

    equalsOneOf [string range $results4 0 512] \
	[list {Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
source2 wrote data 0
source1 wrote data 0
source2 wrote data 1
source1 wrote data 1
source2 wrote data 2
source1 wrote data 2
join1 read data 0
source2 wrote data 3
join1 wrote data 0
sink1 read data 0
source1 wrote data 3
source2 wrote data 4
join1 read data 1
join1 wrote data 1
sink1 read data 1
source1 wrote data 4
source2 wrote data 5
join1 read data 2
join1 } {Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
source2 wrote data 0
source1 wrote data 0
source2 wrote data 1
source1 wrote data 1
source2 wrote data 2
source1 wrote data 2
source2 wrote data 3
join1 read data 0
join1 wrote data 0
sink1 read data 0
source1 wrote data 3
source2 wrote data 4
join1 read data 1
join1 wrote data 1
sink1 read data 1
source1 wrote data 4
source2 wrote data 5
join1 read data 2
join1 }]
} {1}

######################################################################
####
#
test simpleMapping-1.3 {Run run_map.x} {
    # runExecutable is defined in $METRO/util/testDefs.tcl
    # and runs run_map.x for 1 second, don't ignore runtime errors, skip make
    set results [runExecutable run_map.x 1]

    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # This hack is necessary because of problems with crnl under windows
    set changes [regsub -all [java::call System getProperty "line.separator"] \
		     $results3 "\n" results4]

    equalsOneOf [string range $results4 0 512] \
	[list {Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
Task1 write invoked
arch write
Task0 write invoked
arch write
Task2 read invoked
arch read
Task1 write invoked
arch write
Task0 write invoked
arch write
Task1 write invoked
arch write
Task2 read invoked
arch read
Task0 write invoked
arch write
Task1 write invoked
arch write
Task2 read invoked
arch read
Task0 write invoked
arch write
Task1 write invoked
arch write} {Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
Task1 write invoked
arch write
Task0 write invoked
arch write
Task2 read invoked
arch read
Task1 write invoked
arch write
Task0 write invoked
arch write
Task2 read invoked
arch read
Task1 write invoked
arch write
Task0 write invoked
arch write
Task1 write invoked
arch write
Task2 read invoked
arch read
Task0 write invoked
arch write
Task1 write invoked
arch write}]
} {1}
