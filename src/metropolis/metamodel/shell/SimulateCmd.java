/* Class processing simulate commands in the metropolis shell.

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

import metropolis.metamodel.backends.Backend;
import metropolis.metamodel.backends.promela.PromelaBackend;
import metropolis.metamodel.backends.systemc.SystemCBackend;
import metropolis.metamodel.frontend.FileLoader;
import tcl.lang.Interp;
import tcl.lang.TCL;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // SimulateCmd
/**
 * This class deals with 'simulate' commands in the metropolis shell. The option
 * describes which is the target simulation to be used (java simulation, systemc
 * simulation, etc.). The rest of options are passed to the simulator backend
 * selected by the option used.
 *
 * @author Robert Clariso
 * @version $Id: SimulateCmd.java,v 1.30 2006/10/12 20:38:44 cxh Exp $
 */
public class SimulateCmd extends MetropolisCmd {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Build a new SimulateCmd object. */
    public SimulateCmd() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Perform simulation on a meta-model specification. The meta-model
     * specification should be completely loaded before invoking this command.
     * The first argument (option) is used to distinguish between the several
     * simulation backends. The rest of arguments is passed to the appropriate
     * simulation backend.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the Tcl command.
     * @exception TclException
     *                if the number of arguments is not correct or there is an
     *                error in the simulation.
     */
    public void cmdProc(Interp interp, TclObject[] objv) throws TclException {

        // Check the number of arguments
        if (objv.length < 2) {
            throw new TclNumArgsException(interp, 1, objv, "option ?simflags?");
        }

        // Check that the option is correct
        String option = objv[1].toString();
        int optId = 0;
        for (; optId < _validOpts.length; optId++)
            if (_validOpts[optId].equals(option))
                break;

        switch (optId) {
        case JAVAOPT:
            _invokeJavaSimulator(interp, objv);
            break;
        case PROMELAOPT:
            _invokePromelaSimulator(interp, objv);
            break;
        case SYSTEMCOPT:
            _invokeSystemCSimulator(interp, objv);
            break;
        default:
            throw new TclException(interp, "bad option '" + option
                    + "': must be java, promela or systemc");
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

    /** Identifier of Java simulation. */
    protected static final int JAVAOPT = 0;

    /** Identifier of Promela simulation. */
    protected static final int PROMELAOPT = 1;

    /** Identifier of SystemC simulation. */
    protected static final int SYSTEMCOPT = 2;

    /** List of valid options of this method. */
    protected static String _validOpts[] = { "java", "promela", "systemc" };

    /** Help message for the 'simulate' command. */
    protected static HelpTopic _helpTopic;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Invoke the Java simulator with the arguments from the command.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the tcl command.
     * @exception TclException
     *                if there is an error during java simulation.
     */
    protected void _invokeJavaSimulator(Interp interp, TclObject[] objv)
            throws TclException {

        // Create the linked list of arguments needed by the backend
        LinkedList argList = _argumentList(objv, 2, objv.length - 1);

        // Get the list of all source files loaded by the FileLoader
        List allSources = FileLoader.getCompiledSources(2);

        // metropolis.metamodel.backends.simulator.SimulatorBackend
        // is not shipped, so we use reflection here so the code
        // can compile without it.
        Backend backend = _instantiateBackend(interp,
                "metropolis.metamodel.backends.simulator.SimulatorBackend");

        try {
            backend.invoke(argList, allSources);
        } catch (Exception e) {
            // FIXME: TclException should take a cause argument.
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Invoke the Promela backend with the arguments from the command.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the tcl command.
     * @exception TclException
     *                if there is an error during Promela simulation.
     */
    protected void _invokePromelaSimulator(Interp interp, TclObject[] objv)
            throws TclException {

        // Create the linked list of arguments needed by the backend
        LinkedList argList = _argumentList(objv, 2, objv.length - 1);

        // Get the list of all source files loaded by the FileLoader
        List allSources = FileLoader.getCompiledSources(2);

        // Invoke the Promela simulation backend
        // Catch the possible exception
        try {
            Backend backend = new PromelaBackend();
            backend.invoke(argList, allSources);
        } catch (Exception e) {
            System.out.println("Message - " + e.getMessage());
            // FIXME: TclException should take a cause argument.
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Invoke the System C simulator with the arguments from the command.
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Arguments of the tcl command.
     * @exception TclException
     *                if there is an error during System C simulation.
     */
    protected void _invokeSystemCSimulator(Interp interp, TclObject[] objv)
            throws TclException {

        int argLength = objv.length;

        // Create the linked list of arguments needed by the backend
        LinkedList argList = _argumentList(objv, 2, objv.length - 1);
        Iterator itr = argList.iterator();
        while (itr.hasNext()) {
            String arg = (String) itr.next();
            if (arg.equals("regenerate")) {
                itr.remove();
                argList.addFirst("-w");
                argLength--;
                break;
            }
        }

        if (!Shell.isElaborated() && argLength < 3) {
            throw new TclException(interp,
                    "Since SystemC code generation depends on "
                            + "the elaboration result, \n"
                            + "please do elaboration first or give the "
                            + "top-level netlist name \n"
                            + "after 'simulate systemc'.");
        }

        // Get the list of all source files loaded by the FileLoader
        List allSources = FileLoader.getCompiledSources(2);

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

        // Invoke the System C simulation backend
        // Catch the possible exception
        try {
            Backend backend = new SystemCBackend();
            if (Shell.isElaborated())
                argList.addFirst(new String("-elaborated"));
            backend.invoke(argList, allSources);
        } catch (Exception e) {
            System.out.println("Message - " + e.getMessage());
            // FIXME: TclException should take a cause argument.
            e.printStackTrace();
            throw new TclException(interp, e.getMessage());
        }
    }

    /**
     * Create a list with a part of the arguments of the tcl command.
     *
     * @param objv
     *            Arguments of the tcl command.
     * @param first
     *            First argument of the tcl to copy into the list.
     * @param last
     *            Last argument of the tcl to copy into the list.
     * @return A list with the arguments of the tcl command in the given range,
     *         stored in String format. If the range is empty, return the empty
     *         list.
     */
    protected LinkedList _argumentList(TclObject[] objv, int first, int last) {
        LinkedList args = new LinkedList();
        for (int i = first; (i <= last) && (i < objv.length); i++)
            args.add(objv[i].toString());
        return args;
    }

    // Static initializer: initialize the help topic
    static {
        String name = "simulate";
        String summary = "Generate simulateable code for a specification.";
        String usage[] = { "simulate [ java | promela | systemc ] [top-level-netlist] [regenerate]" };
        String text[] = {
                "This command invokes a simulation backend on the loaded specification",
                "(must be loaded previously using the metroload command). The arguments after",
                "selecting the simulator are passed directly to the appropriate simulator",
                "backend.\n",
                " The simulator backends currently available are:",
                "- java : Java based simulation.",
                "- promela: Spin/Promela simulation and verification.",
                "- systemc: System C based simulation.",
                "top-level-netlist is the same as in command 'elaborate'",
                "'regenerate' switch forces regenerate all the code, which is necessary after changing mapping information in synch." };
        String seeAlso[] = { "metroload" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    // Instantiate a backend using reflection.
    private Backend _instantiateBackend(Interp interp, String classname)
            throws TclException {
        Backend backend;
        try {
            Class simulatorBackendClass = Class.forName(classname);
            backend = (Backend) simulatorBackendClass.newInstance();
        } catch (Throwable throwable) {
            // FIXME: TclException should take a cause argument.
            throwable.printStackTrace();
            throw new TclException(interp, "Failed to instantiate '"
                    + classname + "'. "
                    + "Perhaps that package is not currently in " + "your tree");
        }
        return backend;
    }
}
