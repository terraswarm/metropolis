/* A hashtable-based implementation of the Map interface
 (see class java.util.HashMap).

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

package metropolis.metamodel.backends.elaborator.util;

// ////////////////////////////////////////////////////////////////////////
// // HashMap
/**
 * An class that has different hashCode() and equals() policies than its parent
 * class in the java.util package.
 *
 * Objects of this class that have equal elements are not themselves equal. This
 * differs from the parent class in java.util, where the corresponding classes
 * in java.util that have equal elements are themselves equal.
 *
 * @author Unknown
 * @version $Id: HashMap.java,v 1.13 2006/10/12 20:32:32 cxh Exp $
 */
public class HashMap extends java.util.HashMap {
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public boolean equals(Object o) {
        return (this == o);
    }
}
