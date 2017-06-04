/* A base class for all visitors that perform replacement of child nodes.

 Metropolis: Design Environment for Heterogeneus Systems.

 Copyright (c) 1998-2005 The Regents of the University of California.
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


 METROPOLIS_COPYRIGHT_VERSION_1
 COPYRIGHTENDKEY


 */

package metropolis.metamodel.frontend;

import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.PropertyMap;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.BlackboxNode;
import metropolis.metamodel.nodetypes.BoolLitNode;
import metropolis.metamodel.nodetypes.BoolTypeNode;
import metropolis.metamodel.nodetypes.ByteTypeNode;
import metropolis.metamodel.nodetypes.CharLitNode;
import metropolis.metamodel.nodetypes.CharTypeNode;
import metropolis.metamodel.nodetypes.DoubleLitNode;
import metropolis.metamodel.nodetypes.DoubleTypeNode;
import metropolis.metamodel.nodetypes.EmptyStmtNode;
import metropolis.metamodel.nodetypes.FloatLitNode;
import metropolis.metamodel.nodetypes.FloatTypeNode;
import metropolis.metamodel.nodetypes.IntLitNode;
import metropolis.metamodel.nodetypes.IntTypeNode;
import metropolis.metamodel.nodetypes.LongLitNode;
import metropolis.metamodel.nodetypes.LongTypeNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NullPntrNode;
import metropolis.metamodel.nodetypes.PCTypeNode;
import metropolis.metamodel.nodetypes.ShortTypeNode;
import metropolis.metamodel.nodetypes.StringLitNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.VoidTypeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ReplacementVisitor
/**
 * A base class for all visitors that perform replacement of child nodes.
 * Replacement works in the following way:
 * <ul>
 * <li> The children of a node must be visited before the current node is
 * visited
 * <li> The return value of each visit method is the subtree that should replace
 * the node being visited. That is, each method that should not be modified at
 * all during the visit should return itself after the visit (<code> return this </code>).
 * <li> The parent rebuilds the children list from the return values of the
 * visit method of the children.
 * </ul>
 * This base visitor provides methods for "leaves" in the abstract syntax tree,
 * that is, nodes that will never change. The default visit method rebuilds the
 * children list from the return values of the visitors.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ReplacementVisitor.java,v 1.16 2006/10/12 20:34:00 cxh Exp $
 */
public abstract class ReplacementVisitor extends MetaModelVisitor {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new replacement visitor. Use TM_CUSTOM as the default traversal
     * method.
     */
    public ReplacementVisitor() {
        super(TM_CUSTOM);
    }

    /**
     * Create a new replacement visitor. Check that the traversal method is set
     * to TM_CUSTOM or TM_CHILDREN_FIRST, because children must be visited
     * before the parent to perform the traversal.
     *
     * @param traversalMethod
     *            Traversal method used by this visitor.
     * @exception RuntimeException
     *                if the traversal method used is not correct.
     */
    public ReplacementVisitor(int traversalMethod) {
        super(traversalMethod);
        if ((_traversalMethod != TM_CUSTOM)
                && (_traversalMethod != TM_CHILDREN_FIRST)) {
            throw new RuntimeException("traversal method for replacement "
                    + "must be custom or children first.");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    // Visit the "leaves" of the abstract syntax tree; these nodes will
    // be probably unchanged by the replacement.

    public Object visitNameNode(NameNode node, LinkedList args) {
        return node;
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return node;
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return node;
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return node;
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return node;
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return node;
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return node;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return node;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return node;
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitPCTypeNode(PCTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return node;
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return node;
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return node;
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return node;
    }

    public Object visitBlackboxNode(BlackboxNode node, LinkedList args) {
        return node;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. Replace all children by their return values. The
     * visits to the children will be performed used the argument list received
     * in this node.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return This node (i.e. this node will not be replaced).
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        if (_traversalMethod == TM_CUSTOM) {
            List children = node.children();
            ArrayList list = TNLManip.traverseList(this, args, children);
            node.setChildren(list);
        } else {
            // Traversal method = TM_CHILDREN_FIRST
            Integer index = PropertyMap.CHILD_RETURN_VALUES_KEY;
            ArrayList newChildren = (ArrayList) node.getProperty(index);
            node.setChildren(newChildren);
        }
        return node;
    }

}
