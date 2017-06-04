# Tests for Connection runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: Connection.tcl,v 1.13 2005/11/22 20:11:55 allenh Exp $
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
test Connection-1.1 {Constructor} {
    # myConnection is created in runtime.tcl
    $myConnection show
} {myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}

######################################################################
####
#
test Connection-1.2 {Constructor refinedConnection} {

    # many of the variables below are created in runtime.tcl

    set iNodeSource2 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject myINodeSource2 43]
    set iNodeDestination2 [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject myINodeDestination2 44]

    set mmPort3 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 3]

    set inode2 [java::new metropolis.metamodel.runtime.INode \
                   $mmType MyString MyINode2 666]

    set iPortArray2 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort2 $inode2]

    set index2 [java::new {int[]} 1 ]
    $index2 set 0 1

    set iPortElem2 [java::new metropolis.metamodel.runtime.IPortElem \
                       $iPortArray2 $index2 0]

    # Don't operate on myConnection, instead, make another connection
    set myConnection2 [java::new metropolis.metamodel.runtime.Connection \
            $iNodeSource $iNodeDestination $iPortElem $iNetlist3 [java::null]]


    # $myConnection2 refines myConnection2
    set myConnection3 \
         [java::new metropolis.metamodel.runtime.Connection \
            $iNodeSource2 $iNodeDestination2 $iPortElem $iNetlist3 \
        $myConnection2]


    $myConnection3 show

} {myINodeSource2 --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination2}

######################################################################
####
#
test Connection-2.1 {finalConnection} {
    set finalConnections [$myConnection finalConnection]
    set finalConnections2 [$myConnection2 finalConnection]
    set finalConnections3 [$myConnection3 finalConnection]

    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $finalConnections2]

    list \
        [$finalConnections size] \
        [$finalConnections2 size] \
        [$finalConnections3 size] \
        $r1
} {0 1 0 {{metropolis.metamodel.runtime.Connection: myINodeSource2 --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination2}}}

######################################################################
####
#
test Connection-3.1 {getNetList} {
    list \
        [[$myConnection getNetlist] show] \
        [[$myConnection2 getNetlist] show] \
        [[$myConnection3 getNetlist] show]
} {{interface myMMTypeInterface2 {
  o Instance name: myINetlist3
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} {interface myMMTypeInterface2 {
  o Instance name: myINetlist3
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}} {interface myMMTypeInterface2 {
  o Instance name: myINetlist3
  o Component name:
  o No components
  o Not refined by a netlist
  o Does not refine any node
  o No constraints
}}}

######################################################################
####
#
test Connection-4.1 {getPort} {
    [$myConnection getPort] show
} {myMMTypeInterface myMMPort[1][2]}

######################################################################
####
#
test Connection-5.1 {getRefinedConnection} {
    set refinedConnection [$myConnection getRefinedConnection]
    set refinedConnection2 [$myConnection2 getRefinedConnection]
    set refinedConnection3 [$myConnection3 getRefinedConnection]

    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $refinedConnection3]

    list \
            [$refinedConnection size] \
            [$refinedConnection2 size] \
            [$refinedConnection3 size] \
        $r1
} {0 0 1 {{metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}}}

######################################################################
####
#
test Connection-6.1 {getRefinementConnection} {
    set refinementConnection [$myConnection getRefinementConnection]
    set refinementConnection2 [$myConnection2 getRefinementConnection]
    set refinementConnection3 [$myConnection3 getRefinementConnection]

    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $refinementConnection2]

    list \
            [$refinementConnection size] \
            [$refinementConnection2 size] \
            [$refinementConnection3 size] \
        $r1
} {0 1 0 {{metropolis.metamodel.runtime.Connection: myINodeSource2 --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination2}}}

######################################################################
####
#
test Connection-7.1 {getSource} {
    [$myConnection getSource] show
} {interface myMMTypeInterface {
  o Instance name: myINodeSource
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}}

######################################################################
####
#
test Connection-8.1 {getTarget} {
    [$myConnection getTarget] show
} {interface myMMTypeInterface {
  o Instance name: myINodeDestination
  o Component name:
  o No ports
  o Not refined by a netlist
  o No constraints
}}

######################################################################
####
#
test Connection-9.1 {isRefined} {
    list \
        [$myConnection isRefined] \
        [$myConnection2 isRefined] \
        [$myConnection3 isRefined]
} {0 1 0}

######################################################################
####
#
test Connection-10.1 {isRefinement} {
    list \
        [$myConnection isRefinement] \
        [$myConnection2 isRefinement] \
        [$myConnection3 isRefinement] \
} {0 0 1}

######################################################################
####
#
test Connection-11.1 {originalConnection} {
    set originalConnection [$myConnection originalConnection]
    set originalConnection2 [$myConnection2 originalConnection]
    set originalConnection3 [$myConnection3 originalConnection]

    # listToStrings is defined in enums.tcl
    set r1 [listToStrings $originalConnection3]

    list \
        [$originalConnection size] \
        [$originalConnection2 size] \
        [$originalConnection3 size] \
        $r1
} {0 0 1 {{metropolis.metamodel.runtime.Connection: myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination}}}


######################################################################
####
#
test Connection-13.1 {4 arg show} {
    # myConnection is created in runtime.tcl
    list \
        [$myConnection show true true true true] "\n" \
        [$myConnection2 show true true true true] "\n" \
        [$myConnection3 show true true true true] \
} {{myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination
o This connection is not refining another connection
o This connection is not further refined} {
} {myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination
o This connection is not refining another connection
o This connection is further refined by the connection
    myINodeSource2 --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination2} {
} {myINodeSource2 --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination2
o This connection is refining the connection
    myINodeSource --(myMMTypeInterface myMMPort[1][2])--> myINodeDestination
o This connection is not further refined}}

######################################################################
####
#
test Connection-13.2 {4 arg show, all false args} {
    # myConnection is created in runtime.tcl
    $myConnection show false false false false
} { --> }
