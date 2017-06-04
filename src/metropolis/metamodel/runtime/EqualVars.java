/* An equality in the elaborated network.

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

// ////////////////////////////////////////////////////////////////////////
// // EqualVars
/**
 * A class representing an equality: var1@(event1, i) == var2@(event2, i) or
 * var1@(event1, i) == constant.
 *
 * @author Xi Chen, Contributor: Christopher Brooks
 * @version $Id: EqualVars.java,v 1.21 2006/10/12 20:38:21 cxh Exp $
 */
public class EqualVars implements Serializable {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Construct an EqualVars object.
     *
     * @param value1
     *            variable at the left hand side of the equality
     * @param event1
     *            event at the left hand side of the equality
     * @param type1
     *            type of the left hand side of the equality, one of
     *            {@link #BOOLTYPE}, {@link #CHARTYPE}, {@link #DOUBLETYPE},
     *            {@link #FLOATTYPE}, {@link #INTTYPE}, {@link #LONGTYPE},
     *            {@link #STRINGTYPE}, {@link #VARTYPE}.
     * @param value2
     *            variable at the right hand side of the equality
     * @param event2
     *            event at the right hand side of the equality
     * @param type2
     *            type of the right hand side of the equality, one of
     *            {@link #BOOLTYPE}, {@link #CHARTYPE}, {@link #DOUBLETYPE},
     *            {@link #FLOATTYPE}, {@link #INTTYPE}, {@link #LONGTYPE},
     *            {@link #STRINGTYPE}, {@link #VARTYPE}.
     */
    public EqualVars(String value1, Event event1, int type1, String value2,
            Event event2, int type2) {
        _value1 = value1;
        _value2 = value2;
        _event1 = event1;
        _event2 = event2;
        // FIXME: if the type is CHARTYPE, should value have a length of 1?
        _checkType(type1);
        _type1 = type1;
        _checkType(type2);
        _type2 = type2;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get event1.
     *
     * @return event1.
     * @see #setEvent1(Event)
     */
    public Event getEvent1() {
        return _event1;
    }

    /**
     * Get event2.
     *
     * @return event2.
     * @see #setEvent2(Event)
     */
    public Event getEvent2() {
        return _event2;
    }

    /**
     * Get type1.
     *
     * @return type1.
     * @see #setType1(int)
     */
    public int getType1() {
        return _type1;
    }

    /**
     * Get type2.
     *
     * @return type2.
     * @see #setType2(int)
     */
    public int getType2() {
        return _type2;
    }

    /**
     * Get value1.
     *
     * @return value1.
     * @see #setValue1(String)
     */
    public String getValue1() {
        return _value1;
    }

    /**
     * Get value2.
     *
     * @return value2.
     * @see #setValue2(String)
     */
    public String getValue2() {
        return _value2;
    }

    /**
     * Set event1.
     *
     * @param event
     *            The event reference need to be set.
     * @see #getEvent1()
     */
    public void setEvent1(Event event) {
        _event1 = event;
    }

    /**
     * Set event2.
     *
     * @param event
     *            the event reference need to be set.
     * @see #getEvent2()
     */
    public void setEvent2(Event event) {
        _event2 = event;
    }

    /**
     * Set type1.
     *
     * @param type1
     *            the type of left hand side of the equality.
     * @see #getType1()
     */
    public void setType1(int type1) {
        _checkType(type1);
        _type1 = type1;
    }

    /**
     * Set type2.
     *
     * @param type2
     *            the type of right hand side of the equality.
     * @see #getType2()
     */
    public void setType2(int type2) {
        _checkType(type2);
        _type2 = type2;
    }

    /**
     * Set value1.
     *
     * @param value
     *            The variable need to be set.
     * @see #getValue1()
     */
    public void setValue1(String value) {
        _value1 = value;
    }

    /**
     * Set value2.
     *
     * @param value
     *            the variable need to be set.
     * @see #getValue2()
     */
    public void setValue2(String value) {
        _value2 = value;
    }

    /**
     * Show the equality.
     *
     * @return a String displaying the equality
     */
    public String show() {
        String leftside;
        String rightside;

        if ((_type1 == VARTYPE) && (_event1 != null)) {
            leftside = _value1 + "@(" + _event1.show() + ", i)";
        } else {
            if (_type1 == CHARTYPE) {
                leftside = "'" + _value1 + "'";
            } else if (_type1 == STRINGTYPE) {
                leftside = "\"" + _value1 + "\"";
            } else {
                leftside = _value1;
            }
        }

        // if (_event1 != null)
        // leftside = leftside + "@(" + _event1.show() + ", i)";
        if ((_type2 == VARTYPE) && (_event2 != null)) {
            rightside = _value2 + "@(" + _event2.show() + ", i)";
        } else {
            if (_type2 == CHARTYPE) {
                rightside = "'" + _value2 + "'";
            } else if (_type2 == STRINGTYPE) {
                rightside = "\"" + _value2 + "\"";
            } else {
                rightside = _value2;
            }
        }

        // if (_event2 != null)
        // leftside = leftside + "@(" + _event2.show() + ", i)";
        return new String(leftside + " == " + rightside);
    }

    /**
     * Show the equality.
     *
     * @return a String displaying the equality
     */
    public String toString() {
        return show();
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    /** Variable 1. */
    public String _value1;

    /** Variable 2. */
    public String _value2;

    /** Event 1. */
    public Event _event1;

    /** Event 2. */
    public Event _event2;

    /** Type of left hand side of equality. */
    public int _type1;

    /** Type of right hand side of equality. */
    public int _type2;

    /** The type of 'bool'. */
    public static final int BOOLTYPE = 0;

    /** The type of 'char'. */
    public static final int CHARTYPE = 1;

    /** The type of 'double'. */
    public static final int DOUBLETYPE = 2;

    /** The type of 'float'. */
    public static final int FLOATTYPE = 3;

    /** The type of 'int'. */
    public static final int INTTYPE = 4;

    /** The type of 'long'. */
    public static final int LONGTYPE = 5;

    /** The type of 'string'. */
    public static final int STRINGTYPE = 6;

    /** The type of VarInEventRef. */
    public static final int VARTYPE = 7;

    // /////////////////////////////////////////////////////////////////
    // // private methodes ////

    // Throw an exception if the type is out of range
    private void _checkType(int type) {
        if (type < BOOLTYPE || type > VARTYPE) {
            throw new RuntimeException("type '" + type + "' was out of "
                    + "range, it must be >= EqualVars.BOOLTYPE (" + BOOLTYPE
                    + ") and <= EqualVars.VARTYPE (" + VARTYPE + ")");
        }
    }
}
