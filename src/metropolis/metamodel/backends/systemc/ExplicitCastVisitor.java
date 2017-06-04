/* A visitor that adds explicit casts to assignments and parameter passing
 where arrays of a reference type are involved.

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

import metropolis.metamodel.Decl;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.frontend.FormalParameterDecl;
import metropolis.metamodel.frontend.MethodDecl;
import metropolis.metamodel.frontend.TypeParameterDecl;
import metropolis.metamodel.frontend.TypePolicy;
import metropolis.metamodel.frontend.TypeVisitor;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.AllocateNode;
import metropolis.metamodel.nodetypes.ArrayInitTypeNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.AssignNode;
import metropolis.metamodel.nodetypes.CastNode;
import metropolis.metamodel.nodetypes.ConstructorCallNode;
import metropolis.metamodel.nodetypes.ExprNode;
import metropolis.metamodel.nodetypes.MethodCallNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.VarInitDeclNode;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

// ////////////////////////////////////////////////////////////////////////
// // ExplicitCastVisitor
/**
 * A visitor that adds explicit casts to arrays and parameter passes where
 * arrays of a reference type are involved. Explicit casts are required when
 * meta-model files have to be translated into C++.
 * <p>
 * This visitor uses the annotation TYPE_KEY in the expression nodes, created by
 * the type visitor.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ExplicitCastVisitor.java,v 1.30 2006/10/12 20:32:56 cxh Exp $
 */
public class ExplicitCastVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Create a new explicit cast visitor. */
    public ExplicitCastVisitor() {
        super(TM_CHILDREN_FIRST);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. Only two kinds of nodes are relevant for our
     * traversal: assignment methods and method calls.
     *
     * @param node
     *            Node being visited.
     * @param args
     *            List of arguments (unused).
     * @return null, as the return value is unused.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        if (node instanceof AssignNode) {
            explicitCastAssignment((AssignNode) node);
        } else if (node instanceof VarInitDeclNode) {
            explicitCastVarDecl((VarInitDeclNode) node);
        } else if (node instanceof MethodCallNode) {
            explicitCastMethodCall((MethodCallNode) node);
        } else if (node instanceof ConstructorCallNode) {
            explicitCastConstructorCall((ConstructorCallNode) node);
        } else if (node instanceof AllocateNode) {
            explicitCastAllocate((AllocateNode) node);
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Type visitor used to compute the types of expressions. */
    protected static TypeVisitor _typeVisitor = new TypeVisitor();

    /** Type policy used to handle, identify and convert types. */
    protected static TypePolicy _typePolicy = new TypePolicy();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Add an explicit cast to an assignment if it is required.
     *
     * @param assign
     *            Assignment being modified.
     */
    protected void explicitCastAssignment(AssignNode assign) {
        ExprNode lhs = assign.getExpr1();
        ExprNode rhs = assign.getExpr2();
        TypeNode destType = _typeVisitor.type(lhs);
        if (isExplicitCastRequired(destType, rhs)) {
            try {
                TypeNode castType = (TypeNode) destType.clone();
                CastNode explicitCast = new CastNode(castType, rhs);
                assign.setExpr2(explicitCast);
            } catch (CloneNotSupportedException ex) {
                // FIXME: this method should handle exceptions better
                throw new RuntimeException("Clone of '" + destType
                        + "' not supported", ex);
            }

        }
    }

    /**
     * Add an explicit cast to the initialization expression of a local
     * variable/field declaration.
     *
     * @param node
     *            Local variable/field declaration being modified.
     */
    protected void explicitCastVarDecl(VarInitDeclNode node) {
        TreeNode initExp = node.getInitExpr();
        if (initExp == AbsentTreeNode.instance)
            return;
        ExprNode init = (ExprNode) initExp;
        TypeNode destType = node.getDefType();
        if (isExplicitCastRequired(destType, init)) {
            try {
                TypeNode castType = (TypeNode) destType.clone();
                CastNode explicitCast = new CastNode(castType, init);
                node.setInitExpr(explicitCast);
            } catch (CloneNotSupportedException ex) {
                // FIXME: this method should handle exceptions better.
                throw new RuntimeException("Clone of '" + destType
                        + "' not supported", ex);
            }

        }
    }

    /**
     * Add an explicit cast to the parameters of a method call that requires it.
     *
     * @param call
     *            Method call being modified.
     */
    protected void explicitCastMethodCall(MethodCallNode call) {
        List parameters = call.getArgs();
        ExprNode method = call.getMethod();
        // Get the method declaration
        NameNode methodName = ((NamedNode) method).getName();
        MethodDecl methodDecl = (MethodDecl) methodName.getProperty(DECL_KEY);
        if (methodDecl == null) {
            // Not resolved; likely involves a template.
            return;
        }
        parameters = explicitCastCall(methodDecl, parameters);
        call.setArgs(parameters);
    }

    /**
     * Add an explicit cast to the parameters of a constructor call that
     * requires it.
     *
     * @param call
     *            Method call being modified.
     */
    protected void explicitCastConstructorCall(ConstructorCallNode call) {
        List parameters = call.getArgs();
        // Get the method declaration
        MethodDecl methodDecl = (MethodDecl) call.getProperty(DECL_KEY);
        if (methodDecl == null) {
            // The class is 'Object'. No super constructor call
            return;
        }

        parameters = explicitCastCall(methodDecl, parameters);
        call.setArgs(parameters);
    }

    /**
     * Add an explicit cast to the parameters of a "new" call that require it.
     *
     * @param call
     *            New call being modified.
     */
    protected void explicitCastAllocate(AllocateNode call) {
        List parameters = call.getArgs();
        // Get the method declaration
        MethodDecl methodDecl = (MethodDecl) call.getProperty(DECL_KEY);
        parameters = explicitCastCall(methodDecl, parameters);
        call.setArgs(parameters);
    }

    /**
     * Add an explicit cast to the parameters of a method or constructor call
     * that requires it.
     *
     * @param methodDecl
     *            Declaration of the method or constructor being called.
     * @param args
     *            List of real arguments of the call. This list is modified
     *            inside this method, and returned as a result.
     * @return The list of modified arguments, with possibly one or more
     *         explicit casts added.
     */
    protected List explicitCastCall(MethodDecl methodDecl, List args) {

        // Get the list of formal parameters of the call
        List params = methodDecl.getParams();
        ListIterator formalIter = params.listIterator();
        ListIterator currentIter = args.listIterator();

        // Traverse the list of real parameters
        while (currentIter.hasNext()) {
            FormalParameterDecl par = (FormalParameterDecl) formalIter.next();
            ExprNode arg = (ExprNode) currentIter.next();
            TypeNode destType = par.getType();

            if (isExplicitCastRequired(destType, arg)) {
                // Create the explicit cast
                try {
                    TypeNode castType = (TypeNode) destType.clone();
                    CastNode explicitCast = new CastNode(castType, arg);
                    currentIter.remove();
                    currentIter.add(explicitCast);
                } catch (CloneNotSupportedException ex) {
                    // FIXME: this method should handle exceptions better.
                    throw new RuntimeException("Clone of '" + destType
                            + "' not supported", ex);
                }
            }
        }
        return args;
    }

    /**
     * Test if a explicit cast is required before an assignment of an
     * expression.
     *
     * @param expr
     *            Expression being assigned.
     * @param destType
     *            Type of the destination.
     * @return true iff an explicit cast is required.
     */
    protected boolean isExplicitCastRequired(TypeNode destType, ExprNode expr) {
        TypeNode srcType = _typeVisitor.type(expr);
        TypeNode base = srcType;

        if (srcType instanceof ArrayTypeNode) {
            while (base instanceof ArrayTypeNode)
                base = ((ArrayTypeNode) base).getBaseType();
        } else if (srcType instanceof ArrayInitTypeNode) {
            return false;
        }

        if (base instanceof TypeNameNode) {
            Decl decl = (Decl) ((TypeNameNode) base).getName().getProperty(
                    DECL_KEY);
            if (decl instanceof TypeParameterDecl) {
                return false;
            }
        }

        return (!_typePolicy.areEqual(srcType, destType));
    }

}
