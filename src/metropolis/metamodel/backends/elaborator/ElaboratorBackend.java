/* A back-end that computes the structure of the network of processes and
 media described by a set of meta-model files.

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

 */

package metropolis.metamodel.backends.elaborator;

import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.Backend;
import metropolis.metamodel.frontend.ClassDecl;
import metropolis.metamodel.frontend.FileLoader;
import metropolis.metamodel.frontend.FormalParameterDecl;
import metropolis.metamodel.frontend.MetaModelDecl;
import metropolis.metamodel.frontend.MetaModelLibrary;
import metropolis.metamodel.frontend.MethodDecl;
import metropolis.metamodel.frontend.NetlistDecl;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.frontend.PortDecl;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.runtime.Network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // ElaboratorBackend
/**
 * A back-end that computes the structure of the network of processes, media and
 * schedulers described by a set of meta-model files. This back-end translates
 * meta-model files into Java code. These set of Java files is compiled and
 * executed by the JVM, producing a data structure that describes the structure
 * of the network of processes and media.
 *
 * @author Robert Clariso
 * @version $Id: ElaboratorBackend.java,v 1.84 2006/10/12 20:32:24 cxh Exp $
 */
public class ElaboratorBackend implements Backend {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /** Create a new elaborator backend. */
    public ElaboratorBackend() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Implement method 'invoke()' from the Backend interface. The user should
     * specify a single argument, the name of the top-level netlist or
     * net-initiator. This method calls the JavaTranslationVisitor to generate
     * executable Java code that creates and checks the network. The result of
     * this execution is a data structure that describes the resulting network.
     *
     * @param args
     *            List of arguments; expected a single argument, a string with
     *            the name of the net-initiator.
     * @param sources
     *            List of all compiled ASTs.
     */
    public void invoke(List args, List sources) {
        // Check number of arguments
        int size = args.size();
        if ((size != 1) && (size != 3) && (size != 5) && (size != 7)) {
            throw new RuntimeException("Wrong number of arguments (" + size
                    + "), should be 1, 3, 5 or 7: " + _usage);

        }

        // Check arguments
        String netInitiator = null;
        while (args.size() > 0) {
            String arg0 = (String) args.get(0);
            if (arg0.equals("-java")) {
                if (args.size() < 2) {
                    throw new RuntimeException(
                            "Wrong arguments for elaboration, should be: "
                                    + _usage);
                } else {
                    _javaPath = (String) args.get(1);
                    args.remove(0);
                    args.remove(0);
                }
            } else if (arg0.equals("-javac")) {
                if (args.size() < 2) {
                    throw new RuntimeException(
                            "Wrong arguments for elaboration, should be: "
                                    + _usage);
                } else {
                    _javacPath = (String) args.get(1);
                    args.remove(0);
                    args.remove(0);
                }
            } else if (arg0.equals("-dir")) {
                if (args.size() < 2) {
                    throw new RuntimeException(
                            "Wrong arguments for elaboration, should be: "
                                    + _usage);
                } else {
                    _workingDirPath = (String) args.get(1);
                    args.remove(0);
                    args.remove(0);
                }
            } else if (arg0.startsWith("-")) {
                throw new RuntimeException(
                        "Wrong arguments for elaboration, should be: " + _usage);
            } else {
                netInitiator = arg0;
                args.remove(0);
            }
        }
        /*
         * if (size == 1) { netInitiator = (String) args.get(0); } else if (size ==
         * 3) { String arg0 = (String) args.get(0); String arg1 = (String)
         * args.get(1); String arg2 = (String) args.get(2); if
         * (arg0.equals("-javac")) { netInitiator = arg2; _javacPath = arg1; }
         * else if (arg0.equals("-java")) { netInitiator = arg2; _javaPath =
         * arg1; } else { throw new RuntimeException("Wrong arguments for
         * elaboration: " + _usage); } } else if (size == 5) { String arg0 =
         * (String) args.get(0); String arg1 = (String) args.get(1); String arg2 =
         * (String) args.get(2); String arg3 = (String) args.get(3); String arg4 =
         * (String) args.get(4); if (arg0.equals("-javac") &&
         * arg2.equals("-java")) { _javacPath = arg1; _javaPath = arg3;
         * netInitiator = arg4; } else { throw new RuntimeException("Wrong
         * arguments for elaboration, " + "should be: " + _usage); } }
         */

        // Check that the net-initiator exists and that it is a netlist
        // Get a public constructor of that netlist with 0 arguments.
        _findNetInitiatorDecl(netInitiator);

        // Create a temporary directory for Java elaboration sources.
        System.out.println("  Choosing a temporary directory...");
        _createTmpDir();

        // Find environment values involved in elaboration: the path of
        // the Java compiler and interpreter and the Java class-path.
        System.out.println("  Finding java compiler and interpreter...");
        _findClassPath();
        _findJavaCompiler();
        _findJavaInterpreter();

        // Create a class that will store the "main" method of the
        // elaboration
        System.out.println("  Generating Java elaboration code...");

        // xichen template
        // List types = _findTypes();
        // _createMainClass(types);

        // Get the list of all sources loaded by the FileLoader
        // All sources should be loaded until pass 2, so that they
        // are translated by the elaboration backend
        List allSources = FileLoader.getCompiledSources(1);
        Iterator iter = allSources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            if (pkg == MetaModelLibrary.UTIL_PACKAGE) {
                continue;
            }
            String fileName = (String) ast.getProperty(IDENT_KEY);
            FileLoader.loadCompileUnit(fileName, 2);
        }

        // xichen template
        List types = _findAllTypes(allSources);
        _createMainClass(types);

        allSources = FileLoader.getCompiledSources(2);
        iter = allSources.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            PackageDecl pkg = (PackageDecl) ast.getProperty(PACKAGE_KEY);
            if (pkg == MetaModelLibrary.UTIL_PACKAGE) {
                iter.remove();
            }
        }

        List javaFiles = _translateSources(allSources);

        System.out.println("  Compiling elaboration code...");
        _compileJavaFiles(javaFiles);

        System.out.println("  Running elaboration code...");
        _elaborate();

        String inputFileName = _tmpRoot.toString() + File.separator + "NET";
        try {
            CustomClassLoader loader = null;
            if (_classLoaderDelegate == null) {
                loader = new CustomClassLoader(_tmpRoot.toString());
            } else {
                loader = new CustomClassLoader(_tmpRoot.toString(),
                        _classLoaderDelegate);
            }
            FileInputStream in = new FileInputStream(inputFileName);
            try {
                CustomObjectInputStream ois = new CustomObjectInputStream(in,
                        loader);
                // Update the network with the elaborated net.
                Network.net = Network.restore(ois);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to restore network "
                        + "using a CustomObjectInputStream based on '"
                        + inputFileName + "'", ex);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading the elaborated network '"
                    + inputFileName + "'", ex);
        }
    }

    /**
     * Set a <code>ClassLoader</code> for the <code>CustomClassLoader</code>
     * to delegate to instead of the default system <code>ClassLoader</code>.
     * This is here to deal with the complexities of running from an Eclipse
     * plugin.
     *
     * @param delegate
     *            the <code>ClassLoader</code> to set.
     */
    public static void setClassLoaderDelegate(ClassLoader delegate) {
        _classLoaderDelegate = delegate;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The current classpath used by the java compiler. */
    protected static String _classPath = null;

    /**
     * A ClassLoader, other than the default system classloader, to use with the
     * CustomClassLoader.
     */
    protected static ClassLoader _classLoaderDelegate = null;

    /** The declaration of the net-initiator public constructor. */
    protected MethodDecl _constructorDecl = null;

    /** The path of the Java compiler "javac". */
    protected static String _javacPath = null;

    /** The path of the Java interpreter "java". */
    protected static String _javaPath = null;

    /** The declaration of the net-initiator netlist. */
    protected NetlistDecl _netInitDecl = null;

    /** The temporary directory where we store the elaboration code. */
    protected File _tmpRoot = null;

    /** The specified home for _tmpRoot. */
    protected String _workingDirPath = null;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Compile the Java files generated during the translation. Compile also the
     * automatically generated file with a "main".
     *
     * @param classes
     *            A list of the names of all the Java files that will be needed
     *            during this elaboration.
     */
    protected void _compileJavaFiles(List classes) {
        // _javacPath might have spaces in it, so we create a command list
        // and then convert it to an array.

        List commandList = new LinkedList();
        commandList.add(_javacPath);
        commandList.add("-g");

        // Build the classpath string: add the temporary directory
        String classpath = _classPath + File.pathSeparator
                + _tmpRoot.toString();
        commandList.add("-classpath");
        commandList.add(classpath);

        commandList.addAll(classes);

        String mainFile = _tmpRoot.toString() + File.separator + "elaboration"
                + File.separator + "Initiator.java";
        commandList.add(mainFile);

        String[] commandArray = (String[]) commandList
                .toArray(new String[commandList.size()]);

        // Execute the command in a separate process.
        _execute(commandArray, true);
    }

    /**
     * Write a dummy java file that will be compiled to test the Java compiler.
     *
     * @return The name of the dummy java file.
     */
    protected String _createDummyClass() {
        File dummy = new File(_tmpRoot, "Dummy.java");
        try {
            FileWriter target = new FileWriter(dummy);
            target.write("public class Dummy { int i; void a() {} }\n");
            target.flush();
            target.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return dummy.toString();
    }

    /**
     * Create the Java class that will contain the "main" procedure of the
     * elaboration, called "elaboration.Initiator". This main class will
     * initialize the data structures used during the elaboration. Then, it will
     * start elaboration by calling the constructor of the net-initiator
     * netlist. Finally, it will save the contents of the elaborated network in
     * a file in the temporary directory.
     *
     * @param allTypes
     *            A list of the types. The list consists of ObjectDecl objects
     */
    protected void _createMainClass(List allTypes) {
        // The class with the main method will be "elaboration/Initiator"
        File pkgFile = new File(_tmpRoot, "elaboration");
        if (!pkgFile.mkdir()) {
            throw new RuntimeException("Cannot create directory '" + pkgFile
                    + "' for main method for the main class of elaboration.");
        }

        // Create the Java file and write the contents
        try {
            String mainName = pkgFile.toString() + File.separator
                    + "Initiator.java";
            FileWriter main = new FileWriter(mainName);
            main.write("package elaboration;\n\n");
            main.write("import java.util.*;\n");
            String netInitName = _netInitDecl.fullName();
            if (netInitName.indexOf('.') > -1) {
                main.write("import " + _netInitDecl.fullName() + ";\n");
            } else {
                main.write("import UNNAMED_PACKAGE." + _netInitDecl.fullName()
                        + ";\n");
            }
            main.write("import metamodel.lang.Node;\n");
            main.write("import metropolis.metamodel.runtime.*;\n\n");
            main.write("public class Initiator {\n\n");
            main.write("  private static final String resultFile = \n");
            // FIXME: could do better than replace here
            main.write("  \"" + _tmpRoot.toString().replace('\\', '/')
                    + "/NET\";\n\n");
            // main.write(" \"" + _tmpRoot.toString() + File.separator
            // + "NET\";\n\n");

            main.write("  public static void main(String args[]) {\n");
            main.write("    createTypes();\n");
            main.write("    Object netinitiator = null;\n");
            // main.write(" try {\n");
            main.write("    netinitiator = new " + _netInitDecl.getName()
                    + "(\"top_level_netlist\");\n\n");
            // main.write(" } catch (CloneNotSupportedException e){\n");
            // main.write(" System.err.println(\"CloneNotSupportedException
            // thrown\");\n");
            // main.write(" System.exit(1);\n");
            // main.write(" }\n");
            main.write("    Network.net.setNetInitiator("
                    + "Network.net.getNetlist(netinitiator));\n\n");

            main
                    .write("    // Invoke postElaborate() in all the object nodes\n");
            main
                    .write("    Iterator iterNode = Network.net.getNodeInstances();\n");
            main.write("    while (iterNode.hasNext()){\n");
            main.write("        Node node = (Node)iterNode.next();\n");
            main.write("        node.postElaborate();\n");
            main.write("    }\n\n");

            main.write("    // Save the network to disk\n");
            main.write("    Network.net.save(resultFile);\n\n");
            // xichen_loc_beg
            main.write("    // Generate constraint checkers\n");
            main.write("    Network.net.generateLOCCheckers();\n");
            // xichen_loc_end
            main.write("  }\n\n");
            main.write("  public static void createTypes() {\n");
            main.write("    // Create all types\n");
            _writeTypeCreation(main, allTypes);
            main.write("    // Set inheritance information\n");
            _writeInheritanceRelations(main, allTypes);
            main.write("    // Add port information\n");
            _writePortInformation(main, allTypes);
            main.write("  } \n");
            main.write("\n}\n");
            main.flush();
            main.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Create a directory for the Java translated code of a meta-model package.
     * The Java for all meta-model classes in the package will be located in
     * this directory.
     *
     * @param pkgDecl
     *            The package declaration for the package.
     * @return The directory created for the files in that package.
     */
    protected File _createPkgDir(PackageDecl pkgDecl) {
        String pkgName;
        if (pkgDecl == MetaModelLibrary.UNNAMED_PACKAGE) {
            pkgName = "UNNAMED_PACKAGE";
        } else {
            pkgName = pkgDecl.fullName(File.separatorChar);
        }
        File pkgDir = new File(_tmpRoot, pkgName);
        if (!pkgDir.exists()) {
            if (!pkgDir.mkdirs()) {
                throw new RuntimeException("Cannot create a temporary "
                        + " directory to elaborate package "
                        + pkgDecl.fullName());
            }
        }
        return pkgDir;
    }

    /**
     * Create a temporary directory where the Java generated code will be
     * stored. This directory will be located in one of the temporary
     * directories of the machine (/tmp, /var/tmp, ...) or current directory, or
     * user home directory. The created directory is stored in the field
     * _tmpRoot.
     */
    protected void _createTmpDir() {
        // Find a directory where we can create our temporary dir.
        // Our choices will be, in order, the temporary directory
        // used by Java, "/tmp", "/var/tmp", current directory,
        // and the user home directory.
        boolean valid = false;
        File tmp = null;
        String cand[] = { System.getProperty("java.io.tmpdir"),
                File.separator + "tmp",
                File.separator + "var" + File.separator + "tmp",
                System.getProperty("user.dir"), System.getProperty("user.home") };
        for (int i = 0; (!valid) && (i < cand.length); i++) {
            if (cand[i] == null) {
                continue;
            }
            valid = _isValidTmpDir(cand[i]);
        }
        if (!valid) {
            throw new RuntimeException("Cannot find a directory where "
                    + "the Java elaboration code can be stored");
        }

        // Well, after all that, tmp is still null.
        // So, we're using the directory the JVM was started in.
        // ... unless one was specified on the command line:

        if (_workingDirPath != null) {
            tmp = new File(_workingDirPath);
        }

        // Create our temporary directory for the Java code.
        File dir = null;
        boolean success = false;
        for (int i = 0; !success; i++) {
            dir = new File(tmp, "metro" + i + ".tmp");
            success = dir.mkdir();
            if (i == 100) {
                throw new RuntimeException("Cannot create a temporary "
                        + "directory for the elaboration code (in " + tmp
                        + ").  Maybe " + " there are too many temporary dirs?");
            }
        }
        _tmpRoot = dir.getAbsoluteFile();
    }

    /**
     * Execute the Java elaborated files. Call the Java interpreter and to
     * execute the elaborated code.
     */
    protected void _elaborate() {
        // Add the temporary directory to the current classpath
        String classpath = _classPath + File.pathSeparator
                + _tmpRoot.toString();

        // Build the command to be executed
        String command[] = { _javaPath, "-classpath", classpath,
                "elaboration.Initiator" };

        // Execute the command in a separate process
        _execute(command, true);
    }

    /**
     * Notify an error produced during elaboration.
     *
     * @param msg
     *            Error message to be provided to the user.
     * @deprecated Use ElaborationException instead of _elaborationError so that
     *             the stack trace that is printed is more useful.
     */
    protected void _elaborationError(String msg) {
        ElaborationException ex = new ElaborationException(msg, _javacPath,
                _javaPath, _classPath, _tmpRoot);
        throw new RuntimeException(ex);
    }

    /**
     * Execute a command in a separate process. Wait for the command to
     * terminate, and then, if the executed process ended in an error, display
     * an error to the user.
     *
     * @deprecated Use {@link #_execute(String [], boolean)} instead so that
     *             pathnames with spaces will work properly.
     * @param command
     *            Command to be executed.
     * @param showError
     *            True if we must display information in case of error.
     */
    protected void _execute(String command, boolean showError) {
        // Execute and wait for the process
        Runtime runtime = Runtime.getRuntime();
        Process other = null;
        try {
            other = runtime.exec(command);
        } catch (IOException ex) {
            throw new ElaborationException("Execution of:\n" + command
                    + "\nfailed", _javacPath, _javaPath, _classPath, _tmpRoot,
                    ex);
        }
        _parseExecute(command, showError, other);
    }

    /**
     * Execute a command in a separate process. Wait for the command to
     * terminate, and then, if the executed process ended in an error, display
     * an error to the user.
     *
     * @param commandArray
     *            Command to be executed.
     * @param showError
     *            True if we must display information in case of error.
     */
    protected void _execute(String[] commandArray, boolean showError) {
        // Execute and wait for the process
        Runtime runtime = Runtime.getRuntime();
        Process other = null;

        String commandString = null;

        if (commandArray.length <= 0) {
            commandString = "";
        } else {
            StringBuffer command = new StringBuffer(commandArray[0]);
            for (int i = 1; i < commandArray.length; i++) {
                command.append(" " + commandArray[i]);
            }
            commandString = command.toString();
        }

        try {
            other = runtime.exec(commandArray);
        } catch (IOException ex) {
            throw new ElaborationException("Execution of:\n" + commandString
                    + "\nfailed", _javacPath, _javaPath, _classPath, _tmpRoot,
                    ex);
        }

        _parseExecute(commandString, showError, other);
    }

    /**
     * Find the current classpath used by the java interpreter that is running.
     * This can be found out from a property of class System. The classpath will
     * be stored in the field classPath of this class.
     */
    protected void _findClassPath() {
        // Check if the classpath has already been found
        if (_classPath != null) {
            return;
        }

        _classPath = System.getProperty("java.class.path");
        if (_classPath == null) {
            throw new RuntimeException("Cannot find the Java classpath");
        }
        if (_classPath.equals("")) {
            _classPath = ".";
        }
    }

    /**
     * Find the path where the java compiler can be found. WARNING, although
     * this method has been implemented to be as portable as possible, it might
     * fail to find the java compiler if it not located in a very standard
     * location such as "/usr/bin/" or "/usr/local/bin". The location of the
     * java compiler will be stored in
     */
    protected void _findJavaCompiler() {
        // Check if the compiler has already been found
        if (_javacPath != null) {
            return;
        }

        // Create the dummy file to be compiled
        String dummy = _createDummyClass();

        // Try to find 'javac' in a set of typical locations
        String s = File.separator;
        String home = System.getProperty("java.home");
        String cand[] = { s + "usr" + s + "bin",
                s + "usr" + s + "local" + s + "bin",
                s + "usr" + s + "local" + s + "jdk" + s + "bin",
                home + s + ".." + s + "bin" };
        for (int i = 0; i < cand.length; i++) {
            String fileName = cand[i] + s + "javac";
            if (_isValidJavaCompiler(fileName, dummy)) {
                _javacPath = fileName;
                return;

            }
        }

        // Desperate attemp: try to find javac in a subdirectory
        // called /usr/local/jdk*.
        File usrLocal = new File(s + "usr" + s + "local");
        File files[] = usrLocal.listFiles();
        if (files != null) {
            for (int j = 0; j < files.length; j++) {
                File file = files[j];
                if (!file.isDirectory()) {
                    continue;
                }
                if (file.getName().startsWith("jdk")) {
                    String fileName = file.toString() + s + "bin" + s + "javac";
                    if (_isValidJavaCompiler(fileName, dummy)) {
                        _javacPath = fileName;
                        return;
                    }
                }
            }
        }

        // Give up search
        throw new RuntimeException("Cannot find a Java compiler");
    }

    /**
     * Find the path where the java interpreter can be found. Ideally, it can be
     * found using a property of class System. The path where the Java
     * interpreter can be found will be stored in the field javaPath of this
     * class.
     */
    protected void _findJavaInterpreter() {
        // Check if the interpreter has already been found
        if (_javaPath != null) {
            return;
        }

        String path = System.getProperty("java.home");
        if (path == null) {
            throw new RuntimeException("Cannot find the Java interpreter");
        }
        _javaPath = path + File.separator + "bin" + File.separator + "java";
        File interpreter = new File(_javaPath);
        if (!interpreter.exists()) {
            interpreter = new File(_javaPath + ".exe");
            if (!interpreter.exists()) {
                throw new RuntimeException("Cannot find the Java interpreter");
            } else {
                _javaPath = _javaPath + ".exe";
            }
        }
    }

    /**
     * Find the declaration of the net-initiator netlist. Cause an error if the
     * net-initiator error does not exist or if it is not a netlist.
     * Furthermore, check that the net-initiator is public, non-abstract and
     * that it has a public constructor with 0 arguments. Set the values of the
     * net-initiator decl and the constructor declaration.
     *
     * @param netInitName
     *            Fully qualified name of the net-initiator.
     */
    protected void _findNetInitiatorDecl(String netInitName) {
        // Check where we should look for the net-initiator
        int idx = netInitName.lastIndexOf('.');
        ObjectDecl decl;
        if (idx == -1) {
            // Find the netlist in the unnamed package
            decl = MetaModelLibrary.UNNAMED_PACKAGE.getUserType(netInitName);
            if (decl == null) {
                throw new RuntimeException("Specified net-initiator '"
                        + netInitName + "' not found in the unnamed package.");
            }
            if (decl.category != CG_NETLIST) {
                throw new RuntimeException("Specified net-initiator '"
                        + netInitName + "' is not a netlist");
            }
        } else {
            // Find the package where the netlist is stored
            StringTokenizer tk = new StringTokenizer(netInitName, ".");
            PackageDecl pkg = MetaModelLibrary.SYSTEM_PACKAGE;
            String name = "";
            while (tk.hasMoreTokens()) {
                name = tk.nextToken();
                if (!tk.hasMoreTokens()) {
                    break;
                }
                pkg = pkg.getSubPackage(name);
                if (pkg == null) {
                    throw new RuntimeException("Package '" + name + "' not "
                            + "found for net-initiator '" + netInitName + "'");
                }
            }
            decl = pkg.getUserType(name);
            if (decl == null) {
                throw new RuntimeException("Specified net-initiator '" + name
                        + "' not found in package '" + pkg.fullName()
                        + "', package: " + pkg.description());
            }
            if (decl.category != CG_NETLIST) {
                throw new RuntimeException("Specified net-initiator '"
                        + netInitName + "' is not a netlist");
            }
        }

        // Check that the netlist is public, non-abstract and that it has
        // a public constructor with exactly 0 arguments
        int mods = decl.getModifiers();
        if ((mods & ABSTRACT_MOD) != 0) {
            throw new RuntimeException("Specified net-initiator '"
                    + netInitName + "' shouldn't be abstract");
        }
        if ((mods & PUBLIC_MOD) == 0) {
            throw new RuntimeException("Specified net-initiator '"
                    + netInitName + "' should be public");
        }
        ScopeIterator allCons = decl.getConstructors();
        while (allCons.hasNext()) {
            MethodDecl cons = (MethodDecl) allCons.next();
            if ((cons.getModifiers() & PUBLIC_MOD) == 0) {
                continue;
            }
            // if (cons.getParams().size() > 0) continue;
            if (cons.getParams().size() != 1) {
                continue;
            }
            FormalParameterDecl par = (FormalParameterDecl) cons.getParams()
                    .get(0);
            TypeNode type = par.getType();
            if (type instanceof TypeNameNode) {
                String name = ((TypeNameNode) type).getName().getIdent();
                if (!name.equals("String")) {
                    continue;
                }
            } else {
                continue;
            }
            _constructorDecl = cons;
            break;
        }
        if (_constructorDecl == null) {
            throw new RuntimeException("Net-initiator '" + netInitName
                    + "' does not have a public constructor with 0 arguments.");
        }
        _netInitDecl = (NetlistDecl) decl;
    }

    /**
     * Compute a list of all subpackages of a given package.
     *
     * @param pkg
     *            Package.
     * @return A list of all subpackages of a given package.
     */
    protected List _findSubPkgs(PackageDecl pkg) {
        LinkedList packages = new LinkedList();
        packages.add(pkg);
        Scope pkgScope = pkg.initGetScope();
        ScopeIterator iter = pkgScope.allLocalDecls(CG_PACKAGE);
        while (iter.hasNext()) {
            PackageDecl sub = (PackageDecl) iter.next();
            packages.addAll(_findSubPkgs(sub));
        }
        return packages;
    }

    /**
     * Find all the types declared in the program that should appear in the Java
     * language. The list will have no duplicate items.
     *
     * @return A list of all ObjectDecl that are used in the metamodel. program.
     */
    protected List _findTypes() {
        List types = new LinkedList();
        // Compute the list of meta-model packages
        List packages = _findSubPkgs(MetaModelLibrary.SYSTEM_PACKAGE);
        packages.add(MetaModelLibrary.UNNAMED_PACKAGE);

        // Compute all types from the meta-model packages
        Iterator iter = packages.iterator();
        while (iter.hasNext()) {
            PackageDecl pkg = (PackageDecl) iter.next();
            if (!pkg.membersLoaded()) {
                continue;
            }
            Iterator classIter = pkg.getUserTypes();
            while (classIter.hasNext()) {
                ObjectDecl decl = (ObjectDecl) classIter.next();
                _findTypes(types, decl);
            }
        }

        // Use a Set so as to avoid duplicates.
        HashSet packageSet = new HashSet(types);

        return new LinkedList(packageSet);
    }

    /**
     * Find all types inside a given class declaration, including itself, and
     * add it to the type list.
     *
     * @param types
     *            List of types.
     * @param decl
     *            Declaration of a class of objects.
     */
    protected void _findTypes(List types, ObjectDecl decl) {
        // FIXME: should this take a Set argument instead of a List arg?
        types.add(decl);
        Iterator iter = decl.getInnerClasses();
        while (iter.hasNext()) {
            ObjectDecl inner = (ObjectDecl) iter.next();
            if (inner == decl) {
                continue;
            }
            _findTypes(types, inner);
        }
    }

    // xichen template
    /**
     * Find all the types declared in the program that should appear in the Java
     * language from compiled ASTs.
     *
     * @param astList
     *            a list of compiled ASTs
     * @return A list of all ObjectDecl that are used in the metamodel program.
     */
    protected List _findAllTypes(List astList) {
        // FIXME: should this take a Set argument instead of a List arg?
        List types = new LinkedList();
        Iterator iter = astList.iterator();
        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();
            Iterator iterDefTypes = ast.getDefTypes().iterator();
            while (iterDefTypes.hasNext()) {
                Object node = iterDefTypes.next();
                ObjectDecl ocl = null;
                if (node instanceof UserTypeDeclNode) {
                    ocl = (ObjectDecl) MetaModelDecl.getDecl((NamedNode) node);
                }
                if (ocl != null) {
                    _findTypes(types, ocl);
                }
            }
        }

        return types;
    }

    /**
     * Test if the path contains a valid Java compiler. Make the test by trying
     * to compile a valid Java 1.2 class.
     *
     * @param path
     *            The path of the Java compiler.
     * @param dummy
     *            Dummy file to be compiled.
     * @return true if the path contains a valid Java compiler.
     */
    protected boolean _isValidJavaCompiler(String path, String dummy) {
        File compiler = new File(path);
        if (!compiler.exists() || !compiler.isFile()) {
            compiler = new File(path + ".exe");
            if (!compiler.exists() || !compiler.isFile()) {
                return false;
            }
        }

        // Build the command string that will compile the dummy file
        // Replace backslashes with forward slashes.
        // We use the String[] version here so we can handle pathnames
        // with spaces.
        String command[] = { path.replace('\\', '/'), "-classpath",
                _classPath + File.pathSeparator + _tmpRoot, dummy };
        boolean isValid = true;
        try {
            _execute(command, false);
        } catch (Exception e) {
            e.printStackTrace();
            isValid = false;
        }
        return isValid;
    }

    /**
     * Test if a path is a valid temporary directory for elaboration. It is
     * considered valid if it is a directory, it exists, and the user has
     * permissions to create and delete files inside it.
     *
     * @param dirName
     *            Temporary directory.
     * @return true iff the directory exists, and we can create and delete files
     *         in it.
     */
    protected boolean _isValidTmpDir(String dirName) {
        File dir = new File(dirName);
        boolean isValid = true;
        try {
            if (!dir.exists()) {
                return false;
            }
            if (!dir.isDirectory()) {
                return false;
            }
            File tmp = File.createTempFile("MET", "tmp");
            tmp.delete();
        } catch (IOException e) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Write the code for the list of classes that have to be translated into
     * Java code.
     *
     * @param files
     *            List of source files to be translated into Java code.
     * @return A list of names of Java files created for these classes.
     */
    protected List _translateSources(List files) {
        LinkedList javaFiles = new LinkedList();

        // Classes Math, String and Object should not be translated into
        // Java. They cause too many problems to the compiler.
        TreeNode classDecl = MetaModelLibrary.OBJECT_DECL.getSource();
        TreeNode compileUnit = classDecl.getParent();
        files.remove(compileUnit);
        classDecl = MetaModelLibrary.STRING_DECL.getSource();
        compileUnit = classDecl.getParent();
        files.remove(compileUnit);
        ClassDecl mathDecl = MetaModelLibrary.LANG_PACKAGE.getClass("Math");
        classDecl = mathDecl.getSource();
        compileUnit = classDecl.getParent();
        files.remove(compileUnit);

        Iterator iter = files.iterator();

        while (iter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) iter.next();

            // Create the directory for the package
            PackageDecl pkgDecl = (PackageDecl) ast
                    .getDefinedProperty(PACKAGE_KEY);

            File pkgDir = _createPkgDir(pkgDecl);
            // String filename = (String)ast.getProperty(IDENT_KEY);

            // Translate the meta-model file into Java
            ast.accept(new ExplicitCastVisitor(), null);

            Hashtable javaSrcs = null;
            javaSrcs = (Hashtable) ast.accept(new JavaTranslationVisitor(),
                    null);

            String headerString = "";
            if (pkgDecl == MetaModelLibrary.UNNAMED_PACKAGE) {
                headerString = "\npackage UNNAMED_PACKAGE;\n\n";
            }
            headerString = headerString + (String) javaSrcs.get("package");
            headerString = headerString + "\nimport java.lang.*;";
            headerString = headerString + "\nimport java.lang.reflect.*;";
            headerString = headerString + "\nimport metamodel.lang.*;";
            headerString = headerString
                    + "\nimport metropolis.metamodel.runtime.*;";
            headerString = headerString
                    + "\nimport metropolis.metamodel.backends.elaborator.util.*;";
            // "\nimport java.util.*;";

            javaSrcs.remove("package");

            // Generate a Java file foreach top-level class
            // in the meta-model file
            Iterator topClasses = javaSrcs.entrySet().iterator();
            while (topClasses.hasNext()) {
                Map.Entry topClass = (Map.Entry) topClasses.next();
                String className = (String) topClass.getKey();
                String translation = (String) topClass.getValue();
                File javaFileName = new File(pkgDir, className + ".java");
                // Write the Java file
                try {
                    FileWriter target = new FileWriter(javaFileName);
                    target.write(headerString);
                    target.write("\n");
                    target.write(translation);
                    target.flush();
                    target.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                javaFiles.add(javaFileName.toString());
            }
        }

        // We should create a class called metamodel.lang.pcval to simulate
        // the pcval special type in the metamodel language
        File pkgDir = _createPkgDir(MetaModelLibrary.LANG_PACKAGE);
        File pcval = new File(pkgDir, "pcval.java");
        try {
            FileWriter target = new FileWriter(pcval);
            target.write("package metamodel.lang;\n\n");
            target.write("import metamodel.lang.*;\n");
            target.write("public class pcval { }\n");
            target.flush();
            target.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        javaFiles.add(pcval.toString());

        return javaFiles;
    }

    /**
     * Write the part of the main class in charge of the definition of
     * inheritance/implementation relationships among the types.
     *
     * @param writer
     *            FileWriter used to write strings in the file.
     * @param types
     *            List of all types that will be used during elaboration.
     * @exception IOException
     *                If the writing to the main file fails.
     */
    protected void _writeInheritanceRelations(FileWriter writer, List types)
            throws IOException {
        Iterator i = types.iterator();
        while (i.hasNext()) {
            ObjectDecl decl = (ObjectDecl) i.next();

            // xichen: disregard the declarations for templates
            if (decl.getTypeParams() != null && !decl.getTypeParams().isEmpty()) {
                continue;
            }

            ObjectDecl superClass = decl.getSuperClass();
            if (superClass != null) {
                writer.write("    Network.net.getType(\"" + decl.fullName()
                        + "\").setSuperClass(Network.net.getType(\""
                        + superClass.fullName() + "\"));\n");
            }
            Iterator ifs = decl.getInterfaces().iterator();
            while (ifs.hasNext()) {
                ObjectDecl intf = (ObjectDecl) ifs.next();
                writer.write("    Network.net.getType(\"" + decl.fullName()
                        + "\").addInterface(Network.net.getType(\""
                        + intf.fullName() + "\"));\n");
            }
        }
        writer.write("\n");
    }

    /**
     * Write the part of the main class in charge of the definition of the ports
     * of each type.
     *
     * @param writer
     *            FileWriter used to write strings in the file.
     * @param types
     *            List of all types that will be used during elaboration.
     * @exception IOException
     *                If the writing to the main file fails.
     */
    protected void _writePortInformation(FileWriter writer, List types)
            throws IOException {

        // Write information about ports declared directly in the type
        writer.write("    MMPort p;\n\n");
        Iterator i = types.iterator();
        while (i.hasNext()) {
            ObjectDecl decl = (ObjectDecl) i.next();

            // xichen: disregard the declarations for templates
            if (decl.getTypeParams() != null && !decl.getTypeParams().isEmpty()) {
                continue;
            }

            Iterator ports = decl.getPorts();
            while (ports.hasNext()) {
                PortDecl port = (PortDecl) ports.next();
                if (port.getContainer() != decl) {
                    continue;
                }
                TypeNode type = port.getType();
                int dims = 0;
                while (!(type instanceof TypeNameNode)) {
                    if (!(type instanceof ArrayTypeNode)) {
                        throw new RuntimeException("Port '" + port.getName()
                                + "' of type '" + decl.fullName() + "' has an"
                                + " invalid type - must be an interface type"
                                + " or an array of interface types");
                    }
                    dims++;
                    type = ((ArrayTypeNode) type).getBaseType();
                }
                MetaModelDecl typeDecl = MetaModelDecl
                        .getDecl(((TypeNameNode) type).getName());

                if (dims == 0) {
                    // Create a scalar port
                    writer.write("    p = new MMPort(\"" + port.getName()
                            + "\", Network.net.getType(\""
                            + typeDecl.fullName() + "\"), "
                            + "Network.net.getType(\"" + decl.fullName()
                            + "\"));\n");
                    writer.write("    Network.net.getType(\"" + decl.fullName()
                            + "\").addPort(p);\n");
                } else {
                    // Create the array of ports
                    writer.write("    p = new MMPort(\"" + port.getName()
                            + "\", Network.net.getType(\""
                            + typeDecl.fullName() + "\"), "
                            + "Network.net.getType(\"" + decl.fullName()
                            + "\"), " + dims + ");\n");
                    writer.write("    Network.net.getType(\"" + decl.fullName()
                            + "\").addPort(p);\n");
                }
            }
        }

        // Write information about the ports inherited by the type
        i = types.iterator();
        while (i.hasNext()) {
            ObjectDecl decl = (ObjectDecl) i.next();

            // xichen: disregard the declarations for templates
            if (decl.getTypeParams() != null && !decl.getTypeParams().isEmpty()) {
                continue;
            }

            Iterator ports = decl.getPorts();
            while (ports.hasNext()) {
                PortDecl port = (PortDecl) ports.next();
                if (port.getContainer() == decl) {
                    continue;
                }
                ObjectDecl container = (ObjectDecl) port.getContainer();
                writer.write("    Network.net.getType(\"" + decl.fullName()
                        + "\").addPort(Network.net.getType(\""
                        + container.fullName() + "\").getPort(\""
                        + port.getName() + "\"));\n");
            }
        }
    }

    /**
     * Write the part of the main class in charge of the creation of MMType
     * objects.
     *
     * @param writer
     *            FileWriter used to write strings in the file.
     * @param types
     *            List of all types that will be used during elaboration.
     * @exception IOException
     *                If the writing to the main file fails.
     */
    protected void _writeTypeCreation(FileWriter writer, List types)
            throws IOException {
        writer.write("    MMType n;\n\n");
        Iterator i = types.iterator();
        while (i.hasNext()) {
            ObjectDecl decl = (ObjectDecl) i.next();

            // xichen: disregard the declarations for templates
            if (decl.getTypeParams() != null && !decl.getTypeParams().isEmpty()) {
                continue;
            }

            String kind = null;
            switch (decl.category) {
            case CG_NETLIST:
                kind = "MMType.NETLIST";
                break;
            case CG_MEDIUM:
                kind = "MMType.MEDIUM";
                break;
            case CG_SM:
                kind = "MMType.STATEMEDIUM";
                break;
            case CG_SCHEDULER:
                kind = "MMType.SCHEDULER";
                break;
            case CG_PROCESS:
                kind = "MMType.PROCESS";
                break;
            case CG_CLASS:
                kind = "MMType.CLASS";
                break;
            case CG_INTERFACE:
                kind = "MMType.INTERFACE";
                break;
            case CG_QUANTITY:
                kind = "MMType.QUANTITY";
                break;
            default:
                throw new RuntimeException("Internal error, "
                        + "wrong category: " + decl.category);
            }
            writer.write("    n = new MMType(\"" + decl.fullName() + "\", "
                    + kind + ", null , new LinkedList());\n");
            writer.write("    Network.net.addType(n);\n");
        }
        writer.write("\n");
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    // Read in output from a process.
    private void _parseExecute(String command, boolean showError, Process other) {
        // Uncomment the next line for debugging
        // System.out.println("Executing: " + command);

        // FIXME: This code should spawn threads and read both stderr
        // and stdout. See
        // http://ptolemy.eecs.berkeley.edu/ptolemyII/ptIIlatest/ptII/ptolemy/gui/JTextAreaExec.java
        InputStream is = other.getErrorStream();
        InputStreamReader reader = new InputStreamReader(is);
        String error = "";
        try {
            for (int i = reader.read(); i != -1; i = reader.read())
                error = error + ((char) i);
            is.close();
            other.waitFor();
        } catch (IOException ex) {
            throw new ElaborationException("Reading data from:\n" + command
                    + "\nfailed", _javacPath, _javaPath, _classPath, _tmpRoot,
                    ex);
        } catch (InterruptedException ex) {
            throw new ElaborationException("Reading data from:\n" + command
                    + "\nfailed", _javacPath, _javaPath, _classPath, _tmpRoot,
                    ex);
        }

        // Check if there is an error in the execution
        if ((other.exitValue() != 0)) {
            // if (!showError) {
            // throw new RuntimeException("Error during execution of\n"
            // + command + "\n Error Message was:\n" + error);
            // }
            throw new ElaborationException("Error during execution of:\n"
                    + command + "\nError Message was:\n" + error, _javacPath,
                    _javaPath, _classPath, _tmpRoot, null);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    // Usage String.
    private String _usage = "[-javac <path>] [-java <path>] [-dir <dir>] <netlistName>";
}
