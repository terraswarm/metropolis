/* A visitor that finds declarations of templates (class, process, medium,
 interface, netlist, statemedium), and optionally checks these declarations.

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
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TemplateDeclarationVisitor
/**
 * A visitor that traverses a compile unit looking for declarations of
 * templates. It returns a list of the templates (ObjectDecls) declared in the
 * compile unit. Optionally, it can check the declarations of these templates.
 * <p>
 * The visit() methods of this visitor have one argument, a boolean value. If it
 * is true, then it means that the template declarations found as children of
 * this node have to be checked. Otherwise, the template declarations do not
 * need to be checkd. The result of the visit will be ignored, except in
 * CompileUnitNode, where it returns the list of template declarations.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TemplateDeclarationVisitor.java,v 1.12 2004/09/07 15:01:55 cxh
 *          Exp $
 */
public class TemplateDeclarationVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that traverses the compile unit looking for
     * declarations of templates. The traversal method will be TM_CUSTOM, as
     * only few nodes will be traversed.
     */
    public TemplateDeclarationVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit all the object declarations in a CompileUnitNode. Package and
     * imports are ignored.
     *
     * @param node
     *            The compile unit being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return A list of the ObjectDecls of templates that appear in the
     *         CompileUnitNode.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        List typeDecls = node.getDefTypes();
        TNLManip.traverseList(this, args, typeDecls);
        return _templates;
    }

    /**
     * Visit an interface declaration and all its inner classes, looking for
     * template declarations. All other children are ignored.
     *
     * @param node
     *            The interface declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    /**
     * Visit a class declaration and all its inner classes, looking for template
     * declarations. All other children are ignored.
     *
     * @param node
     *            The class declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    /**
     * Visit a process declaration and all its inner classes, looking for
     * template declarations. All other children are ignored.
     *
     * @param node
     *            The process declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    /**
     * Visit a scheduler declaration and all its inner classes, looking for
     * template declarations. All other children are ignored.
     *
     * @param node
     *            The scheduler declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    /**
     * Visit a netlist declaration and all its inner classes, looking for
     * template declarations. All other children are ignored.
     *
     * @param node
     *            The netlist declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    /**
     * Visit a medium declaration and all its inner classes, looking for
     * template declarations. All other children are ignored.
     *
     * @param node
     *            The medium declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    /**
     * Visit a state medium declaration and all its inner classes, looking for
     * template declarations. All other children are ignored.
     *
     * @param node
     *            The state medium declaration being visited.
     * @param args
     *            List of arguments. There should only be one argument
     *            (boolean), if it is true then template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node, args);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** List of template declarations in the compile unit. */
    protected final List _templates = new LinkedList();

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. This node does not declare a template, and it does
     * not have any child that declares a template. Therefore, we do not
     * traverse the children of this node.
     *
     * @param node
     *            The node being visited (unused).
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }

    /**
     * Visit a user type declaration node. If the type is a template, add it to
     * the list of templates of the compile unit. In any case, traverse the
     * members to look for inner classes that are templates. If we have to check
     * template declarations, invoke TemplateChecksVisitor.
     *
     * @param node
     *            Declaration node being visited.
     * @param args
     *            List of arguments. There should only be one argument,
     *            (boolean) if true then the template declarations that are
     *            children of this node have to be checked.
     * @return null (unused).
     */
    protected List _visitUserTypeDecl(UserTypeDeclNode node, LinkedList args) {

        // FIXME: This check is only done for debugging purposes,
        // as this visitor should alway be called with the correct
        // number of arguments.
        if ((args == null) || (args.size() != 1)) {
            throw new RuntimeException("TemplateDeclarationVisitor called"
                    + " with a wrong number of arguments");
        }

        // Get the 'check template declarations?' flag
        Boolean flag = (Boolean) args.getFirst();
        boolean check = flag.booleanValue();

        // Check if the type is a template
        if (!node.getParTypeNames().isEmpty()) {
            MetaModelDecl decl = MetaModelDecl.getDecl(node.getName());
            // FIXME: This check is only made for debugging purposes.
            // Pass 0 has been resolved, so the declaration should
            // be defined.
            // xichen_template_beg
            if (decl == null) {
                String name = node.getName().getIdent();
                throw new RuntimeException("Error : declaration of " + name
                        + " not available after pass 0?");
            }
            // xichen_template_end

            _templates.add(decl);

            // Check the correcteness of this template and children
            // Set the flag to false, so that templates inside this
            // template are not checked twice.
            if (check) {
                node.accept(new TemplateChecksVisitor(), null);
                args = TNLManip.addFirst(Boolean.FALSE);
            }
        }

        // Traverse the members of this node
        List members = node.getMembers();
        TNLManip.traverseList(this, args, members);

        return null;
    }

}
