/* A generic inteface to be implemented by all back-ends.

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

package metropolis.metamodel.backends;

import metropolis.metamodel.MetaModelStaticSemanticConstants;

import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // Backend
/**
 * A generic interface to be implemented by all back-end tools. This defines how
 * back-end modules have to be invoked.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: Backend.java,v 1.10 2006/10/12 20:32:19 cxh Exp $
 */
public interface Backend extends MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public abstract methods ////

    /**
     * Method that invokes the back-end tool, with a list of arguments (possibly
     * obtained from the command-line arguments) and a mapping from source file
     * names to abstract syntax trees.
     *
     * @param args
     *            List of arguments for the back-end. The type and meaning of
     *            this arguments may change from one back-end to another.
     * @param sources
     *            List of asts obtained from the front-end by compiling the
     *            source files in the command line.
     */
    void invoke(List args, List sources);

}
