/* A class for generating source code files to be linked into the user's
 application for metamodel debugging.

 Metropolis: Design Environment for Heterogeneus Systems.

 Copyright (c) 2004-2005 The Regents of the University of California.
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

package metropolis.metamodel.backends.systemc.mmdebug;

import java.io.FileWriter;
import java.io.IOException;

// ////////////////////////////////////////////////////////////////////////
// // DebuggerInfoCodegen
/**
 * A class for generating source code files to be linked into the user's
 * application for metamodel debugging.
 * <p>
 *
 * @author Allen Hopkins
 * @version $Id: DebuggerInfoCodegen.java,v 1.19 2006/10/12 20:33:20 cxh Exp $
 */
public class DebuggerInfoCodegen {

    // //////////////////////////////////////////////////////////////////////
    // // public methods

    /**
     * Create the .cpp file.
     *
     * @exception IOException
     *                If the .cpp file cannot be written.
     */
    public void createCPPFile() throws IOException {
        FileWriter cppFile = new FileWriter(_BASENAME + ".cpp");
        cppFile.write("#include \"debuggerinfo.h\"\n");
        cppFile.write("#include \"process.h\"\n");
        cppFile.write("#include \"global.h\"\n");

        cppFile.write("strList_t       gAllCurrentLines;\n");
        cppFile.write("strList_t       gAllEventTags;\n");
        cppFile.write("strList_t       gAllNextLines;\n");
        cppFile.write("strList_t       gAllSchedStates;\n");
        cppFile.write("String*         gCurrentLine;\n");
        cppFile.write("ProgramCounter* gCurrentPC;\n");
        cppFile.write("String*         gEventTag;\n");
        cppFile.write("int             gMGDBCounter;\n");
        cppFile.write("int             gMGDBDummyVar;\n");
        cppFile.write("scoreboard*     gMmdbSB;\n");
        cppFile.write("String*         gNextLine;\n");
        cppFile.write("strList_t*      gPCNextLines;\n");
        cppFile.write("String*         gPCState;\n");
        cppFile.write("const char*     gProcessName;\n");
        cppFile.write("bool            gDontBreakInAwaitTest = true;\n");
        cppFile.write("bool            gInAwaitTest;\n");
        cppFile.write("bool            gAtMethodEnd;\n");

        cppFile.write("\n");
        cppFile.write("void ");
        cppFile.write(_CLASSNAME);
        cppFile.write("::processSwitched(");
        cppFile.write("ProgramCounter* pc) {\n");
        cppFile.write("    gInAwaitTest = pc->inAwaitTest();\n");
        cppFile.write("    if ((pc != gCurrentPC)\n");
        cppFile.write("        && (!gInAwaitTest || ");
        cppFile.write("!gDontBreakInAwaitTest)){\n");
        cppFile.write("        updateGlobals(pc);\n");
        cppFile.write("        gCurrentPC = pc;\n");
        cppFile.write("        processSwitchBreakpoint(pc);\n");
        cppFile.write("    }\n");
        cppFile.write("}\n\n");
        cppFile.write("void ");
        cppFile.write(_CLASSNAME);
        cppFile.write("::updateGlobals(");
        cppFile.write("ProgramCounter* pc) {\n");
        cppFile.write("    gProcessName = pc->p->name();\n");
        cppFile.write("    gMmdbSB = pc->getScoreboard();\n");
        cppFile.write("    gAllCurrentLines =gMmdbSB->getAllCurrentLines();\n");
        cppFile.write("    gAllEventTags = gMmdbSB->getAllEventTags();\n");
        cppFile.write("    gAllSchedStates = gMmdbSB->getAllSchedStates();\n");
        cppFile.write("    if (gAllSchedStates != (strList_t)0) {\n");
        cppFile.write("        // Here just to compile this feature for gdb\n");
        cppFile.write("        gAllSchedStates.at(0) = gAllSchedStates[0];\n");
        cppFile.write("    }\n");
        cppFile.write("    gAtMethodEnd = ");
        cppFile.write("(pc->funcType == FunctionType::INTFCFUNC\n");
        cppFile.write("                   ");
        cppFile.write(" && pc->eventTag == ProgramCounter::END_TAG);\n");
        cppFile.write("}\n");
        cppFile.write("void DebuggerInfo::processSwitchBreakpoint(");
        cppFile.write("ProgramCounter* pc) {\n");
        cppFile.write("    int dummy = 0;\n");
        cppFile.write("}\n");
        cppFile.close();
    }

    /**
     * Create the .h file.
     *
     * @exception IOException
     *                If the .h file cannot be written.
     */
    public void createHeaderFile() throws IOException {
        FileWriter headerFile = new FileWriter(_BASENAME + ".h");

        headerFile.write("#ifndef DEBUGGER_INFO_H\n");
        headerFile.write("#define DEBUGGER_INFO_H\n");
        headerFile.write("#include \"programcounter.h\"\n");
        headerFile.write("\n");
        headerFile.write("extern strList_t       gAllCurrentLines;\n");
        headerFile.write("extern strList_t       gAllEventTags;\n");
        headerFile.write("extern strList_t       gAllNextLines;\n");
        headerFile.write("extern strList_t       gAllSchedStates;\n");
        headerFile.write("extern String*         gCurrentLine;\n");
        headerFile.write("extern ProgramCounter* gCurrentPC;\n");
        headerFile.write("extern String*         gEventTag;\n");
        headerFile.write("extern int             gMGDBCounter;\n");
        headerFile.write("extern int             gMGDBDummyVar;\n");
        headerFile.write("extern scoreboard*     gMmdbSB;\n");
        headerFile.write("extern String*         gNextLine;\n");
        headerFile.write("extern strList_t*      gPCNextLines;\n");
        headerFile.write("extern String*         gPCState;\n");
        headerFile.write("extern const char*     gProcessName;\n");
        headerFile.write("class " + _CLASSNAME + " {\n");
        headerFile.write("public:\n");
        headerFile.write(" static void processSwitched(ProgramCounter* pc);\n");
        headerFile.write(" static void updateGlobals(ProgramCounter* pc);\n");
        headerFile.write(" static void processSwitchBreakpoint(");
        headerFile.write("ProgramCounter* pc);\n");
        headerFile.write("};\n");
        headerFile.write("#endif\n");
        headerFile.close();
    }

    /**
     * Return the basename of the files that this class creates.
     *
     * @return the basename of the files that this class creates.
     */
    public static String getBaseName() {
        return _BASENAME;
    }

    // //////////////////////////////////////////////////////////////////////
    // // private members

    private final static String _BASENAME = "debuggerinfo";

    private final static String _CLASSNAME = "DebuggerInfo";
}
