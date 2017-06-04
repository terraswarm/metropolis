# Tests for IPortElem class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: IPortElem.tcl,v 1.8 2005/11/22 20:11:56 allenh Exp $
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
#                                                 PT_COPYRIGHT_VERSION_2
#                                                 COPYRIGHTENDKEY
#######################################################################

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Create commonly used runtime objects
if {[string compare test [info procs createRuntimeObjects]] == 1} then {
    source runtime.tcl
} {}
createRuntimeObjects

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test IPortElem-1.1 {constructor} {
    # Very similar to IPortArray-1.1
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $superClass \
            $superInterfaces]

    # $iPortElem is created in runtime.tcl
    list \
        [$iPortElem numPorts] \
        [$iPortArray2 equals [$iPortElem getArray]] \
        [$iPortElem getElemPortIndex] \
        [$iPortElem show]
} {1 1 0 {myMMTypeInterface myMMPort[1][2]}}

######################################################################
####
#
test IPortElem-2.1 {next} {
    # Uses 1.1 above
    list [[$iPortElem next] toString]
} {{myMMPort[0][1]}}
