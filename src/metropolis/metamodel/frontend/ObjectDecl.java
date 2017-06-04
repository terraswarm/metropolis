/* A declaration of an object of some kind (class, interface, process,
 netlist, scheduler, ...).

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

import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.TypeNode;

import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ObjectDecl
/**
 * A declaration of an object of some kind. Interfaces, classes, processes,
 * media, state media, schedulers and netlists fall into this category. All
 * objects have the following members:
 * <ul>
 * <li> a scope, where named members are declared
 * <li> a source node in the AST where the class of objects is declared
 * <li> a set of modifiers in the declaration of the object
 * <li> a container, the declaration of the package or class where this object
 * is declared
 * <li> a list of declarations of type parameters of the object
 * </ul>
 * Objects provide additional methods to access the members of a class. These
 * methods return a ScopeIterator to the iterate through a list, or a single
 * Decl if a name is provided.
 * <ul>
 * <li> getInterfaces(): get a list of the declarations of interfaces
 * implemented by this object.
 * <li> getSuperClass(): get the declaration of the superclass of current
 * object. Return null if object does not have a superclass. This only happens
 * in class Object and in all interfaces; it also happens when the superclass of
 * an object has not been computed yet.
 * <li> getMethods(), getMethod(): get the list of declarations of methods of
 * this object.
 * <li> getConstructors(), getConstructor(): get the list of declarations of
 * constructors of this object.
 * <li> getInvokables(): get the list of declarations of constructors AND
 * methods of this object.
 * <li> getFields(), getField(): get the list of declarations of fields of this
 * object.
 * <li> getPorts(), getPort(): get the list of declarations of ports of this
 * object.
 * <li> getParameters(), getPort(): get the list of declarations of parameters
 * of this object.
 * <li> getAttributes(): get the list of declarations of fields, ports and
 * parameters of this object.
 * <li> getInnerClasses(), getInnerClass(): get the list of inner classes
 * declared inside this object declaration.
 * <li> getBaseClass(): each kind of object has a base class. For example, all
 * classes inherit from Object, all processes inherit from Process, This method
 * returns the declaration of this base class for this kind of object.
 * </ul>
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ObjectDecl.java,v 1.28 2006/10/12 20:33:56 cxh Exp $
 */
public abstract class ObjectDecl extends TypeDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new object declaration, with a given name, kind of object
     * declaration, defined type, set of modifiers, source node in the AST where
     * the declaration is performed and container (package or class where this
     * class is declared).
     *
     * @param name
     *            Identifier of the class of objects.
     * @param category0
     *            Kind of object being declared.
     * @param defType
     *            Type being defined.
     * @param modifiers
     *            Set of modifiers of the class declaration.
     * @param source
     *            Source node of the AST where the class is declared.
     * @param container
     *            Package or class where the class is declared.
     * @param typeParams
     *            List of declarations of type parameters of this class.
     */
    protected ObjectDecl(String name, int category0, TypeNode defType,
            int modifiers, TreeNode source, MetaModelDecl container,
            List typeParams) {
        super(name, category0, defType);
        _modifiers = modifiers;
        _source = source;
        _container = container;
        _typeParams = typeParams;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return true if this object is an interface that extends interface Port
     * (for short, a Port interface).
     *
     * @return true iff the object is a Port interface.
     * @see metropolis.metamodel.frontend.InterfaceDecl
     */
    public boolean isPortInterface() {
        if (category != CG_INTERFACE) {
            return false;
        }
        TypePolicy typePolicy = new TypePolicy();
        return typePolicy.isSuperInterface(MetaModelLibrary.PORT_DECL, this);
    }

    /**
     * Get the list of interfaces implemented by this object. The list does not
     * contain nodes in the AST, but references to InterfaceDecl.
     *
     * @return A List of InterfaceDecl implemented by this object.
     * @see #setInterfaces(List)
     */
    public List getInterfaces() {
        return _interfaces;
    }

    /**
     * Set the list of interfaces implemented by this object. The interfaces
     * should not be references to nodes in the AST.
     *
     * @param interfaces
     *            List of interface declarations implemented by this object.
     * @see #getInterfaces()
     */
    public void setInterfaces(List interfaces) {
        _interfaces = interfaces;
    }

    /**
     * Get the declaration of the superclass of current object.
     *
     * @return The declaration of the superclass of current object, or null if
     *         the class has no superclass. This only happens when the object is
     *         an interface OR the object is Object.
     * @see #setSuperClass(ObjectDecl)
     */
    public ObjectDecl getSuperClass() {
        return _superClass;
    }

    /**
     * Set the declaration of the superclass of current object.
     *
     * @param superClass
     *            Declaration of the superclass.
     * @see #getSuperClass()
     */
    public void setSuperClass(ObjectDecl superClass) {
        _superClass = superClass;
    }

    /**
     * Get the declaration of the base class of this object. For example, Object
     * is the base class for classes, Process is the base class for processes,
     * etc.
     *
     * @return The declaration of the base class for this object.
     * @see #setBaseClass(ObjectDecl)
     */
    public static ObjectDecl getBaseClass() {
        return _baseClass;
    }

    /**
     * Set the declaration of the base class of this object.
     *
     * @param baseClass
     *            The declaration of the base class for this object.
     * @see #getBaseClass
     */
    public static void setBaseClass(ObjectDecl baseClass) {
        _baseClass = baseClass;
    }

    /**
     * Get the list of declarations of type parameters in this object.
     *
     * @return The list of declarations of type parameters in this object.
     * @see #setTypeParams(List)
     */
    public List getTypeParams() {
        return _typeParams;
    }

    /**
     * Set the list of declarations of type parameters in this object.
     *
     * @param typeParams
     *            List of declarations of type parameters.
     * @see #getTypeParams()
     */
    public void setTypeParams(List typeParams) {
        _typeParams = typeParams;
    }

    /**
     * Get a ScopeIterator that will iterate through all labels (global and
     * local) of this object. This method calls the Scope of this object.
     *
     * @return An iterator to generate all labels of this object.
     */
    public ScopeIterator getLabels() {
        return _scope.allLocalDecls(CG_STMTLABEL);
    }

    /**
     * Get a label with a given name in current object.
     *
     * @param name
     *            Name of the label to be found.
     * @return The declaration of the label (global or local), or null if the
     *         declaration does not exist.
     * @exception RuntimeException
     *                if there is more than one label with this name in the
     *                current class.
     */
    public StmtLblDecl getLabel(String name) {
        boolean more[] = new boolean[1];
        StmtLblDecl decl = (StmtLblDecl) _scope.lookupLocal(name, CG_STMTLABEL,
                more);
        if (more[0]) {
            throw new RuntimeException("Label name '" + name + "' in class "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get a ScopeIterator that iterates through all ports of this object. This
     * method calls the Scope of this object.
     *
     * @return An iterator to generate all ports of this object.
     */
    public ScopeIterator getPorts() {
        return _scope.allLocalDecls(CG_PORT);
    }

    /**
     * Get a port with a given name in current object.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the port, or null if the declaration does not
     *         exist.
     * @exception RuntimeException
     *                if there is more than one port with this name in the
     *                current class.
     */
    public PortDecl getPort(String name) {
        boolean more[] = new boolean[1];
        PortDecl decl = (PortDecl) _scope.lookupLocal(name, CG_PORT, more);
        if (more[0]) {
            throw new RuntimeException("Port name '" + name + "' in class "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get a ScopeIterator that iterates through all parameters of this object.
     * This method calls the Scope of this object.
     *
     * @return An iterator to generate all parameters of this object.
     */
    public ScopeIterator getParameters() {
        return _scope.allLocalDecls(CG_PARAMETER);
    }

    /**
     * Get a parameter with a given name in current object.
     *
     * @param name
     *            The name to be found.
     * @return The declaration of the parameter, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one parameter with this name in the
     *                current class.
     */
    public ParameterDecl getParameter(String name) {
        boolean more[] = new boolean[1];
        ParameterDecl decl = (ParameterDecl) _scope.lookupLocal(name,
                CG_PARAMETER, more);
        if (more[0]) {
            throw new RuntimeException("Parameter name '" + name
                    + "' in class " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get a ScopeIterator that iterates through all fields of this object. This
     * method calls the Scope of this object.
     *
     * @return An iterator to generate all fields of this object.
     */
    public ScopeIterator getFields() {
        return _scope.allLocalDecls(CG_FIELD);
    }

    /**
     * Get a field with a given name in current object.
     *
     * @param name
     *            The name of the field to find.
     * @return The declaration of the field, or null if the declaration does not
     *         exist.
     * @exception RuntimeException
     *                if there is more than one field with this name in the
     *                current class.
     */
    public FieldDecl getField(String name) {
        boolean more[] = new boolean[1];
        FieldDecl decl = (FieldDecl) _scope.lookupLocal(name, CG_FIELD, more);
        if (more[0]) {
            throw new RuntimeException("Field name '" + name + "' in class "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get a ScopeIterator that iterates through all fields, parameters, and
     * ports of this object. This method calls the Scope of this object.
     *
     * @return An iterator to generate all fields, parameters and ports of this
     *         object.
     */
    public ScopeIterator getAttributes() {
        return _scope.allLocalDecls(CG_FIELD | CG_PORT | CG_PARAMETER);
    }

    /**
     * Get a field, parameter or port with a given name in current object.
     *
     * @param name
     *            The name of the field, parameter or port to find.
     * @return The declaration of the field, parameter or port, or null if the
     *         declaration does not exist.
     * @exception RuntimeException
     *                if there is more than one field, parameter or port with
     *                this name in the current class.
     */
    public MemberDecl getAttribute(String name) {
        boolean more[] = new boolean[1];
        MemberDecl decl = (MemberDecl) _scope.lookupLocal(name, CG_FIELD
                | CG_PORT | CG_PARAMETER, more);
        if (more[0]) {
            throw new RuntimeException("Member name '" + name + "' in class "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get a ScopeIterator that iterates through all methods (not constructors)
     * of this object. This method calls the Scope of this object.
     *
     * @return An iterator to generate all methods of this object.
     */
    public ScopeIterator getMethods() {
        return _scope.allLocalDecls(CG_METHOD);
    }

    /**
     * Get a ScopeIterator that iterates through all methods (not constructors)
     * of this object that have a given name. This method calls the Scope of
     * this object.
     *
     * @param name
     *            Name of the methods to be found.
     * @return An iterator to generate all methods of this object with a given
     *         name.
     */
    public ScopeIterator getMethods(String name) {
        return _scope.lookupFirstLocal(name, CG_METHOD);
    }

    /**
     * Get a ScopeIterator that iterates through all constructors of this
     * object. This method calls the Scope of this object.
     *
     * @return An iterator to generate all constructors of this object.
     */
    public ScopeIterator getConstructors() {
        return _scope.allLocalDecls(CG_CONSTRUCTOR);
    }

    /**
     * Get a ScopeIterator that iterates through all methods and constructors of
     * this object. This method calls the Scope of this object.
     *
     * @return An iterator to generate all methods and constructors of this
     *         object.
     */
    public ScopeIterator getInvokables() {
        return _scope.allLocalDecls(CG_METHOD | CG_CONSTRUCTOR);
    }

    /**
     * Get a ScopeIterator that iterates through all inner classes of this
     * object. This method calls the Scope of this object. The current class
     * call also be included among the inner classes.
     *
     * @return An iterator to generate all inner classes of this object.
     */
    public ScopeIterator getInnerClasses() {
        return _scope.allLocalDecls(CG_CLASS | CG_INTERFACE);
    }

    /**
     * Get an inner class with a given name in current object.
     *
     * @param name
     *            The name of the inner class to find.
     * @exception RuntimeException
     *                if there is more than one field with this name in the
     *                current class.
     * @return The inner class.
     */
    public ObjectDecl getInnerClass(String name) {
        boolean more[] = new boolean[1];
        ObjectDecl decl = (ObjectDecl) _scope.lookupLocal(name, CG_CLASS
                | CG_INTERFACE, more);
        if (more[0]) {
            throw new RuntimeException("Inner class '" + name + "' in class "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Check if this declaration defines a scope. All object declarations define
     * a scope for named members.
     *
     * @return true, by definition.
     */
    public final boolean hasScope() {
        return true;
    }

    /**
     * Check if this object declaration has a corresponding node in the AST.
     *
     * @return true, all object declarations have a source node.
     */
    public final boolean hasSource() {
        return true;
    }

    /**
     * Check if this object declaration is contained in another declaration. By
     * definition, objects can only be declared inside another object (inner
     * classes) or inside a package. Objects which do not provide a package name
     * are added to the UNNAMED_PACKAGE.
     *
     * @return true, all object declarations have a container.
     */
    public final boolean hasContainer() {
        return true;
    }

    /**
     * Check if this object declaration has modifiers. By definition, All object
     * declarations have modifiers.
     *
     * @return true, by definition.
     */
    public final boolean hasModifiers() {
        return true;
    }

    /**
     * Get the scope for this object, where all members and inner classes are
     * declared.
     *
     * @return The scope for this object.
     * @see #setScope(Scope)
     */
    public final Scope getScope() {
        return _scope;
    }

    /**
     * Get the node in the AST where this object class is declared. If this
     * class has no source, it means that the file where the class has been
     * stored has not been loaded -> do it now.
     *
     * @return The node in the AST where the object is declared.
     * @see #setSource(TreeNode)
     */
    public final TreeNode getSource() {
        return _source;
    }

    /**
     * Get the declaration that encloses the declaration of the object. It can
     * be a declaration of a package or a declaration of a class (if this is an
     * inner class).
     *
     * @return The Decl containing current Decl.
     * @see #setContainer(MetaModelDecl)
     */
    public final MetaModelDecl getContainer() {
        return _container;
    }

    /**
     * Get the modifiers of this object declaration.
     *
     * @return The set of modifiers of this object declaration.
     * @see #setModifiers(int)
     */
    public final int getModifiers() {
        return _modifiers;
    }

    /**
     * Set the scope of this object declaration.
     *
     * @param scope
     *            Scope of this declaration.
     * @see #getScope()
     */
    public final void setScope(Scope scope) {
        _scope = scope;
    }

    /**
     * Set the source node in the AST where this object class is declared.
     *
     * @param source
     *            Node of the AST.
     * @see #getSource()
     */
    public final void setSource(TreeNode source) {
        _source = source;
    }

    /**
     * Set the enclosing declaration of this object declaration (package or
     * object declaration).
     *
     * @param container
     *            Enclosing declaration.
     * @see #getContainer()
     */
    public final void setContainer(MetaModelDecl container) {
        _container = container;
    }

    /**
     * Set the modifiers of this object declaration.
     *
     * @param modifiers
     *            Modifiers of the declaration.
     * @see #getModifiers()
     */
    public final void setModifiers(int modifiers) {
        _modifiers = modifiers;
    }

    /**
     * Return the package of current object.
     *
     * @return The package declaration where this object is declared.
     */
    public MetaModelDecl getPackage() {
        MetaModelDecl container = getContainer();
        while (container.category != CG_PACKAGE) {
            container = container.getContainer();
        }
        return container;
    }

    /**
     * Check if a visible object can be accessed from this object. Take into
     * account visibility (public/non-public).
     *
     * @param access
     *            Object being accessed from current class
     * @return true if the object cannot be accessed from current object.
     */
    public boolean hasAccess(ObjectDecl access) {
        // We can access a visible object A from this object B if:
        // - A is top-level object and
        // o A is public or
        // o A is declared in the same package as B
        // - A is an inner class with accessible container C and
        // o A is public or
        // o A is protected, and declared in the same package as B
        // o A is default, and declared in the same package as B

        if (this == access)
            return true;
        if (access.containsInnerClass(this))
            return true;
        MetaModelDecl container = access.getContainer();
        boolean isTopLevel = (container.category == CG_PACKAGE);
        if (isTopLevel) {
            int modifiers = access.getModifiers();
            boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
            if (isPublic)
                return true;
            MetaModelDecl pkg1 = this.getPackage();
            MetaModelDecl pkg2 = access.getPackage();
            return (pkg1 == pkg2);
        } else {
            ObjectDecl objContainer = (ObjectDecl) container;
            if (!hasAccess(objContainer)) {
                return false;
            } else {
                int modifiers = access.getModifiers();
                boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
                boolean isProtected = ((modifiers & PROTECTED_MOD) != 0);
                boolean isPrivate = ((modifiers & PRIVATE_MOD) != 0);
                if (isPublic)
                    return true;
                if (isPrivate)
                    return objContainer.containsInnerClass(this);
                if (isProtected) {
                    MetaModelDecl pkg1 = this.getPackage();
                    MetaModelDecl pkg2 = access.getPackage();
                    return (pkg1 == pkg2);
                } else {
                    // Default access: Inherited by subclasses and
                    // classes in the same package
                    MetaModelDecl pkg1 = this.getPackage();
                    MetaModelDecl pkg2 = objContainer.getPackage();
                    if (pkg1 == pkg2)
                        return true;
                    TypePolicy tp = new TypePolicy();
                    return tp.isSubClass(this, objContainer);
                }
            }
        }
    }

    /**
     * Check if a visible member can be accessed from current class.
     *
     * @param member
     *            Declaration of the member being accessed.
     * @return true iff the member can be accessed from current class.
     */
    public boolean hasAccess(MemberDecl member) {
        ObjectDecl container = (ObjectDecl) member.getContainer();

        // Check if the member belongs to this class

        if (this == container)
            return true;
        if (!hasAccess(container)) {
            return false;
        } else {
            int modifiers = member.getModifiers();
            boolean isPublic = ((modifiers & PUBLIC_MOD) != 0);
            boolean isProtected = ((modifiers & PROTECTED_MOD) != 0);
            boolean isPrivate = ((modifiers & PRIVATE_MOD) != 0);
            if (isPublic)
                return true;
            if (isPrivate)
                return container.containsInnerClass(this);
            if (isProtected) {
                MetaModelDecl pkg1 = this.getPackage();
                MetaModelDecl pkg2 = container.getPackage();
                return (pkg1 == pkg2);
            } else {
                // Default access: Inherited by subclasses and
                // classes in the same package
                MetaModelDecl pkg1 = this.getPackage();
                MetaModelDecl pkg2 = container.getPackage();
                if (pkg1 == pkg2)
                    return true;
                TypePolicy tp = new TypePolicy();
                return tp.isSubClass(this, container);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Set of modifiers of the declaration of this object. */
    protected int _modifiers;

    /** Source node of the AST where this class is declared. */
    protected TreeNode _source = null;

    /** Package or class declaration where this class is declared. */
    protected MetaModelDecl _container = null;

    /** Scope inside this class. */
    protected Scope _scope = null;

    /** List of interfaces implemented by the class. */
    protected List _interfaces = new LinkedList();

    /** Super class of current object. */
    protected ObjectDecl _superClass = null;

    /** Base class of current object. */
    protected static ObjectDecl _baseClass = null;

    /** List of declarations of type parameters of this class. */
    protected List _typeParams = null;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Test if an object declaration is an inner class of this class.
     *
     * @param innerClass
     *            Candidate inner class.
     * @return true if the object declaration is an inner class of this class.
     */
    protected boolean containsInnerClass(ObjectDecl innerClass) {
        if (this == innerClass)
            return true;
        MetaModelDecl container = innerClass;
        do {
            container = container.getContainer();
            if (container == this)
                return true;
        } while (container.category != CG_PACKAGE);
        return false;
    }

}
