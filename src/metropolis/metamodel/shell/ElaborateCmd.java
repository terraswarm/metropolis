/* Class implementing the elaborate command.

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
import metropolis.metamodel.backends.Backend;
import metropolis.metamodel.backends.elaborator.ElaboratorBackend;
import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import tcl.lang.Interp;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ElaboratorCmd
/**
 * Get compile-time information about the network used in a Meta-Model
 * specification.
 * <p>
 * For further information, see the text array, or start up metashell and type
 * "help elaborate".
 *
 * @author Robert Clariso
 * @version $Id: ElaborateCmd.java,v 1.25 2006/10/12 20:38:36 cxh Exp $
 */
public class ElaborateCmd extends MetropolisCmd implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Build a new ElaborateCmd object. */
    public ElaborateCmd() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Execute a 'elaborate' command. Check that the name of the netlists exists
     * and it is a valid netlist.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param commandArguments
     *            Array of arguments of the Tcl command.
     * @exception TclException
     *                if the number of arguments is incorrect or the
     *                net-initiator provided by the user is somehow incorrect.
     */
    public void cmdProc(Interp interp, TclObject[] commandArguments)
            throws TclException {

        // Check the number of arguments
        if (commandArguments.length != 2) {
            throw new TclNumArgsException(interp, 1, commandArguments,
                    "netinitiator");
        }

        // Create the list of arguments needed by the backend
        LinkedList argList = new LinkedList();
        argList.add(commandArguments[1].toString());

        // Make sure that the libraries have been loaded until pass 2
        /*
         * Iterator iter = MetaModelLibrary.LANG_PACKAGE.getUserTypes(); while
         * (iter.hasNext()) { ObjectDecl decl = (ObjectDecl) iter.next();
         * FileLoader.loadCompileUnit(decl.getName(),
         * MetaModelLibrary.LANG_PACKAGE,2); }
         */

        // Get the list of all sources loaded by the FileLoader
        // All sources should be loaded until pass 2, so that they
        // are translated by the elaboration backend
        List allSources = FileLoader.getCompiledSources(1);
        Iterator asts = allSources.iterator();
        while (asts.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) asts.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            if (pkg == MetaModelLibrary.UTIL_PACKAGE) {
                continue;
            }
            String fileName = (String) ast.getProperty(IDENT_KEY);
            FileLoader.loadCompileUnit(fileName, 2);
        }
        allSources = FileLoader.getCompiledSources(2);

        // Get the paths for java compiler and interpreter, if defined
        TclObject javaInterpreter;
        TclObject javaCompiler;
        try {
            javaInterpreter = interp.getVar("java", TCL.GLOBAL_ONLY);
        } catch (TclException e) {
            javaInterpreter = null;
            interp.resetResult();
        }
        try {
            javaCompiler = interp.getVar("javac", TCL.GLOBAL_ONLY);
        } catch (TclException e) {
            javaCompiler = null;
            interp.resetResult();
        }

        if (javaInterpreter != null) {
            argList.addFirst(javaInterpreter.toString());
            argList.addFirst("-java");
        }
        if (javaCompiler != null) {
            argList.addFirst(javaCompiler.toString());
            argList.addFirst("-javac");
        }

        // Invoke the elaborator backend
        // Catch the possible exception
        try {
            Backend backend = new ElaboratorBackend();
            backend.invoke(argList, allSources);
        } catch (Exception e) {
            throw new TclException(interp, e.getMessage());
        }
        Shell.setElaborated();
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

    /** Help message for the 'elaborate' command. */
    protected static HelpTopic _helpTopic;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // Static initializer: initialize the help topic
    static {
        String name = "elaborate";
        String summary = "Get compile-time information about the network used in a Meta-Model specification";
        String usage[] = { "elaborate toplevelnetlist" };
        String text[] = {
                "Elaboration provides compile-time information about the structure of the",
                "network in a Meta-Model specification. This is done by translating part",
                "of the specification to Java code that is executed to obtain the ",
                "structure of the network.\n",
                "The top-level netlist is specified by providing its fully qualified name.",
                "A class qualifies to be a top-level netlist if and only if:",
                "- it has been previously loaded using the 'metroload' command.",
                "- it is a public and non abstract netlist.",
                "- it has a public constructor with exactly 0 arguments.\n",
                "If a top-level netlist does not satisfy this three properties, a ",
                "compile-time error will occur.\n",
                "Some backends such as simulators might need that the elaborator phase is",
                "performed before being invoked. See the help on the specific backend you",
                "are using to check if it needs elaboration before running.",
                "If Tcl global variables named 'java' or 'javac' exist, then their",
                "values are passed to the backend as values of -java or -javac",
                "arguments." };

        String seeAlso[] = { "load", "simulate" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);
    }
}
