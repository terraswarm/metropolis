/* An scope for declarations, which may be contained in another
 scope.

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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

// ////////////////////////////////////////////////////////////////////////
// // Scope
/**
 * An scope for declarations, which may be contained in another scope. Scopes
 * are used to implement scoping for declarations.
 *
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Titanium project, under funding from the DARPA, DoE, and Army Research
 * Office.
 *
 * @author Jeff Tsay
 * @version $Id: Scope.java,v 1.21 2006/10/12 20:32:05 cxh Exp $
 */
public class Scope implements Serializable {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Construct an empty scope. */
    public Scope() {
        this(null, new LinkedList());
    }

    /**
     * Construct an scope nested inside the parent argument, without its own
     * local Decl's.
     *
     * @param parent
     *            Enclosing scope.
     */
    public Scope(Scope parent) {
        this(parent, new LinkedList());
    }

    /**
     * Construct an scope nested inside the parent argument, with the given List
     * of Decl's in this scope itself.
     *
     * @param parent
     *            Enclosing scope.
     * @param declList
     *            Declarations to be used in the scope.
     */
    public Scope(Scope parent, List declList) {
        _parent = parent;
        _declList = declList;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Adds a mapping to the argument decl in this Scope. This does not affect
     * any Scopes in which this Scope is nested.
     *
     * @param decl
     *            Declaration to be added.
     */
    public void add(Decl decl) {
        _declList.add(decl);
    }

    /**
     * Get an iterator that will iterate over all the Decls in this Scope and in
     * any parent scope, if any.
     *
     * @return The ScopeIterator.
     */
    public ScopeIterator allDecls() {
        return lookupFirst(Decl.ANY_NAME, Decl.CG_ANY, false);
    }

    /**
     * Get an iterator that will iterate over all the Decls in the current scope
     * and any parent scope, if any, that have any of the categories bits set in
     * mask.
     *
     * @param mask
     *            Valid kinds of declarations.
     * @return The ScopeIterator.
     */
    public ScopeIterator allDecls(int mask) {
        return lookupFirst(Decl.ANY_NAME, mask, false);
    }

    /**
     * Get an ScopeIterator that will iterate over all the Decls that have the
     * same name in the current scope and any parent Scope, if any.
     *
     * @param name
     *            Name to be found.
     * @return The ScopeIterator.
     */
    public ScopeIterator allDecls(String name) {
        return lookupFirst(name, Decl.CG_ANY, false);
    }

    /**
     * Get a ListIterator that will iterate over all the local Decls in this
     * Scope but not in any parent scopes.
     *
     * @return The ListIterator.
     */
    public ListIterator allLocalDecls() {
        return _declList.listIterator();
    }

    /**
     * Get an iterator that will iterate over all the local Decls in this Scope
     * and in any parent Scopes that have a matching mask.
     *
     * @param mask
     *            Valid kinds of declarations.
     * @return The ListIterator.
     */
    public ScopeIterator allLocalDecls(int mask) {
        return lookupFirst(Decl.ANY_NAME, mask, true);
    }

    /**
     * Get an iterator that will iterate over all the local Decls in this Scope
     * and in any parent Scopes that have the same name.
     *
     * @param name
     *            The searched for name.
     * @return The ListIterator.
     */
    public ScopeIterator allLocalDecls(String name) {
        return lookupFirst(name, Decl.CG_ANY, true);
    }

    /**
     * Copy the declList from scope. The declarations in the current scope are
     * deleted before performing the copy.
     *
     * @param scope
     *            Scope being copied.
     */
    public void copyDeclList(Scope scope) {
        _declList.clear();
        _declList.addAll(scope._declList);
    }

    /**
     * Get the list of declarations of a given scope.
     *
     * @return The list of declarations of this scope.
     */
    public List getDecls() {
        return _declList;
    }

    /**
     * Lookup a decl by name in the current scope, do not look in the parent
     * scope, if any.
     *
     * @param name
     *            Name to be found.
     * @return A declaration with that name, or null if none was found.
     */
    public Decl lookup(String name) {
        return lookup(name, Decl.CG_ANY, new boolean[1], false);
    }

    /**
     * Lookup a decl by name and mask in the current scope, do not look in the
     * parent scope, if any.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @return A declaration of that name and kind, or null if none was found.
     */
    public Decl lookup(String name, int mask) {
        return lookup(name, mask, new boolean[1], false);
    }

    /**
     * Lookup a decl by name in the current scope, do not look in the parent
     * scope. Set more[0] to true if there are other decls with the same name in
     * the scope.
     *
     * @param name
     *            The name to be found.
     * @param more
     *            Dummy parameter to allow several return values (the
     *            declaration and a boolean).
     * @return A declaration with that name, or null if none was found.
     */
    public Decl lookup(String name, boolean[] more) {
        return lookup(name, Decl.CG_ANY, more, false);
    }

    /**
     * Lookup a decl by name and mask in the current scope, do not look in the
     * parent scope, if any. Set more[0] to true if there is one or more decls
     * with the same name in the scope. Note that more[0] will be true if the
     * other decls have the same name but different masks.
     *
     * @param name
     *            The name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @param more
     *            Dummy parameter to allow several return values (the
     *            declaration and a boolean).
     * @return A declaration with that name and kind, or null if none was found.
     */
    public Decl lookup(String name, int mask, boolean[] more) {
        return lookup(name, mask, more, false);
    }

    /**
     * Lookup a decl by name and mask in the current scope or optionally in the
     * parent scope. Set more[0] to true if there is one or more decls with the
     * same name in the scope. Note that more[0] will be true if the other decls
     * have the same name but different masks. If the local argument is true,
     * then look in the parent scope, if any. If it is false, then do not look
     * in the parent scopes.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @param more
     *            Dummy parameter to allow several return values (the
     *            declaration and a boolean).
     * @param local
     *            false if parent scopes have to be looked up.
     * @return A declaration with that name and kind, or null if none was found.
     */
    public Decl lookup(String name, int mask, boolean[] more, boolean local) {
        ScopeIterator Iterator = lookupFirst(name, mask, local);

        if (Iterator.hasNext()) {
            Decl returnValue = (Decl) Iterator.next();
            more[0] = Iterator.hasNext();
            return returnValue;
        }
        more[0] = false;
        return null;
    }

    /**
     * Lookup a decl by name in the current scope and in any parent Scopes, if
     * any.
     *
     * @param name
     *            Name to be found.
     * @return A ScopeIterator that will iterate on all possible decls with that
     *         name in current and parent scopes.
     */
    public ScopeIterator lookupFirst(String name) {
        return lookupFirst(name, Decl.CG_ANY, false);
    }

    /**
     * Lookup a decl by name and mask in the current scope and in any parent
     * scopes, if any.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @return A ScopeIterator that will iterate on all possible decls with that
     *         name and kind in current and parent scopes.
     */
    public ScopeIterator lookupFirst(String name, int mask) {
        return lookupFirst(name, mask, false);
    }

    /**
     * Lookup a decl by name and mask in the current scope or the parent scopes,
     * if any. If the local argument is true, then look in the parent scopes, if
     * the local argument is false, the do not look in the parent scopes.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @param local
     *            false if parent scopes have to be looked up.
     * @return A ScopeIterator that will iterate on all possible decls with that
     *         name in the current scope, and in parent scopes if the local
     *         argument is true.
     */
    public ScopeIterator lookupFirst(String name, int mask, boolean local) {
        // If local is false, then get the parent scope
        Scope parent = local ? null : _parent;

        return new ScopeIterator(parent, _declList.listIterator(), name, mask);
    }

    /**
     * Lookup a decl by name in the current scope.
     *
     * @param name
     *            Name to be found.
     * @return A ScopeIterator that will iterate on all possible decls with that
     *         name in the current scope.
     */
    public ScopeIterator lookupFirstLocal(String name) {
        return lookupFirst(name, Decl.CG_ANY, true);
    }

    /**
     * Lookup a decl by name and mask in the current scope.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @return A ScopeIterator that will iterate on all possible decls with that
     *         name in the current scope.
     */
    public ScopeIterator lookupFirstLocal(String name, int mask) {
        return lookupFirst(name, mask, true);
    }

    /**
     * Lookup a decl by name in the current scope.
     *
     * @param name
     *            to be found.
     * @return A declaration with that name, or null if none was found.
     */
    public Decl lookupLocal(String name) {
        return lookup(name, Decl.CG_ANY, new boolean[1], true);
    }

    /**
     * Lookup a decl by name and mask in the current scope.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @return A declaration with that name and kind, or null if none was found.
     */
    public Decl lookupLocal(String name, int mask) {
        return lookup(name, mask, new boolean[1], true);
    }

    /**
     * Lookup a decl by name in the current scope. Set more[0] to true if there
     * is one or more decls with the same name in the scope. Note that more[0]
     * will be true if the other decls have the same name but different masks.
     *
     * @param name
     *            Name to be found.
     * @param more
     *            Dummy parameter to allow several return values (the
     *            declaration and a boolean).
     * @return A declaration with that name and kind, or null if none was found.
     */
    public Decl lookupLocal(String name, boolean[] more) {
        return lookup(name, Decl.CG_ANY, more, true);
    }

    /**
     * Lookup a decl by name and mask in the current scope Set more[0] to true
     * if there is one or more decls with the same name in the scope. Note that
     * more[0] will be true if the other decls have the same name but different
     * masks.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @param more
     *            Dummy parameter to allow several return values (the
     *            declaration and a boolean).
     * @return A declaration with that name and kind, or null if none was found.
     */
    public Decl lookupLocal(String name, int mask, boolean[] more) {
        return lookup(name, mask, more, true);
    }

    /**
     * Check if there is more than one matching Decl only in this Scope.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @return true if there is more than one declaration.
     */
    public boolean moreThanOne(String name, int mask) {
        return moreThanOne(name, mask, false);
    }

    /**
     * Check if there is more than one matching Decl in this Scope, and if local
     * is true, in the parent scopes.
     *
     * @param name
     *            Name to be found.
     * @param mask
     *            Valid kinds of declarations.
     * @param local
     *            false if parent scopes have to be looked up
     * @return true if there is more than one declaration.
     */
    public boolean moreThanOne(String name, int mask, boolean local) {
        boolean[] more = new boolean[1];
        lookup(name, mask, more, local);
        return more[0];
    }

    /**
     * Get the parent scope of this Scope.
     *
     * @return The Scope enclosing the current Scope.
     */
    public Scope parent() {
        return _parent;
    }

    /**
     * Remove the first Decl that matches the decl arg.
     *
     * @param decl
     *            Declaration to be removed.
     */
    public void remove(Decl decl) {
        _declList.remove(decl);
    }

    /**
     * Set the parent scope of this Scope.
     *
     * @param parent
     *            Scope enclosing the current Scope.
     */
    public void setParent(Scope parent) {
        _parent = parent;
    }

    /**
     * Get a recursive String representation of this Scope.
     *
     * @return The String representation of the Scope.
     */
    public String toString() {
        return toString(true);
    }

    /**
     * Return a String representation of this Scope. If the recursive argument
     * is true, then append string representations of parent scopes as well.
     *
     * @param recursive
     *            True if parent scopes are also traversed.
     * @return A possibly recursive String representation of this Scope.
     */
    public String toString(boolean recursive) {
        ListIterator declIterator = _declList.listIterator();

        StringBuffer returnValue = new StringBuffer("[");

        while (declIterator.hasNext()) {
            Decl d = (Decl) declIterator.next();
            returnValue.append(d.toString());
            if (declIterator.hasNext()) {
                returnValue.append(", ");
            }
        }

        returnValue.append("] ");

        if (_parent != null) {
            returnValue.append("has parent\n");

            if (recursive) {
                returnValue.append(_parent.toString(true));
            }
        } else {
            returnValue.append("no parent\n");
        }
        return returnValue.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The parent of this Scope. */
    protected Scope _parent;

    /** The list of Decls in this Scope. */
    protected List _declList;
}
