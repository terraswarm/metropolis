/* Generate SystemC code from a meta-model AST to a file.

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

package metropolis.metamodel.backends.elaborator;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.frontend.MetaModelDecl;
import metropolis.metamodel.frontend.TypeVisitor;
import metropolis.metamodel.frontend.TypedDecl;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ArrayAccessNode;
import metropolis.metamodel.nodetypes.BeginEventNode;
import metropolis.metamodel.nodetypes.ELOCConstraintNode;
import metropolis.metamodel.nodetypes.EndEventNode;
import metropolis.metamodel.nodetypes.EqualVarsNode;
import metropolis.metamodel.nodetypes.EventTypeNode;
import metropolis.metamodel.nodetypes.GetConnectionNumNode;
import metropolis.metamodel.nodetypes.GetNthConnectionPortNode;
import metropolis.metamodel.nodetypes.GetNthConnectionSrcNode;
import metropolis.metamodel.nodetypes.ImplyNode;
import metropolis.metamodel.nodetypes.LOCConstraintNode;
import metropolis.metamodel.nodetypes.LTLConstraintNode;
import metropolis.metamodel.nodetypes.LTLSynchNode;
import metropolis.metamodel.nodetypes.LiteralNode;
import metropolis.metamodel.nodetypes.NoneEventNode;
import metropolis.metamodel.nodetypes.ObjectNode;
import metropolis.metamodel.nodetypes.OtherEventNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.VarInEventRefNode;

import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ConstraintCodegenVisitor
/**
 * Generate elaboration code for constraints from a meta-model AST to a file.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Xi Chen
 * @version $Id: ConstraintCodegenVisitor.java,v 1.4 2005/10/24 23:12:41 allenh
 *          Exp $
 */
public class ConstraintCodegenVisitor extends JavaTranslationVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Set the traversal method to custom so that indent level can be properly
     * set.
     *
     * @param indtLevel
     *            current indent level for generated code.
     */
    public ConstraintCodegenVisitor(int indtLevel) {
        super();
        indentLevel = indtLevel;
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return the code represented by an <code>ObjectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> OBJECTNAME </code>
     */
    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        LinkedList retList = (LinkedList) node.getName().accept(this, args);

        if (!(node.getParent() instanceof ArrayAccessNode)) {
            MetaModelDecl objectDecl = MetaModelDecl.getDecl(node.getName());
            TypeNode type = null;
            if (objectDecl instanceof TypedDecl)
                type = ((TypedDecl) objectDecl).getType();

            if (type instanceof EventTypeNode) {
                codeList.addLast(indent(indentLevel));
                codeList.addLast("__tmpConstraint.addEvent(");
                codeList.addLast(retList);
                codeList.addLast(");\n");
                codeList.addLast(indent(indentLevel));
                codeList.addLast(retList);
                codeList.addLast(".addUsed(_constraint_type);\n");
            }
        }

        return retList;
    }

    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        LinkedList arrayStringList = (LinkedList) node.getArray().accept(this,
                args);

        retList.addLast(_parenExpr(node.getArray(), arrayStringList));
        retList.addLast("[");
        retList.addLast(node.getIndex().accept(this, args));
        retList.addLast("]");

        if (!(node.getParent() instanceof ArrayAccessNode)) {
            TypeNode type;
            try {
                type = (TypeNode) node.accept(new TypeVisitor(), null);
            } catch (NullPointerException e) {
                type = null;
            }

            if (type instanceof EventTypeNode) {
                codeList.addLast(indent(indentLevel));
                codeList.addLast("__tmpConstraint.addEvent(");
                codeList.addLast(retList);
                codeList.addLast(");\n");
                codeList.addLast(indent(indentLevel));
                codeList.addLast(retList);
                codeList.addLast(".addUsed(_constraint_type);\n");
            }
        }

        return retList;
    }

    /**
     * Return the code represented by a <code>BeginEventNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> beg(process, action) </code>
     */
    public Object visitBeginEventNode(BeginEventNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        TreeNode obj = node.getAction().getObject();
        TreeNode name = node.getAction().getName();

        retList.addLast("Event.newEvent(Event.BEG, ");

        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("null");
        else
            retList.addLast(node.getProcess().accept(this, args));

        retList.addLast(", ");

        if ((obj == AbsentTreeNode.instance)
                && (name == AbsentTreeNode.instance)) {
            retList.addLast("null, null");
        } else {
            retList.addLast(obj.accept(this, args));
            retList.addLast(", \"");
            retList.addLast(name.accept(this, args));
            retList.addLast("\"");
        }
        retList.addLast(")");

        codeList.addLast(indent(indentLevel));
        codeList.addLast("__tmpConstraint.addEvent(");
        codeList.addLast(retList);
        codeList.addLast(");\n");
        codeList.addLast(indent(indentLevel));
        codeList.addLast(retList);
        codeList.addLast(".addUsed(_constraint_type);\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>EndEventNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> end(process, action) </code>
     */
    public Object visitEndEventNode(EndEventNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        TreeNode obj = node.getAction().getObject();
        TreeNode name = node.getAction().getName();

        retList.addLast("Event.newEvent(Event.END, ");

        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("null");
        else
            retList.addLast(node.getProcess().accept(this, args));

        retList.addLast(", ");

        if ((obj == AbsentTreeNode.instance)
                && (name == AbsentTreeNode.instance)) {
            retList.addLast("null, null");
        } else {
            retList.addLast(obj.accept(this, args));
            retList.addLast(", \"");
            retList.addLast(name.accept(this, args));
            retList.addLast("\"");
        }
        retList.addLast(")");

        codeList.addLast(indent(indentLevel));
        codeList.addLast("__tmpConstraint.addEvent(");
        codeList.addLast(retList);
        codeList.addLast(");\n");
        codeList.addLast(indent(indentLevel));
        codeList.addLast(retList);
        codeList.addLast(".addUsed(_constraint_type);\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>NoneEventNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> none(process) </code>
     */
    public Object visitNoneEventNode(NoneEventNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("Event.newEvent(Event.NONE, ");

        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("null");
        else
            retList.addLast(node.getProcess().accept(this, args));

        retList.addLast(", null, null)");

        codeList.addLast(indent(indentLevel));
        codeList.addLast("__tmpConstraint.addEvent(");
        codeList.addLast(retList);
        codeList.addLast(");\n");
        codeList.addLast(indent(indentLevel));
        codeList.addLast(retList);
        codeList.addLast(".addUsed(_constraint_type);\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>OtherEventNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> other(process) </code>
     */
    public Object visitOtherEventNode(OtherEventNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("Event.newEvent(Event.OTHER, ");

        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("null");
        else
            retList.addLast(node.getProcess().accept(this, args));

        retList.addLast(", null, null)");

        codeList.addLast(indent(indentLevel));
        codeList.addLast("__tmpConstraint.addEvent(");
        codeList.addLast(retList);
        codeList.addLast(");\n");
        codeList.addLast(indent(indentLevel));
        codeList.addLast(retList);
        codeList.addLast(".addUsed(_constraint_type);\n");

        return retList;
    }

    /**
     * Do not generate code represented by an <code>LTLConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>ltl(FORMULA);</code>
     */
    public Object visitLTLConstraintNode(LTLConstraintNode node, LinkedList args) {
        node.getFormula().accept(this, args);
        return codeList;
    }

    /**
     * Returns the code represented by a <code>LOCConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> loc (formula); </code>
     *                         <code> loc BuiltInLOCFormula; </code>
     */
    public Object visitLOCConstraintNode(LOCConstraintNode node, LinkedList args) {
        node.getFormula().accept(this, args);
        return codeList;
    }

    /**
     * Returns the code represented by a <code>ELOCConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> eloc (formula); </code>
     */
    public Object visitELOCConstraintNode(ELOCConstraintNode node,
            LinkedList args) {
        node.getFormula().accept(this, args);
        return codeList;
    }

    /**
     * Return the code represented by a <code>LTLSynchNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> ltl synch(events, equalVars) </code>
     */
    public Object visitLTLSynchNode(LTLSynchNode node, LinkedList args) {
        // LinkedList tmpList = new LinkedList();

        if (node.getEvents().size() == 1
                && node.getEvents().get(0) instanceof ImplyNode) {
            ((ImplyNode) node.getEvents().get(0)).getEvent().accept(this, args);
            codeList.addLast(indent(indentLevel));
            codeList
                    .addLast("((LTLSynchImply)__tmpConstraint).eventsToLeftEvents();\n");
            ((ImplyNode) node.getEvents().get(0)).getExpr().accept(this, args);
            codeList.addLast(indent(indentLevel));
            codeList
                    .addLast("((LTLSynchImply)__tmpConstraint).eventsToRightEvents();\n");

            if (node.getEqualVars().size() > 0) {
                _commaList(TNLManip.traverseList(this, args, node
                        .getEqualVars()));
            }

            Iterator iterVars = node.getEqualVars().iterator();
            while (iterVars.hasNext()) {

                EqualVarsNode equalvars = (EqualVarsNode) iterVars.next();
                if (equalvars.getVar1() instanceof VarInEventRefNode
                        && equalvars.getVar2() instanceof VarInEventRefNode) {

                    VarInEventRefNode var1 = (VarInEventRefNode) equalvars
                            .getVar1();
                    VarInEventRefNode var2 = (VarInEventRefNode) equalvars
                            .getVar2();

                    if (!(_stringListToString(
                            (LinkedList) var1.getIndex().accept(
                                    new ConstraintCodegenVisitor(0), args))
                            .equals("i") && _stringListToString(
                            (LinkedList) var2.getIndex().accept(
                                    new ConstraintCodegenVisitor(0), args))
                            .equals("i")))
                        throw new RuntimeException(
                                "Error on ltl synch constraint: equality must "
                                        + "in the form of var1@(event1, i) == var2@(event2, i) "
                                        + "or var1@(event1, i) == constant or constant == var2@(event2, i)");

                    codeList.addLast(indent(indentLevel));
                    codeList
                            .addLast("((LTLSynchImply)__tmpConstraint).addEqualVars(new EqualVars(\"");
                    if (var1.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var1.getVar())
                                .getLiteral());
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var1.getVar())
                                + ", \"");
                    } else {
                        codeList.addLast(var1.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE, \"");
                    }

                    if (var2.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var2.getVar())
                                .getLiteral());
                        codeList.addLast("\",");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var2.getVar())
                                + "));\n");
                    } else {
                        codeList.addLast(var2.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\",");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE));\n");
                    }
                } else if (equalvars.getVar1() instanceof VarInEventRefNode
                        && equalvars.getVar2() instanceof LiteralNode) {

                    VarInEventRefNode var1 = (VarInEventRefNode) equalvars
                            .getVar1();
                    String const2 = ((LiteralNode) equalvars.getVar2())
                            .getLiteral();
                    String type2 = _literalType((LiteralNode) equalvars
                            .getVar2());

                    if (!(_stringListToString((LinkedList) var1.getIndex()
                            .accept(new ConstraintCodegenVisitor(0), args))
                            .equals("i")))
                        throw new RuntimeException(
                                "Error on ltl synch constraint: equality must "
                                        + "in the form of var1@(event1, i) == var2@(event2, i) "
                                        + "or var1@(event1, i) == constant or constant == var2@(event2, i)");

                    codeList.addLast(indent(indentLevel));
                    codeList
                            .addLast("((LTLSynchImply)__tmpConstraint).addEqualVars(new EqualVars(\"");
                    if (var1.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var1.getVar())
                                .getLiteral());
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var1.getVar())
                                + ", \"");
                    } else {
                        codeList.addLast(var1.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE, \"");
                    }

                    codeList.addLast(const2);
                    codeList.addLast("\", null,");
                    codeList.addLast(type2);
                    codeList.addLast("));\n");
                } else if (equalvars.getVar1() instanceof LiteralNode
                        && equalvars.getVar2() instanceof VarInEventRefNode) {
                    VarInEventRefNode var2 = (VarInEventRefNode) equalvars
                            .getVar2();
                    String const1 = ((LiteralNode) equalvars.getVar1())
                            .getLiteral();
                    String type1 = _literalType((LiteralNode) equalvars
                            .getVar1());

                    if (!(_stringListToString((LinkedList) var2.getIndex()
                            .accept(new ConstraintCodegenVisitor(0), args))
                            .equals("i")))
                        throw new RuntimeException(
                                "Error on ltl synch constraint: equality must "
                                        + "in the form of var1@(event1, i) == var2@(event2, i) "
                                        + "or var1@(event1, i) == constant or constant == var2@(event2, i)");

                    codeList.addLast(indent(indentLevel));
                    codeList
                            .addLast("((LTLSynchImply)__tmpConstraint).addEqualVars(new EqualVars(\"");
                    codeList.addLast(const1);
                    codeList.addLast("\", null, ");
                    codeList.addLast(type1);
                    codeList.addLast(", \"");
                    if (var2.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var2.getVar())
                                .getLiteral());
                        codeList.addLast("\", ");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var2.getVar())
                                + "));\n");
                    } else {
                        codeList.addLast(var2.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\", ");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE));\n");
                    }
                } else
                    throw new RuntimeException(
                            "Error on ltl synch constraint: equality must "
                                    + "in the form of var1@(event1, i) == var2@(event2, i) "
                                    + "or var1@(event1, i) == constant or constant == var2@(event2, i)");
            }
            codeList.addLast(indent(indentLevel));
            codeList
                    .addLast("((LTLSynchImply)__tmpConstraint).checkEvents();\n\n");
        } else {
            _commaList(TNLManip.traverseList(this, args, node.getEvents()));
            if (node.getEqualVars().size() > 0) {
                _commaList(TNLManip.traverseList(this, args, node
                        .getEqualVars()));
            }

            if (node.getEqualVars().size() > 0) {

                EqualVarsNode equalvars = (EqualVarsNode) node.getEqualVars()
                        .get(0);
                if (equalvars.getVar1() instanceof VarInEventRefNode
                        && equalvars.getVar2() instanceof VarInEventRefNode) {

                    VarInEventRefNode var1 = (VarInEventRefNode) equalvars
                            .getVar1();
                    VarInEventRefNode var2 = (VarInEventRefNode) equalvars
                            .getVar2();

                    if (!(_stringListToString(
                            (LinkedList) var1.getIndex().accept(
                                    new ConstraintCodegenVisitor(0), args))
                            .equals("i") && _stringListToString(
                            (LinkedList) var2.getIndex().accept(
                                    new ConstraintCodegenVisitor(0), args))
                            .equals("i")))
                        throw new RuntimeException(
                                "Error on ltl synch constraint: equality must "
                                        + "in the form of var1@(event1, i) == var2@(event2, i) "
                                        + "or var1@(event1, i) == constant or constant == var2@(event2, i)");

                    codeList.addLast(indent(indentLevel));
                    codeList
                            .addLast("((LTLSynch)__tmpConstraint).setEqualVars(new EqualVars(\"");
                    if (var1.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var1.getVar())
                                .getLiteral());
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var1.getVar())
                                + ", \"");
                    } else {
                        codeList.addLast(var1.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE, \"");
                    }

                    if (var2.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var2.getVar())
                                .getLiteral());
                        codeList.addLast("\",");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var2.getVar())
                                + "));\n");
                    } else {
                        codeList.addLast(var2.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\",");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE));\n");
                    }
                } else if (equalvars.getVar1() instanceof VarInEventRefNode
                        && equalvars.getVar2() instanceof LiteralNode) {

                    VarInEventRefNode var1 = (VarInEventRefNode) equalvars
                            .getVar1();
                    String const2 = ((LiteralNode) equalvars.getVar2())
                            .getLiteral();
                    String type2 = _literalType((LiteralNode) equalvars
                            .getVar2());

                    if (!(_stringListToString((LinkedList) var1.getIndex()
                            .accept(new ConstraintCodegenVisitor(0), args))
                            .equals("i")))
                        throw new RuntimeException(
                                "Error on ltl synch constraint: equality must "
                                        + "in the form of var1@(event1, i) == var2@(event2, i) "
                                        + "or var1@(event1, i) == constant or constant == var2@(event2, i)");
                    codeList.addLast(indent(indentLevel));
                    codeList
                            .addLast("((LTLSynch)__tmpConstraint).setEqualVars(new EqualVars(\"");
                    if (var1.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var1.getVar())
                                .getLiteral());
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var1.getVar())
                                + ", \"");
                    } else {
                        codeList.addLast(var1.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\", ");
                        codeList.addLast(var1.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE, \"");
                    }
                    codeList.addLast(const2);
                    codeList.addLast("\", null, ");
                    codeList.addLast(type2);
                    codeList.addLast("));\n");
                } else if (equalvars.getVar1() instanceof LiteralNode
                        && equalvars.getVar2() instanceof VarInEventRefNode) {
                    VarInEventRefNode var2 = (VarInEventRefNode) equalvars
                            .getVar2();
                    String const1 = ((LiteralNode) equalvars.getVar1())
                            .getLiteral();
                    String type1 = _literalType((LiteralNode) equalvars
                            .getVar1());

                    if (!(_stringListToString((LinkedList) var2.getIndex()
                            .accept(new ConstraintCodegenVisitor(0), args))
                            .equals("i")))
                        throw new RuntimeException(
                                "Error on ltl synch constraint: equality must "
                                        + "in the form of var1@(event1, i) == var2@(event2, i) "
                                        + "or var1@(event1, i) == constant or constant == var2@(event2, i)");
                    codeList.addLast(indent(indentLevel));
                    codeList
                            .addLast("((LTLSynch)__tmpConstraint).setEqualVars(new EqualVars(\"");
                    codeList.addLast(const1);
                    codeList.addLast("\", null, ");
                    codeList.addLast(type1);
                    codeList.addLast(", \"");
                    if (var2.getVar() instanceof LiteralNode) {
                        codeList.addLast(((LiteralNode) var2.getVar())
                                .getLiteral());
                        codeList.addLast("\", ");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", "
                                + _literalType((LiteralNode) var2.getVar())
                                + "));\n");
                    } else {
                        codeList.addLast(var2.getVar().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast("\", ");
                        codeList.addLast(var2.getEvent().accept(
                                new ConstraintCodegenVisitor(0), args));
                        codeList.addLast(", EqualVars.VARTYPE));\n");
                    }
                } else
                    throw new RuntimeException(
                            "Error on ltl synch constraint: equality must "
                                    + "in the form of var1@(event1, i) == var2@(event2, i) "
                                    + "or var1@(event1, i) == constant or constant == var2@(event2, i)");
            }
            codeList.addLast(indent(indentLevel));
            codeList.addLast("((LTLSynch)__tmpConstraint).checkEvents();\n\n");
        }

        return codeList;

    }

    /**
     * Return the code represented by an <code>GetConnectionNumNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getconnectionnum(MEDIUM, IFNAME) </code>
     */
    public Object visitGetConnectionNumNode(GetConnectionNumNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("Network.net.getConnectionNum(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", \"");
        MetaModelDecl decl = MetaModelDecl.getDecl(node.getIfName());
        if (decl == null)
            retList.addLast(node.getIfName().accept(this, args));
        else
            retList.addLast(decl.fullName());
        retList.addLast("\")");

        codeList.addLast(indent(indentLevel));
        codeList
                .addLast("__tmpConstraint.addStructureValue(\"getconnectionnum(");
        codeList.addLast(node.getMedium().accept(this, args));
        codeList.addLast(",");
        codeList.addLast(node.getIfName().accept(this, args));
        codeList.addLast(")\", new Integer(");
        codeList.addLast(retList);
        codeList.addLast("));\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>GetNthConnectionSrcNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getnthconnectionsrc(MEDIUM, IFNAME, NUM) </code>
     */
    public Object visitGetNthConnectionSrcNode(GetNthConnectionSrcNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("Network.net.getNthConnectionSrc(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", \"");
        MetaModelDecl decl = MetaModelDecl.getDecl(node.getIfName());
        if (decl == null)
            retList.addLast(node.getIfName().accept(this, args));
        else
            retList.addLast(decl.fullName());
        retList.addLast("\", ");
        retList.addLast(node.getNum().accept(this, args));
        retList.addLast(")");

        codeList.addLast(indent(indentLevel));
        codeList
                .addLast("__tmpConstraint.addStructureValue(\"getnthconnectionsrc(");
        codeList.addLast(node.getMedium().accept(this, args));
        codeList.addLast(",");
        codeList.addLast(node.getIfName().accept(this, args));
        codeList.addLast(",");
        codeList.addLast(node.getNum().accept(this, args));
        codeList.addLast(")\", Network.net.getNode(");
        codeList.addLast(retList);
        codeList.addLast("));\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>GetNthConnectionPortNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getnthconnectionport(MEDIUM, IFNAME, NUM) </code>
     */
    public Object visitGetNthConnectionPortNode(GetNthConnectionPortNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("Network.net.getNthConnectionPort(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", \"");
        MetaModelDecl decl = MetaModelDecl.getDecl(node.getIfName());
        if (decl == null)
            retList.addLast(node.getIfName().accept(this, args));
        else
            retList.addLast(decl.fullName());
        retList.addLast("\", ");
        retList.addLast(node.getNum().accept(this, args));
        retList.addLast(")");

        codeList.addLast(indent(indentLevel));
        codeList
                .addLast("__tmpConstraint.addStructureValue(\"getnthconnectionport(");
        codeList.addLast(node.getMedium().accept(this, args));
        codeList.addLast(",");
        codeList.addLast(node.getIfName().accept(this, args));
        codeList.addLast(",");
        codeList.addLast(node.getNum().accept(this, args));
        codeList.addLast(")\", (IPort)");
        codeList.addLast(retList);
        codeList.addLast(");\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>VarInEventRefNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>var@(event, index);</code>
     */

    public Object visitVarInEventRefNode(VarInEventRefNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        LinkedList varList, eventList;

        retList
                .addLast(varList = (LinkedList) node.getVar()
                        .accept(this, args));
        retList.addLast("@(");
        retList.addLast(eventList = (LinkedList) node.getEvent().accept(this,
                args));
        retList.addLast(", ");
        retList.addLast(node.getIndex().accept(this, args));
        retList.addLast(")");

        if (!(node.getVar() instanceof LiteralNode)) {
            codeList.addLast(indent(indentLevel));
            codeList.addLast("Network.net.addAnnotation(");
            codeList.addLast(eventList);
            codeList.addLast(", \"");
            codeList.addLast(varList);
            codeList.addLast("\");\n");
        }

        return retList;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    /** a LinkedList representing the return string */
    private LinkedList codeList = new LinkedList();

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

}
