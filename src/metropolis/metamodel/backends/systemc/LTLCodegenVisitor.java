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

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.Decl;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.frontend.TypedDecl;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.BeginEventNode;
import metropolis.metamodel.nodetypes.CifNode;
import metropolis.metamodel.nodetypes.CiffNode;
import metropolis.metamodel.nodetypes.ConstraintBlockNode;
import metropolis.metamodel.nodetypes.ELOCConstraintCallNode;
import metropolis.metamodel.nodetypes.ELOCConstraintDeclNode;
import metropolis.metamodel.nodetypes.ELOCConstraintNode;
import metropolis.metamodel.nodetypes.EndEventNode;
import metropolis.metamodel.nodetypes.EventTypeNode;
import metropolis.metamodel.nodetypes.FutureLTLNode;
import metropolis.metamodel.nodetypes.GloballyLTLNode;
import metropolis.metamodel.nodetypes.LOCConstraintCallNode;
import metropolis.metamodel.nodetypes.LOCConstraintDeclNode;
import metropolis.metamodel.nodetypes.LOCConstraintNode;
import metropolis.metamodel.nodetypes.LTLConstraintCallNode;
import metropolis.metamodel.nodetypes.LTLConstraintDeclNode;
import metropolis.metamodel.nodetypes.LTLConstraintNode;
import metropolis.metamodel.nodetypes.LTLSynchNode;
import metropolis.metamodel.nodetypes.MaxDeltaNode;
import metropolis.metamodel.nodetypes.MaxRateNode;
import metropolis.metamodel.nodetypes.MinDeltaNode;
import metropolis.metamodel.nodetypes.MinRateNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.PeriodNode;

import java.util.ArrayList;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // LTLCodegenVisitor
/**
 * Generate SystemC code from a meta-model AST to a file.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Guang Yang
 * @version $Id: LTLCodegenVisitor.java,v 1.23 2006/10/12 20:33:07 cxh Exp $
 */
public class LTLCodegenVisitor extends SystemCCodegenVisitor implements
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
    public LTLCodegenVisitor(int indtLevel) {
        super();
        indentLevel = indtLevel;
    }

    /**
     * Do the same as the one-argument constructor, and allow turning on the
     * output of "<code>#line &lt;line_number&gt; &lt;file_name&gt</code>"
     * directives in the cpp code for debugging metamodel code.
     *
     * @param indtLevel
     *            current indent level for generated code.
     * @param emittingLineDirectives
     *            <code>true</code> to turn on output of <code>#line</code>
     *            directives.
     */
    // public LTLCodegenVisitor(int indtLevel, boolean emittingLineDirectives) {
    // super(emittingLineDirectives);
    // indentLevel = indtLevel;
    // }
    // /////////////////////////////////////////////////////////////////
    // // public variables ////
    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    private LinkedList registerEvents = new LinkedList();

    private boolean firstLTL = true;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////
    /**
     * Returns the code represented by an <code>ActionConstraintNode</code>.
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

        if (!((String) args.get(0)).equals("Netlist")) {
            retList.add(indent(indentLevel));
            retList
                    .add("/* Current version of SystemC-based simulator supports \n");
            retList.add(indent(indentLevel));
            retList
                    .add("   LTL constraints defined in Netlists only. Constraints ignored. */\n");
            return retList;
        }

        // retList.add("\n");
        // retList.add(indent(indentLevel));
        // retList.add("void ltl() {\n");
        increaseIndent();
        firstLTL = true;
        ArrayList temp = TNLManip.traverseList(this, args, node.getStmts());
        retList.add(registerEvents);
        retList.add("\n");
        retList.add(temp);
        retList.add("\n");
        retList.add(indent(indentLevel));
        retList.add("build_buchi();\n");
        decreaseIndent();
        // retList.add(indent(indentLevel));
        // retList.add("}\n\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>LTLSynchNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> ltl synch(events, equalVars) </code>
     */
    public Object visitLTLSynchNode(LTLSynchNode node, LinkedList args) {
        return null;
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
        LinkedList retList = new LinkedList();
        retList.add(indent(indentLevel));
        if (firstLTL) {
            retList.addLast("_ltl_formula += \"");
            firstLTL = false;
        } else {
            retList.add("_ltl_formula += \" && \";\n");
            retList.add(indent(indentLevel));
            retList.add("_ltl_formula += \"");
        }

        retList.add(node.getFormula().accept(this, args));
        retList.add("\";\n");
        return retList;
        // LinkedList temp = node.getFormula().accept(this, args);
        // String ltl = _stringListToString(temp).trim();
    }

    /**
     * Returns the code represented by a <code>BeginEventNode</code>.
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

        retList.addLast("\";\n");
        retList.addLast(indent(indentLevel));
        retList
                .addLast("_ltl_formula += Event.getEventName(Event.newEvent(Event.BEG, ");

        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getAction().accept(this, args));
        retList.addLast("));\n");

        retList.addLast(indent(indentLevel));
        retList.addLast("_ltl_formula += \"");

        return retList;
    }

    /**
     * Returns the code represented by a <code>EndEventNode</code>.
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

        retList.addLast("\";\n");
        retList.addLast(indent(indentLevel));
        retList
                .addLast("_ltl_formula += Event.getEventName(Event.newEvent(Event.END, ");

        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getAction().accept(this, args));
        retList.addLast("));\n");

        retList.addLast(indent(indentLevel));
        retList.addLast("_ltl_formula += \"");

        return retList;

    }

    /**
     * Returns the code represented by an <code>FutureLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> F(SUBFORM) </code>
     */
    public Object visitFutureLTLNode(FutureLTLNode node, LinkedList args) {
        return _visitSingleLTLFormulaNode(node, args, "<>");
    }

    /**
     * Returns the code represented by an <code>GloballyLTLNode</code>.
     *
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> G(SUBFORM) </code>
     */
    public Object visitGloballyLTLNode(GloballyLTLNode node, LinkedList args) {
        return _visitSingleLTLFormulaNode(node, args, "[]");
    }

    /**
     * Returns the code represented by an <code>CifNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 -> EXPR2 </code>
     */
    public Object visitCifNode(CifNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("( ");
        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" -> ");
        retList.addLast(node.getExpr2().accept(this, args));
        retList.addLast(" )");

        return retList;
    }

    /**
     * Returns the code represented by an <code>CiffNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 <-> EXPR2 </code>
     */
    public Object visitCiffNode(CiffNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("(");
        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" <-> ");
        retList.addLast(node.getExpr2().accept(this, args));
        retList.addLast(")");

        return retList;
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
        return null;
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
        return null;
    }

    /**
     * Returns the code represented by a <code>ELOCConstraintDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> eloc name ( params ) ( formula ); </code>
     */
    public Object visitELOCConstraintDeclNode(ELOCConstraintDeclNode node,
            LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by a <code>LOCConstraintDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> loc name ( params ) ( formula ); </code>
     */
    public Object visitLOCConstraintDeclNode(LOCConstraintDeclNode node,
            LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by a <code>LTLConstraintDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> ltl name ( params ) ( formula ); </code>
     */
    public Object visitLTLConstraintDeclNode(LTLConstraintDeclNode node,
            LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by a <code>ELOCConstraintCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> eloc name ( args ) </code>
     */

    public Object visitELOCConstraintCallNode(ELOCConstraintCallNode node,
            LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by a <code>LOCConstraintCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> loc name ( args ) </code>
     *
     */
    public Object visitLOCConstraintCallNode(LOCConstraintCallNode node,
            LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by a <code>LTLConstraintCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> ltl name ( args ) </code>
     */
    public Object visitLTLConstraintCallNode(LTLConstraintCallNode node,
            LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by an <code>MinRateNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> minrate(quantity, event, value) </code>
     */
    public Object visitMinRateNode(MinRateNode node, LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by an <code>MaxRateNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> maxrate(quantity, event, value) </code>
     */
    public Object visitMaxRateNode(MaxRateNode node, LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by an <code>PeriodNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> period(quantity, event, value) </code>
     */
    public Object visitPeriodNode(PeriodNode node, LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by an <code>MinDeltaNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> mindelta(quantity, event1, event2, value) </code>
     */
    public Object visitMinDeltaNode(MinDeltaNode node, LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by an <code>MaxDeltaNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> maxdelta(quantity, event1, event2, value) </code>
     */
    public Object visitMaxDeltaNode(MaxDeltaNode node, LinkedList args) {
        return null;
    }

    /**
     * Returns the code represented by a <code>NameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     */
    public Object visitNameNode(NameNode node, LinkedList args) {
        Decl decl = (Decl) (node.getProperty(DECL_KEY));
        if (!(decl instanceof TypedDecl))
            return super.visitNameNode(node, args);
        if (!(((TypedDecl) decl).getType() instanceof EventTypeNode))
            return super.visitNameNode(node, args);
        LinkedList retList = new LinkedList();

        retList.addLast("\";\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("_ltl_formula += Event.getEventName(");
        retList.addLast(super.visitNameNode(node, args));
        retList.addLast(");\n");

        retList.addLast(indent(indentLevel));
        retList.addLast("_ltl_formula += \"");

        return retList;
    }

}
