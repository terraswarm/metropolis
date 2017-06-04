/* A base class for all visitors that perform resolution of names.

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
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.BlackboxNode;
import metropolis.metamodel.nodetypes.BoolLitNode;
import metropolis.metamodel.nodetypes.BoolTypeNode;
import metropolis.metamodel.nodetypes.BreakNode;
import metropolis.metamodel.nodetypes.ByteTypeNode;
import metropolis.metamodel.nodetypes.CharLitNode;
import metropolis.metamodel.nodetypes.CharTypeNode;
import metropolis.metamodel.nodetypes.ContinueNode;
import metropolis.metamodel.nodetypes.DoubleLitNode;
import metropolis.metamodel.nodetypes.DoubleTypeNode;
import metropolis.metamodel.nodetypes.EmptyStmtNode;
import metropolis.metamodel.nodetypes.FloatLitNode;
import metropolis.metamodel.nodetypes.FloatTypeNode;
import metropolis.metamodel.nodetypes.ImportNode;
import metropolis.metamodel.nodetypes.ImportOnDemandNode;
import metropolis.metamodel.nodetypes.IntLitNode;
import metropolis.metamodel.nodetypes.IntTypeNode;
import metropolis.metamodel.nodetypes.LongLitNode;
import metropolis.metamodel.nodetypes.LongTypeNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NullPntrNode;
import metropolis.metamodel.nodetypes.ObjectNode;
import metropolis.metamodel.nodetypes.PCTypeNode;
import metropolis.metamodel.nodetypes.ShortTypeNode;
import metropolis.metamodel.nodetypes.StringLitNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.VoidTypeNode;

import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ResolutionVisitor
/**
 * This is a base class for all visitors that perform resolution of some kind.
 * Those visitors will traverse most of the nodes of the AST, performing some
 * tasks only in those nodes that have children that are NameNodes.
 * <p>
 * The behavior of this class is the following. The node that are leaves do
 * nothing (return null), and those nodes that only have NameNodes as their
 * children do nothing as well. Each visitor will redefine those nodes with
 * children that are NameNodes to do a particular resolution.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ResolutionVisitor.java,v 1.12 2006/10/12 20:34:04 cxh Exp $
 */
public class ResolutionVisitor extends MetaModelVisitor {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new resolution visitor. Use TM_CUSTOM as the default traversal
     * method.
     */
    public ResolutionVisitor() {
        this(TM_CUSTOM);
    }

    /**
     * Create a new resolution visitor with a given traversal method.
     *
     * @param traversalMethod
     *            Traversal method used by this visitor.
     */
    public ResolutionVisitor(int traversalMethod) {
        super(traversalMethod);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    // Visit the "leaves" of the abstract syntax tree; these nodes will
    // not require any resolution

    public Object visitNameNode(NameNode node, LinkedList args) {
        return null;
    }

    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return null;
    }

    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return null;
    }

    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return null;
    }

    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return null;
    }

    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return null;
    }

    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return null;
    }

    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return null;
    }

    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return null;
    }

    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitPCTypeNode(PCTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return null;
    }

    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return null;
    }

    public Object visitImportNode(ImportNode node, LinkedList args) {
        return null;
    }

    public Object visitImportOnDemandNode(ImportOnDemandNode node,
            LinkedList args) {
        return null;
    }

    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        return null;
    }

    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return null;
    }

    public Object visitThisNode(ThisNode node, LinkedList args) {
        return null;
    }

    public Object visitBlackboxNode(BlackboxNode node, LinkedList args) {
        return null;
    }

    public Object visitBreakNode(BreakNode node, LinkedList args) {
        return null;
    }

    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        return null;
    }

    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * The default visit method. Traverse children with the same argument list.
     *
     * @param node
     *            Node being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (unchanged).
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        if (_traversalMethod == TM_CUSTOM) {
            TNLManip.traverseList(this, args, node.children());
        }
        return null;
    }

}
