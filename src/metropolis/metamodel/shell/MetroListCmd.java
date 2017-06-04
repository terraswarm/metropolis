/* Class processing metrolist commands in the metropolis shell.

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

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // MetroListCmd
/**
 * This class deals with 'metrolist' commands in the metropolis shell. The
 * option describes which kind of information should be listed: the loaded
 * files, the loaded classes, and the loaded packages.
 *
 * @author Robert Clariso
 * @version $Id: MetroListCmd.java,v 1.12 2006/10/12 20:38:39 cxh Exp $
 */
public class MetroListCmd extends MetropolisCmd implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Build a new MetroListCmd object. */
    public MetroListCmd() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * List information about the metamodel classes, packages and files that
     * have been loaded.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the Tcl command.
     * @exception TclException
     *                if the number of arguments is not correct.
     */
    public void cmdProc(Interp interp, TclObject[] objv) throws TclException {

        // Check the number of arguments
        if (objv.length != 2)
            throw new TclNumArgsException(interp, 1, objv, "option");

        // Check that the option is correct
        String option = objv[1].toString();
        int i = 0;
        for (; i < _validOpts.length; i++) {
            if (option.equals(_validOpts[i]))
                break;
        }
        switch (i) {
        case CLASSESOPT:
            _listClasses(interp);
            break;
        case FILESOPT:
            _listFiles(interp);
            break;
        case PKGSOPT:
            _listPkgs(interp);
            break;
        default:
            throw new TclException(interp, "bad option '" + option
                    + "': must be classes, files or pkgs");
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

    /** Identifier of the option 'classes'. */
    protected static final int CLASSESOPT = 0;

    /** Identifier of the option 'files'. */
    protected static final int FILESOPT = 1;

    /** Identifier of the option 'packages'. */
    protected static final int PKGSOPT = 2;

    /** List of valid options of this method. */
    protected static String _validOpts[] = { "classes", "files", "pkgs" };

    /** Help message for the 'list' command. */
    protected static HelpTopic _helpTopic;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * List all the packages currently loaded in the metamodel compiler.
     *
     * @param interp
     *            Tcl command interpreter.
     * @exception TclException
     *                if there is an error while getting the list of classes.
     */
    protected void _listPkgs(Interp interp) throws TclException {
        List allPackages = _findAllPackages();
        Iterator iter = allPackages.iterator();
        String packages = "";
        while (iter.hasNext()) {
            PackageDecl pkgDecl = (PackageDecl) iter.next();
            if (pkgDecl == MetaModelLibrary.SYSTEM_PACKAGE)
                continue;
            if (pkgDecl == MetaModelLibrary.UNNAMED_PACKAGE)
                continue;
            packages = packages + pkgDecl.fullName() + "\n";
        }
        interp.setResult(packages);
    }

    /**
     * List all the classes currently loaded in the metamodel compiler.
     *
     * @param interp
     *            Tcl command interpreter.
     * @exception TclException
     *                if there is an error while getting the list of classes.
     */
    protected void _listClasses(Interp interp) throws TclException {
        List allPackages = _findAllPackages();
        Iterator iter = allPackages.iterator();
        String classes = "";
        while (iter.hasNext()) {
            PackageDecl pkgDecl = (PackageDecl) iter.next();
            Iterator allClasses = pkgDecl.getUserTypes();
            while (allClasses.hasNext()) {
                ObjectDecl decl = (ObjectDecl) allClasses.next();
                classes = classes + _printClass(decl);
            }
        }
        interp.setResult(classes);
    }

    /**
     * List all the files currently loaded in the metamodel compiler.
     *
     * @param interp
     *            Tcl command interpreter.
     * @exception TclException
     *                if there is an error while getting the list of files.
     */
    protected void _listFiles(Interp interp) throws TclException {
        List allSources = FileLoader.getCompiledSources();
        Iterator sources = allSources.iterator();
        String files = "";
        while (sources.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) sources.next();
            String name = (String) ast.getProperty(IDENT_KEY);
            files = files + name + "\n";
        }
        interp.setResult(files);
    }

    /**
     * Get the list of all packages that are loaded in the meta-model compiler.
     * A package is considered loaded if its declaration has been created and
     * the members of the package have all been loaded.
     *
     * @return The list of all packages loaded in the compiler.
     */
    protected List _findAllPackages() {
        LinkedList allPkgs = new LinkedList();
        LinkedList worklist = new LinkedList();
        worklist.add(MetaModelLibrary.SYSTEM_PACKAGE);
        worklist.add(MetaModelLibrary.UNNAMED_PACKAGE);
        do {
            // Get the first package in the worklist
            PackageDecl pkg = (PackageDecl) worklist.getFirst();
            worklist.removeFirst();

            // If the package is loaded, consider this package
            // and load the package members
            if (pkg.membersLoaded()) {
                allPkgs.add(pkg);
                Iterator iter = pkg.getSubPackages();
                while (iter.hasNext()) {
                    worklist.add(iter.next());
                }
            }

        } while (!worklist.isEmpty());
        return allPkgs;
    }

    /**
     * Get a String with the list of names of inner classes defined in this
     * class, beginning with the name of the class itself.
     *
     * @param decl
     *            Declaration of the class.
     * @return A String with the list of names of inner class of this class.
     */
    protected String _printClass(ObjectDecl decl) {
        String allClasses = decl.fullName() + "\n";
        Iterator innerClasses = decl.getInnerClasses();
        while (innerClasses.hasNext()) {
            ObjectDecl inner = (ObjectDecl) innerClasses.next();
            if (inner == decl)
                continue;
            allClasses = allClasses + _printClass(inner);
        }
        return allClasses;
    }

    // Static initializer: initialize the help topic
    static {
        String name = "metrolist";
        String summary = "List the classes/files/packages currently loaded";
        String usage[] = { "metrolist [ classes | files | pkgs ] ?" };
        String text[] = {
                "This command lists the classes, files or packages that are currently",
                "loaded (in memory) in the metamodel compiler. Other classes that are",
                "reachable through the classpath but have not been loaded yet will not",
                "be shown with this command" };
        String seeAlso[] = { "metroload" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);
    }

}
