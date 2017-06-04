# Tests for IPortElment class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: IPortArray.tcl,v 1.10 2005/11/22 20:11:52 allenh Exp $
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

if {[info procs objectsToStrings] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test IPortArray-1.1 {constructor} {
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType CLASS] \
            $superClass \
            $superInterfaces]

    set mmTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $superClass \
            $superInterfaces]

    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType]

    set inode [java::new metropolis.metamodel.runtime.INode \
                   $mmType MyString MyINode 666]


    set iPortArray [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort $inode]
    # Call methods in this class and in the parent class
    list \
        [$iPortArray getName] \
        [$mmTypeInterface equals [$iPortArray getInterface]] \
        [$inode equals [$iPortArray getContainer]] \
        [$mmPort equals [$iPortArray getDecl]] \
        [$iPortArray numPorts] \
        [$iPortArray show] \
        [$iPortArray toString]
} {myMMPort 1 1 1 1 {myMMTypeInterface myMMPort} {IPortArray: myMMPort}}


####
#
test IPortArray-2.1 {allocate() zero length } {
    # Uses 1.1 above
    set limits [java::new {int[]} 0]
    $iPortArray allocate $limits
} {}

####
#
test IPortArray-2.2 {allocate() lengths not the same } {
    # Uses 1.1 above
    set limits [java::new {int[]} 1]
    catch {$iPortArray allocate $limits} errMsg
    list $errMsg 
} {{java.lang.RuntimeException: Internal error allocating an array of ports, length of limits argument should be equal to length of limits of 'myMMPort': Length of limits arg (1) != Length of decl 'myMMPort' limits (0)}}

####
#
test IPortArray-2.3 {allocate()} {
    # Uses 1.1 above
    set limits [java::new {int[]} 1]
    set mmPort1 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 1]
    set iPortArray1 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort1 $inode]

    $iPortArray1 allocate $limits
    list \
        [$iPortArray1 show] \
        [$iPortArray1 numPorts]
} {{myMMTypeInterface myMMPort[]} 0}

####
#
test IPortArray-2.4 {allocate() limits array has a negative value } {
    # Uses 1.1 above
    set limits [java::new {int[]} 1 ]
    $limits set 0 -1
    set mmPort1 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 1]
    set iPortArray1 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort1 $inode]

    catch {$iPortArray1 allocate $limits} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Size of array of ports 'myMMPort' undefined (-1< 0)}}

####
#
test IPortArray-2.5 {allocate() limit array of length 2} {
    # Uses 1.1 above
    set limits [java::new {int[]} 2 ]
    $limits set 0 2
    $limits set 1 3
    set mmPort2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 2]
    set iPortArray2 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort2 $inode]

    $iPortArray2 allocate $limits
    list \
        [$iPortArray2 show] \
        [$iPortArray2 numPorts]
} {{myMMTypeInterface myMMPort[2][3]} 6}

####
#
test IPortArray-3.0 {get(int) } {
    # Uses 2.5 above
    set results {}
    for {set i 0} {$i < 6} {incr i} {
        lappend results [[$iPortArray2 {getElem int} $i] toString] "\n"
    }
    list $results
} {{{myMMPort[0][0]} {
} {myMMPort[1][0]} {
} {myMMPort[0][1]} {
} {myMMPort[1][1]} {
} {myMMPort[0][2]} {
} {myMMPort[1][2]} {
}}}

####
#
test IPortArray-3.1 {get(int) - bogus index} {
    # Uses 2.5 above
    set results {}
    set r1 [java::isnull [$iPortArray2 {getElem int} -1]]
    set r2 [java::isnull [$iPortArray2 {getElem int} 6]]
    list $r1 $r2
} {1 1}


####
#
test IPortArray-4.0 {get(int[])} {
    # Uses 2.5 above
    set index [java::new {int[]} 2 ]
    set r1 [[$iPortArray2 {getElem int[]} $index] toString]
    $index set 0 1
    $index set 1 2
    set r2 [[$iPortArray2 {getElem int[]} $index] toString]
    list $r1 $r2
} {{myMMPort[0][0]} {myMMPort[1][2]}}

####
#
test IPortArray-4.1 {get(int[]) - wrong argument length} {
    # Uses 2.5 above
    set index [java::new {int[]} 3 ]
    catch {$iPortArray2 {getElem int[]} $index} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Wrong limits in array of ports, argument size was 3 should have been 2}}

####
#
test IPortArray-4.2 {get(int[]) - wrong value in argument} {
    # Uses 2.5 above
    set index [java::new {int[]} 2 ]
    $index set 0 -1
    catch {$iPortArray2 {getElem int[]} $index} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Array out of bounds in access to array of ports, index[0] == -1, which is less than zero.}}

####
#
test IPortArray-4.3 {get(int[]) - wrong value in argument} {
    # Uses 2.5 above
    set index [java::new {int[]} 2 ]
    $index set 0 2
    catch {$iPortArray2 {getElem int[]} $index} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Array out of bounds in access to array of ports, index[0] == 2, which is >= 2}}

####
#
test IPortArray-5.0 {getAllElements()} {
    # Uses 2.5 above
    set allElements [$iPortArray2 {getAllElements}] 
    # objectsToStrings is defined in enums.tcl
    list [objectsToStrings [$allElements getrange 0]]
} {{{myMMPort[0][0]} {myMMPort[1][0]} {myMMPort[0][1]} {myMMPort[1][1]} {myMMPort[0][2]} {myMMPort[1][2]}}}
