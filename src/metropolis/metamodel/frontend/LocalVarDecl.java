/* A declaration of a local variable inside a method.

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
// // LocalVarDecl
/**
 * A declaration of a local variable inside a method. Local variable
 * declarations provide a name, a type and a source node.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: LocalVarDecl.java,v 1.14 2006/10/12 20:33:49 cxh Exp $
 */
public class LocalVarDecl extends TypedDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a declaration of a local variable with a given name, type,
     * modifiers and source node in the AST.
     *
     * @param name
     *            Identifier of the variable.
     * @param type
     *            Type of the variable.
     * @param modifiers
     *            Modifiers of this declaration.
     * @param source
     *            Node of the AST where the variable is declared.
     */
    public LocalVarDecl(String name, TypeNode type, int modifiers,
            TreeNode source) {
        super(name, CG_LOCALVAR, type, modifiers);
        _source = source;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if the variable has a corresponding node in the AST.
     *
     * @return true, by definition.
     */
    public final boolean hasSource() {
        return true;
    }

    /**
     * Get the node in the AST where the variable is declared.
     *
     * @return The node in the AST where the variable is declared.
     * @see #setSource(TreeNode)
     */
    public final TreeNode getSource() {
        return _source;
    }

    /**
     * Set the node of the AST where the variable is declared.
     *
     * @param source
     *            Node of the AST.
     * @see #getSource()
     */
    public final void setSource(TreeNode source) {
        _source = source;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Node of the AST where the local variable is declared. */
    protected TreeNode _source;

}
