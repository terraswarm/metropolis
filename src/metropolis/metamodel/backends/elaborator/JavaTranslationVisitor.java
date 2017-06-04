/* Generate netlist elaboration code from a meta-model AST to a file.

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

import metropolis.metamodel.Effect;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.PropertyMap;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.metamodel.MetaModelCodegenVisitor;
import metropolis.metamodel.backends.systemc.LTLCodegenVisitor;
import metropolis.metamodel.frontend.MetaModelDecl;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.nodetypes.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // JavaTranslationVisitor
/**
 * Generate netlist elaboration code from a meta-model AST to a file.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Guang Yang
 * @version $Id: JavaTranslationVisitor.java,v 1.96 2005/10/24 23:12:41 allenh
 *          Exp $
 */
public class JavaTranslationVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Set the traversal method to custom so that indent level can be properly
     * set.
     */
    public JavaTranslationVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return the code represented by an <code>ActionConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>lfo(FORMULA);</code>
     */
    /*
     * public Object visitActionConstraintNode (ActionConstraintNode node,
     * LinkedList args) { LinkedList retList = new LinkedList();
     *
     * retList.addLast(indent(indentLevel)); retList.addLast("lfo(");
     * retList.addLast(node.getFormula().accept(this, args));
     * retList.addLast(");\n");
     *
     * return retList; }
     */

    // /////////Constraints
    /**
     * Return the code represented by an <code>LTLConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>ltl(FORMULA);</code>
     */

    public Object visitLTLConstraintNode(LTLConstraintNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        Object source = node.getFormula().accept(new MetaModelCodegenVisitor(),
                args);

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("//ltl ");
        if (node.getFormula() instanceof BuiltInLTLNode)
            retList.addLast(source);
        else {
            retList.addLast("(");
            retList.addLast(source);
            retList.addLast(")");
        }
        retList.addLast(";\n");

        /*
         * args.addLast(new LinkedList());
         *
         * retList.addLast("\n"); retList.addLast(indent(indentLevel));
         * retList.addLast("//ltl "); if (node.getFormula() instanceof
         * BuiltInLTLNode) retList.addLast(node.getFormula().accept(new
         * MetaModelCodegenVisitor(), args)); else { retList.addLast("(");
         * retList.addLast(node.getFormula().accept(new
         * MetaModelCodegenVisitor(), args)); retList.addLast(")"); }
         * retList.addLast(";\n\n");
         */

        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint = new Constraint(Constraint.LTL);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.LTL;\n");
        retList.addLast(indent(indentLevel));
        retList
                .addLast("Network.net.getNode(this).addConstraint(__tmpConstraint);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.addConstraint(__tmpConstraint);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.setSource(new String(\"");
        retList.addLast("ltl ");
        if (node.getFormula() instanceof BuiltInLTLNode)
            retList.addLast(source);
        else {
            retList.addLast("(");
            retList.addLast(source);
            retList.addLast(")");
        }
        retList.addLast(";");
        retList.addLast("\"));\n");

        /*
         * isInConstraint = true && isInConstraintBlock; LinkedList tmpList =
         * (LinkedList)node.getFormula().accept(this, args); isInConstraint =
         * false;
         *
         * String tmpString = _stringListToString(tmpList); if
         * (!(node.getFormula() instanceof BuiltInLTLNode)) tmpString = "(" +
         * tmpString + ")"; tmpString = tmpString.replace('"', '\'');
         * retList.addLast(tmpString); retList.addLast(";");
         * retList.addLast("\"));\n");
         *
         * retList.addLast(args.removeLast());
         */

        retList.addLast(node.accept(new ConstraintCodegenVisitor(indentLevel),
                args));
        retList.addLast(indent(indentLevel));
        retList.addLast("_ltl_formula = \"\";\n");

        retList.addLast(node.accept(new LTLCodegenVisitor(indentLevel), args));
        retList.addLast(indent(indentLevel));
        retList
                .addLast("__tmpConstraint.addFormula(Constraint.SYSTEMC_FORMULA, _ltl_formula);\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>LOCConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> loc (formula); </code>
     *                         <code> loc BuiltInLOCFormula; </code>
     */
    public Object visitLOCConstraintNode(LOCConstraintNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        Object source = node.getFormula().accept(new MetaModelCodegenVisitor(),
                args);

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("//loc ");

        if (node.getFormula() instanceof BuiltInActionNode) {
            retList.addLast(source);
            retList.addLast(";\n");

            /*
             * retList.addLast(indent(indentLevel));
             * retList.addLast("__tmpConstraint = new
             * Constraint(Constraint.LOC);\n");
             * retList.addLast(indent(indentLevel));
             * retList.addLast("_constraint_type = Constraint.LOC;\n");
             */

            retList.addLast(node.getFormula().accept(this, args));
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("Network.net.getNode(this).addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("Network.net.addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("__tmpConstraint.setSource(new String(\"");
            retList.addLast("loc ");
            retList.addLast(source);
            retList.addLast(";\"));\n");
        } else {
            retList.addLast("(");
            retList.addLast(source);
            retList.addLast(")");
            retList.addLast(";\n");

            retList.addLast(indent(indentLevel));
            retList
                    .addLast("__tmpConstraint = new Constraint(Constraint.LOC);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("_constraint_type = Constraint.LOC;\n");
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("Network.net.getNode(this).addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("Network.net.addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("__tmpConstraint.setSource(new String(\"");
            retList.addLast("loc ");
            retList.addLast("(");
            retList.addLast(source);
            retList.addLast(");\"));\n");
            retList.addLast(node.accept(new ConstraintCodegenVisitor(
                    indentLevel), args));

            args.addLast(new String("loc_checker"));
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("__tmpConstraint.addFormula(Constraint.TRACE_FORMULA, new String(\"");
            retList.addLast(node.getFormula().accept(this, args));
            retList.addLast("\"));\n");
            args.removeLast();
        }

        return retList;
    }

    /**
     * Return the code represented by a <code>ELOCConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> eloc (formula); </code>
     */
    public Object visitELOCConstraintNode(ELOCConstraintNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();
        Object source = node.getFormula().accept(new MetaModelCodegenVisitor(),
                args);

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("//eloc (");
        retList.addLast(source);
        retList.addLast(");\n");

        /*
         * args.addLast(new LinkedList());
         *
         * retList.addLast("\n"); retList.addLast(indent(indentLevel));
         * retList.addLast("//eloc (");
         * retList.addLast(node.getFormula().accept(new
         * MetaModelCodegenVisitor(), args)); retList.addLast(");\n");
         */

        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint = new Constraint(Constraint.ELOC);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.ELOC;\n");
        retList.addLast(indent(indentLevel));
        retList
                .addLast("Network.net.getNode(this).addConstraint(__tmpConstraint);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.addConstraint(__tmpConstraint);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.setSource(new String(\"");
        retList.addLast("eloc (");
        retList.addLast(source);
        retList.addLast(");");
        retList.addLast("\"));\n");

        /*
         * isInConstraint = true && isInConstraintBlock; LinkedList tmpList =
         * (LinkedList)node.getFormula().accept(this, args); isInConstraint =
         * false;
         *
         * String tmpString = _stringListToString(tmpList); tmpString =
         * tmpString.replace('"', '\''); retList.addLast(tmpString);
         * retList.addLast(");"); retList.addLast("\"));\n");
         *
         * retList.addLast(args.removeLast());
         */

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

        if (args.size() > 0)
            if (args.getLast() instanceof String)
                if (((String) args.getLast()).equals("loc_checker")) {
                    args.removeLast();
                    retList.addLast((LinkedList) node.getVar().accept(this,
                            args));
                    retList.addLast("(\") + (");
                    retList.addLast((LinkedList) node.getEvent().accept(this,
                            args));
                    retList.addLast(").locString() + new String(\"[");
                    retList.addLast(node.getIndex().accept(this, args));
                    retList.addLast("])");
                    args.addLast("loc_checker");
                    return retList;
                }

        retList.addLast((LinkedList) node.getVar().accept(this, args));
        retList.addLast("@(");
        retList.addLast((LinkedList) node.getEvent().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIndex().accept(this, args));
        retList.addLast(")");

        /*
         * if (args.size() > 0 && (!(node.getVar() instanceof LiteralNode))) {
         * if (args.getLast() instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("Network.net.addAnnotation(");
         * ((LinkedList)args.getLast()).addLast(eventList);
         * ((LinkedList)args.getLast()).addLast(", \"");
         * ((LinkedList)args.getLast()).addLast(varList);
         * ((LinkedList)args.getLast()).addLast("\");\n"); } }
         */

        return retList;
    }

    /**
     * Return the code represented by a <code>ImplyNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> event => expr </code>
     */
    public Object visitImplyNode(ImplyNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(" => (");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>ConstraintBlockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>constraint { STATEMENTS }</code>
     */

    public Object visitConstraintBlockNode(ConstraintBlockNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        // Constraints

        retList.add("\n");
        retList.add(indent(indentLevel));
        retList.add("/*constraint block*/\n");
        retList.add(indent(indentLevel));
        retList.add("{\n");

        increaseIndent();
        retList.add(indent(indentLevel));
        retList.add("Constraint __tmpConstraint;\n\n");
        retList.add(indent(indentLevel));
        retList.add("String _ltl_formula;\n\n");
        retList.add(indent(indentLevel));
        retList.add("int _constraint_type = 0;\n\n");
        //isInConstraintBlock = true;
        retList.add(TNLManip.traverseList(this, args, node.getStmts()));
        //isInConstraintBlock = false;
        decreaseIndent();

        retList.add(indent(indentLevel));
        retList.add("}\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>MinRateNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> minrate(quantity, event, value) </code>
     */
    public Object visitMinRateNode(MinRateNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList
                .addLast("__tmpConstraint = new BuiltInLOC(Constraint.MINRATE);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.MINRATE;\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addQuantity(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("((BuiltInLOC)__tmpConstraint).setValue(\"");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast("\");\n");

        /*
         * retList.addLast("minrate(");
         * retList.addLast(node.getQuantity().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getEvent().accept(this,
         * args)); retList.addLast(", ");
         * retList.addLast(node.getValue().accept(this, args));
         * retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>MaxRateNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> maxrate(quantity, event, value) </code>
     */
    public Object visitMaxRateNode(MaxRateNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList
                .addLast("__tmpConstraint = new BuiltInLOC(Constraint.MAXRATE);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.MAXRATE;\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addQuantity(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("((BuiltInLOC)__tmpConstraint).setValue(\"");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast("\");\n");

        /*
         * retList.addLast("maxrate(");
         * retList.addLast(node.getQuantity().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getEvent().accept(this,
         * args)); retList.addLast(", ");
         * retList.addLast(node.getValue().accept(this, args));
         * retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>PeriodNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> period(quantity, event, value) </code>
     */
    public Object visitPeriodNode(PeriodNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList
                .addLast("__tmpConstraint = new BuiltInLOC(Constraint.PERIOD);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.PERIOD;\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addQuantity(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("((BuiltInLOC)__tmpConstraint).setValue(\"");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast("\");\n");

        /*
         * retList.addLast("period(");
         * retList.addLast(node.getQuantity().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getEvent().accept(this,
         * args)); retList.addLast(", ");
         * retList.addLast(node.getValue().accept(this, args));
         * retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>MinDeltaNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> mindelta(quantity, event1, event2, value) </code>
     */
    public Object visitMinDeltaNode(MinDeltaNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList
                .addLast("__tmpConstraint = new BuiltInLOC(Constraint.MINDELTA);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.MINDELTA;\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addQuantity(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent1().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent2().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("((BuiltInLOC)__tmpConstraint).setValue(\"");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast("\");\n");

        /*
         * retList.addLast("mindelta(");
         * retList.addLast(node.getQuantity().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getEvent1().accept(this,
         * args)); retList.addLast(", ");
         * retList.addLast(node.getEvent2().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getValue().accept(this,
         * args)); retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>MaxDeltaNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> maxdelta(quantity, event1, event2, value) </code>
     */
    public Object visitMaxDeltaNode(MaxDeltaNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList
                .addLast("__tmpConstraint = new BuiltInLOC(Constraint.MAXDELTA);\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_constraint_type = Constraint.MAXDELTA;\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addQuantity(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent1().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("__tmpConstraint.addEvent(");
        retList.addLast(node.getEvent2().accept(this, args));
        retList.addLast(");\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("((BuiltInLOC)__tmpConstraint).setValue(\"");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast("\");\n");

        /*
         * retList.addLast("maxdelta(");
         * retList.addLast(node.getQuantity().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getEvent1().accept(this,
         * args)); retList.addLast(", ");
         * retList.addLast(node.getEvent2().accept(this, args));
         * retList.addLast(", "); retList.addLast(node.getValue().accept(this,
         * args)); retList.addLast(")");
         */

        return retList;
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
        LinkedList retList = new LinkedList();
        LinkedList source = new LinkedList();

        /*
         * LinkedList tmpList = new LinkedList(); LinkedList tmpList1 = new
         * LinkedList(); LinkedList tmpList2 = new LinkedList(); String
         * tmpString;
         *
         * LinkedList argList = new LinkedList();
         */

        source.addLast(_commaList(TNLManip.traverseList(
                new MetaModelCodegenVisitor(), args, node.getEvents())));
        if (node.getEqualVars().size() > 0) {
            source.addLast(" : ");
            source.addLast(_commaList(TNLManip.traverseList(
                    new MetaModelCodegenVisitor(), args, node.getEqualVars())));
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("//ltl synch(");
        retList.addLast(source);
        retList.addLast(");\n");

        if (node.getEvents().size() == 1
                && node.getEvents().get(0) instanceof ImplyNode) {
            /*
             * args.addLast(argList);
             *
             * isInConstraint = true && isInConstraintBlock;
             *
             * tmpList1.addLast(((ImplyNode)node.getEvents().get(0)).getEvent().accept(this,
             * args)); argList.addLast(indent(indentLevel));
             * argList.addLast("((LTLSynchImply)__tmpConstraint).eventsToLeftEvents();\n");
             * tmpList1.addLast(" => ");
             * tmpList1.addLast(((ImplyNode)node.getEvents().get(0)).getExpr().accept(this,
             * args)); argList.addLast(indent(indentLevel));
             * argList.addLast("((LTLSynchImply)__tmpConstraint).eventsToRightEvents();\n");
             *
             * if (node.getEqualVars().size()>0) { tmpList2 =
             * _commaList(TNLManip.traverseList(this, args,
             * node.getEqualVars())); } isInConstraint = false;
             */

            retList.addLast(indent(indentLevel));
            retList
                    .addLast("__tmpConstraint = new LTLSynchImply(Constraint.LTLSYNCHIMPLY);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("_constraint_type = Constraint.LTLSYNCHIMPLY;\n");
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("Network.net.getNode(this).addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("Network.net.addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("__tmpConstraint.setSource(new String(\"");
            retList.addLast("ltl synch(");
            retList.addLast(source);
            retList.addLast(");");
            retList.addLast("\"));\n");
            retList.addLast(node.accept(new ConstraintCodegenVisitor(
                    indentLevel), args));

            /*
             * tmpList.addLast(tmpList1); if (node.getEqualVars().size()>0) {
             * tmpList.addLast(" : "); tmpList.addLast(tmpList2); } tmpString =
             * _stringListToString(tmpList); tmpString = tmpString.replace('"',
             * '\''); retList.addLast(tmpString); retList.addLast(");");
             * retList.addLast("\"));\n");
             *
             * retList.addLast(args.removeLast());
             *
             * Iterator iterVars = node.getEqualVars().iterator(); while
             * (iterVars.hasNext()) {
             *
             * EqualVarsNode equalvars = (EqualVarsNode)iterVars.next(); if
             * (equalvars.getVar1() instanceof VarInEventRefNode &&
             * equalvars.getVar2() instanceof VarInEventRefNode) {
             *
             * VarInEventRefNode var1 = (VarInEventRefNode)equalvars.getVar1();
             * VarInEventRefNode var2 = (VarInEventRefNode)equalvars.getVar2();
             *
             * if
             * (!(_stringListToString((LinkedList)var1.getIndex().accept(this,
             * args)).equals("i") &&
             * _stringListToString((LinkedList)var2.getIndex().accept(this,
             * args)).equals("i") )) throw new RuntimeException("Error on ltl
             * synch constraint: equality must " + "in the form of var1@(event1,
             * i) == var2@(event2, i) " + "or var1@(event1, i) == constant or
             * constant == var2@(event2, i)");
             *
             * retList.addLast(indent(indentLevel));
             * retList.addLast("((LTLSynchImply)__tmpConstraint).addEqualVars(new
             * EqualVars(\""); if (var1.getVar() instanceof LiteralNode) {
             * retList.addLast(((LiteralNode)var1.getVar()).getLiteral());
             * retList.addLast("\", ");
             * retList.addLast(var1.getEvent().accept(this, args));
             * retList.addLast(", " + _literalType((LiteralNode)var1.getVar()) + ",
             * \""); } else { retList.addLast(var1.getVar().accept(this, args));
             * retList.addLast("\", ");
             * retList.addLast(var1.getEvent().accept(this, args));
             * retList.addLast(", EqualVars.VARTYPE, \""); }
             *
             * if (var2.getVar() instanceof LiteralNode) {
             * retList.addLast(((LiteralNode)var2.getVar()).getLiteral());
             * retList.addLast("\",");
             * retList.addLast(var2.getEvent().accept(this, args));
             * retList.addLast(", " + _literalType((LiteralNode)var2.getVar()) +
             * "));\n"); } else { retList.addLast(var2.getVar().accept(this,
             * args)); retList.addLast("\",");
             * retList.addLast(var2.getEvent().accept(this, args));
             * retList.addLast(", EqualVars.VARTYPE));\n"); } } else if
             * (equalvars.getVar1() instanceof VarInEventRefNode &&
             * equalvars.getVar2() instanceof LiteralNode) {
             *
             * VarInEventRefNode var1 = (VarInEventRefNode)equalvars.getVar1();
             * String const2 = ((LiteralNode)equalvars.getVar2()).getLiteral();
             * String type2 = _literalType((LiteralNode)equalvars.getVar2());
             *
             * if
             * (!(_stringListToString((LinkedList)var1.getIndex().accept(this,
             * args)).equals("i"))) throw new RuntimeException("Error on ltl
             * synch constraint: equality must " + "in the form of var1@(event1,
             * i) == var2@(event2, i) " + "or var1@(event1, i) == constant or
             * constant == var2@(event2, i)");
             *
             * retList.addLast(indent(indentLevel));
             * retList.addLast("((LTLSynchImply)__tmpConstraint).addEqualVars(new
             * EqualVars(\""); if (var1.getVar() instanceof LiteralNode) {
             * retList.addLast(((LiteralNode)var1.getVar()).getLiteral());
             * retList.addLast("\", ");
             * retList.addLast(var1.getEvent().accept(this, args));
             * retList.addLast(", " + _literalType((LiteralNode)var1.getVar()) + ",
             * \""); } else { retList.addLast(var1.getVar().accept(this, args));
             * retList.addLast("\", ");
             * retList.addLast(var1.getEvent().accept(this, args));
             * retList.addLast(", EqualVars.VARTYPE, \""); }
             *
             * retList.addLast(const2); retList.addLast("\", null,");
             * retList.addLast(type2); retList.addLast("));\n"); } else if
             * (equalvars.getVar1() instanceof LiteralNode &&
             * equalvars.getVar2() instanceof VarInEventRefNode) {
             * VarInEventRefNode var2 = (VarInEventRefNode)equalvars.getVar2();
             * String const1 = ((LiteralNode)equalvars.getVar1()).getLiteral();
             * String type1 = _literalType((LiteralNode)equalvars.getVar1());
             *
             * if
             * (!(_stringListToString((LinkedList)var2.getIndex().accept(this,
             * args)).equals("i"))) throw new RuntimeException("Error on ltl
             * synch constraint: equality must " + "in the form of var1@(event1,
             * i) == var2@(event2, i) " + "or var1@(event1, i) == constant or
             * constant == var2@(event2, i)");
             *
             * retList.addLast(indent(indentLevel));
             * retList.addLast("((LTLSynchImply)__tmpConstraint).addEqualVars(new
             * EqualVars(\""); retList.addLast(const1); retList.addLast("\",
             * null, "); retList.addLast(type1); retList.addLast(", \""); if
             * (var2.getVar() instanceof LiteralNode) {
             * retList.addLast(((LiteralNode)var2.getVar()).getLiteral());
             * retList.addLast("\", ");
             * retList.addLast(var2.getEvent().accept(this, args));
             * retList.addLast(", " + _literalType((LiteralNode)var2.getVar()) +
             * "));\n"); } else { retList.addLast(var2.getVar().accept(this,
             * args)); retList.addLast("\", ");
             * retList.addLast(var2.getEvent().accept(this, args));
             * retList.addLast(", EqualVars.VARTYPE));\n"); } } else throw new
             * RuntimeException("Error on ltl synch constraint: equality must " +
             * "in the form of var1@(event1, i) == var2@(event2, i) " + "or
             * var1@(event1, i) == constant or constant == var2@(event2, i)"); }
             * retList.addLast(indent(indentLevel));
             * retList.addLast("((LTLSynchImply)__tmpConstraint).checkEvents();\n\n");
             *
             * return retList;
             */
        } else {
            /*
             * args.addLast(argList);
             *
             * isInConstraint = true && isInConstraintBlock; tmpList1 =
             * _commaList(TNLManip.traverseList(this, args, node.getEvents()));
             * if (node.getEqualVars().size()>0) { tmpList2 =
             * _commaList(TNLManip.traverseList(this, args,
             * node.getEqualVars())); } isInConstraint = false;
             */

            retList.addLast(indent(indentLevel));
            retList
                    .addLast("__tmpConstraint = new LTLSynch(Constraint.LTLSYNCH);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("_constraint_type = Constraint.LTLSYNCH;\n");
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("Network.net.getNode(this).addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("Network.net.addConstraint(__tmpConstraint);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("__tmpConstraint.setSource(new String(\"");
            retList.addLast("ltl synch(");
            retList.addLast(source);
            retList.addLast(");");
            retList.addLast("\"));\n");
            retList.addLast(node.accept(new ConstraintCodegenVisitor(
                    indentLevel), args));
        }

        /*
         * tmpList.addLast(tmpList1); if (node.getEqualVars().size()>0) {
         * tmpList.addLast(" : "); tmpList.addLast(tmpList2); } tmpString =
         * _stringListToString(tmpList); tmpString = tmpString.replace('"',
         * '\''); retList.addLast(tmpString); retList.addLast(");");
         * retList.addLast("\"));\n");
         *
         * retList.addLast(args.removeLast());
         *
         * if (node.getEqualVars().size()>0) {
         *
         * EqualVarsNode equalvars = (EqualVarsNode)node.getEqualVars().get(0);
         * if (equalvars.getVar1() instanceof VarInEventRefNode &&
         * equalvars.getVar2() instanceof VarInEventRefNode) {
         *
         * VarInEventRefNode var1 = (VarInEventRefNode)equalvars.getVar1();
         * VarInEventRefNode var2 = (VarInEventRefNode)equalvars.getVar2();
         *
         * if (!(_stringListToString((LinkedList)var1.getIndex().accept(this,
         * args)).equals("i") &&
         * _stringListToString((LinkedList)var2.getIndex().accept(this,
         * args)).equals("i") )) throw new RuntimeException("Error on ltl synch
         * constraint: equality must " + "in the form of var1@(event1, i) ==
         * var2@(event2, i) " + "or var1@(event1, i) == constant or constant ==
         * var2@(event2, i)");
         *
         * retList.addLast(indent(indentLevel));
         * retList.addLast("((LTLSynch)__tmpConstraint).setEqualVars(new
         * EqualVars(\""); if (var1.getVar() instanceof LiteralNode) {
         * retList.addLast(((LiteralNode)var1.getVar()).getLiteral());
         * retList.addLast("\", "); retList.addLast(var1.getEvent().accept(this,
         * args)); retList.addLast(", " +
         * _literalType((LiteralNode)var1.getVar()) + ", \""); } else {
         * retList.addLast(var1.getVar().accept(this, args));
         * retList.addLast("\", "); retList.addLast(var1.getEvent().accept(this,
         * args)); retList.addLast(", EqualVars.VARTYPE, \""); }
         *
         * if (var2.getVar() instanceof LiteralNode) {
         * retList.addLast(((LiteralNode)var2.getVar()).getLiteral());
         * retList.addLast("\","); retList.addLast(var2.getEvent().accept(this,
         * args)); retList.addLast(", " +
         * _literalType((LiteralNode)var2.getVar()) + "));\n"); } else {
         * retList.addLast(var2.getVar().accept(this, args));
         * retList.addLast("\","); retList.addLast(var2.getEvent().accept(this,
         * args)); retList.addLast(", EqualVars.VARTYPE));\n"); } } else if
         * (equalvars.getVar1() instanceof VarInEventRefNode &&
         * equalvars.getVar2() instanceof LiteralNode) {
         *
         * VarInEventRefNode var1 = (VarInEventRefNode)equalvars.getVar1();
         * String const2 = ((LiteralNode)equalvars.getVar2()).getLiteral();
         * String type2 = _literalType((LiteralNode)equalvars.getVar2());
         *
         * if (!(_stringListToString((LinkedList)var1.getIndex().accept(this,
         * args)).equals("i"))) throw new RuntimeException("Error on ltl synch
         * constraint: equality must " + "in the form of var1@(event1, i) ==
         * var2@(event2, i) " + "or var1@(event1, i) == constant or constant ==
         * var2@(event2, i)"); retList.addLast(indent(indentLevel));
         * retList.addLast("((LTLSynch)__tmpConstraint).setEqualVars(new
         * EqualVars(\""); if (var1.getVar() instanceof LiteralNode) {
         * retList.addLast(((LiteralNode)var1.getVar()).getLiteral());
         * retList.addLast("\", "); retList.addLast(var1.getEvent().accept(this,
         * args)); retList.addLast(", " +
         * _literalType((LiteralNode)var1.getVar()) + ", \""); } else {
         * retList.addLast(var1.getVar().accept(this, args));
         * retList.addLast("\", "); retList.addLast(var1.getEvent().accept(this,
         * args)); retList.addLast(", EqualVars.VARTYPE, \""); }
         * retList.addLast(const2); retList.addLast("\", null, ");
         * retList.addLast(type2); retList.addLast("));\n"); } else if
         * (equalvars.getVar1() instanceof LiteralNode && equalvars.getVar2()
         * instanceof VarInEventRefNode) { VarInEventRefNode var2 =
         * (VarInEventRefNode)equalvars.getVar2(); String const1 =
         * ((LiteralNode)equalvars.getVar1()).getLiteral(); String type1 =
         * _literalType((LiteralNode)equalvars.getVar1());
         *
         * if (!(_stringListToString((LinkedList)var2.getIndex().accept(this,
         * args)).equals("i"))) throw new RuntimeException("Error on ltl synch
         * constraint: equality must " + "in the form of var1@(event1, i) ==
         * var2@(event2, i) " + "or var1@(event1, i) == constant or constant ==
         * var2@(event2, i)"); retList.addLast(indent(indentLevel));
         * retList.addLast("((LTLSynch)__tmpConstraint).setEqualVars(new
         * EqualVars(\""); retList.addLast(const1); retList.addLast("\", null,
         * "); retList.addLast(type1); retList.addLast(", \""); if
         * (var2.getVar() instanceof LiteralNode) {
         * retList.addLast(((LiteralNode)var2.getVar()).getLiteral());
         * retList.addLast("\", "); retList.addLast(var2.getEvent().accept(this,
         * args)); retList.addLast(", " +
         * _literalType((LiteralNode)var2.getVar()) + "));\n"); } else {
         * retList.addLast(var2.getVar().accept(this, args));
         * retList.addLast("\", "); retList.addLast(var2.getEvent().accept(this,
         * args)); retList.addLast(", EqualVars.VARTYPE));\n"); } } else throw
         * new RuntimeException("Error on ltl synch constraint: equality must " +
         * "in the form of var1@(event1, i) == var2@(event2, i) " + "or
         * var1@(event1, i) == constant or constant == var2@(event2, i)"); }
         *
         * retList.addLast(indent(indentLevel));
         * retList.addLast("((LTLSynch)__tmpConstraint).checkEvents();\n\n");
         */

        return retList;

    }

    /**
     * Return the code represented by a <code>EqualVarsNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> var1 == var2 </code>
     */
    public Object visitEqualVarsNode(EqualVarsNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(node.getVar1().accept(this, args));
        retList.addLast("==");
        retList.addLast(node.getVar2().accept(this, args));
        return retList;
    }

    // ///////Constraints

    /**
     * Return the code represented by an <code>AwaitLockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>NODE.IFACE</code>
     */

    public Object visitAwaitLockNode(AwaitLockNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(".");
        retList.addLast(node.getIface().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>AwaitStatementNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>await { GUARDS }</code>
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("/* await */\n");
        retList.addLast(indent(indentLevel));
        // retList.addLast("await {\n");
        retList.addLast("switch(1){\n");
        increaseIndent();
        casenum = 0;
        retList.addLast(TNLManip.traverseList(this, args, node.getGuards()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");
        return retList;

    }

    /**
     * Return the code represented by an <code>AwaitGuardNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>(COND; LOCKTEST; LOCKSET) STMT</code>
     */
    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        if (casenum == 0)
            retList.addLast("default :");
        else
            retList.addLast("case " + casenum + ":");
        casenum++;
        int tmpcasenum = casenum;

        /*
         * retList.addLast("(");
         *
         * TreeNode cond = node.getCond(); if (cond == AbsentTreeNode.instance) {
         * retList.addLast("default"); } else {
         * retList.addLast(cond.accept(this, args)); }
         *
         * retList.addLast("; "); retList.addLast(_commaList(
         * TNLManip.traverseList(this, args, node.getLockTest())));
         * retList.addLast("; "); retList.addLast(_commaList(
         * TNLManip.traverseList(this, args, node.getLockSet())));
         * retList.addLast(")");
         */
        increaseIndent();

        StatementNode stmt = node.getStmt();
        if (stmt instanceof BlockNode && !(stmt instanceof LabeledBlockNode)) {
            retList.addLast(stmt.accept(this, args));
            retList.addLast("\n");
        } else {
            retList.addLast("\n");
            increaseIndent();
            retList.addLast(stmt.accept(this, args));
            decreaseIndent();
        }

        decreaseIndent();
        casenum = tmpcasenum;

        return retList;
    }

    /**
     * Return the code represented by an <code>LabeledBlockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>block(LABEL) { STATEMENTS }</code>
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        // retList.addLast("block(");
        // retList.addLast(node.getLabel().accept(this, args));
        // retList.addLast(") {\n");
        retList.addLast("{ //block(");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")\n");

        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>LocalLabelNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> NAME </code>
     */
    public Object visitLocalLabelNode(LocalLabelNode node, LinkedList args) {
        return node.getName().accept(this, args);
    }

    /**
     * Return the code represented by an <code>GlobalLabelNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> label(OBJECT, LABEL) </code>
     */
    public Object visitGlobalLabelNode(GlobalLabelNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        /*
         * retList.addLast("label("); retList.addLast(node.getObj().accept(this,
         * args)); retList.addLast(", ");
         * retList.addLast(node.getLabel().accept(this, args));
         * retList.addLast(")");
         */

        return retList;
    }

    // Constraints
    /**
     * Return the code represented by an <code>ForallActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> forall VARS: SUBFORM </code>
     */
    public Object visitForallActionNode(ForallActionNode node, LinkedList args) {
        return _visitQuantifiedActionNode(node, args, "forall");
    }

    // Constraints
    /**
     * Return the code represented by an <code>ExistsActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> exists VARS: SUBFORM </code>
     */
    public Object visitExistsActionNode(ExistsActionNode node, LinkedList args) {
        return _visitQuantifiedActionNode(node, args, "exists");
    }

    /**
     * Return the code represented by an <code>ExprActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>EXPR</code>
     */
    public Object visitExprActionNode(ExprActionNode node, LinkedList args) {

        return node.getExpr().accept(this, args);

    }

    /**
     * Return the code represented by an <code>ExprLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>EXPR</code>
     */
    public Object visitExprLTLNode(ExprLTLNode node, LinkedList args) {
        return node.getExpr().accept(this, args);
    }

    /**
     * Return the code represented by an <code>MutexLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> mutex(PROC1, PROC2) </code>
     */
    public Object visitMutexLTLNode(MutexLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "mutex");
    }

    /**
     * Return the code represented by an <code>SimulLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> simul(PROC1, PROC2) </code>
     */
    public Object visitSimulLTLNode(SimulLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "simul");
    }

    /**
     * Return the code represented by an <code>ExclLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> exec(PROC1, PROC2) </code>
     */
    public Object visitExclLTLNode(ExclLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "excl");
    }

    /**
     * Return the code represented by an <code>ExclLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> Priority(PROC1, PROC2) </code>
     */
    public Object visitPriorityLTLNode(PriorityLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "priority");
    }

    /**
     * Return the code represented by an <code>FutureLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> F(SUBFORM) </code>
     */
    public Object visitFutureLTLNode(FutureLTLNode node, LinkedList args) {
        return _visitSingleLTLFormulaNode(node, args, "F");
    }

    /**
     * Return the code represented by an <code>GloballyLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> G(SUBFORM) </code>
     */
    public Object visitGloballyLTLNode(GloballyLTLNode node, LinkedList args) {
        return _visitSingleLTLFormulaNode(node, args, "G");
    }

    /**
     * Return the code represented by an <code>NextLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> X(SUBFORM) </code>
     */
    public Object visitNextLTLNode(NextLTLNode node, LinkedList args) {
        return _visitSingleLTLFormulaNode(node, args, "X");
    }

    /**
     * Return the code represented by an <code>UntilLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> SUBFORM1 U SUBFORM2 </code>
     */
    public Object visitUntilLTLNode(UntilLTLNode node, LinkedList args) {
        return _visitDoubleLTLFormulaNode(node, args, "U");
    }

    /**
     * Return the code represented by an <code>NonDeterminismNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> nondeterminism(TYPE) </code>
     */
    public Object visitNonDeterminismNode(NonDeterminismNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("0");

        return retList;
    }

    /**
     * Return the code represented by an <code>ExecIndexNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> execindex(ACTION)</code>
     */
    public Object visitExecIndexNode(ExecIndexNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        /*
         * retList.addLast("execindex(");
         * retList.addLast(node.getAction().accept(this, args));
         * retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>BoundedLoopNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> boundedloop(VAR, COUNT) { STMT } </code>
     */
    public Object visitBoundedLoopNode(BoundedLoopNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("for (int ");
        retList.addLast(node.getVar().accept(this, args));
        retList.addLast(" = 0; ");
        retList.addLast(node.getVar().accept(this, args));
        retList.addLast(" < ");
        retList.addLast(node.getCount().accept(this, args));
        retList.addLast("; ");
        retList.addLast(node.getVar().accept(this, args));
        retList.addLast("++ ");
        retList.addLast("){\n");
        increaseIndent();
        retList.addLast(node.getStmt().accept(this, args));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>CifNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 -> EXPR2 </code>
     */
    public Object visitCifNode(CifNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" -> ");
        retList.addLast(node.getExpr2().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>CiffNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 <-> EXPR2 </code>
     */
    public Object visitCiffNode(CiffNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" <-> ");
        retList.addLast(node.getExpr2().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>BeginPCNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> beg(LABEL) </code>
     */
    public Object visitBeginPCNode(BeginPCNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        /*
         * retList.addLast("beg("); retList.addLast(node.getLabel().accept(this,
         * args)); retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>EndPCNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> end(LABEL) </code>
     */
    public Object visitEndPCNode(EndPCNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        /*
         * retList.addLast("end("); retList.addLast(node.getLabel().accept(this,
         * args)); retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>PCNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> pc(EXPR) </code>
     */
    public Object visitPCNode(PCNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        /*
         * retList.addLast("pc("); retList.addLast(node.getExpr().accept(this,
         * args)); retList.addLast(")");
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>RefineNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> refine(NODE, NETLIST) </code>
     */
    public Object visitRefineNode(RefineNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.refine(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>RefineConnectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>refineconnect(NETLIST, SRCOBJECT, PORT, COMPONENT)</code>
     */
    public Object visitRefineConnectNode(RefineConnectNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.refineConnect(");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getSrcObject().accept(this, args));
        retList.addLast(", ");
        if (node.getPort() instanceof ObjectNode)
            retList.addLast("\"");
        String pName = _stringListToString((LinkedList) node.getPort().accept(
                this, args));
        int index;
        if ((index = pName.indexOf('[')) == -1) {
            retList.addLast(pName);
        } else {
            retList.addLast("\"");
            retList.addLast(pName.substring(0, index));
            retList.addLast("\"+");
            pName = pName.substring(index);
            String separator = "";
            while ((index = pName.indexOf('[')) >= 0) {
                retList.addLast(separator);
                separator = "+";
                retList.addLast("\"[\"+");
                int endindex = pName.indexOf(']');
                retList.addLast(pName.substring(index + 1, endindex));
                retList.addLast("+\"]\"");
                pName = pName.substring(endindex + 1);
            }
        }
        if (node.getPort() instanceof ObjectNode)
            retList.addLast("\"");
        retList.addLast(", ");
        retList.addLast(node.getComponent().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>ConnectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>connect(SRCOBJECT, PORT, DSTOBJECT)</code>
     */
    public Object visitConnectNode(ConnectNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.connect(");
        retList.addLast(node.getSrcObject().accept(this, args));
        retList.addLast(", ");

        retList.addLast(_visitPortExpr(node.getPort(), args));

        retList.addLast(", ");
        retList.addLast(node.getDstObject().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

        // System.out.println(node.getSrcObject().getClass().getName());
        // System.out.println(node.getPort().getClass().getName());

        // xichen: actually connect the java interface associated with
        // the mmm interface to the medium object
        /*
         * if ((node.getSrcObject() instanceof ObjectNode) ||
         * (node.getSrcObject() instanceof ArrayAccessNode) ||
         * (node.getSrcObject() instanceof FieldAccessNode) ||
         * ((node.getSrcObject() instanceof CastNode) &&
         * ((((CastNode)node.getSrcObject()).getExpr() instanceof ObjectNode) ||
         * (((CastNode)node.getSrcObject()).getExpr() instanceof
         * ArrayAccessNode) || (((CastNode)node.getSrcObject()).getExpr()
         * instanceof FieldAccessNode)))) {
         *
         * String tmpStr =
         * _stringListToString((LinkedList)node.getPort().accept(this, args));
         * if (tmpStr.lastIndexOf('.') >= 0) tmpStr = "(" + tmpStr.substring(0,
         * tmpStr.lastIndexOf('.')) + ")" + ".__pointer_" +
         * tmpStr.substring(tmpStr.lastIndexOf('.')+1); else tmpStr =
         * "__pointer_" + tmpStr;
         *
         * retList.addLast(indent(indentLevel)); retList.addLast("(");
         * retList.addLast(node.getSrcObject().accept(this, args));
         * retList.addLast(")."); retList.addLast(tmpStr); retList.addLast(" =
         * "); retList.addLast(node.getDstObject().accept(this, args));
         * retList.addLast(";\n"); } else { //xichen: TODO: need to take care of
         * more situations String portStr =
         * _stringListToString((LinkedList)node.getPort().accept(this,
         * args)).trim(); retList.addLast(indent(indentLevel));
         * retList.addLast("try {\n"); increaseIndent();
         *
         * if (portStr.indexOf('[') >= 0){ retList.addLast(indent(indentLevel));
         * retList.addLast("Field __vector_port_field_ = ");
         * retList.addLast("Class.forName(Network.net.getNode(");
         * retList.addLast(node.getSrcObject().accept(this, args));
         * retList.addLast(").getType().getName()).getField(\"__pointer_");
         * retList.addLast(portStr.substring(0, portStr.indexOf('[')));
         * retList.addLast("\");\n");
         *
         * int index0 = portStr.indexOf('['); int index1 = portStr.indexOf('[',
         * index0+1); LinkedList arrayList = new LinkedList();
         * arrayList.addLast("__vector_port_field_.get(");
         * arrayList.addLast(node.getSrcObject().accept(this, args));
         * arrayList.addLast(")"); while (index1 >= 0) {
         * arrayList.addFirst("java.lang.reflect.Array.get(");
         * arrayList.addLast(", "); arrayList.addLast(portStr.substring (index0 +
         * 1, portStr.indexOf(']', index0))); arrayList.addLast(")"); index0 =
         * portStr.indexOf('[', index0+1); index1 = portStr.indexOf('[',
         * index0+1); }
         *
         * arrayList.addFirst("java.lang.reflect.Array.set(");
         * arrayList.addLast(", "); arrayList.addLast(portStr.substring (index0 +
         * 1, portStr.indexOf(']', index0))); arrayList.addLast(", ");
         * arrayList.addLast(node.getDstObject().accept(this, args));
         * arrayList.addLast(");\n"); retList.addLast(indent(indentLevel));
         * retList.addLast(arrayList); } else {
         * retList.addLast(indent(indentLevel));
         * retList.addLast("Class.forName(Network.net.getNode(");
         * retList.addLast(node.getSrcObject().accept(this, args));
         * retList.addLast(").getType().getName()).getField(\"__pointer_");
         * retList.addLast(portStr); retList.addLast("\").set(");
         * retList.addLast(node.getSrcObject().accept(this, args));
         * retList.addLast(", ");
         * retList.addLast(node.getDstObject().accept(this, args));
         * retList.addLast(");\n"); } decreaseIndent();
         * retList.addLast(indent(indentLevel)); retList.addLast("} catch
         * (Exception ex){\n");
         *
         * increaseIndent(); retList.addLast(indent(indentLevel));
         * retList.addLast("System.err.println(\"connect() failed - cannot
         * actually connect the two objects\");\n");
         * retList.addLast(indent(indentLevel));
         * retList.addLast("ex.printStackTrace();\n");
         * retList.addLast(indent(indentLevel));
         * retList.addLast("System.exit(1);\n");
         *
         * decreaseIndent(); retList.addLast(indent(indentLevel));
         * retList.addLast("}\n"); }
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>RedirectConnectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>redirectconnect(NETLIST, SRCOBJECT, SRCPORT,
     *                                        COMPONENT, NEWPORT)</code>
     */
    public Object visitRedirectConnectNode(RedirectConnectNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.redirectConnect(");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getSrcObject().accept(this, args));
        retList.addLast(", ");
        if (node.getSrcPort() instanceof ObjectNode)
            retList.addLast("\"");
        String pName = _stringListToString((LinkedList) node.getSrcPort()
                .accept(this, args));
        int index;
        if ((index = pName.indexOf('[')) == -1) {
            retList.addLast(pName);
        } else {
            retList.addLast("\"");
            retList.addLast(pName.substring(0, index));
            retList.addLast("\"+");
            pName = pName.substring(index);
            String separator = "";
            while ((index = pName.indexOf('[')) >= 0) {
                retList.addLast(separator);
                separator = "+";
                retList.addLast("\"[\"+");
                int endindex = pName.indexOf(']');
                retList.addLast(pName.substring(index + 1, endindex));
                retList.addLast("+\"]\"");
                pName = pName.substring(endindex + 1);
            }
        }
        if (node.getSrcPort() instanceof ObjectNode)
            retList.addLast("\"");
        retList.addLast(", ");
        retList.addLast(node.getComponent().accept(this, args));
        retList.addLast(", ");
        if (node.getNewPort() instanceof ObjectNode)
            retList.addLast("\"");
        pName = _stringListToString((LinkedList) node.getNewPort().accept(this,
                args));
        if ((index = pName.indexOf('[')) == -1) {
            retList.addLast(pName);
        } else {
            retList.addLast("\"");
            retList.addLast(pName.substring(0, index));
            retList.addLast("\"+");
            pName = pName.substring(index);
            String separator = "";
            while ((index = pName.indexOf('[')) >= 0) {
                retList.addLast(separator);
                separator = "+";
                retList.addLast("\"[\"+");
                int endindex = pName.indexOf(']');
                retList.addLast(pName.substring(index + 1, endindex));
                retList.addLast("+\"]\"");
                pName = pName.substring(endindex + 1);
            }
        }
        if (node.getNewPort() instanceof ObjectNode)
            retList.addLast("\"");
        retList.addLast(")");
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>AddComponentNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> addcomponent(NODE, NETLIST) </code>
     */
    public Object visitAddComponentNode(AddComponentNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.addComponent(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>SetScopeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> setscope(NODE, pORT, NETLIST) </code>
     */
    public Object visitSetScopeNode(SetScopeNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.setScope(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        if (node.getPort() instanceof ObjectNode)
            retList.addLast("\"");
        String pName = _stringListToString((LinkedList) node.getPort().accept(
                this, args));
        int index;
        if ((index = pName.indexOf('[')) == -1) {
            retList.addLast(pName);
        } else {
            retList.addLast("\"");
            retList.addLast(pName.substring(0, index));
            retList.addLast("\"+");
            pName = pName.substring(index);
            String separator = "";
            while ((index = pName.indexOf('[')) >= 0) {
                retList.addLast(separator);
                separator = "+";
                retList.addLast("\"[\"+");
                int endindex = pName.indexOf(']');
                retList.addLast(pName.substring(index + 1, endindex));
                retList.addLast("+\"]\"");
                pName = pName.substring(endindex + 1);
            }
        }
        if (node.getPort() instanceof ObjectNode)
            retList.addLast("\"");
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>BlackboxNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> blackbox(IDENT) %% CODE %% </code>
     */
    public Object visitBlackboxNode(BlackboxNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        if (((String) node.getIdent()).equals("elaborator")) {
            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast(node.getCode());
            decreaseIndent();
            retList.addLast("\n");
        }

        return retList;
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

        if (args.size() > 0)
            if (args.getLast() instanceof String)
                if (((String) args.getLast()).equals("loc_checker")) {
                    args.removeLast();
                    retList.addLast("\") + Network.net.getConnectionNum(");
                    retList.addLast(node.getMedium().accept(this, args));
                    retList.addLast(", \"");
                    MetaModelDecl decl = MetaModelDecl
                            .getDecl(node.getIfName());
                    if (decl == null)
                        retList.addLast(node.getIfName().accept(this, args));
                    else
                        retList.addLast(decl.fullName());
                    retList.addLast("\") + new String(\"");
                    args.addLast("loc_checker");
                    return retList;
                }

        retList.addLast("Network.net.getConnectionNum(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", \"");
        MetaModelDecl decl = MetaModelDecl.getDecl(node.getIfName());
        if (decl == null)
            retList.addLast(node.getIfName().accept(this, args));
        else
            retList.addLast(decl.fullName());
        retList.addLast("\")");

        /*
         * if (args.size() > 0) { if (args.getLast() instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addStructureValue(\"getconnectionnum(");
         * ((LinkedList)args.getLast()).addLast(node.getMedium().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(",");
         * ((LinkedList)args.getLast()).addLast(node.getIfName().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(")\", new Integer(");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast("));\n"); } }
         */

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

        /*
         * if (args.size() > 0 ) if (args.getLast() instanceof String) if
         * (((String)args.getLast()).equals("loc_checker")) { args.removeLast();
         * retList.addLast("\") + Network.net.getNthConnectionSrc(");
         * retList.addLast(node.getMedium().accept(this, args));
         * retList.addLast(", \""); MetaModelDecl decl =
         * MetaModelDecl.getDecl(node.getIfName()); if (decl == null)
         * retList.addLast(node.getIfName().accept(this, args)); else
         * retList.addLast(decl.fullName()); retList.addLast("\", ");
         * retList.addLast(node.getNum().accept(this, args)); retList.addLast(") +
         * new String(\""); args.addLast("loc_checker"); return retList; }
         */

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

        /*
         * if (args.size() > 0) { if (args.getLast() instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addStructureValue(\"getnthconnectionsrc(");
         * ((LinkedList)args.getLast()).addLast(node.getMedium().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(",");
         * ((LinkedList)args.getLast()).addLast(node.getIfName().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(",");
         * ((LinkedList)args.getLast()).addLast(node.getNum().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(")\",
         * Network.net.getNode(");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast("));\n"); } }
         */

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

        /*
         * if (args.size() > 0 ) if (args.getLast() instanceof String) if
         * (((String)args.getLast()).equals("loc_checker")) { args.removeLast();
         * retList.addLast("\") + Network.net.getNthConnectionPort(");
         * retList.addLast(node.getMedium().accept(this, args));
         * retList.addLast(", \""); MetaModelDecl decl =
         * MetaModelDecl.getDecl(node.getIfName()); if (decl == null)
         * retList.addLast(node.getIfName().accept(this, args)); else
         * retList.addLast(decl.fullName()); retList.addLast("\", ");
         * retList.addLast(node.getNum().accept(this, args)); retList.addLast(") +
         * new String(\""); args.addLast("loc_checker"); return retList; }
         */

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

        /*
         * if (args.size() > 0) { if (args.getLast() instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addStructureValue(\"getnthconnectionport(");
         * ((LinkedList)args.getLast()).addLast(node.getMedium().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(",");
         * ((LinkedList)args.getLast()).addLast(node.getIfName().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(",");
         * ((LinkedList)args.getLast()).addLast(node.getNum().accept(this,
         * args)); ((LinkedList)args.getLast()).addLast(")\", (IPort)");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(");\n"); } }
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>PortDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>port DEFTYPE NAME;</code>
     */
    public Object visitPortDeclNode(PortDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        // LinkedList t1, t2;

        retList.addLast(indent(indentLevel));
        retList.addLast("/* port */ public ");
        // t1 = (LinkedList) node.getDefType().accept(this, args);
        // t2 = (LinkedList) t1.clone();
        // retList.addLast(t1);
        // retList.addLast(" ");
        // retList.addLast(node.getName().accept(this, args));
        // retList.addLast(";\n");
        // retList.addLast(indent(indentLevel));

        retList.addLast(node.getDefType().accept(this, args));

        // TreeNode type = node.getDefType();
        // int dim = 0;
        // while (type instanceof ArrayTypeNode) {
        // dim ++;
        // type = ((ArrayTypeNode) type).getBaseType();
        // }
        // retList.addLast("Object");
        // for (int i=0; i<dim; i++) retList.addLast("[]");
        retList.addLast(" ");
        // t2.set(0, "PortInst");
        // retList.addLast(t2);
        // retList.addLast(" _");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(";\n");

        // xichen: define a java interface pointer associated with an mmm
        // port and use it for interface function calls
        retList.addLast(indent(indentLevel));
        retList.addLast("public __interface_");
        retList.addLast(node.getDefType().accept(this, args));
        retList.addLast(" __pointer_");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>ParameterDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> MODIFIER parameter DEFTYPE NAME; </code>
     */
    public Object visitParameterDeclNode(ParameterDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("/* parameter */");
        retList.addLast(node.getDefType().accept(this, args));
        retList.addLast(" ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>ProcessDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>template(TYPE1, TYPE2, ...)
     *                  MODIFIER process NAME extends SUPERCLASS { ... }</code>
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {
            // xichen: we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        } else {
            retList.addLast(" extends metamodel.lang.Process ");
        }

        retList.addLast(" implements java.io.Serializable");

        retList.addLast(" {\n");
        increaseIndent();
        // args.set(0, new String("Process"));
        getPorts(node.getMembers());
        // flag = !(node.getSuperClass() instanceof AbsentTreeNode);
        // retList.addLast(_genNodeType(node, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        // flag = false;
        retList.addLast(generateScalarPorts());
        releasePorts();
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        retList.addFirst(node.getName().accept(this, args));
        return retList;
    }

    /**
     * Return the code represented by an <code>MediumDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> template(TYPE1, TYPE2, ...)
     *                         MODIFIER medium NAME extends SUPERCLASS
     *                                 implements INTERFACES { ... } </code>
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {
            // xichen: we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        } else {
            retList.addLast(" extends metamodel.lang.Medium ");
        }

        // xichen: actually implements the java interfaces associated with
        // the mmm interfaces

        // retList.addLast(" implements java.io.Serializable");

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        retList.addLast(" implements ");
        if (!retValue.isEmpty()) {
            Iterator stringListItr = retValue.iterator();
            while (stringListItr.hasNext()) {
                retList.addLast("__interface_");
                retList.addLast(stringListItr.next());
                if (stringListItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            retList.addLast(", java.io.Serializable");
        } else
            retList.addLast("java.io.Serializable");

        retList.addLast(" {\n");
        increaseIndent();
        // args.set(0, new String("Medium"));
        getPorts(node.getMembers());
        // flag = !(node.getSuperClass() instanceof AbsentTreeNode);
        // retList.addLast(_genNodeType(node, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        // flag = false;
        retList.addLast(generateScalarPorts());
        releasePorts();
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        retList.addFirst(node.getName().accept(this, args));
        return retList;
    }

    /**
     * Return the code represented by an <code>SchedulerDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>template(TYPE1, TYPE2, ...)
     *                  MODIFIER scheduler NAME extends SUPPERCLASS</code>
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {
            // xichen: we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        } else {
            retList.addLast(" extends metamodel.lang.Scheduler ");
        }
        retList.addLast(" implements java.io.Serializable");

        retList.addLast(" {\n");
        increaseIndent();
        // args.set(0, new String("Scheduler"));
        getPorts(node.getMembers());
        // flag = !(node.getSuperClass() instanceof AbsentTreeNode);
        // retList.addLast(_genNodeType(node, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        // flag = false;
        retList.addLast(generateScalarPorts());
        releasePorts();
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        retList.addFirst(node.getName().accept(this, args));
        return retList;
    }

    /**
     * Return the code represented by an <code>SMDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>template(TYPE1, TYPE2, ...)
     *                  MODIFIER statemedium NAME extends SUPERCLASS
     *                                 implements INTERFACES { ... } </code>
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {
            // xichen: we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        } else {
            retList.addLast(" extends metamodel.lang.StateMedium ");
        }

        // xichen: actually implements the java interfaces associated with
        // the mmm interfaces

        // List retValue =
        // TNLManip.traverseList(this, args, node.getInterfaces());
        // retList.addLast(" implements java.io.Serializable");

        /*
         * if (!retValue.isEmpty()) { retList.addLast(_commaList(retValue));
         * retList.addLast(", java.io.Serializable"); } else
         * retList.addLast("java.io.Serializable");
         */

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        retList.addLast(" implements ");
        if (!retValue.isEmpty()) {
            Iterator stringListItr = retValue.iterator();
            while (stringListItr.hasNext()) {
                retList.addLast("__interface_");
                retList.addLast(stringListItr.next());
                if (stringListItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            retList.addLast(", java.io.Serializable");
        } else
            retList.addLast("java.io.Serializable");

        retList.addLast(" {\n");
        increaseIndent();
        // args.set(0, new String("StateMedium"));
        // flag = !(node.getSuperClass() instanceof AbsentTreeNode);
        // retList.addLast(_genNodeType(node, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        // flag = false;
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        retList.addFirst(node.getName().accept(this, args));
        return retList;
    }

    /**
     * Return the code represented by an <code>NetlistDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>template(TYPE1, TYPE2, ...)
     *                  MODIFIER netlist NAME extends SUPERCLASS
     *                                 implements INTERFACES { ... } </code>
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {
            // xichen: we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        } else {
            retList.addLast(" extends metamodel.lang.Netlist ");
        }

        // xichen: actually implements the java interfaces associated with
        // the mmm interfaces

        // List retValue =
        // TNLManip.traverseList(this, args, node.getInterfaces());
        // retList.addLast(" implements java.io.Serializable");

        /*
         * if (!retValue.isEmpty()) { retList.addLast(_commaList(retValue));
         * retList.addLast(", java.io.Serializable"); } else
         * retList.addLast("java.io.Serializable");
         */

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        retList.addLast(" implements ");
        if (!retValue.isEmpty()) {
            Iterator stringListItr = retValue.iterator();
            while (stringListItr.hasNext()) {
                retList.addLast("__interface_");
                retList.addLast(stringListItr.next());
                if (stringListItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            retList.addLast(", java.io.Serializable");
        } else
            retList.addLast("java.io.Serializable");

        retList.addLast(" {\n");
        increaseIndent();
        // args.set(0, new String("Netlist"));
        // retList.addLast(_genNodeType(node, args));
        // flag = !(node.getSuperClass() instanceof AbsentTreeNode);
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        // flag = false;
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        retList.addFirst(node.getName().accept(this, args));
        return retList;
    }

    /**
     * Return the code represented by an <code>TemplateParametersNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> <TYPE1, TYPE2, ... > </code>
     */
    public Object visitTemplateParametersNode(TemplateParametersNode node,
            LinkedList args) {
        return _commaList(TNLManip.traverseList(this, args, node.getTypes()));
    }

    /**
     * Return the code represented by an <code>ObjectPortAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> OBJECT.NAME </code>
     */
    public Object visitObjectPortAccessNode(ObjectPortAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getObject().accept(this, args));
        retList.addLast(".");

        // xichen: if the port is used to access fields in the connected
        // medium, the real interface pointer is used
        TreeNode parent = node.getParent();
        while (parent instanceof ArrayAccessNode)
            parent = parent.getParent();

        if ((parent instanceof ObjectFieldAccessNode)
                || (parent instanceof ObjectPortAccessNode))
            retList.addLast("__pointer_");

        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>SuperPortAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> super.NAME </code>
     */
    public Object visitSuperPortAccessNode(SuperPortAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("super.");

        // xichen: if the port is used to access fields in the connected
        // medium, the real interface pointer is used
        TreeNode parent = node.getParent();
        while (parent instanceof ArrayAccessNode)
            parent = parent.getParent();

        if ((parent instanceof ObjectFieldAccessNode)
                || (parent instanceof ObjectPortAccessNode))
            retList.addLast("__pointer_");

        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>ThisPortAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> this.NAME </code>
     */
    public Object visitThisPortAccessNode(ThisPortAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        // retList.addLast("this.");

        // xichen: if the port is used to access fields in the connected
        // medium, the real interface pointer is used
        TreeNode parent = node.getParent();
        while (parent instanceof ArrayAccessNode)
            parent = parent.getParent();

        if ((parent instanceof ObjectFieldAccessNode)
                || (parent instanceof ObjectPortAccessNode))
            retList.addLast("__pointer_");

        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>ObjectParamAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> OBJECT.NAME </code>
     */
    public Object visitObjectParamAccessNode(ObjectParamAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getObject().accept(this, args));
        retList.addLast(".");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>ThisParamAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> this.NAME </code>
     */
    public Object visitThisParamAccessNode(ThisParamAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("this.");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>SuperParamAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> super.NAME </code>
     */
    public Object visitSuperParamAccessNode(SuperParamAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("super.");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>PCTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> pcval </code>
     */
    public Object visitPCTypeNode(PCTypeNode node, LinkedList args) {
        return TNLManip.addFirst("pcval");
    }

    /**
     * Return the code represented by a <code>CompileUnitNode</code>. It
     * includes the package, import statements, and class or interfaces
     * definitions.
     * <p>
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a string that represents the code
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // indent level should be currently 0
        Hashtable retHash = new Hashtable();
        LinkedList retList = new LinkedList();

        if (args == null)
            args = new LinkedList();
        // args.addFirst(null);

        // package
        if (node.getPkg() != AbsentTreeNode.instance) {
            retList.addLast("package ");
            retList.addLast(node.getPkg().accept(this, args));
            retList.addLast(";\n");
        }

        // import
        retList.addLast(TNLManip.traverseList(this, args, node.getImports()));
        retList.addLast("\n");

        // def types
        Iterator typeItr = node.getDefTypes().iterator();

        while (typeItr.hasNext()) {
            Object object = typeItr.next();

            if (object instanceof BlackboxNode) {
                retList.addLast(((BlackboxNode) object).accept(this, args));
            } else if (object instanceof TreeNode) {
                TreeNode treeNode = (TreeNode) object;
                Object returnValue;

                returnValue = treeNode.accept(this, args, true);

                if (returnValue == null || ((LinkedList) returnValue).isEmpty()) {
                } /*
                 * else if (treeNode instanceof InterfaceDeclNode) {
                 * retHash.put(name, _stringListToString
                 * ((LinkedList)((LinkedList)returnValue).get(0)));
                 * retHash.put(new String("_"+name),
                 * _stringListToString((LinkedList)((LinkedList)returnValue).get(1))); }
                 */
                else {
                    String name = _stringListToString((LinkedList) ((LinkedList) returnValue)
                            .removeFirst());

                    // xichen:define a java interface associated with an mmm
                    // interface
                    // in a separate file. In the return code, the two files are
                    // separated
                    // by string "***interface***"
                    if (treeNode instanceof InterfaceDeclNode) {
                        String returnStr = _stringListToString((LinkedList) returnValue);
                        retHash.put(name, returnStr.substring(0, returnStr
                                .indexOf("***interface***")));
                        name = "__interface_" + name;
                        retHash.put(name, returnStr.substring(returnStr
                                .indexOf("***interface***") + 15));
                    } else
                        retHash.put(name,
                                _stringListToString((LinkedList) returnValue));
                }

                // FIXME: In the case of a null return value, should
                // NullValue.instance be set as the property value?
                treeNode.setProperty(PropertyMap.RETURN_VALUE_AS_ELEMENT_KEY,
                        returnValue);

            } else {
                throw new RuntimeException("TNLManip.traverseList(): "
                        + "unknown object in list: " + object.getClass());
            }
        }

        retHash.put(new String("package"), _stringListToString(retList));
        return retHash;
    }

    /**
     * Return the code represented by a <code>NameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>NAME</code> or
     *         <code> QUALIFIER.NAME-<PARAMETERS>- </code>
     */
    public Object visitNameNode(NameNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        String ident = node.getIdent();
        TreeNode qualifier = node.getQualifier();

        if (qualifier == AbsentTreeNode.instance) {
            retList.addLast(ident);
        } else {
            retList.addLast(qualifier.accept(this, args));
            retList.addLast(".");
            retList.addLast(ident);
        }

        /*
         * TreeNode params = node.getParameters(); if (params !=
         * AbsentTreeNode.instance) { retList.addLast("-<");
         * retList.addLast(params.accept(this, args)); retList.addLast(">-"); }
         */

        return retList;
    }

    /**
     * Return the code represented by an <code>ImportNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>import NAME;</code>
     */
    public Object visitImportNode(ImportNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        ObjectDecl decl = (ObjectDecl) node.getName().getProperty(DECL_KEY);
        if (decl != null && !decl.getTypeParams().isEmpty())
            return retList;

        retList.addLast(indent(indentLevel));
        retList.addLast("import ");

        // xichen: convert import to import-on-demand due to newly created
        // java interfaces associated with mmm interfaces
        String retStr = _stringListToString((LinkedList) node.getName().accept(
                this, args));
        retList.addLast(retStr.substring(0, retStr.lastIndexOf('.')));
        retList.addLast(".*;\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>ImportOnDemandNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>import NAME.*;</code>
     */
    public Object visitImportOnDemandNode(ImportOnDemandNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        PackageDecl pkgDecl = (PackageDecl) node.getName()
                .getProperty(DECL_KEY);
        if (pkgDecl != null) {
            ScopeIterator iter = pkgDecl.getScope().allDecls();
            boolean found = false;
            while (iter.hasNext()) {

                Object obj = iter.next();
                if (obj instanceof ObjectDecl) {
                    ObjectDecl decl = (ObjectDecl) obj;
                    if (decl.getTypeParams() == null
                            || decl.getTypeParams().isEmpty()) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found)
                return retList;
        }
        retList.addLast(indent(indentLevel));
        retList.addLast("import ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(".*;\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>ClassDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents<br>
     *         <code> MODIFIER class NAME extends SUPERCLASS
     *                         implements INTERFACE1, INTERFACE2, ... { ... }
     * </code>
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // visit type parameters
        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {

            // xichen we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")");
        }
        retList.addLast("\n");

        // visit name node
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        retList.addLast(" ");

        TreeNode superClass = node.getSuperClass();
        if (superClass != AbsentTreeNode.instance) {
            retList.addLast("extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
            retList.addLast(" ");
        }

        // xichen: actually implements the java interfaces associated with
        // the mmm interfaces

        // visit interfaces
        // List retValue =
        // TNLManip.traverseList(this, args, node.getInterfaces());
        // retList.addLast(" implements java.io.Serializable");

        /*
         * if (!retValue.isEmpty()) { retList.addLast(_commaList(retValue));
         * retList.addLast(", java.io.Serializable"); } else
         * retList.addLast("java.io.Serializable");
         */

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        retList.addLast(" implements ");
        if (!retValue.isEmpty()) {
            Iterator stringListItr = retValue.iterator();
            while (stringListItr.hasNext()) {
                retList.addLast("__interface_");
                retList.addLast(stringListItr.next());
                if (stringListItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            retList.addLast(", java.io.Serializable");
        } else
            retList.addLast("java.io.Serializable");

        retList.addLast(" , Cloneable {\n");

        // visit members
        increaseIndent();

        if (node.getParent() instanceof CompileUnitNode) {
            CompileUnitNode parent = (CompileUnitNode) node.getParent();
            PackageDecl pkg = (PackageDecl) parent.getProperty(PACKAGE_KEY);
            if (pkg.fullName().equals("metamodel.lang")) {
                if (node.getName().getIdent().equals("Node")) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("private int objectID = 0;\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("private static int objectIDIndex = 0;\n");
                    retList.addLast(indent(indentLevel));
                    retList
                            .addLast("private int generateObjectID() { return (++objectIDIndex); }\n");
                    retList.addLast(indent(indentLevel));
                    retList
                            .addLast("public int getObjectID() { return objectID; }\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("public void postElaborate(){}\n");
                }
            }
        }

        // args.set(0, new String("Class"));

        // xichen: skip inner classes
        if (!(node.getParent() instanceof UserTypeDeclNode))
            getPorts(node.getMembers());

        // retList.addLast(_genNodeType(node, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));

        // xichen: skip inner classes
        if (!(node.getParent() instanceof UserTypeDeclNode)) {
            retList.addLast(generateScalarPorts());
            releasePorts();
        }
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        if (node.getParent() instanceof CompileUnitNode) {
            retList.addFirst(node.getName().accept(this, args));
        }
        return retList;
    }

    /**
     * Return the code represented by a <code>QuantityDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents<br>
     *         <code> MODIFIER quantity NAME extends SUPERCLASS
     *                         implements INTERFACE1, INTERFACE2, ... { ... }
     * </code>
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // visit type parameters
        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {

            // xichen we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")")
        }
        retList.addLast("\n");

        // visit name node
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));

        retList.addLast(" ");

        TreeNode superClass = node.getSuperClass();
        if (superClass != AbsentTreeNode.instance) {
            retList.addLast("extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
            retList.addLast(" ");
        } else {
            retList.addLast(" extends metamodel.lang.Quantity ");
        }

        // xichen: actually implements the java interfaces associated with
        // the mmm interfaces

        // visit interfaces
        // List retValue =
        // TNLManip.traverseList(this, args, node.getInterfaces());
        // retList.addLast(" implements java.io.Serializable");

        /*
         * if (!retValue.isEmpty()) { retList.addLast(_commaList(retValue));
         * retList.addLast(", java.io.Serializable"); } else
         * retList.addLast("java.io.Serializable");
         */

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        retList.addLast(" implements ");
        if (!retValue.isEmpty()) {
            Iterator stringListItr = retValue.iterator();
            while (stringListItr.hasNext()) {
                retList.addLast("__interface_");
                retList.addLast(stringListItr.next());
                if (stringListItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            retList.addLast(", java.io.Serializable");
        } else
            retList.addLast("java.io.Serializable");

        retList.addLast(" {\n");

        // visit members
        increaseIndent();
        // args.set(0, new String("Class"));
        getPorts(node.getMembers());
        // retList.addLast(_genNodeType(node, args));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        retList.addLast(generateScalarPorts());
        releasePorts();
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        if (node.getParent() instanceof CompileUnitNode) {
            retList.addFirst(node.getName().accept(this, args));
        }
        return retList;
    }

    /**
     * Return the code represented by an <code>InterfaceDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER interface NAME extends SUPERCLASS { ... }</code>
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        LinkedList finalList = new LinkedList();

        // visit type parameters
        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {

            // xichen we disregard the template declarations
            return retList;

            // retList.addLast("\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("template (");
            // retList.addLast(_semicolonList(template));
            // retList.addLast(")")
        }
        retList.addLast("\n");

        // visit name node
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("class ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(" ");

        // visit interfaces
        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        LinkedList mid = new LinkedList();
        if (!retValue.isEmpty()) {
            Iterator iter = retValue.iterator();
            while (iter.hasNext()) {
                String s = _stringListToString((List) iter.next()).trim();
                if (!s.equals("Port")) {
                    mid.addLast(s);
                }
            }
        }

        if (!mid.isEmpty()) {
            retList.addLast("extends ");
            retList.addLast(_commaList(mid));
        }
        retList.addLast(" implements java.io.Serializable");

        retList.addLast(" {\n");

        // visit members
        increaseIndent();
        // args.set(0, new String("Interface"));
        retList.addLast(indent(indentLevel));
        retList.addLast("char MMMInterfaceIdentifier;\n");
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();
        retList.addLast("}\n\n");

        // xichen:define a java interface associated with an mmm interface
        // in a separate file. In the return code, the two files are separated
        // by string "***interface***"
        // visit name node
        retList.addLast("***interface***");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("interface __interface_");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(" ");

        // visit interfaces
        retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        mid = new LinkedList();
        if (!retValue.isEmpty()) {
            Iterator iter = retValue.iterator();
            while (iter.hasNext()) {
                String s = _stringListToString((List) iter.next()).trim();
                if (!s.equals("Port")) {
                    s = "__interface_" + s;
                    mid.addLast(s);
                }
            }
        }

        if (!mid.isEmpty()) {
            retList.addLast("extends ");
            retList.addLast(_commaList(mid));
            retList.addLast(", java.io.Serializable");
        } else
            retList.addLast("extends java.io.Serializable");

        retList.addLast(" {\n");

        // visit members
        increaseIndent();
        Iterator iter = node.getMembers().iterator();
        while (iter.hasNext()) {
            TreeNode memNode = (TreeNode) iter.next();
            if (memNode instanceof MethodDeclNode) {
                retList.addLast(indent(indentLevel));

                int modifiers = ((MethodDeclNode) memNode).getModifiers();
                int effect = ((MethodDeclNode) memNode).getEffect();
                String mod = Modifier.toString(modifiers & (~ELABORATE_MOD));

                if ((modifiers & (PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD)) == 0)
                    mod = "public " + mod;

                if ((modifiers & ELABORATE_MOD) != 0)
                    mod += "/* elaborate */ ";

                String eff = Effect.toString(effect);
                retList.addLast(mod);
                if (effect != NO_EFFECT)
                    retList.addLast("/* " + eff + "*/ ");
                retList.addLast(((MethodDeclNode) memNode).getReturnType()
                        .accept(this, args));
                retList.addLast(" ");
                retList.addLast(((MethodDeclNode) memNode).getName().accept(
                        this, args));

                retList.addLast("(");
                retList.addLast(_commaList(TNLManip.traverseList(this, args,
                        ((MethodDeclNode) memNode).getParams())));
                retList.addLast(");\n");
            } else
                retList.addLast(memNode.accept(this, args));
        }
        decreaseIndent();
        retList.addLast("}\n");

        finalList.addLast(retList);

        if (node.getParent() instanceof CompileUnitNode) {
            finalList.addFirst(node.getName().accept(this, args));
        }

        return finalList;
    }

    /**
     * Return the code represented by a <code>MethodDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER RETURNTYPE NAME (ARGS) { ... }</code>
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        // YW: 091703: beg
        // if (node.getEffect() == ELABORATE_EFFECT) return retList;
        // YW: 091703: end
        retList.addLast("\n");
        retList.addLast(indent(indentLevel));

        int modifiers = node.getModifiers();
        int effect = node.getEffect();
        String mod = Modifier.toString(modifiers & (~ELABORATE_MOD));
        if (node.getParent() instanceof InterfaceDeclNode) {
            // By default, functions defined in an interface are public.
            if ((node.getModifiers() & (PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD)) == 0)
                mod = "public " + mod;
        }
        if ((modifiers & ELABORATE_MOD) != 0)
            mod += "/* elaborate */ ";

        String eff = Effect.toString(effect);
        retList.addLast(mod);
        if (effect != NO_EFFECT)
            retList.addLast("/* " + eff + "*/ ");
        retList.addLast(node.getReturnType().accept(this, args));
        retList.addLast(" ");
        retList.addLast(node.getName().accept(this, args));

        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getParams())));
        retList.addLast(")");

        if ((node.getParent().classID() != MetaModelStaticSemanticConstants.CLASSDECLNODE_ID)
                && ((node.getParent() instanceof InterfaceDeclNode) || (((modifiers & ELABORATE_MOD) == 0) && ((modifiers & ABSTRACT_MOD) == 0)))) {
            retList.addLast("{\n");
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("throw new RuntimeException(\"Non-'elaborate' method "
                            + _stringListToString((List) node.getName().accept(
                                    this, args))
                            + " invoked during elaboration. These methods can"
                            + " only be invoked at runtime.\");\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("}\n");
        } else {
            if (node.getBody() == AbsentTreeNode.instance) {
                retList.addLast(";");
            } else {
                retList.addLast(" ");
                retList.addLast(node.getBody().accept(this, args));
            }
            retList.addLast("\n");
        }

        return retList;
    }

    /**
     * Return the code represented by a <code>ConstructorDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER NAME(ARGS) { ... }</code>
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(node.getName().getIdent());
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getParams())));
        retList.addLast(") ");

        // retList.addLast("throws CloneNotSupportedException");
        retList.addLast("{\n");
        increaseIndent();
        ClassDeclNode parcl = (ClassDeclNode) node.getParent();
        MetaModelDecl decl = MetaModelDecl.getDecl(node.getName());
        if ((TreeNode) node.getConstructorCall() != AbsentTreeNode.instance) // if
            // (parcl.getSuperClass()
            // !=
            // AbsentTreeNode.instance)
            retList.addLast(node.getConstructorCall().accept(this, args));
        else if (decl.getContainer().category != CG_CLASS
                && decl.getContainer().category != CG_QUANTITY) {
            LinkedList l = (LinkedList) node.getParams();
            if (l.size() > 0) {
                retList.addLast(indent(indentLevel + 1));
                retList.addLast("super(");
                ParameterNode p = (ParameterNode) l.getFirst();
                retList.addLast(p.getName().accept(this, args));
                retList.addLast(");\n");
            }
        }

        // xichen: skip the inner classes
        if (!(node.getParent().getParent() instanceof UserTypeDeclNode)) {
            if (sports != null) {
                if (sports.size() > 0) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("_generateScalarPortsInstances();\n");
                }
            }
        }

        String ntype;
        if (node.getParent() instanceof NetlistDeclNode) {
            ntype = "Netlist";
        } else {
            ntype = "Node";
        }

        if (decl.getContainer().category != CG_CLASS) {
            retList.addLast(indent(indentLevel));
            retList.addLast("try {\n");
            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("/* Work on the assumption that the netlist exists */\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("I" + ntype + " node = Network.net.get" + ntype
                    + "(this);\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("MMType type = Network.net.getType(\"");
            retList.addLast(MetaModelDecl.getDecl((NamedNode) node.getParent())
                    .fullName());
            retList.addLast("\");\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("node.castToSubType(type);\n");
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("} catch (RuntimeException e) {\n");
            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList
                    .addLast("/* The netlist does not exist in the network */\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("MMType type = Network.net.getType(\"");
            retList.addLast(MetaModelDecl.getDecl((NamedNode) node.getParent())
                    .fullName());
            retList.addLast("\");\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("I" + ntype + " node = new I" + ntype
                    + "(type, this, ");
            if (node.getParams().size() > 0)
                retList.addLast(((ParameterNode) (node.getParams().get(0)))
                        .getName().accept(this, args));
            else
                retList.addLast("\"\"");
            retList.addLast(", getObjectID());\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("Network.net.addNode(this, node);\n");
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("}\n");
        } else {
            if (node.getName().getIdent().equals("Node")) {
                TreeNode parent = parcl;
                while (!(parent instanceof CompileUnitNode))
                    parent = parent.getParent();
                PackageDecl pkg = (PackageDecl) parent.getProperty(PACKAGE_KEY);
                if (pkg.fullName().equals("metamodel.lang")) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("objectID = generateObjectID();\n");
                }
            }
        }
        decreaseIndent();

        List retValue = (List) node.getBody().accept(this, args);

        if (retValue.size() > 1) {
            // get rid of the first '{' and '\n' of the block node string
            retValue.remove(0);
        }
        if (retValue.size() > 0)
            retValue.remove(retValue.size() - 1);
        retList.addLast(retValue);
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>ThisConstructorCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>this(ARGS);</code>
     */
    public Object visitThisConstructorCallNode(ThisConstructorCallNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("this(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(");\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>SuperConstructorCall</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>super(ARGS);</code>
     */
    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("super(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(");\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>FieldDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER TYPE NAME = INITEXPR</code>
     */
    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node, args);
    }

    /**
     * Return the code represented by a <code>LocalVarDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER TYPE NAME = INITEXPR</code>
     */
    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        return _visitVarInitDeclNode(node, args);
    }

    /**
     * Return the code represented by an <code>ArrayInitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> { INITEXPR1, INITEXPR2, ... } </code>
     */
    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("{");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getInitializers())));
        retList.addLast("}");

        return retList;
    }

    /**
     * Return the code represented by a <code>BlockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> { STATEMENTS } </code>
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("{\n");

        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();

        // retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("}");

        return retList;
    }

    /**
     * Return the code represented by a <code>ParameterNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>MODIFIER TYPE NAME</code>
     */
    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(Modifier.toString(node.getModifiers()));

        retList.addLast(node.getDefType().accept(this, args));
        retList.addLast(" ");

        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by an <code>MethodCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> NAME(ARGS) </code>
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // System.out.println(node.getMethod().getClass().getName());
        /*
         * String methodStr =
         * _stringListToString((LinkedList)node.getMethod().accept(this, args));
         * if (node.getMethod() instanceof ObjectFieldAccessNode){ TreeNode
         * objNode = ((ObjectFieldAccessNode)node.getMethod()).getObject();
         * if ((objNode instanceof ThisPortAccessNode)|| (objNode instanceof
         * ObjectPortAccessNode)|| (objNode instanceof SuperPortAccessNode)) {
         * if (methodStr.lastIndexOf('.', methodStr.lastIndexOf('.')-1) < 0)
         * methodStr = "__pointer_" + methodStr; else { methodStr =
         * methodStr.substring (0, methodStr.lastIndexOf('.',
         * methodStr.lastIndexOf('.')-1)+1) + "__pointer_" + methodStr.substring
         * (methodStr.lastIndexOf('.', methodStr.lastIndexOf('.')-1)+1); } } }
         * retList.addLast(methodStr);
         */

        retList.addLast(node.getMethod().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>ExprStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>EXPR;</code>
     */
    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(";");
        retList.addLast("\n");
        while (args.size() > 0) {
            // retList.addLast(indent(indentLevel));
            retList.addLast(args.removeLast());
        }

        return retList;
    }

    /**
     * Return the code represented by an <code>EmptyStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>;</code>
     */
    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast(";");
        retList.addLast("\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>LabeledStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> LABEL: STATEMENTS </code>
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(": ");

        StatementNode next = node.getStmt();
        if (next instanceof BlockNode && !(next instanceof LabeledBlockNode)) {
            retList.addLast(next.accept(this, args));
            retList.addLast("\n");
        } else {
            retList.addLast("\n");
            increaseIndent();
            retList.addLast(next.accept(this, args));
            decreaseIndent();
        }

        return retList;
    }

    /**
     * Return the code represented by an <code>IfStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> if (CONDITION) THEN-PART else ELSE-PART </code>
     */
    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        TreeNode thenPart = (TreeNode) node.getThenPart();
        TreeNode elsePart = node.getElsePart();

        retList.addLast(indent(indentLevel));
        retList.addLast("if (");
        retList.addLast(node.getCondition().accept(this, args));
        retList.addLast(") ");

        if (thenPart instanceof BlockNode
                && !(thenPart instanceof LabeledBlockNode)) {
            retList.addLast(thenPart.accept(this, args));
        } else {
            retList.addLast("\n");
            increaseIndent();
            retList.addLast(thenPart.accept(this, args));
            decreaseIndent();
        }

        if (elsePart != AbsentTreeNode.instance) {
            if (!(thenPart instanceof BlockNode && !(elsePart instanceof LabeledBlockNode))) {
                retList.addLast(indent(indentLevel));
            } else {
                retList.addLast(" ");
            }

            if (elsePart instanceof BlockNode
                    && !(elsePart instanceof LabeledBlockNode)) {
                retList.addLast("else ");
                retList.addLast(elsePart.accept(this, args));
                retList.addLast("\n");
            } else {
                retList.addLast("else\n");
                increaseIndent();
                retList.addLast(elsePart.accept(this, args));
                decreaseIndent();
            }
        } else
            retList.addLast("\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>SwitchNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents:
     *         <code> switch(EXPR) { ... } </code>
     */
    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("switch (");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(") {\n");
        retList.addLast(_separateList(TNLManip.traverseList(this, args, node
                .getSwitchBlocks()), "\n"));
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>SwitchBranchNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> case EXPR: STATEMENTS; </code>
     */
    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        // case
        List retValue = TNLManip.traverseList(this, args, node.getCases());
        retList.addLast(retValue);

        int initIndent = indentLevel;
        int newIndent = indentOnce - 1 + _stringListToString(retValue).length();
        List stmtList = node.getStmts();

        // first statement
        if (stmtList.size() > 0) {
            TreeNode firstStmt = (TreeNode) stmtList.remove(0);
            if (firstStmt instanceof BlockNode
                    && !(firstStmt instanceof LabeledBlockNode)) {
                indentLevel = newIndent - indentOnce;
                retList.addLast(firstStmt.accept(this, args));
            } else {
                indentLevel = indentOnce - 1; // cancel out the space after
                // ":"
                retList.addLast(firstStmt.accept(this, args));
            }

            // rest of statements
            indentLevel = newIndent;
            retList.addLast(TNLManip.traverseList(this, args, stmtList));
            stmtList.add(0, firstStmt);
        }

        indentLevel = initIndent;

        return retList;
    }

    /**
     * Return the code represented by a <code>CaseNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> case EXPR: </code>
     */
    public Object visitCaseNode(CaseNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        if (node.getExpr() == AbsentTreeNode.instance) {
            retList.addLast(indent(indentLevel));
            retList.addLast("default: ");
        } else {
            retList.addLast(indent(indentLevel));
            retList.addLast("case ");
            retList.addLast(node.getExpr().accept(this, args));
            retList.addLast(": ");
        }

        return retList;
    }

    /**
     * Return the code represented by a <code>BreakNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> break LABEL; </code>
     *         where LABEL is optional
     */
    public Object visitBreakNode(BreakNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("break");

        if (node.getLabel() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.getLabel().accept(this, args));
        }
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>ContinueNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> continue LABEL; </code>
     *         where LABEL is optional
     */
    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("continue");

        if (node.getLabel() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.getLabel().accept(this, args));
        }
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>LoopNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> while (CONDITION) { ... }
     *                            or  do { ... } while (CONDITION) </code>
     */
    public Object visitLoopNode(LoopNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("\n");
        retList.addLast(indent(indentLevel));

        if (node.getForeStmt().classID() == EMPTYSTMTNODE_ID) {
            // while loop
            retList.addLast("while (");
            retList.addLast(node.getTest().accept(this, args));
            retList.addLast(") ");

            TreeNode stmt = node.getAftStmt();
            if (stmt instanceof BlockNode
                    && !(stmt instanceof LabeledBlockNode)) {
                retList.addLast(stmt.accept(this, args));
            } else {
                retList.addLast("\n");
                increaseIndent();
                retList.addLast(stmt.accept(this, args));
                decreaseIndent();
            }
            retList.addLast("\n");
        } else {
            // do loop
            retList.addLast("do ");

            TreeNode stmt = node.getForeStmt();
            if (stmt instanceof BlockNode
                    && !(stmt instanceof LabeledBlockNode)) {
                retList.addLast(stmt.accept(this, args));
                retList.addLast(" ");
            } else {
                retList.addLast("\n");
                increaseIndent();
                retList.addLast(stmt.accept(this, args));
                decreaseIndent();
                retList.addLast(indent(indentLevel));
            }
            retList.addLast("while (");
            retList.addLast(node.getTest().accept(this, args));
            retList.addLast(");\n");
        }
        return retList;
    }

    /**
     * Return the code represented by a <code>ForNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> for (INITEXPR; TEST; UPDATE) { ... } </code>
     */
    public Object visitForNode(ForNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(indent(indentLevel));

        retList.addLast("for (");
        retList.addLast(_forInitStringList(node.getInit(), args));
        retList.addLast("; ");
        retList.addLast(node.getTest().accept(this, args));
        retList.addLast("; ");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getUpdate())));
        retList.addLast(") ");

        StatementNode stmt = node.getStmt();
        if (stmt instanceof BlockNode && !(stmt instanceof LabeledBlockNode)) {
            retList.addLast(stmt.accept(this, args));
        } else {
            retList.addLast("\n");
            increaseIndent();
            retList.addLast(stmt.accept(this, args));
            decreaseIndent();
        }
        retList.addLast("\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>AssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 = EXPR2 </code>
     */
    public Object visitAssignNode(AssignNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" = ");
        retList.addLast(node.getExpr2().accept(this, args));

        if (vports != null) {
            if (node.getExpr1() instanceof ThisPortAccessNode) {
                String pName = ((ThisPortAccessNode) node.getExpr1()).getName()
                        .getIdent();
                // if ( (node.getExpr2() instanceof AllocateArrayNode) &&
                // (vports.containsKey(pName)) ) {
                if ((vports.containsKey(pName))) {
                    if ((node.getExpr2() instanceof AllocateArrayNode)) {
                        String assign = _stringListToString(retList).trim();
                        int index = assign.indexOf(" new ");
                        assign = assign.substring(index + 5);
                        String portType = assign.substring(0, assign
                                .indexOf('['));
                        int begIdx, endIdx;
                        String separator = "";
                        String init = "";
                        int initDim = 0;
                        String idxstr = "";
                        String dim = "";
                        while ((begIdx = assign.indexOf('[')) >= 0) {
                            endIdx = assign.indexOf(']');
                            idxstr += separator;
                            separator = "+";
                            idxstr += "\"[\"+";
                            dim = assign.substring(begIdx + 1, endIdx);
                            String arg = "_index_" + initDim;
                            init += indent(indentLevel);
                            init += ("for (int " + arg + "=0; " + arg + "<"
                                    + dim + "; " + arg + "++)\n");
                            initDim++;
                            idxstr += dim;
                            idxstr += "+\"]\"";
                            assign = assign.substring(endIdx + 1);
                        }

                        String s = indent(indentLevel);
                        s += "Network.net.instantiatePortArray(this, \"";
                        s += pName;
                        s += "\", ";
                        s += idxstr;
                        s += ");\n";

                        increaseIndent();
                        init += indent(indentLevel);
                        decreaseIndent();
                        init += pName;
                        for (int i = 0; i < initDim; i++)
                            init += "[_index_" + i + "]";
                        init += " = new " + portType + "();\n";
                        s += init;

                        // xichen: instantiate port pointers
                        assign = _stringListToString(retList).trim();
                        String instStr = indent(indentLevel)
                                + "__pointer_"
                                + assign.substring(0,
                                        assign.indexOf(" new ") + 5)
                                + "__interface_"
                                + assign.substring(assign.indexOf(" new ") + 5)
                                + ";\n";
                        s = instStr + s;

                        args.addFirst(s);
                    } else if (node.getExpr2() instanceof NullPntrNode) {
                        retList.removeLast();
                        int dim = ((Integer) vportsDim.get(pName)).intValue();
                        String idxstr = "";
                        for (int i = 0; i < dim; i++)
                            idxstr += "[0]";
                        retList.addLast("new ");
                        retList.addLast(vports.get(pName));
                        retList.addLast(idxstr);

                        String s = indent(indentLevel);
                        s += "Network.net.instantiatePortArray(this, \"";
                        s += pName;
                        s += "\", \"";
                        s += idxstr;
                        s += "\");\n";

                        // xichen: instantiate port pointers
                        String assign = _stringListToString(retList).trim();
                        String instStr = indent(indentLevel)
                                + "__pointer_"
                                + assign.substring(0,
                                        assign.indexOf(" new ") + 5)
                                + "__interface_"
                                + assign.substring(assign.indexOf(" new ") + 5)
                                + ";\n";
                        s = instStr + s;

                        args.addFirst(s);
                    }
                }
            }
        }

        return retList;
    }

    /**
     * Return the code represented by an <code>AbsentTreeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list containing a null string
     */
    public Object visitAbsentTreeNode(AbsentTreeNode node, LinkedList args) {
        return TNLManip.addFirst("");
    }

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
            if (args.size() > 0)
                if (args.getLast() instanceof String)
                    if (((String) args.getLast()).equals("loc_checker")) {
                        LinkedList retList1 = new LinkedList();
                        retList1.addLast("\") + ");
                        retList1.addLast(retList);
                        retList1.addLast(" + new String(\"");
                        return retList1;
                    }

            /*
             * MetaModelDecl objectDecl = MetaModelDecl.getDecl(node.getName());
             * TypeNode type = null; if (objectDecl instanceof TypedDecl) type =
             * ((TypedDecl)objectDecl).getType();
             *
             * if (type instanceof EventTypeNode && isInConstraint &&
             * args.size() > 0) { if (args.getLast() instanceof LinkedList) {
             * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
             * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addEvent(");
             * ((LinkedList)args.getLast()).addLast(retList);
             * ((LinkedList)args.getLast()).addLast(");\n");
             * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
             * ((LinkedList)args.getLast()).addLast(retList);
             * ((LinkedList)args.getLast()).addLast(".addUsed(_constraint_type);\n"); } }
             */
        }

        return retList;
    }

    /**
     * Return the code represented by a <code>ReturnNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> return EXPR; </code>
     */
    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("return ");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>CastNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> (CASTTYPE) EXPR </code>
     */
    public Object visitCastNode(CastNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
                args);

        retList.addLast("(");
        retList.addLast(node.getDtype().accept(this, args));
        retList.addLast(") ");
        retList.addLast(_parenExpr(node.getExpr(), exprStringList));

        return retList;
    }

    /**
     * Return the code represented by a <code>NullPntrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> null </code>
     */
    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return TNLManip.addFirst("null");
    }

    /**
     * Return the code represented by a <code>ThisNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>this</code>
     */
    public Object visitThisNode(ThisNode node, LinkedList args) {
        return TNLManip.addFirst("this");
    }

    /**
     * Returns the code represented by an <code>AllocateNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> ENCLOSINGINSTANCE.new DTYPE (ARGS) </code>
     */
    public Object visitAllocateNode(AllocateNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode enclosingInstance = node.getEnclosingInstance();

        int enclosingID = enclosingInstance.classID();
        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) {

            LinkedList enclosingStringList = (LinkedList) node
                    .getEnclosingInstance().accept(this, args);

            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        retList.addLast("new ");
        retList.addLast(node.getDtype().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by <code>AllocateAnonymousClassNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>new SUPERTYPE(SUPERARGS) { ... }   or
     *                ENCLOSINGINSTANCE.new SUPERTYPE(SUPERARGS) { ... } </code>
     */
    public Object visitAllocateAnonymousClassNode(
            AllocateAnonymousClassNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode enclosingInstance = node.getEnclosingInstance();
        int enclosingID = enclosingInstance.classID();

        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) {

            LinkedList enclosingStringList = (LinkedList) enclosingInstance
                    .accept(this, args);
            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        retList.addLast("new ");
        retList.addLast(node.getSuperType().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getSuperArgs())));
        retList.addLast(") {\n");

        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}");

        return retList;
    }

    /**
     * Return the code represented by an <code>AllocateArrayNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> new TYPE[][].. </code>
     */
    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("new ");
        retList.addLast(node.getDtype().accept(this, args));

        List dimExprList = TNLManip
                .traverseList(this, args, node.getDimExprs());
        Iterator dimExprItr = dimExprList.iterator();

        while (dimExprItr.hasNext()) {
            retList.addLast("[");
            retList.addLast(dimExprItr.next());
            retList.addLast("]");
        }

        for (int dimsLeft = node.getDims(); dimsLeft > 0; dimsLeft--) {
            retList.addLast("[]");
        }

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.getInitExpr().accept(this, args));
        }

        return retList;
    }

    /**
     * Return the code represented by an <code>ArrayAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> ARRAYNAME[INDEX] </code>
     */
    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        LinkedList arrayStringList = (LinkedList) node.getArray().accept(this,
                args);

        retList.addLast(_parenExpr(node.getArray(), arrayStringList));
        retList.addLast("[");
        retList.addLast(node.getIndex().accept(this, args));
        retList.addLast("]");

        if (!(node.getParent() instanceof ArrayAccessNode)) {
            if (args.size() > 0)
                if (args.getLast() instanceof String)
                    if (((String) args.getLast()).equals("loc_checker")) {
                        LinkedList retList1 = new LinkedList();
                        retList1.addLast("\") + ");
                        retList1.addLast(retList);
                        retList1.addLast(" + new String(\"");
                        return retList1;
                    }

            /*
             * TypeNode type; try { type = (TypeNode)node.accept(new
             * TypeVisitor(), null); } catch (NullPointerException e) { type =
             * null; }
             *
             * if (type instanceof EventTypeNode && isInConstraint &&
             * args.size() > 0) { if (args.getLast() instanceof LinkedList) {
             * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
             * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addEvent(");
             * ((LinkedList)args.getLast()).addLast(retList);
             * ((LinkedList)args.getLast()).addLast(");\n");
             * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
             * ((LinkedList)args.getLast()).addLast(retList);
             * ((LinkedList)args.getLast()).addLast(".addUsed(_constraint_type);\n"); } }
             */
        }

        return retList;
    }

    /**
     * Return the code represented by an <code>ObjectFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>OBJECT.NAME </code>
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        LinkedList objectStringList = (LinkedList) node.getObject().accept(
                this, args);

        retList.addLast(_parenExpr(node.getObject(), objectStringList));
        retList.addLast(".");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by a <code>SuperFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> super.NAME </code>
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("super.");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by a <code>TypeFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> Type.FIELDNAME </code>
     */
    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getFType().accept(this, args));
        retList.addLast(".");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by a <code>ThisFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>this.NAME</code>
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        MetaModelDecl decl = MetaModelDecl.getDecl(node.getName());
        if ((decl.getModifiers() & STATIC_MOD) == 0) {
            retList.addLast("this.");
        }
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by a <code>TypeClassAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> FIELDTYPE.class </code>
     */
    public Object visitTypeClassAccessNode(TypeClassAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getFType().accept(this, args));
        retList.addLast(".class");

        return retList;
    }

    /**
     * Return the code represented by a <code>OuterThisAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> TYPE.this </code>
     */
    public Object visitOuterThisAccessNode(OuterThisAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getType().accept(this, args));
        retList.addLast(".this");

        return retList;
    }

    /**
     * Return the code represented by a <code>OuterSuperAccess</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> TYPE.super </code>
     */
    public Object visitOuterSuperAccess(OuterSuperAccessNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getType().accept(this, args));
        retList.addLast(".super");

        return retList;
    }

    /**
     * Return the code represented by a <code>IntLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents the integer
     */
    public Object visitIntLitNode(IntLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    /**
     * Return the code represented by a <code>LongLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents the long integer
     */
    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    /**
     * Return the code represented by a <code>FloatLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents the float
     */
    public Object visitFloatLitNode(FloatLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    /**
     * Return the code represented by a <code>DoubleLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents the double
     */
    public Object visitDoubleLitNode(DoubleLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    /**
     * Return the code represented by a <code>BoolLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>true</code> or
     *         <code>false</code>
     */
    public Object visitBoolLitNode(BoolLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    /**
     * Return the code represented by a <code>CharLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents '<code>CHARACTER</code>'
     */
    public Object visitCharLitNode(CharLitNode node, LinkedList args) {
        return TNLManip.addFirst("'" + node.getLiteral() + '\'');
    }

    /**
     * Return the code represented by a <code>StringLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents "<code>STRING</code>"
     */
    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return TNLManip.addFirst("\"" + node.getLiteral() + '\"');
    }

    /**
     * Return the code represented by a <code>BoolTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>boolean</code>
     */
    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return TNLManip.addFirst("boolean");
    }

    /**
     * Return the code represented by a <code>CharTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>char</code>
     */
    public Object visitCharTypeNode(CharTypeNode node, LinkedList args) {
        return TNLManip.addFirst("char");
    }

    /**
     * Return the code represented by a <code>ByteTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents: byte
     */
    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return TNLManip.addFirst("byte");
    }

    /**
     * Return the code represented by a <code>ShortTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents: short
     */
    public Object visitShortTypeNode(ShortTypeNode node, LinkedList args) {
        return TNLManip.addFirst("short");
    }

    /**
     * Return the code represented by an <code>IntTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>int</code>
     */
    public Object visitIntTypeNode(IntTypeNode node, LinkedList args) {
        return TNLManip.addFirst("int");
    }

    /**
     * Return the code represented by a <code>FloatTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>float</code>
     */
    public Object visitFloatTypeNode(FloatTypeNode node, LinkedList args) {
        return TNLManip.addFirst("float");
    }

    /**
     * Return the code represented by a <code>LongTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>long</code>
     */
    public Object visitLongTypeNode(LongTypeNode node, LinkedList args) {
        return TNLManip.addFirst("long");
    }

    /**
     * Return the code represented by a <code>DoubleTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>double</code>
     */
    public Object visitDoubleTypeNode(DoubleTypeNode node, LinkedList args) {
        return TNLManip.addFirst("double");
    }

    // /////////////Constraints
    /**
     * Return the code represented by a <code>EventTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>event</code>
     */
    public Object visitEventTypeNode(EventTypeNode node, LinkedList args) {
        return TNLManip.addFirst("Event");
    }

    /**
     * Return the code represented by a <code>ActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> all </code>
     *                         <code> object.name </code>
     */
    /*
     * public Object visitActionNode(ActionNode node, LinkedList args) {
     * LinkedList retList = new LinkedList();
     *
     * TreeNode obj = node.getObject(); TreeNode name = node.getName();
     *
     * //retList.addLast(obj.getClass().getName() + "\n");
     * //retList.addLast(name.getClass().getName() + "\n");
     *
     * if ((obj == AbsentTreeNode.instance) && (name ==
     * AbsentTreeNode.instance)) retList.addLast("all"); else {
     * retList.addLast(obj.accept(this, args)); retList.addLast(".");
     * retList.addLast(name.accept(this, args)); }
     *
     * return retList; }
     */

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
        //LinkedList codeList = new LinkedList();
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

        /*
         * if (isInConstraint && args.size() > 0) { if (args.getLast()
         * instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addEvent(");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(");\n");
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(".addUsed(_constraint_type);\n"); } }
         */

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

        /*
         * if (isInConstraint && args.size() > 0) { if (args.getLast()
         * instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addEvent(");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(");\n");
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(".addUsed(_constraint_type);\n"); } }
         */

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

        /*
         * if (isInConstraint && args.size() > 0) { if (args.getLast()
         * instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addEvent(");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(");\n");
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(".addUsed(_constraint_type);\n"); } }
         */

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

        /*
         * if (isInConstraint && args.size() > 0) { if (args.getLast()
         * instanceof LinkedList) {
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast("__tmpConstraint.addEvent(");
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(");\n");
         * ((LinkedList)args.getLast()).addLast(indent(indentLevel));
         * ((LinkedList)args.getLast()).addLast(retList);
         * ((LinkedList)args.getLast()).addLast(".addUsed(_constraint_type);\n"); } }
         */

        return retList;
    }

    // /////////////Constraints

    /**
     * Return the code represented by a <code>VoidTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>void</code>
     */
    public Object visitVoidTypeNode(VoidTypeNode node, LinkedList args) {
        return TNLManip.addFirst("void");
    }

    /**
     * Return the code represented by a <code>TypeNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>TYPENAME</code>
     */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return node.getName().accept(this, args);
    }

    /**
     * Return the code represented by an <code>ArrayTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>BASETYPE[]</code>
     */
    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(node.getBaseType().accept(this, args));
        retList.addLast("[]");

        return retList;
    }

    /**
     * Return the code represented by a <code>PostIncrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>EXPR++</code>
     */
    public Object visitPostIncrNode(PostIncrNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "++", true);
    }

    /**
     * Return the code represented by a <code>PostDecrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>EXPR--</code>
     */
    public Object visitPostDecrNode(PostDecrNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "--", true);
    }

    /**
     * Return the code represented by a <code>UnaryPlusNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>+EXPR</code>
     */
    public Object visitUnaryPlusNode(UnaryPlusNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "+", false);
    }

    /**
     * Return the code represented by a <code>UnaryMinusNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>-EXPR</code>
     */
    public Object visitUnaryMinusNode(UnaryMinusNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "-", false);
    }

    /**
     * Return the code represented by a <code>PreIncrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>++EXPR</code>
     */
    public Object visitPreIncrNode(PreIncrNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "++", false);
    }

    /**
     * Return the code represented by a <code>PreDecrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>--EXPR</code>
     */
    public Object visitPreDecrNode(PreDecrNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "--", false);
    }

    /**
     * Return the code represented by a <code>ComplementNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>~EXPR</code>
     */
    public Object visitComplementNode(ComplementNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "~", false);
    }

    /**
     * Return the code represented by a <code>NotNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>!EXPR</code>
     */
    public Object visitNotNode(NotNode node, LinkedList args) {
        return _visitSingleExprNode(node, args, "!", false);
    }

    /**
     * Return the code represented by a <code>MultNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 * EXPR2 </code>
     */
    public Object visitMultNode(MultNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "*");
    }

    /**
     * Return the code represented by a <code>DivNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 / EXPR2 </code>
     */
    public Object visitDivNode(DivNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "/");
    }

    /**
     * Return the code represented by a <code>RemNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 % EXPR2 </code>
     */
    public Object visitRemNode(RemNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "%");
    }

    /**
     * Return the code represented by a <code>PlusNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 + EXPR2 </code>
     */
    public Object visitPlusNode(PlusNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "+");
    }

    /**
     * Return the code represented by a <code>MinusNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 - EXPR2 </code>
     */
    public Object visitMinusNode(MinusNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "-");
    }

    /**
     * Return the code represented by a <code>LeftShiftLogNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 << EXPR2 </code>
     */
    public Object visitLeftShiftLogNode(LeftShiftLogNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "<<");
    }

    /**
     * Return the code represented by a <code>RightShiftLogNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >>> EXPR2 </code>
     */
    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        // return _visitBinaryOpNode(node, ">>>");
        return _visitBinaryOpNode(node, args, ">>");
    }

    /**
     * Return the code represented by a <code>RightShiftArithNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >> EXPR2 </code>
     */
    public Object visitRightShiftArithNode(RightShiftArithNode node,
            LinkedList args) {
        return _visitBinaryOpNode(node, args, ">>");
    }

    /**
     * Return the code represented by a <code>LTNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 < EXPR2 </code>
     */
    public Object visitLTNode(LTNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "<");
    }

    /**
     * Return the code represented by a <code>GTNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 > EXPR2 </code>
     */
    public Object visitGTNode(GTNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, ">");
    }

    /**
     * Return the code represented by a <code>LENode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 <= EXPR2 </code>
     */
    public Object visitLENode(LENode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "<=");
    }

    /**
     * Return the code represented by a <code>GENode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >= EXPR2 </code>
     */
    public Object visitGENode(GENode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, ">=");
    }

    /**
     * Return the code represented by an <code>InstanceOfNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> EXPR instanceof DTYPE </code>
     */
    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
                args);

        retList.addLast(_parenExpr(node.getExpr(), exprStringList));
        retList.addLast(" instanceof ");
        retList.addLast(node.getDtype().accept(this, args));

        return retList;
    }

    /**
     * Return the code represented by a <code>EQNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 == EXPR2 </code>
     */
    public Object visitEQNode(EQNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "==");
    }

    /**
     * Return the code represented by a <code>NENode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 != EXPR2 </code>
     */
    public Object visitNENode(NENode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "!=");
    }

    /**
     * Return the code represented by a <code>BitAndNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 & EXPR2 </code>
     */
    public Object visitBitAndNode(BitAndNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "&");
    }

    /**
     * Return the code represented by a <code>BitOrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 | EXPR2 </code>
     */
    public Object visitBitOrNode(BitOrNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "|");
    }

    /**
     * Return the code represented by a <code>BitXorNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 ^ EXPR2 </code>
     */
    public Object visitBitXorNode(BitXorNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "^");
    }

    /**
     * Return the code represented by a <code>CandNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 && EXPR2 </code>
     */
    public Object visitCandNode(CandNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "&&");
    }

    /**
     * Return the code represented by a <code>CorNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 || EXPR2 </code>
     */
    public Object visitCorNode(CorNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, "||");
    }

    /**
     * Return the code represented by a <code>IfExprNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> EXPR1 ? EXPR2 : EXPR3 </code>
     */
    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        LinkedList e1StringList = (LinkedList) node.getExpr1().accept(this,
                args);
        LinkedList e2StringList = (LinkedList) node.getExpr2().accept(this,
                args);
        LinkedList e3StringList = (LinkedList) node.getExpr3().accept(this,
                args);

        e1StringList = _parenExpr(node.getExpr1(), e1StringList);
        e2StringList = _parenExpr(node.getExpr2(), e2StringList);
        e3StringList = _parenExpr(node.getExpr3(), e3StringList);

        retList.addLast(e1StringList);
        retList.addLast(" ? ");
        retList.addLast(e2StringList);
        retList.addLast(" : ");
        retList.addLast(e3StringList);

        return retList;
    }

    /**
     * Return the code represented by a <code>MultAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 *= EXPR2 </code>
     */
    public Object visitMultAssignNode(MultAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "*=");
    }

    /**
     * Return the code represented by a <code>DivAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 /= EXPR2 </code>
     */
    public Object visitDivAssignNode(DivAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "/=");
    }

    /**
     * Return the code represented by a <code>RemAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 %= EXPR2 </code>
     */
    public Object visitRemAssignNode(RemAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "%=");
    }

    /**
     * Return the code represented by a <code>PlusAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 += EXPR2 </code>
     */
    public Object visitPlusAssignNode(PlusAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "+=");
    }

    /**
     * Return the code represented by a <code>MinusAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 -= EXPR2 </code>
     */
    public Object visitMinusAssignNode(MinusAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "-=");
    }

    /**
     * Return the code represented by a <code>LeftShiftLogAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 <<= EXPR2 </code>
     */
    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node,
            LinkedList args) {
        // return _visitBinaryOpAssignNode(node, "<<=");
        return _visitBinaryOpAssignNode(node, args, "=");
    }

    /**
     * Return the code represented by a <code>RightShiftLogAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >>>= EXPR2 </code>
     */
    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node,
            LinkedList args) {
        // return _visitBinaryOpAssignNode(node, ">>>=");
        return _visitBinaryOpAssignNode(node, args, ">>=");
    }

    /**
     * Return the code represented by a <code>RightShiftArithAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >>= EXPR2 </code>
     */
    public Object visitRightShiftArithAssignNode(
            RightShiftArithAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, ">>=");
    }

    /**
     * Return the code represented by a <code>BitAndAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 &= EXPR2 </code>
     */
    public Object visitBitAndAssignNode(BitAndAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "&=");
    }

    /**
     * Return the code represented by a <code>BitXorAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 ^= EXPR2 </code>
     */
    public Object visitBitXorAssignNode(BitXorAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "^=");
    }

    /**
     * Return the code represented by a <code>BitOrAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 |= EXPR2 </code>
     */
    public Object visitBitOrAssignNode(BitOrAssignNode node, LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "|=");
    }

    // //////////////////////////////////////////////////////////////
    // Newly added Nodetypes for supporting //
    // mapped behavior and constraints syntaxes //
    // //////////////////////////////////////////////////////////////

    /**
     * Return the code represented by a <code>SpecialLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> LAST or retval </code>
     */
    public Object visitSpecialLitNode(SpecialLitNode node, LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast(node.getLiteral());
        return retList;
    }

    /**
     * Return the code represented by a <code>GetComponentNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getcomponent(netlist, name) </code>
     */
    public Object visitGetComponentNode(GetComponentNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("Network.net.getComponent(");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetConnectionDestNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getconnectiondest(node, port) </code>
     */
    public Object visitGetConnectionDestNode(GetConnectionDestNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.getConnectionDest(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(_visitPortExpr(node.getPort(), args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetNthPortNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getnthport(node, ifName, num) </code>
     */
    public Object visitGetNthPortNode(GetNthPortNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("Network.net.getNthPort(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", \"");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast("\", ");
        retList.addLast(node.getNum().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetPortNumNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getportnum(node, ifName) </code>
     */
    public Object visitGetPortNumNode(GetPortNumNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("Network.net.getPortNum(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetScopeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getscope(node, port) </code>
     */
    public Object visitGetScopeNode(GetScopeNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("Network.net.getScope(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(_visitPortExpr(node.getPort(), args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetThreadNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> getthread() </code>
     */
    public Object visitGetThreadNode(GetThreadNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("getthread()");

        return retList;
    }

    /**
     * Return the code represented by a <code>ActionLabelStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> LabelName {&#64; stmt &#64;} </code>
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        // retList.addLast(indent(indentLevel));
        // retList.addLast(node.getLabel().accept(this, args));
        // retList.addLast("{@\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();
        // retList.addLast(indent(indentLevel));
        // retList.addLast("@}\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>ActionLabelExprNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> LabelName {&#64; expr &#64;} </code>
     */
    public Object visitActionLabelExprNode(ActionLabelExprNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        // retList.addLast(node.getLabel().accept(this, args));
        // retList.addLast("{@ ");
        retList.addLast(node.getExpr().accept(this, args));
        // retList.addLast(" @}");

        return retList;
    }

    /**
     * Return the code represented by a <code>AnnotationNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> {$ stmts $} </code>
     */
    public Object visitAnnotationNode(AnnotationNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("{$\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("$}\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>BeginAnnotationNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> beg{ stmts } </code>
     */
    public Object visitBeginAnnotationNode(BeginAnnotationNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("beg{\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>EndAnnotationNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> end{ stmts } </code>
     */
    public Object visitEndAnnotationNode(EndAnnotationNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("end{\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetInstNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getinstname(expr) </code>
     */
    public Object visitGetInstNameNode(GetInstNameNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.getInstName(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>GetCompNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getcompname(expr) </code>
     */
    public Object visitGetCompNameNode(GetCompNameNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast("Network.net.getCompName(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Number of spaces for the current indent level. */
    protected int indentLevel = 0;

    /** Number of spaces for the previous indent level. */
    protected int lastIndentLevel = 0;

    /** Number of spaces for each indentation. */
    protected final int indentOnce = 4;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////
    /**
     * Generate _NodeType variable for Metamodel objects. Generate _NodeType
     * initialization and access functions.
     *
     * @param node
     *            node in ast
     * @param args
     *            args from visitors
     * @return a piece of codes in Java
     */
    /*
     * protected Object _genNodeType(TreeNode node, LinkedList args) {
     * LinkedList retList = new LinkedList(); LinkedList midValue = new
     * LinkedList(); List retValue = null; NameNode nn = null; TreeNode tnn =
     * null; List intfc = null; String modifier = new String(" "); String kind =
     * null;
     *
     * if (node instanceof InterfaceDeclNode) { kind = new String("int kind =
     * NodeType.INTERFACE;\n"); nn = ((InterfaceDeclNode) node).getName(); intfc =
     * ((InterfaceDeclNode) node).getInterfaces(); modifier = new String("
     * static "); } else if (node instanceof NetlistDeclNode) { kind = new
     * String("int kind = NodeType.NETLIST;\n"); nn = ((NetlistDeclNode)
     * node).getName(); tnn = ((NetlistDeclNode) node).getSuperClass(); intfc =
     * ((NetlistDeclNode) node).getInterfaces(); } else if (node instanceof
     * ProcessDeclNode) { kind = new String("int kind = NodeType.PROCESS;\n");
     * nn = ((ProcessDeclNode) node).getName(); tnn = ((ProcessDeclNode)
     * node).getSuperClass(); } else if (node instanceof MediumDeclNode) { kind =
     * new String("int kind = NodeType.MEDIUM;\n"); nn = ((MediumDeclNode)
     * node).getName(); tnn = ((MediumDeclNode) node).getSuperClass(); intfc =
     * ((MediumDeclNode) node).getInterfaces(); } else if (node instanceof
     * SchedulerDeclNode) { kind = new String("int kind =
     * NodeType.SCHEDULER;\n"); nn = ((SchedulerDeclNode) node).getName(); tnn =
     * ((SchedulerDeclNode) node).getSuperClass(); } else if (node instanceof
     * SMDeclNode) { kind = new String("int kind = NodeType.STATEMEDIUM;\n"); nn =
     * ((SMDeclNode) node).getName(); tnn = ((SMDeclNode) node).getSuperClass();
     * intfc = ((SMDeclNode) node).getInterfaces(); } else if (node instanceof
     * ClassDeclNode) { kind = new String("int kind = NodeType.CLASS;\n"); nn =
     * ((ClassDeclNode) node).getName(); tnn = ((ClassDeclNode)
     * node).getSuperClass(); intfc = ((ClassDeclNode) node).getInterfaces(); }
     * else { throw new RuntimeException("Unknown Node Type."); }
     *
     *
     * if (!(node instanceof InterfaceDeclNode)) {
     * retList.addLast(indent(indentLevel)); retList.addLast("private boolean
     * _inConstructor = false;\n"); } retList.addLast(indent(indentLevel));
     * retList.addLast("public"+modifier); if (node instanceof NetlistDeclNode)
     * retList.addLast("NetlistInst "); else retList.addLast("NodeInst ");
     * retList.addLast("_ObjectInst = _createObjectInst();\n\n");
     *
     *
     * retList.addLast(indent(indentLevel));
     * retList.addLast("private"+modifier); if (node instanceof NetlistDeclNode)
     * retList.addLast("NetlistInst "); else retList.addLast("NodeInst ");
     * retList.addLast("_createObjectInst() {\n"); increaseIndent();
     * retList.addLast(indent(indentLevel)); retList.addLast(kind);
     * retList.addLast(indent(indentLevel)); retList.addLast("String name = new
     * String(\""); retList.addLast(nn.accept(this, args));
     * retList.addLast("\");\n"); retList.addLast(indent(indentLevel));
     * retList.addLast("NodeType superClass = "); if (tnn != null) { if (!(tnn
     * instanceof AbsentTreeNode)) {
     * retList.addLast("super._ObjectInst.getType();\n"); } else {
     * retList.addLast("null;\n"); } } else { retList.addLast("null;\n"); }
     *
     * if (intfc!=null && !intfc.isEmpty()) { retValue =
     * TNLManip.traverseList(this, args, intfc); Iterator iter =
     * retValue.iterator(); while (iter.hasNext()) { LinkedList l = new
     * LinkedList(); l.add(iter.next()); String s = _stringListToString(l);
     *
     * if (!s.equals("Port")) { midValue.addLast(indent(indentLevel));
     * midValue.addLast("superIfs.add(_"); midValue.addLast(s);
     * midValue.addLast("._ObjectInst);\n"); } } }
     *
     * retList.addLast(indent(indentLevel)); if
     * (_stringListToString(midValue).trim().length()>0)
     * retList.addLast("LinkedList superIfs = new LinkedList();\n"); else
     * retList.addLast("LinkedList superIfs = null;\n");
     *
     * retList.addLast(midValue); retList.addLast(indent(indentLevel));
     * retList.addLast("return ("); if (node instanceof NetlistDeclNode)
     * retList.addLast("new NetlistInst("); else retList.addLast("new
     * NodeInst("); retList.addLast("new NodeType(name, kind, superClass,
     * superIfs)));\n"); decreaseIndent(); retList.addLast(indent(indentLevel));
     * retList.addLast("}\n\n");
     *
     * return retList; }
     */

    /**
     * Separates items in the list with a comma.
     *
     * @param stringList
     *            a list in which items should be separated by a comma
     * @return a list of items separated by a comma
     */
    protected static LinkedList _commaList(List stringList) {
        return _separateList(stringList, ", ");
    }

    /**
     * Separates items in the list with a semicolon.
     *
     * @param stringList
     *            a list in which items should be separated by a semicolon
     * @return a list of items separated by a semicolon
     */
    protected static LinkedList _semicolonList(List stringList) {
        return _separateList(stringList, "; ");
    }

    /**
     * Separates items in the list with the argument separator.
     *
     * @param stringList
     *            a list in which items should be separated by the other
     *            argument
     * @param separator
     *            a string that would be used to separate items in the list
     * @return a list of items separated by the separator
     */
    protected static LinkedList _separateList(List stringList, String separator) {
        Iterator stringListItr = stringList.iterator();
        LinkedList retList = new LinkedList();

        while (stringListItr.hasNext()) {
            retList.addLast(stringListItr.next());
            if (stringListItr.hasNext()) {
                retList.addLast(separator);
            }
        }
        return retList;
    }

    /**
     * Return the code represented by a variable declaration.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents: MODIFIER TYPE NAME = INITEXPR
     */
    protected LinkedList _visitVarInitDeclNode(VarInitDeclNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        /*
         * TreeNode atn = (TreeNode) node.getDefType(); if (atn instanceof
         * ArrayTypeNode) { while ((atn=((ArrayTypeNode) atn).getBaseType())
         * instanceof ArrayTypeNode); } if ( (atn instanceof TypeNameNode) &&
         * (args.get(0).equals("Netlist")) ) {
         * retList.addLast(indent(indentLevel)); retList.addLast("if
         * ("+((TypeNameNode)atn).getName().getIdent()+"._NodeType.getKind()");
         * retList.addLast(" == NETLIST) {\n"); increaseIndent();
         * retList.addLast(indent(indentLevel)); retList.addLast("NetlistInst");
         * retList.addLast(node.getDefType().accept(this, args));
         * retList.addLast(" " + node.getName().getIdent());
         *
         * if (node.getInitExpr() != AbsentTreeNode.instance) { args.addLast(new
         * String("NetlistInst")); retList.addLast(" = ");
         * retList.addLast(node.getInitExpr().accept(this, args));
         * args.removeLast(); } retList.addLast(";\n"); decreaseIndent();
         * retList.addLast(indent(indentLevel)); retList.addLast("} else {\n");
         * increaseIndent(); retList.addLast(indent(indentLevel));
         * retList.addLast("NodeInst");
         * retList.addLast(node.getDefType().accept(this, args));
         * retList.addLast(" " + node.getName().getIdent());
         *
         * if (node.getInitExpr() != AbsentTreeNode.instance) { args.addLast(new
         * String("NodeInst")); retList.addLast(" = ");
         * retList.addLast(node.getInitExpr().accept(this, args));
         * args.removeLast(); } retList.addLast(";\n"); decreaseIndent();
         * retList.addLast(indent(indentLevel)); retList.addLast("}\n"); } else {
         */
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));

        retList.addLast(node.getDefType().accept(this, args));
        retList.addLast(" " + node.getName().getIdent());

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" = ");
            retList.addLast(node.getInitExpr().accept(this, args));
        }
        retList.addLast(";\n");
        // }

        return retList;
    }

    /**
     * FIXME: what does it do????
     *
     * @param expr
     *            a tree node
     * @param exprStrList
     *            a list of strings
     * @return a list of strings
     */
    protected static LinkedList _parenExpr(TreeNode expr, LinkedList exprStrList) {
        int classID = expr.classID();

        switch (classID) {
        case INTLITNODE_ID:
        case LONGLITNODE_ID:
        case FLOATLITNODE_ID:
        case DOUBLELITNODE_ID:
        case BOOLLITNODE_ID:
        case CHARLITNODE_ID:
        case STRINGLITNODE_ID:
        case BOOLTYPENODE_ID:
        case ARRAYINITNODE_ID:
        case NULLPNTRNODE_ID:
        case THISNODE_ID:
        case ARRAYACCESSNODE_ID:
        case OBJECTNODE_ID:
        case OBJECTFIELDACCESSNODE_ID:
        case SUPERFIELDACCESSNODE_ID:
        case THISFIELDACCESSNODE_ID:
        case TYPECLASSACCESSNODE_ID:
        case OUTERTHISACCESSNODE_ID:
        case OUTERSUPERACCESSNODE_ID:
        case METHODCALLNODE_ID:
        case ALLOCATENODE_ID:
        case ALLOCATEARRAYNODE_ID:
        case POSTINCRNODE_ID:
        case POSTDECRNODE_ID:
        case UNARYPLUSNODE_ID:
        case UNARYMINUSNODE_ID:
        case PREINCRNODE_ID:
        case PREDECRNODE_ID:
        case COMPLEMENTNODE_ID:
        case NOTNODE_ID:
            return exprStrList;

        default:
            return TNLManip.arrayToList(new Object[] { "(", exprStrList, ")" });
        }
    }

    /**
     * Converts a list of strings to a string.
     *
     * @param stringList
     *            a list of strings to be converted
     * @return the converted string
     */
    protected static String _stringListToString(List stringList) {
        Iterator stringItr = stringList.iterator();
        StringBuffer sb = new StringBuffer();

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List) {
                // only use separators for top level
                sb.append(_stringListToString((List) stringObj));
            } else if (stringObj instanceof String) {
                sb.append((String) stringObj);
            } else {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }

        return sb.toString();
    }

    /**
     * Return the code represented by a for init statement.
     *
     * @param list
     *            a list of declarations or statement expressions for the init
     *            part of a for loop
     * @param args
     *            the arguments the get passed to accept().
     * @return a list of strings that represents<br>
     *         <code>MODIFIER TYPE NAME1 = INITEXPR1,
     *                             NAME2 = INITEXPR2, ... </code>
     */
    protected List _forInitStringList(List list, LinkedList args) {
        int length = list.size();

        if (length <= 0)
            return TNLManip.addFirst("");

        TreeNode firstNode = (TreeNode) list.get(0);

        if (firstNode.classID() == LOCALVARDECLNODE_ID) {
            // a list of local variables, with the same type and modifier
            LocalVarDeclNode varDeclNode = (LocalVarDeclNode) firstNode;
            LinkedList retList = new LinkedList();

            retList.addLast(Modifier.toString(varDeclNode.getModifiers()));

            retList.addLast(varDeclNode.getDefType().accept(this, args));
            retList.addLast(" ");

            Iterator declNodeItr = list.iterator();
            while (declNodeItr.hasNext()) {
                LocalVarDeclNode declNode = (LocalVarDeclNode) declNodeItr
                        .next();
                retList.addLast(declNode.getName().getIdent());

                TreeNode initExpr = declNode.getInitExpr();
                if (initExpr != AbsentTreeNode.instance) {
                    retList.addLast(" = ");
                    retList.addLast(initExpr.accept(this, args));
                }

                if (declNodeItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            return retList;

        } else {
            return _separateList(TNLManip.traverseList(this, args, list), ", ");
        }
    }

    /**
     * Return the code represented by a <code>SingleExprNode</code>.
     *
     * @param node
     *            a node that contains the expression
     * @param args
     *            the arguments the get passed to accept().
     * @param opString
     *            the string that represents the operation
     * @param post
     *            a boolean to indicate if it is a post operation
     * @return a list of strings that represents the expression.
     */
    protected LinkedList _visitSingleExprNode(SingleExprNode node,
            LinkedList args, String opString, boolean post) {
        LinkedList retList = new LinkedList();

        LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
                args);
        exprStringList = _parenExpr(node.getExpr(), exprStringList);

        if (post) {
            retList.addLast(exprStringList);
            retList.addLast(opString);
        } else {
            retList.addLast(opString);
            retList.addLast(exprStringList);
        }
        return retList;
    }

    /**
     * Return the code represented by a <code>BinaryOpNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the operation
     * @param args
     *            the arguments the get passed to accept().
     * @param opString
     *            a string that represents the binary operation
     * @return a list of strings that represents <code> EXPR1 OP EXPR2 </code>
     */
    protected LinkedList _visitBinaryOpNode(BinaryOpNode node, LinkedList args,
            String opString) {
        LinkedList retList = new LinkedList();

        LinkedList e1StringList = (LinkedList) node.getExpr1().accept(this,
                args);
        LinkedList e2StringList = (LinkedList) node.getExpr2().accept(this,
                args);

        e1StringList = _parenExpr(node.getExpr1(), e1StringList);
        e2StringList = _parenExpr(node.getExpr2(), e2StringList);

        retList.addLast(e1StringList);
        retList.addLast(" ");
        retList.addLast(opString);
        retList.addLast(" ");
        retList.addLast(e2StringList);

        return retList;
    }

    /**
     * Return the code represented by a <code>BinaryOpAssignNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment.
     * @param args
     *            the arguments the get passed to accept().
     * @param opString
     *            a string that represents the operation assignment.
     * @return a list of strings that represents <code> EXPR1 OP= EXPR2 </code>.
     */
    protected LinkedList _visitBinaryOpAssignNode(BinaryOpAssignNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        List e1StringList = (List) node.getExpr1().accept(this, args);
        List e2StringList = (List) node.getExpr2().accept(this, args);

        retList.addLast(e1StringList);
        retList.addLast(" ");
        retList.addLast(opString);
        retList.addLast(" ");
        retList.addLast(e2StringList);

        return retList;
    }

    /**
     * Return the code represented by a <code>BuiltInLTLNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            the arguments the get passed to accept()
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> BUILTINLTL (PROC1, PROC2)</code>
     */
    protected LinkedList _visitBuiltInLTLNode(BuiltInLTLNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(node.getEvent1().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent2().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>SingleLTLFormulaNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            the arguments the get passed to accept()
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> SINGLELTL (SUBFORM) </code>
     */
    protected LinkedList _visitSingleLTLFormulaNode(SingleLTLFormulaNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(node.getSubform().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>DoubleLTLFormulaNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            the arguments the get passed to accept()
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> SUBFORM1 DOUBLELTL SUBFORM2 </code>
     */
    protected LinkedList _visitDoubleLTLFormulaNode(DoubleLTLFormulaNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast("(");
        retList.addLast(node.getSubform1().accept(this, args));
        retList.addLast(") ");
        retList.addLast(opString);
        retList.addLast(" (");
        retList.addLast(node.getSubform2().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    // Constraints
    /**
     * Return the code represented by an <code>QuantifiedActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @param opString
     *            the string that represents the quantified action
     * @return a list of strings that represents
     *         <code> QUANTIFIER VARS: SUBFORM </code>
     */
    public Object _visitQuantifiedActionNode(QuantifiedActionNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        if (args.size() > 0)
            if (args.getLast() instanceof String)
                if (((String) args.getLast()).equals("loc_checker")) {
                    retList.addLast(node.getSubform().accept(this, args));
                    return retList;
                }

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getVars())));
        retList.addLast(") (");
        retList.addLast(node.getSubform().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code for a port represented by the expression port. The
     * expression can be either a port reference or a String which is the name
     * of the port.
     *
     * @param port
     *            the expression representing a port
     * @param args
     *            a list of arguments to this visit method
     * @return java elaboration code for a port
     */
    protected Object _visitPortExpr(ExprNode port, LinkedList args) {
        LinkedList retList = new LinkedList();
        Boolean localref = (Boolean) port.getDefinedProperty(LOCALPORTREF_KEY);
        if (localref.booleanValue()) {
            retList.addLast(port.accept(this, args));
        } else {
            String pName = _stringListToString((LinkedList) port.accept(this,
                    args));
            int index;

            if ((index = pName.indexOf('[')) == -1) {
                retList.addLast("\"");
                retList.addLast(pName);
                retList.addLast("\"");
            } else {
                retList.addLast("\"");
                retList.addLast(pName.substring(0, index));
                retList.addLast("\"+");
                pName = pName.substring(index);
                String separator = "";
                while ((index = pName.indexOf('[')) >= 0) {
                    retList.addLast(separator);
                    separator = "+";
                    retList.addLast("\"[\"+");
                    int endindex = pName.indexOf(']');
                    retList.addLast(pName.substring(index + 1, endindex));
                    retList.addLast("+\"]\"");
                    pName = pName.substring(endindex + 1);
                }
            }
        }

        return retList;
    }

    /**
     * The default visit method.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return an empty list
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // for testing purposes only
        return new LinkedList();
    }

    /**
     * Return a string to represent the type of a LiteralNode.
     *
     * @param node
     *            The node
     * @return The String representation.
     */
    protected String _literalType(LiteralNode node) {
        if (node instanceof BoolLitNode)
            return new String("EqualVars.BOOLTYPE");
        else if (node instanceof CharLitNode)
            return new String("EqualVars.CHARTYPE");
        else if (node instanceof DoubleLitNode)
            return new String("EqualVars.DOUBLETYPE");
        else if (node instanceof FloatLitNode)
            return new String("EqualVars.FLOATTYPE");
        else if (node instanceof LongLitNode)
            return new String("EqualVars.LONGTYPE");
        else if (node instanceof StringLitNode)
            return new String("EqualVars.STRINGTYPE");
        else
            return new String("EqualVars.INTTYPE");

    }

    /**
     * Return a string containing the number of spaces indicates by the
     * parameter.
     *
     * @param space
     *            Number of spaces to return.
     * @return A string of spaces
     */
    protected String indent(int space) {
        StringBuffer stringBuffer = new StringBuffer(space);

        for (int i = 0; i < space; i++) {
            stringBuffer.append(" ");
        }
        return stringBuffer.toString();
    }

    /** Increase the indent level. */
    protected void increaseIndent() {
        indentLevel += indentOnce;
    }

    /** Decrease the indent level. */
    protected void decreaseIndent() {
        indentLevel -= indentOnce;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    /**
     * (port name, interface name) pairs of scalar ports in a process, medium,
     * scheduler or class
     */
    private Hashtable sports = null;

    /**
     * (port name, interface name) pairs of vectorial ports in a process,
     * medium, scheduler or class
     */
    private Hashtable vports = null;

    /**
     * (port name, number of dimensions) pairs of vectorial ports in a process,
     * medium, scheduler or class
     */
    private Hashtable vportsDim = null;

    /** case numbers for generating case statements for await */
    private int casenum = 0;

    /** a flag indicating whether it is in a constraint block */
    //private boolean isInConstraintBlock = false;
    /** a flag indicating whether it is in a constraint statement */
    //private boolean isInConstraint = false;
    // /////////////////////////////////////////////////////////////////
    // // private methods ////
    /**
     * Create sports and vports.
     *
     * @param LinkedList
     *            members
     * @return void
     */
    private void getPorts(List members) {
        sports = new Hashtable();
        vports = new Hashtable();
        vportsDim = new Hashtable();
        for (int i = 0; i < members.size(); i++) {
            TreeNode t = (TreeNode) members.get(i);
            String pName;
            String pIntfc;
            if (t instanceof PortDeclNode) {
                pName = ((PortDeclNode) t).getName().getIdent();
                TreeNode n = ((PortDeclNode) t).getDefType();
                if (n instanceof ArrayTypeNode) {
                    int dim = 1;
                    while ((n = ((ArrayTypeNode) n).getBaseType()) instanceof ArrayTypeNode)
                        dim++;
                    pIntfc = ((TypeNameNode) n).getName().getIdent();
                    vports.put(pName, pIntfc);
                    vportsDim.put(pName, new Integer(dim));
                } else {
                    pIntfc = ((TypeNameNode) n).getName().getIdent();
                    sports.put(pName, pIntfc);
                }
            }
        }
    }

    /**
     * Release sports and vports.
     *
     * @return void
     */
    private void releasePorts() {
        sports = null;
        vports = null;
        vportsDim = null;
    }

    /**
     * Generate instances for scalar ports
     *
     * @return LinkedList code
     */
    private LinkedList generateScalarPorts() {
        LinkedList retList = new LinkedList();
        if (sports == null)
            return retList;
        if (sports.size() == 0)
            return retList;
        retList.addLast(indent(indentLevel));
        retList.addLast("private void _generateScalarPortsInstances() {\n");
        increaseIndent();
        for (Enumeration e = sports.keys(); e.hasMoreElements();) {
            String portName = (String) (e.nextElement());
            String interfaceName = (String) sports.get(portName);
            retList.addLast(indent(indentLevel));
            retList.addLast(portName + " = new ");
            retList.addLast(interfaceName + "();\n");
        }
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n\n");
        return retList;
    }
}
