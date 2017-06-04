/* Elaborated network of a meta-model specification.

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

import metropolis.metamodel.StringManip;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // Network

/**
 * This class describes the elaborated network in the meta-model specification,
 * and offers methods to implement meta-model constructs easily. For example,
 * redirectconnect is implemented by method redirectConnect(), etc.
 *
 * @author Robert Clariso, Contributor: Christopher Brooks
 * @version $Id: Network.java,v 1.126 2006/10/12 20:38:33 cxh Exp $
 */
public class Network implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    /** Current instance of the network that we are working with. */
    public static Network net = new Network();

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add an annotation to the annotation list of the network. If the event is
     * null, it is not added to the annotation list.
     *
     * @param event
     *            the event associated with the annotation.
     * @param variableName
     *            the variable name of the annotation.
     * @see #getAnnotations()
     */
    public void addAnnotation(Event event, String variableName) {
        if (event == null) {
            return;
        }

        LinkedList vars;
        addVariable(variableName);

        Enumeration events = _annotations.keys();

        while (events.hasMoreElements()) {
            Event current = (Event) events.nextElement();

            if (current.equals(event)) {
                vars = (LinkedList) _annotations.get(current);

                Iterator variables = vars.iterator();

                while (variables.hasNext()) {
                    String variable = (String) variables.next();

                    if (variable.equals(variableName)) {
                        return;
                    }
                }

                vars.addLast(variableName);

                return;
            }
        }

        vars = new LinkedList();
        vars.addLast(variableName);
        _annotations.put(event, vars);
    }

    /**
     * Perform a 'addcomponent' statement in the network. Register that the node
     * is now a new component of the netlist, and that the netlist is now a new
     * container of the component, also set the compName of the node in this
     * netlist.
     *
     * @param nodeObject
     *            Node added to the netlist. The nodeObject must already have
     *            been added to the network using
     *            {@link #addNode(Object, INode)}. The value of the nodeObject
     *            parameter cannot be null.
     * @param netlistObject
     *            Netlist. The netlistObject must must already have been added
     *            to the network using {@link #addNode(Object, INode)}. The
     *            value of the netlistObject parameter cannot be null and must
     *            be an instance of INetlist.
     * @param compName
     *            The compName of node in this particular netlist.
     * @see #addComponent(INode, INetlist, String)
     * @see #getComponent(Object, String)
     * @see #getComponent(String, String)
     */
    public void addComponent(Object nodeObject, Object netlistObject,
            String compName) {
        INode node = null;
        INetlist netlist = null;

        node = getNodeSafely(nodeObject);
        netlist = getNetlist(netlistObject);
        if (netlist == null) {
            // Throw this exception here to avoid a NullPointerException
            // in addComponent(INode, INetlist, String);
            throw new RuntimeException("Could not find netlist '"
                    + netlistObject + "' in the node instances. "
                    + "Perhaps it has not been added with addNode()?");
        }
        addComponent(node, netlist, compName);
    }

    /**
     * Perform a 'addcomponent' statement in the network. Register that the node
     * is now a new component of the netlist, and that the netlist is now a new
     * container of the component, also set the compName for this node in this
     * particular netlist.
     *
     * @param node
     *            The Node to be added to the netlist. The value of this
     *            parameter cannot be null.
     * @param netlist
     *            The netlist. The value of this parameter cannot be null.
     * @param compName
     *            The compName of node in this particular netlist.
     * @see #addComponent(Object, Object, String)
     * @see #getComponent(String, String)
     */
    public void addComponent(INode node, INetlist netlist, String compName) {
        String name;

        if (compName.length() != 0) {
            // check the uniqueness of the compName in this netlist
            Iterator comps = netlist.getComponents();

            while (comps.hasNext()) {
                INode comp = (INode) comps.next();
                String other = "";

                other = comp.getCompName(netlist);

                if (compName.equals(other)) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("addcomponent: component name '"
                            + compName + "' for node '" + node.getName()
                            + "' is not unique in netlist '"
                            + netlist.getName() + "'.");
                }
            }

            name = compName;
        } else {
            name = buildName(node.getType(), true);
        }

        node.addContainer(netlist);
        node.addCompName(netlist, name);
        netlist.addComponent(node);
    }

    /**
     * Add a constraint to the constraint list of the network.
     *
     * @param constraint
     *            the constraint that needs to be added.
     * @see #getConstraints()
     */
    public void addConstraint(Constraint constraint) {
        if (constraint == null) {
            return;
        }

        /*
         * Iterator iter = _constraints.iterator(); while (iter.hasNext()) {
         * Constraint c = (Constraint)iter.next(); if
         * (c.getSource().equals(con.getSource())) return; }
         */
        _constraints.addLast(constraint);
    }

    /**
     * Register a new node instance in the network.
     *
     * @param object
     *            New node of the network.
     * @param instance
     *            Object that contains information about this instance.
     * @see #getNode(Object)
     * @see #getNode(String)
     * @see #getNodeSafely(Object)
     * @see #getNodeInstances()
     * @see #getNodes()
     */
    public void addNode(Object object, INode instance) {
        _nodeInstances.put(object, instance);
        _nodeNames.put(instance.getName(), instance);
    }

    /**
     * Add the information about a type.
     *
     * @param type
     *            Type.
     * @see #getType(String)
     */
    public void addType(MMType type) {
        _nodeTypes.put(type.getName(), type);
    }

    /**
     * Add a variable name to the variable set. If the variable name is null,
     * then nothing is added to the set.
     *
     * @param variableName
     *            the variable name that is added.
     * @see #getVariables()
     */
    public void addVariable(String variableName) {
        if (variableName == null) {
            return;
        }

        _variables.add(variableName);
    }

    /**
     * Get a new name for a node instance. Check that the name is not being used
     * by any other node instance in the network.
     *
     * @param type
     *            The type of the node instance.
     * @param forCompName
     *            Build instName for a node if forCompName equals true,
     *            otherwise generate an instName.
     * @return The name for the node instance.
     */
    public String buildName(MMType type, boolean forCompName) {
        String name = type.getName();
        name = StringManip.partAfterLast(name, '.');

        if (forCompName) {
            name = name + "_Comp_";
        }

        int count = 0;

        if (_instanceCount.containsKey(name)) {
            Integer val = (Integer) _instanceCount.get(name);
            count = val.intValue() + 1;
        }

        _instanceCount.put(name, new Integer(count));

        return name + count;
    }

    /**
     * Perform a 'connect' statement in the network. Register that the two
     * objects are now connected by a given port.
     *
     * @param srcObject
     *            Node whose port is connected to the target. The srcObject
     *            should have INode fields that have names like
     *            <code>__pointer_<i>srcPort</i></code>, where
     *            <code><i>srcPort</i></code> is the value of the srcPort
     *            argument to this method. Other field names may be possible.
     *            The srcObject must already have been added to the network
     *            using {@link #addNode(Object, INode)}.
     * @param srcPort
     *            Port connected to the target. In the simplest case, this
     *            parameter is a string that names the port.
     * @param destObject
     *            Destination of the connection. The destObject must already
     *            have been added to the network using
     *            {@link #addNode(Object, INode)}.
     * @see #connect(INode, String, INode)
     * @see #connect(INode, IPort, INode)
     */
    public void connect(Object srcObject, Object srcPort, Object destObject) {
        INode src = null;
        IPort port = null;
        INode dst = null;
        String spn = _getPortName(srcObject, srcPort);

        src = getNodeSafely(srcObject);
        port = _findPort(src, spn);
        dst = getNodeSafely(destObject);

        _connectPortPointer(srcObject, srcPort, destObject);
        connect(src, port, dst);
    }

    /**
     * Perform a 'connect' statement in the network. Register that the two
     * objects are now connected by a given port.
     *
     * @param src
     *            Node whose port is connected to the target.
     * @param srcPortName
     *            The name of the Port connected to the target.
     * @param dest
     *            Destination of the connection.
     * @see #connect(Object, Object, Object)
     * @see #connect(INode, IPort, INode)
     */
    public void connect(INode src, String srcPortName, INode dest) {
        IPort srcPort = src.getPort(srcPortName);
        if (srcPort == null) {
            // Throw this exception here to avoid a NullPointerException
            // in connect(INode, IPort, INode);
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("connect: port '" + srcPortName
                    + "' is not found in '" + src.getName()
                    + "' source INode was:\n"
                    + src.show(false, false, false, null));
        }
        connect(src, srcPort, dest);
    }

    /**
     * Perform a 'connect' statement in the network. Register that the two
     * objects are now connected by a given port.
     *
     * @param src
     *            Node whose port is connected to the target.
     * @param srcPort
     *            Port connected to the target.
     * @param dest
     *            Destination of the connection.
     * @see #connect(INode, String, INode)
     * @see #connect(Object, Object, Object)
     */
    public void connect(INode src, IPort srcPort, INode dest) {
        // Check that the port belongs to the source node
        if (srcPort.getContainer() != src) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("connect: port '" + srcPort.show()
                    + "' does not belong to node '" + src.getName()
                    + "', but to node '" + srcPort.getContainer().getName()
                    + "'");
        }

        // Check that the target node implements the interface
        // defined by the port
        if (!dest.getType().implementsInterface(srcPort.getInterface())) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("connect: target '" + dest.getName()
                    + "' of type '" + dest.getType().getName() + "' does not "
                    + "implement the interface '"
                    + srcPort.getInterface().getName() + "' of the port '"
                    + srcPort.getName() + "'");
        }

        // Check that there aren't other connections using this port
        Connection c = src.getConnection(srcPort);

        if (c != null) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("connect: port '" + srcPort.show()
                    + "' already connected to node '" + c.getTarget().getName()
                    + "', so it cannot be connected"
                    + " again to another node, '" + dest.getName() + "'");
        }

        // Perform the connection after checking correctness
        c = new Connection(src, dest, srcPort, null, null);
        src.addOutConnection(c);
        dest.addInConnection(c);

        /*
         * try { Object srcObj = src.getUserObject(); Object destObj =
         * dest.getUserObject(); Class cls = srcObj.getClass(); boolean flag =
         * false; Field f = null; Field g = null;
         *
         * do { try { f = cls.getDeclaredField(srcPort.getName()); flag = true; }
         * catch (NoSuchFieldException e) { cls = cls.getSuperclass(); if
         * (cls.getName().equals("java.lang.Object")) throw e; } } while
         * (!flag);
         *
         * f.setAccessible(true); f.set(srcObj, destObj); } catch
         * (NoSuchFieldException ex) { throw new
         * RuntimeException("Network.connect() failed" + "In process " +
         * src.getName() + ", port '" + srcPort.getName() + "' does not
         * exist."); } catch (IllegalAccessException e) { throw new
         * RuntimeException("Network.connect() failed"); }
         */
    }

    /**
     * Flatten the elaborated network into a network where refined nodes and
     * connections are replaced by their refinements.
     *
     * @return The netlist that contains the entire system, without refinements
     *         or redirections.
     * @see #isFlattened()
     */
    public INetlist flatten() {
        if (isFlattened()) {
            return _netInitiator;
        }

        // Flatten netlists recursively, starting from the
        // net-initiator netlist.
        _flattenConnections(_netInitiator);
        _flattenNetlist(_netInitiator);

        _isFlattened = true;

        return _netInitiator;
    }

    /**
     * Generate constraint checker files. For each constraint a file named
     * <code>.constr<i>N</i>.loc</code> will be created, where
     * <code><i>N</i></code> will be 0 through the number of constraints. The
     * constraints must be of type {@link Constraint#LOC} and formulas of type
     * {@link Constraint#TRACE_FORMULA}.
     */
    public void generateLOCCheckers() {
        StringBuffer annotation = new StringBuffer("annotation: event");
        StringBuffer trace = new StringBuffer("trace: \"%s");
        Iterator variableNames = _variables.iterator();

        while (variableNames.hasNext()) {
            String variableName = (String) variableNames.next();
            annotation.append(" " + variableName.replace('.', '_'));
            trace.append(" %d");
        }

        int counter = 0;
        String fileName;
        Iterator constraints = _constraints.iterator();

        while (constraints.hasNext()) {
            Constraint constraint = (Constraint) constraints.next();

            if ((constraint.getKind() == Constraint.LOC)
                    && (constraint.getFormula(Constraint.TRACE_FORMULA) != null)) {
                fileName = ".constr" + (counter++) + ".loc";

                String formula = (String) constraint
                        .getFormula(Constraint.TRACE_FORMULA);
                formula = formula.replace('.', '_');

                FileWriter locFile = null;
                try {
                    locFile = new FileWriter(fileName);
                    locFile.write("loc: " + formula + "\n");
                    locFile.write(annotation.toString() + "\n");
                    locFile.write(trace.toString() + "\"");
                    locFile.flush();
                    locFile.close();
                } catch (Exception ex) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("Error generating checker "
                            + "to path '" + fileName + "'", ex);
                } finally {
                    if (locFile != null) {
                        try {
                            locFile.close();
                        } catch (IOException ex) {
                            // FIXME: this method should declare exceptions
                            // and not throw RuntimeException.
                            throw new RuntimeException("Problem closing '"
                                    + fileName + "'", ex);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get a list of annotations in the network.
     *
     * @return A list of mappings from event objects to lists of variables.
     * @see #addAnnotation(Event, String)
     */
    public Hashtable getAnnotations() {
        return _annotations;
    }

    /**
     * Get the component name of a network node in a netlist.
     *
     * @param instance
     *            A network node that will be passed to
     *            {@link #getNodeSafely(Object)}.
     * @param netlist
     *            A netlist node that will be passed to
     *            {@link #getNetlist(Object)}.
     * @return The compName.
     * @see INode#getCompName(INetlist)
     * @see #getCompName(INode, INetlist)
     * @see #getCompName(String, String)
     */
    public String getCompName(Object instance, Object netlist) {
        INode foundInstance = null;
        INetlist foundNetlist = null;
        foundInstance = getNodeSafely(instance);
        foundNetlist = getNetlist(netlist);

        return getCompName(foundInstance, foundNetlist);
    }

    /**
     * Get the component name of a network node in a netlist.
     *
     * @param instance
     *            A network node.
     * @param netlist
     *            A netlist node.
     * @return The component name of the network node in a netlist. If the
     *         instance is null, then the string "null" is returned.
     * @see INode#getCompName(INetlist)
     * @see #getCompName(Object, Object)
     * @see #getCompName(String, String)
     */
    public String getCompName(INode instance, INetlist netlist) {
        if (instance != null) {
            return instance.getCompName(netlist);
        }
        return "null";
    }

    /**
     * Get the component name of a network node in a netlist.
     *
     * @param nodeName
     *            Name of the network node.
     * @param netName
     *            Name of the netlist.
     * @return The component name. If the node or netlist is not found, then the
     *         string "null" is returned.
     * @see INode#getCompName(INetlist)
     * @see #getCompName(INode, INetlist)
     * @see #getCompName(Object, Object)
     */
    public String getCompName(String nodeName, String netName) {
        // FIXME: This should probably call getNodeSafely()
        // and throw an exception if nodeName is not found
        INode instance = getNode(nodeName);
        INetlist netlist = getNetlist(netName);

        if ((instance != null) && (netlist != null)) {
            return instance.getCompName(netlist);
        }
        return "null";
    }

    /**
     * Get a reference to a component of netlist by name.
     *
     * @param netlist
     *            a netlist node.
     * @param name
     *            component name to look up.
     * @return The component.
     * @see #addComponent(Object, Object, String)
     * @see #addComponent(INode, INetlist, String)
     * @see #getComponent(Object, String)
     * @see #getComponent(String, String)
     */
    public Object getComponent(INetlist netlist, String name) {
        Iterator components = netlist.getComponents();
        while (components.hasNext()) {
            INode componentNode = (INode) components.next();

            if (componentNode.getCompName(netlist).equals(name)) {
                return componentNode.getUserObject();
            }
        }
        return null;
    }

    /**
     * Get a reference to a component of netlist by name.
     *
     * @param netlist
     *            a netlist node that is passed to
     *            {@link #getNodeSafely(Object)}.
     * @param name
     *            component name.
     * @return The component.
     * @see #addComponent(Object, Object, String)
     * @see #addComponent(INode, INetlist, String)
     * @see #getComponent(INetlist, String)
     * @see #getComponent(String, String)
     */
    public Object getComponent(Object netlist, String name) {
        // FIXME: Shouldn't getNode() be getNetlist()?
        return getComponent((INetlist) getNodeSafely(netlist), name);
    }

    /**
     * Get a reference to a component of netlist by name.
     *
     * @param netName
     *            name of a netlist that is passed to
     *            {@link #getNetlist(Object)}.
     * @param name
     *            component name.
     * @return The component.
     * @see #addComponent(Object, Object, String)
     * @see #addComponent(INode, INetlist, String)
     * @see #getComponent(INetlist, String)
     * @see #getComponent(Object, String)
     */
    public Object getComponent(String netName, String name) {
        INetlist netlist = getNetlist(netName);

        if (netlist == null) {
            return null;
        }

        return getComponent(netlist, name);
    }

    /**
     * Get the node to which a node is connected through a port.
     *
     * @param nodeObject
     *            source node.
     * @param port
     *            a port of the source node.
     * @return The destination node of a connection.
     * @see #getConnectionDest(String, String)
     * @see #getConnectionDest(INode, IPort)
     * @see #getConnectionDest(INode, String)
     */
    public Object getConnectionDest(Object nodeObject, Object port) {
        String portName = _getPortName(nodeObject, port);
        INode iNode = getNodeSafely(nodeObject);
        IPort iPort = _findPort(iNode, portName);

        return getConnectionDest(iNode, iPort);
    }

    /**
     * Get the node to which a node is connected through a port.
     *
     * @param nodeName
     *            name of source node that is passed to {@link #getNode(Object)}.
     * @param portName
     *            name of port.
     * @return The destination node of a connection.
     * @see #getConnectionDest(Object, Object)
     * @see #getConnectionDest(INode, IPort)
     * @see #getConnectionDest(INode, String)
     */
    public Object getConnectionDest(String nodeName, String portName) {
        INode iNode = getNodeSafely(nodeName);
        IPort iPort = iNode.getPort(portName);

        return getConnectionDest(iNode, iPort);
    }

    /**
     * Get the node to which a node is connected through a port.
     *
     * @param node
     *            source node.
     * @param portName
     *            name of port.
     * @return The destination node of a connection.
     * @see #getConnectionDest(Object, Object)
     * @see #getConnectionDest(String, String)
     * @see #getConnectionDest(INode, IPort)
     */
    public Object getConnectionDest(INode node, String portName) {
        IPort port = node.getPort(portName);

        return getConnectionDest(node, port);
    }

    /**
     * Get the node to which a node is connected through a port.
     *
     * @param node
     *            source node.
     * @param port
     *            a port of the source node.
     * @return The destination node of a connection.
     * @see #getConnectionDest(Object, Object)
     * @see #getConnectionDest(String, String)
     * @see #getConnectionDest(INode, String)
     */
    public Object getConnectionDest(INode node, IPort port) {
        Connection connection = node.getConnection(port);

        if (connection != null) {
            if (connection.isRefined()) {
                Set refinements = connection.getRefinementConnection();

                if (refinements.size() > 1) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("Warning! " + connection
                            + " has more than one refinement connections.");
                }

                Iterator iter = refinements.iterator();

                return ((Connection) iter.next()).getTarget().getUserObject();
            } else {
                return connection.getTarget().getUserObject();
            }
        } else {
            return null;
        }
    }

    /**
     * Perform a 'getconnectionnum' statement. Return the number of connections
     * to a given node that implement a given interface.
     *
     * @param mediumObject
     *            The node to check. The node must be of kind
     *            {@link MMType#MEDIUM} or {@link MMType#STATEMEDIUM} or else an
     *            exception is thrown. The medium type must implement the
     *            interface named by the ifName argument or else an exception is
     *            thrown.
     * @param ifName
     *            The name of the interface to search for. The interface must be
     *            of kind {@link MMType#INTERFACE} or else an exception is
     *            thrown.
     * @return The number of connections.
     * @see #getConnectionNum(INode, String)
     * @see #getConnectionNum(INode, MMType)
     */
    public int getConnectionNum(Object mediumObject, String ifName) {
        INode medium = getNodeSafely(mediumObject);
        MMType intf = getType(ifName);

        return getConnectionNum(medium, intf);
    }

    /**
     * Perform a 'getconnectionnum' statement. Return the number of connections
     * to a given node that implement a given interface.
     *
     * @param medium
     *            The node to check. The node must be of kind
     *            {@link MMType#MEDIUM} or {@link MMType#STATEMEDIUM} or else an
     *            exception is thrown. The medium type must implement the
     *            interface named by the intf argument or else an exception is
     *            thrown.
     * @param ifName
     *            The name of the interface to search for. The interface must be
     *            of kind {@link MMType#INTERFACE} or else an exception is
     *            thrown.
     * @return The number of connections.
     * @see #getConnectionNum(Object, String)
     * @see #getConnectionNum(INode, MMType)
     */
    public int getConnectionNum(INode medium, String ifName) {
        MMType intf = getType(ifName);

        return getConnectionNum(medium, intf);
    }

    /**
     * Perform a 'getconnectionnum' statement. Return the number of connections
     * to a given node that implement a given interface.
     *
     * @param medium
     *            The node to check. The node must be of kind
     *            {@link MMType#MEDIUM} or {@link MMType#STATEMEDIUM} or else an
     *            exception is thrown. The medium type must implement the
     *            interface named by the intf argument or else an exception is
     *            thrown.
     * @param intf
     *            The interface to search for. The interface must be of kind
     *            {@link MMType#INTERFACE} or else an exception is thrown.
     * @return The number of connections.
     * @see #getConnectionNum(Object, String)
     * @see #getConnectionNum(INode, String)
     */
    public int getConnectionNum(INode medium, MMType intf) {
        // Check that the object is a medium
        MMType mediumType = medium.getType();

        if ((mediumType.getKind() != MMType.MEDIUM)
                && (mediumType.getKind() != MMType.STATEMEDIUM)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("getconnectionnum: node '"
                    + medium.getName() + "' is not a "
                    + "medium or statemedium, it is a "
                    + MMType.show(mediumType.getKind()) + " and of type '"
                    + medium.getType().getName() + "'");
        }

        // Check that the type is an interface
        if (intf.getKind() != MMType.INTERFACE) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("getconnectionnum: type with name '"
                    + intf.getName() + "' is not an interface, it is a "
                    + MMType.show(intf.getKind()) + ".");
        }

        // Check that the object implements the interface
        if (!mediumType.implementsInterface(intf)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            String mediumTypeSuperInterfaces = mediumType.showSuperInterfaces();
            throw new RuntimeException("getconnectionnum: medium '"
                    + medium.getName()
                    + "' does not implement interface '"
                    + intf.getName()
                    + "', it implements"
                    + (mediumTypeSuperInterfaces.length() > 0 ? (": "
                            + mediumTypeSuperInterfaces + " interfaces")
                            : (" no interfaces")));
        }

        // Count the number of connections
        return medium.getNumUsers(intf);
    }

    /**
     * Get a list of constraints in the network.
     *
     * @return A list of constraints in the network.
     * @see #addConstraint(Constraint)
     */
    public LinkedList getConstraints() {
        return _constraints;
    }

    /**
     * Get all unique events in the network.
     *
     * @return A Hashtable of unique events with keys being events and values
     *         being names
     */
    public Hashtable getEventCache() {
        // FIXME: There is no code that sets _eventCache
        return _eventCache;
    }

    /**
     * Get the instance name of a network node.
     *
     * @param instance
     *            A network node.
     * @return The name of the instance.
     */
    public String getInstName(Object instance) {
        INode inst = getNodeSafely(instance);

        return getInstName(inst);
    }

    /**
     * Gets this object's inst name.
     *
     * @param instance
     *            A network node.
     * @return The name of the instance.
     */
    public String getInstName(INode instance) {
        if (instance != null) {
            return instance.getName();
        } else {
            return "null";
        }
    }

    /**
     * Get the net-initiator netlist.
     *
     * @return The netlist instance that builds the elaborated network.
     * @see #setNetInitiator(INetlist)
     */
    public INetlist getNetInitiator() {
        return _netInitiator;
    }

    /**
     * Get the object that stores information about a netlist in the network.
     * The netlist is added with {@link #addNode(Object, INode)}.
     *
     * @param object
     *            Netlist of the network.
     * @return The INetlist object that stores information about the given
     *         netlist of this network or null if the netlist object cannot be
     *         found.
     * @exception RuntimeException
     *                If the node found is not a netlist node
     * @see #addNode(Object, INode)
     * @see #getNetlist(String)
     */
    public INetlist getNetlist(Object object) {
        Object inst = _nodeInstances.get(object);

        if (inst == null) {
            return null;
        }

        // throw new RuntimeException("Object is not a node of the network");
        if (!(inst instanceof INetlist)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Object '" + inst
                    + "' is a node of the network, "
                    + "but it is not a netlist, it is a "
                    + inst.getClass().getName());
        }

        return (INetlist) inst;
    }

    /**
     * Get the object that stores information about a netlist in the network
     * from its name. The netlist is added with {@link #addNode(Object, INode)}.
     *
     * @param name
     *            Name of the netlist in the network.
     * @return The INetlist object that stores information about the given
     *         netlist of this network or null if the name does not belong to
     *         any netlist in the network.
     * @see #getNetlist(Object)
     */
    public INetlist getNetlist(String name) {
        Object inst = _nodeNames.get(name);

        if (!(inst instanceof INetlist)) {
            return null;
        }

        return (INetlist) inst;
    }

    /**
     * Get the object that stores information about a node in the network.
     *
     * @param nodeObject
     *            Node of the network. The object parameter cannot be null as
     *            the underlying datastructure is a Hashtable, which does not
     *            permit null keys.
     * @return The INode object that stores information about. the given node of
     *         this network or null if the node is not found. See
     *         {@link #getNodeSafely(Object)} for a method that throws an
     *         exception if the node is not found.
     * @see #addNode(Object, INode)
     * @see #getNode(String)
     * @see #getNodeSafely(Object)
     * @see #getNodeSafely(String)
     * @see #getNodes()
     * @see #getNodeInstances()
     */
    public INode getNode(Object nodeObject) {
        return (INode) _nodeInstances.get(nodeObject);
    }

    /**
     * Get the object that stores information about a node in the network from
     * its name.
     *
     * @param name
     *            Name of the node in the network.
     * @return The INode object that stores information about the given node of
     *         this network or null if the name does not belong to any node in
     *         the network. See {@link #getNodeSafely(String)} for a method that
     *         throws an exception if the node is not found.
     * @see #addNode(Object, INode)
     * @see #getNode(Object)
     * @see #getNodeSafely(Object)
     * @see #getNodeSafely(String)
     * @see #getNodes()
     * @see #getNodeInstances()
     */
    public INode getNode(String name) {
        return (INode) _nodeNames.get(name);
    }

    /**
     * Get the object that stores information about a node in the network and
     * throw an exception if it is not found.
     *
     * @param nodeObject
     *            Node of the network. The object parameter cannot be null as
     *            the underlying datastructure is a Hashtable, which does not
     *            permit null keys.
     * @return The INode object that stores information about. the given node of
     *         this network or throws an exception if the node is not found. See
     *         {@link #getNode(Object)} for a method that returns null if the
     *         node is not found.
     * @see #addNode(Object, INode)
     * @see #getNode(Object)
     * @see #getNode(String)
     * @see #getNodeSafely(String)
     * @see #getNodes()
     * @see #getNodeInstances()
     */
    public INode getNodeSafely(Object nodeObject) {
        INode inode = (INode) _nodeInstances.get(nodeObject);

        if (inode == null) {
            // Throw this exception here to avoid a NullPointerException
            // in later method calls.
            throw new RuntimeException("Could not find node '" + nodeObject
                    + "' in the node instances. "
                    + "Perhaps it has not been added with addNode()?");
        }
        return inode;
    }

    /**
     * Get the object that stores information about a node in the network from
     * its name.
     *
     * @param name
     *            Name of the node in the network.
     * @return The INode object that stores information about the given node of
     *         this network or throw an exception if the name does not belong to
     *         any node in the network. See {@link #getNode(String)} for a
     *         method that returns null if the node is not found.
     * @see #addNode(Object, INode)
     * @see #getNode(Object)
     * @see #getNode(String)
     * @see #getNodeSafely(Object)
     * @see #getNodes()
     * @see #getNodeInstances()
     */
    public INode getNodeSafely(String name) {
        INode inode = (INode) _nodeNames.get(name);

        if (inode == null) {
            // Throw this exception here to avoid a NullPointerException
            // in later method calls.
            throw new RuntimeException("Could not find a node named '" + name
                    + "' in the node instances. "
                    + "Perhaps it has not been added with addNode()?");
        }
        return inode;
    }

    /**
     * Get an iterator that will traverse all node instances in the network.
     *
     * @return An iterator that will generate all nodes instances.
     * @see #addNode(Object, INode)
     * @see #getNode(Object)
     * @see #getNode(String)
     * @see #getNodeSafely(Object)
     * @see #getNodeSafely(String)
     * @see #getNodes()
     */
    public Iterator getNodeInstances() {
        return _nodeInstances.keySet().iterator();
    }

    /**
     * Get an iterator that will traverse all nodes in the network.
     *
     * @return An iterator that will generate all nodes of the network.
     * @see #addNode(Object, INode)
     * @see #getNode(Object)
     * @see #getNode(String)
     * @see #getNodeSafely(Object)
     * @see #getNodeSafely(String)
     * @see #getNodeInstances()
     */
    public Iterator getNodes() {
        // FIXME: I find it confusing that some of the get*s() methods
        // return iterators and others return the underlying Collection.
        // Perhaps this methods should be called getNodeValues()?
        return _nodeInstances.values().iterator();
    }

    /**
     * Perform a 'getnthconnectionport' statement. Get the port of the n-th
     * connection to a given node that implements a given interface. This method
     * uses reflection to traverse the class hierarchy of the mediumObject
     * parameter and search for a class that has a field with the same name as
     * the nth user of mediumObject.
     *
     * @param mediumObject
     *            Node.
     * @param ifName
     *            Name of the interface.
     * @param n
     *            Index of the connection being accessed.
     * @return The port used in the n-th connection to the node implementing the
     *         interface with the given name.
     */
    public Object getNthConnectionPort(Object mediumObject, String ifName, int n) {
        INode medium = getNodeSafely(mediumObject);
        MMType intf = getType(ifName);
        Connection connection = medium.getNthUser(intf, n);

        Object object = connection.getSource().getUserObject();
        Field field = null;
        IPort iPort = connection.getPort();

        try {

            Class cls = object.getClass();
            boolean flag = false;

            do {
                try {
                    field = cls.getDeclaredField(iPort.getName());
                    flag = true;
                } catch (NoSuchFieldException ex) {
                    cls = cls.getSuperclass();

                    if (cls.getName().equals("java.lang.Object")) {
                        throw ex;
                    }
                }
            } while (flag == false);

        } catch (NoSuchFieldException ex) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Failed to find a field named '"
                    + iPort.getName() + "' in " + object.getClass()
                    + " or its superclasses.", ex);
        }
        try {
            field.setAccessible(true);

            if (iPort instanceof IPortElem) {
                return Array.get(field.get(object), ((IPortElem) iPort)
                        .getElemPortIndex());
            } else {
                return field.get(object);
            }
        } catch (IllegalAccessException ex) {
            // This exception is not likely to be thrown unless we
            // are running under a security manager because the
            // field.setAccessible() line above will insure that the
            // field.get() call will succeed.

            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("getnthconnectionport failed.", ex);
        }

        // Unreachable
        // return null;
    }

    /**
     * Perform a 'getnthconnectionsrc' statement. Get the target of the n-th
     * connection to a given node that implements a given interface.
     *
     * @param mediumObject
     *            Node.
     * @param ifName
     *            Name of the interface.
     * @param n
     *            Index of the connection being accessed.
     * @return The target of the n-th connection to the node implementing the
     *         interface with the given name.
     */
    public Object getNthConnectionSrc(Object mediumObject, String ifName, int n) {
        INode medium = getNodeSafely(mediumObject);
        MMType intf = getType(ifName);

        return getNthConnectionSrc(medium, intf, n);
    }

    /**
     * Perform a 'getnthconnectionsrc' statement. Get the target of the n-th
     * connection to a given node that implements a given interface.
     *
     * @param medium
     *            Node.
     * @param ifName
     *            Name of the interface.
     * @param n
     *            Index of the connection being accessed.
     * @return The target of the n-th connection to the node implementing the
     *         interface with the given name.
     */
    public Object getNthConnectionSrc(INode medium, String ifName, int n) {
        MMType intf = getType(ifName);

        return getNthConnectionSrc(medium, intf, n);
    }

    /**
     * Perform a 'getnthconnectionsrc' statement. Get the target of the n-th
     * connection to a given node that implements a given interface.
     *
     * @param medium
     *            Node.
     * @param intf
     *            Interface.
     * @param n
     *            Index of the connection being accessed.
     * @return The target of the n-th connection to the node implementing the
     *         interface with the given name.
     */
    public Object getNthConnectionSrc(INode medium, MMType intf, int n) {
        int max = getConnectionNum(medium, intf);

        if (max < n) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("medium '" + medium.getName()
                    + "'  does not have " + n + " connections implementing"
                    + " interface '" + intf.getName() + "', it only has " + max);
        }

        Connection connection = medium.getNthUser(intf, n);

        return connection.getSource().getUserObject();
    }

    /**
     * Get the index-th port of type ifName defined in node.
     *
     * @param srcObject
     *            source node.
     * @param ifName
     *            name of interface.
     * @param index
     *            the index of the port.
     * @return The port.
     * @see #getNthPort(INode, String, int)
     * @see #getNthPort(String, String, int)
     */
    public Object getNthPort(Object srcObject, String ifName, int index) {
        // FIXME: Should the return type be something other than Object?
        INode src = getNodeSafely(srcObject);

        return getNthPort(src, ifName, index);
    }

    /**
     * Get the index-th port of type ifName defined in node. This method uses
     * reflection to traverse the class hierarchy of the user object of the node
     * parameter and search for a declared field has a field with the same name
     * as a port of the node parameter. with the same name as the nth user of
     * mediumObject.
     *
     * @param node
     *            network node.
     * @param ifName
     *            name of interface.
     * @param index
     *            the index of the port. FIXME: Note that the index is zero
     *            based, so the first port has an index of 0
     * @return The index-th port. Return null if the node does not have any
     *         ports than end with the value of the ifName parameter.
     * @see #getNthPort(Object, String, int)
     * @see #getNthPort(String, String, int)
     */
    public Object getNthPort(INode node, String ifName, int index) {
        // FIXME: Should the return type be something other than Object?
        MMType nodeType = node.getType();
        int count = index;
        Object object = node.getUserObject();
        Class cls = object.getClass();
        Iterator portTypes = nodeType.portsIterator();

        while (portTypes.hasNext()) {
            MMPort portType = (MMPort) portTypes.next();
            if (portType.getInterface().getName().endsWith(ifName)) {
                Field field = null;

                while ((cls != null) && (field == null)) {
                    try {
                        field = cls.getDeclaredField(portType.getName());
                    } catch (NoSuchFieldException e) {
                        cls = cls.getSuperclass();
                    }
                }

                if (field == null) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("getnthport: Object "
                            + "of class '" + object.getClass().getName()
                            + "' does not have a field '" + portType.getName()
                            + "' of type '" + ifName + "' declared, which "
                            + "should match a port in " + nodeType.getName()
                            + ".");
                }

                try {
                    field.setAccessible(true);

                    if (field.getType().isArray()) {
                        int length = Array.getLength(field.get(object));

                        if (count < length) {
                            return Array.get(field.get(object), count);
                        } else {
                            count -= length;
                        }
                    } else {
                        if (count == 0) {
                            return field.get(object);
                        }

                        count--;
                    }
                } catch (IllegalAccessException ex) {
                    // This exception is not likely to be thrown unless we
                    // are running under a security manager because the
                    // field.setAccessible() line above will insure that the
                    // field.get() call will succeed.

                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("getnthport failed.", ex);
                }
            }
        } // end while iterator

        return null;
    }

    /**
     * Get the index-th port of type ifName defined in node.
     *
     * @param nodeName
     *            name of a network node.
     * @param ifName
     *            name of interface.
     * @param index
     *            the index of the port.
     * @return The port.
     * @see #getNthPort(Object, String, int)
     * @see #getNthPort(INode, String, int)
     */
    public Object getNthPort(String nodeName, String ifName, int index) {
        INode node = getNodeSafely(nodeName);

        return getNthPort(node, ifName, index);
    }

    /**
     * Get the number of elementary ports of type ifName defined in node.
     *
     * @param node
     *            network node.
     * @param ifName
     *            name of interface.
     * @return The port number.
     */
    public int getPortNum(INode node, String ifName) {
        if (node == null) {
            return 0;
        }

        MMType nodeType = node.getType();
        Iterator iterator = nodeType.portsIterator();
        int count = 0;

        while (iterator.hasNext()) {
            MMPort portType = (MMPort) iterator.next();

            if (portType.getInterface().getName().equals(ifName)) {
                IPort iPort = node.getPort(portType.getName());
                if (iPort != null) {
                    count += node.getPort(portType.getName()).numPorts();
                }
            }
        }

        return count;
    }

    /**
     * Get the number of elementary ports of type ifName defined in node.
     *
     * @param nodeName
     *            name of network node.
     * @param ifName
     *            name of interface.
     * @return The port number.
     */
    public int getPortNum(String nodeName, String ifName) {
        if (nodeName == null) {
            return 0;
        }
        INode node = getNodeSafely(nodeName);

        return getPortNum(node, ifName);
    }

    /**
     * Get the netlist to which the node is connected through port.
     *
     * @param node
     *            Node whose scope is set.
     * @param port
     *            Port being referred.
     * @return The scope.
     * @exception RuntimeException
     *                If the node does not have such a port or the port does not
     *                implement interface scope
     * @see #setScope(INode, IPort, INetlist)
     */
    public Object getScope(Object node, Object port) {
        String portName = _getPortName(node, port);
        INode iNode = getNodeSafely(node);
        IPort iPort = _findPort(iNode, portName);

        return getScope(iNode, iPort);
    }

    /**
     * Get the netlist to which the node is connected through port.
     *
     * @param node
     *            Node whose scope is set.
     * @param port
     *            Port being referred.
     * @return The scope.
     * @exception RuntimeException
     *                If the node does not have such a port or the port does not
     *                implement interface scope
     * @see #setScope(INode, IPort, INetlist)
     */
    public Object getScope(INode node, IPort port) {
        MMType scope = getType("metamodel.lang.Scope");

        // Check that the port belongs to this node
        if (port.getContainer() != node) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("getscope: port '" + port.show()
                    + "' does not belong to node '" + node.getName()
                    + "', but to node '" + port.getContainer().getName() + "'");
        }

        // Check that the port implements interface scope
        if (!port.getInterface().implementsInterface(scope)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("getscope: port '" + port.show()
                    + "' of node '" + node.getName() + "' of interface '"
                    + port.getInterface() + "' does not implement "
                    + "interface 'metamodel.lang.Scope' ("
                    + scope.showSuperInterfaces() + ")");
        }

        return node.getScope(port).getUserObject();
    }

    /**
     * Get the netlist to which the node is connected through port.
     *
     * @param nodeName
     *            name of node whose scope is set.
     * @param portName
     *            name of port being referred.
     * @return The scope.
     * @exception RuntimeException
     *                If the node does not have such a port or the port does not
     *                implement interface scope
     * @see #setScope(INode, IPort, INetlist)
     */
    public Object getScope(String nodeName, String portName) {
        INode node = getNodeSafely(nodeName);
        IPort port = node.getPort(portName);

        return getScope(node, port);
    }

    /**
     * Get the type of a node. This methods merely calls {@link INode#getType()}.
     *
     * @param instance
     *            The node to get the instance of.
     * @return The type
     */
    public MMType getType(INode instance) {
        return instance.getType();
    }

    /**
     * Get the information about a type from its name.
     *
     * @param name
     *            Name of the type.
     * @return The type.
     * @see #addType(MMType)
     */
    public MMType getType(String name) {
        MMType type = (MMType) _nodeTypes.get(name);

        if (type == null) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.

            // List some possible names in the error message.
            String allNodeTypeNames = "<<Unknown>>";

            if (_nodeTypes.size() == 0) {
                allNodeTypeNames = "There are no named type objects, "
                        + "try calling addType(MMType) first.";
            } else {
                try {

                    StringBuffer results = new StringBuffer();
                    Enumeration nodeTypeNames = _nodeTypes.keys();
                    while (nodeTypeNames.hasMoreElements()) {
                        String nodeTypeName = (String) nodeTypeNames
                                .nextElement();
                        if (results.length() > 0) {
                            results.append(", ");
                        }
                        results.append(nodeTypeName);
                        if (results.length() > 100) {
                            results.append("...");
                            break;
                        }
                    }
                    allNodeTypeNames = "Possible names include: "
                            + results.toString();
                } catch (Throwable throwable) {
                    // Ignore, but print
                    allNodeTypeNames = "WARNING: problem reading named type "
                            + "objects: " + throwable;
                }
            }
            throw new RuntimeException("type " + name + " not declared. "
                    + allNodeTypeNames);
        }

        return type;
    }

    /**
     * Get a set of variable names that appear in the annotation list.
     *
     * @return A set of variable names
     * @see #addVariable(String)
     */
    public LinkedHashSet getVariables() {
        return _variables;
    }

    /**
     * Allocate an array of ports with a given run-time dimension.
     *
     * @param nodeObject
     *            Node where the array of ports is instantiated.
     * @param portName
     *            Name of the array of ports.
     * @param idxString
     *            String describing the size of the array of ports. The String
     *            must be defined as "[x][y][z]..." where x, y and z are integer
     *            constants.
     * @exception RuntimeException
     *                If the node does not have an array with that name, or if
     *                the port is not an array of ports, or if the size of the
     *                index does not match with the number of dimensions of the
     *                array, or if any of the indexes is <= 0.
     */
    public void instantiatePortArray(Object nodeObject, String portName,
            String idxString) {
        INode node = getNodeSafely(nodeObject);
        IPort port = node.getPort(portName);

        if (!(port instanceof IPortArray)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Port '" + portName + "' of node '"
                    + node.getName() + "' is not an array");
        }

        // Parse the idxString
        StringTokenizer token = new StringTokenizer(idxString, "]");
        int count = token.countTokens();

        if (port.getDecl().getDims() != count) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Port '" + port.show()
                    + "' does not have the same " + count + " dimensions");
        }

        int[] idx = new int[count];

        int i = 0;

        while (token.hasMoreTokens()) {
            String number = token.nextToken().substring(1);
            idx[i] = Integer.parseInt(number);

            if (idx[i] < 0) {
                // FIXME: this method should declare exceptions
                // and not throw RuntimeException.
                throw new RuntimeException("Index '" + idx[i]
                        + "' used to allocate port '" + portName
                        + "' of node '" + node.getName() + "' is invalid");
            }

            i++;
        }

        IPortArray array = (IPortArray) port;
        array.allocate(idx);
    }

    /**
     * Test if a connection through port from src to dest is refined or not.
     *
     * @param srcName
     *            Name of source node.
     * @param portName
     *            Port name.
     * @param destName
     *            Name of destination node.
     * @return True if a connection is refined.
     */
    public boolean isConnectionRefined(String srcName, String portName,
            String destName) {
        INode src = getNodeSafely(srcName);
        INode dest = getNodeSafely(destName);
        IPort port = src.getPort(portName);

        return isConnectionRefined(src, port, dest);
    }

    /**
     * Test if a connection through port from src to dest is refined or not.
     *
     * @param src
     *            source node.
     * @param port
     *            Port.
     * @param dest
     *            destination node.
     * @return True if a connection is refined.
     * @exception RuntimeException
     *                If there is no valid connection from src to dest through
     *                port
     */
    public boolean isConnectionRefined(INode src, IPort port, INode dest) {
        if ((src == null) || (port == null) || (dest == null)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("isConnectionRefined: "
                    + "none of src, port or dest could be null");
        }

        // Check that the source port belongs to the source object
        if (port.getContainer() != src) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("isConnectionRefined: port '"
                    + port.show() + "' does not belong to node '"
                    + src.getName() + "', but to node '"
                    + port.getContainer().getName() + "'");
        }

        // Check if there is connections from the source node to dest node that
        // uses the port.
        Iterator iterator = src.getPortConnections(port).iterator();
        Connection conn = null;

        while (iterator.hasNext()) {
            Connection tmpConn = (Connection) iterator.next();

            if (tmpConn.getTarget() == dest) {
                conn = tmpConn;

                break;
            }
        }

        if (conn == null) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("isConnectionRefined: connection "
                    + "does not exist, there is no connection from node '"
                    + src.getName() + "' using port '" + port.show()
                    + "' to node " + dest.getName());
        }

        return conn.isRefined();
    }

    /**
     * Test if the network has been flattened.
     *
     * @return true if the netlist has been flattened, false otherwise.
     * @see #flatten()
     */
    public boolean isFlattened() {
        if (_isFlattened) {
            return true;
        }

        // The network is flattened if the components of the net-initiator
        // meet two requirements:
        // - they have not been refined
        // - they are not of netlist type
        Iterator components = _netInitiator.getComponents();
        _isFlattened = true;

        while (components.hasNext() && _isFlattened) {
            INode component = (INode) components.next();
            INetlist ref = component.getRefinement();
            _isFlattened = (ref == null) && !(component instanceof INetlist);
        }

        return _isFlattened;
    }

    /**
     * Perform a 'redirectconnect' statement in the network. Register the source
     * of the connection has changed.
     *
     * @param netlistObject
     *            Netlist defining the refinement.
     * @param orgObject
     *            Old source of the connection.
     * @param orgPortRef
     *            Old port that performs the connection.
     * @param componentObject
     *            New source of the connection.
     * @param newPortRef
     *            New port of the connection.
     */
    public void redirectConnect(Object netlistObject, Object orgObject,
            Object orgPortRef, Object componentObject, Object newPortRef) {
        INetlist netlist = null;
        INode origNode = null;
        INode newNode = null;
        IPort origPort = null;
        IPort newPort = null;
        String orgPortName = _getPortName(orgObject, orgPortRef);
        String newPortName = _getPortName(componentObject, newPortRef);

        try {
            netlist = getNetlist(netlistObject);
            origNode = getNodeSafely(orgObject);
            origPort = _findPort(origNode, orgPortName);
            newNode = getNodeSafely(componentObject);
            newPort = _findPort(newNode, newPortName);
        } catch (Exception ex) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect failed.", ex);
        }

        _connectPortPointer(componentObject, newPortRef, getConnectionDest(
                orgObject, orgPortRef));
        redirectConnect(netlist, origNode, origPort, newNode, newPort);
    }

    /**
     * Perform a 'redirectconnect' statement in the network. Register the source
     * of the connection has changed.
     *
     * @param netlist
     *            Netlist defining the refinement.
     * @param org
     *            Old source of the connection.
     * @param orgPortName
     *            Old port that performs the connection.
     * @param component
     *            New source of the connection. newPort = _findPort(newNode,
     *            newPortName);
     * @param newPortName
     *            New port of the connection.
     */
    public void redirectConnect(INetlist netlist, INode org,
            String orgPortName, INode component, String newPortName) {
        IPort orgPort = _findPort(org, orgPortName);
        IPort newPort = _findPort(component, newPortName);
        redirectConnect(netlist, org, orgPort, component, newPort);
    }

    /**
     * Perform a 'redirectconnect' statement in the network. Register the source
     * of the connection has changed.
     *
     * @param netlist
     *            Netlist defining the refinement.
     * @param org
     *            Old source of the connection.
     * @param orgPort
     *            Old port that performs the connection.
     * @param component
     *            New source of the connection.
     * @param newPort
     *            New port of the connection.
     */
    public void redirectConnect(INetlist netlist, INode org, IPort orgPort,
            INode component, IPort newPort) {
        // Check that the instances of ports passed as parameters belong
        // to the objects passed as parameters
        if (orgPort.getContainer() != org) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect: port '"
                    + orgPort.show() + "' of the original connection does not "
                    + "belong to node '" + org.getName() + "', but to node '"
                    + orgPort.getContainer().getName() + "'");
        }

        if (newPort.getContainer() != component) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect: port '"
                    + newPort.show() + "' to be used as the replacement port "
                    + "does not belong to the node '" + component.getName()
                    + "but to node '" + newPort.getContainer().getName() + "'");
        }

        // Check that the interface implemented by the original port is a
        // subinterface of the interface implemented by the new port
        MMType orgIf = orgPort.getInterface();
        MMType newIf = newPort.getInterface();

        if (!orgIf.implementsInterface(newIf)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect: port '"
                    + orgPort.show() + "' of the original connection does not"
                    + " implement the interface '" + newPort.show()
                    + "' of the" + "redirected connection");
        }

        // Check that component is a component of the netlist
        if (!netlist.isComponent(component)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect: node '"
                    + component.getName() + "' is not a component of netlist '"
                    + netlist.getName() + "' although it is required by the "
                    + "redirectconnect statement");
        }

        // Check that there is a valid connection from the original node
        // using the original port. Also check that there are no connections
        // from the component node using the new port
        Connection last = org.getConnection(orgPort);

        if (last == null) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect: port '"
                    + orgPort.show() + "' of node '" + org.getName()
                    + "' is not used by any connection, so we cannot redirect "
                    + "this connection");
        }

        Connection other = component.getConnection(newPort);

        if (other != null) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("redirectconnect: port '"
                    + newPort.show() + "' of node '" + component.getName()
                    + "' is already in use, "
                    + "so we cannot redirect a connection to this port");
        }

        // Perform the redirection after checking correctness
        _isFlattened = false;

        Connection redirect = new Connection(component, last.getTarget(),
                newPort, null, last);
        last.getTarget().addInConnection(redirect);
        component.addOutConnection(redirect);
    }

    /**
     * Perform a 'refine' statement in the network. Register that the netlist
     * now refines the given node.
     *
     * @param nodeObject
     *            Node being refined.
     * @param netlistObject
     *            Netlist describing the refinement.
     */
    public void refine(Object nodeObject, Object netlistObject) {
        INode node = getNode(nodeObject);
        INetlist netlist = getNetlist(netlistObject);

        refine(node, netlist);
    }

    /**
     * Perform a 'refine' statement in the network. Register that the netlist
     * now refines the given node.
     *
     * @param node
     *            Node being refined.
     * @param netlist
     *            Netlist describing the refinement.
     */
    public void refine(INode node, INetlist netlist) {
        // Check that the node is not refined several times
        if (node.isRefined()) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("refine: node '" + node.getName()
                    + "' already refined by netlist '"
                    + node.getRefinement().getName()
                    + "', so it cannot be refined" + "again by netlist '"
                    + netlist.getName() + "'");
        }

        // Register the refinement after checking correctness
        _isFlattened = false;
        node.setRefinement(netlist);
        netlist.addRefinedNode(node);
    }

    /**
     * Perform a 'refineconnect' statement in the network. Register that the
     * target of the connection has changed.
     *
     * @param netlistObject
     *            Netlist defining the refinement.
     * @param srcObject
     *            Source of the connection.
     * @param portRef
     *            Port defining the connection being refined.
     * @param componentObject
     *            New destination of the connection.
     */
    public void refineConnect(Object netlistObject, Object srcObject,
            Object portRef, Object componentObject) {
        String portName = _getPortName(srcObject, portRef);
        INetlist netlist = getNetlist(netlistObject);
        INode src = getNode(srcObject);
        IPort port = _findPort(src, portName);
        INode dst = getNode(componentObject);

        _connectPortPointer(srcObject, portRef, componentObject);
        refineConnect(netlist, src, port, dst);
    }

    /**
     * Perform a 'refineconnect' statement in the network. Register that the
     * target of the connection has changed.
     *
     * @param netlist
     *            Netlist defining the refinement.
     * @param src
     *            Source of the connection.
     * @param portName
     *            Port defining the connection being refined.
     * @param component
     *            New destination of the connection.
     */
    public void refineConnect(INetlist netlist, INode src, String portName,
            INode component) {
        IPort port = _findPort(src, portName);
        refineConnect(netlist, src, port, component);
    }

    /**
     * Perform a 'refineconnect' statement in the network. Register that the
     * target of the connection has changed.
     *
     * @param netlist
     *            Netlist defining the refinement.
     * @param src
     *            Source of the connection.
     * @param port
     *            Port defining the connection being refined.
     * @param component
     *            New destination of the connection.
     */
    public void refineConnect(INetlist netlist, INode src, IPort port,
            INode component) {
        // Check that the source port belongs to the source object
        if (port.getContainer() != src) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("refineconnect: port '" + port.show()
                    + "' does not belong to node '" + src.getName()
                    + "', but to node '" + port.getContainer().getName() + "'");
        }

        // Check that component implements the interface in the port
        if (!component.getType().implementsInterface(port.getInterface())) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("refineconnect: component '"
                    + component.getName() + "' of type '"
                    + component.getType().getName()
                    + "' does not implement the " + "interface '"
                    + port.getInterface().getName() + "' of the " + "port '"
                    + port.getName() + "'");
        }

        // Check that component is a component of the netlist
        if (!netlist.isComponent(component)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("refineconnect: node '"
                    + component.getName() + "' is not a component of netlist '"
                    + netlist.getName() + "' although it is required by the "
                    + "refineconnect statement");
        }

        // Check that there is one connection from the source node that
        // uses the port. This will be the connection to be refined
        Connection last = src.getConnection(port);

        if (last == null) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("refineconnect: connection to be "
                    + "refined does not exist, "
                    + "there is no connection from node '" + src.getName()
                    + "' using port '" + port.show() + "'");
        }

        // Build the refined connection
        _isFlattened = false;

        Connection refined = new Connection(src, component, port, null, last);
        component.addInConnection(refined);
        src.addOutConnection(refined);
    }

    /**
     * Restore the information about a network from a file. The information will
     * have been stored using the Serialization procedure offered by Java.
     *
     * @param fileName
     *            Name of the file.
     * @return The network.
     */
    public static Network restore(String fileName) {
        Network network = null;

        FileInputStream istream = null;
        try {
            istream = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(istream);
            network = (Network) in.readObject();
        } catch (Exception ex) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Error reading network from path '"
                    + fileName + "'", ex);
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ex) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("Problem closing '" + fileName
                            + "'", ex);
                }
            }
        }

        return network;
    }

    /**
     * Restore the information about a network from an ObjectInputStream. Close
     * the stream after reading. Catch all possible exceptions.
     *
     * @param ois
     *            The ObjectInputStream.
     * @return The network.
     */
    public static Network restore(ObjectInputStream ois) {
        // FIXME: Finish the implementation of this class
        Network network = null;

        try {
            network = (Network) ois.readObject();
        } catch (Throwable throwable) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Error reading ObjectInputStream: "
                    + ois, throwable);
        }

        return network;
    }

    /**
     * Save the information about a network to a file. Use Java Object
     * Serialization to store the objects to disk.
     *
     * @param fileName
     *            Name of the file.
     */
    public void save(String fileName) {
        FileOutputStream ostream = null;
        ObjectOutputStream out = null;
        try {
            ostream = new FileOutputStream(fileName);
            out = new ObjectOutputStream(ostream);
            out.writeObject(Network.net);
            out.flush();
        } catch (Exception ex) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("Error writing network to path '"
                    + fileName + "'", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("Problem closing '" + fileName
                            + "'", ex);
                }
            }
            // if (ostream != null) {
            // try {
            // ostream.close();
            // } catch (IOException ex) {
            // throw new RuntimeException("Problem closing '"
            // + fileName + "'", ex);
            // }
            // }
        }

    }

    /**
     * Set the net-initiator netlist. The constructor of this netlist
     * initializes the elaboration of the network.
     *
     * @param netInitiator
     *            Net-initiator netlist.
     * @see #getNetInitiator()
     */
    public void setNetInitiator(INetlist netInitiator) {
        _netInitiator = netInitiator;
    }

    /**
     * Perform a 'setscope' statement in the network. Register that the 'scope'
     * port in the node is now pointing to the netlist.
     *
     * @param node
     *            Node whose scope is set.
     * @param port
     *            Port being referred.
     * @param netlist
     *            Netlist being set.
     * @see #getScope(Object, Object)
     * @see #getScope(INode, IPort)
     * @see #getScope(String, String)
     */
    public void setScope(INode node, IPort port, INetlist netlist) {
        MMType scope = getType("metamodel.lang.Scope");

        // Check that the port belongs to this node
        if (port.getContainer() != node) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("setscope: port '" + port.show()
                    + "' does not belong to node '" + node.getName()
                    + "', but to node '" + port.getContainer().getName() + "'");
        }

        // Check that the port implements interface scope
        if (!port.getInterface().implementsInterface(scope)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("setscope: port '" + port.show()
                    + "' of node '" + node.getName() + "' does not implement"
                    + "interface 'metamodel.lang.Scope'");
        }

        // Check that the netlist also implements interface scope
        if (!netlist.getType().implementsInterface(scope)) {
            // FIXME: this method should declare exceptions
            // and not throw RuntimeException.
            throw new RuntimeException("setscope: netlist '"
                    + netlist.getName() + "' of type '"
                    + netlist.getType().getName() + "' does not implement"
                    + "interface 'metamodel.lang.Scope'");
        }

        // Check that there are no other setscopes which set port
        // to another netlistobject
        Iterator scopePorts = node.getScopePorts();

        while (scopePorts.hasNext()) {
            Connection connection = (Connection) scopePorts.next();

            if (connection.getPort() == port) {
                // FIXME: this method should declare exceptions
                // and not throw RuntimeException.
                throw new RuntimeException("setscope: scope port '"
                        + port.show() + "' is connected to netlist '"
                        + connection.getTarget().getName()
                        + "', cannot reset scope to '" + netlist.getName()
                        + "'");
            }
        }

        // Perform the 'setscope' statement after checking that
        // everything is correct
        Connection connection = new Connection(node, netlist, port, null, null);
        node.addScopeConnection(connection);
        netlist.addScopeUser(connection);
        addComponent(node, netlist, node.getName());
    }

    /**
     * Perform a 'setscope' statement in the network. Register that the 'scope'
     * port in the node is now pointing to the netlist.
     *
     * @param nodeObject
     *            Node whose scope is set.
     * @param portName
     *            Name of the scope port.
     * @param netlistObject
     *            Netlist being set.
     * @see #getScope(Object, Object)
     * @see #getScope(INode, IPort)
     * @see #getScope(String, String)
     */
    public void setScope(Object nodeObject, String portName,
            Object netlistObject) {
        INode node = getNodeSafely(nodeObject);
        IPort port = _findPort(node, portName);
        INetlist netlist = getNetlist(netlistObject);

        setScope(node, port, netlist);
    }

    /**
     * Perform a 'setscope' statement in the network. Register that the 'scope'
     * port in the node is now pointing to the netlist.
     *
     * @param node
     *            Node whose scope is set.
     * @param portName
     *            Name of the scope port.
     * @param netlist
     *            Netlist being set.
     * @see #getScope(Object, Object)
     * @see #getScope(INode, IPort)
     * @see #getScope(String, String)
     */
    public void setScope(INode node, String portName, INetlist netlist) {
        IPort port = _findPort(node, portName);
        setScope(node, port, netlist);
    }

    /**
     * Get a String with information about all nodes in the network. The amount
     * of information provided this way can be very large, so the usage of
     * show() in each node is recommended.
     *
     * @return A String with information about the nodes in the network.
     */
    public String show() {
        StringBuffer results = new StringBuffer("Top-Level netlist is "
                + ((_netInitiator == null) ? "null" : _netInitiator.getName())
                + "\n");

        Iterator nodes = _nodeInstances.values().iterator();

        while (nodes.hasNext()) {
            INode node = (INode) nodes.next();

            // results = results + "\n" + node.show() + "\n";
            results.append("\n" + node.show(_netInitiator) + "\n");
        }

        results.append("\n### List of annotations ###\n");

        Enumeration events = _annotations.keys();

        while (events.hasMoreElements()) {
            Event event = (Event) events.nextElement();
            LinkedList vars = (LinkedList) _annotations.get(event);

            results.append("   o " + event.show());

            Iterator eventVariables = vars.iterator();

            while (eventVariables.hasNext()) {
                String eventVariable = (String) eventVariables.next();
                results.append(" " + eventVariable);
            }

            results.append("\n");
        }

        results.append("\n### List of variables ###\n");

        Iterator variables = _variables.iterator();

        while (variables.hasNext()) {
            String variable = (String) variables.next();
            results.append(variable + "\t");
        }

        results.append("\n");

        results.append("\n### List of constraints ###\n");

        Iterator constraints = _constraints.iterator();

        while (constraints.hasNext()) {
            Constraint con = (Constraint) constraints.next();
            results.append(con.show());
        }

        // results.append("\n");
        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Remove all connections except the connections that are not
     * refined/redirected.
     *
     * @param netlist
     *            Netlist where connections should be removed.
     */
    protected void _flattenConnections(INetlist netlist) {
        Iterator nodes = getNodes();
        Set removedConnections = new HashSet();

        while (nodes.hasNext()) {
            INode node = (INode) nodes.next();
            Iterator connections = node.getInConnections();

            while (connections.hasNext()) {
                Connection connection = (Connection) connections.next();

                if (connection.isRefined()) {
                    removedConnections.add(connection);
                }
            }
        }

        Iterator connections = removedConnections.iterator();

        while (connections.hasNext()) {
            Connection connection = (Connection) connections.next();
            connection.getTarget().removeInConnection(connection);
            connection.getSource().removeOutConnection(connection);
        }
    }

    /**
     * Flatten a netlist recursively. Add all components of the refined netlists
     * to the current netlist. Remove the refinement netlists u as components of
     * that netlist and modify connections of the network so that the last
     * redirections/refinements are the last that are taken into account.
     *
     * @param netlist
     *            Netlist to be flattened.
     */
    protected void _flattenNetlist(INetlist netlist) {
        // _flattenConnections(netlist);
        List allNodes = new LinkedList();
        Iterator nodes = getNodes();

        while (nodes.hasNext()) {
            INode node = (INode) nodes.next();

            if (node instanceof INetlist) {
                continue;
            }

            allNodes.add(node);
        }

        nodes = allNodes.iterator();

        while (nodes.hasNext()) {
            INode node = (INode) nodes.next();
            String name = _removeContainers(node, netlist);

            if (!netlist.isComponent(node)) {
                addComponent(node, netlist, name + node.getName());

                // node.addCompName(netlist);
                node.addContainer(netlist);
            }
        }
    }

    /**
     * Remove all refined versions of a given connection. Leave only the latest
     * version of this connection.
     *
     * @param connection
     *            Connection being flattened.
     */
    protected void _refineConnection(Connection connection) {
        Set last = connection.finalConnection();
        Set current = new HashSet();
        Iterator finalConnections = last.iterator();

        while (finalConnections.hasNext()) {
            Connection c = (Connection) finalConnections.next();
            current.add(c.getRefinedConnection());
        }

        while (!current.isEmpty()) {
            last = new HashSet();
            Iterator currentConnections = current.iterator();

            while (currentConnections.hasNext()) {
                Connection c = (Connection) currentConnections.next();
                c.getTarget().removeInConnection(c);
                c.getSource().removeOutConnection(c);
                last.add(c.getRefinedConnection());
            }

            current = last;
        }
    }

    /**
     * Remove a node from all its containers except the top-level netlist. The
     * containers no longer have this node as a component.
     *
     * @param node
     *            Node to be removed.
     * @param top
     *            Top level netlist.
     * @return The name of the containers from which the container was removed.
     */
    protected String _removeContainers(INode node, INetlist top) {
        Iterator containers = node.getContainers();
        String nameContainers = new String();

        while (containers.hasNext()) {
            INetlist netlist = (INetlist) containers.next();

            if (netlist == top) {
                continue;
            }

            nameContainers = nameContainers + netlist.getName() + ".";
            containers.remove();
            netlist.removeComponent(node);
        }

        return nameContainers;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Netlist that builds the elaborated network. */
    protected INetlist _netInitiator = null;

    /**
     * Flag indicating if the network has been flattened. A flattened network is
     * a single netlist, where refined nodes have been replaced by their
     * refinement and connections are not redirected.
     */
    protected boolean _isFlattened = false;

    /**
     * Mapping from the node instances in the network to the objects that keep
     * information about the connection/refinements of that instance.
     */
    protected Hashtable _nodeInstances = new Hashtable();

    /**
     * Mapping from the name of a node to INode object that has information
     * about this instance.
     */
    protected Hashtable _nodeNames = new Hashtable();

    /**
     * Mapping from the name of a type to the number of instances of nodes with
     * that name.
     */
    protected Hashtable _instanceCount = new Hashtable();

    /**
     * Mapping from the names of the type objects containing information about
     * the types.
     */
    protected Hashtable _nodeTypes = new Hashtable();

    /** Mapping from event objects containing a variable. */
    protected Hashtable _annotations = new Hashtable();

    /** A list of constraints in the network. */
    protected LinkedList _constraints = new LinkedList();

    /** A Hashtable of unique events in the network. */
    protected Hashtable _eventCache = Event.eventCache;

    /** A set of variable names that appear in the annotation list. */
    protected LinkedHashSet _variables = new LinkedHashSet();

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * Connect a dest object to the port pointer of the src object the name of
     * the port pointer is the original port name plus a prefix.
     *
     * @param srcObject
     *            the source object.
     * @param srcPort
     *            port String or a reference to the port
     * @param destObject
     *            the destination object.
     * @exception RuntimeException
     *                If port does exist in obj
     */
    private void _connectPortPointer(Object srcObject, Object srcPort,
            Object destObject) {
        String prefix = "__pointer_";
        Class srcClass = srcObject.getClass();
        Field f;
        String portName = _getPortName(srcObject, srcPort);

        if (portName.indexOf("[") >= 0) {
            try {
                f = srcClass.getField(prefix
                        + portName.substring(0, portName.indexOf("[")).trim());
                f.setAccessible(true);

                int index0 = portName.indexOf('[');
                int index1 = portName.indexOf('[', index0 + 1);
                int i;
                Object arrayObj = f.get(srcObject);

                while (index1 > 0) {
                    i = Integer.valueOf(
                            portName.substring(index0 + 1, portName.indexOf(
                                    ']', index0 + 1))).intValue();
                    arrayObj = java.lang.reflect.Array.get(arrayObj, i);
                    index0 = index1;
                    index1 = portName.indexOf('[', index0 + 1);
                }

                i = Integer.valueOf(
                        portName.substring(index0 + 1, portName.indexOf(']',
                                index0 + 1))).intValue();
                java.lang.reflect.Array.set(arrayObj, i, destObject);
            } catch (Exception ex) {
                // FIXME: this method should declare exceptions
                // and not throw RuntimeException.
                throw new RuntimeException("_connectPortPointer() failed.", ex);
            }
        } else {
            try {
                f = srcClass.getField(prefix + portName);
                f.setAccessible(true);
                f.set(srcObject, destObject);
            } catch (Exception ex) {
                // FIXME: this method should declare exceptions
                // and not throw RuntimeException.
                throw new RuntimeException("_connectPortPointer() failed.", ex);
            }
        }
    }

    /**
     * Get the instance of a port that is referenced by a String. The String has
     * the form "portName" or "portName[x][y]", where x, y... are integer
     * constants.
     *
     * @param node
     *            Instance of the node where the port is declared.
     * @param port
     *            String that defines the port.
     * @return The instance of a port that is referenced by a String.
     * @exception RuntimeException
     *                If the node instance does not have a port with that name,
     *                or if the port should be an array and is not accessed with
     *                an index or viceversa.
     * @exception IndexOutOfBoundsException
     *                If the port is an array but it does not have an element
     *                with this index.
     */
    private IPort _findPort(INode node, String port) {
        StringTokenizer tokenizer = new StringTokenizer(port, "[");
        int count = tokenizer.countTokens();
        String portName = tokenizer.nextToken();

        if (count == 1) {
            // The port should be scalar, because it is not
            // specified using an index
            IPort thePort = node.getPort(portName);

            if (thePort == null) {
                // FIXME: this method should declare exceptions
                // and not throw RuntimeException.
                throw new RuntimeException("Node '" + node.getName()
                        + "' does not have a port called '" + portName + "'");
            }

            if (thePort instanceof IPortArray) {
                // FIXME: this method should declare exceptions
                // and not throw RuntimeException.
                throw new RuntimeException("Port '" + portName + "' of node '"
                        + node.getName() + "' is an array"
                        + " of ports, so it cannot be used as an scalar.");
            }

            return thePort;
        } else {
            // The port should be an array of ports
            int[] idx = new int[count - 1];
            int i = 0;

            while (tokenizer.hasMoreTokens()) {
                String number = tokenizer.nextToken();
                number = number.substring(0, number.length() - 1);
                idx[i++] = Integer.parseInt(number);
            }

            // Get the element of the port in the node
            IPort thePort = node.getPort(portName, idx);

            return thePort;
        }
    }

    /**
     * Get the name of a port defined in an array of object.
     *
     * @param obj
     *            The array object.
     * @param reference
     *            to the port.
     * @return The name of the port in obj, or null if the port does not exist
     *         in the array.
     */
    private String _getPortIndexFromArray(Object obj, Object port) {
        //Class cls = obj.getClass();
        int length = Array.getLength(obj);

        for (int i = 0; i < length; i++) {
            Object ithElem = Array.get(obj, i);

            if (ithElem == null) {
                continue;
            }

            if (ithElem.getClass().isArray()) {
                String pn = _getPortIndexFromArray(ithElem, port);

                if (pn != null) {
                    return ("[" + i + "]" + pn);
                }
            } else {
                if (ithElem == port) {
                    return "[" + i + "]";
                }
            }
        } // end for

        return null;
    }

    /**
     * Get the name of a port defined in an object.
     *
     * @param obj
     *            The object
     * @param port
     *            String or a reference to the port
     * @return The name of the port in obj
     * @exception RuntimeException
     *                If port does exist in obj
     */
    private String _getPortName(Object obj, Object port) {
        if (port instanceof String) {
            return (String) port;
        }

        Class cls = obj.getClass();

        while ((cls != null) && !cls.getName().equals("java.lang.Object")) {
            Field[] f = cls.getDeclaredFields();
            AccessibleObject.setAccessible(f, true);

            for (int i = 0; i < f.length; i++) {
                try {
                    Object ithField = f[i].get(obj);

                    if (ithField == null) {
                        continue;
                    } else if (ithField == port) {
                        return f[i].getName();
                    } else if (ithField.getClass().isArray()) {
                        String pn = _getPortIndexFromArray(ithField, port);

                        if (pn != null) {
                            return (f[i].getName() + pn);
                        }
                    }
                } catch (IllegalAccessException ex) {
                    // FIXME: this method should declare exceptions
                    // and not throw RuntimeException.
                    throw new RuntimeException("Cannot access object "
                            + cls.getName() + " to determine its ports.");
                }
            }

            cls = cls.getSuperclass();
        }

        // FIXME: this method should declare exceptions
        // and not throw RuntimeException.
        throw new RuntimeException(obj.getClass().getName()
                + " does not have a " + port.getClass().getName()
                + " typed port");
    }
}
