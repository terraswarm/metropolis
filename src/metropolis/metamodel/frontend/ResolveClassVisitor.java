/* A visitor that traverses the declarations of classes, creating Decls for
 fields, constructors and methods, and adding them to their enclosing class
 scope.

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

import metropolis.metamodel.Decl;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.NullValue;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ActionLabelStmtNode;
import metropolis.metamodel.nodetypes.AllocateAnonymousClassNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ConstraintDeclNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.ELOCConstraintDeclNode;
import metropolis.metamodel.nodetypes.FieldDeclNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.LOCConstraintDeclNode;
import metropolis.metamodel.nodetypes.LTLConstraintDeclNode;
import metropolis.metamodel.nodetypes.LabeledBlockNode;
import metropolis.metamodel.nodetypes.LabeledStmtNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ParameterDeclNode;
import metropolis.metamodel.nodetypes.ParameterNode;
import metropolis.metamodel.nodetypes.PortDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.nodetypes.VoidTypeNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ResolveClassVisitor
/**
 * A visitor that traverses a file, focusing in the declarations of classes of
 * objects (class, interface, process, medium...). In each of these classes, the
 * visitor creates a declaration for each member (port, parameter, field, etc.)
 * and adds this declaration to the Scope of the enclosing class.
 * <p>
 * The return value of the visit methods is ignored, so 'null' can be returned
 * safely.
 * <p>
 * As only few nodes will be traversed by this visitor, the traversal method is
 * set to TM_CUSTOM.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ResolveClassVisitor.java,v 1.32 2006/10/12 20:34:05 cxh Exp $
 */
public class ResolveClassVisitor extends ResolutionVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new visitor that will traverse class declarations adding
     * annotations for each member of the class. Set the traversal method to
     * TM_CUSTOM;
     */
    public ResolveClassVisitor() {
        super(TM_CUSTOM);
        _typePolicy = new TypePolicy();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit a compile unit of a meta-model program. Traverse all the
     * user-defined types in this file performing class resolution.
     *
     * @param node
     *            The compile unit being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        if (args != null && args.size() > 0 && args.getLast() instanceof String
                && ((String) args.getLast()).equals("template")) {
            args.removeLast();
            _isTemplateResolution = true;
        }

        // Initialize argument list for the children
        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // enclosing class decl

        // Traverse classes of the compile unit
        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        return null;
    }

    // Visit the declaration of the diffent classes

    /**
     * Visit the declaration of an interface. Traverse all the members and inner
     * classes of this interface, creating declarations for each of them. Check
     * correctness of superinterfaces, as members will be checked in their
     * visit() methods.
     *
     * @param node
     *            The interface declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {

        InterfaceDecl thisDecl = (InterfaceDecl) MetaModelDecl
                .getDecl((NamedNode) node);

        int modifiers = thisDecl.getModifiers();
        modifiers |= ABSTRACT_MOD;
        thisDecl.setModifiers(modifiers);

        // Find the outer class, if applicable
        if (args.get(0) instanceof InterfaceDecl) {
            // Inner interfaces of an interface are static + public.
            modifiers = thisDecl.getModifiers();
            modifiers |= PUBLIC_MOD | STATIC_MOD;
            thisDecl.setModifiers(modifiers);
        }

        if (_isTemplateResolution) {
            Iterator declIter = thisDecl.getScope().getDecls().iterator();
            while (declIter.hasNext()) {
                MetaModelDecl decl = (MetaModelDecl) declIter.next();
                if ((decl instanceof ObjectDecl)
                        || (decl instanceof TypeParameterDecl))
                    continue;
                declIter.remove();
            }
            thisDecl.clearVisitors();
        }

        // Record the fact that we are modifying the Decl.
        // Exit if we have already done class resolution
        if (!thisDecl.addVisitor(_myClass))
            return null;

        // Register and check super-interfaces.
        _addSuperInterfaces(thisDecl, node);

        // Initialize arguments for the children of this node
        LinkedList childArgs = new LinkedList();
        childArgs.add(thisDecl); // Outer class = this

        // Traverse members and inner classes of the interface.
        // Members will add themselves to the scope of the class.
        TNLManip.traverseList(this, childArgs, node.getMembers());

        return null;
    }

    /**
     * Visit the declaration of a class. Traverse all the members and inner
     * classes of this class, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            The class declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, MetaModelLibrary.OBJECT_DECL);
    }

    /**
     * Visit the declaration of a quantity. Traverse all the members and inner
     * classes of this quantity, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            The class declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args,
                MetaModelLibrary.QUANTITY_DECL);
    }

    /**
     * Visit the declaration of a process. Traverse all the members and inner
     * classes of this process, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            The process declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, MetaModelLibrary.PROCESS_DECL);
    }

    /**
     * Visit the declaration of a medium. Traverse all the members and inner
     * classes of this medium, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            The medium declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, MetaModelLibrary.MEDIUM_DECL);
    }

    /**
     * Visit the declaration of a scheduler. Traverse all the members and inner
     * classes of this scheduler, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            The scheduler declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args,
                MetaModelLibrary.SCHEDULER_DECL);
    }

    /**
     * Visit the declaration of a state medium. Traverse all the members and
     * inner classes of this state medium, creating declarations for each of
     * them. Check correctness of superinterfaces and the superclass, as members
     * will be checked in their visit() methods.
     *
     * @param node
     *            The declaration of the state medium.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args,
                MetaModelLibrary.STATEMEDIUM_DECL);
    }

    /**
     * Visit the declaration of a process. Traverse all the members and inner
     * classes of this process, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            The netlist declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, MetaModelLibrary.NETLIST_DECL);
    }

    // Visit the declaration of the different members

    /**
     * Visit a field declaration inside a class. Create the declaration for that
     * field, checking that there are no duplicate names in the class. Add the
     * declaration of the field to the scope of the class. Children of this node
     * don't need to be visited.
     *
     * @param node
     *            The field declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        ObjectDecl objectDecl = (ObjectDecl) args.get(0);

        // Check visibility modifiers
        int modifiers = node.getModifiers();
        if ((modifiers & STATIC_MOD) != 0) {
            if ((modifiers & FINAL_MOD) == 0)
                throw new RuntimeException("Field " + node.getName().getIdent()
                        + " of " + _typePolicy.toString(objectDecl, true)
                        + " is declared static" + " - static fields must be "
                        + "declared final as well.");
        }
        if ((objectDecl.category & CG_NODE) != 0) {
            boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
            boolean isProt = ((modifiers & PROTECTED_MOD) != 0);
            String vis = (isPublic ? " public " : (isProt ? " protected "
                    : null));
            if (vis != null) {
                throw new RuntimeException("Field " + node.getName().getIdent()
                        + " of " + _typePolicy.toString(objectDecl, true)
                        + " is declared" + vis + "- fields of Nodes can "
                        + "only have default or private visibility.");
            }
        }

        // Add default modifiers of the field
        if (objectDecl.category == CG_INTERFACE) {
            // Fields of an interface are always public and final
            modifiers |= FINAL_MOD | PUBLIC_MOD;

            // All fiels of an interface must be initialized
            if (node.getInitExpr() == AbsentTreeNode.instance) {
                throw new RuntimeException("Field " + node.getName().getIdent()
                        + " of " + _typePolicy.toString(objectDecl, true)
                        + " should be initialized "
                        + "- all fields of interfaces are final");
            }
        }

        // Check that the name of the field is unique.
        String ident = node.getName().getIdent();
        Decl d = objectDecl.getAttribute(ident);
        if (d != null) {
            throw new RuntimeException("Redeclaration of name " + ident
                    + " in " + _typePolicy.toString(objectDecl, true));
        }

        // Create the new declaration
        // Add it to the scope of the parent class.
        // Annotate the name with this declaration
        d = new FieldDecl(ident, node.getDefType(), modifiers, node, objectDecl);
        objectDecl.getScope().add(d);
        node.getName().setProperty(DECL_KEY, d);

        // Traverse the init expression of the field (as we have
        // anonymous classes)
        TreeNode initExpr = node.getInitExpr();
        if (initExpr != null) {
            initExpr.accept(this, args);
        }

        return null;
    }

    /**
     * Visit a port declaration inside a class. Create the declaration for that
     * port, checking that there are no duplicate names in the class. Add the
     * declaration of the port to the scope of the class. Children of this node
     * don't need to be visited.
     *
     * @param node
     *            The port declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitPortDeclNode(PortDeclNode node, LinkedList args) {
        ObjectDecl objectDecl = (ObjectDecl) args.get(0);

        // Compute modifiers of the port
        int modifiers = node.getModifiers();

        // Check that the name of the port is unique.
        String ident = node.getName().getIdent();
        Decl d = objectDecl.getAttribute(ident);
        if (d != null) {
            throw new RuntimeException("Redeclaration of name " + ident
                    + " in " + _typePolicy.toString(objectDecl, true));
        }

        // Create the new declaration
        // Add it to the scope of the parent class.
        // Annotate the name with this declaration
        d = new PortDecl(ident, node.getDefType(), modifiers, node, objectDecl);
        objectDecl.getScope().add(d);
        node.getName().setProperty(DECL_KEY, d);

        return null;
    }

    /**
     * Visit a parameter declaration inside a class. Create the declaration for
     * that parameter, checking that there are no duplicate names in the class.
     * Add the declaration of the parameter to the scope of the class. Children
     * of this node don't need to be visited.
     *
     * @param node
     *            The parameter declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitParameterDeclNode(ParameterDeclNode node, LinkedList args) {
        ObjectDecl objectDecl = (ObjectDecl) args.get(0);

        // Compute modifiers of the parameter
        int modifiers = node.getModifiers();

        // Check that the name of the parameter is unique.
        String ident = node.getName().getIdent();
        Decl d = objectDecl.getAttribute(ident);
        if (d != null) {
            throw new RuntimeException("Redeclaration of name " + ident
                    + " in " + _typePolicy.toString(objectDecl, true));
        }

        // Create the new declaration
        // Add it to the scope of the parent class.
        // Annotate the name with this declaration
        d = new ParameterDecl(ident, node.getDefType(), modifiers, node,
                objectDecl);
        objectDecl.getScope().add(d);
        node.getName().setProperty(DECL_KEY, d);

        return null;
    }

    /**
     * Visit a method declaration inside a class. Create the declaration for
     * that method, checking that there are no duplicate names in the class. Add
     * the declaration of the method to the scope of the class. Children of this
     * node don't need to be visited.
     *
     * @param node
     *            The method declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        ObjectDecl objectDecl = (ObjectDecl) args.get(0);

        int modifiers = node.getModifiers();

        // Add default modifiers if necessary
        if ((objectDecl.category & CG_INTERFACE) != 0) {
            modifiers |= PUBLIC_MOD | ABSTRACT_MOD;
        }

        String ident = node.getName().getIdent();

        // Check that abstract methods do not define a method body
        boolean hasBody = (node.getBody() != AbsentTreeNode.instance);
        boolean isAbstract = ((modifiers & ABSTRACT_MOD) != 0);
        if (isAbstract && hasBody) {
            throw new RuntimeException("Error in "
                    + _typePolicy.toString(objectDecl, true) + " : method  "
                    + ident + " is declared abstract, but has "
                    + "a method body");
        } else if (!isAbstract && !hasBody) {
            throw new RuntimeException("Error in "
                    + _typePolicy.toString(objectDecl, true) + " : method  "
                    + ident + " is declared non-abstract, but "
                    + "it does not provide a body");
        }

        // Create declarations for formal parameters.
        List paramList = _createFormalParDecls(objectDecl, ident, node
                .getParams());

        // Create the declaration of the method.
        // Add the declaration of the method to the scope of the class.
        MethodDecl d = new MethodDecl(ident, node.getReturnType(), modifiers,
                node.getEffect(), node, objectDecl, paramList, new LinkedList());
        objectDecl.getScope().add(d);
        node.getName().setProperty(DECL_KEY, d);

        // Check visibility modifiers
        if ((objectDecl.category & CG_NODE) != 0) {
            boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
            boolean isProt = ((modifiers & PROTECTED_MOD) != 0);
            boolean isElaborate = ((modifiers & ELABORATE_MOD) != 0);
            //String vis = (isPublic ? " public " : (isProt ? " protected "
            //        : null));
            boolean hasEffect = (d.getEffect() != NO_EFFECT);
            if (isProt) {
                throw new RuntimeException("In "
                        + _typePolicy.toString(objectDecl, true) + ", method "
                        + _typePolicy.toString(d, true)
                        + " is declared protected"
                        + " - methods in Nodes cannot have this visibility");
                // YW: 091703: beg
                // } else if (!isPublic && hasEffect) {
            } else if (!isPublic && hasEffect && !isElaborate) {
                /*
                 * throw new RuntimeException("In " +
                 * _typePolicy.toString(objectDecl,true) + ", method " +
                 * _typePolicy.toString(d, true) + " is declared public" + " -
                 * methods in Nodes can only be public if they" + " have an
                 * effect (eval, update, constant).");
                 */
                throw new RuntimeException(
                        "In "
                                + _typePolicy.toString(objectDecl, true)
                                + ", method "
                                + _typePolicy.toString(d, true)
                                + " has an effect (eval, update, constant)"
                                + " - methods in Nodes can have such an effect only if they"
                                + " are declared public.");
                // YW: 091703: end

            }
        }

        // Traverse the body of the method (as we have to resolve
        // labels and anonymous classes)
        LinkedList childArgs = new LinkedList();
        childArgs.add(objectDecl); // enclosing class declaration
        childArgs.add(d); // enclosing method declaration
        node.getBody().accept(this, childArgs);

        return null;
    }

    /**
     * Visit a constructor declaration inside a class. Create the declaration
     * for that constructor, checking that there are no duplicate names in the
     * class. Add the declaration of the constructor to the scope of the class.
     * Children of this node don't need to be visited.
     *
     * @param node
     *            The constructor declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        ObjectDecl objectDecl = (ObjectDecl) args.get(0);

        // Check that the name of the constructor is the same as the name
        // of the enclosing class.
        String ident = node.getName().getIdent();
        if (!ident.equals(objectDecl.getName())) {
            throw new RuntimeException(_typePolicy.toString(objectDecl, true)
                    + " declares a constructor with name " + ident);
        }

        // Create declarations for formal parameters.
        List paramList = _createFormalParDecls(objectDecl, ident, node
                .getParams());

        // Create the method declaration.
        // Add it to the scope of the parent class.
        int modifiers = node.getModifiers();
        MethodDecl d = new MethodDecl(ident, objectDecl.getDefType(),
                modifiers, NO_EFFECT, node, objectDecl, paramList,
                new LinkedList());
        objectDecl.getScope().add(d);
        node.getName().setProperty(DECL_KEY, d);

        // Traverse the body of the constructor (as we have to
        // resolve labels and anonymous classes)
        LinkedList childArgs = new LinkedList();
        childArgs.add(objectDecl); // enclosing class declaration
        childArgs.add(d); // enclosing method declaration
        node.getBody().accept(this, childArgs);

        return null;
    }

    /**
     * Visit a labeled statement. Create a new declaration for this label, and
     * check uniqueness among the labels of this method.
     *
     * @param node
     *            The labeled statement being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        _createLabel(node, node.getLabel().getName(), args, false);
        // Traverse statement
        node.getStmt().accept(this, args);
        return null;
    }

    /**
     * Visit a labeled block. Create a new declaration of this label, and check
     * uniqueness among the labels of this method.
     *
     * @param node
     *            The labeled block being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        _createLabel(node, node.getLabel().getName(), args, true);
        // Traverse statements in the block
        TNLManip.traverseList(this, args, node.getStmts());
        return null;

    }

    /**
     * Visit an action label statement block. Create a new declaration of this
     * label, and check uniqueness among the labels of this method.
     *
     * @param node
     *            The labeled statement block being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        _createLabel(node, node.getLabel().getName(), args, true);
        // Traverse statements in the block
        TNLManip.traverseList(this, args, node.getStmts());
        return null;

    }

    /**
     * Visit a node that allocates an anonymous class.
     *
     * @param node
     *            The anonymous class being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitAllocateAnonymousClassNode(
            AllocateAnonymousClassNode node, LinkedList args) {

        // FIXME: Fill in this method
        // Fill information about the anonymous class declaration

        return null;
    }

    /**
     * Get the Class object for this visitor. This Class object will be used in
     * methods from TrackedPropertyMap to check if a node was visited by a
     * ResolveClassVisitor.
     *
     * @return The Class object for this visitor.
     */
    public static Class visitorClass() {
        return _myClass;
    }

    /**
     * Visit a loc constraint formula declaration. Create the declaration for
     * that formula (like a method), checking that there are no duplicate names
     * in the class. Add the declaration of the formula to the scope of the
     * class. Children of this node don't need to be visited.
     *
     * @param node
     *            The formula declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitLOCConstraintDeclNode(LOCConstraintDeclNode node,
            LinkedList args) {
        return _visitConstraintDeclNode(node, args);
    }

    /**
     * Visit a eloc constraint formula declaration. Create the declaration for
     * that formula (like a method), checking that there are no duplicate names
     * in the class. Add the declaration of the formula to the scope of the
     * class. Children of this node don't need to be visited.
     *
     * @param node
     *            The formula declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitELOCConstraintDeclNode(ELOCConstraintDeclNode node,
            LinkedList args) {
        return _visitConstraintDeclNode(node, args);
    }

    /**
     * Visit a ltl constraint formula declaration. Create the declaration for
     * that formula (like a method), checking that there are no duplicate names
     * in the class. Add the declaration of the formula to the scope of the
     * class. Children of this node don't need to be visited.
     *
     * @param node
     *            The formula declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    public Object visitLTLConstraintDeclNode(LTLConstraintDeclNode node,
            LinkedList args) {
        return _visitConstraintDeclNode(node, args);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Type policy used to detect circular inheritance. */
    protected final TypePolicy _typePolicy;

    /**
     * A flag indicating that if the visitor is called again after template
     * resolution.
     */
    protected boolean _isTemplateResolution = false;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // We use the _defaultVisit() from ResolutionVisitor

    /**
     * Visit a constraint formula declaration. Create the declaration for that
     * formula (like a method), checking that there are no duplicate names in
     * the class. Add the declaration of the formula to the scope of the class.
     * Children of this node don't need to be visited.
     *
     * @param node
     *            The formula declaration being visited.
     * @param args
     *            List of arguments of the visit.
     * @return null (not used).
     */
    protected Object _visitConstraintDeclNode(ConstraintDeclNode node,
            LinkedList args) {
        ObjectDecl objectDecl = (ObjectDecl) args.get(0);

        int modifiers = PROTECTED_MOD;

        String ident = node.getName().getIdent();

        //boolean hasBody = true;
        //boolean isAbstract = false;

        // Create declarations for formal parameters.
        List paramList = _createFormalParDecls(objectDecl, ident, node
                .getParams());

        // Create the declaration of the formula.
        // Add the declaration of the formula to the scope of the class.
        MethodDecl d = new MethodDecl(ident, VoidTypeNode.instance, modifiers,
                NO_EFFECT, node, objectDecl, paramList, new LinkedList());
        objectDecl.getScope().add(d);
        node.getName().setProperty(DECL_KEY, d);

        // Traverse the body of the method (as we have to resolve
        // labels and anonymous classes)
        LinkedList childArgs = new LinkedList();
        childArgs.add(objectDecl); // enclosing class declaration
        childArgs.add(d); // enclosing method declaration
        node.getFormula().accept(this, childArgs);

        return null;
    }

    /**
     * Visit a declaration of a user-defined type. Traverse all the members and
     * inner classes of this type, creating declarations for each of them. Check
     * correctness of superinterfaces and the superclass, as members will be
     * checked in their visit() methods.
     *
     * @param node
     *            Node that contains the declaration of the type.
     * @param args
     *            List of arguments of the visit.
     * @param defaultSuper
     *            Default superclass for this kind of objects, e.g. Process for
     *            processes, Object for classes, etc.
     * @return null (not used).
     */
    protected Object _visitUserTypeDeclNode(ClassDeclNode node,
            LinkedList args, ObjectDecl defaultSuper) {

        ObjectDecl thisDecl = (ObjectDecl) MetaModelDecl
                .getDecl((NamedNode) node);

        // Find the outer class, if applicable
        if (args.get(0) instanceof InterfaceDecl) {
            // Inner classes of an interface are always static and
            int modifiers = thisDecl.getModifiers();
            modifiers |= PUBLIC_MOD | STATIC_MOD;
            thisDecl.setModifiers(modifiers);
        }

        if (_isTemplateResolution) {
            Iterator declIter = thisDecl.getScope().getDecls().iterator();
            while (declIter.hasNext()) {
                MetaModelDecl decl = (MetaModelDecl) declIter.next();
                if ((decl instanceof ObjectDecl)
                        || (decl instanceof TypeParameterDecl))
                    continue;
                declIter.remove();
            }
            thisDecl.clearVisitors();
        }

        // Record the fact that we are modifying the Decl.
        // Exit if we have already done class resolution
        if (!thisDecl.addVisitor(_myClass))
            return null;

        // Register and check super-classes.
        _addSuperClass(thisDecl, node, defaultSuper);

        // Register and check super-interfaces.
        _addSuperInterfaces(thisDecl, node);

        // Initialize arguments for the children of this node
        LinkedList childArgs = new LinkedList();
        childArgs.add(thisDecl); // Outer class = this

        // Traverse members and inner classes of the interface.
        // Members will add themselves to the scope of the class.
        TNLManip.traverseList(this, childArgs, node.getMembers());

        return null;
    }

    /**
     * Add the declaration of a superclass to a given ObjectDel.
     *
     * @param decl
     *            ObjectDecl declaring the class.
     * @param node
     *            Node of the tree where the declaration is stored.
     * @param defaultSuper
     *            Default superclass for this kind of objects, e.g. Process for
     *            processes, Object for classes, etc.
     * @exception RuntimeException
     *                if the superclass is a subclass of current class, or it is
     *                a 'final' class, or it is has an incompatible category.
     */
    protected void _addSuperClass(ObjectDecl decl, ClassDeclNode node,
            ObjectDecl defaultSuper) {

        TreeNode superTree = node.getSuperClass();

        // Special case: class Object in the meta-model library
        if ((decl.getContainer() == MetaModelLibrary.LANG_PACKAGE)
                && (decl.getName().equals("Object"))) {
            if (superTree != AbsentTreeNode.instance) {
                throw new RuntimeException("Error in meta-model library: "
                        + "class Object shouldn't extend any other class");
            }
            decl.setSuperClass(null);
            return;
        }

        // Rest of cases: superclass can be stated explicitly or not
        ObjectDecl superDecl;
        if (superTree == AbsentTreeNode.instance) {
            superDecl = defaultSuper;
        } else {
            superDecl = (ObjectDecl) MetaModelDecl.getDecl(superTree);
        }

        // Check that the category of the superclass is the same as the
        // category of the superclass; the only exception to this is if
        // we are in class Process, Medium... of the meta-model library.
        // This classes extend class Node, which is a class.
        if (decl.category != superDecl.category) {
            if ((superDecl != MetaModelLibrary.NODE_DECL)
                    || ((decl != MetaModelLibrary.PROCESS_DECL)
                            && (decl != MetaModelLibrary.SCHEDULER_DECL)
                            && (decl != MetaModelLibrary.MEDIUM_DECL)
                            && (decl != MetaModelLibrary.STATEMEDIUM_DECL) && (decl != MetaModelLibrary.NETLIST_DECL))) {
                // Check if we are still loading the library 'metamodel.lang'.
                // If that is the case, skip this test.
                if ((MetaModelLibrary.NODE_DECL != null)
                        && (MetaModelLibrary.PROCESS_DECL != null)
                        && (MetaModelLibrary.SCHEDULER_DECL != null)
                        && (MetaModelLibrary.MEDIUM_DECL != null)
                        && (MetaModelLibrary.STATEMEDIUM_DECL != null)
                        && (MetaModelLibrary.NETLIST_DECL != null)) {
                    throw new RuntimeException(_typePolicy.toString(decl, true)
                            + " cannot extend "
                            + _typePolicy.toString(superDecl, true)
                            + " - they should be objects of the same kind");
                }
            }
        }

        // Check that the super class is not a final class
        int modifiers = superDecl.getModifiers();
        if ((modifiers & FINAL_MOD) != 0) {
            throw new RuntimeException(_typePolicy.toString(decl, true)
                    + " cannot extend " + _typePolicy.toString(superDecl, true)
                    + " - superclass is declared final");
        }

        // Detect circular inheritance.
        if (_typePolicy.isSubClass(superDecl, decl)) {
            throw new RuntimeException(_typePolicy.toString(decl, true)
                    + " cannot extend " + _typePolicy.toString(superDecl, true)
                    + " - circular inheritance!");
        }

        decl.setSuperClass(superDecl);
    }

    /**
     * Add the list of declarations of implemented interfaces of a
     * UserTypeDeclNode to the given ObjectDecl.
     *
     * @param decl
     *            ObjectDecl declaring the class.
     * @param node
     *            Node of the tree where the declaration is stored.
     * @exception RuntimeException
     *                if any of the names declared in the list of implemented
     *                interfaces is not an interface, or if there are duplicate
     *                names, or if this in a superinterface of any of the
     *                interfaces in the 'implements' clause.
     */
    protected void _addSuperInterfaces(ObjectDecl decl, UserTypeDeclNode node) {
        LinkedList declInterfaces = new LinkedList();
        Iterator iter = node.getInterfaces().iterator();

        // Check all interfaces in the 'implements' clause one by one.
        while (iter.hasNext()) {
            NamedNode intfNode = (NamedNode) iter.next();
            MetaModelDecl intf = MetaModelDecl.getDecl(intfNode);

            if ((intf.category & CG_INTERFACE) == 0) {
                throw new RuntimeException(_typePolicy.toString(decl, true)
                        + " cannot implement "
                        + _typePolicy.toString(intf, true)
                        + " - it should be an interface");
            }

            // If this is a template interface, checking for duplicates will
            // be postponed until the template is resolved
            if (((ObjectDecl) intf).getTypeParams().isEmpty())
                if (declInterfaces.contains(intf)) {
                    throw new RuntimeException(_typePolicy.toString(intf, true)
                            + " appears twice in the 'implements' clause of "
                            + _typePolicy.toString(decl, true));
                }

            // If this is an interface, check that there is no circular
            // inheritance, i.e. decl is a superinterface of one of its
            // superinterfaces.
            if (decl.category == CG_INTERFACE) {
                if (_typePolicy.isSuperInterface(decl, (ObjectDecl) intf)) {
                    throw new RuntimeException(_typePolicy.toString(decl, true)
                            + " cannot implement "
                            + _typePolicy.toString(intf, true)
                            + " - circular inheritance!");
                }
            }

            declInterfaces.addLast(intf);
        }

        // All interfaces implement Interface by default.
        // Add the declaration of Interface to the implement list if
        // necessary (i.e. this is not Interface and Interface does
        // not appear in the list of implemented interfaces).
        InterfaceDecl superIf = MetaModelLibrary.INTERFACE_DECL;
        if ((decl.category == CG_INTERFACE) && (superIf != null)) {
            if ((decl != superIf) && (!declInterfaces.contains(superIf))) {
                declInterfaces.addLast(MetaModelLibrary.INTERFACE_DECL);
            }
        }

        decl.setInterfaces(declInterfaces);
    }

    /**
     * Create the declarations for the list of formal parameters of a method or
     * constructor. Return a list with this declarations, checking that names
     * are unique.
     *
     * @param object
     *            Class where the method is found.
     * @param method
     *            Name of the method/constructor.
     * @param params
     *            List of nodes declaring the parameters.
     * @return A list of FormalParameterDecl objects.
     * @exception RuntimeException
     *                if two parameters of this method or constructor have the
     *                same name.
     */
    public List _createFormalParDecls(ObjectDecl object, String method,
            List params) {
        ArrayList parDecls = new ArrayList();
        HashSet names = new HashSet();
        Iterator iter = params.iterator();
        while (iter.hasNext()) {

            ParameterNode node = (ParameterNode) iter.next();

            // Create the parameter declaration
            String ident = node.getName().getIdent();
            int modifiers = node.getModifiers();
            TypeNode type = node.getDefType();
            FormalParameterDecl decl = new FormalParameterDecl(ident, type,
                    modifiers, node);
            parDecls.add(decl);

            // Check that the name of the parameter is unique
            if (names.contains(ident)) {
                throw new RuntimeException("In method " + method + " of "
                        + _typePolicy.toString(object, true) + " : parameter "
                        + ident + " redeclared");
            } else {
                names.add(ident);
            }
        }
        parDecls.trimToSize();
        return parDecls;
    }

    /**
     * Create a declaration for a new labeled statement. Add this declaration to
     * the scope of the current method, checking uniqueness of names inside the
     * method. Set the declaration of the label in the DECL_KEY property of its
     * NameNode.
     *
     * @param node
     *            Node of the AST where the label is declared.
     * @param name
     *            NameNode of the label.
     * @param args
     *            List of arguments of the visit method.
     * @param isGlobal
     *            Flag indicating if the label will be visible outside the class
     *            where it is declared.
     * @exception RuntimeException
     *                if the label is redeclared in this method.
     */
    protected void _createLabel(TreeNode node, NameNode name, LinkedList args,
            boolean isGlobal) {
        ObjectDecl object = (ObjectDecl) args.get(0);
        MethodDecl method = (MethodDecl) args.get(1);
        String ident = name.getIdent();
        // Check uniqueness
        if (method.getLabel(ident) != null) {
            TypePolicy tp = new TypePolicy();
            throw new RuntimeException("Error in method "
                    + tp.toString(method, true) + " of "
                    + tp.toString(object, true) + ", label '" + ident
                    + "' redeclared - in the meta-model, labels must"
                    + " be unique in an object");
        }
        // Create the new declaration
        StmtLblDecl decl = new StmtLblDecl(ident, node, method, isGlobal);
        method.addLabel(decl);
        name.setProperty(DECL_KEY, decl);
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /** The Class object for this visitor. */
    private static Class _myClass = new ResolveClassVisitor().getClass();

}
