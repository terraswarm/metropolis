/* A shell for the Metropolis environment.

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
import metropolis.metamodel.frontend.MetaModelLibrary;
import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // Shell
/**
 * A shell for the Metropolis environment, based on Tcl for Java (Jacl). This
 * shell provides commands to interactively load meta-model files and invoke
 * back-end tools without leaving the shell. The shell also provides help
 * procedures to get information on the different commands of the system.
 * <p>
 * This shell defines an alternate "main" procedure (alternate to the one
 * defined in "Compiler" for command-line invocation). This mean that the shell
 * process commands from its standard input until an 'exit' or 'quit' command is
 * issued.
 * <p>
 * The commands can also be provided from other sources.
 *
 * @author Robert Clariso
 * @version $Id: Shell.java,v 1.28 2006/10/12 20:38:43 cxh Exp $
 */
public class Shell {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Main procedure of the shell. The main procedure can take as arguments a
     * list of files to be executed before entering interactive execution. Using
     * the flag '-ni' causes the shell to exit after processing these files.
     *
     * @param args
     *            Command-line arguments of the shell.
     */
    public static void main(String[] args) {
        // Register metropolis commands in the Jacl interpreter
        _registerMetropolisCommands();

        // Load classpaths from the command-line arguments
        _processArgs(args);

        // Initialize the meta-model frontend
        FileLoader.setClassPath(_classpath);
        MetaModelLibrary.initLibraries();

        // Load user configuration file '~.metroshrc'
        String homeDir = System.getProperty("user.home");
        File configFile = new File(homeDir, ".metroshrc");
        if (configFile.exists()) {
            _loadCommandFile(configFile.toString());
        }

        // Process input files non-interactively
        Iterator iter = _commandFiles.iterator();
        while (iter.hasNext()) {
            String fileName = (String) iter.next();
            _loadCommandFile(fileName);
            if (_nonInteractiveError)
                break;
        }

        // Interactive execution
        if (!_interactive)
            System.exit(0);
        _interactiveExecution();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Flag indicating if interactive execution is expected. */
    protected static boolean _interactive = true;

    /**
     * Flag indicating if there has been an error during non-interactive.
     * execution.
     */
    protected static boolean _nonInteractiveError = false;

    /**
     * Result to be shown at the beginning of interactive execution after all
     * command files have been executed.
     */
    protected static String _nonInteractiveResult = "";

    /**
     * List of command files that should be executed non-interactively before
     * going into interactive mode.
     */
    protected static List _commandFiles = new LinkedList();

    /**
     * Classpath of the meta-model compiler. This is the list of directories
     * where the meta-model compiler expects to find the top level meta-model
     * packages. This includes the directories where the compiler expects to
     * find the meta-model library (metamodel.lang) and the meta-model utilities
     * (metamodel.util) packages.
     */
    protected static List _classpath = new LinkedList();

    /** Jacl command interpreter used to process the commands. */
    protected static Interp _cmdInterp = new Interp();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Register all the commands used in the Metropolis environment. Use method
     * 'createCommand' of the Jacl interpreter to register all the commands that
     * are defined in the arrays 'commandName' and 'commandObjects'.
     */
    protected static void _registerMetropolisCommands() {
        // Define the functions for leaving as 'exit' or 'quit'
        Command exitCmd = _cmdInterp.getCommand("exit");
        if (exitCmd == null) {
            System.err.println("Error: cannot initialize tcl - aborting");
            System.exit(1);
        }
        _cmdInterp.createCommand("quit", exitCmd);

        // Define the commands for listing files 'ls' and 'dir' as 'glob'
        Command globCmd = _cmdInterp.getCommand("glob");
        if (globCmd == null) {
            System.err.println("Error: cannot initialize tcl - aborting");
            System.exit(1);
        }
        _cmdInterp.createCommand("ls", globCmd);
        _cmdInterp.createCommand("dir", globCmd);

        // Register metropolis defined commands
        for (int i = 0; i < _commandNames.length; i++) {
            String name = _commandNames[i];
            Command cmd = _commandObjects[i];
            _cmdInterp.createCommand(name, cmd);
        }

        // Add the help messages to the help command
        HelpCmd help = (HelpCmd) _commandObjects[0];
        for (int i = 0; i < _commandObjects.length; i++) {
            help.addHelpTopic(_commandObjects[i].getHelpTopic());
        }

    }

    /**
     * Execute a command file non-interactively, that is, ignoring the result.
     * Update _nonInteractiveError and _nonInteractiveResult as needed. Catch
     * the possible TclException from the evaluation of the command file.
     *
     * @param filename
     *            Name of the file to be loaded.
     */
    protected static void _loadCommandFile(String filename) {
        try {
            _cmdInterp.evalFile(filename);
            _nonInteractiveResult = _nonInteractiveResult
                    + "<> Loaded script '" + filename + "'\n";
        } catch (TclException e) {
            _nonInteractiveError = true;
            _nonInteractiveResult = _nonInteractiveResult
                    + "<> Error loading script '" + filename + "'"
                    + e.getMessage() + " - skipping\n";
        } catch (Exception e) {
            System.err.println("Error loading command file '" + filename
                    + "': " + e.getMessage() + " - aborting");
            System.exit(1);
        }
    }

    /**
     * Interactive session of the metropolis environment. Keep processing
     * commands read from the standard input until a 'quit' or 'exit' command is
     * invoked.
     */
    protected static void _interactiveExecution() {

        _printWelcomeMessage();

        // If there is any result to show from the interactive
        // execution, show it!
        if (!_nonInteractiveResult.equals("")) {
            System.out.println("\n");
            System.out.println(_nonInteractiveResult);
        }
        // Process all user commands until the user types 'quit'
        // or 'exit'. Other commands are passed to the Jacl interpreter
        BufferedReader input = new BufferedReader(new InputStreamReader(
                System.in));
        boolean finish = false;
        do {
            System.out.print(_prompt);
            String cmd = _readCommand(input);
            finish = _processCommand(cmd);
        } while (!finish);
    }

    /**
     * Read a command from the standard input. Normally, a command will be a
     * line ended by return. However, if the last character of the line is a
     * backslash (\), print the auxiliary prompt and keep reading lines until a
     * end of line is detected. Catch the possible IOException.
     *
     * @param input
     *            The buffer from which to read the input.
     * @return The line read from standard input.
     */
    protected static String _readCommand(BufferedReader input) {
        String cmd = "";
        boolean backSlashed = false;
        do {
            // Read a line
            try {
                cmd = cmd + " " + input.readLine();
                cmd = cmd.trim();
            } catch (IOException e) {
                System.err.println("Error: unable to read command from "
                        + "standard input - aborting.");
                System.exit(1);
            }
            if (cmd.endsWith("\\")) {
                cmd = cmd.substring(0, cmd.length() - 1);
                backSlashed = true;
                System.out.print(_auxPrompt);
            } else {
                backSlashed = false;
            }
        } while (backSlashed);
        return cmd;
    }

    /**
     * Process a command introduced by the user by calling the Jacl command
     * interpreter.
     *
     * @param cmd
     *            String with the command.
     * @return true iff the shell should be exited after the command is
     *         processed.
     */
    protected static boolean _processCommand(String cmd) {
        try {
            _cmdInterp.eval(cmd);
        } catch (TclException e) {
            System.out.print("ERROR - ");
        }
        String result = _cmdInterp.getResult().toString();
        if (!result.equals(""))
            System.out.println(result);
        return false;
    }

    /** Print the welcome message to the metropolis environment. */
    protected static void _printWelcomeMessage() {
        for (int i = 0; i < _welcomeMessage.length; i++) {
            System.out.println(_welcomeMessage[i]);
        }
    }

    /**
     * Process the command line arguments of the shell.
     *
     * @param args
     *            Array of command line arguments.
     */
    protected static void _processArgs(String args[]) {
        for (int i = 0; i < args.length;) {
            if (args[i].equals("-ni")) {
                // No interactive execution
                _interactive = false;
                i++;
            } else if (args[i].equals("-classpath")) {
                // Read the classpath from the command-line
                // This should handle the following
                // -classpath c:/foo : c:/bar
                // -classpath c:/foo ; c:/bar
                // Under Windows:
                // -classpath c:/foo;c:/bar

                i++;
                while (i < args.length) {
                    // Handle each argument that might have a : or ; in it
                    String[] classpath = args[i].split(File.pathSeparator);
                    for (int j = 0; j < classpath.length; j++) {
                        try {
                            File file = new File(classpath[j]);
                            classpath[j] = file.getCanonicalPath();
                        } catch (Exception ex) {
                            throw new RuntimeException("Failed to process "
                                    + "-classpath value '" + classpath[j]
                                    + "'.", ex);
                        }
                        _classpath.add(classpath[j]);
                    }
                    i++;
                    if (i < args.length) {
                        if (args[i].equals(":") || args[i].equals(";")) {
                            i++;
                        } else {
                            // If we get to an argument that is not : or ;
                            // the we are done.
                            break;
                        }
                    }
                }
            } else {
                // Command file to be executed non-interactively
                _commandFiles.add(args[i]);
                i++;
            }
        }
    }

    /** Package loaded is elaborated. */
    protected static void setElaborated() {
        _elaborated = true;
    }

    /** Package loaded is not elaborated. */
    protected static void setNotElaborated() {
        _elaborated = false;
    }

    /**
     * Return true if the package loaded has been elaborated.
     *
     * @return true if this object is elaborated
     */
    protected static boolean isElaborated() {
        return _elaborated;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * Welcome message. This message will be printed to standard output each
     * time that an interactive session with the shell begins.
     */
    private static String _welcomeMessage[] = {
            "/------------------------------------------------------------------------\\",
            "|         Metropolis: Design Environment for Heterogeneous Systems       |",
            "|                                                                        |",
            "|  Copyright (c) 1998-2005 The Regents of the University of California.  |",
            "|                        All rights reserved                             |",
            "\\------------------------------------------------------------------------/",
            "",
            "New to Metropolis? Type 'help' for information on the available commands.",
            "" };

    /** Prompt of the metropolis shell. */
    private static String _prompt = "metropolis> ";

    /**
     * Auxiliary prompt of metropolis. This prompt is used when a command lasts
     * more than one line.
     */
    private static String _auxPrompt = "cont> ";

    /** Array of metamodel command names. */
    private static String _commandNames[] = { "help", "classpath", "metroload",
            "simulate", "elaborate", "metrolist", "network" };

    /** Array of objects serving the meta-model commands. */
    private static MetropolisCmd _commandObjects[] = { new HelpCmd(),
            new ClasspathCmd(), new MetroLoadCmd(), new SimulateCmd(),
            new ElaborateCmd(), new MetroListCmd(), new NetworkCmd() };

    /**
     * Flag indicating whether a metamodel package has been loaded but not
     * elaborated
     */
    private static boolean _elaborated = false;

}
