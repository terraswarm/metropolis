/* A visitor that performs resolution of fields (in expressions, rather than
 names), overloading and other semantic checks.

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
import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.AllocateNode;
import metropolis.metamodel.nodetypes.ArrayAccessNode;
import metropolis.metamodel.nodetypes.BlockNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ConnectNode;
import metropolis.metamodel.nodetypes.ConstructorCallNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.EventNode;
import metropolis.metamodel.nodetypes.ExprNode;
import metropolis.metamodel.nodetypes.FieldAccessNode;
import metropolis.metamodel.nodetypes.FieldDeclNode;
import metropolis.metamodel.nodetypes.GetConnectionDestNode;
import metropolis.metamodel.nodetypes.GetNthPortNode;
import metropolis.metamodel.nodetypes.GetScopeNode;
import metropolis.metamodel.nodetypes.GlobalLabelNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.LocalLabelNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodCallNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.NullTypeNode;
import metropolis.metamodel.nodetypes.ObjectFieldAccessNode;
import metropolis.metamodel.nodetypes.ObjectNode;
import metropolis.metamodel.nodetypes.ObjectParamAccessNode;
import metropolis.metamodel.nodetypes.ObjectPortAccessNode;
import metropolis.metamodel.nodetypes.OuterSuperAccessNode;
import metropolis.metamodel.nodetypes.OuterThisAccessNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.RedirectConnectNode;
import metropolis.metamodel.nodetypes.RefineConnectNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.SetScopeNode;
import metropolis.metamodel.nodetypes.SuperConstructorCallNode;
import metropolis.metamodel.nodetypes.SuperFieldAccessNode;
import metropolis.metamodel.nodetypes.SuperParamAccessNode;
import metropolis.metamodel.nodetypes.SuperPortAccessNode;
import metropolis.metamodel.nodetypes.ThisConstructorCallNode;
import metropolis.metamodel.nodetypes.ThisFieldAccessNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.ThisParamAccessNode;
import metropolis.metamodel.nodetypes.ThisPortAccessNode;
import metropolis.metamodel.nodetypes.TypeFieldAccessNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ResolveFieldVisitor
/**
 * A visitor that does field and method resolution. Overloading and other tests
 * that require types are also performed here. The traversal method used is
 * TM_CUSTOM.
 * <p>
 * The nodes of this visitor return the subtree that should be used to replace
 * the node. If a node does not want to be replaced, then it has to return
 * itself.
 * <p>
 * The list of arguments in each visit() method contains only one argument,
 * called FieldContext, that has all the necessary information.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ResolveFieldVisitor.java,v 1.47 2006/10/12 20:34:06 cxh Exp $
 */
public class ResolveFieldVisitor extends ReplacementVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that traverses an AST performing field and method
     * resolution. Set the traversal method to TM_CUSTOM by default.
     */
    public ResolveFieldVisitor() {
        super(TM_CUSTOM);
        _typeVisitor = new TypeVisitor();
        _typeID = new TypeIdentifier();
        _typePolicy = new TypePolicy();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit a compile unit of a meta-model program, resolving accesses to
     * fields and methods inside the classes of the compile unit.
     *
     * @param node
     *            The compile unit being visited.
     * @param args
     *            List of arguments (unused).
     * @return This node.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        FieldContext ctx = new FieldContext();
        LinkedList childArgs = TNLManip.addFirst(ctx);
        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        return node;
    }

    /**
     * Visit a class declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The class declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a quantity declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The quantity declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit an interface declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The interface declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a process declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The process declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a medium declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The medium declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a scheduler declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The scheduler declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a state medium declaration, resolving accesses to fields and
     * methods inside it.
     *
     * @param node
     *            The state medium being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a netlist declaration, resolving accesses to fields and methods
     * inside it.
     *
     * @param node
     *            The netlist declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a declaration of a field of an object. Traverse the expression used
     * to initialize the field.
     *
     * @param node
     *            The declaration of a field being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        TreeNode expr = node.getInitExpr();
        expr = (TreeNode) expr.accept(this, args);
        node.setInitExpr(expr);

        return node;
    }

    /**
     * Visit a declaration of a method. Traverse the code of this method
     * resolving the names of fields and objects. If the method is static,
     * register this in the context used to traverse the code.
     *
     * @param node
     *            The method declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        // Build context to traverse code
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.inStatic = ((node.getModifiers() & STATIC_MOD) != 0);
        subCtx.currentMethod = (MethodDecl) MetaModelDecl.getDecl(node
                .getName());
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Traverse the body of the method
        TreeNode body = node.getBody();
        body = (TreeNode) body.accept(this, childArgs);
        node.setBody(body);

        return node;
    }

    /**
     * Visit a declaration of a constructor. Traverse the code of the
     * constructor, resolving names of fields and methods.
     *
     * @param node
     *            The method declaration being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.currentMethod = (MethodDecl) MetaModelDecl.getDecl(node
                .getName());
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Visit the call to the constructor of this class
        ConstructorCallNode call = node.getConstructorCall();
        call = (ConstructorCallNode) call.accept(this, childArgs);
        node.setConstructorCall(call);

        // Visit the code of the constructor
        BlockNode body = node.getBody();
        body = (BlockNode) body.accept(this, childArgs);
        node.setBody(body);

        return node;
    }

    /**
     * Visit a call to a constructor of current object. Traverse the arguments
     * of the call, and compute its types. According to the signature of the
     * method being called, set the correct declaration of the constructor being
     * called.
     *
     * @param node
     *            The call to a constructor being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitThisConstructorCallNode(ThisConstructorCallNode node,
            LinkedList args) {
        // Get the list of possible methods in this call
        FieldContext ctx = (FieldContext) args.get(0);
        ObjectDecl current = ctx.currentObject;
        Scope scope = current.getScope();
        ScopeIterator methods = scope.lookupFirstLocal(current.getName(),
                CG_CONSTRUCTOR);

        // Traverse the arguments of the method call
        List callArgs = node.getArgs();
        callArgs = TNLManip.traverseList(this, args, callArgs);
        node.setArgs(callArgs);

        // Find the correct declaration of the method/constructor
        MethodDecl constructor = _resolveCall(ctx, methods, callArgs);
        node.setProperty(DECL_KEY, constructor);

        // A user-defined type can always access its constructors,
        // so we don't need to check if the constructor can be
        // accessed.

        return node;
    }

    /**
     * Visit a call to the constructor of the superclass. Traverse the arguments
     * of the call, and compute its types. According to the signature of the
     * method being called, set the correct declaration of the constructor being
     * called.
     *
     * @param node
     *            The call to a constructor being visited.
     * @param args
     *            List of arguments w
     * @return This node.
     */
    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        ObjectDecl current = ctx.currentObject.getSuperClass();
        if (current == null) {
            // The class is 'Object'
            // remove the call to the parent
            // return AbsentTreeNode.instance;
            // FIXME: Must modify constructorDeclNode in order to use
            // absent tree node
            return node;
        }
        Scope scope = current.getScope();
        ScopeIterator methods = scope.lookupFirstLocal(current.getName(),
                CG_CONSTRUCTOR);

        if (!methods.hasNext()) {
            throw new RuntimeException("Class " + current.getName()
                    + " does not have a constructor");
        }

        // Traverse the arguments of the method call
        List callArgs = node.getArgs();
        callArgs = TNLManip.traverseList(this, args, callArgs);
        node.setArgs(callArgs);

        // Find the correct declaration of the method/constructor
        MethodDecl constructor = _resolveCall(ctx, methods, callArgs);
        node.setProperty(DECL_KEY, constructor);

        return node;
    }

    /**
     * Visit a reference to 'this' object. Check that we are not inside a static
     * block of code.
     *
     * @param node
     *            The reference to 'this' being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node.
     */
    public Object visitThisNode(ThisNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic) {
            _error(ctx, "cannot use 'this' in static code");
        }
        return node;
    }

    /**
     * Visit an access to a port of an object. Test if the access can be
     * replaced by a ThisPortAccessNode.
     *
     * @param node
     *            The port access being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node, if the object is not a ThisNode, or ThisPortAccessNode
     *         otherwise.
     */
    public Object visitObjectPortAccessNode(ObjectPortAccessNode node,
            LinkedList args) {

        // Build the context to be used to resolve the object
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.methodArgs = null;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Resolve the object before resolving the field
        // getObject() should return an expression after name resolution
        ExprNode expr = (ExprNode) (node.getObject().accept(this, childArgs));
        node.setObject(expr);

        if (expr.classID() == THISNODE_ID) {
            Object thisClass = expr.getProperty(THIS_CLASS_KEY);
            TreeNode retVal = new ThisPortAccessNode(node.getName());
            retVal.setProperty(THIS_CLASS_KEY, thisClass);
            return retVal;
        } else {
            return node;
        }
    }

    /**
     * Visit an access to a parameter of an object. Test if the access can be
     * replaced by a ThisParamAccessNode.
     *
     * @param node
     *            The parameter access being visited.
     * @param args
     *            List of arguments with the context.
     * @return This node, if the object is not a ThisNode, or
     *         ThisParamAccessNode otherwise.
     */
    public Object visitObjectParamAccessNode(ObjectParamAccessNode node,
            LinkedList args) {

        // Build the context to be used to resolve the object
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.methodArgs = null;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Resolve the object before resolving the field
        // getObject() should return an expression after name resolution
        ExprNode expr = (ExprNode) (node.getObject().accept(this, childArgs));
        node.setObject(expr);

        if (expr.classID() == THISNODE_ID) {
            Object thisClass = expr.getProperty(THIS_CLASS_KEY);
            TreeNode retVal = new ThisParamAccessNode(node.getName());
            retVal.setProperty(THIS_CLASS_KEY, thisClass);
            return retVal;
        } else {
            return node;
        }
    }

    /**
     * Visit an access to a field of an object. If the name of the field has not
     * been resolved yet, it may result that this is an access to a port or
     * parameter, instead of an access to a field.
     *
     * @param node
     *            The field access being visited.
     * @param args
     *            List of arguments with the context.
     * @return The node that represents this access, after resolving the name of
     *         the field/port/parameter. It can be one of the following:
     *         ObjectFieldAccessNode, ObjectPortAcessNode,
     *         ObjectParamAccessNode, ThisFieldAccessNode, ThisPortAccessNode or
     *         ThisParamAccessNode.
     * @exception RuntimeException
     *                if the port/field/parameter is not declared or it is not
     *                accessible.
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {

        // Build the context to be used to resolve the object
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.methodArgs = null;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Resolve the object before resolving the field
        // getObject() should return an expression after name resolution
        ExprNode expr = (ExprNode) (node.getObject().accept(this, childArgs));
        node.setObject(expr);

        TypeNode t = _typeVisitor.type(expr);

        if (node.getObject() instanceof EventNode)
            return node; // GY
        else if (_typePolicy.isTypeParameter(t))
            return node; // xichen
        else if (!(_typePolicy.isReferenceType(t) || _typePolicy.isArrayType(t))) {
            _error(ctx, "attempt to select from non-reference type "
                    + _typePolicy.toString(t, true));
        }

        _resolveFieldName(node, ctx, CG_FIELD | CG_PORT | CG_PARAMETER);

        NameNode name = node.getName();
        MemberDecl decl = (MemberDecl) name.getProperty(DECL_KEY);

        // Choose the kind of node that has to be returned
        if (expr.classID() == THISNODE_ID) {
            Object thisClass = expr.getProperty(THIS_CLASS_KEY);
            TreeNode retVal;
            switch (decl.category) {
            case CG_PARAMETER:
                retVal = new ThisParamAccessNode(name);
                break;
            case CG_PORT:
                retVal = new ThisPortAccessNode(name);
                break;
            default:
                retVal = new ThisFieldAccessNode(name);
                break;
            }
            retVal.setProperty(THIS_CLASS_KEY, thisClass);
            return retVal.accept(this, args);
        } else {
            switch (decl.category) {
            case CG_PARAMETER:
                return new ObjectParamAccessNode(name, expr);
            case CG_PORT:
                return new ObjectPortAccessNode(name, expr);
            default:
                return node;
            }
        }
    }

    /**
     * Visit an access to a TypeFieldAccessNode. This node only cannot be
     * generated by the grammar; it can only be generated in ResolveNameVisitor
     * after resolving the name of an object. The name in this node can only be
     * the name of a static method.
     *
     * @param node
     *            Access to a static method being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if the method is not declared, or it is not accessible, or
     *                we are inside a static code section.
     */
    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        MemberDecl name = (MemberDecl) MetaModelDecl.getDecl(node.getName());
        if (name.category == CG_METHOD) {
            node.getName().removeProperty(DECL_KEY);
            _resolveFieldName(node, ctx, CG_METHOD);
        } else
            _resolveFieldName(node, ctx, CG_FIELD);
        MemberDecl decl = (MemberDecl) MetaModelDecl.getDecl((NamedNode) node);
        if ((decl.getModifiers() & STATIC_MOD) == 0) {
            ObjectDecl cont = (ObjectDecl) decl.getContainer();
            _error(ctx, "field/method " + _typePolicy.toString(decl, true)
                    + " of " + _typePolicy.toString(cont, true)
                    + " is not static, so it cannot be selected from"
                    + " the name of the class");
        }
        return node;
    }

    /**
     * Visit an access to a field of the superclass. Check that we are not
     * inside a 'static' section of code. As the name of the field has not been
     * resolved yet, it may result that this is an access to a port or
     * parameter, instead of an access to a field.
     *
     * @param node
     *            The field of the superclass being accessed.
     * @param args
     *            List of arguments with context information.
     * @return The node that represents this access, after resolving the name of
     *         the field/port/parameter. It can be one of the following:
     *         SuperFieldAccessNode, SuperPortAccessNode, SuperParamAccessNode.
     * @exception RuntimeException
     *                if the field/port/parameter is not declared, or it is not
     *                accessible, or we are inside a static code section.
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic) {
            _error(ctx, "cannot access fields/methods of the superclass "
                    + "like '" + node.getName().getIdent()
                    + "' inside static code");
        }
        MetaModelDecl decl = MetaModelDecl.getDecl(node.getName());
        if ((decl != null) && (decl.category == CG_METHOD)) {
            // Resolution of overloading
            node.getName().removeProperty(DECL_KEY);
        }
        _resolveFieldName(node, ctx, CG_FIELD | CG_PORT | CG_PARAMETER
                | CG_METHOD);
        // Choose the kind of node that we should return
        NameNode name = node.getName();
        decl = MetaModelDecl.getDecl(name);
        TreeNode retVal;
        switch (decl.category) {
        case CG_PORT:
            retVal = new SuperPortAccessNode(name);
            break;
        case CG_PARAMETER:
            retVal = new SuperParamAccessNode(name);
            break;
        default:
            retVal = node;
            break;
        }
        ObjectDecl object = (ObjectDecl) decl.getContainer();
        ObjectDecl superClass = object.getSuperClass();
        if (superClass == null) // class Object
            retVal.setProperty(THIS_CLASS_KEY, object.getDefType());
        else
            retVal.setProperty(THIS_CLASS_KEY, superClass.getDefType());
        return retVal;
    }

    /**
     * Visit an access to a port of a superclass. The name of the port has
     * already been resolved (this is why we know its a port!), so we just have
     * to check that we are not inside static code.
     *
     * @param node
     *            The node containing an access to a port of the superclass.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are inside a static code section.
     */
    public Object visitSuperPortAccessNode(SuperPortAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic)
            _error(ctx, "cannot access ports of the superclass like '"
                    + node.getName().getIdent() + "' in static code");
        return node;
    }

    /**
     * Visit an access to a parameter of a superclass. The name of the parameter
     * has already been resolved (this is why we know that it is a parameter!),
     * so we just have to check that we are not inside static code.
     *
     * @param node
     *            The node containing an access to a port of the superclass.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are inside a static code section.
     */
    public Object visitSuperParamAccessNode(SuperParamAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic)
            _error(ctx, "cannot access parameters of the superclass like '"
                    + node.getName().getIdent() + "'in static code");
        return node;
    }

    /**
     * Visit an access to a field of 'this'. The name of the field has already
     * been resolved, we only have to check that we are not inside a static code
     * section.
     *
     * @param node
     *            The access to a field of 'this'.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        MemberDecl decl = (MemberDecl) MetaModelDecl.getDecl(node.getName());
        // If the field is a method, resolve overloading
        if (decl.category == CG_METHOD) {
            node.getName().removeProperty(DECL_KEY);
            _resolveFieldName(node, ctx, CG_METHOD);
            decl = (MemberDecl) MetaModelDecl.getDecl(node.getName());
        }
        boolean nonStatic = ((decl.getModifiers() & STATIC_MOD) == 0);
        if (ctx.inStatic && nonStatic) {
            if (decl.category == CG_METHOD) {
                _error(ctx, "trying to call non-static method "
                        + _typePolicy.toString((MethodDecl) decl, true)
                        + " inside static code");
            } else {
                _error(ctx, "trying to access non-static field "
                        + node.getName().getIdent() + " of current class "
                        + "inside static code");
            }
        }
        return node;
    }

    /**
     * Visit an access to a port of 'this'. The name of the port has already
     * been resolved, we only have to check that we are not inside a static code
     * section.
     *
     * @param node
     *            The access to a port of 'this'.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are inside a static code section.
     */
    public Object visitThisPortAccessNode(ThisPortAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        // FIXME: Port has been resolved?
        if (ctx.inStatic) {
            _error(ctx, "cannot access port '" + node.getName().getIdent()
                    + "' of current class inside static code");
        }
        return node;
    }

    /**
     * Visit an access to a parameter of 'this'. The name of the parameter has
     * already been resolve, we only have to check that we are not inside a
     * static code section.
     *
     * @param node
     *            The access to a parameter of 'this'.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are inside a static code section.
     */
    public Object visitThisParamAccessNode(ThisParamAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        // FIXME: Param has been resolved?
        if (ctx.inStatic) {
            _error(ctx, "cannot access parameter '" + node.getName().getIdent()
                    + "' of current class inside static code");
        }
        return node;
    }

    /**
     * Visit a connect node. Resolve names for the source node and the
     * destination node. Check that if the port is expressed as a simple name,
     * it is resolved using the type of the source node.
     *
     * @param node
     *            The connect node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitConnectNode(ConnectNode node, LinkedList args) {
        // Traverse the source and destination objects with the same
        // argument list.
        ExprNode src = node.getSrcObject();
        src = (ExprNode) src.accept(this, args);
        ExprNode dst = node.getDstObject();
        dst = (ExprNode) dst.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the source object,
        // rather than an independent expression.
        ExprNode port = node.getPort();
        Boolean localpr = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localpr.booleanValue()) {
            port = (ExprNode) port.accept(this, args);
        } else {
            port = _resolvePortAccess(src, port, args);
        }
        node.setPort(port);

        // Replace source and target object if necessary
        node.setSrcObject(src);
        node.setDstObject(dst);

        return node;
    }

    /**
     * Visit a setscope node. Resolve names for the node and the netlist. Check
     * that if the port is expressed as a simple name, it is resolved using the
     * type of the node.
     *
     * @param node
     *            The setscope node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitSetScopeNode(SetScopeNode node, LinkedList args) {
        // Traverse the node and netlist with the same argument list.
        ExprNode srcNode = node.getNode();
        srcNode = (ExprNode) srcNode.accept(this, args);
        ExprNode netlist = node.getNetlist();
        netlist = (ExprNode) netlist.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the node, rather than
        // an independent expression.
        ExprNode port = node.getPort();
        node.setPort(_resolvePortAccess(srcNode, port, args));

        // Replace node and netlist if necessary
        node.setNode(srcNode);
        node.setNetlist(netlist);

        return node;
    }

    /**
     * Visit a refine connect node. Resolve names for the netlist, the source
     * object and the component object. Check that if the port is expressed as a
     * simple name, it is resolved using the type of the node.
     *
     * @param node
     *            The refine connect node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitRefineConnectNode(RefineConnectNode node, LinkedList args) {
        // Traverse the node and netlist with the same argument list.
        ExprNode netlist = node.getNetlist();
        netlist = (ExprNode) netlist.accept(this, args);
        ExprNode srcObject = node.getSrcObject();
        srcObject = (ExprNode) srcObject.accept(this, args);
        ExprNode component = node.getComponent();
        component = (ExprNode) component.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the node, rather than
        // an independent expression.
        ExprNode port = node.getPort();
        Boolean localpr = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localpr.booleanValue()) {
            port = (ExprNode) port.accept(this, args);
        } else {
            port = _resolvePortAccess(srcObject, port, args);
        }
        node.setPort(port);

        // Replace node, netlist and source object if necessary
        node.setNetlist(netlist);
        node.setSrcObject(srcObject);
        node.setComponent(component);

        return node;
    }

    /**
     * Visit a redirect connect node. Resolve names for the netlist, the source
     * object and the component object. Check that if the port is expressed as a
     * simple name, it is resolved using the type of the node.
     *
     * @param node
     *            The redirect connect node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitRedirectConnectNode(RedirectConnectNode node,
            LinkedList args) {
        // Traverse the node and netlist with the same argument list.
        ExprNode netlist = node.getNetlist();
        netlist = (ExprNode) netlist.accept(this, args);
        ExprNode srcObject = node.getSrcObject();
        srcObject = (ExprNode) srcObject.accept(this, args);
        ExprNode component = node.getComponent();
        component = (ExprNode) component.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the node, rather than
        // an independent expression.
        ExprNode port = node.getSrcPort();
        Boolean localpr = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localpr.booleanValue()) {
            port = (ExprNode) port.accept(this, args);
        } else {
            port = _resolvePortAccess(srcObject, port, args);
        }
        node.setSrcPort(port);

        port = node.getNewPort();
        localpr = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localpr.booleanValue()) {
            port = (ExprNode) port.accept(this, args);
        } else {
            port = _resolvePortAccess(component, port, args);
        }
        node.setNewPort(port);

        // Replace node, netlist and source object if necessary
        node.setNetlist(netlist);
        node.setSrcObject(srcObject);
        node.setComponent(component);

        return node;
    }

    /**
     * Visit a getconnectiondest node. Resolve names for the source node. Check
     * that if the port is expressed as a simple name, it is resolved using the
     * type of the source node.
     *
     * @param node
     *            The getconnectiondest node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitGetConnectionDestNode(GetConnectionDestNode node,
            LinkedList args) {
        // Traverse the source
        ExprNode src = node.getNode();
        src = (ExprNode) src.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the source object,
        // rather than an independent expression.
        ExprNode port = node.getPort();
        Boolean localpr = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localpr.booleanValue()) {
            port = (ExprNode) port.accept(this, args);
        } else {
            port = _resolvePortAccess(src, port, args);
        }
        node.setPort(port);

        // Replace source and target object if necessary
        node.setNode(src);

        return node;
    }

    /**
     * Visit a getnthport node. Resolve names for the source node. Check the
     * index of the port.
     *
     * @param node
     *            The getnthport node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitGetNthPortNode(GetNthPortNode node, LinkedList args) {
        // Traverse the source
        ExprNode src = node.getNode();
        src = (ExprNode) src.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the source object,
        // rather than an independent expression.
        ExprNode num = node.getNum();
        num = (ExprNode) num.accept(this, args);
        node.setNum(num);

        // Replace source and target object if necessary
        node.setNode(src);

        return node;
    }

    /**
     * Visit a getscope node. Resolve names for the source node. Check that if
     * the port is expressed as a simple name, it is resolved using the type of
     * the source node.
     *
     * @param node
     *            The getscope node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitGetScopeNode(GetScopeNode node, LinkedList args) {
        // Traverse the source
        ExprNode src = node.getNode();
        src = (ExprNode) src.accept(this, args);

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the source object,
        // rather than an independent expression.
        ExprNode port = node.getPort();
        Boolean localpr = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localpr.booleanValue()) {
            port = (ExprNode) port.accept(this, args);
        } else {
            port = _resolvePortAccess(src, port, args);
        }
        node.setPort(port);

        // Replace source and target object if necessary
        node.setNode(src);

        return node;
    }

    /**
     * Visit a method call. Register the list of arguments in the context, this
     * name will be resolved when the ObjectFieldAccessNode is reached.
     *
     * @param node
     *            The method call being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {

        // Traverse the arguments of the call with the same argument list
        List callArgs = node.getArgs();
        callArgs = TNLManip.traverseList(this, args, callArgs);
        node.setArgs(callArgs);

        // Build the context used to traverse the method call
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.methodArgs = callArgs;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Traverse the method call
        ExprNode expr = node.getMethod();
        expr = (ExprNode) expr.accept(this, childArgs);
        node.setMethod(expr);

        return node;
    }

    /**
     * Visit an access to an outer 'this' node. Check that this access happens
     * outside a static code section.
     *
     * @param node
     *            The outer 'this' access being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are inside a static code section.
     */
    public Object visitOuterThisAccessNode(OuterThisAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic)
            _error(ctx, "cannot access an outer 'this' in static code");
        return node;
    }

    /**
     * Visit an access to an outer 'super' node. Check that this access happens
     * outside a static code section.
     *
     * @param node
     *            The outer 'super' access being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are inside a static code section.
     */
    public Object visitOuterSuperAccessNode(OuterSuperAccessNode node,
            LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);
        if (ctx.inStatic)
            _error(ctx, "cannot access an outer 'super' in static code");
        return node;
    }

    /**
     * Visit an access to a node that invokes a constructor. Check that a
     * constructor with a matching signature is defined, and that the type being
     * allocated is not an interface or an abstract class.
     *
     * @param node
     *            The constructor invocation node being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if the constructor is not defined.
     */
    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        FieldContext ctx = (FieldContext) args.get(0);

        // Visit the enclosing instance
        if (!ctx.inStatic
                && (node.getEnclosingInstance() == AbsentTreeNode.instance)) {
            // If we didn't specify the enclosing instance, and we are not
            // inside static code, enclosing instance is 'this'
            ThisNode thisNode = new ThisNode();

            // Duplicate what's done by ResolveNameVisitor
            TypeNode type = ctx.currentObject.getDefType();
            thisNode.setProperty(THIS_CLASS_KEY, type);
            thisNode = (ThisNode) thisNode.accept(this, args);
            node.setEnclosingInstance(thisNode);
        } else {
            TreeNode enclosing = node.getEnclosingInstance();
            enclosing = (TreeNode) enclosing.accept(this, args);
            node.setEnclosingInstance(enclosing);
        }

        // Visit the arguments of the constructor call
        List callArgs = node.getArgs();
        callArgs = TNLManip.traverseList(this, args, callArgs);
        node.setArgs(callArgs);

        // Check the type being allocated
        TypeNameNode typeName = node.getDtype();
        ObjectDecl decl = (ObjectDecl) MetaModelDecl
                .getDecl(typeName.getName());
        if (decl.category == CG_INTERFACE) {
            _error(ctx, _typePolicy.toString(decl, true) + " cannot be "
                    + "used in a 'new' statement. Interfaces cannot be "
                    + " allocated!");
        }
        int modifiers = decl.getModifiers();
        if ((modifiers & ABSTRACT_MOD) != 0) {
            _error(ctx, "trying to allocate abstract "
                    + _typePolicy.toString(decl, true) + ". Abstract classes "
                    + " cannot have any instances");
        }

        // Get the constructor being called (check overloading)
        Scope scope = decl.getScope();
        String ident = decl.getName();
        ScopeIterator methods = scope.lookupFirstLocal(ident, CG_CONSTRUCTOR);
        MethodDecl constructor = _resolveCall(ctx, methods, node.getArgs());
        node.setProperty(DECL_KEY, constructor);

        return node;
    }

    /**
     * Visit an access to a local label of an object. Check if the label is
     * defined in the object where labels have to be resolved; if we are inside
     * a global label, then check that the label being accessed is global
     * (declared inside a 'block' statement).
     *
     * @param node
     *            Local label access being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are trying to access a global label which is local,
     *                or if the label is not declared.
     */
    public Object visitLocalLabelNode(LocalLabelNode node, LinkedList args) {
        // Don't visit nodes with labels that have been resolved
        // previously, i.e. LabeledStatementNode, LabeledBlockNode,
        // BreakNode, ContinueNode.
        StmtLblDecl decl = (StmtLblDecl) MetaModelDecl.getDecl(node.getName());
        if (decl != null)
            return node;

        FieldContext ctx = (FieldContext) args.get(0);
        boolean isGlobal = (ctx.labelObject != null)
                && (ctx.labelObject != ctx.currentObject);
        ObjectDecl where = (isGlobal ? ctx.labelObject : ctx.currentObject);
        String ident = node.getName().getIdent();
        decl = where.getLabel(ident);
        if (decl == null) {
            _error(ctx, _typePolicy.toString(where, true) + " does not have"
                    + " any label with identifier '" + ident + "'");
        } else if (isGlobal && !decl.isGlobal()) {
            _error(ctx, "label '" + ident + "' of "
                    + _typePolicy.toString(where, true) + " is not global "
                    + "- it should be declared using keyworkd 'block'");
        }
        // Set the declaration of the label being visited
        node.getName().setProperty(DECL_KEY, decl);

        return node;
    }

    /**
     * Visit an access to a global label of an object. Check that the object
     * being accessed is a Node, and set the object where the label will have to
     * be resolved.
     *
     * @param node
     *            Global label access being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     * @exception RuntimeException
     *                if we are trying to access a global label that is not
     *                local, or if the object whose label is accessed is not a
     *                node.
     */
    public Object visitGlobalLabelNode(GlobalLabelNode node, LinkedList args) {
        // Visit the object being accessed by the label
        ExprNode expr = node.getObj();
        expr = (ExprNode) expr.accept(this, args);
        node.setObj(expr);

        // Build the context used to traverse the local label
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        TypeNode type = _typeVisitor.type(expr);
        LocalLabelNode label = node.getLabel();
        if (!(type instanceof TypeNameNode)) {
            _error(ctx, "attempting to access label '"
                    + label.getName().getIdent()
                    + "' of an object that is not a node");
        }
        TypeNameNode typeName = (TypeNameNode) type;
        subCtx.labelObject = (ObjectDecl) MetaModelDecl
                .getDecl((NamedNode) typeName);
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Check that this object whose label we are inspecting is a node
        switch (subCtx.labelObject.category) {
        case CG_NETLIST:
        case CG_PROCESS:
        case CG_MEDIUM:
        case CG_SM:
        case CG_SCHEDULER:
            break;
        default:
            _error(ctx, "attempting to access label '"
                    + label.getName().getIdent()
                    + "' of an object that is not a node");
        }
        label = (LocalLabelNode) label.accept(this, childArgs);
        node.setLabel(label);

        return node;
    }

    // FIXME: add visit() method for anonymous classes

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Field used to perform analysis of types in the AST. It provides a mapping
     * from types to integer identifiers.
     */
    protected final TypeIdentifier _typeID;

    /**
     * Field used to perform analysis of types in the AST. It provides a default
     * policy to compare types.
     */
    protected final TypePolicy _typePolicy;

    /**
     * Field used to perform analysis of types in the AST. This visitor
     * traverses expressions setting type of the expression in a property of the
     * node.
     */
    protected final TypeVisitor _typeVisitor;

    /** True if this visitor is a template field access. */
    protected boolean _isTemplateFieldAccess = false;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // We use the defaultVisit() defined in ReplacementNode

    /**
     * Visit the declaration of a user defined type, resolving accesses to
     * fields and methods inside it. Traverse all the members inside the class,
     * registering the declaration of the class.
     *
     * @param node
     *            The user-defined type being visited.
     * @param args
     *            List of arguments with context information.
     * @return This node.
     */
    public Object _visitUserTypeDeclNode(UserTypeDeclNode node, LinkedList args) {
        // Build context for members
        FieldContext ctx = (FieldContext) args.get(0);
        FieldContext subCtx = (FieldContext) ctx.clone();
        subCtx.currentObject = (ObjectDecl) MetaModelDecl.getDecl(node
                .getName());
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        TNLManip.traverseList(this, childArgs, node.getMembers());

        return node;
    }

    /**
     * Compare two method signatures and return true if the signature of first
     * method is more specific that the signature of the second. A signature s1
     * is more specific than a signature s2 if s1 could be used where s1 is used
     * without a compile-time error.
     *
     * @param m1
     *            Declaration of the first method.
     * @param m2
     *            Declaration of the second method.
     * @return true iff the signature of method m1 is more specific that the
     *         signature of m2. That is false is returned in case m2 is more
     *         specific that m1 or if none of the methods is more specific that
     *         the other.
     */
    protected boolean _isMoreSpecific(MethodDecl m1, MethodDecl m2) {
        List params1 = m1.getParams();
        List params2 = m2.getParams();
        if (params1.size() != params2.size())
            return false;

        // Method m1 (class C1) is more specific than method m2 (class C2)
        // if and only if:
        // - C1 can be converted to C2 using method invocation conversion
        // - Each parameter of m1 can be converted to a parameter of m2
        // by method invocation conversion.

        ObjectDecl container1 = (ObjectDecl) m1.getContainer();
        ObjectDecl container2 = (ObjectDecl) m2.getContainer();
        TypeNode type1 = container1.getDefType();
        TypeNode type2 = container2.getDefType();

        // Check classes where methods are declared
        if (!_typePolicy.canPassParameter(type1, type2))
            return false;

        Iterator iter1 = params1.iterator();
        Iterator iter2 = params2.iterator();

        while (iter1.hasNext()) {
            FormalParameterDecl param1 = (FormalParameterDecl) iter1.next();
            FormalParameterDecl param2 = (FormalParameterDecl) iter2.next();
            type1 = param1.getType();
            type2 = param2.getType();
            // Check parameters of methods
            if (!_typePolicy.canPassParameter(type1, type2))
                return false;
        }
        return true;
    }

    /**
     * Return true if a given method can be called with a given list of real
     * arguments. If the signature of the method does not match with the types
     * of the parameters, return false.
     *
     * @param method
     *            Declaration of the candidate method.
     * @param argTypes
     *            List of types of the arguments of the call.
     * @return true if the method can be called with a list of arguments with
     *         this given type.
     */
    protected boolean _isCallableWith(MethodDecl method, List argTypes) {
        List params = method.getParams();

        if (argTypes.size() != params.size())
            return false;

        Iterator paramItr = params.iterator();
        Iterator argItr = argTypes.iterator();

        while (paramItr.hasNext()) {
            TypeNode argType = (TypeNode) argItr.next();
            FormalParameterDecl param = (FormalParameterDecl) paramItr.next();
            TypeNode type = param.getType();
            if (!_typePolicy.canPassParameter(argType, type))
                return false;
        }
        return true;
    }

    /**
     * Get the method/constructor called in a method invocation expression,
     * considering the signature of the method and the list of types used in the
     * call.
     *
     * @param ctx
     *            Context where this call is performed.
     * @param methods
     *            Iterator through all methods with correct category, correct
     *            name, and visible in this point of the program.
     * @param args
     *            List of arguments used in the call to the method.
     * @return The declaration of the method with the most specific signature
     *         (according to the parameter list) within those selected by the
     *         iterator methods.
     * @exception RuntimeException
     *                if no accessible method can be called with the given
     *                argument list or if the call is ambiguous (i.e. there are
     *                several "most specific" methods").
     */
    protected MethodDecl _resolveCall(FieldContext ctx, ScopeIterator methods,
            List args) {

        MethodDecl aMethod = (MethodDecl) methods.peek();
        MethodDecl possible = null;

        // Build a list with the type of the arguments
        LinkedList argTypes = new LinkedList();
        Iterator iter = args.iterator();
        boolean typeParameterArg = false;
        while (iter.hasNext()) {
            ExprNode expr = (ExprNode) iter.next();
            argTypes.addLast(_typeVisitor.type(expr));
            if (_typePolicy.isTypeParameter(_typeVisitor.type(expr)))
                typeParameterArg = true;
        }

        // Build a list with the methods whose signature allows them
        // to be called using this list of arguments
        LinkedList matches = new LinkedList();
        while (methods.hasNext()) {
            MethodDecl method = (MethodDecl) methods.next();
            if (!ctx.currentObject.hasAccess(method)) {
                possible = method;
                continue;
            }
            if (_isCallableWith(method, argTypes))
                matches.addLast(method);
        }

        // Check if there are any possible methods!
        if (matches.size() == 0) {
            String str = argListToString(argTypes);
            str = " there are methods with name '" + aMethod.getName()
                    + "', but none of them can be " + "called with signature "
                    + aMethod.getName() + str;
            if (possible != null) {
                ObjectDecl container = (ObjectDecl) possible.getContainer();
                str = str + ". Maybe you were calling method "
                        + _typePolicy.toString(possible, true) + " of "
                        + _typePolicy.toString(container, true) + "?"
                        + " The visibility of this method does not allow"
                        + " it to be called here.";
            } else {
                ObjectDecl container = (ObjectDecl) aMethod.getContainer();
                str = str + ". Maybe you were calling method "
                        + _typePolicy.toString(aMethod, true) + " of "
                        + _typePolicy.toString(container, true) + "?"
                        + " Check the number of arguments and its types.";
            }
            _error(ctx, str);
        }

        if (matches.size() == 1)
            return ((MethodDecl) matches.getFirst());

        if (_isTemplateFieldAccess)
            return ((MethodDecl) matches.getFirst());

        // Check if there is only one "most specific" method that
        // can be called with this argument list
        Iterator iter1 = matches.iterator();
        MethodDecl m1 = null, m2 = null;
        while (iter1.hasNext()) {
            m1 = (MethodDecl) iter1.next();
            Iterator iter2 = matches.iterator();
            boolean thisOne = true;
            while (iter2.hasNext()) {
                m2 = (MethodDecl) iter2.next();
                if (m1 == m2)
                    continue;
                if (!_isMoreSpecific(m1, m2) || (_isMoreSpecific(m2, m1))) {
                    thisOne = false; // keep looking
                    continue;
                }
            }
            if (thisOne)
                return m1;
        }

        // if there is an argument that is a type parameter, pick the last one
        if (typeParameterArg && m1 != null)
            return m1;

        // If we reach this point is because the call is ambiguous
        String str = argListToString(argTypes);
        str = " ambiguous call to " + aMethod.getName() + str;
        str = str + ". There are several methods that can be called with "
                + "this signature, and none is more specific than the others.";
        _error(ctx, str);

        // Never reached
        return null;
    }

    /**
     * Resolve an access to an attribute of an object (field/port/parameter) .
     * If the name can't be found or the attribute is not accessible, throw a
     * RuntimeException. Otherwise set the declaration of the attribute in the
     * node of the tree that contains the name.
     *
     * @param node
     *            Node of the AST where the field is accessed.
     * @param ctx
     *            Context where the access is performed.
     * @param mask
     *            Valid categories that can be assumed by the name.
     * @exception RuntimeException
     *                if the attribute does not exist, or it is not accessible.
     */
    protected void _resolveFieldName(FieldAccessNode node, FieldContext ctx,
            int mask) {
        // Check if the node has already been resolved
        MetaModelDecl decl = MetaModelDecl.getDecl((NamedNode) node);
        if (decl != null)
            return; // This has already been resolved

        // Get the declaration of the object/array where the method
        // is declared
        ObjectDecl object = null;
        String ident = node.getName().getIdent();
        TypeNode type = _typeVisitor.accessedObjectType(node);
        if (type == NullTypeNode.instance) {
            _error(ctx, "attempting to access field/method '" + ident
                    + "' of something with null type");
        } else if (_typePolicy.isArrayType(type)) {
            object = MetaModelLibrary.ARRAY_CLASS_DECL;
        } else if (_typePolicy.isReferenceType(type)) {
            // Type must be a name of an object class
            TypeNameNode typeName = (TypeNameNode) type;
            object = (ObjectDecl) MetaModelDecl.getDecl(typeName.getName());
        } else {
            _error(ctx, "attempting to select field/method '" + ident
                    + "' of something not an object or array: "
                    + _typePolicy.toString(type, true));
        }

        if (object == null)
            return;

        if (!object.getTypeParams().isEmpty())
            _isTemplateFieldAccess = true;

        // Resolve the name
        // It can be a method or a field/port/parameter
        ScopeIterator resolutions;
        Scope scope = object.getScope();
        if (ctx.methodArgs != null) {
            // Name refers to a method
            resolutions = scope.lookupFirstLocal(ident, CG_METHOD);
            if (!resolutions.hasNext()) {
                _error(ctx, "there are no methods with name '" + ident
                        + "' in " + _typePolicy.toString(object, true));
            }
            decl = _resolveCall(ctx, resolutions, ctx.methodArgs);
        } else {
            // Name refers to a field/port/parameter
            resolutions = scope.lookupFirstLocal(ident, CG_FIELD | CG_PORT
                    | CG_PARAMETER);
            if (!resolutions.hasNext()) {
                _error(ctx, "there is no field/port/parameter with name '"
                        + ident + "' in " + _typePolicy.toString(object, true));
            }
            decl = (MetaModelDecl) resolutions.nextDecl();
            if (resolutions.hasNext()) {
                _error(ctx, "ambiguous reference to field/port/parameter "
                        + " with name '" + ident + "'");
            }
        }

        _isTemplateFieldAccess = false;

        // Store the resolved declaration
        node.getName().setProperty(DECL_KEY, decl);
    }

    /**
     * Resolve an access to a port name. This name should be resolved in the
     * context of the type of a Node object instead of the current context.
     *
     * @param node
     *            Node that has this port.
     * @param expr
     *            Expression holding the port access.
     * @param args
     *            List of arguments of the visit.
     * @return The expression that replaces the original access to the port.
     */
    protected ExprNode _resolvePortAccess(ExprNode node, ExprNode expr,
            LinkedList args) {

        // Check if the port name is specified as a single name. If it
        // is, it should be checked as a port of the source object,
        // rather than an independent expression.
        FieldContext ctx = null;
        if (expr instanceof ObjectNode) {
            ObjectNode portName = (ObjectNode) expr;
            NameNode name = portName.getName();
            if (name.getQualifier() != AbsentTreeNode.instance) {
                expr = (ExprNode) expr.accept(this, args);
            } else if (node instanceof ObjectNode) {
                ctx = (FieldContext) args.get(0);
                ObjectFieldAccessNode access = new ObjectFieldAccessNode(name,
                        node);
                _resolveFieldName(access, ctx, CG_PORT);
                portName.setName(name);
            }
        } else if (expr instanceof ArrayAccessNode) {
            ArrayAccessNode arrayAccess = (ArrayAccessNode) expr;
            // Resolve the index
            ExprNode index = arrayAccess.getIndex();
            index = (ExprNode) index.accept(this, args);
            arrayAccess.setIndex(index);
            // Resolve the array
            ExprNode array = arrayAccess.getArray();
            if (array instanceof ObjectNode) {
                ObjectNode arrayName = (ObjectNode) array;
                NameNode name = arrayName.getName();
                if (name.getQualifier() != AbsentTreeNode.instance) {
                    array = (ExprNode) array.accept(this, args);
                    arrayAccess.setArray(array);
                } else {
                    ctx = (FieldContext) args.get(0);
                    ObjectFieldAccessNode access = new ObjectFieldAccessNode(
                            name, node);
                    _resolveFieldName(access, ctx, CG_PORT);
                    arrayName.setName(name);
                }
            } else {
                array = (ExprNode) array.accept(this, args);
                arrayAccess.setArray(array);
            }
        } else {
            expr = (ExprNode) expr.accept(this, args);
        }

        return expr;
    }

    /**
     * Return a String that represents the list of types used in a call to a
     * method. This string will be used to print error messages.
     *
     * @param types
     *            List of types used in the call to a method.
     * @return A String representation of the list of types.
     */
    protected String argListToString(List types) {
        Iterator iter = types.iterator();
        String str = "(";
        while (iter.hasNext()) {
            TypeNode type = (TypeNode) iter.next();
            str = str + _typePolicy.toString(type, false);
            if (iter.hasNext())
                str = str + ", ";
        }
        str = str + ")";
        return str;
    }

    /**
     * Emit an error message and produce a run-time exception.
     *
     * @param ctx
     *            Context where the error is found.
     * @param msg
     *            Error message.
     * @exception RuntimeException
     *                notifying the error.
     */
    protected void _error(FieldContext ctx, String msg) {
        String errorMsg = "Error in ";
        if (ctx.currentMethod != null) {
            errorMsg = errorMsg + "method "
                    + _typePolicy.toString(ctx.currentMethod, true) + " of ";
        }
        if (ctx.currentObject != null) {
            errorMsg += _typePolicy.toString(ctx.currentObject, true) + ", ";
        }
        errorMsg = errorMsg + msg;
        throw new RuntimeException(errorMsg);
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    // /////////////////////////////////////////////////////////////////
    // // inner classes ////

    /**
     * Class used to check access of fields and methods. This class keeps track
     * of context information like the current object, if we are inside a static
     * block of code, or the list of arguments of a method call.
     */
    protected static class FieldContext implements Cloneable {

        /** Create a new empty FieldContext. */
        public FieldContext() {
        }

        /**
         * Create a shallow copy of a FieldContext. Information inside the
         * FieldContext is not cloned as well.
         */
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalError("clone of FieldContext not suported");
            }
        }

        /** The declaration representing the class we are currently in. */
        public ObjectDecl currentObject = null;

        /** The declaration of the method we are currently in. */
        public MethodDecl currentMethod = null;

        /** A flag indicating that we are in static code. */
        public boolean inStatic = false;

        /**
         * Object where the label has to be resolved, if it has to be resolved
         * in an object which is not current object.
         */
        public ObjectDecl labelObject = null;

        /** A list of arguments of the last method call. */
        public List methodArgs = null;
    }

}
