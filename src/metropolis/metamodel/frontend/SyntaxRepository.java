/* A repository that contains the entire parsed AST of a compile unit.

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
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // SyntaxRepository
/**
 * A repository that contains an entire AST of a compile unit. This repository
 * allows us to skip parsing and the first passes in pass 0.
 * <p>
 * The exact information stored in this repository is the following:
 * <ul>
 * <li> an array of TreeNodes, the sources of the ObjectDecl performed in this
 * file
 * <li> the entire abstract syntax tree of the compile unit
 * </ul>
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: SyntaxRepository.java,v 1.17 2006/10/12 20:34:18 cxh Exp $
 */
public class SyntaxRepository extends FileRepository implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new repository for an abstract syntax tree of a compile unit.
     * The contents of the repository will have to be loaded using method load()
     * or rebuild().
     *
     * @param src
     *            Path where the compile unit is stored.
     */
    public SyntaxRepository(File src) {
        super(src);
    }

    /**
     * Create a new repository for an abstract syntax tree of a compile unit.
     * The contents of the repository will have to be loaded using method load()
     * or rebuild().
     *
     * @param src
     *            Path where the compile unit is stored.
     */
    public SyntaxRepository(String src) {
        this(new File(src));
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Load this repository from disk. Use class ASTCache to load the abstract
     * syntax tree and the array of source nodes.
     */
    public void load() {
        super.load();
        ASTCache.load(this);
        String filename;
        try {
            filename = _src.getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException("Error loading cache for source file "
                    + _src.getName());
        }
        // _ast.setProperty(IDENT_KEY, filename);
        _ast.accept(new SourceFileNameVisitor(filename));
        FileLoader._parsedFiles.put(filename, _ast);
        _ast.accept(new PackageResolutionVisitor(), null);
        _ast.accept(new ResolvePackageVisitor(), null);
    }

    /**
     * Store this repository to disk. Use class ASTCache to store the abstract
     * syntax tree and the array of source nodes.
     *
     * @exception RuntimeException
     *                if the repository has not been loaded.
     */
    public void store() {
        super.store();
        ASTCache.store(this);
    }

    /**
     * Rebuild the information in this repository completely from scratch. All
     * information available should be modified. Parse the file again and
     * compute the list of object declarations in this file.
     */
    public void rebuild() {
        super.rebuild();
        String canon;
        try {
            canon = _src.getCanonicalPath();
        } catch (Exception e) {
            throw new RuntimeException("Error loading file " + _src.getName()
                    + "\n" + e.toString());
        }

        // Create object declarations for all the files in the AST.
        // All properties stored in the PackageResolution pass will not
        // be stored in the cache in disk. This is something that we will
        // have to do when we load the repository from disk.
        _ast = FileLoader._parseCanonicalName(canon);

        _ast.accept(new PackageResolutionVisitor(), null);
        LinkedList objs = (LinkedList) _ast.accept(new ResolvePackageVisitor(),
                null);
        int j = 0;
        Iterator iter = objs.iterator();
        _objects = new ObjectDecl[objs.size()];
        while (iter.hasNext()) {
            _objects[j++] = (ObjectDecl) iter.next();
        }

        // Get the array of sources from the array of class
        // declarations of this compile unit. Set the references to the
        // source to 'null', as we don't want the tree to be serialized.
        // Also, set the parent scope (for top-level classes in the file)
        // to null
        /*
         * Scope fileScope = (Scope) _ast.getProperty(SCOPE_KEY); _sources = new
         * TreeNode[_objects.length]; for (int i = 0; i < _objects.length; i++) {
         * _sources[i] = _objects[i].getSource(); List typeParams =
         * _objects[i].getTypeParams(); Iterator iter2 = typeParams.iterator();
         * while (iter2.hasNext()) { TypeParameterDecl typePar =
         * (TypeParameterDecl) iter2.next(); typePar.setSource(null); }
         * _objects[i].setSource(null); Scope thisScope =
         * _objects[i].getScope(); Scope parentScope = thisScope.parent(); if
         * (parentScope == fileScope) {
         * System.out.println("*\n*\n*\n*\n*\n*\n*"); thisScope.setParent(null); } }
         */
    }

    /**
     * Get the array of source nodes of ObjectDecls stored in this file.
     *
     * @return The array of TreeNodes that are source of an ObjectDecl in this
     *         file, or null if the repository has not been loaded or rebuilt.
     */
    public TreeNode[] getSources() {
        return _sources;
    }

    /**
     * Get the abstract syntax tree of this compile unit.
     *
     * @return The abstract syntax tree of this compile unit, without semantic
     *         information, or null if the repository has not been loaded or
     *         rebuilt.
     */
    public CompileUnitNode getSyntaxTree() {
        return _ast;
    }

    /**
     * Get the object declarations performed in this compile unit.
     *
     * @return An array containing the object declarations of this compile unit,
     *         or null if the repository has not been rebuilt.
     */
    public ObjectDecl[] getObjectDecls() {
        return _objects;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Array of declarations of objects in the compile unit. This information is
     * invalid until the repository is loaded or rebuilt.
     */
    protected ObjectDecl[] _objects = null;

    /**
     * Array of sources of the ObjectDecls performed in this file. Each element
     * i of this array corresponds to the element i in the array of object
     * declarations. This information is invalid until the repository is loaded
     * or rebuilt.
     */
    protected TreeNode[] _sources = null;

    /**
     * Abstract syntax tree of the compile unit. This information is invalid
     * until the repository is loaded or rebuilt.
     */
    protected CompileUnitNode _ast = null;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Get the extension used by repositories of this file.
     *
     * @return the Extension used by repositories of this file.
     */
    protected String _getRepositoryExtension() {
        return "ast";
    }

}
