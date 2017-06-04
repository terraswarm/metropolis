/* An interface for a Visitor of a node of an abstract syntax tree.

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

// ////////////////////////////////////////////////////////////////////////
// // IVisitor

/**
 * An interface for a Visitor of a node of an abstract syntax tree.
 *
 * @author Jeff Tsay
 * @version $Id: IVisitor.java,v 1.12 2006/10/12 20:32:00 cxh Exp $
 */
public interface IVisitor {

    /**
     * Return an integer specifying how the nodes should be traversed by this
     * visitor.
     *
     * @return an integer with a value of {@link #TM_CHILDREN_FIRST},
     *         {@link #TM_SELF_FIRST} or {@link #TM_CUSTOM}.
     */
    public int traversalMethod();

    /**
     * An integer indicating that the children of a node should be visited
     * before it is visited itself.
     */
    public static final int TM_CHILDREN_FIRST = 0;

    /**
     * An integer indicating that the children of a node should be visited after
     * it is visited itself.
     */
    public static final int TM_SELF_FIRST = 1;

    /**
     * An integer indicating that the children of a node should not be
     * automatically visited. Therefore, each visitXXX() method has
     * responsibility for visiting its children.
     */
    public static final int TM_CUSTOM = 2;
}
