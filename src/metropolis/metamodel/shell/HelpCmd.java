/* Class processing help commands in the metropolis shell.

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

import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;

import java.util.Hashtable;
import java.util.Iterator;

// ////////////////////////////////////////////////////////////////////////
// // HelpCmd
/**
 * A class dealing with the help command in the metropolis shell.
 *
 * @author Robert Clariso
 * @version $Id: HelpCmd.java,v 1.18 2006/10/12 20:38:37 cxh Exp $
 */
public class HelpCmd extends MetropolisCmd {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Execute the help command by displaying a help message related to the used
     * topic. This command can be invoked with 0 arguments (general help) or
     * with 1 argument (help on a specific topic).
     *
     * @param interp
     *            Tcl command interpreter.
     * @param objv
     *            Parameters of the tcl command.
     * @exception TclNumArgsException
     *                if there are too many parameters in the command.
     * @exception TclException
     *                if the help topic is not available.
     */
    public void cmdProc(Interp interp, TclObject[] objv) throws TclException {

        // Check the number of arguments
        String topicName = null;
        switch (objv.length) {
        case 1:
            topicName = _generalHelp.getName();
            break;
        case 2:
            topicName = objv[1].toString();
            break;
        default:
            throw new TclNumArgsException(interp, 1, objv, "?topic?");
        }

        // Check if we want a list of all help topics
        if (topicName.equals("topics")) {
            String msg = "\n";
            Iterator topics = _helpTopics.values().iterator();
            while (topics.hasNext()) {
                HelpTopic topic = (HelpTopic) topics.next();
                msg = msg + topic.getSummary() + "\n";
            }
            interp.setResult(msg);
        } else {
            // Find information about the topic
            HelpTopic topic = (HelpTopic) _helpTopics.get(topicName);
            if (topic == null)
                throw new TclException(interp, "help about topic '" + topicName
                        + "' not available");
            interp.setResult(topic.toString());
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

    /**
     * Add a new help topic to this command. If the name of the topic already
     * exists, the older topic is removed.
     *
     * @param topic
     *            A help topic to be added.
     */
    public void addHelpTopic(HelpTopic topic) {
        _helpTopics.put(topic.getName(), topic);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Hashtable of help topics. This table can be used to look up a topic by
     * name; it is initialized statically.
     */
    protected static Hashtable _helpTopics;

    /** Help message for the 'help' command. */
    protected static HelpTopic _helpTopic;

    /** General help message for the shell. */
    protected static HelpTopic _generalHelp;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // Static initializer: initialize the help topics
    static {

        // Help about the 'help' command
        String name = "help";
        String summary = "Print a help message for a given topic";
        String usage[] = { "help ?topic?\n" };
        String text[] = {
                "All commands of the Metropolis shell provide some help information about",
                "how they should be used. This commands prints a help message about a",
                "given topic, or a warning if the help topic." };
        String seeAlso[] = { "metropolis-shell", "topics",
                "metropolis-invocation", "tcl-commands" };
        _helpTopic = new HelpTopic(name, summary, usage, text, seeAlso);

        // Help about the Metropolis shell
        name = "metropolis-shell";
        summary = "General information about the metropolis shell";
        String text2[] = {
                "The Metropolis shell is intended to provide an interactive tool that",
                "provides access to the tools in the Metropolis framework. It provides",
                "commands to load meta-model specifications and perform some tasks",
                "like simulation, code generation, etc.\n",
                "A typical session begins by using the 'metroload' command to get a meta-model",
                "specification in memory. Then, commands like 'simulate' or 'elaborate'",
                "can be used to handle this specification.\n",
                "Use 'help' followed by a topic name to get information about the topic.",
                "Type 'help topics' to get the list of help topics available. Use ",
                "'help tcl-commands' to get help on the Tcl commands supported by the",
                "metropolis shell. Finally, you can learn about how the metropolis shell",
                "can be invoked by typing 'help metropolis-invocation'." };
        String seeAlso2[] = { "help", "topics", "tcl-commands",
                "metropolis-invocation" };
        _generalHelp = new HelpTopic(name, summary, text2, seeAlso2);

        // Help about Tcl commands available in the Metropolis shell
        name = "tcl-commands";
        summary = "Summary of the Tcl commands available in the Metropolis shell";
        String text3[] = {
                "All tcl built-in commands are available in the metropolis shell. This ",
                "list is not exhaustive:",
                "- exit/quit       leave the Metropolis shell.",
                "- exec COMMAND    execute a command in the system shell.",
                "- pwd             show the current working directory.",
                "- cd DIR          change the working directory.",
                "- set NAME ?VAL?  define the value of a variable. The value can be",
                "                  accessed in any context as $NAME.",
                "- expr EXPR       compute a mathematical expression that can contain ",
                "                  variables defined using 'set'.",
                "- glob/ls/dir     list all files that match a given pattern",
                "- #COMMENT        comment until the end of this line",
                "Other features that would be to long to describe here include defining",
                "procedures, loops, conditionals, reading from file... These commands",
                "are described in detail in any Tcl manual." };
        String seeAlso3[] = { "metropolis-shell", "metropolis-invocation" };
        HelpTopic tclCommands = new HelpTopic(name, summary, text3, seeAlso3);

        // Help about invoking the Metropolis shell
        name = "metropolis-invocation";
        String usage4[] = { "metroshell [-classpath path [ : paths]] [scriptfiles] [-ni]" };
        summary = "Options of the invocation of the Metropolis shell";
        String text4[] = {
                "The Metropolis shell invocation mechanism proceeds as follows:",
                "- First, the classpath from the command line is read. The path to the",
                "  Meta-Model library and the paths in METRO_CLASSPATH are automatically",
                "  included in the classpath, and therefore they should not be added by",
                "  the user.",
                "- Then, the meta-model library classes are loaded.",
                "- Then, the shell tests if the user has a file '.metroshrc' in their",
                "  home directory. If it exists, it is executed. This file can change",
                "  the classpath, define useful variables or even load meta-model files.",
                "  All commands available in the shell can be used in this script.",
                "- After that, the shell executes all the commands contained in the",
                "  script files specified in the command line. Again, they can use all",
                "  the commands available in the shell, and this can be used to",
                "  automate tasks (e.g. loading a set of packages).",
                "- Finally, if the flag '-ni' (no interactive) does not appear in the",
                "  command line, the shell enters in interactive mode, until the command",
                "  'exit' or 'quit' is used.\n",
                "Users are encouraged to use '.metroshrc' and script files to automate",
                "tasks and simplify the use of the Metropolis shell. It should be",
                "emphasized that the entire shell can work non-interactively if the flag",
                "'-ni' is provided and the script file to be executed is passed to the",
                "command line" };
        String seeAlso4[] = { "metropolis-shell" };
        HelpTopic invocation = new HelpTopic(name, summary, usage4, text4,
                seeAlso4);

        // Initialize the hash table of help topics
        _helpTopics = new Hashtable();
        _helpTopics.put(_generalHelp.getName(), _generalHelp);
        _helpTopics.put(tclCommands.getName(), tclCommands);
        _helpTopics.put(invocation.getName(), invocation);
    }

}
