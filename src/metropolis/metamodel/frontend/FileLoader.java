/* A class that loads meta-model packages and classes.

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

package metropolis.metamodel.frontend;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.Scope;
import metropolis.metamodel.StringManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.frontend.parser.MetaModelParser;
import metropolis.metamodel.nodetypes.CompileUnitNode;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // FileLoader
/**
 * A class that load packages and classes in the meta-model. This class uses
 * severals caches and strategies to speed up the compilation of meta-model
 * files.
 * <p>
 * This class consists of several static "load()" methods that load packages or
 * user-defined types. The result of this "load()" is a PackageDecl for classes
 * and an AST in the case of user-defined types.
 * <p>
 * Caches are used extensively, so users of this class can safely invoke
 * "load()" several times on the same file; the file will be loaded only once,
 * and all calls will return the same result.
 * <ul>
 * <li> loadPackage(name, parent): Load a package with a parent name.
 * <li> loadTopLevelPackage(name): Load a top-level package with a given name.
 * <li> loadCompileUnit(pathname, pass): Load a file with a given name, and
 * perform semantic analysis up to pass 'pass'.
 * <li> loadAllCompileUnits(package, pass): Load all files inside a package.
 * </ul>
 * <p>
 * The paths where the top-level packages can be found (what is called
 * class-path in Java) can be set using the method setClassPath() or
 * addClassPathDir(). This methods should be invoked to initialize this class,
 * possibly passing arguments from the command line,
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: FileLoader.java,v 1.59 2006/10/12 20:33:40 cxh Exp $
 */
public final class FileLoader implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return the contents of the classpath where the top-level packages can be
     * found as a string.
     *
     * @param separator
     *            The string with which to separate each classpath element.
     * @return The classpath as a string.
     */
    public static String classPathToString(String separator) {
        StringBuffer results = new StringBuffer();
        Iterator paths = _classPath.iterator();
        while (paths.hasNext()) {
            String path = (String) paths.next();
            if (results.length() > 0) {
                results.append(separator);
            }
            results.append(path);
        }
        return results.toString();
    }

    /**
     * Return the list of paths where the top-level packages can be found.
     *
     * @return The list of paths where the top-level packages can be found.
     * @see #setClassPath(List)
     */
    public static List getClassPath() {
        return _classPath;
    }

    /**
     * Set the list of paths where the top-level package can be found.
     *
     * @param paths
     *            List of directories in the class-path.
     * @exception RuntimeException
     *                if any of these paths is not a valid directory.
     * @see #getClassPath()
     * @see #addClassPathDir(String)
     */
    public static void setClassPath(List paths) {
        _classPath.clear();
        Iterator pathIter = paths.iterator();
        while (pathIter.hasNext()) {
            String path = (String) pathIter.next();
            addClassPathDir(path);
        }
    }

    /**
     * Add one directory at the end of the class-path list. If the directory is
     * already in the list, then do nothing.
     *
     * @param path
     *            Path of the directory.
     * @exception RuntimeException
     *                if any of these paths is not a valid. directory.
     * @see #setClassPath(List)
     */
    public static void addClassPathDir(String path) {
        // Check if the path is repeated
        if (_classPath.contains(path))
            return;
        // Check if the path is correct.
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            _classPath.add(path);
        } else {
            if (!file.exists())
                System.err.println("\nWarning: Directory '" + path
                        + "' of the class-path does not exist");
            else
                System.err.println("\nWarning: Path '" + path
                        + "' of the class-path is not a directory");
        }
    }

    /**
     * Create a package declaration with a given package name and a given
     * parent. The declaration is added to the parent scope.
     *
     * @param name
     *            Name of the subpackage.
     * @param parent
     *            Enclosing package.
     * @return The PackageDecl of the package or null if the FileLoader cannot
     *         find this subpackage.
     */
    public static PackageDecl loadPackage(String name, PackageDecl parent) {
        // Check special packages
        if (parent == MetaModelLibrary.SYSTEM_PACKAGE)
            return loadTopLevelPackage(name);
        if (parent == MetaModelLibrary.UNNAMED_PACKAGE)
            throw new RuntimeException("Attempting to create a subpackage '"
                    + name + "' of the unnamed package");

        // Compute the path of the subpackage
        String parentPath = (String) _packagePaths.get(parent.fullName());
        String fullPath = parentPath + "/" + name;

        // Lookup the name in the scope of parent directory
        PackageDecl decl;
        if (parent._membersLoaded) {
            decl = parent.getSubPackage(name);
            if (decl != null)
                return decl;
        } else {
            Scope scope = parent.initGetScope();
            decl = (PackageDecl) scope.lookupLocal(name, CG_PACKAGE);
            if (decl != null)
                return decl;
        }

        File dir = new File(fullPath);

        if (!dir.exists() || !dir.isDirectory())
            return null;

        // Update mapping of packages to paths
        try {
            fullPath = dir.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed path to package '"
                    + parent.fullName() + "." + name + "'");
        }
        _packagePaths.put(parent.fullName() + "." + name, fullPath);

        decl = new PackageDecl(name, parent);
        parent.initGetScope().add(decl);
        decl.setScope(new Scope(parent.initGetScope()));

        return decl;
    }

    /**
     * Create a package declaration of a top-level package with a given name.
     * Subpackages are loaded on demand, while user-defined classes are loaded
     * directly.
     *
     * @param name
     *            Name of the package.
     * @return The PackageDecl of the package or null if the FileLoader cannot
     *         find this package in the class-path.
     */
    public static PackageDecl loadTopLevelPackage(String name) {
        // Lookup the name in scope of system package
        PackageDecl parent = MetaModelLibrary.SYSTEM_PACKAGE;
        PackageDecl decl = parent.getSubPackage(name);
        if (decl != null)
            return decl;

        // Find the directory in the class path
        Iterator pathIter = _classPath.iterator();
        while (pathIter.hasNext()) {
            String basePath = (String) pathIter.next();
            String fullPath = basePath + "/" + name;

            File dir = new File(fullPath);
            if (!dir.exists() || !dir.isDirectory())
                continue;
            // Update mapping of packages to paths
            try {
                fullPath = dir.getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException("Failed path to package '" + name
                        + "'");
            }
            _packagePaths.put(name, fullPath);

            // Top level package found
            decl = new PackageDecl(name, parent);
            parent.initGetScope().add(decl);
            decl.setScope(new Scope(parent.initGetScope()));

            return decl;
        }
        return null;
    }

    /**
     * Load all the package members (subpackages, user defined types) of a
     * package up to a pass.
     *
     * @param pack
     *            Package whose members should be loaded.
     * @param pass
     *            Last pass to be applied to all files of the package. This pass
     *            must be &gt;= 1.
     */
    public static void loadPackageMembers(PackageDecl pack, int pass) {
        if ((pack == MetaModelLibrary.SYSTEM_PACKAGE)
                || (pack == MetaModelLibrary.UNNAMED_PACKAGE)) {
            _loadSpecialPackageMembers(pack, pass);
            return;
        }
        String path = (String) _packagePaths.get(pack.fullName());
        _loadPackageMembers(pack, path, true, true, pass);
        pack._membersLoaded = true;
    }

    /**
     * Load all the package members (subpackages, user defined types) of a
     * package.
     *
     * @param pack
     *            Package whose members should be loaded.
     */
    public static void loadPackageMembers(PackageDecl pack) {
        loadPackageMembers(pack, 1);
    }

    /**
     * Load a compile unit from a file. Static analysis is performed on this
     * file up to the pass indicated by the parameter.
     *
     * @param file
     *            The file to be loaded
     * @param pass
     *            Last pass to be performed on the file.
     * @return the CompileUnitNode.
     */
    public static CompileUnitNode loadCompileUnit(File file, int pass) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException("Error loading file " + file.getName()
                    + "\n" + e.toString());
        }
        return _loadCanonicalName(name, pass);
    }

    /**
     * Load a compile unit from a file. Static analysis is performed on this
     * file up to the pass indicated by the parameter.
     *
     * @param filename
     *            Name of the file to be loaded.
     * @param pass
     *            Last pass to be performed on the file.
     * @return the CompileUnitNode.
     */
    public static CompileUnitNode loadCompileUnit(String filename, int pass) {
        return loadCompileUnit(new File(filename), pass);
    }

    /**
     * Load a compile unit from a file, given the file's name and the name of a
     * parent directory to find it in. Static analysis is performed on this file
     * up to the pass indicated by the parameter.
     *
     * @param parentDirName
     *            Name of the parent directory.
     * @param filename
     *            Name of the file to be loaded.
     * @param pass
     *            Last pass to be performed on the file.
     * @return the CompileUnitNode.
     */
    public static CompileUnitNode loadCompileUnit(String parentDirName,
            String filename, int pass) {
        return loadCompileUnit(new File(parentDirName, filename), pass);
    }

    /**
     * Load a compile unit of a class inside a given package. Static analysis is
     * performed on the class up to the pass indicated by the parameter.
     *
     * @param className
     *            Name of the class to be loaded.
     * @param pkg
     *            Declaration of the package where the class is located.
     * @param pass
     *            Last pass to be performed on the file.
     * @return the CompileUnitNode.
     */
    public static CompileUnitNode loadCompileUnit(String className,
            PackageDecl pkg, int pass) {
        String pathName = (String) _packagePaths.get(pkg.fullName());
        String filename = pathName + "/" + className + "."
                + _metaModelExtension;

        File file = new File(filename);
        if (!file.exists()) {
            loadPackageMembers(pkg);
            ObjectDecl decl = pkg.getUserType(className);
            if (decl == null) {
                throw new RuntimeException("Error: cannot find class '"
                        + className + "' in package " + pkg.fullName());
            } else {
                TreeNode source = decl.getSource();
                // The parent of the source must be the compile unit
                CompileUnitNode ast = (CompileUnitNode) source.getParent();
                filename = (String) ast.getProperty(IDENT_KEY);
                return loadCompileUnit(filename, pass);
            }
        }
        return loadCompileUnit(file, pass);
    }

    /**
     * Get all the ASTs of all the files that have been compiled.
     *
     * @return A list of all ASTs that have been compiled.
     */
    public static List getCompiledSources() {
        return getCompiledSources(0);
    }

    /**
     * Get a list of ASTs of all the files that have been compiled until a given
     * pass. Even if the ASTs have undergone later passes, they will be included
     * in the list.
     *
     * @param pass
     *            Minimum pass.
     * @return A list of all ASTs that have been compiled until the given pass.
     */
    public static List getCompiledSources(int pass) {
        List result = new LinkedList();
        if ((pass < 0) || (pass > 4))
            return result;
        Hashtable cachedASTs = _resolvedFiles[pass];
        result.addAll(cachedASTs.values());
        return result;
    }

    /**
     * Return the truth of whether <code>CompileUnit</code>s must be rebuilt
     * when loaded from their files. If <code>false</code>, then
     * <code>loadCompileUnit()</code> may load them from pre-compiled files.
     *
     * @return The truth of whether <code>CompileUnit</code>s must be rebuilt
     *         when loaded from their files.
     * @see #setMustRebuild(boolean)
     */
    public static boolean getMustRebuild() {
        return _mustRebuild;
    }

    /**
     * Set the flag that controls whether ASTs must be forcibly rebuilt when
     * loaded. The default is <code>false</code>, which allows them to be
     * loaded from pre-compiled files.
     *
     * @param mustRebuild
     *            <code>true</code> if ASTs are to be forcibly rebuilt when
     *            loaded from their source files.
     * @see #getMustRebuild()
     */
    public static void setMustRebuild(boolean mustRebuild) {
        _mustRebuild = mustRebuild;
    }

    // xichen added for template resolution
    /**
     * Load a resolved compile unit from a file after pass 1.
     *
     * @param filename
     *            Name of the file to be loaded.
     * @return the compile unit or null if it is not available.
     */
    public static CompileUnitNode getResolvedFilePass1(String filename) {
        return (CompileUnitNode) _resolvedFilesPass1.get(filename);
    }

    /**
     * Given a package, return the corresponding full path name.
     *
     * @param pack
     *            The package to look up.
     * @return The corresponding full path name.
     */
    public static String packagePath(PackageDecl pack) {
        return (String) _packagePaths.get(pack.fullName());
    }

    /**
     * Clear out all state, which is held statically in the class itself.
     */
    public static void initialize() {
        /*
         * List of paths where top-level packages can be found. These packages
         * will be looked-up in the same order in which they are declared.
         */
        _classPath = new LinkedList();

        /* A mapping from top level package names to full path names. */
        _packagePaths = new Hashtable(10);

        /* Cache of previously parsed files. */
        _parsedFiles = new Hashtable();

        /* Store resolved files after pass 1 before template resolution. */
        _resolvedFilesPass1 = new Hashtable();

        /*
         * Cache of previously analized files. _resolvedFiles[0] contains al
         * files that have undergone pass 0, _resolvedFiles[1] contains the
         * files that have undergone pass 1, etc.
         */
        _resolvedFiles = new Hashtable[] { new Hashtable(), new Hashtable(),
                new Hashtable(), new Hashtable(), new Hashtable() };

        /*
         * List of files that have undergone pass 0 but have not undergone pass
         * 1. This list is needed to perform class resolution on ALL these files
         * BEFORE performing pass inheritance resolution on them.
         */
        _pass0ResolvedList = new LinkedList();

        /**
         * List of files that have undergone pass 1 but have not undergone pass
         * 2. This list is needed to perform template resolution on ALL these
         * files BEFORE performing name and field resolution on them.
         */
        _pass1ResolvedList = new LinkedList();

        /**
         * Boolean flag indicating whether ASTs must be rebuilt on load.
         */
        _mustRebuild = false;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    // See the method initialize() for initializations of these.
    // This all needs to be re-initializable to allow multiple builds
    // from a single VM, as under Eclipse.

    /**
     * List of paths where top-level packages can be found. These packages will
     * be looked-up in the same order in which they are declared.
     */
    protected static List _classPath = null;

    /** A mapping from top level package names to full path names. */
    protected static Hashtable _packagePaths = null;

    /** Valid extension for metamodel files. */
    protected static final String _metaModelExtension = "mmm";

    /** Cache of previously parsed files. */
    protected static Hashtable _parsedFiles = null;

    /** Store resolved files after pass 1 before template resolution. */
    protected static Hashtable _resolvedFilesPass1 = null;

    /**
     * Cache of previously analized files. _resolvedFiles[0] contains al files
     * that have undergone pass 0, _resolvedFiles[1] contains the files that
     * have undergone pass 1, etc.
     */
    protected static Hashtable[] _resolvedFiles = null;

    /**
     * List of files that have undergone pass 0 but have not undergone pass 1.
     * This list is needed to perform class resolution on ALL these files BEFORE
     * performing pass inheritance resolution on them.
     */
    protected static List _pass0ResolvedList = null;

    /**
     * List of files that have undergone pass 1 but have not undergone pass 2.
     * This list is needed to perform template resolution on ALL these files
     * BEFORE performing name and field resolution on them.
     */
    protected static List _pass1ResolvedList = null;

    /**
     * Boolean flag indicating whether ASTs must be rebuilt on load.
     */
    protected static boolean _mustRebuild = false;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Load the members of a package, in a given path. This method allows the
     * caller to select if subpackages or user defined classes will be loaded.
     *
     * @param pack
     *            Enclosing package.
     * @param path
     *            Path where all members are declared.
     * @param dirs
     *            True if subdirectories have to be considered subpackages.
     * @param files
     *            True if al files have to be considered compile units.
     * @param pass
     *            Last semantic pass to be applied to the files of this package.
     * @exception RuntimeException
     *                if the path does not exist or it is not a directory.
     */
    protected static void _loadPackageMembers(PackageDecl pack, String path,
            boolean dirs, boolean files, int pass) {

        pack._membersLoaded = true;
        File dir = new File(path);

        if (!dir.exists() || !dir.isDirectory())
            throw new RuntimeException("Incorrect package path");
        File[] list = dir.listFiles();
        LinkedList members = new LinkedList();
        for (int i = 0; i < list.length; i++) {
            File file = list[i];
            if (file.isDirectory() && file.canRead() && dirs) {
                loadPackage(file.getName(), pack);
            } else if (file.isFile() && file.canRead() && files) {
                String name = file.getName();
                String extension = StringManip.partAfterLast(name, '.');
                if ((extension.equals(name))
                        || (!extension.equals(_metaModelExtension)))
                    continue;
                loadCompileUnit(file, -3);
                members.add(file);
            }
        }

        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            loadCompileUnit(file, 0);
        }
        iter = members.iterator();
        while (iter.hasNext()) {
            File file = (File) iter.next();
            loadCompileUnit(file, 1);
        }
        if (pass > 1) {
            iter = members.iterator();
            while (iter.hasNext()) {
                File file = (File) iter.next();
                loadCompileUnit(file, pass);
            }
        }
    }

    /**
     * Load the members of a special package, SYSTEM or UNNAMED.
     *
     * @param pack
     *            Special package whose members must be loaded.
     * @param pass
     *            Last semantic pass to be applied.
     */
    protected static void _loadSpecialPackageMembers(PackageDecl pack, int pass) {
        // Select the members that we want to load.
        // In SYSTEM, we want to load the subpackages.
        // In UNNAMED, we want to load the defined types.
        boolean dirs = false, files = false;
        if (pack == MetaModelLibrary.SYSTEM_PACKAGE)
            dirs = true;
        else if (pack == MetaModelLibrary.UNNAMED_PACKAGE)
            files = true;

        // Load members in all the class-path
        Iterator pathIter = _classPath.iterator();
        while (pathIter.hasNext()) {
            String path = (String) pathIter.next();
            _loadPackageMembers(pack, path, dirs, files, pass);
        }
    }

    /**
     * Load a compile unit with a given canonical name, performing semantic
     * analysis up to a given pass.
     *
     * @param name
     *            Canonical name of the compile unit being loaded.
     * @param pass
     *            Last pass to be performed on the file.
     * @return the CompileUnitNode.
     */
    protected static CompileUnitNode _loadCanonicalName(String name, int pass) {
        // Load information about a file:
        // - if the file is already loaded, use that information
        // - if the file is not loaded but a repository exists, use the
        // repository
        // - if the file is not loaded and there is no repository, then
        // parse the file
        CompileUnitNode ast = (CompileUnitNode) _parsedFiles.get(name);
        if (ast == null) {
            SyntaxRepository repository = new SyntaxRepository(name);
            repository.setMustRebuild(getMustRebuild());
            repository.loadContent();
            ast = repository.getSyntaxTree();
        }

        // Perform semantic analysis (again, if necessary)
        switch (pass) {
        case -3:
            return ast;
        case 0:
            return _resolvePass0(ast);
        case 1:
            ast = _resolvePass0(ast);
            return _resolvePass1(ast);
        case 2:
            ast = _resolvePass0(ast);
            ast = _resolvePass1(ast);
            return _resolvePass2(ast);
        case 3:
            ast = _resolvePass0(ast);
            ast = _resolvePass1(ast);
            ast = _resolvePass2(ast);
            return _resolvePass3(ast);
        case 4:
            ast = _resolvePass0(ast);
            ast = _resolvePass1(ast);
            ast = _resolvePass2(ast);
            ast = _resolvePass3(ast);
            return _resolvePass4(ast);
        default:
            throw new RuntimeException("Wrong pass number: " + pass);
        }
    }

    /**
     * Perform semantic analysis pass 0 on an abstract syntax tree. Use caches
     * to avoid repeating analysis.
     *
     * @param ast
     *            Abstract syntax tree to be analyzed.
     * @return The abstract syntax tree produced by the analysis.
     */
    protected static CompileUnitNode _resolvePass0(CompileUnitNode ast) {

        // Build the package scope for the package of this file, if this
        // has not been done already
        PackageDecl pkg = (PackageDecl) ast.getDefinedProperty(PACKAGE_KEY);
        pkg.getScope();

        // Lookup this name in the cache of resolved files
        int pass = 0;
        String name = (String) ast.getProperty(IDENT_KEY);
        CompileUnitNode result = (CompileUnitNode) _resolvedFiles[pass]
                .get(name);
        if (result != null)
            return result;

        // Call pass 0 visitors
        ast.accept(new ResolveImportsVisitor(), null);
        ast.accept(new ResolveTypesVisitor(), null);

        // Update cache of resolved files
        _resolvedFiles[pass].put(name, ast);
        _pass0ResolvedList.add(ast);

        return ast;
    }

    /**
     * Perform semantic analysis pass 1 on an abstract syntax tree. Use caches
     * to avoid repeating analysis.
     *
     * @param ast
     *            Abstract syntax tree to be analyzed.
     * @return The abstract syntax tree produced by the analysis.
     */
    protected static CompileUnitNode _resolvePass1(CompileUnitNode ast) {
        // Lookup this name in the cache of resolved files
        int pass = 1;
        String name = (String) ast.getProperty(IDENT_KEY);
        CompileUnitNode result = (CompileUnitNode) _resolvedFiles[pass]
                .get(name);
        if (result != null)
            return result;

        // Perform class resolution on all files that have undergone
        // pass 0 before performing inheritance resolution on any file
        Iterator nodeItr = _pass0ResolvedList.iterator();
        while (nodeItr.hasNext()) {
            CompileUnitNode node = (CompileUnitNode) nodeItr.next();
            node.accept(new ResolveClassVisitor(), null);
        }

        // Then, apply inheritance resolution on all these files, before
        // further analysis can be performed. Add the file to the cache
        // of resolved files in pass 1.
        // GY: nodeItr = _pass0ResolvedList.iterator();
        // GY: while (nodeItr.hasNext()) {
        // GY: CompileUnitNode node = (CompileUnitNode) nodeItr.next();
        CompileUnitNode node = ast;
        String filename = (String) node.getProperty(IDENT_KEY);
        node.accept(new ResolveInheritanceVisitor(), null);

        _resolvedFiles[pass].put(filename, node);
        _pass1ResolvedList.add(node);

        if (!_resolvedFilesPass1.containsKey(filename)) {
            CompileUnitNode dup = null;
            try {
                dup = (CompileUnitNode) node.clone();
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException("Clone of AST '"
                        + ((CompileUnitNode) node).getProperty(IDENT_KEY)
                        + "' fails.", ex);
            }

            _resolvedFilesPass1.put(filename, dup);
        }
        // GY: }

        // All files from pass 0 have been processed up to pass 1
        // GY: _pass0ResolvedList.clear();

        return ast;
    }

    /**
     * Perform semantic analysis pass 2 on an abstract syntax tree. Use caches
     * to avoid repeating analysis.
     *
     * @param ast
     *            Abstract syntax tree to be analyzed.
     * @return The abstract syntax tree produced by the analysis.
     */
    protected static CompileUnitNode _resolvePass2(CompileUnitNode ast) {
        // Lookup this name in the cache of resolved files
        int pass = 2;
        String name = (String) ast.getProperty(IDENT_KEY);
        CompileUnitNode result = (CompileUnitNode) _resolvedFiles[pass]
                .get(name);
        if (result != null)
            return result;

        // xichen_template_beg
        // Perform template resolution on all files that have undergone
        // pass 1 before performing name and field resolution on any file
        Iterator nodeItr = _pass1ResolvedList.iterator();
        LinkedHashSet revisitNodeSet = new LinkedHashSet();
        while (nodeItr.hasNext()) {
            CompileUnitNode node = (CompileUnitNode) nodeItr.next();
            revisitNodeSet.addAll((Collection) node.accept(
                    new TemplateEliminationVisitor(), null));
        }

        nodeItr = revisitNodeSet.iterator();
        while (nodeItr.hasNext()) {
            CompileUnitNode node = (CompileUnitNode) nodeItr.next();
            String filename = (String) node.getProperty(IDENT_KEY);

            // If templates were found, redo passes 0, 1, and 2.

            _resolvedFiles[0].remove(filename);

            // Call pass 0 visitors
            node.accept(new ResolveTypesVisitor(), null);

            // Update cache of resolved files
            _resolvedFiles[0].put(filename, node);
        }

        // if (name.endsWith("ProdCons.mmm"))
        // System.out.println("ProdCons.mmm");

        if (revisitNodeSet.size() > 0) {
            revisitNodeSet.addAll((Collection) _pass1ResolvedList);

            // Call pass 1 visitors
            nodeItr = revisitNodeSet.iterator();
            // nodeItr = _pass1ResolvedList.iterator();
            while (nodeItr.hasNext()) {
                CompileUnitNode node = (CompileUnitNode) nodeItr.next();
                // PackageDecl pkg = (PackageDecl) node.getProperty(PACKAGE_KEY);
                // if (pkg == MetaModelLibrary.LANG_PACKAGE)
                // continue;

                String filename = (String) node.getProperty(IDENT_KEY);

                _resolvedFiles[1].remove(filename);
                _resolvedFiles[2].remove(filename);

                // if (filename.endsWith("GlobalTime.mmm"))
                // System.out.println("ProdCons.mmm");

                LinkedList argList = new LinkedList();
                argList.addLast(new String("template"));
                node.accept(new ResolveClassVisitor(), argList);
            }

            nodeItr = revisitNodeSet.iterator();
            // nodeItr = _pass1ResolvedList.iterator();
            while (nodeItr.hasNext()) {
                CompileUnitNode node = (CompileUnitNode) nodeItr.next();
                // PackageDecl pkg = (PackageDecl) node.getProperty(PACKAGE_KEY);
                // if (pkg == MetaModelLibrary.LANG_PACKAGE)
                // continue;

                String filename = (String) node.getProperty(IDENT_KEY);

                // if (filename.endsWith("ProdCons.mmm"))
                // System.out.println("ProdCons.mmm");

                node.accept(new ResolveInheritanceVisitor(), null);

                // Update cache of resolved files
                _resolvedFiles[1].put(filename, node);
            }

            // Call pass 2 visitors
            nodeItr = revisitNodeSet.iterator();
            // nodeItr = _pass1ResolvedList.iterator();
            while (nodeItr.hasNext()) {
                CompileUnitNode node = (CompileUnitNode) nodeItr.next();
                // PackageDecl pkg = (PackageDecl) node.getProperty(PACKAGE_KEY);
                // if (pkg == MetaModelLibrary.LANG_PACKAGE)
                // continue;

                String filename = (String) node.getProperty(IDENT_KEY);

                if (node != ast) {
                    node.accept(new ResolveNameVisitor(), null);
                    node.accept(new ResolveFieldVisitor(), null);

                    // Update cache of resolved files for pass 2
                    _resolvedFiles[2].put(filename, node);
                }
            }
        }

        _pass1ResolvedList.clear();

        ast.accept(new ResolveNameVisitor(), null);
        ast.accept(new ResolveFieldVisitor(), null);

        // Update cache of resolved files
        _resolvedFiles[pass].put(name, ast);

        return ast;
    }

    /**
     * Perform semantic analysis pass 3 on an abstract syntax tree. Use caches
     * to avoid repeating analysis.
     *
     * @param ast
     *            Abstract syntax tree to be analyzed.
     * @return The abstract syntax tree produced by the analysis.
     */
    protected static CompileUnitNode _resolvePass3(CompileUnitNode ast) {
        // Lookup this name in the cache of resolved files
        int pass = 3;
        String name = (String) ast.getProperty(IDENT_KEY);
        CompileUnitNode result = (CompileUnitNode) _resolvedFiles[pass]
                .get(name);
        if (result != null)
            return result;

        // FIXME: Add pass 3 visitors here

        // Update cache of resolved files
        _resolvedFiles[pass].put(name, ast);

        return ast;
    }

    /**
     * Perform semantic analysis pass 4 on an abstract syntax tree. Use caches
     * to avoid repeating analysis.
     *
     * @param ast
     *            Abstract syntax tree to be analyzed.
     * @return The abstract syntax tree produced by the analysis.
     */
    protected static CompileUnitNode _resolvePass4(CompileUnitNode ast) {
        // Lookup this name in the cache of resolved files
        int pass = 4;
        String name = (String) ast.getProperty(IDENT_KEY);
        CompileUnitNode result = (CompileUnitNode) _resolvedFiles[pass]
                .get(name);
        if (result != null)
            return result;

        // FIXME: Add pass 4 visitors here

        // Update cache of resolved files
        _resolvedFiles[pass].put(name, ast);

        return ast;
    }

    /**
     * Parse a metamodel file, returning the resulting compile unit. Use a cache
     * to make sure that files are not parsed several times.
     *
     * @param name
     *            Name of the file to be parsed.
     * @return The abstract syntax tree obtained from parsing this file.
     */
    protected static CompileUnitNode _parseCanonicalName(String name) {
        CompileUnitNode ast = (CompileUnitNode) _parsedFiles.get(name);
        if (ast != null)
            return ast;

        MetaModelParser parser = new MetaModelParser();
        try {
            parser.init(name);
        } catch (Exception e) {
            throw new RuntimeException("Error: opening input file " + name
                    + "\n" + e.toString());
        }
        parser.parse();
        ast = parser.getAST();
        // Set the name of the file in the CompileUnitNode
        // ast.setProperty(IDENT_KEY,name);
        ast.accept(new SourceFileNameVisitor(name));
        // Update the cache
        _parsedFiles.put(name, ast);
        return ast;
    }

    // /////////////////////////////////////////////////////////////////
    // // static initializer ////
    static {
        initialize();
    }
}
