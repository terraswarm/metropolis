# Tests for ChildList
#
# @Author: Christopher Brooks
#
# @Version: $Id: ChildList.tcl,v 1.5 2005/11/22 20:05:21 allenh Exp $
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs listToStrings] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test ChildList-1.1 {Call the constructors} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode 9]
    list [$childList size] [$childList2 size] \
	[[$childList getParent] equals $parentTreeNode] \
	[[$childList2 getParent] equals $parentTreeNode]
} {0 0 1 1}


######################################################################
####
#
test ChildList-2.1 {add} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    set r1 [$childList add "Zero"]
    set r2 [$childList add "One"]

    list  $r1 $r2 [listToStrings $childList] 
} {1 1 {Zero One}}

######################################################################
####
#
test ChildList-3.1 {add int} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    # Size is 0, can't add at index 1 yet.
    catch {$childList add 1 "One"} errMsg
    # Can add at index 0 though.
    $childList add 0 "Zero"

    list $errMsg [listToStrings $childList] 
} {{java.lang.IndexOutOfBoundsException: Index: 1, Size: 0} Zero}

######################################################################
####
#
test ChildList-3.2 {add int - Simple Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]

    $childList add "Zero"
    $childList add "One"
    $childList add "Two"

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]
    $childList2 add "A"
    $childList2 add "B"
    $childList2 add "C"

    $childList add 1 $childList2
    list [listToStrings $childList] \
		[[$childList getParent] equals $parentTreeNode] \
		[[$childList2 getParent] equals $parentTreeNode2]
} {{Zero {[A, B, C]} One Two} 1 1}

######################################################################
####
#
test ChildList-4.1 {addAll Simple Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    $childList add "Zero"
    $childList add "One"

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]

    $childList2 add "A"
    $childList2 add "B"
    $childList2 add "C"

    $childList addAll $childList2
    list [listToStrings $childList] \
	[[$childList getParent] equals $parentTreeNode] \
	[[$childList2 getParent] equals $parentTreeNode2]
} {{Zero One A B C} 1 1}


######################################################################
####
#
test ChildList-4.2 {addAll TreeNode Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    set treeNode1 [java::new metropolis.metamodel.test.TestTreeNode]
    set treeNode2 [java::new metropolis.metamodel.test.TestTreeNode]

    $childList add $treeNode1
    $childList add $treeNode2

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]

    set treeNode3 [java::new metropolis.metamodel.test.TestTreeNode]
    set treeNode4 [java::new metropolis.metamodel.test.TestTreeNode]

    $childList2 add $treeNode3
    $childList2 add $treeNode4

    set r1 [list \
		[[$treeNode1 getParent] equals $parentTreeNode] \
		[[$treeNode2 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode2] \
		[[$treeNode4 getParent] equals $parentTreeNode2]]
    
    # addAll will set the parents of treeNode3 and 4 to $parentTreeNode
    $childList addAll $childList2
    set r2 [list \
		[[$treeNode1 getParent] equals $parentTreeNode] \
		[[$treeNode2 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode2] \
		[[$treeNode4 getParent] equals $parentTreeNode2] \
		[[$treeNode3 getParent] equals $parentTreeNode] \
		[[$treeNode4 getParent] equals $parentTreeNode]]

    list $r1 $r2 [$childList size]
} {{1 1 1 1} {1 1 0 0 1 1} 4}

######################################################################
####
#
test ChildList-5.1 {addAll  int - Simple Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]

    $childList add "Zero"
    $childList add "One"
    $childList add "Two"

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]
    $childList2 add "A"
    $childList2 add "B"
    $childList2 add "C"

    $childList addAll 1 $childList2
    list [listToStrings $childList] \
		[[$childList getParent] equals $parentTreeNode] \
		[[$childList2 getParent] equals $parentTreeNode2]
} {{Zero A B C One Two} 1 1}

######################################################################
####
#
test ChildList-5.2 {addAll int TreeNode Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    set treeNode1 [java::new metropolis.metamodel.test.TestTreeNode]
    set treeNode2 [java::new metropolis.metamodel.test.TestTreeNode]

    $childList add $treeNode1
    $childList add $treeNode2

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]

    set treeNode3 [java::new metropolis.metamodel.test.TestTreeNode]
    set treeNode4 [java::new metropolis.metamodel.test.TestTreeNode]

    $childList2 add $treeNode3
    $childList2 add $treeNode4

    set r1 [list \
		[[$treeNode1 getParent] equals $parentTreeNode] \
		[[$treeNode2 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode2] \
		[[$treeNode4 getParent] equals $parentTreeNode2]]
    
    # addAll will set the parents of treeNode3 and 4 to $parentTreeNode
    $childList addAll 1 $childList2
    set r2 [list \
		[[$treeNode1 getParent] equals $parentTreeNode] \
		[[$treeNode2 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode2] \
		[[$treeNode4 getParent] equals $parentTreeNode2] \
		[[$treeNode3 getParent] equals $parentTreeNode] \
		[[$treeNode4 getParent] equals $parentTreeNode]]

    list $r1 $r2 [$childList size]
} {{1 1 1 1} {1 1 0 0 1 1} 4}

######################################################################
####
#
test ChildList-6.1 {set - Simple Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    $childList add "Zero"
    $childList add "One"
    $childList add "Two"

    $childList set 1 ONE
    list [listToStrings $childList]
} {{Zero ONE Two}}

######################################################################
####
#
test ChildList-6.2 {set TreeNode Version} {
    set parentTreeNode [java::new metropolis.metamodel.test.TestTreeNode]
    set childList [java::new metropolis.metamodel.ChildList $parentTreeNode]

    set treeNode1 [java::new metropolis.metamodel.test.TestTreeNode]
    set treeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set treeNode3 [java::new metropolis.metamodel.test.TestTreeNode]

    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList2 [java::new metropolis.metamodel.ChildList $parentTreeNode2]

    set treeNode3 [java::new metropolis.metamodel.test.TestTreeNode]

    $childList2 add $treeNode3

    $childList add $treeNode1
    $childList add $treeNode2

    set r1 [list \
		[[$treeNode1 getParent] equals $parentTreeNode] \
		[[$treeNode2 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode2]] 

    # set will set the parents of treeNode3 and 4 to $parentTreeNode
    $childList set 0 $treeNode3

    set r2 [list \
		[[$treeNode1 getParent] equals $parentTreeNode] \
		[[$treeNode2 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode] \
		[[$treeNode3 getParent] equals $parentTreeNode2]]

    list $r1 $r2 [$childList size]
} {{1 1 0 1} {1 1 1 0} 2}

######################################################################
####
#
test ChildList-20.1 {setParent} {
    set parentTreeNode2 [java::new metropolis.metamodel.test.TestTreeNode]
    set childList3 [java::new metropolis.metamodel.ChildList $parentTreeNode2]
    set parentTreeNode3 [java::new metropolis.metamodel.test.TestTreeNode]
    $childList3 setParent $parentTreeNode3
    list [$childList3 size] \
	[[$childList3 getParent] equals $parentTreeNode3]
} {0 1}

