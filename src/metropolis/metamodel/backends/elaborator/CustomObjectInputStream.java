/* A subclass of ObjectInputStream that uses the elaboration ClassLoader.

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

package metropolis.metamodel.backends.elaborator;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

// ////////////////////////////////////////////////////////////////////////
// // CustomObjectInputStream
/**
 * A subclass of ObjectInputStream that is used to restore the result of
 * elaboration from disk. The point is that it used the elaboration custom
 * ClassLoader instead of the system ClassLoader.
 *
 * @author Robert Clariso
 * @version $Id: CustomObjectInputStream.java,v 1.14 2006/10/12 20:32:22 cxh Exp $
 */
public class CustomObjectInputStream extends ObjectInputStream {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new CustomObjectInputStream from an InputStream.
     *
     * @param in
     *            Stream from where the objects are read.
     * @param loader
     *            Custom class loader to be used instead of the system
     *            ClassLoader.
     * @exception IOException
     *                if an input/ouput error occurs.
     */
    public CustomObjectInputStream(InputStream in, CustomClassLoader loader)
            throws IOException {
        super(in);
        _loader = loader;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Custom class loader used to resolve classes instead of the system class
     * loader. The system class loader is called using the delegation
     * implemented for ClassLoaders.
     */
    protected CustomClassLoader _loader;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Load the local class equivalent of the specified stream class
     * description. Use the custom class loader to get this information, calling
     * the system ClassLoader for delegation.
     *
     * @param v
     *            The object description in the input stream.
     * @return The resolved class.
     * @exception IOException
     *                if there is an input/output error.
     * @exception ClassNotFoundException
     *                if a class of the serialized object cannot be found.
     */
    protected Class resolveClass(ObjectStreamClass v) throws IOException,
            ClassNotFoundException {
        String name = v.getName();

        // Under Java 1.5.0, we need to call Class.forName() instead
        // of loadClass() or else we get a java.io.StreamCorruptedException
        // while loading the Network from the NET file.
        // See http://forum.java.sun.com/thread.jsp?forum=4&thread=468339

        // return _loader.loadClass(name);

        return Class.forName(name, true, _loader);
    }

}
