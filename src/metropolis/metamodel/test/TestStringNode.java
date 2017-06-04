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
// // TestStringNode
/**
 * A leaf node that contains a string.
 *
 * @author Christopher Brooks
 * @version $Id: TestStringNode.java,v 1.7 2006/10/12 20:38:47 cxh Exp $
 */
public class TestStringNode extends TreeNode {

    /**
     * Construct a TestStringNode with an unspecified number of children to be
     * added to the child list later.
     */
    public TestStringNode() {

    }

    /**
     * Construct a TestStringNode with one string arguments.
     */
    public TestStringNode(String string) {
        _literal = string;
    }

    /**
     * Return the class ID number, which is unique for each sub-type. The ID
     * number is intended to be used in switch statements.
     *
     * @return A unique class ID number.
     */
    public int classID() {
        return 668;
    }

    public final String getString() {
        return _literal;
    }

    public final void setString(String string) {
        _literal = string;
    }

    public static final int CHILD_INDEX_ARGUMENT1 = 0;

    protected String _literal;
}
