/* A declaration of a method or a constructor.

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

package metropolis.metamodel.frontend;

import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.ConstraintDeclNode;
import metropolis.metamodel.nodetypes.TypeNode;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// ////////////////////////////////////////////////////////////////////////
// // MethodDecl
/**
 * A declaration of a method or constructor. Methods and constructors are
 * members of objects, but they can be overloaded, i.e. several methods of the
 * same object can have the same name as long as they can be distinguished by
 * the type of their arguments. Methods defined in classes and interfaces can be
 * overridden by methods defined in subclasses.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: MethodDecl.java,v 1.20 2006/10/12 20:33:54 cxh Exp $
 */
public class MethodDecl extends MemberDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new method or constructor. Provide the method name, return type,
     * modifiers, effect, node of the AST where the method is declared,
     * container, list of parameter declarations, and list of ports used by the
     * method. If the name of the method matches the name of the container, then
     * the method is a constructor.
     *
     * @param name
     *            Name of the method.
     * @param type
     *            Return type of the method.
     * @param modifiers
     *            Set of modifiers of the method.
     * @param effect
     *            Effect on the state of media.
     * @param source
     *            Node of the AST where the method is declared.
     * @param container
     *            Object class where the method is declared.
     * @param paramList
     *            List of parameter declarations.
     * @param usePorts
     *            List of ports used by the method.
     */
    public MethodDecl(String name, TypeNode type, int modifiers, int effect,
            TreeNode source, MetaModelDecl container, List paramList,
            List usePorts) {

        super(name, (source instanceof ConstraintDeclNode) ? CG_CNSTFORMULA
                : (name.equals(container.getName()) ? CG_CONSTRUCTOR
                        : CG_METHOD), type, modifiers, source, container);
        _effect = effect;
        _paramList = paramList;
        _usePorts = usePorts;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the list of parameter of this method.
     *
     * @return A List with the FormalParameterDecl of the parameters of this
     *         method.
     */
    public List getParams() {
        return _paramList;
    }

    /**
     * Get the list of ports used in this method.
     *
     * @return A List with the PortDecls of the ports used in this method.
     */
    public List getUsePorts() {
        return _usePorts;
    }

    /**
     * Add a port to the list of ports used in this method. If the port is
     * already in the list, do nothing.
     *
     * @param port
     *            Port used by this method.
     */
    public void addUsePort(PortDecl port) {
        if (!_usePorts.contains(port))
            _usePorts.add(port);
    }

    /**
     * Get the effect of this method on the state of media.
     *
     * @return The effect of this method.
     * @see #setEffect(int)
     */
    public int getEffect() {
        return _effect;
    }

    /**
     * Set the effect of this method on the state of media.
     *
     * @param effect
     *            Effect of this method.
     * @see #getEffect()
     */
    public void setEffect(int effect) {
        _effect = effect;
    }

    /**
     * Get the method overridden by this method.
     *
     * @return The MethodDecl of the method overridden by this method, or null
     *         if the method doesn't override other methods.
     * @see #setOverrides(MethodDecl)
     */
    public MethodDecl getOverrides() {
        return _overrides;
    }

    /**
     * Set the method overridden by this method.
     *
     * @param overrides
     *            Method overridden by this method.
     * @see #getOverrides()
     */
    public void setOverrides(MethodDecl overrides) {
        _overrides = overrides;
    }

    /**
     * Get the list of method that override current method.
     *
     * @return The list of methods overriding current method.
     */
    public List getOverridedBy() {
        return _overridedBy;
    }

    /**
     * Register a new method overriding this method. If the method was already
     * in the list of overriders of this method, then do nothing.
     *
     * @param overrider
     *            Method overriding this method.
     */
    public void addOverrider(MethodDecl overrider) {
        if (!_overridedBy.contains(overrider))
            _overridedBy.add(overrider);
    }

    /**
     * Get the list of interface methods that are implemented by this method.
     *
     * @return The list of MethodDecl of interface methods that are implemented
     *         by this method.
     */
    public List getImplements() {
        return _implements;
    }

    /**
     * Register a new interface method that is implemented by this method. If
     * the method was already in the list of implemented methods, do nothing.
     *
     * @param implemented
     *            Method implemented by this method.
     */
    public void addImplement(MethodDecl implemented) {
        if (!_implements.contains(implemented))
            _implements.add(implemented);
    }

    /**
     * Check it the method is an interface method. An interface method is
     * defined recursively: a method that is declared in a Port interface; or a
     * method that implements an interface method; or a method that overrides an
     * interface method.
     *
     * @return true iff the method is an interface method.
     */
    public boolean isInterfaceMethod() {
        ObjectDecl container = (ObjectDecl) _container;
        if (container.isPortInterface())
            return true;
        if ((_overrides != null) && (_overrides.isInterfaceMethod()))
            return true;
        Iterator iter = _implements.iterator();
        while (iter.hasNext()) {
            MethodDecl impl = (MethodDecl) iter.next();
            container = (ObjectDecl) impl.getContainer();
            if (container.isPortInterface())
                return true;
        }
        return false;
    }

    /**
     * Get the set of all label names in this method.
     *
     * @return The set of all label names in this method. The contents of this
     *         set should not be modified.
     */
    public Set allLabelNames() {
        return _labels.keySet();
    }

    /**
     * Get a collection of all label declarations in this method.
     *
     * @return A collection with all label declarations in this method. The
     *         contents of this collection should not be modified.
     */
    public Iterator allLabelDecls() {
        return _labels.values().iterator();
    }

    /**
     * Get a label of this method. Return null if a label with that name has not
     * been declared in this method.
     *
     * @param name
     *            Name of the label being looked up.
     * @return The label declaration or null if the label was not found.
     */
    public StmtLblDecl getLabel(String name) {
        return (StmtLblDecl) _labels.get(name);
    }

    /**
     * Add a new label to this method. Check that the names of the labels in
     * this method are unique.
     *
     * @param label
     *            Declaration of the label to be added.
     * @exception RuntimeException
     *                if the method already has a label with that name.
     */
    public void addLabel(StmtLblDecl label) {
        Object previous = _labels.put(label.getName(), label);
        if (previous != null) {
            throw new RuntimeException("label '" + label.getName()
                    + "' in method '" + this.getName() + "' redeclared");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Effect of this method on the state of media. */
    protected int _effect;

    /** Declarations of the parameters of this method. */
    protected List _paramList;

    /** Ports (PortDecls) used by this method. */
    protected List _usePorts;

    /**
     * MethodDecl that this MethodDecl overrides. It is null if this is the
     * initial declaration of the method.
     */
    protected MethodDecl _overrides = null;

    /** List of MethodDecls that override this declaration. */
    protected final List _overridedBy = new LinkedList();

    /** List of interface method this MethodDecl implements. */
    protected final List _implements = new LinkedList();

    /** Hashtable of labels inside the method. */
    protected final Hashtable _labels = new Hashtable();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

}
