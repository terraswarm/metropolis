/*
This file was generated automatically by GenerateVisitor.

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

package metropolis.metamodel.nodetypes;


import metropolis.metamodel.IVisitor;
import metropolis.metamodel.MetaModelVisitor;

import java.util.LinkedList;

/** A period built-in action formula.
 *    <p>Parent type: BuiltInActionNode
 *    <p>Implements:
 *    <p>Represents:<p>
 *    A period constraint of the form:
 *    <pre>
 *      maxrate ( quantity , event, value )
 *    </pre>
 *    where quantity is the reference of the Quantity associated 
 *    with event, value has a number type and should be the same 
 *    as the return type of quantity.A()
 */

/**
 * $Id$
 */
public class PeriodNode extends BuiltInActionNode {
    /** Construct a PeriodNode.
     *  @param quantity The ExprNode.
     *  @param event The ExprNode.
     *  @param value The ExprNode.
     */
    public PeriodNode(ExprNode quantity, ExprNode event, ExprNode value) {
        _childList.add(quantity);
        _childList.add(event);
        _childList.add(value);
        _childList.trimToSize();
    }

    /** Return the Quantity.
     *  @return true the Quantity
     *  @see #setQuantity(ExprNode)
     */
    public final ExprNode getQuantity() {
        return (ExprNode) _childList.get(CHILD_INDEX_QUANTITY);
    }

    /** Set the Quantity.
     *  @param quantity the Quantity
     *  @see #getQuantity()
     */
    public final void setQuantity(ExprNode quantity) {
        _childList.set(CHILD_INDEX_QUANTITY, quantity);
    }

    /** Return the Event.
     *  @return true the Event
     *  @see #setEvent(ExprNode)
     */
    public final ExprNode getEvent() {
        return (ExprNode) _childList.get(CHILD_INDEX_EVENT);
    }

    /** Set the Event.
     *  @param event the Event
     *  @see #getEvent()
     */
    public final void setEvent(ExprNode event) {
        _childList.set(CHILD_INDEX_EVENT, event);
    }

    /** Return the Value.
     *  @return true the Value
     *  @see #setValue(ExprNode)
     */
    public final ExprNode getValue() {
        return (ExprNode) _childList.get(CHILD_INDEX_VALUE);
    }

    /** Set the Value.
     *  @param value the Value
     *  @see #getValue()
     */
    public final void setValue(ExprNode value) {
        _childList.set(CHILD_INDEX_VALUE, value);
    }

    /** Construct a PeriodNode.
     */
    public PeriodNode() {
        
    }

    /** Return the classID.
     *  @return int.
     */
    public  int classID() {
        return NodeClassID.PERIODNODE_ID;
    }

    /** Accept a visitor at this node.
     *  @param visitor The IVisitor.
     *  @param args The LinkedList.
     *  @return Object.
     */
    protected  Object _acceptHere(IVisitor visitor, LinkedList args) {
        return ((MetaModelVisitor) visitor).visitPeriodNode(this, args);
    }


    /** Index of the quantity field in the _childList.
      * @see metropolis.metamodel.TreeNode#_childList
      */
    public static final int CHILD_INDEX_QUANTITY = 0;

    /** Index of the event field in the _childList.
      * @see metropolis.metamodel.TreeNode#_childList
      */
    public static final int CHILD_INDEX_EVENT = 1;

    /** Index of the value field in the _childList.
      * @see metropolis.metamodel.TreeNode#_childList
      */
    public static final int CHILD_INDEX_VALUE = 2;
}
