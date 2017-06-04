/*

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

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.NullValue;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.elaborator.JavaTranslationVisitor;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.AwaitLockNode;
import metropolis.metamodel.nodetypes.AwaitStatementNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.MMType;
import metropolis.metamodel.runtime.Network;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// ////////////////////////////////////////////////////////////////////////
// // ICAwaitTestlistComputeVisitor
/**
 * Collect interleaving concurrent atomicity for test lists in await statement.
 *
 * @author Guang Yang
 * @version $Id: ICAwaitTestlistComputeVisitor.java,v 1.1 2004/08/06 07:53:40
 *          guyang Exp $
 */
public class ICAwaitTestlistComputeVisitor extends TraverseStmtsVisitor {

    /**
     * A list of all ASTs.
     *
     * @param inode
     *            The corresponding INode for the process or medium being
     *            processed.
     */
    public ICAwaitTestlistComputeVisitor(INode inode) {
        _inode = inode;
    }

    /**
     * await statement.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return null
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        List retList = TNLManip.traverseList(this, args, node.getGuards());
        ICASymbolicFlag f = (ICASymbolicFlag) node.getProperty(ICATOMIC_KEY);
        if (f == null)
            f = new ICASymbolicFlag();
        f.addDependingFlag(reduceListToSet(retList));
        node.setProperty(ICATOMIC_KEY, f);

        return null;
    }

    /**
     * One branch of await statement including a guard condition, a test list, a
     * set list and a critical section.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return a IC-atomicity flag
     */
    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {
        List list = TNLManip.traverseList(this, args, node.getLockTest());

        node.getStmt().accept(this, args);

        return list;
    }

    /**
     * A test list includes a list of port.interface pairs.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return a IC-atomicity flag
     */
    public Object visitAwaitLockNode(AwaitLockNode node, LinkedList args) {
        TreeNode port = node.getNode();
        TreeNode intfc = node.getIface();

        //INode desNode;
        String desName;
        int desType;
        if (port instanceof ThisNode) {
            //desNode = _inode;
            desName = _inode.getType().getName();
            desType = _inode.getType().getKind();
        } else {
            String portName = SystemCCodegenVisitor
                    ._stringListToString((List) port.accept(
                            new JavaTranslationVisitor(), args));
            Object desObj = Network.net.getConnectionDest(_inode, portName);
            Class desClass = desObj.getClass();
            desName = desClass.getName();
            String superName = desName;
            while (!superName.startsWith("metamodel.lang.")) {
                desClass = desClass.getSuperclass();
                superName = desClass.getName();
            }
            if (superName.equals("metamodel.lang.Process"))
                desType = MMType.PROCESS;
            else if (superName.equals("metamodel.lang.Medium"))
                desType = MMType.MEDIUM;
            else
                throw new RuntimeException(
                        "Only process and medium are expected.");

            // desNode = Network.net.getNode(desObj);
        }
        UserTypeDeclNode desAST = SystemCBackend._findObjectAST(desName,
                desType);

        Hashtable ica = (Hashtable) desAST.getName().getProperty(ICATOMIC_KEY);
        if (ica == null)
            throw new RuntimeException(
                    "Medium "
                            + desName
                            + " is not yet resolved for interleaving concurrent atomicity.");

        ICASymbolicFlag f = (ICASymbolicFlag) node.getProperty(ICATOMIC_KEY);
        if (f == null)
            f = new ICASymbolicFlag();

        if (intfc instanceof AbsentTreeNode) {
            Iterator iter = ica.values().iterator();
            while (iter.hasNext())
                f.addDependingFlag((ICASymbolicFlag) iter.next());
        } else {
            String intfcName = SystemCCodegenVisitor
                    ._stringListToString((List) intfc.accept(
                            new JavaTranslationVisitor(), args));
            ICASymbolicFlag t = (ICASymbolicFlag) ica.get(intfcName);
            if (t != null)
                f.addDependingFlag(t);
        } // end if

        node.setProperty(ICATOMIC_KEY, f);

        return f;
    }

    /**
     * Reduce a List to a Set The List could include ICASymbolicFlag, Set, List
     * and NullValue.instance.
     *
     * @param list
     *            The input List
     * @return The reduced Set which includes only ICASymbolicFlag If there is
     *         any ICASymbolicFlag evaluted to false already, the reduced Set
     *         includes only a false ICASymbolicFlag.
     */
    protected Set reduceListToSet(List list) {
        Set ret = _reduceListToSet(list);

        Iterator iter = ret.iterator();
        while (iter.hasNext()) {
            ICASymbolicFlag flag = (ICASymbolicFlag) iter.next();
            if (flag.isEvaluated())
                if (!flag.isICA()) {
                    ret.clear();
                    ret.add(flag);
                    return ret;
                }
        }

        return ret;
    }

    /**
     * Reduce a List to a Set The List could include ICASymbolicFlag, Set, List
     * and NullValue.instance.
     *
     * @param list
     *            The input List
     * @return The reduced Set which includes only ICASymbolicFlag
     */
    protected Set _reduceListToSet(List list) {
        Set ret = new HashSet();

        if (list == null)
            return ret;

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof NullValue)
                continue;
            else if (obj instanceof ICASymbolicFlag)
                ret.add(obj);
            else if (obj instanceof Set)
                ret.addAll((Collection) obj);
            else if (obj instanceof List)
                ret.addAll(_reduceListToSet((List) obj));
            else
                throw new RuntimeException(
                        "Unexpected element type in reducing a List to a Set");
        }

        return ret;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * The corresponding INode for the process or medium being processed
     */
    private INode _inode;

}
