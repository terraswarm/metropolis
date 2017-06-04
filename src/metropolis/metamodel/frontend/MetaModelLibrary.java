/* A class encapsulating special classes and packages in the meta-model
 language.

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

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.nodetypes.TypeNameNode;

// ////////////////////////////////////////////////////////////////////////
// // MetaModelLibrary
/**
 * A class that encapsulates special packages and classes in the meta-model
 * language. This class loads classes from the meta-model language and offer
 * mechanisms to access this classes.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: MetaModelLibrary.java,v 1.25 2006/10/12 20:33:53 cxh Exp $
 */
public abstract class MetaModelLibrary implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // Special package declarations

    /**
     * System package. This package is the root of the package hierarchy, i.e.
     * it is the parent of all top level packages.
     */
    public static PackageDecl SYSTEM_PACKAGE = null;

    /**
     * Unnamed package. This package is the parent of all classes that are not
     * declared inside any package.
     */
    public static PackageDecl UNNAMED_PACKAGE;

    /**
     * Language package. Package that contains all language classes in the
     * meta-model. This package is imported by default by any file.
     */
    public static PackageDecl LANG_PACKAGE;

    /**
     * Utility package. Package that describes containers and useful data
     * structures. This Package is imported by default by any file.
     */
    public static PackageDecl UTIL_PACKAGE;

    // Special class declarations

    /** Class 'Object', the superclass of all classes in the meta-model. */
    public static ClassDecl OBJECT_DECL;

    /** Class 'Quantity', the superclass of all quantities in the meta-model. */
    public static QuantityDecl QUANTITY_DECL;

    /** Interface 'Interface', the superinterface of all interfaces. */
    public static InterfaceDecl INTERFACE_DECL;

    /** Interface 'Port', the superinterface of all port interfaces. */
    public static InterfaceDecl PORT_DECL;

    /** Class 'Node', the superclass of all nodes in a network. */
    public static ClassDecl NODE_DECL;

    /** Process 'Process', the superclass of all processes. */
    public static ProcessDecl PROCESS_DECL;

    /** Scheduler 'Scheduler', the superclass of all schedulers. */
    public static SchedulerDecl SCHEDULER_DECL;

    /**
     * Communication medium 'Medium', the superclass of all communication media.
     */
    public static MediumDecl MEDIUM_DECL;

    /** State medium 'StateMedium', the superclass of all state medium. */
    public static StateMediumDecl STATEMEDIUM_DECL;

    /** Netlist 'Netlist', the superclass of all netlists. */
    public static NetlistDecl NETLIST_DECL;

    /** Class 'String'. */
    public static ClassDecl STRING_DECL;

    /** Class 'Array'. */
    public static ClassDecl ARRAY_CLASS_DECL;

    /** Field 'length' of class Array. */
    public static FieldDecl ARRAY_LENGTH_DECL;

    /** Method 'clone()' of class Array. */
    public static MethodDecl ARRAY_CLONE_DECL;

    // Special types

    /** Type of class 'Object'. */
    public static TypeNameNode OBJECT_TYPE;

    /** Type of class 'Interface'. */
    public static TypeNameNode INTERFACE_TYPE;

    /** Type of class 'Port'. */
    public static TypeNameNode PORT_TYPE;

    /** Type of class 'Netlist'. */
    public static TypeNameNode NETLIST_TYPE;

    /** Type of class 'Process'. */
    public static TypeNameNode PROCESS_TYPE;

    /** Type of class 'StateMedium'. */
    public static TypeNameNode STATEMEDIUM_TYPE;

    /** Type of class 'Medium'. */
    public static TypeNameNode MEDIUM_TYPE;

    /** Type of class 'Scheduler'. */
    public static TypeNameNode SCHEDULER_TYPE;

    /** Type of class 'String'. */
    public static TypeNameNode STRING_TYPE;

    /** Type of class 'Quantity'. */
    public static TypeNameNode QUANTITY_TYPE;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /** Initialize the classes and packages of the meta-model library. */
    public static void initLibraries() {
        if (SYSTEM_PACKAGE != null) {
            // FIXME: the cache prevents us from calling initLibraries()
            // twice. This means that changing the metroshell classpath after
            // startup probably does not do much.
            throw new RepeatInitLibrariesException();
        }
        try {
            _loadLibraryPackages();
            _loadLibraryClasses();
            _createSpecialTypes();
        } catch (RuntimeException ex) {
            String classpathString = "Unknown";
            try {
                classpathString = FileLoader.classPathToString(", ");
            } catch (Exception ex2) {
                System.err.println("Warning, problem getting classpath?");
                ex2.printStackTrace();
            }
            throw new RuntimeException("Failed to initialize libraries. "
                    + "metropolis classpath was:\n" + classpathString, ex);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Load the special packages in the meta-model language. This method should
     * be called once to initialize the class.
     */
    protected static void _loadLibraryPackages() {
        // Create the system package
        Scope scope1 = new Scope();
        SYSTEM_PACKAGE = new PackageDecl("_SYSTEM_", null);
        SYSTEM_PACKAGE.setScope(scope1);

        // Create the unnamed package
        Scope scope2 = new Scope();
        UNNAMED_PACKAGE = new PackageDecl("_UNNAMED_", SYSTEM_PACKAGE);
        UNNAMED_PACKAGE.setScope(scope2);

        PackageDecl meta = FileLoader.loadTopLevelPackage("metamodel");

        if (meta == null)
            throw new RuntimeException("Cannot find package 'metamodel'"
                    + " in the class-path.");

        LANG_PACKAGE = FileLoader.loadPackage("lang", meta);
        if (LANG_PACKAGE == null)
            throw new RuntimeException("Cannot find package "
                    + "'metamodel.lang'" + " in the class-path");
        UTIL_PACKAGE = FileLoader.loadPackage("util", meta);
        if (UTIL_PACKAGE == null)
            throw new RuntimeException("Cannot find package "
                    + "'metamodel.util'" + " in the class-path");
    }

    /**
     * Load the special classes in the meta-model library. This method should be
     * called once to initialize the class.
     */
    protected static void _loadLibraryClasses() {
        FileLoader.loadPackageMembers(UTIL_PACKAGE);
        FileLoader.loadPackageMembers(LANG_PACKAGE);

        // Initialize important classes
        OBJECT_DECL = LANG_PACKAGE.getClass("Object");
        STRING_DECL = LANG_PACKAGE.getClass("String");
        ARRAY_CLASS_DECL = LANG_PACKAGE.getClass("Array");
        INTERFACE_DECL = LANG_PACKAGE.getInterface("Interface");
        PORT_DECL = LANG_PACKAGE.getInterface("Port");
        MEDIUM_DECL = LANG_PACKAGE.getMedium("Medium");
        NETLIST_DECL = LANG_PACKAGE.getNetlist("Netlist");
        PROCESS_DECL = LANG_PACKAGE.getProcess("Process");
        SCHEDULER_DECL = LANG_PACKAGE.getScheduler("Scheduler");
        STATEMEDIUM_DECL = LANG_PACKAGE.getStateMedium("StateMedium");
        QUANTITY_DECL = LANG_PACKAGE.getQuantity("Quantity");

        // Load important members from class 'Array'.
        if (ARRAY_CLASS_DECL == null) {
            throw new RuntimeException("Class 'Array' declaration is null, "
                    + "perhaps $METRO/lib/metamodel/lang/Array.class "
                    + "is missing or not in the classpath?");
        }
        ARRAY_LENGTH_DECL = ARRAY_CLASS_DECL.getField("length");
        if (ARRAY_LENGTH_DECL == null) {
            throw new RuntimeException("Class 'Array' does not have"
                    + " a field 'length'.");
        }
        ScopeIterator methodIter = ARRAY_CLASS_DECL.getMethods("clone");
        if (!methodIter.hasNext()) {
            throw new RuntimeException("Class 'Array' does not have"
                    + "a method 'clone()'");
        }
        ARRAY_CLONE_DECL = (MethodDecl) methodIter.next();
    }

    /**
     * Create the special types in the meta-model language. This method should
     * be called once to initialize the class.
     */
    protected static void _createSpecialTypes() {
        OBJECT_TYPE = (TypeNameNode) OBJECT_DECL.getDefType();
        INTERFACE_TYPE = (TypeNameNode) INTERFACE_DECL.getDefType();
        PORT_TYPE = (TypeNameNode) PORT_DECL.getDefType();
        NETLIST_TYPE = (TypeNameNode) NETLIST_DECL.getDefType();
        PROCESS_TYPE = (TypeNameNode) PROCESS_DECL.getDefType();
        STATEMEDIUM_TYPE = (TypeNameNode) STATEMEDIUM_DECL.getDefType();
        MEDIUM_TYPE = (TypeNameNode) MEDIUM_DECL.getDefType();
        SCHEDULER_TYPE = (TypeNameNode) SCHEDULER_DECL.getDefType();
        STRING_TYPE = (TypeNameNode) STRING_DECL.getDefType();
        QUANTITY_TYPE = (TypeNameNode) QUANTITY_DECL.getDefType();
    }

}
