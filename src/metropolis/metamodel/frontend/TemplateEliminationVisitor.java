/* A visitor that rewrites a meta-model program with templates into a
 semantically equivalent meta-model program that does not have templates.

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
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Scope;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ImportNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.TemplateParametersNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TemplateEliminationVisitor
/**
 * This visitor rewrites a meta-model program that declares or uses templates
 * into a semantically equivalent meta-model program that does not use nor
 * declare any template. If the meta-model program does not use or declare any
 * template, it is not changed by this visitor.
 * <p>
 * This visitor uses the following auxiliary visitors:
 * <ul>
 * <li> TemplateDeclarationVisitor: returns a list of the declarations of
 * templates that appear in the compile unit.
 * <li> TemplateInstanceVisitor: returns a list of the instances of templates
 * that appear in the compile unit.
 * </ul>
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TemplateEliminationVisitor.java,v 1.25 2005/10/17 19:34:35
 *          allenh Exp $
 */
public class TemplateEliminationVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that performs template elimination. The traversal
     * method for this visitor will be custom, as only some nodes will be
     * traversed.
     */
    public TemplateEliminationVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    /**
     * - Check problems with "infinite number of template instances"?
     *
     * template(a) public class X { X&lt; a[] &gt; problem; }
     *
     * If X is ever instantiated, say with type T, it will also be instantiated
     * with a field of type T[]. But then, the object in this field will be
     * instantied with type T[] and have a field of type T[][]. It results that
     * we need to instantiat T[]*, an infinite number of template instances, to
     * implement this class.
     *
     * The same thing happens with the following circularity:
     *
     * template (a) template (b) class X1 { class X2 { X2&lt; X1 &lt;a&gt; &gt;
     * p1; X1&lt; X2 &lt; b&gt; &gt; p2; } }
     *
     * If a = int b = X1 &lt; int &gt; (field of X1) a = X2 &lt; X1 &lt; int
     * &gt; &gt; (field of X2) b = X1 &lt; X2 &lt; X1 &lt; int &gt; &gt; &gt;
     * (field of X!)
     *
     * And the same thing happens in
     *
     * template (a) class X1
     *
     * By the way, can we do this?
     *
     * template(a) class X1 { a&lt;int&gt; container; // Use type parameter as a
     * template }
     *
     *  - Step 1: Build a map with PackageDecls -&gt; List of TypeNodes used to
     * instance it At this step, a template can be instatiated with types that
     * are type parameters of another template, or types that are "derived" from
     * the type parameters of another template (even itself).
     *  - Step 2: Build the "transitive closure" of this map. At this step, the
     * templates can only be instantiated by primitive or reference types that
     * do not depend in any way on the type parameters or another templates (or
     * itself).
     *  - Step 3: Foreach list of instances, produce a file
     *
     *
     */

    // /////////////////////////////////////////////////////////////////
    // // public methods ////
    /**
     * Visit a CompileUnitNode.
     *
     * @param node
     *            The compile unit being visited.
     * @param args
     *            List of arguments.
     * @return null (unused).
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // FIXME: checking templates or not might be a compilation flag?

        /*
         * List decls= (List) node.accept(new TemplateDeclarationVisitor(),
         * TNLManip.addFirst(Boolean.FALSE)); List instances = (List)
         * node.accept(new TemplateInstanceVisitor(), null);
         *
         * String name = (String) node.getProperty(IDENT_KEY);
         *
         * if (!decls.isEmpty()) { // FIXME: Eliminate template declarations
         * here System.out.println("File '" + name + "' declares " +
         * decls.size() + " templates."); }
         *
         * if (!instances.isEmpty()) { // FIXME: Replace template instances here
         * System.out.println("File " + name + "instances " + instances.size() + "
         * templates."); } return null;
         */

        // xichen_template_beg
        // Create a list of all template declarations within the ast. The list
        // contains a bunch of MetaModelDecl objects.
        List decls = (List) node.accept(new TemplateDeclarationVisitor(),
                TNLManip.addFirst(Boolean.TRUE));

        // Create a list of all template instances within the ast. The list is
        // composed of NamedNode pointers.
        List instances = (List) node
                .accept(new TemplateInstanceVisitor(), null);

        // Walk through the entire list of instances and make a new list
        // containing only template instances that are not dependent on
        // some other type.
        Iterator iter = instances.iterator();
        List instantiatedList = new LinkedList();
        MetaModelDecl templDecl = null;
        NameNode instanceNode = null;
        String instanceName = null, instantiatedName = null;
        TemplateParametersNode params = null;
        TreeNode instantiatedTree = null, parentNode = null;

        LinkedHashSet revisitNodeSet = new LinkedHashSet();

        while (iter.hasNext()) {
            // Obtain the name of the object used in this instance. It will be
            // compared with the names of the template declaration objects in
            // this AST node.
            instanceNode = (NameNode) iter.next();
            instanceName = instanceNode.getIdent();
            templDecl = _getMatchingDecl(instanceName, decls);

            if (templDecl != null) {
                // The rest of this section creates the new ast, if necessary.
                // Start by creating the instantiated name for this instance.
                params = (TemplateParametersNode) instanceNode.getParameters();
                instantiatedName = TemplateHandler.buildNewName(
                        (ObjectDecl) templDecl, params.getTypes());

                // Make sure an AST instantiated with the same types has not
                // already been created.
                if (instantiatedList.indexOf(instantiatedName) == -1
                        && !_isInstantiated(instantiatedName, node)) {
                    instantiatedList.add(instantiatedName);

                    // Create the instantiated template tree using the static
                    // TemplateHandler class functions.
                    instantiatedTree = TemplateHandler.instantiate(
                            (ObjectDecl) templDecl, params.getTypes());

                    // Place the newly created tree into the correct location,
                    // alongside the original template declaration tree.
                    _placeInstantiatedTree(templDecl, instantiatedName,
                            instantiatedTree);
                    revisitNodeSet.add(node);
                }

                // Replace the template name for this instance.
                instanceNode.accept(new InstantiateTemplateUseVisitor(), null);
                continue;
            }

            // search the template declaration in this package
            templDecl = _getMatchingDeclFromPackage(instanceName, node);

            if (templDecl != null) {
                // The rest of this section creates the new ast, if necessary.
                // Start by creating the instantiated name for this instance.
                params = (TemplateParametersNode) instanceNode.getParameters();
                instantiatedName = TemplateHandler.buildNewName(
                        (ObjectDecl) templDecl, params.getTypes());

                // Make sure an AST instantiated with the same types has not
                // already been created within the package.
                if (!_isInstantiated(instantiatedName, node)) {
                    // Get the CompileUnitNode where the template prototype
                    // is defined
                    parentNode = templDecl.getSource().getParent();

                    // Create the instantiated template tree using the static
                    // TemplateHandler class functions.
                    instantiatedTree = TemplateHandler.instantiate(
                            (ObjectDecl) templDecl, params.getTypes(),
                            (CompileUnitNode) parentNode, false);

                    // Place the newly created tree into the correct location,
                    // alongside the original template declaration tree.
                    _placeInstantiatedTree(templDecl, instantiatedName,
                            instantiatedTree);
                    revisitNodeSet.add(parentNode);
                }

                // Replace the template name for this instance.
                instanceNode.accept(new InstantiateTemplateUseVisitor(), null);
                continue;
            }

            // search the template declaration in all the imports
            templDecl = _getMatchingDeclFromImports(instanceName, node);
            if (templDecl != null) {
                // The rest of this section creates the new ast, if necessary.
                // Start by creating the instantiated name for this instance.
                params = (TemplateParametersNode) instanceNode.getParameters();
                instantiatedName = TemplateHandler.buildNewName(
                        (ObjectDecl) templDecl, params.getTypes());

                // Make sure an AST instantiated with the same types has not
                // already been created within the package.
                if (!_isInstantiated(instantiatedName, node)) {
                    // Get the CompileUnitNode where the template prototype
                    // is defined
                    parentNode = templDecl.getSource().getParent();

                    // Create the instantiated template tree using the static
                    // TemplateHandler class functions.
                    instantiatedTree = TemplateHandler.instantiate(
                            (ObjectDecl) templDecl, params.getTypes(),
                            (CompileUnitNode) parentNode, true);

                    // Place the newly created tree into the correct location,
                    // alongside the original template declaration tree.
                    _placeInstantiatedTree(templDecl, instantiatedName,
                            instantiatedTree);
                    _addImportNode(node, templDecl, instantiatedName,
                            MetaModelDecl.getDecl(instantiatedTree));
                    revisitNodeSet.add(parentNode);
                }

                // Replace the template name for this instance.
                instanceNode.accept(new InstantiateTemplateUseVisitor(), null);
            }
        }

        Iterator revisitIter = revisitNodeSet.iterator();
        while (revisitIter.hasNext()) {
            CompileUnitNode revisitNode = (CompileUnitNode) revisitIter.next();
            revisitNode.accept(this, args);
        }

        // The final work to be done is elmination of all template
        // declarations now that each instance has been resolved. Note that
        // even template declarations that were not instanced will be removed.
        /*
         * iter = decls.iterator(); while (iter.hasNext()) { templDecl =
         * (MetaModelDecl)iter.next(); TreeNode parent =
         * templDecl.getSource().getParent(); Iterator iterChild =
         * parent.children().iterator(); boolean found = false; while
         * (iterChild.hasNext()) { Object child = iterChild.next(); if (child
         * instanceof List) { Iterator iterChildList = ((List)child).iterator();
         * while (iterChildList.hasNext() && found == false) { Object childElem =
         * iterChildList.next(); MetaModelDecl decl = MetaModelDecl.getDecl(
         * (TreeNode)childElem); if (decl != null && decl.getName() ==
         * templDecl.getName()) { found = true; ((List)child).remove(childElem);
         * break; } } } }
         *
         * //xichen also delete the template decls from the scope hierarchy
         * Scope parentScope = templDecl.getScope().parent(); while (parentScope !=
         * null) { if (parentScope.lookupLocal(templDecl.getName()) ==
         * templDecl) parentScope.remove(templDecl);
         *
         * parentScope = parentScope.parent(); }
         *
         * MetaModelDecl parentDecl = templDecl.getContainer(); while
         * (parentDecl != null) { if (parentDecl instanceof PackageDecl &&
         * ((PackageDecl)parentDecl).membersLoaded()) { Scope scp =
         * parentDecl.getScope(); if (scp.lookupLocal(templDecl.getName()) ==
         * templDecl) scp.remove(templDecl); }
         *
         * parentDecl = parentDecl.getContainer(); } }
         */

        // Output some stuff that the user may want to know.
        // String name = (String) node.getProperty(IDENT_KEY);
        // System.out.println("File '" + name + "' declares " + decls.size()
        // + " templates.");
        // System.out.println("File " + name + " instantiates " +
        // instantiatedList.size() + " templates.");
        // Only perform passes 0-2 if instances of templates were found in
        // this ast.
        // boolean retVal[] = {(instances.size() > 0) ? true: false};
        // return retVal;
        _revisitNodeSet.addAll((Collection) revisitNodeSet);
        if (instances.size() > 0)
            _revisitNodeSet.add(node);
        return _revisitNodeSet;

        // xichen_template_end
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * List of ASTs that are altered during template resolution and need go
     * through all the other visitors once again.
     */
    protected static LinkedHashSet _revisitNodeSet = new LinkedHashSet();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // xichen_template_beg
    /**
     * Search for a matching declaration name in a list of template
     * declarations.
     *
     * @param instName
     *            A template instance name.
     * @param decls
     *            A list of template declarations being searched.
     * @return The first matched template declaration. If there is no matching,
     *         return null.
     */
    protected MetaModelDecl _getMatchingDecl(String instName, List decls) {
        MetaModelDecl templDecl = null;
        String templName = null;

        // Walk through the entire list of template declarations searching
        // for a matching declaration name.
        Iterator iter = decls.iterator();
        while (iter.hasNext()) {
            // Obtain the name for this template declaration.
            templDecl = (MetaModelDecl) iter.next();
            templName = templDecl.getName();

            // If it matches the instance name, return this declaration.
            if (instName == templName)
                return templDecl;
        }

        // No matching template declaration was found in the list.
        // throw new RuntimeException(
        // "No template declaration found for '" + instName + "'.\n");

        return null;
    }

    /**
     * Place an instantiated tree node for a template instance into the existing
     * AST.
     *
     * @param templDecl
     *            The template declaration being instantiated.
     * @param instantiatedName
     *            The new name for the template instance.
     * @param instantiatedTree
     *            The instantiated tree node for a template instance.
     */
    protected void _placeInstantiatedTree(MetaModelDecl templDecl,
            String instantiatedName, TreeNode instantiatedTree) {
        TreeNode parent = instantiatedTree.getParent();
        MetaModelDecl dec = MetaModelDecl.getDecl(instantiatedTree);

        // First add the decl to the file scope.
        Scope scope = (Scope) parent.getDefinedProperty(SCOPE_KEY);
        if (scope != null)
            scope.add(dec);

        // Then add the decl to the package scope.
        scope = scope.parent();
        if (scope != null)
            scope.add(dec);

        // add the decl to the import scope where the template is imported
        Iterator astIter = FileLoader._pass1ResolvedList.iterator();
        while (astIter.hasNext()) {
            CompileUnitNode ast = (CompileUnitNode) astIter.next();
            Scope fileScope = (Scope) ast.getDefinedProperty(SCOPE_KEY);
            Scope importScope = fileScope.parent().parent();
            if (importScope.lookupLocal(templDecl.getName()) == templDecl
                    && importScope.lookupLocal(dec.getName()) != dec)
                importScope.add(dec);
        }

        // also add the decl to all the super scopes that contain the templDecl
        /*
         * Scope parentScope = scope.parent(); while (parentScope != null) { if
         * (parentScope.lookupLocal(templDecl.getName()) == templDecl &&
         * parentScope.lookupLocal(dec.getName()) != dec) parentScope.add(dec);
         *
         * parentScope = parentScope.parent(); }
         */

        // also add the decl to all the other scopes that contain the templDecl
        /*
         * MetaModelDecl parentDecl = dec.getContainer(); while (parentDecl !=
         * null) { if (parentDecl instanceof PackageDecl &&
         * ((PackageDecl)parentDecl).membersLoaded()) { Scope scp =
         * parentDecl.getScope(); if (scp.lookupLocal(templDecl.getName()) ==
         * templDecl && scope.lookupLocal(dec.getName()) != dec) scp.add(dec); }
         *
         * parentDecl = parentDecl.getContainer(); }
         */

        // The children of the parent should be a series of TreeNodes and
        // Lists. For each list, search it for the template declaration. When
        // a declaration is found, add the new, instantiated template into the
        // list.
        /*
         * List children = parent.children(); Iterator iterChild =
         * children.iterator(); boolean found = false; while
         * (iterChild.hasNext() && found == false) { Object child =
         * iterChild.next();
         *  // Search any list for the template declaration. if (child
         * instanceof List) {
         *
         * Iterator iterChildList = ((List)child).iterator(); while
         * (iterChildList.hasNext()) {
         *
         * Object childListElem = iterChildList.next(); if (childListElem
         * instanceof TreeNode) {
         *
         * MetaModelDecl decl = MetaModelDecl.getDecl( (TreeNode)childListElem);
         *
         * if (decl != null && decl.getName().compareTo( templDecl.getName()) ==
         * 0) { found = true; ((List)child).add(instantiatedTree); break; } } } } }
         */

        if (parent instanceof CompileUnitNode)
            ((CompileUnitNode) parent).getDefTypes().add(instantiatedTree);
    }

    /**
     * Search for a matching declaration of a template name in the scope of a
     * package.
     *
     * @param instName
     *            A template instance name.
     * @param node
     *            A CompileUnitNode that is in the package being searched.
     * @return The first matched template declaration. If there is no matching,
     *         return null.
     */
    protected MetaModelDecl _getMatchingDeclFromPackage(String instName,
            CompileUnitNode node) {
        ObjectDecl templDecl = null;
        //String templName;

        Scope pkgScope = ((Scope) node.getProperty(SCOPE_KEY)).parent();
        templDecl = (ObjectDecl) pkgScope.lookupLocal(instName);
        if (templDecl == null || templDecl.getTypeParams() == null
                || templDecl.getTypeParams().isEmpty())
            return null;
        return templDecl;
    }

    /**
     * Search for a matching declaration of a template name in the scope of all
     * the imported packages.
     *
     * @param instName
     *            A template instance name.
     * @param node
     *            A CompileUnitNode that imports the packages being searched.
     * @return The first matched template declaration. If there is no matching,
     *         return null.
     */
    protected MetaModelDecl _getMatchingDeclFromImports(String instName,
            CompileUnitNode node) {
        ObjectDecl templDecl = null;
        //String templName = null;

        Scope pkgScope = ((Scope) node.getProperty(SCOPE_KEY)).parent();
        templDecl = (ObjectDecl) pkgScope.lookup(instName);
        if (templDecl == null || templDecl.getTypeParams() == null
                || templDecl.getTypeParams().isEmpty())
            return null;

        return templDecl;
    }

    /**
     * Check if a template instance has been instantiated within the visible
     * scope that includes the CompileUnitNode.
     *
     * @param instanceName
     *            A template instance name.
     * @param node
     *            A CompileUnitNode whose scope is searched for the template
     *            instance.
     * @return true if the template instance has been instantiated within the
     *         visible scope that includes the CompileUnitNode.
     */
    protected boolean _isInstantiated(String instanceName, CompileUnitNode node) {
        ObjectDecl instDecl = null;
        Scope pkgScope = ((Scope) node.getProperty(SCOPE_KEY)).parent();
        instDecl = (ObjectDecl) pkgScope.lookup(instanceName);
        if (instDecl == null)
            return false;
        else
            return true;
    }

    /**
     * Add an import node for an instantiated template if the template prototype
     * is imported.
     *
     * @param node
     *            A CompileUnitNode where import nodes need to be added.
     * @param templDecl
     *            The declaration of the template prototype.
     * @param instantiatedName
     *            The name of the instantiated template.
     * @param instantiatedDecl
     *            The declaration of the instantiated template.
     */
    protected void _addImportNode(CompileUnitNode node,
            MetaModelDecl templDecl, String instantiatedName,
            MetaModelDecl instantiatedDecl) {
        Iterator importIter = node.getImports().iterator();
        ImportNode newImpt = null;
        TreeNode impt = null;

        while (importIter.hasNext()) {
            impt = (TreeNode) importIter.next();
            if (impt instanceof ImportNode) {
                if (((ImportNode) impt).getName().getIdent().equals(
                        instantiatedName))
                    return;
            }
        }

        importIter = node.getImports().iterator();
        while (importIter.hasNext()) {
            impt = (TreeNode) importIter.next();
            if (MetaModelDecl.getDecl(impt) == templDecl) {
                try {
                    newImpt = (ImportNode) impt.clone();
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException("Clone of '" + (ImportNode) impt
                            + "' not supported.", ex);
                }
                newImpt.getName().setIdent(instantiatedName);
                newImpt.getName().setProperty(DECL_KEY, instantiatedDecl);
                break;
            }
        }

        if (newImpt != null)
            node.getImports().add(newImpt);
    }

    /*
     * protected boolean _templateNested(NameNode instNode) { // Obtain the
     * template parameters. TemplateParametersNode params =
     * (TemplateParametersNode)instNode.getParameters(); List paramsList =
     * params.getTypes(); // Test each parameter to see if it contains a
     * template. Iterator iter = paramsList.iterator(); while (iter.hasNext()) {
     * TypeNode type = (TypeNode)iter.next(); if (_templateNested(type)) return
     * true; } // Nope, this template is not nested. return false; }
     *
     * protected boolean _templateNested(TypeNode type) { // Check this node to
     * see if it is one of the type possible types of // template nodes: either
     * an array or a name node. if (type.classID() == ARRAYTYPENODE_ID) { // If
     * this node is an array type, then check the type of the array // to see if
     * it is a template. ArrayTypeNode array = (ArrayTypeNode)type; return
     * _templateNested(array.getBaseType()); } else if (type.classID() ==
     * TYPENAMENODE_ID) { // Check the named node to see if it contains template
     * parameters. // If so, then this node is a template, and as it is part of
     * a // template, there must be nested templates. TypeNameNode namedType =
     * (TypeNameNode)type; NameNode name = namedType.getName(); TreeNode params =
     * name.getParameters(); // Theoretically, a check for null here is not
     * necessary, but one // can never be too safe. if (params ==
     * AbsentTreeNode.instance || params == null) return false; else { // If the
     * parameters list is not empty, then this node is a // template.
     * TemplateParametersNode pars = (TemplateParametersNode)params; if
     * (!pars.getTypes().isEmpty()) return true; } } return false; }
     */
    // xichen_template_end
}
