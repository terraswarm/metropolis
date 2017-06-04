/* A base class for all help topics.

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

// ////////////////////////////////////////////////////////////////////////
// // HelpTopic
/**
 * A base class for all help topics available in the Metropolis shell.
 *
 * @author Robert Clariso
 * @version $Id: HelpTopic.java,v 1.11 2006/10/12 20:38:38 cxh Exp $
 */
public class HelpTopic {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new help topic of a command, with a given name, usage and
     * description. Information is provided as a String for some fields, or as
     * an array of Strings. In the latter case, a "\n" will be appended after
     * each String of the array.
     *
     * @param name
     *            The name of this topic.
     * @param summary
     *            A one-line summary of this command.
     * @param usage
     *            A usage String for the command.
     * @param text
     *            A long text message.
     * @param seeAlso
     *            Related commands.
     */
    public HelpTopic(String name, String summary, String usage[],
            String text[], String seeAlso[]) {
        _name = name;
        _summary = summary;
        _usage = usage;
        _text = text;
        _seeAlso = seeAlso;
    }

    /**
     * Build a new help topic, with a given name and a given help information.
     * Information is provided as a String for some fields, or as an array of
     * Strings. In the latter case, a "\n" will be appended after each String of
     * the array.
     *
     * @param name
     *            The name of this topic.
     * @param summary
     *            A one-line summary of this command.
     * @param text
     *            A long text message.
     * @param seeAlso
     *            Related commands.
     */
    public HelpTopic(String name, String summary, String text[],
            String seeAlso[]) {
        _name = name;
        _summary = summary;
        _usage = null;
        _text = text;
        _seeAlso = seeAlso;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the name of this help topic.
     *
     * @return The name of this help topic.
     */
    public String getName() {
        return _name;
    }

    /**
     * Get a one-line description of the topic.
     *
     * @return A String with a one-line description of the help topic.
     */
    public String getSummary() {
        String msgName = _name;
        int len = 23 - _name.length();
        while (len > 0) {
            msgName = msgName + " ";
            len--;
        }
        return msgName + "- " + _summary;
    }

    /**
     * Convert this help topic to a String.
     *
     * @return A String with the information contained in the help topic.
     */
    public String toString() {
        String msg = "\n" + _name + " - " + _summary + "\n";
        if (_usage != null) {
            msg = msg + "\nUSAGE\n";
            for (int i = 0; i < _usage.length; i++)
                msg = msg + "    " + _usage[i] + "\n";
        }
        msg = msg + "\n";
        for (int i = 0; i < _text.length; i++)
            msg = msg + _text[i] + "\n";
        msg = msg + "\nSEE ALSO\n  ";
        for (int i = 0; i < _seeAlso.length; i++)
            msg = msg + _seeAlso[i] + "  ";
        msg = msg + "\n";
        return msg;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Name of the help topic. */
    protected String _name;

    /** One-line summary of the behavior of the command. */
    protected String _summary;

    /** Usage information (null if no usage information is relevant). */
    protected String _usage[];

    /**
     * Text of the help message. This message should elaborate on the summary of
     * the command, and provide information about the different options provided
     * in the usage (if any).
     */
    protected String _text[];

    /** Array of related help topics. */
    protected String _seeAlso[];

}
