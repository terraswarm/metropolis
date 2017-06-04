/* A declaration of a type parameter of a template.

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

import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.TemplateTypeNode;

// ////////////////////////////////////////////////////////////////////////
// // TypeParameterDecl
/**
 * A declaration of a type parameter of a template. A type parameter of a
 * template has a source node in the AST where it is declared, a defining type
 * and a container (the template class where it is declared).
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TypeParameterDecl.java,v 1.17 2006/10/12 20:34:26 cxh Exp $
 */
public class TypeParameterDecl extends TypeDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new type parameter of a template, with a given name, and source
     * node in the AST.
     *
     * @param name
     *            Identifier of the type parameter.
     * @param source
     *            Node of the AST where the type parameter is defined.
     * @param container
     *            Template where the the parameter is defined.
     */
    public TypeParameterDecl(String name, TreeNode source,
            MetaModelDecl container) {
        super(name, CG_TEMPLATE, TemplateTypeNode.instance);
        _source = source;
        _container = container;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if the type parameter has a corresponding node in the AST.
     *
     * @return true, by definition.
     */
    public final boolean hasSource() {
        return true;
    }

    /**
     * Get the node in the AST where the type parameter is declared.
     *
     * @return The node in the AST where the type parameter is declared.
     * @see #setSource(TreeNode)
     */
    public final TreeNode getSource() {
        return _source;
    }

    /**
     * Set the node of the AST where the type parameter is declared.
     *
     * @param source
     *            Node of the AST.
     * @see #getSource()
     */
    public final void setSource(TreeNode source) {
        _source = source;
    }

    /**
     * Check if the type parameter is contained by another declaration.
     *
     * @return true, all type parameters are declared in a template.
     */
    public final boolean hasContainer() {
        return true;
    }

    /**
     * Get the template where the type parameter is declared.
     *
     * @return The Decl of the template where the type parameter is declared.
     * @see #setContainer(MetaModelDecl)
     */
    public final MetaModelDecl getContainer() {
        return _container;
    }

    /**
     * Set the template where the type parameter is declared.
     *
     * @param container
     *            Template that declares this type parameter
     * @see #getContainer()
     */
    public final void setContainer(MetaModelDecl container) {
        _container = container;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Node of the AST where the type parameter is declared. */
    protected TreeNode _source = null;

    /** Template where the type parameter is declared. */
    protected MetaModelDecl _container = null;

}
