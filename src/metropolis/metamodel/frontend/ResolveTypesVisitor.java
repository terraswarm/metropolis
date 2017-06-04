/* A meta-model visitor that resolves type names.

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
import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.GetConnectionNumNode;
import metropolis.metamodel.nodetypes.GetNthConnectionPortNode;
import metropolis.metamodel.nodetypes.GetNthConnectionSrcNode;
import metropolis.metamodel.nodetypes.GetNthPortNode;
import metropolis.metamodel.nodetypes.GetProcessNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ResolveTypesVisitor
/**
 * A meta-model visitor that resolves type names.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Jeff Tsay & Duny Lam
 * @version $Id: ResolveTypesVisitor.java,v 1.26 2006/10/12 20:34:13 cxh Exp $
 */
public class ResolveTypesVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that looks up names of types in the correct scope of
     * classes/packages.
     */
    public ResolveTypesVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Resolve the type names referred in this file by visiting the type
     * declarations node.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        _currentPackage = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        // visit all def types with the file scope as the argument
        Scope scope = (Scope) node.getDefinedProperty(SCOPE_KEY);
        LinkedList childArgs = TNLManip.addFirst(scope);
        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        return null;
    }

    /**
     * Resolve the name of the superclass, interfaces, and members of a
     * ClassDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    /**
     * Resolve the name of the superquantity, interfaces, and members of a
     * QuantityDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    /**
     * Resolve the name of the interfaces, and members of an InterfaceDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeNode(node, args);
    }

    /**
     * Resolve the name of the superclass and members of an ProcessDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        // the following method will visit interfaces too
        // but since a Process doesn't have interfaces, it does nothing.
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    /**
     * Resolve the name of the superclass, interfaces and members of a
     * MediumDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    /**
     * Resolve the name of the superclass and members of a SchedulerDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        // the following method visits interfaces too
        // but since a Scheduler doesn't have interface, it does nothing
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    /**
     * Resolve the name of the superclass, interfaces and members of a
     * StateMediumDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    /**
     * Resolve the name of the superclass, interfaces and members of a
     * NetlistDeclNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        _visitUserTypeNode(node, args);

        // resolve the super class with the same input scope
        // node.getSuperClass().accept(this, args);

        return null;
    }

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        Scope scope = (Scope) args.get(0);

        _resolveTypeName(node.getName(), scope, CG_USERTYPE | CG_TEMPLATE);
        node.getName().getParameters().accept(this, args);

        /*
         * ScopeIterator itr = scope.lookupFirst(name.getIdent(), CG_USERTYPE);
         *
         * ObjectDecl decl; if (itr.hasNext()) { decl = (ObjectDecl)itr.next();
         * if (itr.hasNext()) System.err.println("In class '" + _className + "':
         * user type name '" + name.getIdent() + " has been declared multiple
         * times. " + "One of the declarations is used.");
         * name.setProperty(DECL_KEY, decl); } else { throw new RuntimeException
         * ("In class '" + _className + "': user type name '" + name.getIdent() + "
         * undeclared."); }
         */

        return null;
    }

    public Object visitGetConnectionNumNode(GetConnectionNumNode node,
            LinkedList args) {
        Scope scope = (Scope) args.get(0);
        TreeNode ifName = node.getIfName();
        if (ifName != AbsentTreeNode.instance)
            _resolveTypeName((NameNode) ifName, scope, CG_INTERFACE);
        node.getMedium().accept(this, args);
        return null;
    }

    public Object visitGetNthConnectionPortNode(GetNthConnectionPortNode node,
            LinkedList args) {
        Scope scope = (Scope) args.get(0);
        _resolveTypeName(node.getIfName(), scope, CG_INTERFACE);
        node.getNum().accept(this, args);
        node.getMedium().accept(this, args);
        return null;
    }

    public Object visitGetNthConnectionSrcNode(GetNthConnectionSrcNode node,
            LinkedList args) {
        Scope scope = (Scope) args.get(0);
        _resolveTypeName(node.getIfName(), scope, CG_INTERFACE);
        node.getNum().accept(this, args);
        node.getMedium().accept(this, args);
        return null;
    }

    /** Resolve the possible TypeNameNode that is the base class. */
    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        node.getBaseType().accept(this, args);

        return null;
    }

    // //////////////////////////////////////////////////////////////
    // Newly added Nodetypes for supporting //
    // mapped behavior and constraints syntaxes //
    // //////////////////////////////////////////////////////////////

    public Object visitGetNthPortNode(GetNthPortNode node, LinkedList args) {
        Scope scope = (Scope) args.get(0);
        _resolveTypeName(node.getIfName(), scope, CG_INTERFACE);
        node.getNum().accept(this, args);
        node.getNode().accept(this, args);
        return null;
    }

    public Object visitGetProcessNode(GetProcessNode node, LinkedList args) {
        //Scope scope = (Scope) args.get(0);
        node.getEvent().accept(this, args);
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The package this compile unit is in. */
    protected PackageDecl _currentPackage = null;

    /** The class in which types are being resolved. */
    protected String _className = null;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * The default visit method. Visits all child nodes with the same scope as
     * in the argument list. Nodes that do not have their own scope should call
     * this method.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // just pass on the same scope to the children
        return TNLManip.traverseList(this, args, node.children());
    }

    /**
     * Visit a user-defined type of the compile unit.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return Always return null.
     */
    protected Object _visitUserTypeNode(UserTypeDeclNode node, LinkedList args) {
        _className = node.getName().getIdent();

        // scope for this class is argument for children
        LinkedList childArgs = TNLManip.addFirst(node
                .getDefinedProperty(SCOPE_KEY));

        TNLManip.traverseList(this, childArgs, node.getInterfaces());
        TNLManip.traverseList(this, childArgs, node.getMembers());

        // resolve the super class with the same input scope
        if (!(node instanceof InterfaceDeclNode))
            ((ClassDeclNode) node).getSuperClass().accept(this, childArgs);

        // remove the SCOPE_KEY property which is no longer needed
        // node.removeProperty(SCOPE_KEY);

        return null;
    }

    /**
     * Resolve a name of a type or package. Set the declaration for this
     * NameNode recursively.
     *
     * @param name
     *            NameNode to be resolved.
     * @param scope
     *            Scope to be used to resolve this name.
     * @param mask
     *            Set of valid categories for this type
     */
    protected void _resolveTypeName(NameNode name, Scope scope, int mask) {
        TreeNode qualifier = name.getQualifier();
        //Scope initScope = scope;
        if (qualifier == AbsentTreeNode.instance) {
            String ident = name.getIdent();
            boolean found = false;
            while ((!found) && (scope != null)) {
                ScopeIterator iter = scope.lookupFirstLocal(ident, mask);
                if (iter.hasNext()) {
                    MetaModelDecl decl = (MetaModelDecl) iter.next();
                    name.setProperty(DECL_KEY, decl);
                    found = true;
                    if (iter.hasNext()) {
                        Scope thisScope = _currentPackage.getScope();
                        System.out.println("scope" + thisScope);
                        throw new RuntimeException("In class " + _className
                                + ", ambiguous name '" + ident + "' used. Use "
                                + "fully qualified name to avoid ambiguity.");
                    }
                } else {
                    scope = scope.parent();
                }
            }
            if (!found) {
                PackageDecl decl = FileLoader.loadTopLevelPackage(ident);
                if (decl == null) {
                    throw new RuntimeException(
                            "In class "
                                    + _className
                                    + ", name '"
                                    + ident
                                    + "' is undefined. Check that"
                                    + " the name is correct and it has been imported,"
                                    + " or, if it is a top-level package, check that it"
                                    + " is on the classpath");
                }
                name.setProperty(DECL_KEY, decl);
            }
        } else {
            // Qualified name, solve qualifier first
            _resolveTypeName((NameNode) qualifier, scope, CG_USERTYPE
                    | CG_PACKAGE);
            MetaModelDecl decl = (MetaModelDecl) qualifier
                    .getProperty(DECL_KEY);
            Scope validScope = decl.getScope();
            String ident = name.getIdent();
            MetaModelDecl thisDecl = (MetaModelDecl) validScope.lookupLocal(
                    ident, mask);
            if (thisDecl == null) {
                throw new RuntimeException("In class " + _className
                        + ", name of type or subpackage '" + ident
                        + "' not found" + "in package '" + decl.fullName()
                        + "'");
            }
            name.setProperty(DECL_KEY, thisDecl);
        }
    }

}
