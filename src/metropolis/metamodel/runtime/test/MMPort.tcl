# Tests for MMPort class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: MMPort.tcl,v 1.9 2005/11/22 20:11:47 allenh Exp $
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
test MMPort-1.1 {simple constructor} {
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

    list \
        [$mmPort getDims] \
        [$mmPort getLength 0] \
        [$mmPort hasFixedSize] \
        [$mmPort isArray] \
        [$mmPort isScalar] \
        [$mmTypeInterface equals [$mmPort getInterface]] \
        [$mmPort getName] \
        [$mmType equals [$mmPort getType]] \
        [$mmPort hasFixedSize] \
        [$mmPort toString]
} {0 0 1 0 1 1 myMMPort 1 1 {MMPort: myMMTypeInterface myMMPort}}

######################################################################
####
#
test MMPort-2.1 {dims constructor, dimension 0} {
    # Used MMTypes from 1.1 above 
    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 0]
    # Results should be similar to 1.1 above 
    list \
        [$mmPort getDims] \
        [$mmPort getLength 0] \
        [$mmPort hasFixedSize] \
        [$mmPort isArray] \
        [$mmPort isScalar]
} {0 0 1 0 1}

######################################################################
####
#
test MMPort-2.2 {dims constructor, dimension 1} {
    # Used MMTypes from 1.1 above 
    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 1]
    list \
        [$mmPort getDims] \
        [$mmPort getLength 0] \
        [$mmPort hasFixedSize] \
        [$mmPort isArray] \
        [$mmPort isScalar]
} {1 0 0 1 0}

######################################################################
####
#
test MMPort-2.3 {dims constructor, dimension 2} {
    # Used MMTypes from 1.1 above 
    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 2]
    list \
        [$mmPort getDims] \
        [$mmPort getLength 0] \
        [$mmPort hasFixedSize] \
        [$mmPort isArray] \
        [$mmPort isScalar]
} {2 0 0 1 0}

######################################################################
####
#
test MMPort-3.1 {dims, int []  constructor, dim 0-3} {
    # Used MMTypes from 1.1 above 
    set results {}
    for {set i 1} {$i < 4} {incr i} {
        # Create arrays of size 1, 2 ,3
        set limits [java::new {int[]} $i]
        for {set j 0} {$j < $i} {incr j} {
            $limits set $j [expr {$j+1}]
        }
        set mmPort3_1 [java::new metropolis.metamodel.runtime.MMPort \
                        myMMPort $mmTypeInterface $mmType $i $limits]
        lappend results \
            [list \
                 [$mmPort3_1 getDims] \
                 [$mmPort3_1 getLength 0] \
                 [$mmPort3_1 hasFixedSize] \
                 [$mmPort3_1 isArray] \
                 [$mmPort3_1 isScalar] \
                 [$mmPort3_1 toString] "\n"] \
         }
    list $results 
} {{{1 0 1 1 0 {MMPort: myMMTypeInterface myMMPort[1]} {
}} {2 0 1 1 0 {MMPort: myMMTypeInterface myMMPort[1][2]} {
}} {3 0 1 1 0 {MMPort: myMMTypeInterface myMMPort[1][2][3]} {
}}}}

######################################################################
####
#
test MMPort-3.2 {dims, int []  constructor, dim does not match limits} {
    # Used MMTypes from 1.1 above 
    set limits [java::new {int[]} 2]
    catch {
            set mmPort4_1 [java::new metropolis.metamodel.runtime.MMPort \
                        myMMPort $mmTypeInterface $mmType 1 $limits]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Internal error, length of limits []  != dims (2 != 1)}}

######################################################################
####
#
test MMPort-4.1 {getLength} {
    # Used mmPort3_1 from 3.1 above
    list [$mmPort3_1 getLength -1] \
        [$mmPort3_1 getLength 0] \
        [$mmPort3_1 getLength 1] \
        [$mmPort3_1 getLength 2] \
        [$mmPort3_1 getLength 3] \
        [$mmPort3_1 getLength 100]
} {0 0 1 2 0 0}

######################################################################
####
#
test MMPort-4.2 {getLength} {
    # Uses mmTypes from 1.1 above 
    set mmPort4_2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 42]
    list [$mmPort4_2 getLength -1] \
        [$mmPort4_2 getLength 0] \
        [$mmPort4_2 getLength 1] \
        [$mmPort4_2 getLength 100]
} {0 0 0 0}

######################################################################
####
#
test MMPort-5.1 {instantiate} {
    # Used MMTypes from 1.1 above 
    set inode [java::new metropolis.metamodel.runtime.INode \
                   $mmType MyString MyINode, 666]
    
    set mmPort4 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType]

    set mmPort5 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 2]

    set iPortScalar4 [$mmPort4 instantiate $inode]
    set iPortScalar5 [$mmPort5 instantiate $inode]
    list \
        [$iPortScalar4 show] \
        [$iPortScalar4 numPorts] \
        [$iPortScalar5 show] \
        [$iPortScalar5 numPorts]

} {{myMMTypeInterface myMMPort} 1 {myMMTypeInterface myMMPort[][]} 0}

######################################################################
####
#
test MMPort-5.2 {show} {
    # Used mmTypes from 1.1 above 
    set mmPort5_2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 0]
    $mmPort5_2 show
} {myMMTypeInterface myMMPort}

######################################################################
####
#
test MMPort-5.3 {show} {
    # Used mmTypes from 1.1 above 
    set mmPort5_3 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 1]
    $mmPort5_3 show
} {myMMTypeInterface myMMPort[]}

######################################################################
####
#
test MMPort-5.4 {show} {
    # Used mmPort3_1 from 3.1 above
    $mmPort3_1 show
} {myMMTypeInterface myMMPort[1][2][3]}
