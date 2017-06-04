/* A program that reads a meta-model file and checks basic syntax.

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

import metropolis.metamodel.frontend.parser.MetaModelParser;

// ////////////////////////////////////////////////////////////////////////
// // SyntaxChecker
/**
 * A very simple program that reads a set of meta-model files and checks syntax.
 * Errors are notified providing "filename" + "line number", but little (if any)
 * information is provided on the cause of the error.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: SyntaxChecker.java,v 1.16 2006/10/12 20:34:17 cxh Exp $
 */
public class SyntaxChecker {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Print usage of the syntax checker. The input parameters can be a list of
     * meta-model files, that can be preceded (optionally) by a debug flag '-d'.
     */
    public static void printUsage() {
        System.out.println("Usage: SyntaxChecker [-d] file_list");
    }

    /**
     * Check syntax of current file. Invoke the parser to get the job done.
     *
     * @param filename
     *            Name of the file being checked.
     * @param debug
     *            Turns on/off debugging.
     */
    public static void check(String filename, boolean debug) {
        MetaModelParser parser = new MetaModelParser();
        try {
            parser.init(filename);
        } catch (Exception e) {
            System.err.println("Error: opening input file " + filename);
            System.err.println(e.toString());
        }
        parser.yydebug = debug;
        System.out.println("Parsing file " + filename + "...");
        parser.parse();
    }

    /**
     * Main method of the syntax checker. Reads parameters from the command line
     * and invokes the parser to check syntax
     *
     * @param args
     *            Command line arguments
     * @see #printUsage()
     */
    public static void main(String[] args) {

        int files = args.length;
        int fileStart = 0;
        boolean debug = false;

        if (files >= 1) {
            debug = args[0].equals("-d");
            if (debug) {
                fileStart++;
            }
        }

        if (files < 1)
            printUsage();
        else
            for (int f = fileStart; f < files; f++)
                check(args[f], debug);
    }

}
