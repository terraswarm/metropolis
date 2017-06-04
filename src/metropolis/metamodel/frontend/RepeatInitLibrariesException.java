/* An exception to be thrown when MetaModelLibrary.initLibraries is
 invoked more than once.

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

// ////////////////////////////////////////////////////////////////////////
// // RepeatInitLibrariesException
/**
 * A <code>RuntimeException</code> to be thrown when
 * <code>MetaModelLibrary.initLibraries()</code> is invoked more than once,
 * which is illegal due to a bug with the cache.
 *
 * @author Allen Hopkins
 * @version $Id: RepeatInitLibrariesException.java,v 1.1 2005/10/03 22:57:20
 *          allenh Exp $
 */
public class RepeatInitLibrariesException extends RuntimeException {

    /** Initialize the classes and packages of the meta-model library. */
    public RepeatInitLibrariesException() {
        super("Internal error: Currently, you " + "may only invoke "
                + "MetaModelLibrary.initLibraries() "
                + "once per invocation.  There is a "
                + "bug with the cache such that when "
                + "this method is invoked twice, "
                + "classes in the lang package are " + "not found.");
    }
}
