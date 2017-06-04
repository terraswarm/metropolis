/* A visitor that instantiates a template with a fixed list of types.

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
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.ObjectNode;
import metropolis.metamodel.nodetypes.PrimitiveTypeNode;
import metropolis.metamodel.nodetypes.TemplateParametersNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // InstantiateTemplateDeclVisitor
/**
 * A visitor that traverses a template declaration replacing references to the
 * type parameters by references to the types used to instantiate the template.
 * The names of the constructors of this template are also replaced to match the
 * new name for the instantiated template. This visitor should be invoked in the
 * UserTypeDeclNode where the template is declared, not in the CompileUnitNode.
 * <p>
 * The visit() methods of this visitor use the following arguments:
 * <ul>
 * <li> A reference to the declaration of the template.
 * <li> The name of the instantiated template.
 * <li> The list of TypeNodes used to instantiate this template.
 * </ul>
 * This visitor returns the subtree that should be used instead of current tree.
 * Type parameter names are replaced by type nodes, for example. If the node
 * should not be replaced, then the same node should be returned.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: InstantiateTemplateDeclVisitor.java,v 1.19 2004/09/07 20:33:29
 *          cxh Exp $
 */
public class InstantiateTemplateDeclVisitor extends ReplacementVisitor
        implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new instantiation visitor, that will traverse a template
     * replacing type parameters by concrete types. The traversal method will be
     * set to TM_CHILDREN_FIRST, as we will replace some of the children with
     * the values provided by their visit() methods.
     */
    public InstantiateTemplateDeclVisitor() {
        super(TM_CHILDREN_FIRST);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit a constructor declaration. If this is a constructor of the template
     * that we are instantiating, change the name of the constructor to the new
     * name of the template.
     *
     * @param node
     *            The constructor declaration being visited.
     * @param args
     *            List of arguments (see full description in the comments of
     *            this class).
     * @return The same node.
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        // Check if this is a constructor of the template being
        // instantiated.
        TreeNode parent = node.getParent();
        if (!(parent instanceof UserTypeDeclNode))
            return node;

        MetaModelDecl decl = MetaModelDecl.getDecl(node.getName());
        MetaModelDecl templateDecl = (MetaModelDecl) args.get(0);

        // xichen_template_beg
        // Compare the declaration names only, as the construct and
        // parent categories will differ.
        if (decl.getName() != templateDecl.getName())
            return node;
        // xichen_template_end

        // This is a constructor of the template being instantiated.
        // Change the name of the constructor to the new name of the
        // template instance.
        String newName = (String) args.get(1);
        node.getName().setIdent(newName);

        // Perform the default visit on this node.
        return _defaultVisit(node, args);
    }

    /**
     * Visit a name node. If this name node refers to a type parameter of the
     * template being instantiated, replace it with the type used to instantiate
     * this particular type parameter. Check that the instantiated type makes
     * sense, i.e. if it is qualifying a name of a class, field or method, then
     * the type that we instantiate in this place can only be an object.
     *
     * @param node
     *            The constructor declaration being visited.
     * @param args
     *            List of arguments (see full description in the comments of
     *            this class).
     * @return The subtree that should be used to replace this NameNode.
     */
    public Object visitNameNode(NameNode node, LinkedList args) {
        // test
        //boolean isFound = false;
        //if (node.getIdent().equals("yapioutinterface"))
        //    isFound = true;

        // Replace children if necessary
        _defaultVisit(node, args);

        // Check if this name is a type parameter of the template
        // that we are instantiating.
        MetaModelDecl decl = MetaModelDecl.getDecl(node);
        if (decl == null) {
            if (!(node.getParent().getParent() instanceof TemplateParametersNode))
                return node;

            // Get the type used to replace the type parameter
            ObjectDecl templateDecl = (ObjectDecl) args.get(0);
            List params = templateDecl.getTypeParams();
            Iterator iter = params.iterator();
            while (iter.hasNext()) {
                TypeParameterDecl typeParam = (TypeParameterDecl) iter.next();
                if (typeParam.getName().equals(node.getIdent())) {
                    List types = (List) args.get(2);
                    TypeNode type = (TypeNode) types.get(params
                            .indexOf(typeParam));

                    // Get the subtree that has to replace this NameNode
                    TreeNode replacement = _getReplacement(templateDecl, node,
                            type);

                    return replacement;
                }
            }
            return node;
        }
        if (decl.category != CG_TEMPLATE)
            return node;
        MetaModelDecl parent = decl.getContainer();
        ObjectDecl templateDecl = (ObjectDecl) args.get(0);
        if (parent != templateDecl)
            return node;

        // Get the type used to replace the type parameter
        List params = templateDecl.getTypeParams();
        int pos = params.indexOf(decl);
        List types = (List) args.get(2);
        TypeNode type = (TypeNode) types.get(pos);

        // Get the subtree that has to replace this NameNode
        TreeNode replacement = _getReplacement(templateDecl, node, type);

        return replacement;
    }

    /**
     * Visit a type that has a name. It is possible that the visit() method that
     * has traversed the NameNode has returned a TypeNode instead of a NameNode.
     * If that is the case, replace the TypeNameNode by the type returned by the
     * child.
     *
     * @param node
     *            The type name node being visited.
     * @param args
     *            List of arguments of the visit.
     * @return The TypeNameNode being visited with a name replaced or
     */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        TreeNode result = (TreeNode) node
                .childReturnValueAt(TypeNameNode.CHILD_INDEX_NAME);
        if (result instanceof NameNode) {
            if (result != node.getName())
                node.setName((NameNode) result);
            return node;
        } else if (result instanceof TypeNode) {
            return result;
        } else {
            // FIXME: Checking for debuging purposes
            throw new RuntimeException("Wrong result returned by "
                    + " visitNameNode.");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. If any children has changed, then replace it by the
     * new
     *
     * @param node
     *            Tree node being visited.
     * @param args
     *            List of arguments of the visit.
     * @return The node with changes in its children.
     */
    /*
     * protected Object _defaultVisit(TreeNode node, LinkedList args) { Iterator
     * childIter = node.children().iterator(); int index = 0; while
     * (childIter.hasNext()) { TreeNode child = (TreeNode) childIter.next();
     * TreeNode result = (TreeNode) node.childReturnValueAt(index); if (child !=
     * result) node.setChild(index,result); index++; } return node; }
     */

    /**
     * Check that a NameNode can be replaced by a type, and return the subtree
     * should replace it.
     *
     * @param templateDecl
     *            Template being instantiated.
     * @param node
     *            Type parameter that we want to replace.
     * @param type
     *            Type to be used as a replacement.
     * @return The subtree that should be used to replace this name node.
     */
    protected TreeNode _getReplacement(MetaModelDecl templateDecl,
            NameNode node, TypeNode type) {

        // Replace the type parameter with the fixed type
        boolean isPrimitive = (type instanceof PrimitiveTypeNode);
        boolean isArray = (type instanceof ArrayTypeNode);
        boolean isObject = (type instanceof TypeNameNode);
        if (!isPrimitive && !isArray && !isObject) {
            throw new RuntimeException("Unknown type used to instantiate"
                    + " template " + templateDecl.getName());
        }

        TreeNode parent = node.getParent();
        if ((parent instanceof NameNode) || (parent instanceof ObjectNode)) {
            if (!isObject) {
                throw new RuntimeException("Cannot instantiate type "
                        + " parameter " + node.getIdent() + " of template "
                        + templateDecl.getName() + " with type "
                        + TemplateHandler.buildTypeName(type)
                        + " because the type parameter is used as an object.");
            }
            TypeNameNode namedType = (TypeNameNode) type;
            try {
                return (TreeNode) namedType.getName().clone();
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException("Clone of '"
                        + (TreeNode) namedType.getName() + "' not supported.",
                        ex);
            }
        } else if (parent instanceof TypeNameNode) {
            // xichen_template_beg
            // return (TreeNode) type.clone();

            // If this node is a TypeNameNode, then it is possible that it
            // contains more template parameters that need to be dealt with.
            if (type instanceof TypeNameNode) {
                // Clone the replacement node.
                TypeNameNode replacement;
                try {
                    replacement = (TypeNameNode) type.clone();
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException("Clone of '"
                            + (TypeNameNode) type + "' not supported.", ex);
                }

                // Check to see if this node is a template and needs to be
                // resolved.
                TreeNode paramNode = replacement.getName().getParameters();
                if (paramNode != AbsentTreeNode.instance) {
                    TemplateParametersNode params = (TemplateParametersNode) paramNode;
                    // These next few lines simply remove all of the parameters
                    // and change the name to reflect that this type refers to
                    // a type that is a resolved template.
                    NameNode name = replacement.getName();
                    List types = params.getTypes();
                    ObjectDecl decl = (ObjectDecl) MetaModelDecl
                            .getDecl((TreeNode) replacement);
                    String newName = TemplateHandler.buildNewName(decl, types);
                    name.setIdent(newName);
                    name.setParameters(AbsentTreeNode.instance);
                }
                // Return the de-templated node.
                return (TreeNode) replacement;
            } else {

                // Return a simple clone of this type.
                try {
                    return (TreeNode) type.clone();
                } catch (CloneNotSupportedException ex) {
                    throw new RuntimeException("Clone of '" + (TreeNode) type
                            + "' not supported.", ex);
                }
            }
            // xichen_template_end
        }
        // If we reach this point, we have found a type parameter in
        // place where it was not expected.
        throw new RuntimeException("Type parameter used in a wrong context "
                + " in the program, not checked by the compiler");
    }

}
