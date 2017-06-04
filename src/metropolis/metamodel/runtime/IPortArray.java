/* An array of port of a Node instance.

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
// // IPortArray

/**
 * An array of ports of a Node instance. This array can have an arbitrary fixed
 * number of dimensions (i.e. "[]"), and the size can be fixed either during
 * declaration of during execution. Once the size is fixed, however, it cannot
 * be assigned again.
 *
 * @author Robert Clariso, Contributor: Christopher Brooks
 * @version $Id: IPortArray.java,v 1.24 2006/10/12 20:38:26 cxh Exp $
 */
public class IPortArray extends IPort implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new port array from a declaration.
     *
     * @param decl
     *            Declaration of the port in a type.
     * @param container
     *            Node instance where this port is declared.
     */
    public IPortArray(MMPort decl, INode container) {
        super(decl, container);
        _limits = (int[]) decl._limits.clone();
        _elements = null;

        if (_decl.hasFixedSize()) {
            allocate(_limits);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Set the size of the array of ports through a runtime allocation (using
     * new). Fail if the array already has a defined size. Otherwise, create the
     * elements in the array.
     *
     * @param limits
     *            The maximum size for each of the dimensions of the array.
     */
    public void allocate(int[] limits) {
        if (limits.length != _limits.length) {
            throw new RuntimeException("Internal error allocating an "
                    + "array of ports, length of limits argument should be "
                    + "equal to length of limits of '" + _decl.getName()
                    + "': Length of limits arg (" + limits.length + ")"
                    + " != Length of decl '" + _decl.getName() + "' limits ("
                    + _limits.length + ")");
        }

        // Initialize the _limits and _elements
        int total = 1;

        for (int j = 0; j < limits.length; j++) {
            int limit = limits[j];
            _limits[j] = limit;
            total *= limit;
        }

        if (total < 0) {
            throw new RuntimeException("Size of array of ports '"
                    + _decl.getName() + "' undefined (" + total + "< 0)");
        }

        if (total == 0) {
            // At least one of the elements of _limits was 0, so
            // we zero out the entire array.
            _elements = null;

            for (int j = 0; j < _limits.length; j++) {
                _limits[j] = 0;
            }

            return;
        } else {
            _elements = new IPortElem[total];
        }

        // Create the initial index
        int[] index = new int[limits.length];

        for (int i = 0; i < index.length; i++) {
            index[i] = 0;
        }

        // Create the array of elementary ports
        for (int linearIndex = 0; linearIndex < total; linearIndex++) {
            _elements[linearIndex] = new IPortElem(this, index, linearIndex);

            if (linearIndex < (total - 1)) {
                _nextIndex(index);
            }
        }
    }

    /**
     * Get all the elementary ports of this array.
     *
     * @return An array with all elementary ports of this array. This array
     *         should not be modified in any way by the caller.
     */
    public IPortElem[] getAllElements() {
        return _elements;
    }

    /**
     * Get the elementary port with the given linear index. Linear index are a
     * mechanism to access a multidimensional array as an undimensional array.
     * Return null if the linear index is out of bounds.
     *
     * @param linearIndex
     *            The linear index of an element of the array.
     * @return The elementary port with this index or null if there is no port
     *         with this index.
     */
    public IPortElem getElem(int linearIndex) {
        // FIXME: Rename this to getElement() or element()
        if ((linearIndex < 0) || (linearIndex >= _elements.length)) {
            return null;
        }

        return _elements[linearIndex];
    }

    /**
     * Get the elementary port with the given multidimensional index, or null if
     * this array of ports does not have this multidimensional index. This index
     * is the normal index of the array.
     *
     * @param index
     *            The multidimensional index of this port.
     * @return The elementary port with this index or null if there is no port
     *         with this index.
     */
    public IPortElem getElem(int[] index) {
        // FIXME: Rename this to getElement() or element()
        if (index.length != _limits.length) {
            throw new RuntimeException("Wrong limits in array of ports, "
                    + "argument size was " + index.length
                    + " should have been " + _limits.length);
        }

        // Compute the linear index
        int linearIndex = 0;

        for (int i = index.length - 1; i >= 0; i--) {
            // Check out-of-bounds access
            int thisIndex = index[i];
            int thisLimit = _limits[i];

            if (thisIndex < 0) {
                throw new RuntimeException("Array out of bounds in access"
                        + " to array of ports, " + "index[" + i + "] == "
                        + index[i] + ", which is less than zero.");
            }

            if (thisIndex >= thisLimit) {
                throw new RuntimeException("Array out of bounds in access"
                        + " to array of ports, " + "index[" + i + "] == "
                        + index[i] + ", which is >= " + thisLimit);
            }

            linearIndex = (linearIndex * thisLimit) + thisIndex;
        }

        // Get the elementary array with this index
        return getElem(linearIndex);
    }

    /**
     * Get the number of elementary ports declared in this port. This would be 1
     * for scalar ports, the product of all dimensions for arrays, and 0 for
     * arrays with an unknown size.
     *
     * @return The number of elementary ports declared in this port.
     */
    public int numPorts() {
        // FIXME: Rename this to numberOfPorts.
        if (_elements == null) {
            return 0;
        } else {
            return _elements.length;
        }
    }

    /**
     * Show information about this port in a instance.
     *
     * @return A String with information about this port in an instance.
     */
    public String show() {
        StringBuffer results = new StringBuffer(_decl.getInterface().getName()
                + " " + _decl.getName());

        for (int i = 0; i < _decl.getDims(); i++) {
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
     * Return a short textual description of this port.
     *
     * @return A String that identifies the port.
     */
    public String toString() {
        return "IPortArray: " + super.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Runtime limits for this array of ports (null if none). */
    protected int[] _limits;

    /** Linearized version of the elements in this array of ports. */
    protected IPortElem[] _elements;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Modify an index array so that it points to the next element of the array.
     *
     * @param index
     *            An array of integers where each position I is an element
     *            between 0 and _limits[I]-1.
     */
    protected void _nextIndex(int[] index) {
        int i;

        for (i = 0; i < index.length; i++) {
            if (index[i] != (_limits[i] - 1)) {
                index[i]++;

                break;
            } else {
                index[i] = 0;
            }
        }
    }
}
