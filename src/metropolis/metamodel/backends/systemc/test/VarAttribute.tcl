# Tests for VarAttribute.tcl
#
# @Author: Christopher Brooks
#
# @Version: $Id: VarAttribute.tcl,v 1.8 2005/11/22 20:12:09 allenh Exp $
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

######################################################################
####
#
test Test-VarAttribute-1.1 { Construct a VarAttribute and call methods on it} {
    set event [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event NONE] \
		   "null" "null" "null"]
    set varAttribute \
	    [java::new metropolis.metamodel.backends.systemc.VarAttribute \
	    $event myVariableName myType true]
    list \
	    [$varAttribute getVarName] \
	    [$varAttribute getType] \
	    [$varAttribute isNondetVar]
} {myVariableName myType 1}

######################################################################
####
#
test Test-VarAttribute-2.1 {equals} {
    set event [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event NONE] \
		   "null" "null" "null"]
    set event2 [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event OTHER] \
		   "null" "null" "null"]

    set varAttribute \
	    [java::new metropolis.metamodel.backends.systemc.VarAttribute \
	    $event myVariableName myType true]

    list \
	[$varAttribute equals $event myVariableName myType true] \
	[$varAttribute equals $event2 myVariableName myType true] \
	[$varAttribute equals $event myOtherVariableName myType true] \
	[$varAttribute equals $event myVariableName myOtherType true] \
	[$varAttribute equals $event myVariableName myType false]
} {1 0 0 0 0}

######################################################################
####
#
test Test-VarAttribute-3.1 {getEvent} {
    set event [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event NONE] \
		   "null" "null" "null"]

    set varAttribute \
	    [java::new metropolis.metamodel.backends.systemc.VarAttribute \
	    $event myVariableName myType true]

    set newEvent [$varAttribute getEvent]
    list \
	[$event equals $newEvent]
} {1}

######################################################################
####
#
test Test-VarAttribute-3.1 {setEvent} {
    set event [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event NONE] \
		   "null" "null" "null"]

    set event2 [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event OTHER] \
		   "null" "null" "null"]

    set varAttribute \
	    [java::new metropolis.metamodel.backends.systemc.VarAttribute \
	    $event myVariableName myType true]

    $varAttribute setEvent $event2
    set newEvent [$varAttribute getEvent]
    list \
	[$event equals $newEvent] \
	[$event2 equals $newEvent]
} {0 1}
