/* An instance of a node in the elaborated network.

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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // INode

/**
 * Description of an instance of a node in the elaborated network. This class
 * defines the name of the node, the connections with other nodes and the
 * relation of the node with netlists.
 *
 * @author Robert Clariso
 * @version $Id: INode.java,v 1.62 2006/10/12 20:38:24 cxh Exp $
 */
public class INode implements Serializable {
    // FIXME: some of the get* methods return Lists, other return
    // Iterators. We should have one or the other. Perhaps the methods
    // that return Iterators should just be called connections() instead of
    // getConnections()

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new instance of a node of a given type.
     *
     * @param type
     *            The type of the node that is instantiated. This argument
     *            cannot be null, its value is used to build dynamic instances
     *            of ports.
     * @param userObject
     *            Object in the user's network.
     * @param name
     *            The name of the netlist.
     * @param objectID
     *            The ID of the netlist.
     */
    public INode(MMType type, Object userObject, String name, int objectID) {
        if (type == null) {
            throw new NullPointerException("Type argument cannot be null");
        }
        _type = type;
        _userObject = userObject;
        _objectID = objectID;

        // Build the unique name of this instance
        if ((name == null) || (name.length() == 0)) {
            _name = Network.net.buildName(type, false);
        } else {
            _name = name;
        }

        // Build dynamic instances of ports from the static
        // definition available in the type
        Iterator ports = type.portsIterator();

        while (ports.hasNext()) {
            MMPort port = (MMPort) ports.next();

            // Build a dynamic instance of this port in a given node instance.
            // If the dimension of port == 0, we get an IPortScalar.
            // If the dimension of port > 0, we get an IPortScalar.
            // FIXME: We are we instantiating a new object here? Why?
            // It seem like we then have to call
            // ((IPortArray)getPort("name")).allocate() to allocate
            // the port. See test 15-2 in test/INode.tcl
            _ports.put(port.getName(), port.instantiate(this));
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Set the compName of this instance as a component of a netlist.
     *
     * @param netlist
     *            Netlist that contains this node.
     * @param compName
     *            The compName of this node.
     * @see #getCompName(INetlist)
     * @see #removeCompName(INetlist)
     */
    public void addCompName(INetlist netlist, String compName) {
        _compName.put(netlist, compName);
    }

    /**
     * Add a constraint instance to this node. The constraint could be an LTL,
     * LOC, or ELOC constraint. Each constraint in a node will be given a unique
     * sequence number.
     *
     * @param constr
     *            the constraint that needs to be added.
     * @see #getConstraints()
     */
    public void addConstraint(Constraint constr) {
        int n = _constraints.size();
        constr.setIndex(n);
        constr.setContainer(this);
        _constraints.addLast(constr);
    }

    /**
     * Add this instance as a component of another netlist.
     *
     * @param netlist
     *            Netlist that contains this node.
     * @see #getContainers()
     * @see #removeContainer(INetlist)
     */
    public void addContainer(INetlist netlist) {
        _containers.add(netlist);
    }

    /**
     * Add a new connection into this node. The connection need not be unique.
     *
     * @param connection
     *            Connection being added.
     * @see #getInConnections()
     * @see #removeInConnection(Connection)
     */
    public void addInConnection(Connection connection) {
        _inConnections.add(connection);
    }

    /**
     * Perform a new connection from this node to another node.
     *
     * @param connection
     *            Connection being added.
     * @see #getConnection(IPort)
     * @see #getOutConnections()
     * @see #removeOutConnection(Connection)
     */
    public void addOutConnection(Connection connection) {
        _outConnections.add(connection);
    }

    /**
     * Register a new scope connection from a scope port of this node to a
     * netlist.
     *
     * @param connection
     *            Scope connection being added.
     * @see #getScopeConnections()
     */
    public void addScopeConnection(Connection connection) {
        _scopePorts.add(connection);
    }

    /**
     * Update the information about the node considering that its type is now a
     * direct subtype of the current type. This method allows an incremental
     * construction of the node instance across several constructors in a
     * inheritance hierarchy.
     *
     * @param type
     *            The new type that will be set to the node. This type must be a
     *            subclass of the current type and cannot be null.
     * @exception RuntimeException
     *                If the type argument is not a subclass of the current type
     *                or is null.
     *
     */
    public void castToSubType(MMType type) {
        if (type == null) {
            throw new RuntimeException("Node '" + _name + "', of type '"
                    + _type.show(true)
                    + "' cannot be cast to the null subtype.");
        }

        if (!type.isSubClass(_type)) {
            throw new RuntimeException("Internal error: Node '" + _name
                    + "', of type '" + _type.show(true)
                    + "' cannot be an object of type '" + type.show(true)
                    + "', because the second type is not a subclass "
                    + "of the first");
        }

        // Set the type to the new type
        MMType oldType = _type;
        _type = type;

        // Build dynamic instances of the ports that are declared in
        // the new type or its superclasses AND are not declared in the
        // previous type
        do {
            Iterator ports = type.portsIterator();

            while (ports.hasNext()) {
                MMPort port = (MMPort) ports.next();

                if (port.getType() != type) {
                    continue;
                }
                _ports.put(port.getName(), port.instantiate(this));
            }

            type = type.getSuperClass();
        } while ((type != oldType) && (type != null));
    }

    /**
     * Get the compName of this instance as a component of a netlist.
     *
     * @param netlist
     *            Netlist that contains this node.
     * @return The compName of this instance as a component of a netlist.
     * @see #addCompName(INetlist, String)
     * @see #removeCompName(INetlist)
     */
    public String getCompName(INetlist netlist) {
        String name = (String) _compName.get(netlist);

        if (name == null) {
            return "null";
        }

        // throw new RuntimeException("Node does not have this container");
        return name;
    }

    /**
     * Get the final connection (after all refinements) from this node to
     * another node on a given port.
     *
     * @param port
     *            Port of this node.
     * @return The connection on this port that is performed after all
     *         refinements, or null if no connection has been performed yet.
     * @see #getOutConnections()
     * @see #removeOutConnection(Connection)
     */
    public Connection getConnection(IPort port) {
        Iterator outConnections = _outConnections.iterator();

        while (outConnections.hasNext()) {
            Connection connection = (Connection) outConnections.next();

            if (connection.getPort() == port) {
                return connection;
            }
        }

        return null;
    }

    /**
     * Get the list of connection to this instance from a given instance.
     *
     * @param source
     *            Source of the connections.
     * @return A list of Connections that consists of all the connections to
     *         this node from another node. This list can be modified by the
     *         callers.
     */
    public List getConnectionsFrom(INode source) {
        return source.getConnectionsTo(this);
    }

    /**
     * Get the list of connections from this instance to another.
     *
     * @param target
     *            Destination of the connections.
     * @return A list of Connections, that consists of all the connections from
     *         this node to the target node. This list can be modified by the
     *         callers.
     * @see #getUsedNodes()
     */
    public List getConnectionsTo(INode target) {
        LinkedList connections = new LinkedList();

        Iterator outConnections = _outConnections.iterator();
        while (outConnections.hasNext()) {
            Connection connection = (Connection) outConnections.next();

            if (connection.getTarget() == target) {
                connections.add(connection);
            }
        }

        return connections;
    }

    /**
     * Get the list of constraints in the node.
     *
     * @return the constraint list in the node.
     * @see #addConstraint(Constraint)
     */
    public LinkedList getConstraints() {
        return _constraints;
    }

    /**
     * Iterate over all netlists that contain this node instance as a component.
     *
     * @return An Iterator over all the netlists that contain this node.
     * @see #addContainer(INetlist)
     * @see #removeContainer(INetlist)
     */
    public Iterator getContainers() {
        // FIXME: some of the get methods return Lists, the other return
        // Iterators.
        return _containers.iterator();
    }

    /**
     * Iterate over all connections to this node.
     *
     * @return An iterator that will generate all connections to this node.
     * @see #addInConnection(Connection)
     * @see #removeInConnection(Connection)
     */
    public Iterator getInConnections() {
        return _inConnections.iterator();
    }

    /**
     * Get the name of this node. The name of the node identifies it uniquely in
     * the network of processes.
     *
     * @return The name of this node.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the nth input connection that is connected to a node instance through
     * a port that implements a given interface. Several connections from the
     * same object are considered as a different connection. If a connection is
     * refined/redirected by another connection, then it is not taken into
     * account when computing this number.
     *
     * @param intf
     *            Interface.
     * @param n
     *            Number of the connection, starting with 0.
     * @return The n-th connection to this node that implements a given
     *         interface.
     */
    public Connection getNthUser(MMType intf, int n) {
        // FIXME: The notion of nth connection is messed up 0th connection
        // is bogus, we should require n >= 1.
        Iterator inConnections = _inConnections.iterator();
        int i = n;

        while (inConnections.hasNext()) {
            Connection connection = (Connection) inConnections.next();

            // if (con.isRefined()) continue;
            MMType type = connection.getPort().getInterface();

            if (type.implementsInterface(intf)) {
                if (--i <= 0) {
                    return connection;
                }
            }
        }

        throw new RuntimeException(this.getType().getName() + " does not have "
                + n + " input connections implementing interface "
                + intf.getName());
    }

    /**
     * Get the number of nodes that are connected to this node instance through
     * an input port that implements a given interface. Several connections from
     * the same object are considered as different connections. If a connection
     * is refined/redirected by another connection, then it is not taken into
     * account when computing this number.
     *
     * @param intf
     *            Interface.
     * @return The number of nodes that are connected to this node instance
     *         through a given interface.
     */
    public int getNumUsers(MMType intf) {
        Iterator inConnections = _inConnections.iterator();
        int numUsers = 0;

        while (inConnections.hasNext()) {
            Connection connection = (Connection) inConnections.next();

            if (connection.isRefined()) {
                continue;
            }

            MMType type = connection.getPort().getInterface();

            if (type.implementsInterface(intf)) {
                numUsers++;
            }
        }

        return numUsers;
    }

    /**
     * Get the objectID of this node. The objectID of the node identifies it
     * uniquely in the network of processes.
     *
     * @return The objectID of this node.
     */
    public int getObjectID() {
        return _objectID;
    }

    /**
     * Iterate over all connections performed from this node.
     *
     * @return An iterator that will generate all connections from this node.
     * @see #addOutConnection(Connection)
     * @see #getConnection(IPort)
     * @see #removeOutConnection(Connection)
     */
    public Iterator getOutConnections() {
        return _outConnections.iterator();
    }

    /**
     * Get a port with a given name.
     *
     * @param name
     *            The name of the port, which should be of the format
     *            <code><i>name</i>[<i>integer</i>]</code>, where <i>name</i>
     *            is the name of the port and <i>integer</i> is the index or
     *            channel of the port. Note that any number of square bracket
     *            pairs is permissible, so
     *            <code><i>name</i>[<i>integer</i>][<i>integer</i>]</code>
     *            is allowed.
     * @return The port or null if there is no port in this node with that name.
     */
    public IPort getPort(String name) {
        if (name.indexOf(']') != -1 || name.indexOf('[') != -1) {

            StringTokenizer token = new StringTokenizer(name, "[]");
            int count = token.countTokens();

            if (count < 2) {
                throw new RuntimeException("Port name '" + name
                        + "' is invalid, it must have at least two "
                        + "square brackets with integers between them.");
            }

            int[] idx = new int[count - 1];
            String portName = token.nextToken();

            int i = 0;

            while (token.hasMoreTokens()) {
                String number = token.nextToken();
                idx[i] = Integer.parseInt(number);

                if (idx[i] < 0) {
                    throw new RuntimeException("Index '" + idx[i]
                            + "' used to allocate port '" + portName
                            + "' is invalid");
                }

                i++;
            }

            return getPort(portName, idx);
        } else {
            return (IPort) _ports.get(name);
        }
    }

    /**
     * Get a port element inside an array of ports, with a given name and index
     * in the array of ports.
     *
     * @param name
     *            The name of the port.
     * @param idx
     *            An array of indices.
     * @return The IPortElem describing this port.
     * @exception RuntimeException
     *                If this port is not an array of ports.
     * @exception IndexOutOfBoundsException
     *                If the index is outside the bounds of the array.
     */
    public IPort getPort(String name, int[] idx) {
        IPort port = (IPort) _ports.get(name);

        if (port == null) {
            throw new RuntimeException("Port '" + name + "' not found in "
                    + "node '" + _name + "', must be one of\n"
                    + _showComponents());
        }

        if (port.numPorts() == 0) {
            throw new RuntimeException("Array of ports '" + port.show()
                    + "' inside node '" + _name
                    + "' has not been allocated before trying to access "
                    + "element '" + name + _indicesArrayToString(idx)
                    + "', try calling IPortArray.allocate().");
        }

        if (port instanceof IPortArray) {
            IPortArray portArray = (IPortArray) port;
            IPortElem elem = portArray.getElem(idx);

            if (elem == null) {
                throw new RuntimeException("Array of ports '"
                        + portArray.show() + "' inside node '" + _name
                        + "' is allocated, but it does not have element '"
                        + name + _indicesArrayToString(idx) + "'");
            }

            return elem;
        } else {
            throw new RuntimeException("Port '" + name + "' inside node '"
                    + _name + "' is not an array of ports");
        }
    }

    /**
     * Get a list of all connections performed through a given port of this
     * node. This port must NOT be a scope port.
     *
     * @param port
     *            A port of this node.
     * @return A list of all connections performed through a given port.
     */
    public List getPortConnections(IPort port) {
        Iterator iter = _outConnections.iterator();
        LinkedList cons = new LinkedList();

        while (iter.hasNext()) {
            Connection con = (Connection) iter.next();

            if (con.getPort() == port) {
                cons.add(con);
            }
        }

        return cons;
    }

    /**
     * Get the refinement of this node.
     *
     * @return The netlist refining this node or null if there is none.
     * @see #setRefinement(INetlist)
     */
    public INetlist getRefinement() {
        return _refinement;
    }

    /**
     * Get the reference to the netlist in the scope connection through a port.
     *
     * @param port
     *            port through which a node is connected to a netlist
     * @return The reference to the netlist.
     */
    public INetlist getScope(IPort port) {
        Iterator iterator = _scopePorts.iterator();

        while (iterator.hasNext()) {
            Connection conn = (Connection) iterator.next();

            if (conn.getPort() == port) {
                return (INetlist) conn.getTarget();
            }
        }

        return null;
    }

    /**
     * Return an iterator over the ports used to contain the 'scope' in this
     * node.
     *
     * @return An iterator that will generate all scope ports in this node.
     * @see #addScopeConnection(Connection)
     */
    public Iterator getScopeConnections() {
        return _scopePorts.iterator();
    }

    /**
     * Return an iterator over the ports used to contain the 'scope' in this
     * node.
     *
     * @return An iterator that will generate all scope ports in this node.
     * @deprecated Call getScopeConnections(), the name is a better match to
     *             addScopeConnection() and the other add/get methods
     * @see #addScopeConnection(Connection)
     */
    public Iterator getScopePorts() {
        // FIXME: Should this be called getScopeConnections()?
        return getScopeConnections();
    }

    /**
     * Get the type of this node.
     *
     * @return The type of this node.
     */
    public MMType getType() {
        return _type;
    }

    /**
     * Get a set of all node instances connected from this instance. Connections
     * from this node are created with {@link #addOutConnection(Connection)}.
     *
     * @return A set with all nodes that are connected from a port of this
     *         instance. This set can be modified by the callers.
     * @see #getConnectionsTo(INode)
     */
    public Set getUsedNodes() {
        Iterator outConnections = _outConnections.iterator();
        Set used = new HashSet();

        while (outConnections.hasNext()) {
            Connection connection = (Connection) outConnections.next();
            used.add(connection.getTarget());
        }

        return used;
    }

    /**
     * Get a set of all node instances that are connected to this node using a
     * port. Connections to this node are created with
     * {@link #addInConnection(Connection)}.
     *
     * @return A set with all nodes that are connected to this instance using a
     *         port. This set can be modified by the callers.
     */
    public Set getUserNodes() {
        Iterator inConnections = _inConnections.iterator();
        Set user = new HashSet();

        while (inConnections.hasNext()) {
            Connection connection = (Connection) inConnections.next();
            user.add(connection.getSource());
        }

        return user;
    }

    /**
     * Get the object in the user's network from this instance. This object
     * holds information about runtime values of the fields of this object.
     *
     * @return The object in the user's network that corresponds to this node.
     */
    public Object getUserObject() {
        return _userObject;
    }

    /**
     * Test if this instance is used from a given instance using a port.
     *
     * @param source
     *            Source of the connection.
     * @return true if there is a connection from another node to this node
     *         using a port.
     */
    public boolean isConnectedFrom(INode source) {
        return source.isConnectedTo(this);
    }

    /**
     * Test if this instance is connected to a given instance using a port.
     *
     * @param target
     *            Destination of the connection.
     * @return true if this instance is connected to another instance through a
     *         port.
     */
    public boolean isConnectedTo(INode target) {
        Iterator connections = _outConnections.iterator();

        while (connections.hasNext()) {
            Connection connection = (Connection) connections.next();

            if (connection.getTarget() == target) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if this node is refined.
     *
     * @return true iff this node is refined.
     */
    public boolean isRefined() {
        return _refinement != null;
    }

    /**
     * Remove the compName associated with a container already removed from this
     * node. If the compName was not present, then an exception is throws.
     *
     * @param netlist
     *            A netlist that contains this node.
     * @see #addCompName(INetlist, String)
     * @see #getCompName(INetlist)
     */
    public void removeCompName(INetlist netlist) {
        if (_compName.remove(netlist) == null) {
            throw new RuntimeException("Node does not have the container '"
                    + netlist.getName() + "'");
        }
    }

    /**
     * Remove a container from this node.
     *
     * @param netlist
     *            A netlist that contains this node.
     * @see #addContainer(INetlist)
     * @see #getContainers()
     */
    public void removeContainer(INetlist netlist) {
        if (!_containers.remove(netlist)) {
            throw new RuntimeException("Node does not have the container '"
                    + netlist.getName() + "'");
        }
    }

    /**
     * Remove an input connection. The connection is not removed on the source
     * node.
     *
     * @param connection
     *            Input connection to be removed.
     * @see #addInConnection(Connection)
     * @see #getInConnections()
     */
    public void removeInConnection(Connection connection) {
        if (!_inConnections.remove(connection)) {
            throw new RuntimeException("Node does not have this input "
                    + "connection '" + connection + "'");
        }
    }

    /**
     * Remove an output connection. The connection is not removed on the target
     * node.
     *
     * @param connection
     *            Output connection to be removed.
     * @see #addOutConnection(Connection)
     * @see #getOutConnections()
     * @see #getConnection(IPort)
     */
    public void removeOutConnection(Connection connection) {
        if (!_outConnections.remove(connection)) {
            throw new RuntimeException("Node does not have this output "
                    + "connection '" + connection + "'");
        }
    }

    /**
     * Set the refinement of this node.
     *
     * @param netlist
     *            Netlist that refines this node.
     * @see #getRefinement()
     */
    public void setRefinement(INetlist netlist) {
        _refinement = netlist;
    }

    /**
     * Show information about an instance of a node.
     *
     * @return A String with information about the name of the node, the ports
     *         in this node, the connections and the refinements.
     */
    public String show() {
        return show(true, true, true, null);
    }

    /**
     * Show information about an instance of a node in a netlist.
     *
     * @param netlist
     *            The netlist where this node belongs to.
     * @return A String with information about the name of the node, the ports
     *         in this node, the connections and the refinements.
     */
    public String show(INetlist netlist) {
        return show(true, true, true, netlist);
    }

    /**
     * Show information about an instance of a node in the network of processes
     * and media.
     *
     * @param showInCons
     *            Show information about the connections made from other nodes
     *            to this node.
     * @param showOutCons
     *            Show information about the connections made from this node to
     *            other nodes.
     * @param showRefine
     *            Show information about the refinements of this node.
     * @param netlist
     *            Show compName of the node in this netlist
     * @return A String with information about this node.
     */
    public String show(boolean showInCons, boolean showOutCons,
            boolean showRefine, INetlist netlist) {

        // Constraints
        StringBuffer results = new StringBuffer(MMType.show(_type.getKind())
                + " " + _type.getName() + " {\n");

        Iterator connections;

        // Print the instName and
        // compName if netlist is given.
        results.append("  o Instance name: " + _name + "\n");

        if (netlist == null) {
            results.append("  o Component name:\n");

            Enumeration netlists = _compName.keys();

            while (netlists.hasMoreElements()) {
                INetlist nl = (INetlist) netlists.nextElement();
                results.append("        - in netlist " + nl.getName() + ": "
                        + getCompName(nl) + "\n");
            }
        } else {
            results
                    .append("  o component name: " + getCompName(netlist)
                            + "\n");
        }

        // Print information about the components in this node
        // For a node, this means the ports. For a netlist,
        // this means the information about the
        results.append(_showComponents());

        // Print information about refinements. For a node,
        // print the netlists that refine this node. For a netlist,
        // print also the nodes that are refined by this node.
        if (showRefine) {
            results.append(_showRefinements());
        }

        // Print information about the incoming connections
        if (showInCons) {
            if (_inConnections.size() != 0) {
                results.append("  o Input connections:\n");
            }

            connections = _inConnections.iterator();

            while (connections.hasNext()) {
                Connection con = (Connection) connections.next();
                results.append("        - " + con.show(true, true, true, false)
                        + "\n");
            }
        }

        // Print information about the outcoming connections
        if (showOutCons) {
            if (_outConnections.size() != 0) {
                results.append("  o Output connections:\n");
            }

            connections = _outConnections.iterator();

            while (connections.hasNext()) {
                Connection con = (Connection) connections.next();
                results.append("        - " + con.show(true, true, true, false)
                        + "\n");
            }
        }

        // Constraints
        if (_constraints.size() != 0) {
            results.append("  o Constraints:\n");

            Iterator constraints = _constraints.iterator();

            while (constraints.hasNext()) {
                Constraint constr = (Constraint) constraints.next();
                results.append("        - " + constr.show() + "\n");

                // Object constr = constraints.next();
                // results.append(Integer.toString(constr.getIndex()) + " "
                // + Integer.toString(constr.getKind()) + "\n";
            }
        } else {
            results.append("  o No constraints\n");
        }

        return results.toString() + "}";
    }

    /**
     * Return a string representation of this INode.
     *
     * @return A String with information about the name of the node, the ports
     *         in this node, the connections and the refinements.
     */
    public String toString() {
        return show(true, true, true, null);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Print information about the components (ports) of this node.
     *
     * @return A String with information about the ports in this node.
     */
    protected String _showComponents() {
        if (_ports.size() == 0) {
            return "  o No ports\n";
        }

        StringBuffer results = new StringBuffer("  o Ports:\n");
        Iterator ports = _ports.values().iterator();
        while (ports.hasNext()) {
            IPort port = (IPort) ports.next();
            results.append("       " + port.show() + "\n");
        }

        return results.toString();
    }

    /**
     * Print information about the refinements of this node.
     *
     * @return A String with information about the refinements of this node.
     */
    protected String _showRefinements() {
        if (_refinement == null) {
            return "  o Not refined by a netlist\n";
        } else {
            return "  o Refined by netlist " + _refinement.getName() + "\n";
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Unique identifier of this node in the network. This name is based on the
     * name of the type of this node.
     */
    protected String _name;

    /** Unique identifier of this node in the network. */
    protected int _objectID;

    /** Netlist/Name pairs of this node. */
    protected Hashtable _compName = new Hashtable();

    /** Type of this instance of a node class. */
    protected MMType _type;

    /** Object in the user's network. */
    protected Object _userObject;

    /** Ports used to map a 'scope'. */
    protected List _scopePorts = new LinkedList();

    /** Connections that have this node as a target. */
    protected LinkedList _inConnections = new LinkedList();

    /** Connections that have this node as a source. */
    protected LinkedList _outConnections = new LinkedList();

    /** Instances of port in this node instance. */
    protected Hashtable _ports = new Hashtable();

    /** Netlist that refine this node. */
    protected INetlist _refinement = null;

    /** Netlists that have this node as a component. */
    protected Set _containers = new HashSet();

    /** Constraints that are associated with this node. */
    protected LinkedList _constraints = new LinkedList();

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * Return a string description of the array of indices. A one-dimensional
     * string might have a description "[0]", a two-demensional string,
     * "[2][9]".
     *
     * @param idx
     *            An array of indices.
     * @return The string description.
     */
    private String _indicesArrayToString(int[] indices) {
        StringBuffer indexBuffer = new StringBuffer();

        for (int i = 0; i < indices.length; i++) {
            indexBuffer.append("[" + indices[i] + "]");
        }
        return indexBuffer.toString();
    }
}
