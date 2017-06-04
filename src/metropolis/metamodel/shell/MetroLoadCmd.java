/* Command processing metroload commands in the metropolis shell.

 Metropolis: Design Environment for Heterogeneus Systems.

 @Copyright (c) 1998-2005 The Regents of the University of California.
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

package metropolis.metamodel.shell;

import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.PackageDecl;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // MetroLoadCmd
/**
 * This class implements the 'metroload' commands from the Metropolis shell. The
 * set of 'metroload' commands allow the user to load a set of Metropolis files.
 *
 * @author Robert Clariso
 * @version $Id: MetroLoadCmd.java,v 1.14 2006/10/12 20:38:40 cxh Exp $
 */
public class MetroLoadCmd extends MetropolisCmd {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Build a new MetroLoadCmd. No initialization is needed. */
    public MetroLoadCmd() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Execute the 'metroload' command in one of its options. Check that the
     * number of parameters is correct according to its options.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Parameters of the tcl command.
     * @exception TclException
     *                if the number of arguments is not valid or if there is
     *                something wrong with the message.
     */
    public void cmdProc(Interp interp, TclObject commandArguments[])
            throws TclException {

        // Check the number of arguments
        if (commandArguments.length < 4) {
            throw new TclNumArgsException(interp, 1, commandArguments,
                    "option compflag name ?name ...?");
        }

        // Check that the option for loading is correct
        String option = commandArguments[1].toString();
        int optId = 0;
        for (; optId < _validOpts.length; optId++)
            if (option.equals(_validOpts[optId])) {
                break;
            }
        if (optId == _validOpts.length) {
            throw new TclException(interp, "bad option '" + option
                    + "': must be class, file or pkg");
        }

        // Check that the flag for loading is correct
        String flag = commandArguments[2].toString();
        int flagId = 0;
        for (; flagId < _validFlags.length; flagId++)
            if (flag.equals(_validFlags[flagId])) {
                break;
            }
        if (flagId == _validFlags.length) {
            throw new TclException(interp, "bad flag '" + flag
                    + "': must be -classes," + " -expressions or -semantics");
        }

        // Load the files/packages/classes
        for (int i = 3; i < commandArguments.length; i++) {
            String name = commandArguments[i].toString();
            switch (optId) {
            case LOADFILE:
                _loadFile(interp, name, flagId);
                break;
            case LOADPKG:
                _loadPackage(interp, name, flagId);
                Shell.setNotElaborated();
                break;
            case LOADCLASS:
                _loadClass(interp, name, flagId);
                break;
            default:
                break;
            }
        }
    }

    /**
     * Return a help topic for this command.
     *
     * @return A help topic for this command.
     */
    public HelpTopic getHelpTopic() {
        return _helpTopic;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Constant that identifies option = 'file'. */
    protected static final int LOADFILE = 0;

    /** Constant that identifies option = 'pkg'. */
    protected static final int LOADPKG = 1;

    /** Constant that identifies option = 'class'. */
    protected static final int LOADCLASS = 2;

    /** Valid options for the 'metroload' command. */
    protected static String _validOpts[] = { "file", "pkg", "class" };

    /**
     * Compilation flag for checking class declarations and inheritance. No name
     * checking inside expressions is performed.
     */
    protected static final int CLASSESFLAG = 0;

    /**
     * Compilation flag for checking names and fields inside expressions. After
     * this pass, all variables and fields point to their correct Decl object.
     */
    protected static final int EXPRESSIONSFLAG = 1;

    /**
     * Compilation flag for checking meta-model semantics in the file. Perform
     * type checking on input files, for instance.
     */
    protected static final int SEMANTICSFLAG = 2;

    /** Valid compilation flags for the 'metroload' command. */
    protected static String _validFlags[] = { "-classes", "-expressions",
            "-semantics" };

    /** Help message for this command. */
    protected static HelpTopic _helpTopic;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Load a file specified by the user, up to a given semantic pass. Catch the
     * possible I/O exception or semantic exception and notify it to the user.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param name
     *            Name of the file.
     * @param flag
     *            Flag that describes the semantic passes that should be used to
     *            compile this file.
     * @exception TclException
     *                if the file does not exist, or a compilation error occurs.
     */
    protected void _loadFile(Interp interp, String name, int flag)
            throws TclException {

        try {
            int pass = _getPass(flag);
            FileLoader.loadCompileUnit(name, pass);
        } catch (Exception e) {
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Load all the files in a given package, up to a given semantic pass. Catch
     * the possible I/0 exception or semantic exception and notify it to the
     * user.
     *
     * @param interp
     *            Tcl command interpreter
     * @param name
     *            Fully qualified name of the package.
     * @param flag
     *            Flat that describes the semantic passes that should be used to
     *            compile this file.
     * @exception TclException
     *                if the package does not exist or a compilation error
     *                occurs.
     */
    protected void _loadPackage(Interp interp, String name, int flag)
            throws TclException {

        // Divide the qualified name in a list of Strings, e.g.
        // 'pkg1.pkg2.pkg3' becomes 'pkg1', 'pkg2', 'pkg3'.
        LinkedList pkgList = _splitName(name);

        // Get the declaration of the package
        PackageDecl pkg = _locatePackage(interp, pkgList);

        // The package has been located, the PackageDecl is stored in 'pkg'
        // Load the members of the package up to the given semantic pass
        try {
            int pass = _getPass(flag);
            FileLoader.loadPackageMembers(pkg, pass);
        } catch (Exception e) {
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Load a given class with a given name up to a given level of semantic
     * analysis.
     *
     * @param interp
     *            TclCommand interpreter.
     * @param name
     *            Fully qualified name of the class.
     * @param flag
     *            Flag that describes the semantic passes that should be used to
     *            compile this file.
     * @exception TclException
     *                if there is a problem loading the class.
     */
    protected void _loadClass(Interp interp, String name, int flag)
            throws TclException {

        // Divide the qualified name in a list of Strings, e.g.
        // 'pkg1.pkg2.pkg3' becomes 'pkg1', 'pkg2', 'pkg3'.
        LinkedList pkgList = _splitName(name);

        // Get the name of the class
        String className = (String) pkgList.getLast();
        pkgList.removeLast();

        // Get the declaration of the package
        PackageDecl pkg = _locatePackage(interp, pkgList);

        // Compile the file up to the semantic level specified by the flag
        try {
            int pass = _getPass(flag);
            FileLoader.loadCompileUnit(className, pkg, pass);
        } catch (Exception e) {
            throw new TclException(interp, e.toString());
        }
    }

    /**
     * Get the last semantic pass that should be applied for a given compilation
     * flag.
     *
     * @param flag
     *            Flag passed to the load command.
     * @return The number of the last semantic pass that should be applied for a
     *         given compilation flag.
     */
    protected int _getPass(int flag) {
        switch (flag) {
        case CLASSESFLAG:
            return 1;
        case EXPRESSIONSFLAG:
            return 2;
        case SEMANTICSFLAG:
            return 4;
        default:
            throw new RuntimeException("Internal error");
        }
    }

    /**
     * Split a qualified names into a list of simple, non-qualified names, where
     * the '.' separating the characters have been removed.
     *
     * @param name
     *            A qualified name.
     * @return A linked list where each element is a simple name, in the same
     *         order as they appear in the qualified name.
     */
    protected LinkedList _splitName(String name) {
        LinkedList simpleNames = new LinkedList();
        StringTokenizer tok = new StringTokenizer(name, ".");
        while (tok.hasMoreElements()) {
            String aName = tok.nextToken();
            simpleNames.add(aName);
        }
        return simpleNames;
    }

    /**
     * Locate a package with a given qualified name.
     *
     * @param interp
     *            Tcl Command interpreter.
     * @param pkgList
     *            List of simple names of the package.
     * @return The package declaration of this package.
     * @exception TclException
     *                if the package does not exist.
     */
    protected PackageDecl _locatePackage(Interp interp, LinkedList pkgList)
            throws TclException {

        // Find the declaration for this package
        PackageDecl pkg = null;
        Iterator iter = pkgList.iterator();
        while (iter.hasNext()) {
            String pkgName = (String) iter.next();
            if (pkg == null) {
                pkg = FileLoader.loadTopLevelPackage(pkgName);
                if (pkg == null)
                    throw new TclException(interp, "top level package '"
                            + pkgName + "' not found. Check that the "
                            + "package name and classpath are correct");
            } else {
                PackageDecl oldPkg = pkg;
                pkg = FileLoader.loadPackage(pkgName, pkg);
                if (pkg == null) {
                    throw new TclException(interp, "subpackage '" + pkgName
                            + "' of package '" + oldPkg.fullName()
                            + "' not found. Check the package name");
                }
            }
        }

        // Sanity check to avoid errors like package "."
        if (pkg == null)
            throw new TclException(interp, "wrong package name '" + pkg + "'");
        return pkg;
    }

    // Static initializer: initialize the help topic
    static {
        String name = "metroload";
        String summary = "Parse and load a Meta-Model specification.";
        String usage[] = {
                "metroload [ file | class | pkg ] [ -classes | -expressions | -semantics ]",
                "       name ?name ...?" };
        String text[] = {
                "The metroload command provides the mechanism to load meta-model specifications.",
                "Users can specify: which specification will be loaded and which will be ",
                "the level of semantic analysis used to analyze this command.",
                "The specification can be selected using three formats:",
                "- file + flag + filename: Loads the specified file. For example,",
                "      metroload file -classes metro/myClass.mmm",
                "- class + flag + classname: Loads a given class inside a package, given",
                "the fully qualified name of the class. For example,",
                "      metroload class -expressions metro.lib.Writer",
                "- pkg + flag + packagename: Loads all classes inside a package, given the",
                "fully qualified name of the package. For example,",
                "      metroload pkg -expressions metro.lib",
                "The level of semantic analysis can be specified using 3 flags:",
                "- classes: Checks class declarations and inheritance.",
                "- expressions: In addition to checking everything checked in 'classes',",
                "checks variable names inside expressions. This also includes checking ",
                "field accesses and method calls. After this pass every name in the ASTs",
                "is linked to the proper declaration.",
                "- semantics: In addition to everything checked in 'expressions',performs",
                "type checking and performs meta-model specific checks." };
        String seeAlso[] = { "classpath" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);
    }

}
