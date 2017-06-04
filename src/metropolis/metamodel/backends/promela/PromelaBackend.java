/* A back-end that generates Promela code from an AST.

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

package metropolis.metamodel.backends.promela;

import metropolis.metamodel.StringManip;
import metropolis.metamodel.backends.Backend;
import metropolis.metamodel.nodetypes.CompileUnitNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // PromelaBackend
/**
 * A back-end that generates Promela code from an AST. The class
 * PromelaCodegenVisitor is used to generate the Promela code.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Xi Chen
 * @version $Id: PromelaBackend.java,v 1.13 2006/10/12 20:32:48 cxh Exp $
 */
public class PromelaBackend implements Backend {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Create a new Promela back-end. */
    public PromelaBackend() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Implement method 'invoke()' from the Backend interface. This method calls
     * the SystemCCodegenVisitor on all the compiled asts. Sources are stored as
     * files with the same name and extension changed to 'cpp'.
     *
     * @param args
     *            List of arguments; expected empty.
     * @param sources
     *            List of compiled ASTs.
     */
    public void invoke(List args, List sources) {
        // Check arguments
        if (args.size() > 0)
            throw new RuntimeException("Unexpected argument");

        // Generate code for each source file
        Iterator iter = sources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            _emitCode(ast);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Default extension for generated files. */
    protected final String _defaultExtension = ".pml";

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Get the name of the target file where the source will be stored.
     *
     * @param ast
     *            AST obtained from the source file.
     * @return the target name of the AST.
     */
    protected String _getTargetName(CompileUnitNode ast) {
        String filename = (String) ast.getProperty(IDENT_KEY);
        String targetName = StringManip.partBeforeLast(filename, '.');
        targetName = targetName + _defaultExtension;
        return targetName;
    }

    /**
     * Generate code from an AST and print it to a file. The generation of the
     * code is performed by a call to PromelaCodegenVisitor.
     *
     * @param ast
     *            Compiled AST.
     */
    protected void _emitCode(CompileUnitNode ast) {

        // Call PromelaCodegenVisitor
        String code = (String) ast.accept(new PromelaCodegenVisitor(), null);

        // Write the code to an output file
        try {
            String targetName = _getTargetName(ast);
            FileWriter target = new FileWriter(targetName);
            target.write(code);
            target.flush();
            target.close();
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }

    }

}
