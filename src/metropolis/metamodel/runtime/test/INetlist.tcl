# Tests for INetlist class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: INetlist.tcl,v 1.8 2005/11/22 20:11:57 allenh Exp $
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
test INetlist-1.1 {constructor} {
    # Very similar to IPortArray-1.1
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $superClass \
            $superInterfaces]

    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType]

    $mmTypeInterface addPort $mmPort

    set iNode [java::new metropolis.metamodel.runtime.INode \
                   $mmTypeInterface iNodeObject myINode 42]
    
    set myString [java::new String myString]
    set iNetlist [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface $myString myINetlist 43]

    $iNetlist show
} {interface myMMTypeInterface {
  o Instance name: myINetlist
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}}


test INetlist-2.1 {addComponent, isComponent, removeComponent } {
    # Uses INetlist-1.1 above

    set myString2 [java::new String myString2]
    set iNetlist2 [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 $myString2 myINetlist2 44]

    set r1 [$iNetlist2 isComponent $iNetlist]
    set r2 [$iNetlist isComponent $iNetlist2]
    
    # Add the component
    $iNetlist2 addComponent $iNetlist
    set r3 [$iNetlist2 isComponent $iNetlist]
    set r4 [$iNetlist isComponent $iNetlist2]

    # Exercise _showComponents
    set r5 [$iNetlist2 show]
    set r6 [$iNetlist show]

    # Iterate over the inode connections
    set results {}
    set inodes [$iNetlist2 getComponents]
    while {[$inodes hasNext]} {
        set inode [java::cast metropolis.metamodel.runtime.INetlist \
                [$inodes next]]
        #puts $inode
        lappend results [$iNetlist equals $inode]
    }

    # Now remove it
    $iNetlist2 removeComponent $iNetlist
    set r7 [$iNetlist2 isComponent $iNetlist]
    set r8 [$iNetlist isComponent $iNetlist2]

    list $r1 $r2 $r3 $r4 $r5 $r6 $results $r7 $r8 
} {0 0 1 0 {interface myMMTypeInterface2 {
  o Instance name: myINetlist2
  o Component name:
  o Components:
       - null  (interface instance name: myINetlist)
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} {interface myMMTypeInterface {
  o Instance name: myINetlist
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} 1 0 0}

test INetlist-2.2 {removeComponent: error condition } {
    # Uses 2.1 above
    catch {$iNetlist2 removeComponent $iNetlist} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Netlist 'myINetlist2' does not have the component 'myINetlist'}}

test INetlist-3.1 {addRefinedNode, isRefinement, getRefinedNodes } {
    # Uses INetlist-1.1 above
    set r1 [$iNetlist isRefinement]
    set r2 [$iNetlist isRefined]
    set r3 [$iNetlist isRefinement $iNode]
    $iNetlist addRefinedNode $iNode
    set r4 [$iNetlist isRefinement]
    set r5 [$iNetlist isRefined]
    set r6 [$iNetlist isRefinement $iNode]

    set results {}
    set inodes [$iNetlist getRefinedNodes]
    while {[$inodes hasNext]} {
        set inode [java::cast metropolis.metamodel.runtime.INode \
                [$inodes next]]
        lappend results [$iNode equals $inode]
    }

    list $r1 $r2 $r3 $r4 $r5 $r6 $results [$iNetlist show]
} {0 0 0 1 0 1 1 {interface myMMTypeInterface {
  o Instance name: myINetlist
  o Component name:
  o No components
  o Not refined by a netlist
  o Refines
  myINode
  o No constraints
}}}

test INetlist-4.1 {addScopeUser } {

    # Add the connection.
    $iNetlist addScopeUser $myConnection

    # Iterate over the connections.
    set results {}
    set connections [$iNetlist getScopeUsers]
    while {[$connections hasNext]} {
        set connection [java::cast metropolis.metamodel.runtime.Connection \
                [$connections next]]
        lappend results [$myConnection equals $connection]
    }
    list $results
} {1}
