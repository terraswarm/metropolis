# Tests for xscale_yapi
#
# @Author: Christopher Brooks
#
# @Version: $Id: xscale_yapi.tcl,v 1.6 2005/11/22 20:02:47 allenh Exp $
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
#set VERBOSE 1

# Build ../xscale_yapi.x
set TESTDIR [pwd]

cd ..
if [catch {exec -stderrok make all} errMsg] {
    cd $TESTDIR
    error "ProducersConsumer-1.1 failed:  $errMsg"
}
cd $TESTDIR

if {! [ file exists ..[java::field java.io.File separator]xscale.x]} {
    error "[pwd]..[java::field java.io.File separator]xscale.x does not exist"
}

######################################################################
####
#
# Run xscale_yapi.x on a tracefile, expect a known good results
#
proc Xscale_YapiTest {testNumber tracefile results} {
    test Xscale_Yapi-$testNumber "run $tracefile" {
	set results1 [exec -stderrok -keepnewline \
			 "..[java::field java.io.File separator]xscale.x" $tracefile]
	#puts $results1
	regsub {^ *SystemC 2\..*$} $results1 {} results2
	regsub {^ *Copyright.*$} $results2 {} results3
	list $results3
    } $results
}


######################################################################
####
#
Xscale_YapiTest 1.1 ../../object_files/arith2.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/arith2.tr
Mult:1
Mult:1
Fetch--- Cycles: 1368 Stalls:730
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1369 Stalls:730
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1370 Stalls:730
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1371 Stalls:730
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1372 Stalls:730
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1373 Stalls:730
Fetch--- Branches: 96 Mispredicts:0


Cycles: 1374  Stalls: 147  Bubbles: 588
Branches: 96  Mispredicts: 16  BTB_Loads: 48

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.2 ../../object_files/arith-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/arith-core.tr
Fetch--- Cycles: 10 Stalls:3
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 11 Stalls:3
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 12 Stalls:3
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 13 Stalls:3
Fetch--- Branches: 0 Mispredicts:0

Mult:1
Fetch--- Cycles: 15 Stalls:4
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 16 Stalls:4
Fetch--- Branches: 0 Mispredicts:0


Cycles: 17  Stalls: 3  Bubbles: 6
Branches: 0  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.3 ../../object_files/arith.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/arith.tr
Mult:1
Fetch--- Cycles: 1341 Stalls:712
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1342 Stalls:712
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1343 Stalls:712
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1344 Stalls:712
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1345 Stalls:712
Fetch--- Branches: 96 Mispredicts:0

Fetch--- Cycles: 1346 Stalls:712
Fetch--- Branches: 96 Mispredicts:0


Cycles: 1347  Stalls: 136  Bubbles: 581
Branches: 96  Mispredicts: 16  BTB_Loads: 48

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.4 ../../object_files/fib-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/fib-core.tr
Fetch--- Cycles: 375 Stalls:155
Fetch--- Branches: 22 Mispredicts:0

Fetch--- Cycles: 376 Stalls:155
Fetch--- Branches: 22 Mispredicts:0

Fetch--- Cycles: 377 Stalls:155
Fetch--- Branches: 22 Mispredicts:0

Fetch--- Cycles: 378 Stalls:155
Fetch--- Branches: 22 Mispredicts:0

Fetch--- Cycles: 379 Stalls:155
Fetch--- Branches: 22 Mispredicts:0


Cycles: 380  Stalls: 142  Bubbles: 17
Branches: 22  Mispredicts: 1  BTB_Loads: 3

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.5 ../../object_files/fib_mem.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/fib_mem.tr
Fetch--- Cycles: 44 Stalls:24
Fetch--- Branches: 1 Mispredicts:0

Fetch--- Cycles: 45 Stalls:24
Fetch--- Branches: 1 Mispredicts:0

Fetch--- Cycles: 46 Stalls:24
Fetch--- Branches: 1 Mispredicts:0

Fetch--- Cycles: 47 Stalls:24
Fetch--- Branches: 1 Mispredicts:0

Fetch--- Cycles: 48 Stalls:24
Fetch--- Branches: 1 Mispredicts:0


Cycles: 49  Stalls: 4  Bubbles: 24
Branches: 1  Mispredicts: 0  BTB_Loads: 1

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.6 ../../object_files/fib.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/fib.tr
Fetch--- Cycles: 1414 Stalls:722
Fetch--- Branches: 105 Mispredicts:0

Fetch--- Cycles: 1415 Stalls:722
Fetch--- Branches: 105 Mispredicts:0

Fetch--- Cycles: 1416 Stalls:722
Fetch--- Branches: 105 Mispredicts:0

Fetch--- Cycles: 1417 Stalls:722
Fetch--- Branches: 105 Mispredicts:0

Fetch--- Cycles: 1418 Stalls:722
Fetch--- Branches: 105 Mispredicts:0


Cycles: 1419  Stalls: 128  Bubbles: 598
Branches: 105  Mispredicts: 17  BTB_Loads: 48

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.7 ../../object_files/mults-core.tr \
    {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
TRACE FILE IS:../../object_files/mults-core.tr
Mult:1
Mult:1
Mult:1
Mult:1
Fetch--- Cycles: 48 Stalls:17
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 49 Stalls:17
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 50 Stalls:17
Fetch--- Branches: 0 Mispredicts:0

Mult:1
Fetch--- Cycles: 52 Stalls:18
Fetch--- Branches: 0 Mispredicts:0

Fetch--- Cycles: 54 Stalls:19
Fetch--- Branches: 0 Mispredicts:0


Cycles: 55  Stalls: 14  Bubbles: 9
Branches: 0  Mispredicts: 0  BTB_Loads: 0

SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED
}}

######################################################################
####
#
Xscale_YapiTest 1.8 ../../object_files/test1-core.tr \
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
