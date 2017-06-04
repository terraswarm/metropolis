/* ResolvePackageVisitor adds the names of classes defined in the file to the
 file scope and create scopes for top-level nodes.

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
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.NullValue;
import metropolis.metamodel.Scope;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
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

import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ResolvePackageVisitor
/**
 * Add the names of classes defined in the file to the file scope, create scopes
 * for all nodes with scope.
 * <p>
 * Unlike Java, Meta-model does not allow a class be defined inside a method or
 * block. Therefore, unlike in Ptolemy II where blocks are given new scope
 * during this pass, they are not in meta-model.
 * <p>
 * Since templates are allowed in meta-model, template declarations will also be
 * added to the scope of the class at this stage.
 * <p>
 * This visitor returns an array of declarations of objects; it contains all
 * objects that have been declared inside this compile unit. This array is
 * returned in visitCompileUnitNode (other visit methods do not return this
 * value).
 * <p>
 * The arguments to each visit method comprise the enclosing scope, a boolean
 * that indicates if it is an inner class, and the enclosing declaration. The
 * only exception is visitCompileUnitNode() where these arguments are first
 * initialized.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Jeff Tsay & Duny Lam
 * @version $Id: ResolvePackageVisitor.java,v 1.31 2006/10/12 20:34:12 cxh Exp $
 */
public class ResolvePackageVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that performs package resolution. This visitor
     * traverses the compile unit creating a declaration for all objects (media,
     * processes, classes, ...) declared in the compile unit. The traversal
     * method is set to TM_CUSTOM.
     */
    public ResolvePackageVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Initialize _pkgDecl and _pkgScope from its PACKAGE_KEY and SCOPE_KEY
     * property which has been defined during the visit by the
     * PackageResolutionVisitor.
     * <p>
     * Set up the appropriate arguments: enclosing scope, inner class or not,
     * and the enclosing declaration; Then make appropriate calls to visit all
     * the type declarations of this CompileUnitNode.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // Get package declaration
        _pkgDecl = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        // Get file scope
        Scope scope = (Scope) node.getDefinedProperty(SCOPE_KEY);

        // Get package scope
        _pkgScope = scope.parent();

        // Get the file name
        _fileName = (String) node.getDefinedProperty(IDENT_KEY);

        // set up arguments
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(scope); // enclosing scope = file scope
        childArgs.addLast(Boolean.FALSE); // inner class = false
        childArgs.addLast(NullValue.instance); // no enclosing decl

        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        return _objectDecls;
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_CLASS category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_CLASS);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_QUANTITY category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_QUANTITY);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_INTERFACE category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_INTERFACE);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_PROCESS category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_PROCESS);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_MEDIUM category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_MEDIUM);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_SM category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_SM);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_SCHEDULER category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_SCHEDULER);
    }

    /**
     * Call the _visitUserTypeDeclNode() with the CG_NETLIST category.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args, CG_NETLIST);
    }

    /**
     * The default visit method does not do anything.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // TNLManip.traverseList(this, args, node.children());
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;

    /** The package scope. */
    protected Scope _pkgScope = null;

    /** List of object declarations performed in this class. */
    protected LinkedList _objectDecls = new LinkedList();

    /** The name of the current class. */
    protected String _fileName = null;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Set up a declaration for this user type as an inner class and add the
     * declaration into its appropriate scope.
     * <p>
     * The node that this method visits must be an inner class or interface. If
     * a process, netlist, medium, statemedium or scheduler is declared as an
     * inner class, a runtime exception will be thrown.
     * <p>
     * Most operations are similar to the _visitUserTypeDeclNode() method except
     * for the things such as the enclosing declaration.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @param category
     *            The category, which is one of the CG_ values from
     *            {@link metropolis.metamodel.MetaModelStaticSemanticConstants}
     * @return Always return null
     */
    protected Object _visitInnerUserTypeDeclNode(UserTypeDeclNode node,
            LinkedList args, int category) {

        // get enclosing scope
        Scope encScope = (Scope) args.get(0);
        // get inner class name
        String className = ((NameNode) node.getName()).getIdent();

        // error checking
        // non inner class shouldn't use this method
        if (((Boolean) args.get(1)).booleanValue() == false)
            throw new RuntimeException("Non inner class type in "
                    + "_visitInnerUserTypeDeclNode()");

        // process, medium, statemedium, scheduler, and netlist can't be
        // an inner class
        if ((category & CG_NODE) != 0)
            throw new RuntimeException("In file " + _fileName
                    + ", trying to create an inner class '" + className
                    + "' that is not a class or interface");

        // check if this inner class has already been declared in enclosing
        // scope
        Decl other = encScope.lookupLocal(className);
        if (other != null) {
            throw new RuntimeException("In file " + _fileName
                    + ", inner class '" + className + "' is declared twice");
        }

        MetaModelDecl encDecl;
        ObjectDecl ocl = null;

        // get enclosing declaration
        Object encDeclObject = args.get(2);
        if (encDeclObject == NullValue.instance) {
            encDecl = null;
        } else {
            encDecl = (MetaModelDecl) encDeclObject;
        }

        // type name of this inner class
        TypeNameNode name = new TypeNameNode(node.getName());

        // create new declaration for the inner class
        // only class or interface are allowed
        switch (category) {
        case CG_CLASS:
            ocl = new ClassDecl(className, name, node.getModifiers(), node,
                    encDecl, new LinkedList());
            break;
        case CG_INTERFACE:
            ocl = new InterfaceDecl(className, name, node.getModifiers(), node,
                    encDecl, new LinkedList());
            break;
        }
        _objectDecls.add(ocl);

        // create new scope for this user type
        Scope scope = new Scope(encScope);
        node.setProperty(SCOPE_KEY, scope);

        ocl.setScope(scope);
        encScope.add(ocl);

        node.getName().setProperty(DECL_KEY, ocl);

        // set up arguments for its children
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(scope); // scope for this class
        childArgs.addLast(Boolean.TRUE); // inner class = true
        childArgs.addLast(ocl); // last class decl

        TNLManip.traverseList(this, childArgs, node.getMembers());
        _visitParTypeNode(childArgs, node); // add parameter type declarations
        return null;
    }

    /**
     * Set up a declaration for this user type and add the declaration into its
     * appropriate scope.
     * <p>
     * If it is an inner class or interface declaration, it will use the
     * _visitInnerUserTypeDeclNode() to handle the declaration.
     * <p>
     * Checks will be made to ensure this user type has not defined in the file
     * scope and package scope. If it already has a declaration in the package
     * scope, it must not have the source set. Otherwise, it is considered as a
     * redefinition in the same package and an exception will be thrown.
     * <p>
     * The enclosing declaration for this user type should be the package
     * declaration. New scope will be built for this user type declaration and
     * the declaration for this user type will also be added into its enclosing
     * scope.
     * <p>
     * Arguments will be prepared so that inner class is set to true. Visit all
     * its children with the new arguments.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @param category
     *            The category, which is one of the CG_ values from
     *            {@link metropolis.metamodel.MetaModelStaticSemanticConstants}
     * @return Always return null
     */
    protected Object _visitUserTypeDeclNode(UserTypeDeclNode node,
            LinkedList args, int category) {

        // if inner class, visit another method
        if (((Boolean) args.get(1)).booleanValue())
            return _visitInnerUserTypeDeclNode(node, args, category);

        // get enclosing scope
        Scope encScope = (Scope) args.get(0);

        // set up new scope for this node
        Scope scope = new Scope(encScope);
        node.setProperty(SCOPE_KEY, scope);

        // get enclosing declaration which is _pkgDecl because it is non-inner
        MetaModelDecl encDecl = _pkgDecl;
        // get user type name
        String className = ((NameNode) node.getName()).getIdent();

        ObjectDecl ocl;

        // Error checking:
        // Check if this className has already been defined in the enclosing
        // scope. If so, it is an error.
        Decl other = encScope.lookupLocal(className, CG_USERTYPE);
        if (other != null) {
            throw new RuntimeException("In file, " + _fileName
                    + ", object class '" + className + "' declared twice");
        }

        // lookup in package
        ocl = (ObjectDecl) _pkgScope.lookupLocal(className, CG_USERTYPE);

        // if this user type has already been declared, do nothing
        if (ocl != null && ocl.getSource() == node) {
            throw new RuntimeException(
                    "Internal error in ResolvePackageVisitor");
            // return null;
        }

        // check of className has already been defined in the same package
        // if it has been defined and source is different, it is an error
        if (ocl != null && (ocl.getSource() != null)
                && ocl.getSource() != AbsentTreeNode.instance) {
            String name = "";
            switch (category) {
            case CG_CLASS:
                name = "Class";
                break;
            case CG_QUANTITY:
                name = "Quantity";
                break;
            case CG_INTERFACE:
                name = "Inteface";
                break;
            case CG_PROCESS:
                name = "Process";
                break;
            case CG_NETLIST:
                name = "Netlist";
                break;
            case CG_SCHEDULER:
                name = "Scheduler";
                break;
            case CG_MEDIUM:
                name = "Medium";
                break;
            case CG_SM:
                name = "Statemedium";
                break;
            default:
                break;
            }
            name = name + " name '" + className + "' (declared in file '"
                    + _fileName + "') already used in package '"
                    + _pkgDecl.fullName()
                    + "'. Previous declaration can be found " + "in file ";
            TreeNode srcNode = ocl.getSource();
            while (!(srcNode instanceof CompileUnitNode)) {
                srcNode = srcNode.getParent();
            }
            name = name + ((String) srcNode.getDefinedProperty(IDENT_KEY));
            throw new RuntimeException(name);
        }

        // declaration of className is found but there is no source
        // so assume this is the definition of this type declaration
        if ((ocl != null)
                && ((ocl.getSource() == null) || (ocl.getSource() == AbsentTreeNode.instance))) {
            ocl.setSource(node);
            ocl.setModifiers(node.getModifiers());
            ocl.category = category;

        } else {

            // create new declaration for this user type from scratch
            TypeNameNode name = new TypeNameNode(node.getName());
            switch (category) {
            case CG_CLASS:
                ocl = new ClassDecl(className, name, node.getModifiers(), node,
                        encDecl, null);
                break;
            case CG_QUANTITY:
                ocl = new QuantityDecl(className, name, node.getModifiers(),
                        node, encDecl, null);
                break;
            case CG_INTERFACE:
                ocl = new InterfaceDecl(className, name, node.getModifiers(),
                        node, encDecl, null);
                break;
            case CG_NETLIST:
                ocl = new NetlistDecl(className, name, node.getModifiers(),
                        node, encDecl, new LinkedList());
                break;
            case CG_PROCESS:
                ocl = new ProcessDecl(className, name, node.getModifiers(),
                        node, encDecl, new LinkedList());
                break;
            case CG_MEDIUM:
                ocl = new MediumDecl(className, name, node.getModifiers(),
                        node, encDecl, new LinkedList());
                break;
            case CG_SCHEDULER:
                ocl = new SchedulerDecl(className, name, node.getModifiers(),
                        node, encDecl, new LinkedList());
                break;
            case CG_SM:
                ocl = new StateMediumDecl(className, name, node.getModifiers(),
                        node, encDecl, new LinkedList());
                break;
            }
            _objectDecls.add(ocl);

            // Initialization that is necessary to parse
            // the package 'metamodel.util'. If 'metamodel.util'
            // contained netlists, processes, etc. the
            // base classes NETLIST_DECL, PROCESS_DECL, etc. from the
            // LANG package should be initialized here as well
            if (_pkgDecl == MetaModelLibrary.LANG_PACKAGE) {
                if (className.equals("Object"))
                    MetaModelLibrary.OBJECT_DECL = (ClassDecl) ocl;
                else if (className.equals("String"))
                    MetaModelLibrary.STRING_DECL = (ClassDecl) ocl;
                else if (className.equals("Array"))
                    MetaModelLibrary.ARRAY_CLASS_DECL = (ClassDecl) ocl;
                else if (className.equals("Interface"))
                    MetaModelLibrary.INTERFACE_DECL = (InterfaceDecl) ocl;
                else if (className.equals("Quantity"))
                    MetaModelLibrary.QUANTITY_DECL = (QuantityDecl) ocl;
            }

            _pkgDecl.initGetScope().add(ocl);
        }

        // set up new scope for this user type
        ocl.setScope(scope);

        // add to file scope
        encScope.add(ocl);

        node.getName().setProperty(DECL_KEY, ocl);

        LinkedList childArgs = new LinkedList();
        childArgs.addLast(scope); // scope for this class
        childArgs.addLast(Boolean.TRUE); // inner class = true
        childArgs.addLast(ocl); // last class decl

        TNLManip.traverseList(this, childArgs, node.getMembers());
        _visitParTypeNode(childArgs, node); // add parameter type declarations

        // Checking:
        // System.out.println("class name = " + node.getName().getIdent());
        // System.out.println("class scope = " +
        // node.getDefinedProperty(SCOPE_KEY));

        return null;
    }

    /**
     * Create a TypeParameterDecl for any parameter types. Throw an exception if
     * duplicate parameter type names are declared. The declarations of all the
     * parameter types will be added to the class scope.
     *
     * @param args
     *            a list of args containing the class scope, a boolean
     *            indicating if it is inner, and the enclosing declaration
     * @param node
     *            the node that is being visited
     */
    protected static void _visitParTypeNode(LinkedList args,
            UserTypeDeclNode node) {

        Scope classScope = (Scope) args.get(0);
        ObjectDecl container = (ObjectDecl) args.get(2);
        Iterator itr = node.getParTypeNames().iterator();

        Decl old;
        String name;

        // add all type parameter declarations into the class scop
        // throw an exception if duplicate type names are found

        LinkedList typeParams = new LinkedList();

        while (itr.hasNext()) {
            NameNode declNode = ((NameNode) itr.next());
            name = declNode.getIdent();
            old = classScope.lookupLocal(name, CG_TEMPLATE);
            if (old != null) {
                throw new RuntimeException("In class " + container.fullName()
                        + ", parameter name '" + name + "' is used twice");
            }
            old = classScope.lookupLocal(name, CG_USERTYPE);
            if (old != null) {
                throw new RuntimeException("In class " + container.fullName()
                        + ", parameter name '" + name + "' is also used as the"
                        + " name of an inner class");
            }

            TypeParameterDecl typeParamDecl = new TypeParameterDecl(name,
                    declNode, container);

            typeParams.addLast(typeParamDecl);

            // Store the declaration in the node
            declNode.setProperty(DECL_KEY, typeParamDecl);
            classScope.add(typeParamDecl);
        }
        container.setTypeParams(typeParams);

    }

}
