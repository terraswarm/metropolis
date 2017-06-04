/* A constraint in the elaborated network.

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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // Constraint

/**
 * A constraint in the elaborated network.
 *
 * @author Xi Chen, contributor: Christopher Brooks
 * @version $Id: Constraint.java,v 1.55 2006/10/12 20:38:20 cxh Exp $
 */
public class Constraint implements Serializable {
    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new constraint in the network.
     *
     * @param kind
     *            the constraint type, one of {@link #LTL}, {@link #LOC},
     *            {@link #ELOC}, {@link #LTLSYNCH}, {@link #LTLSYNCHIMPLY},
     *            {@link #MINRATE}, {@link #MAXRATE}, {@link #MINDELTA},
     *            {@link #MAXDELTA}, {@link #PERIOD}, or
     *            {@link #DEFAULT_CONSTRAINT}.
     */
    public Constraint(int kind) {
        this(null, kind, -1);
    }

    /**
     * Build a new constraint in the network.
     *
     * @param container
     *            the node which contains this constraint.
     * @param kind
     *            the constraint type, one of {@link #LTL}, {@link #LOC},
     *            {@link #ELOC}, {@link #LTLSYNCH}, {@link #LTLSYNCHIMPLY},
     *            {@link #MINRATE}, {@link #MAXRATE}, {@link #MINDELTA},
     *            {@link #MAXDELTA}, {@link #PERIOD}, or
     *            {@link #DEFAULT_CONSTRAINT}.
     * @param index
     *            the sequence number of the constraint.
     */
    public Constraint(Object container, int kind, int index) {
        _kind = kind;
        _index = index;
        _source = new String();

        if ((_kind < LTL) || (_kind >= DEFAULT_CONSTRAINT)) {
            throw new RuntimeException("kind '" + _kind + "' was out of "
                    + "range, it must be >= " + LTL + " and < "
                    + DEFAULT_CONSTRAINT);
        }

        if (container == null) {
            _container = null;
        } else {
            _container = Network.net.getNode(container);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add an event reference to the constraint. If the constraint already has
     * an event equal to the event argument, then the event is not added to the
     * constraint, otherwise the event is added to the end of the list of events
     * for this constraint.
     *
     * @param event
     *            the event reference to be added. Note that null is a
     *            legitimate event.
     * @see #getEvents()
     */
    public void addEvent(Event event) {
        // FIXME: why use a linked list here and check whether it
        // is already present. Instead, use a LinkedHashSet.
        Iterator events = _events.iterator();

        while (events.hasNext()) {
            Event existingEvent = (Event) events.next();

            if (existingEvent == null && event == null) {
                return;
            }
            if (existingEvent != null && existingEvent.equals(event)) {
                return;
            }
        }

        _events.addLast(event);
    }

    /**
     * Add a formula text of the constraint for a particular backend tool. If a
     * formula with the same key has already been added, then keep the previous
     * formula, the new formula is <b>not</b> added.
     *
     * @param key
     *            an integer key for a particular backend tool, one of
     *            {@link #SYSTEMC_FORMULA} or {@link #TRACE_FORMULA}.
     * @param formula
     *            The formula text of the constraint.
     * @see #getFormula(Integer)
     */
    public void addFormula(Integer key, String formula) {
        // FIXME: Use enum or type safe enum.

        if ((key.intValue() < SYSTEMC_FORMULA.intValue())
                || (key.intValue() >= DEFAULT_FORMULA.intValue())) {
            throw new RuntimeException("key '" + key + "' was out of "
                    + "range, it must be >= SYSTEMC_FORMULA ("
                    + SYSTEMC_FORMULA + ") and < DEFAULT_FORMULA("
                    + DEFAULT_FORMULA + ")");
        }
        // FIXME: why use a linked list here and check whether it
        // is already present. Instead, use a LinkedHashSet.
        if (_formulas.containsKey(key)) {
            return;
        }

        _formulas.put(key, formula);
    }

    /**
     * Add a quantity to the constraint. The object argument must be a node in
     * the network that {@link Network#getNode(Object)} can find. The object to
     * be added must also by of kind {@link MMType#QUANTITY}, if the object to
     * be added is not of kind {@link MMType#QUANTITY}, then it is not added to
     * the list of quantities for this constraint. If the object argument is
     * already referenced by this constraint, the it is not added to the list of
     * quantities for this constraint.
     *
     * @param object
     *            the quantity object to be added.
     * @see #getQuantities()
     */
    public void addQuantity(Object object) {
        INode quantityINode = Network.net.getNode(object);

        if (quantityINode == null) {
            throw new RuntimeException("Could not find '" + object
                    + "' in Network.net. Perhaps you need to call "
                    + "Network.addNode()?");
        }

        if (quantityINode.getType().getKind() != MMType.QUANTITY) {
            return;
        }

        Iterator quantities = _quantities.iterator();
        while (quantities.hasNext()) {
            INode quantity = (INode) quantities.next();

            if (quantity.equals(quantityINode)) {
                return;
            }
        }

        // FIXME: why use a linked list here and check whether it
        // is already present. Instead, use a HashSet ot LinkedHashSet
        _quantities.addLast(quantityINode);
    }

    /**
     * Add a pair of structural keyword and its resolved value to the
     * constraint. The types of values:
     * <ul>
     * <li>getconnectionnum - Integer
     * <li>getnthconnectionsrc - INode
     * <li>getnthconnectionport - IPort
     * </ul>
     * If the keyword has already been added, the old value is retained and the
     * new value is ignored.
     *
     * @param keyword
     *            the structural keyword.
     * @param value
     *            the resolved value of the keyword, which must be one of
     *            Integer, INode or IPort.
     * @see #getStructureValues()
     */
    public void addStructureValue(String keyword, Object value) {

        // FIXME: why use a linked list here and check whether it
        // is already present. Instead, use a HashSet or a LinkedHashSet.

        if (_structureValues.containsKey(keyword)) {
            return;
        }
        if (!(value instanceof Integer) && !(value instanceof INode)
                && !(value instanceof IPort)) {
            throw new RuntimeException("The value parameter must be one of "
                    + "Integer, INode or IPort, it was a: " + value);

        }

        // FIXME: should we check that the keyword starts with
        // the appropriate string?
        _structureValues.put(keyword, value);
    }

    /**
     * Get the container of the constraint.
     *
     * @return The network node that contains the constraint.
     * @see #setContainer(Object)
     */
    public INode getContainer() {
        return _container;
    }

    /**
     * Get the list of event references in the constraint.
     *
     * @return A linked list of events.
     * @see #addEvent(Event)
     */
    public LinkedList getEvents() {
        return _events;
    }

    /**
     * Get a formula text using an integer key for a particular backend tool.
     *
     * @param key
     *            an integer key for a particular backend tool, one of
     *            {@link #SYSTEMC_FORMULA} or {@link #TRACE_FORMULA}.
     * @return A string containing a formula text.
     * @see #addFormula(Integer, String)
     */
    public String getFormula(Integer key) {
        // FIXME: Use enum or type safe enum.
        return (String) _formulas.get(key);
    }

    /**
     * Get the index of the constraint.
     *
     * @return The index of the constraint.
     * @see #setIndex(int)
     */
    public int getIndex() {
        return _index;
    }

    /**
     * Get the type of the constraint.
     *
     * @return The type of the constraint, one of {@link #LTL}, {@link #LOC},
     *         {@link #ELOC}, {@link #LTLSYNCH}, {@link #LTLSYNCHIMPLY},
     *         {@link #MINRATE}, {@link #MAXRATE}, {@link #MINDELTA},
     *         {@link #MAXDELTA}, {@link #PERIOD}, or
     *         {@link #DEFAULT_CONSTRAINT}.
     * @see #setKind(int)
     */
    public int getKind() {
        return _kind;
    }

    /**
     * Get the list of quantities in the constraint.
     *
     * @return A linked list of quantities.
     * @see #addQuantity(Object)
     */
    public LinkedList getQuantities() {
        return _quantities;
    }

    /**
     * Get the source code of the constraint.
     *
     * @return A string that contains the source code of the constraint.
     * @see #setSource(String)
     */
    public String getSource() {
        return _source;
    }

    /**
     * Get the hash table that stores all the structure keywords and values in
     * the constraint.
     *
     * @return A hash table of (keyword, value) pairs.
     * @see #addStructureValue(String, Object)
     */
    public Hashtable getStructureValues() {
        return _structureValues;
    }

    /**
     * Set the container of the constraint.
     *
     * @param container
     *            the container of the constraint. The container should be
     *            non-null and castable to {@link INode}.
     * @see #getContainer()
     */
    public void setContainer(Object container) {
        // FIXME: shouldn't the type of container be INode?
        // FIXME: Why is it required theat the container be non-null?
        // we can construct a Constraint object with a null container.
        if (container == null) {
            throw new RuntimeException("container was null? Constraint:\n"
                    + show());
        }

        _container = (INode) container;
    }

    /**
     * Set the index of the constraint.
     *
     * @param index
     *            The index of the constraint.
     * @see #getIndex()
     */
    public void setIndex(int index) {
        _index = index;
    }

    /**
     * Set the type of the constraint.
     *
     * @param kind
     *            The type of the constraint one of {@link #LTL}, {@link #LOC},
     *            {@link #ELOC}, {@link #LTLSYNCH}, {@link #LTLSYNCHIMPLY},
     *            {@link #MINRATE}, {@link #MAXRATE}, {@link #MINDELTA},
     *            {@link #MAXDELTA}, {@link #PERIOD}, or
     *            {@link #DEFAULT_CONSTRAINT}.
     * @see #getKind()
     */
    public void setKind(int kind) {
        if ((kind < LTL) || (kind >= DEFAULT_CONSTRAINT)) {
            throw new RuntimeException("kind '" + kind + "' was out of "
                    + "range, it must be >= " + LTL + " and < "
                    + DEFAULT_CONSTRAINT);
        }
        _kind = kind;
    }

    /**
     * Set the source code of the constraint.
     *
     * @param source
     *            The source code of the constraint. If the source parameter
     *            contains any single quotes, the single quotes are replaced
     *            with double quotes.
     * @see #getSource()
     */
    public void setSource(String source) {
        // FIXME: why are single quotes turned in to double quotes?
        _source = source.replace('\'', '"');
    }

    /**
     * Get a String providing information about this constraint.
     *
     * @return A String providing information about this constraint.
     */
    public String show() {
        StringBuffer results = null;

        switch (_kind) {
        case LTL:
            results = new StringBuffer("LTL");
            break;

        case LOC:
            results = new StringBuffer("LOC");
            break;

        case ELOC:
            results = new StringBuffer("ELOC");
            break;

        case LTLSYNCH:
            results = new StringBuffer("LTL SYNCH");
            break;

        case LTLSYNCHIMPLY:
            results = new StringBuffer("LTL SYNCH IMPLY");
            break;

        default:
            if (_kind < DEFAULT_CONSTRAINT) {
                // FIXME: should we have clauses for MINRATE,
                // MAXRATE, MINDELTA, PERIOD?
                // These correspond with kinds of BuiltINLOC's, but
                // shouldn't this base class print them?
                results = new StringBuffer("Unknown Constraint " + _kind + "\n");
                return results.toString();
            } else {
                throw new RuntimeException("Unexpected constraint type: "
                        + _kind);
            }
            // break;
        }

        results.append(_showHeader());
        results.append(_showFormulas());

        if (_events.size() != 0) {
            results.append(_indent + "o Event references: \n");

            Iterator events = _events.iterator();
            while (events.hasNext()) {
                Event event = (Event) events.next();
                results.append(_indent + "  - "
                        + ((event == null) ? "null" : event.show()) + "\n");
            }
        } else {
            results.append(_indent + "o No event references\n");
        }

        results.append(_showQuantities());
        results.append(_showStructuralValues());

        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    /** Kind for ltl constraint. */
    public static final int LTL = 0x1;

    /**
     * Kind for loc constraint.
     *
     * @see Network#generateLOCCheckers()
     */
    public static final int LOC = 0x2;

    /** Kind for eloc constraint. */
    public static final int ELOC = 0x4;

    /** Kind for ltl synch constraint. */
    public static final int LTLSYNCH = 0x8;

    /** Kind for one way ltl synch constraint. */
    public static final int LTLSYNCHIMPLY = 0x10;

    /** Kind for built-in loc constraint minrate. */
    public static final int MINRATE = 0x20;

    /** Kind for built-in loc constraint maxrate. */
    public static final int MAXRATE = 0x40;

    /** Kind for built-in loc constraint mindelta. */
    public static final int MINDELTA = 0x80;

    /** Kind for built-in loc constraint maxdelta. */
    public static final int MAXDELTA = 0x100;

    /** Kind for built-in loc constraint period. */
    public static final int PERIOD = 0x200;

    /** Kind for a default constraint. */
    public static final int DEFAULT_CONSTRAINT = 0x400;

    /** Formula type for SystemC backend. */
    public static final Integer SYSTEMC_FORMULA = new Integer(0);

    /**
     * Formula type for trace checking.
     *
     * @see Network#generateLOCCheckers()
     */
    public static final Integer TRACE_FORMULA = new Integer(1);

    /** Formula type for default backend tool. */
    public static final Integer DEFAULT_FORMULA = new Integer(2);

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Return a string that includes the constraint formulas.
     *
     * @return A string that includes the constraint formulas.
     */
    protected String _showFormulas() {
        // Note: we have these _showXXX() methods so we can avoid
        // code duplication with the derived classes.
        StringBuffer results = new StringBuffer();
        Enumeration keys = _formulas.keys();

        if (!keys.hasMoreElements()) {
            results.append(_indent + "o No formulas\n");
        } else {
            results.append(_indent + "o Formulas: \n");

            while (keys.hasMoreElements()) {
                String keyStr;
                Integer key = (Integer) keys.nextElement();
                String text = (String) _formulas.get(key);

                if (key.equals(SYSTEMC_FORMULA)) {
                    keyStr = new String("SYSTEMC_FORMULA");
                } else if (key.equals(TRACE_FORMULA)) {
                    keyStr = new String("TRACE_FORMULA");
                } else {
                    throw new RuntimeException("Unexpected formula type: "
                            + key);
                }

                results.append(_indent + "  " + keyStr + " : " + text + "\n");
            }
        }
        return results.toString();
    }

    /**
     * Return a string that includes the constraint index, container and source.
     *
     * @return A header.
     */
    protected String _showHeader() {
        return " Constraint (# " + _index + ")\n" + _indent + "o Container: "
                + (_container == null ? "null" : _container.getName()) + "\n"
                + _indent + "o Source: " + _source + "\n";
    }

    /**
     * Return a string that includes the quantities.
     *
     * @return A string that includes the quantities
     */
    protected String _showQuantities() {
        StringBuffer results = new StringBuffer();
        if (_quantities.size() != 0) {
            results.append(_indent + "o Quantities: \n");

            Iterator quantities = _quantities.iterator();
            while (quantities.hasNext()) {
                INode quantity = (INode) quantities.next();
                // FIXME: INode.show() should take an indent argument.
                results.append(_indent + "  - " + quantity.show() + "\n");
            }
        } else {
            results.append(_indent + "o No quantities\n");
        }
        return results.toString();
    }

    /**
     * Return a string that includes the structural values.
     *
     * @return A string that includes the structural values.
     */
    protected String _showStructuralValues() {

        StringBuffer results = new StringBuffer();
        Enumeration keywords = _structureValues.keys();
        if (!keywords.hasMoreElements()) {
            results.append(_indent + "o No structural values\n");
        } else {
            results.append(_indent + "o Structural values: \n");

            while (keywords.hasMoreElements()) {
                String keyword = (String) keywords.nextElement();
                Object value = _structureValues.get(keyword);

                if (value instanceof Integer) {
                    results.append(_indent + "  - " + keyword + " = "
                            + value.toString() + "\n");
                } else if (value instanceof INode) {
                    results.append(_indent + "  - " + keyword + " = "
                            + ((INode) value).getName() + "\n");
                } else if (value instanceof IPort) {
                    results.append(_indent + "  - " + keyword + " = "
                            + ((IPort) value).getName() + "\n");
                } else {
                    throw new RuntimeException("Unexpected structure value. "
                            + "The value parameter must be one of "
                            + "Integer, INode or IPort, it was a: " + value);
                }
            }
        }
        return results.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The node where this constraint is defined. */
    protected INode _container;

    /** The kind of constraint. */
    protected int _kind;

    /** The string with which to indent the output of the show() method. */
    protected static String _indent = "          ";

    /** The sequence number(index) of this constraint in the container. */
    protected int _index;

    /** The source code for the constraint. */
    protected String _source;

    /**
     * The formula texts for the constraint used for other backend tools. The
     * hashtable maps a keyword to a formula text of the constraint for a
     * particular backend tool.
     */
    protected Hashtable _formulas = new Hashtable();

    /**
     * Mapping from structural keywords to their resolved values. For example:
     * getconnectionnum(MediumObject, IfName) -> 4 <br>
     * getnthconnectionsrc(MediumObject, IfName, n) -> p1
     */
    protected Hashtable _structureValues = new Hashtable();

    /** Event references referred by this constraint. */
    protected LinkedList _events = new LinkedList();

    /** Quantities referenced by this constraint. */
    protected LinkedList _quantities = new LinkedList();
}
