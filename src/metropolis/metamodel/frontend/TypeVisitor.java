/* A visitor that computes the types of expressions and performs
 type-checking.

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

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.TypeUtility;
import metropolis.metamodel.nodetypes.*;

import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // TypeVisitor
/**
 * A visitor that traverses instructions computing the type of the expressions.
 * TypePolicy and TypeIdentifier are used in the computation of the type. The
 * traversal method used in the visitor is TM_CUSTOM;
 * <p>
 * This visitor returns a TypeNode with the type of the expression represented
 * by the current node. The argument list in the visit() methods is not used for
 * most of the nodes.
 * <p>
 * As the type is being computed, is expression is annotated with its type. This
 * annotation is stored in the TYPE_KEY property of the expression.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso & Duny Lam
 * @version $Id: TypeVisitor.java,v 1.37 2006/10/12 20:34:28 cxh Exp $
 */
public class TypeVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Construct a new TypeVisitor with a default {@link TypePolicy}.
     */
    public TypeVisitor() {
        this(new TypePolicy());
    }

    /**
     * Construct a new TypeVisitor with a custom {@link TypePolicy}.
     *
     * @param typePolicy
     *            The custom type policy to use.
     */
    public TypeVisitor(TypePolicy typePolicy) {
        super(TM_CUSTOM);
        _typeID = typePolicy.typeIdentifier();
        _typePolicy = typePolicy;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////
    /**
     * Return the TypeIdentifier that's used to identify types in this visitor.
     *
     * @return the TypeIdentifier
     */
    public TypeIdentifier typeIdentifier() {
        return _typeID;
    }

    /**
     * Return the TypePolicy that's used in figuring out the type of an
     * expression in this visitor.
     *
     * @return the TypePolicy
     */
    public TypePolicy typePolicy() {
        return _typePolicy;
    }

    /**
     * Sets the type of the node to type given to nondeterminism expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the type
     */
    public Object visitNonDeterminismNode(NonDeterminismNode node,
            LinkedList args) {
        return _setType(node, node.getType());
    }

    /**
     * Sets the type of the node to integer type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of IntTypeNode
     */
    public Object visitExecIndexNode(ExecIndexNode node, LinkedList args) {
        return _setType(node, IntTypeNode.instance);
    }

    /**
     * Sets the type of the node to type of its expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode that corresponds to the type of its expression
     */
    public Object visitExprLTLNode(ExprLTLNode node, LinkedList args) {
        return _setType(node, type(node.getExpr()));
    }

    /**
     * Sets the type of the node to boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitExclLTLNode(ExclLTLNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode.
     */
    public Object visitMutexLTLNode(MutexLTLNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode.
     */
    public Object visitSimulLTLNode(SimulLTLNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if the
     * formula is not of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitFutureLTLNode(FutureLTLNode node, LinkedList args) {
        if (type(node.getSubform()) != BoolTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid subform in F(). "
                    + "It must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if the
     * formula is not of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitGloballyLTLNode(GloballyLTLNode node, LinkedList args) {
        if (type(node.getSubform()) != BoolTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid subform in G(). "
                    + "It must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if the
     * formula is not of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitNextLTLNode(NextLTLNode node, LinkedList args) {
        if (type(node.getSubform()) != BoolTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid subform in N(). "
                    + "It must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if either
     * one of the formulas is not of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitUntilLTLNode(UntilLTLNode node, LinkedList args) {
        if (type(node.getSubform1()) != BoolTypeNode.instance
                || type(node.getSubform2()) != BoolTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid subforms in U(). "
                    + "Both subforms must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type pcval.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of PCTypeNode
     */
    public Object visitBeginPCNode(BeginPCNode node, LinkedList args) {
        return _setType(node, PCTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type pcval.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of PCTypeNode
     */
    public Object visitEndPCNode(EndPCNode node, LinkedList args) {
        return _setType(node, PCTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type pcval.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of PCTypeNode
     */
    public Object visitPCNode(PCNode node, LinkedList args) {
        return _setType(node, PCTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if its
     * subexpressions are not of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitCifNode(CifNode node, LinkedList args) {
        if (type(node.getExpr1()) != BoolTypeNode.instance
                || type(node.getExpr2()) != BoolTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid expressions for -> "
                    + "operands. Both operands must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if its
     * subexpressions are not of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitCiffNode(CiffNode node, LinkedList args) {
        if (type(node.getExpr1()) != BoolTypeNode.instance
                || type(node.getExpr2()) != BoolTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid expressions for <-> "
                    + "operands. Both operands must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if its
     * formula is not of of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitExistsActionNode(ExistsActionNode node, LinkedList args) {
        if (type(node.getSubform()) != BoolTypeNode.instance)
            throw new RuntimeException(
                    "ERROR: Invalid subform in exists "
                            + "expression. The formula subform must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type of its expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the type of its expression
     */
    public Object visitExprActionNode(ExprActionNode node, LinkedList args) {
        return _setType(node, type(node.getExpr()));
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if its
     * formula is not of of boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitForallActionNode(ForallActionNode node, LinkedList args) {
        if (type(node.getSubform()) != BoolTypeNode.instance)
            throw new RuntimeException(
                    "ERROR: Invalid subform in forall "
                            + "expression. The formula subform must be of boolean type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitMaxRateNode(MaxRateNode node, LinkedList args) {
        if (type(node.getValue()) != DoubleTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid type for max rate in "
                    + "the maxrate expression. It must be of double type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitPeriodNode(PeriodNode node, LinkedList args) {
        if (type(node.getValue()) != DoubleTypeNode.instance)
            throw new RuntimeException("ERROR: Invalid type for period in "
                    + "the period expression. It must be of double type.");

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to boolean type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    /*
     * public Object visitMaxLateNode(MaxLateNode node, LinkedList args) { if
     * (type(node.getOccur()) != IntTypeNode.instance) throw new
     * RuntimeException ("ERROR: Invalid type for occur in " + "the maxlate
     * expression. It must be of integer type.");
     *
     * if (type(node.getMaxLatency()) != DoubleTypeNode.instance) throw new
     * RuntimeException("ERROR: Invalid type for max latency "+ "in the maxlate
     * expression. It must be of integer type.");
     *
     * return _setType(node, BoolTypeNode.instance); }
     */

    /**
     * Sets the type of the node to integer type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of IntTypeNode
     */
    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return _setType(node, IntTypeNode.instance);
    }

    /**
     * Sets the type of the node to LongTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of LongTypeNode
     */
    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return _setType(node, LongTypeNode.instance);
    }

    /**
     * Sets the type of the node to FloatTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of FloatTypeNode
     */
    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return _setType(node, FloatTypeNode.instance);
    }

    /**
     * Sets the type of the node to DoubleTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of DoubleTypeNode
     */
    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return _setType(node, DoubleTypeNode.instance);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to CharTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of CharTypeNode
     */
    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return _setType(node, CharTypeNode.instance);
    }

    /**
     * Sets the type of the node to STRING_TYPE that has been initialized when
     * special classes were loaded. See MetaModelLibrary.java.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNameNode corresponds to STRING_TYPE
     */
    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return _setType(node, MetaModelLibrary.STRING_TYPE);
    }

    /**
     * Sets the type of the node to ArrayInitTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of ArrayInitTypeNode
     */
    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        return _setType(node, ArrayInitTypeNode.instance);
    }

    /**
     * Sets the type of the node to NullTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of NullTypeNode
     */
    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return _setType(node, NullTypeNode.instance);
    }

    /**
     * Sets the type of the node to the corresponding TypeNameNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of this class
     */
    public Object visitThisNode(ThisNode node, LinkedList args) {
        TypeNameNode type = (TypeNameNode) node
                .getDefinedProperty(THIS_CLASS_KEY);
        return _setType(node, type);
    }

    /**
     * Sets the type of the node to TypeNode of the array base type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        ArrayTypeNode arrType = (ArrayTypeNode) type(node.getArray());
        return _setType(node, arrType.getBaseType());
    }

    /**
     * Sets the type of the node to the corresponding object type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to this type
     */
    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        TypedDecl decl = (TypedDecl) MetaModelDecl.getDecl(node.getName());
        return _setType(node, decl.getType());
    }

    /**
     * Sets the type of the node to the type of the parameter.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the parameter
     */
    public Object visitObjectParamAccessNode(ObjectParamAccessNode node,
            LinkedList args) {
        return _visitParamAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the parameter.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the parameter
     */
    public Object visitThisParamAccessNode(ThisParamAccessNode node,
            LinkedList args) {
        return _visitParamAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the parameter.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the parameter
     */
    public Object visitSuperParamAccessNode(SuperParamAccessNode node,
            LinkedList args) {
        return _visitParamAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the port.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the port
     */
    public Object visitObjectPortAccessNode(ObjectPortAccessNode node,
            LinkedList args) {
        return _visitPortAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the port.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the port
     */
    public Object visitThisPortAccessNode(ThisPortAccessNode node,
            LinkedList args) {
        return _visitPortAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the port.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the port
     */
    public Object visitSuperPortAccessNode(SuperPortAccessNode node,
            LinkedList args) {
        return _visitPortAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the field.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the field
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the field.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the field
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the field.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the field
     */
    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    /**
     * Sets the type of the node to the type of the field.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the field
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        return _visitFieldAccessNode(node);
    }

    /**
     * Sets the type of the node to OBJECT_TYPE which was initialized when the
     * special classes were loaded. See MetaModelLibrary.java.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNameNode corresponds to OBJECT_TYPE
     */
    public Object visitTypeClassAccessNode(TypeClassAccessNode node,
            LinkedList args) {
        return _setType(node, MetaModelLibrary.OBJECT_TYPE);
    }

    /**
     * Sets the type of the node to the type of the object.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the object
     */
    public Object visitOuterThisAccessNode(OuterThisAccessNode node,
            LinkedList args) {
        return _setType(node, node.getType());
    }

    /**
     * Sets the type of the node to the type of its superclass.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the type of superclass
     */
    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node,
            LinkedList args) {
        ClassDecl thisDecl = (ClassDecl) node.getType().getName()
                .getDefinedProperty(DECL_KEY);
        return _setType(node, thisDecl.getSuperClass().getDefType());
    }

    /**
     * Sets the type of the node to the return type of method.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the return type of method
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        MethodDecl decl = (MethodDecl) MetaModelDecl.getDecl(node.getMethod());
        if (decl == null)
            return TemplateTypeNode.instance;
        else
            return _setType(node, decl.getType());
    }

    /**
     * Sets the type of the node to the Dtype of the allocation.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the type of the allocation
     */
    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    /**
     * Sets the type of the node to the return type of the array allocation.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode corresponds to the type of the allocation
     */
    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        // returned type is an N-D array of the element type, where
        // N = # dimension expressions + # empty dimensions
        return _setType(node, TypeUtility.makeArrayType(node.getDtype(), node
                .getDimExprs().size()
                + node.getDims()));
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitSingleOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitSingleOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitSingleOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitSingleOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitSingleOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitSingleOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitComplementNode(ComplementNode node, LinkedList args) {

        // FIXME: need to check for numeric type?
        return _setType(node, _typePolicy
                .arithPromoteType(type(node.getExpr())));
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitNotNode(NotNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type to be cast.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the cast type
     */
    public Object visitCastNode(CastNode node, LinkedList args) {
        return _setType(node, node.getDtype());
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitMultNode(MultNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitDivNode(DivNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitRemNode(RemNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to STRING_TYPE if one of the expression is of
     * String type. Otherwise, set the type to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type or STRING_TYPE
     */
    public Object visitPlusNode(PlusNode node, LinkedList args) {
        TypeNode type1 = type(node.getExpr1());

        if (_typePolicy.areEqual(type1, MetaModelLibrary.STRING_TYPE)) {
            return _setType(node, MetaModelLibrary.STRING_TYPE);
        }

        TypeNode type2 = type(node.getExpr2());

        if (_typePolicy.areEqual(type2, MetaModelLibrary.STRING_TYPE)) {
            return _setType(node, MetaModelLibrary.STRING_TYPE);
        }

        return _setType(node, _typePolicy.arithPromoteType(type1, type2));
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitMinusNode(MinusNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to the type of the first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitLeftShiftLogNode(LeftShiftLogNode node, LinkedList args) {
        return _visitShiftNode(node);
    }

    /**
     * Sets the type of the node to the type of the first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        return _visitShiftNode(node);
    }

    /**
     * Sets the type of the node to the type of the first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitRightShiftArithNode(RightShiftArithNode node,
            LinkedList args) {
        return _visitShiftNode(node);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitLTNode(LTNode node, LinkedList args) {
        return _visitComparisonNode(node);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitGTNode(GTNode node, LinkedList args) {
        return _visitComparisonNode(node);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitLENode(LENode node, LinkedList args) {
        return _visitComparisonNode(node);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitGENode(GENode node, LinkedList args) {
        return _visitComparisonNode(node);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitEQNode(EQNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to BoolTypeNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of BoolTypeNode
     */
    public Object visitNENode(NENode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitBinaryOpNode(node);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitCandNode(CandNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to the promoted type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitCorNode(CorNode node, LinkedList args) {
        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Sets the type of the node to the promoted type of the arithmetic.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the promoted type
     */
    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        TypeNode thenType = type(node.getExpr2());
        TypeNode elseType = type(node.getExpr3());

        if (_typePolicy.areEqual(thenType, elseType)) {
            return _setType(node, thenType);
        }

        if (_typePolicy.isNumericType(thenType)) {
            if (((thenType == ByteTypeNode.instance) && (elseType == ShortTypeNode.instance))
                    || ((thenType == ShortTypeNode.instance) && (elseType == ByteTypeNode.instance))) {
                return _setType(node, ShortTypeNode.instance);
            }

            ExprNode thenExpr = node.getExpr2();
            ExprNode elseExpr = node.getExpr3();

            // check _validIf() for byte, short, char
            for (int kind = TypeIdentifier.TYPE_KIND_BYTE; kind <= TypeIdentifier.TYPE_KIND_CHAR; kind++) {
                if (_validIf(thenExpr, thenType, elseExpr, elseType, kind)) {
                    return _setType(node, _typeID.primitiveKindToType(kind));
                }
            }

            return _setType(node, _typePolicy.arithPromoteType(thenType,
                    elseType));

        } else if (_typePolicy.isReferenceType(thenType)) {
            if (_typePolicy.canCast(thenType, elseType)) {
                return _setType(node, thenType);
            } else {
                return _setType(node, elseType);
            }
        }

        return _setType(node, _typePolicy.arithPromoteType(thenType, elseType));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitAssignNode(AssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitMultAssignNode(MultAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitDivAssignNode(DivAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitRemAssignNode(RemAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitPlusAssignNode(PlusAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitMinusAssignNode(MinusAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node,
            LinkedList args) {
        return _visitBinaryOpAssignNode(node);
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node,
            LinkedList args) {
        return _visitBinaryOpAssignNode(node);
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitRightShiftArithAssignNode(
            RightShiftArithAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node);
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitBitAndAssignNode(BitAndAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitBitXorAssignNode(BitXorAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Sets the type of the node to the type of first expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the TypeNode of the type of first expression
     */
    public Object visitBitOrAssignNode(BitOrAssignNode node, LinkedList args) {
        return _setType(node, type(node.getExpr1()));
    }

    /**
     * Return the type of an expression node, checking for a memoized type
     * before starting a new visitation. The visitor must not call this method
     * with the same node it handles, or else an infinite recursion will occur.
     *
     * @param node
     *            an ExprNode whose type needs to be determined
     * @return the TypeNode corresponds to the type of the expression
     */
    public TypeNode type(ExprNode node) {
        if (node.hasProperty(TYPE_KEY)) {
            return (TypeNode) node.getDefinedProperty(TYPE_KEY);
        }
        return (TypeNode) node.accept(this, null);
    }

    /**
     * For nodes that represent field accesses (ObjectFieldAccessNode,
     * ThisFieldAccessNode, SuperFieldAccessNode) the type of the object that is
     * accessed (e.g., for a node representing FOO.BAR, the type of FOO). This
     * method figures out the sub-type of NODE and calls the appropriate more
     * specific method.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the TypeNode corresponds to the type
     */
    public TypeNode accessedObjectType(FieldAccessNode node) {
        switch (node.classID()) {
        case TYPEFIELDACCESSNODE_ID:
            return accessedObjectType((TypeFieldAccessNode) node);

        case OBJECTFIELDACCESSNODE_ID:
            return accessedObjectType((ObjectFieldAccessNode) node);

        case THISFIELDACCESSNODE_ID:
            return accessedObjectType((ThisFieldAccessNode) node);

        case SUPERFIELDACCESSNODE_ID:
            return accessedObjectType((SuperFieldAccessNode) node);
        }

        throw new RuntimeException("ERROR: in TypeVisitor, "
                + "accessdObjectType() not supported for node " + node);
    }

    /**
     * Return the type of the object that is accessed.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the TypeNode corresponds to the type
     */
    public TypeNode accessedObjectType(TypeFieldAccessNode node) {
        return MetaModelDecl.getDecl((NamedNode) node.getFType()).getDefType();
    }

    /**
     * Return the type of the object that is accessed.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the TypeNode corresponds to the type
     */
    public TypeNode accessedObjectType(ObjectFieldAccessNode node) {
        return type((ExprNode) node.getObject());
    }

    /**
     * Return the type of the object that is accessed, which is the type of
     * THIS.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the TypeNode corresponds to the type
     */
    public TypeNameNode accessedObjectType(ThisFieldAccessNode node) {
        return (TypeNameNode) node.getDefinedProperty(THIS_CLASS_KEY);
    }

    /**
     * Return the type of the object that is accessed, which is the type of the
     * superclass of THIS.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the TypeNode corresponds to the type
     */
    public TypeNode accessedObjectType(SuperFieldAccessNode node) {
        ObjectDecl myClass = (ObjectDecl) MetaModelDecl
                .getDecl((NamedNode) node.getDefinedProperty(THIS_CLASS_KEY));
        // ObjectDecl sclass = myClass.getSuperClass();

        // if (myClass!=null) return myClass.getDefType();
        // else return node.getDefType(); //Object class
        return myClass.getDefType();
    }

    /**
     * Sets the type of the node to the type event.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of EventTypeNode
     */
    public Object visitBeginEventNode(BeginEventNode node, LinkedList args) {
        return _setType(node, EventTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type event.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of EventTypeNode
     */
    public Object visitEndEventNode(EndEventNode node, LinkedList args) {
        return _setType(node, EventTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type event.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of EventTypeNode
     */
    public Object visitOtherEventNode(OtherEventNode node, LinkedList args) {
        return _setType(node, EventTypeNode.instance);
    }

    /**
     * Sets the type of the node to the type event.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of EventTypeNode
     */
    public Object visitNoneEventNode(NoneEventNode node, LinkedList args) {
        return _setType(node, EventTypeNode.instance);
    }

    /**
     * Sets the type of the node to the interface type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetNthPortNode
     */
    public Object visitGetNthPortNode(GetNthPortNode node, LinkedList args) {
        return _setType(node, new TypeNameNode(node.getIfName()));
    }

    /**
     * Sets the type of the node to the object type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetComponentNode
     */
    public Object visitGetComponentNode(GetComponentNode node, LinkedList args) {
        // Fix me: The type checking here is not so correct.
        // Finer type has to be identified!!!
        return _setType(node, new TypeNameNode(new NameNode(
                AbsentTreeNode.instance, new String("Node"),
                AbsentTreeNode.instance)));
    }

    /**
     * Sets the type of the node to the object type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetNthConnectionSrcNode
     */
    public Object visitGetNthConnectionSrcNode(GetNthConnectionSrcNode node,
            LinkedList args) {
        // Fix me: The type checking here is not so correct.
        // Finer type has to be identified!!!
        return _setType(node, new TypeNameNode(new NameNode(
                AbsentTreeNode.instance, new String("Node"),
                AbsentTreeNode.instance)));
    }

    /**
     * Sets the type of the node to the object type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetConnectionDestNode
     */
    public Object visitGetConnectionDestNode(GetConnectionDestNode node,
            LinkedList args) {
        // Fix me: The type checking here is not so correct.
        // Finer type has to be identified!!!
        return _setType(node, new TypeNameNode(new NameNode(
                AbsentTreeNode.instance, new String("Netlist"),
                AbsentTreeNode.instance)));
    }

    /**
     * Sets the type of the node to the int type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetConnectionNumNode
     */
    public Object visitGetConnectionNumNode(GetConnectionNumNode node,
            LinkedList args) {
        return _setType(node, IntTypeNode.instance);
    }

    /**
     * Sets the type of the node to the process type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetProcessNode
     */
    public Object visitGetProcessNode(GetProcessNode node, LinkedList args) {
        /*
         * NameNode name = new NameNode(AbsentTreeNode.instance,"metamodel",
         * AbsentTreeNode.instance); name = new
         * NameNode(name,"lang",AbsentTreeNode.instance); name = new
         * NameNode(name,"Process",AbsentTreeNode.instance);
         */
        return _setType(node, MetaModelLibrary.PROCESS_TYPE);
    }

    /**
     * Sets the type of the node to the object type.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of GetScopeNode
     */
    public Object visitGetScopeNode(GetScopeNode node, LinkedList args) {
        // Fix me: The type checking here is not so correct.
        // Finer type has to be identified!!!
        return _setType(node, new TypeNameNode(new NameNode(
                AbsentTreeNode.instance, new String("Node"),
                AbsentTreeNode.instance)));
    }

    /**
     * Sets the type of the node to the type of its expression.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of ActionLabelExprNode
     */
    public Object visitActionLabelExprNode(ActionLabelExprNode node,
            LinkedList args) {
        return node.getExpr().accept(this, args);
    }

    /**
     * Sets the type of the node to int.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of SpecialLitNode
     */
    public Object visitSpecialLitNode(SpecialLitNode node, LinkedList args) {
        return _setType(node, IntTypeNode.instance);
    }

    /**
     * Sets the type of the node to String.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of SpecialLitNode
     */
    public Object visitGetInstNameNode(GetInstNameNode node, LinkedList args) {
        return _setType(node, MetaModelLibrary.STRING_TYPE);
    }

    /**
     * Sets the type of the node to String.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return the instance of SpecialLitNode
     */
    public Object visitGetCompNameNode(GetCompNameNode node, LinkedList args) {
        return _setType(node, MetaModelLibrary.STRING_TYPE);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** TypeIdentifier used to identify types. */
    protected final TypeIdentifier _typeID;

    /** TypePolicy used to figure out the result type of expressions. */
    protected final TypePolicy _typePolicy;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Check if a then-expr can be assigned to the type of else-expr or
     * else-expr can be assigned to the type of then-expr.
     *
     * @param e1
     *            then expression
     * @param t1
     *            then type
     * @param e2
     *            else expression
     * @param t2
     *            else type
     * @param kind
     *            The kind of primitive type, see {@link TypeVisitor}.
     * @return true if one expression can be assigned to another type
     */
    protected boolean _validIf(ExprNode e1, TypeNode t1, ExprNode e2,
            TypeNode t2, int kind) {
        return (((_typeID.kind(t1) == kind) && _typePolicy.canAssign(e2, t1)) || ((_typeID
                .kind(t2) == kind) && _typePolicy.canAssign(e1, t2)));
    }

    /**
     * Sets the type of the node to the promoted type of the arithmetic.
     * Exception will be thrown if the types are not numeric.
     *
     * @param node
     *            the node that is being visited
     * @return the TypeNode of the promoted type
     */
    public TypeNode _visitBinaryOpNode(BinaryOpNode node) {
        TypeNode type1 = type(node.getExpr1());
        TypeNode type2 = type(node.getExpr2());

        if ((!_typePolicy.isNumericType(type1) && !_typePolicy
                .isTypeParameter(type1))
                || (!_typePolicy.isNumericType(type2) && !_typePolicy
                        .isTypeParameter(type2))) {
            char op;
            if (node instanceof MinusNode)
                op = '-';
            else if (node instanceof MultNode)
                op = '*';
            else if (node instanceof DivNode)
                op = '\\';
            else if (node instanceof RemNode)
                op = '%';
            else if (node instanceof BitAndNode)
                op = '&';
            else if (node instanceof BitOrNode)
                op = '|';
            else if (node instanceof BitXorNode)
                op = '&';
            else
                op = ' ';

            throw new RuntimeException("ERROR: Invalid operands for " + op
                    + ". Operands must be of numeric type.");
        }

        return _setType(node, _typePolicy.arithPromoteType(type1, type2));
    }

    /**
     * Sets the type of the node by looking at its declaration.
     *
     * @param node
     *            ParamAccessNode whose type should be set
     * @return the TypeNode corresponds to the port type
     */
    public TypeNode _visitParamAccessNode(ParamAccessNode node) {
        ParameterDecl parameterDecl = (ParameterDecl) MetaModelDecl
                .getDecl((NamedNode) node);

        return _setType(node, parameterDecl.getType());
    }

    /**
     * Sets the type of the node by looking at its declaration.
     *
     * @param node
     *            PortAccessNode whose type should be set
     * @return the TypeNode corresponds to the port type
     */
    public TypeNode _visitPortAccessNode(PortAccessNode node) {
        PortDecl portDecl = (PortDecl) MetaModelDecl.getDecl((NamedNode) node);

        return _setType(node, portDecl.getType());
    }

    /**
     * Sets the type of the node by looking at its declaration.
     *
     * @param node
     *            FieldAccessNode whose type should be set
     * @return the TypeNode corresponds to the field type
     */
    public TypeNode _visitFieldAccessNode(FieldAccessNode node) {
        FieldDecl fieldDecl = (FieldDecl) MetaModelDecl
                .getDecl((NamedNode) node);

        return _setType(node, fieldDecl.getType());
    }

    /**
     * Sets the type of the node to the promoted type of the arithmetic. Throw
     * an exception if the type is not numeric.
     *
     * @param node
     *            the node that is being visited
     * @return the TypeNode of the promoted type
     */
    public TypeNode _visitSingleOpNode(SingleOpNode node) {
        TypeNode type1 = type(node.getExpr());

        if (!_typePolicy.isNumericType(type1)) {
            String op;
            if (node instanceof UnaryPlusNode)
                op = "+";
            else if (node instanceof UnaryMinusNode)
                op = "-";
            else if (node instanceof PostIncrNode
                    || node instanceof PreIncrNode)
                op = "++";
            else if (node instanceof PostDecrNode
                    || node instanceof PreDecrNode)
                op = "--";
            else
                op = " ";

            throw new RuntimeException("ERROR: Invalid operands for " + op
                    + ". Operand must be of numeric type.");
        }

        return _setType(node, _typePolicy.arithPromoteType(type1));
    }

    /**
     * Sets the type of the node to the type of first expression. Throw an
     * exception if either expression is not of numeric type.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return TypeNode the TypeNode corresponds to type of first expression
     */
    protected TypeNode _visitBinaryOpAssignNode(BinaryOpAssignNode node) {
        TypeNode type1 = type(node.getExpr1());
        TypeNode type2 = type(node.getExpr2());

        if (!_typePolicy.isNumericType(type1)
                || !_typePolicy.isNumericType(type2)) {
            String op;

            if (node instanceof LeftShiftLogAssignNode)
                op = "<<=";
            else if (node instanceof RightShiftLogAssignNode)
                op = ">>>=";
            else if (node instanceof RightShiftArithAssignNode)
                op = ">>=";
            else
                op = " ";

            throw new RuntimeException("ERROR: Invalid operands for " + op
                    + ". Operands must be of numeric type.");
        }

        // assignment expression, thus set type to type of first expression
        return _setType(node, type1);
    }

    /**
     * Sets the type of the node to the promoted type of the left expression.
     * Throw an exception if either operand expression is not of numeric type.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the TypeNode corresponds to the promoted type of the left
     *         expression
     */
    protected TypeNode _visitShiftNode(ShiftNode node) {
        TypeNode type1 = type(node.getExpr1());
        TypeNode type2 = type(node.getExpr2());

        if (!_typePolicy.isNumericType(type1)
                || !_typePolicy.isNumericType(type2)) {
            String op;

            if (node instanceof LeftShiftLogNode)
                op = "<<";
            else if (node instanceof RightShiftLogNode)
                op = ">>>";
            else if (node instanceof RightShiftArithNode)
                op = ">>";
            else
                op = " ";

            throw new RuntimeException("ERROR: Invalid operands for " + op
                    + ". Operands must be of numeric type.");
        }

        return _setType(node, _typePolicy.arithPromoteType(type1));
    }

    /**
     * Sets the type of the node to boolean type. Throw an exception if either
     * operand expression is not of numeric type.
     *
     * @param node
     *            the node whose type needs to be determined
     * @return the instance of BoolTypeNode
     */
    protected TypeNode _visitComparisonNode(RelationNode node) {
        TypeNode type1 = type(node.getExpr1());
        TypeNode type2 = type(node.getExpr2());

        if (!_typePolicy.isNumericType(type1)
                || !_typePolicy.isNumericType(type2)) {
            String op;

            if (node instanceof GTNode)
                op = ">";
            else if (node instanceof LTNode)
                op = "<";
            else if (node instanceof GENode)
                op = ">=";
            else if (node instanceof LENode)
                op = "<=";
            else
                op = " ";

            throw new RuntimeException("ERROR: Invalid operands for " + op
                    + ". Operands must be of numeric type.");
        }

        return _setType(node, BoolTypeNode.instance);
    }

    /**
     * Memoize the type by setting the TYPE_KEY property of the node and return
     * the TypeNode.
     *
     * @param expr
     *            the ExprNode whose TYPE_KEY needs to be set
     * @param type
     *            the TypeNode corresponds to the type of the ExprNode
     * @return the same TypeNode passed in from the argument
     */
    protected TypeNode _setType(ExprNode expr, TypeNode type) {
        expr.setProperty(TYPE_KEY, type);
        return type;
    }

    /**
     * The default visit method. It always throw an exception because if a node
     * is an expression, it must have been handled by other methods. But if it
     * is not an expression, this visitor should not have visited it in the
     * first place.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return it will never return
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("node " + node.toString()
                + " is not an expression, so it does not have a type");
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

}
