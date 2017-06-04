/*

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

package metropolis.metamodel.backends.systemc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// ////////////////////////////////////////////////////////////////////////
// // ICASymbolicFlag
/**
 * Used in interleaving concurrent specific optimization(ICSO) Record the
 * inter-relationship of interleaving concurrent atomicities among functions,
 * awaits critical sections and await statements.
 *
 * @author Guang Yang
 * @version $Id: ICASymbolicFlag.java,v 1.8 2006/10/12 20:33:05 cxh Exp $
 */
public class ICASymbolicFlag {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////
    /**
     * Constructor.
     */
    public ICASymbolicFlag() {
        _dependingFlags = new HashSet();
        _ICA = false;
        _isEvaluated = false;
        _isEvaluating = false;
    }

    /**
     * Constructor.
     *
     * @param value
     *            The value of this flag
     */
    public ICASymbolicFlag(boolean value) {
        _dependingFlags = new HashSet();
        _ICA = value;
        _isEvaluated = true;
        _isEvaluating = false;
    }

    /**
     * Constructor.
     *
     * @param i
     *            The instance that this instance depends on.
     */
    public ICASymbolicFlag(ICASymbolicFlag i) {
        _dependingFlags = new HashSet();
        _ICA = true;
        _isEvaluated = false;
        _isEvaluating = false;
        addDependingFlag(i);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////
    /**
     * Set IC-Atomicity symbolic flag value.
     *
     * @param ica
     *            The vaule to be set
     * @see #getICA
     */
    public void setICA(boolean ica) {
        _ICA = ica;
        _isEvaluated = true;
    }

    /**
     * Get the ICA value.
     *
     * @return Get IC-Atomicity symbolic flag value If the flag is not yet
     *         evaluated, evaluate it.
     * @see #setICA
     */
    public boolean getICA() {
        if (!_isEvaluated)
            evaluate();
        return _ICA;
    }

    /**
     * Get the depending flags.
     *
     * @return Return the set of depending instances.
     */
    public Set getDependingFlags() {
        return _dependingFlags;
    }

    /**
     * Add an IC-Atomicity symbolic flag to the depending instance set of this
     * instance.
     *
     * @param i
     *            IC-Atomicity symbolic flag to be added
     */
    public void addDependingFlag(ICASymbolicFlag i) {
        _dependingFlags.add(i);
    }

    /**
     * Add a set of IC-Atomicity symbolic flags to the depending instance set of
     * this instance.
     *
     * @param i
     *            A set of IC-Atomicity symbolic flags to be added
     */
    public void addDependingFlag(Set i) {
        _dependingFlags.addAll(i);
    }

    /**
     * Return if this flag is IC-atomic.
     *
     * @return Get IC-Atomicity symbolic flag value. Do NOT evaluate the flag
     *         regardless it is already evaluated or not.
     */
    public boolean isICA() {
        return _ICA;
    }

    /**
     * Check if this flag is evaluated.
     *
     * @return Return if this IC-Atomicity symbolic flag has been evaluated or
     *         not.
     */
    public boolean isEvaluated() {
        return _isEvaluated;
    }

    /**
     * Decide whether this instance is IC-Atomic or not based on the depending
     * IC-Atomicity symbolic flag instances.
     */
    public void evaluate() {
        if (_isEvaluating)
            throw new RuntimeException("Among IC-Atomicity symbolic flags, "
                    + "there exist cyclic dependencies.");
        if (_isEvaluated)
            System.err
                    .println("Warning: IC-Atomicity symbolic flag is already evaluated to "
                            + _ICA + ". Re-evaluating!");
        // else if (_dependingFlags.size() == 0)
        // throw new RuntimeException("Leaf IC-Atomicity symbolic flag is not
        // set.");

        _isEvaluating = true;
        _ICA = true;
        Iterator iter = _dependingFlags.iterator();
        while (iter.hasNext()) {
            ICASymbolicFlag i = (ICASymbolicFlag) iter.next();
            if (!i.getICA()) {
                _ICA = false;
                break;
            }
        } // end while
        _isEvaluating = false;
        _isEvaluated = true;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /*
     * The IC atomicity of this instance depends on a set of the IC atomicities
     * of others'. This instance is IC atomic only if all instances in the Set
     * are IC atomic.
     */
    private Set _dependingFlags;

    /*
     * The IC atomicity of this instance after evaluating all depending
     * instances. This instance is IC atomic only if all instances in the Set
     * are IC atomic.
     */
    private boolean _ICA;

    /*
     * Remember whether the IC atomicity of this instance has been evaluated or
     * not.
     */
    private boolean _isEvaluated;

    /*
     * Remember whether the IC atomicity of this instance is being evalutated or
     * not. This flag is used to detect cyclic dependencies.
     */
    private boolean _isEvaluating;

}
