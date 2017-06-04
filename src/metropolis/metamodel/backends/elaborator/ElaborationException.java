/* A back-end that computes the structure of the network of processes and
 media described by a set of meta-model files.

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
package metropolis.metamodel.backends.elaborator;

import java.io.File;

// ////////////////////////////////////////////////////////////////////////
// // ElaboratorException
/**
 * Thrown when there is a problem with elaboration.
 *
 * @author Christopher Brooks
 * @version $Id: ElaborationException.java,v 1.14 2006/10/12 20:32:23 cxh Exp $
 */
public class ElaborationException extends RuntimeException {
    /**
     * Construct an exception with no detail message.
     */
    public ElaborationException() {
        super();
    }

    /**
     * Construct an exception with a detail message, paths and filenames.
     *
     * @param msg
     *            The detail message.
     * @param javacPath
     *            The path to the javac Java compiler.
     * @param javaPath
     *            The path to the java binary.
     * @param classPath
     *            The classpath.
     * @param tmpRoot
     *            The temporary root.
     */
    public ElaborationException(String msg, String javacPath, String javaPath,
            String classPath, File tmpRoot) {
        this(msg, javacPath, javaPath, classPath, tmpRoot, null);
    }

    /**
     * Construct an exception with a detail message, paths and filenames.
     *
     * @param msg
     *            The detail message.
     * @param javacPath
     *            The path to the javac Java compiler.
     * @param javaPath
     *            The path to the java binary.
     * @param classPath
     *            The classpath.
     * @param tmpRoot
     *            The temporary root.
     * @param cause
     *            The cause of the exception.
     */
    public ElaborationException(String msg, String javacPath, String javaPath,
            String classPath, File tmpRoot, Throwable cause) {
        super(msg, cause);
        _localMessage = "Elaboration error\n" + "Using Java compiler:\t"
                + javacPath + "\n" + "Using Java interpreter:\t" + javaPath
                + "\n" + "Using class-path:\t" + classPath + "\n"
                + "Temporary directory:\t"
                + (tmpRoot == null ? "null" : tmpRoot.toString()) + "\n\n";
    }

    /**
     * Returns the detail message of this Exception. If the
     *
     * @return the detail message
     */
    public String getMessage() {
        return _localMessage + super.getMessage();
    }

    // The exception specific message that gets filled in if
    // we call the multi arg constructor.
    private String _localMessage = new String();
}
