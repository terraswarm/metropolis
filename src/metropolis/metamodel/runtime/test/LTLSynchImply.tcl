# Tests for LTLSynchImply class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: LTLSynchImply.tcl,v 1.5 2005/11/22 20:11:49 allenh Exp $
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

if {[info procs listToStrings] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

if {[info procs makeINode] == "" } then {
    source makeINode.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test LTLSynchImply-1.1 {constructor} {
    set ltlSynchImply [java::new metropolis.metamodel.runtime.LTLSynchImply \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY]]
    list \
        [$ltlSynchImply getContainer] \
        [$ltlSynchImply getFormula 0] \
        [$ltlSynchImply getIndex] \
        [$ltlSynchImply getKind] \
        [$ltlSynchImply getSource]
} {java0x0 {} -1 16 {}}

######################################################################
####
#
test LTLSynchImply-1.2 {constructor with bad kind} {
    catch { set ltlSynchImply \
        [java::new metropolis.metamodel.runtime.LTLSynchImply -1]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: kind '-1' was out of range, it must be >= 1 and < 1024}}

######################################################################
####
#
test LTLSynchImply-1.3 {3 arg constructor} {
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]

    set ltlSynchImply1 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY] \
        2]
    list \
        [[$ltlSynchImply1 getContainer] equals $iNode] \
        [$ltlSynchImply1 getFormula 0] \
        [$ltlSynchImply1 getIndex] \
        [$ltlSynchImply1 getKind] \
        [$ltlSynchImply1 getSource]
} {1 {} 2 16 {}}

######################################################################
####
#
test LTLSynchImply-1.5 {addEqualVars, getEqualVars} {
    set event1 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent]
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]
    set event2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    $iNode "null" "myEvent2"]

    set equalVars [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" $event1 \
        [java::field metropolis.metamodel.runtime.EqualVars STRINGTYPE] \
        "foo" $event2 \
        [java::field metropolis.metamodel.runtime.EqualVars VARTYPE]]

    set ltlSynch1_5 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY]]

    set equalVars1 [$ltlSynch1_5 getEqualVars]
    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $equalVars1]

    $ltlSynch1_5 addEqualVars $equalVars

    # Add the same equalVar twice
    $ltlSynch1_5 addEqualVars $equalVars

    set equalVars2 [$ltlSynch1_5 getEqualVars]
    set r2 [listToStrings $equalVars1]

    list $r1 $r2 [$ltlSynch1_5 show]
} {{} {{"const1" == foo@(none(myINode), i)} {"const1" == foo@(none(myINode), i)}} {LTL SYNCH IMPLY Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
          o Imply:  => 
          o Equalities
            - "const1" == foo@(none(myINode), i)
            - "const1" == foo@(none(myINode), i)
}}

######################################################################
####
#
test LTLSynchImply-2.1 {addLeftEvent, addRightEvent} {
    set ltlSynchImply2_1 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY] \
        3]

    # Add two events to the left        
    set eventBEGLeft [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEGLeft]
    $ltlSynchImply2_1 addLeftEvent $eventBEGLeft

    set eventENDLeft [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventENDLeft]

    $ltlSynchImply2_1 addLeftEvent $eventENDLeft


    # Add two events to the right        
    set eventBEGRight [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEGRight]
    $ltlSynchImply2_1 addRightEvent $eventBEGRight

    set eventENDRight [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventENDRight]

    $ltlSynchImply2_1 addRightEvent $eventENDRight


    $ltlSynchImply2_1 checkEvents

    list [$ltlSynchImply2_1 show]
} {{LTL SYNCH IMPLY Constraint (# 3)
          o Container: myINode
          o Source: 
          o No formulas
          o Event references: 
            - beg(myINode, myINode.myEventBEGLeft)
            - end(myINode, myINode.myEventENDLeft)
            - beg(myINode, myINode.myEventBEGRight)
            - end(myINode, myINode.myEventENDRight)
          o No quantities
          o No structural values
          o Imply: beg(myINode, myINode.myEventBEGLeft)||end(myINode, myINode.myEventENDLeft) => beg(myINode, myINode.myEventBEGRight)||end(myINode, myINode.myEventENDRight)
          o No equalities
}}

######################################################################
####
#
test LTLSynchImply-2.2 {getLeftEvent, getRightEvent} {
    # uses 2.1 above
    set leftEvents [$ltlSynchImply2_1 getLeftEvents]
    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $leftEvents]

    set rightEvents [$ltlSynchImply2_1 getRightEvents]
    set r2 [listToStrings $rightEvents]

    list $r1 $r2            
} {{{beg(myINode, myINode.myEventBEGLeft)} {end(myINode, myINode.myEventENDLeft)}} {{beg(myINode, myINode.myEventBEGRight)} {end(myINode, myINode.myEventENDRight)}}}


######################################################################
####
#
test LTLSynchImply-4.2 {checkEvents: 2 events} {
    set ltlSynchImply4_2 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY] \
        3]

    # Add two events        
    set eventBEG [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG]
    $ltlSynchImply4_2 addEvent $eventBEG

    set eventEND [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventEND]

    $ltlSynchImply4_2 addEvent $eventEND

    $ltlSynchImply4_2 checkEvents
    $ltlSynchImply4_2 show
} {LTL SYNCH IMPLY Constraint (# 3)
          o Container: myINode
          o Source: 
          o No formulas
          o Event references: 
            - beg(myINode, myINode.myEventBEG)
            - end(myINode, myINode.myEventEND)
          o No quantities
          o No structural values
          o Imply:  => 
          o No equalities
}

######################################################################
####
#
test LTLSynchImply-4.3 {checkEvents: 3 events} {
    # Uses 4.2 above
    set eventOTHER [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event OTHER] \
                    $iNode $iNode myEventOTHER]
    $ltlSynchImply4_2 addEvent $eventOTHER

    catch {        
        $ltlSynchImply4_2 checkEvents
    } errMsg
    list $errMsg
} {{}}

######################################################################
####
#
test LTLSynchImply-5.1 {eventsToLeftEvents} {
    set ltlSynchImply5_1 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY]]

    set leftEvents1 [$ltlSynchImply5_1 getLeftEvents]
    set r1 [listToStrings $leftEvents1]

    set events1 [$ltlSynchImply5_1 getEvents]
    set r2 [listToStrings $events1]

    $ltlSynchImply5_1 eventsToLeftEvents

    set eventBEG [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG]

    $ltlSynchImply5_1 addEvent $eventBEG

    set events3 [$ltlSynchImply5_1 getEvents]
    set r3 [listToStrings $events3]

    $ltlSynchImply5_1 eventsToLeftEvents
    $ltlSynchImply5_1 eventsToLeftEvents

    set leftEvents2 [$ltlSynchImply5_1 getLeftEvents]
    set r4 [listToStrings $leftEvents2]

    set events2 [$ltlSynchImply5_1 getEvents]
    set r5 [listToStrings $events2]

    list $r1 $r2 $r3 $r4 $r5        
} {{} {} {{beg(myINode, myINode.myEventBEG)}} {{beg(myINode, myINode.myEventBEG)}} {}}


######################################################################
####
#
test LTLSynchImply-5.2 {eventsToRightEvents} {
    set ltlSynchImply5_2 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY]]

    set rightEvents1 [$ltlSynchImply5_2 getRightEvents]
    set r1 [listToStrings $rightEvents1]

    set events1 [$ltlSynchImply5_2 getEvents]
    set r2 [listToStrings $events1]

    $ltlSynchImply5_2 eventsToRightEvents

    set eventBEG [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG]

    $ltlSynchImply5_2 addEvent $eventBEG

    set events3 [$ltlSynchImply5_2 getEvents]
    set r3 [listToStrings $events3]

    $ltlSynchImply5_2 eventsToRightEvents
    $ltlSynchImply5_2 eventsToRightEvents

    set rightEvents2 [$ltlSynchImply5_2 getRightEvents]
    set r4 [listToStrings $rightEvents2]

    set events2 [$ltlSynchImply5_2 getEvents]
    set r5 [listToStrings $events2]

    list $r1 $r2 $r3 $r4 $r5        
} {{} {} {{beg(myINode, myINode.myEventBEG)}} {{beg(myINode, myINode.myEventBEG)}} {}}

######################################################################
####
#
test LTLSynchImply-6.1 {show} {
    set ltlSynchImply6_1 [java::new metropolis.metamodel.runtime.LTLSynchImply \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCHIMPLY]]
    $ltlSynchImply6_1 show 
} {LTL SYNCH IMPLY Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
          o Imply:  => 
          o No equalities
}
