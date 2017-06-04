/* PackageResolutionVisitor builds a file scope.

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

import metropolis.metamodel.Compiler;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Scope;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.NameNode;

import java.io.File;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // PackageResolutionVisitor
/**
 * Set property PACKAGE_KEY in the CompileUnitNode and build a file scope. Call
 * ResolvePackageVisitor and ResolveTypesVisitor in this order.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @see ResolvePackageVisitor
 * @see ResolveImportsVisitor
 * @see ResolveTypesVisitor
 *
 * @author Jeff Tsay & DunyLam
 * @version $Id: PackageResolutionVisitor.java,v 1.25 2005/10/24 23:11:08 allenh
 *          Exp $
 */
public class PackageResolutionVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new PackageResolutionVisitor. Set the traversal method to
     * TM_CUSTOM.
     */
    public PackageResolutionVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * It first resolve the name of the package in which this CompileUnitNode
     * belongs. Declaration for this package will be defined during the
     * resolution.
     * <p>
     * PACKAGE_KEY property of the CompileUnitNode would be set to the
     * declaration of the package resulted from the first step.
     * <p>
     * The file scope for this CompileUnitNode has its parent as the package
     * scope, which in turn has the SYSTEM_PACKAGE as its parent scope.
     * <p>
     * SCOPE_KEY property of the CompileUnitNode would be set to the file scope
     * established from the previous step.
     * <p>
     * Call ResolvePackageVisitor and ResolveTypesVisitor in order.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            arguments to this visit method
     * @return Always return null.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        PackageDecl thePkgDecl = _resolvePkgDecl(node);
        node.setProperty(PACKAGE_KEY, thePkgDecl);

        // Build an scope for this file
        Scope importOnDemandScope = new Scope(MetaModelLibrary.SYSTEM_PACKAGE
                .getScope());
        Scope pkgScope = thePkgDecl.initGetScope();
        Scope fileScope = new Scope(importOnDemandScope, pkgScope.getDecls());

        // pkgScope.copyDeclList(thePkgDecl.getScope());
        Scope scope = new Scope(fileScope); // the file level scope
        node.setProperty(SCOPE_KEY, scope);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////
    /**
     * Returns the package declaration of this CompileUnitNode. If it does not
     * have a package declaration, set declaration to UNNAMED_PACKAGE.
     *
     * @param node
     *            the CompileUnitNode
     * @return the package declaration of this CompileUnitNode
     */
    protected PackageDecl _resolvePkgDecl(CompileUnitNode node) {
        PackageDecl thePkgDecl;
        TreeNode pkgDeclNode = node.getPkg();

        if (pkgDeclNode == AbsentTreeNode.instance) {
            // The file belongs to the unnamed package
            thePkgDecl = MetaModelLibrary.UNNAMED_PACKAGE;
        } else {
            // Find the packages in the classpath
            try {
                thePkgDecl = _resolveParentPkgDecl(pkgDeclNode);
            } catch (Exception e) {
                String ident = (String) node.getProperty(IDENT_KEY);
                throw new RuntimeException("In file '" + ident
                        + "', error in package declaration.", e);
            }
        }

        return thePkgDecl;
    }

    /**
     * Recursive function to load packages and return the package declaration.
     * If the package is top-level, call the FileLoader.loadTopLevelPackage().
     * Otherwise, call FileLoader.loadPackage() with the parent package. The
     * parent package name is the qualifier of the node. If the qualifier is
     * AbsentTreeNode.instance, it is a top-level package.
     *
     * @param node
     *            the node whose package needs to be loaded
     * @return the PackageDecl of <code>node</code>
     */
    protected PackageDecl _resolveParentPkgDecl(TreeNode node) {
        NameNode nameNode = (NameNode) node;
        String ident = nameNode.getIdent();
        TreeNode qual = nameNode.getQualifier();
        PackageDecl decl = null;

        if (qual == AbsentTreeNode.instance) {
            // Base case = top level package
            decl = FileLoader.loadTopLevelPackage(ident);
            if (decl == null) {
                String classpathString = "Unknown";
                try {
                    classpathString = FileLoader.classPathToString(", ");
                } catch (Exception ex2) {
                    System.err.println("Warning, problem getting classpath?");
                    ex2.printStackTrace();
                }
                throw new RuntimeException("Top level package '" + ident
                        + "' not found, check that classpath contains the "
                        + "parent directory of '" + ident + "'\n"
                        + "Metropolis classpath was: " + classpathString);
            }
        } else {
            // Recursion = a qualified package
            PackageDecl parent = _resolveParentPkgDecl(qual);
            decl = FileLoader.loadPackage(ident, parent);
            if (decl == null) {
                throw new RuntimeException("Subpackage '" + parent.fullName()
                        + "." + ident + "' not found, but parent package '"
                        + parent.fullName() + "' is found. "
                        + "Check that the name of " + "the subpackage ("
                        + ident + ") is correct, and that"
                        + " there is a directory called '" + ident + "' in '"
                        + parent.fullName(File.separatorChar) + "'. "
                        + "This problem can occur if you have two directories "
                        + "with the same name in your Metropolis classpath. "
                        + "Also, check that the METRO_CLASSPATH environment "
                        + "variable is set properly. The Metropolis "
                        + "classpath was: '" + Compiler.classPathToString()
                        + "'. The parent package was: " + parent.description());
            }
        }
        return decl;
    }

}
