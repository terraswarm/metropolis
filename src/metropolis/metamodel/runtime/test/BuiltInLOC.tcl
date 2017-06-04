# Tests for BuiltInLOC class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: BuiltInLOC.tcl,v 1.11 2005/11/22 20:11:59 allenh Exp $
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
test BuiltInLOC-1.1 {constructor} {
    set builtInLOCMINRATE [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE]]
    list \
        [$builtInLOCMINRATE getValue]
} {{}}

######################################################################
####
#
test BuiltInLOC-1.2 {constructor with bad kind} {
    # Note that the error message is thrown in the super class
    catch { set builtInLOCMINRATE \
        [java::new metropolis.metamodel.runtime.BuiltInLOC -1]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: kind '-1' was out of range, it must be >= 1 and < 1024}}

######################################################################
####
#
test BuiltInLOC-1.3 {constructor with bad kind} {
    # Not all Contraint types are legitmate BuiltInLOC types
    catch {java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint LTL]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: kind '1'was out of range. It must be >= MINRATE (32) and <= PERIOD (512)}}

######################################################################
####
#
test BuiltInLOC-1.3 {3 arg constructor} {
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]

    set builtInLOCMINRATE1 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE] \
        2]
    list \
        [[$builtInLOCMINRATE1 getContainer] equals $iNode] \
        [$builtInLOCMINRATE1 getFormula 0] \
        [$builtInLOCMINRATE1 getIndex] \
        [$builtInLOCMINRATE1 getKind] \
        [$builtInLOCMINRATE1 getSource] \
        [$builtInLOCMINRATE1 getValue]
} {1 {} 2 32 {} {}}

######################################################################
####
#
test BuiltInLOC-2.1 {setValue} {
    set builtInLOCMINRATE2 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE]]
    set r1 [$builtInLOCMINRATE2 getValue]
    $builtInLOCMINRATE2 setValue \
        "This string has 'single quotes' and \"double quotes\""
    list $r1 [$builtInLOCMINRATE2 getValue]
} {{} {This string has 'single quotes' and "double quotes"}}


######################################################################
####
#
test BuiltInLOC-3.1 {show} {
    set builtInLOCMINRATE10 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE]]
    $builtInLOCMINRATE10 show
} {LOC MINRATE Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.1.2 {show MAXRATE } {
    set builtInLOCMAXRATE10 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MAXRATE]]
    $builtInLOCMAXRATE10 show
} {LOC MAXRATE Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.1.3 {show MAXDELTA } {
    set builtInLOCMAXDELTA10 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MAXDELTA]]
    $builtInLOCMAXDELTA10 show
} {LOC MAXDELTA Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event1: null
          o Event2: null
          o Value: 
}


######################################################################
####
#
test BuiltInLOC-3.1.4 {show MAXDELTA no events} {
    set builtInLOCMAXDELTA3_1_4 \
        [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MAXDELTA]]
    $builtInLOCMAXDELTA3_1_4 addEvent [java::null]
    # Try to add it twice
    $builtInLOCMAXDELTA3_1_4 addEvent [java::null]
    $builtInLOCMAXDELTA3_1_4 show
} {LOC MAXDELTA Constraint (# -1)
          o Container: null
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event1: null
          o Event2: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.2 {show MINRATE} {
    # builtInLOCMINRATE1 is defined in 1.3 above
    $builtInLOCMINRATE1 show
} {LOC MINRATE Constraint (# 2)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.2.1 {show MINRATE with a null event} {
    # builtInLOCMINRATE1 is defined in 1.3 above
    $builtInLOCMINRATE1 addEvent [java::null]
    $builtInLOCMINRATE1 show
} {LOC MINRATE Constraint (# 2)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.3 {show 1 Event MINRATE } {
    set builtInLOCMINRATE11 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE] \
        11]
    $builtInLOCMINRATE11 setSource "single quote: '"

    # Formulas
    $builtInLOCMINRATE11 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        SYSTEMC_FORMULA] "x=x"
    $builtInLOCMINRATE11 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        TRACE_FORMULA] "y=y"

    # Event
    set eventBEG2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG2]

    $builtInLOCMINRATE11 addEvent $eventBEG2

    # Quantity
    # makeINode is defined in makeINode.tcl
    set iNodeQuantity [makeINode QUANTITY]
    $builtInLOCMINRATE11 addQuantity $iNodeQuantity

    # Structural Values
    $builtInLOCMINRATE11 addStructureValue "getconnectionnum(foo)" \
        [java::new Integer 42]

    $builtInLOCMINRATE11 addStructureValue "getnthconnectionsrc(foo)" \
        $iNode

    #$iPortElem is created in runtime.tcl
    $builtInLOCMINRATE11 addStructureValue "getconnectionport(foo)" \
        $iPortElem

    $builtInLOCMINRATE11 show
} {LOC MINRATE Constraint (# 11)
          o Container: myINode
          o Source: single quote: "
          o Formulas: 
            TRACE_FORMULA : y=y
            SYSTEMC_FORMULA : x=x
          o Structural values: 
            - getconnectionnum(foo) = 42
            - getconnectionport(foo) = myMMPort
            - getnthconnectionsrc(foo) = myINode
          o Quantities: 
            - quantity myMMTypeInterface {
  o Instance name: myINode
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
  o Not refined by a netlist
  o No constraints
}
          o Event: beg(myINode, myINode.myEventBEG2)
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.4 {show 2 events MINRATE} {
    set builtInLOCMINRATE12 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE] \
        12]

    set eventBEG12 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG22]

    $builtInLOCMINRATE12 addEvent $eventBEG12

    # Note that adding this event is ignored because we only print the
    # first event?

    set eventEND12 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventEND22]

    $builtInLOCMINRATE12 addEvent $eventEND12
    $builtInLOCMINRATE12 show
} {LOC MINRATE Constraint (# 12)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event: beg(myINode, myINode.myEventBEG22)
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.5 {show 1 Event MAXDELTA } {
    set builtInLOCMAXDELTA11 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint MAXDELTA] \
        11]
    $builtInLOCMAXDELTA11 setSource "single quote: '"

    # Formulas
    $builtInLOCMAXDELTA11 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        SYSTEMC_FORMULA] "x=x"
    $builtInLOCMAXDELTA11 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        TRACE_FORMULA] "y=y"

    # Event
    set eventBEG2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG2]

    $builtInLOCMAXDELTA11 addEvent $eventBEG2

    # Quantity
    # makeINode is defined in makeINode.tcl
    set iNodeQuantity [makeINode QUANTITY]
    $builtInLOCMAXDELTA11 addQuantity $iNodeQuantity

    # Structural Values
    $builtInLOCMAXDELTA11 addStructureValue "getconnectionnum(foo)" \
        [java::new Integer 42]

    $builtInLOCMAXDELTA11 addStructureValue "getnthconnectionsrc(foo)" \
        $iNode

    #$iPortElem is created in runtime.tcl
    $builtInLOCMAXDELTA11 addStructureValue "getconnectionport(foo)" \
        $iPortElem

    $builtInLOCMAXDELTA11 show
} {LOC MAXDELTA Constraint (# 11)
          o Container: myINode
          o Source: single quote: "
          o Formulas: 
            TRACE_FORMULA : y=y
            SYSTEMC_FORMULA : x=x
          o Structural values: 
            - getconnectionnum(foo) = 42
            - getconnectionport(foo) = myMMPort
            - getnthconnectionsrc(foo) = myINode
          o Quantities: 
            - quantity myMMTypeInterface {
  o Instance name: myINode
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
  o Not refined by a netlist
  o No constraints
}
          o Event1: beg(myINode, myINode.myEventBEG2)
          o Event2: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.6 {show 2 events MAXDELTA} {
    set builtInLOCMAXDELTA12 [java::new metropolis.metamodel.runtime.BuiltInLOC \
        $iNode \
        [java::field metropolis.metamodel.runtime.Constraint MAXDELTA] \
        12]

    set eventBEG12 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event BEG] \
                    $iNode $iNode myEventBEG22]

    $builtInLOCMAXDELTA12 addEvent $eventBEG12


    set eventEND12 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event END] \
                    $iNode $iNode myEventEND22]

    $builtInLOCMAXDELTA12 addEvent $eventEND12
    $builtInLOCMAXDELTA12 show
} {LOC MAXDELTA Constraint (# 12)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event1: beg(myINode, myINode.myEventBEG22)
          o Event2: end(myINode, myINode.myEventEND22)
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.7 {show MINDELTA with two null events} {
    set builtInLOCMINDELTA3_7 \
	[java::new metropolis.metamodel.runtime.BuiltInLOC \
	$iNode \
	[java::field metropolis.metamodel.runtime.Constraint MINDELTA] \
        37]
    $builtInLOCMINDELTA3_7 addEvent [java::null]
    $builtInLOCMINDELTA3_7 addEvent [java::null]
    $builtInLOCMINDELTA3_7 show 
} {LOC MINDELTA Constraint (# 37)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event1: null
          o Event2: null
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.8 {show MINDELTA with one null event and a non-null event} {
    set builtInLOCMINDELTA3_8 \
	[java::new metropolis.metamodel.runtime.BuiltInLOC \
	$iNode \
	[java::field metropolis.metamodel.runtime.Constraint MINDELTA] \
        38]
    $builtInLOCMINDELTA3_8 addEvent [java::null]
    set eventBEG12 [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event BEG] \
		    $iNode $iNode myEventBEG22]

    $builtInLOCMINDELTA3_8 addEvent $eventBEG12
    $builtInLOCMINDELTA3_8 show 
} {LOC MINDELTA Constraint (# 38)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event1: null
          o Event2: beg(myINode, myINode.myEventBEG22)
          o Value: 
}

######################################################################
####
#
test BuiltInLOC-3.9 {show MINDELTA with one non-null event and null event} {
    set builtInLOCMINDELTA3_9 \
	[java::new metropolis.metamodel.runtime.BuiltInLOC \
	$iNode \
	[java::field metropolis.metamodel.runtime.Constraint MINDELTA] \
        39]
    set eventBEG12 [java::call metropolis.metamodel.runtime.Event newEvent \
		   [java::field metropolis.metamodel.runtime.Event BEG] \
		    $iNode $iNode myEventBEG22]

    $builtInLOCMINDELTA3_9 addEvent $eventBEG12
    $builtInLOCMINDELTA3_9 addEvent [java::null]
    $builtInLOCMINDELTA3_9 show 
} {LOC MINDELTA Constraint (# 39)
          o Container: myINode
          o Source: 
          o No formulas
          o No structural values
          o No quantities
          o Event1: beg(myINode, myINode.myEventBEG22)
          o Event2: null
          o Value: 
}
