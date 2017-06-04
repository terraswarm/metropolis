/* A declaration of a member of an object class.

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
import metropolis.metamodel.nodetypes.TypeNode;

// ////////////////////////////////////////////////////////////////////////
// // MemberDecl
/**
 * A declaration of a member of a class of objects. Fields, methods,
 * constructors, ports and parameters are examples of members. Members have a
 * source declaration node and a container (the class where they are declared).
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: MemberDecl.java,v 1.13 2006/10/12 20:33:51 cxh Exp $
 */
public class MemberDecl extends TypedDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a MemberDecl with a given name, kind of member declaration, type,
     * modifiers, source node in the AST and container class.
     *
     * @param name
     *            Identifier of the member.
     * @param category0
     *            Kind of member.
     * @param type
     *            Type of the member.
     * @param modifiers
     *            Modifiers of the member declaration.
     * @param source
     *            Node in the AST where the member is declared.
     * @param container
     *            Class where the member is declared.
     */
    public MemberDecl(String name, int category0, TypeNode type, int modifiers,
            TreeNode source, MetaModelDecl container) {
        super(name, category0, type, modifiers);
        _source = source;
        _container = container;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if the member has a corresponding node in the AST.
     *
     * @return true, by definition.
     */
    public final boolean hasSource() {
        return true;
    }

    /**
     * Get the node in the AST where the member is declared.
     *
     * @return The node in the AST where the member is declared.
     * @see #setSource(TreeNode)
     */
    public final TreeNode getSource() {
        return _source;
    }

    /**
     * Set the node of the AST where the member is declared.
     *
     * @param source
     *            Node of the AST.
     * @see #getSource()
     */
    public final void setSource(TreeNode source) {
        _source = source;
    }

    /**
     * Check if the member is contained in another declaration.
     *
     * @return true, by definition.
     */
    public final boolean hasContainer() {
        return true;
    }

    /**
     * Get the Decl of the class where the member is defined.
     *
     * @return The Decl of the class where the member is defined.
     * @see #setContainer(MetaModelDecl)
     */
    public final MetaModelDecl getContainer() {
        return _container;
    }

    /**
     * Set the Decl of the class where the member is defined.
     *
     * @param container
     *            The Decl where the member is defined.
     * @see #getContainer()
     */
    public final void setContainer(MetaModelDecl container) {
        _container = container;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Class where the member is declared. */
    protected MetaModelDecl _container = null;

    /** Node of the AST where the member is declared. */
    protected TreeNode _source = null;

}
