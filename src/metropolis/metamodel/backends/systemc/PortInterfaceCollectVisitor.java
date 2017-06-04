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

import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.frontend.InterfaceDecl;
import metropolis.metamodel.frontend.MethodDecl;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.PortDecl;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.AwaitLockNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.ThisPortAccessNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // PortInterfaceCollectVisitor
/**
 * Collect port-interface pair info in test lists and set lists used in awaits.
 * (Note: await should be used only in media and processes.)
 *
 * @author Guang Yang
 * @version $Id: PortInterfaceCollectVisitor.java,v 1.9 2004/10/06 00:29:48 cxh
 *          Exp $
 */
public class PortInterfaceCollectVisitor extends TraverseStmtsVisitor {

    public PortInterfaceCollectVisitor() {
        super();
    }

    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {
        TNLManip.traverseList(this, args, node.getLockSet());
        TNLManip.traverseList(this, args, node.getLockTest());
        node.getStmt().accept(this, args);
        return null;
    }

    public Object visitAwaitLockNode(AwaitLockNode node, LinkedList args) {
        // No need to continue, because all port.interface combinations are
        // included.
        // if (_portIntfc.containsKey("all")) return null;

        TreeNode port = node.getNode();
        TreeNode intfc = node.getIface();
        String portName;
        String intfcName;
        if (port instanceof AbsentTreeNode)
            throw new RuntimeException(
                    "'all' is not supported in port.interface pair in await.");
        else if (port instanceof ThisNode)
            portName = "this";
        else if (port instanceof ThisPortAccessNode)
            portName = ((ThisPortAccessNode) port).getName().getIdent();
        else
            throw new RuntimeException(
                    "Unsupported port specification in port.interface pair in await."
                            + port);

        if (intfc instanceof AbsentTreeNode)
            intfcName = "all";
        else if (intfc instanceof NameNode)
            intfcName = ((NameNode) intfc).getIdent();
        else
            throw new RuntimeException(
                    "Unsupported interface specification in port.interface pair in await."
                            + intfc);

        HashSet ifs = (HashSet) _portIntfc.get(portName);
        if (ifs == null)
            ifs = new HashSet();

        PortDecl pdecl = _thisDecl.getPort(portName);
        TypeNameNode tNode = null;
        if (pdecl != null)
            tNode = (TypeNameNode) pdecl.getType();

        if (intfcName.equals("all")) { // replace 'all' with all interfaces
            if (portName.equals("this")) { // this.all must be in a medium
                Iterator implIntfcs = _thisDecl.getInterfaces().iterator();
                while (implIntfcs.hasNext()) {
                    InterfaceDecl idecl = (InterfaceDecl) implIntfcs.next();
                    while (idecl != null) {
                        ifs.add(idecl.getName());
                        idecl = (InterfaceDecl) idecl.getSuperClass();
                    } // end while idecl
                } // end while implIntfcs
            } else { // port.all refer to the port type (an interface ) all
                // super interfaces of the type
                InterfaceDecl idecl = (InterfaceDecl) tNode.getName()
                        .getProperty(DECL_KEY);
                while (idecl != null) {
                    ifs.add(idecl.getName());
                    idecl = (InterfaceDecl) idecl.getSuperClass();
                } // end while idecl
            }
        } else {
            if (pdecl != null) {
                String intfcTypeName = tNode.getName().getIdent();
                if (!intfcTypeName.equals(intfcName))
                    throw new RuntimeException("Port " + portName
                            + " has type " + intfcTypeName
                            + ". However, it was specified as " + intfcName
                            + " in await in medium " + _thisDecl.fullName());
            }
            ifs.add(intfcName);
        }

        _portIntfc.put(portName, ifs);

        return null;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        MethodDecl mdecl = (MethodDecl) node.getName().getProperty(DECL_KEY);
        List overriden = mdecl.getOverridedBy();
        boolean needProc = true;

        Iterator iter = overriden.iterator();
        while (iter.hasNext() & needProc) {
            MethodDecl ord = (MethodDecl) iter.next();
            if (_processedMethods.contains(ord))
                needProc = false;
        }
        if (needProc) {
            node.getBody().accept(this, args);
            _processedMethods.add(mdecl);
        }
        return null;
    }

    public Object visitUserTypeDeclNode(UserTypeDeclNode node, LinkedList args) {
        _thisDecl = (ObjectDecl) node.getName().getProperty(DECL_KEY);
        TNLManip.traverseList(this, args, node.getMembers());
        ObjectDecl odecl = (ObjectDecl) node.getName().getProperty(DECL_KEY);
        odecl = odecl.getSuperClass();
        if (odecl != null) {
            TreeNode n = odecl.getSource();
            n.accept(this, args);
        }
        return _portIntfc;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /*
     * port-interface pair info Key: port name Value: a HashSet of all
     * interfaces associated with the Key
     */
    private Hashtable _portIntfc = new Hashtable();

    /**
     * When walking up the inheritance hierarchy, store all methods that have
     * been processed.
     */
    private HashSet _processedMethods = new HashSet();

    private ObjectDecl _thisDecl;

}
