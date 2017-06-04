/* Class implementing shell commands related to the classpath.

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
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclList;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.lang.TclString;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ClasspathCmd
/**
 *
 * This class implements the commands of the Metropolis shell that are related
 * to the classpath. The classpath is a list of directories where the shell will
 * expect to find the top-level meta-model packages, and the files that do not
 * belong to any meta-model package. It is possible to modify the classpath
 * within the metropolis shell.
 * <p>
 * It is important that this classpath is not confused with the Java classpath
 * that is used to compile the meta-model compiler.
 *
 * @author Robert Clariso
 * @version $Id: ClasspathCmd.java,v 1.21 2006/10/12 20:38:35 cxh Exp $
 */
public class ClasspathCmd extends MetropolisCmd {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Build a new ClasspathCmd object. */
    public ClasspathCmd() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Execute a 'classpath' command. Check the number of arguments and the
     * option used. Check that directories specified exist and are directories.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the Tcl command.
     * @exception TclException
     *                If the number of arguments is incorrect, or the user has
     *                chosen a wrong option.
     */
    public void cmdProc(Interp interp, TclObject[] objv) throws TclException {

        String option = null;

        // Check the number of arguments
        switch (objv.length) {
        case 1:
            throw new TclNumArgsException(interp, 1, objv, "option ?arg ...?");
        default:
            option = objv[1].toString();
        }

        // Perform the classpath command requested by the option
        int optionIdx;
        for (optionIdx = 0; optionIdx < _validOpts.length; optionIdx++)
            if (_validOpts[optionIdx].equals(option))
                break;

        switch (optionIdx) {
        case ADDOPTION:
            _addClassPath(interp, objv);
            break;
        case GETOPTION:
            _getClassPath(interp, objv);
            break;
        case REMOVEOPTION:
            _removeClassPath(interp, objv);
            break;
        case RESETOPTION:
            _resetClassPath(interp, objv);
            break;
        case SHOWOPTION:
            _showClassPath(interp, objv);
            break;

        default:
            throw new TclException(interp, "bad option \"" + option
                    + "\": must be add, get, remove, reset or show.");
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

    // Keep these alphabetical

    /** Index of the 'add' option. */
    protected static final int ADDOPTION = 0;

    /** Index of the 'get' option. */
    protected static final int GETOPTION = 1;

    /** Index of the 'remove' option. */
    protected static final int REMOVEOPTION = 2;

    /** Index of the 'reset' option. */
    protected static final int RESETOPTION = 3;

    /** Index of the 'show' option. */
    protected static final int SHOWOPTION = 4;

    /** Valid options of the 'classpath' command. */
    protected static String _validOpts[] = { "add", "get", "remove", "reset",
            "show" };

    /** Help message for the 'classpath' command. */
    protected static HelpTopic _helpTopic;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Perform a 'classpath add' command. This command adds a list of directory
     * names to the class-path list. Catch the potential exception if the path
     * does not exist or it does not refer to a directory.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Array of arguments of the command.
     * @exception TclException
     *                if no file name is provided.
     */
    protected void _addClassPath(Interp interp, TclObject commandArguments[])
            throws TclException {

        // Check the number of arguments
        if (commandArguments.length == 2) {
            throw new TclNumArgsException(interp, 2, commandArguments,
                    "file ?file ...?");
        }

        // Add the directories to the package iff they don't belong
        // to the directory list
        List classPath = FileLoader.getClassPath();
        StringBuffer result = new StringBuffer();
        for (int i = 2; i < commandArguments.length; i++) {
            String path = commandArguments[i].toString();
            // Get the canonical path for this directory
            File file = new File(path);
            try {
                if (file.exists()) {
                    path = file.getCanonicalPath();
                } else {
                    throw new TclException(interp, "Path '" + path
                            + "' does not exist");
                }
            } catch (IOException e) {
                throw new TclException(interp, "Problem checking '" + path
                        + "' for existance.\n" + e.getMessage());
            }

            if (classPath.contains(path)) {
                result
                        .append("Ignored '" + path
                                + "' - already in classpath\n");
            } else {
                try {
                    FileLoader.addClassPathDir(path);

                    // Check to see if the classpath was loaded
                    List classPaths = FileLoader.getClassPath();
                    result.append("Added '" + path + "' to classpath");
                    if (classPaths.contains(path)) {
                        result.append(".\n");
                    } else {
                        result.append(", but path does not appear in the "
                                + "valued returned by getClassPath()? "
                                + "This can occur when there are problems "
                                + "mapping file names to drive names.");
                    }
                } catch (Exception e) {
                    throw new TclException(interp, result.toString() + "\n"
                            + e.getMessage() + "\n");
                }
            }
        }

        String results = result.toString();
        // Remove the last carry return if any
        if (results.endsWith("\n")) {
            results = results.substring(0, result.length() - 1);
        }

        // Set the result in the interpreter
        interp.setResult(results);
    }

    /**
     * Perform a 'classpath get' command. This command returns a Tcl list that
     * contains the directories in the classpath.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Array of arguments of the command.
     * @exception TclException
     *                if the number of arguments if greater than 2.
     */
    protected void _getClassPath(Interp interp, TclObject commandArguments[])
            throws TclException {

        // Check the number of arguments
        if (commandArguments.length != 2) {
            throw new TclNumArgsException(interp, 2, commandArguments, "");
        }

        // We use a list here so that we can get at the results.
        TclObject list = TclList.newInstance();
        // Show the directories in the classpath.
        List classPath = FileLoader.getClassPath();
        Iterator iter = classPath.iterator();
        while (iter.hasNext()) {
            String path = (String) iter.next();
            TclObject pathElement = TclString.newInstance(path);
            TclList.append(interp, list, pathElement);
        }

        interp.setResult(list);
    }

    /**
     * Perform a 'classpath remove' command. Remove a set of directories from
     * the classpath. Ignore the directories that are not in the classpath.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Array of arguments of the command.
     * @exception TclException
     *                if the no file name was provided.
     */
    protected void _removeClassPath(Interp interp, TclObject commandArguments[])
            throws TclException {
        // Check the number of arguments
        if (commandArguments.length == 2) {
            throw new TclNumArgsException(interp, 2, commandArguments,
                    "file ?file ...?");
        }

        // Remove the directories from the classpath
        // Ignore the directories that don't belong to the classpath
        List classPath = FileLoader.getClassPath();
        String result = "";
        for (int i = 2; i < commandArguments.length; i++) {
            String path = commandArguments[i].toString();
            if (!classPath.contains(path)) {
                result = result + "Ignored '" + path + "' - not in classpath\n";
            } else {
                classPath.remove(path);
                result = result + "Removing '" + path + "' from classpath\n";
            }
        }

        // Remove the last carry return if any
        if (result.endsWith("\n")) {
            result = result.substring(0, result.length() - 1);
        }

        // Set the result in the interpreter
        interp.setResult(result);
    }

    /**
     * Perform a 'classpath reset' command. Reset the contents of the classpath
     * variable.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Array of arguments of the command.
     * @exception TclException
     *                if the number of arguments if greater than 2.
     */
    protected void _resetClassPath(Interp interp, TclObject commandArguments[])
            throws TclException {

        // Check the number of arguments
        if (commandArguments.length > 2) {
            throw new TclNumArgsException(interp, 2, commandArguments, "");
        }

        // Reset the classpath
        FileLoader.setClassPath(new LinkedList());
    }

    /**
     * Perform a 'classpath show' command. This command returns as a result a
     * String with the list of directories in the classpath.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Array of arguments of the command.
     * @exception TclException
     *                if the number of arguments if greater than 2.
     */
    protected void _showClassPath(Interp interp, TclObject commandArguments[])
            throws TclException {

        // Check the number of arguments
        if (commandArguments.length != 2) {
            throw new TclNumArgsException(interp, 2, commandArguments, "");
        }

        // Show the directories in the class-path
        List classPath = FileLoader.getClassPath();
        String result = "   " + classPath.size() + " dirs in classpath";
        Iterator iter = classPath.iterator();
        while (iter.hasNext()) {
            String path = (String) iter.next();
            result = result + "\n   " + path;
        }

        // Set this text as the result
        interp.setResult(result);
    }

    // Static initializer: initialize the help topic
    static {
        String name = "classpath";
        String summary = "Display or change the classpath variable.";
        String usage[] = { "classpath [ get | show | reset ]",
                "classpath [ add | remove ] dir ?dir ...?" };
        String text[] = {
                "The meta-model compiler keeps a list of directories where the top-level",
                "meta-model packages (including system libraries) can be found. This list,",
                "called 'classpath' is used whenever the compiler is looking for a",
                "top-level package.\n",
                "The classpath is initialized with the environment variable",
                "'METRO_CLASSPATH'. This command allows us to view and change the",
                "list of directories in the classpath. The options are:",
                "- add: Add a new directory to the classpath list.",
                "- get: Get the classpaths as a Tcl list.",
                "- remove: Remove a directory from the classpath list",
                "- reset: Clear the contents of the classpath. This command should ",
                "  be handled with care",
                "- show: Display the directories in the classpath list",
                "If the user specifies a path that does not exist, is not a directory or",
                "a directory that is (add) or is not (remove) in the classpath, that",
                "directory is ignored" };
        String seeAlso[] = { "load" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);
    }

}
