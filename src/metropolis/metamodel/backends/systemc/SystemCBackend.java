/*

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

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.StringManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.elaborator.CustomClassLoader;
import metropolis.metamodel.backends.elaborator.CustomObjectInputStream;
import metropolis.metamodel.backends.elaborator.ElaboratorBackend;
import metropolis.metamodel.backends.systemc.mmdebug.BreakpointVisitor;
import metropolis.metamodel.backends.systemc.mmdebug.DebuggerInfoCodegen;
import metropolis.metamodel.backends.systemc.mmdebug.NextLineVisitor;
import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.runtime.BuiltInLOC;
import metropolis.metamodel.runtime.Connection;
import metropolis.metamodel.runtime.Constraint;
import metropolis.metamodel.runtime.EqualVars;
import metropolis.metamodel.runtime.Event;
import metropolis.metamodel.runtime.INetlist;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.IPort;
import metropolis.metamodel.runtime.IPortArray;
import metropolis.metamodel.runtime.IPortElem;
import metropolis.metamodel.runtime.IPortScalar;
import metropolis.metamodel.runtime.LTLSynch;
import metropolis.metamodel.runtime.LTLSynchImply;
import metropolis.metamodel.runtime.MMType;
import metropolis.metamodel.runtime.Network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

// ////////////////////////////////////////////////////////////////////////
// // SystemCBackend
/**
 * A back-end that generates SystemC code from an AST in memory. The
 * SystemCCodegenVisitor is used to generate this code.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Guang Yang, Robert Clariso
 * @version $Id: SystemCBackend.java,v 1.132 2006/10/12 20:33:13 cxh Exp $
 */
public class SystemCBackend extends ElaboratorBackend {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Create a new SystemC back-end. */
    public SystemCBackend() {
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
        List debuggerBreakpoints = null;

        // First check for "-mmdebug" option in args list, and consume it.
        int argIndex = 0;
        boolean _elaborated = false;
        while (argIndex < args.size()) {
            String arg = (String) args.get(argIndex);
            if (arg.equals("-mmdebug")) {
                _debuggingMetamodel = true;
                _ic = false;
                args.remove(argIndex);
                continue;
            } else if (arg.equals("-w")) {
                _overwrite = true;
                args.remove(argIndex);
                continue;
            } else if (arg.equals("-noic")) {
                _ic = false;
                args.remove(argIndex);
                continue;
            } else if (arg.equals("-elaborated")) { // for metroshell only
                _elaborated = true;
                args.remove(argIndex);
                continue;
            }
            argIndex++;
        }

        // Check arguments
        int size = args.size();

        if (size > 5) {
            throw new RuntimeException("Wrong number of arguments, number "
                    + "of arguments was " + size + ", which is greater "
                    + "than 5");
        }

        // if (sources.size() < 3)
        // throw new RuntimeException("Wrong number of AST groups");

        // Generate code for each source file
        /***********************************************************************
         * If all ASTs in all packages are grouped and passed from frontend, use
         * this segment of code. LinkedList srcs = (LinkedList) sources.get(0);
         * for (int i=3; i <sources.size(); i++) { Iterator elem =
         * ((LinkedList)sources.get(i)).iterator(); while (elem.hasNext())
         * srcs.addLast(elem.next()); } Iterator iter = srcs.iterator();
         **********************************************************************/

        // Get the list of all sources loaded by the FileLoader
        // All sources should be loaded until pass 2, so that they
        // are translated by the elaboration backend
        allSources = FileLoader.getCompiledSources(1);
        Iterator iter = allSources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            if (pkg == MetaModelLibrary.UTIL_PACKAGE)
                continue;
            String fileName = (String) ast.getProperty(IDENT_KEY);
            FileLoader.loadCompileUnit(fileName, 2);
        }

        allSources = FileLoader.getCompiledSources(2);
        iter = allSources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            if (pkg == MetaModelLibrary.UTIL_PACKAGE)
                iter.remove();
        }

        boolean sc_main_gen_ready = false;

        // Generate top level netlist
        if (size == 1 || size == 3 || size == 5) {
            System.out.println(args + " is doing elaboration.");

            // Make sure that the libraries have been loaded until pass 2
            iter = MetaModelLibrary.LANG_PACKAGE.getUserTypes();
            while (iter.hasNext()) {
                ObjectDecl decl = (ObjectDecl) iter.next();
                FileLoader.loadCompileUnit(decl.getName(),
                        MetaModelLibrary.LANG_PACKAGE, 2);
            }

            // Get the list of all sources loaded by the FileLoader
            // List allSources = FileLoader.getCompiledSources(2);

            // move the netinitiator to the end of args
            if (size == 3) {
                if (!args.get(0).equals("-java")
                        && !args.get(0).equals("-javac")) {

                    Object netinit = args.remove(0);
                    args.add(netinit);
                }
            } else if (size == 5) {
                if (!args.get(0).equals("-java")
                        && !args.get(0).equals("-javac")) {

                    Object netinit = args.remove(0);
                    args.add(netinit);
                }
                if (args.get(0).equals("-java") && args.get(2).equals("-javac")) {

                    Object interp = args.remove(3);
                    args.remove(2);
                    args.add(0, interp);
                    args.add(0, "-javac");
                } else if (args.get(0).equals("-javac")
                        && args.get(2).equals("-java")) {
                    // this is the correct case
                } else {
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < 5; i++)
                        buf.append("i=" + i + " obj=" + args.get(i) + "\n");
                    throw new RuntimeException(
                            "Wrong arguments -java and -javac: \n"
                                    + buf.toString());
                }
            }

            // If we can an exception, throw it.
            super.invoke(args, allSources);

            if (_tmpRoot == null) {
                throw new RuntimeException("ElaboratorBackend._tmpRoot == null"
                        + ", probably ElaboratorBackend._createTmpDir() was "
                        + "not called?");

            }

            String inputFileName = _tmpRoot.toString() + File.separator + "NET";
            try {
                CustomClassLoader loader = new CustomClassLoader(_tmpRoot
                        .toString());
                FileInputStream in = new FileInputStream(inputFileName);
                try {
                    CustomObjectInputStream ois = new CustomObjectInputStream(
                            in, loader);
                    net = Network.restore(ois);
                    Event.eventCache = net.getEventCache();
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to restore network "
                            + "using a CustomObjectInputStream based on '"
                            + inputFileName + "'", ex);
                } finally {
                    in.close();
                }
                // net.flatten();
            } catch (Exception ex) {
                throw new RuntimeException(
                        "Error reading the elaborated network '"
                                + inputFileName + "'", ex);
            }
        } else if (_elaborated) {
            net = Network.net;
        }

        // collect synch event info into _synchEventPool:
        if (net != null) {
            collect_constraints(net.getNetInitiator());

            process_synch_constraints();

            annotate_synch_events();

            annotate_synchimply_events();

            annotate_builtinLOC_events();

            annotate_constrained_events();

            if (_ic)
                simplify_ic_awaits(net.getNetInitiator());

            annotate_referred_interfaces(net.getNetInitiator());

            sc_main_gen_ready = true;
        } else {
            throw new RuntimeException(
                    "Network is not correctly elaborated. Abort.");
        }

        MakefileCodegen mkcgen = null;
        try {
            System.out.println("  Generating SystemC code...");
            mkcgen = new MakefileCodegen(_debuggingMetamodel);

            if (_debuggingMetamodel) {
                // This List is to be added to by visits to each CompileUnit
                debuggerBreakpoints = new Vector();
                _addDebuggingInfo(allSources, debuggerBreakpoints);
                DebuggerInfoCodegen dbigen = new DebuggerInfoCodegen();
                dbigen.createHeaderFile();
                dbigen.createCPPFile();

                // Now gets added explicitly in MakefileCodegen.
                // mkcgen.add(DebuggerInfoCodegen.getBaseName());
            }

            iter = allSources.iterator();

            while (iter.hasNext()) {
                CompileUnitNode ast = (CompileUnitNode) iter.next();
                _emitCode(ast, mkcgen);
            }

            if (_debuggingMetamodel) {
                _emitDebuggerBreakpoints(debuggerBreakpoints);
            }

            // Generate the header before calling _compileUnits.save()
            // so that if save() throws an exception, we have a well
            // formed makefile. If we do not do this, then "make clean"
            // fails.

            mkcgen.header();

            _compileUnits.setGeneralIncludes(mkcgen.createIncludes());
            _compileUnits.save(_overwrite);

            mkcgen.body();
        } catch (Exception ex) {
            if (mkcgen != null) {
                try {
                    mkcgen.deleteIfEmpty();
                } catch (IOException ex2) {
                    System.err.println("Failed to delete empty makefile?");
                    ex2.printStackTrace();
                }
            }
            throw new RuntimeException(
                    "Problem creating SystemC code or makefile \""
                            + mkcgen.getFileName() + "\".", ex);
        } finally {
            if (mkcgen != null) {
                try {
                    mkcgen.close();
                } catch (Exception ex3) {
                    throw new RuntimeException("Problem closing makefile "
                            + mkcgen.getFileName(), ex3);
                }
            }
        }

        if (sc_main_gen_ready) {
            // System.out.println(net.show());
            try {
                generate_sc_main();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Can not access class member.", ex);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Network restored from deserialization. */
    protected Network net = null;

    /** a file identifier for sc_main.cpp generation. */
    protected FileWriter target = null;

    /** Objects that already initialized. */
    protected Hashtable instName = new Hashtable();

    /** Process instances created in the system. */
    protected LinkedList process = new LinkedList();

    /** Media instances created in the system. */
    protected LinkedList medium = new LinkedList();

    /** state media instances created in the system. */
    protected LinkedList smedium = new LinkedList();

    /** scheduler instances created in the system. */
    protected LinkedList scheduler = new LinkedList();

    /** quantity instances created in the system. */
    protected LinkedList quantity = new LinkedList();

    /**
     * All the synch info stored in a SynchEventPool.
     */
    protected SynchEventPool _synchEventPool = new SynchEventPool();

    /**
     * Key: INode Value: a list of port-interface pairs used in test lists and
     * set lists in awaits.
     */
    protected Hashtable _portInterfaces = new Hashtable();

    /**
     * Store all CompileUnitNodes that have been processed.
     */
    protected CompileUnitsCache _compileUnits = new CompileUnitsCache();

    /**
     * A list of LOC constraints.
     */
    protected LinkedList _LOCConstraints = new LinkedList();

    /**
     * A list of built-in LOC constraints.
     */
    protected LinkedList _BILOCConstraints = new LinkedList();

    /**
     * A list of ELOC constraints.
     */
    protected LinkedList _ELOCConstraints = new LinkedList();

    /**
     * A list of LTL constraints.
     */
    protected LinkedList _LTLConstraints = new LinkedList();

    /**
     * A list of LTL Synch constraints.
     */
    protected LinkedList _LTLSConstraints = new LinkedList();

    /**
     * A list of LTL Synch Imply constraints.
     */
    protected LinkedList _LTLSIConstraints = new LinkedList();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Test whether a type is a primitive type.
     *
     * @param type
     *            The name of the type.
     * @return whether type is primitive type or not
     */
    protected static boolean _isPrimitiveType(String type) {
        if (type.equals("boolean") || type.equals("byte")
                || type.equals("char") || type.equals("short")
                || type.equals("int") || type.equals("long")
                || type.equals("float") || type.equals("double")
                || type.equals("void"))
            return true;
        else
            return false;
    }

    /**
     * Convert from MMM template name format to c++ template format. e.g.
     * proc1_$int_float$ is converted into proc1<TT>&lt;int, float&gt;</TT>.
     *
     * @param objName
     *            The object name in MMM template format. It could be a name
     *            returned by getName() method of a Class instance.
     * @return the AST node or null if not found
     */
    protected static String _convertToCppTemplateFormat(String objName) {
        String name = objName;
        while (name.charAt(name.length() - 1) == ';')
            name = name.substring(0, name.length() - 1);
        int begPos = name.indexOf("_$");
        if (begPos == -1)
            return name;
        int endPos = name.indexOf('$', begPos + 2);
        if (endPos == -1 || name.indexOf('$', endPos + 1) != -1
                || endPos != name.length() - 1)
            throw new RuntimeException("Wrong template class name '" + name
                    + "'.");
        String tmpl = name.substring(begPos + 2, endPos);
        tmpl = tmpl.replaceAll("array_of_", "*");
        int i;
        String dest = "";
        String sep = "";
        StringTokenizer t = new StringTokenizer(tmpl, "_");
        while (t.hasMoreTokens()) {
            String s = (String) t.nextToken();
            for (i = 0; i < s.length(); i++)
                if (s.charAt(i) != '*')
                    break;
            String basetype = s.substring(i);
            if (!_isPrimitiveType(basetype))
                basetype += "*";
            dest += (sep + basetype + s.substring(0, i));
            sep = ", ";
        }
        tmpl = name.substring(0, begPos) + "<" + dest + ">";
        return tmpl;
    }

    /**
     * Get the name of the target file where the source will be stored.
     *
     * @param ast
     *            AST obtained from the source file.
     * @return return the target name.
     */
    protected String _getTargetName(CompileUnitNode ast) {
        String filename = (String) ast.getProperty(IDENT_KEY);
        String targetName = StringManip.partBeforeLast(filename, '.');
        return targetName;
    }

    /**
     * Get the name of the mmm file.
     *
     * @param ast
     *            AST obtained from the source file.
     * @return return the MMM file name.
     */
    protected String _getMMMFileName(CompileUnitNode ast) {
        String filename = (String) ast.getProperty(IDENT_KEY);
        return filename;
    }

    /**
     * Generate code from an AST and print it to a file. The generation of the
     * code is performed by a call to SystemCCodegenVisitor.
     *
     * @param ast
     *            Compiled AST.
     * @param mkcgen
     *            Makefile generation file.
     */
    protected void _emitCode(CompileUnitNode ast, MakefileCodegen mkcgen) {
        ast.accept(new ExplicitCastVisitor(), null);

        PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
        boolean includingDebugCode = _debuggingMetamodel
                && !pkg.getName().equals("lang");

        // xichen_loc_beg
        LinkedList args = new LinkedList();
        args.addLast(net);
        SystemCCodegenVisitor sccgen = new SystemCCodegenVisitor(
                includingDebugCode, _ic);
        CompileUnit compileUnit = (CompileUnit) ast.accept(sccgen, args);
        compileUnit.setHasDebuggerInfo(includingDebugCode);

        // CompileUnit compileUnit =
        // (CompileUnit)ast.accept(
        // new SystemCCodegenVisitor(_debuggingMetamodel), null);
        // xichen_loc_end

        String targetName = _getTargetName(ast);
        compileUnit.setFilename(targetName);

        String mmmFileName = _getMMMFileName(ast);
        compileUnit.setMMMFilename(mmmFileName);

        compileUnit.setHasTemplates(sccgen.hasTemplates());

        if (!pkg.getName().equals("lang")) {
            mkcgen.add(targetName);
        } else {
            if (_debuggingMetamodel) {
                mkcgen.addToLibFiles(targetName);
            }
        }

        _compileUnits.add(compileUnit);
    }

    /**
     * Back annotate AST with the information gained from elaboration including
     * annotation of events which are referred to by synch statements.
     */
    protected void annotate_synch_events() {
        ASTLookupVisitor alv = new ASTLookupVisitor();
        LinkedList args = new LinkedList();
        args.addLast(null);
        args.addLast(null);

        Iterator iter = _synchEventPool.getSynchEventsOfObjects();
        while (iter.hasNext()) {
            HashSet seHashSet = (HashSet) iter.next();
            Iterator hsIter = seHashSet.iterator();

            LinkedList ASTOfThisGroup = new LinkedList();
            while (hsIter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) hsIter.next();
                Event e = se.getEvent();
                String eventName = e.getName();
                MMType mtype = e.getNodeObject().getType();
                String fullName = mtype.getName();
                args.set(0, fullName);
                args.set(1, eventName);

                // Find AST of an event by the fully qualified name
                // of the object and the name of the event
                TreeNode eventAST = null;
                Iterator asts = allSources.iterator();
                CompileUnitNode ast = null;
                int astIndex = 0;
                boolean getFromHistory;

                while (asts.hasNext() && eventAST == null) {
                    if (ASTOfThisGroup.size() > astIndex) {
                        ast = (CompileUnitNode) ASTOfThisGroup.get(astIndex);
                        astIndex++;
                        getFromHistory = true;
                    } else {
                        ast = (CompileUnitNode) asts.next();
                        getFromHistory = false;
                    }
                    eventAST = (TreeNode) ast.accept(alv, args);
                    if (eventAST instanceof TypeNameNode) {
                        mtype = mtype.getSuperClass();
                        args.set(0, mtype.getName());
                        asts = allSources.iterator();
                        eventAST = null;
                        if (!getFromHistory) {
                            ASTOfThisGroup.addLast(ast);
                            astIndex++;
                        }
                    } else if (eventAST instanceof AbsentTreeNode) {
                        throw new RuntimeException(
                                "Can not find in synch constraint the event "
                                        + "with name " + eventName
                                        + " in object " + fullName);
                    } else if (eventAST != null) {
                        if (!getFromHistory) {
                            ASTOfThisGroup.addLast(ast);
                            astIndex++;
                        }
                    }
                } // end while
                if (eventAST == null)
                    throw new RuntimeException(
                            "Can not find in synch constraint the event "
                                    + "with name " + eventName + " in object "
                                    + fullName);

                // Annotate synched event
                LinkedList eid = (LinkedList) eventAST
                        .getProperty(LTLSYNCHEVENT_KEY);
                if (eid == null)
                    eid = new LinkedList();
                eid.addLast(se);
                eventAST.setProperty(LTLSYNCHEVENT_KEY, eid);
            } // end while
        } // end while
    }

    /**
     * Back annotate AST with the information gained from elaboration including
     * annotation of events which are referred to by synch imply statements.
     */
    protected void annotate_synchimply_events() {
        //ASTLookupVisitor alv = new ASTLookupVisitor();
        LinkedList args = new LinkedList();
        args.addLast(null);
        args.addLast(null);

        for (int ltlsi = 0; ltlsi < _LTLSIConstraints.size(); ltlsi++) {
            LTLSynchImply ltl = (LTLSynchImply) _LTLSIConstraints.get(ltlsi);

            // Annotate event information
            for (int i = 0; i < 2; i++) {
                LinkedList events = (i == 0) ? ltl.getLeftEvents() : ltl
                        .getRightEvents();
                for (int j = 0; j < events.size(); j++) {
                    Event e = (Event) events.get(j);
                    TreeNode eventAST = _findEventAST(e);

                    // Annotate event
                    Hashtable hs = (Hashtable) eventAST
                            .getProperty(LTLSYNCHIMPLYEVENT_KEY);
                    if (hs == null) {
                        hs = new Hashtable();
                        eventAST.setProperty(LTLSYNCHIMPLYEVENT_KEY, hs);
                    }
                    LinkedList eid = (LinkedList) hs.get(e);
                    if (eid == null) {
                        eid = new LinkedList();
                        hs.put(e, eid);
                    }
                }// end for j
            }// end for i

            // Annotate information of variables in equality comparison parts
            LinkedList eqvars = ltl.getEqualVars();
            for (int eqvi = 0; eqvi < eqvars.size(); eqvi++) {
                EqualVars eqv = (EqualVars) eqvars.get(eqvi);
                for (int ei = 0; ei < 2; ei++) {
                    int type = (ei == 0) ? eqv.getType1() : eqv.getType2();
                    if (type != EqualVars.VARTYPE)
                        continue;
                    Event e = (ei == 0) ? eqv.getEvent1() : eqv.getEvent2();
                    String value = (ei == 0) ? eqv.getValue1() : eqv
                            .getValue2();
                    TreeNode eventAST = _findEventAST(e);

                    // Annotate event
                    Hashtable hs = (Hashtable) eventAST
                            .getProperty(LTLSYNCHIMPLYEVENT_KEY);
                    if (hs == null) {
                        hs = new Hashtable();
                        eventAST.setProperty(LTLSYNCHIMPLYEVENT_KEY, hs);
                    }
                    LinkedList eid = (LinkedList) hs.get(e);
                    if (eid == null) {
                        eid = new LinkedList();
                        hs.put(e, eid);
                    }
                    LinkedList p = new LinkedList();
                    p.addLast(value);
                    p.addLast(new Integer(ltlsi));
                    p.addLast(new Integer(eqvi));
                    p.addLast(new Integer(ei));
                    eid.addLast(p);
                }// end for ei
            }// end for eqvi
        }// end for ltlsi
    }

    /**
     * Back annotate AST with the information gained from elaboration including
     * annotation of events which are referred to by constraints.
     */
    protected void annotate_constrained_events() {
        Integer properties[] = { LTLEVENT_KEY, LOCEVENT_KEY, ELOCEVENT_KEY };
        //int used[] = { Constraint.LTL, Constraint.LOC, Constraint.ELOC };
        LinkedList constraints[] = { _LTLConstraints, _LOCConstraints,
                _ELOCConstraints };
        //ASTLookupVisitor alv = new ASTLookupVisitor();
        LinkedList args = new LinkedList();
        args.addLast(null);
        args.addLast(null);

        for (int kind = 0; kind < constraints.length; kind++) {
            Set events = new HashSet();
            Iterator iter = constraints[kind].iterator();
            while (iter.hasNext()) {
                Constraint cstr = (Constraint) iter.next();
                Iterator event = cstr.getEvents().iterator();
                while (event.hasNext()) {
                    events.add(event.next());
                }// end while event
            }// end while iter

            Iterator eventsIter = events.iterator();
            while (eventsIter.hasNext()) {

                Event e = (Event) eventsIter.next();
                TreeNode eventAST = _findEventAST(e);

                // Annotate event
                LinkedList eid = (LinkedList) eventAST
                        .getProperty(properties[kind]);
                if (eid == null)
                    eid = new LinkedList();
                eid.add(e);
                eventAST.setProperty(properties[kind], eid);
            } // end while
        }// end for kind
    }

    /**
     * Back annotate AST with the information gained from elaboration including
     * annotation of events which are referred to by built-in LOC constraints.
     */
    protected void annotate_builtinLOC_events() {
        //ASTLookupVisitor alv = new ASTLookupVisitor();
        LinkedList args = new LinkedList();
        args.addLast(null);
        args.addLast(null);

        for (int biloc = 0; biloc < _BILOCConstraints.size(); biloc++) {
            BuiltInLOC loc = (BuiltInLOC) _BILOCConstraints.get(biloc);

            Event e1 = null;
            Event e2 = null;

            switch (loc.getKind()) {
            case Constraint.MAXDELTA:
            case Constraint.MINDELTA:
                e1 = (Event) loc.getEvents().get(0);
                e2 = (Event) loc.getEvents().get(1);
                if (e1.getProcess() != e2.getProcess())
                    throw new RuntimeException(
                            "Unsupported built-in LOC constraint. "
                                    + "The two events constrained by maxdelta/mindelta "
                                    + "must belong to the same process.\n"
                                    + loc.toString());
                break;
            case Constraint.MAXRATE:
            case Constraint.MINRATE:
            case Constraint.PERIOD:
                e1 = (Event) loc.getEvents().getFirst();
                break;
            default:
                throw new RuntimeException("Unexpected Built-in LOC type.\n"
                        + loc.toString());
            }

            // Annotate event information
            if (e1 != null) {
                TreeNode eventAST = _findEventAST(e1);

                // Annotate event
                Hashtable hs = (Hashtable) eventAST
                        .getProperty(BUILTINLOCEVENT_KEY);
                if (hs == null) {
                    hs = new Hashtable();
                    eventAST.setProperty(BUILTINLOCEVENT_KEY, hs);
                }
                LinkedList eid = (LinkedList) hs.get(e1);
                if (eid == null) {
                    eid = new LinkedList();
                    hs.put(e1, eid);
                }
                eid.addLast(loc);
            }

            if (e2 != null) {
                TreeNode eventAST = _findEventAST(e2);

                // Annotate event
                Hashtable hs = (Hashtable) eventAST
                        .getProperty(BUILTINLOCEVENT_KEY);
                if (hs == null) {
                    hs = new Hashtable();
                    eventAST.setProperty(BUILTINLOCEVENT_KEY, hs);
                }
                LinkedList eid = (LinkedList) hs.get(e2);
                if (eid == null) {
                    eid = new LinkedList();
                    hs.put(e2, eid);
                }
                eid.addLast(loc);
            }
        }// end for biloc
    }

    /**
     * Generate system structure in sc_main.
     *
     * @exception IllegalAccessException
     */
    protected void generate_sc_main() throws IllegalAccessException {
        LinkedList code = new LinkedList();
        try {
            String targetName = "sc_main.cpp";
            target = new FileWriter(targetName);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open sc_main.cpp", ex);
        }
        code.addLast("#include \"library.h\"\n");
        code.addLast(_compileUnits.getGeneralIncludes());

        _flush_code(code);
        code = new LinkedList();

        // Generate global variables to check equal vars in synch
        generate_synch_equalvars();

        // Generate manager and scoreboard instances
        code.addLast("\nmanager _mng(\"Manager\");\n");
        code.addLast("scoreboard _sb(\"ScoreBoard\");\n\n");

        // Generate sc_main()
        // NOTE: Do not use __argc or __argv here as MINGW redefines these.
        code.addLast("int sc_main(int metro_argc, char * metro_argv[])\n");
        code.addLast("{\n");

        code.addLast("  argc = metro_argc;\n");
        code.addLast("  argv = metro_argv;\n\n");

        // xichen_loc_beg
        code.addLast("  trace_file.open(trace_file_name, ofstream::out);\n");
        // xichen_loc_end

        code.addLast("  //Connect manager to scoreboard\n");
        code.addLast("  _mng.cntl(_sb);\n");
        code.addLast("  _mng.pc->setScoreboard(&_sb);\n");
        code.addLast("  if (_sb.processArgs()) return 0;\n\n");

        code.addLast("  //Arguments to reconstruct user's objects\n");
        code.addLast("  DUMMY_CTOR_ARG _arg;\n");
        code.addLast("  int * index = NULL;\n\n");

        Class cls = net.getNetInitiator().getUserObject().getClass();
        String nlName = StringManip.partAfterLast(
                _convertToCppTemplateFormat(cls.getName()), '.');
        code.addLast("  "
                + nlName
                + " *top_level_netlist = new "
                + StringManip.partAfterLast(_convertToCppTemplateFormat(cls
                        .getName()), '.'));
        code.addLast("(\"top_level_netlist\");\n");
        code.addLast("  _sb.topNetlist = top_level_netlist;\n");
        code
                .addLast("  if (top_level_netlist->type == SchedulingNetlistType) {\n");
        code
                .addLast("    cout<<\"A scheduling netlist can not be simulated alone as the top level netlist.\"<<endl;\n");
        code.addLast("    sc_sim_stop();\n");
        code.addLast("  }\n");

        _flush_code(code);
        code = new LinkedList();

        code.addLast(instantiate_objects(net.getNetInitiator()));
        _flush_code(code);
        code = new LinkedList();

        code.addLast(initialize_netlist(net.getNetInitiator().getUserObject(),
                net.getNetInitiator().getName()));
        _flush_code(code);
        code = new LinkedList();

        connect_objects();

        register_timed_processes();

        setup_synch_events_info();

        setup_synch_event_groupid_correspondence();

        setup_synchimply_events_info();

        setup_ltl_constraints();

        setup_builtin_loc_constraints();

        code.addLast("\n  //Start simulation\n");
        code.addLast("  sc_start();\n");

        // xichen_loc_beg
        code.addLast("  //Simulation end\n");
        code.addLast("  trace_file.close();\n");
        // xichen_loc_end

        code.addLast("  return 0;\n");
        code.addLast("}\n\n");
        _flush_code(code);
        code = new LinkedList();

        compare_equalvars_in_synch();

        try {
            target.close();
        } catch (IOException ex) {
            throw new RuntimeException("Problem closing target", ex);
        }
    }

    /**
     * Instantiate objects in the system.
     *
     * @param netlist
     *            netlist instance in the network
     * @return the generated code
     * @exception IllegalAccessException
     */
    protected LinkedList instantiate_objects(INetlist netlist)
            throws IllegalAccessException {
        LinkedList code = new LinkedList();
        String nname = netlist.getName();
        Iterator iter = netlist.getComponents();

        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String iname = node.getName();
            String cname = node.getCompName(netlist);

            if ((node.isRefined() && node.getRefinement() != netlist)
                    || node.getType().getKind() == MMType.NETLIST) {
                INetlist ref;
                if (node.isRefined())
                    ref = node.getRefinement();
                else
                    ref = (INetlist) node;
                Object obj = ref.getUserObject();
                Class cls = obj.getClass();
                String nlName = StringManip.partAfterLast(
                        _convertToCppTemplateFormat(cls.getName()), '.');

                if (!instName.containsKey(obj)) {
                    instName.put(obj, ref.getName());
                    code.addLast("  " + nlName + " *" + ref.getName()
                            + " = new " + nlName);
                    code.addLast("(\"" + ref.getCompName(netlist) + "\");\n");
                    code.addLast("  " + nname + "->registerNetlist(\"");
                    code.addLast(ref.getCompName(netlist) + "\", "
                            + ref.getName() + ");\n\n");
                    _flush_code(code);
                    code = new LinkedList();
                    code.addLast(instantiate_objects(ref));
                    code.addLast(initialize_netlist(ref.getUserObject(), ref
                            .getName()));
                } else if (!ref.getName().equals(instName.get(obj))) {
                    code.addLast("  " + nlName + " *" + ref.getName() + " = ");
                    code.addLast(instName.get(obj));
                    code.addLast(";\n");
                    code.addLast("  " + nname + "->registerNetlist(\"");
                    code.addLast(ref.getCompName(netlist) + "\", "
                            + ref.getName() + ");\n\n");
                }
                _flush_code(code);
                code = new LinkedList();
            } else {
                if (node.isRefined())
                    continue; // Fix this, not sure about it.
                switch (node.getType().getKind()) {
                case MMType.PROCESS:
                    process.addLast(node);
                    code.addLast(initialize(node.getUserObject(), iname));
                    code.addLast("  " + nname + "->registerProcess(\"");
                    code.addLast(cname + "\", " + iname + ");\n");
                    code.addLast("  " + iname + "->_id = " + node.getObjectID()
                            + ";\n");
                    code.addLast("  _sb.registerProcess(" + iname + "->pc);\n");
                    break;
                case MMType.MEDIUM:
                    medium.addLast(node);
                    code.addLast(initialize(node.getUserObject(), iname));
                    code.addLast("  " + nname + "->registerMedium(\"");
                    code.addLast(cname + "\", " + iname + ");\n\n");
                    code.addLast("  " + iname + "->_id = " + node.getObjectID()
                            + ";\n");
                    code.addLast("  _sb.registerMedium(" + iname + ");\n");
                    break;
                case MMType.STATEMEDIUM:
                    smedium.addLast(node);
                    code.addLast(initialize(node.getUserObject(), iname));
                    code.addLast("  " + nname + "->registerStatemedium(\"");
                    code.addLast(cname + "\", " + iname + ");\n\n");
                    break;
                case MMType.SCHEDULER:
                    scheduler.addLast(node);
                    code.addLast(initialize(node.getUserObject(), iname));
                    code.addLast("  " + nname + "->registerScheduler(\"");
                    code.addLast(cname + "\", " + iname + ");\n\n");
                    break;
                case MMType.QUANTITY:
                    quantity.addLast(node);
                    code.addLast(initialize(node.getUserObject(), iname));
                    code.addLast("  " + nname + "->registerQuantity(\"");
                    code.addLast(cname + "\", " + iname + ");\n\n");
                    break;
                default:
                }
                _flush_code(code);
                code = new LinkedList();
            }

        }

        return code;
    }

    /**
     * Connect the objects in the system.
     */
    protected void connect_objects() {
        LinkedList code = new LinkedList();
        Iterator iter;

        code.addLast("\n");
        code.addLast("  /***** You can modify this order here *****/\n");
        code.addLast("  //Set execution order of processes\n");
        code.addLast("  cout<<\"Set execution order of processes\"<<endl;\n");
        code.addLast("  procorder = new int[" + process.size() + "];\n");
        for (int i = 0; i < process.size(); i++) {
            code.addLast("  procorder[" + i + "] = " + (i + 1) + ";\n");
        }
        // procorder=new int[3];
        // procorder[0]=1;

        code.addLast("\n");
        code.addLast("  //Setup connections\n");
        code.addLast("  cout<<\"Begin setting up connections\"<<endl;\n\n");

        code.addLast("  //Setting up connections from processes to media\n");
        code.addLast("  cout<<\"- processes to media\"<<endl;\n");
        iter = process.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;
                if (con.getTarget().getType().getKind() == MMType.MEDIUM) {
                    IPort ipt = con.getPort();
                    String portName = "";
                    if (ipt instanceof IPortElem) {
                        String pNtmp = ((IPortElem) ipt).show();
                        portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                    } else if (ipt instanceof IPortScalar) {
                        portName = ipt.getName();
                    } else {
                        throw new RuntimeException("port "
                                + con.getPort().getName() + " in scheduler "
                                + name + " can not be used in connection");
                    }
                    String dstName = con.getTarget().getName();
                    code.addLast("  ");
                    code.addLast(name + "->" + portName + "(*" + dstName
                            + ");\n");
                    code.addLast("  ");
                    // code.addLast(name+"->add2portMapList(\"this->"+portName);
                    code.addLast(name + "->add2portMapList(\"" + portName);
                    code.addLast("\",(sc_object *) " + dstName + ");\n");
                }
            }

            _flush_code(code);
            code = new LinkedList();
        }
        // p0.port0(r1);
        // p0.add2portMapList("port0",(sc_object *)&r1);

        code.addLast("\n");
        code.addLast("  //Setting up connections from media to media\n");
        code.addLast("  cout<<\"- media to media\"<<endl;\n");
        iter = medium.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;

                if (con.getTarget().getType().getKind() != MMType.MEDIUM)
                    continue;

                IPort ipt = con.getPort();
                String portName = "";
                if (ipt instanceof IPortElem) {
                    String pNtmp = ((IPortElem) ipt).show();
                    portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                } else if (ipt instanceof IPortScalar) {
                    portName = ipt.getName();
                } else {
                    throw new RuntimeException("port "
                            + con.getPort().getName() + " in scheduler " + name
                            + " can not be used in connection");
                }
                String dstName = con.getTarget().getName();
                code.addLast("  ");
                code.addLast(name + "->" + portName + "(*" + dstName + ");\n");
                code.addLast("  ");
                code.addLast(name + "->add2portMapList(\"" + portName);
                code.addLast("\",(sc_object *) " + dstName + ");\n");
            }
            _flush_code(code);
            code = new LinkedList();
        }

        code.addLast("\n");
        code
                .addLast("  //Setting up connections from processes to statemedia\n");
        code.addLast("  cout<<\"- processes to statemedia\"<<endl;\n");
        iter = process.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;
                if (con.getTarget().getType().getKind() == MMType.STATEMEDIUM) {
                    IPort ipt = con.getPort();
                    String portName = "";
                    if (ipt instanceof IPortElem) {
                        String pNtmp = ((IPortElem) ipt).show();
                        portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                    } else if (ipt instanceof IPortScalar) {
                        portName = ipt.getName();
                    } else {
                        throw new RuntimeException("port "
                                + con.getPort().getName() + " in scheduler "
                                + name + " can not be used in connection");
                    }
                    String dstName = con.getTarget().getName();
                    code.addLast("  ");
                    code.addLast(name + "->" + portName + "(*" + dstName
                            + ");\n");
                    if (con.getPort().getInterface().isSubClass(
                            net.getType("metamodel.lang.StateMediumProc"))) {

                        code.addLast("  ");
                        code.addLast(dstName + "->registerObj(" + name
                                + ", true);\n");
                    }
                }
            }
            _flush_code(code);
            code = new LinkedList();
        }
        // p0.port2(sm0);
        // sm0.registerProcess(&p0);

        code.addLast("\n");
        code.addLast("  //Setting up connections from media to statemedia\n");
        code.addLast("  cout<<\"- media to statemedia\"<<endl;\n");
        iter = medium.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;
                if (con.getTarget().getType().getKind() == MMType.STATEMEDIUM) {
                    IPort ipt = con.getPort();
                    String portName = "";
                    if (ipt instanceof IPortElem) {
                        String pNtmp = ((IPortElem) ipt).show();
                        portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                    } else if (ipt instanceof IPortScalar) {
                        portName = ipt.getName();
                    } else {
                        throw new RuntimeException("port "
                                + con.getPort().getName() + " in scheduler "
                                + name + " can not be used in connection");
                    }
                    String dstName = con.getTarget().getName();
                    code.addLast("  ");
                    code.addLast(name + "->" + portName + "(*" + dstName
                            + ");\n");
                }
            }
            _flush_code(code);
            code = new LinkedList();
        }

        code.addLast("\n");
        code
                .addLast("  //Setting up connections from quantities to statemedia\n");
        code.addLast("  cout<<\"- quantities to statemedia\"<<endl;\n");
        iter = quantity.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;
                if (con.getTarget().getType().getKind() == MMType.STATEMEDIUM) {
                    IPort ipt = con.getPort();
                    String portName = "";
                    if (ipt instanceof IPortElem) {
                        String pNtmp = ((IPortElem) ipt).show();
                        portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                    } else if (ipt instanceof IPortScalar) {
                        portName = ipt.getName();
                    } else {
                        throw new RuntimeException("port "
                                + con.getPort().getName() + " in scheduler "
                                + name + " can not be used in connection");
                    }
                    String dstName = con.getTarget().getName();
                    code.addLast("  ");
                    code.addLast(name + "->" + portName + "(*" + dstName
                            + ");\n");
                }
            }
            _flush_code(code);
            code = new LinkedList();
        }

        code.addLast("\n");
        code
                .addLast("  //Setting up connections from quantities to quantities\n");
        code.addLast("  cout<<\"- quantities to quantities\"<<endl;\n");
        iter = quantity.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;
                if (con.getTarget().getType().getKind() == MMType.QUANTITY) {
                    IPort ipt = con.getPort();
                    String portName = "";
                    if (ipt instanceof IPortElem) {
                        String pNtmp = ((IPortElem) ipt).show();
                        portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                    } else if (ipt instanceof IPortScalar) {
                        portName = ipt.getName();
                    } else
                        throw new RuntimeException("port "
                                + con.getPort().getName() + " in scheduler "
                                + name + " can not be used in connection");
                    String dstName = con.getTarget().getName();
                    code.addLast("  ");
                    code.addLast(name + "->" + portName + "(*" + dstName
                            + ");\n");
                }
            }
            _flush_code(code);
            code = new LinkedList();
        }

        code.addLast("\n");
        code
                .addLast("  //Setting up connections from statemedia to quantities\n");
        code.addLast("  cout<<\"- statemedia to quantities\"<<endl;\n");
        iter = smedium.listIterator();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            String name = node.getName();
            Iterator outc = node.getOutConnections();
            while (outc.hasNext()) {
                Connection con = (Connection) outc.next();
                if (con.isRefined())
                    continue;
                if (con.getTarget().getType().getKind() == MMType.QUANTITY) {
                    IPort ipt = con.getPort();
                    String portName = "";
                    if (ipt instanceof IPortElem) {
                        String pNtmp = ((IPortElem) ipt).show();
                        portName = pNtmp.substring(pNtmp.lastIndexOf(" ") + 1);
                    } else if (ipt instanceof IPortScalar) {
                        portName = ipt.getName();
                    } else
                        throw new RuntimeException("port "
                                + con.getPort().getName() + " in scheduler "
                                + name + " can not be used in connection");
                    String dstName = con.getTarget().getName();
                    code.addLast("  ");
                    code.addLast(name + "->" + portName + "(*" + dstName
                            + ");\n");
                }
            }
            _flush_code(code);
            code = new LinkedList();
        }

    }

    /**
     * Register timed processes to global time quantity managers.
     */
    protected void register_timed_processes() {
        LinkedList code = new LinkedList();
        HashSet found = new HashSet();
        HashSet visited = new HashSet();
        MMType GlobalTime = net.getType("metamodel.lang.GlobalTime");
        boolean hasGT = false;
        Iterator qit = quantity.iterator();

        while (qit.hasNext()) {
            INode qt = (INode) qit.next();
            if (qt.getType().isSubClass(GlobalTime)) {
                find_timed_processes(qt, found, visited);
                if (found.size() > 0) {
                    String qtName = qt.getName();
                    if (!hasGT) {
                        hasGT = true;
                        code.addLast("\n");
                        code
                                .addLast("  //Register timed processes to GlobalTime\n");
                    }
                    Iterator pit = found.iterator();
                    while (pit.hasNext()) {
                        String procName = ((INode) pit.next()).getName();
                        code.addLast("  " + qtName + "->registerTimedProcess("
                                + procName + ");\n");
                    }
                    code.addLast("  _sb.gtimeSet.insert(" + qtName + ");\n");
                    _flush_code(code);
                    code = new LinkedList();
                    found.clear();
                }
                visited.clear();
            }
        }
    }

    /**
     * Setup synch events information including total number of synch events
     * groups, total number of synch events in each group, initialization of
     * enabledSynchEventEachGroup, initialization of enabledSynchEventPC.
     */
    protected void setup_synch_events_info() {
        LinkedList ret = new LinkedList();
        int size = _synchEventPool.getNumSynchEventsGroups();
        ret.addLast("\n");
        ret.addLast("  //Set up synch events information for simulation\n");
        ret.addLast("  _sb.numSynchEventGroup = " + size + ";\n");
        if (size > 0) {
            ret.addLast("  _sb.numSynchEventEachGroup = new int[" + size
                    + "];\n");
            ret.addLast("  _sb.enabledSynchEventEachGroup = new int[" + size
                    + "];\n");
            ret.addLast("  _sb.enabledSynchEventPC = new ProgramCounter**["
                    + size + "];\n");
            for (int i = 0; i < size; i++) {
                HashSet hs = _synchEventPool.getIthSynchEventsGroup(i);
                ret.addLast("  _sb.numSynchEventEachGroup[" + i + "] = "
                        + hs.size() + ";\n");
                if (i % 20 == 19) {
                    _flush_code(ret);
                    ret = new LinkedList();
                }
            }
            ret.addLast("  for (int i=0; i<" + size + "; i++){\n");
            ret.addLast("    _sb.enabledSynchEventEachGroup[i] = 0;\n");
            ret
                    .addLast("    _sb.enabledSynchEventPC[i] = new ProgramCounter*[_sb.numSynchEventEachGroup[i]];\n");
            ret
                    .addLast("    for (int j=0; j<_sb.numSynchEventEachGroup[i]; j++)\n");
            ret.addLast("       _sb.enabledSynchEventPC[i][j] = NULL;\n");
            ret.addLast("  }\n");
        }// end if size>0

        _flush_code(ret);
    }

    /**
     * Setup synch event and synch event group id correspondence.
     */
    protected void setup_synch_event_groupid_correspondence() {
        LinkedList ret = new LinkedList();
        int index = 0;
        for (int ith = 0; ith < _synchEventPool.getNumSynchEventsGroups(); ith++) {
            HashSet oneGroup = _synchEventPool.getIthSynchEventsGroup(ith);
            //int num = 0;
            Iterator iter = oneGroup.iterator();
            while (iter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                String procName = (String) instName.get(se.getEvent()
                        .getProcess().getUserObject());
                String objName = (String) instName.get(se.getEvent()
                        .getNodeObject().getUserObject());
                String name = se.getEvent().getName();
                int kind = se.getEvent().getKind();
                ret.addLast("  _sb.addSynchEventGroupInfo((process*)"
                        + procName + ", ");
                ret.addLast("(medium*)" + objName + ", \"" + name + "\", ");
                if (kind == Event.BEG)
                    ret.addLast("ACTION_STATE_BEGIN");
                else if (kind == Event.END)
                    ret.addLast("ACTION_STATE_END");
                ret.addLast(", " + se.getSynchGroupID() + ");\n");
                index++;
            } // end iter while
            if (index >= 20) {
                index = 0;
                _flush_code(ret);
                ret = new LinkedList();
            }
        } // end for

        _flush_code(ret);
    }

    /**
     * Generate equality comparison for variables in synch statements.
     */
    protected void generate_synch_equalvars() {
        LinkedList ret = new LinkedList();
        int index = 0;
        int totalnum = _synchEventPool.getNumSynchEventsGroups();
        if (totalnum > 0) {
            ret.addLast("\n");
            ret
                    .addLast("//Global variables reflecting equal variables in synch\n");
        }
        for (int ith = 0; ith < totalnum; ith++) {
            HashSet oneGroup = _synchEventPool.getIthSynchEventsGroup(ith);
            int num = 0;
            Iterator iter = oneGroup.iterator();
            while (iter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                num += se.getNumSynchVariables();
            } // end iter while

            if (num > 0) {
                ret.addLast("void ** _synchEqualVars_" + ith + " = new void*["
                        + num + "];\n");
                index++;
            }

            if (index >= 20) {
                index = 0;
                _flush_code(ret);
                ret = new LinkedList();
            }
        } // end for

        _flush_code(ret);
    }

    /**
     * Assign indices to variables in equality comparison in synch statements.
     */
    protected void assign_equalvars_in_synch() {
        for (int ith = 0; ith < _synchEventPool.getNumSynchEventsGroups(); ith++) {
            HashSet oneGroup = _synchEventPool.getIthSynchEventsGroup(ith);
            int num = 0;
            Iterator iter = oneGroup.iterator();
            while (iter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                Iterator vars = se.getVariables().iterator();
                while (vars.hasNext()) {
                    VarAttribute var = (VarAttribute) vars.next();
                    if (var.getType().startsWith("constant")) {
                        var.setIndexInSynchGroup(-1);
                    } else {
                        var.setIndexInSynchGroup(num);
                        num++;
                    }
                } // end vars while
            } // end iter while
        } // end for
    }

    /**
     * Generate equality comparison code for synch statements.
     */
    protected void compare_equalvars_in_synch() {
        LinkedList ret = new LinkedList();
        ret.addLast("void process_equalvars_in_synch(){\n");
        for (int ith = 0; ith < _synchEventPool.getNumSynchEventsGroups(); ith++) {
            HashSet oneGroup = _synchEventPool.getIthSynchEventsGroup(ith);
            boolean hasEqualVar = false;
            Iterator iter = oneGroup.iterator();
            while (iter.hasNext() && (!hasEqualVar)) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                if (se.getNumSynchVariables() > 0)
                    hasEqualVar = true;
            } // end iter while

            if (hasEqualVar) {
                ret.addLast("  if (_sb.isSynchEventGroupEnabled(" + ith
                        + ")) {\n");
                ret.addLast("  bool satisfied = true;\n");
            }
            VarAttribute var = null;
            iter = oneGroup.iterator();
            while (iter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                //String objName = (String) instName.get(se.getEvent()
                //        .getNodeObject().getUserObject());
                Iterator vars = se.getVariables().iterator();
                while (vars.hasNext()) {
                    var = (VarAttribute) vars.next();
                    if (var.isProcessed())
                        continue;
                    LinkedList det = new LinkedList();
                    LinkedList nondet = new LinkedList();

                    VarAttribute vatmp = var;
                    do {
                        String type;
                        if (vatmp.isNondetVar())
                            type = "Nondet*";
                        else
                            type = vatmp.getType();
                        String varName;
                        if (type.startsWith("constant"))
                            varName = vatmp.getVarName();
                        else
                            varName = "(*((" + type + "*)_synchEqualVars_"
                                    + ith + "[" + vatmp.getIndexInSynchGroup()
                                    + "]))";
                        if (vatmp.isNondetVar())
                            nondet.add(varName);
                        else
                            det.add(varName);
                        vatmp.setProcessed(true);
                        vatmp = vatmp.getEqualVar();
                    } while (vatmp != var);

                    ret.addLast("  {\n");
                    if (var.getType().startsWith("constant"))
                        ret.addLast("  "
                                + StringManip.partAfterLast(var.getType(), '.')
                                + " eq;\n");
                    else
                        ret.addLast("  " + var.getType() + " eq;\n");
                    if (det.size() > 0) {
                        ret.addLast("  eq = " + det.get(0) + ";\n");
                        String closePar = "  ";
                        for (int i = 1; i < det.size(); i++) {
                            ret
                                    .addLast("  if (satisfied) {  satisfied &= (eq=="
                                            + det.get(i) + ");\n");
                            closePar = closePar + "}";
                        } // end for
                        ret.addLast(closePar + "\n");

                        closePar = "  ";
                        for (int i = 0; i < nondet.size(); i++) {
                            ret.addLast("  if (satisfied) {\n");
                            ret.addLast("  if (" + nondet.get(i)
                                    + "->isNondet()) " + nondet.get(i)
                                    + "->set(eq);\n");
                            ret.addLast("  else satisfied &= (eq=="
                                    + nondet.get(i) + "->get());\n");
                            closePar = closePar + "}";
                        } // end for
                        ret.addLast(closePar + "\n\n");
                    } else if (nondet.size() > 0) {
                        ret.addLast("  bool hasStandard = false;\n");
                        String closePar = "  ";
                        for (int i = 0; i < nondet.size(); i++) {
                            ret.addLast("  if (!hasStandard) {\n");
                            ret.addLast("  if (!" + nondet.get(i)
                                    + "->isNondet()) { eq = " + nondet.get(i)
                                    + "->get(); hasStandard = true;}\n");
                            closePar = closePar + "}";
                        } // end for
                        ret.addLast(closePar + "\n");

                        ret
                                .addLast("  if (!hasStandard) eq = nondeterminism(sizeof("
                                        + var.getType() + "));\n\n");
                        closePar = "  ";
                        for (int i = 0; i < nondet.size(); i++) {
                            ret.addLast("  if (satisfied) {\n");
                            ret.addLast("  if (" + nondet.get(i)
                                    + "->isNondet()) " + nondet.get(i)
                                    + "->set(eq);\n");
                            ret.addLast("  else satisfied &= (eq=="
                                    + nondet.get(i) + "->get());\n");
                            closePar = closePar + "}";
                        } // end for
                        ret.addLast("  " + closePar + "\n\n");
                    } // end if
                    ret.addLast("  }\n\n");

                    _flush_code(ret);
                    ret = new LinkedList();
                } // end vars while
            } // end iter while
            if (hasEqualVar) {
                ret.addLast("  _sb.setSynchEventGroupStatus(" + ith
                        + ", satisfied);\n");
                ret.addLast("  }\n\n");
            }
        } // end for
        ret.addLast("}\n\n");

        _flush_code(ret);
    }

    /**
     * Setup synch imply events information.
     */
    protected void setup_synchimply_events_info() {
        String varTypes[] = { "bool", "char", "double", "float", "int", "long",
                "String*", "byte", "short" };
        String varTypesConst[] = { "BOOLTYPE", "CHARTYPE", "DOUBLETYPE",
                "FLOATTYPE", "INTTYPE", "LONGTYPE", "STRINGTYPE", "BYTETYPE",
                "SHORTTYPE" };
        int tmpVarID = 0;
        String tmpVarPrefix = "constVar_";

        LinkedList ret = new LinkedList();
        int size = _LTLSIConstraints.size();
        ret.addLast("\n");
        ret
                .addLast("  //Set up synch imply events information for simulation\n");
        ret.addLast("  _sb.numSynchImplyGroup = " + size + ";\n");
        ret.addLast("  ltlsi = new LTLSynchImply*[" + size + "];\n");

        for (int ltlsii = 0; ltlsii < size; ltlsii++) {
            ret.addLast("\n  ltlsi[" + ltlsii + "] = new LTLSynchImply();\n");
            LTLSynchImply ltlsi = (LTLSynchImply) _LTLSIConstraints.get(ltlsii);
            String ltlsiName = "ltlsi[" + ltlsii + "]";
            for (int i = 0; i < 2; i++) {
                LinkedList events = (i == 0) ? ltlsi.getLeftEvents() : ltlsi
                        .getRightEvents();
                int esize = events.size();
                String h = ltlsiName + "->" + ((i == 0) ? "lhs" : "rhs");
                ret.addLast("  " + h + "Num = " + esize + ";\n");
                ret.addLast("  " + h + " = new event*[" + esize + "];\n");
                for (int ei = 0; ei < esize; ei++) {
                    Event e = (Event) events.get(ei);
                    String eName = null;
                    switch (e.getKind()) {
                    case Event.BEG:
                        eName = "beg(";
                        break;
                    case Event.END:
                        eName = "end(";
                        break;
                    default:
                        System.err
                                .println("Simulator can not handle NONE and OTHER events for now.");
                    }
                    eName += instName.get(e.getProcess().getUserObject());
                    eName += ", ";
                    eName += instName.get(e.getNodeObject().getUserObject());
                    eName += ", \"";
                    eName += e.getName();
                    eName += "\")";
                    ret.addLast("  " + h + "[" + ei + "] = " + eName + ";\n");
                }// end for ei
            }// end for i

            LinkedList eqvars = ltlsi.getEqualVars();
            int eqvsize = eqvars.size();
            ret.addLast("  " + ltlsiName + "->eqvarsNum = " + eqvsize + ";\n");
            ret.addLast("  " + ltlsiName + "->eqvars = new EqualVar*["
                    + eqvsize + "];\n");
            for (int i = 0; i < eqvsize; i++) {
                EqualVars eqv = (EqualVars) eqvars.get(i);
                String eqvName = ltlsiName + "->eqvars[" + i + "]";
                String eName[] = { "NULL", "NULL" };
                boolean nondet[] = { false, false };
                boolean constant[] = { false, false };
                int type[] = { -1, -1 };
                String constAsgn = "";
                for (int ei = 0; ei < 2; ei++) {
                    Event e = (ei == 0) ? eqv.getEvent1() : eqv.getEvent2();
                    if (e != null) {
                        switch (e.getKind()) {
                        case Event.BEG:
                            eName[ei] = "beg(";
                            break;
                        case Event.END:
                            eName[ei] = "end(";
                            break;
                        default:
                            System.err
                                    .println("Simulator can not handle NONE and OTHER events for now.");
                        }
                        eName[ei] += instName.get(e.getProcess()
                                .getUserObject());
                        eName[ei] += ", ";
                        eName[ei] += instName.get(e.getNodeObject()
                                .getUserObject());
                        eName[ei] += ", \"";
                        eName[ei] += e.getName();
                        eName[ei] += "\")";
                    }// end e
                    type[ei] = (ei == 0) ? eqv.getType1() : eqv.getType2();
                    if (type[ei] != EqualVars.VARTYPE) {
                        nondet[ei] = false;
                        constant[ei] = true;
                        String tmpVar = tmpVarPrefix + (tmpVarID++);
                        constAsgn += "  " + varTypes[type[ei]] + " " + tmpVar
                                + " = ";
                        switch (type[ei]) {
                        case EqualVars.CHARTYPE:
                            constAsgn += "'"
                                    + ((ei == 0) ? eqv.getValue1() : eqv
                                            .getValue2()) + "';\n";
                            break;
                        case EqualVars.STRINGTYPE:
                            constAsgn += "new String(\""
                                    + ((ei == 0) ? eqv.getValue1() : eqv
                                            .getValue2()) + "\");\n";
                            break;
                        default:
                            constAsgn += ((ei == 0) ? eqv.getValue1() : eqv
                                    .getValue2())
                                    + ";\n";
                        }// end switch
                        constAsgn += "  " + eqvName + "->v[" + ei + "]->v = &"
                                + tmpVar + ";\n";
                    } else {
                        String typeName = SynchEventPool.getVarType(e,
                                (ei == 0) ? eqv.getValue1() : eqv.getValue2());
                        if (typeName.startsWith("Nondet")) {
                            nondet[ei] = true;
                            typeName = StringManip.partAfterLast(typeName, '.');
                        } else {
                            nondet[ei] = false;
                        }// end if
                        int t = 0;
                        for (; t < varTypes.length; t++)
                            if (typeName.startsWith(varTypes[t]))
                                break;
                        type[ei] = t;
                    }// end if
                }// end for ei
                ret.addLast("  " + eqvName + " = new EqualVar(\n");
                ret.addLast("    new Variable(" + eName[0] + ", " + nondet[0]);
                ret.addLast(", " + constant[0]);
                ret.addLast(", VarType::" + (varTypesConst[type[0]]) + "),\n");
                ret.addLast("    new Variable(" + eName[1] + ", " + nondet[1]);
                ret.addLast(", " + constant[1]);
                ret.addLast(", VarType::" + (varTypesConst[type[1]]) + ")\n");
                ret.addLast("  );\n");
                ret.addLast(constAsgn);
            }// end for i
            _flush_code(ret);
            ret = new LinkedList();
        }// end for ltlsii
    }

    /**
     * Add LTL constraints into top level netlist.
     */
    protected void setup_ltl_constraints() {
        Hashtable events = net.getEventCache();
        if (events.size() == 0)
            return;
        Enumeration keys = events.keys();
        LinkedList ret = new LinkedList();
        int i = 0;
        ret.addLast("\n  //Register all unique events to top level netlist\n");
        while (keys.hasMoreElements()) {
            Event e = (Event) keys.nextElement();
            if ((e.getUsed() & Constraint.LTL) == 0)
                continue;
            ret.addLast("  top_level_netlist->registerLTLEvent(");
            switch (e.getKind()) {
            case Event.BEG:
                ret.addLast("beg(");
                break;
            case Event.END:
                ret.addLast("end(");
                break;
            default:
                System.err
                        .println("Simulator can not handle NONE and OTHER events for now.");
            }
            ret.addLast(instName.get(e.getProcess().getUserObject()));
            ret.addLast(", ");
            ret.addLast(instName.get(e.getNodeObject().getUserObject()));
            ret.addLast(", \"");
            ret.addLast(e.getName());
            ret.addLast("\"), \"");
            ret.addLast(Event.getEventName(e));
            ret.addLast("\");\n");
            i++;
            if (i == 10) {
                _flush_code(ret);
                ret = new LinkedList();
                i = 0;
            }
        }

        ret.addLast("\n  //Construct LTL formula\n");
        LinkedList constraints = net.getConstraints();
        Iterator iter = constraints.iterator();
        String sep = "";
        while (iter.hasNext()) {
            Constraint c = (Constraint) iter.next();
            if (c.getKind() != Constraint.LTL)
                continue;
            ret.addLast("  top_level_netlist->appendLTLFormula(\"" + sep + "(");
            ret.addLast(c.getFormula(Constraint.SYSTEMC_FORMULA));
            ret.addLast(")\");\n");
            sep = " && ";
            i++;
            if (i == 10) {
                _flush_code(ret);
                ret = new LinkedList();
                i = 0;
            }
        }
        if (i > 0)
            _flush_code(ret);
    }

    /**
     * Setup information of quantities that govern built-in LOC constraints.
     */
    protected void setup_builtin_loc_constraints() {
        if (_BILOCConstraints.size() == 0)
            return;

        LinkedList ret = new LinkedList();
        ret.addLast("\n  //Register all unique events to top level netlist\n");

        HashSet quantities = new HashSet();
        int i = 0;
        Iterator it = _BILOCConstraints.iterator();
        while (it.hasNext()) {
            LinkedList qts = ((BuiltInLOC) it.next()).getQuantities();
            Iterator qtIter = qts.iterator();
            while (qtIter.hasNext()) {
                INode qt = (INode) qtIter.next();
                if (quantities.contains(qt))
                    continue;
                quantities.add(qt);
                ret.addLast("  _sb.BILOCQuantities[" + qt.getObjectID()
                        + "] = ");
                ret.addLast(instName.get(qt.getUserObject()));
                ret.addLast(";\n");
                i++;
            }
            if (i >= 10) {
                _flush_code(ret);
                ret = new LinkedList();
                i = 0;
            }
        }

        if (i > 0)
            _flush_code(ret);
    }

    /**
     * Read from instances of mmm objects all information about fields, and
     * initialize the instances' fields explicitly.
     *
     * @param obj
     *            Instance of mmm object
     * @param objName
     *            Name of the instance obj
     * @return the initialization code in a list of Strings
     * @exception IllegalAccessException
     */
    protected Object initialize(Object obj, String objName)
            throws IllegalAccessException {
        LinkedList ret = new LinkedList();

        if (obj == null)
            return null;

        // Object n = net.getNode(obj);
        // if (n!=null && n instanceof INode && objName.indexOf("->")>-1)
        // return ret;

        Class cls = obj.getClass();
        String clsName = _convertToCppTemplateFormat(cls.getName());

        // Already initialized before, use the former reference
        if (instName.containsKey(obj)) {
            if (objName.indexOf("->") == -1) {
                ret.addLast("  " + StringManip.partAfterLast(clsName, '.')
                        + "*");
            }
            ret.addLast("  ");
            ret.addLast(objName);
            ret.addLast(" = ");
            ret.addLast(instName.get(obj));
            ret.addLast(";\n");
            return ret;
        }
        if (cls.isPrimitive() || clsName.equals("java.lang.String")
                || clsName.equals("java.lang.Boolean")
                || clsName.equals("java.lang.Byte")
                || clsName.equals("java.lang.Character")
                || clsName.equals("java.lang.Double")
                || clsName.equals("java.lang.Float")
                || clsName.equals("java.lang.Integer")
                || clsName.equals("java.lang.Long")
                || clsName.equals("java.lang.Short")) {
            // a primitive type variable
            // or an element in a primitive type variable array
            String value = obj.toString();
            ret.addLast("  ");
            if (objName.indexOf("->") == -1) {
                ret.addLast(StringManip.partAfterLast(clsName, '.') + "* ");
            }
            ret.addLast(objName);
            ret.addLast(" = ");
            if (clsName.equals("char") || clsName.equals("java.lang.Character"))
                ret.addLast("'" + value + "';\n");
            else if (clsName.equals("java.lang.String"))
                ret.addLast("new String(\"" + value + "\");\n");
            else if (!objName.endsWith("]"))
                ret.addLast(obj.toString() + ";\n");
            else if (!value.equals("0"))
                ret.addLast(obj.toString() + ";\n");
            else
                return null;
        } else if (clsName
                .startsWith("metropolis.metamodel.backends.elaborator.util.")) {
            // an object defined in util package
            ret.addLast("  ");
            if (objName.indexOf("->") == -1) {
                ret.addLast(StringManip.partAfterLast(clsName, '.') + "* ");
            }
            ret.addLast(objName);
            ret.addLast(" = new ");
            ret.addLast(StringManip.partAfterLast(clsName, '.') + "();\n");
            instName.put(obj, objName);
            if (clsName.endsWith("HashMap") || clsName.endsWith("Hashtable")
                    || clsName.endsWith("TreeMap")) {
                Iterator keyIter = ((Map) obj).keySet().iterator();
                while (keyIter.hasNext()) {
                    Object key = keyIter.next();
                    Object value = ((Map) obj).get(key);
                    String keyName = "_key_" + _generateUniqueVarID();
                    String valueName = "_value_" + _generateUniqueVarID();
                    Object retvalue = initialize(key, keyName);
                    if (retvalue != null)
                        ret.addLast(retvalue);
                    retvalue = initialize(value, valueName);
                    if (retvalue != null)
                        ret.addLast(retvalue);
                    ret.addLast("  ");
                    ret.addLast(objName);
                    ret.addLast("->put((Object*)" + keyName + ", (Object*)"
                            + valueName + ");\n");
                }
            } else if (clsName.endsWith("ArrayList")
                    || clsName.endsWith("LinkedList")
                    || clsName.endsWith("Vector")) {
                Iterator elemIter = ((List) obj).iterator();
                while (elemIter.hasNext()) {
                    Object value = elemIter.next();
                    String valueName = "_value_" + _generateUniqueVarID();
                    Object retvalue = initialize(value, valueName);
                    if (retvalue != null)
                        ret.addLast(retvalue);
                    ret.addLast("  ");
                    ret.addLast(objName);
                    ret.addLast("->add((Object*)" + valueName + ");\n");
                }
            } else if (clsName.endsWith("HashSet")
                    || clsName.endsWith("TreeSet")) {
                Iterator elemIter = ((Set) obj).iterator();
                while (elemIter.hasNext()) {
                    Object value = elemIter.next();
                    String valueName = "_value_" + _generateUniqueVarID();
                    Object retvalue = initialize(value, valueName);
                    if (retvalue != null)
                        ret.addLast(retvalue);
                    ret.addLast("  ");
                    ret.addLast(objName);
                    ret.addLast("->add((Object*)" + valueName + ");\n");
                }
            } else if (clsName.endsWith("BitSet")) {
                int size = ((BitSet) obj).size();
                for (int i = 0; i < size; i++) {
                    boolean value = ((BitSet) obj).get(i);
                    ret.addLast("  ");
                    ret.addLast(objName);
                    if (value)
                        ret.addLast("->set(" + i + ");\n");
                    else
                        ret.addLast("->clear(" + i + ");\n");
                }
            } else {
                System.err.println("Warning: Unsupported util class "
                        + StringManip.partAfterLast(clsName, '.') + ".");
                System.err
                        .println("Its contents cannot be reproduced in simulation!");
            }
        } else if (cls.isInterface()) {
        } else if (cls.isArray()) {
            // Get limits of all dimensions, and base type
            if (Array.getLength(obj) == 0) {
                System.err.println("Warning: Array " + objName
                        + " is ignored in "
                        + "initialization because it has 0 length.");
                return new LinkedList();
            }
            int dim = 1;
            Class comType = cls.getComponentType();
            Object comObj = Array.get(obj, 0);
            while (comType.isArray()) {
                dim++;
                comType = comType.getComponentType();
                comObj = Array.get(comObj, 0);
            }

            // test if comType is an interface
            Field testIntfc[] = comType.getDeclaredFields();
            boolean isInterface = false;
            if (comType.getName().startsWith("__interface_")) {
                isInterface = true;
            } else {
                for (int j = 0; j < testIntfc.length; j++) {
                    if (testIntfc[j].getName().equals("MMMInterfaceIdentifier")) {
                        isInterface = true;
                        break;
                    }
                }
            }

            if (!isInterface) {
                ret.addLast("  ");
                if (objName.indexOf("->") == -1) {
                    ret.addLast(StringManip.partAfterLast(clsName, '.') + "* ");
                }
                ret.addLast(objName);
                ret.addLast(" = new ");
                /*
                 * This is a C syntax error as of gcc 3.4: if (dim > 1)
                 * ret.addLast("(");
                 */
                String comName = comType.getName();
                if (comName.equals("metamodel.lang.Process"))
                    comName = "process";
                else if (comName.equals("metamodel.lang.Medium"))
                    comName = "medium";
                else if (comName.equals("metamodel.lang.Quantity"))
                    comName = "quantity";
                else if (comName.equals("metamodel.lang.Netlist"))
                    comName = "netlist";
                else if (comName.equals("metamodel.lang.StateMedium"))
                    comName = "statemedium";
                comName = StringManip.partAfterLast(comName, '.');

                if (comType.isPrimitive()) {
                    if (comName.equals("boolean"))
                        ret.addLast("bool");
                    else
                        ret.addLast(comName);
                } // else if (comName.equals("java.lang.Integer"))
                // ret.addLast(comName);
                else
                    ret.addLast(comName + "*");
                for (int j = 1; j < dim; j++)
                    ret.addLast("*");
                /*
                 * This is a C syntax error as of gcc 3.4: if (dim > 1)
                 * ret.addLast(")");
                 */
                // ret.addLast("["+index.removeFirst()+"]");
                ret.addLast("[" + Array.getLength(obj) + "]");
                ret.addLast(";\n");
                instName.put(obj, objName);
                if (dim == 1) {
                    /*
                     * No need to check the object types, just set them to NULL
                     * clsName = comType.getName(); if (comType.isPrimitive() ||
                     * clsName.equals("java.lang.Boolean") ||
                     * clsName.equals("java.lang.Byte") ||
                     * clsName.equals("java.lang.Character") ||
                     * clsName.equals("java.lang.Double") ||
                     * clsName.equals("java.lang.Float") ||
                     * clsName.equals("java.lang.Integer") ||
                     * clsName.equals("java.lang.Long") ||
                     * clsName.equals("java.lang.Short"))
                     */{
                        ret.addLast("  for (int i=0; i<");
                        ret.addLast(String.valueOf(Array.getLength(obj)));
                        ret.addLast("; i++) " + objName + "[i] = 0;\n");
                    }
                }
                for (int j = 0; j < Array.getLength(obj); j++) {
                    Object retvalue = initialize(Array.get(obj, j), objName
                            + "[" + j + "]");
                    if (retvalue != null)
                        ret.addLast(retvalue);
                }
            }
        } else {
            // instance of a class

            // Interface
            Field testIntfc[] = cls.getDeclaredFields();
            for (int i = 0; i < testIntfc.length; i++) {
                if (testIntfc[i].getName().equals("MMMInterfaceIdentifier"))
                    return ret;
            }

            // Generate constructor argument (int * index)
            LinkedList tmp = (LinkedList) _get_ports_dims(obj);
            if (!tmp.isEmpty()) {
                Object[] dims = tmp.toArray();
                ret.addLast("  index = new int[" + dims.length + "];\n");
                for (int i = 0; i < dims.length; i++)
                    ret.addLast("  index[" + i + "] = " + dims[i] + ";\n");
            }

            ret.addLast("  ");
            if (objName.indexOf("->") == -1) {
                ret.addLast(StringManip.partAfterLast(clsName, '.') + "* ");
            }
            ret.addLast(objName + " = new "
                    + StringManip.partAfterLast(clsName, '.') + "(");
            if (net.getNode(obj) != null) {
                ret.addLast("\"" + net.getNode(obj).getName() + "\", ");
            }
            ret.addLast("_arg, index);\n");
            if (!tmp.isEmpty()) {
                ret.addLast("  delete index;\n");
            }

            instName.put(obj, objName);

            while (cls != null && !clsName.equals("java.lang.Object")) {
                Field f[] = cls.getDeclaredFields();
                AccessibleObject.setAccessible(f, true);
                for (int i = 0; i < f.length; i++) {
                    if (java.lang.reflect.Modifier
                            .isStatic(f[i].getModifiers()))
                        continue;
                    if (java.lang.reflect.Modifier.isFinal(f[i].getModifiers()))
                        continue;
                    if (f[i].getName().startsWith("__pointer_"))
                        continue;
                    String fobjName;
                    if (objName.indexOf("->") == -1)
                        fobjName = objName;
                    else
                        fobjName = "(("
                                + StringManip.partAfterLast(clsName, '.')
                                + "*)" + objName + ")";
                    fobjName = fobjName + "->" + f[i].getName();
                    Object retvalue = initialize(f[i].get(obj), fobjName);
                    if (retvalue != null)
                        ret.addLast(retvalue);
                }
                cls = cls.getSuperclass();
                clsName = _convertToCppTemplateFormat(cls.getName());
                if (clsName.startsWith("metamodel.lang")) {
                    if (clsName.equals("metamodel.lang.Process")) {
                        try {
                            Field mpf = cls.getDeclaredField("mappingProcess");
                            mpf.setAccessible(true);
                            boolean mp = mpf.getBoolean(obj);
                            if (mp)
                                ret.addLast("  " + objName
                                        + "->mappingProcess = true;\n");
                            else
                                ret.addLast("  " + objName
                                        + "->mappingProcess = false;\n");
                        } catch (Exception e) {
                            System.err.println("Cannot determine whether "
                                    + objName);
                            System.err
                                    .println(" is a mapping process or not.\n");
                            System.err.println(e);
                        }
                    }
                    cls = null;
                }
            }
        }

        return ret;
    }

    /**
     * Read from an instance of netlist all information about fields, and
     * initialize the instances' fields explicitly.
     *
     * @param obj
     *            Instance of mmm object
     * @param objName
     *            Name of the instance obj
     * @return the initialization code in a list of Strings
     * @exception IllegalAccessException
     */
    protected Object initialize_netlist(Object obj, String objName)
            throws IllegalAccessException {
        LinkedList ret = new LinkedList();

        if (obj == null)
            return null;

        Class cls = obj.getClass();
        String clsName = _convertToCppTemplateFormat(cls.getName());

        while (cls != null && !clsName.equals("java.lang.Object")) {
            Field f[] = cls.getDeclaredFields();
            AccessibleObject.setAccessible(f, true);
            for (int i = 0; i < f.length; i++) {
                if ((f[i].getModifiers() & java.lang.reflect.Modifier.FINAL) != 0)
                    continue;
                Object retvalue = initialize(f[i].get(obj), objName + "->"
                        + f[i].getName());
                if (retvalue != null)
                    ret.addLast(retvalue);
            }
            cls = cls.getSuperclass();
            clsName = _convertToCppTemplateFormat(cls.getName());
            if (clsName.startsWith("metamodel.lang"))
                cls = null;
        }

        return ret;
    }

    /**
     * Traverse backwards the connections from a node to find process nodes.
     *
     * @param node
     *            A node instance in the system.
     * @param found
     *            Stores all the processes found so far
     * @param visited
     *            Stores all nodes that have been visited
     */
    protected void find_timed_processes(INode node, HashSet found,
            HashSet visited) {
        Iterator con = node.getInConnections();
        if (visited.contains(node))
            return;
        else
            visited.add(node);
        while (con.hasNext()) {
            Connection c = (Connection) con.next();
            if (c.isRefined())
                continue;
            INode src = c.getSource();
            if (src.getType().getKind() == MMType.PROCESS) {
                found.add(src);
            } else {
                find_timed_processes(src, found, visited);
            }
        }
    }

    /**
     * Traverse from top level netlist to collect constraint information
     * excluding those refined ones.
     *
     * @param node
     *            A node/netlist instance in the system.
     */
    protected void collect_constraints(INode node) {
        LinkedList constraints = node.getConstraints();
        Iterator iter = constraints.iterator();
        while (iter.hasNext()) {
            Constraint cstr = (Constraint) iter.next();
            switch (cstr.getKind()) {
            case Constraint.LOC:
                _LOCConstraints.add(cstr);
                break;
            case Constraint.ELOC:
                _ELOCConstraints.add(cstr);
                break;
            case Constraint.LTL:
                _LTLConstraints.add(cstr);
                break;
            case Constraint.LTLSYNCH:
                _LTLSConstraints.add(cstr);
                break;
            case Constraint.LTLSYNCHIMPLY:
                _LTLSIConstraints.add(cstr);
                break;
            case Constraint.MAXRATE:
            case Constraint.MINRATE:
            case Constraint.MAXDELTA:
            case Constraint.MINDELTA:
            case Constraint.PERIOD:
                _BILOCConstraints.add(cstr);
                break;
            default:
                throw new RuntimeException("Unknow kind of constraint."
                        + cstr.show());
            }
        }// end while

        if (node instanceof INetlist) {
            iter = ((INetlist) node).getComponents();
            INode n;
            INode ref;
            while (iter.hasNext()) {
                n = (INode) iter.next();

                if (n.isRefined() && n.getRefinement() != node)
                    ref = n.getRefinement();
                else
                    // if (n.getType().getKind() == MMType.NETLIST)
                    ref = n;

                collect_constraints(ref);
            }
        }
    }

    /**
     * Build equivalent classes of synch events.
     */
    protected void process_synch_constraints() {
        Iterator iter = _LTLSConstraints.iterator();
        while (iter.hasNext())
            _synchEventPool.addLTLSynch((LTLSynch) iter.next());
        _synchEventPool.compactSynchEventsStorage();

        assign_equalvars_in_synch();
    }

    /**
     * Traverse from top level netlist to collect interfaces used in test lists
     * and set lists in awaits either in media themselves or in the processes
     * connected to the media. Store referred interfaces as a property
     * (REFINTFC_KEY) of MediumDeclNode.
     *
     * @param netlist
     *            a netlist instance in the system.
     */
    protected void annotate_referred_interfaces(INetlist netlist) {
        Iterator iter = netlist.getComponents();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();

            if (node.isRefined() && node.getRefinement() != netlist) {
                annotate_referred_interfaces(node.getRefinement());
            } else if (node.getType().getKind() == MMType.NETLIST) {
                annotate_referred_interfaces((INetlist) node);
            } else if (node.isRefined()) {
                continue;
            } else if (node.getType().getKind() == MMType.MEDIUM) {
                String objFullName = node.getType().getName();
                MediumDeclNode medium = (MediumDeclNode) _findObjectAST(
                        objFullName, MMType.MEDIUM);

                LinkedList refIntfcs = (LinkedList) medium.getName()
                        .getProperty(REFINTFC_KEY);
                if (refIntfcs == null) {
                    refIntfcs = new LinkedList();
                }

                // For every connection source,
                // collect the interfaces that will be referred.
                HashSet intfcs = new HashSet(refIntfcs);
                Iterator cons = node.getInConnections();
                while (cons.hasNext()) {
                    Connection con = (Connection) cons.next();
                    INode source = con.getSource();

                    int type = source.getType().getKind();
                    if (type != MMType.MEDIUM && type != MMType.PROCESS)
                        continue;
                    IPort port = con.getPort();
                    String portName = port.show();
                    portName = portName.substring(portName.indexOf(' ')).trim();

                    // port-interface pairs for connection source node
                    // (a medium or process)
                    Hashtable portIntfcs;
                    if (_portInterfaces.containsKey(source)) {
                        portIntfcs = (Hashtable) _portInterfaces.get(source);
                    } else {
                        String srcFullName = source.getType().getName();
                        UserTypeDeclNode src = _findObjectAST(srcFullName, type);
                        portIntfcs = (Hashtable) src.accept(
                                new PortInterfaceCollectVisitor(), null);
                        _portInterfaces.put(source, portIntfcs);
                    }

                    // Add interfaces referred by the connection through port
                    // with name pn
                    Iterator keys = portIntfcs.keySet().iterator();
                    while (keys.hasNext()) {
                        String pn = (String) keys.next();
                        if (pn.equals(portName)) {
                            HashSet ifs = (HashSet) portIntfcs.get(pn);
                            Iterator ifsIter = ifs.iterator();
                            while (ifsIter.hasNext())
                                intfcs.add(ifsIter.next());
                        }
                    } // end while keys
                } // end while cons

                // Collect port.interface of the medium itself
                Hashtable mPortIntfcs;
                if (_portInterfaces.containsKey(node)) {
                    mPortIntfcs = (Hashtable) _portInterfaces.get(node);
                } else {
                    mPortIntfcs = (Hashtable) medium.accept(
                            new PortInterfaceCollectVisitor(), null);
                    _portInterfaces.put(node, mPortIntfcs);
                }

                HashSet ifs = (HashSet) mPortIntfcs.get("this");
                if (ifs != null) {
                    Iterator ifsIter = ifs.iterator();
                    while (ifsIter.hasNext()) {
                        intfcs.add(ifsIter.next());
                    }
                }

                // Annotate referred interface function events
                LinkedList prop = new LinkedList(intfcs);
                medium.getName().setProperty(REFINTFC_KEY, prop);
            }
        }
    }

    /**
     * Traverse asts to determine whether actions are interleaving concurrent
     * atomic. Actions include functions, critical sections of an await, an
     * await, and the entire interface that a medium implements. When necessary,
     * connection relationship from elaboration will be consulted.
     *
     * @param netlist
     *            a netlist instance in the system.
     */
    protected void simplify_ic_awaits(INetlist netlist) {

        // Compute interleaving concurrent atomicities for all processes and
        // media
        _icProcessed = new HashSet();
        build_ica_formula(netlist, allSources, 1);
        _icProcessed = new HashSet();
        build_ica_formula(netlist, allSources, 2);

        Iterator iter = allSources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            ast.accept(new ICAwaitOptimizeVisitor(), null);
        }
    }

    /**
     * For each function, await, await critical section and the entire interface
     * that a medium implements, build a symbolic computation formula indicating
     * when it is interleaving concurrent atomic.
     *
     * @param netlist
     *            The INetlist instance in Network
     * @param allSources
     *            All ASTs
     * @param mode
     *            Two different modes: 1: Compute interleaving concurrent
     *            atomicities for all processes and media except for test lists
     *            in await statements 2: Compute interleaving concurrent
     *            atomicities for test lists in await statements
     */
    protected void build_ica_formula(INetlist netlist, List allSources, int mode) {
        // Compute interleaving concurrent atomicities
        Iterator iter = netlist.getComponents();
        while (iter.hasNext()) {
            INode node = (INode) iter.next();
            int nodetype = node.getType().getKind();

            if (node.isRefined() && node.getRefinement() != netlist) {
                build_ica_formula(node.getRefinement(), allSources, mode);
            } else if (nodetype == MMType.NETLIST) {
                build_ica_formula((INetlist) node, allSources, mode);
            } else if (node.isRefined()) {
                continue;
            } else {
                if (nodetype != MMType.MEDIUM && nodetype != MMType.PROCESS)
                    continue;
                if (_icProcessed.contains(node))
                    continue;
                else
                    _icProcessed.add(node);

                String objFullName = node.getType().getName();
                UserTypeDeclNode mp = _findObjectAST(objFullName, nodetype);
                if (mode == 1)
                    mp.accept(new ICAComputeVisitor(node), new LinkedList());
                else
                    // mode == 2
                    mp.accept(new ICAwaitTestlistComputeVisitor(node),
                            new LinkedList());
            }
        }
    }

    /**
     * Find an mmm object AST node with fully qualified name and its type.
     *
     * @param fullName
     *            fully qualified name of an mmm object
     * @param type
     *            the type of the mmm object
     * @return the AST node or null if not found
     */
    protected static UserTypeDeclNode _findObjectAST(String fullName, int type) {
        // get package name and object name
        String _objName = StringManip.partAfterLast(fullName, '.');
        String _pkgName = StringManip.partBeforeLast(fullName, '.');
        if (_pkgName.equals(_objName))
            _pkgName = "_UNNAMED_";
        if (_objName.indexOf("_$") > 0)
            _objName = _objName.substring(0, _objName.indexOf("_$"));

        // Find AST
        Iterator asts = allSources.iterator();
        UserTypeDeclNode src = null;
        while (asts.hasNext() && src == null) {
            CompileUnitNode ast = (CompileUnitNode) asts.next();
            PackageDecl pkg = (PackageDecl) ast.getDefinedProperty(PACKAGE_KEY);
            if (!pkg.fullName().equals(_pkgName)) {
                continue;
            }

            Iterator defTypes = ast.getDefTypes().iterator();
            while (defTypes.hasNext() && src == null) {
                TreeNode tree = (TreeNode) defTypes.next();
                if (!(tree instanceof UserTypeDeclNode))
                    continue;
                UserTypeDeclNode tn = (UserTypeDeclNode) tree;
                if (!tn.getName().getIdent().equals(_objName))
                    continue;
                switch (type) {
                case MMType.CLASS:
                    if (tn instanceof ClassDeclNode)
                        src = tn;
                    break;
                case MMType.QUANTITY:
                    if (tn instanceof QuantityDeclNode)
                        src = tn;
                    break;
                case MMType.STATEMEDIUM:
                    if (tn instanceof SMDeclNode)
                        src = tn;
                    break;
                case MMType.INTERFACE:
                    if (tn instanceof InterfaceDeclNode)
                        src = tn;
                    break;
                case MMType.NETLIST:
                    if (tn instanceof NetlistDeclNode)
                        src = tn;
                    break;
                case MMType.MEDIUM:
                    if (tn instanceof MediumDeclNode)
                        src = tn;
                    break;
                case MMType.PROCESS:
                    if (tn instanceof ProcessDeclNode)
                        src = tn;
                    break;
                } // end switch
            } // end defType while
        } // end while
        if (src == null) {
            throw new RuntimeException("Can not find the object '" + fullName
                    + "', type was " + MMType.show(type));
        }
        return src;
    }

    /**
     * Find an AST node corresponding to a specific event.
     *
     * @param e
     *            The event
     * @return The AST node
     */
    protected static TreeNode _findEventAST(Event e) {
        String eventName = e.getName();
        MMType mtype = e.getNodeObject().getType();
        String fullName = mtype.getName();
        LinkedList args = new LinkedList();
        args.add(fullName);
        args.add(eventName);

        // Find AST of an event by the fully qualified name
        // of the object and the name of the event
        TreeNode eventAST = null;
        Iterator asts = allSources.iterator();
        CompileUnitNode ast = null;
        ASTLookupVisitor alv = new ASTLookupVisitor();

        while (asts.hasNext() && eventAST == null) {
            ast = (CompileUnitNode) asts.next();
            eventAST = (TreeNode) ast.accept(alv, args);
            if (eventAST instanceof TypeNameNode) {
                mtype = mtype.getSuperClass();
                args.set(0, mtype.getName());
                asts = allSources.iterator();
                eventAST = null;
            } else if (eventAST instanceof AbsentTreeNode) {
                throw new RuntimeException("Can not find the event " + e
                        + " in object " + fullName);
            }
        } // end while
        if (eventAST == null)
            throw new RuntimeException("Can not find the event " + e
                    + " in object " + fullName);

        return eventAST;
    }

    /**
     * Converts a list of strings to a string.
     *
     * @param stringList
     *            a list of strings to be converted
     * @return the converted string
     */
    protected static String _stringListToString(List stringList) {
        Iterator stringItr = stringList.iterator();
        StringBuffer sb = new StringBuffer();

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List) {
                // only use separators for top level
                sb.append(_stringListToString((List) stringObj));
            } else if (stringObj instanceof String) {
                sb.append((String) stringObj);
            } else {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }

        return sb.toString();
    }

    /**
     * convert a LinkedList to string, then save the string to the file pointed
     * by targetName (=sc_main.cpp).
     *
     * @param code
     *            a LinkedList of code
     */
    protected void _flush_code(LinkedList code) {
        try {
            target.write(_stringListToString(code));
            target.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Problem flushing code", ex);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * Add information to the metamodel AST's to be used to modify the generated
     * C++ code for use by the metamodel debugger.
     *
     * @param allSources
     *            The <code>List</code> of <code>CompileUnit</code>s.
     * @param breakpoints
     *            A <code>List</code> to be filled in with breakpoint commands
     *            for initializing the metamodel debugger.
     * @exception RuntimeException
     *                if there are any problems found with the format of the
     *                source code that make debugging impossible.
     */
    private void _addDebuggingInfo(List allSourcesList, List breakpoints) {

        LinkedList errorList = new LinkedList();
        Iterator sources = allSourcesList.iterator();

        while (sources.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) sources.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            ast.accept(new NextLineVisitor(NextLineVisitor.TM_SELF_FIRST),
                    errorList);
            if (pkg.getName() != "lang") {
                // ast.accept(new
                // NextLineVisitor(NextLineVisitor.TM_SELF_FIRST),
                // errorList);
                ast.accept(new BreakpointVisitor(breakpoints), null);
            }
        }

        if (errorList.size() > 0) {
            StringBuffer buf = new StringBuffer();
            Iterator errors = errorList.iterator();
            while (errors.hasNext()) {
                buf.append((String) errors.next());
                buf.append("\n");
            }
            System.err.println("\n" + buf.toString());
            System.exit(1);
            // throw new RuntimeException(buf.toString());
        }
    }

    /**
     * For metamodel debugging. In a gdb debugging session, before execution has
     * begun, gdb cannot set breakpoints by line number in a file until it has
     * set a breakpoint in one of its members. Doing so causes it to load the
     * line-number information for that file. So this method generates a file of
     * class names that can be used to set these bootstrapping breakpoints.
     *
     * @exception IOException
     *                on file i/o problems.
     */
    private void _emitDebuggerBreakpoints(List breakpoints) throws IOException {
        FileWriter bkptsWriter = new FileWriter("bkpts.cmd");
        Iterator iter = breakpoints.iterator();
        while (iter.hasNext()) {
            bkptsWriter.write((String) iter.next() + "\n");
        }
        bkptsWriter.close();
    }

    /**
     * Generate a unique variable ID used in sc_main.cpp
     */
    private int _generateUniqueVarID() {
        variableID++;
        return variableID;
    }

    /**
     * Get all port arrays' dimensions.
     *
     * @param obj
     *            the user's object which may have port arrays
     * @return a LinkedList of all dimensions
     */
    private Object _get_ports_dims(Object obj) throws IllegalAccessException {
        Class cls = obj.getClass();
        LinkedList completeIndex = new LinkedList();

        while (cls != null && !cls.getName().equals("java.lang.Object")) {
            Field f[] = cls.getDeclaredFields();
            AccessibleObject.setAccessible(f, true);
            LinkedList portName = new LinkedList();
            Hashtable nameDims = new Hashtable();
            for (int i = 0; i < f.length; i++) {
                Object objt = f[i].get(obj);
                if (objt != null) {
                    if (objt.getClass().isArray()) {
                        // Get limits of all dimensions, and base type
                        LinkedList index = new LinkedList();
                        if (Array.getLength(objt) == 0) {
                            INode nobj = net.getNode(obj);
                            IPort ipt = nobj.getPort(f[i].getName());
                            if (ipt != null) {
                                if (!(ipt instanceof IPortArray))
                                    throw new RuntimeException("Inner error.");
                                int dim = ipt.getDecl().getDims();
                                for (int j = 0; j < dim; j++)
                                    index.addLast(new Integer(0));
                                String pn = f[i].getName();
                                portName.addLast(pn);
                                nameDims.put(pn, index);
                                continue;
                            }
                        }
                        int dim = 1;
                        index.addLast(new Integer(Array.getLength(objt)));
                        Class comType = objt.getClass().getComponentType();
                        Object comObj = Array.get(objt, 0);
                        while (comType.isArray()) {
                            dim++;
                            index.addLast(new Integer(Array.getLength(comObj)));
                            comType = comType.getComponentType();
                            comObj = Array.get(comObj, 0);
                        }

                        // test if comType is an interface
                        Field testIntfc[] = comType.getDeclaredFields();
                        boolean isInterface = false;
                        for (int j = 0; j < testIntfc.length; j++) {
                            if (testIntfc[j].getName().equals(
                                    "MMMInterfaceIdentifier")) {
                                isInterface = true;
                                break;
                            }
                        }
                        if (isInterface) {
                            String pn = f[i].getName();
                            portName.addLast(pn);
                            nameDims.put(pn, index);
                        }
                    }
                }
            }
            if (!portName.isEmpty()) {
                Object[] pName = portName.toArray();
                Arrays.sort(pName);
                for (int i = 0; i < pName.length; i++) {
                    LinkedList tmp = (LinkedList) nameDims.get(pName[i]);
                    for (int j = 0; j < tmp.size(); j++)
                        completeIndex.addLast(tmp.get(j));
                }
            }
            cls = cls.getSuperclass();
        }

        return completeIndex;
    }

    // /////////////////////////////////////////////////////////////////
    // // private members ////

    /**
     * If <code>_debuggingMetamodel</code> is <code>true</code>, include
     * "#line" directives in generated .cpp code, and configure the generated
     * makefile for using gdb on the executable.
     */
    private boolean _debuggingMetamodel = false;

    /**
     * By default, .h/.cpp files will be generated only for those .mmm files
     * that have newer time stamps than .h/.cpp files do. By specifying -w
     * switch for systemc backend, .h/.cpp files are forced to be generated
     * regardless of the status of .mmm files.
     */
    private boolean _overwrite = false;

    /**
     * Apply interleaving concurrent specific optimization techniques
     */
    private boolean _ic = false;

    /**
     * Apply interleaving concurrent specific optimization techniques
     */
    private Set _icProcessed;

    /**
     * All ASTs
     */
    private static List allSources;

    private int variableID = 0;
}
