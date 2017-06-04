/* A utility class for getting information about TreeNodes for debugging.

 Metropolis: Design Environment for Heterogeneus Systems.

 Copyright (c) 2004-2005 The Regents of the University of California.
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
import metropolis.metamodel.NullValue;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.EmptyStmtNode;
import metropolis.metamodel.nodetypes.NameNode;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // DebugUtil
/**
 * A utility class for getting information about <code>TreeNode</code>s for
 * debugging.
 * <p>
 *
 * @author Allen Hopkins
 * @version $Id: DebugUtil.java,v 1.20 2006/10/12 20:33:19 cxh Exp $
 */
public class DebugUtil implements MetaModelStaticSemanticConstants {

    // //////////////////////////////////////////////////////////////////////
    // // public methods

    /**
     * Return <code>true</code> if this node's DONT_DEBUG_ME_KEY property has
     * been set, or if it descends from such a node.
     *
     * @param node
     *            the <code>TreeNode</code> whose ancestry is to be checked.
     * @return <code>true</code> if this is part of a tree that doesn't get
     *         debugging info put in it, else <code>false</code>.
     */
    public static boolean debuggingDefeated(TreeNode node) {
        do {
            if (node.getProperty(DONT_DEBUG_ME_KEY) != null) {
                return (true);
            }
            node = node.getParent();
        } while (!(node instanceof CompileUnitNode));
        return (false);
    }

    /**
     * Print out the contents of the Treenode.
     *
     * @param node
     *            The node that is printed.
     */
    public static void familyTree(TreeNode node) {
        System.err.println("I am a " + getNodeKind(node));
        System.err.print("...at " + getNodeFileName(node));
        System.err.println(":" + getNodeLineNumber(node));
        System.err.println("   with next sibling of "
                + getNodeNextSibling(node));
        for (TreeNode p = node.getParent(); p != null; p = p.getParent()) {
            System.err.println(".. whose parent is a " + getNodeKind(p));
            System.err.println("   with next sibling of "
                    + getNodeNextSibling(p));
        }
    }

    /**
     * Return just the basename of the name of the .mmm file this node comes
     * from.
     *
     * @param node
     *            The TreeNode.
     * @return The basename of the .mmm file this node comes from, or the string
     *         "null" if the IDENT_KEY property cannot be found in this node.
     */
    public static String getNodeFileName(TreeNode node) {
        String fullName = (String) node.getProperty(IDENT_KEY);
        if (fullName == null) {
            return "null";
        } else {
            return fullName.substring(fullName.lastIndexOf(File.separator) + 1);
        }
    }

    /**
     * Return the simple, un-qualified class name of this node.
     *
     * @param node
     *            The TreeNode
     * @return a String describing the kind of node.
     */
    public static String getNodeKind(TreeNode node) {
        String fullName = node.getClass().getName();
        return fullName.substring(fullName.lastIndexOf(".") + 1);
    }

    /**
     * Return the name of this node, if it has one.
     *
     * @param node
     *            The TreeNode
     * @return the name of the node, or the empty string if the node has no
     *         name.
     */
    public static String getNodeName(TreeNode node) {
        Class nodeClass = node.getClass();
        try {
            // Java 1.5: Must cast 2nd arg of getMethod() to Class[] so
            // as to avoid varargs warning.
            java.lang.reflect.Method nameMethod = nodeClass.getMethod(
                    "getName", (Class[]) null);
            // Java 1.5: Must cast 2nd arg of invoke() to Object[] so
            // as to avoid varargs warning.
            NameNode name = (NameNode) nameMethod.invoke(node, (Object[]) null);
            return name.getIdent();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Return the LINENUMBER_KEY property of this node.
     *
     * @param node
     *            The TreeNode
     * @return the line node line number.
     */
    public static String getNodeLineNumber(TreeNode node) {
        StringBuffer buf = new StringBuffer();
        buf.append(node.getProperty(LINENUMBER_KEY));
        return buf.toString();
    }

    /**
     * Return the NEXT_SIBLING_LINENUMBER_KEY property of this node.
     *
     * @param node
     *            The TreeNode
     * @return the line next sibling line number.
     */
    public static String getNodeNextSibling(TreeNode node) {
        StringBuffer buf = new StringBuffer();
        buf.append(node.getProperty(NEXT_SIBLING_LINENUMBER_KEY));
        return buf.toString();
    }

    /**
     * Return the given node's current line number, and simple source-code file
     * name, in the form "blah.mmm:xx".
     *
     * @param node
     *            The <code>TreeNode</code> to get the filename and line
     *            number property for.
     * @return the <code>String</code>, in the form "blah.mmm:xx".
     */
    public static String getCurrentLineNumberString(TreeNode node) {
        Object lineNumberObj = node.getProperty(LINENUMBER_KEY);
        try {
            return getCurrentLineNumberString(node, (Integer) lineNumberObj);
        } catch (ClassCastException ex) {
            if (lineNumberObj instanceof NullValue) {
                return getCurrentLineNumberString(node, null);
            } else {
                throw ex;
            }
        }
    }

    /**
     * Return the given node's current line number, and simple source-code file
     * name, in the form "blah.mmm:xx", where the line number is explicitly
     * supplied.
     *
     * @param node
     *            The <code>TreeNode</code> to get the filename for.
     * @param lineNumber
     *            The explicit <code>Integer</code> line number.
     * @return the <code>String</code>, in the form "blah.mmm:xx".
     */
    public static String getCurrentLineNumberString(TreeNode node,
            Integer lineNumber) {
        StringBuffer buf = new StringBuffer();
        buf.append(getNodeFileName(node));
        buf.append(":");
        buf.append(lineNumber);
        return buf.toString();
    }

    /**
     * Return the index in the given <code>List</code> of the first non-<code>EmptyStmtNode</code>
     * node. If none found, return <code>-1</code>.
     *
     * @param statementList
     *            <code>List</code> to search.
     * @param node
     *            the <code>TreeNode</code> to which the list of statements
     *            belongs, for error reporting in case the list contains a null.
     * @return the index in the <code>List</code> of the first non-empty
     *         statement, or <code>-1</code> if none is found.
     * @exception Throws
     *                <code>MMDebugException</code> if a null member is found
     *                in the list before the first non-<code>EmptyStmtNode</code>.
     */
    public static int getFirstNonemptyStatementIndex(List statementList,
            TreeNode node) {
        if (statementList != null) {
            for (int i = 0; i < statementList.size(); i++) {
                Object statement = statementList.get(i);
                if (statement == null) {
                    throw new MMDebugException(node,
                            "Statement list includes null.");
                }
                if (!(statement instanceof EmptyStmtNode)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Return the index in the given <code>List</code> of the last non-<code>EmptyStmtNode</code>
     * node. If none found, return <code>-1</code>.
     *
     * @param statementList
     *            <code>List</code> to search.
     * @param node
     *            the <code>TreeNode</code> to which the list of statements
     *            belongs, for error reporting in case the list contains a null.
     * @return the index in the <code>List</code> of the last non-empty
     *         statement, or <code>-1</code> if none is found.
     * @exception Throws
     *                <code>MMDebugException</code> if a null member is found
     *                in the list after the last non-<code>EmptyStmtNode</code>.
     */
    public static int getLastNonemptyStatementIndex(List statementList,
            TreeNode node) {
        if (statementList != null) {
            for (int i = statementList.size() - 1; i >= 0; i--) {
                Object statement = (TreeNode) statementList.get(i);
                if (statement == null) {
                    throw new MMDebugException(node,
                            "Statement list includes null.");
                }
                if (!(statement instanceof EmptyStmtNode)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Return a copy of the given <code>String</code>, with each instance of
     * a backslash turned into a slash.
     *
     * @param s
     *            the <code>String</code> to convert
     * @return a copy of the given <code>String</code>, with each backslash
     *         replace by a slash.
     */
    public static String flipBackslashes(String s) {
        StringBuffer buf = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(s, "\\");
        while (tokenizer.hasMoreTokens()) {
            buf.append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens()) {
                buf.append("/");
            }
        }
        return (buf.toString());
    }

    /**
     * Return a copy of the given <code>String</code>, with each instance of
     * a backslash changed to two backslashes.
     *
     * @param s
     *            the <code>String</code> to convert
     * @return a copy of the given <code>String</code>, with each backslash
     *         escaped (preceded) by another backslash.
     */
    /*
     * public static String escapeBackslashes(String s) { StringBuffer buf = new
     * StringBuffer(); StringTokenizer tokenizer = new StringTokenizer(s, "\\");
     * while (tokenizer.hasMoreTokens()) { buf.append(tokenizer.nextToken()); if
     * (tokenizer.hasMoreTokens()) { buf.append("\\\\"); } }
     * return(buf.toString()); }
     */
}
