/* A base class for objects that may be visited.

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

import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // TrackedPropertyMap
/**
 * A base class for objects that may be visited. Visitors may mark such objects
 * with their corresponding Class objects.
 *
 * @author Jeff Tsay and Robert Clariso
 * @version $Id: TrackedPropertyMap.java,v 1.19 2006/10/12 20:32:06 cxh Exp $
 */
public class TrackedPropertyMap extends PropertyMap {

    /** Create a new TrackedPropertyMap. */
    public TrackedPropertyMap() {
        super();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add the visitor with the argument class object to the set of visitors
     * that have visited this object.
     *
     * @param c
     *            The class to be added.
     * @return true if the set did not already contain the specified element.
     */
    public boolean addVisitor(Class c) {
        if (_visitedBySet == null)
            _visitedBySet = new LinkedList();
        else if (_visitedBySet.contains(c))
            return false;
        return _visitedBySet.add(c);
    }

    /** Clear all traces of visitation from any visitor. */
    public void clearVisitors() {
        _visitedBySet = null;
    }

    /**
     * Remove the visitor with the argument class object from the set of
     * visitors that have visited this object.
     *
     * @param c
     *            The class to be added.
     * @return true if the set did already contain the specified element.
     */
    public boolean removeVisitor(Class c) {
        if (_visitedBySet == null)
            return false;
        boolean returnValue = _visitedBySet.remove(c);
        if (_visitedBySet.size() == 0)
            _visitedBySet = null;
        return returnValue;
    }

    /**
     * Return an iterator over the class objects of the visitors that have
     * visited this object.
     *
     * @return The Iterator of visitor class objects that have visited.
     */
    public Iterator visitorIterator() {
        if (_visitedBySet == null)
            _visitedBySet = new LinkedList();
        return _visitedBySet.iterator();
    }

    /**
     * Return true iff this object was visited by a visitor with the argument
     * class object.
     *
     * @param c
     *            The class that is being checked.
     * @return true if the class argument has visited.
     */
    public boolean wasVisitedBy(Class c) {
        if (_visitedBySet == null)
            return false;
        else
            return _visitedBySet.contains(c);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * A set of class objects of the visitors that have visited this object. The
     * set is implemented as a linked list to minimize memory usage. Moreover,
     * this list is instantiated lazily, i.e. it will not use memory if it is
     * empty.
     */
    protected LinkedList _visitedBySet = null;
}
