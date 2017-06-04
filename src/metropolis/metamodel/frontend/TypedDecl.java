/* A declaration of an entity that is typed.

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

import metropolis.metamodel.nodetypes.TypeNode;

// ////////////////////////////////////////////////////////////////////////
// // TypedDecl
/**
 * An abstract class for the declarations of entities that have a type. Class
 * members, local variables and parameters are examples of typed declarations.
 * Types are represented as pointers to AST nodes of type TypeNode, just as they
 * are during the parsing. Packages, statement labels, and classes are not typed
 * (although the latter is defining a type).
 * <p>
 * The members accepted in TypedDecl are the type of the declaration and a set
 * of modifiers.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TypedDecl.java,v 1.13 2006/10/12 20:34:29 cxh Exp $
 */
public abstract class TypedDecl extends MetaModelDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a declaration with a given name, category, type and modifier. This
     * constructor should be called by the subclasses.
     *
     * @param name
     *            Identifier of the declaration.
     * @param category0
     *            Kind of declaration.
     * @param type
     *            Data type of the declaration.
     * @param modifiers
     *            Set of modifiers.
     */
    protected TypedDecl(String name, int category0, TypeNode type, int modifiers) {
        super(name, category0);
        _type = type;
        _modifiers = modifiers;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if the declaration is typed. By definition, typed declarations are
     * always typed.
     *
     * @return true always.
     */
    public final boolean hasType() {
        return true;
    }

    /**
     * Get the type of this declaration.
     *
     * @return A TypeNode containing the type of this declaration.
     * @see #setType(TypeNode)
     */
    public final TypeNode getType() {
        return _type;
    }

    /**
     * Set the type of this declaration.
     *
     * @param type
     *            Type of this declaration.
     * @see #getType()
     */
    public final void setType(TypeNode type) {
        _type = type;
    }

    /**
     * Check if the declaration has modifiers. By definition, typed declarations
     * always have modifiers.
     *
     * @return true always.
     */
    public final boolean hasModifiers() {
        return true;
    }

    /**
     * Get the modifiers of this declaration.
     *
     * @return An integer defining the set of modifiers of this declaration.
     * @see #setModifiers(int)
     */
    public final int getModifiers() {
        return _modifiers;
    }

    /**
     * Set the modifiers of this declaration.
     *
     * @param mods
     *            Set of modifiers in this declaration.
     * @see #getModifiers()
     */
    public final void setModifiers(int mods) {
        _modifiers = mods;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Type of the declaration. */
    protected TypeNode _type;

    /** Modifiers of this declaration. */
    protected int _modifiers;

}
