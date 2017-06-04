/* A visitor that checks the usage of the template mechanism in a meta-model
 file.

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
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TemplateChecksVisitor
/**
 * A visitor that checks that the template mechanism in the meta-model is used
 * correctly. This visitor should be called on the UserTypeDeclNode of the
 * template. It does not return any value, it only produces an error if the
 * template mechanism is not used correctly.
 * <p>
 * Situations that will be reported as errors are:
 * <ul>
 * <li> Type parameter used as interface name or superclass name in a class
 * declaration, e.g. <br>
 * <code> template(a) class C extends a </code>.
 * <li> Type parameter used as templates. A type parameter can be used to
 * replace a class (it can have fields, or methods), but it cannot have type
 * parameters e.g. <br>
 * <code> template(a) class C { a &lt; int $gt; v = 0; } </code>.
 * <li> Using a type parameter as a parameter of the template where it was
 * declared, inside an array or template instance. Some examples: <br>
 * <code> template(a) class C { C &lt; a[] $gt; v = 0; }  </code> or <br>
 * <code> template(a) class C { C &lt; D &lt; a $gt; $gt; v = 0; } </code>.
 * </ul>
 * The visit() methods of this visitor do not need any arguments, so the
 * argument list will be set to null. The result of the visit will be ignored as
 * well, except that there exists any error.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TemplateChecksVisitor.java,v 1.18 2006/10/12 20:34:19 cxh Exp $
 */
public class TemplateChecksVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new visitor that traverses a template declaration checking
     * correctness. The traversal method will be set to TM_SELF_FIRST, as many
     * nodes will be traversed.
     */
    public TemplateChecksVisitor() {
        super(TM_SELF_FIRST);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit a NameNode. Check that a type parameter is not used as a template
     * in this NameNode.
     *
     * @param node
     *            The NameNode that we have to inspect.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitNameNode(NameNode node, LinkedList args) {
        // Get the list of type parameters (if any)
        TreeNode typePars = node.getParameters();
        if (typePars == AbsentTreeNode.instance)
            return null;
        TemplateParametersNode pars = (TemplateParametersNode) typePars;
        List types = pars.getTypes();

        if (_isTypeParameter(node) && !types.isEmpty()) {
            throw new RuntimeException("Error: type parameter '"
                    + node.getIdent() + "' used as a template.");
        }

        // FIXME: add other checks? Check circularities?

        return null;
    }

    /**
     * Check type parameters used in the declaration of an interface.
     *
     * @param node
     *            The interface declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        _checkSuperInterfaces(node);
        return null;
    }

    /**
     * Check type parameters used in the declaration of a class.
     *
     * @param node
     *            The class declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        _checkSuperClass(node);
        _checkSuperInterfaces(node);
        return null;
    }

    /**
     * Check type parameters used in the declaration of a process.
     *
     * @param node
     *            The process declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        _checkSuperClass(node);
        _checkSuperInterfaces(node);
        return null;
    }

    /**
     * Check type parameters used in the declaration of a scheduler.
     *
     * @param node
     *            The scheduler declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        _checkSuperClass(node);
        _checkSuperInterfaces(node);
        return null;
    }

    /**
     * Check type parameters used in the declaration of a netlist.
     *
     * @param node
     *            The netlist declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        _checkSuperClass(node);
        _checkSuperInterfaces(node);
        return null;
    }

    /**
     * Check type parameters used in the declaration of a medium.
     *
     * @param node
     *            The medium declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        _checkSuperClass(node);
        _checkSuperInterfaces(node);
        return null;
    }

    /**
     * Check type parameters used in the declaration of a state medium.
     *
     * @param node
     *            The state medium declaration being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        _checkSuperClass(node);
        _checkSuperInterfaces(node);
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. This node does not use a template, or type
     * parameter, so we can safely skip it. However, children of this node
     * should be visited.
     *
     * @param node
     *            The node being visited.
     * @param args
     *            List of arguments (unused).
     * @return null (unused).
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }

    /**
     * Check that the superclass of this node is not a type parameter.
     *
     * @param node
     *            UserTypeDeclNode whose superclass is being tested.
     */
    protected void _checkSuperClass(ClassDeclNode node) {
        TreeNode superClass = node.getSuperClass();
        if (superClass == AbsentTreeNode.instance)
            return;
        NameNode name = ((TypeNameNode) superClass).getName();
        if (_isTypeParameter(name)) {
            MetaModelDecl decl = (MetaModelDecl) node.getName().getProperty(
                    DECL_KEY);
            String className = decl.fullName();
            throw new RuntimeException("Error in class '" + className
                    + "': type parameter used as a superclass.");
        }
    }

    /**
     * Check that the superclass of this node is not a type parameter.
     *
     * @param node
     *            UserTypeDeclNode whose list of implemented interfaces is being
     *            tested.
     */
    protected void _checkSuperInterfaces(UserTypeDeclNode node) {
        List interfaces = node.getInterfaces();
        Iterator iter = interfaces.iterator();
        while (iter.hasNext()) {
            TypeNameNode intface = (TypeNameNode) iter.next();
            if (_isTypeParameter(intface.getName())) {
                MetaModelDecl decl = (MetaModelDecl) node.getName()
                        .getProperty(DECL_KEY);
                String className = decl.fullName();
                throw new RuntimeException("Error in class '" + className
                        + "': type parameter used as a superinterface.");
            }
        }
    }

    /**
     * Return true if NameNode refers to a type parameter.
     *
     * @param name
     *            NameNode being tested.
     * @return true if NameNode refers to a type parameter.
     */
    protected boolean _isTypeParameter(NameNode name) {
        MetaModelDecl decl = (MetaModelDecl) name.getProperty(DECL_KEY);
        return (decl != null) && (decl.category == CG_TEMPLATE);
    }

}
