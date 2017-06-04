# Tests for Network class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: Network.tcl,v 1.37 2005/11/22 20:11:51 allenh Exp $
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

# Current instance of the network that we are working with.
set net [java::field metropolis.metamodel.runtime.Network net]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Network-1.1 {addAnnotation getAnnotation} {
    set net1_1 [java::new metropolis.metamodel.runtime.Network]
    set r1 [[$net1_1 getAnnotations] size]

    # Null events are not added 
    $net1_1 addAnnotation [java::null] [java::null]
    set r2 [[$net1_1 getAnnotations] size]

    # Add an event
    set event1_1 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent1_1]
    $net1_1 addAnnotation $event1_1 myEvent1_1

    set r3 [[$net1_1 getAnnotations] size]

    # Look up the event and get the variable name
    set variableName [[$net1_1 getAnnotations] get $event1_1]
    list $r1 $r2 $r3 [$variableName toString]
} {0 0 1 {[myEvent1_1]}}

######################################################################
####
#
test Network-1.2 {show} {
    # Uses 1.1 above
    $net1_1 show
} {Top-Level netlist is null

### List of annotations ###
   o none(all) myEvent1_1

### List of variables ###
myEvent1_1	

### List of constraints ###
}

######################################################################
####
#
test Network-1.3 {addAnnotation: Try to add the same annotation again} {
    set r1 [[$net1_1 getAnnotations] size]
    # This annotation is already present
    $net1_1 addAnnotation $event1_1 myEvent1_1
    set r2 [[$net1_1 getAnnotations] size]
    set variableName [[$net1_1 getAnnotations] get $event1_1]
    list $r1 $r2 [$variableName toString]
} {1 1 {[myEvent1_1]}}

######################################################################
####
#
test Network-1.4 {addAnnotation: Try to add a different annotation} {
    set r1 [[$net1_1 getAnnotations] size]
    # This annotation has the same event, different variable name
    $net1_1 addAnnotation $event1_1 myEvent1_1_DifferentName
    # Increase code coverage inside the while loop by searching for a name.
    $net1_1 addAnnotation $event1_1 myEvent1_1_3rdName
    set r2 [[$net1_1 getAnnotations] size]
    set variableName [[$net1_1 getAnnotations] get $event1_1]
    list $r1 $r2 [$variableName toString]
} {1 1 {[myEvent1_1, myEvent1_1_DifferentName, myEvent1_1_3rdName]}}

######################################################################
####
#
test Network-1.5 {addAnnotation: Try to add a different event} {
    # Add an event
    set event1_2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent1_2]
    $net1_1 addAnnotation $event1_2 myEvent1_2
    set r1 [[$net1_1 getAnnotations] size]
    set variableName [[$net1_1 getAnnotations] get $event1_2]
    list $r1 [$variableName toString]
} {2 {[myEvent1_2]}}



######################################################################
####
#
test Network-2.1 {addComponent(Object, Object, String)} {
    set net2_1 [java::new metropolis.metamodel.runtime.Network]    

    # mmTypeInterface is set in runtime.tcl
    set iNode2_1 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject2_1 iNode2_1 42]

    catch {
	# Need to add the nodeObject first
	$net2_1 addComponent "NodeObject String" "NetList String" \
	    "CompName String"
    } errMsg


    $net2_1 addNode "NodeObject String" $iNode2_1

    catch {
	# Need to add the netlist
	$net2_1 addComponent "NodeObject String" "NetList String2" \
	    "CompName String"
    } errMsg2

    $net2_1 addNode "NetList String2" $iNode2_1
    catch {
	# NetList must be an INetlist
	$net2_1 addComponent "NodeObject String" "NetList String2" \
	    "CompName String"
    } errMsg3

    # Ok, add a component
    set iNetlist2_1a [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 myString2_1a myINetlist2_1a 44]
    set iNetlist2_1b [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 myString2_1b myINetlist2_1b 44]

    $net2_1 addNode $iNetlist2_1a $iNetlist2_1b

    $net2_1 addComponent "NodeObject String" $iNetlist2_1a \
	"CompName String"

    # Call getComponent
    set r1 [$net2_1 getComponent $iNetlist2_1b "CompName String"]

    list $errMsg "\n" $errMsg2 "\n" $errMsg3 "\n" $r1
} {{java.lang.RuntimeException: Could not find node 'NodeObject String' in the node instances. Perhaps it has not been added with addNode()?} {
} {java.lang.RuntimeException: Could not find netlist 'NetList String2' in the node instances. Perhaps it has not been added with addNode()?} {
} {java.lang.RuntimeException: Object 'interface myMMTypeInterface {
  o Instance name: iNode2_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}' is a node of the network, but it is not a netlist, it is a metropolis.metamodel.runtime.INode} {
} iNodeObject2_1}

######################################################################
####
#
test Network-2.5 {addComponent(INode, INetlist, String), compName length == 0} {
    # Uses 2.1 above
    set iNetlist2_5 [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 myString2_5 myINetlist2_5 44]
    $net2_1 addNode $iNetlist2_5 $iNetlist2_1b

    # Add a component with a compName that has a length of 0
    $net2_1 addComponent "NodeObject String" $iNetlist2_5 \
	""

    # Get the constructed name
    $iNode2_1 getCompName $iNetlist2_1b    
} {myMMTypeInterface_Comp_0}

######################################################################
####
#
test Network-2.6.1 {addComponent(INode, INetlist, String), non-unique name} { 
    set net2_6 [java::new metropolis.metamodel.runtime.Network]    
    set iNetlist2_6a [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 myString2_6b myINetlist2_6a 44]

    set iNetlist2_6b [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 myString2_6b myINetlist2_6b 44]

    $net2_6 addNode "NodeObject2_6 String" $iNetlist2_6b
    $net2_6 addNode $iNetlist2_6a $iNetlist2_6b

    $net2_6 addComponent "NodeObject2_6 String" $iNetlist2_6a "UniqueCompName"
    catch {
	$net2_6 addComponent "NodeObject2_6 String" $iNetlist2_6a "UniqueCompName"
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: addcomponent: component name 'UniqueCompName' for node 'myINetlist2_6b' is not unique in netlist 'myINetlist2_6b'.}}


######################################################################
####
#
test Network-2.6.2 {getComponent} { 
    # Uses 2.6.1 above
    set r1 [$net2_6 getComponent $iNetlist2_6b "UniqueCompName"]
    set r2 [[$net2_6 getNode "myINetlist2_6b" ] getName]
    set r3 [[$net2_6 getNetlist "myINetlist2_6b" ] getName]

    set r4 [$net2_6 getComponent [$net2_6 getNetlist "myINetlist2_6b"] \
		"UniqueCompName"]

    set r5 [$net2_6 getComponent \
		[java::cast metropolis.metamodel.runtime.INetlist \
		     [$net2_6 getNode "myINetlist2_6b"]] \
		"UniqueCompName"]

    set r6 [$net2_6 {getComponent String String} myINetlist2_6b \
		"UniqueCompName"]

    # "NotAUniqueCompName" is not a valid coponent name
    set r7 [$net2_6 getComponent \
		[java::cast metropolis.metamodel.runtime.INetlist \
		     [$net2_6 getNode "myINetlist2_6b"]] \
		"NotAUniqueCompName"]

    # getComponent(INetlist, String)
    set r8 [$net2_6 \
	{getComponent metropolis.metamodel.runtime.INetlist String} \
	$iNetlist2_6b "UniqueCompName"]

    # getComponent(String, String), netlist not found
    set r9 [$net2_6 getComponent "not a inetlist name" "UniqueCompName"]

    # getComponent(Object, String)
    set r10 [$net2_6 {getComponent Object String} \
	"NodeObject2_6 String" UniqueCompName]

    list $r1 $r2 $r3 $r4 $r5 $r6 [java::isnull $r7] $r8 [java::isnull $r9] $r10
} {myString2_6b myINetlist2_6b myINetlist2_6b myString2_6b myString2_6b myString2_6b 1 myString2_6b 1 myString2_6b}

######################################################################
####
#
test Network-2.7 {addConstraint} { 
    set net2_7 [java::new metropolis.metamodel.runtime.Network]    
    set constraintLTL [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LTL]]

    set r1 [[$net2_7 getConstraints] size]	

    $net2_7 addConstraint $constraintLTL	
    $net2_7 addConstraint $constraintLTL	

    set r2 [[$net2_7 getConstraints] size]	    

    # listToStrings is defined in enums.tcl
    regsub -all {@[a-f0-9]*} [listToStrings [$net2_7 getConstraints]] {@xxx} r3
    list $r1 $r2 $r3
} {0 2 {metropolis.metamodel.runtime.Constraint@xxx metropolis.metamodel.runtime.Constraint@xxx}}

######################################################################
####
#
test Network-2.7.1 {addConstraint with a null} { 
    $net2_7 addConstraint [java::null]
    # does nothing, just returned.
} {}

######################################################################
####
#
test Network-2.8 {buildName} { 
    set net2_8 [java::new metropolis.metamodel.runtime.Network]    
    # mmType is defined in runtime.tcl
    set r1 [$net2_8 buildName $mmType false]
    set r2 [$net2_8 buildName $mmType false]
    set r3 [$net2_8 buildName $mmType true]
    list $r1 $r2 $r3
} {myMMType0 myMMType1 myMMType_Comp_0}

######################################################################
####
#
test Network-3.1 {addNode, getNode*} {
    set net3_1 [java::new metropolis.metamodel.runtime.Network]    

    # mmTypeInterface is set in runtime.tcl
    set iNode3_1 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject iNode3_1 42]
    set r1 [java::isnull [$net3_1 getNode $iNode3_1]]
    set r2 [[$net3_1 getNodes] hasNext]
    set r3 [[$net3_1 getNodeInstances] hasNext]

    $net3_1 addNode [java::new Double 3.1] $iNode3_1
    
    # getNode(Object)
    set r4 [[$net3_1 getNode [java::new Double 3.1]] getName]

    # getNode(String)
    set r5 [[$net3_1 getNode iNode3_1] getName]

    list $r1 $r2 $r3 $r4 $r5
} {1 0 0 iNode3_1 iNode3_1}

######################################################################
####
#
test Network-3.2 {getNodes, getNodeInstances} {
    # Uses 3.1 above

    # objectsToStrings and iterToObjects is defined in enums.tcl
    set r1 [objectsToStrings [iterToObjects [$net3_1 getNodes]]]

    set r2 [objectsToStrings [iterToObjects [$net3_1 getNodeInstances]]]

    list $r1 $r2
} {{{interface myMMTypeInterface {
  o Instance name: iNode3_1
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}}} 3.1}


######################################################################
####
#
test Network-3.3 {getNodeSafely} {
    # getNodeSafely(Object)
    set r1 [[$net3_1 getNodeSafely [java::new Double 3.1]] getName]

    # getNodeSafely(String)
    set r2 [[$net3_1 getNodeSafely iNode3_1] getName]
    
    catch {$net3_1 getNodeSafely [java::new Double 666.6]} errMsg
    catch {$net3_1 getNodeSafely "Not an inode"} errMsg2
    list $r1 $r2 "\n" $errMsg "\n" $errMsg2
} {iNode3_1 iNode3_1 {
} {java.lang.RuntimeException: Could not find node '666.6' in the node instances. Perhaps it has not been added with addNode()?} {
} {java.lang.RuntimeException: Could not find a node named 'Not an inode' in the node instances. Perhaps it has not been added with addNode()?}}

######################################################################
####
#
test Network-4.1 {addType, getType} {
    set net4_1 [java::new metropolis.metamodel.runtime.Network]
    catch {
        $net4_1 getType myMMTypeInterface
    } errMsg

    $net4_1 addType $mmTypeInterface
    $net4_1 addType $mmTypeInterface
    set r2 [[$net4_1 getType myMMTypeInterface] getName]
    list $errMsg $r2	
} {{java.lang.RuntimeException: type myMMTypeInterface not declared. There are no named type objects, try calling addType(MMType) first.} myMMTypeInterface}

######################################################################
####
#
test Network-5.1 {addVariable} {
    set net5_1 [java::new metropolis.metamodel.runtime.Network]
    set r1 [[$net5_1 getVariables] size]    

    # Try adding null
    $net5_1 addVariable [java::null]
    set r2 [[$net5_1 getVariables] size]    

    $net5_1 addVariable "myVariable"

    # Try to add it twice
    $net5_1 addVariable "myVariable"

    set r3 [[$net5_1 getVariables] size]    
    set variables [$net5_1 getVariables]

    list $r1 $r2 $r3 [$variables toString]
} {0 0 1 {[myVariable]}}

######################################################################
####
#
test Network-7.1 {connect(INode, IPort, INode} {
    set net7_1 [java::new metropolis.metamodel.runtime.Network]
    set iNode7_1src [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode7_1src 42]
    set iNode7_1dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject iNode7_1dst 42]

    set iPortScalar7_1 [java::new metropolis.metamodel.runtime.IPortScalar \
			 $mmPort2 $iNode7_1src]
    $net7_1 connect $iNode7_1src $iPortScalar7_1 $iNode7_1dst
    list [$net7_1 show] "\n" [$iNode7_1src show] "\n" [$iNode7_1dst show]
} {{Top-Level netlist is null

### List of annotations ###

### List of variables ###


### List of constraints ###
} {
} {class myMMType {
  o Instance name: iNode7_1src
  o Component name:
  o No ports
  o Not refined by a netlist
  o Output connections:
        - iNode7_1src --(myMMTypeInterface myMMPort[][])--> iNode7_1dst
  o No constraints
}} {
} {interface myMMTypeInterface {
  o Instance name: iNode7_1dst
  o Component name:
  o No ports
  o Not refined by a netlist
  o Input connections:
        - iNode7_1src --(myMMTypeInterface myMMPort[][])--> iNode7_1dst
  o No constraints
}}}

######################################################################
####
#
test Network-7.2 {connect(INode, IPort, INode) port does not belong to src } {
    # Uses 7.1 above
    set iNode7_2 [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode7_1src 42]

    # This port belongs to iNode7_2, though we try to connect with iNode7_1
    set iPortScalar7_2 [java::new metropolis.metamodel.runtime.IPortScalar \
			 $mmPort2 $iNode7_2]

    catch {
	$net7_1 connect $iNode7_1src $iPortScalar7_2 $iNode7_1dst
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: connect: port 'myMMTypeInterface myMMPort[][]' does not belong to node 'iNode7_1src', but to node 'iNode7_1src'}}

######################################################################
####
#
test Network-7.3 {connect(INode, IPort, INode) does not implement interface defined by the port } {
    set net7_3 [java::new metropolis.metamodel.runtime.Network]
    set iNode7_3src [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode7_3src 42]

    # Note that the type of this is mmType, not mmTypeInterface
    set iNode7_3dst [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode7_3dst 42]

    set iPortScalar7_3 [java::new metropolis.metamodel.runtime.IPortScalar \
			 $mmPort2 $iNode7_3src]

    
    catch {
	$net7_3 connect $iNode7_3src $iPortScalar7_3 $iNode7_3dst
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: connect: target 'iNode7_3dst' of type 'myMMType' does not implement the interface 'myMMTypeInterface' of the port 'myMMPort'}}


######################################################################
####
#
test Network-7.4 {connect(INode, IPort, INode) Connection already using this port } {
    # Uses 7.1 above
    set iNode7_4dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject iNode7_4dst 42]
    catch {

	$net7_1 connect $iNode7_1src $iPortScalar7_1 $iNode7_4dst
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: connect: port 'myMMTypeInterface myMMPort[][]' already connected to node 'iNode7_1dst', so it cannot be connected again to another node, 'iNode7_4dst'}}

######################################################################
####
#
test Network-7.5.1 {connect(INode, IPort, INode) Connection to self} {
    set net7_5 [java::new metropolis.metamodel.runtime.Network]
    set iNode7_5srcAndDst [java::new metropolis.metamodel.runtime.INode \
		       $mmTypeInterface iNodeObject iNode7_5srcAndDst 42]

    set iPortScalar7_5 [java::new metropolis.metamodel.runtime.IPortScalar \
			 $mmPort2 $iNode7_5srcAndDst]


    $net7_5 connect $iNode7_5srcAndDst $iPortScalar7_5 $iNode7_5srcAndDst
    list [$iNode7_5srcAndDst show]
} {{interface myMMTypeInterface {
  o Instance name: iNode7_5srcAndDst
  o Component name:
  o No ports
  o Not refined by a netlist
  o Input connections:
        - iNode7_5srcAndDst --(myMMTypeInterface myMMPort[][])--> iNode7_5srcAndDst
  o Output connections:
        - iNode7_5srcAndDst --(myMMTypeInterface myMMPort[][])--> iNode7_5srcAndDst
  o No constraints
}}}

######################################################################
####
#
test Network-7.5.2 {connect Object, Object, Object} {
    set net7_5_2 [java::new metropolis.metamodel.runtime.Network]

    set mmType7_5_2 [java::new metropolis.metamodel.runtime.MMType \
            myMMType7_5_2 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort7_5_2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort7_5_2 $mmTypeInterface $mmType7_5_2 0]
    $mmType7_5_2 addPort $mmPort7_5_2

    set iNode7_5_2src [java::new metropolis.metamodel.runtime.INode \
            $mmType7_5_2 iNodeObject iNode7_5_2src 42]
    set iNode7_5_2dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject iNode7_5_2dst 42]

    set iNetlist7_5_2Name [java::new String "myINetlist7_5_2"]
    set iNetlist7_5_2 [java::new metropolis.metamodel.runtime.INetlist \
            $mmType7_5_2 myString7_5_2 $iNetlist7_5_2Name 44]

    # NetworkTest has a field called __pointer_myMMPort7_5_2, which
    # is used by _connectPortPointer(), which is eventually called by connect()
    set networkTest7_5_2 \
	[java::new metropolis.metamodel.runtime.test.NetworkTest \
	     $mmType7_5_2 networkTest7_5_2Object networkTest7_5_2 50]

    $net7_5_2 addNode $networkTest7_5_2 $iNetlist7_5_2
    set r1 [[$net7_5_2 {getNode Object} $networkTest7_5_2] getName]


    # FIXME: it seems odd to add a Node as itself here? 
    $net7_5_2 addNode $iNode7_5_2dst $iNode7_5_2dst

    $net7_5_2 {connect Object Object Object} \
	$networkTest7_5_2 myMMPort7_5_2 $iNode7_5_2dst

    equalsOneOf \
	[list [$net7_5_2 show] "\n" [$iNetlist7_5_2 show]] \
	[list \
	     {{Top-Level netlist is null

class myMMType7_5_2 {
  o Instance name: myINetlist7_5_2
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o Output connections:
        - myINetlist7_5_2 --(myMMTypeInterface myMMPort7_5_2)--> iNode7_5_2dst
  o No constraints
}

interface myMMTypeInterface {
  o Instance name: iNode7_5_2dst
  o Component name:
  o No ports
  o Not refined by a netlist
  o Input connections:
        - myINetlist7_5_2 --(myMMTypeInterface myMMPort7_5_2)--> iNode7_5_2dst
  o No constraints
}

### List of annotations ###

### List of variables ###


### List of constraints ###
} {
} {class myMMType7_5_2 {
  o Instance name: myINetlist7_5_2
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o Output connections:
        - myINetlist7_5_2 --(myMMTypeInterface myMMPort7_5_2)--> iNode7_5_2dst
  o No constraints
}}} \
{{Top-Level netlist is null

interface myMMTypeInterface {
  o Instance name: iNode7_5_2dst
  o Component name:
  o No ports
  o Not refined by a netlist
  o Input connections:
        - myINetlist7_5_2 --(myMMTypeInterface myMMPort7_5_2)--> iNode7_5_2dst
  o No constraints
}

class myMMType7_5_2 {
  o Instance name: myINetlist7_5_2
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o Output connections:
        - myINetlist7_5_2 --(myMMTypeInterface myMMPort7_5_2)--> iNode7_5_2dst
  o No constraints
}

### List of annotations ###

### List of variables ###


### List of constraints ###
} {
} {class myMMType7_5_2 {
  o Instance name: myINetlist7_5_2
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o Output connections:
        - myINetlist7_5_2 --(myMMTypeInterface myMMPort7_5_2)--> iNode7_5_2dst
  o No constraints
}}}]



} {1}

######################################################################
####
#
test Network-7.6 {connect INode, String, INode} {
    set net7_6 [java::new metropolis.metamodel.runtime.Network]

    set mmType7_6 [java::new metropolis.metamodel.runtime.MMType \
            myMMType7_6 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort7_6 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort7_6 $mmTypeInterface $mmType7_6 2]
    $mmType7_6 addPort $mmPort7_6

    set iNode7_6src [java::new metropolis.metamodel.runtime.INode \
            $mmType7_6 iNodeObject iNode7_6src 42]
    set iNode7_6dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject iNode7_6dst 42]

    # connect INode, String, INode
    $net7_6 connect $iNode7_6src myMMPort7_6 $iNode7_6dst
    list [$iNode7_6src show]
} {{class myMMType7_6 {
  o Instance name: iNode7_6src
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort7_6[][]
  o Not refined by a netlist
  o Output connections:
        - iNode7_6src --(myMMTypeInterface myMMPort7_6[][])--> iNode7_6dst
  o No constraints
}}}

######################################################################
####
#
test Network-7.6.1 {connect(INode, String, INode) error Message} {
    catch {$net7_6 connect $iNode7_6src "not a port" $iNode7_6dst} errMsg
    list $errMsg	
} {{java.lang.RuntimeException: connect: port 'not a port' is not found in 'iNode7_6src' source INode was:
class myMMType7_6 {
  o Instance name: iNode7_6src
  o Component name:
  o Ports:
       myMMTypeInterface myMMPort7_6[][]
  o No constraints
}}}

######################################################################
####
#
test Network-9.1 {generateLOCCheckers} {
    if [catch {file delete -force .constr0.loc} errMsg] {
	puts "WARNING: $errMsg"
    }
    set r1 [file exists .constr0.loc]
    set net9_1 [java::new metropolis.metamodel.runtime.Network]    
    set constraintLOC9_1 [java::new metropolis.metamodel.runtime.Constraint \
        [java::field metropolis.metamodel.runtime.Constraint LOC]]
    $constraintLOC9_1 addFormula \
        [java::field metropolis.metamodel.runtime.Constraint \
        TRACE_FORMULA] "a=b"

    $net9_1 addConstraint $constraintLOC9_1	
    $net9_1 addVariable "myVariable9_1"
    $net9_1 generateLOCCheckers
    set r2 [file exists .constr0.loc]
    set fd [open .constr0.loc]
    set r3 {}
    while {[gets $fd line] != -1} {
	lappend r3 $line
    }
    lappend r3 $line
    list $r1 $r2 $r3
} {0 1 {{loc: a=b} {annotation: event myVariable9_1} {trace: "%s %d"}}}

######################################################################
####
#
test Network-10.1 {getCompName} {
    # See also INode-2.1 in INode.tcl
    set net10_1 [java::new metropolis.metamodel.runtime.Network]    

    set iNode10_1a [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject myINode10_1a 42]
    set iNode10_1b [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject myINode10_1b 43]

 
    set iNetlist10_1 [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 myString10_1 myINetlist10_1 44]

    set myConnection10_1 [java::new metropolis.metamodel.runtime.Connection \
            $iNode10_1a $iNode10_1b $iPortElem $iNetlist3 [java::null]]

    set r1 [$net10_1 getCompName $iNode10_1a $iNetlist10_1]
    set r2 [$net10_1 getCompName myINode10_1a iNetlist10_1]

    $iNode10_1a addCompName $iNetlist10_1 iNode10_1aCompName

    set r3 [$net10_1 getCompName $iNode10_1a $iNetlist10_1]
    set r4 [$net10_1 getCompName myINode10_1a myINetlist10_1]

    # Need to add the INode and INetlist to the Network
    $net10_1 addNode [java::new Double 10.1] $iNode10_1a
    $net10_1 addNode [java::new Double 10.11] $iNetlist10_1

    set r5 [$net10_1 getCompName $iNode10_1a $iNetlist10_1]
    set r6 [$net10_1 getCompName myINode10_1a myINetlist10_1]

    # First arg is null, which returns the string "null" 
    set r7 [$net10_1 {getCompName metropolis.metamodel.runtime.INode \
	metropolis.metamodel.runtime.INetlist} \
	[java::null] $iNetlist10_1]

    list $r1 $r2 $r3 $r4 $r5 $r6 $r7
} {null null iNode10_1aCompName null iNode10_1aCompName iNode10_1aCompName null}

######################################################################
####
#
test Network-10.2 {getCompName(Object, Object)} {
    # Uses 10.1 above
    set instanceObject10_2 [java::new Double 10.1]	
    set netlistObject10_2 [java::new Double 10.11]	
    set r1 [$net10_1 {getCompName Object Object} \
	$instanceObject10_2 $netlistObject10_2]
} {iNode10_1aCompName}

######################################################################
####
#
test Network-12.1 {getConnectionDest} {
    set net12_1 [java::new metropolis.metamodel.runtime.Network]
    set iNode12_1src [java::new metropolis.metamodel.runtime.INode \
            $mmType iNodeObject iNode12_1src 42]
    set iNode12_1dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject iNode12_1dst 42]

    set iPortScalar12_1 [java::new metropolis.metamodel.runtime.IPortScalar \
			 $mmPort2 $iNode12_1src]
    $net12_1 connect $iNode12_1src $iPortScalar12_1 $iNode12_1dst

    set r1 [$net12_1 getConnectionDest $iNode12_1src $iPortScalar12_1]	
    # No connection found	
    set r2 [$net12_1 getConnectionDest $iNode12_1dst $iPortScalar12_1]	

    list $r1 [java::isnull $r2]
} {iNodeObject 1}

######################################################################
####
#
test Network-12.2 {getConnectionDest with an MMPort} {
    set net12_2 [java::new metropolis.metamodel.runtime.Network]

    set mmType12_2 [java::new metropolis.metamodel.runtime.MMType \
            myMMType12_2 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort12_2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort12_2 $mmTypeInterface $mmType12_2]
    $mmType12_2 addPort $mmPort12_2

    set iNode12_2src [java::new metropolis.metamodel.runtime.INode \
            $mmType12_2 iNode12_2srcObject iNode12_2src 42]
    set iNode12_2dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNode12_2dstObject iNode12_2dst 42]

    # connect INode, String, INode
    $net12_2 connect $iNode12_2src myMMPort12_2 $iNode12_2dst

    $net12_2 addNode [java::new Double 12.1] $iNode12_2src
    $net12_2 addNode [java::new Double 12.11] $iNode12_2dst

    set r1 [$net12_2 getConnectionDest $iNode12_2src myMMPort12_2]
    set r2 [$net12_2 getConnectionDest iNode12_2src myMMPort12_2]
    set r3 [$net12_2 getConnectionDest [java::new Double 12.1] myMMPort12_2]
    list $r1 $r2 $r3
} {iNode12_2dstObject iNode12_2dstObject iNode12_2dstObject}

######################################################################
####
#
test Network-12.2.1 {getConnectionDest(Object, Object) error message} {
    # Uses 12.2 above
    catch {$net12_2 getConnectionDest [java::new Double -1.0] myMMPort12_2} errMsg
    list $errMsg	
} {{java.lang.RuntimeException: Could not find node '-1.0' in the node instances. Perhaps it has not been added with addNode()?}}

######################################################################
####
#
test Network-12.3 {getConnectionDest with a refined connection} {
    set net12_3 [java::new metropolis.metamodel.runtime.Network]

    set mmType12_3 [java::new metropolis.metamodel.runtime.MMType \
            myMMType12_3 \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort12_3 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort12_3 $mmTypeInterface $mmType12_3]
    $mmType12_3 addPort $mmPort12_3

    set iNode12_3src [java::new metropolis.metamodel.runtime.INode \
            $mmType12_3 iNode12_3srcObject iNode12_3src 42]
    set iNode12_3dst [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNode12_3dstObject iNode12_3dst 42]

    # connect INode, String, INode
    $net12_3 connect $iNode12_3src myMMPort12_3 $iNode12_3dst

    $net12_3 addNode [java::new Double 12.1] $iNode12_3src
    $net12_3 addNode [java::new Double 12.11] $iNode12_3dst

    set iNetlist12_3a [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface myString12_3b myINetlist12_3a 44]
    set iNetlist12_3b [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface myString12_3b myINetlist12_3b 44]

    $net12_3 addNode "NodeObject12_3 String" $iNetlist12_3b
    $net12_3 addNode $iNetlist12_3a $iNetlist12_3b

    $net12_3 addComponent "NodeObject12_3 String" $iNetlist12_3a "UniqueCompName"

    $net12_3 refineConnect $iNetlist12_3b $iNode12_3src myMMPort12_3 $iNetlist12_3a

    set r1 [$net12_3 getConnectionDest $iNode12_3src myMMPort12_3]
    set r2 [$net12_3 getConnectionDest iNode12_3src myMMPort12_3]
    set r3 [$net12_3 getConnectionDest [java::new Double 12.1] myMMPort12_3]
    list $r1 $r2 $r3
} {myString12_3b myString12_3b myString12_3b}

######################################################################
####
#
test Network-12.3.2 {getConnectionDest with two refined connections} {
    # Uses 12_3 above

    set iNetlist12_3c [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface myString12_3b myINetlist12_3c 44]
    set iNetlist12_3d [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface myString12_3b myINetlist12_3d 44]

    $net12_3 addNode "NodeObject12_3_2 String" $iNetlist12_3d
    $net12_3 addNode $iNetlist12_3c $iNetlist12_3d

    $net12_3 addComponent "NodeObject12_3_2 String" $iNetlist12_3c "UniqueCompNameC"

    $net12_3 refineConnect $iNetlist12_3b $iNode12_3src myMMPort12_3 $iNetlist12_3c

    catch {[$net12_3 getConnectionDest $iNode12_3src myMMPort12_3]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Warning! metropolis.metamodel.runtime.Connection: iNode12_3src --(myMMTypeInterface myMMPort12_3)--> iNode12_3dst has more than one refinement connections.}}


######################################################################
####
#
test Network-13.1 {getConnectionNum} {
    set net13_1 [java::new metropolis.metamodel.runtime.Network]

    set mmTypeInterface13_1 [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface13_1 \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE] \
            $superClass \
            $superInterfaces]
    $net13_1 addType $mmTypeInterface13_1

    set superInterfaces13_1 [java::new java.util.LinkedList]
    $superInterfaces13_1 add $mmTypeInterface13_1

    # Must be of kind MEDIUM
    set mmType13_1 [java::new metropolis.metamodel.runtime.MMType \
            myMMType13_1 \
            [java::field metropolis.metamodel.runtime.MMType MEDIUM] \
            $superClass \
	    $superInterfaces13_1]
    $net13_1 addType $mmType13_1
            
    set mmPort13_1a [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort13_1a $mmTypeInterface13_1 $mmType13_1]
    $mmType13_1 addPort $mmPort13_1a

    set mmPort13_1b [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort13_1b $mmTypeInterface13_1 $mmType13_1]
    $mmType13_1 addPort $mmPort13_1b

    set iNode13_1src [java::new metropolis.metamodel.runtime.INode \
            $mmType13_1 iNode13_1srcObject iNode13_1src 42]
    set iNode13_1dst [java::new metropolis.metamodel.runtime.INode \
            $mmType13_1 iNode13_1dstObject iNode13_1dst 42]

    set iNode13_1src2 [java::new metropolis.metamodel.runtime.INode \
            $mmType13_1 iNode13_1src2Object iNode13_1src2 42]
    set iNode13_1dst2 [java::new metropolis.metamodel.runtime.INode \
            $mmType13_1 iNode13_1dst2Object iNode13_1dst2 42]

    #  Create a dag where dst2 has two in connections 
    #  src->dst<--src2 
    #  |--->dst2

    $net13_1 connect $iNode13_1src myMMPort13_1a $iNode13_1dst
    $net13_1 connect $iNode13_1src myMMPort13_1b $iNode13_1dst2
    $net13_1 connect $iNode13_1src2 myMMPort13_1b $iNode13_1dst

    $net13_1 addNode [java::new Double 13.1] $iNode13_1src
    $net13_1 addNode [java::new Double 13.11] $iNode13_1dst
    $net13_1 addNode [java::new Double 13.12] $iNode13_1src2
    $net13_1 addNode [java::new Double 13.121] $iNode13_1dst2

    set r1 [$net13_1 getConnectionNum $iNode13_1src $mmTypeInterface13_1]
    set r2 [$net13_1 getConnectionNum $iNode13_1dst $mmTypeInterface13_1]
    set r3 [$net13_1 getConnectionNum $iNode13_1src2 $mmTypeInterface13_1]
    set r4 [$net13_1 getConnectionNum $iNode13_1dst2 $mmTypeInterface13_1]

    list $r1 $r2 $r3 $r4
} {0 2 0 1}

######################################################################
####
#
test Network-13.2 {getConnectionNum(Object, String)} {

    set r1 [$net13_1 {getConnectionNum Object String} [java::new Double 13.1] myMMTypeInterface13_1]
    set r2 [$net13_1 {getConnectionNum Object String} [java::new Double 13.11] myMMTypeInterface13_1]
    set r3 [$net13_1 {getConnectionNum Object String} [java::new Double 13.12] myMMTypeInterface13_1]
    set r4 [$net13_1 {getConnectionNum Object String} [java::new Double 13.121] myMMTypeInterface13_1]
    list $r1 $r2 $r3 $r4
} {0 2 0 1}

######################################################################
####
#
test Network-13.3 {getConnectionNum(INode, String)} {

    set r1 [$net13_1 getConnectionNum $iNode13_1src myMMTypeInterface13_1]
    set r2 [$net13_1 getConnectionNum $iNode13_1dst myMMTypeInterface13_1]
    set r3 [$net13_1 getConnectionNum $iNode13_1src2 myMMTypeInterface13_1]
    set r4 [$net13_1 getConnectionNum $iNode13_1dst2 myMMTypeInterface13_1]
    list $r1 $r2 $r3 $r4
} {0 2 0 1}

######################################################################
####
#
test Network-13.4 {getConnectionNum(INode, MMType) error messages} {
    # Non MEDIUM or STATEMEDIUM INode
    # mmTypeInterface is defined in runtime.tcl
    set iNode13_4 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject13_1 iNode13_4 42]

    catch {$net13_1 getConnectionNum $iNode13_4 $mmTypeInterface13_1} errMsg1
    
    # Second argument is not an interface
    # mmType is defined in runtime.tcl
    catch {$net13_1 getConnectionNum $iNode13_1src $mmType} errMsg2 

    # object does not implement interface
    catch {$net13_1 getConnectionNum $iNode13_1src $mmTypeInterface} errMsg3

    list $errMsg1 "\n" $errMsg2 "\n" $errMsg3

} {{java.lang.RuntimeException: getconnectionnum: node 'iNode13_4' is not a medium or statemedium, it is a interface and of type 'myMMTypeInterface'} {
} {java.lang.RuntimeException: getconnectionnum: type with name 'myMMType' is not an interface, it is a class.} {
} {java.lang.RuntimeException: getconnectionnum: medium 'iNode13_1src' does not implement interface 'myMMTypeInterface', it implements: myMMTypeInterface13_1 interfaces}}


######################################################################
####
#
test Network-13.5.1 {getNthConnectionSrc(Inode, MMType, int)} {
    # Uses 13.1 above
    # get the 1st connection
    set r1 [$net13_1 \
		{getNthConnectionSrc metropolis.metamodel.runtime.INode \
		     metropolis.metamodel.runtime.MMType int} \
		$iNode13_1dst $mmTypeInterface13_1 1]

    # get the 2nd connection
    set r2 [$net13_1 \
		{getNthConnectionSrc metropolis.metamodel.runtime.INode \
		     metropolis.metamodel.runtime.MMType int} \
		$iNode13_1dst $mmTypeInterface13_1 2]
    list $r1 $r2

} {iNode13_1srcObject iNode13_1src2Object}

######################################################################
####
#
test Network-13.5.1.1 {getNthConnectionSrc(Inode, MMType, int): bogus n } {
    # Uses 13.1 above
    catch {
	$net13_1 \
	    {getNthConnectionSrc metropolis.metamodel.runtime.INode \
		 metropolis.metamodel.runtime.MMType int} \
	    $iNode13_1dst $mmTypeInterface13_1 666
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: medium 'iNode13_1dst'  does not have 666 connections implementing interface 'myMMTypeInterface13_1', it only has 2}}


######################################################################
####
#
test Network-13.5.2 {getNthConnectionSrc(INode, String, int)} {
    # Uses 13.1 above
    $net13_1 \
	{getNthConnectionSrc metropolis.metamodel.runtime.INode String int} \
	$iNode13_1dst myMMTypeInterface13_1 1
} {iNode13_1srcObject}

######################################################################
####
#
test Network-13.5.3 {getNthConnectionSrc(Object, String, int)} {
    # Uses 13.1 above
    $net13_1 \
	{getNthConnectionSrc Object String int} \
	[java::new Double 13.11] myMMTypeInterface13_1 1
} {iNode13_1srcObject}

######################################################################
####
#
test Network-14.1 {getInstName} {
    # Uses 13.1 above

    # getInstName(INode)
    set r1 [$net13_1 getInstName $iNode13_1src]

    # getInstName(Object)
    set r2 [$net13_1 getInstName [java::new Double 13.1]]

    # getInstName(INode) where INode is null
    set r3 [$net13_1 {getInstName metropolis.metamodel.runtime.INode} \
		[java::null]]

    list $r1 $r2 $r3
} {iNode13_1src iNode13_1src null}

######################################################################
####
#
test Network-20.1 {getNthConnectionPort} {
    set net20_1 [java::new metropolis.metamodel.runtime.Network]

    set mmTypeInterface20_1 [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface20_1 \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE] \
            $superClass \
            $superInterfaces]
    $net20_1 addType $mmTypeInterface20_1

    set superInterfaces20_1 [java::new java.util.LinkedList]
    $superInterfaces20_1 add $mmTypeInterface20_1

    # Must be of kind MEDIUM
    set mmType20_1 [java::new metropolis.metamodel.runtime.MMType \
            myMMType20_1 \
            [java::field metropolis.metamodel.runtime.MMType MEDIUM] \
            $superClass \
	    $superInterfaces20_1]
    $net20_1 addType $mmType20_1

    # Create a port with the same name as the field in NetworkTest
    set mmPort20_1 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort20_1 $mmTypeInterface20_1 $mmType20_1 0]

    $mmType20_1 addPort $mmPort20_1

    # NetworkTestChild has a field called myMMPort20_1
    set networkTestChild20_1a \
	[java::new metropolis.metamodel.runtime.test.NetworkTestChild \
	     $mmTypeInterface20_1 networkTestChild20_1aObject networkTestChild20_1a 50]

    set networkTestChild20_1b \
	[java::new metropolis.metamodel.runtime.test.NetworkTestChild \
	     $mmTypeInterface20_1 networkTestChild20_1bObject \
		networkTestChild20_1b 50]

    #$net20_1 addType $mmTypeInterface

    set iNode20_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmType20_1 $networkTestChild20_1a iNode20_1 45]
    set iNode20_1a [java::new metropolis.metamodel.runtime.INode \
                      $mmType20_1 $networkTestChild20_1b iNode20_1a 451]

    $net20_1 addNode $networkTestChild20_1a $iNode20_1
    $net20_1 addNode $networkTestChild20_1b $iNode20_1a


    set iPortScalar20_1 [java::new metropolis.metamodel.runtime.IPortScalar \
			     $mmPort20_1 $iNode20_1a]

    set myConnection20_1 [java::new metropolis.metamodel.runtime.Connection \
            $iNode20_1 $iNode20_1a $iPortScalar20_1 $iNetlist3 [java::null]]

    $iNode20_1 addInConnection $myConnection20_1
    set connection20_1 [$iNode20_1 getNthUser $mmTypeInterface20_1 0]

    # Replicate some of the java code here to make it more clear as
    # to what is going on
    set medium [$net20_1 getNodeSafely $networkTestChild20_1a]
    set intf [$net20_1 getType myMMTypeInterface20_1]
    set connection [$medium getNthUser $intf 1]

    # The NetworkTest object
    set object [[[[$connection20_1 getSource] getUserObject] getClass] getName]

    # Should be the same name as the field of NetworkTest
    set r2 [[$connection20_1 getPort] getName]

    # get the 1st connection port
    set iNode \
	[java::cast metropolis.metamodel.runtime.INode \
	     [$net20_1 getNthConnectionPort $networkTestChild20_1a \
		  myMMTypeInterface20_1 1]]
    set r3 [$iNode getName]

    list [$medium getName] [$intf getName] "\n" \
	[$connection show] "\n" \
	$object $r2 "\n" \
	$r3
} {iNode20_1 myMMTypeInterface20_1 {
} {iNode20_1 --(myMMTypeInterface20_1 myMMPort20_1)--> iNode20_1a} {
} metropolis.metamodel.runtime.test.NetworkTestChild myMMPort20_1 {
} networkTestChild20_1amyMMPort20_1}

######################################################################
####
#
test Network-20.2 {getNthConnectionPort add a second connection} {
    # Create a second port with the same name a different field in NetworkTest
    set mmPort20_2 [java::new metropolis.metamodel.runtime.MMPort \
                    __pointer_myMMPort7_5_2 $mmTypeInterface20_1 $mmType20_1 0]

    set iPortScalar20_2 [java::new metropolis.metamodel.runtime.IPortScalar \
			     $mmPort20_2 $iNode20_1a]

    set myConnection20_2 [java::new metropolis.metamodel.runtime.Connection \
            $iNode20_1 $iNode20_1a $iPortScalar20_2 $iNetlist3 [java::null]]

    $iNode20_1 addInConnection $myConnection20_2

    # get the 1st connection port
    set iNode1 \
	[java::cast metropolis.metamodel.runtime.INode \
	     [$net20_1 getNthConnectionPort $networkTestChild20_1a \
		  myMMTypeInterface20_1 1]]
    set r1 [$iNode1 getName]

    set iNode2 \
	[java::cast metropolis.metamodel.runtime.INode \
	     [$net20_1 getNthConnectionPort $networkTestChild20_1a \
		  myMMTypeInterface20_1 2]]
    set r2 [$iNode2 getName]
    list $r1 $r2
} {networkTestChild20_1amyMMPort20_1 networkTestChild20_1a__pointer_myMMPort7_5_2}

######################################################################
####
#
test Network-20.2.1 {getNthConnectionPort with non-existant connection} {
    # There is no third connection
    catch {$net20_1 getNthConnectionPort $networkTestChild20_1a \
	       myMMTypeInterface20_1 3} errMsg
    list $errMsg
} {{java.lang.RuntimeException: myMMType20_1 does not have 3 input connections implementing interface myMMTypeInterface20_1}}

######################################################################
####
#
test Network-20.3 {getNthConnectionPort private field} {
    # Create a port with the same name as the field in NetworkTest
    set _privateMMPort20_1 [java::new metropolis.metamodel.runtime.MMPort \
                    _privateMMPort20_1 $mmTypeInterface20_1 $mmType20_1 0]

    set iPortScalar20_3 [java::new metropolis.metamodel.runtime.IPortScalar \
			     $_privateMMPort20_1 $iNode20_1a]

    set myConnection20_3 [java::new metropolis.metamodel.runtime.Connection \
            $iNode20_1 $iNode20_1a $iPortScalar20_3 $iNetlist3 [java::null]]

    $iNode20_1 addInConnection $myConnection20_3

    # get the 3rd connection port, even though it is private!
    set iNode \
	[java::cast metropolis.metamodel.runtime.INode \
	     [$net20_1 getNthConnectionPort $networkTestChild20_1a \
		  myMMTypeInterface20_1 3]]
    set r1 [$iNode getName]
    list $r1
} {networkTestChild20_1a_privateMMPort20_1}

######################################################################
####
#
test Network-20.4 {getNthConnectionPort NetworkTest does not have this field} {
    # Create a port with the same name as the field in NetworkTest
    set networkTestDoesNotHaveThisField \
	 [java::new metropolis.metamodel.runtime.MMPort \
                    networkTestDoesNotHaveThisField \
			$mmTypeInterface20_1 $mmType20_1 0]

    set iPortScalar20_4 [java::new metropolis.metamodel.runtime.IPortScalar \
			     $networkTestDoesNotHaveThisField $iNode20_1a]

    set myConnection20_4 [java::new metropolis.metamodel.runtime.Connection \
            $iNode20_1 $iNode20_1a $iPortScalar20_4 $iNetlist3 [java::null]]

    $iNode20_1 addInConnection $myConnection20_4

    catch {$net20_1 getNthConnectionPort $networkTestChild20_1a \
		  myMMTypeInterface20_1 4} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Failed to find a field named 'networkTestDoesNotHaveThisField' in class metropolis.metamodel.runtime.test.NetworkTestChild or its superclasses.}}

######################################################################
####
#
test Network-20.9 {getNthPort(INode, String, int): get 2nd port} { 
    # Create a port with the same name as the field in NetworkTest
    set _privateMMPort20_1 [java::new metropolis.metamodel.runtime.MMPort \
                    _privateMMPort20_1 $mmTypeInterface20_1 $mmType20_1 0]

    $mmType20_1 addPort $_privateMMPort20_1

    set iNode20_9 [java::new metropolis.metamodel.runtime.INode \
                      $mmType20_1 $networkTestChild20_1a iNode20_9 45]
    set iNode20_9a [java::new metropolis.metamodel.runtime.INode \
                      $mmType20_1 $networkTestChild20_1a iNode20_9 45]

    $net20_1 addNode $networkTestChild20_1a $iNode20_9
    $net20_1 addNode $networkTestChild20_1a $iNode20_9a

    set iPortScalar20_9 [java::new metropolis.metamodel.runtime.IPortScalar \
			     $mmPort20_1 $iNode20_9]

    set myConnection20_9 [java::new metropolis.metamodel.runtime.Connection \
            $iNode20_9 $iNode20_9a $iPortScalar20_9 $iNetlist3 [java::null]]

    $iNode20_9 addInConnection $myConnection20_9

    set iNode20_9results \
	[java::cast metropolis.metamodel.runtime.INode \
	    [$net20_1 {getNthPort metropolis.metamodel.runtime.INode String int} $iNode20_9 myMMTypeInterface20_1 1]]

    list [$iNode20_9results getName]
} {networkTestChild20_1a_privateMMPort20_1}


######################################################################
####
#
test Network-21.1 {getNthPort(INode, String, int)} { 
    set nodeType [$iNode20_1 getType] 
    set objectClassName [[[$iNode20_1 getUserObject] getClass] getName]

    # FIXME: Note that the index is 0 based, so we are getting the 0th
    # or first connection.
    set iNode21_1 \
	[java::cast metropolis.metamodel.runtime.INode \
	     [$net20_1 \
		  {getNthPort metropolis.metamodel.runtime.INode String int} \
		  $iNode20_1a myMMTypeInterface20_1 0]]

    list "nodeType: [$nodeType {show boolean} true]\n" \
	$objectClassName "\n" \
	[$iNode21_1 getName]
} {{nodeType: medium myMMType20_1
} metropolis.metamodel.runtime.test.NetworkTestChild {
} networkTestChild20_1bmyMMPort20_1}

######################################################################
####
#
test Network-21.1.1 {getNthPort(INode, String, int): no such field} { 
    # Create a port with the name that is not a field in NetworkTest
    set notANetworkTestPort21_1_1 [java::new metropolis.metamodel.runtime.MMPort \
                    notANetworkTestPort21_1_1 \
	 $mmTypeInterface20_1 $mmType20_1 0]

    # Note that adding a bogus port means that if we look at the third
    # port or later, we will get an error.
    $mmType20_1 addPort $notANetworkTestPort21_1_1

    set iNode21_1_1 [java::new metropolis.metamodel.runtime.INode \
                      $mmType20_1 $networkTestChild20_1a iNode21_1_1 45]
    set iNode21_1_1a [java::new metropolis.metamodel.runtime.INode \
                      $mmType20_1 $networkTestChild20_1a iNode21_1_1 45]

    $net20_1 addNode $networkTestChild20_1a $iNode21_1_1
    $net20_1 addNode $networkTestChild20_1a $iNode21_1_1a

    set iPortScalar21_1_1 [java::new metropolis.metamodel.runtime.IPortScalar \
			     $mmPort20_1 $iNode21_1_1]

    set myConnection21_1_1 [java::new metropolis.metamodel.runtime.Connection \
            $iNode21_1_1 $iNode21_1_1a $iPortScalar21_1_1 $iNetlist3 [java::null]]

    $iNode21_1_1 addInConnection $myConnection21_1_1

    catch {$net20_1 {getNthPort metropolis.metamodel.runtime.INode String int} $iNode20_1 myMMTypeInterface20_1 2} errMsg
    list $errMsg
} {{java.lang.RuntimeException: getnthport: Object of class 'metropolis.metamodel.runtime.test.NetworkTestChild' does not have a field 'notANetworkTestPort21_1_1' of type 'myMMTypeInterface20_1' declared, which should match a port in myMMType20_1.}}

######################################################################
####
#
test Network-21.2 {getNthPort(String, String, int)} { 
    set iNode21_2 \
	[java::cast metropolis.metamodel.runtime.INode \
	     [$net20_1 {getNthPort String String int} \
		  iNode20_1a myMMTypeInterface20_1 0]]

    list [$iNode21_2 getName]
} {networkTestChild20_1bmyMMPort20_1}

######################################################################
####
#
test Network-22.1 {getPortNum(INode, String)} {
    set r1 [$net20_1 {getPortNum String String} \
    	[java::null] myMMTypeInterface20_1]
    set r2 [$net20_1 {getPortNum metropolis.metamodel.runtime.INode String} \
	[java::null] myMMTypeInterface20_1]
    set r3 [$net20_1 getPortNum $iNode20_1 notAnInterface]
    set r4 [$net20_1 getPortNum $iNode20_1 myMMTypeInterface20_1]
    list $r1 $r2 $r3 $r4
} {0 0 0 1}

