/* A class on which to base objects that have properties.

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

package metropolis.metamodel;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

// ////////////////////////////////////////////////////////////////////////
// // PropertyMap
/**
 * A class on which to base objects that have properties. A property is an
 * arbitrary object that is associated with the object by a key.
 *
 * @author Jeff Tsay, Shuvra S. Bhattacharyya and Robert Clariso
 * @version $Id: PropertyMap.java,v 1.19 2006/10/12 20:31:54 cxh Exp $
 */
public class PropertyMap implements Cloneable, Serializable {

    /**
     * Make a deep copy of the property map, so that the new instance can have
     * different values for the same property than those of the old instance.
     *
     * @return The new PropertyMap.
     * @exception CloneNotSupportedException
     *                If throw while cloning a property.
     */
    public Object clone() throws CloneNotSupportedException {
        PropertyMap propertyMap = null;
        propertyMap = (PropertyMap) super.clone();
        // Make a shallow copy of keys and values, only if the map
        // is instantiated
        if (_propertyMap != null) {
            propertyMap._propertyMap = (HashMap) _propertyMap.clone();
        } else {
            propertyMap._propertyMap = null;
        }
        return propertyMap;
    }

    /**
     * Define a property.
     *
     * @param property
     *            Integer that identifies the property.
     * @return false if the property is already defined.
     */
    public boolean defineProperty(Integer property) {
        Object object = setProperty(property, NullValue.instance);
        return (object == null);
    }

    /**
     * Get a defined property.
     *
     * @param property
     *            Integer that identifies the property.
     * @return The value for the property.
     * @exception RuntimeException
     *                If the property is not defined.
     * @see #setDefinedProperty(Integer, Object)
     */
    public Object getDefinedProperty(Integer property) {
        if (_propertyMap == null) {
            throw new RuntimeException("Property " + property
                    + " not defined, Property Map is empty.");
        }
        Object returnValue = _propertyMap.get(property);
        if (returnValue == null) {
            throw new RuntimeException("Property " + property + " not defined");
        }
        return returnValue;
    }

    /**
     * Get a property that may be defined or not.
     *
     * @param property
     *            Integer identifying the property.
     * @return The value for the property, or null if the property is not
     *         defined.
     * @see #setProperty(Integer, Object)
     */
    public Object getProperty(Integer property) {
        if (_propertyMap == null) {
            return null;
        }
        return _propertyMap.get(property);
    }

    /**
     * Check if a property is defined.
     *
     * @param property
     *            Integer identifying the property.
     * @return true if this instance has the specified property.
     */
    public boolean hasProperty(Integer property) {
        if (_propertyMap == null)
            return false;
        return _propertyMap.containsKey(property);
    }

    /**
     * Get the set of defined properties.
     *
     * @return A set of Integers that identify the defined properties.
     */
    public Set keySet() {
        if (_propertyMap == null) {
            return new TreeSet();
        }
        return _propertyMap.keySet();
    }

    /**
     * Remove a property.
     *
     * @param property
     *            Integer identifying the property.
     * @return The previously defined value, or null if the property was not
     *         defined.
     */
    public Object removeProperty(Integer property) {
        if (_propertyMap == null)
            return null;
        Object value = _propertyMap.remove(property);
        if (_propertyMap.isEmpty()) {
            _propertyMap = null;
        }
        return value;
    }

    /**
     * Set a defined property.
     *
     * @param property
     *            Integer identifying the property.
     * @param object
     *            Value for the property.
     * @return The previous value for the property.
     * @exception RuntimeException
     *                If the property is not defined.
     * @see #getDefinedProperty(Integer)
     */
    public Object setDefinedProperty(Integer property, Object object) {
        if (object == null) {
            object = NullValue.instance;
        }
        if (_propertyMap == null) {
            throw new RuntimeException("Property " + property
                    + " not defined, Property Map is empty.");
        }

        Object returnValue = _propertyMap.put(property, object);

        if (returnValue == null) {
            throw new RuntimeException("Property " + property + " not defined");
        }
        return returnValue;
    }

    /**
     * Set a property that may be defined or not.
     *
     * @param property
     *            Integer identifying the property.
     * @param object
     *            Value for the property.
     * @return The previously defined value, or null if the property was not
     *         defined.
     * @see #getProperty(Integer)
     */
    public Object setProperty(Integer property, Object object) {
        if (object == null) {
            object = NullValue.instance;
        }
        if (_propertyMap == null) {
            _propertyMap = new HashMap(2);
        }
        return _propertyMap.put(property, object);
    }

    /**
     * Get the values of the properties.
     *
     * @return A Collection containing all of the property values.
     */
    public Collection values() {
        if (_propertyMap == null)
            return new LinkedList();
        return _propertyMap.values();
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // reserved properties

    /**
     * The key that retrieves the List of return values of the child nodes,
     * after accept() is called on all of them by TNLManip.traverseList().
     */
    public static final Integer CHILD_RETURN_VALUES_KEY = new Integer(-3);

    /**
     * The key that retrieves the return value of a tree node when it was last
     * traversed as an element of a "hierarchical" tree node (a list of nodes).
     * Such return values are not accessible through CHILD_RETURN_VALUES_KEY due
     * to the way in which hierarchical tree nodes are processed during
     * traversal.
     */
    public static final Integer RETURN_VALUE_AS_ELEMENT_KEY = new Integer(-2);

    /** The key that retrieves indicating a numbering. */
    public static final Integer NUMBER_KEY = new Integer(-1);

    // 0 is reserved for a dummy value for the interrogator

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * A map from properties (instances of Integer) to values (instances of
     * Objects). The map is instantiated lazily to conserve memory.
     */
    protected HashMap _propertyMap = null;
}
