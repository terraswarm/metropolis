/* A connection between two nodes in the elaborated network.

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

 */
package metropolis.metamodel.runtime;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// ////////////////////////////////////////////////////////////////////////
// // Connection

/**
 * This class describes a connection between two nodes in the elaborated
 * network. A connection is described by the "source node" of the connection,
 * the "port" used to make the connection and the "target node" of the
 * connection. When a node is refined, the connections that it has with other
 * nodes might change due to refinement.
 *
 * @author Robert Clariso
 * @version $Id: Connection.java,v 1.29 2006/10/12 20:38:19 cxh Exp $
 */
public class Connection implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new connection between two nodes of the network.
     *
     * @param source
     *            Source node.
     * @param destination
     *            Destination node.
     * @param port
     *            Port used to make the connection.
     * @param net
     *            Netlist where this connection is defined.
     * @param refinedConnection
     *            Connection refined by this connection. It should be set to
     *            null if the nodes are connected using "connect", and set to
     *            the refined connection with "refineconnect" or
     *            "redirectconnect".
     */
    public Connection(INode source, INode destination, IPort port,
            INetlist net, Connection refinedConnection) {
        _source = source;
        _target = destination;
        _port = port;
        _netlist = net;
        _refined = new HashSet();
        _refinement = new HashSet();

        if (refinedConnection != null) {
            _refined.add(refinedConnection);
            refinedConnection._refinement.add(this);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the final connections after all refinements to current connection
     * have been performed.
     *
     * @return The Set of final Connections that will be in effect in the
     *         elaborated network, after all refinements and redirections have
     *         been performed.
     * @see #originalConnection()
     */
    public Set finalConnection() {
        Set finalConnections = new HashSet();
        Set currentConnections = new HashSet();
        currentConnections.addAll(_refinement);

        while (!currentConnections.isEmpty()) {
            Set temporaryConnections = new HashSet();
            Iterator connections = currentConnections.iterator();

            while (connections.hasNext()) {
                Connection connection = (Connection) connections.next();

                if (!connection.isRefined()) {
                    finalConnections.add(connection);
                } else {
                    temporaryConnections.add(connection
                            .getRefinementConnection());
                }
            }

            currentConnections = temporaryConnections;
        }

        return finalConnections;
    }

    /**
     * Get the netlist where this connection has been performed.
     *
     * @return The netlist where this connection has been performed.
     */
    public INetlist getNetlist() {
        return _netlist;
    }

    /**
     * Get the port used in the connection.
     *
     * @return The port used to connect the source and target.
     */
    public IPort getPort() {
        return _port;
    }

    /**
     * Get the connections refined by this connection.
     *
     * @return The Set of Connections refined by this connection in a
     *         "redirectconnect" or "refineconnect", or null if no connection is
     *         being refined/redirected.
     */
    public Set getRefinedConnection() {
        // FIXME: rename to getRefinedConnections() because this method
        // now returns a Set of Connections.
        return _refined;
    }

    /**
     * Get the connections that refines this connection.
     *
     * @return The Set of Connections that refines this connection in a
     *         "redirectconnect" or "refineconnect", or null if this connection
     *         is not refined/redirected.
     */
    public Set getRefinementConnection() {
        // FIXME: rename to getRefinedConnections() because this method
        // now returns a Set of Connections.
        return _refinement;
    }

    /**
     * Get the source of this connection.
     *
     * @return The source node of this connection.
     */
    public INode getSource() {
        return _source;
    }

    /**
     * Get the destination of this connection.
     *
     * @return The destination node of this connection.
     */
    public INode getTarget() {
        return _target;
    }

    /**
     * Test if the connection is refined by another connection.
     *
     * @return true if the connection is refined by another connection.
     */
    public boolean isRefined() {
        return (!_refinement.isEmpty());
    }

    /**
     * Test if the connection is a refinement of another connection.
     *
     * @return true if the connection is a refinement of another connection.
     */
    public boolean isRefinement() {
        return (!_refined.isEmpty());
    }

    /**
     * Get the original connections that were refined by this connection (or
     * connections refined by this connection).
     *
     * @return The Set of original Connections, established using a "connect"
     *         statement, that has been refined successively until this
     *         connection.
     * @see #finalConnection()
     */
    public Set originalConnection() {
        Set originalConnections = new HashSet();
        Set currentConnections = new HashSet();
        currentConnections.addAll(_refined);

        while (!currentConnections.isEmpty()) {
            Set temporaryConnections = new HashSet();
            Iterator currentConnection = currentConnections.iterator();

            while (currentConnection.hasNext()) {
                Connection connection = (Connection) currentConnection.next();
                if (!connection.isRefinement()) {
                    originalConnections.add(connection);
                } else {
                    temporaryConnections.add(connection.getRefinedConnection());
                }
            }

            currentConnections = temporaryConnections;
        }

        return originalConnections;
    }

    /**
     * Get a String providing information about this connection: the source node
     * of the connection, the destination, and the port used in the connection.
     *
     * @return A String providing information about this connection.
     */
    public String show() {
        return show(true, true, true, false);
    }

    /**
     * Get a String providing information about this connection. The parameters
     * customize the information that will be present in the String.
     *
     * @param showSource
     *            True if the String must define the source of the connection.
     * @param showPort
     *            True if the String must show the port used in the connection.
     * @param showTarget
     *            True if the String must throw the target of the connection.
     * @param showRefine
     *            Show information about the connections that refine or are
     *            refined by this connection.
     * @return A String with information about this connection.
     */
    public String show(boolean showSource, boolean showPort,
            boolean showTarget, boolean showRefine) {
        StringBuffer results = new StringBuffer();

        if (showSource) {
            results.append(_source.getName());
        }

        if (showPort) {
            results.append(" --(" + _port.show() + ")--> ");
        } else {
            results.append(" --> ");
        }

        if (showTarget) {
            results.append(_target.getName());
        }

        if (showRefine) {
            if (_refined.isEmpty()) {
                results.append("\no This connection is not refining "
                        + "another connection");
            } else {
                results.append("\no This connection is refining "
                        + "the connection");

                Iterator refineds = _refined.iterator();

                while (refineds.hasNext()) {
                    Connection connection = (Connection) refineds.next();
                    results.append("\n    " + connection.show());
                }
            }

            if (_refinement.isEmpty()) {
                results.append("\no This connection is not further refined");
            } else {
                results.append("\no This connection is further refined by "
                        + "the connection");

                Iterator refinements = _refinement.iterator();

                while (refinements.hasNext()) {
                    Connection connection = (Connection) refinements.next();
                    results.append("\n    " + connection.show());
                }
            }
        }

        return results.toString();
    }

    /**
     * Return a string representation of this Connection.
     *
     * @return A String with information about this Connection.
     */
    public String toString() {
        return getClass().getName() + ": " + show();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Source node of the connection, i.e. node that has a port that is
     * connected to the target node.
     */
    protected INode _source;

    /**
     * Target node of the connection, i.e. node that is connected from another
     * node in the network using a port.
     */
    protected INode _target;

    /** Port used to connect one node to another. */
    protected IPort _port;

    /** Netlist where this connection is performed. */
    protected INetlist _netlist;

    /**
     * Connection refined by this connection. The source or target of the
     * original connection have changed to a node in the refinement, using a
     * "refineconnect" or "redirectconnect" statement. If it is set to null, it
     * means that the connection is not refining any other connection.
     */
    protected Set _refined;

    /**
     * Connection that refines/modifies this connection directly by changing the
     * source/target to a node in the refinement. If it is set to null, it means
     * that the connection is not refined by any other connection.
     */
    protected Set _refinement;
}
