# Tests for the TNLManip class
#
# @Author: Christopher Brooks
#
# @Version: $Id: TNLManip.tcl,v 1.10 2005/11/22 20:05:28 allenh Exp $
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

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
test TNLManip-1.1 {addFirst} {
    set list [java::call metropolis.metamodel.TNLManip addFirst "One"]
    set list [java::call metropolis.metamodel.TNLManip addFirst "Two"]    
    $list getFirst
} {Two}

######################################################################
####
#
test TNLManip-1.2 {addFirst object Linked List} {
    set list [java::call metropolis.metamodel.TNLManip addFirst "Three"]    
    set list [java::call metropolis.metamodel.TNLManip addFirst \
		  [java::null] $list ]
    set list [java::call metropolis.metamodel.TNLManip addFirst "One" $list]
    set nullValue [java::field metropolis.metamodel.NullValue instance]
    set second [$list get 1]
    list [$list size] [$list get 0] [$nullValue equals $second] [$list get 2]
} {3 One 1 Three}

######################################################################
####
#
test TNLManip-3.1 {arrayToList} {
    set objectArray [java::new {Object[]}  {2} [list "first" [java::null]]]
    set list [java::call metropolis.metamodel.TNLManip arrayToList \
		  $objectArray]    
    set nullValue [java::field metropolis.metamodel.NullValue instance]
    set second [$list get 1]
    list [$list size] [$list get 0] [$nullValue equals $second]
} {2 first 1}

######################################################################
####
#
test TNLManip-3.2 {arrayToList - empty array} {
    set objectArray [java::new {Object[]}  {0} {}]
    set list [java::call metropolis.metamodel.TNLManip arrayToList \
		  $objectArray]    
    list [$list size]
} {0}

######################################################################
####
#
test TNLManip-4.1 {cloneList - empty List} {
    set list [java::new java.util.LinkedList]
    set list2 [java::call metropolis.metamodel.TNLManip cloneList $list]
    list [$list2 size]
} {0}

######################################################################
####
#
test TNLManip-4.2 {cloneList - null List} {
    set list [java::null]
    set list2 [java::call metropolis.metamodel.TNLManip cloneList $list]
    list [java::isnull $list2]
} {1}

######################################################################
####
#
test TNLManip-4.3 {cloneList - List contains something other than a TreeNode or a List} {
    set list [java::new java.util.LinkedList]
    $list add StringElement
    catch {java::call metropolis.metamodel.TNLManip cloneList $list} errMsg
    list $errMsg
} {{java.lang.RuntimeException: unknown object in list: class java.lang.String}}

######################################################################
####
#
test TNLManip-5.1 {showTree} {
    # See also TreeNode-2.1 in TreeNode.tcl 
    set list3 [java::new java.util.LinkedList]
    set string3 [java::new metropolis.metamodel.test.TestStringNode Three]
    $list3 add $string3

    set list4 [java::new java.util.LinkedList]
    set string4 [java::new metropolis.metamodel.test.TestStringNode Four]
    $list4 add $string4

    set listTreeNode2 [java::new metropolis.metamodel.test.TestTwoListNode \
		      $list3 $list4]

    set listTreeNode3 [java::new metropolis.metamodel.test.TestTwoListNode \
		      $list4 $list3]

    set complexTreeNode [java::new metropolis.metamodel.test.TestTreeNode \
			     $listTreeNode2 $listTreeNode3]

    jdkCapture {
	java::call metropolis.metamodel.TNLManip showTree $complexTreeNode ""
    } log

    list $log
} {{metropolis.metamodel.test.TestTreeNode
  metropolis.metamodel.test.TestTwoListNode
    java.util.LinkedList
      metropolis.metamodel.test.TestStringNode
    java.util.LinkedList
      metropolis.metamodel.test.TestStringNode
  metropolis.metamodel.test.TestTwoListNode
    java.util.LinkedList
      metropolis.metamodel.test.TestStringNode
    java.util.LinkedList
      metropolis.metamodel.test.TestStringNode
}}

######################################################################
####
#
test TNLManip-5.2 {showTree - node is neither a TreeNode or a List} {
    set list [java::new java.util.LinkedList]
    $list add StringElement
    jdkCapture {
	java::call metropolis.metamodel.TNLManip showTree $list ""
    } log
    list $log
} {{java.util.LinkedList
  java.lang.String
}}

######################################################################
####
#
test TNLManip-6.1 {toString} {
    # Uses setup in 5.1 above
    java::call metropolis.metamodel.TNLManip toString $list4
} {{ 
  {TestStringNode { 
   {String Four} 
 }}}}

######################################################################
####
#
test TNLManip-6.2 {toString with a list that has neither a TreeNode nor a List } {
    set list5 [java::new java.util.LinkedList]
    $list5 add string5
    catch {java::call metropolis.metamodel.TNLManip toString $list5} errMsg
    list $errMsg
} {{java.lang.RuntimeException: toString([string5], ""): unknown object in list: class java.lang.String}}

######################################################################
####
#
test TNLManip-6.3 {toString with an empty list} {
    set list6 [java::new java.util.LinkedList]
    java::call metropolis.metamodel.TNLManip toString $list6
} { {}}

######################################################################
####
#
test TNLManip-6.4 {toString with an nest list} {
    set list7 [java::new java.util.LinkedList]
    set string7 [java::new metropolis.metamodel.test.TestStringNode Seven]
    $list7 add $string7

    set list8 [java::new java.util.LinkedList]
    set string8 [java::new metropolis.metamodel.test.TestStringNode Eight]
    $list8 add $string8

    $list8 add $list7
    java::call metropolis.metamodel.TNLManip toString $list8
} {{ 
  {TestStringNode { 
   {String Eight} 
 }} { 
   {TestStringNode { 
    {String Seven} 
  }}}}}

######################################################################
####
#
test TNLManip-7.1 {traverseList - List element is not a TreeNode or List } {
    set list [java::new java.util.LinkedList]
    $list add StringElement
    set visitor [java::new metropolis.metamodel.test.TestVisitor]
    catch {java::call metropolis.metamodel.TNLManip traverseList \
	       $visitor [java::new java.util.LinkedList] $list true} errMsg
    list $errMsg
} {{java.lang.RuntimeException: TNLManip.traverseList(): object must be either a TreeNode or a List. Unknown object in list: class java.lang.String}}
