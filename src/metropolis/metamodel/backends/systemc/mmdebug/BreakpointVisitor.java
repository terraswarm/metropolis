/* A visitor to collect the names of metamodel objects that produce CPP
 classes with executable methods.  This information is used by the metamodel
 debugger.

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
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.NullValue;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.systemc.SystemCCodegenVisitor;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ParameterNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // BreakpointVisitor
/**
 * This is a <code>MetaModelVisitor</code> that collects the names of
 * metamodel objects that become CPP classes with executable methods. These
 * class names are used by the metamodel debugger to bootstrap itself to be able
 * to set breakpoints by line number in files where it has not yet executed any
 * code.
 *
 * @author Allen Hopkins
 * @version $Id: BreakpointVisitor.java,v 1.17 2006/10/12 20:33:21 cxh Exp $
 */
public class BreakpointVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors
    /**
     * Constructs a BreakpointVisitor object.
     *
     * @param breakpointList
     *            A <code>List</code> to be filled with the breakpoint
     *            commands this visitor creates.
     */
    public BreakpointVisitor(List breakpointList) {
        _breakpointList = breakpointList;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods

    /**
     * Create a breakpoint command for a <code>ClassDeclNode</code>.
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitBreakpointTargetObjectNode(node, args);
    }

    /**
     * Create a breakpoint command for a <code>MediumDeclNode</code>.
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitBreakpointTargetObjectNode(node, args);
    }

    /**
     * Create a breakpoint command for a <code>NetlistDeclNode</code>.
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitBreakpointTargetObjectNode(node, args);
    }

    /**
     * Create a breakpoint command for a <code>ProcessDeclNode</code>.
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitBreakpointTargetObjectNode(node, args);
    }

    /**
     * Create a breakpoint command for a <code>QuantityDeclNode</code>.
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return _visitBreakpointTargetObjectNode(node, args);
    }

    /**
     * Create a breakpoint command for a <code>SMDeclNode</code>.
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitBreakpointTargetObjectNode(node, args);
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods

    /**
     * Add a breakpoint command to the list for this <code>ClassDeclNode</code>'s
     * first encountered method, if it has one.
     *
     * @param node
     *            a <code>ClassDeclNode</code>
     * @param args
     *            an unused <code>LinkedList</code>
     * @return <code>null</code>
     */
    private Object _visitBreakpointTargetObjectNode(ClassDeclNode node,
            LinkedList args) {
        StringBuffer buf = null;
        // LinkedList codeList = null;
        Object member = null;
        Iterator memberIter = null;
        List memberList = null;
        String methodName = null;
        ParameterNode param = null;
        Iterator params = null;
        List paramList = null;
        // TypeNode typeNode = null;

        memberList = node.getMembers();
        memberIter = memberList.iterator();

        while (memberIter.hasNext()) {
            member = memberIter.next();
            if (member instanceof MethodDeclNode) {
                //
                // Do all this only once, then break out of while-loop, below.
                //
                buf = new StringBuffer("break ");
                buf.append(node.getName().getIdent());
                buf.append("::");
                methodName = ((MethodDeclNode) member).getName().getIdent();
                buf.append(methodName);
                if (methodName.equals("thread")
                        && node instanceof ProcessDeclNode) {
                    buf.append("()");
                } else {
                    buf.append("(" + _ADDED_PARAMS);
                    paramList = ((MethodDeclNode) member).getParams();
                    params = paramList.iterator();
                    while (params.hasNext()) {
                        param = (ParameterNode) params.next();
                        buf.append(", ");

                        buf.append(Modifier.toString(param.getModifiers()));

                        List defTypeList = (List) param.getDefType().accept(
                                new SystemCCodegenVisitor(), args);
                        buf.append(_stringListToString(defTypeList));

                        TreeNode baseType = param.getDefType();
                        while (baseType instanceof ArrayTypeNode) {
                            baseType = ((ArrayTypeNode) baseType).getBaseType();
                        }
                    }
                    buf.append(")");
                }
                _breakpointList.add(buf.toString());
                break;
            }
        }
        return null;
    }

    /**
     * Converts a list of strings to a string.
     *
     * @param stringList
     *            A list of strings to be converted.
     * @return the converted string.
     */
    protected static String _stringListToString(List stringList) {
        // This is duplicated from ../SystemCCodegenVisitor.java.
        if (stringList == null)
            return new String("");

        Iterator stringItr = stringList.iterator();
        StringBuffer sb = new StringBuffer();

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List || stringObj instanceof String) {
                String s = null;
                if (stringObj instanceof List) {
                    // only use separators for top level
                    s = _stringListToString((List) stringObj);
                } else {
                    s = (String) stringObj;
                }

                sb.append(s);

            } else if (stringObj != null && !(stringObj instanceof NullValue)) {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }

        return sb.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // private members

    private static final String _ADDED_PARAMS = "process*";

    /**
     * The list of breakpoint commands to be built up by this
     * <code>Visitor</code>. This is passed in by the constructor.
     */
    private List _breakpointList = null;
}
