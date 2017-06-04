# Tests for the ObjectDecl class
#
# @Author: Christopher Brooks
#
# @Version: $Id: ObjectDecl.tcl,v 1.2 2005/11/22 20:12:04 allenh Exp $
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Metropolis test bed $METRO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs iterToObjects] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

if {[info procs jdkCapture] == "" } then {
    source [ file join $METRO util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test ObjectDecl-1.1 {ObjectDecl is abstract, so we use NetlistDecl} {

    set rootTypeNameNode [java::new \
			      metropolis.metamodel.nodetypes.NameNode]
    set typeNameNode [java::new metropolis.metamodel.nodetypes.TypeNameNode \
			  $rootTypeNameNode]

    # Simple TreeNode
    set list1 [java::new java.util.LinkedList]
    set string1 [java::new metropolis.metamodel.test.TestStringNode One]
    $list1 add $string1

    set list2 [java::new java.util.LinkedList]
    set string2 [java::new metropolis.metamodel.test.TestStringNode Two]
    $list2 add $string2

    set simpleTreeNode [java::new metropolis.metamodel.test.TestTwoListNode \
		      $list1 $list2]

    set modifiers 0
    set packageDecl [java::new metropolis.metamodel.frontend.PackageDecl \
			 "myPackageDecl" [java::null]]


    set netlistDecl [java::new metropolis.metamodel.frontend.NetlistDecl \
			 "myNetlistDecl" \
			 $typeNameNode \
			 $modifiers \
			 $simpleTreeNode \
			 $packageDecl \
			 [java::new java.util.LinkedList]]

    set interfaceDecl [java::new metropolis.metamodel.frontend.InterfaceDecl \
			   "myInterfaceDecl" \
			   $typeNameNode \
			   $modifiers \
			   $simpleTreeNode \
			   $packageDecl \
			   [java::new java.util.LinkedList]]


    set PORT_DECL [java::field \
		       metropolis.metamodel.frontend.MetaModelLibrary \
		       PORT_DECL]

    set interfaceDecl2 [java::new metropolis.metamodel.frontend.InterfaceDecl \
			    "myInterfaceDecl2" \
			    $typeNameNode \
			    $modifiers \
			    $simpleTreeNode \
			    $PORT_DECL \
			    [java::new java.util.LinkedList]]

    list \
	[$netlistDecl isPortInterface] \
	[$interfaceDecl isPortInterface] \
	[$PORT_DECL isPortInterface] \
	[$interfaceDecl2 isPortInterface]
} {0 0 1 0}


######################################################################
####
#
test ObjectDecl-2.1 {getInterfaces} {
    # Uses 1.1 above
    list \
	[[$netlistDecl getInterfaces] size] \
	[[$interfaceDecl getInterfaces] size] \
	[[$PORT_DECL getInterfaces] size] \
	[[$interfaceDecl2 getInterfaces] size]
} {0 0 1 0}


######################################################################
####
#
test ObjectDecl-2.2 {getInterfaces PORT_DECL} {
    # Uses 1.1 above
    set interfaces [$PORT_DECL getInterfaces] 
    set interface [java::cast metropolis.metamodel.frontend.MetaModelDecl \
		       [$interfaces get 0]]

    list [$interface toString]
} {{{Interface, 2}}}

