/* A constraint in the elaborated network.

 Metropolis: Design Environment for Heterogeneous Systems.

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
import java.util.Enumeration;
import java.util.Hashtable;

// ////////////////////////////////////////////////////////////////////////
// // Event
/**
 * An event instance in the elaborated network.
 *
 * @author Xi Chen, Contributor: Christopher Brooks
 * @version $Id: Event.java,v 1.49 2006/10/12 20:38:22 cxh Exp $
 */
public class Event implements Serializable {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new event in the network.
     * <p>
     * Events are one of beg(process, object.name), end(process, object.name),
     * none(process), or other(process)
     *
     * @param kind
     *            The kind of event, one of {@link #BEG}, {@link #END},
     *            {@link #NONE}, or {@link #OTHER}.
     * @param process
     *            The process where the event belongs to. It is "null" if the
     *            process is set to "all".
     * @param nodeObject
     *            The object node of the action. It is "null" if kind is NONE or
     *            OTHER or if the action is set to "all"
     * @param name
     *            The label or function name in the action. It is "null" if kind
     *            is NONE or OTHER, or if the action is set to "all"
     *
     * @see #newEvent(int, Object, Object, String)
     */
    private Event(int kind, Object process, Object nodeObject, String name) {
        // FIXME: Should the type of process be a INode?
        _kind = kind;
        if (_kind < BEG || _kind > OTHER) {
            throw new RuntimeException("kind '" + _kind + "' was out of "
                    + "range, it must be >= " + BEG + " and <= " + OTHER);
        }
        _process = null;
        _nodeObject = null;
        _name = name;

        // We used to catch any errors and then exit, but this is very harsh
        // and makes it hard to test the code.
        _process = Network.net.getNode(process);
        _nodeObject = Network.net.getNode(nodeObject);
        _used = 0;

    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add a type of constraint this event is used in.
     *
     * @param used
     *            The type of constraint to be added, usually one of the fields
     *            from {@link Constraint}.
     * @see #getUsed()
     */
    public void addUsed(int used) {
        _used |= used;
    }

    /**
     * Compare this event to another Object.
     *
     * @param object
     *            The object that is compared to this event.
     * @return True if this object is the same as object argument; otherwise
     *         return false.
     */
    public boolean equals(Object object) {
        if (!(object instanceof Event)) {
            return false;
        }
        return equals((Event) object);
    }

    /**
     * Compare this event is the same as another event.
     *
     * @param event
     *            The event that is compared to this event.
     * @return If is the same return true; otherwise return false.
     * @see #hashCode()
     */
    public boolean equals(Event event) {
        if (event == null) {
            return false;
        }
        if (event.getKind() == _kind && event.getProcess() == _process
                && event.getNodeObject() == _nodeObject) {

            if (event.getName() == null && _name == null) {
                return true;
            } else if (event.getName() != null && event.getName().equals(_name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * See if this event is the same as another event.
     *
     * @param kind
     *            The kind of event, one of {@link #BEG}, {@link #END},
     *            {@link #NONE}, or {@link #OTHER}.
     * @param process
     *            The process where the event belongs to. It is "null" if the
     *            process is set to "all". Unlike in {@link #equals(Object)},
     *            here we look up process in {@link Network#net}
     * @param nodeObject
     *            The object node of the action. It is the string "null" if kind
     *            is NONE or OTHER or if the action is set to "all". Unlike in
     *            {@link #equals(Object)}, here we look up nodeObject in
     *            {@link Network#net}
     * @param name
     *            The label or function name in the action. It is the string
     *            "null" if kind is NONE or OTHER, or if the action is set to
     *            "all".
     * @return If is the same return true, otherwise return false.
     */
    public boolean equals(int kind, Object process, Object nodeObject,
            String name) {
        // FIXME: why do we look up peocess and nodeObject in Network.net?
        // but not in equals(Event)?
        if (kind == _kind && Network.net.getNode(process) == _process
                && Network.net.getNode(nodeObject) == _nodeObject) {

            if (name == null && _name == null) {
                return true;
            } else if (name != null && name.equals(_name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the unique name for the unique Event.
     *
     * @param event
     *            The unique event
     * @return the unique name for event. If the event is null, then return the
     *         empty string.
     */
    public static String getEventName(Event event) {
        if (event == null) {
            return "";
        } else if (!eventCache.containsKey(event)) {
            throw new RuntimeException("Internal error: the event cache "
                    + "does not contain event '" + event + "'");
        }
        return (String) eventCache.get(event);
    }

    /**
     * Get the kind of this event.
     *
     * @return The kind of this event.
     */
    public int getKind() {
        return _kind;
    }

    /**
     * Get the label or function name of the action of the event.
     *
     * @return The label or function name of the action of the event.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the object node of the action of the event.
     *
     * @return The object node of the action of the event.
     */
    public INode getNodeObject() {
        return _nodeObject;
    }

    /**
     * Get the process of this event.
     *
     * @return The process node of this connection.
     */
    public INode getProcess() {
        return _process;
    }

    /**
     * Get the types of constraints this event is used in.
     *
     * @return The types of constraints this event is used in, usually one of
     *         the fields from {@link Constraint}.
     * @see #addUsed(int)
     */
    public int getUsed() {
        return _used;
    }

    /**
     * Return the hashcode of this Event object.
     *
     * @return the hashcode.
     * @see #equals(Object)
     */
    public int hashCode() {
        return ((_process == null ? 0 : _process.hashCode() * 4913)
                + (_nodeObject == null ? 0 : _nodeObject.hashCode() * 289)
                + (_name == null ? 0 : _name.hashCode()) + _kind);
    }

    /**
     * Get a String representing the event for loc constraint only, e.g.
     * "BEG_processName_nodeObject_label".
     *
     * @return A String representing the event.
     */
    public String locString() {
        String returnString = new String();
        String processName = new String();
        String actionName = new String();

        if (_process == null) {
            return returnString;
        } else {
            processName = _process.getName();
        }

        if (_nodeObject == null) {
            return returnString;
        } else {
            actionName = _nodeObject.getName() + "_" + _name;
        }

        switch (_kind) {
        case BEG:
            returnString = "BEG_" + processName + "_" + actionName;
            break;
        case END:
            returnString = "END_" + processName + "_" + actionName;
            break;
        }
        return returnString;
    }

    /**
     * Create a 'new' Event instance. The Event class keeps a list of unique
     * Events. If the new event is in the list, return the one in the list;
     * otherwise, create a new instance.
     *
     * @param kind
     *            The kind of event, one of {@link #BEG}, {@link #END},
     *            {@link #NONE}, or {@link #OTHER}.
     * @param process
     *            The process where the event belongs to. It is "null" if the
     *            process is set to "all".
     * @param nodeObject
     *            The object node of the action. It is "null" if kind is NONE or
     *            OTHER or if the action is set to "all"
     * @param name
     *            The label or function name in the action. It is "null" if kind
     *            is NONE or OTHER, or if the action is set to "all"
     * @return The event from the list of unique Events or a new Event if the
     *         event was not found on the list.
     */
    public static Event newEvent(int kind, Object process, Object nodeObject,
            String name) {
        Enumeration keys = eventCache.keys();
        Event k = null;
        while (keys.hasMoreElements()) {
            k = (Event) keys.nextElement();
            if (k.equals(kind, process, nodeObject, name)) {
                return k;
            }
        }

        k = new Event(kind, process, nodeObject, name);
        eventCache.put(k, new String(_prefix + _index));
        _index++;
        return k;
    }

    /**
     * Get a String providing information about this event.
     *
     * @return A String providing information about this event.
     */
    public String show() {
        String returnString = null;
        String processName = null;
        String actionName = null;

        if (_process == null) {
            processName = "all";
        } else {
            processName = _process.getName();
        }

        if (_nodeObject == null) {
            actionName = "all";
        } else {
            actionName = _nodeObject.getName() + "." + _name;
        }

        switch (_kind) {
        case BEG:
            returnString = "beg(" + processName + ", " + actionName + ")";
            break;
        case END:
            returnString = "end(" + processName + ", " + actionName + ")";
            break;
        case NONE:
            returnString = "none(" + processName + ")";
            break;
        case OTHER:
            returnString = "other(" + processName + ")";
            break;
        default:
            throw new RuntimeException("Event.show(), unexpected event '"
                    + _kind + "'");
            // break;
        }
        return returnString;
    }

    /**
     * Get a String representing the event.
     *
     * @return A String representing the event.
     */
    public String toString() {
        // FIXME: Why are toString() and show() two separate files?
        String returnString = new String();
        String processName = new String();
        String actionName = new String();

        if (_process == null) {
            return returnString;
        } else {
            processName = _process.getName();
        }

        if (_nodeObject == null) {
            return returnString;
        } else {
            actionName = _nodeObject.getName() + "." + _name;
        }

        switch (_kind) {
        case BEG:
            returnString = "beg(" + processName + ", " + actionName + ")";
            break;
        case END:
            returnString = "end(" + processName + ", " + actionName + ")";
            break;
        }
        return returnString;
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // FIXME: In Java 1.5, use an enum.

    /** Kind for beg(process, object.name). */
    public static final int BEG = 0;

    /** Kind for end(process, object.name). */
    public static final int END = 1;

    /** Kind for none(process). */
    public static final int NONE = 2;

    /** Kind for other(process). */
    public static final int OTHER = 3;

    /** Event cache that keeps all unique events. */
    public static Hashtable eventCache = new Hashtable();

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Kind of event. One of {@link #BEG}, {@link #END}, {@link #NONE}, or
     * {@link #OTHER}.
     */
    protected int _kind;

    /** Process of event. */
    protected INode _process;

    /** Object of the action in the event. */
    protected INode _nodeObject;

    /** Label or function name of the action in the event. */
    protected String _name;

    /** Types of the constraints that this event is used in. */
    protected int _used;

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * Name prefix for unique name generation for each event
     */
    private static String _prefix = "e";

    /**
     * Name suffix (index) for unique name generation for each event
     */
    private static int _index = 0;

}
