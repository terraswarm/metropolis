/* A class that handle instantiations of template definitions.

 Metropolis: Design Environment for Heterogeneous Systems.

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
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.TemplateParametersNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TemplateHandler
/**
 * A class that handles the instantiation of template classes. A template class
 * like <code> template (type1,type2) process A {} </code> can be instantiated
 * with several types in its type parameters. Each of this instances will be a
 * copy of the original AST that declares the class, with the type parameter
 * replaced by the instance types.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TemplateHandler.java,v 1.29 2006/10/12 20:34:22 cxh Exp $
 */
public class TemplateHandler implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Instantiate a template with a list of TypeNodes. The list of TypeNodes
     * cannot be any type that depends on a type parameter of the current
     * template. The template is defined in the same file.
     *
     * @param templateDecl
     *            Declaration of the template being instantiated.
     * @param types
     *            ArrayList of TypeNodes with the values of the type parameters
     *            for this instantiation.
     * @return The AST of the instantiated template that is a copy of the
     *         original template with the following changes:
     *         <ul>
     *         <li> The name of the template class will changed, e.g. List-<int>-
     *         to List_$int$
     *         <li> The new class will have no name parameters
     *         <li> NameNodes that refer to the type parameters will be changed
     *         to names of the types being instantiated.
     *         <li> Declarations inside the class will be invalidated
     *         </ul>
     *         This AST will not have any declarations, so pass 0 will have to
     *         be repeated in the copied class.
     */
    // xichen_template_beg
    public static TreeNode instantiate(ObjectDecl templateDecl, List types) {
        return instantiate(templateDecl, types, null, false);
    }

    /*
     * public static TreeNode instantiate(ObjectDecl templateDecl, List types) {
     * String newName = buildNewName(templateDecl,types);
     *  // Copy the template definition TreeNode definition =
     * templateDecl.getSource(); UserTypeDeclNode instance; try { instance =
     * (UserTypeDeclNode) definition.clone(); } catch
     * (CloneNotSupportedException ex) { throw new RuntimeException("Clone of '" +
     * (UserTypeDeclNode) definition + "' not supported.", ex); }
     *  // Change the name of the class instance.getName().setIdent(newName);
     *  // Create a new decl for the class. ObjectDecl decl; try { decl =
     * (ObjectDecl)MetaModelDecl .getDecl((TreeNode)instance).clone(); } catch
     * (CloneNotSupportedException ex) { throw new RuntimeException("Clone of '" +
     * (MetaModelDecl)MetaModelDecl.getDecl((TreeNode)instance) + "' not
     * supported.", ex); }
     *  // create a new scope Scope newScope = new
     * Scope(decl.getScope().parent()); instance.setProperty(SCOPE_KEY,
     * newScope);
     *  // Eliminate the type parameters from the instance
     * instance.setParTypeNames(new LinkedList());
     *  // Set the flag and indicate that it's an instantiated node
     * instance.getName().setProperty(TEMPLDECL_KEY, templateDecl);
     *  // reset the fields in decl if necessary
     * MetaModelDecl.setDecl((TreeNode)instance, decl); decl.setName(newName);
     * decl.setScope(newScope); decl.setSource(instance); decl.setTypeParams(new
     * LinkedList()); decl.setDefType(new TypeNameNode(instance.getName()));
     * decl.clearVisitors();
     *  // Call instantiation visitor to instantiate the template LinkedList
     * args = new LinkedList(); args.add(templateDecl); args.add(newName);
     * args.add(types); instance.accept(new InstantiateTemplateDeclVisitor(),
     * args);
     *
     * return instance; }
     */

    /**
     * Instantiate a template with a list of TypeNodes. The list of TypeNodes
     * cannot be any type that depends on a type parameter of the current
     * template. The template is defined in a different file or a different
     * package.
     *
     * @param templateDecl
     *            Declaration of the template being instantiated.
     * @param types
     *            ArrayList of TypeNodes with the values of the type parameters
     *            for this instantiation.
     * @param parentNode
     *            The CompileUnitNode where the template instance will be added
     *            as a child node.
     * @param isImported
     *            A flag that indicates if the template declaration is imported
     *            from an outside package.
     * @return The AST of the instantiated template that is a copy of the
     *         original template with the following changes:
     *         <ul>
     *         <li> The name of the template class will changed, e.g. List-<int>-
     *         to List_$int$
     *         <li> The new class will have no name parameters
     *         <li> NameNodes that refer to the type parameters will be changed
     *         to names of the types being instantatied.
     *         <li> Various fields will be updated
     *         </ul>
     *         This AST will not have any declarations, so pass 0 will have to
     *         be repeated in the copied class.
     */
    public static TreeNode instantiate(ObjectDecl templateDecl, List types,
            CompileUnitNode parentNode, boolean isImported) {
        String newName = buildNewName(templateDecl, types);

        // Copy the template definition
        // TreeNode definition = templateDecl.getSource();

        // Have to get the ASTs of the template declarations before pass 2
        TreeNode definition = null;
        CompileUnitNode ast = FileLoader.getResolvedFilePass1(templateDecl
                .getSource().getProperty(IDENT_KEY).toString());

        // Find the treeNode of the template prototype being instantiated
        if (parentNode != null) {
            Iterator iterDefTypes = ast.getDefTypes().iterator();
            while (iterDefTypes.hasNext()) {
                TreeNode defType = (TreeNode) iterDefTypes.next();

                if (!(defType instanceof UserTypeDeclNode))
                    continue;

                if (MetaModelDecl.getDecl(((UserTypeDeclNode) defType)
                        .getName()) == templateDecl) {
                    definition = defType;
                    break;
                }

                /*
                 * if (((UserTypeDeclNode)defType).getName().getIdent().equals
                 * (((UserTypeDeclNode)templateDecl.getSource()).getName()
                 * .getIdent())) { definition = defType; break; }
                 */
            }
        }

        if (definition == null)
            definition = templateDecl.getSource();

        UserTypeDeclNode instance;
        try {
            instance = (UserTypeDeclNode) definition.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone of '"
                    + (UserTypeDeclNode) definition + "' not supported.", ex);
        }

        // Change the name of the instance
        instance.getName().setIdent(newName);

        // Set the parent node of the instance
        if (parentNode != null)
            parentNode.setAsParent(instance);

        // Create a new decl for the class.
        ObjectDecl decl;
        try {
            decl = (ObjectDecl) MetaModelDecl.getDecl((TreeNode) instance)
                    .clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone of '"
                    + (MetaModelDecl) MetaModelDecl
                            .getDecl((TreeNode) instance) + "' not supported.",
                    ex);
        }

        // create a new scope
        Scope newScope = null;
        if (parentNode != null)
            newScope = new Scope((Scope) parentNode.getProperty(SCOPE_KEY));
        else
            newScope = new Scope(decl.getScope().parent());

        instance.setProperty(SCOPE_KEY, newScope);

        // Eliminate the type parameters from the instance
        instance.setParTypeNames(new LinkedList());

        // Set the flag and indicate that it's an instantiated node
        instance.getName().setProperty(TEMPLDECL_KEY, templateDecl);

        // update the fields in decl if necessary
        MetaModelDecl.setDecl((TreeNode) instance, decl);
        decl.setName(newName);
        decl.setScope(newScope);
        decl.setSource(instance);
        decl.setTypeParams(new LinkedList());
        decl.setDefType(new TypeNameNode(instance.getName()));
        decl.clearVisitors();

        if (isImported && parentNode != null)
            decl.setContainer((MetaModelDecl) parentNode
                    .getProperty(PACKAGE_KEY));

        // Call instantiation visitor to instantiate the template
        LinkedList args = new LinkedList();
        args.add(templateDecl);
        args.add(newName);
        args.add(types);
        instance.accept(new InstantiateTemplateDeclVisitor(), args);

        return instance;
    }

    // xichen_template_end

    /**
     * Build a name of a template which is instantiated with a list of types.
     * The name must depend on the original name and the list of types being
     * used.
     *
     * @param templateDecl
     *            Declaration of the template.
     * @param types
     *            List of types used to instance the template
     * @return The new name for the template.
     */
    public static String buildNewName(ObjectDecl templateDecl, List types) {
        // New name =
        // old name + "_$" + name of types used as parameters + "$"
        String suffix;
        if (types.isEmpty()) {
            suffix = "";
        } else {
            suffix = "_$";
            Iterator iter = types.iterator();
            while (iter.hasNext()) {
                TypeNode type = (TypeNode) iter.next();
                if (!suffix.equals("_$"))
                    suffix = suffix + "_";
                suffix = suffix + buildTypeName(type);
            }
            suffix = suffix + "$";
        }
        return templateDecl.getName() + suffix;
    }

    /**
     * Return a string that contains the name of a type. This string is used to
     * build the name of a instantiated template.
     *
     * @param type
     *            TypeNode whose name we want to get.
     * @return The name of this type.
     */
    public static String buildTypeName(TypeNode type) {
        switch (type.classID()) {
        case BOOLTYPENODE_ID:
            return "bool";
        case CHARTYPENODE_ID:
            return "char";
        case BYTETYPENODE_ID:
            return "byte";
        case SHORTTYPENODE_ID:
            return "short";
        case INTTYPENODE_ID:
            return "int";
        case FLOATTYPENODE_ID:
            return "float";
        case LONGTYPENODE_ID:
            return "long";
        case DOUBLETYPENODE_ID:
            return "double";
        case EVENTTYPENODE_ID:
            return "event";
        case ARRAYTYPENODE_ID:
            // Name = "array_of_" + name of type
            ArrayTypeNode array = (ArrayTypeNode) type;
            String base = "array_of_";
            String suffix = buildTypeName(array.getBaseType());
            return base + suffix;
        case TYPENAMENODE_ID:
            TypeNameNode namedType = (TypeNameNode) type;
            NameNode name = namedType.getName();
            TreeNode params = name.getParameters();
            List types;
            if (params == AbsentTreeNode.instance) {
                types = new LinkedList();
            } else {
                TemplateParametersNode pars = (TemplateParametersNode) params;
                types = pars.getTypes();
            }
            ObjectDecl decl = (ObjectDecl) MetaModelDecl.getDecl(name);
            if (decl == null)
                return buildNewName(name.getIdent().toString(), types);
            else
                return buildNewName(decl, types);
        default:
            throw new RuntimeException("Error: unknown type in the"
                    + "instantiation of a class");
        }
    }

    /**
     * Build a name of a template which is instantiated with a list of types.
     * The name must depend on the original name and the list of types being
     * used.
     *
     * @param templateName
     *            name of the template prototype.
     * @param types
     *            List of types used to instance the template
     * @return The new name for the template.
     */
    public static String buildNewName(String templateName, List types) {
        String suffix;
        if (types.isEmpty()) {
            suffix = "";
        } else {
            suffix = "_$";
            Iterator iter = types.iterator();
            while (iter.hasNext()) {
                TypeNode type = (TypeNode) iter.next();
                if (!suffix.equals("_$"))
                    suffix = suffix + "_";
                suffix = suffix + buildTypeName(type);
            }
            suffix = suffix + "$";
        }
        return templateName + suffix;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

}
