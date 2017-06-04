/* An instance of a netlist in the elaborated network.

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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

// ////////////////////////////////////////////////////////////////////////
// // INetlist

/**
 * Description of an instance of a netlist in the elaborated network. It
 * describes the component of the network, refinements, ..., in addition to the
 * information related to a node.
 *
 * @author Robert Clariso, contributor: Christopher Brooks
 * @version $Id: INetlist.java,v 1.33 2006/10/12 20:38:23 cxh Exp $
 */
public class INetlist extends INode implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new instance of a netlist of a given type.
     *
     * @param type
     *            The type of the netlist that is instantiated.
     * @param userObject
     *            Object in the user's Network.
     * @param name
     *            The name of the netlist.
     * @param objectID
     *            The ID of the netlist.
     */
    public INetlist(MMType type, Object userObject, String name, int objectID) {
        super(type, userObject, name, objectID);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add a new component to this netlist.
     *
     * @param component
     *            New component of the netlist.
     * @see #removeComponent(INode)
     */
    public void addComponent(INode component) {
        _components.add(component);
    }

    /**
     * Add a node refined by this netlist. If the list of refined nodes is
     * empty, it means that this netlist does not refine any node.
     *
     * @param node
     *            Node being refined by this netlist.
     */
    public void addRefinedNode(INode node) {
        // FIXME: Need removedRefinedNode() for symmetry?
        _refined.add(node);
    }

    /**
     * Register that a new node points to this netlist with a 'scope' port.
     *
     * @param connection
     *            The connection to be added to the scope users.
     */
    public void addScopeUser(Connection connection) {
        // FIXME: Need removeScopeUser() for symmetry?
        _scopeUsers.add(connection);
    }

    /**
     * Iterate over all components of this netlist.
     *
     * @return An iterator that will generate all components of this network.
     */
    public Iterator getComponents() {
        // FIXME: perhaps this should be called componentsIterator()?
        return _components.iterator();
    }

    /**
     * Iterate over all nodes refined by this netlist.
     *
     * @return An iterator that will generate all nodes refined by this netlist.
     */
    public Iterator getRefinedNodes() {
        return _refined.iterator();
    }

    /**
     * Iterate over all nodes with a 'scope' port that is pointing to this
     * netlist.
     *
     * @return An iterator that will generate all nodes that have a 'scope' port
     *         pointing to this netlist.
     */
    public Iterator getScopeUsers() {
        // FIXME: perhaps this should be called scopeUsersIterator()?
        return _scopeUsers.iterator();
    }

    /**
     * Test if a node is a component of the netlist.
     *
     * @param node
     *            Node being tested.
     * @return true iff the node is a component of the netlist.
     */
    public boolean isComponent(INode node) {
        return _components.contains(node);
    }

    /**
     * Test if this netlist is a refinement of another node instance in the
     * network.
     *
     * @return true is this netlist refines a node of the network.
     */
    public boolean isRefinement() {
        return (_refined.size() != 0);
    }

    /**
     * Test if this netlist is a refinement of a given node instance in the
     * network.
     *
     * @param node
     *            Node instance.
     * @return true if this netlist is refining that node instance.
     */
    public boolean isRefinement(INode node) {
        return (_refined.contains(node));
    }

    /**
     * Remove a component of a netlist.
     *
     * @param component
     *            Component of this netlist.
     * @see #addComponent(INode)
     */
    public void removeComponent(INode component) {
        if (!_components.remove(component)) {
            throw new RuntimeException("Netlist '" + getName()
                    + "' does not have the component '" + component.getName()
                    + "'");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Components of this netlist. Use a <code>TreeSet</code> so components
     * are always iterated in the same order, to support regression testing.
     */
    protected Set _components = new TreeSet(new _ComponentOrderComparator());

    /** Node instances refined by this netlist. */
    protected LinkedList _refined = new LinkedList();

    /** Nodes that point this netlist in a 'scope' node. */
    protected LinkedList _scopeUsers = new LinkedList();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Return information about the components (members) of this netlist.
     *
     * @return A String with information about the components in this netlist.
     */
    protected String _showComponents() {
        if (_components.size() == 0) {
            return "  o No components\n";
        }

        StringBuffer results = new StringBuffer("  o Components:\n");
        Iterator components = _components.iterator();

        while (components.hasNext()) {
            INode component = (INode) components.next();
            results.append("       - " + component.getCompName(this) + "  ("
                    + MMType.show(component.getType().getKind())
                    + " instance name: " + component.getName() + ")\n");
        }

        return results.toString();
    }

    /**
     * Return information about the refinements of this node.
     *
     * @return A String with information about the refinements of this node.
     */
    protected String _showRefinements() {
        StringBuffer results = new StringBuffer(super._showRefinements());

        if (_refined.size() == 0) {
            return results.toString() + "  o Does not refine any node\n";
        }

        results.append("  o Refines\n");

        Iterator refinements = _refined.iterator();

        while (refinements.hasNext()) {
            INode refinement = (INode) refinements.next();
            results.append("  " + refinement.getName() + "\n");
        }

        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // private classes ////

    /**
     * A <code>Comparator</code> for use by the set <code>_components</code>.
     * <code>_components</code> is a TreeSet so its Iterator will always give
     * the component <code>INode</code>s in the same order. This private
     * class is important for regression testing.
     */
    private class _ComponentOrderComparator implements Comparator, Serializable {
        public int compare(Object obj1, Object obj2) {
            if (obj1 instanceof INode && obj2 instanceof INode) {
                int id1 = ((INode) obj1).getObjectID();
                int id2 = ((INode) obj2).getObjectID();

                if (id1 == id2) {
                    return 0;
                } else if (id1 < id2) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                throw new ClassCastException("Attempt to compare two Objects "
                        + "that are not both INodes.");
            }
        }

        // Use the equals(Object object) method the Object class provides.
        // Do not define equals(_ComponentOrderComparator object), that
        // is a different method than equals(Object object)
    }
}
