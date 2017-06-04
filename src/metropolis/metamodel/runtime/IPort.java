/* A port of a Node instance.

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

// ////////////////////////////////////////////////////////////////////////
// // IPort

/**
 * A port of a Node instance. The instance of a port holds information about the
 * port declaration of the type.
 *
 * @author Robert Clariso, Contributor: Christopher Brooks
 * @version $Id: IPort.java,v 1.12 2006/10/12 20:38:25 cxh Exp $
 */
public abstract class IPort implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new instance of a port from a given declaration and a given
     * object that will contain it.
     *
     * @param decl
     *            Declaration of the port in the type.
     * @param container
     *            Node instance that contains this port.
     */
    public IPort(MMPort decl, INode container) {
        _decl = decl;
        _container = container;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the Node instance that contains this port.
     *
     * @return The Node instance that contains this port.
     */
    public INode getContainer() {
        return _container;
    }

    /**
     * Get the declaration of this port.
     *
     * @return The declaration of this port in the meta-model class.
     */
    public MMPort getDecl() {
        return _decl;
    }

    /**
     * Get the interface implemented by this port.
     *
     * @return The interface implemented by this port.
     */
    public MMType getInterface() {
        return _decl.getInterface();
    }

    /**
     * Get the name of this port.
     *
     * @return The name of this port.
     */
    public String getName() {
        return _decl.getName();
    }

    /**
     * Get the number of elementary ports declared in this port. This would be 1
     * for scalar ports, the product of all dimensions for arrays, and 0 for
     * arrays with an unknown size.
     *
     * @return The number of elementary ports declared in this port.
     */
    abstract public int numPorts();

    /**
     * Show information about this port in a instance.
     *
     * @return A String with information about this port in an instance.
     */
    abstract public String show();

    /**
     * Get a text version of this port. This text version identifies the port
     * completely, although it does not provide information about the interface
     * implemented by the port.
     *
     * @return A String that identifies the port.
     */
    public String toString() {
        return _decl.getName();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Declaration of this port. */
    protected MMPort _decl;

    /** Node instance that defines this port instance. */
    protected INode _container;
}
