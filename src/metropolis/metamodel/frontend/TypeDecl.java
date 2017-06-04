/* A declaration of a new type (class type or type parameter).

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
// // TypeDecl
/**
 * A declaration of a new type, that can be an object class (class, interface,
 * process, netlist, etc.) or a parametric type of a template. All TypeDecl have
 * a defining type.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TypeDecl.java,v 1.13 2006/10/12 20:34:23 cxh Exp $
 */
public abstract class TypeDecl extends MetaModelDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new type declaration with a given name, kind of type declaration
     * and a given type. This method should only be used by subclasses.
     *
     * @param name
     *            Identifier of the type.
     * @param category0
     *            Kind of declaration.
     * @param defType
     *            Type being defined.
     */
    protected TypeDecl(String name, int category0, TypeNode defType) {
        super(name, category0);
        _defType = defType;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if this declaration defines a type. By definition, a type
     * declaration defines a type.
     *
     * @return true, by definition.
     */
    public boolean hasDefType() {
        return true;
    }

    /**
     * Get the type defined by this declaration.
     *
     * @return A TypeNode with the type defined by this declaration.
     * @see #setDefType(TypeNode)
     */
    public TypeNode getDefType() {
        return _defType;
    }

    /**
     * Set the type defined by this declaration.
     *
     * @param defType
     *            Type Defined by this declaration.
     * @see #getDefType()
     */
    public void setDefType(TypeNode defType) {
        _defType = defType;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Type defined in this declaration. */
    protected TypeNode _defType = null;

}
