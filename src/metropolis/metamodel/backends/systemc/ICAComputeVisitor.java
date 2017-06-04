/*

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

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.NullValue;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.elaborator.JavaTranslationVisitor;
import metropolis.metamodel.frontend.ClassDecl;
import metropolis.metamodel.frontend.InterfaceDecl;
import metropolis.metamodel.frontend.MetaModelDecl;
import metropolis.metamodel.frontend.MethodDecl;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.ProcessDecl;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ActionLabelStmtNode;
import metropolis.metamodel.nodetypes.AnnotationNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.AwaitStatementNode;
import metropolis.metamodel.nodetypes.BoolLitNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.FieldAccessNode;
import metropolis.metamodel.nodetypes.LabeledBlockNode;
import metropolis.metamodel.nodetypes.LabeledStmtNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodCallNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.ObjectFieldAccessNode;
import metropolis.metamodel.nodetypes.PortAccessNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.SuperFieldAccessNode;
import metropolis.metamodel.nodetypes.ThisFieldAccessNode;
import metropolis.metamodel.nodetypes.TypeFieldAccessNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.MMType;
import metropolis.metamodel.runtime.Network;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// ////////////////////////////////////////////////////////////////////////
// // ICAComputeVisitor
/**
 * Collect interleaving concurrent atomicity for a process or medium except for
 * the test lists in await statements.
 *
 * @author Guang Yang
 * @version $Id: ICAComputeVisitor.java,v 1.22 2006/10/12 20:33:03 cxh Exp $
 */
public class ICAComputeVisitor extends TraverseStmtsVisitor {

    /**
     * Constructor.
     *
     * @param inode
     *            The corresponding INode for the process or medium being
     *            processed
     */
    public ICAComputeVisitor(INode inode) {
        _inode = inode;
        _implementedIntfcs = new Hashtable();
        _top = true;
    }

    /**
     * visit an await statement.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        List retList = TNLManip.traverseList(this, args, node.getGuards());
        ICASymbolicFlag f = (ICASymbolicFlag) node.getProperty(ICATOMIC_KEY);
        if (f == null)
            f = new ICASymbolicFlag();
        f.addDependingFlag(reduceListToSet(retList));
        node.setProperty(ICATOMIC_KEY, f);

        return f;
    }

    /**
     * one branch of await statement including a guard condition, a test list, a
     * set list and a critical section.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode cond = node.getCond();
        ICASymbolicFlag c = (ICASymbolicFlag) cond.getProperty(ICATOMIC_KEY);
        if (c == null)
            c = new ICASymbolicFlag();
        if (cond instanceof BoolLitNode) {
            if (((BoolLitNode) cond).getLiteral().equals("true"))
                c.setICA(true);
            else
                c.setICA(false);
        } else
            c.setICA(false);
        cond.setProperty(ICATOMIC_KEY, c);

        retList.addLast(c);

        ICASymbolicFlag f = visitEventRelatedNode((TreeNode) node.getStmt(),
                node.getStmt().accept(this, args));
        retList.addLast(f);

        return retList;
    }

    /**
     * A method declaration.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        visitEventRelatedNode(node, node.getBody().accept(this, args));
        MethodDecl m = (MethodDecl) node.getName().getProperty(DECL_KEY);
        if (m.getOverridedBy().size() > 0)
            return null;
        if (m.getImplements().size() == 0)
            return null;
        else {
            Iterator iter = m.getImplements().iterator();
            while (iter.hasNext()) {
                // the interface function declaration that this method
                // implements
                MethodDecl method = (MethodDecl) iter.next();

                Enumeration intfcs = _implementedIntfcs.keys();
                while (intfcs.hasMoreElements()) {
                    // one interface implemented by the medium
                    InterfaceDecl intfc = (InterfaceDecl) intfcs.nextElement();

                    Iterator iterMethod = intfc.getScope().getDecls()
                            .iterator();
                    while (iterMethod.hasNext()) {
                        // one interface function declaration defined in the
                        // interface intfc
                        if (method == iterMethod.next()) {
                            // method is one of the interface functions defined
                            // in intfc
                            // therefore, method contribute to IC atomicity to
                            // the entire interface intfc
                            ((HashSet) _implementedIntfcs.get(intfc)).add(node
                                    .getProperty(ICATOMIC_KEY));
                            break;
                        }
                    } // end while iterMethod
                } // end while intfcs
            } // end iter
        }
        return null;
    }

    /**
     * A method call.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(node.getMethod().accept(this, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getArgs()));

        return retList;
    }

    /**
     * An object field access.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();
        TreeNode object = node.getObject();
        NameNode name = node.getName();
        MetaModelDecl decl = (MetaModelDecl) name.getProperty(DECL_KEY);
        if (decl == null) // FIXME: This should not happen ideally. It happens
            // because
            return new ICASymbolicFlag(false); // template prototypes are not
        // correctly resolved.
        TreeNode ast = decl.getSource();

        if (object instanceof PortAccessNode) {
            String portName = SystemCCodegenVisitor
                    ._stringListToString((List) object.accept(
                            new JavaTranslationVisitor(), args));
            Object desObj = Network.net.getConnectionDest(_inode, portName);
            Class desClass = desObj.getClass();
            String objName = desClass.getName();
            String superName = objName;
            while (!superName.startsWith("metamodel.lang.")) {
                desClass = desClass.getSuperclass();
                superName = desClass.getName();
            }
            int objType;
            if (superName.equals("metamodel.lang.Process"))
                objType = MMType.PROCESS;
            else if (superName.equals("metamodel.lang.Medium"))
                objType = MMType.MEDIUM;
            else
                return retList;
            // throw new RuntimeException("Only process and medium are
            // expected.");

            // INode desNode = Network.net.getNode(desObj);
            UserTypeDeclNode desAST = SystemCBackend._findObjectAST(objName,
                    objType);
            ObjectDecl desDecl = (ObjectDecl) desAST.getName().getProperty(
                    DECL_KEY);
            List decls = desDecl.getScope().getDecls();
            Iterator iter = decls.iterator();
            while (iter.hasNext()) {
                MetaModelDecl m = (MetaModelDecl) iter.next();
                if (m instanceof MethodDecl) {
                    Iterator impl = ((MethodDecl) m).getImplements().iterator();
                    while (impl.hasNext()) {
                        if (impl.next() == decl) {
                            ICASymbolicFlag f = (ICASymbolicFlag) m.getSource()
                                    .getProperty(ICATOMIC_KEY);
                            if (f == null) {
                                f = new ICASymbolicFlag();
                                m.getSource().setProperty(ICATOMIC_KEY, f);
                            }
                            return f;
                        } // end if
                    } // end while impl
                } // end if
            } // end while iter
        } else {
            retList.addLast(object.accept(this, args));
            if (decl instanceof MethodDecl) {
                ICASymbolicFlag f = (ICASymbolicFlag) ast
                        .getProperty(ICATOMIC_KEY);
                if (f == null) {
                    f = new ICASymbolicFlag();
                    ast.setProperty(ICATOMIC_KEY, f);
                }
                retList.addLast(f);
            } else {
                retList.addLast(name.accept(this, args));
            }
        } // end if
        return retList;
    }

    /**
     * A This field access node.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        return visitFieldAccessNode(node, args);
    }

    /**
     * SuperFieldAccessNode represents the access of a field or method defined
     * in super classes.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        return visitFieldAccessNode(node, args);
    }

    /**
     * SuperFieldAccessNode represents the access of a field or method defined
     * in super classes.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        return visitFieldAccessNode(node, args);
    }

    /**
     * A process declaration.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return null
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {

        super.visitProcessDeclNode(node, args);

        ProcessDecl m = (ProcessDecl) node.getName().getProperty(DECL_KEY);
        m = (ProcessDecl) m.getSuperClass();
        if (m != null) {
            if (!m.fullName().startsWith("metamodel.lang"))
                m.getSource().accept(this, args);
        }

        return null;
    }

    /**
     * A class declaration.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return null
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {

        super.visitClassDeclNode(node, args);

        ClassDecl m = (ClassDecl) node.getName().getProperty(DECL_KEY);
        m = (ClassDecl) m.getSuperClass();
        if (m != null) {
            if (!m.fullName().startsWith("metamodel.lang"))
                m.getSource().accept(this, args);
        }

        return null;
    }

    /**
     * A medium declaration.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return null
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        boolean thisTop = _top;

        ObjectDecl m = (ObjectDecl) node.getName().getProperty(DECL_KEY);
        if (_top) {
            _top = false;
            while (!m.fullName().startsWith("metamodel.lang")) {
                addImplementedInterfaces(m.getInterfaces());
                m = (ObjectDecl) m.getSuperClass();
            } // end while
        } // end if _top

        super.visitMediumDeclNode(node, args);

        m = (ObjectDecl) m.getSuperClass();
        if (m != null) {
            if (!m.fullName().startsWith("metamodel.lang"))
                m.getSource().accept(this, args);
        }

        if (thisTop) {
            Hashtable ica = (Hashtable) node.getName()
                    .getProperty(ICATOMIC_KEY);

            if (ica == null)
                ica = new Hashtable();
            Enumeration iter = _implementedIntfcs.keys();
            while (iter.hasMoreElements()) {
                InterfaceDecl intfc = (InterfaceDecl) iter.nextElement();
                Set depends = (Set) ica.get(intfc);
                if (depends == null)
                    depends = new HashSet();
                String intfcName = intfc.getName();
                ICASymbolicFlag f = (ICASymbolicFlag) ica.get(intfcName);
                if (f == null)
                    f = new ICASymbolicFlag();
                f.addDependingFlag(depends);
                ica.put(intfcName, f);
            } // end while

            node.getName().setProperty(ICATOMIC_KEY, ica);
        } // end if (thisTop)

        return null;
    }

    // Note that &#64; is an html code for @. We need to hide the @
    // from javadoc.

    /**
     * label {&#64; statement; &#64;}.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        return visitEventRelatedNode(node, TNLManip.traverseList(this, args,
                node.getStmts()));
    }

    /**
     * block(label){ statements }.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        return visitEventRelatedNode(node, TNLManip.traverseList(this, args,
                node.getStmts()));
    }

    /**
     * label: statement;.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        return visitEventRelatedNode(node, node.getStmt().accept(this, args));
    }

    /**
     * Quantity annotation {$ statements $};.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    public Object visitAnnotationNode(AnnotationNode node, LinkedList args) {
        return new ICASymbolicFlag(false);
    }

    /**
     * Decide whether an event related statement is involved in constraints
     * including LTL, LOC and LTL synch.
     *
     * @param node
     *            The label related AST node
     * @param depends
     *            Depending actions. It could be one or a list of
     *            ICASymbolicFlag instances.
     * @return The complete dependence relation
     */
    protected ICASymbolicFlag visitEventRelatedNode(TreeNode node,
            Object depends) {
        boolean ica = true;
        ica &= (node.getProperty(LTLSYNCHEVENT_KEY) == null);
        ica &= (node.getProperty(LTLSYNCHIMPLYEVENT_KEY) == null);
        ica &= (node.getProperty(LTLEVENT_KEY) == null);
        ica &= (node.getProperty(LOCEVENT_KEY) == null);

        ICASymbolicFlag h;

        if (ica) {
            h = (ICASymbolicFlag) node.getProperty(ICATOMIC_KEY);
            if (h == null)
                h = new ICASymbolicFlag();
            if (depends instanceof List)
                h.addDependingFlag(reduceListToSet((List) depends));
            else if (depends instanceof ICASymbolicFlag)
                h.addDependingFlag((ICASymbolicFlag) depends);
            else
                throw new RuntimeException(
                        "Unexpected argument type for 'depends' "
                                + "in ICAComputeVisitor.visitEventRelatedNode");
        } else {
            h = new ICASymbolicFlag(false);
        }
        node.setProperty(ICATOMIC_KEY, h);

        return h;
    }

    /**
     * FieldAccessNode represents a field or method access. It includes four
     * more specific access types: ObjectFieldAccessNode, ThisFieldAccessNode,
     * SuperFieldAccessNode, TypeFieldAccessNode.
     *
     * @param node
     *            the node being visited
     * @param args
     *            visitor arguments
     * @return the a flag indicating the IC atomicity
     */
    protected Object visitFieldAccessNode(FieldAccessNode node, LinkedList args) {
        MetaModelDecl mDecl = (MetaModelDecl) node.getName().getProperty(
                DECL_KEY);
        if (mDecl instanceof MethodDecl) {
            TreeNode mAst = mDecl.getSource();
            ICASymbolicFlag f = (ICASymbolicFlag) mAst
                    .getProperty(ICATOMIC_KEY);
            if (f == null) {
                f = new ICASymbolicFlag();
                mAst.setProperty(ICATOMIC_KEY, f);
            }
            return f;
        } else {
            return _defaultVisit(node, args);
        }
    }

    /**
     * Reduce a List to a Set The List could include ICASymbolicFlag, Set, List
     * and NullValue.instance.
     *
     * @param list
     *            The input List
     * @return The reduced Set which includes only ICASymbolicFlag If there is
     *         any ICASymbolicFlag evaluted to false already, the reduced Set
     *         includes only a false ICASymbolicFlag.
     */
    protected Set reduceListToSet(List list) {
        Set ret = _reduceListToSet(list);

        Iterator iter = ret.iterator();
        while (iter.hasNext()) {
            ICASymbolicFlag flag = (ICASymbolicFlag) iter.next();
            if (flag.isEvaluated())
                if (!flag.isICA()) {
                    ret.clear();
                    ret.add(flag);
                    return ret;
                }
        }

        return ret;
    }

    /**
     * Reduce a List to a Set The List could include ICASymbolicFlag, Set, List
     * and NullValue.instance.
     *
     * @param list
     *            The input List
     * @return The reduced Set which includes only ICASymbolicFlag
     */
    protected Set _reduceListToSet(List list) {
        Set ret = new HashSet();

        if (list == null)
            return ret;

        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj == null)
                continue;
            else if (obj instanceof NullValue)
                continue;
            else if (obj instanceof ICASymbolicFlag)
                ret.add(obj);
            else if (obj instanceof Set)
                ret.addAll((Collection) obj);
            else if (obj instanceof List)
                ret.addAll(_reduceListToSet((List) obj));
            else
                throw new RuntimeException(
                        "Unexpected element type in reducing a List to a Set");
        }

        return ret;
    }

    /**
     * Add recursively to _implementedIntfcs all interfaces and their parent
     * interfaces that the medium implements.
     *
     * @param intfcs
     *            A list of interface declarations.
     */
    protected void addImplementedInterfaces(List intfcs) {
        if (intfcs == null)
            return;

        Iterator iter = intfcs.iterator();
        while (iter.hasNext()) {
            InterfaceDecl intfc = (InterfaceDecl) iter.next();
            if (intfc.fullName().startsWith("metamodel.lang"))
                return;
            _implementedIntfcs.put(intfc, new HashSet());
            addImplementedInterfaces(intfc.getInterfaces());
        } // end while
    }

    /**
     * The default visit method.
     *
     * @param node
     *            The TreeNode base type node.
     * @param args
     *            Arguments to pass in.
     * @return an Object, which is usually a list of ICASymbolicFlags from
     *         children nodes.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        List children = node.children();
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            Object child = iter.next();
            if (child == null || child instanceof AbsentTreeNode)
                continue;
            else if (child instanceof List)
                retList
                        .addLast(TNLManip
                                .traverseList(this, args, (List) child));
            else if (child instanceof TreeNode)
                retList.addLast(((TreeNode) child).accept(this, args));
            else
                throw new RuntimeException("Unexpected child " + child
                        + " of node" + node);
        }

        return retList;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * The corresponding INode for the process or medium being processed
     */
    private INode _inode;

    /**
     * Record of the interfaces implemented by a medium and the condition under
     * which the interfaces are interleaving concurrent atomic key: interface
     * declaration value: a set of depending interface functions
     */
    private Hashtable _implementedIntfcs;

    /**
     * Record of the interfaces implemented by a medium and the condition under
     * which the interfaces are interleaving concurrent atomic keys: interface
     * names values: depending interface functions
     */
    private boolean _top;

}
