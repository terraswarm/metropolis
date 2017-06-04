/* The main class of the meta-model compiler.

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

package metropolis.metamodel;

import metropolis.metamodel.backends.Backend;
import metropolis.metamodel.backends.elaborator.ElaboratorBackend;
import metropolis.metamodel.backends.metamodel.MetaModelBackend;
import metropolis.metamodel.backends.promela.PromelaBackend;
import metropolis.metamodel.backends.runtimetest.RuntimeTestBackend;
import metropolis.metamodel.backends.systemc.SystemCBackend;
import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.nodetypes.CompileUnitNode;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // Compiler
/**
 * The main class of the meta-model compiler. This class contains the main()
 * method that starts the compilation of meta-model file. The user supplies some
 * information about the compilation in the command line:
 * <ul>
 * <li> The class path, i.e. the list of classes where the meta-model files can
 * be found.
 * <li> The list of sources, i.e. the list of source classes that have to be
 * parsed.
 * <li> Some flags to choose the back-end to be used
 * <li> Possibly additional flags to specify more details about the compilation
 * </ul>
 * The compiler loads the basic library classes, parses the sources, performs
 * semantic analysis on them and finally, applies the back-end specified by the
 * user.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: Compiler.java,v 1.80 2006/10/12 20:31:55 cxh Exp $
 */
public class Compiler implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return the value of the Metropolis classpath as a comma separated string.
     *
     * @return The classpath as a string
     */
    public static String classPathToString() {
        StringBuffer results = new StringBuffer();
        Iterator classpaths = _classPath.iterator();
        while (classpaths.hasNext()) {
            if (results.length() > 0) {
                results.append(", ");
            }
            results.append((String) classpaths.next());
        }
        return results.toString();
    }

    /**
     * Main method of the syntax checker. Reads parameters from the command line
     * and invokes the parser to check syntax
     *
     * @param args
     *            The arguments to use. To see all the arguments, run:
     *
     * <pre>
     *   java -classpath $METRO/src metropolis.metamodel.Compiler -help
     * </pre>
     */
    public static void main(String[] args) {

        // Reinitialize static variables each time we run main().
        _backend = new MetaModelBackend();
        _backendName = _METAMODEL;
        _backendArgs = new LinkedList();
        _classPath = new LinkedList();
        _sources = new LinkedList();

        if (_processArguments(args) != 0) {
            return;
        }
        System.out.print("Loading libraries");
        _initCompiler();
        System.out.print("\nLoading sources");
        FileLoader.setMustRebuild(_mustRebuild);
        List compiledAsts = _compileSources();
        FileLoader.setMustRebuild(false);
        System.out.println("\nInvoking backend");

        /***********************************************************************
         * if all ASTs of all packages are needed by backends, and we want them
         * to be passed through arguments, then use the following segment of
         * code.
         *
         * List rP1 = FileLoader.getCompiledSources(1); List rP2 =
         * FileLoader.getCompiledSources(2); List rP3 =
         * FileLoader.getCompiledSources(3); List rP4 =
         * FileLoader.getCompiledSources(4); Hashtable pkgs = new Hashtable();
         * Iterator iter = rP1.iterator(); while (iter.hasNext()) {
         * CompileUnitNode ast = (CompileUnitNode) iter.next(); PackageDecl
         * pdecl = (PackageDecl) ast.getProperty(PACKAGE_KEY); LinkedList llist;
         * if (pkgs.containsKey(pdecl)) llist = (LinkedList) pkgs.get(pdecl);
         * else { llist = new LinkedList(); pkgs.put(pdecl, llist); }
         * llist.addLast(ast); }
         *
         * LinkedList groupedAsts = new LinkedList();
         *
         * CompileUnitNode cun = (CompileUnitNode)compiledAsts.get(0);
         * PackageDecl pdecl = (PackageDecl) cun.getProperty(PACKAGE_KEY);
         * groupedAsts.addLast(pkgs.get(pdecl)); pkgs.remove(pdecl);
         * groupedAsts.addLast(null); groupedAsts.addLast(null);
         *
         * iter = pkgs.keySet().iterator(); while (iter.hasNext()) { PackageDecl
         * pd = (PackageDecl) iter.next(); if
         * (pd.equals(MetaModelLibrary.LANG_PACKAGE)) groupedAsts.set(1,
         * pkgs.get(pd)); else if (pd.equals(MetaModelLibrary.UTIL_PACKAGE))
         * groupedAsts.set(2, pkgs.get(pd)); else
         * groupedAsts.addLast(pkgs.get(pd)); //System.out.println(pd + " has " + //
         * ((LinkedList)pkgs.get(pd)).size()+" ASTs."); }
         *
         * compiledAsts = groupedAsts;
         **********************************************************************/

        // FIXME: Testing template elimination visitor
        /*
         * System.out.println("Invoking template elimination"); CompileUnitNode
         * ast = (CompileUnitNode) compiledAsts.get(0); List classes =
         * ast.getDefTypes(); UserTypeDeclNode aClass = (UserTypeDeclNode)
         * classes.get(0); ObjectDecl decl = (+ObjectDecl)
         * MetaModelDecl.getDecl(aClass.getName()); ArrayList list = new
         * fArrayList();
         *
         * list.add(metropolis.metamodel.nodetypes.IntTypeNode.instance);
         * TreeNode node = TemplateHandler.instantiate(decl, list);
         * classes.add(node);
         */

        _backend.invoke(_backendArgs, compiledAsts);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Back-end module that will be used after front-end. By default, use the
     * meta-model back-end.
     */
    protected static Backend _backend;

    /** List of arguments that will be passed to the back-end. */
    protected static List _backendArgs;

    /**
     * List of paths where meta-model files and libraries can be found.
     */
    protected static List _classPath;

    /**
     * List of source meta-model files that have to be compiled. Files that are
     * imported or extended in these files are automatically compiled as well.
     */
    protected static List _sources;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Compile the source files specified in the command-line. The options
     * specified in the command-line will be used in the compilation.
     *
     * @return A list of the abstract syntax trees produced by the front-end.
     */
    protected static List _compileSources() {
        List compiledAsts = new LinkedList();
        Iterator sourceIter = _sources.iterator();
        while (sourceIter.hasNext()) {
            String source = (String) sourceIter.next();
            // CompileUnitNode ast = FileLoader.loadCompileUnit(
            // _workingDir, source, 4);
            File sourceFile = new File(source);
            if (!sourceFile.isAbsolute()) {
                sourceFile = new File(_workingDir, source);
            }
            CompileUnitNode ast = FileLoader.loadCompileUnit(sourceFile, 4);
            compiledAsts.add(ast);
        }
        return compiledAsts;
    }

    /**
     * Initialize the meta-model compiler. Set the class path values used by the
     * compiler to find meta-model files and libraries.
     */
    protected static void _initCompiler() {
        FileLoader.setClassPath(_classPath);
        // FIXME: MetaModelLibrary has a bug that causes problems when
        // initLibraries() is called twice.
        if (MetaModelLibrary.SYSTEM_PACKAGE == null) {
            MetaModelLibrary.initLibraries();
        }
    }

    /**
     * Print an error message and usually exit If the metro.doNotAutoExit
     * property is not set, then this method prints the message, includes
     * information about the -help argument and then call System.exit(). If the
     * metro.doNotAutoExit property is set, then this method does not exit.
     *
     * @param message
     *            The error message to print.
     */
    protected static void _printError(String message) {
        System.err.println(message);
        System.err.println("  Use -help to get help");
        _possiblyExit();
    }

    /**
     * Print a help message. This text message is longer than the usage message,
     * and provides a short description of all flags that can be used in the
     * meta-model compiler. If the metro.doNotAutoExit property is set then we
     * do not exit. Setting metro.doNotAutoExit to any value allows us to test
     * the code in the regression tests.
     */
    protected static void _printHelp() {
        System.err.println("");
        for (int i = 0; i < _help.length; i++)
            System.err.println(_help[i]);
        System.err.println("");
        _possiblyExit();
    }

    /**
     * Process the arguments from the command line. In case there's is something
     * wrong with the syntax, _printUsage(true) is called, which exits the
     * program.
     *
     * @param args
     *            Command-line options.
     * @return 0 if everything is ok.
     */
    protected static int _processArguments(String[] args) {
        int numArgs = args.length;
        int currentArg = 0;
        if (numArgs == 0) {
            _printHelp();
        }
        while (currentArg < numArgs) {
            if (args[currentArg] == null) {
                throw new RuntimeException("Compiler: Internal error: "
                        + "argument #" + currentArg + " is null.");
            }
            if (args[currentArg].charAt(0) == '-') {
                // Flag
                String current = args[currentArg];
                if (current.length() == 1) {
                    _printError("Error: empty flag '-' " + "in command line");
                    return -16;
                }
                current = current.substring(1);
                currentArg = _processFlag(current, currentArg, numArgs, args);
                if (currentArg < 0) {
                    // _processFlag had a problem.
                    return currentArg;
                }
            } else {
                if (_backendName.equals(_ELABORATOR) && !_minusTopFound) {
                    // First non-option arg is the netlist.
                    _backendArgs.add(args[currentArg++]);
                }
                // Source file
                while (currentArg < numArgs) {
                    String current = args[currentArg++];
                    if (current.charAt(0) == '-') {
                        _printError("Error: all flags must appear"
                                + " before source files," + " arg was '"
                                + current + "'");
                        return -15;
                    }
                    _sources.add(current);
                }
            }
        }
        if (_sources.size() == 0) {
            _printError("Error: source files expected");
            return -14;
        }
        return 0;
    }

    /**
     * Process one flag from the command line. In case there's is something
     * wrong with the syntax, _printError() is called, which exits the program.
     *
     * @param flag
     *            Identifier of the flag.
     * @param pos
     *            Position of the flag in the array of options.
     * @param max
     *            Total number of command-lne options.
     * @param args
     *            Array of command-line options.
     * @return The next command-line option to be examined.
     */
    protected static int _processFlag(String flag, int pos, int max,
            String[] args) {

        if (flag.equals("h") || flag.equals("?") || flag.equals("help")) {
            // Print a help message
            _printHelp();
            return -1;
        } else if (flag.equals(_METAMODEL)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = new MetaModelBackend();
            return pos + 1;
        } else if (flag.equals("classpath")) {
            // Read all paths in the class-path list
            String file;
            int classPathInsertIndex = 0;
            do {
                // Two possible scenarios:
                // 1) path
                // 2) path1 : path2
                // If we don't find ':' after current path,
                // then it is the last one.
                if (pos + 1 >= max) {
                    _printError("Error: wrong classpath, path " + "expected");
                    return -2;
                }
                pos++;
                file = args[pos];
                if (file.equals(":")) {
                    _printError("Error: ':' used where a path was"
                            + " expected");
                    return -3;
                }
                StringTokenizer tokenizer = new StringTokenizer(file,
                        File.pathSeparator);
                while (tokenizer.hasMoreTokens()) {
                    // Each time we see a new -classpath arg, we start
                    // inserting the elements from that arg at the beginning
                    // of the list. This is necessary so that we can
                    // override the default -classpath flag set in
                    // $METRO/bin/systemc (et al). For example,
                    // if METRO_CLASSPATH is set to /foo, then
                    // systemc -classpath /bar : /bif should set
                    // the classPath List to /bar /bif /foo,
                    // which allows us to override METRO_CLASSPATH
                    _classPath.add(classPathInsertIndex++, tokenizer
                            .nextToken());
                }
                if (pos + 1 >= max) {
                    _printError("Error: source files expected after"
                            + " class-paths");
                    return -4;
                }
                pos++;
                file = args[pos];
            } while (file != null && file.equals(":"));
            return pos;
        } else if (flag.equals(_SIMULATOR)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = _instantiateBackend("metropolis.metamodel.backends.simulator.SimulatorBackend");
            return pos + 1;
        } else if (flag.equals(_JAVASIM)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = _instantiateBackend("metropolis.metamodel.backends.javasim.JavasimBackend");
            return pos + 1;
        } else if (flag.equals(_SYSTEMC)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = new SystemCBackend();
            return pos + 1;
        } else if (flag.equals(_CFA)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = _instantiateBackend("metropolis.metamodel.backends.cfa.CFABackend");
            return pos + 1;
        } else if (flag.equals(_CPP)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = _instantiateBackend("metropolis.metamodel.backends.cpp.CPPBackend");
            return pos + 1;
        } else if (flag.equals("top")) {
            if (_backendName.equals(_SYSTEMC)
                    || _backendName.equals(_ELABORATOR)
                    || _backendName.equals(_JAVASIM)) {
                if (pos == max - 1) {
                    _printError("Error: -top must be followed by the "
                            + "fully qualified name of the net-initiator netlist");
                    return -6;
                } else {
                    _backendArgs.add(args[++pos]);
                    _minusTopFound = true;
                    return pos + 1;
                }
            } else {
                return pos + 1;
            }
        } else if (flag.equals("w")) {
            _backendArgs.add(args[pos]);
            return pos + 1;
        } else if (flag.equals("noic")) {
            _backendArgs.add(args[pos]);
            return pos + 1;
        } else if (flag.equals("java")) {
            if (_backendName.equals(_SYSTEMC)
                    || _backendName.equals(_ELABORATOR)
                    || _backendName.equals(_JAVASIM)) {
                if (pos == max - 1) {
                    _printError("Error: -java must be followed by the "
                            + "full path of java");
                    return -7;
                } else {
                    _backendArgs.add("-java");
                    _backendArgs.add(args[++pos]);
                    return pos + 1;
                }
            } else { // For other backends which need -java, modify here.
                _printError("Warning: -java must appear after -systemc "
                        + "or -javasim or -elaborator. Ignored.");
                return -8;
            }
            // return pos + 1;
        } else if (flag.equals("javac")) {
            if (_backendName.equals(_SYSTEMC)
                    || _backendName.equals(_ELABORATOR)
                    || _backendName.equals(_JAVASIM)) {
                if (pos == max - 1) {
                    _printError("Error: -javac must be followed by the "
                            + "full path of javac");
                    return -9;
                } else {
                    _backendArgs.add("-javac");
                    _backendArgs.add(args[++pos]);
                    return pos + 1;
                }
            } else { // For other backends which need -javac, modify here.
                _printError("Warning: -javac must appear after -systemc "
                        + " or -javasim or -elaborator. Ignored.");
                return -10;
            }
            // return pos + 1;
        } else if (flag.equals(_ELABORATOR)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = new ElaboratorBackend();
            if (pos == max - 1) {
                _printError("Error: elaborator needs the fully "
                        + "qualified name of the net-initiator netlist");
                return -11;
            } else {
                /* elaborator now has "-javac <compiler_path>" option. */
                // _backendArgs.add(args[++pos]);
                return pos + 1;
            }
        } else if (flag.equals(_PROMELA)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = new PromelaBackend();
            return pos + 1;
        } else if (flag.equals(_RUNTIMETEST)) {
            _backendArgs.clear();
            _backendName = flag;
            _backend = new RuntimeTestBackend();
            if (pos >= max - 2) {
                _printError("Error: runtimetest needs an option and the fully"
                        + " qualified name of the net-initiator netlist");
                return -12;
            } else {
                _backendArgs.add(args[++pos]);
                _backendArgs.add(args[++pos]);
                return pos + 1;
            }
            // } else if (flag.equals(_PROMETHEUS)) {
            // _backendArgs.clear();
            // _backendName = flag;
            // _backend = new PrometheusBackend();
            // return pos + 1;
        } else if (flag.equalsIgnoreCase("mmdebug")) {
            if (_backendName.equals(_SYSTEMC)) {
                _backendArgs.add(args[pos]);
                _mustRebuild = true;
                return pos + 1;
            } else {
                _printError("\"-mmdebug\" must appear after "
                        + "\"-systemc\", only supported for "
                        + "\"-systemc\".  Ignored.");
                return -17;
            }
        } else if (flag.equals("dir")) {
            if (_backendName.equals(_ELABORATOR)) {
                _workingDir = args[++pos];
                _backendArgs.add("-dir");
                _backendArgs.add(_workingDir);
                return pos + 1;
            } else {
                _printError("\"-dir\" only supported for \"-elaborator\".  "
                        + "Ignored.");
                return -19;
            }
        } else if (flag.equalsIgnoreCase("dumpast")) {
            if (_backendName.equals(_METAMODEL)) {
                _backendArgs.add(args[pos]);
                return pos + 1;
            } else {
                _printError("\"-dumpast\" only supported for "
                        + "\"-metamodel\".  Ignored.");
                return -18;
            }
        } else {
            _printError("Error: unknown flag '-" + flag + "'");
            return -13;
        }
        // return max;
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    // Instantiate a backend using reflection.
    private static Backend _instantiateBackend(String classname) {
        Backend backend;
        try {
            Class simulatorBackendClass = Class.forName(classname);
            backend = (Backend) simulatorBackendClass.newInstance();
        } catch (Throwable throwable) {
            // FIXME: this class needs to handle exceptions better.
            throw new RuntimeException("Failed to find or instantiate '"
                    + classname + "'", throwable);
        }
        return backend;
    }

    // If the metro.doNotAutoExit property is set, then do not exit,
    // otherwise, exit. This method is used by the test suite.
    private static void _possiblyExit() {
        if (System.getProperty("metro.doNotAutoExit") == null
                || System.getProperty("metro.doNotAutoExit").length() == 0) {
            System.exit(1);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * Help message. This message is printed when the 'help' flag is used, with
     * an end of line after each element of the array,
     */
    private static final String[] _help = {
            "class metropolis.metamodel.Compiler:",
            "       A compiler for the meta-model language, the internal representation",
            "       used in the Metropolis design framework",
            "",
            "Usage: java metropolis.metamodel.Compiler [options] source_files",
            "         ",
            "where options include:",
            "   -? -help -h                Display this help message",
            "   -classpath <classpath>     Specify where to find user class files",
            "   -metamodel [-dumpast]      Use the metamodel back-end (default)",
            "                              -dumpast prints a dump of the AST to STDOUT",
            "   -javasim [-top <netinit>] [-java <path>/java] [-javac <path>/javac]",
            "                              Generate java simulation code (semantics 3.0)",
            "                              [javasim not shipped in Metropolis 1.0]",
            "   -cpp                       Generate C++ simulation code",
            "                              [cpp not shipped in Metropolis 1.0]",
            "   -simulator                 Generate java simulation code",
            "                              [simulator not shipped in Metropolis 1.0]",
            "   -systemc [-top <netinit>] [-java <path>/java] [-javac <path>/javac]",
            "            [-w] [-noic] [-mmdebug]",
            "                              Generate SystemC simulation code",
            "                              -top Fully qualified top level netlist name",
            "                              -java/javac Java virtual machine and",
            "                                 java compiler",
            "                              -w Regenerate systemc code regardless the time",
            "                                 stamps of mmm and systemc files",
            "                              -noic No interleaving concurrent",
            "                                 specific optimization",
            "                              -mmdebug Support mmm level debugging",
            "                                  in simulation",
            "   -elaborator [-dir <dir>] [-java <path>/javac] <netinit>",
            "                              Compute the structure of the network",
            "                              -dir specify the directory to create elaboration",
            "                                 products in (default is current directory)",
            "                              -javac specify the java compiler",
            "   -promela                   Generate Promela/Spin code",
            "   -runtimetest runtime/constraint",
            "                              runtime Test the runtime library",
            "                              constraint Test the constraint",
            "                              elaboration and show the results",
            // " -prometheus Perform compositional verification",
            "   -cfa                       Generate a CFA; Visual, KISS,",
            "                              and RML representations",
            "                              [cfa not shipped in Metropolis 1.0]", };

    private static boolean _mustRebuild = false;

    // For backward compatibility with -elaborate, after adding -top
    // for consistency with usage of other backends:
    private static boolean _minusTopFound = false;

    private static String _workingDir = null;

    private static String _backendName = null;

    private static final String _METAMODEL = "metamodel";

    private static final String _ELABORATOR = "elaborator";

    private static final String _SYSTEMC = "systemc";

    private static final String _SIMULATOR = "simulator";

    private static final String _JAVASIM = "javasim";

    private static final String _CFA = "cfa";

    private static final String _CPP = "cpp";

    private static final String _PROMELA = "promela";

    private static final String _RUNTIMETEST = "runtimetest";

    // private static final String _PROMETHEUS = "prometheus";
}
