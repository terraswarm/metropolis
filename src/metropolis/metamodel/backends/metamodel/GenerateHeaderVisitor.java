/* Produce the header of a source file

 Metropolis: Design Environment for Heterogeneus Systems.

 Copyright (c) 2001-2005 The Regents of the University of California.
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

package metropolis.metamodel.backends.metamodel;

import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.BlockNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ConstraintBlockNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.FieldDeclNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.NetworkNodeDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // GenerateHeaderVisitor
/**
 * Produce the header of the code which includes package, import statements,
 * class declarations, methods without local variables and statements, and field
 * declarations without initialization.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Duny Lam
 * @version $Id: GenerateHeaderVisitor.java,v 1.16 2006/10/12 20:32:44 cxh Exp $,
 *          initially created: 07/17/01
 */
public class GenerateHeaderVisitor extends MetaModelVisitor {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////
    /**
     * set the traversal method as custom.
     */
    public GenerateHeaderVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Returns the header of a <code>CompileUnitNode</code>, which is always
     * null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        TNLManip.traverseList(this, args, node.getDefTypes());
        return null;
    }

    /**
     * Returns the header of a <code>ClassDeclNode</code>, which is always
     * null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>InterfaceDeclNode</code>, which is
     * always null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>NetworkNodeDeclNode</code>, which is
     * always null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitNetworkNodeDeclNode(NetworkNodeDeclNode node,
            LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>ProcessDeclNode</code>, which is always
     * null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>MediumDeclNode</code>, which is always
     * null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        TNLManip.traverseList(this, args, node.getMembers());
        return null;
    }

    /**
     * Returns the header of a <code>SchedulerDeclNode</code>, which is
     * always null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null.
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>SMDeclNode</code>, which is always null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>NetlistDeclNode</code>, which is always
     * null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Returns the header of a <code>ConstructorDeclNode</code>, which is
     * always null. Eliminate all the statements inside the constructor.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method, ignored in this
     *            method.
     * @return null
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        node.getBody().setStmts((List) (new LinkedList()));
        return null;
    }

    /**
     * Returns the header of a <code>MethodDeclNode</code>, which is always
     * null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method, ignored in this
     *            method.
     * @return null
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        TreeNode methodBlock = node.getBody();
        if (methodBlock != AbsentTreeNode.instance) {
            ((BlockNode) methodBlock).setStmts((List) (new LinkedList()));
        }
        return null;
    }

    /**
     * Returns the header of a <code>FieldDeclNode</code>, which is always
     * null. Set the initial expression of the FieldDeclNode to AbsentTreeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method, ignored in this
     *            method.
     * @return null
     */
    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        node.setInitExpr(AbsentTreeNode.instance);
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Returns the header of a <code>UserTypeDeclNode</code>, which is always
     * null. Remove instances of ConstraintBlockNode from the UserTypeDeclNode
     * members.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
            LinkedList args) {
        List members = node.getMembers();

        // eliminate constraint block
        Iterator membersItr = members.iterator();
        while (membersItr.hasNext()) {
            TreeNode testNode = (TreeNode) membersItr.next();
            if (testNode instanceof ConstraintBlockNode)
                membersItr.remove();
        }
        TNLManip.traverseList(this, args, node.getMembers());

        return null;
    }

    /**
     * Default visit method that returns null.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }
}
