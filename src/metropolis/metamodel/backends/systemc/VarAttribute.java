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

/**
 * A class used to represent one variable specified by synch constraints.
 *
 * @author Guang Yang
 * @version $Id: VarAttribute.java,v 1.18 2006/10/12 20:33:16 cxh Exp $
 */
public class VarAttribute {

    /**
     * Create a VarAttribute, which represents on variable specified by synch
     * constraints.
     *
     * @param event
     *            the event
     * @param variableName
     *            variable name
     * @param type
     *            variable type
     * @param nondet
     *            if the variable is nondeterministic
     */
    public VarAttribute(Event event, String variableName, String type,
            boolean nondet) {
        _variableName = variableName;
        _type = type;
        _nondet = nondet;
        _event = event;
        _equalVar = this;
        _indexInSynchGroup = -1;
        _processed = false;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return true if equal.
     *
     * @param event
     *            the event
     * @param variableName
     *            variable name
     * @param type
     *            variable type
     * @param nondet
     *            if the variable is nondeterministic
     * @return whether equal or not
     */
    public boolean equals(Event event, String variableName, String type,
            boolean nondet) {
        // FIXME: if we define equal, we should define hashvalue
        return ((event == _event) && (variableName.equals(_variableName)
                && (type.equals(_type)) && (nondet == _nondet)));
    }

    /**
     * Return the equal variable.
     *
     * @return Returns the _equalVar.
     * @see #setEqualVar
     */
    public VarAttribute getEqualVar() {
        return _equalVar;
    }

    /**
     * Return the event.
     *
     * @return Returns the _event.
     * @see #setEvent
     */
    public Event getEvent() {
        return _event;
    }

    /**
     * Get the index in the synch group.
     *
     * @return Returns the _indexInSynchGroup.
     * @see #setIndexInSynchGroup
     */
    public int getIndexInSynchGroup() {
        return _indexInSynchGroup;
    }

    /**
     * Get the type of the variable.
     *
     * @return Variable type
     */
    public String getType() {
        return _type;
    }

    /**
     * Get the name of the variable.
     *
     * @return Variable name
     */
    public String getVarName() {
        return _variableName;
    }

    /**
     * Check whether or not a variable is nondeterministic.
     *
     * @return whether or not a variable is nondeterministic
     */
    public boolean isNondetVar() {
        return _nondet;
    }

    /**
     * Check if the variable has been processed in code generation.
     *
     * @return true if this variable is processed in code generation for
     *         comparing equal variables.
     */
    public boolean isProcessed() {
        return _processed;
    }

    /**
     * Set the equal variable.
     *
     * @param var
     *            The _equalVar to set.
     * @see #getEqualVar
     */
    public void setEqualVar(VarAttribute var) {
        _equalVar = var;
    }

    /**
     * Set the Event.
     *
     * @param event
     *            The _event to set.
     * @see #getEvent
     */
    public void setEvent(Event event) {
        _event = event;
    }

    /**
     * Set the variable index in the same synch event group.
     *
     * @param inSynchGroup
     *            The _indexInSynchGroup to set.
     * @see #getIndexInSynchGroup
     */
    public void setIndexInSynchGroup(int inSynchGroup) {
        _indexInSynchGroup = inSynchGroup;
    }

    /**
     * Set the processed flag that indicates if this variable has been processed
     * in code generation for comparing equal variables.
     *
     * @param processed
     *            The _processed to set.
     */
    public void setProcessed(boolean processed) {
        _processed = processed;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected fields ////

    /**
     * The event in whose scope the variable exists.
     */
    protected Event _event;

    /**
     * The variable index in the same synch event group.
     */
    protected int _indexInSynchGroup;

    /**
     * Indicate whether or not this variable is processed in code generation for
     * comparing equal variables.
     */
    protected boolean _processed;

    /**
     * The varAttribute this instance is equal to.
     */
    protected VarAttribute _equalVar;

    /**
     * The name of the variable.
     */
    protected String _variableName;

    /**
     * The type of the Variable.
     */
    protected String _type;

    /**
     * True if this variable is nondeterministic.
     */
    protected boolean _nondet;
}
