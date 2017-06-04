/*
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


 METROPOLIS_COPYRIGHT_VERSION_1
 COPYRIGHTENDKEY
 */

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.runtime.Event;

import java.util.HashSet;
import java.util.Iterator;

/**
 * A OneSynchEvent class instance represents an event that is synched with other
 * events. All events that are synched together form a synch event group. Each
 * such group shares the same and unique ID. In execution, all events in the
 * same synch event group must proceed concurrently. At the same time, variables
 * specified by synch constraint that are in the scope of each event must have
 * identical values.
 *
 * @author Guang Yang
 * @version $Id: OneSynchEvent.java,v 1.18 2006/10/12 20:33:04 cxh Exp $
 */
public class OneSynchEvent {

    /**
     * Create a SynchEvent.
     *
     * @param event
     *            The event to be synched with other events.
     */
    public OneSynchEvent(Event event) {
        _event = event;
        _variables = new HashSet();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add one variable specified in synch constraint to the event that is
     * represented by this OneSynchEvent instance.
     *
     * @param variableName
     *            The name of the synch variable to add.
     * @param type
     *            the type of the variable
     * @param nondet
     *            whether or not the variable is nondeterministic
     * @return true if the variable being added was not contained previously
     */
    public VarAttribute addSynchVar(String variableName, String type,
            boolean nondet) {
        Iterator variables = _variables.iterator();
        VarAttribute variableAttribute = null;
        while (variables.hasNext() && (variableAttribute == null)) {
            VarAttribute temporaryAttribute = (VarAttribute) variables.next();
            if (temporaryAttribute.equals(_event, variableName, type, nondet)) {
                variableAttribute = temporaryAttribute;
            }
        }

        if (variableAttribute == null) {
            variableAttribute = new VarAttribute(_event, variableName, type,
                    nondet);
        }
        _variables.add(variableAttribute);
        return variableAttribute;
    }

    /**
     * Add one variable specified in synch constraint to the event that is
     * represented by this OneSynchEvent instance.
     *
     * @param variableName
     *            The name of the synch variable to add.
     * @param nondet
     *            True if the variable is nondeterministic.
     * @return true if the variable being added was not previously contained.
     */
    public boolean addSynchVar(String variableName, boolean nondet) {
        VarAttribute newVar = new VarAttribute(_event, variableName, "", nondet);
        _synchGroupID = -1;
        return _variables.add(newVar);
    }

    /**
     * Get the event represented by this OneSynchEvent instance.
     *
     * @return the event represented by this OneSynchEvent instance
     */
    public Event getEvent() {
        return _event;
    }

    /**
     * Get the number of variables associated to the event represented by this
     * OneSynchEvent instance.
     *
     * @return the number of variables
     */
    public int getNumSynchVariables() {
        int num = 0;
        Iterator iter = _variables.iterator();
        while (iter.hasNext()) {
            if (!((VarAttribute) iter.next()).getType().startsWith("constant"))
                num++;
        }
        return num;
    }

    /**
     * Get the unique synch event group ID.
     *
     * @return the unique synch event group ID.
     * @see #setSynchGroupID
     */
    public int getSynchGroupID() {
        return _synchGroupID;
    }

    /**
     * Get all variables associated to the event represented by this
     * OneSynchEvent instance.
     *
     * @return all variables organized in a HashSet.
     */
    public HashSet getVariables() {
        return _variables;
    }

    /**
     * Check whether or not the event is represented by this OneSynchEvent
     * instance.
     *
     * @param event
     *            an event
     * @return whether or not the event is represented by this OneSynchEvent
     *         instance.
     */
    public boolean isSameEvent(Event event) {
        return (_event.equals(event));
    }

    /**
     * Set the unique synch event group ID.
     *
     * @param synchGroupID
     *            the unique synch event group ID.
     * @see #getSynchGroupID
     */
    public void setSynchGroupID(int synchGroupID) {
        _synchGroupID = synchGroupID;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected fields ////

    /**
     * One event in a synch event group.
     */
    protected Event _event;

    /**
     * The unique synch event group ID shared by all events in that group.
     */
    protected int _synchGroupID;

    /**
     * Variables specified by synch constraint that are in the scope of _event.
     * Its elements are of type VarAttribute.
     */
    protected HashSet _variables;
}
