/* A back-end that generates meta-model code from an AST.

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

package metropolis.metamodel.backends.metamodel;

import metropolis.metamodel.StringManip;
import metropolis.metamodel.backends.Backend;
import metropolis.metamodel.nodetypes.CompileUnitNode;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // MetaModelBackend
/**
 * A back-end that generates meta-model code from an AST in memory. The
 * MetaModelCodegenVisitor is used to generate this code.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: MetaModelBackend.java,v 1.18 2006/10/12 20:32:45 cxh Exp $
 */
public class MetaModelBackend implements Backend {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Create a new meta-model back-end. */
    public MetaModelBackend() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Implement method 'invoke()' from the Backend interface. This method calls
     * the MetaModelCodegenVisitor on all the compiled asts. Sources are stored
     * as files with the same name and extension changed to 'out'.
     *
     * @param args
     *            List of arguments; expected empty.
     * @param sourceList
     *            List of compiled ASTs.
     */
    public void invoke(List args, List sourceList) {
        boolean dumpingAST = false;

        // Check arguments
        if (args.size() > 0) {
            if (!((String) args.get(0)).equalsIgnoreCase("dumpast")) {
                dumpingAST = true;
                args.remove(0);
            }
        }
        if (args.size() > 0) {
            StringBuffer buf = new StringBuffer();
            buf.append("Unexpected argument(s): \"");
            String separator = "";
            for (int i = 0; i < args.size(); i++) {
                buf.append(separator);
                buf.append((String) args.get(i));
                separator = "\", \"";
            }
            buf.append("\".");
            throw new RuntimeException(buf.toString());
        }

        // Generate code for each source file
        Iterator sources = sourceList.iterator();
        while (sources.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) sources.next();
            if (dumpingAST) {
                System.out.println(ast);
            }
            _emitCode(ast);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Default extension for generated files. */
    protected final String _defaultExtension = ".out";

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Generate code from an AST and print it to a file. The generation of the
     * code is performed by a call to MetaModelCodegenVisitor.
     *
     * @param ast
     *            Compiled AST.
     */
    protected void _emitCode(CompileUnitNode ast) {

        // Call MetaModelCodegenVisitor
        String code = (String) ast.accept(new MetaModelCodegenVisitor(), null);

        // Write the code to an output file
        String targetName = _getTargetName(ast);
        try {
            FileWriter target = new FileWriter(targetName);
            target.write(code);
            target.flush();
            target.close();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to open or write '" + targetName
                    + "'", ex);
        }

    }

    /**
     * Get the name of the target file where the source will be stored.
     *
     * @param ast
     *            AST obtained from the source file.
     * @return The name of the target file.
     */
    protected String _getTargetName(CompileUnitNode ast) {
        String filename = (String) ast.getProperty(IDENT_KEY);
        String targetName = StringManip.partBeforeLast(filename, '.');
        targetName = targetName + _defaultExtension;
        return targetName;
    }
}
