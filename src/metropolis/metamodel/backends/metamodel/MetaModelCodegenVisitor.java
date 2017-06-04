/* Generate meta-model code from a meta-model AST to a file.

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

package metropolis.metamodel.backends.metamodel;

import metropolis.metamodel.Effect;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // MetaModelCodegenVisitor
/**
 * Generate meta-model code from a meta-model AST to a file.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Duny Lam
 * @version $Id: MetaModelCodegenVisitor.java,v 1.34 2004/10/19 08:21:19 guyang
 *          Exp $
 */
public class MetaModelCodegenVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Instantiate a MetaModelCodegenVisitor. Set the traversal method to custom
     * so that indent level can be properly set.
     */
    public MetaModelCodegenVisitor() {
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

        retList.addLast(indent(indentLevel));
        retList.addLast("ltl ");
        if (node.getFormula() instanceof BuiltInLTLNode)
            retList.addLast(node.getFormula().accept(this, args));
        else {
            retList.addLast("(");
            retList.addLast(node.getFormula().accept(this, args));
            retList.addLast(")");
        }
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>ActionConstraintNode</code>.
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

        retList.add("\n");
        retList.add(indent(indentLevel));
        retList.add("constraint {\n");

        increaseIndent();
        retList.add(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();

        retList.add(indent(indentLevel));
        retList.add("}\n");

        return retList;
    }

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
        retList.addLast("await {\n");
        increaseIndent();
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
        retList.addLast("(");

        TreeNode cond = node.getCond();
        if (cond == AbsentTreeNode.instance) {
            retList.addLast("default");
        } else {
            retList.addLast(cond.accept(this, args));
        }

        retList.addLast("; ");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getLockTest())));
        retList.addLast("; ");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getLockSet())));
        retList.addLast(")");

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
        retList.addLast("block(");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(") {\n");

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

        retList.addLast("label(");
        retList.addLast(node.getObj().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>MaxLateNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> maxlate(LABEL1, LABEL2, OCCUR, MAXLATENCY) </code>
     */
    /*
     * public Object visitMaxLateNode (MaxLateNode node, LinkedList args) {
     * LinkedList retList = new LinkedList();
     *
     * retList.addLast("maxlate(");
     * retList.addLast(node.getLabel1().accept(this, args)); retList.addLast(",
     * "); retList.addLast(node.getLabel2().accept(this, args));
     * retList.addLast(", "); retList.addLast(node.getOccur().accept(this,
     * args)); retList.addLast(", ");
     * retList.addLast(node.getMaxLatency().accept(this, args));
     * retList.addLast(")");
     *
     * return retList; }
     */

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
        return _visitQuantifiedActionNode(node, "forall");
    }

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
        return _visitQuantifiedActionNode(node, "exists");
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
     *         <code> mutex(event1, event2) </code>
     */
    public Object visitMutexLTLNode(MutexLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, "mutex");
    }

    /**
     * Return the code represented by an <code>SimulLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> simul(event1, event2) </code>
     */
    public Object visitSimulLTLNode(SimulLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, "simul");
    }

    /**
     * Return the code represented by an <code>ExclLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> exec(event1, event2) </code>
     */
    public Object visitExclLTLNode(ExclLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, "excl");
    }

    /**
     * Return the code represented by an <code>PriorityLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> priority(event1, event2) </code>
     */
    public Object visitPriorityLTLNode(PriorityLTLNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("priority(");
        retList.addLast(node.getEvent1().accept(this, null));
        retList.addLast(", ");
        retList.addLast(node.getEvent2().accept(this, null));
        retList.addLast(", ");
        retList.addLast(node.getBoolExpr().accept(this, null));
        retList.addLast(")");

        return retList;
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
        return _visitSingleLTLFormulaNode(node, "F");
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
        return _visitSingleLTLFormulaNode(node, "G");
    }

    /**
     * Return the Code represented by an <code>NextLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> X(SUBFORM) </code>
     */
    public Object visitNextLTLNode(NextLTLNode node, LinkedList args) {
        return _visitSingleLTLFormulaNode(node, "X");
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
        return _visitDoubleLTLFormulaNode(node, "U");
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

        retList.addLast("nondeterminism(");
        retList.addLast(node.getType().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("GXI(");
        retList.addLast(node.getAction().accept(this, args));
        retList.addLast(")");

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
        retList.addLast("boundedloop(");
        retList.addLast(node.getVar().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getCount().accept(this, args));
        retList.addLast(")");
        retList.addLast(node.getStmt().accept(this, args));
        retList.addLast("\n");

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

        retList.addLast("beg(");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("end(");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("pc(");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(")");

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
        retList.addLast("refine(");
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
        retList.addLast("refineconnect(");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getSrcObject().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
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
        retList.addLast("connect(");
        retList.addLast(node.getSrcObject().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getDstObject().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

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
        retList.addLast("redirectconnect(");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getSrcObject().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getSrcPort().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getComponent().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNewPort().accept(this, args));
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
        retList.addLast("addcomponent(");
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
        retList.addLast("setscope(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
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

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("blackbox(");
        retList.addLast(node.getIdent());
        retList.addLast(") ");
        retList.addLast("%%\n");

        increaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast(node.getCode());
        decreaseIndent();

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("%%\n");

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

        retList.addLast("getconnectionnum(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("getnthconnectionsrc(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNum().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("getnthconnectionport(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNum().accept(this, args));
        retList.addLast(")");

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

        retList.addLast(indent(indentLevel));
        retList.addLast("port ");
        retList.addLast(node.getDefType().accept(this, args));
        retList.addLast(" ");
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
        retList.addLast("parameter ");
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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("process ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        }

        retList.addLast(" {\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("medium ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        }

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            retList.addLast(" implements ");
            retList.addLast(_commaList(retValue));
        }

        retList.addLast(" {\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("scheduler ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        }

        retList.addLast(" {\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("statemedium ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        }

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            retList.addLast(" implements ");
            retList.addLast(_commaList(retValue));
        }

        retList.addLast(" {\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("netlist ");
        retList.addLast(node.getName().accept(this, args));

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            retList.addLast(" extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
        }

        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            retList.addLast(" implements ");
            retList.addLast(_commaList(retValue));
        }

        retList.addLast(" {\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

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

        retList.addLast("this.");
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
        LinkedList retList = new LinkedList();

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
        Iterator typeItr = TNLManip
                .traverseList(this, args, node.getDefTypes()).iterator();

        while (typeItr.hasNext()) {
            retList.addLast(typeItr.next());
            retList.addLast("\n");
        }

        return _stringListToString(retList);
        // return retList;
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

        TreeNode params = node.getParameters();
        if (params != AbsentTreeNode.instance) {
            retList.addLast("-<");
            retList.addLast(params.accept(this, args));
            retList.addLast(">-");
        }

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

        retList.addLast(indent(indentLevel));
        retList.addLast("import ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(";\n");

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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
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

        // visit interfaces
        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            retList.addLast("implements ");
            retList.addLast(_commaList(retValue));
            retList.addLast(" ");
        }

        retList.addLast("{\n");

        // visit members
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

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

        // visit type parameters
        List template = TNLManip.traverseList(this, args, node
                .getParTypeNames());
        if (!template.isEmpty()) {
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }
        retList.addLast("\n");

        // visit name node
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("interface ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(" ");

        // visit interfaces
        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            retList.addLast("extends ");
            retList.addLast(_commaList(retValue));
            retList.addLast(" ");
        }

        retList.addLast("{\n");

        // visit members
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();
        retList.addLast("}\n");

        return retList;
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
        retList.addLast("\n");
        retList.addLast(indent(indentLevel));

        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast(Effect.toString(node.getEffect()));
        retList.addLast(node.getReturnType().accept(this, args));
        retList.addLast(" ");
        retList.addLast(node.getName().accept(this, args));

        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getParams())));
        retList.addLast(")");

        if (!node.getUsePorts().isEmpty()) {
            retList.addLast(" useport ");
            retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                    .getUsePorts())));
        }

        if (node.getBody() == AbsentTreeNode.instance) {
            retList.addLast(";");
        } else {
            retList.addLast(" ");
            retList.addLast(node.getBody().accept(this, args));
        }
        retList.addLast("\n");

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

        retList.addLast("{\n");

        increaseIndent();
        retList.addLast(node.getConstructorCall().accept(this, args));
        decreaseIndent();

        List retValue = (List) node.getBody().accept(this, args);

        if (retValue.size() > 1) {
            // get rid of the first '{' and '\n' of the block node string
            retValue.remove(0);
            retList.addLast(retValue);
        }
        retList.addLast("\n");

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

        if (stmtList.size() > 0) {
            // first statement
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
        retList.addLast(_forInitStringList(node.getInit()));
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
        return (node.getName().accept(this, args));
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
     * Return the code represented by an <code>AllocateNode</code>.
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

        retList.addLast("this.");
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
        return _visitSingleExprNode(node, "++", true);
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
        return _visitSingleExprNode(node, "--", true);
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
        return _visitSingleExprNode(node, "+", false);
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
        return _visitSingleExprNode(node, "-", false);
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
        return _visitSingleExprNode(node, "++", false);
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
        return _visitSingleExprNode(node, "--", false);
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
        return _visitSingleExprNode(node, "~", false);
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
        return _visitSingleExprNode(node, "!", false);
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
        return _visitBinaryOpNode(node, "*");
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
        return _visitBinaryOpNode(node, "/");
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
        return _visitBinaryOpNode(node, "%");
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
        return _visitBinaryOpNode(node, "+");
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
        return _visitBinaryOpNode(node, "-");
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
        return _visitBinaryOpNode(node, "<<");
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
        return _visitBinaryOpNode(node, ">>>");
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
        return _visitBinaryOpNode(node, ">>");
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
        return _visitBinaryOpNode(node, "<");
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
        return _visitBinaryOpNode(node, ">");
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
        return _visitBinaryOpNode(node, "<=");
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
        return _visitBinaryOpNode(node, ">=");
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
        return _visitBinaryOpNode(node, "==");
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
        return _visitBinaryOpNode(node, "!=");
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
        return _visitBinaryOpNode(node, "&");
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
        return _visitBinaryOpNode(node, "|");
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
        return _visitBinaryOpNode(node, "^");
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
        return _visitBinaryOpNode(node, "&&");
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
        return _visitBinaryOpNode(node, "||");
    }

    /**
     * Return the code represented by a <code>IfExprNode</code>.
     *
     * @param node
     *            the node that is being visited.
     * @param args
     *            a list of arguments to this visit method.
     * @return a list of strings that represents
     *         <code> EXPR1 ? EXPR2 : EXPR3 </code>.
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
        return _visitBinaryOpAssignNode(node, "*=");
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
        return _visitBinaryOpAssignNode(node, "/=");
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
        return _visitBinaryOpAssignNode(node, "%=");
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
        return _visitBinaryOpAssignNode(node, "+=");
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
        return _visitBinaryOpAssignNode(node, "-=");
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
        return _visitBinaryOpAssignNode(node, "<<=");
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
        return _visitBinaryOpAssignNode(node, ">>>=");
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
        return _visitBinaryOpAssignNode(node, ">>=");
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
        return _visitBinaryOpAssignNode(node, "&=");
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
        return _visitBinaryOpAssignNode(node, "^=");
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
        return _visitBinaryOpAssignNode(node, "|=");
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

        retList.addLast("getcomponent(");
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

        retList.addLast("getconnectiondestnode(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
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

        retList.addLast("getnthport(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(", ");
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

        retList.addLast("getportnum(");
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

        retList.addLast("getscope(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
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

        retList.addLast(indent(indentLevel));
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast("{@\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("@}\n");

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

        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast("{@ ");
        retList.addLast(node.getExpr().accept(this, args));
        if (!(node.getAnode() instanceof AbsentTreeNode))
            retList.addLast(node.getAnode().accept(this, args));
        retList.addLast(" @}");

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

        retList.addLast(indent(indentLevel));
        retList.addLast("ltl synch(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getEvents())));
        if (node.getEqualVars().size() > 0) {
            retList.addLast(" : ");
            retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                    .getEqualVars())));
        }
        retList.addLast(");\n");

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
    public Object visitActionNode(ActionNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        TreeNode obj = node.getObject();
        TreeNode name = node.getName();

        if ((obj == AbsentTreeNode.instance)
                && (name == AbsentTreeNode.instance))
            retList.addLast("all");
        else {
            retList.addLast(obj.accept(this, args));
            retList.addLast(".");
            retList.addLast(name.accept(this, args));
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

        retList.addLast("beg(");
        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getAction().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("end(");
        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getAction().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("none(");
        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("other(");
        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("(");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(") => (");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>VarInEventRefNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> var@(event, index); </code>
     */
    public Object visitVarInEventRefNode(VarInEventRefNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast((LinkedList) node.getVar().accept(this, args));
        retList.addLast("@(");
        retList.addLast((LinkedList) node.getEvent().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIndex().accept(this, args));
        retList.addLast(")");

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

        retList.addLast(indent(indentLevel));
        retList.addLast("loc ");
        if (node.getFormula() instanceof BuiltInActionNode)
            retList.addLast(node.getFormula().accept(this, args));
        else {
            retList.addLast("(");
            retList.addLast(node.getFormula().accept(this, args));
            retList.addLast(")");
        }
        retList.addLast(";\n");

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

        retList.addLast(indent(indentLevel));
        retList.addLast("eloc (");
        retList.addLast(node.getFormula().accept(this, args));
        retList.addLast(");\n");

        return retList;
    }

    /**
     * Return the code represented by a <code>ELOCConstraintDeclNode</code>.
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
        return _visitConstraintDeclNode(node, args, "eloc");
    }

    /**
     * Return the code represented by a <code>LOCConstraintDeclNode</code>.
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
        return _visitConstraintDeclNode(node, args, "loc");
    }

    /**
     * Return the code represented by a <code>LTLConstraintDeclNode</code>.
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
        return _visitConstraintDeclNode(node, args, "ltl");
    }

    /**
     * Return the code represented by a <code>ELOCConstraintCallNode</code>.
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
        LinkedList retList = new LinkedList();
        retList.addLast("eloc ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");
        return retList;
    }

    /**
     * Return the code represented by a <code>LOCConstraintCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> loc name ( args ) </code>
     */
    public Object visitLOCConstraintCallNode(LOCConstraintCallNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();
        retList.addLast("loc ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");
        return retList;
    }

    /**
     * Return the code represented by a <code>LTLConstraintCallNode</code>.
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
        LinkedList retList = new LinkedList();
        retList.addLast("ltl ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");
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

        retList.addLast("minrate(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("maxrate(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("period(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("mindelta(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent1().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent2().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast(")");

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

        retList.addLast("maxdelta(");
        retList.addLast(node.getQuantity().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent1().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent2().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getValue().accept(this, args));
        retList.addLast(")");

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
            retList.addLast("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("template (");
            retList.addLast(_semicolonList(template));
            retList.addLast(")");
        }
        retList.addLast("\n");

        // visit name node
        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));
        retList.addLast("quantity ");
        retList.addLast(node.getName().accept(this, args));

        retList.addLast(" ");

        TreeNode superClass = node.getSuperClass();
        if (superClass != AbsentTreeNode.instance) {
            retList.addLast("extends ");
            retList.addLast(node.getSuperClass().accept(this, args));
            retList.addLast(" ");
        }

        // visit interfaces
        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            retList.addLast("implements ");
            retList.addLast(_commaList(retValue));
            retList.addLast(" ");
        }

        retList.addLast("{\n");

        // visit members
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Return the code represented by an <code>EventTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>event</code>
     */
    public Object visitEventTypeNode(EventTypeNode node, LinkedList args) {
        return TNLManip.addFirst("event");
    }

    /**
     * Return the code represented by an <code>GetInstNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getinstname( node ) </code>
     */
    public Object visitGetInstNameNode(GetInstNameNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("getinstname(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>GetCompNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getcompname(node, netlist) </code>
     */
    public Object visitGetCompNameNode(GetCompNameNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("getcompname(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>IsConnectionRefinedNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> isconnectionrefined(src, port, dest) </code>
     */
    public Object visitIsConnectionRefinedNode(IsConnectionRefinedNode node,
            LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("isconnectionrefined(");
        retList.addLast(node.getSrc().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getDest().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>GetTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> gettype( node ) </code>
     */
    public Object visitGetTypeNode(GetTypeNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("gettype(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>GetProcessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getprocess( event ) </code>
     */
    public Object visitGetProcessNode(GetProcessNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        retList.addLast("getprocess(");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

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
     *            argument.
     * @param separator
     *            a string that would be used to separate items in the list.
     * @return a list of items separated by the separator.
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

        retList.addLast(indent(indentLevel));
        retList.addLast(Modifier.toString(node.getModifiers()));

        retList.addLast(node.getDefType().accept(this, args));
        retList.addLast(" " + node.getName().getIdent());

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" = ");
            retList.addLast(node.getInitExpr().accept(this, args));
        }
        retList.addLast(";\n");

        return retList;
    }

    /**
     * what does it do????
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
     *            a list of strings to be converted.
     * @return the converted string.
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
     * @return a list of strings that represents<br>
     *         <code>MODIFIER TYPE NAME1 = INITEXPR1,
     *                             NAME2 = INITEXPR2, ... </code>
     */
    protected List _forInitStringList(List list) {
        int length = list.size();

        if (length <= 0)
            return TNLManip.addFirst("");

        TreeNode firstNode = (TreeNode) list.get(0);

        if (firstNode.classID() == LOCALVARDECLNODE_ID) {
            // a list of local variables, with the same type and modifier
            LocalVarDeclNode varDeclNode = (LocalVarDeclNode) firstNode;
            LinkedList retList = new LinkedList();

            retList.addLast(Modifier.toString(varDeclNode.getModifiers()));

            retList.addLast(varDeclNode.getDefType().accept(this, null));
            retList.addLast(" ");

            Iterator declNodeItr = list.iterator();
            while (declNodeItr.hasNext()) {
                LocalVarDeclNode declNode = (LocalVarDeclNode) declNodeItr
                        .next();
                retList.addLast(declNode.getName().getIdent());

                TreeNode initExpr = declNode.getInitExpr();
                if (initExpr != AbsentTreeNode.instance) {
                    retList.addLast(" = ");
                    retList.addLast(initExpr.accept(this, null));
                }

                if (declNodeItr.hasNext()) {
                    retList.addLast(", ");
                }
            }
            return retList;

        } else {
            return _separateList(TNLManip.traverseList(this, null, list), ", ");
        }
    }

    /**
     * Return the code represented by a <code>SingleExprNode</code>.
     *
     * @param node
     *            a node that contains the expression
     * @param opString
     *            the string that represents the operation
     * @param post
     *            a boolean to indicate if it is a post operation
     * @return a list of strings that represents the expression.
     */
    protected LinkedList _visitSingleExprNode(SingleExprNode node,
            String opString, boolean post) {
        LinkedList retList = new LinkedList();

        LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
                null);
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
     * @param opString
     *            a string that represents the binary operation
     * @return a list of strings that represents <code> EXPR1 OP EXPR2 </code>
     */
    protected LinkedList _visitBinaryOpNode(BinaryOpNode node, String opString) {
        LinkedList retList = new LinkedList();

        LinkedList e1StringList = (LinkedList) node.getExpr1().accept(this,
                null);
        LinkedList e2StringList = (LinkedList) node.getExpr2().accept(this,
                null);

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
     *            a node that contains the expressions of the assignment
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents <code> EXPR1 OP= EXPR2 </code>
     */
    protected LinkedList _visitBinaryOpAssignNode(BinaryOpAssignNode node,
            String opString) {
        LinkedList retList = new LinkedList();

        List e1StringList = (List) node.getExpr1().accept(this, null);
        List e2StringList = (List) node.getExpr2().accept(this, null);

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
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> BUILTINLTL (event1, event2)</code>
     */
    protected LinkedList _visitBuiltInLTLNode(BuiltInLTLNode node,
            String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(node.getEvent1().accept(this, null));
        retList.addLast(", ");
        retList.addLast(node.getEvent2().accept(this, null));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>SingleLTLFormulaNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> SINGLELTL (SUBFORM) </code>
     */
    protected LinkedList _visitSingleLTLFormulaNode(SingleLTLFormulaNode node,
            String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(node.getSubform().accept(this, null));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by a <code>DoubleLTLFormulaNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> SUBFORM1 DOUBLELTL SUBFORM2 </code>
     */
    protected LinkedList _visitDoubleLTLFormulaNode(DoubleLTLFormulaNode node,
            String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast("(");
        retList.addLast(node.getSubform1().accept(this, null));
        retList.addLast(") ");
        retList.addLast(opString);
        retList.addLast(" (");
        retList.addLast(node.getSubform2().accept(this, null));
        retList.addLast(")");

        return retList;
    }

    /**
     * Return the code represented by an <code>QuantifiedActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param opString
     *            the string that represents the quantified action
     * @return a list of strings that represents
     *         <code> QUANTIFIER VARS: SUBFORM </code>
     */
    public Object _visitQuantifiedActionNode(QuantifiedActionNode node,
            String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, null, node
                .getVars())));
        retList.addLast(") (");
        retList.addLast(node.getSubform().accept(this, null));
        retList.addLast(")");

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
     * Return the code represented by a <code>ConstraintDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @param opString
     *            Operation string? Typical values include <code>eloc</code>,
     *            <code>loc</code> and <code>ltl</code>.
     * @return a list of strings that represents
     *         <code> opString name ( params ) ( formula )</code>
     */
    public Object _visitConstraintDeclNode(ConstraintDeclNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(indent(indentLevel));
        retList.addLast(opString);
        retList.addLast(" ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(" (");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getParams())));
        retList.addLast(") (");
        retList.addLast(node.getFormula().accept(this, args));
        retList.addLast(");\n");

        return retList;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    /** number of spaces for the current indent level */
    private int indentLevel = 0;

    /** number of spaces for each indentation */
    private final int indentOnce = 4;

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * make a string of spaces indicated by the parameter
     *
     * @param indentation
     *            level
     * @return a string of spaces
     */
    private String indent(int space) {
        StringBuffer stringBuffer = new StringBuffer(space);

        for (int i = 0; i < space; i++)
            stringBuffer.append(" ");

        return stringBuffer.toString();
    }

    /**
     * increase the indent level
     */
    private void increaseIndent() {
        indentLevel += indentOnce;
    }

    /**
     * decrease the indent level
     */
    private void decreaseIndent() {
        indentLevel -= indentOnce;
    }

}
