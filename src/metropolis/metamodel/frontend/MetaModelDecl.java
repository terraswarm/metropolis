/* A declaration in the meta-model language.

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
import metropolis.metamodel.Scope;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.TypeNode;

// ////////////////////////////////////////////////////////////////////////
// // MetaModelDecl
/**
 * The class MetaModelDecl describes all possible declarations in the meta-model
 * language. Some members of this class only make sense in certain kinds of
 * declarations. A method hasMember() is provided for each member, and is
 * implemented to return true in each class that uses the member.
 * <p>
 * Each member provides the following methods: hasMember(), getMember() and
 * setMember(memberVal). These allow us check if a member is valid, get the
 * value of the member and set the value of the member.
 * </p>
 * <p>
 * Some of the members of the MetaModelDecl classes are:
 * </p>
 * <ul>
 * <li> Name: All Decls have a name of type String. Severals declarations within
 * the same scope can use the same name in some cases (e.g. overloading
 * methods).
 * <li> Container: Some Decls are performed inside the Decl of another entity.
 * Members should refer to the class where they are declared, object
 * declarations (in general) should refer to the class or package where they are
 * declared, and packages should refer to the parent package where they are
 * declared. All classes that are declared without providing a package name are
 * contained by the UNNAMED_PACKAGE. The root package is called the
 * SYSTEM_PACKAGE.
 * <li> Modifiers: Modifiers declared by the user + default modifiers for this
 * kind of declaration. Changing the modifiers in the Decl does not change the
 * AST.
 * <li> Scope: Classe, interfaces and packages define scopes (mappings of names
 * of members, classes, interfaces, subpackages... to MetaModelDecl of those
 * entities.
 * <li> Source: Node of the AST associated with the declaration.
 * <li> DefType: Some declarations define a type (e.g. class declarations, type
 * parameter declarations), which is stored in this member.
 * </ul>
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: MetaModelDecl.java,v 1.23 2006/10/12 20:33:52 cxh Exp $
 */
public abstract class MetaModelDecl extends Decl implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Set the name and category. Used by subclasses to set the category of the
     * declaration (i.e. the kind of entity being declared) and the name of the
     * entity. This class is abstract, so the constructor cannot be called
     * directly.
     *
     * @param name
     *            Name of the declaration.
     * @param category0
     *            Kind of declaration.
     */
    protected MetaModelDecl(String name, int category0) {
        super(name, category0);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    // hasMember() methods
    // By default return false, should be redefined by Decls that use
    // the member.

    /**
     * Check if the declaration has a container (i.e. is declared inside the
     * declaration of a larger entity).
     *
     * @return true iff the declaration has a container.
     */
    public boolean hasContainer() {
        return false;
    }

    /**
     * Check if the declaration defines a new type.
     *
     * @return true iff the declaration defines a new type.
     */
    public boolean hasDefType() {
        return false;
    }

    /**
     * Check if the declaration has a scope (i.e. it can contain several
     * members, classes or subpackages).
     *
     * @return true iff the declaration has a container.
     */
    public boolean hasScope() {
        return false;
    }

    /**
     * Check if the declaration has modifiers.
     *
     * @return true iff the declaration has modifiers.
     */
    public boolean hasModifiers() {
        return false;
    }

    /**
     * Check if the declaration has a source node in the AST.
     *
     * @return true iff the declaration has a container.
     */
    public boolean hasSource() {
        return false;
    }

    // setMember() methods
    // By default throw a RuntimeException, should be redefined by
    // Decls that use the member.

    /**
     * Set the container (enclosing declaration) for this declaration.
     *
     * @param decl
     *            Enclosing declaration.
     * @exception RuntimeException
     *                if the Decl does not have a container.
     * @see #getContainer()
     */
    public void setContainer(MetaModelDecl decl) {
        String name = getClass().getName();
        throw new RuntimeException(name + " has no container");
    }

    /**
     * Set the defined type for this declaration.
     *
     * @param type
     *            Defined type.
     * @exception RuntimeException
     *                if the Decl does not define a type.
     * @see #getDefType()
     */
    public void setDefType(TypeNode type) {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not define a type");
    }

    /**
     * Set the scope of this declaration.
     *
     * @param scope
     *            Scope for current declaration.
     * @exception RuntimeException
     *                if the Decl does not define a scope.
     * @see #getScope()
     */
    public void setScope(Scope scope) {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not define a type");
    }

    /**
     * Set the modifiers of this declaration.
     *
     * @param modifiers
     *            Set of modifiers.
     * @exception RuntimeException
     *                if the Decl does not allow modifiers.
     * @see #getModifiers()
     */
    public void setModifiers(int modifiers) {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not define a type");
    }

    /**
     * Set the source node of the AST where this declaration is performed.
     *
     * @param source
     *            Node of the AST.
     * @exception RuntimeException
     *                if the Decl does not have a source node.
     * @see #getSource()
     */
    public void setSource(TreeNode source) {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not have a source node");
    }

    // getMember() methods
    // By default throw a RuntimeException, should be redefined by
    // Decls that use the member.

    /**
     * Get the container (enclosing declaration) for this declaration.
     *
     * @return The enclosing declaration.
     * @exception RuntimeException
     *                if the Decl does not have a container.
     * @see #setContainer(MetaModelDecl)
     */
    public MetaModelDecl getContainer() {
        String name = getClass().getName();
        throw new RuntimeException(name + " has no container");
    }

    /**
     * Get the defined type for this declaration.
     *
     * @return The defined type.
     * @exception RuntimeException
     *                if the Decl does not define a type.
     * @see #setDefType(TypeNode)
     */
    public TypeNode getDefType() {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not define a type");
    }

    /**
     * Get the scope of this declaration.
     *
     * @return The scope for this declaration.
     * @exception RuntimeException
     *                if the Decl does not define a scope.
     * @see #setScope(Scope)
     */
    public Scope getScope() {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not define a type");
    }

    /**
     * Get the modifiers of this declaration.
     *
     * @return the set of modifiers.
     * @exception RuntimeException
     *                if the Decl does not allow modifiers.
     * @see #setModifiers(int)
     */
    public int getModifiers() {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not define a type");
    }

    /**
     * Get the source node of the AST where this declaration is performed.
     *
     * @return The node of the AST.
     * @exception RuntimeException
     *                if the Decl does not have a source node.
     * @see #setSource(TreeNode)
     */
    public TreeNode getSource() {
        String name = getClass().getName();
        throw new RuntimeException(name + " does not have a source node");
    }

    // Other public methods

    /**
     * Get an scope of types associated with this declaration. This method is
     * used instead of getScope() when looking up types contained in the scope.
     * getScope() for ClassDecl needs to run pass 1 to get all the fields and
     * methods into the scope. This method avoids running pass 1 while still in
     * pass 0.
     *
     * This method is necessary to allow for inner classes, since the scope a
     * class is necessary to lookup its inner classes.
     *
     * The default method just calls getScope().
     *
     * @return A scope of types associated with this declaration.
     */
    public Scope getTypeScope() {
        return getScope();
    }

    /**
     * Get a string giving the full name (including class, etc) of this
     * MetaModelDecl. Optional delimiter will appear between nested components
     * and defaults to a period.
     *
     * @return The canonical name of the declaration, with names separated by
     *         '.'.
     */
    public String fullName() {
        return fullName('.');
    }

    /**
     * Get a string giving the full name (including class, etc) of this
     * MetaModelDecl. Optional delimiter will appear between nested components.
     *
     * @param delimiter
     *            Character used to separate simple names.
     * @return The canonical name of the declaration, with names separated by
     *         delimiter.
     */
    public String fullName(char delimiter) {
        StringBuffer prefix = new StringBuffer();

        if (hasContainer() && (getContainer() != null)) {

            MetaModelDecl container = getContainer();
            if ((container != MetaModelLibrary.SYSTEM_PACKAGE)
                    && (container != MetaModelLibrary.UNNAMED_PACKAGE))
                prefix.append(container.fullName(delimiter));
        }

        if (prefix.length() > 0) {
            prefix.append(delimiter);
        }

        prefix.append(getName());
        return prefix.toString();
    }

    /**
     * Check if this declaration is contained by the container Decl. Search all
     * super-containers of this declaration for the container.
     *
     * @param container
     *            Declaration to be searched.
     * @return true iff this declaration is enclosed by the declaration
     *         container or any of its super-containers.
     */
    public boolean deepContainedBy(MetaModelDecl container) {
        MetaModelDecl decl = this;
        while (decl.hasContainer()) {
            decl = decl.getContainer();
            if (decl == container) {
                return true;
            }
            if (decl == null) {
                return false;
            }
        }
        return false;
    }

    /**
     * Get the Decl associated with the node. This method figures out the type
     * of node, and passes to the appropriate more specific getDecl() method.
     *
     * @param node
     *            The node in the AST.
     * @return The declaration associated with the node, or null if the
     *         declaration is not found.
     * @see #setDecl(TreeNode, MetaModelDecl)
     */
    public static final MetaModelDecl getDecl(TreeNode node) {
        if (node instanceof NamedNode) {
            return getDecl((NamedNode) node);
        }
        return (MetaModelDecl) node.getProperty(DECL_KEY);
    }

    /**
     * Get the Decl associated with a named node.
     *
     * @param node
     *            Named node.
     * @return The declaration associated with a named node, or null if it is
     *         not found.
     * @see #setDecl(NamedNode, MetaModelDecl)
     */
    public static final MetaModelDecl getDecl(NamedNode node) {
        return (MetaModelDecl) node.getName().getProperty(DECL_KEY);
    }

    /**
     * Set the Decl associated with the node. This method figures out the type
     * of node, and passes to the appropriate more specific setDecl() method.
     *
     * @param node
     *            Node in the AST.
     * @param decl
     *            Declaration associated with the node.
     * @see #getDecl(TreeNode)
     */
    public static final void setDecl(TreeNode node, MetaModelDecl decl) {
        if (node instanceof NamedNode) {
            setDecl((NamedNode) node, decl);
            return;
        }
        node.setProperty(DECL_KEY, decl);
    }

    /**
     * Set the Decl associated with the named node.
     *
     * @param node
     *            Named node in the AST.
     * @param decl
     *            Declaration associated with the name node.
     * @see #getDecl(NamedNode)
     */
    public static final void setDecl(NamedNode node, MetaModelDecl decl) {
        node.getName().setProperty(DECL_KEY, decl);
    }

}
