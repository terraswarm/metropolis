/* A back-end that generates SystemC code from an AST.

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

package metropolis.metamodel.backends.runtimetest;

import metropolis.metamodel.backends.elaborator.CustomClassLoader;
import metropolis.metamodel.backends.elaborator.CustomObjectInputStream;
import metropolis.metamodel.backends.elaborator.ElaboratorBackend;
import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.runtime.INetlist;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.Network;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // RuntimeTestBackend
/**
 * A back-end that is used to test runtime library
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Xi Chen
 * @version $Id: RuntimeTestBackend.java,v 1.28 2006/10/12 20:32:51 cxh Exp $
 */
public class RuntimeTestBackend extends ElaboratorBackend {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Create a new Runtime Test back-end. */
    public RuntimeTestBackend() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Implement method 'invoke()' from the Backend interface. It will initiate
     * a Network object from ASTs and call the method of Network object to test
     * them.
     *
     * @param args
     *            List of arguments; expected empty.
     * @param sources
     *            List of compiled ASTs.
     */
    public void invoke(List args, List sources) {

        // Get the list of all sources loaded by the FileLoader
        // All sources should be loaded until pass 2, so that they
        // are translated by the elaboration backend
        List allSources = FileLoader.getCompiledSources(1);
        Iterator iter = allSources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            if (pkg == MetaModelLibrary.UTIL_PACKAGE)
                continue;
            String fileName = (String) ast.getProperty(IDENT_KEY);
            FileLoader.loadCompileUnit(fileName, 2);
        }
        allSources = FileLoader.getCompiledSources(2);

        //iter = allSources.iterator();
        //while (iter.hasNext()) {
        //    CompileUnitNode ast = (CompileUnitNode) iter.next();
        // String fileName = (String) ast.getProperty(IDENT_KEY);
        // FileLoader.loadCompileUnit(fileName, 2);
        // _emitCode(ast);
        //}

        String select = null;
        if (args.size() == 2)
            select = (String) args.remove(0);

        super.invoke(args, allSources);

        String inputFileName = _tmpRoot.toString() + File.separator + "NET";
        try {
            CustomClassLoader loader = new CustomClassLoader(_tmpRoot
                    .toString());
            FileInputStream in = new FileInputStream(inputFileName);
            CustomObjectInputStream ois = new CustomObjectInputStream(in,
                    loader);
            net = Network.restore(ois);
            in.close();
        } catch (IOException ex) {
            throw new RuntimeException("Error reading the elaborated network '"
                    + inputFileName + "'", ex);
        }

        if (select.equals("runtime")) {
            System.out.println("\nOutput for testing Network object ...\n");

            System.out
                    .println("\n1. Print out layout of the netlist by calling Network.show() ... \n");
            System.out.println(net.show());

            System.out
                    .println("\n2. INode node = Network.getNode(\"InstIntM\")\n");
            INode node = net.getNode("InstIntM");
            if (node != null)
                System.out.println(node.getName());
            else
                System.out.println("null");

            System.out.println("\n3. INode node1 = Network.getNode(\"ss\")\n");
            INode node1 = net.getNode("ss");
            if (node1 != null)
                System.out.println(node1.getName());
            else
                System.out.println("null");

            System.out.println("\n4. INode node2 = Network.getNode(\"M2\")\n");
            INode node2 = net.getNode("M2");
            if (node2 != null)
                System.out.println(node2.getName());
            else
                System.out.println("null");

            System.out
                    .println("\n5. INetlist netlist = Network.getNetlist(\"top_level_netlist\")\n");
            INetlist netlist = net.getNetlist("top_level_netlist");
            if (netlist != null)
                System.out.println(netlist.getName());
            else
                System.out.println("null");

            System.out
                    .println("\n6. INetlist netlist1 = Network.getNetlist(\"refNet\")\n");
            INetlist netlist1 = net.getNetlist("refNet");
            if (netlist1 != null)
                System.out.println(netlist1.getName());
            else
                System.out.println("null");

            System.out.println("\n7. test Network.getInstName(node)\n");
            System.out.println(net.getInstName(node));

            System.out
                    .println("\n8. test Network.getCompName(node, netlist)\nalso test INode.getCompName()");
            if (netlist != null)
                System.out.println(net.getCompName(node, netlist));
            else
                System.out.println("null");

            System.out
                    .println("\n9. test Network.getCompName(\"InstIntM\", \"top_level_netlist\")\n");
            System.out
                    .println(net.getCompName("InstIntM", "top_level_netlist"));

            System.out.println("\n10. test  Network.getType(node)\n");
            if (node != null)
                System.out.println(net.getType(node).getKind());
            else
                System.out.println("null");

            System.out
                    .println("\n11. test  Network.isConnectionRefined(\"Producer0\", \"port0\", \"DummyR\")\n");

            try {
                System.out.println(net.isConnectionRefined("Producer0",
                        "port0", "DummyR"));
            } catch (Exception e) {
                System.out.println(e);
            }

            System.out
                    .println("\n12. test  Network.isConnectionRefined(\"Producer0\", \"port1\", \"InstIntM\")\n");
            try {
                System.out.println(net.isConnectionRefined("Producer0",
                        "port1", "InstIntM"));
            } catch (Exception e) {
                System.out.println(e);
            }

            System.out
                    .println("\n13. test  Network.getNthPort(\"Producer0\", \"IntWriter\", 0) ... not completed \n");
            // System.out.println(((IPort)net.getNthPort("Producer0",
            // "IntWriter", 0)).getName());

            System.out
                    .println("\n14. test  Network.getPortNum(\"Producer1\", \"test_runtime.IntReader\")\n");
            System.out.println(net.getPortNum("Producer1",
                    "test_runtime.IntReader"));

            System.out
                    .println("\n15. test  Network.getComponent(\"top_level_netlist\", \"MEDIUM\") ... not completed \n");
            System.out.println(((INode) net.getNode(net.getComponent(
                    "top_level_netlist", "MEDIUM"))).getName());

            System.out
                    .println("\n16. test  Network.getConnectionDest(\"Consumer\", \"port1\") ... not completed \n");
            System.out.println(((INode) net.getNode(net.getConnectionDest(
                    "Consumer", "port1"))).getName());

            System.out
                    .println("\n17. test  Network.getScope(\"ss\", \"portScope\") ... not completed \n");
            System.out.println(((INetlist) net.getNode(net.getScope("ss",
                    "portScope"))).getName());

            System.out.println("\n18. test  node1.show(netlist1)\n");
            if (node1 != null && netlist1 != null)
                System.out.println(node1.show(netlist1));

            System.out.println("\n19. test  node2.show(netlist1)\n");
            if (node2 != null && netlist1 != null)
                System.out.println(node2.show(netlist1));

            System.out.println("\n20. test  Network.isFlattened()\n");
            System.out.println(net.isFlattened());

            System.out.println("\n21. test  Network.flatten()\n");
            net.flatten();

            System.out.println("\n22. test  Network.isFlattened()\n");
            System.out.println(net.isFlattened());

            System.out
                    .println("\n23. call Network.show() to print out the layout of the netlist after flatten ... \n");
            System.out.println(net.show());
        } else if (select.equals("constraint"))
            System.out.println(net.show());
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Network restored from deserialization. */
    protected Network net = null;

    /** Objects that already initialized. */
    protected Hashtable instName = new Hashtable();
}
