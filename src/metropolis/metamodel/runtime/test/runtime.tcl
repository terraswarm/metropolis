# Tests for INetlist class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: runtime.tcl,v 1.8 2005/11/22 20:11:54 allenh Exp $
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

####
# Create various metropolis.metamodel.runtime objects
proc createRuntimeObjects {} {

    global superClass superInterfaces
    global mmTypeInterface mmType iNodeSource iNodeDestination
    global mmPort2 iNode2 iPortArray2 iPortElem2 
    global myString3 iNetlist3 myConnection

    global mmTypeInterface2 iPortElem

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

    set iNodeSource [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject myINodeSource 42]
    set iNodeDestination [java::new metropolis.metamodel.runtime.INode \
            $mmTypeInterface iNodeObject myINodeDestination 42]


    ####
    # See IPortElem-1.1
    set mmPort2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 2]

    set iNode2 [java::new metropolis.metamodel.runtime.INode \
                   $mmType MyString MyINode2 666]

    set iPortArray2 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort2 $iNode2]

    set limits [java::new {int[]} 2 ]
    $limits set 0 1
    $limits set 1 2
    $iPortArray2 allocate $limits

    set index [java::new {int[]} 2 ]
    $index set 0 1
    $index set 1 2

    set iPortElem2 [java::new metropolis.metamodel.runtime.IPortElem \
                       $iPortArray2 $index 0]
    ####

    set myString3 [java::new String myString2]

    set mmTypeInterface2 [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface2 \
            [java::field metropolis.metamodel.runtime.MMType INTERFACE ] \
            $superClass \
            $superInterfaces]

    set iNetlist3 [java::new metropolis.metamodel.runtime.INetlist \
            $mmTypeInterface2 $myString3 myINetlist3 44]

    set mmPort2 [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType 2]

    set inode [java::new metropolis.metamodel.runtime.INode \
                   $mmType MyString MyINode 666]

    set iPortArray2 [java::new metropolis.metamodel.runtime.IPortArray \
                        $mmPort2 $inode]

    set limits [java::new {int[]} 2 ]
    $limits set 0 1
    $limits set 1 2
    $iPortArray2 allocate $limits

    set index [java::new {int[]} 2 ]
    $index set 0 1
    $index set 1 2

    set iPortElem [java::new metropolis.metamodel.runtime.IPortElem \
                       $iPortArray2 $index 0]

    set myConnection [java::new metropolis.metamodel.runtime.Connection \
            $iNodeSource $iNodeDestination $iPortElem $iNetlist3 [java::null]]
}
