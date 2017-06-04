# Tests for INode class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: INode.tcl,v 1.20 2005/11/22 20:11:53 allenh Exp $
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
test INode-1.1 {constructor} {
    # iNodeSource and iNodeDestination are created in runtime.tcl

    list \
            [$iNodeSource show] \
            [$iNodeDestination show] \
            [$iNodeSource getName] \
            [$iNodeDestination getName]
} {{interface myMMTypeInterface {
  o Instance name: myINodeSource
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}} {interface myMMTypeInterface {
  o Instance name: myINodeDestination
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}} myINodeSource myINodeDestination}

######################################################################
####
#
test INode-1.2 {constructor null name} {
    # many of these variables are created in runtime.tcl
    set iNode1_2 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject [java::null] 42]
    # The default name changes each time we run this test, so we check
    # for a regexp
    regexp {myMMTypeInterface[0-9]*} [$iNode1_2 getName]
} {1}


######################################################################
####
#
test INode-1.3 {constructor: type has ports} {
    # many of these variables are created in runtime.tcl
    set mmTypeInterfaceWithPorts \
         [java::new metropolis.metamodel.runtime.MMType \
         myMMTypeInterface \
         [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
         $superClass \
         $superInterfaces]

    # Add two ports so that we get the false branch in getPort()
    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterfaceWithPorts $mmType]
    set mmPort1 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort1 $mmTypeInterfaceWithPorts $mmType]

    # Add a port
    $mmTypeInterfaceWithPorts addPort $mmPort
    $mmTypeInterfaceWithPorts addPort $mmPort1

    set iNode1_3 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterfaceWithPorts iNodeObject iNode1_3 4213]
    $iNode1_3 show
} {interface myMMTypeInterface {
  o Instance name: iNode1_3
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort
       myMMTypeInterface myMMPort1
  o Not refined by a netlist
  o No constraints
}}

######################################################################
####
#

test INode-1.4 {constructor: null type} {
    catch {
        java::new metropolis.metamodel.runtime.INode \
             [java::null] iNodeObject iNode8_1 45
    } errMsg
    list $errMsg
} {{java.lang.NullPointerException: Type argument cannot be null}}

######################################################################
####
#
test INode-2.1 {addCompName, getCompName, removeCompName} {
    set r1 [$iNodeSource getCompName $iNetlist3]
    catch {
        $iNodeSource removeCompName $iNetlist3
    } errMsg
    $iNodeSource addCompName $iNetlist3 iNodeSourceCompName
    set r2 [$iNodeSource getCompName $iNetlist3]
    $iNodeSource removeCompName $iNetlist3
    set r3 [$iNodeSource getCompName $iNetlist3]
    list $r1 $errMsg $r2 $r3
} {null {java.lang.RuntimeException: Node does not have the container 'myINetlist3'} iNodeSourceCompName null}

######################################################################
####
#
test INode-3.1 {addConstraint, getConstraints} {
    set iNode3_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject [java::null] 42]

    set constraints1 [$iNode3_1 getConstraints]
    set r1 [$constraints1 size]

    set constraintLTL [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]
    $iNode3_1 addConstraint $constraintLTL

    set builtInLOCMINRATE [java::new metropolis.metamodel.runtime.BuiltInLOC \
        [java::field metropolis.metamodel.runtime.Constraint MINRATE]]
    $iNode3_1 addConstraint $builtInLOCMINRATE

    set constraints2 [$iNode3_1 getConstraints]

    set constraint0 [java::cast metropolis.metamodel.runtime.Constraint \
                         [$constraints2 get 0]]
    set r2 [[$constraint0 getContainer] equals $iNode3_1]

    set constraint1 [java::cast metropolis.metamodel.runtime.Constraint \
                         [$constraints2 get 1]]
    set r3 [[$constraint0 getContainer] equals $iNode3_1]

    list \
        $r1 $r2 $r3 \
        [$constraint0 getIndex] \
        [$constraint1 getIndex] \
        [expr {[$constraints2 get 0] ==  $constraintLTL}] \
        [expr {[$constraints2 get 1] == $builtInLOCMINRATE}] \
        [[$constraints2 get 0] equals $constraintLTL] \
        [[$constraints2 get 1] equals $builtInLOCMINRATE] \
} {0 1 1 0 1 0 0 1 1}

######################################################################
####
#
test INode-4.1 {addContainer, getContainers, removeContainer} {
    set iNode4_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode4_1 45]

    set containers1 [$iNode4_1 getContainers]
    # iterToObjects is defined in enums.tcl 
    set r1 [iterToObjects $containers1]

    set iNetlist4_1a [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface userObject4_1a  myINetlist4_1a 46]
    set iNetlist4_1b [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface userObject4_1b myINetlist4_1b 47]

    catch {
        $iNode4_1 removeContainer $iNetlist4_1a
    } errMsg

    $iNode4_1 addContainer $iNetlist4_1a
    $iNode4_1 addContainer $iNetlist4_1b

    set r2 [$iNode4_1 show]

    # getContainers returns an iterator on a Set, so it is unordered
    set containers3 [$iNode4_1 getContainers]
    # iterToObjects is defined in enums.tcl 
    set r3 [lsort [objectsToStrings [iterToObjects $containers3]]]

    $iNode4_1 removeContainer $iNetlist4_1a
    $iNode4_1 removeContainer $iNetlist4_1b

    set containers3 [$iNode4_1 getContainers]
    set r4 [objectsToStrings [iterToObjects $containers3]]

    list $r1 $errMsg "\n" $r2 "\n" $r3 $r4
} {{} {java.lang.RuntimeException: Node does not have the container 'myINetlist4_1a'} {
} {interface myMMTypeInterface {
  o Instance name: iNode4_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}} {
} {{interface myMMTypeInterface {
  o Instance name: myINetlist4_1a
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} {interface myMMTypeInterface {
  o Instance name: myINetlist4_1b
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}}} {}}

######################################################################
####
#
test INode-5.1 {addInConnection, getInConnections, removeInConnection} {
    set iNode5_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode5_1 45]

    set connections1 [$iNode5_1 getInConnections]
    set r1 [iterToObjects $connections1]

    # myConnection is created in runtime.tcl
    catch {
        $iNode5_1 removeInConnection $myConnection
    } errMsg

    $iNode5_1 addInConnection $myConnection

    set r2 [$iNode5_1 show]

    set connections2 [$iNode5_1 getInConnections]
    # iterToObjects is defined in enums.tcl 
    set r3 [objectsToStrings [iterToObjects $connections2]]

    $iNode5_1 removeInConnection $myConnection

    set connections3 [$iNode5_1 getInConnections]
    set r4 [objectsToStrings [iterToObjects $connections3]]

    list $r1 $errMsg "\n" $r2 "\n" $r3 $r4
} {{} {java.lang.RuntimeException: Node does not have this input connection 'metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination'} {
} {interface myMMTypeInterface {
  o Instance name: iNode5_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o Input connections:
        - myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination
  o No constraints
}} {
} {{metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}} {}}

######################################################################
####
#
test INode-5.2 {getUserNodes} {
    # Uses 5.1 above
    $iNode5_1 addInConnection $myConnection
    set userNodesSet [$iNode5_1 getUserNodes]
    # listToStrings is defined in enums.tcl
    listToStrings $userNodesSet
} {{interface myMMTypeInterface {
  o Instance name: myINodeSource
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}}}

######################################################################
####
#
test INode-6.1 {addOutConnection, getOutConnections, removeOutConnection} {
    set iNode6_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode6_1 45]

    set connections1 [$iNode6_1 getOutConnections]
    set r1 [iterToObjects $connections1]

    # myConnection is created in runtime.tcl
    catch {
        $iNode6_1 removeOutConnection $myConnection
    } errMsg

    $iNode6_1 addOutConnection $myConnection

    set r2 [$iNode6_1 show]

    set connections2 [$iNode6_1 getOutConnections]
    # iterToObjects is defined in enums.tcl 
    set r3 [objectsToStrings [iterToObjects $connections2]]

    $iNode6_1 removeOutConnection $myConnection

    set connections3 [$iNode6_1 getOutConnections]
    set r4 [objectsToStrings [iterToObjects $connections3]]

    list $r1 $errMsg "\n" $r2 "\n" $r3 $r4
} {{} {java.lang.RuntimeException: Node does not have this output connection 'metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination'} {
} {interface myMMTypeInterface {
  o Instance name: iNode6_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o Output connections:
        - myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination
  o No constraints
}} {
} {{metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}} {}}

######################################################################
####
#
test INode-6.2 {getUsedNodes} {
    # Uses 6.1 above
    $iNode6_1 addOutConnection $myConnection
    set usedNodesSet [$iNode6_1 getUsedNodes]
    # listToStrings is defined in enums.tcl
    listToStrings $usedNodesSet
} {{interface myMMTypeInterface {
  o Instance name: myINodeDestination
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}}}

######################################################################
####
#
test INode-6.3 {isConnectedTo} {

    # Uses 6.2 above
    list \
        [$iNode6_1 isConnectedTo $iNodeSource] \
        [$iNode6_1 isConnectedTo $iNodeDestination] \
        [$iNodeSource isConnectedTo $iNode6_1] \
        [$iNodeDestination isConnectedTo $iNode6_1] \
        [$iNodeDestination isConnectedTo $iNodeSource] \
        [$iNodeSource isConnectedTo $iNodeDestination]
} {0 1 0 0 0 0}

######################################################################
####
#
test INode-6.4 {isConnectedFrom} {

    # Uses 6.2 above
    list \
        [$iNode6_1 isConnectedFrom $iNodeSource] \
        [$iNode6_1 isConnectedFrom $iNodeDestination] \
        [$iNodeSource isConnectedFrom $iNode6_1] \
        [$iNodeDestination isConnectedFrom $iNode6_1] \
        [$iNodeDestination isConnectedFrom $iNodeSource] \
        [$iNodeSource isConnectedFrom $iNodeDestination]

} {0 0 0 1 0 0}

######################################################################
####
#
test INode-7.1 {addScopeConnection, getScopeConnections} {
    set iNode7_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode7_1 45]

    # getScopePorts() is deprecated and calls getScopeConnections()
    #set connections1 [$iNode7_1 getScopeConnections]
    set connections1 [$iNode7_1 getScopePorts]

    set r1 [iterToObjects $connections1]

    # myConnection is created in runtime.tcl
    $iNode7_1 addScopeConnection $myConnection

    set r2 [$iNode7_1 show]

    set connections2 [$iNode7_1 getScopeConnections]
    # iterToObjects is defined in enums.tcl 
    set r3 [objectsToStrings [iterToObjects $connections2]]

    list $r1 $errMsg "\n" $r2 "\n" $r3
} {{} {java.lang.RuntimeException: Node does not have this output connection 'metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination'} {
} {interface myMMTypeInterface {
  o Instance name: iNode7_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}} {
} {{metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}}}

######################################################################
####
#
test INode-8.1 {castToSubType: cast to null and to itself} {
    set iNode8_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode8_1 45]
    set r1 [[$iNode8_1 getType] toString]

    catch {
        $iNode8_1 castToSubType [java::null]
    } errMsg

    # Cast to the same type that we already have
    $iNode8_1 castToSubType $mmTypeInterface

    set r2 [[$iNode8_1 getType] toString]

    list $r1 "\n" $errMsg "\n" $r2
} {{MMType: interface myMMTypeInterface} {
} {java.lang.RuntimeException: Node 'iNode8_1', of type 'interface myMMTypeInterface' cannot be cast to the null subtype.} {
} {MMType: interface myMMTypeInterface}}

######################################################################
####
#
test INode-8.2 {castToSubType} {

    set superInterfaces [java::new java.util.LinkedList]
    $superInterfaces add $mmTypeInterface

    set mmSubTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMSubTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $mmType \
            $superInterfaces]

    set mmSubType [java::new metropolis.metamodel.runtime.MMType \
            mySubMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $mmType \
            $superInterfaces]


    set superInterfaces2 [java::new java.util.LinkedList]
    $superInterfaces2 add $mmSubTypeInterface

    set mmSubSubTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMSubSubTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $mmSubType \
            $superInterfaces2]

    set mmSubSubType [java::new metropolis.metamodel.runtime.MMType \
            mySubSubType \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $mmSubType \
            $superInterfaces]


    set iNode8_2 [java::new metropolis.metamodel.runtime.INode \
                      $mmType iNodeObject iNode8_2 45]

    # Cast a mmType to a mmSubSubType
    set r1 [[$iNode8_2 getType] toString]
    $iNode8_2 castToSubType $mmSubSubType
    set r2 [[$iNode8_2 getType] toString]

    list $r1 $r2
} {{MMType: class myMMType} {MMType: class mySubSubType}}

######################################################################
####
#
test INode-8.3 {castToSubType cast in two steps} {
    # Uses 8.2 above
    set iNode8_3 [java::new metropolis.metamodel.runtime.INode \
                      $mmType iNodeObject iNode8_3 45]

    set r1 [[$iNode8_3 getType] toString]
    $iNode8_3 castToSubType $mmSubType
    set r2 [[$iNode8_3 getType] toString]
    $iNode8_3 castToSubType $mmSubSubType
    set r3 [[$iNode8_3 getType] toString]

    list $r1 $r2 $r3        
} {{MMType: class myMMType} {MMType: class mySubMMType} {MMType: class mySubSubType}}

######################################################################
####
#
test INode-8.4 {castToSubType: try to cast to a parent type} {
    # Uses 8.3 above
    catch {
        $iNode8_3 castToSubType $mmType
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Internal error: Node 'iNode8_3', of type 'class mySubSubType' cannot be an object of type 'class myMMType', because the second type is not a subclass of the first}}


######################################################################
####
#
test INode-8.5 {castToSubType: cast to a type that has ports that are not declared in the new type} {
    # Create a type with a port
    set mmType8_5a [java::new metropolis.metamodel.runtime.MMType \
            myMMType8_5a \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $mmType \
            $superInterfaces]
    set mmPort8_5a [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort8_5a $mmTypeInterface $mmType 2]

    $mmType8_5a addPort $mmPort8_5a


    # Create a subtype with an additional port
    set mmType8_5b [java::new metropolis.metamodel.runtime.MMType \
            myMMType8_5b \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $mmType8_5a \
            $superInterfaces]
    set mmPort8_5b [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort8_5b $mmTypeInterface $mmType8_5b 2]

    $mmType8_5b addPort $mmPort8_5a
    $mmType8_5b addPort $mmPort8_5b


    # Create an INode with 8_5a as a type
    set iNode8_5 [java::new metropolis.metamodel.runtime.INode \
                      $mmType8_5a iNodeObject iNode8_5 45]

    $iNode8_5 castToSubType $mmType8_5b
    $iNode8_5 show
} {class myMMType8_5b {
  o Instance name: iNode8_5
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort8_5b[][]
       myMMTypeInterface myMMPort8_5a[][]
  o Not refined by a netlist
  o No constraints
}}

######################################################################
####
#
test INode-10.1 {getConnection} {
    set iNode10_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode10_1 45]

    # iPortElem is created in runtime.tcl
    set r1 [java::isnull [$iNode10_1 getConnection $iPortElem]]

    # myConnection is created in runtime.tcl
    $iNode10_1 addOutConnection $myConnection

    list $r1 [[$iNode10_1 getConnection $iPortElem] toString]
} {1 {metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}}


######################################################################
####
#
test INode-11.1 {getConnectionFrom} {
    set iNode11_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode11_1 45]

    # iPortElem is created in runtime.tcl
    set connectionsFrom1 [$iNode11_1 getConnectionsFrom $iNode11_1]
    set r1 [$connectionsFrom1 size]


    # myConnection is created in runtime.tcl
    $iNode11_1 addOutConnection $myConnection

    set iNode11_1a [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode11_1a 45]

    set myConnection11_1 [java::new metropolis.metamodel.runtime.Connection \
            $iNode11_1 $iNode11_1a $iPortElem $iNetlist3 [java::null]]

    $iNode11_1 addOutConnection $myConnection11_1

    set connectionsFrom2 [$iNode11_1 getConnectionsFrom $iNode11_1a]
    set r2 [$connectionsFrom2 size]

    set connectionsFrom3 [$iNode11_1a getConnectionsFrom $iNode11_1]
    set r3 [$connectionsFrom3 size]

    set r4 [$myConnection11_1 equals [$connectionsFrom3 get 0]]

    list $r1 $r2 $r3 $r4
} {0 0 1 1}

######################################################################
####
#
test INode-12.1 {getConnectionTo} {
    # Uses 11.1 above
    set connectionsTo2 [$iNode11_1 getConnectionsTo $iNode11_1a]
    set r2 [$connectionsTo2 size]

    set connectionsTo3 [$iNode11_1a getConnectionsTo $iNode11_1]
    set r3 [$connectionsTo3 size]
   
    set r4 [$myConnection11_1 equals [$connectionsTo2 get 0]]

    list $r2 $r3 $r4
} {1 0 1}

######################################################################
####
#
test INode-13.1 {getNthUser} {
    # Uses 11.1 above
    catch {
            set connections1 [$iNode11_1 getNthUser $mmTypeInterface 0]
    } errMsg

    set myConnection13_1 [java::new metropolis.metamodel.runtime.Connection \
            $iNode11_1 $iNode11_1a $iPortElem $iNetlist3 [java::null]]

    $iNode11_1 addInConnection $myConnection13_1
    set connections2 [$iNode11_1 getNthUser $mmTypeInterface 0]
    list $errMsg "\n" [$connections2 toString]\
} {{java.lang.RuntimeException: myMMTypeInterface does not have 0 input connections implementing interface myMMTypeInterface} {
} {metropolis.metamodel.runtime.Connection: iNode11_1 --(myMMTypeInterface myMMPort[1][2])--> iNode11_1a}}


######################################################################
####
#
test INode-13.2 {getNthUser with a different interface} {
    # Uses 11.1 above
    catch {
        set connections13_2 [$iNode11_1 getNthUser $mmTypeInterface2 0]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: myMMTypeInterface does not have 0 input connections implementing interface myMMTypeInterface2}}

######################################################################
####
#
test INode-13.3 {getNthUser with a different interface} {
    # Uses 13.1 above
    set iNode13_3 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface2 iNodeObject iNode13_3 45]


    set mmTypeInterface3 [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface3 \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $superClass \
            $superInterfaces]

    set mmPort3 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort3 $mmTypeInterface3 $mmType 2]

    set iNode3 [java::new metropolis.metamodel.runtime.INode \
                   $mmType MyString MyINode3 666]

    set iPortArray3 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort3 $iNode3]

    set limits [java::new {int[]} 2 ]
    $limits set 0 1
    $limits set 1 2
    $iPortArray2 allocate $limits

    set index [java::new {int[]} 2 ]
    $index set 0 1
    $index set 1 2

    set iPortElem3 [java::new metropolis.metamodel.runtime.IPortElem \
                       $iPortArray3 $index 0]

    set myConnection13_2a [java::new metropolis.metamodel.runtime.Connection \
            $iNode11_1 $iNode13_3 $iPortElem3 $iNetlist3 [java::null]]

    set myConnection13_2b [java::new metropolis.metamodel.runtime.Connection \
            $iNode11_1 $iNode3 $iPortElem3 $iNetlist3 [java::null]]

    # Add two connections
    $iNode11_1 addInConnection $myConnection13_2a
    $iNode11_1 addInConnection $myConnection13_2b

    set connections2 [$iNode11_1 getNthUser $mmTypeInterface3 2]

    list [$connections2 toString]
} {{metropolis.metamodel.runtime.Connection: iNode11_1 --(myMMTypeInterface3 myMMPort3[1][2])--> MyINode3}}

######################################################################
####
#
test INode-14.1 {getNumUsers} {
    # Uses 13.1 above
    list \
        [$iNode11_1 getNumUsers $mmTypeInterface] \
        [$iNode11_1 getNumUsers $mmTypeInterface2] \
        [$iNode11_1 getNumUsers $mmTypeInterface3] \
        [$iNode13_3 getNumUsers $mmTypeInterface] \
        [$iNode13_3 getNumUsers $mmTypeInterface2] \
        [$iNode13_3 getNumUsers $mmTypeInterface3] \
        [$iNode3 getNumUsers $mmTypeInterface] \
        [$iNode3 getNumUsers $mmTypeInterface2] \
        [$iNode3 getNumUsers $mmTypeInterface3]
} {3 0 2 0 0 0 0 0 0}

######################################################################
####
#
test INode-14.2 {getNumUsers with a refined connection} {
    set iNode14_2 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode14_2a 45]

    # This is a refined connection, so it does not count
    set iNode14_2a [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface2 iNodeObject iNode14_2a 45]

    set myConnection14_2a [java::new metropolis.metamodel.runtime.Connection \
            $iNode14_2 $iNode13_3 $iPortElem3 $iNetlist3 [java::null]]


    $iNode14_2 addInConnection $myConnection14_2a

    set iNode14_2b [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface3 iNodeObject iNode14_2b 45]

    set myConnection14_2b [java::new metropolis.metamodel.runtime.Connection \
            $iNode14_2 $iNode14_2b $iPortElem3 $iNetlist3 $myConnection14_2a]

    $iNode14_2 addInConnection $myConnection14_2b

    # myConnection14_2a is refined, so it does not count towards users
    # of myConnection2. 
    # myConnection14_2b implements myTypeInterface1
    list \
        [$iNode14_2 getNumUsers $mmTypeInterface] \
        [$iNode14_2 getNumUsers $mmTypeInterface2] \
        [$iNode14_2 getNumUsers $mmTypeInterface3] \
        [$mmTypeInterface3 implementsInterface $mmTypeInterface] \
        [$myConnection14_2a isRefined] \
        [$myConnection14_2a isRefinement] \
        [$myConnection14_2b isRefined] \
        [$myConnection14_2b isRefinement]
} {1 0 1 1 1 0 0 1}


######################################################################
####
#
test INode-14.3 {getRefinement, isRefined, setRefinement} {
    # Uses $14.2 above
    set iNode14_3 [java::new metropolis.metamodel.runtime.INode \
                      $mmTypeInterface iNodeObject iNode14_3 45]
 
    set r1 [java::isnull [$iNode14_3 getRefinement]]
    set r2 [$iNode14_3 isRefined]

    set iNetlist14_3 [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface userObject4_1a  myINetlist14_3 46]
    $iNode14_3 setRefinement $iNetlist14_3 

    set r3 [[$iNode14_3 getRefinement] show]
    set r4 [$iNode14_3 isRefined]
    list $r1 $r2 $r3 $r4 [$iNode14_3 show]
} {1 0 {interface myMMTypeInterface {
  o Instance name: myINetlist14_3
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} 1 {interface myMMTypeInterface {
  o Instance name: iNode14_3
  o Component name:
  o No ports
  o Refined by netlist myINetlist14_3
  o No constraints
}}}

######################################################################
####
#
test INode-15.1 {getPort} {
    set mmType15_1 [java::new metropolis.metamodel.runtime.MMType \
            myMMType15_1 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort1 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort1 $mmTypeInterface $mmType]
    set mmPort2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort2 $mmTypeInterface $mmType]
    
    $mmType15_1 addPort $mmPort1
    $mmType15_1 addPort $mmPort2

    set iNode15_1 [java::new metropolis.metamodel.runtime.INode \
            $mmType15_1 iNodeObject iNode15_1 42]

    set r1 [[$iNode15_1 getPort myMMPort1] getName]

    # Note that the ports are _copies_ not equals, see the INode ctor.
    set r2 [$mmPort1 equals [$iNode15_1 getPort myMMPort1]]

    set r3 [[$iNode15_1 getPort myMMPort2] getName]
    set r4 [$mmPort2 equals [$iNode15_1 getPort myMMPort2]]

    set r5 [java::isnull [$iNode15_1 getPort NotAPortName]]

    catch {
        $iNode15_1 getPort {myMMPort2]}
    } errMsg1

    catch {
        $iNode15_1 getPort {myMMPort2[}
    } errMsg2

    list $r1 $r2 $r3 $r4 $r5 "\n" $errMsg1 "\n" $errMsg2
} {myMMPort1 0 myMMPort2 0 1 {
} {java.lang.RuntimeException: Port name 'myMMPort2]' is invalid, it must have at least two square brackets with integers between them.} {
} {java.lang.RuntimeException: Port name 'myMMPort2[' is invalid, it must have at least two square brackets with integers between them.}}

######################################################################
####
#
test INode-15.2 {getPort with square brackets: check error messages} {
    set mmType15_2 [java::new metropolis.metamodel.runtime.MMType \
            myMMType15_2 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort15_2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort15_2 $mmTypeInterface $mmType15_2 1]

    $mmType15_2 addPort $mmPort15_2

    set iNode15_2 [java::new metropolis.metamodel.runtime.INode \
            $mmType15_2 iNodeObject iNode15_2 42]

    set iPortArray15_2 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort15_2 $iNode15_2]

    set limits15_2 [java::new {int[]} 1 ]
    $limits15_2 set 0 1
    $iPortArray15_2 allocate $limits15_2

    set index15_2 [java::new {int[]} 1 ]
    $index15_2 set 0 1

    set iPortElem [java::new metropolis.metamodel.runtime.IPortElem \
                       $iPortArray15_2 $index15_2 0]

    catch {
        $iNode15_2 getPort {myMMType15_2[0]}
    } errMsg

    catch {
        $iNode15_2 getPort {myMMPort15_2[0]}
    } errMsg2

    catch {
        $iNode15_2 getPort {myMMPort15_2[-1]}
    } errMsg3
     

    list $errMsg "\n" $errMsg2 "\n" $errMsg3
} {{java.lang.RuntimeException: Port 'myMMType15_2' not found in node 'iNode15_2', must be one of
  o Ports:
       myMMTypeInterface myMMPort15_2[]
} {
} {java.lang.RuntimeException: Array of ports 'myMMTypeInterface myMMPort15_2[]' inside node 'iNode15_2' has not been allocated before trying to access element 'myMMPort15_2[0]', try calling IPortArray.allocate().} {
} {java.lang.RuntimeException: Index '-1' used to allocate port 'myMMPort15_2' is invalid}}

     
######################################################################
####
#
test INode-15.3 {getPort(String) with square brackets: works} {
    # uses 15.2 above

    # FIXME: Crickey! it seems like we cannot easily allocate the port
    # instead of keeping the reference?  It seem like we should be
    # able to have the allocate call on iPortArray15_2 work here?

    # Why do we have to get the port and then allocate instead of allocating
    # first?
    set iPort [$iNode15_2 getPort {myMMPort15_2}]
    set portArray [java::cast metropolis.metamodel.runtime.IPortArray $iPort]
    set r1 [$portArray numPorts]

    set limits15_2a [java::new {int[]} 1 ]
    $limits15_2a set 0 1
    $portArray allocate $limits15_2

    set r2 [$portArray numPorts]
    set foundPort [$iNode15_2 getPort {myMMPort15_2[0]}]
    # FIXME: shouldn't these be equal?
    set r3 [$foundPort equals $iPortArray15_2]
    set r4 [$foundPort show]

    list $r1 $r2 $r3 $r4
} {0 1 0 {myMMTypeInterface myMMPort15_2[0]}}

######################################################################
####
#
test INode-15.4 {getPort(String) with square brackets: out of range} {
    # uses 15.2 above
    catch {
        # The port index 1 is out of range
        $iNode15_2 getPort {myMMPort15_2[1]}
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Array out of bounds in access to array of ports, index[0] == 1, which is >= 1}}


######################################################################
####
#
test INode-15.5 {getPort(String) with square brackets: out of range} {
    # uses 15.2 above
    set iPort [$iNode15_2 getPort {myMMPort15_2}]
    set portArray [java::cast metropolis.metamodel.runtime.IPortArray $iPort]
    set iPortElemArray [$portArray getAllElements]
    # The port index 0 is now null
    $iPortElemArray set 0 [java::null]

    catch {
        $iNode15_2 getPort {myMMPort15_2[0]}
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Array of ports 'myMMTypeInterface myMMPort15_2[1]' inside node 'iNode15_2' is allocated, but it does not have element 'myMMPort15_2[0]'}}

######################################################################
####
#
test INode-16.1 {getPort(String, int[]): port by that name is not a PortArray} {
    set mmType16_2 [java::new metropolis.metamodel.runtime.MMType \
            myMMType16_2 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    # Last argument of MMPort ctor is 0, so this is not an array of ports
    set mmPort16_2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort16_2 $mmTypeInterface $mmType16_2 0]

    $mmType16_2 addPort $mmPort16_2

    set iNode16_2 [java::new metropolis.metamodel.runtime.INode \
            $mmType16_2 iNodeObject iNode16_2 42]

    set iPortScalar [java::new metropolis.metamodel.runtime.IPortScalar \
                       $mmPort16_2 $iNode16_2]

    catch {
        set index16_2 [java::new {int[]} 1 ]
        $index16_2 set 0 0
        $iNode16_2 getPort {myMMPort16_2} $index16_2
    } errMsg
    list $errMsg "\n" [$iPortScalar toString]
} {{java.lang.RuntimeException: Port 'myMMPort16_2' inside node 'iNode16_2' is not an array of ports} {
} {IPortScalar: myMMPort16_2}}

######################################################################
####
#
test INode-20.1 {getScope: look for a port not in scope} {
    set iNode20_1 [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode20_1 42]

    set iPortScalar20_1 [java::new metropolis.metamodel.runtime.IPortScalar \
                       $mmPort2 $iNode20_1]

    java::isnull [$iNode20_1 getScope $iPortScalar20_1]
} {1}

######################################################################
####
#
test INode-20.2 {getScope: look for a port in scope} {
    # Uses 20.1 above

    set iNetlist20_2 [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 $myString3 myINetlist20_2 44]

    set myConnection20_2 [java::new metropolis.metamodel.runtime.Connection \
          $iNode20_1 $iNetlist20_2 $iPortScalar20_1 $iNetlist3 [java::null]]

    $iNode20_1 addScopeConnection $myConnection20_2
    set scope20_1 [$iNode20_1 getScope $iPortScalar20_1]
    list [$iNode20_1 show] [$scope20_1 show]
} {{class myMMType {
  o Instance name: iNode20_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}} {interface myMMTypeInterface2 {
  o Instance name: myINetlist20_2
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}}}

######################################################################
####
#
test INode-20.3 {getScope: look for a port in scope with two ports} {

    # Create two INodes
    set iNode20_3a [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode20_3a 42]

    set iNode20_3b [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode20_3b 42]

    # Create two IPortScalars
    set iPortScalar20_3a [java::new metropolis.metamodel.runtime.IPortScalar \
                       $mmPort2 $iNode20_3a]

    set iPortScalar20_3b [java::new metropolis.metamodel.runtime.IPortScalar \
                       $mmPort2 $iNode20_3b]

    # Create another INetList and add it to the Scope
    set iNetlist20_3a [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 $myString3 myINetlist20_3a 44]

    set myConnection20_3a [java::new metropolis.metamodel.runtime.Connection \
          $iNode20_3a $iNetlist20_3a $iPortScalar20_3a $iNetlist3 [java::null]]

    $iNode20_3a addScopeConnection $myConnection20_3a

    # Create another INetList and add it to the Scope
    set iNetlist20_3b [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 $myString3 myINetlist20_3b 44]

    set myConnection20_3b [java::new metropolis.metamodel.runtime.Connection \
          $iNode20_3a $iNetlist20_3b $iPortScalar20_3b $iNetlist3 [java::null]]

    $iNode20_3a addScopeConnection $myConnection20_3b

    # Get the scopes
    set scope20_3a [$iNode20_3a getScope $iPortScalar20_3a]
    set scope20_3b [$iNode20_3a getScope $iPortScalar20_3b]
    # FIXME: seems like show on iNode20_3a should show the scopes?
    list [$iNode20_3a show] [$scope20_3a show] [$scope20_3b show]
} {{class myMMType {
  o Instance name: iNode20_3a
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}} {interface myMMTypeInterface2 {
  o Instance name: myINetlist20_3a
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} {interface myMMTypeInterface2 {
  o Instance name: myINetlist20_3b
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}}}

######################################################################
####
#
test INode-22.3 {show false false false null} {
    set iNode22_3 [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode22_3 42]
    $iNode22_3 show false false false [java::null]
} {class myMMType {
  o Instance name: iNode22_3
  o Component name:
  o No ports
  o No constraints
}}

######################################################################
####
#
test INode-22.4 {show false false false null with Component Names} {
    set iNode22_4 [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode22_4 42]
    $iNode22_4 addCompName $iNetlist3 iNodeSourceCompName
    $iNode22_4 show false false false [java::null]
} {class myMMType {
  o Instance name: iNode22_4
  o Component name:
        - in netlist myINetlist3: iNodeSourceCompName
  o No ports
  o No constraints
}}
