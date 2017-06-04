/* Class processing network commands in the metropolis shell.

 Metropolis: Design Environment for Heterogeneus Systems.

 @Copyright (c) 1998-2005 The Regents of the University of California.
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

package metropolis.metamodel.shell;

import metropolis.metamodel.runtime.INetlist;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.Network;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

// ////////////////////////////////////////////////////////////////////////
// // NetworkCmd
/**
 * This class deals with 'network' commands in the metropolis shell. This
 * commands attempt to display and modify an elaborated network which has been
 * loaded previously.
 *
 * @author Robert Clariso
 * @version $Id: NetworkCmd.java,v 1.27 2006/10/12 20:38:42 cxh Exp $
 */
public class NetworkCmd extends MetropolisCmd {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Build a new NetworkCmd object. */
    public NetworkCmd() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Display or modify the elaborated network structure of a metamodel
     * specification.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the Tcl command.
     * @exception TclException
     *                if the number of arguments is not correct.
     */
    public void cmdProc(Interp interp, TclObject[] objv) throws TclException {

        // Check the number of arguments
        if (objv.length < 2)
            throw new TclNumArgsException(interp, 1, objv, "option optargs");

        // Check that the option is correct
        String option = objv[1].toString();
        int i = 0;
        for (; i < _validOpts.length; i++) {
            if (option.equals(_validOpts[i]))
                break;
        }
        switch (i) {
        case ADDCOMPONENTOPT:
            _addComponentCmd(interp, objv);
            break;
        case CONNECTOPT:
            _connectCmd(interp, objv);
            break;
        case FLATTENOPT:
            _flattenCmd(interp, objv);
            break;
        case REDIRECTCONNECTOPT:
            _redirectConnectCmd(interp, objv);
            break;

        case REFINEOPT:
            _refineCmd(interp, objv);
            break;
        case REFINECONNECTOPT:
            _refineConnectCmd(interp, objv);
            break;
        case SETSCOPEOPT:
            _setScopeCmd(interp, objv);
            break;
        case SHOWOPT:
            _showCmd(interp, objv);
            break;

        default:
            throw new TclException(interp, "bad option '" + option
                    + "': must be addcomponent, connect, flatten, "
                    + " redirectconnect, refine, "
                    + "refineconnect, setscope or show");
        }
    }

    /**
     * Return a help topic for this command.
     *
     * @return A help topic for this command.
     */
    public HelpTopic getHelpTopic() {
        return _helpTopic;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Identifier of the option 'addcomponent'. */
    protected static final int ADDCOMPONENTOPT = 0;

    /** Identifier of the option 'connect'. */
    protected static final int CONNECTOPT = 1;

    /** Identifier of the option 'flatten'. */
    protected static final int FLATTENOPT = 2;

    /** Identifier of the option 'redirectconnect'. */
    protected static final int REDIRECTCONNECTOPT = 3;

    /** Identifier of the option 'refine'. */
    protected static final int REFINEOPT = 4;

    /** Identifier of the option 'refineconnect'. */
    protected static final int REFINECONNECTOPT = 5;

    /** Identifier of the option 'setscope'. */
    protected static final int SETSCOPEOPT = 6;

    /** Identifier of the option 'show'. */
    protected static final int SHOWOPT = 7;

    /** List of valid options of this method. */
    protected static String _validOpts[] = { "addcomponent", "connect",
            "flatten", "redirectconnect", "refine", "refineconnect",
            "setscope", "show" };

    /** Help message for the 'network' command. */
    protected static HelpTopic _helpTopic;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Perform an addcomponent command. Check that the number of parameters is
     * correct, and that the node and netlist have a proper name.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, there are no
     *                nodes/netlists with that name or the addcomponent
     *                statement is incorrect in some way.
     */
    protected void _addComponentCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 4 && objv.length != 5)
            throw new TclNumArgsException(interp, 2, objv,
                    "node netlist [component_name]");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Check that the netlist and node have a correct name
        String nodeName = objv[2].toString();
        String netlistName = objv[3].toString();
        INode node = Network.net.getNode(nodeName);
        INetlist netlist = Network.net.getNetlist(netlistName);
        if (node == null)
            throw new TclException(interp, "there is no node with name '"
                    + nodeName + "', check the node name");
        if (netlist == null)
            throw new TclException(interp, "there is no netlist with name '"
                    + netlistName + "', check the netlist name");
        String compName;
        if (objv.length == 4)
            compName = Network.net.buildName(node.getType(), true);
        else
            compName = objv[4].toString();

        // Perform the addcomponent statement. Catch the possible exception in
        // case the statement is semantically incorrect
        try {
            Network.net.addComponent(node, netlist, compName);
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a connect command. Check that the number of parameters is
     * correct, and that the source and target node exist. Other checks about
     * the correction of the connect statement will be made by the runtime
     * library.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, there are no
     *                nodes with that name or the connect statement is incorrect
     *                in some way.
     */
    protected void _connectCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 5)
            throw new TclNumArgsException(interp, 2, objv,
                    "srcnode port dstnode");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Check that the name of source and target nodes are correct
        String portName = objv[3].toString();
        String srcName = objv[2].toString();
        String dstName = objv[4].toString();
        INode src = Network.net.getNode(srcName);
        INode dst = Network.net.getNode(dstName);
        if (src == null)
            throw new TclException(interp, "there is no node with name '"
                    + srcName + "', check the source node argument.");
        if (dst == null)
            throw new TclException(interp, "there is no node with name '"
                    + dstName + "', check the destination node argument.");

        // Perform the connect statement in the runtime library. Catch the
        // possible exception in case there is a semantic problem with
        // this connection
        try {
            Network.net.connect(src, portName, dst);
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a flatten command. Remove all refinements from the elaborated
     * network, removing the refined nodes and setting the remaining nodes as
     * components of the top-level netlist.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, or there is no
     *                elaborated network.
     */
    protected void _flattenCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 2)
            throw new TclNumArgsException(interp, 2, objv, "");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Perform the flattening if it was not done before
        try {
            if (!Network.net.isFlattened())
                Network.net.flatten();
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a redirect connection command. Check that the number of
     * parameters is correct, and that the netlist and nodes have a proper name.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, there are no
     *                nodes/netlists with that name or the redirectonnect
     *                statement is incorrect in some way.
     */
    protected void _redirectConnectCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 7)
            throw new TclNumArgsException(interp, 2, objv, "netlist src "
                    + "srcport dst dstport");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Check that the netlist, source node and destination node exist
        // with the name provided by the user
        String netlistName = objv[2].toString();
        String srcName = objv[3].toString();
        String dstName = objv[5].toString();
        String srcPortName = objv[4].toString();
        String dstPortName = objv[6].toString();
        INetlist netlist = Network.net.getNetlist(netlistName);
        INode src = Network.net.getNode(srcName);
        INode dst = Network.net.getNode(dstName);
        if (netlist == null)
            throw new TclException(interp, "there is no netlist with name '"
                    + netlistName + "', check the netlist name");
        if (src == null)
            throw new TclException(interp, "there is no node with name '"
                    + srcName + "', check the source node");
        if (dst == null)
            throw new TclException(interp, "there is no node with name '"
                    + dstName + "', check the destination node");

        // Perform the refineconnect statement. Catch the possible exception
        // in case the refineconnect is semantically incorrect
        try {
            Network.net.redirectConnect(netlist, src, srcPortName, dst,
                    dstPortName);
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a refinement command. Check that the number of parameters is
     * correct, and that the netlist and the refined node have the proper name.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, there are no
     *                nodes/netlists with that name or the refine statement is
     *                incorrect in some way.
     */
    protected void _refineCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 4)
            throw new TclNumArgsException(interp, 2, objv, "node netlist");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Check that the name of the netlist and node are correct
        String nodeName = objv[2].toString();
        String netlistName = objv[3].toString();
        INode node = Network.net.getNode(nodeName);
        INetlist netlist = Network.net.getNetlist(netlistName);
        if (node == null)
            throw new TclException(interp, "there is no node with name '"
                    + nodeName + "', check the refined node");
        if (netlist == null)
            throw new TclException(interp, "there is no netlist with name '"
                    + netlistName + "', check the netlist name");

        // Perform the refine statement. Catch the possible exception in case
        // there is something semantically wrong about the refinement
        try {
            Network.net.refine(node, netlist);
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a refine connection command. Check that the number of parameters
     * is correct, and that the netlist and nodes have a proper name.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, there are no
     *                nodes/netlists with that name or the refineconnect
     *                statement is incorrect in some way.
     */
    protected void _refineConnectCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 6)
            throw new TclNumArgsException(interp, 2, objv, "netlist src port "
                    + "dst");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Check that the netlist, source node and destination node exist
        // with the name provided by the user
        String netlistName = objv[2].toString();
        String srcName = objv[3].toString();
        String dstName = objv[5].toString();
        String portName = objv[4].toString();
        INetlist netlist = Network.net.getNetlist(netlistName);
        INode src = Network.net.getNode(srcName);
        INode dst = Network.net.getNode(dstName);
        if (netlist == null)
            throw new TclException(interp, "there is no netlist with name '"
                    + netlistName + "', check the netlist name");
        if (src == null)
            throw new TclException(interp, "there is no node with name '"
                    + srcName + "', check the source node");
        if (dst == null)
            throw new TclException(interp, "there is no node with name '"
                    + dstName + "', check the destination node");

        // Perform the refineconnect statement. Catch the possible exception
        // in case the refineconnect is semantically incorrect
        try {
            Network.net.refineConnect(netlist, src, portName, dst);
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a set scope command. Check that the number of parameters is
     * correct, and that the node and netlist have a proper name.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of parameters is incorrect, there are no
     *                nodes/netlists with that name or the setscope statement is
     *                incorrect in some way.
     */
    protected void _setScopeCmd(Interp interp, TclObject objv[])
            throws TclException {

        // Check the number of parameters
        if (objv.length != 5)
            throw new TclNumArgsException(interp, 2, objv, "node port netlist");

        // Check that there is a elaborated network
        _checkNetwork(interp);

        // Check that the netlist and node have a correct name
        String nodeName = objv[2].toString();
        String portName = objv[3].toString();
        String netlistName = objv[4].toString();
        INode node = Network.net.getNode(nodeName);
        INetlist netlist = Network.net.getNetlist(netlistName);
        if (node == null)
            throw new TclException(interp, "there is no node with name '"
                    + nodeName + "', check the node name");
        if (netlist == null)
            throw new TclException(interp, "there is no netlist with name '"
                    + netlistName + "', check the netlist name");

        // Perform the setscope statement. Catch the possible exception in
        // case the statement is semantically incorrect
        try {
            Network.net.setScope(node, portName, netlist);
        } catch (Exception e) {
            // FIXME: Use chaining exceptions or add stack trace to errorInfo
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Perform a 'show' network command. Check the number of arguments.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Array of parameters of the Tcl command.
     * @exception TclException
     *                if the number of arguments is incorrect, there is no
     *                elaborated network available, or there is no node with
     *                this name.
     */
    protected void _showCmd(Interp interp, TclObject objv[])
            throws TclException {
        // Check the number of arguments
        if (objv.length > 3) {
            throw new TclNumArgsException(interp, 2, objv, "[node]");
        }

        _checkNetwork(interp);

        // Check that the node does exist
        if (objv.length == 2) {
            // No name provided
            INode node = Network.net.getNetInitiator();
            String show = "Top-level netlist:\n" + node.show();
            interp.setResult(show);
        } else {
            // Name provided
            String name = objv[2].toString();
            INode node = Network.net.getNode(name);
            if (node == null)
                throw new TclException(interp, "there is no node with name '"
                        + name + "'");
            interp.setResult(node.show());
        }
    }

    // Static initializer: initialize the help topic
    static {
        String name = "network";
        String summary = "Display/modify the elaborated network";
        String usage[] = { "network show ?node?", "network flatten",
                "network connect src port dst", "network refine node netlist",
                "network refineconnect netlist src port dst",
                "network redirectconnect netlist src srcport " + "dst dstport",
                "network setscope node port netlist",
                "network addcomponent node netlist" };
        String text[] = {
                "This commands provides a mechanism to display/modify a network after",
                "it is elaborated using the 'elaborate' command. All the subcommands",
                "check that the operation that is performed abides by the semantics of",
                "the meta-model.",
                "The available subcommands are:",
                "- show:            display information about a Node of the network;",
                "                   by default, it displays the top-level netlist of",
                "                   the network.",
                "- flatten:         remove all refinements from the network; all",
                "                   refined nodes disappear from the netlist, while",
                "                   the remaining nodes become a component of the",
                "                   top-level netlist.",
                "- connect:         equivalent to a meta-model connect statement.",
                "- refine:          equivalent to a meta-model refine statement.",
                "- refineconnect:   equivalent to a meta-model refineconnect statement.",
                "- redirectconnect: equivalent to a meta-model redirectconnect statement.",
                "- setscope:        equivalent to a meta-model setscope statement.",
                "- addcomponent:    equivalent to a meta-model addcomponent statement." };
        String seeAlso[] = { "elaborate" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);
    }

    // Check that there is an elaborated network
    private void _checkNetwork(Interp interp) throws TclException {
        if (Network.net == null) {
            throw new TclException(interp, "Network.net is null! "
                    + "You must elaborate a network before"
                    + " retrieving information about it.");
        } else if (Network.net.getNetInitiator() == null) {
            throw new TclException(interp, "There is no net initiator. "
                    + "You must elaborate a network before"
                    + " retrieving information about it.");
        }
    }
}
