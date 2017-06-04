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

import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.AwaitLockNode;

import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ICAwaitOptimizeVisitor
/**
 * Get rid of unnecessary test list and set list checks in interleaving
 * concurrent simulation environment.
 *
 * @author Guang Yang
 * @version $Id: ICAwaitOptimizeVisitor.java,v 1.10 2006/10/12 20:33:02 cxh Exp $
 */
public class ICAwaitOptimizeVisitor extends TraverseStmtsVisitor {

    /**
     * Constructor.
     */
    public ICAwaitOptimizeVisitor() {
    }

    /**
     * one branch of await statement including a guard condition, a test list, a
     * set list and a critical section.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return null
     */
    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {
        ICASymbolicFlag f = (ICASymbolicFlag) ((TreeNode) node.getStmt())
                .getProperty(ICATOMIC_KEY);

        if (f == null)
            return null;

        // IC-atomic critical section implies empty set list
        if (f.getICA())
            node.getLockSet().clear();

        // IC-atomic test list does not need checking at run time
        Iterator iter = node.getLockTest().iterator();
        while (iter.hasNext()) {
            AwaitLockNode testList = (AwaitLockNode) iter.next();
            ICASymbolicFlag t = (ICASymbolicFlag) testList
                    .getProperty(ICATOMIC_KEY);
            if (t.getICA())
                iter.remove();
        }

        node.getStmt().accept(this, args);

        return null;
    }

}
