/*
 A visitor to annotate nodes with the line numbers of the next executable
 lines in the metamodel file.  This information is use by the metamodel
 debugger.

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

package metropolis.metamodel.backends.systemc.mmdebug;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.NullValue;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ActionLabelStmtNode;
import metropolis.metamodel.nodetypes.AnnotationNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.AwaitStatementNode;
import metropolis.metamodel.nodetypes.BeginAnnotationNode;
import metropolis.metamodel.nodetypes.BlockNode;
import metropolis.metamodel.nodetypes.BreakNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.ContinueNode;
import metropolis.metamodel.nodetypes.EmptyStmtNode;
import metropolis.metamodel.nodetypes.EndAnnotationNode;
import metropolis.metamodel.nodetypes.ForNode;
import metropolis.metamodel.nodetypes.IfStmtNode;
import metropolis.metamodel.nodetypes.LabeledBlockNode;
import metropolis.metamodel.nodetypes.LoopNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.ReturnNode;
import metropolis.metamodel.nodetypes.StatementNode;
import metropolis.metamodel.nodetypes.SwitchBranchNode;
import metropolis.metamodel.nodetypes.SwitchNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

// ////////////////////////////////////////////////////////////////////////
// // NextLineVisitor
/**
 * This is a <code>MetaModelVisitor</code> that annotates nodes with the line
 * numbers of the next possible executable lines in the metamodel source file.
 * For example, it sets the <code>NEXT_LINENUMBERS_KEY</code> property of an
 * <code>AwaitStatementNode</code> with the first line numbers of all its
 * critical sections.
 * <p>
 * This is used for metamodel debugging.
 *
 * @author Allen Hopkins
 * @version $Id: NextLineVisitor.java,v 1.31 2006/10/12 20:33:24 cxh Exp $
 */
public class NextLineVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors
    /**
     * Constructs a NextLineVisitor object.
     *
     * @param traversalMethod
     *            an int specifying the traversal method, usually one of
     *            {@link metropolis.metamodel.IVisitor#TM_CHILDREN_FIRST},
     *            {@link metropolis.metamodel.IVisitor#TM_SELF_FIRST} or
     *            {@link metropolis.metamodel.IVisitor#TM_CUSTOM}.
     */
    public NextLineVisitor(int traversalMethod) {
        super(traversalMethod);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods

    /**
     * Set the NEXT_SIBLING_LINENUMBER_KEY and NEXT_LINENUMBERS_KEY properties
     * for all statements in this <code>ActionLabelStmtNode</code> and for
     * this node itself. The next sibling of the last non-empty statement is the
     * next sibling of this node. The next sibling of every other statement in
     * this <code>ActionLabelStmtNode</code> is non-empty statement following
     * it.
     *
     * @param node
     *            The <code>ActionLabelStmtNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> which is ignored.
     * @return <code>null</code>.
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        return _setNextLinesForBlockOfStatements(node.getStmts(), node);
    }

    /**
     * Set the NEXT_SIBLING_LINENUMBER_KEY and NEXT_LINENUMBERS_KEY properties
     * for all statements in this <code>AnnotationNode</code> and for this
     * node itself. The next sibling of the last non-empty statement is the next
     * sibling of this node. The next sibling of every other statement in this
     * <code>AnnotationNode</code> is non-empty statement following it.
     *
     * @param node
     *            The <code>AnnotationNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> which is ignored.
     * @return <code>null</code>.
     */
    public Object visitAnnotationNode(AnnotationNode node, LinkedList args) {
        return _setNextLinesForBlockOfStatements(node.getStmts(), node);
    }

    /**
     * Set the <code>NEXT_SIBLING_LINENUMBER_KEY</code> property value to that
     * of this node's parent <code>AwaitStatementNode</code>. Also, if this
     * <code>AwaitGuardNode</code>'s statement is not a
     * <code>BlockNode</code>, set its
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to the same thing,
     * and also add it to its <code>NEXT_LINENUMBERS</code> list property.
     *
     * @param node
     *            The <code>AwaitGuardNode</code> being visited.
     * @param args
     *            An unused <code>LinkedList</code>.
     * @return <code>null</code>.
     */
    public Object visitAwaitGuardNode(AwaitGuardNode node, LinkedList args) {

        AwaitStatementNode parent = (AwaitStatementNode) node.getParent();
        Object nextParentSibling = parent
                .getProperty(NEXT_SIBLING_LINENUMBER_KEY);
        node.setProperty(NEXT_SIBLING_LINENUMBER_KEY, nextParentSibling);
        _addNextLineNumber((TreeNode) node, nextParentSibling);

        StatementNode statement = node.getStmt();
        if (statement != null) {
            if (statement instanceof BlockNode) {
                // Do nothing. It's handled by visitBlockNode().
            } else {
                ((TreeNode) statement).setProperty(NEXT_SIBLING_LINENUMBER_KEY,
                        nextParentSibling);
                _addNextLineNumber((TreeNode) statement, nextParentSibling);
            }

            // Check that critical section starts on its own line:
            Integer awaitLineNumber = (Integer) parent
                    .getProperty(LINENUMBER_KEY);
            Object csLineNumberObject = ((TreeNode) statement)
                    .getProperty(LINENUMBER_KEY);
            if (!(csLineNumberObject instanceof NullValue)) {
                // Assume NullValue line number means empty C/S & that's OK.
                Integer criticalSectionLineNumber = (Integer) csLineNumberObject;
                if (awaitLineNumber.equals(criticalSectionLineNumber)) {
                    throw new MMDebugException(node,
                            "Await critical section must begin on a "
                                    + "line separate from the await keyword.");
                }
            }
        }

        // Avoid visiting the condition statement's children, so we don't
        // try to set breakpoints around any method calls contained there.
        //
        // XXX This assumes that the user doesn't want to step into
        // await condition test. Valid assumption?
        //
        TreeNode cond = node.getCond();
        if (cond != null) {
            cond.ignoreChildren();
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the <code>NEXT_LINENUMBERS_KEY</code> property value to a
     * <code>List</code> of <code>Integer</code> line numbers of the
     * <code>await</code> statement's critical sections. For each empty
     * critical section, the corresponding line number is this await statement's
     * NEXT_SIBLING.
     *
     * @param node
     *            The <code>AwaitStatementNode</code> to analyze.
     * @param errorsToReturn
     *            A <code>LinkedList</code> that will contain any errors on
     *            return, as a <code>LinkedList</code> of
     *            <code>MMDebugError</code>s.
     * @return <code>null</code>
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList errorsToReturn) {
        List guardList = node.getGuards();
        Iterator guards = guardList.iterator();
        //Object myLineNumber = node.getProperty(LINENUMBER_KEY);

        while (guards.hasNext()) {
            StatementNode statementNode = ((AwaitGuardNode) guards.next())
                    .getStmt();

            if (statementNode != null) {
                _addNextLineNumber(node, ((TreeNode) statementNode)
                        .getProperty(LINENUMBER_KEY));
            }
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the NEXT_SIBLING_LINENUMBER_KEY and NEXT_LINENUMBERS_KEY properties
     * for all statements in this <code>BeginAnnotationNode</code> and for
     * this node itself. The next sibling of the last non-empty statement is the
     * next sibling of this node. The next sibling of every other statement in
     * this <code>BeginAnnotationNode</code> is non-empty statement following
     * it.
     *
     * @param node
     *            The <code>BeginAnnotationNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> which is ignored.
     * @return <code>null</code>.
     */
    public Object visitBeginAnnotationNode(BeginAnnotationNode node,
            LinkedList args) {
        //TreeNode firstStatement = null;
        //int firstStatementIndex = 0;
        //TreeNode lastStatement = null;
        //int lastStatementIndex = 0;
        List statementList = null;

        statementList = node.getStmts();

        return _setNextLinesForBlockOfStatements(statementList, node);
    }

    /**
     * For each statement in the visited BlockNode, see that its
     * NEXT_LINENUMBERS_KEY property contains the line number of the statement
     * following it in the block. For the last statement, set its
     * NEXT_LINENUMBERS_KEY property according to the node type of the parent of
     * this block.
     */
    public Object visitBlockNode(BlockNode node, LinkedList errorsToReturn) {

        TreeNode firstStatement = null;
        int i = 0;
        TreeNode lastStatement = null;
        int lastStatementIndex = 0;
        TreeNode parent = node.getParent();
        List statements = node.getStmts();

        if (statements == null || statements.size() == 0) {
            // The block is empty.
            return null;
        }

        i = statements.size() - 1;
        lastStatement = (TreeNode) statements.get(i);
        while (lastStatement instanceof EmptyStmtNode && i > 0) {
            i--;
            lastStatement = (TreeNode) statements.get(i);
        }

        i = DebugUtil.getLastNonemptyStatementIndex(statements, node);
        if (i < 0) {
            // Block contains only EmptyStmtNode's.
            return null;
        }

        lastStatementIndex = i;

        i = DebugUtil.getFirstNonemptyStatementIndex(statements, node);
        if (i >= 0) {
            firstStatement = (TreeNode) statements.get(i);
        }

        // First, set next line number of the last statement in the block:

        switch (parent.classID()) {
        case ACTIONLABELEXPRNODE_ID:
        case ACTIONLABELSTMTNODE_ID:
        case ANNOTATIONNODE_ID:
        case BOUNDEDLOOPNODE_ID:
        case IFSTMTNODE_ID:
        case LABELEDBLOCKNODE_ID:
        case LABELEDSTMTNODE_ID:
            _setNextSibling(lastStatement, _getNextSibling(parent));
            break;
        case FORNODE_ID:
            // numberToSet = parent.getProperty(LINENUMBER_KEY);
            // Don't use the for-statement's line number, since a bkpt
            // there will only be hit on entry, and not on iterations.
            // (feature of gdb)
            // Replace it with *both* line# of 1st stmt in loop *and*
            // loop's next-sibling line#.
            _setNextSibling(lastStatement, _getNextSibling(parent));
            _addNextSibling(lastStatement, firstStatement
                    .getProperty(LINENUMBER_KEY));
            break;
        case LOOPNODE_ID:
            if (((LoopNode) parent).getForeStmt() instanceof EmptyStmtNode) {
                // while-loop
                _setNextSibling(lastStatement, parent
                        .getProperty(LINENUMBER_KEY));
            } else {
                // do-loop
                _setNextSibling(lastStatement, parent
                        .getProperty(DOLOOP_TEST_LINENUMBER_KEY));
            }
            break;
        case SWITCHBRANCHNODE_ID:
            // Next line is the 1st line of the next case in the switch,
            // unless this is a break statement, in which case this
            // assumption will be fixed in VisitBreakNode().
            // XXX Test this.
            _setNextSibling(lastStatement, _getNextSibling(parent));
            break;
        case AWAITGUARDNODE_ID:
            // numberToSet = node.getProperty(BLOCK_END_LINENUMBER_KEY);
            _setNextSibling(lastStatement, _getNextSibling(parent));
            // Let's not. This is a big gotcha if the only statement
            // in the block is an if statement. (Results in next-line
            // stack underflow.)
            // Instead, when we're stopped at a "pnext" that's
            // entering this guard's statement block, have "next" break
            // at the first statement, even though we're claiming
            // to already be there.
            /*
             * i = DebugUtil.getFirstNonemptyStatementIndex(statements, node);
             * if (i > -1) { ((TreeNode)statements.get(i)).setProperty(
             * NO_LINENUMBER_POP_KEY, Boolean.TRUE);
             * ((TreeNode)statements.get(i)).setProperty(
             * NO_LINENUMBER_PUSH_KEY, Boolean.TRUE); // The last statement
             * always pops next-lines, even if it's // also the first statement:
             * ((TreeNode)statements.get(lastStatementIndex))
             * .removeProperty(NO_LINENUMBER_POP_KEY); }
             */
            break;
        case CONSTRUCTORDECLNODE_ID:
            _setNextSibling(lastStatement, node
                    .getProperty(BLOCK_END_LINENUMBER_KEY));
            break;
        case METHODDECLNODE_ID:
            _setNextSibling(lastStatement, node
                    .getProperty(BLOCK_END_LINENUMBER_KEY));
            i = DebugUtil.getFirstNonemptyStatementIndex(statements, node);
            if (i > -1) {
                ((TreeNode) statements.get(i)).setProperty(
                        NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            }
            break;
        case BLOCKNODE_ID:
            _setNextSibling(lastStatement, _getNextSibling(node));
            break;
        default:
            // FIXME
            System.err.println("VisitBlockNode(): parent is "
                    + parent.getClass().getName());
            break;
        }

        _addNextLineNumber(lastStatement, _getNextSibling(lastStatement));

        _setNextLinesForSwitchAndBlock(statements, lastStatementIndex);

        // The NEXT_LINENUMBER of the block itself is the NEXT_SIBLING of
        // its first statement.

        if (firstStatement != null && firstStatement != lastStatement) {
            // If there's more than one statement in the block...
            _addNextLineNumber(node, _getNextSibling(firstStatement));
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the <code>NEXT_LINENUMBERS_KEY</code> property of this
     * <code>break</code> statement to be the
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property of its closest
     * ancestor that is a <code>LoopNode</code>, <code>ForNode</code> or
     * <code>SwitchNode</code>.
     *
     * @param node
     *            The <code>BreakNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitBreakNode(BreakNode node, LinkedList args) {
        TreeNode ancestor = node.getParent();
        while (ancestor != null
                && !(ancestor instanceof LoopNode
                        || ancestor instanceof ForNode || ancestor instanceof SwitchNode)) {
            ancestor = ancestor.getParent();
        }

        if (ancestor == null) {
            throw new RuntimeException("break-able parent not found for "
                    + "break statement in " + node.getProperty(IDENT_KEY)
                    + ", line " + node.getProperty(LINENUMBER_KEY));
        }

        node.removeProperty(NEXT_SIBLING_LINENUMBER_KEY);
        node.removeProperty(NEXT_LINENUMBERS_KEY);
        _addNextLineNumber(node, _getNextSibling(ancestor));

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to the
     * <code>NullValue</code>, and prevent trying to put debugging info into
     * constructors, since they have no ProgramCounter pointer available.
     *
     * @param node
     *            The <code>ConstructorDeclNode</code> to analyze.
     * @param errorsToReturn
     *            A <code>LinkedList</code> that will contain any errors on
     *            return.
     * @return <code>null</code>
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList errorsToReturn) {

        _setNextSibling(node, NullValue.instance);
        node.ignoreChildren();
        node.setProperty(DONT_DEBUG_ME_KEY, Boolean.TRUE);
        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the <code>NEXT_LINENUMBERS_KEY</code> property of this
     * <code>continue</code> statement to be the <code>LINENUMBER_KEY</code>
     * property of its closest ancestor that is a <code>LoopNode</code> or
     * <code>ForNode</code>. The exception is for a <i>do-while</i> loop, in
     * which case, set it to the loop's <code>DOLOOP_TEST_LINENUMBER_KEY</code>
     * property.
     *
     * @param node
     *            The <code>ContinueNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        TreeNode ancestor = node.getParent();
        while (ancestor != null
                && !(ancestor instanceof LoopNode || ancestor instanceof ForNode)) {
            ancestor = ancestor.getParent();
        }

        if (ancestor == null) {
            throw new MMDebugException(node,
                    "continue-able parent not found for continue statement.");
        }

        node.removeProperty(NEXT_SIBLING_LINENUMBER_KEY);
        node.removeProperty(NEXT_LINENUMBERS_KEY);

        if (ancestor instanceof LoopNode
                && ancestor.getProperty(DOLOOP_TEST_LINENUMBER_KEY) != null) {
            // Ancestor is a do-while.

            _addNextLineNumber(node, ancestor
                    .getProperty(DOLOOP_TEST_LINENUMBER_KEY));
        } else {
            _addNextLineNumber(node, ancestor.getProperty(LINENUMBER_KEY));
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the NEXT_SIBLING_LINENUMBER_KEY and NEXT_LINENUMBERS_KEY properties
     * for all statements in this <code>EndAnnotationNode</code> and for this
     * node itself. The next sibling of the last non-empty statement is the next
     * sibling of this node. The next sibling of every other statement in this
     * <code>EndAnnotationNode</code> is non-empty statement following it.
     *
     * This code is identical to <code>visitBeginAnnotationNode()</code>.
     *
     * @param node
     *            The <code>EndAnnotationNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> which is ignored.
     * @return <code>null</code>.
     */
    public Object visitEndAnnotationNode(EndAnnotationNode node, LinkedList args) {
        return _setNextLinesForBlockOfStatements(node.getStmts(), node);
    }

    /**
     * Add to this <code>ForNode</code>'s <code>NEXT_LINENUMBERS_KEY</code>
     * property the line number of the first statement in its body. Also, if the
     * body is not a <code>BlockNode</code> and not empty, set the body's
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to be the same as
     * that of the <code>ForNode</code> itself.
     *
     * @param node
     *            The <code>ForNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitForNode(ForNode node, LinkedList args) {
        TreeNode body = (TreeNode) node.getStmt();
        boolean bodyIsEmpty = _containsNoStatements(body);
        Object myLineNumber = node.getProperty(LINENUMBER_KEY);

        // This is now pointless, but harmless, since gdb never stops at
        // a breakpoint set on the "for (...)" line after the first iteration:
        // _addNextLineNumber(node, myLineNumber);

        if (!bodyIsEmpty) {
            Object bodyFirstLineNumber = body.getProperty(LINENUMBER_KEY);
            if (bodyFirstLineNumber instanceof Integer) {
                if (((Integer) bodyFirstLineNumber)
                        .compareTo((Integer) myLineNumber) > 0) {
                    _addNextLineNumber(node, (Integer) bodyFirstLineNumber);
                } else {
                    Object bodySecondLineNumber = body
                            .getProperty(BLOCK_SECOND_LINENUMBER_KEY);
                    if (bodySecondLineNumber != null) {
                        _addNextLineNumber(node, (Integer) bodySecondLineNumber);
                    }
                }
            }

            if (body instanceof BlockNode) {
                List statementList = ((BlockNode) body).getStmts();
                int firstIndex = DebugUtil.getFirstNonemptyStatementIndex(
                        statementList, body);
                ((TreeNode) statementList.get(firstIndex)).setProperty(
                        NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            } else {
                _setNextSibling(body, myLineNumber);
                _addNextLineNumber(body, myLineNumber);
                body.setProperty(NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            }
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Add to this <code>IfStmtNode</code>'s
     * <code>NEXT_LINENUMBERS_KEY</code> property the line numbers of the
     * first statements in its "<i>then</i>" and "<i>else</i>" parts. Also,
     * if each of those parts are not a <code>BlockNode</code> and not empty,
     * set that part's <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to be
     * the same as that of the <code>IfStmtNode</code> itself.
     *
     * @param node
     *            The <code>IfStmtNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>
     */
    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {

        StatementNode thenPart = node.getThenPart();
        TreeNode elsePart = node.getElsePart();
        Object myLineNumber = node.getProperty(LINENUMBER_KEY);
        Object myNextSibling = _getNextSibling(node);

        if (!(thenPart instanceof EmptyStmtNode)
                && (!(thenPart instanceof BlockNode) || ((BlockNode) thenPart)
                        .getStmts().size() > 0)) {

            Object thenFirstLineNumber = ((TreeNode) thenPart)
                    .getProperty(LINENUMBER_KEY);
            if (thenFirstLineNumber instanceof Integer) {
                if (((Integer) thenFirstLineNumber)
                        .compareTo((Integer) myLineNumber) > 0) {
                    _addNextLineNumber(node, (Integer) thenFirstLineNumber);
                } else {
                    Object thenSecondLineNumber = ((TreeNode) thenPart)
                            .getProperty(BLOCK_SECOND_LINENUMBER_KEY);
                    if (thenSecondLineNumber != null) {
                        _addNextLineNumber(node, (Integer) thenSecondLineNumber);
                    }
                }
            }
            if (!(thenPart instanceof BlockNode)) {
                _setNextSibling((TreeNode) thenPart, myNextSibling);
                _addNextLineNumber((TreeNode) thenPart, myNextSibling);
            }
        }

        if (elsePart != null
                && !(elsePart instanceof AbsentTreeNode)
                && (!(elsePart instanceof BlockNode) || ((BlockNode) elsePart)
                        .getStmts().size() > 0)) {

            Object elseFirstLineNumber = elsePart.getProperty(LINENUMBER_KEY);
            if (elseFirstLineNumber instanceof Integer) {
                if (((Integer) elseFirstLineNumber)
                        .compareTo((Integer) myLineNumber) > 0) {
                    _addNextLineNumber(node, (Integer) elseFirstLineNumber);
                } else {
                    Object elseSecondLineNumber = elsePart
                            .getProperty(BLOCK_SECOND_LINENUMBER_KEY);
                    if (elseSecondLineNumber != null) {
                        _addNextLineNumber(node, (Integer) elseSecondLineNumber);
                    }
                }
            }
            if (!(elsePart instanceof BlockNode)) {
                _setNextSibling(elsePart, myNextSibling);
                _addNextLineNumber(elsePart, myNextSibling);
            }
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Do exactly what <code>visitBlockNode</code> does to this
     * <code>LabeledBlockNode</code>, and also add this block's first
     * statement's line number to this block's NEXT_LINENUMBERS_KEY list.
     *
     * @param node
     *            The <code>LabeledBlockNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        List statementList = node.getStmts();
        int firstStatementIndex = DebugUtil.getFirstNonemptyStatementIndex(
                statementList, node);
        if (firstStatementIndex >= 0) {
            TreeNode firstStatement = (TreeNode) statementList
                    .get(firstStatementIndex);
            _addNextLineNumber(node, firstStatement.getProperty(LINENUMBER_KEY));
        }
        return visitBlockNode((BlockNode) node, args);
    }

    /**
     * Add to this <code>LoopNode</code>'s <code>NEXT_LINENUMBERS_KEY</code>
     * property the line number of the first statement in its body. Also, if the
     * body is not a <code>BlockNode</code> and not empty, set the body's
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to be the same as
     * that of the <code>LoopNode</code> itself.
     *
     * @param node
     *            The <code>LoopNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitLoopNode(LoopNode node, LinkedList args) {

        if (node.getProperty(DOLOOP_TEST_LINENUMBER_KEY) != null) {
            return _visitDoLoopNode(node, args);
        } else {
            return _visitWhileLoopNode(node, args);
        }
    }

    /**
     * Prevent <code>MediumDeclNode</code>s from being traversed.
     *
     * @param node
     *            The <code>MediumDeclNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        // This is done in visitConstructorDecl().
        /*
         * List members = node.getMembers(); for (int i = 0; i < members.size();
         * i++) { TreeNode child = (TreeNode)members.get(i); if (child
         * instanceof ConstructorDeclNode) { child.ignoreChildren(); } }
         */
        // _defaultVisit(node,null);
        return null;
    }

    /**
     * Set the <code>NEXT_LINENUMBERS_KEY</code> property of this
     * <code>return</code> statement to be the
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property of its enclosing
     * method body.
     *
     * @param node
     *            The <code>ReturnNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        TreeNode ancestor = node.getParent();
        while (ancestor != null && !(ancestor instanceof MethodDeclNode)) {
            ancestor = ancestor.getParent();
        }

        if (ancestor == null) {
            throw new MMDebugException(node,
                    "Method body not found for return statement.");
        }

        node.removeProperty(NEXT_SIBLING_LINENUMBER_KEY);
        node.removeProperty(NEXT_LINENUMBERS_KEY);
        TreeNode body = ((MethodDeclNode) ancestor).getBody();
        _addNextLineNumber(node, body.getProperty(BLOCK_END_LINENUMBER_KEY));

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Set the <code>NEXT_LINENUMBERS_KEY</code> property of this
     * <code>switch</code> statement to the list of the line numbers of the
     * first statements of all its cases.
     *
     * @param node
     *            The <code>SwitchNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        List allStatements = new ArrayList();
        List branchList = node.getSwitchBlocks(); // List of
        // SwitchBranchNode's
        Iterator branches = branchList.iterator();
        while (branches.hasNext()) {
            SwitchBranchNode branch = (SwitchBranchNode) branches.next();
            List branchStmts = branch.getStmts();
            allStatements.addAll(branchStmts);
            int firstStatementIndex = DebugUtil.getFirstNonemptyStatementIndex(
                    branchStmts, branch);
            if (firstStatementIndex != -1) {
                TreeNode firstStatement = (TreeNode) branchStmts
                        .get(firstStatementIndex);
                _addNextLineNumber(node, firstStatement
                        .getProperty(LINENUMBER_KEY));
            }
        }

        if (allStatements != null && allStatements.size() > 0) {
            int i = allStatements.size() - 1;
            TreeNode lastStatement = (TreeNode) allStatements.get(i);
            while (lastStatement instanceof EmptyStmtNode && i > 0) {
                i--;
                lastStatement = (TreeNode) allStatements.get(i);
            }

            if (!(lastStatement instanceof EmptyStmtNode)) {
                _setNextLinesForSwitchAndBlock(allStatements, i);
            }
        }

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // private methods

    //    /**
    //     *
    //     */
    //    private Integer _getIntegerProperty(TreeNode node, Integer key) {
    //        Object value = node.getProperty(key);
    //        if (value instanceof Integer) {
    //            return (Integer) value;
    //        } else {
    //            throw new MMDebugException(node,
    //                    "Integer property expected w/ key " + key);
    //        }
    //    }

    /**
     * For a given node and line-number value, add the value to the node's
     * NEXT_LINENUMBERS_KEY list property, if it is not already in that list.
     * Create the list if it doesn't exist. XXX Note: Don't add if it is
     * <code>NullValue.instance</code>, because everything in the
     * NEXT_LINENUMBERS_KEY list gets used to set breakpoints.
     *
     * @param node
     *            the TreeNode whose property is added to
     * @param value
     *            the stuff to add -- either a List of Objects, or a single
     *            Object.
     */
    private void _addNextLineNumber(TreeNode node, Object value) {
        List nextLines = (List) node.getProperty(NEXT_LINENUMBERS_KEY);
        if (nextLines == null) {
            nextLines = new Vector();
        }
        if (value != null) {

            if (value instanceof List) {
                List listToAdd = (List) value;
                for (int i = 0; i < listToAdd.size(); i++) {
                    Object valueToAdd = listToAdd.get(i);
                    if (valueToAdd != NullValue.instance
                            && nextLines.indexOf(valueToAdd) == -1) {
                        nextLines.add(valueToAdd);
                    }
                }
            } else {
                if (value != NullValue.instance
                        && nextLines.indexOf(value) == -1) {
                    nextLines.add(value);
                }
            }
        }
        node.setProperty(NEXT_LINENUMBERS_KEY, nextLines);
    }

    /**
     * Add the given <code>Object</code> to the given <code>TreeNode</code>'s
     * NEXT_SIBLING_LINENUMBERS_KEY property.
     *
     * @param node
     *            the <code>TreeNode</code> whose property is to be updated
     * @param sibling
     *            the stuff to add to the node's property, either a
     *            <code>List</code>, or an <code>Object</code> to be added
     *            to the property value, which ends up being a list if it isn't
     *            already.
     */
    private void _addNextSibling(TreeNode node, Object sibling) {
        Object existingSiblings = _getNextSibling(node);
        List targetList = null;

        if (existingSiblings == null) {
            _setNextSibling(node, sibling);
        } else {
            if (existingSiblings instanceof List) {
                targetList = (List) existingSiblings;
            } else {
                targetList = new Vector();
                targetList.add(existingSiblings);
            }
            if (sibling instanceof List) {
                targetList.addAll((List) sibling);
            } else {
                targetList.add(sibling);
            }
            _setNextSibling(node, targetList);
        }
    }

    /**
     * Return <code>true</code> if the given statement body is empty. That is,
     * return true if
     * <ul>
     * <li>the given node is <code>null</code>,
     * <li>the given node is an <code>EmptyStmtNode</code>,
     * <li>the given node is a <code>BlockNode</code> containing no non-empty
     * statements.
     * </ul>
     *
     * @param body
     *            the <code>TreeNode</code> to check for contents.
     * @return the truth of whether the given node is empty.
     */
    private boolean _containsNoStatements(TreeNode body) {
        if (body == null) {
            return true;
        }

        if (body instanceof EmptyStmtNode) {
            return true;
        }

        if (body instanceof BlockNode) {
            List statementList = ((BlockNode) body).getStmts();
            if (DebugUtil.getFirstNonemptyStatementIndex(statementList, body) == -1) {
                return true;
            }
        }

        return false;
    }

    //    /**
    //     *
    //     */
    //    private void _debugPrint(TreeNode node) {
    //        List nextLines = (List) node.getProperty(NEXT_LINENUMBERS_KEY);
    //        if (nextLines != null && nextLines.contains(NullValue.instance)) {
    //            StringBuffer buf = new StringBuffer();
    //            buf.append(DebugUtil.getNodeFileName(node));
    //            buf.append(":");
    //            buf.append(node.getProperty(LINENUMBER_KEY));
    //            buf.append(" ");
    //            buf.append(DebugUtil.getNodeKind(node));
    //            buf.append(" \"");
    //            buf.append(DebugUtil.getNodeName(node));
    //            buf.append("\"");
    //            System.err.println(buf.toString());
    //        }
    //    }

    /**
     *
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        // _debugPrint(node);
        return null;
    }

    // /**
    // * Return the LINENUMBER_KEY property of this TreeNode, if it generates
    // * C++ code. If not, recursively call this on the first subnode of this
    // * node that does. A LabeledBlockNode, for example, does not appear
    // * in C++ code output if it is not refered to by some constraint or
    // * await() statement.
    // * <p>
    // * As a side-effect, if the passed-in TreeNode is the node whose line
    // * number is returned, set its NEXT_LINENUMBERS_KEY property to an empty
    // * Vector, unless that property already has a value. The empty Vector
    // * is used to clear the ProgramCounter's list of next executable lines
    // * when it enters the critical section (which is to say, when it becomes
    // * unblocked.)
    // */
    // private Object _getFirstCriticalSectionLineNumber(TreeNode node) {
    // Object lineNumberToReturn = null;
    // List statementList = null;
    //
    // if (node == null) {
    // return null;
    // }
    //
    // if (node instanceof ActionLabelStmtNode &&
    // node.getProperty(EVENTID_KEY) == null) {
    // statementList = ((ActionLabelStmtNode)node).getStmts();
    // if (statementList == null) {
    // return null;
    // }
    // return _getFirstCriticalSectionLineNumber(
    // (TreeNode)statementList.get(0));
    // }
    //
    // if (node instanceof LabeledBlockNode &&
    // node.getProperty(EVENTID_KEY) == null) {
    // statementList = ((LabeledBlockNode)node).getStmts();
    // if (statementList == null) {
    // return null;
    // }
    // return _getFirstCriticalSectionLineNumber(
    // (TreeNode)statementList.get(0));
    // }

    // if (node instanceof LabeledStmtNode &&
    // node.getProperty(EVENTID_KEY) == null) {
    // return _getFirstCriticalSectionLineNumber(
    // (TreeNode)((LabeledStmtNode)node).getStmt());
    // }

    // lineNumberToReturn = node.getProperty(LINENUMBER_KEY);

    // // At the first line of the critical section, we want to insert
    // // code to clear the ProgramCounter's list of next executable
    // // lines to set breakpoints at. To do that, set the property
    // // with an empty Vector, but only if property hasn't already
    // // been set. E.g., first line of critical section might be an
    // // await().

    // // FIXME: This test should not be necessary & should be removed,
    // // now that the tree is traversed parents-first.

    // // if (node.getProperty(NEXT_LINENUMBERS_KEY) == null) {
    // node.setProperty(NEXT_LINENUMBERS_KEY, new Vector(0));
    // // }

    // return lineNumberToReturn;
    // }

    //    /**
    //     * Return a message that that named <i>action label</i> must be formatted
    //     * to meet the debugger's needs.
    //     *
    //     * @param name
    //     *            The statement's label.
    //     * @return The error message.
    //     */
    //    private String _getActionLabelFormatError(String name) {
    //        StringBuffer buf = new StringBuffer();
    //        buf.append("To do metamodel debugging, ");
    //        buf.append("Action Label");
    //        buf.append(" \"");
    //        buf.append(name);
    //        buf.append("\" must have its ");
    //        buf.append("label, first statement, and closing brace all ");
    //        buf.append("on separate lines.");
    //        return buf.toString();
    //    }
    //
    //    /**
    //     * Return a message that that an <i>await</i> statement must be formatted
    //     * to meet the debugger's needs.
    //     *
    //     * @return The error message.
    //     */
    //    private String _getAwaitFormatError() {
    //        StringBuffer buf = new StringBuffer();
    //        buf.append("To do metamodel debugging, ");
    //        buf.append("an await statement's critical section must begin ");
    //        buf.append("on a separate line from the \"await\" keyword.");
    //        return buf.toString();
    //    }
    //
    //    /**
    //     * Return a message that that named <i>labeled statement</i> must be
    //     * formatted to meet the debugger's needs.
    //     *
    //     * @param kind
    //     *            The kind of labeled statement.
    //     * @param name
    //     *            The statement's label.
    //     * @return The error message.
    //     */
    //    private String _getLabelFormatError(String kind, String name) {
    //        StringBuffer buf = new StringBuffer();
    //        buf.append("To do metamodel debugging, ");
    //        buf.append(kind);
    //        buf.append(" \"");
    //        buf.append(name);
    //        buf.append("\" must be a block (surrounded by braces), and its ");
    //        buf.append("label, first statement, and closing brace must each ");
    //        buf.append("be on a separate line.");
    //        return buf.toString();
    //    }

    /**
     *
     */
    private Object _getNextSibling(TreeNode node) {
        return node.getProperty(NEXT_SIBLING_LINENUMBER_KEY);
    }

    /**
     * Set the next lines for a block of statements.
     *
     * @param statementList
     *            The list of statements.
     * @param node
     *            The TreeNode to which the list of statements belong, for error
     *            reporting in case the list contains a null.
     * @return Always return null.
     */
    public Object _setNextLinesForBlockOfStatements(List statementList,
            TreeNode node) {
        TreeNode firstStatement = null;
        int firstStatementIndex = 0;
        TreeNode lastStatement = null;
        int lastStatementIndex = 0;

        lastStatementIndex = DebugUtil.getLastNonemptyStatementIndex(
                statementList, node);

        if (lastStatementIndex < 0) {
            return null;
        }

        firstStatementIndex = DebugUtil.getFirstNonemptyStatementIndex(
                statementList, node);

        if (firstStatementIndex < 0) {
            return null;
        }

        lastStatement = (TreeNode) statementList.get(lastStatementIndex);
        firstStatement = (TreeNode) statementList.get(firstStatementIndex);

        _setNextSibling(lastStatement, _getNextSibling(node));
        _addNextLineNumber(lastStatement, _getNextSibling(lastStatement));
        _setNextLinesForSwitchAndBlock(statementList, lastStatementIndex);

        // The NEXT_LINENUMBER of the block itself is the NEXT_SIBLING of
        // its first statement.

        if (firstStatement != null && firstStatement != lastStatement) {
            // If there's more than one statement in the block...
            _addNextLineNumber(node, _getNextSibling(firstStatement));
        }

        return null;
    }

    /**
     * Step backwards through the given list of statements, setting each one's
     * NEXT_SIBLING_LINENUMBER_KEY property to the line number of the statement
     * following it, provided they are on separate lines. Return the index in
     * the list of the first non-empty statement before the last one.
     *
     * @param statements
     *            The <code>List</code> of statements to process.
     * @param lastStatementIndex
     *            The index in the list of the last non-empty statement.
     */
    private void _setNextLinesForSwitchAndBlock(List statements,
            int lastStatementIndex) {

        int i = 0;
        int j = 0;

        for (i = lastStatementIndex - 1, j = i + 1; i >= 0; i--) {
            TreeNode thisOne = (TreeNode) statements.get(i);
            if (thisOne instanceof EmptyStmtNode) {
                continue;
            }
            TreeNode nextOne = (TreeNode) statements.get(j);
            Integer thisLineNumber = (Integer) thisOne
                    .getProperty(LINENUMBER_KEY);
            Integer nextLineNumber = (Integer) nextOne
                    .getProperty(LINENUMBER_KEY);
            if (thisLineNumber == null) {
                throw new MMDebugException(thisOne, "No line number property.");
            }
            if (nextLineNumber == null) {
                throw new MMDebugException(nextOne, "No line number property.");
            }
            if (nextLineNumber.compareTo(thisLineNumber) > 0) {
                _setNextSibling(thisOne, nextLineNumber);
            } else {
                Integer nextBlock2ndLineNumber = (Integer) nextOne
                        .getProperty(BLOCK_SECOND_LINENUMBER_KEY);
                if (nextBlock2ndLineNumber != null
                        && nextBlock2ndLineNumber.compareTo(thisLineNumber) > 0) {
                    // The next statement is a block that starts on this same
                    // line, but has a subsequent member statement on a
                    // subsequent line. (Oy!)
                    _setNextSibling(thisOne, nextBlock2ndLineNumber);
                } else {
                    // The next statement is on this same line, so propogate
                    // its next-sibling to this statement.
                    _setNextSibling(thisOne, _getNextSibling(nextOne));
                }
            }
            _addNextLineNumber(thisOne, _getNextSibling(thisOne));
            do {
                j--;
                nextOne = (TreeNode) statements.get(j);
            } while (nextOne instanceof EmptyStmtNode);
        }
    }

    /**
     *
     */
    private void _setNextSibling(TreeNode node, Object siblingObject) {
        node.setProperty(NEXT_SIBLING_LINENUMBER_KEY, siblingObject);
    }

    /**
     * Add to this <code>LoopNode</code>'s <code>NEXT_LINENUMBERS_KEY</code>
     * property the line number of the first statement in its body. Also, if the
     * body is not a <code>BlockNode</code> and not empty, set the body's
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to be the same as
     * that of the <code>LoopNode</code> itself.
     *
     * @param node
     *            The <code>LoopNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    private Object _visitDoLoopNode(LoopNode node, LinkedList args) {
        TreeNode body = node.getForeStmt();
        boolean bodyIsEmpty = _containsNoStatements(body);
        Integer loopTestLineNumber = (Integer) node
                .getProperty(DOLOOP_TEST_LINENUMBER_KEY);

        _addNextLineNumber(node, loopTestLineNumber);

        if (!bodyIsEmpty) {
            _addNextLineNumber(node, ((TreeNode) body)
                    .getProperty(LINENUMBER_KEY));
            if (body instanceof BlockNode) {
                List statementList = ((BlockNode) body).getStmts();
                int firstIndex = DebugUtil.getFirstNonemptyStatementIndex(
                        statementList, body);
                ((TreeNode) statementList.get(firstIndex)).setProperty(
                        NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            } else {
                _setNextSibling(body, loopTestLineNumber);
                _addNextLineNumber(body, loopTestLineNumber);
                body.setProperty(NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            }
        }

        // _defaultVisit(node,null);

        return null;
    }

    /**
     * Add to this <code>LoopNode</code>'s <code>NEXT_LINENUMBERS_KEY</code>
     * property the line number of the first statement in its body. Also, if the
     * body is not a <code>BlockNode</code> and not empty, set the body's
     * <code>NEXT_SIBLING_LINENUMBER_KEY</code> property to the line number of
     * the <code>LoopNode</code> itself.
     *
     * @param node
     *            The <code>LoopNode</code> to analyze.
     * @param args
     *            A <code>LinkedList</code> that is ignored.
     * @return <code>null</code>.
     */
    private Object _visitWhileLoopNode(LoopNode node, LinkedList args) {
        TreeNode body = node.getAftStmt();
        boolean bodyIsEmpty = _containsNoStatements(body);
        Integer myLineNumber = (Integer) node.getProperty(LINENUMBER_KEY);

        _addNextLineNumber(node, myLineNumber);

        if (!bodyIsEmpty) {
            Integer bodyLineNumber = (Integer) body.getProperty(LINENUMBER_KEY);
            if (bodyLineNumber != null) {
                if (bodyLineNumber.compareTo((Integer) myLineNumber) > 0) {
                    _addNextLineNumber(node, bodyLineNumber);
                } else {
                    Integer secondLineNumber = (Integer) body
                            .getProperty(BLOCK_SECOND_LINENUMBER_KEY);
                    if (secondLineNumber != null) {
                        _addNextLineNumber(node, secondLineNumber);
                    }
                }
            }
            if (body instanceof BlockNode) {
                List statementList = ((BlockNode) body).getStmts();
                int firstIndex = DebugUtil.getFirstNonemptyStatementIndex(
                        statementList, body);
                ((TreeNode) statementList.get(firstIndex)).setProperty(
                        NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            } else {
                _setNextSibling(body, myLineNumber);
                _addNextLineNumber(body, myLineNumber);
                body.setProperty(NO_LINENUMBER_POP_KEY, Boolean.TRUE);
            }
        }

        // _defaultVisit(node,null);

        return null;
    }
}
