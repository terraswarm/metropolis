# Tests for the strongarm
#
# @Author: Christopher Hylands Brooks
#
# @Version: $Id: strongarm_yapi.tcl,v 1.6 2005/11/22 20:02:38 allenh Exp $
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

# Build ../strongarm.x
set TESTDIR [pwd]

cd ..
if [catch {exec -stderrok make all} errMsg] {
    cd $TESTDIR
    error "StrongArmYapi-1.1 failed:  $errMsg"
}
cd $TESTDIR

if {! [ file exists ..[java::field java.io.File separator]strongarm.x]} {
    error "[pwd]..[java::field java.io.File separator]strongarm.x does not exist"
}

######################################################################
####
#
# Run strongarm.x on a tracefile, expect a known good results
#
proc StrongArmTest {testNumber tracefile results} {
    test Strongarm-$testNumber "run $tracefile" {
	set results1 [exec -stderrok -keepnewline \
			  ..[java::field java.io.File separator]strongarm.x \
			 $tracefile]
	#puts $results1
	regsub {^ *SystemC 2\..*$} $results1 {} results2
	regsub {^ *Copyright.*$} $results2 {} results3
       # This hack is necessary because of problems with crnl under windows
       set changes \
	    [regsub -all [java::call System getProperty "line.separator"] \
		 $results3 "\n" results4]

	list $results4
    } $results
}


######################################################################
####
#
StrongArmTest 1.1 ../../object_files/arith2.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/arith2.tr
Fetch--- Cycles: 1131 Stalls:493
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1132 Stalls:493
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1133 Stalls:493
Fetch--- Branches: 96 Mispredicts:0


Cycles: 1134  Stalls: 0  Bubbles: 495
Branches: 96  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.2 ../../object_files/arith-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/arith-core.tr
Fetch--- Cycles: 8 Stalls:1
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 9 Stalls:1
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 10 Stalls:1
Fetch--- Branches: 0 Mispredicts:0


Cycles: 11  Stalls: 0  Bubbles: 3
Branches: 0  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.3 ../../object_files/arith.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/arith.tr
Fetch--- Cycles: 1112 Stalls:483
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1113 Stalls:483
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1114 Stalls:483
Fetch--- Branches: 96 Mispredicts:0


Cycles: 1115  Stalls: 0  Bubbles: 485
Branches: 96  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.4 ../../object_files/fib-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/fib-core.tr
Fetch--- Cycles: 242 Stalls:22
Fetch--- Branches: 22 Mispredicts:0

Fetch--- Cycles: 243 Stalls:22
Fetch--- Branches: 22 Mispredicts:0

Fetch--- Cycles: 244 Stalls:22
Fetch--- Branches: 22 Mispredicts:0


Cycles: 245  Stalls: 0  Bubbles: 24
Branches: 22  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.5 ../../object_files/fib_mem.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/fib_mem.tr
Fetch--- Cycles: 39 Stalls:19
Fetch--- Branches: 1 Mispredicts:0

Fetch--- Cycles: 40 Stalls:19
Fetch--- Branches: 1 Mispredicts:0

Fetch--- Cycles: 41 Stalls:19
Fetch--- Branches: 1 Mispredicts:0


Cycles: 42  Stalls: 0  Bubbles: 21
Branches: 1  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.6 ../../object_files/fib.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/fib.tr
Fetch--- Cycles: 1192 Stalls:500
Fetch--- Branches: 105 Mispredicts:0

Fetch--- Cycles: 1193 Stalls:500
Fetch--- Branches: 105 Mispredicts:0

Fetch--- Cycles: 1194 Stalls:500
Fetch--- Branches: 105 Mispredicts:0


Cycles: 1195  Stalls: 0  Bubbles: 502
Branches: 105  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.7 ../../object_files/mults-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/mults-core.tr
Fetch--- Cycles: 36 Stalls:5
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 37 Stalls:5
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 38 Stalls:5
Fetch--- Branches: 0 Mispredicts:0


Cycles: 39  Stalls: 0  Bubbles: 7
Branches: 0  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
StrongArmTest 1.8 ../../object_files/test1-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/test1-core.tr
Fetch--- Cycles: 0 Stalls:0
Fetch--- Branches: 0 Mispredicts:0


Cycles: 1  Stalls: 0  Bubbles: 1
Branches: 0  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}
