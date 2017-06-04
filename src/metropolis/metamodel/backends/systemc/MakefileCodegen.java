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

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.backends.systemc.mmdebug.DebuggerInfoCodegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

// ////////////////////////////////////////////////////////////////////////
// // MakefileCodeGen
/**
 * Generate a makefile.
 *
 * <p>
 * Created on May 5, 2003, 10:42 AM
 *
 * @author Daniele Gasperini, Contributor: Christopher Brooks
 * @version $Id: MakefileCodegen.java,v 1.38 2006/10/12 20:33:08 cxh Exp $
 */
public class MakefileCodegen {

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // We use a makefile with the .mk extension so that tools can tell
    // it is a makefile
    /** The file extension for cpp files. */
    public static String _CPP = ".cpp";

    /** The file extension for header files. */
    public static String _H = ".h";

    /** The file extension for object files. */
    public static String _O = ".o";

    /** The prefix for macros for each header files. */
    public static String _PREFIX = "MMSCS_";

    /**
     * Create a new instance of MakefileCodegen (non-debugging version).
     *
     * @exception IOException
     */
    public MakefileCodegen() throws IOException {
        _file = new FileWriter(_FILENAME);
    }

    /**
     * Create a new instance of MakefileCodegen.
     *
     * @param debuggingMetamodel
     *            <code>true</code> if generated makefile should be configured
     *            for running a debugger on the executable, with the <i>.mmm</i>
     *            files substituted for the corresponding <i>.cpp</i> files.
     * @exception IOException
     */
    public MakefileCodegen(boolean debuggingMetamodel) throws IOException {
        this();
        _debuggingMetamodel = debuggingMetamodel;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Delete the makefile if it exists and it is empty. This method is used to
     * clean up if there is an error while creating the makefile that results in
     * a 0 length file. Zero length makefiles cause problems with "make clean".
     *
     * @exception IOException
     *                If deleting the file throws it.
     */
    public void deleteIfEmpty() throws IOException {
        File file = new File(_FILENAME);
        if (file.exists() && file.length() == 0) {
            file.delete();
        }
    }

    /**
     * Return the name of the makefile.
     *
     * @return The name of the makefile, which should be "systemc_sim.mk".
     */
    public String getFileName() {
        return _FILENAME;
    }

    /**
     * Generate a header for the makefile. If a file named makefile or makefile
     * exists, then read it and look for lines that start with "include", but do
     * not contain $(CONFIG), metro.mk or metrocommon.mk and output them in the
     * generated file.
     *
     * @exception IOException
     */
    public void header() throws IOException {
        // If we find a makefile or Makefile, we save the name.
        String sourceMakefile = null;

        // include lines from the makefile or Makefile
        String includes = "";
        try {
            BufferedReader bf;
            // First, we check for makefile and then for Makefile because
            // that is what GNU make does.
            try {
                sourceMakefile = "makefile";
                bf = new BufferedReader(new FileReader(sourceMakefile));

            } catch (FileNotFoundException ex) {
                sourceMakefile = "Makefile";
                bf = new BufferedReader(new FileReader(sourceMakefile));
            }

            do {
                String line = bf.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("include")) {
                    // Skip includes of $(CONFIG), metro.mk and metrocommon
                    if (line.indexOf("$(CONFIG)") == -1
                            && line.indexOf("metro.mk") == -1
                            && line.indexOf("metrocommon.mk") == -1) {
                        includes += line + "\n";
                    }
                }
            } while (true);
        } catch (Exception e) {
            // do nothing, Makefile or makefile does not exist.
        }

        _file.write("# This makefile is automatically generated by "
                + "Metropolis.\n\n");
        if (_debuggingMetamodel) {
            _file.write("override CXX_OPTIMIZER += -ggdb\n\n");
        }
        _file
                .write("ROOT =      $(METRO)\n\n"
                        + "CONFIG =    $(ROOT)/mk/metro.mk\n"
                        + "include $(CONFIG)\n\n");
        if (includes.length() > 0) {
            _file.write("\n# The following includes appeared in the "
                    + sourceMakefile + ":\n" + includes + "\n");
        }
        _file.write("CXX_INCLUDES = " + _includes + "\n\n"
                + "LIBRARIES_PATH = " + _librariesPath + "\n\n");
        if (_debuggingMetamodel) {
            _file.write("MMDEBUG_INCLUDES = " + _mmdebugIncludes + "\n\n");
            _file.write("MMDEBUG_SRC_CPP = \\\n");
            for (int i = 0; i < _filelist.size(); i++) {
                String baseFilename = (String) _filelist.get(i);

                // Windows: Convert backslashes to forward slashes.
                baseFilename = baseFilename.replace('\\', '/');

                _file.write("\t" + baseFilename + ".cpp \\\n");
            }
            _file.write("\nMMDEBUG_SRC_H = $(MMDEBUG_SRC_CPP:%.cpp=%.h)\n");
            _file.write("\nMMDEBUG_SRC_O = $(MMDEBUG_SRC_CPP:%.cpp=%.o)\n");
            _file
                    .write("\nSYSTEMC_SIM_KRUFT = $(MMDEBUG_SRC_CPP) $(MMDEBUG_SRC_H)");
            _file.write(" $(MMDEBUG_SRC_O)\n\n");

        }

        _file.write("all: bin\n\n");
    }

    /**
     * Add a file to the list of files.
     *
     * @param baseFilename
     *            The file name to be added.
     */
    public void add(String baseFilename) {
        _filelist.add(baseFilename);
    }

    /**
     * Add a file to the list of library files involved in the build.
     *
     * @param baseFilename
     *            The file name to be added.
     */
    public void addToLibFiles(String baseFilename) {
        _langFileList.add(baseFilename);
    }

    /**
     * Generate the body of the makefile.
     *
     * @exception IOException
     */
    public void body() throws IOException {
        String debuggerInfoBaseName = null;

        for (int i = 0; i < _filelist.size(); i++) {
            String baseFilename = (String) _filelist.get(i);
            baseFilename = _fixWindowsPathname(baseFilename);

            _file.write(baseFilename + _O + ": \\\n" + "\t\t" + baseFilename
                    + _CPP + " \\\n " + "\t\t" + baseFilename + _H + " \n"
                    + "\t$(METRO_CXX) -c $(CXX_FLAGS) $(CXX_INCLUDES) \\\n"
                    + "\t\t" + baseFilename + _CPP + " \\\n" + "\t\t -o "
                    + baseFilename + _O + " \\\n" + "\t\t" + _compFlags
                    + "\n\n");
        }

        if (_debuggingMetamodel) {
            debuggerInfoBaseName = DebuggerInfoCodegen.getBaseName();
            _file.write(debuggerInfoBaseName + _O + ": \\\n" + "\t\t"
                    + debuggerInfoBaseName + _CPP + " \\\n " + "\t\t"
                    + debuggerInfoBaseName + _H + " \n"
                    + "\t$(METRO_CXX) -c $(CXX_FLAGS) $(MMDEBUG_INCLUDES) \\\n"
                    + "\t\t" + debuggerInfoBaseName + _CPP + " \\\n"
                    + "\t\t -o " + debuggerInfoBaseName + _O + " \\\n" + "\t\t"
                    + _compFlags + "\n\n");
        }

        _file.write("bin: \\\n ");

        for (int i = 0; i < _filelist.size(); i++) {
            String baseFilename = (String) _filelist.get(i);
            baseFilename = _fixWindowsPathname(baseFilename);

            _file.write("\t\t" + baseFilename + ".o" + " \\\n ");
        }

        if (_debuggingMetamodel) {
            _file.write("\t" + debuggerInfoBaseName + ".o" + " \\\n ");
        }

        _file.write("\t\tsc_main.o\n"
                + "\t $(METRO_CXX) $(CXX_FLAGS) $(CXX_INCLUDES) "
                + " $(LIBRARIES_PATH) -o " + _EXENAME + " \\\n");

        for (int i = 0; i < _filelist.size(); i++) {
            String baseFilename = (String) _filelist.get(i);
            baseFilename = _fixWindowsPathname(baseFilename);

            _file.write("\t\t" + baseFilename + ".o" + " \\\n");
        }

        if (_debuggingMetamodel) {
            _file.write("\t\t" + debuggerInfoBaseName + ".o" + " \\\n ");
        }

        _file.write("\t\tsc_main.o " + _linkArchives + " \\\n " + "\t\t"
                + _linkFlags + " \\\n " + "\t\t" + _librariesName + "\n"
                + "\nsc_main.o: ./sc_main.cpp\n"
                + "\t$(METRO_CXX) $(CXX_FLAGS) $(CXX_INCLUDES) -c "
                + "./sc_main.cpp " + _compFlags + " " + "\n");

        if (_debuggingMetamodel) {
            _file.write("\n" + "systemc_sim_clean:\n" + "\trm -f $(KRUFT)\n\n"
                    + "FORCE:\n\n");
        }
    }

    /**
     * Return a string containing the include files.
     *
     * @return The string containing the includes.
     */
    public String createIncludes() {
        String result = "";
        for (int i = 0; i < _filelist.size(); i++) {
            result += "#include \"" + _filelist.get(i) + ".h\"\n";
        }
        return result;
    }

    /**
     * Write the last lines of the makefile and close the file descriptor.
     *
     * @exception IOException
     */
    public void close() throws IOException {
        _file.write("\n\n# Get the rest of the rules\n"
                + "include $(ROOT)/mk/metrocommon.mk\n");
        _file.close();
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    private final String _EXENAME = "run.x";

    private final String _FILENAME = "systemc_sim.mk";

    private Vector _langFileList = new Vector();

    private String _compFlags = "$(COMPFLAGS)";

    private boolean _debuggingMetamodel = false;

    private FileWriter _file = null;

    private Vector _filelist = new Vector();

    private String _includes = "-I. $(INCDIR) " + "-I$(SYSTEMC_BACKEND) "
            + "-I$(SYSTEMC_BACKEND)/util " + "-I$(SYSTEMC_BACKEND)/ltl2ba-1.0 "
            + "-I$(SYSTEMC_BACKEND)/zchaff " + "-I$(SYSTEMC)/include";

    private String _librariesName = "$(LINKLIBS) -lsystemc -lm " + "-llibrary "
            + "-lutil " + "-lltl2ba -lsat $(LIBS)";

    private String _librariesPath = "-L. -L.. $(LIBDIR) "
            + "-L$(SYSTEMC_BACKEND)/util " + "-L$(SYSTEMC_BACKEND)/ltl2ba-1.0 "
            + "-L$(SYSTEMC_BACKEND)/zchaff " + "-L$(SYSTEMC_BACKEND) "
            + "-L$(SYSTEMC)/$(SYSTEMC_LIB)";

    private String _linkArchives = "$(ARCHIVES)";

    private String _linkFlags = "$(LINKFLAGS)";

    private String _mmdebugIncludes = "-I. -I$(SYSTEMC_BACKEND) "
            + "-I$(SYSTEMC)/include";

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    private String _fixWindowsPathname(String pathname) {
        // Convert any backslashes to forward slashes:
        String fixedName = pathname.replace('\\', '/');
        // Escape any embedded spaces:
        fixedName = fixedName.replaceAll(" ", "\\\\ ");
        return fixedName;
    }
}
