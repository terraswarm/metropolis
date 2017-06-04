/* A constraint in the elaborated network.

 Metropolis: Design Environment for Heterogeneus Systems.

 Copyright (c) 2004-2005 The Regents of the University of California.
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
// // BuiltInLOC

/**
 * A built-in loc constraint in the elaborated network.
 *
 * @author Xi Chen, contributor: Christopher Brooks
 * @version $Id: BuiltInLOC.java,v 1.16 2006/10/12 20:38:18 cxh Exp $
 */
public class BuiltInLOC extends Constraint implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new constraint in the network.
     *
     * @param kind
     *            the constraint for a built-in loc constraint, one of
     *            {@link Constraint#MINRATE}, {@link Constraint#MAXRATE},
     *            {@link Constraint#MINDELTA}, {@link Constraint#MAXDELTA} or
     *            {@link Constraint#PERIOD}.
     */
    public BuiltInLOC(int kind) {
        this(null, kind, -1);
    }

    /**
     * Build a new constraint in the network.
     *
     * @param container
     *            the node which contains this constraint.
     * @param kind
     *            the constraint for a built-in loc constraint, one of
     *            {@link Constraint#MINRATE}, {@link Constraint#MAXRATE},
     *            {@link Constraint#MINDELTA}, {@link Constraint#MAXDELTA} or
     *            {@link Constraint#PERIOD}.
     * @param index
     *            the sequence number of the constraint.
     */
    public BuiltInLOC(Object container, int kind, int index) {
        super(container, kind, index);

        if ((_kind < MINRATE) || (_kind > PERIOD)) {
            throw new RuntimeException("kind '" + _kind + "'was out of range. "
                    + "It must be >= MINRATE (" + MINRATE + ") and <= PERIOD ("
                    + PERIOD + ")");
        }
    }

    /**
     * Get the value field of a built-in loc constraint.
     *
     * @return A String representing the expression of the value field.
     * @see #setValue(String)
     */
    public String getValue() {
        return _valueExpression;
    }

    /**
     * Set the value field of a built-in loc constraint.
     *
     * @param value
     *            A String representing the expression of the value field.
     * @see #getValue()
     */
    public void setValue(String value) {
        _valueExpression = value;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get a String providing information about this constraint.
     *
     * @return A String providing information about this constraint.
     */
    public String show() {
        StringBuffer results = null;

        switch (_kind) {
        case MINRATE:
            results = new StringBuffer("LOC MINRATE");
            break;

        case MAXRATE:
            results = new StringBuffer("LOC MAXRATE");
            break;

        case MINDELTA:
            results = new StringBuffer("LOC MINDELTA");
            break;

        case MAXDELTA:
            results = new StringBuffer("LOC MAXDELTA");
            break;

        case PERIOD:
            results = new StringBuffer("LOC PERIOD");
            break;

        default:
            throw new RuntimeException(
                    "Unexpected type for a built-in loc constraint: " + _kind
                            + ". " + "It must be >= MINRATE (" + MINRATE
                            + ") and <= PERIOD (" + PERIOD + ")");
            // break;
        }

        results.append(_showHeader());
        results.append(_showFormulas());
        results.append(_showStructuralValues());
        results.append(_showQuantities());

        if ((_kind == MINDELTA) || (_kind == MAXDELTA)) {
            if (_events.size() == 0) {
                results.append(_indent + "o Event1: null\n");
                results.append(_indent + "o Event2: null\n");
            } else if (_events.size() == 1) {
                Event e1 = (Event) _events.get(0);

                if (e1 != null) {
                    results.append(_indent + "o Event1: " + e1.show() + "\n");
                } else {
                    results.append(_indent + "o Event1: null\n");
                }

                results.append(_indent + "o Event2: null\n");
            } else {
                Event e1 = (Event) _events.get(0);
                Event e2 = (Event) _events.get(1);

                if (e1 != null) {
                    results.append(_indent + "o Event1: " + e1.show() + "\n");
                } else {
                    results.append(_indent + "o Event1: null\n");
                }

                if (e2 != null) {
                    results.append(_indent + "o Event2: " + e2.show() + "\n");
                } else {
                    results.append(_indent + "o Event2: null\n");
                }
            }
        } else {
            if (_events.size() == 0) {
                results.append(_indent + "o Event: null\n");
            } else {
                Event e = (Event) _events.get(0);

                if (e != null) {
                    results.append(_indent + "o Event: " + e.show() + "\n");
                } else {
                    results.append(_indent + "o Event: null\n");
                }
            }
        }

        results.append(_indent + "o Value: " + _valueExpression + "\n");

        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////
    String _valueExpression = new String();
}
