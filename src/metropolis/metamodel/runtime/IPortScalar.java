/* A scalar port of a Node instance.

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
// // IPortScalar

/**
 * A scalar port of a Node instance. The instance of a port holds information
 * about the port declaration of the type.
 *
 * @author Robert Clariso, Contributor: Christopher Brooks
 * @version $Id: IPortScalar.java,v 1.14 2006/10/12 20:38:28 cxh Exp $
 */
public class IPortScalar extends IPort implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new scalar port from a declaration.
     *
     * @param decl
     *            Declaration of the port in a type.
     * @param container
     *            Node instance that defines this port.
     */
    public IPortScalar(MMPort decl, INode container) {
        super(decl, container);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the number of elementary ports declared in this port. This would be 1
     * for scalar ports, the product of all dimensions for arrays, and 0 for
     * arrays with an unknown size.
     *
     * @return The number of elementary ports declared in this port.
     */
    public int numPorts() {
        return 1;
    }

    /**
     * Show information about this port in a instance.
     *
     * @return A String with information about this port in an instance.
     */
    public String show() {
        return _decl.show();
    }

    /**
     * Return a short textual description of this port.
     *
     * @return A String that identifies the port.
     */
    public String toString() {
        return "IPortScalar: " + super.toString();
    }
}
