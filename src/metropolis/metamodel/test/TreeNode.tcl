# Tests for the TreeNode class
#
# @Author: Christopher Brooks
#
# @Version: $Id: TreeNode.tcl,v 1.10 2005/11/22 20:05:24 allenh Exp $
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
test TreeNode-1.1 {Constructor} {
    set list1 [java::new java.util.LinkedList]
    set string1 [java::new metropolis.metamodel.test.TestStringNode One]
    $list1 add $string1

    set list2 [java::new java.util.LinkedList]
    set string2 [java::new metropolis.metamodel.test.TestStringNode Two]
    $list2 add $string2

    set simpleTreeNode [java::new metropolis.metamodel.test.TestTwoListNode \
		      $list1 $list2]
    list [$simpleTreeNode classID] [$simpleTreeNode toString] \
	[$simpleTreeNode isSingleton]
} {667 { {TestTwoListNode { 
  {Argument1 { 
   {TestStringNode { 
    {String One} 
  }}}} 
  {Argument2 { 
   {TestStringNode { 
    {String Two} 
  }}}} 
}}} 0}

######################################################################
####
#
test TreeNode-2.1 {Complex Constructor} {
    set list3 [java::new java.util.LinkedList]
    set string3 [java::new metropolis.metamodel.test.TestStringNode Three]
    $list3 add $string3

    set list4 [java::new java.util.LinkedList]
    set string4 [java::new metropolis.metamodel.test.TestStringNode Four]
    $list4 add $string4

    set listTreeNode2 [java::new metropolis.metamodel.test.TestTwoListNode \
		      $list3 $list4]

    set complexTreeNode [java::new metropolis.metamodel.test.TestTreeNode \
			     $listTreeNode2 [java::null]]

    list [$complexTreeNode classID] [$complexTreeNode toString]
} {666 { {TestTreeNode { 
  {Child1 {TestTwoListNode { 
           {Argument1 { 
            {TestStringNode { 
             {String Three} 
           }}}} 
           {Argument2 { 
            {TestStringNode { 
             {String Four} 
           }}}} 
         }}} 
  {Child2 null}
}}}}

####
#
# test TreeNode-2.1 {Constructor(int)} {
#     set treeNode [java::new metropolis.metamodel.test.TestTreeNode 6]
#     $treeNode setArgument1 One
#     $treeNode setArgument2 One
#     set childList [$treeNode children]
#     # Size of the childList is 0 because we have not yet added anything
#     list [$treeNode classID] \
# 	[$treeNode toString] \
# 	[$childList size]
# } {666 { {TestTreeNode {leaf}}} 0}

####
#
test TreeNode-3.1 {accept(IVisitor)} {
    set visitor3_1 [java::new metropolis.metamodel.test.TestVisitor]
    set simpleTreeNodeClone3_1 [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    jdkCapture {
	set r1 [$simpleTreeNodeClone3_1 accept $visitor3_1]
    } log
    catch {$simpleTreeNodeClone3_1 childReturnValueAt 0} errMsg
    list $r1 \
	$log \
	$errMsg

} {{{visitTestTwoListNode}} {{visitTestStringNode}
{visitTestStringNode}
{visitTestTwoListNode}
} {java.lang.RuntimeException: Property -3 not defined, Property Map is empty.}}
####
#
test TreeNode-4.1 {accept(IVisitor, visitorArgs, setChildReturnValues)} {
    set visitor4_1 [java::new metropolis.metamodel.test.TestVisitor \
		     [java::field metropolis.metamodel.IVisitor TM_CUSTOM]]
    set simpleTreeNodeClone4_1 [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    jdkCapture {
	set r1 [$simpleTreeNodeClone4_1 accept \
		    $visitor4_1 [java::new java.util.LinkedList] false]
    } log
    catch {$simpleTreeNodeClone4_1 childReturnValueAt 0} errMsg4_1
    list $r1 \
	$log \
	$errMsg4_1

} {{{visitTestTwoListNode}} {{visitTestTwoListNode}
} {java.lang.RuntimeException: Property -3 not defined, Property Map is empty.}}

####
#
test TreeNode-4.2 {accept(IVisitor, visitorArgs, setChildReturnValues) TM_SELF_FIRST} {
    set visitor4_2 [java::new metropolis.metamodel.test.TestVisitor \
		     [java::field metropolis.metamodel.IVisitor TM_SELF_FIRST]]
    set simpleTreeNodeClone4_2 [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    jdkCapture {
	set r1 [$simpleTreeNodeClone4_2 accept \
		    $visitor4_2 [java::new java.util.LinkedList] false]
    } log
    catch {$simpleTreeNodeClone4_2 childReturnValueAt 0} errMsg4_2
    list $r1 \
	$log \
	$errMsg4_2

} {{{visitTestTwoListNode}} {{visitTestTwoListNode}
{visitTestStringNode}
{visitTestStringNode}
} {java.lang.RuntimeException: Property -3 not defined, Property Map is empty.}}


####
#
test TreeNode-4.3 {accept(IVisitor, visitorArgs, setChildReturnValues) TM_SELF_FIRST, ignoreChildren} {
    set visitor4_3 [java::new metropolis.metamodel.test.TestVisitor \
		     [java::field metropolis.metamodel.IVisitor TM_SELF_FIRST]]
    set simpleTreeNodeClone4_3 [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    
    # Call ignoreChildren, which only affects TM_SELF_FIRST
    $simpleTreeNodeClone4_3 ignoreChildren

    jdkCapture {
	set r1 [$simpleTreeNodeClone4_3 accept \
		    $visitor4_3 [java::new java.util.LinkedList] false]
    } log
    catch {$simpleTreeNodeClone4_3 childReturnValueAt 0} errMsg4_3
    list $r1 \
	$log \
	$errMsg4_3

} {{{visitTestTwoListNode}} {{visitTestTwoListNode}
} {java.lang.RuntimeException: Property -3 not defined, Property Map is empty.}}

####
#
test TreeNode-4.4 {accept(IVisitor, visitorArgs, setChildReturnValues), illegal Traversal method of -1} {
    set visitor4_4 [java::new metropolis.metamodel.test.TestVisitor -1]
    set simpleTreeNodeClone4_4 [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    catch {$simpleTreeNodeClone4_4 accept \
	       $visitor4_4 [java::new java.util.LinkedList] false} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Unknown traversal method '-1' for visitor.}}

####
#
test TreeNode-8.1 {children} {
    # Uses 1.2 above
    set childList [$complexTreeNode children]
    list [$childList size] \
	[java::isnull [$childList get 1]] \
	[[$childList get 0] toString]
} {2 1 { {TestTwoListNode { 
  {Argument1 { 
   {TestStringNode { 
    {String Three} 
  }}}} 
  {Argument2 { 
   {TestStringNode { 
    {String Four} 
  }}}} 
}}}}

####
#
test TreeNode-10.1 {clone} {
    # Uses 1.1 above
    set c [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    # Should be the same as 
    list [expr {[$c toString] == [$simpleTreeNode toString]}]
} {1}

####
#
test TreeNode-10.2 {clone with a null element in the list} {
    # Uses 1.2 above
    catch {
	set c [java::cast metropolis.metamodel.TreeNode [$complexTreeNode clone]]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: null in list: class metropolis.metamodel.ChildList}}


####
#
test TreeNode-10.2 {clone: Fix up complexTreeNode and clone} {
    # Uses 1.2 above

    set string5 [java::new metropolis.metamodel.test.TestStringNode Five]

    # Modifies complexTreeNode from 1.2
    $complexTreeNode setChild2 $string5

    set c2 [java::cast metropolis.metamodel.TreeNode [$complexTreeNode clone]]
    list [expr {[$c2 toString] == [$complexTreeNode toString]}]
} {1}

####
#
test TreeNode-10.3 {clone - childList is null} {
    # Uses 1.1 above
    set c [java::cast metropolis.metamodel.TreeNode [$simpleTreeNode clone]]
    # Should be the same as 
    list [expr {[$c toString] == [$simpleTreeNode toString]}]
} {1}

####
#
test TreeNode-13.1 {isSingleton, clone} {
    set singleton [java::new metropolis.metamodel.test.TestSingletonNode Singleton-node]
    # clone of a singleton should return 'this'
    set singletonClone [$singleton clone]
    list [$singleton isSingleton] \
	[$singleton equals $singletonClone]
} {1 1}


####
#
test TreeNode-16.1 {setAsParent, getParent} {
    # Uses 1.1 above
    set simpleTreeNodeClone1 [java::cast metropolis.metamodel.TreeNode \
				 [$simpleTreeNode clone]]

    set simpleTreeNodeClone2 [java::cast metropolis.metamodel.TreeNode \
				 [$simpleTreeNodeClone1 clone]]

    set r1 [$simpleTreeNodeClone1 getParent]
    set r2 [$simpleTreeNodeClone2 getParent]
    $simpleTreeNodeClone2 setAsParent $simpleTreeNodeClone1
    set r3 [$simpleTreeNodeClone1 getParent]
    set r4 [$simpleTreeNodeClone2 getParent]
    list [java::isnull $r1] \
	[java::isnull $r2] \
	[$r3 equals $simpleTreeNodeClone2] \
	[java::isnull $r4]
} {1 1 1 1}


####
#
test TreeNode-17.1 {setChild, getChild} {
    set treeNode17 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $treeNode17]

    $childList add "Zero"
    $childList add "One"
    $treeNode17 setChildren $childList
    $treeNode17 setChild 1 "NewOne"
    catch {$treeNode17 setChild 2 "NewOne"} errMsg
    list [$treeNode17 getChild 0] \
	[$treeNode17 getChild 1] \
	[java::isnull [$treeNode17 getChild 2]] \
	$errMsg
} {Zero NewOne 1 {java.lang.IndexOutOfBoundsException: Index: 2, Size: 2}}

####
#
test TreeNode-18.1 {setChildren, getChild} {
    set treeNode18 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList18 [java::new metropolis.metamodel.ChildList $treeNode18]

    $childList18 add "Zero"
    $childList18 add "One"
    $treeNode18 setChildren $childList18
    list [$treeNode18 getChild 0] \
	[$treeNode18 getChild 1] \
	[java::isnull [$treeNode18 getChild -1]] \
	[java::isnull [$treeNode18 getChild 2]]
} {Zero One 1 1}

####
#
test TreeNode-20.1 {toString(String)} {
    # uses 1.1 above
    list [$simpleTreeNode toString "-->"]
} {{ {TestTwoListNode { 
-->  {Argument1 { 
-->   {TestStringNode { 
-->    {String One} 
-->  }}}} 
-->  {Argument2 { 
-->   {TestStringNode { 
-->    {String Two} 
-->  }}}} 
-->}}}}

####
#
test TreeNode-20.2 {toString - node has a NUMBER_KEY property} {
    set stringNode [java::new metropolis.metamodel.test.TestStringNode Twenty]

    $stringNode defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]

    set object2 [$stringNode setDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY] \
		     [java::new Integer 20]]

    $stringNode toString
} { {TestStringNode (20) { 
  {String Twenty} 
}}}

####
#
test TreeNode-21.1 {traverseChildren(IVisitor, LinkedList} {
    set visitor [java::new metropolis.metamodel.test.TestVisitor]
    set complexTreeNodeClone \
	[java::cast metropolis.metamodel.TreeNode [$complexTreeNode clone]]
    jdkCapture {
	$complexTreeNodeClone traverseChildren $visitor \
	    [java::new java.util.LinkedList]
    } log
    catch {$complexTreeNodeClone childReturnValueAt 2} errMsg
    list \
	$log "\n" \
	[$complexTreeNodeClone childReturnValueAt 0] \
	[$complexTreeNodeClone childReturnValueAt 1] "\n"\
	$errMsg
} {{{visitTestStringNode}
{visitTestStringNode}
{visitTestTwoListNode}
{visitTestStringNode}
} {
} {{visitTestTwoListNode}} {{visitTestStringNode}} {
} {java.lang.IndexOutOfBoundsException: Index: 2, Size: 2}}


####
#
test TreeNode-21.2 {traverseChildren(IVisitor, LinkedList, TM_SELF_FIRST} {
    set visitor [java::new metropolis.metamodel.test.TestVisitor \
		     [java::field metropolis.metamodel.IVisitor TM_SELF_FIRST]]
    set complexTreeNodeClone2 \
	[java::cast metropolis.metamodel.TreeNode [$complexTreeNode clone]]
    jdkCapture {
	$complexTreeNodeClone2 traverseChildren $visitor \
	    [java::new java.util.LinkedList] } log
    catch {$complexTreeNodeClone2 childReturnValueAt 2} errMsg
    list \
	$log "\n" \
	[$complexTreeNodeClone2 childReturnValueAt 0] \
	[$complexTreeNodeClone2 childReturnValueAt 1] "\n" \
	$errMsg
} {{{visitTestTwoListNode}
{visitTestStringNode}
{visitTestStringNode}
{visitTestStringNode}
} {
} {{visitTestTwoListNode}} {{visitTestStringNode}} {
} {java.lang.IndexOutOfBoundsException: Index: 2, Size: 2}}


####
#
test TreeNode-21.3 {traverseChildren(IVisitor, LinkedList, TM_CUSTOM} {
    set visitor [java::new metropolis.metamodel.test.TestVisitor \
		     [java::field metropolis.metamodel.IVisitor TM_CUSTOM]]
    set complexTreeNodeClone3 \
	[java::cast metropolis.metamodel.TreeNode [$complexTreeNode clone]]
    jdkCapture {
	$complexTreeNodeClone3 traverseChildren $visitor \
	    [java::new java.util.LinkedList] } log
    catch {$complexTreeNodeClone3 childReturnValueAt 2} errMsg
    list \
	$log "\n" \
	[$complexTreeNodeClone3 childReturnValueAt 0] \
	[$complexTreeNodeClone3 childReturnValueAt 1] "\n" \
	$errMsg
} {{{visitTestTwoListNode}
{visitTestStringNode}
} {
} {{visitTestTwoListNode}} {{visitTestStringNode}} {
} {java.lang.IndexOutOfBoundsException: Index: 2, Size: 2}}

####
#
test TreeNode-22.1 {traverseChildren(IVisitor, LinkedList, boolean)} {
    set visitor [java::new metropolis.metamodel.test.TestVisitor \
		     [java::field metropolis.metamodel.IVisitor TM_CHILDREN_FIRST]]
    set complexTreeNodeClone4 \
	[java::cast metropolis.metamodel.TreeNode [$complexTreeNode clone]]
    jdkCapture {
	$complexTreeNodeClone4 traverseChildren $visitor \
	    [java::new java.util.LinkedList] false } log
    catch {$complexTreeNodeClone4 childReturnValueAt 0} errMsg
    list \
	$log "\n" \
	$errMsg
} {{{visitTestStringNode}
{visitTestStringNode}
{visitTestTwoListNode}
{visitTestStringNode}
} {
} {java.lang.RuntimeException: Property -3 not defined, Property Map is empty.}}
####
#
test TreeNode-23.1 {childReturnValueFor} {
    # Uses 21.3 above
    set zero [$complexTreeNodeClone3 -noconvert getChild 0]
    set one [$complexTreeNodeClone3 -noconvert getChild 1]
    catch {$complexTreeNodeClone3 childReturnValueFor [java::null]} errMsg
    list \
	[$complexTreeNodeClone3 childReturnValueFor $zero] \
	[$complexTreeNodeClone3 childReturnValueFor $one] \
	$errMsg
} {{{visitTestTwoListNode}} {{visitTestStringNode}} {java.lang.RuntimeException: TreeNode.childReturnValueFor(): Child not found}}

