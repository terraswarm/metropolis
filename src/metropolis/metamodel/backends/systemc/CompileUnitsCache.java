/*
 Copyright (c) 2003-2005 The Regents of the University of California.
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
/*
 * CompileUnitsCache.java
 *
 * Created on May 6, 2003, 1:35 PM
 */

package metropolis.metamodel.backends.systemc;

import java.util.Vector;

/**
 * This class serves to collect all the <code>CompileUnit</code> in a
 * meta-model compilation, to simplify dealing with them collectively, instead
 * of having to deal with each one individually.
 *
 * @author Daniele Gasperini
 * @version $Id: CompileUnitsCache.java,v 1.21 2006/10/12 20:32:59 cxh Exp $
 */
public class CompileUnitsCache {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Creates a new instance of CompileUnitsCache. */
    public CompileUnitsCache() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Add a <code>CompileUnit</code> to this collection.
     *
     * @param cu
     *            the <code>CompileUnit</code> to add.
     */
    public void add(CompileUnit cu) {
        _compileUnits.add(cu);
    }

    /**
     * Set the C++ include lines to be added to each C++ file generated.
     *
     * @param includes
     *            the <code>#include</code> lines to be, um, included.
     * @see #getGeneralIncludes
     */
    public void setGeneralIncludes(String includes) {
        _includes = includes;
    }

    /**
     * Return the C++ include lines to be added to each C++ file generated.
     *
     * @return the <code>#include</code> lines.
     * @see #setGeneralIncludes
     */
    public String getGeneralIncludes() {
        return _includes;
    }

    /**
     * Generate the C++ files for all the <code>CompileUnit</code>s,
     * optionally adding information to the C++ files to be used by the
     * meta-model debugger.
     *
     * @param overwrite
     *            <code>true</code> if existing output files should be
     *            overwritten.
     */
    public void save(boolean overwrite) {
        _fromClassToInclude();

        for (int i = 0; i < _compileUnits.size(); i++) {
            CompileUnit cu = (CompileUnit) _compileUnits.get(i);
            /*
             * CompileUnitNode ast = (CompileUnitNode) iter.next(); PackageDecl
             * pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
             */
            // cu.save(overwrite,
            // "#include \""+cu.getHTargetName()+"\"\n", debuggingMetamodel);
            cu.save(overwrite, _includes);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    private void _fromClassToInclude() {
        for (int i = 0; i < _compileUnits.size(); i++) {
            CompileUnit cu = (CompileUnit) _compileUnits.get(i);
            Vector classes = cu.getHeaderCache().getIncludeDecl();

            for (int j = 0; j < classes.size(); j++) {
                String className = (String) classes.get(j);
                String classFile = _getClassFile(className);
                cu.getHeaderCache().replaceIncludeDecl(j, classFile);
                if (classFile == null)
                    j--;
            }

            cu.getHeaderCache().compactIncludeDecl(cu.getHTargetName());
        }
    }

    private String _getClassFile(String name) {
        for (int i = 0; i < _compileUnits.size(); i++) {
            CompileUnit cu = (CompileUnit) _compileUnits.get(i);
            if (cu.getHeaderCache().containsClassDeclaration(name))
                return cu.getHTargetName();
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // private members ////

    private String _includes = null;

    private Vector _compileUnits = new Vector();
}
