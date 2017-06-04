/* A one way ltl synch constraint in the elaborated network.

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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // LTLSynchImply

/**
 * A one way ltl synch constraint in the elaborated network. For example:
 *
 * <pre>
 *  ltl synch(event1 || event2 || ...|| eventN =&gt; eventX:
 *  V1@(event1, i) == v2@(event2, i), ...)
 * </pre>
 *
 * or
 *
 * <pre>
 *  ltl synch(eventX =&gt; event1 || event2 || ...|| eventN:
 * <br>
 *  v1@(event1, i) == vx@(eventx, i), ...).
 * </pre>
 *
 * @author Xi Chen, Contributor: Christopher Brooks
 * @version $Id: LTLSynchImply.java,v 1.18 2006/10/12 20:38:30 cxh Exp $
 */
public class LTLSynchImply extends Constraint implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new ltl synch imply constraint in the network.
     *
     * @param kind
     *            the constraint type LTLSYNCHIMPLY
     */
    public LTLSynchImply(int kind) {
        // FIXME: what if kind is not LTLSYNCHIMPLY?
        this(null, kind, -1);
    }

    /**
     * Build a new ltl synch imply constraint in the network.
     *
     * @param container
     *            the node which contains this constraint.
     * @param kind
     *            the constraint type, LTLSYNCH.
     * @param index
     *            the sequence number of the constraint.
     */
    public LTLSynchImply(Object container, int kind, int index) {
        // FIXME: what if kind is not LTLSYNCHIMPLY?
        super(container, kind, index);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add an equality var1@(event1, i) == var2@(event2, i).
     *
     * @param eq
     *            an equality
     */
    public void addEqualVars(EqualVars eq) {
        // FIXME: Should this be a LinkedHashSet so that each equalVar
        // is unique?
        _equalVars.addLast(eq);
    }

    /**
     * Add an event at the left hand side of the imply operator.
     *
     * @param event
     *            an event at the left hand side of the imply operator.
     */
    public void addLeftEvent(Event event) {
        // FIXME: In the other constraints, we check to see if the
        // constraints are already present in the list before adding them
        // again. Should we do the same here? If so, then perhaps we
        // should use a LinkedHashSet?
        _leftEvents.addLast(event);
    }

    /**
     * Add an event at the right hand side of the imply operator.
     *
     * @param event
     *            an event at the right hand side of the imply operator.
     */
    public void addRightEvent(Event event) {
        // FIXME: In the other constraints, we check to see if the
        // constraints are already present in the list before adding them
        // again. Should we do the same here? If so, then perhaps we
        // should use a LinkedHashSet?
        _rightEvents.addLast(event);
    }

    /**
     * Check the consistency between the event list and the equalities. Add the
     * events from the imply part to the central event list.
     */
    public void checkEvents() {
        Iterator events = _leftEvents.iterator();
        while (events.hasNext()) {
            addEvent((Event) events.next());
        }

        events = _rightEvents.iterator();
        while (events.hasNext()) {
            addEvent((Event) events.next());
        }

        // FIXME: Why is this exception never thrown?
        if (false) {
            throw new RuntimeException("Error on ltl synch imply constraint: "
                    + "event references in equality part must appear in "
                    + "the event list.");
        }
    }

    /**
     * Transfer events in the event list to the left hand side of the imply
     * operator.
     */
    public void eventsToLeftEvents() {
        _leftEvents.addAll((Collection) _events);
        _events.clear();
    }

    /**
     * Transfer events in the event list to the right hand side of the imply
     * operator.
     */
    public void eventsToRightEvents() {
        _rightEvents.addAll((Collection) _events);
        _events.clear();
    }

    /**
     * Get the list of equalities.
     *
     * @return a list of EqualVars.
     */
    public LinkedList getEqualVars() {
        return _equalVars;
    }

    /**
     * Get the events at the left hand side of the imply operator.
     *
     * @return a list of events.
     */
    public LinkedList getLeftEvents() {
        return _leftEvents;
    }

    /**
     * Get the events at the right hand side of the imply operator.
     *
     * @return a list of events.
     */
    public LinkedList getRightEvents() {
        return _rightEvents;
    }

    /**
     * Get a String providing information about this constraint.
     *
     * @return A String providing information about this constraint.
     */
    public String show() {
        StringBuffer results = new StringBuffer(super.show());

        results.append(_indent + "o Imply: ");

        Iterator events = _leftEvents.iterator();

        while (events.hasNext()) {
            Event event = (Event) events.next();
            results.append(event.show());

            if (events.hasNext()) {
                results.append("||");
            }
        }

        results.append(" => ");
        events = _rightEvents.iterator();

        while (events.hasNext()) {
            Event event = (Event) events.next();
            results.append(event.show());

            if (events.hasNext()) {
                results.append("||");
            }
        }

        // if (_equalVars.size() == 0)
        // return results.toString();
        results.append("\n");

        if (_equalVars.size() == 0) {
            results.append(_indent + "o No equalities\n");
        } else {
            results.append(_indent + "o Equalities\n");

            Iterator equalVars = _equalVars.iterator();
            while (equalVars.hasNext()) {
                EqualVars equalVar = (EqualVars) equalVars.next();
                results.append(_indent + "  - ");
                results.append(equalVar.show());
                results.append("\n");
            }
        }

        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The list of events on the left hand side of the imply operator. */
    protected LinkedList _leftEvents = new LinkedList();

    /** The list of events on the right hand side of the imply operator. */
    protected LinkedList _rightEvents = new LinkedList();

    /**
     * A list of the equalities which are pairs in the form of var1@(event1, i) ==
     * var2@(event2, i).
     */
    protected LinkedList _equalVars = new LinkedList();
}
