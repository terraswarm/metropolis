# Tests for Event class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: Event.tcl,v 1.14 2005/11/22 20:11:50 allenh Exp $
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


if {[info procs makeINode] == "" } then {
    source makeINode.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Event-1.1 {newEvent} {
    set event1 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent]
    list [expr {[$event1 getKind] ==  \
                [java::field metropolis.metamodel.runtime.Event NONE]}] \
        [$event1 getName] \
        [$event1 getNodeObject] \
        [$event1 getProcess] \
        [$event1 getUsed] \
} {1 myEvent java0x0 java0x0 0}


######################################################################
####
#
test Event-1.2 { newEvent with a bogus event kind of -1} {
    catch {[java::call metropolis.metamodel.runtime.Event newEvent \
                   -1 \
                    "null" "null" myEvent]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: kind '-1' was out of range, it must be >= 0 and <= 3}}

######################################################################
####
#
test Event-1.3 {newEvent with a real process} {
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]
    set event2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    $iNode "null" "myEvent2"]

    list [expr {[$event2 getKind] ==  \
                [java::field metropolis.metamodel.runtime.Event NONE]}] \
        [$event2 getName] \
        [$event2 getNodeObject] \
        [[$event2 getProcess] getName] \
        [$event2 getUsed] \
} {1 myEvent2 java0x0 myINode 0}

######################################################################
####
#
test Event-3.1 {addUsed} {
    # Generate a unique name so that we avoid using the cached version
    # if the test is run twice.
    set uniqueName "myEvent3_1[clock clicks]"        
    set event3_1 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" $uniqueName]
    set r1 [ $event3_1 getUsed]
    $event3_1 addUsed [java::field metropolis.metamodel.runtime.Constraint LTL]
    $event3_1 addUsed [java::field metropolis.metamodel.runtime.Constraint PERIOD]
    list $r1 \
        [$event3_1 getUsed]
} {0 513}

######################################################################
####
#
test Event-4.1 { equals} {
    # Same as event1
    set event3 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent]
    # Same as event1, but different name
    set event4 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent4]

    list \
        [$event1 equals [java::null]] \
        [$event1 equals $event1] \
        [$event2 equals $event2] \
        [$event1 equals $event2] \
        [$event2 equals $event1] \
        [$event2 equals "Foo"] \
        [$event1 equals $event3] \
        [$event3 equals $event1] \
        [$event1 equals $event4] \
        [$event4 equals $event1] \
} {0 1 1 0 0 0 1 1 0 0}


######################################################################
####
#
test Event-4.2 { equals with null names} {
    # Same as event1
    set event5 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" [java::null]]
    # Same as event1, but different name
    set event6 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" [java::null]]

    list \
        [$event5 equals $event6] \
        [$event6 equals $event5]
} {1 1}

######################################################################
####
#
test Event-4.3 { equals(Object) with null} {
    list \
        [$event5 {equals metropolis.metamodel.runtime.Event} [java::null]]
} {0}

######################################################################
####
#
test Event-4.3.1 { equals(Event) with null} {
    list \
        [$event5 {equals metropolis.metamodel.runtime.Event} [java::null]]
} {0}

######################################################################
####
#
test Event-5.1 { equals(int, Object, Object)} {
    list \
        [$event1 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           "null" "null" myEvent] \
        [$event1 equals \
           [java::field metropolis.metamodel.runtime.Event OTHER] \
           "null" "null" myEvent] \
        [$event1 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           "null" "null" myOtherEvent]

} {1 0 0}

######################################################################
####
#
test Event-5.2 { equals with null names} {
    # Uses 4.2 above 
    list \
        [$event5 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           "null" "null" [java::null]] \
        [$event5 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           "null" "null" not-null-name] \
} {1 0}

######################################################################
####
#
test Event-5.3 { equals(int, Object, Object), vary the 1st Object field} {
    list \
        [$event1 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           $iNode "null" myEvent] \
        [$event2 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           $iNode "null" myEvent] \
        [$event2 equals \
           [java::field metropolis.metamodel.runtime.Event NONE] \
           $iNode "null" myEvent2]

} {0 0 1}

######################################################################
####
#
test Event-6.1 { getEventName } { 
    list \
        [java::call metropolis.metamodel.runtime.Event getEventName \
                [java::null]] \
        [regexp {e[0-9]*} \
            [java::call metropolis.metamodel.runtime.Event getEventName \
                $event1]]
} {{} 1}

######################################################################
####
#
test Event-6.1 { getEventName } { 
    list \
        [java::call metropolis.metamodel.runtime.Event getEventName \
                [java::null]] \
        [regexp {e[0-9]*} \
            [java::call metropolis.metamodel.runtime.Event getEventName \
                $event1]]
} {{} 1}

######################################################################
####
#
test Event-7.1 { hashCode} { 
    # If objects are equal, then their hashcodes should be the same 
    list \
        [$event1 equals $event3] \
        [expr [$event1 hashCode] == [$event3 hashCode]]
} {1 1}

######################################################################
####
#
test Event-8.1 { locString} { 
    set eventBEG [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG]
    set eventEND [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventEND]

    list [$event1 locString] \
        [$event2 locString] \
        [$eventBEG locString] \
        [$eventEND locString]
} {{} {} BEG_myINode_myINode_myEventBEG END_myINode_myINode_myEventEND}

######################################################################
####
#
test Event-9.1 { show} { 
    set eventNONE [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    $iNode $iNode myEventNONE]

    set eventOTHER [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event OTHER] \
                    $iNode $iNode myEventOTHER]
    list \
        [$event1 show] \
        [$event2 show] \
        [$event3 show] \
        [$eventBEG show] \
        [$eventEND show] \
        [$eventNONE show] \
        [$eventOTHER show]

} {none(all) none(myINode) none(all) {beg(myINode, myINode.myEventBEG)} {end(myINode, myINode.myEventEND)} none(myINode) other(myINode)}


######################################################################
####
#
test Event-10.1 { toString} { 

    list \
        [$event1 toString] \
        [$event2 toString] \
        [$event3 toString] \
        [$eventBEG toString] \
        [$eventEND toString] \
        [$eventNONE toString] \
        [$eventOTHER toString]

} {{} {} {} {beg(myINode, myINode.myEventBEG)} {end(myINode, myINode.myEventEND)} {} {}}
