/* Generate SystemC code from a meta-model AST to a fle.

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
import metropolis.metamodel.ITreeNode;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.NullValue;
import metropolis.metamodel.StringManip;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.backends.systemc.mmdebug.DebugUtil;
import metropolis.metamodel.backends.systemc.mmdebug.LineList;
import metropolis.metamodel.frontend.MetaModelDecl;
import metropolis.metamodel.frontend.MethodDecl;
import metropolis.metamodel.frontend.ObjectDecl;
import metropolis.metamodel.frontend.TypeParameterDecl;
import metropolis.metamodel.nodetypes.*;
import metropolis.metamodel.runtime.BuiltInLOC;
import metropolis.metamodel.runtime.Constraint;
import metropolis.metamodel.runtime.Event;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.Network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

// ////////////////////////////////////////////////////////////////////////
// // SystemCCodegenVisitor
/**
 * Generate SystemC code from a meta-model AST to a file.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Guang Yang
 * @version $Id: SystemCCodegenVisitor.java,v 1.142 2005/10/24 23:12:00 allenh
 *          Exp $
 */
public class SystemCCodegenVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Set the traversal method to custom so that indent level can be properly
     * set.
     */
    public SystemCCodegenVisitor() {
        this(false, true);
    }

    /**
     * Set the traversal method to custom so that indent level can be properly
     * set, and allow turning on output of "<code>#line</code>" directives
     * to the generated cpp code, to support debugging metamodel code.
     *
     * @param doingMMDebug
     *            <code>true</code> if Extra information, including "<code>#line <i>&lt;line_number&gt; &lt;file_name&gt;</i></code>"
     *            directives, is to be included in the generated
     *            <code>.cpp</code> code, for use by the metamodel debugger.
     *            Default is <code>false</code>.
     * @param ic
     *            A flag indicating whether to perform interleaving concurrent
     *            optimization.
     */
    public SystemCCodegenVisitor(boolean doingMMDebug, boolean ic) {
        super(TM_CUSTOM);
        _doingMMDebug = doingMMDebug;
        _labels = new AutoLabel(this);
        _ic = ic;
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Returns the code represented by an <code>ActionConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>lfo(FORMULA);</code>
     */
    /*
     * public Object visitActionConstraintNode (ActionConstraintNode node,
     * LinkedList args) { LineList retList = new LineList(node, _doingMMDebug);
     *
     * retList.addLast(indent(indentLevel)); retList.addLast("lfo(");
     * retList.addLast(node.getFormula().accept(this, args));
     * retList.addLast(");\n");
     *
     * return retList; }
     */

    /**
     * Returns the code represented by an <code>LTLConstraintNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>ltl(FORMULA);</code>
     */
    public Object visitLTLConstraintNode(LTLConstraintNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast("ltl(");
        retList.addLast(node.getFormula().accept(this, args));
        retList.addLast(");\n");

        return retList;
    }

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
        LineList retList = new LineList(node, _doingMMDebug);

        /***********************************************************************
         * Old code to handle synch **** _header.visitConstraintBlockNode(node,
         * args);
         *
         * TreeNode parent = node.getParent(); while (!(parent instanceof
         * ConstructorDeclNode || parent instanceof MethodDeclNode)) parent =
         * parent.getParent(); if (parent instanceof ConstructorDeclNode) {
         * parent = ((ConstructorDeclNode)parent).getParent(); } else if (parent
         * instanceof MethodDeclNode) { parent =
         * ((MethodDeclNode)parent).getParent(); } boolean parent_was_object =
         * _header.wasParentObject(((UserTypeDeclNode)parent).classID());
         *
         * retList.add("\n"); retList.add(indent(indentLevel));
         *
         * if (parent_was_object) { retList.add("void ");
         * retList.addLast(((UserTypeDeclNode) parent).getName().getIdent());
         * retList.addLast("::synch(int mode) {\n"); } else retList.add("void
         * synch(int mode) {\n");
         *
         * increaseIndent(); retList.addLast(node.accept(new
         * SynchCodegenVisitor(indentLevel), args)); decreaseIndent();
         *
         * retList.add(indent(indentLevel)); retList.add("}\n\n");
         *
         * retList.add(indent(indentLevel)); if (parent_was_object) {
         * retList.add("void "); retList.addLast(((UserTypeDeclNode)
         * parent).getName().getIdent()); retList.addLast("::ltl() {\n"); } else
         * retList.add("void ltl() {\n");
         *
         * increaseIndent(); retList.addLast(node.accept(new
         * LTLCodegenVisitor(indentLevel), args)); decreaseIndent();
         *
         * retList.add(indent(indentLevel)); retList.add("}\n\n");
         *
         **********************************************************************/
        return retList;
    }

    /**
     * Returns the code represented by an <code>AwaitLockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>NODE.IFACE</code>
     */

    public Object visitAwaitLockNode(AwaitLockNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug, true);
        LineList.setExplicitLineNumbering(true);

        retList.addLast(indent(indentLevel));
        retList.addLast("addPortIntfcEntry(list, \"");

        if (node.getNode() instanceof AbsentTreeNode)
            retList.addLast("all");
        else
            retList.addLast(node.getNode().accept(this, args));

        retList.addLast("\", \"");

        if (node.getIface() instanceof AbsentTreeNode)
            retList.addLast("all");
        else
            retList.addLast(node.getIface().accept(this, args));

        retList.addLast("\"");
        if (!((String) args.get(0)).equals("Process"))
            retList.addLast(", pc");
        retList.addLast(");\n");

        LineList.setExplicitLineNumbering(false);
        return retList;
    }

    /**
     * Returns the code represented by an <code>AwaitStatementNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>await { GUARDS }</code>
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        boolean ica = false;
        if (_ic) {
            ICASymbolicFlag f = (ICASymbolicFlag) node
                    .getProperty(ICATOMIC_KEY);
            if (f == null) {
                ica = false;
            } else {
                ica = f.getICA();
            }
        }
        List guards = node.getGuards();
        Iterator guardsIter;

        retList.addLast("\n");
        retList.addLast(indent(indentLevel));
        retList.addLast("{\n");
        if (_doingMMDebug) {
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->setCurrentLineNumber(\""
                    + DebugUtil.getCurrentLineNumberString(node) + "\");\n");
            // retList.addLast(indent(indentLevel));
            // retList.addLast("gAllCurrentLines=gMmdbSB->getAllCurrentLines();\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->eventTag=pc->BEG_TAG; gAllEventTags = "
                    + "gMmdbSB->getAllEventTags();\n");
        }
        if (!ica) {
            retList.addLast(indent(indentLevel));
            retList.addLast("INIT(AWAIT)\n\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("BEGIN_AWAIT_STATEMENT(" + guards.size() + ")\n");

            retList.addLast(indent(indentLevel));
            retList.addLast("//set testList for await statement\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("tslist = new _2DList_t;\n");
            guardsIter = guards.iterator();
            while (guardsIter.hasNext()) {
                retList.addLast(indent(indentLevel));
                retList.addLast("list = new simpleList_t;\n");
                retList.addLast(TNLManip.traverseList(this, args,
                        ((AwaitGuardNode) guardsIter.next()).getLockTest()));
                retList.addLast(indent(indentLevel));
                retList.addLast("tslist->push_back(list);\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->testList.push_back(tslist);\n\n");

            retList.addLast(indent(indentLevel));
            retList.addLast("//set setList for await statement\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("tslist = new _2DList_t;\n");
            guardsIter = guards.iterator();
            while (guardsIter.hasNext()) {
                retList.addLast(indent(indentLevel));
                retList.addLast("list = new simpleList_t;\n");
                retList.addLast(TNLManip.traverseList(this, args,
                        ((AwaitGuardNode) guardsIter.next()).getLockSet()));
                retList.addLast(indent(indentLevel));
                retList.addLast("tslist->push_back(list);\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->setList.push_back(tslist);\n\n");

            retList.addLast(indent(indentLevel));
            retList.addLast("do {\n");
            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("do {\n");
            increaseIndent();
            if (_doingMMDebug) {
                // Process may wait() in ASK_EVALUATE, so record current line #
                // and event tag:
                retList.addLast(indent(indentLevel));
                retList
                        .add("pc->setCurrentLineNumber(\""
                                + DebugUtil.getCurrentLineNumberString(node)
                                + "\");\n");
                retList.addLast(indent(indentLevel));
                retList
                        .addLast("gAllCurrentLines=gMmdbSB->getAllCurrentLines();\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->eventTag=pc->BEG_TAG; "
                        + "gAllEventTags = gMmdbSB->getAllEventTags();\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("ASK_EVALUATE;\n");
            if (_doingMMDebug) {
                retList.addLast(indent(indentLevel));
                retList.addLast("gProcessName = pc->p->name();\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->setState(this, intfcName, "
                    + "Keep_Unchanged_String, SchedStateVal::UNKNOW);\n");
            guardsIter = guards.iterator();
            int condNum = 0;
            while (guardsIter.hasNext()) {
                _labels.push();
                if (_doingMMDebug) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("pc->enterAwaitTest();\n");
                }
                retList.addLast(indent(indentLevel));
                retList.addLast("try {\n");
                increaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("if (mask[" + condNum + "]){\n");
                increaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast(" condition[" + condNum + "] = (");
                AwaitGuardNode curNode = (AwaitGuardNode) guardsIter.next();
                if (curNode.getCond() instanceof AbsentTreeNode) {
                    boolean flag = false;
                    for (int i = 0; i < guards.size(); i++) {
                        if (i != condNum) {
                            if (flag)
                                retList.addLast(" && ");
                            else
                                flag = true;
                            retList.addLast("!condition[" + i + "]");
                        }
                    }
                } else {
                    /*
                     * change it to pc->mode args.set(1, new
                     * String("RunType::TRY"));
                     */
                    retList.addLast(curNode.getCond().accept(this, args));
                }
                retList.addLast(");\n");
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("} else condition[" + condNum + "] = false;\n");
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("} catch (STUCK) {condition[" + condNum
                        + "] = false; }\n");
                if (_doingMMDebug) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("pc->exitAwaitTest();\n");
                }
                _labels.pop();
                condNum++;
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->cond = condition;\n");

            guardsIter = guards.iterator();
            condNum = 0;
            while (guardsIter.hasNext()) {
                ITreeNode stmt = ((AwaitGuardNode) guardsIter.next()).getStmt();
                while (stmt instanceof BlockNode) {
                    List stmts = ((BlockNode) stmt).getStmts();
                    if (stmts.size() == 0)
                        stmt = null;
                    else
                        stmt = (ITreeNode) stmts.get(0);
                }

                boolean haveEvent = false;
                if (stmt instanceof ActionLabelStmtNode) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("if (condition[" + condNum
                            + "]) pc->addPendingEvent(beg(pc->p,");
                    retList.addLast("this, \"");
                    retList.addLast(((ActionLabelStmtNode) stmt).getLabel()
                            .accept(this, args));
                    retList.addLast("\"), " + condNum + ");\n");
                    stmt = (ITreeNode) ((ActionLabelStmtNode) stmt).getStmts()
                            .get(0);
                    haveEvent = true;
                }

                if (stmt instanceof ExprStmtNode) {
                    stmt = ((ExprStmtNode) stmt).getExpr();
                    if (stmt instanceof AssignNode) {
                        stmt = ((AssignNode) stmt).getExpr2();
                    }
                    if (stmt instanceof MethodCallNode) {
                        stmt = ((MethodCallNode) stmt).getMethod();
                        if (stmt instanceof ObjectFieldAccessNode) {
                            retList.addLast(indent(indentLevel));
                            retList.addLast("if (condition[" + condNum
                                    + "]) pc->addPendingEvent(beg(pc->p, ");

                            ObjectFieldAccessNode obj = (ObjectFieldAccessNode) stmt;

                            if (obj.getObject() instanceof ThisPortAccessNode) {
                                // Interface function call
                                retList.addLast("dynamic_cast<sc_object*>(");
                                retList.addLast(obj.getObject().accept(this,
                                        args));
                                retList.addLast(".get_interface()), \"");
                                retList.addLast(obj.getName()
                                        .accept(this, args));
                                retList.addLast("\"), " + condNum + ");\n");
                            } else {
                                retList.addLast("this, \"");
                                retList.addLast(obj.getObject().accept(this,
                                        args));
                                retList.addLast("->");
                                retList.addLast(obj.getName()
                                        .accept(this, args));
                                retList.addLast("\"), " + condNum + ");\n");
                            }
                            haveEvent = true;
                        } else if (stmt instanceof ThisFieldAccessNode) {
                            retList.addLast(indent(indentLevel));
                            retList.addLast("if (condition[" + condNum
                                    + "]) pc->addPendingEvent(beg(pc->p,");
                            retList.addLast("this, \"");
                            retList.addLast(((ThisFieldAccessNode) stmt)
                                    .getName().accept(this, args));
                            retList.addLast("\"), " + condNum + ");\n");
                            haveEvent = true;
                        }
                    }
                }

                if (!haveEvent) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("if (condition[" + condNum
                            + "]) pc->addPendingEvent(beg(pc->p,");
                    retList.addLast("this, \"" + condNum + "\"), " + condNum
                            + ");\n");
                }

                condNum++;
            }

            retList.addLast("\n");
            if (_doingMMDebug) {
                // Process may wait() in ASK_MANAGER, so record current line #
                // and event tag:
                retList.addLast(indent(indentLevel));
                retList
                        .add("pc->setCurrentLineNumber(\""
                                + DebugUtil.getCurrentLineNumberString(node)
                                + "\");\n");
                retList.addLast(indent(indentLevel));
                retList
                        .addLast("gAllCurrentLines=gMmdbSB->getAllCurrentLines();\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->eventTag=pc->BEG_TAG; "
                        + "gAllEventTags = gMmdbSB->getAllEventTags();\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("ASK_MANAGER;\n");
            if (_doingMMDebug) {
                retList.addLast(indent(indentLevel));
                retList.addLast("gProcessName = pc->p->name();\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->freeAllPendingEvents();\n");
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("} while (pc->SchedState != SchedStateVal::RUN && "
                    + "pc->mode==RunType::EXEC);\n\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("if (pc->SchedState == SchedStateVal::RUN) {\n");
            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->SchedState = SchedStateVal::RUNNING;\n\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("BEGIN_PREVENT_LIST;\n");
        }// end if !ica
        retList.addLast(indent(indentLevel));
        if (ica) {
            retList.addLast("switch (nondeterminism(2) %" + guards.size()
                    + ")\n");
        } else {
            retList.addLast("switch (pc->selected)\n");
        }// end if !ica
        retList.addLast(indent(indentLevel));
        retList.addLast("{\n");
        increaseIndent();
        guardsIter = guards.iterator();
        int condNum = 0;
        while (guardsIter.hasNext()) {
            AwaitGuardNode currentGuardNode = (AwaitGuardNode) guardsIter
                    .next();
            retList.addLast(indent(indentLevel));
            retList.addLast("case " + condNum + ":\n");
            increaseIndent();

            if (_doingMMDebug) {
                retList.addAwaitGuardSetup(currentGuardNode);
            }
            if (!ica) {
                retList.addLast(indent(indentLevel));
                _labels.push();
                retList.addLast("try {\n");
            }// end if !ica

            increaseIndent();
            retList.addLast(currentGuardNode.getStmt().accept(this, args));
            decreaseIndent();

            if (!ica) {
                retList.addLast(indent(indentLevel));
                retList.addLast("} catch (STUCK) {\n");
                increaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("END_PREVENT_LIST;\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("mask[" + condNum + "] = false;\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("continue;\n");
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}\n");
                retList.addLast(_labels.getCatchStatement());
                _labels.pop();
            }// end if !ica
            retList.addLast(indent(indentLevel));
            retList.addLast("break;\n");
            decreaseIndent();
            condNum++;
        }// end while guardsIter
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");
        if (!ica) {
            retList.addLast(indent(indentLevel));
            retList.addLast("END_PREVENT_LIST;\n");
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("}\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("else stuck_flag = true;\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("flag = false;\n");
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("} while (flag);\n\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("END_AWAIT_STATEMENT;\n");
            retList.addLast(_labels.getReturnStatement());
        }// end if !ica
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Returns the code represented by an <code>LabeledBlockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>block(LABEL) { STATEMENTS }</code>
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {

        LineList retList = new LineList(node, _doingMMDebug, true);

        String type = args.get(0).toString();

        if (type.equals("StateMedium")) {
            increaseIndent();
            retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
            decreaseIndent();
            return retList;
        }

        return _getAnnotatedBodyCode(node.getStmts(), args, "LABEL",
                _stringListToString((List) node.getLabel().accept(this, args)),
                node);
    }

    /**
     * Returns the code represented by an <code>LocalLabelNode</code>.
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
     * Returns the code represented by an <code>GlobalLabelNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> label(OBJECT, LABEL) </code>
     */
    public Object visitGlobalLabelNode(GlobalLabelNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("label(");
        retList.addLast(node.getObj().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>ForallActionNode</code>.
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

    /**
     * Returns the code represented by an <code>ExistsActionNode</code>.
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
     * Returns the code represented by an <code>ExprActionNode</code>.
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
     * Returns the code represented by an <code>ExprLTLNode</code>.
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
     * Returns the code represented by an <code>MutexLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> mutex(event1, event2) </code>
     */
    public Object visitMutexLTLNode(MutexLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "mutex");
    }

    /**
     * Returns the code represented by an <code>SimulLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> simul(event1, event2) </code>
     */
    public Object visitSimulLTLNode(SimulLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "simul");
    }

    /**
     * Returns the code represented by an <code>ExclLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> exec(event1, event2) </code>
     */
    public Object visitExclLTLNode(ExclLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "excl");
    }

    /**
     * Returns the code represented by an <code>PriorityLTLNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> priority(event1, event2) </code>
     */
    public Object visitPriorityLTLNode(PriorityLTLNode node, LinkedList args) {
        return _visitBuiltInLTLNode(node, args, "priority");
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
        return _visitSingleLTLFormulaNode(node, args, "F");
    }

    /**
     * Returns the code represented by an <code>GloballyLTLNode</code>.
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
     * Returns the code represented by an <code>NextLTLNode</code>.
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
     * Returns the code represented by an <code>UntilLTLNode</code>.
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
     * Returns the code represented by an <code>NonDeterminismNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("nondeterminism(sizeof(");
        retList.addLast(node.getType().accept(this, args));
        retList.addLast("))");

        return retList;
    }

    /**
     * Returns the code represented by an <code>ExecIndexNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> execindex(ACTION)</code>
     */
    public Object visitExecIndexNode(ExecIndexNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("execindex(");
        retList.addLast(node.getAction().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>BoundedLoopNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> boundedloop(VAR, COUNT) { STMT } </code>
     */
    public Object visitBoundedLoopNode(BoundedLoopNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>CifNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 -> EXPR2 </code>
     */
    public Object visitCifNode(CifNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("(!");
        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(") || ");
        retList.addLast(node.getExpr2().accept(this, args));

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
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("(");
        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" == ");
        retList.addLast(node.getExpr2().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>BeginPCNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> beg(LABEL) </code>
     */
    public Object visitBeginPCNode(BeginPCNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("beg(");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>EndPCNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> end(LABEL) </code>
     */
    public Object visitEndPCNode(EndPCNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("end(");
        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>PCNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> pc(EXPR) </code>
     */
    public Object visitPCNode(PCNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("pc(");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>RefineNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> refine(NODE, NETLIST) </code>
     */
    public Object visitRefineNode(RefineNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>RefineConnectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>refineconnect(NETLIST, SRCOBJECT, PORT, COMPONENT)</code>
     */
    public Object visitRefineConnectNode(RefineConnectNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>ConnectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>connect(SRCOBJECT, PORT, DSTOBJECT)</code>
     */
    public Object visitConnectNode(ConnectNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>RedirectConnectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>redirectconnect(NETLIST, SRCOBJECT, SRCPORT,
     *                                  COMPONENT, NEWPORT)</code>
     */
    public Object visitRedirectConnectNode(RedirectConnectNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>AddComponentNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> addcomponent(NODE, NETLIST) </code>
     */
    public Object visitAddComponentNode(AddComponentNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast("addcomponent(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(")");
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Returns the code represented by an <code>SetScopeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> setscope(NODE, pORT, NETLIST) </code>
     */
    public Object visitSetScopeNode(SetScopeNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>BlackboxNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> blackbox(IDENT) %% CODE %% </code>
     */
    public Object visitBlackboxNode(BlackboxNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        String bbname = node.getIdent();
        if (bbname.equals("SystemCSim")) {
            boolean goToHeader;

            if (node.getParent() instanceof CompileUnitNode)
                goToHeader = true;
            else
                goToHeader = _header
                        .wasParentObject(node.getParent().classID());

            if (goToHeader)
                _header.visitBlackboxNode(node, args);
            else {
                retList.addLast("\n");
                retList.addLast(indent(indentLevel));
                retList.addLast(node.getCode());
                retList.addLast("\n");
            }
        }
        return retList;
    }

    /**
     * Returns the code represented by an <code>GetConnectionNumNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getconnectionnum(");
        retList.addLast(node.getMedium().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>GetNthConnectionSrcNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>GetNthConnectionPortNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>PortDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>port DEFTYPE NAME;</code>
     */
    public Object visitPortDeclNode(PortDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        _inObjectDef = true;
        _header.visitPortDeclNode(node, args);
        LinkedList defType = (LinkedList) node.getDefType().accept(this, args);
        _inObjectDef = false;
        LinkedList portName = (LinkedList) node.getName().accept(this, args);
        String portType = _stringListToString(defType).trim();
        String pointer = "";
        int index = portType.lastIndexOf('>');
        if (index == -1)
            index = portType.indexOf('*');
        else {
            index++;
            index += portType.substring(index).indexOf('*');
        }
        if (index > 0) {
            pointer = portType.substring(index);
            portType = portType.substring(0, index);
        }
        int dim = 0;
        for (int i = 0; i < pointer.length(); i++)
            if (pointer.charAt(i) == '*')
                dim++;
        String[] value = new String[2];
        value[0] = portType;
        value[1] = String.valueOf(dim);
        _ports.put(_stringListToString(portName), value);

        return retList;
    }

    /**
     * Returns the code represented by an <code>ParameterDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> MODIFIER parameter DEFTYPE NAME; </code>
     */
    public Object visitParameterDeclNode(ParameterDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        _header.visitParameterDeclNode(node, args);

        boolean parent_was_object = _header.wasParentObject(node.getParent()
                .classID());

        if (!parent_was_object) {
            retList.addLast(indent(indentLevel));
            retList.addLast(Modifier.toString(node.getModifiers()));
            // retList.addLast("parameter ");
            retList.addLast(node.getDefType().accept(this, args));
            // TreeNode baseType = node.getDefType();
            // while (baseType instanceof ArrayTypeNode)
            // baseType = ((ArrayTypeNode)baseType).getBaseType();
            // if (baseType instanceof TypeNameNode) {
            // String type = ((TypeNameNode) baseType).getName().getIdent();
            // //if (!type.equals("String")) retList.addLast("*");
            // if (! (((TypeNameNode)baseType).getName().getProperty(DECL_KEY)
            // instanceof TypeParameterDecl) )
            // retList.addLast("*");
            // }
            retList.addLast(" ");
            retList.addLast(node.getName().accept(this, args));
            retList.addLast(";\n");
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>ProcessDeclNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _objectName = _stringListToString((LinkedList) node.getName().accept(
                this, args)); // xichen
        _inObjectDef = true;
        _header.visitProcessDeclNodeBegin(node, args);
        _inObjectDef = false;

        increaseIndent();
        args.set(0, new String("Process"));
        _ports = new Hashtable();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();
        retList.addLast(_getDummyConstructorCode(node, args));
        _ports = null;

        _header.visitProcessDeclNodeEnd(node, args);

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>MediumDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> template(TYPE1, TYPE2, ...)
     *                  MODIFIER medium NAME extends SUPERCLASS
     *                          implements INTERFACES { ... } </code>
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _objectName = _stringListToString((LinkedList) node.getName().accept(
                this, args)); // xichen
        _inObjectDef = true;
        _header.visitMediumDeclNodeBegin(node, args);
        _inObjectDef = false;

        increaseIndent();
        args.set(0, new String("Medium"));
        _ports = new Hashtable();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();
        retList.addLast(_getDummyConstructorCode(node, args));
        _ports = null;

        _header.visitMediumDeclNodeEnd(node, args);

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>SchedulerDeclNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

        _objectName = _stringListToString((LinkedList) node.getName().accept(
                this, args)); // xichen
        _inObjectDef = true;
        _header.visitSchedulerDeclNodeBegin(node, args);
        _inObjectDef = false;

        increaseIndent();
        args.set(0, new String("Scheduler"));
        _ports = new Hashtable();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();
        retList.addLast(_getDummyConstructorCode(node, args));
        _ports = null;

        _header.visitSchedulerDeclNodeEnd(node, args);

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>SMDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>template(TYPE1, TYPE2, ...)
     *                  MODIFIER statemedium NAME extends SUPERCLASS
     *                          implements INTERFACES { ... } </code>
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _objectName = _stringListToString((LinkedList) node.getName().accept(
                this, args)); // xichen
        _inObjectDef = true;
        _header.visitSMDeclNodeBegin(node, args);
        _inObjectDef = false;
        increaseIndent();
        args.set(0, new String("StateMedium"));
        _ports = new Hashtable();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();
        retList.addLast(_getDummyConstructorCode(node, args));
        _ports = null;

        _header.visitSMDeclNodeEnd(node, args);

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>NetlistDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>template(TYPE1, TYPE2, ...)
     *                  MODIFIER netlist NAME extends SUPERCLASS
     *                          implements INTERFACES { ... } </code>
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _objectName = _stringListToString((LinkedList) node.getName().accept(
                this, args)); // xichen
        _inObjectDef = true;
        _header.visitNetlistDeclNodeBegin(node, args);
        _inObjectDef = false;

        increaseIndent();
        args.set(0, new String("Netlist"));
        _ports = new Hashtable();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        _ports = null;
        decreaseIndent();

        _header.visitNetlistDeclNodeEnd(node, args);

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>TemplateParametersNode</code>.
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
     * Returns the code represented by an <code>ObjectPortAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> OBJECT.NAME </code>
     */
    public Object visitObjectPortAccessNode(ObjectPortAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getObject().accept(this, args));
        retList.addLast("->");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>SuperPortAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> super.NAME </code>
     */
    public Object visitSuperPortAccessNode(SuperPortAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        TreeNode utdNode = node;

        while (!(utdNode instanceof ClassDeclNode)
                && !(utdNode instanceof InterfaceDeclNode))
            utdNode = utdNode.getParent();

        if (utdNode instanceof ClassDeclNode) {
            TreeNode superClassNode = ((ClassDeclNode) utdNode).getSuperClass();
            if (superClassNode == AbsentTreeNode.instance) {
                // System.err.println("Internal Error (1002). Abort.");
                // System.exit(1);
                retList.addLast("Object");
            } else
                retList.addLast(superClassNode.accept(this, args));
        }

        // retList.addLast("super.");
        retList.addLast("::");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>ThisPortAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> this.NAME </code>
     */
    public Object visitThisPortAccessNode(ThisPortAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>ObjectParamAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> OBJECT.NAME </code>
     */
    public Object visitObjectParamAccessNode(ObjectParamAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getObject().accept(this, args));
        retList.addLast("->");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>ThisParamAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> this.NAME </code>
     */
    public Object visitThisParamAccessNode(ThisParamAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("this->");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>SuperParamAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> super.NAME </code>
     */
    public Object visitSuperParamAccessNode(SuperParamAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        TreeNode utdNode = node;

        while (!(utdNode instanceof ClassDeclNode)
                && !(utdNode instanceof InterfaceDeclNode))
            utdNode = utdNode.getParent();

        if (utdNode instanceof ClassDeclNode) {
            TreeNode superClassNode = ((ClassDeclNode) utdNode).getSuperClass();
            if (superClassNode == AbsentTreeNode.instance) {
                // System.err.println("Internal Error (1003). Abort.");
                // System.exit(1);
                retList.addLast("Object");
            } else
                retList.addLast(superClassNode.accept(this, args));
        }

        // retList.addLast("super.");
        retList.addLast("::");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>PCTypeNode</code>.
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
     * Returns the code represented by a <code>CompileUnitNode</code>. It
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

        // xichen_loc_beg
        _net = (Network) args.getFirst();
        // xichen_loc_end

        // indent level should be currently 0
        LineList retList = new LineList(node, _doingMMDebug);
        if (args == null)
            args = new LinkedList();
        args.addFirst(null);
        args.addFirst(null);
        args.addFirst(null);

        // package
        if (node.getPkg() != AbsentTreeNode.instance) {
            retList.addLast("//package ");
            retList.addLast(node.getPkg().accept(this, args));
            retList.addLast(";\n");
        }

        // include systemc library
        retList.addLast("#include \"library.h\"\n");

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

        args.removeFirst();
        args.removeFirst();
        args.removeFirst();

        return new CompileUnit(_header, retList);
    }

    /**
     * Returns the code represented by a <code>NameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>NAME</code> or
     *         <code> QUALIFIER.NAME-<PARAMETERS>- </code>
     */
    public Object visitNameNode(NameNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        String ident = node.getIdent();
        if (_inTypeNameNode)
            ident = SystemCBackend._convertToCppTemplateFormat(ident);
        TreeNode qualifier = node.getQualifier();

        // if (ident.equals("String")) ident = "sc_string";
        // else if (ident.equals("boolean")) ident = "bool";
        if (ident.equals("boolean"))
            ident = "bool";

        if (qualifier == AbsentTreeNode.instance) {
            retList.addLast(ident);
        } else {
            if (!(node.getParent() instanceof ObjectFieldAccessNode)) {
                retList.addLast(qualifier.accept(this, args));
                retList.addLast(".");
            }
            retList.addLast(ident);
        }

        TreeNode params = node.getParameters();
        if (params instanceof TemplateParametersNode) {
            retList.addLast("<");
            retList.addLast(params.accept(this, args));
            retList.addLast(">");
        }

        if (_inTypeNameNode) {
            int numTypes = 0;
            String typeName = "";
            if (ident.indexOf('<') != -1) {
                int beg = ident.indexOf('<');
                int end = ident.indexOf('>');
                typeName = ident.substring(0, beg);
                String type = ident.substring(beg + 1, end);
                StringTokenizer typeToken = new StringTokenizer(type, ",");
                numTypes = typeToken.countTokens();
                while (typeToken.hasMoreTokens()) {
                    String t = typeToken.nextToken();
                    int star = t.indexOf('*');
                    if (star != -1)
                        t = t.substring(0, star);
                    if (!SystemCBackend._isPrimitiveType(t)) {
                        _header.addIncludeDecl(t);
                    }
                }// end while
            } else {
                /*
                 * Decl d = (Decl) node.getProperty(DECL_KEY); if (d != null) if (!
                 * (d instanceof TypeParameterDecl)) retList.addLast("*");
                 */if (params instanceof TemplateParametersNode) {
                    numTypes = ((TemplateParametersNode) params).getTypes()
                            .size();
                    typeName = ident;
                } else {
                    numTypes = 0;
                    typeName = ident;
                }
            }// end if

            if (node.getProperty(TEMPLDECL_KEY) == null) {
                _header.addIncludeDecl(typeName);
            }

            if (numTypes > 0) {
                // String types = "template <";
                // String sep = "";
                // for (int i=0; i<numTypes; i++) {
                // types += sep + "typename __T" + i;
                // sep = ", ";
                // }
                // types += "> " + typeName;
                // _header.addForwardDecl(types);
                _header.addForwardDecl(typeName, numTypes);
            }
        } // end if (_inTypeNameNode)

        return retList;
    }

    /**
     * Returns the code represented by an <code>ImportNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>import NAME;</code>
     */
    public Object visitImportNode(ImportNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast("//import ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(";\n");

        return retList;
    }

    /**
     * Returns the code represented by an <code>ImportOnDemandNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>import NAME.*;</code>
     */
    public Object visitImportOnDemandNode(ImportOnDemandNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast("//import ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(".*;\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>ClassDeclNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _inObjectDef = true;
        _header.visitClassDeclNodeBegin(node, args);
        _inObjectDef = false;

        increaseIndent();
        Object args0 = args.get(0);
        Object args1 = args.get(1);
        args.set(0, new String("Class"));
        if (_ports == null) {
            _ports = new Hashtable();
            retList.addLast(TNLManip
                    .traverseList(this, args, node.getMembers()));
            decreaseIndent();
            retList.addLast(_getDummyConstructorCode(node, args));
            _ports = null;
        } else {
            Hashtable temp = (Hashtable) _ports.clone();
            retList.addLast(TNLManip
                    .traverseList(this, args, node.getMembers()));
            decreaseIndent();
            retList.addLast(_getDummyConstructorCode(node, args));
            _ports = temp;
        }
        args.set(0, args0);
        args.set(1, args1);

        _header.visitClassDeclNodeEnd(node, args);

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>InterfaceDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER interface NAME extends SUPERCLASS { ... }</code>
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _inObjectDef = true;
        _header.visitInterfaceDeclNodeBegin(node, args);
        _inObjectDef = false;

        increaseIndent();
        args.set(0, new String("Interface"));
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        decreaseIndent();

        _header.visitInterfaceDeclNodeEnd(node, args);

        return retList;
    }

    /**
     * Returns the code represented by a <code>MethodDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>MODIFIER RETURNTYPE NAME (ARGS) { ... }</code>
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug, true);
        String type = args.get(0).toString();
        boolean isThread = (type.equals("Process") && node.getName().getIdent()
                .toString().equals("thread"));

        _header.visitMethodDeclNode(node, args);

        _labels.pushScope();

        boolean parent_was_object = _header.wasParentObject(node.getParent()
                .classID());

        if (!parent_was_object)
            retList.addLast(indent(indentLevel));

        if (node.getBody() instanceof AbsentTreeNode) {
            if (parent_was_object && type.equals("Interface")) {
                return retList;
            }
        }

        LineList.setExplicitLineNumbering(true);

        if (_header.hasTemplate()) {
            retList.addLast(_header.getTemplate());
            retList.addLast("\n");
        }

        boolean isStatic = false;
        boolean isConst = false;
        String mod = Modifier.toString(node.getModifiers());
        StringTokenizer st = new StringTokenizer(mod);
        while (st.hasMoreTokens()) {
            String modifier = st.nextToken();
            if (modifier.equals("final")) {
                retList.addLast("const ");
                isConst = true;
            } else if (modifier.equals("static")) {
                isStatic = true;
                if (!parent_was_object)
                    retList.addLast(modifier + " ");
            }
            // public, protected, private, abstract ignored
        }
        if (isConst & !isStatic) {
            if (!parent_was_object)
                retList.addLast("static ");
            isStatic = true;
        }

        if (!isStatic && !parent_was_object) {
            retList.addLast("virtual ");
        }

        retList.addLast(node.getReturnType().accept(this, args));
        TreeNode baseType = node.getReturnType();
        String btype = null;
        boolean templateType = false;
        while (baseType instanceof ArrayTypeNode) {
            baseType = ((ArrayTypeNode) baseType).getBaseType();
        }
        if (baseType instanceof TypeNameNode) {
            btype = ((TypeNameNode) baseType).getName().getIdent();
            if (((TypeNameNode) baseType).getName().getProperty(DECL_KEY) instanceof TypeParameterDecl) {
                templateType = true;
            } else {
                // retList.addLast("*");
            }
        } else {
            btype = "Primitive";
        }

        retList.addLast(" ");

        if (parent_was_object) {
            retList.addLast(((UserTypeDeclNode) node.getParent()).getName()
                    .getIdent());
            if (_header.hasTemplate()) {
                retList.addLast(_header.getTemplateTypes());
            }
            retList.addLast("::");
        }

        retList.addLast(node.getName().accept(this, args));

        retList.addLast("(");
        LinkedList par = _commaList(TNLManip.traverseList(this, args, node
                .getParams()));
        if (!isThread) {
            retList.addLast("process * caller");
            if (_stringListToString(par).trim().length() > 0) {
                retList.addLast(", ");
            }
        }

        retList.addLast(par);
        retList.addLast(")");

        _methodReturnType = "";

        if (node.getBody() instanceof AbsentTreeNode) {
            if (!parent_was_object && type.equals("Interface")) {
                retList.addLast("= 0");
            }
        } else {

            retList.addLast("\n");
            retList.addLast(indent(indentLevel));

            if ((node.getModifiers() & ELABORATE_MOD) != 0) {

                LineList.setExplicitLineNumbering(false);

                retList.addLast("{\n");
                increaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("return");
                if (node.getReturnType() instanceof VoidTypeNode) {
                    retList.addLast(";\n");
                } else {
                    retList.addLast("(");
                    _inObjectDef = true;
                    retList.addLast(node.getReturnType().accept(this, args));
                    _inObjectDef = false;
                    if (!btype.equals("Primitive") && !templateType) {
                        retList.addLast("*");
                    }
                    retList.addLast(") 0;\n");
                }
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}\n");
            } else if ((!isThread)
                    && (type.equals("Medium") || type.equals("Process"))) {

                retList.addLast("{\n");
                increaseIndent();
                retList.addLast("#undef RETURN\n");
                if (!(node.getReturnType() instanceof VoidTypeNode)) {
                    retList.addLast("#define RETURN retval\n");
                    retList.addLast(indent(indentLevel));
                    _inObjectDef = true;
                    _methodReturnType = _stringListToString((List) node
                            .getReturnType().accept(this, args));
                    _inObjectDef = false;
                    if (!btype.equals("Primitive") && !templateType) {
                        _methodReturnType = _methodReturnType + "*";
                    }
                    retList.addLast(_methodReturnType);
                    retList.addLast(" retval;\n\n");
                } else {
                    retList.addLast("#define RETURN\n");
                }
                retList.addLast(indent(indentLevel));
                retList.addLast("ProgramCounter *pc = caller->pc;\n");
                if (_doingMMDebug) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("gProcessName = pc->p->name();\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("gMmdbSB = pc->getScoreboard();\n");
                    retList.addLast(indent(indentLevel));
                }

                LineList.setExplicitLineNumbering(false);

                // All interfaces that are referred by other objects connected
                // to it or by itself in the form of this.interface
                if (type.equals("Medium")) {
                    MediumDeclNode md = (MediumDeclNode) node.getParent();
                    LinkedList prop = (LinkedList) md.getName().getProperty(
                            REFINTFC_KEY);
                    if (prop != null) {
                        args.addLast(prop);
                    } else {
                        args.addLast(new LinkedList());
                    }
                } else {
                    args.addLast(new LinkedList());
                }

                MethodDecl mdecl = (MethodDecl) node.getName().getProperty(
                        DECL_KEY);
                List implInterfaceDecls = mdecl.getImplements();
                Iterator iter = implInterfaceDecls.iterator();
                LinkedList implInterfaces = new LinkedList();
                while (iter.hasNext()) {
                    MethodDecl md = (MethodDecl) iter.next();
                    ObjectDecl obj = (ObjectDecl) md.getContainer();
                    while (obj != null) {
                        String intfcName = obj.getName();
                        implInterfaces.addLast(intfcName);
                        obj = obj.getSuperClass();
                    }
                }
                args.addLast(implInterfaces);
                retList.addLast(_getAnnotatedBodyCode(((BlockNode) node
                        .getBody()).getStmts(), args, "INTFCFUNC",
                        _stringListToString((List) node.getName().accept(this,
                                args)), node));
                args.removeLast();
                args.removeLast();

                // Replaced by FINISH() macro?
                // if (_doingMMDebug) {
                // retList.addLast(indent(indentLevel));
                // retList.addMethodEndFallthroughCode();
                // }

                if (_doingMMDebug) {
                    retList.addBreakpointDummyStatement((BlockNode) node
                            .getBody());
                }

                retList.addLast(indent(indentLevel));
                retList.addLast("return RETURN;\n");
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}");
            } else {

                retList.addLast("{\n");
                increaseIndent();

                if (_doingMMDebug) {
                    if (!isThread) {
                        retList.addLast(indent(indentLevel));
                        retList.addLast("ProgramCounter *pc = caller->pc;\n");
                    }
                    retList.addLast(indent(indentLevel));
                    retList.addLast("gProcessName = pc->p->name();\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("gMmdbSB = pc->getScoreboard();\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("pc->pushNewNextLineContext();\n");
                }

                LineList.setExplicitLineNumbering(false);

                if (isThread) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("wait(pc->invoke);\n");
                    if (_doingMMDebug) {
                        retList.addLast(indent(indentLevel));
                        retList.addLast("gProcessName = pc->p->name();\n");
                    }
                } else if (type.equals("Quantity")
                        && (node.getName().getIdent().equals("request") || node
                                .getName().getIdent().equals("requestI"))) {
                    retList.addLast("    reqPCs.insert(caller->pc);\n");
                    retList.addLast("    if (!requestQA) return;\n\n");
                }

                retList.addLast("#undef RETURN\n");
                if (!(node.getReturnType() instanceof VoidTypeNode)) {
                    retList.addLast("#define RETURN retval\n");
                    retList.addLast(indent(indentLevel));
                    _inObjectDef = true;
                    _methodReturnType = _stringListToString((List) node
                            .getReturnType().accept(this, args));
                    _inObjectDef = false;
                    if (!btype.equals("Primitive") && !templateType) {
                        _methodReturnType = _methodReturnType + "*";
                    }
                    retList.addLast(_methodReturnType);
                    retList.addLast(" retval;\n\n");
                } else {
                    retList.addLast("#define RETURN\n");
                }

                retList.addLast(node.getBody().accept(this, args));

                if (isThread) {
                    retList.addLast(indent(indentLevel));
                    retList.addLast("pc->setState(this, Empty_String, "
                            + "Empty_String, SchedStateVal::FINISHED);" + "\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("_sb.clearBuiltInLOC(pc->p);\n");
                    retList.addLast(indent(indentLevel));
                    retList.addLast("notify(DeltaCycle, _mng.pc->invoke);\n");
                }

                if (_doingMMDebug) {
                    retList.addMethodEndFallthroughCode();
                    retList.addBreakpointDummyStatement((BlockNode) node
                            .getBody());
                }

                retList.addLast(indent(indentLevel));
                retList.addLast("return RETURN;\n");
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}");
            }
        }
        _methodReturnType = "";

        retList.addLast(";\n\n");
        _labels.popScope();

        return retList;
    }

    /**
     * Returns the code represented by a <code>ConstructorDeclNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);
        String type = (String) args.get(0);
        LinkedList par = _commaList(TNLManip.traverseList(this, args, node
                .getParams()));

        if (type.equals("Netlist")) {
            // Because we already forced one constructor with the same
            // signature.
            // if (_stringListToString(par).trim().length() ==0)
            BlockNode bn = node.getBody();
            Iterator it = bn.getStmts().iterator();
            while (it.hasNext()) {
                TreeNode tn = (TreeNode) it.next();
                if (tn instanceof ConstraintBlockNode) {
                    retList.addLast(visitConstraintBlockNode(
                            (ConstraintBlockNode) tn, args));
                }
            }
            return retList;
        }

        retList.addLast("\n");

        _header.visitConstructorDeclNode(node, args);

        boolean parent_was_object = _header.wasParentObject(node.getParent()
                .classID());

        if (_header.hasTemplate()) {
            retList.addLast(_header.getTemplate());
            retList.addLast("\n");
        }

        if (parent_was_object) {
            retList.addLast(node.getName().accept(this, args));
            if (_header.hasTemplate())
                retList.addLast(_header.getTemplateTypes());
            retList.addLast("::");
        }

        if (!parent_was_object)
            retList.addLast(indent(indentLevel));

        retList.addLast(node.getName().accept(this, args));
        if (args.get(0).equals("Class")) {
            retList.addLast("(");
        } else {
            retList.addLast("(sc_module_name _name");
            if (_stringListToString(par).trim().length() > 0) {
                retList.addLast(", ");
            }
        }

        retList.addLast(par);
        retList.addLast(") ");

        if (((ClassDeclNode) (node.getParent())).getSuperClass() instanceof AbsentTreeNode) {
            if (type.equals("Process")) {
                retList.addLast(": process(_name)");
            } else if (type.equals("Medium")) {
                retList.addLast(": medium(_name)");
            } else if (type.equals("StateMedium")) {
                retList.addLast(": statemedium(_name)");
            } else if (type.equals("Scheduler")) {
                retList.addLast(": scheduler(_name)");
            } else if (type.equals("Netlist")) {
                retList.addLast(": netlist(_name)");
            } else if (type.equals("Quantity")) {
                retList.addLast(": quantity(_name)");
            }
        } else {
            _inObjectDef = true;
            if (!type.equals("Class")) {
                retList.addLast(": ");
                retList.addLast(((ClassDeclNode) (node.getParent()))
                        .getSuperClass().accept(this, args));
                retList.addLast("(");
                retList.addLast("_name");
                if (node.getConstructorCall() instanceof SuperConstructorCallNode) {
                    par = (LinkedList) node.getConstructorCall().accept(this,
                            args);
                    if (_stringListToString(par).trim().length() > 0) {
                        retList.addLast(", ");
                    }
                    retList.addLast(par);
                }
                retList.addLast(")");
            } else if (node.getConstructorCall() instanceof SuperConstructorCallNode) {
                retList.addLast(": ");
                retList.addLast(((ClassDeclNode) (node.getParent()))
                        .getSuperClass().accept(this, args));
                retList.addLast("(");
                retList.addLast(node.getConstructorCall().accept(this, args));
                retList.addLast(")");
            }
            _inObjectDef = false;
        }

        if (type.equals("Netlist")) {
            retList.addLast(" {}\n");
            Iterator stmts = ((BlockNode) node.getBody()).getStmts().iterator();
            while (stmts.hasNext()) {
                TreeNode stmt = (TreeNode) stmts.next();
                if (stmt instanceof ConstraintBlockNode) {
                    retList.addLast(stmt.accept(this, args));
                }
            }
            return retList;
        }

        retList.addLast(" {\n");
        increaseIndent();
        if (node.getConstructorCall() instanceof ThisConstructorCallNode) {
            retList.addLast(indent(indentLevel));
            retList.addLast(node.getName().accept(this, args));
            retList.addLast("(");
            par = (LinkedList) node.getConstructorCall().accept(this, args);
            if (!type.equals("Class")) {
                retList.addLast("_name");
                if (_stringListToString(par).trim().length() > 0) {
                    retList.addLast(", ");
                }
            }
            retList.addLast(par);
            retList.addLast(");\n");
        }

        if (type.equals("Process")) {
            if (!_header.hasSuperProcess()) {
                retList.addLast(indent(indentLevel));
                retList.addLast("SC_THREAD(thread);\n");
            }
        }
        retList.addLast(node.getBody().accept(this, args));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("};\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>ThisConstructorCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>this(ARGS);</code>
     */
    public Object visitThisConstructorCallNode(ThisConstructorCallNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        // ConstructorDeclNode n = (ConstructorDeclNode) node.getParent();
        // retList.addLast(indent(indentLevel));
        // retList.addLast(n.getName().accept(this, args));
        // retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        // retList.addLast(");\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>SuperConstructorCall</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>super(ARGS);</code>
     */
    public Object visitSuperConstructorCallNode(SuperConstructorCallNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        // retList.addLast(((ClassDeclNode) (node.getParent()))
        // .getSuperClass().accept(this, args));
        // retList.addLast("(");

        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));

        return retList;
    }

    /**
     * Returns the code represented by a <code>FieldDeclNode</code>.
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
     * Returns the code represented by a <code>LocalVarDeclNode</code>.
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
     * Returns the code represented by an <code>ArrayInitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> { INITEXPR1, INITEXPR2, ... } </code>
     */
    public Object visitArrayInitNode(ArrayInitNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("{");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getInitializers())));
        retList.addLast("}");

        return retList;
    }

    /**
     * Returns the code represented by a <code>BlockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> { STATEMENTS } </code>
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));

        return retList;
    }

    /**
     * Returns the code represented by a <code>ParameterNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>MODIFIER TYPE NAME</code>
     */
    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(Modifier.toString(node.getModifiers()));

        retList.addLast(node.getDefType().accept(this, args));

        // TreeNode baseType = node.getDefType();
        // while (baseType instanceof ArrayTypeNode)
        // baseType = ((ArrayTypeNode)baseType).getBaseType();
        // if (baseType instanceof TypeNameNode) {
        // String type = ((TypeNameNode) baseType).getName().getIdent();
        // //if (!type.equals("String")) retList.addLast("*");
        // if (! (((TypeNameNode)baseType).getName().getProperty(DECL_KEY)
        // instanceof TypeParameterDecl) )
        // retList.addLast("*");
        // }
        retList.addLast(" ");

        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>MethodCallNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> NAME(ARGS) </code>
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getMethod().accept(this, args));
        String type = args.get(0).toString();

        if (type.equals("Process")) {
            retList.addLast("(this");
        } else if (type.equals("Quantity") || type.equals("Netlist"))
            retList.addLast("((process *)&_mng");
        else {
            retList.addLast("(caller");
        }

        LinkedList par = _commaList(TNLManip.traverseList(this, args, node
                .getArgs()));
        if (_stringListToString(par).trim().length() > 0
                && !_stringListToString(retList).endsWith("(")) {
            retList.addLast(", ");
        }
        retList.addLast(par);
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>ExprStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>EXPR;</code>
     */
    public Object visitExprStmtNode(ExprStmtNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(";");
        retList.addLast("\n");

        return retList;
    }

    /**
     * Returns the code represented by an <code>EmptyStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>;</code>
     */
    public Object visitEmptyStmtNode(EmptyStmtNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        // retList.addLast(indent(indentLevel));
        // retList.addLast(";");
        retList.addLast("\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>LabeledStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> LABEL: STATEMENTS </code>
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug, true);

        String type = args.get(0).toString();

        if (type.equals("StateMedium")) {
            retList.addLast(indent(indentLevel));
            retList.addLast(node.getLabel().accept(this, args));
            retList.addLast(": ");
            increaseIndent();
            retList.addLast(node.getStmt().accept(this, args));
            decreaseIndent();
            return retList;
        }

        if (node.getStmt() instanceof AnnotationNode) {
            System.err.println("Error: Use 'Label{@ statement @}' for "
                    + "annotating a "
                    + "single statement instead of 'Label: statement'");
            System.exit(1);
        }

        LinkedList stmts = new LinkedList();
        stmts.addLast(node.getStmt());
        return _getAnnotatedBodyCode(stmts, args, "LABEL",
                _stringListToString((List) node.getLabel().accept(this, args)),
                node);
    }

    /**
     * Returns the code represented by an <code>IfStmtNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> if (CONDITION) THEN-PART else ELSE-PART </code>
     */
    public Object visitIfStmtNode(IfStmtNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        TreeNode thenPart = (TreeNode) node.getThenPart();
        TreeNode elsePart = node.getElsePart();

        retList.addLast(indent(indentLevel));
        retList.addLast("if (");
        retList.addLast(node.getCondition().accept(this, args));
        retList.addLast("){\n");

        increaseIndent();
        retList.addLast(thenPart.accept(this, args));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("} ");

        if (elsePart != AbsentTreeNode.instance) {
            retList.addLast("else {\n");
            increaseIndent();
            retList.addLast(elsePart.accept(this, args));
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("}\n");
        } else
            retList.addLast("\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>SwitchNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents:
     *         <code> switch(EXPR) { ... } </code>
     */
    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>SwitchBranchNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> case EXPR: STATEMENTS; </code>
     */
    public Object visitSwitchBranchNode(SwitchBranchNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        // case
        List retValue = TNLManip.traverseList(this, args, node.getCases());
        retList.addLast(retValue);

        int initIndent = indentLevel;
        int newIndent = _INDENT_ONCE - 1
                + _stringListToString(retValue).length();
        List stmtList = node.getStmts();

        if (stmtList.size() > 0) {
            // first statement
            TreeNode firstStmt = (TreeNode) stmtList.remove(0);
            if (firstStmt instanceof BlockNode
                    && !(firstStmt instanceof LabeledBlockNode)) {
                indentLevel = newIndent - _INDENT_ONCE;
                retList.addLast(firstStmt.accept(this, args));
            } else {
                indentLevel = _INDENT_ONCE - 1; // cancel out the space after
                // ":"
                retList.addLast(firstStmt.accept(this, args));
            }

            // rest of statements
            indentLevel = newIndent;
            retList.addLast(TNLManip.traverseList(this, args, stmtList));

            // add first stmt back
            stmtList.add(0, firstStmt);
        }

        indentLevel = initIndent;

        return retList;
    }

    /**
     * Returns the code represented by a <code>CaseNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> case EXPR: </code>
     */
    public Object visitCaseNode(CaseNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>BreakNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> break LABEL; </code>
     *         where LABEL is optional
     */
    public Object visitBreakNode(BreakNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>ContinueNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> continue LABEL; </code>
     *         where LABEL is optional
     */
    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>LoopNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> while (CONDITION) { ... }
     *                     or  do { ... } while (CONDITION) </code>
     */
    public Object visitLoopNode(LoopNode node, LinkedList args) {
        LineList retList = null;

        if (node.getForeStmt().classID() == EMPTYSTMTNODE_ID) {
            // while loop
            retList = new LineList(node, _doingMMDebug);
            retList.add("\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("while (");
            retList.addLast(node.getTest().accept(this, args));
            retList.addLast("){\n");

            TreeNode stmt = node.getAftStmt();
            increaseIndent();
            retList.addLast(stmt.accept(this, args));
            if (_getsDebugCode(node)) {
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->popNextLines();\n");
            }
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("}\n");
        } else {
            // do loop
            retList = new LineList(node, false);
            retList.add("\n");
            if (_getsDebugCode(node)) {
                retList.add(indent(indentLevel));
                retList.addNextLinePop();
                retList.add(indent(indentLevel));
                retList.addNextLinePush();
            }
            retList.add(indent(indentLevel));
            retList.add("do {\n");

            TreeNode stmt = node.getForeStmt();
            increaseIndent();
            retList.addLast(stmt.accept(this, args));
            if (_getsDebugCode(node)) {
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->popNextLines();\n");
            }
            decreaseIndent();
            if (_getsDebugCode(node)) {
                retList.addLast(indent(indentLevel));
                retList.addLineDirective((Integer) node
                        .getProperty(DOLOOP_TEST_LINENUMBER_KEY));
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("} while (");
            retList.addLast(node.getTest().accept(this, args));
            retList.addLast(");\n");
        }
        return retList;
    }

    /**
     * Returns the code represented by a <code>ForNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> for (INITEXPR; TEST; UPDATE) { ... } </code>
     */
    public Object visitForNode(ForNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast("for (");
        retList.addLast(_forInitStringList(node.getInit(), args));
        retList.addLast("; ");
        retList.addLast(node.getTest().accept(this, args));
        retList.addLast("; ");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getUpdate())));
        retList.addLast("){\n");

        increaseIndent();
        retList.addLast(node.getStmt().accept(this, args));
        if (_getsDebugCode(node)) {
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->popNextLines();\n");
        }
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        return retList;
    }

    /**
     * Returns the code represented by an <code>AssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 = EXPR2 </code>
     */
    public Object visitAssignNode(AssignNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getExpr1().accept(this, args));
        retList.addLast(" = ");
        retList.addLast(node.getExpr2().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by an <code>AbsentTreeNode</code>.
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
     * Returns the code represented by an <code>ObjectNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *
     * <code> OBJECTNAME </code>
     */
    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        return (node.getName().accept(this, args));
    }

    /**
     * Returns the code represented by a <code>ReturnNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> return EXPR; </code>
     */
    public Object visitReturnNode(ReturnNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        //String type = args.get(0).toString();

        if (!(node.getExpr() instanceof AbsentTreeNode)) {
            retList.addLast(indent(indentLevel));
            retList.addLast("retval= (" + _methodReturnType + ")(");
            retList.addLast(node.getExpr().accept(this, args));
            retList.addLast(");\n");
        }
        // if (!type.equals("StateMedium") && !type.equals("Scheduler") &&
        // !type.equals("Quantity") && !type.equals("Class"))
        if (!(node.getParent() instanceof BlockNode && node.getParent()
                .getParent() instanceof MethodDeclNode)) {
            retList.addLast(_labels.getReturnStatementOrigin());
        }

        return retList;
    }

    /**
     * Returns the code represented by a <code>CastNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> (CASTTYPE) EXPR </code>
     */
    public Object visitCastNode(CastNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
                args);

        retList.addLast("((");
        retList.addLast(node.getDtype().accept(this, args));
        // TreeNode baseType = node.getDtype();
        // while (baseType instanceof ArrayTypeNode)
        // baseType = ((ArrayTypeNode)baseType).getBaseType();
        // if (baseType instanceof TypeNameNode) {
        // String type = ((TypeNameNode) baseType).getName().getIdent();
        // //if (!type.equals("String")) retList.addLast("*");
        // if (! (((TypeNameNode)baseType).getName().getProperty(DECL_KEY)
        // instanceof TypeParameterDecl) )
        // retList.addLast("*");
        // }
        retList.addLast(") ");
        retList.addLast(_parenExpr(node.getExpr(), exprStringList));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>NullPntrNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> null </code>
     */
    public Object visitNullPntrNode(NullPntrNode node, LinkedList args) {
        return TNLManip.addFirst("NULL");
    }

    /**
     * Returns the code represented by a <code>ThisNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

        TreeNode enclosingInstance = node.getEnclosingInstance();

        int enclosingID = enclosingInstance.classID();
        if ((enclosingID != ABSENTTREENODE_ID) && (enclosingID != THISNODE_ID)) {

            LinkedList enclosingStringList = (LinkedList) node
                    .getEnclosingInstance().accept(this, args);

            retList.addLast(_parenExpr(enclosingInstance, enclosingStringList));
            retList.addLast(".");
        }

        retList.addLast("new ");
        _inObjectDef = true;
        retList.addLast(node.getDtype().accept(this, args));
        _inObjectDef = false;
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by <code>AllocateAnonymousClassNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code>new SUPERTYPE(SUPERARGS) { ... }   or
     *          ENCLOSINGINSTANCE.new SUPERTYPE(SUPERARGS) { ... } </code>
     */
    public Object visitAllocateAnonymousClassNode(
            AllocateAnonymousClassNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>AllocateArrayNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> new TYPE[][].. </code>
     */
    public Object visitAllocateArrayNode(AllocateArrayNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("new ");
        _inObjectDef = true;
        String defType = _stringListToString((List) (node.getDtype().accept(
                this, args)));
        Collection value = _ports.values();
        Iterator iter = value.iterator();
        boolean contains = false;
        while (iter.hasNext()) {
            String[] type = (String[]) iter.next();
            if (type[0].equals(defType)) {
                contains = true;
                break;
            }
        }
        if (contains) {
            defType = "sc_port<" + defType + " >";
            retList.add(defType);
        } else if (node.getDtype() instanceof TypeNameNode) {
            //String type = ((TypeNameNode) node.getDtype()).getName().getIdent();
            // if (!type.equals("String")) defType = defType + "*";
            if (!(((TypeNameNode) node.getDtype()).getName().getProperty(
                    DECL_KEY) instanceof TypeParameterDecl))
                defType = defType + "*";
            retList.add(defType);
        } else
            retList.add(defType);
        _inObjectDef = false;

        List dimExprList = TNLManip
                .traverseList(this, args, node.getDimExprs());
        int dim = dimExprList.size();
        // Iterator dimExprItr = dimExprList.iterator();

        TreeNode parNode = node.getParent();
        if (dim <= 0) {
            throw new RuntimeException("No array dimensions are provided for "
                    + ((FieldDeclNode) parNode).getName().getIdent()
                    + " when creating it.");
        }
        if (dim > 1 && (parNode instanceof FieldDeclNode)) {
            throw new RuntimeException("Do not support creating "
                    + "multi-dimension array "
                    + ((FieldDeclNode) parNode).getName().getIdent()
                    + " in field declaration, try to do that in a constructor.");
        }
        if (dim > 1 && !(parNode instanceof LocalVarDeclNode)
                && !(parNode instanceof AssignNode)) {
            throw new RuntimeException("Do not support creating "
                    + "multi-dimension array "
                    + ((FieldDeclNode) parNode).getName().getIdent()
                    + " in a complex expression. Try to do 'new' operation "
                    + "separately.");
        }
        if (dim == 1) {
            retList.addLast("[");
            retList.addLast(dimExprList.get(0));
            retList.addLast("]");
        } else {
            String arrayName = "Unidentified";
            if (parNode instanceof FieldDeclNode) {
                arrayName = ((FieldDeclNode) parNode).getName().getIdent();
            } else if (parNode instanceof LocalVarDeclNode) {
                arrayName = ((LocalVarDeclNode) parNode).getName().getIdent();
            } else if (parNode instanceof AssignNode) {
                arrayName = _stringListToString((List) ((AssignNode) parNode)
                        .getExpr1().accept(this, args));
            }
            for (int i = 0; i < dim - 1; i++)
                retList.addLast("*");
            retList.addLast(" ");
            retList.addLast("[");
            retList.addLast(dimExprList.get(0));
            retList.addLast("];\n");
            retList.addLast(_createArray(arrayName, defType, 0, dimExprList));
        }

        // while (dimExprItr.hasNext()) {
        // retList.addLast("[");
        // retList.addLast(dimExprItr.next());
        // retList.addLast("]");
        // }

        // for (int dimsLeft = node.getDims(); dimsLeft > 0; dimsLeft--) {
        // retList.addLast("[]");
        // }

        if (node.getInitExpr() != AbsentTreeNode.instance) {
            retList.addLast(" ");
            retList.addLast(node.getInitExpr().accept(this, args));
        }

        retList.addLast(";\n");
        /*
         * retList.addLast(indent(indentLevel)); retList.addLast("#ifdef
         * DEBUG\n"); increaseIndent(); for (int i=0; i<dim; i++) {
         * retList.addLast(indent(indentLevel)); retList.addLast("assert(");
         * retList.addLast(dimExprList.get(i)); retList.addLast(">=0);\n"); }
         * decreaseIndent(); retList.addLast(indent(indentLevel));
         * retList.addLast("#endif\n");
         */

        return retList;
    }

    /**
     * Returns the code represented by an <code>ArrayAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> ARRAYNAME[INDEX] </code>
     */
    public Object visitArrayAccessNode(ArrayAccessNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        LinkedList arrayStringList = (LinkedList) node.getArray().accept(this,
                args);

        retList.addLast(_parenExpr(node.getArray(), arrayStringList));
        retList.addLast("[");
        retList.addLast(node.getIndex().accept(this, args));
        retList.addLast("]");

        return retList;
    }

    /**
     * Returns the code represented by an <code>ObjectFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>OBJECT.NAME </code>
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getObject().accept(this, args));
        retList.addLast("->");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by a <code>SuperFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> super.NAME </code>
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        TreeNode utdNode = node;

        while (!(utdNode instanceof ClassDeclNode)
                && !(utdNode instanceof InterfaceDeclNode))
            utdNode = utdNode.getParent();

        if (utdNode instanceof ClassDeclNode) {
            TreeNode superClassNode = ((ClassDeclNode) utdNode).getSuperClass();
            if (superClassNode == AbsentTreeNode.instance) {
                // System.err.println("Internal Error (1001). Abort.");
                // System.exit(1);
                retList.addLast("Object");
            } else {
                _inObjectDef = true;
                retList.addLast(superClassNode.accept(this, args));
                _inObjectDef = false;
            }
        }

        retList.addLast("::");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by a <code>TypeFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> Type.FIELDNAME </code>
     */
    public Object visitTypeFieldAccessNode(TypeFieldAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        _inObjectDef = true;
        String type = _stringListToString((List) node.getFType().accept(this,
                args));
        _inObjectDef = false;
        type = StringManip.partAfterLast(type, '.');

        retList.addLast(type);
        retList.addLast("::");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by a <code>ThisFieldAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>this.NAME</code>
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        MetaModelDecl mmd = (MetaModelDecl) node.getName()
                .getProperty(DECL_KEY);
        if ((mmd.getModifiers() & STATIC_MOD) == 0)
            retList.addLast("this->");
        retList.addLast(node.getName().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by a <code>TypeClassAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> FIELDTYPE.class </code>
     */
    public Object visitTypeClassAccessNode(TypeClassAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getFType().accept(this, args));
        retList.addLast(".class");

        return retList;
    }

    /**
     * Returns the code represented by a <code>OuterThisAccessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> TYPE.this </code>
     */
    public Object visitOuterThisAccessNode(OuterThisAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getType().accept(this, args));
        retList.addLast(".this");

        return retList;
    }

    /**
     * Returns the code represented by a <code>OuterSuperAccess</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> TYPE.super </code>
     */
    public Object visitOuterSuperAccess(OuterSuperAccessNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getType().accept(this, args));
        retList.addLast(".super");

        return retList;
    }

    /**
     * Returns the code represented by a <code>IntLitNode</code>.
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
     * Returns the code represented by a <code>LongLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents the long integer
     *
     */
    public Object visitLongLitNode(LongLitNode node, LinkedList args) {
        return TNLManip.addFirst(node.getLiteral());
    }

    /**
     * Returns the code represented by a <code>FloatLitNode</code>.
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
     * Returns the code represented by a <code>DoubleLitNode</code>.
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
     * Returns the code represented by a <code>BoolLitNode</code>.
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
     * Returns the code represented by a <code>CharLitNode</code>.
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
     * Returns the code represented by a <code>StringLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents "<code>STRING</code>"
     */
    public Object visitStringLitNode(StringLitNode node, LinkedList args) {
        return TNLManip.addFirst("new String(\"" + node.getLiteral() + "\")");
    }

    /**
     * Returns the code represented by a <code>BoolTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>boolean</code>
     */
    public Object visitBoolTypeNode(BoolTypeNode node, LinkedList args) {
        return TNLManip.addFirst("bool");
    }

    /**
     * Returns the code represented by a <code>CharTypeNode</code>.
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
     * Returns the code represented by a <code>ByteTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents: byte
     */
    public Object visitByteTypeNode(ByteTypeNode node, LinkedList args) {
        return TNLManip.addFirst("unsigned char");
    }

    /**
     * Returns the code represented by a <code>ShortTypeNode</code>.
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
     * Returns the code represented by an <code>IntTypeNode</code>.
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
     * Returns the code represented by a <code>FloatTypeNode</code>.
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
     * Returns the code represented by a <code>LongTypeNode</code>.
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
     * Returns the code represented by a <code>DoubleTypeNode</code>.
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
     * Returns the code represented by a <code>VoidTypeNode</code>.
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
     * Returns the code represented by a <code>TypeNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>TYPENAME</code>
     */
    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        TypeNameNode origType = (TypeNameNode) node.getProperty(ORIGTYPE_KEY);
        if (origType != null) {
            return origType.accept(this, args);
        }

        boolean top = false;
        if (!_inTypeNameNode) {
            _inTypeNameNode = true;
            top = true;
        }

        LinkedList retList = (LinkedList) node.getName().accept(this, args);
        String type = _stringListToString(retList).trim();

        Decl d = (Decl) node.getName().getProperty(DECL_KEY);
        if (type.startsWith("metamodel.lang.")) {
            if (type.equals("metamodel.lang.Process"))
                type = "process ";
            else if (type.equals("metamodel.lang.Medium"))
                type = "medium ";
            else if (type.equals("metamodel.lang.StateMedium"))
                type = "statemedium ";
            else if (type.equals("metamodel.lang.Quantity"))
                type = "quantity ";
            else if (type.equals("metamodel.lang.Netlist"))
                type = "netlist ";
        } else if (!type.equals("Port")) { // && !type.equals("String")) {
            int i = type.indexOf('<');
            if (i == -1) {
                if (d != null)
                    if (!(d instanceof TypeParameterDecl))
                        _header.addForwardDecl(type, 0);
            } else {
                TreeNode t = node.getName().getParameters();
                if (t instanceof TemplateParametersNode) {
                    _header.addForwardDecl(node.getName().getIdent(),
                            ((TemplateParametersNode) t).getTypes().size());
                }
                /*
                 * String forwardType = type.replaceAll("<", "<typename ");
                 * forwardType = forwardType.replaceAll(",", ", typename ");
                 * String t = "template " + forwardType.substring(i) + " " +
                 * forwardType.substring(0, i); _header.addForwardDecl(t);
                 */
            }

        }
        if (!(_inObjectDef && top) && d != null)
            if (d instanceof ObjectDecl)
                type += "*";
        retList = new LinkedList();
        retList.addLast(type);

        if (top)
            _inTypeNameNode = false;

        return retList;
    }

    /**
     * Returns the code represented by an <code>ArrayTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>BASETYPE[]</code>
     */
    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getBaseType().accept(this, args));
        retList.addLast("*");
        /*
         * still miss the case where init expression is memory allocation if
         * (node.getParent() instanceof VarInitDeclNode) { VarInitDeclNode v =
         * (VarInitDeclNode) node.getParent(); if (!(v.getInitExpr() instanceof
         * AbsentTreeNode)) retList.addLast("[]"); else retList.addLast("*"); }
         * else retList.addLast("*");
         */

        return retList;
    }

    /**
     * Returns the code represented by a <code>PostIncrNode</code>.
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
     * Returns the code represented by a <code>PostDecrNode</code>.
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
     * Returns the code represented by a <code>UnaryPlusNode</code>.
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
     * Returns the code represented by a <code>UnaryMinusNode</code>.
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
     * Returns the code represented by a <code>PreIncrNode</code>.
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
     * Returns the code represented by a <code>PreDecrNode</code>.
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
     * Returns the code represented by a <code>ComplementNode</code>.
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
     * Returns the code represented by a <code>NotNode</code>.
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
     * Returns the code represented by a <code>MultNode</code>.
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
     * Returns the code represented by a <code>DivNode</code>.
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
     * Returns the code represented by a <code>RemNode</code>.
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
     * Returns the code represented by a <code>PlusNode</code>.
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
     * Returns the code represented by a <code>MinusNode</code>.
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
     * Returns the code represented by a <code>LeftShiftLogNode</code>.
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
     * Returns the code represented by a <code>RightShiftLogNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >>> EXPR2 </code>
     */
    public Object visitRightShiftLogNode(RightShiftLogNode node, LinkedList args) {
        return _visitBinaryOpNode(node, args, ">>>");
    }

    /**
     * Returns the code represented by a <code>RightShiftArithNode</code>.
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
     * Returns the code represented by a <code>LTNode</code>.
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
     * Returns the code represented by a <code>GTNode</code>.
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
     * Returns the code represented by a <code>LENode</code>.
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
     * Returns the code represented by a <code>GENode</code>.
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
     * Returns the code represented by an <code>InstanceOfNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> EXPR instanceof DTYPE </code>
     */
    public Object visitInstanceOfNode(InstanceOfNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
                args);

        retList.addLast(_parenExpr(node.getExpr(), exprStringList));
        retList.addLast(" instanceof ");
        retList.addLast(node.getDtype().accept(this, args));

        return retList;
    }

    /**
     * Returns the code represented by a <code>EQNode</code>.
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
     * Returns the code represented by a <code>NENode</code>.
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
     * Returns the code represented by a <code>BitAndNode</code>.
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
     * Returns the code represented by a <code>BitOrNode</code>.
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
     * Returns the code represented by a <code>BitXorNode</code>.
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
     * Returns the code represented by a <code>CandNode</code>.
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
     * Returns the code represented by a <code>CorNode</code>.
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
     * Returns the code represented by a <code>IfExprNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> EXPR1 ? EXPR2 : EXPR3 </code>
     */
    public Object visitIfExprNode(IfExprNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>MultAssignNode</code>.
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
     * Returns the code represented by a <code>DivAssignNode</code>.
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
     * Returns the code represented by a <code>RemAssignNode</code>.
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
     * Returns the code represented by a <code>PlusAssignNode</code>.
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
     * Returns the code represented by a <code>MinusAssignNode</code>.
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
     * Returns the code represented by a <code>LeftShiftLogAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 <<= EXPR2 </code>
     */
    public Object visitLeftShiftLogAssignNode(LeftShiftLogAssignNode node,
            LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, "<<=");
    }

    /**
     * Returns the code represented by a <code>RightShiftLogAssignNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> EXPR1 >>>= EXPR2 </code>
     */
    public Object visitRightShiftLogAssignNode(RightShiftLogAssignNode node,
            LinkedList args) {
        return _visitBinaryOpAssignNode(node, args, ">>>=");
    }

    /**
     * Returns the code represented by a <code>RightShiftArithAssignNode</code>.
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
     * Returns the code represented by a <code>BitAndAssignNode</code>.
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
     * Returns the code represented by a <code>BitXorAssignNode</code>.
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
     * Returns the code represented by a <code>BitOrAssignNode</code>.
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
     * Returns the code represented by a <code>SpecialLitNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> LAST or retval </code>
     */
    public Object visitSpecialLitNode(SpecialLitNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        retList.addLast(node.getLiteral());
        return retList;
    }

    /**
     * Returns the code represented by a <code>GetComponentNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getcomponent(netlist, name) </code>
     */
    public Object visitGetComponentNode(GetComponentNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getcomponent(");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>GetConnectionDestNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getconnectiondestnode(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>GetNthPortNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getnthport(node, ifName, num) </code>
     */
    public Object visitGetNthPortNode(GetNthPortNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>GetPortNumNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getportnum(node, ifName) </code>
     */
    public Object visitGetPortNumNode(GetPortNumNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getportnum(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getIfName().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>GetScopeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getscope(node, port) </code>
     */
    public Object visitGetScopeNode(GetScopeNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getscope(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getPort().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>GetThreadNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> getthread() </code>
     */
    public Object visitGetThreadNode(GetThreadNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("pc->p");

        return retList;
    }

    /**
     * Returns the code represented by a <code>ActionLabelStmtNode</code>.
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
        //LineList retList = new LineList(node, _doingMMDebug, true);

        //String type = args.get(0).toString();

        return _getAnnotatedBodyCode(node.getStmts(), args, "LABEL",
                _stringListToString((List) node.getLabel().accept(this, args)),
                node);
    }

    /**
     * Returns the code represented by a <code>ActionLabelExprNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getLabel().accept(this, args));
        retList.addLast("{@ ");
        retList.addLast(node.getExpr().accept(this, args));
        retList.addLast(" @}");

        return retList;
    }

    /**
     * Returns the code represented by a <code>AnnotationNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> {$ stmts $} </code>
     */
    public Object visitAnnotationNode(AnnotationNode node, LinkedList args) {

        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        // retList.addLast("{$\n");
        increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        // retList.addLast("$}\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>BeginAnnotationNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> beg{ stmts } </code>
     */
    public Object visitBeginAnnotationNode(BeginAnnotationNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        // retList.addLast(indent(indentLevel));
        // retList.addLast("beg{\n");
        // increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        // decreaseIndent();
        // retList.addLast(indent(indentLevel));
        // retList.addLast("}\n");

        return retList;
    }

    /**
     * Returns the code represented by a <code>EndAnnotationNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> end{ stmts } </code>
     */
    public Object visitEndAnnotationNode(EndAnnotationNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        // retList.addLast(indent(indentLevel));
        // retList.addLast("end{\n");
        // increaseIndent();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        // decreaseIndent();
        // retList.addLast(indent(indentLevel));
        // retList.addLast("}\n");

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
    /*
     * public Object visitLTLSynchNode(LTLSynchNode node, LinkedList args) {
     * LineList retList = new LineList(node, _doingMMDebug);
     *
     * List events = node.getEvents(); List equVars = node.getEqualVars();
     * Iterator iter = events.iterator(); retList.addLast(indent(indentLevel));
     * retList.addLast("if (\n"); increaseIndent(); while (iter.hasNext()) {
     * Object event = ((TreeNode) iter.next()).accept(this, args);
     * retList.addLast(indent(indentLevel)); retList.addLast(event);
     * retList.addLast("->getProcess()->pc->_currentEvent == ");
     * retList.addLast(event); if (iter.hasNext()) retList.addLast(" && \n"); }
     * retList.addLast("\n"); decreaseIndent();
     * retList.addLast(indent(indentLevel)); retList.addLast(") {\n");
     *
     * increaseIndent(); if (equVars.size()>0) { iter = equVars.iterator();
     * while (iter.hasNext()) { EqualVarsNode evNode = (EqualVarsNode)
     * iter.next(); //FIXME: finish this part //which is to compare the
     * condition for mapped behavior } } decreaseIndent();
     *
     * retList.addLast(indent(indentLevel)); retList.addLast("} else {\n");
     * increaseIndent(); iter = events.iterator(); while (iter.hasNext()) {
     * Object event = ((TreeNode) iter.next()).accept(this, args);
     * retList.addLast(indent(indentLevel)); retList.addLast("if (");
     * retList.addLast(event);
     * retList.addLast("->getProcess()->pc->_currentEvent == ");
     * retList.addLast(event); retList.addLast(")\n"); increaseIndent();
     * retList.addLast(indent(indentLevel)); retList.addLast(event);
     * retList.addLast("->getProcess()->pc->SchedState =
     * SchedStateVal::DONTRUN;\n"); decreaseIndent(); } decreaseIndent();
     * retList.addLast(indent(indentLevel)); retList.addLast("}\n\n");
     *
     *
     * return retList; }
     */

    /**
     * Returns the code represented by a <code>EqualVarsNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> var1 == var2 </code>
     */
    public Object visitEqualVarsNode(EqualVarsNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        retList.addLast(node.getVar1().accept(this, args));
        retList.addLast("==");
        retList.addLast(node.getVar2().accept(this, args));
        return retList;
    }

    /**
     * Returns the code represented by a <code>ActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> all </code>
     *                  <code> object.name </code>
     */
    public Object visitActionNode(ActionNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        TreeNode obj = node.getObject();
        TreeNode name = node.getName();

        if ((obj == AbsentTreeNode.instance)
                && (name == AbsentTreeNode.instance))
            retList.addLast("all");
        else {
            retList.addLast(obj.accept(this, args));

            retList.addLast(", \"");
            retList.addLast(name.accept(this, args));
            retList.addLast("\"");
        }

        return retList;
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
        LineList retList = new LineList(node, _doingMMDebug);

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
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>NoneEventNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> none(process) </code>
     */
    public Object visitNoneEventNode(NoneEventNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("none(");
        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>OtherEventNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> other(process) </code>
     */
    public Object visitOtherEventNode(OtherEventNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("other(");
        if (node.getProcess() == AbsentTreeNode.instance)
            retList.addLast("all");
        else
            retList.addLast(node.getProcess().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>ImplyNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> event =&gt; expr </code>
     */
    public Object visitImplyNode(ImplyNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(" -> (");
        retList.addLast(node.getExpr().accept(this, args));
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
     *                  <code> loc BuiltInLOCFormula; </code>
     */
    public Object visitLOCConstraintNode(LOCConstraintNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

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
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(indent(indentLevel));
        retList.addLast("eloc (");
        retList.addLast(node.getFormula().accept(this, args));
        retList.addLast(");\n");

        return retList;
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
        return _visitConstraintDeclNode(node, args, "eloc");
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
        return _visitConstraintDeclNode(node, args, "loc");
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
        return _visitConstraintDeclNode(node, args, "ltl");
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
        LineList retList = new LineList(node, _doingMMDebug);
        retList.addLast("eloc ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");
        return retList;
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
     */
    public Object visitLOCConstraintCallNode(LOCConstraintCallNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        retList.addLast("loc ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");
        return retList;
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
        LineList retList = new LineList(node, _doingMMDebug);
        retList.addLast("ltl ");
        retList.addLast(node.getName().accept(this, args));
        retList.addLast("(");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getArgs())));
        retList.addLast(")");
        return retList;
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
        LineList retList = new LineList(node, _doingMMDebug);

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
        LineList retList = new LineList(node, _doingMMDebug);

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
        LineList retList = new LineList(node, _doingMMDebug);

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
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>QuantityDeclNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);
        if (node.getName().getProperty(TEMPLDECL_KEY) != null) {
            return retList;
        }

        _inObjectDef = true;
        _header.visitQuantityDeclNodeBegin(node, args);
        _inObjectDef = false;

        /*
         * // visit type parameters List template = TNLManip.traverseList(this,
         * args, node.getParTypeNames()); if (!template.isEmpty()) {
         * retList.addLast("\n"); retList.addLast(indent(indentLevel));
         * retList.addLast("template (");
         * retList.addLast(_separateList(template, "; ")); retList.addLast(")"); }
         * retList.addLast("\n");
         *  // visit name node retList.addLast(indent(indentLevel));
         * retList.addLast(Modifier.toString(node.getModifiers()));
         * retList.addLast("quantity ");
         * retList.addLast(node.getName().accept(this, args));
         *
         * retList.addLast(" ");
         *
         * TreeNode superClass = node.getSuperClass(); if (superClass !=
         * AbsentTreeNode.instance) { retList.addLast("extends ");
         * retList.addLast(node.getSuperClass().accept(this, args));
         * retList.addLast(" "); }
         *  // visit interfaces List retValue = TNLManip.traverseList(this,
         * args, node.getInterfaces()); if (!retValue.isEmpty()) {
         * retList.addLast("implements ");
         * retList.addLast(_commaList(retValue)); retList.addLast(" "); }
         *
         * retList.addLast("{\n");
         */

        args.set(0, new String("Quantity"));

        // visit members
        increaseIndent();
        _ports = new Hashtable();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));
        retList.addLast(_getDummyConstructorCode(node, args));
        _ports = null;
        decreaseIndent();

        _header.visitQuantityDeclNodeEnd(node, args);

        /*
         * retList.addLast(indent(indentLevel)); retList.addLast("}\n");
         */

        if (node.getParTypeNames().size() > 0) {
            _header.addTemplateImplCode(retList);
            retList = new LineList(node, _doingMMDebug);
        }

        return retList;
    }

    /**
     * Returns the code represented by an <code>EventTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>event</code>
     */
    public Object visitEventTypeNode(EventTypeNode node, LinkedList args) {
        return TNLManip.addFirst("event *");
    }

    /**
     * Returns the code represented by an <code>GetInstNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getinstname( node ) </code>
     */
    public Object visitGetInstNameNode(GetInstNameNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getinstname(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>GetCompNameNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getcompname(node, netlist) </code>
     */
    public Object visitGetCompNameNode(GetCompNameNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("getcompname(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getNetlist().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>IsConnectionRefinedNode</code>.
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
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by an <code>GetTypeNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code> gettype( node ) </code>
     */
    public Object visitGetTypeNode(GetTypeNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("gettype(");
        retList.addLast(node.getNode().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>GetProcessNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents
     *         <code> getprocess( event ) </code>
     */
    public Object visitGetProcessNode(GetProcessNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("(");
        retList.addLast(node.getEvent().accept(this, args));
        retList.addLast(")->");
        retList.addLast("getProcess()");

        return retList;
    }

    /**
     * Check if a CompileUnitNode has a template or not.
     *
     * @return whether a CompileUnitNode has templates or not
     */
    public boolean hasTemplates() {
        return _header.hasTemplate();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////
    /** number of spaces for the current indent level. */
    protected int indentLevel = 0;

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
     * Used for metamodel debugging. C++ code generated for metamodel debugging
     * includes <code>#line</code> preprocessor directives to fool the
     * debugger into using the .mmm file as the source code to debug. Every
     * source code line needs such a directive, and the line numbers have to be
     * monotonically non-decreasing. This routine makes both those conditions
     * come true in a given file of generated CPP code.
     *
     * @param cppFile
     *            The <code>File</code> to get its #line directives fixed.
     * @exception IOException
     *                on I/O or file-creation errors.
     */
    protected static void _fixLineDirectives(File cppFile) throws IOException {

        int currentLineNumber = 0;
        int greatestLineNumber = 0;
        boolean lastLineWasDirective = false;
        String mmmFileName = null;

        File tmpFile = File.createTempFile("mmdebug", null, cppFile
                .getParentFile());
        tmpFile.deleteOnExit();
        BufferedReader reader = new BufferedReader(new FileReader(cppFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

        String inputLine = null;
        inputLine = reader.readLine();
        while (inputLine != null) {
            String splitLine2 = null; // In case we find a "#line " in
            // mid-line and have to split the line.

            // if (inputLine.trim().length() == 0) {
            // writer.newLine();
            // lastLineWasDirective = false;
            // } else if (inputLine.startsWith("#line ")) {
            // Number even the blank lines.
            if (inputLine.startsWith("#line ")) {
                StringTokenizer lineTokenizer = new StringTokenizer(inputLine);
                lineTokenizer.nextToken();
                String lineNumberToken = lineTokenizer.nextToken();
                String fileNameToken = lineTokenizer.nextToken();
                try {
                    currentLineNumber = Integer.parseInt(lineNumberToken);
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Can't parse line number in"
                            + " this directive: \"" + inputLine + "\"", ex);
                }
                if (currentLineNumber > greatestLineNumber) {
                    greatestLineNumber = currentLineNumber;
                }
                if (mmmFileName == null) {
                    mmmFileName = fileNameToken;
                }
                writer.write("#line ");
                writer.write(Integer.toString(greatestLineNumber));
                writer.write(" ");
                writer.write(mmmFileName);
                writer.newLine();
                lastLineWasDirective = true;
            } else {
                if (inputLine.indexOf("#line ") > 0) {
                    // There's a "#line" directive that's not at the beginning
                    // of the line. If it's not in a comment, split the line.
                    boolean isComment = false;
                    int poundLinePos = inputLine.indexOf("#line ");
                    int slashSlashPos = inputLine.indexOf("//");
                    int slashStarPos = inputLine.indexOf("/*");
                    int starSlashPos = inputLine.indexOf("*/");
                    if (-1 < slashSlashPos && slashSlashPos < poundLinePos) {
                        isComment = true;
                    } else if (-1 < slashStarPos
                            && slashStarPos < poundLinePos
                            && (starSlashPos == -1 || poundLinePos < starSlashPos)) {
                        isComment = true;
                    }
                    if (!isComment) {
                        splitLine2 = inputLine.substring(poundLinePos);
                        inputLine = inputLine.substring(0, poundLinePos);
                    }
                }
                if (greatestLineNumber > 0 && !lastLineWasDirective) {
                    writer.write("#line ");
                    writer.write(Integer.toString(greatestLineNumber));
                    writer.write(" ");
                    writer.write(mmmFileName);
                    writer.newLine();
                }
                writer.write(inputLine);
                writer.newLine();
                lastLineWasDirective = false;
            }
            if (splitLine2 != null) {
                inputLine = splitLine2;
            } else {
                inputLine = reader.readLine();
            }
        }
        writer.flush();
        writer.close();
        reader.close();

        if (!cppFile.delete()) {
            // Cygwin seems to really want us to delete the file first.
            throw new RuntimeException("Could not delete " + cppFile.getName());
        }
        if (!tmpFile.renameTo(cppFile)) {
            throw new RuntimeException("Could not rename " + tmpFile.getName()
                    + " to " + cppFile.getName());
        }
    }

    /**
     * make a string of spaces indicated by the parameter.
     *
     * @param space
     *            indentation level
     * @return a string of spaces
     */
    protected String indent(int space) {
        StringBuffer stringBuffer = new StringBuffer(space);

        for (int i = 0; i < space; i++)
            stringBuffer.append(" ");

        return stringBuffer.toString();
    }

    /**
     * Return a string of spaces. The number of spaces are determined by the
     * indentLevel field.
     *
     * @return a string of spaces
     */
    protected String indent() {
        StringBuffer stringBuffer = new StringBuffer(indentLevel);

        for (int i = 0; i < indentLevel; i++)
            stringBuffer.append(" ");

        return stringBuffer.toString();
    }

    /**
     * Increase the indent level.
     */
    protected void increaseIndent() {
        indentLevel += _INDENT_ONCE;
    }

    /**
     * Decrease the indent level.
     */
    protected void decreaseIndent() {
        indentLevel -= _INDENT_ONCE;
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
     * Returns the code represented by a variable declaration.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents: MODIFIER TYPE NAME = INITEXPR
     */
    protected LinkedList _visitVarInitDeclNode(VarInitDeclNode node,
            LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        boolean parent_was_object = _header.wasParentObject(node.getParent()
                .classID());

        //String type = " ";
        //TreeNode baseType = node.getDefType();

        _header.visitVarInitDeclNode(node, args);

        if (!parent_was_object) {
            boolean isConst = false;
            boolean isStatic = false;
            retList.addLast(indent(indentLevel));
            String mod = Modifier.toString(node.getModifiers());
            StringTokenizer st = new StringTokenizer(mod);
            while (st.hasMoreTokens()) {
                String modifier = st.nextToken();
                if (modifier.equals("final")) {
                    retList.addLast("const ");
                    isConst = true;
                } else if (modifier.equals("static")) {
                    retList.addLast(modifier + " ");
                    isStatic = true;
                } else if (modifier.equals("abstract")) {
                    retList.addLast(modifier + " ");
                }
                // public, protected, private ignored
            }
            if (isConst & !isStatic)
                retList.addLast("static ");

            retList.addLast(node.getDefType().accept(this, args));

            // while (baseType instanceof ArrayTypeNode) {
            // baseType = ((ArrayTypeNode)baseType).getBaseType();
            // }
            //
            // if (baseType instanceof TypeNameNode) {
            // type = ((TypeNameNode) baseType).getName().getIdent();
            // if (! (((TypeNameNode)baseType).getName().getProperty(DECL_KEY)
            // instanceof TypeParameterDecl) )
            // retList.addLast("*");
            // }

            retList.addLast(" " + node.getName().getIdent() + " = ");

            if (node.getInitExpr() != AbsentTreeNode.instance) {
                retList.addLast(node.getInitExpr().accept(this, args));
            } else {
                retList.addLast("0");
            }

            retList.addLast(";\n");
        }

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
     *            a list of strings to be converted
     * @return the converted string
     */
    protected static String _stringListToString(List stringList) {

        if (stringList == null) {
            return new String("");
        }

        Iterator stringItr = stringList.iterator();
        StringBuffer sb = new StringBuffer();
        boolean newlineLast = false;

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List || stringObj instanceof String) {
                String s = null;
                if (stringObj instanceof List) {
                    // only use separators for top level
                    s = _stringListToString((List) stringObj);
                } else {
                    s = (String) stringObj;
                }

                // Make sure "#line" directive appears at start of line:
                if (!newlineLast && s.startsWith("#line") && sb.length() > 0) {
                    sb.append("\n");
                }

                sb.append(s);
                newlineLast = (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n');

            } else if (stringObj != null && !(stringObj instanceof NullValue)) {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }

        return sb.toString();
    }

    /**
     * A version of <code>_stringListToString(List)</code> that writes to a
     * file instead of generating one long string (which may be to long for the
     * machine's memory.).
     *
     * @param stringList
     *            A list of strings to be converted.
     * @param file
     *            A <code>FileWriter</code> to write the results to.
     * @exception Exception
     */
    protected static void _stringListToString2(List stringList, FileWriter file)
            throws Exception {

        if (stringList == null) {
            return;
        }

        Iterator stringItr = stringList.iterator();

        while (stringItr.hasNext()) {
            Object stringObj = stringItr.next();

            if (stringObj instanceof List || stringObj instanceof String) {
                if (stringObj instanceof List) {
                    _stringListToString2((List) stringObj, file);
                } else {
                    file.write((String) stringObj);
                }

            } else if (stringObj != null && !(stringObj instanceof NullValue)) {
                throw new IllegalArgumentException(
                        "unknown object in string list : " + stringObj);
            }
        }
        return;
    }

    /**
     * Returns the code represented by a for init statement.
     *
     * @param list
     *            a list of declarations or statement expressions for the init
     *            part of a for loop
     * @param args
     *            arguments
     * @return a list of strings that represents<br>
     *         <code>MODIFIER TYPE NAME1 = INITEXPR1,
     *                      NAME2 = INITEXPR2, ... </code>
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
            ;

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
     * Returns the code represented by a <code>SingleExprNode</code>.
     *
     * @param node
     *            a node that contains the expression
     * @param args
     *            arguments
     * @param opString
     *            the string that represents the operation
     * @param post
     *            a boolean to indicate if it is a post operation
     * @return a list of strings that represents the expression.
     */
    protected LinkedList _visitSingleExprNode(SingleExprNode node,
            LinkedList args, String opString, boolean post) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>BinaryOpNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the operation
     * @param args
     *            arguments
     * @param opString
     *            a string that represents the binary operation
     * @return a list of strings that represents <code> EXPR1 OP EXPR2 </code>
     */
    protected LinkedList _visitBinaryOpNode(BinaryOpNode node, LinkedList args,
            String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>BinaryOpAssignNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            arguments
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents <code> EXPR1 OP= EXPR2 </code>
     */
    protected LinkedList _visitBinaryOpAssignNode(BinaryOpAssignNode node,
            LinkedList args, String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

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
     * Returns the code represented by a <code>BuiltInLTLNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            arguments
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> BUILTINLTL (event1, event2)</code>
     */
    protected LinkedList _visitBuiltInLTLNode(BuiltInLTLNode node,
            LinkedList args, String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(node.getEvent1().accept(this, args));
        retList.addLast(", ");
        retList.addLast(node.getEvent2().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>SingleLTLFormulaNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            arguments
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> SINGLELTL (SUBFORM) </code>
     */
    protected LinkedList _visitSingleLTLFormulaNode(SingleLTLFormulaNode node,
            LinkedList args, String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(opString);
        retList.addLast("(");
        retList.addLast(node.getSubform().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by a <code>DoubleLTLFormulaNode</code>.
     *
     * @param node
     *            a node that contains the expressions of the assignment
     * @param args
     *            arguments
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents
     *         <code> SUBFORM1 DOUBLELTL SUBFORM2 </code>
     */
    protected LinkedList _visitDoubleLTLFormulaNode(DoubleLTLFormulaNode node,
            LinkedList args, String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast("(");
        retList.addLast(node.getSubform1().accept(this, args));
        retList.addLast(") ");
        retList.addLast(opString);
        retList.addLast(" (");
        retList.addLast(node.getSubform2().accept(this, args));
        retList.addLast(")");

        return retList;
    }

    /**
     * Returns the code represented by an <code>QuantifiedActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            arguments
     * @param opString
     *            the string that represents the quantified action
     * @return a list of strings that represents
     *         <code> QUANTIFIER VARS: SUBFORM </code>
     */
    public Object _visitQuantifiedActionNode(QuantifiedActionNode node,
            LinkedList args, String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

        retList.addLast(opString);
        retList.addLast(" ");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getVars())));
        retList.addLast(": ");
        retList.addLast(node.getSubform().accept(this, args));

        return retList;
    }

    /**
     * the default visit method.
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

    // //////////////////////////////////////////////////////////////
    // Newly added protected method to support //
    // mapped behavior and constraints syntaxes //
    // //////////////////////////////////////////////////////////////

    /**
     * Returns the code represented by a <code>ConstraintDeclNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @param opString
     *            a string of the type of the constraint
     * @return a list of strings that represents
     *         <code> opString name ( params ) ( formula )</code>
     */
    public Object _visitConstraintDeclNode(ConstraintDeclNode node,
            LinkedList args, String opString) {
        LineList retList = new LineList(node, _doingMMDebug);

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
    // // private methods ////

    private Object _createArray(String arrayName, String defType, int curDim,
            List dimExpr) {
        LinkedList retList = new LinkedList();
        if (curDim >= dimExpr.size() - 1) {
            return retList;
        }
        String curIdx = "_dim" + String.valueOf(curDim);
        retList.addLast(indent(indentLevel));
        retList.addLast("for (int " + curIdx + "=0; " + curIdx + "<");
        retList.addLast(dimExpr.get(curDim));
        retList.addLast("; " + curIdx + "++) {\n");
        increaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast(arrayName + "[" + curIdx + "] = new " + defType);
        for (int i = 0; i < dimExpr.size() - curDim - 2; i++)
            retList.addLast("*");
        retList.addLast(" [");
        retList.addLast(dimExpr.get(curDim + 1));
        retList.addLast("];\n");
        retList.addLast(_createArray(arrayName + "[" + curIdx + "]", defType,
                curDim + 1, dimExpr));
        decreaseIndent();
        retList.addLast(indent(indentLevel));
        retList.addLast("}");
        if (curDim > 0)
            retList.addLast("\n");
        return retList;
    }

    /**
     * Returns the body code represented by a <code>LabeledBlockNode</code>,
     * <code>LabeledStmtNode</code>, <code>ActionLabelStmtNode</code> or
     * <code>MethodDeclNode</code>
     *
     * @param stmtsO
     *            the body of above nodes
     * @param args
     *            a list of arguments to this visit method the first arg is the
     *            type of the object(Process, Medium etc.); the second last arg
     *            is a list of referenced interfaces by await defined in either
     *            a medium or processes connected to the medium; the last arg is
     *            a list of interfaces that a method implements.
     * @param type
     *            either a LABEL type or an INTFCFUNC type
     * @param name
     *            the name of the label or the method
     * @param node
     *            The <code>TreeNode</code> from whose visiting method this is
     *            called.
     * @return SystemC code to simulate the annotated action
     */
    private Object _getAnnotatedBodyCode(List stmtsO, LinkedList args,
            String type, String name, TreeNode node) {

        Integer blockEndLineNumber = null; // For mm debugging, below.

        // search for beg and end annotation
        Iterator stmtsIterO = stmtsO.iterator();
        //List stmts;
        Iterator stmtsIter;
        TreeNode stmtO = null, stmt = null;
        StatementNode begStmt = null;
        StatementNode endStmt = null;

        while (stmtsIterO.hasNext()) {
            stmtO = (TreeNode) stmtsIterO.next();
            if (!(stmtO instanceof AnnotationNode))
                continue;

            stmtsIter = ((AnnotationNode) stmtO).getStmts().iterator();
            while (stmtsIter.hasNext()) {
                stmt = (TreeNode) stmtsIter.next();
                if (stmt instanceof BeginAnnotationNode) {
                    begStmt = (StatementNode) stmt;
                } else if (stmt instanceof EndAnnotationNode) {
                    endStmt = (StatementNode) stmt;
                }
            }
        }

        boolean begBuiltInLOC = false;
        boolean endBuiltInLOC = false;
        Hashtable hs = (Hashtable) node.getProperty(BUILTINLOCEVENT_KEY);
        if (hs != null) {
            stmtsIter = hs.keySet().iterator();
            while (stmtsIter.hasNext() && (!begBuiltInLOC || !endBuiltInLOC)) {
                Event e = (Event) stmtsIter.next();
                int kind = e.getKind();
                if (kind == Event.BEG)
                    begBuiltInLOC = true;
                else if (kind == Event.END)
                    endBuiltInLOC = true;
            }
        }

        boolean begSynched = false;
        boolean endSynched = false;
        LinkedList synchInfo = (LinkedList) node.getProperty(LTLSYNCHEVENT_KEY);
        if (synchInfo != null) {
            stmtsIter = synchInfo.iterator();
            while (stmtsIter.hasNext() && (!begSynched || !endSynched)) {
                OneSynchEvent se = (OneSynchEvent) stmtsIter.next();
                int kind = se.getEvent().getKind();
                if (kind == Event.BEG)
                    begSynched = true;
                else if (kind == Event.END)
                    endSynched = true;
            }
        }

        boolean begSynchImply = false;
        boolean endSynchImply = false;
        Hashtable synchImplyInfo = (Hashtable) node
                .getProperty(LTLSYNCHIMPLYEVENT_KEY);
        if (synchImplyInfo != null) {
            stmtsIter = synchImplyInfo.keySet().iterator();
            while (stmtsIter.hasNext() && (!begSynchImply || !endSynchImply)) {
                Event e = (Event) stmtsIter.next();
                int kind = e.getKind();
                if (kind == Event.BEG)
                    begSynchImply = true;
                else if (kind == Event.END)
                    endSynchImply = true;
            }
        }

        LinkedList referencedInterfaces = new LinkedList();
        LinkedList implementedInterfaces = new LinkedList();

        if (type.equals("INTFCFUNC")) {
            referencedInterfaces = (LinkedList) args.get(args.size() - 2);
            LinkedList temp = (LinkedList) args.getLast();
            Iterator iter = temp.iterator();
            while (iter.hasNext()) {
                String intfcName = (String) iter.next();
                if (referencedInterfaces.contains(intfcName))
                    implementedInterfaces.addLast(intfcName);
            }
        } // end if

        boolean usedIntfc = (implementedInterfaces.size() > 0);

        boolean begLTL = false;
        boolean endLTL = false;
        LinkedList ltlInfo = (LinkedList) node.getProperty(LTLEVENT_KEY);
        if (ltlInfo != null) {
            stmtsIter = ltlInfo.iterator();
            while (stmtsIter.hasNext() && (!begLTL || !endLTL)) {
                Event e = (Event) stmtsIter.next();
                int kind = e.getKind();
                if (kind == Event.BEG)
                    begLTL = true;
                else if (kind == Event.END)
                    endLTL = true;
            }
        }

        boolean begLOC = false;
        boolean endLOC = false;

        LinkedList locInfo = (LinkedList) node.getProperty(LOCEVENT_KEY);
        if (locInfo != null) {
            stmtsIter = locInfo.iterator();
            while (stmtsIter.hasNext() && (!begLOC || !endLOC)) {
                Event e = (Event) stmtsIter.next();
                int kind = e.getKind();
                if (kind == Event.BEG)
                    begLOC = true;
                else if (kind == Event.END)
                    endLOC = true;
            }
        }

        boolean begNeedResolve = (begStmt != null) | begSynched | begSynchImply
                | usedIntfc | begLTL | begLOC | begBuiltInLOC;
        boolean endNeedResolve = (endStmt != null) | endSynched | endSynchImply
                | usedIntfc | endLTL | endLOC | endBuiltInLOC;

        LineList retList = new LineList(node, _doingMMDebug, true);

        if (begNeedResolve | endNeedResolve) {
            if (_doingMMDebug) {

                // Set the current line number and event tag in the
                // ProgramCounter, in preparation for being blocked at the
                // "begin" event:
                retList.addLast(indent(indentLevel));
                retList
                        .add("pc->setCurrentLineNumber(\""
                                + DebugUtil.getCurrentLineNumberString(node)
                                + "\");\n");
                retList.addLast(indent(indentLevel));
                retList
                        .addLast("gAllCurrentLines=gMmdbSB->getAllCurrentLines();\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->eventTag=pc->BEG_TAG; "
                        + "gAllEventTags = gMmdbSB->getAllEventTags();\n");

                // Find the line number of the closing brace, for further down
                // in setting a break point at exit to the method/block:
                if (node instanceof MethodDeclNode) {
                    BlockNode body = (BlockNode) ((MethodDeclNode) node)
                            .getBody();
                    blockEndLineNumber = (Integer) body
                            .getProperty(BLOCK_END_LINENUMBER_KEY);
                } else if (node instanceof LabeledBlockNode
                        || node instanceof ActionLabelStmtNode) {
                    blockEndLineNumber = (Integer) node
                            .getProperty(BLOCK_END_LINENUMBER_KEY);
                } else if (node instanceof LabeledStmtNode) {
                    StatementNode statement = ((LabeledStmtNode) node)
                            .getStmt();
                    if (statement instanceof BlockNode) {
                        blockEndLineNumber = (Integer) ((TreeNode) statement)
                                .getProperty(BLOCK_END_LINENUMBER_KEY);
                    } else {
                        blockEndLineNumber = (Integer) node
                                .getProperty(LINENUMBER_KEY);
                    }
                }
                if (blockEndLineNumber == null) {
                    // Punt.
                    blockEndLineNumber = (Integer) node
                            .getProperty(LINENUMBER_KEY);
                }
            }
            retList.addLast("\n");
            retList.addLast(_parOpen());
            retList.addLast(indent(indentLevel));
            retList.addLast("INIT(" + type + ")\n\n");
        } else {
            if (_doingMMDebug && type.equals("INTFCFUNC")) {
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->pushNewNextLineContext();\n");
            }
        }

        if (_doingMMDebug) {
            // Start next-line context stack w/ empty entry, so any first
            // statement
            // can be preceded by a pop & not have to be treated specially.
            retList.addEmptyNextLinePush();
            if (type.equals("INTFCFUNC")) {
                retList.addLineDirective();
            }
        }

        if (begNeedResolve) {
            retList.addLast(indent(indentLevel));
            retList.addLast("//beg event\n");

            if (begStmt instanceof BeginAnnotationNode || begBuiltInLOC) {
                retList.addLast(indent(indentLevel));
                retList.addLast("if (ANNOTATION_ENABLED) {\n");
                increaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->funcType |= FunctionType::ANNOTATION;\n");
                if (begBuiltInLOC) {
                    hs = (Hashtable) node.getProperty(BUILTINLOCEVENT_KEY);
                    stmtsIter = hs.keySet().iterator();
                    while (stmtsIter.hasNext()) {
                        Event e = (Event) stmtsIter.next();
                        int kind = e.getKind();
                        if (kind != Event.BEG)
                            continue;
                        LinkedList locs = (LinkedList) hs.get(e);
                        Iterator it = locs.iterator();
                        while (it.hasNext()) {
                            BuiltInLOC loc = (BuiltInLOC) it.next();
                            String locType;
                            switch (loc.getKind()) {
                            case Constraint.PERIOD:
                                locType = "PERIOD";
                                break;
                            case Constraint.MAXDELTA:
                                if (loc.getEvents().getFirst() == e)
                                    continue;
                                locType = "MAXDELTA";
                                break;
                            case Constraint.MINDELTA:
                                if (loc.getEvents().getFirst() == e)
                                    continue;
                                locType = "MINDELTA";
                                break;
                            case Constraint.MAXRATE:
                                locType = "MAXRATE";
                                break;
                            case Constraint.MINRATE:
                                locType = "MINRATE";
                                break;
                            default:
                                throw new RuntimeException(
                                        "Unexpected built-in LOC constraint.\n"
                                                + loc.toString());
                            }

                            int proc = e.getProcess().getObjectID();
                            int obj = e.getNodeObject().getObjectID();
                            retList.addLast(indent(indentLevel));
                            retList.addLast("if (pc->p->_id==" + proc);
                            retList.addLast(" && this->_id==" + obj);
                            retList.addLast(")\n");

                            retList.addLast(indent(indentLevel));
                            retList.addLast("  _sb.BILOCQuantities["
                                    + ((INode) loc.getQuantities().getFirst())
                                            .getObjectID());
                            retList.addLast("]->registerLOC(pc->p, ");
                            retList.addLast("BuiltInLOCType::" + locType);
                            if (locType.endsWith("DELTA")) {
                                Event ref = (Event) loc.getEvents().getFirst();
                                if (ref.getKind() == Event.BEG)
                                    retList.addLast(", beg(pc->p, this, \""
                                            + ref.getName() + "\"), ");
                                else
                                    // Event.END
                                    retList.addLast(", end(pc->p, this, \""
                                            + ref.getName() + "\"), ");
                                retList.addLast("beg(pc->p, this, \"" + name
                                        + "\"), ");
                            } else {
                                retList.addLast(", beg(pc->p, this, \"" + name
                                        + "\"), NULL, ");
                            }
                            retList.addLast("new GlobalTimeRequestClass(");
                            StringTokenizer value = new StringTokenizer(loc
                                    .getValue(), ".");
                            retList.addLast(value.nextToken());
                            while (value.hasMoreTokens()) {
                                retList.addLast("->" + value.nextToken());
                            }
                            retList.addLast("));\n");
                        }// end while it
                    }// end while stmtsIter
                }// end if
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("ADD_PENDING_EVENT(beg, \"" + name + "\")\n\n");

            // xichen_loc_beg
            // if (type.equals("LABEL"))
            if (begLOC || endLOC)
                retList.addLast(_traceCodeLOC(Event.BEG, name, args));
            // xichen_loc_end

            if (usedIntfc) {
                Iterator iter = implementedInterfaces.iterator();
                while (iter.hasNext()) {
                    retList.addLast(indent(indentLevel));
                    retList
                            .addLast("SETUP_STATE(\"" + iter.next()
                                    + "\");\n\n");
                }
            } else {
                retList.addLast(indent(indentLevel));
                retList.addLast("SETUP_STATE(\"\");\n");
            }

            if (begStmt instanceof BeginAnnotationNode) {
                retList.addLast(indent(indentLevel));
                retList.addLast("if (ANNOTATION_ENABLED) {\n");
                increaseIndent();
                retList.addLast(begStmt.accept(this, args));
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}\n\n");
            }

            if (begSynched) {
                boolean first = true;
                stmtsIter = synchInfo.iterator();
                while (stmtsIter.hasNext()) {
                    OneSynchEvent se = (OneSynchEvent) stmtsIter.next();
                    Event e = se.getEvent();
                    int kind = e.getKind();
                    if (kind != Event.BEG)
                        continue;
                    int proc = e.getProcess().getObjectID();
                    int obj = e.getNodeObject().getObjectID();
                    int id = se.getSynchGroupID();
                    retList.addLast(indent(indentLevel));
                    if (!first)
                        retList.addLast("else ");
                    first = false;
                    retList.addLast("if (pc->p->_id==" + proc);
                    retList.addLast(" && this->_id==" + obj);
                    retList.addLast(") {\n");
                    increaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("updateSynchEventInfo(" + id + ", pc);\n");
                    Iterator varIter = se.getVariables().iterator();
                    while (varIter.hasNext()) {
                        VarAttribute var = (VarAttribute) varIter.next();
                        if (!var.getType().startsWith("constant")) {
                            retList.addLast(indent(indentLevel));
                            retList.addLast("_synchEqualVars_" + id + "["
                                    + var.getIndexInSynchGroup() + "] = &("
                                    + var.getVarName() + ");\n");
                        }
                    }// end while varIter
                    _header.addExternDecl("void** _synchEqualVars_" + id);
                    decreaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("}\n");
                }// end while stmtsIter
            }// end if begSynched

            if (begSynchImply) {
                stmtsIter = synchImplyInfo.keySet().iterator();
                boolean first = true;
                while (stmtsIter.hasNext()) {
                    Event e = (Event) stmtsIter.next();
                    if (e.getKind() != Event.BEG)
                        continue;
                    LinkedList ps = (LinkedList) synchImplyInfo.get(e);
                    int proc = e.getProcess().getObjectID();
                    int obj = e.getNodeObject().getObjectID();
                    if (first)
                        retList.addLast(indent(indentLevel));
                    else
                        retList.addLast(" else ");
                    retList.addLast("if (pc->p->_id==" + proc);
                    retList.addLast(" && this->_id==" + obj + ") {\n");
                    increaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("pc->_inSynch = true;\n");
                    if (ps.size() > 0) {
                        Iterator pIter = ps.iterator();
                        while (pIter.hasNext()) {
                            LinkedList p = (LinkedList) pIter.next();
                            retList.addLast(indent(indentLevel));
                            retList.addLast("ltlsi[" + p.get(1) + "]->");
                            retList.addLast("eqvars[" + p.get(2) + "]->");
                            retList.addLast("v[" + p.get(3) + "]->v = &(");
                            retList.addLast(p.get(0) + ");\n");
                        }// end while pIter
                    }// end if ps.size()>0
                    decreaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("}");
                    first = false;
                }// end while stmtsIter
                retList.addLast("\n");
            }// end if begSynchImply

            if (usedIntfc) {
                retList.addLast(indent(indentLevel));
                retList.addLast("_potentialUserPCs.insert(pc);\n\n");
            }

            retList.addLast(indent(indentLevel));
            retList.addLast("RESOLVE_EXEC\n\n");
            if (_doingMMDebug) {
                retList.addLast(indent(indentLevel));
                retList.addLast("gProcessName = pc->p->name();\n");
            }

            if (usedIntfc) {
                retList.addLast(indent(indentLevel));
                retList.addLast("_potentialUserPCs.erase(pc);\n\n");
            }

            _labels.push();
            retList.addLast(indent(indentLevel));
            retList.addLast("if (pc->SchedState == SchedStateVal::RUN) {\n");
            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->SchedState = SchedStateVal::RUNNING;\n");
            if (_doingMMDebug) {
                // if (node instanceof MethodDeclNode) {
                int firstStmtIndex = DebugUtil.getFirstNonemptyStatementIndex(
                        stmtsO, node);
                if (firstStmtIndex != -1) {
                    TreeNode firstStmt = (TreeNode) stmtsO.get(firstStmtIndex);
                    // retList.addLast("pc->pushNewNextLineContext(); // C\n");
                    LineList firstStmtLineList = new LineList(firstStmt, true,
                            true);
                    firstStmtLineList.addNextLinePush(true);
                    firstStmtLineList.addCurrentLineInfo();
                    retList.addLast(firstStmtLineList);
                }
                // }
                retList.addLast(indent(indentLevel));
                // retList.addLast("if (pc != gCurrentPC) {DebuggerInfo::");
                // retList.addLast("processSwitched(pc);}\n");
                retList.addLast("DebuggerInfo::processSwitched(pc);\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->eventTag=pc->NO_EVENT_TAG; "
                        + "gAllEventTags = gMmdbSB->getAllEventTags();\n");
            }
            if (usedIntfc) {
                retList.addLast(indent(indentLevel));
                retList.addLast("((medium*)pc->obj.object)->addUsedIntfc("
                        + "pc->obj.intfcName, pc);\n");
            }
            retList.addLast(indent(indentLevel));
            retList.addLast("try {");
        } // end beg first half

        // body of the event
        increaseIndent();
        stmtsIter = stmtsO.iterator();
        while (stmtsIter.hasNext()) {
            stmt = (TreeNode) stmtsIter.next();
            if (stmt instanceof AnnotationNode)
                continue;
            else
                retList.addLast(stmt.accept(this, args));
        }
        decreaseIndent();

        if (begNeedResolve) {
            retList.addLast(indent(indentLevel));
            retList.addLast("} catch (STUCK) { stuck_flag = true; }\n");
            retList.addLast(_labels.getCatchStatement());
            decreaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("} else \n");
            retList.addLast(indent(indentLevel + _INDENT_ONCE));
            retList.addLast("stuck_flag = true;\n\n");
            _labels.pop();
        }// end beg second half

        if (endNeedResolve) {
            retList.addLast(indent(indentLevel));
            retList.addLast("if (!stuck_flag) {\n");

            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("//end event\n");

            if (endStmt instanceof EndAnnotationNode || endBuiltInLOC) {
                retList.addLast(indent(indentLevel));
                retList.addLast("if (ANNOTATION_ENABLED) {\n");
                increaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->funcType |= FunctionType::ANNOTATION;\n");
                if (endBuiltInLOC) {
                    hs = (Hashtable) node.getProperty(BUILTINLOCEVENT_KEY);
                    stmtsIter = hs.keySet().iterator();
                    while (stmtsIter.hasNext()) {
                        Event e = (Event) stmtsIter.next();
                        int kind = e.getKind();
                        if (kind != Event.END)
                            continue;
                        LinkedList locs = (LinkedList) hs.get(e);
                        Iterator it = locs.iterator();
                        while (it.hasNext()) {
                            BuiltInLOC loc = (BuiltInLOC) it.next();
                            String locType;
                            switch (loc.getKind()) {
                            case Constraint.PERIOD:
                                locType = "PERIOD";
                                break;
                            case Constraint.MAXDELTA:
                                if (loc.getEvents().getFirst() == e)
                                    continue;
                                locType = "MAXDELTA";
                                break;
                            case Constraint.MINDELTA:
                                if (loc.getEvents().getFirst() == e)
                                    continue;
                                locType = "MINDELTA";
                                break;
                            case Constraint.MAXRATE:
                                locType = "MAXRATE";
                                break;
                            case Constraint.MINRATE:
                                locType = "MINRATE";
                                break;
                            default:
                                throw new RuntimeException(
                                        "Unexpected built-in LOC constraint.\n"
                                                + loc.toString());
                            }

                            int proc = e.getProcess().getObjectID();
                            int obj = e.getNodeObject().getObjectID();
                            retList.addLast(indent(indentLevel));
                            retList.addLast("if (pc->p->_id==" + proc);
                            retList.addLast(" && this->_id==" + obj);
                            retList.addLast(")\n");

                            retList.addLast(indent(indentLevel));
                            retList.addLast("  _sb.BILOCQuantities["
                                    + ((INode) loc.getQuantities().getFirst())
                                            .getObjectID());
                            retList.addLast("]->registerLOC(pc->p, ");
                            retList.addLast("BuiltInLOCType::" + locType);
                            if (locType.endsWith("DELTA")) {
                                Event ref = (Event) loc.getEvents().getFirst();
                                if (ref.getKind() == Event.BEG)
                                    retList.addLast(", beg(pc->p, this, \""
                                            + ref.getName() + "\"), ");
                                else
                                    // Event.END
                                    retList.addLast(", end(pc->p, this, \""
                                            + ref.getName() + "\"), ");
                                retList.addLast("end(pc->p, this, \"" + name
                                        + "\"), ");
                            } else {
                                retList.addLast(", end(pc->p, this, \"" + name
                                        + "\"), NULL, ");
                            }
                            retList.addLast("new GlobalTimeRequestClass(");
                            StringTokenizer value = new StringTokenizer(loc
                                    .getValue(), ".");
                            retList.addLast(value.nextToken());
                            while (value.hasMoreTokens()) {
                                retList.addLast("->" + value.nextToken());
                            }
                            retList.addLast("));\n");
                        }// end while it
                    }// end while stmtsIter
                }// end if
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("} else pc->funcType &= "
                        + "(~FunctionType::ANNOTATION);\n\n");
            } else {
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->funcType &= "
                        + "(~FunctionType::ANNOTATION);\n\n");
            }

            if (_doingMMDebug) {
                // Add current line number of "end" event, and set the "end"
                // event tag in the ProgramCounter, in preparation for being
                // blocked on the "end" event. (The #line directive w/ this
                // line # doesn't get inserted, though, until just after
                // "pc->restoreState()", below.)
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->setCurrentLineNumber(\""
                        + DebugUtil.getCurrentLineNumberString(node,
                                blockEndLineNumber) + "\");\n");
                retList.addLast(indent(indentLevel));
                retList
                        .addLast("gAllCurrentLines=gMmdbSB->getAllCurrentLines();\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->eventTag=pc->END_TAG; "
                        + "gAllEventTags = gMmdbSB->getAllEventTags();\n");
                // retList.addLineDirective(blockEndLineNumber);
            }

            retList.addLast(indent(indentLevel));
            retList.addLast("ADD_PENDING_EVENT(end, \"" + name + "\")\n\n");

            // xichen_loc_beg
            // if (type.equals("LABEL"))
            if (begLOC || endLOC)
                retList.addLast(_traceCodeLOC(Event.END, name, args));
            // xichen_loc_end

            if (usedIntfc) {
                Iterator iter = implementedInterfaces.iterator();
                while (iter.hasNext()) {
                    retList.addLast(indent(indentLevel));
                    retList
                            .addLast("SETUP_STATE(\"" + iter.next()
                                    + "\");\n\n");
                }
            } else {
                retList.addLast(indent(indentLevel));
                retList.addLast("SETUP_STATE(\"\");\n");
            }

            if (endStmt instanceof EndAnnotationNode) {
                retList.addLast(indent(indentLevel));
                retList.addLast("if (ANNOTATION_ENABLED) {\n");
                increaseIndent();
                retList.addLast(endStmt.accept(this, args));
                decreaseIndent();
                retList.addLast(indent(indentLevel));
                retList.addLast("}\n\n");
            }

            if (endSynched) {
                boolean first = true;
                stmtsIter = synchInfo.iterator();
                while (stmtsIter.hasNext()) {
                    OneSynchEvent se = (OneSynchEvent) stmtsIter.next();
                    Event e = se.getEvent();
                    int kind = e.getKind();
                    if (kind != Event.END)
                        continue;
                    int proc = e.getProcess().getObjectID();
                    int obj = e.getNodeObject().getObjectID();
                    int id = se.getSynchGroupID();
                    retList.addLast(indent(indentLevel));
                    if (!first)
                        retList.addLast("else ");
                    first = false;
                    retList.addLast("if (pc->p->_id==" + proc);
                    retList.addLast(" && this->_id==" + obj);
                    retList.addLast(") {\n");
                    increaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("updateSynchEventInfo(" + id + ", pc);\n");
                    Iterator varIter = se.getVariables().iterator();
                    while (varIter.hasNext()) {
                        VarAttribute var = (VarAttribute) varIter.next();
                        if (!var.getType().startsWith("constant")) {
                            retList.addLast(indent(indentLevel));
                            retList.addLast("_synchEqualVars_" + id + "["
                                    + var.getIndexInSynchGroup() + "] = &("
                                    + var.getVarName() + ");\n");
                        }
                    }// end while varIter
                    _header.addExternDecl("void** _synchEqualVars_" + id);
                    decreaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("}\n");
                }// end while stmtsIter
            }// end if endSynched

            if (endSynchImply) {
                stmtsIter = synchImplyInfo.keySet().iterator();
                boolean first = true;
                while (stmtsIter.hasNext()) {
                    Event e = (Event) stmtsIter.next();
                    if (e.getKind() != Event.END)
                        continue;
                    LinkedList ps = (LinkedList) synchImplyInfo.get(e);
                    int proc = e.getProcess().getObjectID();
                    int obj = e.getNodeObject().getObjectID();
                    if (first)
                        retList.addLast(indent(indentLevel));
                    else
                        retList.addLast(" else ");
                    retList.addLast("if (pc->p->_id==" + proc);
                    retList.addLast(" && this->_id==" + obj + ") {\n");
                    increaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("pc->_inSynch = true;\n");
                    if (ps.size() > 0) {
                        Iterator pIter = ps.iterator();
                        while (pIter.hasNext()) {
                            LinkedList p = (LinkedList) pIter.next();
                            retList.addLast(indent(indentLevel));
                            retList.addLast("ltlsi[" + p.get(1) + "]->");
                            retList.addLast("eqvars[" + p.get(2) + "]->");
                            retList.addLast("v[" + p.get(3) + "]->v = &(");
                            retList.addLast(p.get(0) + ");\n");
                        }// end while pIter
                    }// end if ps.size()>0
                    decreaseIndent();
                    retList.addLast(indent(indentLevel));
                    retList.addLast("}");
                    first = false;
                }// end while stmtsIter
                retList.addLast("\n");
            }// end if endSynchImply

            retList.addLast(indent(indentLevel));
            retList.addLast("RESOLVE_EXEC\n\n");
            if (_doingMMDebug) {
                retList.addLast(indent(indentLevel));
                retList.addLast("gProcessName = pc->p->name();\n");
            }

            retList.addLast(indent(indentLevel));
            retList.addLast("if (pc->SchedState == SchedStateVal::RUN) {\n");

            increaseIndent();
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->SchedState = SchedStateVal::RUNNING;\n");

            if (_doingMMDebug) {
                retList.addLast(indent(indentLevel));
                // retList.addLast("if (pc != gCurrentPC) {DebuggerInfo::");
                // retList.addLast("processSwitched(pc);}\n");
                retList.addLast("DebuggerInfo::processSwitched(pc);\n");
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->eventTag=pc->NO_EVENT_TAG; "
                        + "gAllEventTags = gMmdbSB->getAllEventTags();\n");
            }

            if (usedIntfc) {
                retList.addLast(indent(indentLevel));
                retList.addLast("((medium*)pc->obj.object)->releaseUsedIntfc("
                        + "pc->obj.intfcName, pc);\n");
            }
            decreaseIndent();

            retList.addLast(indent(indentLevel));
            retList.addLast("} else stuck_flag = true;\n\n");

            decreaseIndent();

            retList.addLast(indent(indentLevel));
            retList.addLast("}\n\n");
        } // end 'end' part

        if (begNeedResolve | endNeedResolve) {
            if (usedIntfc) {
                retList.addLast(indent(indentLevel));
                retList.addLast("((medium*)pc->obj.object)->releaseUsedIntfc("
                        + "pc->obj.intfcName, pc);\n");
            }
            // retList.addLast(indent(indentLevel));
            // retList.addLast("FINISH("+type+")\n\n");
            retList.addLast(indent(indentLevel));
            retList.addLast("pc->restoreState();\n");

            if (_doingMMDebug) {
                retList.addLineDirective(blockEndLineNumber);
            }

            retList.addLast(indent(indentLevel));
            retList.addLast("if (stuck_flag) throw STUCK();\n");
            retList.addLast(_labels.getReturnStatement());
            retList.addLast(_parClose());
        } else {
            if (_doingMMDebug && type.equals("INTFCFUNC")) {
                retList.addLast(indent(indentLevel));
                retList.addLast("pc->popNextLineContext();\n");
            }
        }

        return retList;
    }

    /**
     * Return the boolean value of <code>_doingMMDebug</code>.
     *
     * @return Return true if doing debugging
     */
    protected boolean _getDoingMMDebug() {
        return _doingMMDebug;
    }

    /**
     * Generate dummy constructors with argument DUMMY_CTOR_ARG for mmm objects
     * (processes, media, statemedia, schedulers, NOT for classes) For classes,
     * generate a constructor with no arguments if no such a constructor is
     * provided by user.
     *
     * @param node
     *            Object node that is being visited.
     * @param args
     *            Arguments of the constructor.
     * @return a LinkedList of String
     */
    public Object _getDummyConstructorCode(ClassDeclNode node, LinkedList args) {
        LineList retList = new LineList(node, _doingMMDebug);
        increaseIndent();
        retList.addLast(indent(indentLevel));

        _header.dummy_ctor_gen(node, args);

        if (args.get(0).equals("Class")) {
            retList.addLast("\n");
            if (_header.hasTemplate()) {
                retList.addLast(_header.getTemplate() + "\n");
            }
            retList.addLast(node.getName().accept(this, args));
            if (_header.hasTemplate()) {
                retList.addLast(_header.getTemplateTypes());
            }
            retList.addLast("::");
            retList.addLast(node.getName().accept(this, args));
            retList.addLast("(DUMMY_CTOR_ARG _arg, int *index)");
        } else {
            retList.addLast("\n");
            if (_header.hasTemplate()) {
                retList.addLast(_header.getTemplate() + "\n");
            }
            retList.addLast(node.getName().accept(this, args));
            if (_header.hasTemplate()) {
                retList.addLast(_header.getTemplateTypes());
            }
            retList.addLast("::");
            retList.addLast(node.getName().accept(this, args));
            retList.addLast("(sc_module_name _name, DUMMY_CTOR_ARG _arg, "
                    + "int *index) ");
        }

        LinkedList aryPts = new LinkedList();
        Object[] arrayPorts = null;
        int indexSum = 0;
        Enumeration key = _ports.keys();
        while (key.hasMoreElements()) {
            String name = (String) key.nextElement();
            String[] type = (String[]) _ports.get(name);
            int dim = Integer.parseInt(type[1]);
            if (dim > 0) {
                indexSum += dim;
                aryPts.addLast(name);
            }
        }

        if (indexSum > 0) {
            arrayPorts = aryPts.toArray();
            Arrays.sort(arrayPorts);
        }

        _inObjectDef = true;
        if (node.getSuperClass() instanceof AbsentTreeNode) {
            if (args.get(0).equals("Process")) {
                retList.addLast(": process(_name)");
            } else if (args.get(0).equals("Medium")) {
                retList.addLast(": medium(_name)");
            } else if (args.get(0).equals("StateMedium")) {
                retList.addLast(": statemedium(_name)");
            } else if (args.get(0).equals("Scheduler")) {
                retList.addLast(": scheduler(_name)");
            } else if (args.get(0).equals("Quantity")) {
                retList.addLast(": quantity(_name)");
            }
        } else if (args.get(0).equals("Class")) {
            retList.addLast(": ");
            retList.addLast(node.getSuperClass().accept(this, args));
            retList.addLast("(_arg, &index[" + indexSum + "])");
        } else {
            retList.addLast(": ");
            retList.addLast(node.getSuperClass().accept(this, args));
            retList.addLast("(_name, _arg, &index[" + indexSum + "])");
        }
        _inObjectDef = false;

        retList.addLast(" {\n");
        if (args.get(0).equals("Process")) {
            increaseIndent();
            if (!_header.hasSuperProcess()) {
                retList.addLast(indent(indentLevel));
                retList.addLast("SC_THREAD(thread);\n");
            }
            decreaseIndent();
        }
        if (indexSum > 0) {
            indexSum = 0;
            for (int i = 0; i < arrayPorts.length; i++) {
                String[] type = (String[]) _ports.get(arrayPorts[i]);
                retList.addLast(indent(indentLevel));
                retList.addLast(arrayPorts[i]);
                retList.addLast(" = new sc_port<");
                retList.addLast(type[0]);
                retList.addLast(" > ");
                int dim = Integer.parseInt(type[1]);
                if (dim == 1)
                    retList.addLast("[index[" + (indexSum++) + "]]");
                else {
                    for (int m = 1; m < dim; m++)
                        retList.addLast("*");
                    retList.addLast(" ");
                    retList.addLast("[index[" + indexSum + "]];\n");
                    LinkedList dimExpr = new LinkedList();
                    for (int j = 0; j < dim; j++)
                        dimExpr.addLast("index[" + (indexSum++) + "]");
                    retList.addLast(_createArray((String) arrayPorts[i],
                            "sc_port<" + type[0] + " >", 0, dimExpr));
                }
                retList.addLast(";\n");
            }
        }
        Iterator member = ((List) node.getMembers()).iterator();
        while (member.hasNext()) {
            TreeNode tnode = (TreeNode) member.next();
            if (tnode instanceof BlackboxNode) {
                String ident = ((BlackboxNode) tnode).getIdent();
                if (ident.equals("SystemCSim")) {
                    String code = ((BlackboxNode) tnode).getCode();
                    if (code.indexOf("insertSystemCSimConstructor") > -1) {
                        retList.addLast(indent(indentLevel));
                        retList.addLast("insertSystemCSimConstructor();\n");
                        break;
                    }
                }
            }
        }
        retList.addLast(indent(indentLevel));
        retList.addLast("}\n");

        decreaseIndent();
        return retList;
    }

    /**
     * Return true if the given TreeNode gets mmdebug code put in its generated
     * C++ source code.
     */
    private boolean _getsDebugCode(TreeNode node) {
        return (_doingMMDebug && !DebugUtil.debuggingDefeated(node));
    }

    private String _parClose() {
        return indent(indentLevel) + "}\n";
    }

    private String _parOpen() {
        return indent(indentLevel) + "{\n";
    }

    // xichen_loc_beg
    /**
     * Generate code for outputing trace to the trace file for loc constraint
     * checking
     */
    private LinkedList _traceCodeLOC(int type, String name, LinkedList args) {
        LinkedList retList = new LinkedList();

        if (_objectName != null) {
            retList.addLast(indent(indentLevel));
            if (type == Event.BEG)
                retList.addLast("/** LOC Checker - beg of a label **/\n");
            else if (type == Event.END)
                retList.addLast("/** LOC Checker - end of a label **/\n");

            Hashtable hash = _net.getAnnotations();
            Enumeration keys = hash.keys();
            while (keys.hasMoreElements()) {
                Event e = (Event) keys.nextElement();
                INode n = e.getNodeObject();
                // xichen: convert classname_$types$ back to classname
                String typeName = n.getType().getName();
                if (typeName.indexOf('$') >= 0
                        && typeName.indexOf('$') != typeName.lastIndexOf('$')) {
                    typeName = typeName.substring(0, typeName.indexOf('$') - 1);
                    // System.out.println(n.getType().getName() + " ---> " +
                    // typeName);
                }
                if (e.getKind() == type && e.getName().equals(name)
                        && typeName.endsWith(_objectName)) {
                    if (((String) args.get(0)).equals("Process")) {
                        retList.addLast(indent(indentLevel));
                        retList.addLast("if (strcmp(\"");
                        retList.addLast(n.getName() + "\", name()) == 0 || ");
                        retList.addLast("strlen(name()) > 19 && strcmp(\"");
                        retList.addLast(n.getName()
                                + "\", name() + 19) == 0)\n");
                    } else {
                        retList.addLast(indent(indentLevel));
                        retList.addLast("if (strcmp(\"");
                        retList
                                .addLast(n.getName()
                                        + "\", name()) == 0 && strcmp(caller->name(), \""
                                        + e.getProcess().getName()
                                        + "\") == 0 || ");
                        retList
                                .addLast("strlen(name()) > 19 && strlen(caller->name()) > 19 && strcmp(\"");
                        retList
                                .addLast(n.getName()
                                        + "\", name() + 19) == 0 && strcmp(caller->name() + 19, \""
                                        + e.getProcess().getName()
                                        + "\") == 0)\n");
                    }
                    increaseIndent();
                    retList.addLast(indent(indentLevel));
                    if (type == Event.BEG)
                        retList.addLast("trace_file << \"BEG_");
                    else if (type == Event.END)
                        retList.addLast("trace_file << \"END_");
                    retList.addLast(e.getProcess().getName() + "_"
                            + n.getName() + "_" + e.getName() + "\"");

                    Iterator iter = (_net.getVariables()).iterator();
                    while (iter.hasNext()) {
                        String var = (String) iter.next();
                        retList.addLast(" << \" \" << ");
                        if (((LinkedList) hash.get(e)).contains(var))
                            retList
                                    .addLast(var.replaceAll("\\x2E", "->")
                                            + " "); // change var.x to var->x
                        else
                            retList.addLast("0 ");
                    }
                    retList.addLast(" << \"\\n\";\n");
                    decreaseIndent();
                }

            }
            retList.addLast("\n");
        }

        return retList;

    }

    // xichen_loc_end

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /** number of spaces for each indentation */
    private final int _INDENT_ONCE = 2;

    /**
     * A flag indicating that we are including <code>#line</code> preprocessor
     * directives in the cpp-code output.
     */
    private boolean _doingMMDebug = false;

    /**
     * Generate code for TypeNameNodes.
     */
    private boolean _inTypeNameNode = false;

    /**
     * In the object (class, process, medium, etc.) prototype definition other
     * than object's body. Used by TypeNameNode to distinguish whether it is
     * used as a type or as a superclass, interfaces.
     */
    public boolean _inObjectDef = false;

    /**
     * Apply interleaving concurrent specific optimization techniques
     */
    private boolean _ic;

    private HeaderCodegen _header = new HeaderCodegen(this);

    private AutoLabel _labels = null;

    /** number of spaces for the previous indent level */
    //private int _lastIndentLevel = 0;
    /** port-portType pairs defined in process or medium */
    private Hashtable _ports = new Hashtable();

    // xichen_loc_beg
    /** used to store elaborated network */
    private Network _net;

    /** the name of the current object declaration process or medium etc. */
    private String _objectName;

    // xichen_loc_end

    /**
     * return type of a method. empty if it is void.
     */
    private String _methodReturnType;
}
