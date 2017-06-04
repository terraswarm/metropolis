/* A meta-model visitor that resolves the names of the import nodes of the
 abstract syntax tree.

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

import metropolis.metamodel.Decl;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Scope;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ImportNode;
import metropolis.metamodel.nodetypes.ImportOnDemandNode;
import metropolis.metamodel.nodetypes.NameNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ResolveImportsVisitor
/**
 * A meta-model visitor that resolves the names of the import nodes of the
 * abstract syntax tree. It sets the value of property IMPORTED_PACKAGES_KEY in
 * the CompileUnitNode. Declaration of imported classes will be added to the
 * import scope.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Jeff Tsay & Duny Lam
 * @version $Id: ResolveImportsVisitor.java,v 1.27 2006/10/12 20:34:08 cxh Exp $
 */
public class ResolveImportsVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////
    /**
     * Set traversal method be TM_CUSTOM.
     */
    public ResolveImportsVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////
    /**
     * Initialize _compileUnit, _fileScope, and _importScope variables by
     * extracting information from the CompileUnitNode.
     * <p>
     * Initialize _importedPackages to an empty LinkedList or retrieve its value
     * if it has already been defined.
     * <p>
     * Import the package "java.lang" and add it into the _importedPackages
     * list. Visit all import nodes to resolve all imported package names and
     * add them into the _importedPackages list.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {

        _compileUnit = node;
        _fileScope = (Scope) node.getDefinedProperty(SCOPE_KEY); // File
        // scope
        _importScope = _fileScope.parent().parent();
        _packageDecl = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        // initialize importedPackages property
        if (!node.hasProperty(IMPORTED_PACKAGES_KEY)) {
            _importedPackages = new LinkedList();
            node.setProperty(IMPORTED_PACKAGES_KEY, _importedPackages);
        } else {
            _importedPackages = (Collection) node
                    .getDefinedProperty(IMPORTED_PACKAGES_KEY);
        }

        // visit all import nodes
        TNLManip.traverseList(this, null, node.getImports());

        // all files import the metamodel library by default; if this
        // file does not import "metamodel.util" and "metamodel.lang"
        // explicity, import them implicitly.
        if (MetaModelLibrary.LANG_PACKAGE != null)
            _importOnDemand(MetaModelLibrary.LANG_PACKAGE);
        if (MetaModelLibrary.UTIL_PACKAGE != null)
            _importOnDemand(MetaModelLibrary.UTIL_PACKAGE);

        return null;
    }

    /**
     * Resolve the name of the imported class and add it into the import scope.
     * <p>
     * A RuntimeException will be thrown if there are any conflicting names.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitImportNode(ImportNode node, LinkedList args) {

        NameNode name = node.getName();

        // resolve import name
        PackageDecl decl = _resolveImportName(name);

        // validate the import type exists

        ObjectDecl importType = decl.getUserType(name.getIdent());
        if (importType == null) {
            System.err.println("The import type " + name.getIdent()
                    + " cannot be found." + "It may be resolved later.");
            return null;
        }

        name.setProperty(DECL_KEY, decl.getUserType(name.getIdent()));

        // check if there is a same name in the file scope
        // if so, throw a RuntimeException

        Decl old = _fileScope.lookupLocal(name.getIdent());
        Decl current = (Decl) name.getProperty(DECL_KEY);

        if ((old != null) && (old != current)) {
            if (old != current) {
                throw new RuntimeException("attempt to import conflicting "
                        + " name: " + old.getName());
            }
        }

        // add the imported class declaration into the import scope
        Decl imptDecl = (Decl) name.getDefinedProperty(DECL_KEY);
        if (_importScope.lookupLocal(imptDecl.getName()) != imptDecl)
            _importScope.add(imptDecl);

        return null;
    }

    /**
     * Resolve the name of the imported package name and add all declarations of
     * this package into the import scope. Add this package to the
     * _importedPackages list.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    public Object visitImportOnDemandNode(ImportOnDemandNode node,
            LinkedList args) {
        // resolve package name
        NameNode name = node.getName();
        PackageDecl decl = _resolveImportPackageName(node.getName());

        if (decl == null) {
            throw new RuntimeException("Invalid import \"" + name.getIdent()
                    + "\" in file: " + _compileUnit.getProperty(IDENT_KEY));
        }

        name.setProperty(DECL_KEY, decl);

        _importOnDemand(decl);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** The CompileUnitNode that is the root of the tree. */
    protected CompileUnitNode _compileUnit = null;

    /** The file scope. */
    protected Scope _fileScope = null;

    /** The import scope. */
    protected Scope _importScope = null;

    /** The package declaration. */
    protected PackageDecl _packageDecl = null;

    /** The Collection of imported packages for this compile unit. */
    protected Collection _importedPackages = null;

    /** The list containing classes that are importing other classes. */
    protected static LinkedList _importingList = new LinkedList();

    /**
     * The list of classes that needs to be reimported when the _importingList
     * becomes empty.
     */
    protected static LinkedList _reimportList = new LinkedList();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * The default visit method. We shouldn't visit this node, so throw an
     * exception.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return null
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        throw new RuntimeException("ResolveImports not defined on node type : "
                + node.getClass().getName());
    }

    /**
     * Resolve the name of an import by loading the corresponding compile unit.
     *
     * @param node
     *            the node that is being visited.
     * @return The declaration.
     */
    protected PackageDecl _resolveImportName(NameNode node) {
        TreeNode qual = node.getQualifier();
        PackageDecl decl;

        if (qual == AbsentTreeNode.instance) {
            decl = _packageDecl;
            FileLoader.loadCompileUnit(node.getIdent(), 0);
        } else {
            decl = _resolveImportPackageName(qual);
            FileLoader.loadCompileUnit(node.getIdent(), decl, 0);
        }
        return decl;
    }

    /**
     * Resolve the name of an import package by loading the corresponding
     * package.
     *
     * @param node
     *            the node that is being visited.
     * @return The declaration.
     */
    protected PackageDecl _resolveImportPackageName(TreeNode node) {
        NameNode name = (NameNode) node;
        TreeNode qual = name.getQualifier();

        //PackageDecl temp;

        if (qual == AbsentTreeNode.instance) {
            return FileLoader.loadTopLevelPackage(name.getIdent());
        }

        return FileLoader.loadPackage(name.getIdent(),
                _resolveImportPackageName(qual));
    }

    /**
     * If this package has already been imported, ignore duplicate imports.
     * Otherwise, add it to the _importPackages list and add all declarations in
     * the package to the import scope.
     *
     * @param importedPackage
     *            the package declaration that needs to be imported
     */
    protected final void _importOnDemand(PackageDecl importedPackage) {

        // ignore duplicate imports
        if (_importedPackages.contains(importedPackage)) {
            System.err.println("Warning: ResolveImportsVisitor._importOnDemand"
                    + " : ignoring duplicated package "
                    + importedPackage.toString());
            return;
        }

        // add this package to the _importedPackages list
        _importedPackages.add(importedPackage);

        // retrieve all declarations within the package and add all
        // declarations to the import scope
        Scope pkgScope = importedPackage.getScope();
        Iterator scopeItr = pkgScope.allLocalDecls();

        while (scopeItr.hasNext()) {
            MetaModelDecl type = (MetaModelDecl) scopeItr.next();

            if (type.category != CG_PACKAGE) {
                if (_importScope.lookupLocal(type.getName()) != type)
                    _importScope.add(type); // conflicts appear on use only
            }
        }
    }

    /**
     * Resolve the package name <code>qualName</code> and import it by calling
     * _importOnDemand with the package declaration as the argument.
     *
     * @param qualName
     *            the package name that needs to be imported.
     */
    protected final void _importOnDemand(String qualName) {
        NameNode name = (NameNode) makeNameNode(qualName);

        PackageDecl decl = MetaModelLibrary.SYSTEM_PACKAGE.getSubPackage(name
                .getIdent());
        name.setProperty(DECL_KEY, decl);

        _importOnDemand((PackageDecl) name.getDefinedProperty(DECL_KEY));
    }

    /**
     * Return a NameNode corresponding to the name, which is qualified by '.'.
     * If the name is the empty string, return an AbsentTreeNode.
     *
     * @param qualifiedName
     *            the package name that needs to be imported.
     * @return the corresponding NameNode.
     */
    public static final TreeNode makeNameNode(String qualifiedName) {
        TreeNode retval = AbsentTreeNode.instance;

        int firstDotPosition;

        do {
            firstDotPosition = qualifiedName.indexOf('.');

            if (firstDotPosition > 0) {
                String ident = qualifiedName.substring(0, firstDotPosition);

                retval = new NameNode(retval, ident, AbsentTreeNode.instance);

                qualifiedName = qualifiedName.substring(firstDotPosition + 1,
                        qualifiedName.length());

            } else {
                if (qualifiedName.length() > 0) {
                    return new NameNode(retval, qualifiedName,
                            AbsentTreeNode.instance);
                }
            }
        } while (firstDotPosition > 0);

        return retval;
    }
}
