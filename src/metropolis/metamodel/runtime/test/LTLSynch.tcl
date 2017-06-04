# Tests for LTLSynch class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: LTLSynch.tcl,v 1.5 2005/11/22 20:11:46 allenh Exp $
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
test LTLSynch-1.1 {constructor} {
    set ltlSynch [java::new metropolis.metamodel.runtime.LTLSynch \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCH]]
    list \
        [$ltlSynch getContainer] \
        [$ltlSynch getFormula 0] \
        [$ltlSynch getIndex] \
        [$ltlSynch getKind] \
        [$ltlSynch getSource]
} {java0x0 {} -1 8 {}}

######################################################################
####
#
test LTLSynch-1.2 {constructor with bad kind} {
    catch { set ltlSynch \
        [java::new metropolis.metamodel.runtime.LTLSynch -1]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: kind '-1' was out of range, it must be >= 1 and < 1024}}

######################################################################
####
#
test LTLSynch-1.3 {3 arg constructor} {
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]

    set ltlSynch1 [java::new metropolis.metamodel.runtime.LTLSynch \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCH] \
        2]
    list \
        [[$ltlSynch1 getContainer] equals $iNode] \
        [$ltlSynch1 getFormula 0] \
        [$ltlSynch1 getIndex] \
        [$ltlSynch1 getKind] \
        [$ltlSynch1 getSource]
} {1 {} 2 8 {}}

######################################################################
####
#
test LTLSynch-2.1 {checkEvents: no events} {
    # Uses 1.1 above
    catch {        
        $ltlSynch checkEvents
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Error on ltl synch constraint: the constraint must have two synchronized events and event references in equality part must appear in the event list.  Number of events found was: 0}}



######################################################################
####
#
test LTLSynch-2.2 {checkEvents: no events} {
    set ltlSynch2_2 [java::new metropolis.metamodel.runtime.LTLSynch \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCH] \
        3]

    # Add two events        
    set eventBEG [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG]
    $ltlSynch2_2 addEvent $eventBEG

    set eventEND [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventEND]

    $ltlSynch2_2 addEvent $eventEND

    $ltlSynch2_2 checkEvents
    $ltlSynch2_2 show
} {LTL SYNCH Constraint (# 3)
          o Container: myINode
          o Source: 
          o No formulas
          o Event references: 
            - beg(myINode, myINode.myEventBEG)
            - end(myINode, myINode.myEventEND)
          o No quantities
          o No structural values
          o No equality
}

######################################################################
####
#
test LTLSynch-2.3 {checkEvents: 3 events} {
    # Uses 2.2 above
    set eventOTHER [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event OTHER] \
                    $iNode $iNode myEventOTHER]
    $ltlSynch2_2 addEvent $eventOTHER

    catch {        
        $ltlSynch2_2 checkEvents
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Error on ltl synch constraint: the constraint must have two synchronized events and event references in equality part must appear in the event list.  Number of events found was: 3}}

######################################################################
####
#
test LTLSynch-3.1 {getEqualVars setEqualVars} {
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

    set ltlSynch3_1 [java::new metropolis.metamodel.runtime.LTLSynch \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCH]]

    set r1 [$ltlSynch3_1 getEqualVars]
    $ltlSynch3_1 setEqualVars $equalVars
    set r2 [$ltlSynch3_1 getEqualVars]
    $ltlSynch3_1 show
} {LTL SYNCH Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
          o Equality
            "const1" == foo@(none(myINode), i)
}

######################################################################
####
#
test LTLSynch-4.1 {show} {
    set ltlSynch4_1 [java::new metropolis.metamodel.runtime.LTLSynch \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCH]]
    $ltlSynch4_1 show 
} {LTL SYNCH Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
          o No equality
}
