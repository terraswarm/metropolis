/* A custom class loader that can find the Java packages generated by the
 elaboration backend. It will be used to load the result of elaboration.

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

import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.PackageDecl;

import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // CustomClassLoader
/**
 * The result of elaboration is a file that contains the elaborated network.
 * This file contains objects whose type has been generated by the elaboration
 * backend, and therefore, they are not available in the class-path. This loader
 * attempts to load the definitions of these objects.
 * <p>
 * The path where the top level generated packages are stored is available to
 * the loader.
 *
 * @author Robert Clariso
 * @version $Id: CustomClassLoader.java,v 1.19 2006/10/12 20:32:21 cxh Exp $
 */
public class CustomClassLoader extends ClassLoader {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new loader that deals with classes generated dynamically during
     * elaboration.
     *
     * @param path
     *            Path where the top-level generated packages are stored.
     */
    public CustomClassLoader(String path) {
        super();
        _classPath = path;
    }

    /**
     * Build a new loader that deals with classes generated dynamically during
     * elaboration.
     *
     * @param parent
     *            Parent ClassLoader, for delegation.
     * @param path
     *            Path where the top-level generated packages are stored.
     * @see java.lang.ClassLoader#ClassLoader(ClassLoader)
     */
    public CustomClassLoader(String path, ClassLoader parent) {
        super(parent);
        _classPath = path;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Path where the top-level packages generated by elaboration are stored.
     */
    protected String _classPath;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Find the specified class inside the Java files generated by the
     * elaboration phase.
     *
     * @param name
     *            Name of the class.
     * @return The Class object for this class.
     * @exception ClassNotFoundException
     *                if the class could not be found.
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        // Find the class file for this class
        String fileName = className(name);
        File file = new File(fileName);
        if (!file.exists())
            throw new ClassNotFoundException(name);

        // Load the file
        byte b[] = null;
        int size = 0;
        try {
            FileInputStream classFile = new FileInputStream(file);
            size = classFile.available();
            b = new byte[size];
            if (classFile.read(b) != size)
                throw new ClassFormatError("Error in file: " + fileName);
        } catch (Exception e) {
            throw new ClassFormatError("Error in file: " + fileName);
        }

        // Declare the class as resolved
        Class c = defineClass(name, b, 0, size);
        if (c == null)
            throw new ClassNotFoundException(name);
        resolveClass(c);
        return c;
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * Build the file name where a class is stored from the fully qualified name
     * of the class.
     *
     * @param name
     *            Fully qualified name of the class.
     * @return The name of the file where the class is stored.
     * @exception ClassNotFoundException
     *                if the name of the class is invalid.
     */
    private String className(String name) throws ClassNotFoundException {
        String fileName = _classPath + File.separator;
        StringTokenizer tokenizer;

        // xichen: handle newly created java interfaces associated with mmm
        // interfaces
        if (name.indexOf("__interface_") >= 0)
            tokenizer = new StringTokenizer(name.substring(0, name
                    .indexOf("__interface_"))
                    + name.substring(name.indexOf("__interface_") + 12), ".");
        else
            tokenizer = new StringTokenizer(name, ".");

        if (!tokenizer.hasMoreTokens())
            throw new ClassNotFoundException(name);
        String token = tokenizer.nextToken();
        fileName = fileName + token;
        if (token.equals("UNNAMED_PACKAGE")) {
            token = tokenizer.nextToken();
            fileName = fileName + File.separator + token;
        }

        // Deal with the system and unnamed packages
        ObjectDecl objDecl = null;
        PackageDecl pkgDecl = MetaModelLibrary.SYSTEM_PACKAGE;
        pkgDecl = pkgDecl.getSubPackage(token);
        if (pkgDecl == null) {
            pkgDecl = MetaModelLibrary.UNNAMED_PACKAGE;
            objDecl = pkgDecl.getUserType(token);
            if (objDecl == null)
                throw new ClassNotFoundException(name);
        }

        // Get the class in the package
        while (tokenizer.hasMoreTokens() && (objDecl == null)) {
            token = tokenizer.nextToken();
            PackageDecl subPkg = pkgDecl.getSubPackage(token);
            if (subPkg != null) {
                pkgDecl = subPkg;
                fileName = fileName + File.separator + token;
            } else {
                objDecl = pkgDecl.getUserType(token);
                if (objDecl == null)
                    throw new ClassNotFoundException(name);
                fileName = fileName + File.separator + token;
            }
        }

        // Get the inner class part
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            objDecl = objDecl.getInnerClass(token);
            if (objDecl == null)
                throw new ClassNotFoundException(name);
            fileName = fileName + "$" + token;
        }

        // xichen: handle newly created java interfaces associated with mmm
        // interfaces
        if (name.indexOf("__interface_") >= 0)
            return fileName.substring(0,
                    fileName.lastIndexOf(File.separator) + 1)
                    + "__interface_"
                    + fileName
                            .substring(fileName.lastIndexOf(File.separator) + 1)
                    + ".class";

        return fileName + ".class";
    }

}
