/* An declaration of a port of a meta-model class.

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
// // MMPort

/**
 * Information about a declaration of a port of a meta-model class. All
 * instances of the class will have a port with this name. The dynamic
 * information about a port is stored in IPort objects, while the information
 * about the declaration is stored in this MMPort object.
 *
 * @author Robert Clariso, contributor: Christopher Brooks
 * @version $Id: MMPort.java,v 1.19 2006/10/12 20:38:31 cxh Exp $
 */
public class MMPort implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new scalar port of a given name inside a given type.
     *
     * @param name
     *            Name of the port.
     * @param intf
     *            Interface implemented by the port.
     * @param type
     *            Type where the port has been declared.
     */
    public MMPort(String name, MMType intf, MMType type) {
        this(name, intf, type, 0, new int[0]);
    }

    /**
     * Create an unbounded array of ports of a given name inside a given type.
     *
     * @param name
     *            Name of the port.
     * @param intf
     *            Interface implemented by the port.
     * @param type
     *            Type where the port has been declared.
     * @param dims
     *            Dimensions of this port. If dimension > 0 then this is an
     *            array of ports.
     */
    public MMPort(String name, MMType intf, MMType type, int dims) {
        _name = name;
        _intf = intf;
        _type = type;
        _dims = dims;

        // Create the limits
        _limits = new int[dims];

        for (int i = 0; i < dims; i++) {
            _limits[i] = 0;
        }
    }

    /**
     * Create a new array of ports of a given name inside a given type.
     *
     * @param name
     *            Name of the port.
     * @param intf
     *            Interface implemented by the port.
     * @param type
     *            Type where the port has been declared.
     * @param dims
     *            Dimensions of this port. If dimension > 0 then this is an
     *            array of ports.
     * @param limits
     *            Maximum number of elements in each dimension of the array. Use
     *            0 for those dimensions that have not been declared.
     */
    public MMPort(String name, MMType intf, MMType type, int dims, int[] limits) {
        // FIXME: Why not eliminate the dims arg? We can get that from
        // the limits arg?
        _name = name;
        _intf = intf;
        _type = type;
        _dims = dims;

        // FIXME: clone or arrayCopy?
        _limits = (int[]) limits.clone();

        if (limits.length != dims) {
            throw new RuntimeException("Internal error, length of limits [] "
                    + " != dims (" + limits.length + " != " + dims + ")");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the number of dimensions of the port. 0 means this is an scalar port.
     * >0 means that this is an array of ports.
     *
     * @return The number of dimensions of this port.
     */
    public int getDims() {
        return _dims;
    }

    /**
     * Get the interface implemented by the port.
     *
     * @return The interface implemented by the port;
     */
    public MMType getInterface() {
        return _intf;
    }

    /**
     * Get the number of elements in a given dimension of the port. Return 0 if
     * the dimension is invalid or the number of elements of the dimension has
     * not been defined.
     *
     * @param dim
     *            Dimension whose maximum we are querying.
     * @return The number of elements in a dimension, or 0 if the dimension is
     *         invalid or has an undefined size.
     */
    public int getLength(int dim) {
        if ((dim <= 0) || (dim >= _dims)) {
            return 0;
        }

        return _limits[dim - 1];
    }

    /**
     * Get the name of the port.
     *
     * @return The name of the port.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the type where this port is declared.
     *
     * @return The type where this port is declared.
     */
    public MMType getType() {
        return _type;
    }

    /**
     * Test if the port has a fixed size. All scalar ports have a fixed size;
     * port arrays have a fixed size iff the limit for each dimension has been
     * fixed.
     *
     * @return true if the port has a fixed size.
     */
    public boolean hasFixedSize() {
        if (_dims == 0) {
            return true;
        }

        for (int i = 0; i < _dims; i++) {
            if (_limits[i] == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Build a dynamic instance of this port in a given node instance.
     *
     * @param container
     *            Node instance where this port is instantiated.
     * @return A dynamic instance of this port.
     */
    public IPort instantiate(INode container) {
        if (_dims == 0) {
            return new IPortScalar(this, container);
        }

        return new IPortArray(this, container);
    }

    /**
     * Test if this is an array of ports. A port is an array of scalar ports if
     * it has dimension 0.
     *
     * @return true iff the port is an array.
     */
    public boolean isArray() {
        return _dims > 0;
    }

    /**
     * Test if this is a scalar port. A port is scalar if it has dimension 0.
     *
     * @return true iff the port is scalar.
     */
    public boolean isScalar() {
        return _dims == 0;
    }

    /**
     * Show information about this port (name, interface implemented by the port
     * and number of dimensions).
     *
     * @return A String that will show information about this port.
     */
    public String show() {
        StringBuffer results = new StringBuffer(_intf.getName() + " " + _name);

        for (int i = 0; i < _dims; i++) {
            int limit = _limits[i];

            if (limit == 0) {
                results.append("[]");
            } else {
                results.append("[" + limit + "]");
            }
        }

        return results.toString();
    }

    /**
     * Return information about this port.
     */
    public String toString() {
        return "MMPort: " + show();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Compute the number of elementary ports that are stored in this port
     * declaration. This number will be 1 port scalar ports, the number of
     * elements for arrays, and 0 for arrays of unknown size.
     *
     * @return The number of elementary ports stored in this declaration.
     */

    // protected int _numPorts() {
    // int num = 1;
    // for (int i = 0; i < _dims; i++) {
    // num *= _limits[i];
    // }
    // return num;
    // }
    // /////////////////////////////////////////////////////////////////
    // // protected variables ////
    /** Name of the port. */
    protected String _name;

    /** Interface implemented by the port. */
    protected MMType _intf;

    /** Type where the port is declared. */
    protected MMType _type;

    /**
     * Number of dimensions of the array. 0 means this is just one port. >0
     * means that this is an array of ports.
     */
    protected int _dims;

    /**
     * Limits in the numbers of dimensions of the array. For example, myPort[3]
     * would have 3 as a limit.
     */
    protected int[] _limits;
}
