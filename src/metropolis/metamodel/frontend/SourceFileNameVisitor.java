/* A visitor to set every node's IDENT_KEY property.

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

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.TreeNode;

import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // SourceFileNameVisitor
/**
 * This is a <code>MetaModelVisitor</code> that simply sets the
 * <code>IDENT_KEY</code> property to a single value, which is given in the
 * constructor.
 *
 * @author Allen Hopkins
 * @version $Id: SourceFileNameVisitor.java,v 1.9 2006/10/12 20:34:14 cxh Exp $
 */
public class SourceFileNameVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a <code>SourceFileNameVisitor</code> to set every node's
     * <code>IDENT_KEY</code> property to the given file name.
     *
     * @param value
     *            the <code>String</code> value to give every node's
     *            <code>IDENT_KEY</code> property.
     */
    public SourceFileNameVisitor(String value) {
        super();
        _valueToSet = value;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods

    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        node.setProperty(IDENT_KEY, _valueToSet);
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // private members

    private String _valueToSet = null;
}
