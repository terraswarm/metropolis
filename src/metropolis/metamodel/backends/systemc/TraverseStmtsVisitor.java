/*

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

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.nodetypes.ActionLabelStmtNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.AwaitStatementNode;
import metropolis.metamodel.nodetypes.BlockNode;
import metropolis.metamodel.nodetypes.BoundedLoopNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ForNode;
import metropolis.metamodel.nodetypes.IfStmtNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.LabeledBlockNode;
import metropolis.metamodel.nodetypes.LabeledStmtNode;
import metropolis.metamodel.nodetypes.LoopNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SwitchBranchNode;
import metropolis.metamodel.nodetypes.SwitchNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // TraverseStmtsVisitor
/**
 * Traverse all statements in an AST.
 *
 * @author Guang Yang
 * @version $Id: TraverseStmtsVisitor.java,v 1.13 2006/10/12 20:33:15 cxh Exp $
 */
public class TraverseStmtsVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    public TraverseStmtsVisitor() {
        super(TM_CUSTOM);
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        return TNLManip.traverseList(this, args, node.getDefTypes());
    }

    public Object visitBlockNode(BlockNode node, LinkedList args) {
        return TNLManip.traverseList(this, args, node.getStmts());
    }

    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(node.getThenPart().accept(this, args));
        retList.addLast(node.getElsePart().accept(this, args));
        return retList;
    }

    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        return TNLManip.traverseList(this, args, node.getSwitchBlocks());
    }

    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        return TNLManip.traverseList(this, args, node.getStmts());
    }

    public Object visitLoopNode(LoopNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(node.getForeStmt().accept(this, args));
        retList.addLast(node.getAftStmt().accept(this, args));
        return retList;
    }

    public Object visitForNode(ForNode node, LinkedList args) {
        return node.getStmt().accept(this, args);
    }

    public Object visitBoundedLoopNode(BoundedLoopNode node, LinkedList args) {
        return node.getStmt().accept(this, args);
    }

    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        return TNLManip.traverseList(this, args, node.getGuards());
    }

    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {
        return node.getStmt().accept(this, args);
    }

    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return node.getStmt().accept(this, args);
    }

    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        return TNLManip.traverseList(this, args, node.getStmts());
    }

    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        return TNLManip.traverseList(this, args, node.getStmts());
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        return node.getBody().accept(this, args);
    }

    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a user type declaration node.
     *
     * @param node
     *            The user type declaration node.
     * @param args
     *            The arguments that are pased to TNLManip.traverseList().
     * @return The results of calling TNLManip.traverseList().
     */
    public Object visitUserTypeDeclNode(UserTypeDeclNode node, LinkedList args) {
        return TNLManip.traverseList(this, args, node.getMembers());
    }
}
