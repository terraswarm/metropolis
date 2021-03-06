/*
 This file was generated automatically by GenerateVisitor and then updated
 by hand.

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

 */

package metropolis.metamodel.nodetypes;

import metropolis.metamodel.TreeNode;

/**
 * A Generic Name Node. A node of the form:
 *
 * <pre>
 *       QUALIFIER . IDENT &lt; PARAMETERS &gt;
 * </pre>
 *
 * where PARAMETERS is the list of type parameters (for templates only), and
 * QUALIFIER is the qualifier of the name (for qualified names only). PARAMETERS
 * OR QUALIFIER can be AbsentTreeNode.instance.
 *
 * <p>
 * Note that this class is <b>not</b> automatically generated by
 * GenerateVisitor.
 *
 * @author Robert Clariso
 * @version $Id: GenericNameNode.java,v 1.17 2006/10/12 20:35:57 cxh Exp $
 */
public abstract class GenericNameNode extends TreeNode implements MetaModelNode {

    /** Construct a GenericNameNode. */
    public GenericNameNode() {
    }

    /**
     * Construct a GenericNameNode.
     *
     * @param qualifier
     *            The TreeNode.
     * @param ident
     *            The String.
     * @param parameters
     *            The TreeNode.
     */
    public GenericNameNode(TreeNode qualifier, String ident, TreeNode parameters) {
        _childList.add(qualifier);
        _ident = ident.intern();
        _childList.add(parameters);
        _childList.trimToSize();
    }

    /**
     * Return the Qualifier.
     *
     * @return the Qualifier.
     * @see #setQualifier(TreeNode)
     */
    public final TreeNode getQualifier() {
        return (TreeNode) _childList.get(CHILD_INDEX_QUALIFIER);
    }

    /**
     * Set the Qualifier.
     *
     * @param qualifier
     *            The TreeNode
     * @see #getQualifier()
     */
    public final void setQualifier(TreeNode qualifier) {
        _childList.set(CHILD_INDEX_QUALIFIER, qualifier);
    }

    /**
     * Return the Ident.
     *
     * @return the Ident
     * @see #setIdent(String)
     */
    public final String getIdent() {
        return _ident;
    }

    /**
     * Set the Ident.
     *
     * @param ident
     *            the Ident
     * @see #getIdent()
     */
    public final void setIdent(String ident) {
        _ident = ident;
    }

    /**
     * Return the Parameters.
     *
     * @return the TreeNode.
     * @see #setParameters(TreeNode)
     */
    public final TreeNode getParameters() {
        return (TreeNode) _childList.get(CHILD_INDEX_PARAMETERS);
    }

    /**
     * Set the Parameters.
     *
     * @param parameters
     *            the Parameters.
     * @see #getParameters()
     */
    public final void setParameters(TreeNode parameters) {
        _childList.set(CHILD_INDEX_PARAMETERS, parameters);
    }

    /**
     * Index of the qualifier field in the _childList.
     *
     * @see metropolis.metamodel.TreeNode#_childList
     */
    public static final int CHILD_INDEX_QUALIFIER = 0;

    /**
     * Index of the parameters field in the _childList.
     *
     * @see metropolis.metamodel.TreeNode#_childList
     */
    public static final int CHILD_INDEX_PARAMETERS = 1;

    /** ident of type String. */
    protected String _ident;
}
