# Tests for the TypeIdentifier class
#
# @Author: Christopher Brooks
#
# @Version: $Id: TypeIdentifier.tcl,v 1.2 2005/11/22 20:12:03 allenh Exp $
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
test TypeIdentifier-1.0 {kind} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    list \
	[$typeIdentifier kind \
	     [java::new metropolis.metamodel.nodetypes.ArrayTypeNode]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.NullTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.TemplateTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.BoolTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.ByteTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.CharTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.ShortTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.IntTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.LongTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.FloatTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.DoubleTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.ArrayInitTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.VoidTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.EventTypeNode \
	      instance]] \
	[$typeIdentifier kind \
	     [java::field metropolis.metamodel.nodetypes.PCTypeNode \
	      instance]]
} {13 12 11 0 1 3 2 4 5 6 7 14 10 8 9}

######################################################################
####
#
test TypeIdentifier-1.2 {kind TypeNameNode} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    set rootTypeNameNode [java::new \
			      metropolis.metamodel.nodetypes.NameNode]
    set typeNameNode [java::new metropolis.metamodel.nodetypes.TypeNameNode \
			  $rootTypeNameNode]

    # This returns -1, which is probably not quite right
    list \
	[$typeIdentifier kind $typeNameNode]
} {-1}

######################################################################
####
#
test TypeIdentifier-2.1 {primitiveKindToType kind < 0} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    catch {$typeIdentifier primitiveKindToType -2} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Unknown type kind '-2' used as primitive type, type kind must be > 0.}}

######################################################################
####
#
test TypeIdentifier-2.2 {primitiveKindToType kind > NUM_PRIMITIVE_TYPES} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    set NUM_PRIMITIVE_TYPES [java::field \
				 metropolis.metamodel.frontend.TypeIdentifier \
				 NUM_PRIMITIVE_TYPES]

    catch {$typeIdentifier primitiveKindToType \
	       [expr {$NUM_PRIMITIVE_TYPES + 1}] } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Type kind '11' is not primitive, type must be < 10.}}

######################################################################
####
#
test TypeIdentifier-2.3 {primitiveKindToType cycle through legit primitive kinds} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    set NUM_PRIMITIVE_TYPES [java::field \
				 metropolis.metamodel.frontend.TypeIdentifier \
				 NUM_PRIMITIVE_TYPES]

    # Loop through the valid kinds
    set results {}
    for {set i 0} {$i < $NUM_PRIMITIVE_TYPES} {incr i} {
	lappend results [[$typeIdentifier primitiveKindToType $i] toString]
    }
    list $results
} {{{ {BoolTypeNode {leaf}}} { {ByteTypeNode {leaf}}} { {ShortTypeNode {leaf}}} { {CharTypeNode {leaf}}} { {IntTypeNode {leaf}}} { {LongTypeNode {leaf}}} { {FloatTypeNode {leaf}}} { {DoubleTypeNode {leaf}}} { {EventTypeNode {leaf}}} { {PCTypeNode {leaf}}}}}

######################################################################
####
#
test TypeIdentifier-3.1 {kindOfName} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    set rootTypeNameNode [java::new \
			      metropolis.metamodel.nodetypes.NameNode]
    set typeNameNode [java::new metropolis.metamodel.nodetypes.TypeNameNode \
			  $rootTypeNameNode]

    # This returns -1, which is probably not quite right
    $typeIdentifier kindOfName $typeNameNode
} {-1}

######################################################################
####
#
test TypeIdentifier-4.1 {kindOfDecl} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]
    set packageDecl [java::new metropolis.metamodel.frontend.PackageDecl \
			 "myPackageDecl" [java::null]]
    catch {[$typeIdentifier kindOfDecl $packageDecl]} errMsg

    list \
	[$typeIdentifier kindOfDecl [java::null]] \
	$errMsg


} {-1 {java.lang.RuntimeException: Declaration 'myPackageDecl' does not declare a type.}}

######################################################################
####
#
test TypeIdentifier-4.2 {kindOfDecl with a NetlistDecl} {
    set typeIdentifier [java::new metropolis.metamodel.frontend.TypeIdentifier]

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


    set NetlistDecl [java::new metropolis.metamodel.frontend.NetlistDecl \
			 "myNetlistDecl" \
			 $typeNameNode \
			 $modifiers \
			 $simpleTreeNode \
			 $packageDecl \
			 [java::new java.util.LinkedList]]

    list \
	[$typeIdentifier kindOfDecl $NetlistDecl]

} {21}