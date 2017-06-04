# Tests for Constraint class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: Constraint.tcl,v 1.16 2005/11/22 20:12:00 allenh Exp $
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
test Constraint-1.1 {constructor} {
    set constraintLTL [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    list \
        [$constraintLTL getContainer] \
        [$constraintLTL getFormula 0] \
        [$constraintLTL getIndex] \
        [$constraintLTL getKind] \
        [$constraintLTL getSource]
} {java0x0 {} -1 1 {}}

######################################################################
####
#
test Constraint-1.2 {constructor with bad kind} {
    catch { set constraintLTL \
        [java::new metropolis.metamodel.runtime.Constraint -1]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: kind '-1' was out of range, it must be >= 1 and < 1024}}

######################################################################
####
#
test Constraint-1.3 {3 arg constructor} {
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]

    set constraintLTL1 [java::new metropolis.metamodel.runtime.Constraint \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTL] \
        2]
    list \
        [[$constraintLTL1 getContainer] equals $iNode] \
        [$constraintLTL1 getFormula 0] \
        [$constraintLTL1 getIndex] \
        [$constraintLTL1 getKind] \
        [$constraintLTL1 getSource]
} {1 {} 2 1 {}}

######################################################################
####
#
test Constraint-2.1 {addEvent, getEvents} {

    set constraintLTL2 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    set events [$constraintLTL2 getEvents]

    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $events]

    set eventBEG [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG]

    $constraintLTL2 addEvent $eventBEG

    # Add it twice so as to get better coverage
    $constraintLTL2 addEvent $eventBEG
    set events2 [$constraintLTL2 getEvents]
    set r2 [listToStrings $events2]

    set eventEND [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventEND]

    $constraintLTL2 addEvent $eventEND
    set events3 [$constraintLTL2 getEvents]
    set r3 [listToStrings $events2]

    list $r1 $r2 $r3
} {{} {{beg(myINode, myINode.myEventBEG)}} {{beg(myINode, myINode.myEventBEG)} {end(myINode, myINode.myEventEND)}}}

######################################################################
####
#
test Constraint-2.2 {addEvent null Event} {

    set constraintLTL2_2 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]

    $constraintLTL2_2 addEvent [java::null]
    # Try to add it twice
    $constraintLTL2_2 addEvent [java::null]

    set events [$constraintLTL2_2 getEvents]
    list \
        [$events size] \
        [$events get 0] \
        [$constraintLTL2_2 show]
} {1 java0x0 {LTL Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o Event references: 
            - null
          o No quantities
          o No structural values
}}

######################################################################
####
#
test Constraint-3.1 {addFormula, getFormula} {
    set constraintLTL3 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    $constraintLTL3 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        SYSTEMC_FORMULA] "a=b"


    # If a formula with the same key has already been added, then
    #  keep the previous formula, the new formula is <b>not</b> added.

    $constraintLTL3 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        SYSTEMC_FORMULA] "foo=bar"

    list \
        [$constraintLTL3 getFormula \
                [java::field metropolis.metamodel.runtime.Constraint \
                SYSTEMC_FORMULA]] \
        [$constraintLTL3 getFormula \
                [java::field metropolis.metamodel.runtime.Constraint \
                TRACE_FORMULA]]
} {a=b {}}

######################################################################
####
#
test Constraint-3.2 {addFormula key is out of range} {
    set constraintLTL3 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    catch {$constraintLTL3 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
                DEFAULT_FORMULA] "2wrongs==right"} errMsg
    list $errMsg
} {{java.lang.RuntimeException: key '2' was out of range, it must be >= SYSTEMC_FORMULA (0) and < DEFAULT_FORMULA(2)}}

######################################################################
####
#
test Constraint-4.1 {addQuantity, getQuantity} {
    set constraintLTL4 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]

    catch {$constraintLTL4 addQuantity dummyInode} errMsg

    # makeINode is defined in makeINode.tcl
    set iNodeQuantity [makeINode QUANTITY]
    $constraintLTL4 addQuantity $iNodeQuantity

    # Try adding it twice
    $constraintLTL4 addQuantity $iNodeQuantity

    # Add a non-quantity inode to get better coverage.
    # The non-quantity node is not added to the list of quantities
    set iNode4 [makeINode]
    $constraintLTL4 addQuantity $iNode4

    set iNode4 [makeINode QUANTITY CLASS myINode4]
    $constraintLTL4 addQuantity $iNode4

    set quantities [$constraintLTL4 getQuantities]


    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $quantities]

    list $errMsg [$quantities size] $r1
} {{java.lang.RuntimeException: Could not find 'dummyInode' in Network.net. Perhaps you need to call Network.addNode()?} 2 {{quantity myMMTypeInterface {
  o Instance name: myINode
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
  o Not refined by a netlist
  o No constraints
}} {quantity myMMTypeInterface {
  o Instance name: myINode4
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
  o Not refined by a netlist
  o No constraints
}}}}

######################################################################
####
#
test Constraint-5.1 {addStructuredValue, getStructuredValues} {
    set constraintLTL5 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]

    $constraintLTL5 addStructureValue "getconnectionnum(foo)" \
        [java::new Integer 42]

    # Should not be added twice
    $constraintLTL5 addStructureValue "getconnectionnum(foo)" \
        [java::new Integer 42]

    $constraintLTL5 addStructureValue "getnthconnectionsrc(foo)" \
        $iNode

    #$iPortElem is created in runtime.tcl
    $constraintLTL5 addStructureValue "getconnectionport(foo)" \
        $iPortElem


    set structuredValues [$constraintLTL5 getStructureValues]

    set entrySet [$structuredValues entrySet]
    set iter [$entrySet iterator]
    # iterToObjects is defined in enums.tcl
    set list [iterToObjects $iter]
    set r1 [objectsToStrings $list]

    list [$structuredValues size] $r1
} {3 {getconnectionnum(foo)=42 {getconnectionport(foo)=myMMPort[1][2]} {getnthconnectionsrc(foo)=interface myMMTypeInterface {
  o Instance name: myINode
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
  o Not refined by a netlist
  o No constraints
}}}}

######################################################################
####
#
test Constraint-5.2 {addStructuredValue with bogus value} {
    set constraintLTL5_1 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]


    catch {
        $constraintLTL5_1 addStructureValue "getconnectionnum(foo)" \
                [java::null]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: The value parameter must be one of Integer, INode or IPort, it was a: null}}

######################################################################
####
#
test Constraint-6.1 {setContainer} {
    set constraintLTL6 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]

    catch { $constraintLTL6 setContainer [java::null]} errMsg
    # iNode is set in 1.1
    $constraintLTL6 setContainer $iNode
    list $errMsg \
        [[$constraintLTL6 getContainer] equals $iNode]
} {{java.lang.RuntimeException: container was null? Constraint:
LTL Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
} 1}


######################################################################
####
#
test Constraint-7.1 {setIndex} {
    set constraintLTL7 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    set r1 [$constraintLTL7 getIndex]
    $constraintLTL7 setIndex 0
    list $r1 [$constraintLTL7 getIndex]
} {-1 0}

######################################################################
####
#
test Constraint-8.1 {setKind} {
    set constraintLTL8 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    catch {$constraintLTL8 setKind -1} errMsg\

    set r1 [$constraintLTL8 getKind]
    $constraintLTL8 setKind \
        [java::field metropolis.metamodel.runtime.Constraint LOC]

    list $errMsg $r1 [$constraintLTL8 getKind]
} {{java.lang.RuntimeException: kind '-1' was out of range, it must be >= 1 and < 1024} 1 2}

######################################################################
####
#
test Constraint-9.1 {setSource} {
    set constraintLTL9 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    set r1 [$constraintLTL9 getSource]
    $constraintLTL9 setSource \
        "This string has 'single quotes' and \"double quotes\""
    list $r1 [$constraintLTL9 getSource]
} {{} {This string has "single quotes" and "double quotes"}}

######################################################################
####
#
test Constraint-10.1 {show LTL} {
    set constraintLTL10 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    $constraintLTL10 show
} {LTL Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
}

######################################################################
####
#
test Constraint-10.2 {show} {
    # constraintLTL1 is defined in 1.3 above
    $constraintLTL1 show
} {LTL Constraint (# 2)
          o Container: myINode
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
}

######################################################################
####
#
test Constraint-10.3 {show: complex Constraint} {
    set constraintLTL11 [java::new metropolis.metamodel.runtime.Constraint \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint LTL] \
        11]
    $constraintLTL11 setSource "single quote: '"

    # Formulas
    $constraintLTL11 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        SYSTEMC_FORMULA] "x=x"
    $constraintLTL11 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        TRACE_FORMULA] "y=y"

    # Event
    set eventBEG2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG2]

    $constraintLTL11 addEvent $eventBEG2

    # Quantity
    # makeINode is defined in makeINode.tcl
    set iNodeQuantity [makeINode QUANTITY]
    $constraintLTL11 addQuantity $iNodeQuantity

    # Structural Values
    $constraintLTL5 addStructureValue "getconnectionnum(foo)" \
        [java::new Integer 42]

    $constraintLTL5 addStructureValue "getnthconnectionsrc(foo)" \
        $iNode

    #$iPortElem is created in runtime.tcl
    $constraintLTL5 addStructureValue "getconnectionport(foo)" \
        $iPortElem

    $constraintLTL11 show
} {LTL Constraint (# 11)
          o Container: myINode
          o Source: single quote: "
          o Formulas: 
            TRACE_FORMULA : y=y
            SYSTEMC_FORMULA : x=x
          o Event references: 
            - beg(myINode, myINode.myEventBEG2)
          o Quantities: 
            - quantity myMMTypeInterface {
  o Instance name: myINode
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
  o Not refined by a netlist
  o No constraints
}
          o No structural values
}

######################################################################
####
#
test Constraint-10.4 {show ELOC} {
    set constraintELOC [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint ELOC]]
    $constraintELOC show
} {ELOC Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
}


######################################################################
####
#
test Constraint-10.5 {show LTLSYNCH} {
    set constraintLTLSYNCH [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTLSYNCH]]
    $constraintLTLSYNCH show
} {LTL SYNCH Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No event references
          o No quantities
          o No structural values
}

######################################################################
####
#
test Constraint-10.7 {show MINRATE} {
    set constraintMINRATE [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE]]
    $constraintMINRATE show
} {Unknown Constraint 32
}
