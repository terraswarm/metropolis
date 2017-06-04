/* Generate output for one or more compile units.

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
package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.StringManip;
import metropolis.metamodel.backends.systemc.mmdebug.DebuggerInfoCodegen;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // CompileUnit
/**
 * Generate output for one or more compile units.
 *
 * <p>
 * Created on May 6, 2003, 1:23 PM
 *
 * @author Daniele Gasperini, Contributor: Christopher Brooks
 * @version $Id: CompileUnit.java,v 1.33 2006/10/12 20:32:58 cxh Exp $
 */
public class CompileUnit {

    /**
     * Instantiate a CompileUnit.
     *
     * @param header
     *            The corresponding HeaderCodegen class instance
     * @param source
     *            Source file
     */
    public CompileUnit(HeaderCodegen header, LinkedList source) {
        _headerCache = header;
        _sourceCache = source;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the base of the header cache file name. The basename is the substring
     * that follows after the last file separator.
     *
     * @return The basename.
     * @see metropolis.metamodel.StringManip#baseFilename(String)
     */
    public String getBaseName() {
        return StringManip.baseFilename(_headerCache.getFilename());
    }

    /**
     * Get the base name of the C++ file to be generated. The basename is the
     * substring that follows after the last file separator
     *
     * @return The C++ file name.
     * @see metropolis.metamodel.backends.systemc.MakefileCodegen#_CPP
     * @see metropolis.metamodel.StringManip#baseFilename(String)
     */
    public String getCppBaseName() {
        return getBaseName() + MakefileCodegen._CPP;
    }

    /**
     * Get the full name of the C++ file to be generated.
     *
     * @return The C++ file name.
     * @see metropolis.metamodel.backends.systemc.MakefileCodegen#_CPP
     */
    public String getCppTargetName() {
        return _headerCache.getFilename() + MakefileCodegen._CPP;
    }

    /**
     * Get the base name of the include file to be generated. The basename is
     * the substring that follows after the last file separator.
     *
     * @return The include file name.
     * @see metropolis.metamodel.backends.systemc.MakefileCodegen#_H
     * @see metropolis.metamodel.StringManip#baseFilename(String)
     */
    public String getHBaseName() {
        return getBaseName() + MakefileCodegen._CPP;
    }

    /**
     * Get the header cache that was passed in to the constructor.
     *
     * @return The header cache
     */
    public HeaderCodegen getHeaderCache() {
        return _headerCache;
    }

    /**
     * Get the full name of the include file to be generated.
     *
     * @return The include file name.
     * @see metropolis.metamodel.backends.systemc.MakefileCodegen#_H
     */
    public String getHTargetName() {
        return _headerCache.getFilename() + MakefileCodegen._H;
    }

    /**
     * Get the name of the MMM file.
     *
     * @return The MMM file name.
     */
    public String getMMMFileName() {
        return _headerCache.getMMMFilename();
    }

    /**
     * Get the truth of whether this <code>CompileUnit</code> includes added
     * information for the metamodel debugger.
     *
     * @return The truth of whether this <code>CompileUnit</code> includes
     *         added information for the metamodel debugger.
     * @see #setHasDebuggerInfo
     */
    public boolean getHasDebuggerInfo() {
        return _hasDebuggerInfo;
    }

    /**
     * Return the truth of whether this <code>CompileUnite</code>
     * has templates.
     *
     * @return <code>true</code> if this has templates.
     * @see #setHasTemplates(boolean)
     */
    public boolean getHasTemplates() {
        return _hasTemplates;
    }

    /**
     * Save the current CompileUnit to a C++ file and an include file.
     *
     * @param overwrite
     *            <code>true</code> if existing output files should be
     *            overwritten.
     * @param includes
     *            The string that usually contains include files and has a
     *            trailing newline. This string appears first in the .cpp file.
     */
    public void save(boolean overwrite, String includes) {
        try {
            _saveHeader(overwrite);
            _saveSource(overwrite, includes);
        } catch (Exception ex) {
            // FIXME: we should not throw a Runtime here.
            // Instead the caller should handle the Exception.
            throw new RuntimeException(ex);
        }
    }

    /**
     * Set the file name of header cache.
     *
     * @param name
     *            The file name.
     */
    public void setFilename(String name) {
        // FIXME: Should this be setFileName instead of setFilename?
        _headerCache.setFilename(name);
    }

    /**
     * Set the boolean state indicating whether this <code>CompileUnit</code>
     * includes information for use by the metamodel debugger.
     *
     * @param hasDebuggerInfo
     *            <code>true</code> if it does, <code>false</code> if it
     *            doesn't.
     * @see #getHasDebuggerInfo
     */
    public void setHasDebuggerInfo(boolean hasDebuggerInfo) {
        _hasDebuggerInfo = hasDebuggerInfo;
    }

    /**
     * Set the MMM file name of header cache.
     *
     * @param name
     *            The MMM file name.
     */
    public void setMMMFilename(String name) {
        // FIXME: Should this be setMMMFileName instead of setMMMFilename?
        _headerCache.setMMMFilename(name);
    }

    /**
     * Set whether this CompileUnit includes templates or not.
     *
     * @param hasTemplates
     * whether or not it has templates.
     * @see #getHasTemplates()
     */
    public void setHasTemplates(boolean hasTemplates) {
        _hasTemplates = hasTemplates;
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    // Save the compile unit into an include file.
    private void _saveHeader(boolean overwrite) throws Exception {
        File hFile = new File(getHTargetName());
        File mmmFile = new File(getMMMFileName());
        if (overwrite || !hFile.exists()
                || mmmFile.lastModified() > hFile.lastModified()) {

            FileWriter file = new FileWriter(hFile);

            file.write("#ifndef " + MakefileCodegen._PREFIX
                    + getBaseName().toUpperCase() + "_H\n");
            file.write("#define " + MakefileCodegen._PREFIX
                    + getBaseName().toUpperCase() + "_H\n");

            file.write("#include \"library.h\"\n");
            if (_hasDebuggerInfo) {
                file.write("#include \"" + DebuggerInfoCodegen.getBaseName()
                        + ".h\"\n");
            }
            _headerCache.EliminateMetamodelLangFromIncludeDecl();
            file.write(_headerCache.getIncludeDeclString());
            file.write(_headerCache.getExternDeclString());
            file.write(_headerCache.getForwardDeclString());
            file.write(SystemCCodegenVisitor._stringListToString(_headerCache
                    .getCode()));
            file.write("\n#endif\n");

            // FIXME: put these in a finally clause
            file.flush();
            file.close();
        }
    }

    // Save the compile unit into a c++ file.
    private void _saveSource(boolean overwrite, String includes)
            throws Exception {
        File cppFile = new File(getCppTargetName());
        File mmmFile = new File(getMMMFileName());
        if (overwrite || !cppFile.exists()
                || mmmFile.lastModified() > cppFile.lastModified()) {
            FileWriter cppWriter = new FileWriter(cppFile);

            cppWriter.write(includes);
            cppWriter.write("// begin static variables\n");
            cppWriter.write(_headerCache.getStaticDeclString());
            cppWriter.write("// end static variables\n");
            SystemCCodegenVisitor._stringListToString2(_sourceCache, cppWriter);
            // FIXME: put these in a finally clause
            cppWriter.write("\n\n");
            cppWriter.flush();
            cppWriter.close();
            if (_hasDebuggerInfo) {
                SystemCCodegenVisitor._fixLineDirectives(cppFile);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private members ////

    private boolean _hasTemplates = false;

    private HeaderCodegen _headerCache = null;

    private boolean _hasDebuggerInfo = false;

    private LinkedList _sourceCache = null;
}
