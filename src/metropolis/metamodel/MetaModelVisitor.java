/*
This file was generated automatically by GenerateVisitor.

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

*/

package metropolis.metamodel;

import java.util.LinkedList;

import metropolis.metamodel.IVisitor;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.*;

/** A visitor for AST's for meta-model code.
<p>MetaModelVisitor.java is generated from NodeTypes.def by GenerateVisitor.
<p>See {@link GenerateVisitor} for a description of the syntax.
@author JeffTsay
@version $Id: NodeTypes.def,v 1.34 2006/10/12 13:58:19 cxh Exp $
*/

public class MetaModelVisitor implements IVisitor {
    /** Construct a MetaModelVisitor. */
    public MetaModelVisitor() {
        this(TM_CHILDREN_FIRST);
    }

    /** Construct a MetaModelVisitor.
     *  @param traversalMethod The method used to traverse the
     *  children, usually one of
     *  {@link metropolis.metamodel.IVisitor#TM_CHILDREN_FIRST},
     *  {@link metropolis.metamodel.IVisitor#TM_SELF_FIRST}
     *  or {@link metropolis.metamodel.IVisitor#TM_CUSTOM}.
     */
    public MetaModelVisitor(int traversalMethod) {
        if (traversalMethod > TM_CUSTOM) {
           throw new RuntimeException("Illegal traversal method");
        }
        _traversalMethod = traversalMethod;
    }

    /** Specify the order in visiting the nodes. */
    public final int traversalMethod() { return _traversalMethod; }

    /** Visit a AbsentTreeNode node.
     *  @param node The AbsentTreeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAbsentTreeNode(AbsentTreeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BoolTypeNode node.
     *  @param node The BoolTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBoolTypeNode(BoolTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CharTypeNode node.
     *  @param node The CharTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCharTypeNode(CharTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ByteTypeNode node.
     *  @param node The ByteTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitByteTypeNode(ByteTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ShortTypeNode node.
     *  @param node The ShortTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitShortTypeNode(ShortTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a IntTypeNode node.
     *  @param node The IntTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitIntTypeNode(IntTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a FloatTypeNode node.
     *  @param node The FloatTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitFloatTypeNode(FloatTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LongTypeNode node.
     *  @param node The LongTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLongTypeNode(LongTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a DoubleTypeNode node.
     *  @param node The DoubleTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitDoubleTypeNode(DoubleTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EventTypeNode node.
     *  @param node The EventTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEventTypeNode(EventTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a VoidTypeNode node.
     *  @param node The VoidTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitVoidTypeNode(VoidTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a TypeNameNode node.
     *  @param node The TypeNameNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitTypeNameNode(TypeNameNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ArrayTypeNode node.
     *  @param node The ArrayTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitArrayTypeNode(ArrayTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CompileUnitNode node.
     *  @param node The CompileUnitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCompileUnitNode(CompileUnitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ImportNode node.
     *  @param node The ImportNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitImportNode(ImportNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ImportOnDemandNode node.
     *  @param node The ImportOnDemandNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitImportOnDemandNode(ImportOnDemandNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a FieldDeclNode node.
     *  @param node The FieldDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitFieldDeclNode(FieldDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LocalVarDeclNode node.
     *  @param node The LocalVarDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLocalVarDeclNode(LocalVarDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ConstructorDeclNode node.
     *  @param node The ConstructorDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ThisConstructorCallNode node.
     *  @param node The ThisConstructorCallNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitThisConstructorCallNode(ThisConstructorCallNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SuperConstructorCallNode node.
     *  @param node The SuperConstructorCallNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ParameterNode node.
     *  @param node The ParameterNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitParameterNode(ParameterNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BlockNode node.
     *  @param node The BlockNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBlockNode(BlockNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EmptyStmtNode node.
     *  @param node The EmptyStmtNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEmptyStmtNode(EmptyStmtNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LabeledStmtNode node.
     *  @param node The LabeledStmtNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a IfStmtNode node.
     *  @param node The IfStmtNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitIfStmtNode(IfStmtNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SwitchNode node.
     *  @param node The SwitchNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSwitchNode(SwitchNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CaseNode node.
     *  @param node The CaseNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCaseNode(CaseNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SwitchBranchNode node.
     *  @param node The SwitchBranchNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSwitchBranchNode(SwitchBranchNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LoopNode node.
     *  @param node The LoopNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLoopNode(LoopNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ExprStmtNode node.
     *  @param node The ExprStmtNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitExprStmtNode(ExprStmtNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a UserTypeDeclStmtNode node.
     *  @param node The UserTypeDeclStmtNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitUserTypeDeclStmtNode(UserTypeDeclStmtNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ForNode node.
     *  @param node The ForNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitForNode(ForNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BreakNode node.
     *  @param node The BreakNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBreakNode(BreakNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ContinueNode node.
     *  @param node The ContinueNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitContinueNode(ContinueNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ReturnNode node.
     *  @param node The ReturnNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitReturnNode(ReturnNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a IntLitNode node.
     *  @param node The IntLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitIntLitNode(IntLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LongLitNode node.
     *  @param node The LongLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLongLitNode(LongLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a FloatLitNode node.
     *  @param node The FloatLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitFloatLitNode(FloatLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a DoubleLitNode node.
     *  @param node The DoubleLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitDoubleLitNode(DoubleLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BoolLitNode node.
     *  @param node The BoolLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBoolLitNode(BoolLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CharLitNode node.
     *  @param node The CharLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCharLitNode(CharLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a StringLitNode node.
     *  @param node The StringLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitStringLitNode(StringLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NullPntrNode node.
     *  @param node The NullPntrNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNullPntrNode(NullPntrNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ThisNode node.
     *  @param node The ThisNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitThisNode(ThisNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ArrayInitNode node.
     *  @param node The ArrayInitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitArrayInitNode(ArrayInitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ArrayAccessNode node.
     *  @param node The ArrayAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitArrayAccessNode(ArrayAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ObjectNode node.
     *  @param node The ObjectNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitObjectNode(ObjectNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ObjectFieldAccessNode node.
     *  @param node The ObjectFieldAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a TypeFieldAccessNode node.
     *  @param node The TypeFieldAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SuperFieldAccessNode node.
     *  @param node The SuperFieldAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ThisFieldAccessNode node.
     *  @param node The ThisFieldAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a TypeClassAccessNode node.
     *  @param node The TypeClassAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitTypeClassAccessNode(TypeClassAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a OuterThisAccessNode node.
     *  @param node The OuterThisAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitOuterThisAccessNode(OuterThisAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a OuterSuperAccessNode node.
     *  @param node The OuterSuperAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MethodCallNode node.
     *  @param node The MethodCallNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMethodCallNode(MethodCallNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AllocateNode node.
     *  @param node The AllocateNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAllocateNode(AllocateNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AllocateArrayNode node.
     *  @param node The AllocateArrayNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAllocateArrayNode(AllocateArrayNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AllocateAnonymousClassNode node.
     *  @param node The AllocateAnonymousClassNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PostIncrNode node.
     *  @param node The PostIncrNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPostIncrNode(PostIncrNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PostDecrNode node.
     *  @param node The PostDecrNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPostDecrNode(PostDecrNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a UnaryPlusNode node.
     *  @param node The UnaryPlusNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitUnaryPlusNode(UnaryPlusNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a UnaryMinusNode node.
     *  @param node The UnaryMinusNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitUnaryMinusNode(UnaryMinusNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PreIncrNode node.
     *  @param node The PreIncrNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPreIncrNode(PreIncrNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PreDecrNode node.
     *  @param node The PreDecrNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPreDecrNode(PreDecrNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ComplementNode node.
     *  @param node The ComplementNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitComplementNode(ComplementNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NotNode node.
     *  @param node The NotNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNotNode(NotNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CastNode node.
     *  @param node The CastNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCastNode(CastNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MultNode node.
     *  @param node The MultNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMultNode(MultNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a DivNode node.
     *  @param node The DivNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitDivNode(DivNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RemNode node.
     *  @param node The RemNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRemNode(RemNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PlusNode node.
     *  @param node The PlusNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPlusNode(PlusNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MinusNode node.
     *  @param node The MinusNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMinusNode(MinusNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LeftShiftLogNode node.
     *  @param node The LeftShiftLogNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLeftShiftLogNode(LeftShiftLogNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RightShiftLogNode node.
     *  @param node The RightShiftLogNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRightShiftLogNode(RightShiftLogNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RightShiftArithNode node.
     *  @param node The RightShiftArithNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRightShiftArithNode(RightShiftArithNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LTNode node.
     *  @param node The LTNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLTNode(LTNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GTNode node.
     *  @param node The GTNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGTNode(GTNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LENode node.
     *  @param node The LENode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLENode(LENode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GENode node.
     *  @param node The GENode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGENode(GENode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a InstanceOfNode node.
     *  @param node The InstanceOfNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitInstanceOfNode(InstanceOfNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EQNode node.
     *  @param node The EQNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEQNode(EQNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NENode node.
     *  @param node The NENode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNENode(NENode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BitAndNode node.
     *  @param node The BitAndNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBitAndNode(BitAndNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BitOrNode node.
     *  @param node The BitOrNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBitOrNode(BitOrNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BitXorNode node.
     *  @param node The BitXorNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBitXorNode(BitXorNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CandNode node.
     *  @param node The CandNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCandNode(CandNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CorNode node.
     *  @param node The CorNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCorNode(CorNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a IfExprNode node.
     *  @param node The IfExprNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitIfExprNode(IfExprNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AssignNode node.
     *  @param node The AssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAssignNode(AssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MultAssignNode node.
     *  @param node The MultAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMultAssignNode(MultAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a DivAssignNode node.
     *  @param node The DivAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitDivAssignNode(DivAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RemAssignNode node.
     *  @param node The RemAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRemAssignNode(RemAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PlusAssignNode node.
     *  @param node The PlusAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPlusAssignNode(PlusAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MinusAssignNode node.
     *  @param node The MinusAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMinusAssignNode(MinusAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LeftShiftLogAssignNode node.
     *  @param node The LeftShiftLogAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RightShiftLogAssignNode node.
     *  @param node The RightShiftLogAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RightShiftArithAssignNode node.
     *  @param node The RightShiftArithAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRightShiftArithAssignNode(RightShiftArithAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BitAndAssignNode node.
     *  @param node The BitAndAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBitAndAssignNode(BitAndAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BitXorAssignNode node.
     *  @param node The BitXorAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBitXorAssignNode(BitXorAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BitOrAssignNode node.
     *  @param node The BitOrAssignNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBitOrAssignNode(BitOrAssignNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NameNode node.
     *  @param node The NameNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNameNode(NameNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a TemplateParametersNode node.
     *  @param node The TemplateParametersNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitTemplateParametersNode(TemplateParametersNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CifNode node.
     *  @param node The CifNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCifNode(CifNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a CiffNode node.
     *  @param node The CiffNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitCiffNode(CiffNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PCTypeNode node.
     *  @param node The PCTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPCTypeNode(PCTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LocalLabelNode node.
     *  @param node The LocalLabelNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLocalLabelNode(LocalLabelNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GlobalLabelNode node.
     *  @param node The GlobalLabelNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGlobalLabelNode(GlobalLabelNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LabeledBlockNode node.
     *  @param node The LabeledBlockNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ConstraintBlockNode node.
     *  @param node The ConstraintBlockNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitConstraintBlockNode(ConstraintBlockNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BoundedLoopNode node.
     *  @param node The BoundedLoopNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBoundedLoopNode(BoundedLoopNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NonDeterminismNode node.
     *  @param node The NonDeterminismNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNonDeterminismNode(NonDeterminismNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ExecIndexNode node.
     *  @param node The ExecIndexNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitExecIndexNode(ExecIndexNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ExprLTLNode node.
     *  @param node The ExprLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitExprLTLNode(ExprLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a FutureLTLNode node.
     *  @param node The FutureLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitFutureLTLNode(FutureLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GloballyLTLNode node.
     *  @param node The GloballyLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGloballyLTLNode(GloballyLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NextLTLNode node.
     *  @param node The NextLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNextLTLNode(NextLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a UntilLTLNode node.
     *  @param node The UntilLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitUntilLTLNode(UntilLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ForallActionNode node.
     *  @param node The ForallActionNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitForallActionNode(ForallActionNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ExistsActionNode node.
     *  @param node The ExistsActionNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitExistsActionNode(ExistsActionNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ExprActionNode node.
     *  @param node The ExprActionNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitExprActionNode(ExprActionNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BeginPCNode node.
     *  @param node The BeginPCNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBeginPCNode(BeginPCNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EndPCNode node.
     *  @param node The EndPCNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEndPCNode(EndPCNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PCNode node.
     *  @param node The PCNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPCNode(PCNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AwaitStatementNode node.
     *  @param node The AwaitStatementNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AwaitGuardNode node.
     *  @param node The AwaitGuardNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAwaitGuardNode(AwaitGuardNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AwaitLockNode node.
     *  @param node The AwaitLockNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAwaitLockNode(AwaitLockNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a InterfaceDeclNode node.
     *  @param node The InterfaceDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ClassDeclNode node.
     *  @param node The ClassDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitClassDeclNode(ClassDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NetlistDeclNode node.
     *  @param node The NetlistDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ProcessDeclNode node.
     *  @param node The ProcessDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitProcessDeclNode(ProcessDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MediumDeclNode node.
     *  @param node The MediumDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMediumDeclNode(MediumDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SchedulerDeclNode node.
     *  @param node The SchedulerDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SMDeclNode node.
     *  @param node The SMDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSMDeclNode(SMDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MethodDeclNode node.
     *  @param node The MethodDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMethodDeclNode(MethodDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RefineNode node.
     *  @param node The RefineNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRefineNode(RefineNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ConnectNode node.
     *  @param node The ConnectNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitConnectNode(ConnectNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AddComponentNode node.
     *  @param node The AddComponentNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAddComponentNode(AddComponentNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SetScopeNode node.
     *  @param node The SetScopeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSetScopeNode(SetScopeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RefineConnectNode node.
     *  @param node The RefineConnectNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRefineConnectNode(RefineConnectNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a RedirectConnectNode node.
     *  @param node The RedirectConnectNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitRedirectConnectNode(RedirectConnectNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetConnectionNumNode node.
     *  @param node The GetConnectionNumNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetConnectionNumNode(GetConnectionNumNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetNthConnectionSrcNode node.
     *  @param node The GetNthConnectionSrcNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetNthConnectionSrcNode(GetNthConnectionSrcNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetNthConnectionPortNode node.
     *  @param node The GetNthConnectionPortNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetNthConnectionPortNode(GetNthConnectionPortNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PortDeclNode node.
     *  @param node The PortDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPortDeclNode(PortDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ParameterDeclNode node.
     *  @param node The ParameterDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitParameterDeclNode(ParameterDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BlackboxNode node.
     *  @param node The BlackboxNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBlackboxNode(BlackboxNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ObjectPortAccessNode node.
     *  @param node The ObjectPortAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitObjectPortAccessNode(ObjectPortAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ThisPortAccessNode node.
     *  @param node The ThisPortAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitThisPortAccessNode(ThisPortAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SuperPortAccessNode node.
     *  @param node The SuperPortAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSuperPortAccessNode(SuperPortAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ObjectParamAccessNode node.
     *  @param node The ObjectParamAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitObjectParamAccessNode(ObjectParamAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ThisParamAccessNode node.
     *  @param node The ThisParamAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitThisParamAccessNode(ThisParamAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SuperParamAccessNode node.
     *  @param node The SuperParamAccessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSuperParamAccessNode(SuperParamAccessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SpecialLitNode node.
     *  @param node The SpecialLitNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSpecialLitNode(SpecialLitNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetComponentNode node.
     *  @param node The GetComponentNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetComponentNode(GetComponentNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetConnectionDestNode node.
     *  @param node The GetConnectionDestNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetConnectionDestNode(GetConnectionDestNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetNthPortNode node.
     *  @param node The GetNthPortNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetNthPortNode(GetNthPortNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetPortNumNode node.
     *  @param node The GetPortNumNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetPortNumNode(GetPortNumNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetScopeNode node.
     *  @param node The GetScopeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetScopeNode(GetScopeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetThreadNode node.
     *  @param node The GetThreadNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetThreadNode(GetThreadNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ActionLabelStmtNode node.
     *  @param node The ActionLabelStmtNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ActionLabelExprNode node.
     *  @param node The ActionLabelExprNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitActionLabelExprNode(ActionLabelExprNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a AnnotationNode node.
     *  @param node The AnnotationNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitAnnotationNode(AnnotationNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BeginAnnotationNode node.
     *  @param node The BeginAnnotationNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBeginAnnotationNode(BeginAnnotationNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EndAnnotationNode node.
     *  @param node The EndAnnotationNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEndAnnotationNode(EndAnnotationNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LTLSynchNode node.
     *  @param node The LTLSynchNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLTLSynchNode(LTLSynchNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EqualVarsNode node.
     *  @param node The EqualVarsNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEqualVarsNode(EqualVarsNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ActionNode node.
     *  @param node The ActionNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitActionNode(ActionNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EventNode node.
     *  @param node The EventNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEventNode(EventNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a BeginEventNode node.
     *  @param node The BeginEventNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitBeginEventNode(BeginEventNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a EndEventNode node.
     *  @param node The EndEventNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitEndEventNode(EndEventNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a NoneEventNode node.
     *  @param node The NoneEventNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitNoneEventNode(NoneEventNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a OtherEventNode node.
     *  @param node The OtherEventNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitOtherEventNode(OtherEventNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ImplyNode node.
     *  @param node The ImplyNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitImplyNode(ImplyNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LTLConstraintNode node.
     *  @param node The LTLConstraintNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLTLConstraintNode(LTLConstraintNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LOCConstraintNode node.
     *  @param node The LOCConstraintNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLOCConstraintNode(LOCConstraintNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ELOCConstraintNode node.
     *  @param node The ELOCConstraintNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitELOCConstraintNode(ELOCConstraintNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ConstraintDeclNode node.
     *  @param node The ConstraintDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitConstraintDeclNode(ConstraintDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ELOCConstraintDeclNode node.
     *  @param node The ELOCConstraintDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitELOCConstraintDeclNode(ELOCConstraintDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LOCConstraintDeclNode node.
     *  @param node The LOCConstraintDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLOCConstraintDeclNode(LOCConstraintDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LTLConstraintDeclNode node.
     *  @param node The LTLConstraintDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLTLConstraintDeclNode(LTLConstraintDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ELOCConstraintCallNode node.
     *  @param node The ELOCConstraintCallNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitELOCConstraintCallNode(ELOCConstraintCallNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LOCConstraintCallNode node.
     *  @param node The LOCConstraintCallNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLOCConstraintCallNode(LOCConstraintCallNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a LTLConstraintCallNode node.
     *  @param node The LTLConstraintCallNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitLTLConstraintCallNode(LTLConstraintCallNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a QuantityDeclNode node.
     *  @param node The QuantityDeclNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a ExclLTLNode node.
     *  @param node The ExclLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitExclLTLNode(ExclLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MutexLTLNode node.
     *  @param node The MutexLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMutexLTLNode(MutexLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a SimulLTLNode node.
     *  @param node The SimulLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitSimulLTLNode(SimulLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PriorityLTLNode node.
     *  @param node The PriorityLTLNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPriorityLTLNode(PriorityLTLNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MinRateNode node.
     *  @param node The MinRateNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMinRateNode(MinRateNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MaxRateNode node.
     *  @param node The MaxRateNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMaxRateNode(MaxRateNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a PeriodNode node.
     *  @param node The PeriodNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitPeriodNode(PeriodNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MinDeltaNode node.
     *  @param node The MinDeltaNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMinDeltaNode(MinDeltaNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a MaxDeltaNode node.
     *  @param node The MaxDeltaNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitMaxDeltaNode(MaxDeltaNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetInstNameNode node.
     *  @param node The GetInstNameNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetInstNameNode(GetInstNameNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetCompNameNode node.
     *  @param node The GetCompNameNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetCompNameNode(GetCompNameNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a IsConnectionRefinedNode node.
     *  @param node The IsConnectionRefinedNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitIsConnectionRefinedNode(IsConnectionRefinedNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetTypeNode node.
     *  @param node The GetTypeNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetTypeNode(GetTypeNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a GetProcessNode node.
     *  @param node The GetProcessNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitGetProcessNode(GetProcessNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** Visit a VarInEventRefNode node.
     *  @param node The VarInEventRefNode to visit.
     *  @param args The arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    public Object visitVarInEventRefNode(VarInEventRefNode node,
            LinkedList args) {
        return _defaultVisit(node, args);
    }

    /** The default visit method.
     *  @param node The TreeNode base type node.
     *  @param args Arguments to pass in.
     *  @return an Object, which is usually a node.
     *  The return value may be null.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }

    /** Value is used to determine how the children of
     *  a node are visited.
     */
    protected final int _traversalMethod;
}
