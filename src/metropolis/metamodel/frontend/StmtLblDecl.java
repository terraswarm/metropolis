/* A declaration of a statement label in the code.

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

// ////////////////////////////////////////////////////////////////////////
// // StmtLblDecl
/**
 * A declaration of a statement label in the code. The declaration has a source,
 * which can be of the form "label: stmt;" or "block(label) {stmt}". Unlike
 * Java, the parent scope of a label is ALWAYS the scope of the class where the
 * label is declared. It is NOT the scope of the enclosing block. This should be
 * taken into account when searching a label or adding a new label to the scope.
 * A label has also an enclosing method.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: StmtLblDecl.java,v 1.16 2006/10/12 20:34:16 cxh Exp $
 */
public class StmtLblDecl extends MetaModelDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a StmtLblDecl with a given name. and source node. Set the category
     * to the correct value (CG_STMTLABEL).
     *
     * @param name
     *            Identifier of the label.
     * @param source
     *            Node in the AST where the label is declared.
     * @param method
     *            Method where the label is declared.
     * @param isGlobal
     *            Flag indicating if the label will be visible from other types,
     *            other than the object where it was declared. It is true if the
     *            label was declared using 'block' keyword.
     */
    public StmtLblDecl(String name, TreeNode source, MethodDecl method,
            boolean isGlobal) {
        super(name, CG_STMTLABEL);
        _source = source;
        _method = method;
        _global = isGlobal;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Override the method in MetaModelDecl, as all labels have a source node in
     * the AST.
     *
     * @return true, as described.
     */
    public final boolean hasSource() {
        return true;
    }

    /**
     * Get the source node in the AST where the label is defined.
     *
     * @return The source node in the AST.
     * @see #setSource(TreeNode)
     */
    public final TreeNode getSource() {
        return _source;
    }

    /**
     * Set the source node in the AST where the label is defined.
     *
     * @param source
     *            Source node in the AST.
     * @see #getSource()
     */
    public final void setSource(TreeNode source) {
        _source = source;
    }

    /**
     * Get the method declaration where label is defined.
     *
     * @return The method declaration where the label is defined.
     * @see #setMethod(MethodDecl)
     */
    public final MethodDecl getMethod() {
        return _method;
    }

    /**
     * Set the method declaration where label is defined.
     *
     * @param method
     *            Method declaration where the label is defined.
     * @see #getMethod()
     */
    public final void setMethod(MethodDecl method) {
        _method = method;
    }

    /**
     * Set the visibility of the label. If global is true, then the label is
     * visible from outside the class where it was declared.
     *
     * @param isGlobal
     *            Visibility flag.
     */
    public final void setVisibility(boolean isGlobal) {
        _global = isGlobal;
    }

    /**
     * Check if the label is global, i.e. it can be accessed from outside the
     * class where it was declared.
     *
     * @return true if the label will be visible from other types, other than
     *         the object where it was declared. It is true if the label was
     *         declared using 'block' keyword.
     *
     */
    public final boolean isGlobal() {
        return _global;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Node of the AST where the label is defined. */
    protected TreeNode _source;

    /** Method where this label is declared. */
    protected MethodDecl _method;

    /**
     * Flag indicating if the flag is visible from objects other than the object
     * where it is declared.
     */
    protected boolean _global;

}
