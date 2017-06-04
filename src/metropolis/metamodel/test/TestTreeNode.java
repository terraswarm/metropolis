/* Test suite class to test abstract TreeNode class

 Copyright (c) 2004-2005 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software and that appropriate acknowledgments are made
 to the research of the Metropolis group.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @ProposedRating Red (ctsay@eecs.berkeley.edu)
 @AcceptedRating Red (ctsay@eecs.berkeley.edu)
 */
package metropolis.metamodel.test;

import metropolis.metamodel.TreeNode;

// ////////////////////////////////////////////////////////////////////////
// // TestTreeNode
/**
 * A non-leaf node that can contain other TreeNodes TreeNode is an abstract
 * class, so we define this class for testing
 *
 * @author Christopher Brooks
 * @version $Id: TestTreeNode.java,v 1.8 2006/10/12 20:38:48 cxh Exp $
 */
public class TestTreeNode extends TreeNode {

    /**
     * Construct a TestTreeNode with an unspecified number of children to be
     * added to the child list later.
     */
    public TestTreeNode() {
        super();
    }

    /**
     * Construct a TestTreeNode with the specified number of children to be
     * added to the child list later.
     */
    public TestTreeNode(TreeNode child1, TreeNode child2) {
        _childList.add(child1);
        _childList.add(child2);
    }

    /**
     * Return the class ID number, which is unique for each sub-type. The ID
     * number is intended to be used in switch statements.
     *
     * @return A unique class ID number.
     */
    public int classID() {
        return 666;
    }

    public final TreeNode getChild1() {
        return (TreeNode) _childList.get(CHILD_INDEX_CHILD1);
    }

    public final void setChild1(TreeNode child1) {
        _childList.set(CHILD_INDEX_CHILD1, child1);
    }

    public final TreeNode getChild2() {
        return (TreeNode) _childList.get(CHILD_INDEX_CHILD2);
    }

    public final void setChild2(TreeNode child2) {
        _childList.set(CHILD_INDEX_CHILD2, child2);
    }

    public static final int CHILD_INDEX_CHILD1 = 0;

    public static final int CHILD_INDEX_CHILD2 = 1;
}
