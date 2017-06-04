/* Test suite class to test IVisitor interface

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

import metropolis.metamodel.IVisitor;
import metropolis.metamodel.TreeNode;

import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // TestVisitor
/**
 * A test visitor
 *
 * @author Christopher Brooks
 * @version $Id: TestVisitor.java,v 1.8 2006/10/12 20:38:50 cxh Exp $
 */
public class TestVisitor implements IVisitor {

    public TestVisitor() {
        this(TM_CHILDREN_FIRST);
    }

    public TestVisitor(int traversalMethod) {
        if (traversalMethod > TM_CUSTOM) {
            throw new RuntimeException("Illegal traversal method");
        }
        _traversalMethod = traversalMethod;
    }

    /** Specify the order in visiting the nodes. */
    public final int traversalMethod() {
        return _traversalMethod;
    }

    public Object visitTestSingletonNode(TestSingletonNode node, LinkedList args) {
        return _defaultVisit("visitTestSingletonNode", node, args);
    }

    public Object visitTestStringNode(TestStringNode node, LinkedList args) {
        return _defaultVisit("visitTestStringNode", node, args);
    }

    public Object visitTestTreeNode(TestTreeNode node, LinkedList args) {
        return _defaultVisit("visitTestTreeNode", node, args);
    }

    public Object visitTestTwoListNode(TestTwoListNode node, LinkedList args) {
        return _defaultVisit("visitTestTwoListNode", node, args);
    }

    /** The default visit method. */
    protected Object _defaultVisit(String name, TreeNode node, LinkedList args) {
        String returnValue;
        if (args.size() > 0) {
            returnValue = "{" + name + " " + args.get(0) + "}";
        } else {
            returnValue = "{" + name + "}";
        }
        System.out.println(returnValue);
        return returnValue;
    }

    protected final int _traversalMethod;
}
