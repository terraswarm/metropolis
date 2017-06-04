/*
 Copyright (c) 2003-2005 The Regents of the University of California.
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
/*
 * AutoLabel.java
 *
 * Created on April 3, 2003, 11:08 AM
 */

package metropolis.metamodel.backends.systemc;

import java.util.Vector;

/**
 * Generate labels for handling await and label.
 *
 * @author Daniele Gasperini
 * @version $Id: AutoLabel.java,v 1.19 2006/10/12 20:32:55 cxh Exp $ changes:
 *          Yoshi: removed bug return_flag never to true
 */
public class AutoLabel {

    private int _index = 0;

    private int _current = -1;

    private Vector _stack = new Vector();

    private SystemCCodegenVisitor _sysC = null;

    /**
     * Creates a new instance of AutoLabel.
     *
     * @param sysC
     *            The SystemCCodegenVisitor object associated with this
     *            AutoLabel
     */
    public AutoLabel(SystemCCodegenVisitor sysC) {
        _sysC = sysC;
        _doingMMDebug = _sysC._getDoingMMDebug();
    }

    /** Push into the stack a new label. */
    public void push() {
        _current = _index++;
        _stack.add(0, new Integer(_current));
    }

    /** Pop from the stack a label. */
    public void pop() {
        _stack.remove(0);
        if (_stack.size() > 0)
            _current = ((Integer) _stack.get(0)).intValue();
        else
            _current = -1;
    }

    /**
     * Gett the label name in String.
     *
     * @return the label
     */
    public String get() {
        return "label_" + _current;
    }

    /**
     * Get the return statement.
     *
     * @return the return statement
     */
    public String getReturnStatement() {
        if (isOutOfScope()) {
            return _sysC.indent() + "if (return_flag) return RETURN;\n";
        } else {
            return _sysC.indent() + "if (return_flag) goto " + get() + ";\n";
        }
    }

    /**
     * Get the original return statement.
     *
     * @return the original return statement
     */
    public String getReturnStatementOrigin() {
        if (isOutOfScope())
            if (_doingMMDebug) {
                StringBuffer buf = new StringBuffer();
                buf.append(_sysC.indent());
                buf.append("pc->popNextLineContext();\n");
                buf.append(_sysC.indent());
                buf.append("return RETURN;\n");
                return buf.toString();
            } else {
                return _sysC.indent() + "return RETURN;\n";
            }
        else
            return _sysC.indent() + "return_flag = true;  goto " + get()
                    + ";\n";
    }

    /**
     * Get the catch statement.
     *
     * @return the catch statement
     */
    public String getCatchStatement() {
        return _sysC.indent() + "goto skip_" + get() + ";\n" + _sysC.indent()
                + get() + ": { return_flag = true; }\n" + _sysC.indent()
                + "skip_" + get() + ": ;\n";
    }

    /** Push into stack a scope. */
    public void pushScope() {
        _stack.add(0, new Integer(-1));
    }

    /** Pop from stack a scope. */
    public void popScope() {
        pop();
    }

    /**
     * Check if it is out of scope.
     *
     * @return whether it is out of scope
     */
    public boolean isOutOfScope() {
        if (_current == -1)
            return true;
        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // private members

    private boolean _doingMMDebug = false;
}
