/* A ltl synch constraint in the elaborated network.

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
// // LTLSynch

/**
 * An ltl synch constraint in the elaborated network.
 *
 * @author Xi Chen, Contributor: Christopher Brooks
 * @version $Id: LTLSynch.java,v 1.29 2006/10/12 20:38:29 cxh Exp $
 */
public class LTLSynch extends Constraint implements Serializable {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new ltl synch constraint in the network.
     *
     * @param kind
     *            the constraint type LTLSYNCH
     */
    public LTLSynch(int kind) {
        // FIXME: What if the kind is not LTLSYNCH?
        this(null, kind, -1);
    }

    /**
     * Build a new ltl synch constraint in the network.
     *
     * @param container
     *            the node which contains this constraint.
     * @param kind
     *            the constraint type, LTLSYNCH.
     * @param index
     *            the sequence number of the constraint.
     */
    public LTLSynch(Object container, int kind, int index) {
        // FIXME: What if the kind is not LTLSYNCH? Is this a problem?
        super(container, kind, index);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if both event1 and event2 are in the event list. Do nothing if both
     * event1 and event2 are in the event list, otherwise throw an exception.
     */
    public void checkEvents() {
        if (_events.size() != 2) {
            throw new RuntimeException("Error on ltl synch constraint: "
                    + "the constraint must have two synchronized events and "
                    + "event references in equality part must appear in "
                    + "the event list.  Number of events found was: "
                    + _events.size());
        }
    }

    /**
     * Get equality.
     *
     * @return equality.
     * @see #setEqualVars(EqualVars)
     */
    public EqualVars getEqualVars() {
        return _equality;
    }

    /**
     * Set equality.
     *
     * @param equality
     *            The equality that needs to be set.
     * @see #getEqualVars()
     */
    public void setEqualVars(EqualVars equality) {
        _equality = equality;
    }

    /**
     * Get a String providing information about this constraint.
     *
     * @return A String providing information about this constraint.
     */
    public String show() {
        StringBuffer results = new StringBuffer(super.show());
        if (_equality == null) {
            results.append(_indent + "o No equality\n");
        } else {
            results.append(_indent + "o Equality\n" + _indent + "  "
                    + _equality.show() + "\n");
        }

        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The equality part of the synch constraint. */
    protected EqualVars _equality = null;
}
