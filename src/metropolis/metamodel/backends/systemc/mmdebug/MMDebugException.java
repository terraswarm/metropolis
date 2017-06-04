/* MMDebugException

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

package metropolis.metamodel.backends.systemc.mmdebug;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.TreeNode;

// ////////////////////////////////////////////////////////////////////////
// // MMDebugException
/**
 * This is a <code>RuntimeException</code> representing a problem processing
 * metamodel code for metamodel debugging.
 *
 * @author Allen Hopkins
 * @version $Id: MMDebugException.java,v 1.7 2006/10/12 20:33:23 cxh Exp $
 */
public class MMDebugException extends RuntimeException implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructorsds

    /**
     * Construct a MMDebugException object.
     *
     * @param node
     *            the TreeNode object associated with this exception.
     * @param message
     *            the message.
     */
    public MMDebugException(TreeNode node, String message) {
        super();
        _node = node;
        _message = message;
    }

    /**
     * Construct a MMDebugException object.
     *
     * @param node
     *            the TreeNode object associated with this exception.
     * @param ex
     *            the causing throwable.
     */
    public MMDebugException(TreeNode node, Throwable ex) {
        super(ex);
        _node = node;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods

    public String getMessage() {
        StringBuffer buf = new StringBuffer();
        String fileName = null;
        Object lineNumber = null;

        if (_node != null) {
            fileName = (String) _node.getProperty(IDENT_KEY);
            lineNumber = _node.getProperty(LINENUMBER_KEY);

            if (fileName != null) {
                buf.append(fileName);
                buf.append(": ");
            }

            if (lineNumber != null) {
                buf.append("line ");
                buf.append(lineNumber);
                buf.append(": ");
            }

            buf.append("(");
            buf.append(DebugUtil.getNodeKind(_node));
            buf.append(") ");
        }

        if (getCause() != null) {
            buf.append("Caused by " + getCause().getClass().getName() + ": "
                    + getCause().getMessage() + "; ");
        }

        if (_message != null) {
            buf.append(_message);
        }

        return buf.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // private members

    private String _message = null;

    private TreeNode _node = null;
}
