/* A type of a node or interface in the meta-model specification.

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
import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // MMType

/**
 * A type of a node, object or interface in the meta-model specification.
 *
 * @author Robert Clariso
 * @version $Id: MMType.java,v 1.28 2006/10/12 20:38:32 cxh Exp $
 */
public class MMType implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new type defined in the meta-model specification.
     *
     * @param name
     *            Fully qualified name of the type.
     * @param kind
     *            Kind of type.
     * @param superClass
     *            Super class of this type.
     * @param superIfs
     *            Super interfaces of this type.
     */
    public MMType(String name, int kind, MMType superClass, LinkedList superIfs) {
        // FIXME: Should there be a constructor that has ports as an arg?
        _name = name;
        _kind = kind;
        _superClass = superClass;
        _superInterfaces = superIfs;
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////
    // FIXME: In java 1.5, we should use enums.

    /** Kind for interfaces. */
    public static final int INTERFACE = 0;

    /** Kind for classes. */
    public static final int CLASS = 1;

    /** Kind for netlists. */
    public static final int NETLIST = 2;

    /** Kind for processes. */
    public static final int PROCESS = 3;

    /** Kind for media. */
    public static final int MEDIUM = 4;

    /** Kind for schedulers. */
    public static final int SCHEDULER = 5;

    /** Kind for state medium. */
    public static final int STATEMEDIUM = 6;

    /** Kind for quantity. */
    public static final int QUANTITY = 7;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add a new implemented interface to this type.
     *
     * @param newIf
     *            New interface implemented by this type.
     */
    public void addInterface(MMType newIf) {
        if (_superInterfaces == null) {
            _superInterfaces = new LinkedList();
        }

        _superInterfaces.add(newIf);
    }

    /**
     * Add a new port to this object.
     *
     * @param port
     *            A port of this object.
     */
    public void addPort(MMPort port) {
        _ports.add(port);
    }

    /**
     * Get the kind of this type. Kinds are defined by constant fields of this
     * class.
     *
     * @return The kind of this type (process, class, etc.)
     */
    public int getKind() {
        return _kind;
    }

    /**
     * Get the fully qualified name of this type.
     *
     * @return The fully qualified name of this type.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the port with the given name.
     *
     * @param name
     *            Name of the port.
     * @return The port with the name specified, or null if this type does not
     *         have a port with this name, or if there are no ports.
     */
    public MMPort getPort(String name) {
        Iterator ports = _ports.iterator();

        while (ports.hasNext()) {
            MMPort port = (MMPort) ports.next();

            if (port.getName().equals(name)) {
                return port;
            }
        }

        return null;
    }

    /**
     * Get the superclass of this type.
     *
     * @return The superclass of this type, or null if it does not have a
     *         superclass (e.g. interfaces, class Object).
     * @see #setSuperClass(MMType)
     */
    public MMType getSuperClass() {
        return _superClass;
    }

    /**
     * Test if this type implements a given interface.
     *
     * @param intf
     *            Interface.
     * @return true if this type implements a given interface, or inherits a
     *         type that implements this interface.
     */
    public boolean implementsInterface(MMType intf) {
        if (this == intf) {
            return true;
        }

        if (_superClass != null) {
            if (_superClass.implementsInterface(intf)) {
                return true;
            }
        }

        if (_superInterfaces != null) {
            Iterator interfaces = _superInterfaces.iterator();

            while (interfaces.hasNext()) {
                MMType type = (MMType) interfaces.next();

                if (type.implementsInterface(intf)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Iterate over all interfaces implemented explicitly by this type.
     *
     * @return An iterator that will generate all interfaces implemented
     *         explicitly by this type.
     */
    public Iterator interfacesIterator() {
        if (_superInterfaces != null) {
            return _superInterfaces.iterator();
        }

        return null;
    }

    /**
     * Test if this type is a subclass of a given class.
     *
     * @param superClass
     *            Super class.
     * @return True if this class is a subclass of the superClass argument. <br>
     *         If the superClass argument is a reference to the current object,
     *         the return true. <br>
     *         If this MMType was constructed with a null superClass argument
     *         and the superClass argument of this method is not a reference to
     *         the current object, then this method will return false.
     */
    public boolean isSubClass(MMType superClass) {
        if (superClass == this) {
            return true;
        }

        if (_superClass == null) {
            // FIXME: Note that this is not the same as Java.
            // Here, superClass == null means that there is no parent;
            // however, it has no SubClasses either.
            return false;
        } else {
            return _superClass.isSubClass(superClass);
        }
    }

    /**
     * Iterate over all ports of this type.
     *
     * @return An iterator that will generate all ports of this type.
     */
    public Iterator portsIterator() {
        return _ports.iterator();
    }

    /**
     * Set the superclass of this type.
     *
     * @param superClass
     *            The superclass of this type, or null if it does not have a
     *            superclass (e.g. interfaces, class Object).
     * @see #getSuperClass()
     */
    public void setSuperClass(MMType superClass) {
        _superClass = superClass;
    }

    /**
     * Get a text describing the kind of a type.
     *
     * @param kind
     *            Kind of type constant.
     * @return A string describing the kind of type.
     */
    public static String show(int kind) {
        switch (kind) {
        case CLASS:
            return "class";

        case INTERFACE:
            return "interface";

        case PROCESS:
            return "process";

        case MEDIUM:
            return "medium";

        case SCHEDULER:
            return "scheduler";

        case NETLIST:
            return "netlist";

        case STATEMEDIUM:
            return "statemedium";

        case QUANTITY:
            return "quantity";

        default:
            return null;
        }
    }

    /**
     * Get a short information about this type.
     *
     * @param showKind
     *            Should kind be displayed with the name?
     * @return A String displaying the name of this type.
     */
    public String show(boolean showKind) {
        return (showKind ? (show(_kind) + " " + _name) : _name);
    }

    /**
     * Get a short information about this type.
     *
     * @param showKind
     *            Should kind be displayed with the name?
     * @param showClass
     *            Should classes be shown?
     * @param showInterfaces
     *            Should super interfaces be shown?
     * @param showPorts
     *            Should ports be shown?
     * @return A String displaying the name of this type.
     */
    public String show(boolean showKind, boolean showClass,
            boolean showInterfaces, boolean showPorts) {
        StringBuffer results = new StringBuffer("MMType " + show(showKind)
                + "\n");
        if (showClass) {
            results.append("  o Super Class: "
                    + (_superClass != null ? _superClass.show(true) + "\n"
                            : "null\n"));
        }
        if (showInterfaces) {
            results.append("  o Super Interfaces: " + showSuperInterfaces()
                    + "\n");
        }
        if (showPorts) {
            results.append("  o Ports: " + showPorts() + "\n");
        }
        return results.toString();
    }

    /**
     * Return a comma separated string that contains the names of any and all
     * ports.
     *
     * @return A string containing the ports. If there are no superinterfaces,
     *         return the empty string.
     */
    public String showPorts() {
        Iterator ports = portsIterator();
        if (ports == null) {
            return "";
        }
        StringBuffer results = new StringBuffer();
        while (ports.hasNext()) {
            MMPort port = (MMPort) ports.next();
            if (results.length() > 0) {
                results.append(", ");
            }
            results.append(port.getName());
        }
        return results.toString();
    }

    /**
     * Return a comma separated string that contains the names of any and all
     * superinterfaces .
     *
     * @return A string containing the superinterfaces. If there are no
     *         superinterfaces, return the empty string.
     */
    public String showSuperInterfaces() {
        Iterator superInterfaces = interfacesIterator();
        if (superInterfaces == null) {
            return "";
        }
        StringBuffer results = new StringBuffer();
        while (superInterfaces.hasNext()) {
            MMType superInterface = (MMType) superInterfaces.next();
            if (results.length() > 0) {
                results.append(", ");
            }
            results.append(superInterface.getName());
        }
        return results.toString();
    }

    /**
     * Return information about this type.
     */
    public String toString() {
        return "MMType: " + show(true);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Kind of type. */
    protected int _kind;

    /** List of ports declared in this type or inherited from other types. */
    protected LinkedList _ports = new LinkedList();

    /** Fully qualified name of the type. */
    protected String _name;

    /**
     * Superclass of the type (null if none, e.g. interfaces, class Object).
     */
    protected MMType _superClass;

    /** Superinterfaces of this type. */
    protected LinkedList _superInterfaces;
}
