# Create an INode
#
# @Author: Christopher Brooks
#
# @Version: $Id: makeINode.tcl,v 1.6 2005/11/22 20:11:48 allenh Exp $
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

proc makeINode {{typeInterface INTERFACE} {type CLASS} {iNodeName myINode}} {
    global mmPort
    # Very similar to INode-1.1         
    set superClass [java::null]
    set superInterfaces [java::null]
    set mmTypeInterface [java::new metropolis.metamodel.runtime.MMType \
            myMMTypeInterface \
            [java::field metropolis.metamodel.runtime.MMType $typeInterface ] \
            $superClass \
            $superInterfaces]

    set mmType [java::new metropolis.metamodel.runtime.MMType \
            myMMType \
            [java::field metropolis.metamodel.runtime.MMType $type] \
            $superClass \
            $superInterfaces]

    set mmPort [java::new metropolis.metamodel.runtime.MMPort \
                    myMMPort $mmTypeInterface $mmType]

    $mmTypeInterface addPort $mmPort

    set iNode [java::new metropolis.metamodel.runtime.INode \
                   $mmTypeInterface iNodeObject $iNodeName 42]
    set net [java::field metropolis.metamodel.runtime.Network net]

    # Add it to the network so that when we construct the Event
    # the process gets added        
    $net addNode $iNode $iNode

    return $iNode
}
