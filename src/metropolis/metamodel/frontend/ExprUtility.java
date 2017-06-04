/* Utility methods that deal with expressions.

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
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.ExprNode;
import metropolis.metamodel.nodetypes.IntLitNode;
import metropolis.metamodel.nodetypes.ObjectFieldAccessNode;
import metropolis.metamodel.nodetypes.ObjectParamAccessNode;
import metropolis.metamodel.nodetypes.ObjectPortAccessNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.TypeNameNode;

// ////////////////////////////////////////////////////////////////////////
// // ExprUtility
/**
 * Utility methods that are related to expressions.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ExprUtility.java,v 1.16 2006/10/12 20:33:37 cxh Exp $
 */
public class ExprUtility implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Public constructor allows inheritance of methods although this class has
     * no instance members.
     */
    public ExprUtility() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return a resolved node corresponding to the object that FieldAccessNode,
     * PortAccessNode or ParameterAccessNode accesses.
     *
     * @param node
     *            Node with a field, parameter or port access.
     * @return A resolved node with information about the object being accessed,
     *         i.e. the object that has that port, field or parameter.
     * @exception RuntimeException
     *                if the access expression is not a FieldAccessNode,
     *                PortAccessNode or ParameterAccessNode.
     */
    public static TreeNode accessedObject(TreeNode node) {
        switch (node.classID()) {
        case OBJECTFIELDACCESSNODE_ID:
            return ((ObjectFieldAccessNode) node).getObject();
        case OBJECTPORTACCESSNODE_ID:
            return ((ObjectPortAccessNode) node).getObject();
        case OBJECTPARAMACCESSNODE_ID:
            return ((ObjectParamAccessNode) node).getObject();
        case THISFIELDACCESSNODE_ID:
        case SUPERFIELDACCESSNODE_ID:
        case THISPORTACCESSNODE_ID:
        case SUPERPORTACCESSNODE_ID:
        case THISPARAMACCESSNODE_ID:
        case SUPERPARAMACCESSNODE_ID:
            TreeNode retval = new ThisNode();
            TypeNameNode tn = (TypeNameNode) node.getProperty(THIS_CLASS_KEY);
            if (tn != null)
                retval.setProperty(THIS_CLASS_KEY, tn);
            return retval;
        default:
            break;
        }
        // FIXME: Only checked for debugging purposes
        throw new RuntimeException("Node is not a field acess");
    }

    /**
     * Get the integer value stored in a IntLitNode.
     *
     * @param litNode
     *            Integer literal.
     * @return The int value stored in the constant.
     */
    public static int intValue(IntLitNode litNode) {
        String literal = litNode.getLiteral();
        return Integer.decode(literal).intValue();
    }

    /**
     * Return true iff a expression is an integer constant with a a value
     * between 'from' and 'to'.
     *
     * @param exp
     *            Expression being analyzed.
     * @param from
     *            Lower bound of the integer value.
     * @param to
     *            Upper bound of the integer value.
     * @return true iff the expression is an integer constant with a value
     *         between the given bounds.
     */
    public static boolean isIntConstant(ExprNode exp, int from, int to) {
        if (exp instanceof IntLitNode) {
            int value = intValue((IntLitNode) exp);
            return ((value >= from) && (value <= to));
        }
        return false;
    }

    /**
     * Return true iff the ExprNode is a statement expression, that is, may
     * appear as a legal statement if a semicolon is appended to the end.
     *
     * @param exp
     *            Expression being analyzed.
     * @return true iff the expression is a statement expression.
     */
    public static boolean isStatementExpression(ExprNode exp) {
        switch (exp.classID()) {
        case ASSIGNNODE_ID:
        case MULTASSIGNNODE_ID:
        case DIVASSIGNNODE_ID:
        case REMASSIGNNODE_ID:
        case PLUSASSIGNNODE_ID:
        case MINUSASSIGNNODE_ID:
        case LEFTSHIFTLOGASSIGNNODE_ID:
        case RIGHTSHIFTLOGASSIGNNODE_ID:
        case RIGHTSHIFTARITHASSIGNNODE_ID:
        case BITANDASSIGNNODE_ID:
        case BITXORASSIGNNODE_ID:
        case BITORASSIGNNODE_ID:
        case PREINCRNODE_ID:
        case PREDECRNODE_ID:
        case POSTINCRNODE_ID:
        case POSTDECRNODE_ID:
        case METHODCALLNODE_ID:
        case ALLOCATENODE_ID:
        case ALLOCATEANONYMOUSCLASSNODE_ID:
            return true;
        default:
            return false;
        }
    }

}
