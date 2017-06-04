# Tests for MMType class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: MMType.tcl,v 1.9 2005/11/22 20:12:01 allenh Exp $
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

if {[info procs iterToObjects] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test MMType-1.1 {constructor} {
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $superClass \
            $superInterfaces]
    list \
            [$mmType getName] \
            [$mmType getKind] \
            [java::call metropolis.metamodel.runtime.MMType {show int} \
             [$mmType getKind]] \
            [java::isnull [$mmType getSuperClass]]
} {myMMType 1 class 1}


####
#
test MMType-2.1 {addInterface(MMType newIf)} {
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $superClass \
            $superInterfaces]

    set mmType1 [java::new metropolis.metamodel.runtime.MMType \
                myMMType1_CLASS \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $superClass \
            $superInterfaces]

    # Has mmType as a super class
    set mmType2 [java::new metropolis.metamodel.runtime.MMType \
                myMMType2_CLASS \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $mmType \
            $superInterfaces]

    # Has mmType2 as a superinterface
    set superInterfaces2 [java::new java.util.LinkedList]
    $superInterfaces2 add $mmType
    set mmType2a [java::new metropolis.metamodel.runtime.MMType \
                myMMType2_CLASS \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $mmType \
            $superInterfaces2]

    $mmType addInterface $mmType1
    list \
        [list \
             [$mmType implementsInterface $mmType] \
             [$mmType implementsInterface $mmType1] \
             [$mmType implementsInterface $mmType2] \
             [$mmType implementsInterface $mmType2a]] \
        [list \
             [$mmType1 implementsInterface $mmType] \
             [$mmType1 implementsInterface $mmType1] \
             [$mmType1 implementsInterface $mmType2] \
             [$mmType1 implementsInterface $mmType2a]] \
        [list \
             [$mmType2 implementsInterface $mmType] \
             [$mmType2 implementsInterface $mmType1] \
             [$mmType2 implementsInterface $mmType2] \
             [$mmType2 implementsInterface $mmType2a]] \
        [list \
             [$mmType2a implementsInterface $mmType] \
             [$mmType2a implementsInterface $mmType1] \
             [$mmType2a implementsInterface $mmType2] \
             [$mmType1 implementsInterface $mmType2a]]
} {{1 1 0 0} {0 1 0 0} {1 1 1 0} {1 1 0 0}}

####
#
test MMType-2.1.1 {addInterfaces() - null interfaces } {
    # Uses 1.1 above
    set mmType4 [java::new metropolis.metamodel.runtime.MMType \
                    myMMType4 \
                    [java::field metropolis.metamodel.runtime.MMType CLASS ] \
                    $superClass \
                    [java::null]]
    set mmType5 [java::new metropolis.metamodel.runtime.MMType \
                myMMType5_CLASS \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $mmType \
            $superInterfaces]


    $mmType4 addInterface $mmType5

    list \
        [$mmType4 implementsInterface $mmType5] \
        [$mmType5 implementsInterface $mmType4]
} {1 0}


####
#
test MMType-2.2 {interfacesIterator()} {
    # Uses 2.1 above
    set interfaces [$mmType interfacesIterator]
    list [objectsToStrings [iterToObjects $interfaces]]
} {{{MMType: class myMMType1_CLASS}}}


####
#
test MMType-2.3 {interfacesIterator() - null interfaces } {
    # Uses 2.1 above
    set mmType [java::new metropolis.metamodel.runtime.MMType \
                    myMMType \
                    [java::field metropolis.metamodel.runtime.MMType CLASS ] \
                    $superClass \
                    [java::null]]

    set interfaces [$mmType interfacesIterator]
    list [java::isnull $interfaces]
} {1}


####
#
test MMType-3.1 {addPort(MMPort port)} {
    
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $superClass \
            $superInterfaces]


    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $superClass \
            $superInterfaces]

    # Add two ports so that we get the false branch in getPort()
    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType]
    set mmPort1 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort1 $mmTypeInterface $mmType]

    set newPort0 [$mmTypeInterface getPort myMMPort]
    # Add a port
    $mmTypeInterface addPort $mmPort
    # Add two ports so that we get the false branch in getPort()
    $mmTypeInterface addPort $mmPort1
    set newPort1 [$mmTypeInterface getPort myMMPort1]
    list \
        [java::isnull $newPort0] \
        [$newPort1 show]
} {1 {myMMTypeInterface myMMPort1}}



####
#
test MMType-3.2 {portsIterator()} {
    # Uses 3.1 above
    set ports [$mmTypeInterface portsIterator]
    list [objectsToStrings [iterToObjects $ports]]
} {{{MMPort: myMMTypeInterface myMMPort} {MMPort: myMMTypeInterface myMMPort1}}}


####
#
test MMType-11.1 {isSubClass(MMType superClass), setSuperClass} {
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmType1 [java::new metropolis.metamodel.runtime.MMType \
                myMMType_CLASS \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $superClass \
            $superInterfaces]

    set r1 [$mmType1 isSubClass $mmType1]

    # Set it to be its own parent
    $mmType1 setSuperClass $mmType1
    set r2 [$mmType1 isSubClass $mmType1]

    set mmType2 [java::new metropolis.metamodel.runtime.MMType \
                myMMType_SUPERCLASS \
            [java::field metropolis.metamodel.runtime.MMType CLASS ] \
            $superClass \
            $superInterfaces]

    # Set the superClass
    $mmType1 setSuperClass $mmType2
    set r3 [$mmType1 getSuperClass]

    set r4 [$mmType1 isSubClass $mmType2]

    # Not a parent class
    set r5 [$mmType1 isSubClass [java::null]]

    list $r1 $r2 [$r3 equals $mmType2] $r4 $r5
} {1 1 1 1 0}



####
#
test MMType-13.1 {String show(int kind)} {
    #show(-1) and show(99) should return null
    list \
            [java::isnull \
            [java::call -noconvert metropolis.metamodel.runtime.MMType \
            {show int} -1]] \
            [java::isnull \
            [java::call -noconvert metropolis.metamodel.runtime.MMType \
            {show int} 99]] \
} {1 1}

####
#
test MMType-14.1 {show(boolean showKind)} {

    set superClass [java::null]
    set superInterfaces [java::null]

    set results {}
    set kinds [list INTERFACE CLASS NETLIST PROCESS MEDIUM SCHEDULER STATEMEDIUM QUANTITY]
    foreach kind $kinds {
        set mmType [java::new metropolis.metamodel.runtime.MMType \
                myMMType_$kind \
            [java::field metropolis.metamodel.runtime.MMType $kind ] \
            $superClass \
            $superInterfaces]
        lappend results [$mmType {show boolean} true]
    }
    list $results
} {{{interface myMMType_INTERFACE} {class myMMType_CLASS} {netlist myMMType_NETLIST} {process myMMType_PROCESS} {medium myMMType_MEDIUM} {scheduler myMMType_SCHEDULER} {statemedium myMMType_STATEMEDIUM} {quantity myMMType_QUANTITY}}}


####
#
test MMType-14.1 {show(boolean, boolean, boolean, boolean )} {
    # Uses 11.1 above
    $mmType1 show true true true true
} {MMType class myMMType_CLASS
  o Super Class: class myMMType_SUPERCLASS
  o Super Interfaces: 
  o Ports: 
}
