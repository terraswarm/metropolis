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

// ////////////////////////////////////////////////////////////////////////
// // TestSingletonNode
/**
 * A leaf node that contains a string.
 *
 * @author Christopher Brooks
 * @version $Id: TestSingletonNode.java,v 1.9 2006/10/12 20:38:45 cxh Exp $
 */
public class TestSingletonNode extends TestStringNode {

    /**
     * Construct a TestSingletonNode with one string arguments.
     */
    public TestSingletonNode(String string) {
        super(string);
    }

    /**
     * Return the class ID number, which is unique for each sub-type. The ID
     * number is intended to be used in switch statements.
     *
     * @return A unique class ID number.
     */
    public int classID() {
        return 669;
    }

    /**
     * Return true if the class of this object is a singleton, i.e. there exists
     * only one object of the subclass. This method always returns true for this
     * singleton class.
     *
     * @return True, by default.
     */
    public boolean isSingleton() {
        return true;
    }
}
