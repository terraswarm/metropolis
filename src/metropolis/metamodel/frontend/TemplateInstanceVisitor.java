/* A visitor that finds instances of templates.

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
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ImportNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.TemplateParametersNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TemplateInstanceVisitor
/**
 * A visitor that traverses a compile unit looking for instances of templates.
 * It returns a list of instances (NameNodes) of templates used in the compile
 * unit. This visitor also checks that the number of type parameters used in the
 * instance of the template is correct according to the number of type
 * parameters of the template declaration.
 * <p>
 * The visit() methods of this visitor do not need any arguments, so the
 * argument list will be set to null. The result of each visit method will be
 * ignored as well, except in CompileUnitNode.
 * <p>
 * Nodes that do not contain any name node can be ignored.
 * <p>
 * For multi-level template instances, only the most inner one is recognized.
 * For example, for A-&lt;B-&lt;int$gt;-$gt;-, only B-&lt;int$gt;- will be
 * recognized and resolved at first time. As a result, it is resolved to
 * A-&lt;B_$int$$gt;-, than TemplateEliminationVisitor will be called again to
 * resolve it to A_$B_$int$$.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TemplateInstanceVisitor.java,v 1.26 2005/10/24 23:11:09 allenh
 *          Exp $
 */
public class TemplateInstanceVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that looks for template instances. The traversal
     * method is set to TM_SELF_FIRST as many nodes will be visited.
     */
    public TemplateInstanceVisitor() {
        super(TM_SELF_FIRST);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit a CompileUnitNode. Store the file name of the compile unit to
     * provide error messages. Return the list of all NameNodes that instantiate
     * templates inside this compile unit.
     *
     * @param node
     *            The compile unit being traversed.
     * @param args
     *            List of arguments (unused).
     * @return The list of template instances used in this compile unit.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // Store the filename to provide error messages
        _filename = (String) node.getProperty(IDENT_KEY);
        return _instances;
    }

    /**
     * Visit a NameNode. If the name refers to a template, check that it uses
     * the right number of arguments; check that no arguments are used if it's
     * not a template. If the name is an instance of a template, add it to the
     * list of template instances.
     *
     * @param node
     *            The NameNode being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitNameNode(NameNode node, LinkedList args) {

        // If the current user type is a template, don't count the template
        // instances within it
        if (_isTemplateDecl)
            return null;

        // Get the numbers of parameters needed in the instance
        int declPars = 0;
        MetaModelDecl decl = MetaModelDecl.getDecl(node);
        if (decl != null) {
            if (decl instanceof ObjectDecl) {
                ObjectDecl obj = (ObjectDecl) decl;
                declPars = obj.getTypeParams().size();
            }
        }

        // Get the number of types used to instantiate the name
        int instancePars = 0;
        TreeNode typePars = node.getParameters();
        if (typePars != AbsentTreeNode.instance) {
            TemplateParametersNode pars = (TemplateParametersNode) typePars;
            instancePars = pars.getTypes().size();

            // if one of the parameters of the name node is a template instance
            // also, skip this name node and it will be resolved later
            Iterator typeIter = pars.getTypes().iterator();
            while (typeIter.hasNext()) {
                TreeNode type = (TreeNode) typeIter.next();
                if (type instanceof TypeNameNode) {
                    if (((TypeNameNode) type).getName().getParameters() != AbsentTreeNode.instance)
                        return null;
                }
            }
        }

        boolean isTemplate = declPars > 0;
        boolean usesPars = instancePars > 0;
        String name = node.getIdent();

        if (decl == null & usesPars) {
            _instances.add(node);
            return null;
        }

        // xichen_template_beg
        // Check the number of type parameters
        if (!isTemplate && usesPars)
            _error("Name '" + name + "' used with type parameters, but "
                    + "it is not a template");

        // These two checks are commented out because template declarations are
        // somehow visited by this visitor. This means that templates with
        // parameter
        // declarations but not instance parameters can be found and will
        // trigger
        // an error.
        /*
         * if (isTemplate && !usesPars) _error("Template '" + name + "' needs " +
         * declPars + " type parameters, but it is used with no type
         * parameters");
         *
         * if (declPars != instancePars) _error("Template '" + name + "'
         * declared with " + declPars + " type parameters but used with " +
         * instancePars);
         */

        // If necessary, aAdd the name to the list of instances
        // if (declPars >0) {
        // _instances.add(node);
        // }
        // If necessary, add the name to the list of instances
        if (declPars > 0 && instancePars == declPars) {
            _instances.add(node);

            // if template instances are found in a user type, clear its visitor
            // list
            // and scope so that the various visitors can go through it once
            // again
            ObjectDecl objectDecl = (ObjectDecl) MetaModelDecl
                    .getDecl(_currentNode.getName());
            objectDecl.clearVisitors();

            List typeParams = objectDecl.getTypeParams();
            Scope typeScope = objectDecl.getScope();
            if (typeScope != null) {
                List declList = typeScope.getDecls();
                declList.clear();
                if (typeParams != null)
                    declList.addAll((Collection) typeParams);
            }
        } else if (instancePars != declPars
                && !(node.getParent() instanceof ImportNode)) {
            _error("The number of type parameters used to instantiate the template \""
                    + node.getIdent().toString()
                    + "\" is "
                    + instancePars
                    + ", but "
                    + declPars
                    + " is/are required by that template.");
        }
        // xichen_template_end;

        return null;
    }

    /**
     * Visit an interface declaration and all its inner classes, check if they
     * are template declarations.
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
     * Visit a class declaration and all its inner classes, check if they are
     * template declarations.
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
     * Visit a process declaration and all its inner classes, check if they are
     * template declarations.
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
     * Visit a scheduler declaration and all its inner classes, check if they
     * are template declarations.
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
     * Visit a netlist declaration and all its inner classes, check if they are
     * template declarations.
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
     * Visit a medium declaration and all its inner classes, check if they are
     * template declarations.
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
     * Visit a state medium declaration and all its inner classes, check if they
     * are template declarations.
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

    /** List of instances of templates in the CompileUnitNode. */
    protected final List _instances = new LinkedList();

    /**
     * Name of the file where the compile unit is stored. Used for error
     * messages.
     */
    protected String _filename = null;

    /** A flag that indicates if the current user type is a template. */
    protected boolean _isTemplateDecl = false;

    /** The current UserTypeDeclNode that is being visited. */
    protected UserTypeDeclNode _currentNode = null;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. This node does not instantiate a template.
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
     * Throw a runtime exception with an error message.
     *
     * @param msg
     *            Error message to be displayed.
     * @exception RuntimeException
     *                to notice the error.
     */
    protected void _error(String msg) {
        throw new RuntimeException("Error in file " + _filename + ": " + msg);
    }

    /**
     * Visit a user type declaration node. If the type is a template, set a flag
     * and the template instances within the type won't be counted by
     * visitNameNode().
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

        // Check if the type is a template
        if (!node.getParTypeNames().isEmpty()) {
            _isTemplateDecl = true;
        } else
            _isTemplateDecl = false;

        _currentNode = node;

        return null;
    }

}
