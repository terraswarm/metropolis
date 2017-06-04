/* A single element of an array of ports.

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
// // IPortElem

/**
 * A single element of a (possibly multidimensional) array of ports. The
 * instance of a port holds information about the port declaration of the type.
 *
 * @author Robert Clariso, Contributor: Christopher Brooks
 * @version $Id: IPortElem.java,v 1.17 2006/10/12 20:38:27 cxh Exp $
 */
public class IPortElem extends IPort implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build an element of an array of ports.
     *
     * @param array
     *            Array containing this element.
     * @param index
     *            Index (possibly multidimensional) that identifies the element
     *            inside the array.
     * @param linearIndex
     *            Linear index (unidimensional) that identifies the element
     *            inside the array;
     */
    public IPortElem(IPortArray array, int[] index, int linearIndex) {
        super(array._decl, array._container);
        _array = array;

        // Is this necessary?
        _index = (int[]) index.clone();

        // FIXME: check to see if this is < 0 or >= the size?
        _linearIndex = linearIndex;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the array where this elementary port has been declared.
     *
     * @return The array where this elementary port has been declared.
     */
    public IPortArray getArray() {
        return _array;
    }

    /**
     * Get the index of the elementary port of this array of ports ordered by
     * the linear index of the elementary port.
     *
     * @return The index of the elementary port of this array of ports
     */
    public int getElemPortIndex() {
        return _linearIndex;
    }

    /**
     * Get the next elementary port of this array of ports, ordered by the
     * linear index of the elementary port.
     *
     * @return The next elementary port of this array of ports, or null if there
     *         are no more elementary ports in this array.
     */
    public IPortElem next() {
        return _array.getElem(_linearIndex + 1);
    }

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
        return _decl.getInterface().getName() + " " + toString();
    }

    /**
     * Get a text version of this port. This text version identifies the port
     * completely, although it does not provide information about the interface
     * implemented by the port.
     *
     * @return A String that identifies the port.
     */
    public String toString() {
        StringBuffer results = new StringBuffer(getName());

        for (int i = 0; i < _index.length; i++) {
            results.append("[" + _index[i] + "]");
        }

        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Array of ports that contains this elementary port. */
    protected IPortArray _array;

    /**
     * Index (possibly multidimensional) that identifies the element in the
     * array that contains it.
     */
    protected int[] _index;

    /**
     * Linear index that identifies the element in the array that contains it.
     */
    protected int _linearIndex;
}
