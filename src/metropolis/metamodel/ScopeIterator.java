/* An iterator for declarations from an scope. Instead of looking up
 all matches of a declaration at once, declarations are found on an
 as-needed basis.

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
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An iterator for declarations from an scope. Instead of looking up all matches
 * of a declaration at once, declarations are found on an as-needed basis.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Titanium project, under funding from the DARPA, DoE, and Army Research
 * Office.
 *
 * @author Jeff Tsay
 * @version $Id: ScopeIterator.java,v 1.19 2006/10/12 20:32:07 cxh Exp $
 */
public class ScopeIterator implements Iterator {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new Scope iterator with default values.
     */
    public ScopeIterator() {
        _nextScope = null;
        _declIter = null;
        _name = null;
        _mask = 0;
    }

    /**
     * Create a new ScopeIterator that will look for declarations with a given
     * name and set of valid types, in current scope and enclosing scopes (if
     * nextScope is not null).
     *
     * @param nextScope
     *            Next Scope to be lookup (null if none).
     * @param declIter
     *            Iterator over all the Decls in current scope.
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     */
    public ScopeIterator(Scope nextScope, ListIterator declIter, String name,
            int mask) {
        _nextScope = nextScope;
        _declIter = declIter;
        _name = name;
        _mask = mask;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if the iterator contains more elements with the given name and
     * kind.
     *
     * @return true if the iterator contains more Decls with the given name and
     *         kind.
     */
    public boolean hasNext() {
        // System.out.println("ScopeIterator : hasNext for " + _name);

        try {
            nextDecl();

            // Rewind to valid Decl.
            _declIter.previous();
        } catch (NoSuchElementException e) {
            // System.out.println("ScopeIterator : hasNext for " + _name +
            // " = false");
            return false;
        }
        // System.out.println("ScopeIterator : hasNext for " + _name +
        // " = true");
        return true;
    }

    /**
     * Check if there is more than one matching Decl that can be reached.
     *
     * @return true if there is more than one such Decls.
     */
    public boolean moreThanOne() {

        // System.out.println("ScopeIterator: moreThanOne for " + _name);
        if (_declIter == null) {
            // empty list
            return false;
        }

        Decl lastMatch = null;
        int movesAfterMatch = 0;
        int matches = 0;

        while (_declIter.hasNext() && (matches < 2)) {

            Decl d = (Decl) _declIter.next();

            // make sure we don't have a reference to the last found match
            if (d.matches(_name, _mask) && (d != lastMatch)) {
                matches++;
                lastMatch = d;
            }

            if (matches > 0) {
                movesAfterMatch++;
            }
        }

        if (matches >= 1) {

            // rewind back to first matching Decl
            for (; movesAfterMatch > 0; movesAfterMatch--) {
                _declIter.previous();
            }

            if (matches >= 2) {
                // System.out.println("ScopeIterator: moreThanOne = true" +
                // " for " + _name);
                return true;
            }

            if (_nextScope == null) {
                // just one match
                return false;
            }

            ScopeIterator nextScopeIterator = _nextScope.lookupFirst(_name,
                    _mask);

            while (nextScopeIterator.hasNext()) {
                Decl nextMatch = nextScopeIterator.nextDecl();

                // make sure we don't have a reference to the last found match
                if (lastMatch != nextMatch) {
                    return true;
                }
            }

            return false;

        } else {
            // matches == 0
            // don't bother to move the iterator back, since there are
            // no matches

            if (_nextScope == null) {
                // System.out.println("ScopeIterator: moreThanOne = " +
                // false for " + _name);
                return false;
            }

            // move on to the next scope, discarding last scope

            _declIter = _nextScope.allLocalDecls();
            _nextScope = _nextScope.parent();

            // try again on this modified ScopeIterator
            return moreThanOne();
        }
    }

    /**
     * Get the next Decl matching the name and kind provided in the constructor.
     *
     * @return The next Decl matching the name and kind.
     */
    public Object next() {
        return nextDecl();
    }

    /**
     * Get the next Decl matching the name and kind provided in the constructor.
     *
     * @return The next Decl matching the name and kind.
     * @exception NoSuchElementException
     *                If the iterator does not contain more elements. hasMore()
     *                should be used to avoid getting this exception.
     */
    public Decl nextDecl() {

        if (_declIter == null) {
            throw new NoSuchElementException("No elements in ScopeIterator.");
        }

        do {

            while (_declIter.hasNext()) {
                Decl decl = (Decl) _declIter.next();

                if (decl == null)
                    continue;

                if (decl.matches(_name, _mask)) {
                    // System.out.println("ScopeIterator : found match " +
                    // " for " + _name);
                    return decl;
                }
            }

            if (_nextScope == null) {
                // System.out.println("ScopeIterator : no more elements " +
                // "looking for " + _name);

                throw new NoSuchElementException(
                        "No more elements in ScopeIterator.");
            }

            // System.out.println("ScopeIterator : going to next " +
            // "scope looking for " + _name);

            _declIter = _nextScope.allLocalDecls();
            _nextScope = _nextScope.parent();

        } while (true);
    }

    /**
     * Get the first Decl that can be found by this iterator. If there is more
     * than one Decl that can be found in the same Scope, return null.
     *
     * @return The element of the first Scope that can be found is there is
     *         exactly one matching decl in that Scope, or null otherwise.
     */
    public Decl nonConflictingDecl() {
        if (!hasNext())
            return null;
        Decl d = nextDecl();
        Scope current = _nextScope;
        if (!hasNext())
            return d;
        /* Decl d2 = */nextDecl();
        if (_nextScope != current)
            return d;
        return null;
    }

    /**
     * Get the first Decl without advancing the iterator.
     *
     * @return The first declaration.
     */
    public Decl peek() {
        Decl returnValue = nextDecl();

        // Rewind back to valid Decl.
        _declIter.previous();

        return returnValue;
    }

    /**
     * This method is not supported by this iterator.
     *
     * @exception RuntimeException
     *                Always thrown, because the method remove() of the Iterator
     *                interface is not supported.
     */
    public void remove() {
        // Can't do this!!!
        throw new RuntimeException("remove() not supported on ScopeIterator");
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Next scope to lookup. If the scope is null, we will look up the name only
     * in the current scope; if it is not null, we will look up the name in
     * current scope and all enclosing scopes.
     */
    protected Scope _nextScope;

    /** Iterator over all declarations in the current scope. */
    protected ListIterator _declIter;

    /** Name to be found. All Decls with a different name will be ignored. */
    protected String _name;

    /**
     * Valid kinds of declarations. All Decls with a kind not in this set will
     * be ignored.
     */
    protected int _mask;
}
