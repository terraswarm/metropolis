/* Generate Promela code from a meta-model AST to a file.

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

package metropolis.metamodel.backends.promela;

import metropolis.metamodel.Effect;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// //////////////////////////////////////////////////////////////////////
// //Internal data structures
class entity {
    public String type; // "Process" or "Medium"

    public String defType;

    public String name;

    public AllocateNode initExpr; // new ByteM("p0", 1)

    public boolean equals(String t, String d, String n) {
        if (type.equals(t) && defType.equals(d) && name.equals(n))
            return true;
        else
            return false;
    }
}

class connection {
    public String src;

    public String port;

    public String des;

    public String inter;

    public String srcDefType;

    public String desDefType;
}

class inter {
    public String name;

    public List members;
}

class variables {
    public LinkedList names = new LinkedList(); // a list of variable names

    public LinkedList scopes = new LinkedList(); // a list of corresponding

    // scopes

    public void addLast(String n, BlockScope s) {
        names.addLast(n);
        scopes.addLast(s);
    }

    public void addFirst(String n, BlockScope s) {
        names.addFirst(n);
        scopes.addFirst(s);
    }

    public BlockScope find(String n, BlockScope s) {

        BlockScope ss = new BlockScope(s);
        while (ss.level >= -1) {
            Iterator iterN = names.iterator();
            Iterator iterS = scopes.iterator();
            while (iterN.hasNext()) {
                String name = (String) iterN.next();
                BlockScope sp = (BlockScope) iterS.next();
                if (name.equals(n) && sp.equals(ss))
                    return sp;
            }
            ss.level--;
        }

        return null;
    }

    public BlockScope find(String n) {
        Iterator iterN = names.iterator();
        Iterator iterS = scopes.iterator();
        BlockScope sp = null;

        while (iterN.hasNext()) {
            String name = (String) iterN.next();
            BlockScope s = (BlockScope) iterS.next();
            if (name.equals(n)) {
                if (sp == null)
                    sp = s;
                else if (sp.level < s.level)
                    sp = s;
            }
        }

        return sp;
    }
}

class function {
    public entity src;

    public entity des;

    public String name;

    public String decl;

    public LinkedList functionCalls;

    public LinkedList args;

    public function() {
        name = new String();
        src = null;
        des = null;
        functionCalls = new LinkedList();
        args = new LinkedList();
    }

    public function(entity d, String n) {
        src = null;
        des = d;
        name = new String(n);
        functionCalls = new LinkedList();
        args = new LinkedList();
    }

    public function(entity s, entity d, String n) {
        src = s;
        des = d;
        name = new String(n);
        functionCalls = new LinkedList();
        args = new LinkedList();
    }

    public boolean equals(entity s, entity d, String n) {
        if (s == null) {
            if (s == null && des.equals(d) && name.equals(n))
                return true;
            else
                return false;
        } else {
            if (src.equals(s) && des.equals(d) && name.equals(n))
                return true;
            else
                return false;
        }
    }
}

class BlockScope {
    public int level; // -1: object member level; 0: function top level; >0:

    // normal block level

    public int[] blockLevel; // identify the block level and which block

    public String[] blockName; // blockName for each level of blocks

    private final int max = 100;

    public BlockScope() {
        level = -1;
        blockLevel = new int[max];
        blockName = new String[max];

        for (int i = 0; i < max; i++) {
            blockLevel[i] = -1;
            blockName[i] = null;
        }
    }

    public BlockScope(BlockScope s) {
        blockLevel = new int[max];
        blockName = new String[max];

        if (s == null) {
            level = -1;

            for (int i = 0; i < max; i++) {
                blockLevel[i] = -1;
                blockName[i] = null;
            }
        } else {
            level = s.level;

            for (int i = 0; i < max; i++) {
                blockLevel[i] = s.blockLevel[i];
                blockName[i] = s.blockName[i];
            }
        }
    }

    public boolean equals(BlockScope s) {
        if (level != s.level)
            return false;
        for (int i = 0; i <= level; i++)
            if (blockLevel[i] != s.blockLevel[i])
                return false;

        return true;
    }

    public void copy(BlockScope s) {
        level = s.level;
        for (int i = 0; i < max; i++)
            blockLevel[i] = s.blockLevel[i];
    }

    public void goIn() {
        level++;
        blockLevel[level]++;
        blockName[level] = null;
        for (int i = level + 1; i < max; i++) {
            blockLevel[i] = -1;
            blockName[i] = null;
        }
    }

    public void goIn(String name) {
        level++;
        blockLevel[level]++;
        blockName[level] = new String(name);
        for (int i = level + 1; i < max; i++) {
            blockLevel[i] = -1;
            blockName[i] = null;
        }
    }

    public void goOut() {
        level--;
    }
}

// ////////////////////////////////////////////////////////////////////////
// // PromelaCodegenVisitor
/**
 * Generate Promela code from a meta-model AST to a file.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Xi Chen
 * @version $Id: PromelaCodegenVisitor.java,v 1.29 2006/10/12 20:32:46 cxh Exp $
 */
public class PromelaCodegenVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Set the traversal method to custom so that indent level can be properly
     * set.
     */
    public PromelaCodegenVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return the code represented by an <code>AwaitLockNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            a list of arguments to this visit method
     * @return a list of strings that represents <code>NODE.IFACE</code>
     */

    // args[0]: "Netlist", "Process", "Medium"
    // args[1]: the name of an object
    // args[2]: the name of an object instance
    // args[3]: method name
    // args[4]: "ProcessName_InstanceName"
    // args[5]: BlockScope
    // args[6]: "await guard"
    // suffix will be appended to the labels:
    // main_exit%local%, await_exit_m_n%local%, continue_m%local%,
    // varialble_name%local%(local variables), function_name%call%
    // function_return%call%
    public Object visitAwaitLockNode(AwaitLockNode node, LinkedList args) {
        LinkedList retList = new LinkedList();

        String lockNode = _stringListToString((List) node.getNode().accept(
                this, args));
        String lockIf = _stringListToString((List) node.getIface().accept(this,
                args));

        if (lockNode.equals("this"))
            retList.addLast(args.get(1).toString() + "_"
                    + args.get(2).toString() + "_" + lockIf);
        else {
            Iterator iterator = connections.iterator();
            while (iterator.hasNext()) {
                // retList.addLast(lockNode + "." + lockIf);
                connection conn = (connection) iterator.next();
                if (conn.src.equals(args.get(2).toString())
                        && conn.port.equals(lockNode)) {
                    retList.addLast(conn.desDefType + "_" + conn.des + "_"
                            + lockIf);
                    break;
                }
            }
            // need to be completed
            // refer connections list
        }

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
        Iterator iterator = node.getGuards().iterator();
        int guardNum = node.getGuards().size();
        int myFlag = awaitFlagCounter++;
        String myExitPoint = new String(exitPoint);
        String methodName = null;
        String syncReturn = null;

        if (args.get(0).equals("Process")) {
            methodName = args.get(1).toString() + "_" + args.get(2).toString()
                    + "_" + args.get(3).toString();
            syncReturn = args.get(1).toString() + "_" + args.get(2).toString();
        } else if (args.get(0).equals("Medium")) {
            methodName = args.get(4).toString() + "_" + args.get(1).toString()
                    + "_" + args.get(2).toString() + "_"
                    + args.get(3).toString();
            syncReturn = args.get(4).toString();
        }

        if (guardNum == 0)
            return retList;

        methodList.addLast(indent(methodIndent));
        methodList.addLast("bool awaitFlag_" + myFlag + "%local%[" + guardNum
                + "];\n");
        for (int i = 0; i < guardNum; i++) {
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("awaitFlag_" + myFlag + "%local%[" + i
                    + "] = false;\n");
        }
        // bodyList.addLast("\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("do\n");
        bodyIndent += indentOnce;
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("::atomic{\n");

        // condition evaluations
        LinkedList conList = new LinkedList();
        while (iterator.hasNext()) {
            AwaitGuardNode guard = (AwaitGuardNode) iterator.next();
            args.set(6, new String("await guard"));
            conList.addLast(guard.getCond().accept(this, args));
            args.set(6, null);
        }

        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("if\n");
        iterator = node.getGuards().iterator();
        Iterator iterCon = conList.iterator();
        int guardIndex = 0;
        while (iterator.hasNext()) {
            AwaitGuardNode guard = (AwaitGuardNode) iterator.next();
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::(");
            bodyList.addLast(iterCon.next());
            bodyList.addLast(" && sync" + syncReturn + "_thread == NONBLOCK");
            // test list
            Iterator iterTest = guard.getLockTest().iterator();
            while (iterTest.hasNext()) {

                String lockTest = _stringListToString((List) ((TreeNode) iterTest
                        .next()).accept(this, args));
                bodyList.addLast(" &&\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast(lockTest + "_state[0] == 0 && " + lockTest
                        + "_state[1] == 0 ");
            }
            bodyList.addLast(") -> \n");

            // set list
            Iterator iterSet = guard.getLockSet().iterator();
            while (iterSet.hasNext()) {
                String lockSet = _stringListToString((List) ((TreeNode) iterSet
                        .next()).accept(this, args));
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast(lockSet + "_state[1]++;\n");
            }

            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("awaitFlag_" + myFlag + "%local%["
                    + (guardIndex++) + "] = true;\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("break;\n\n");
        }
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("::else -> if\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("           ::(sync" + methodName
                + "  == BLOCKABLE)-> skip;\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList
                .addLast("           ::(sync" + methodName
                        + " == UNBLOCKABLE)-> sync" + syncReturn
                        + "_thread = BLOCK;\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("             function_return%local% = true;\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("             goto " + myExitPoint + ";\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("           fi\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("fi\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("}\n");
        bodyIndent -= indentOnce;
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("od;\n\n");

        // guard selection
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("if\n");
        iterator = node.getGuards().iterator();
        guardIndex = 0;
        while (iterator.hasNext()) {
            AwaitGuardNode guard = (AwaitGuardNode) iterator.next();
            int myGuardIndex = guardIndex++;
            exitPoint = "await_exit_" + myFlag + "_" + myGuardIndex + "%local%";

            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::(awaitFlag_" + myFlag + "%local%["
                    + myGuardIndex + "] == true) ->\n");
            bodyIndent += indentOnce;

            // statement
            guard.getStmt().accept(this, args);

            // exit this await statement
            bodyList.addLast("\n");
            bodyList.addLast(indent(bodyIndent - indentOnce));
            bodyList.addLast("await_exit_" + myFlag + "_" + myGuardIndex
                    + "%local%:\n");

            // release set list
            Iterator iterSet = guard.getLockSet().iterator();
            while (iterSet.hasNext()) {
                String lockSet = _stringListToString((List) ((TreeNode) iterSet
                        .next()).accept(this, args));
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast(lockSet + "_state[1]--;\n");
            }

            // exit point
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("if\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::(function_return%local% == true ) -> goto "
                    + myExitPoint + ";\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::(function_return%local% == false) -> skip;\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("fi;\n");

            bodyIndent -= indentOnce;
        }
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("fi;\n");

        // restore the exitPoint to this level !!!
        exitPoint = myExitPoint;

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
        ((BlockScope) args.get(5)).goIn(node.getLabel().getName().getIdent());
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        ((BlockScope) args.get(5)).goOut();
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

        // need to be completed

        return retList;
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

        /*
         * retList.addLast("nondeterminism(");
         * retList.addLast(node.getType().accept(this, args));
         * retList.addLast(")");
         */

        /*
         * String tmpVar = "tmpVariable" + (localVarCounter++);
         * methodList.addLast(indent(methodIndent));
         * methodList.addLast(node.getType().accept(this, args));
         * methodList.addLast(" " + tmpVar + " = 0;\n");
         */

        /*
         * bodyList.addLast(indent(bodyIndent)); bodyList.addLast("do\n");
         * bodyList.addLast(indent(bodyIndent)); bodyList.addLast("::(true) -> " +
         * tmpVar + " = "+ tmpVar + " + 1;\n");
         * bodyList.addLast(indent(bodyIndent)); bodyList.addLast("::(true) -> " +
         * tmpVar + " = "+ tmpVar + " - 1;\n");
         * bodyList.addLast(indent(bodyIndent)); bodyList.addLast("::(true) ->
         * break;\n"); bodyList.addLast(indent(bodyIndent));
         * bodyList.addLast("od;\n");
         */

        // nondeterminism is simplified and it always returns 0
        retList.addLast("0");

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
        connection conn = new connection();

        conn.src = _stringListToString((List) node.getSrcObject().accept(this,
                args));
        conn.port = _stringListToString((List) node.getPort()
                .accept(this, args));
        conn.des = _stringListToString((List) node.getDstObject().accept(this,
                args));
        connections.addLast(conn);

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

        // non-functionnal, only output some comments
        // retList.addLast("/*addcomponent(");
        // retList.addLast(node.getNode().accept(this, args));
        // retList.addLast(", ");
        // retList.addLast(node.getNetlist().accept(this, args));
        // retList.addLast(")");
        // retList.addLast(";*/\n\n");

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

        // non-functionnal, only output some comments
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("/* blackbox code */\n");
        // retList.addLast(indent(indentLevel));
        // retList.addLast("blackbox(");
        // retList.addLast(node.getIdent());
        // retList.addLast(") ");
        // retList.addLast("%%\n");
        // indentLevel += indentOnce;

        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast(node.getCode());

        // indentLevel -= indentOnce;
        // retList.addLast("\n");
        // retList.addLast(indent(indentLevel));
        // retList.addLast("%%\n ***************/");
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
        String objName = ((ProcessDeclNode) node.getParent()).getName()
                .getIdent();
        String defType = _stringListToString((List) node.getDefType().accept(
                this, args));
        String portName = node.getName().getIdent();

        // retList.addLast("/*** port declarations ***\n");
        if (args.get(0).equals("Process") || args.get(0).equals("Medium")) {
            Iterator iterator = entities.iterator();

            // retList.addLast(objName + " " + defType + " " + portName + "\n");

            while (iterator.hasNext()) {
                entity en = (entity) iterator.next();
                if (!objName.equals(en.defType))
                    continue;
                Iterator iterConn = connections.iterator();
                while (iterConn.hasNext()) {
                    connection conn = (connection) iterConn.next();
                    if (conn.src.equals(en.name) && conn.port.equals(portName)) {
                        conn.inter = defType;
                    }
                }
            }
        }

        // retList.addLast(" *** port declarations ***/\n\n");
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

        // non-functionnal

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

        // skip the template declarations
        if (node.getParTypeNames() != null && !node.getParTypeNames().isEmpty()) {
            return retList;
        }

        String processName = node.getName().getIdent();
        Iterator iterator = entities.iterator();
        while (iterator.hasNext()) {
            entity en = (entity) iterator.next();
            if (processName.equals(en.defType))
                en.type = new String("Process");
        }

        args.set(0, new String("Process"));
        args.set(1, new String(processName));
        args.set(5, new BlockScope());

        varRename = new variables();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));

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

        // skip the template declarations
        if (node.getParTypeNames() != null && !node.getParTypeNames().isEmpty()) {
            return retList;
        }

        String mediumName = node.getName().getIdent();
        Iterator iterator = entities.iterator();
        while (iterator.hasNext()) {
            entity en = (entity) iterator.next();
            if (mediumName.equals(en.defType))
                en.type = new String("Medium");
        }

        iterator = node.getInterfaces().iterator();
        while (iterator.hasNext()) {
            TreeNode interfaceNode = (TreeNode) iterator.next();
            String inter = _stringListToString((List) interfaceNode.accept(
                    this, args));
            Iterator iterEntity = entities.iterator();
            while (iterEntity.hasNext()) {
                entity en = (entity) iterEntity.next();
                if (en.defType.equals(mediumName)) {
                    headList2.addLast("byte\t");
                    headList2.addLast(mediumName + "_" + en.name + "_" + inter
                            + "_state[2];\n");
                    initList.addLast(indent(indentLevel));
                    initList.addLast(mediumName + "_" + en.name + "_" + inter
                            + "_state[0] = 0;\n");
                    initList.addLast(indent(indentLevel));
                    initList.addLast(mediumName + "_" + en.name + "_" + inter
                            + "_state[1] = 0;\n");
                }
            }
        }

        args.set(0, new String("Medium"));
        args.set(1, new String(mediumName));
        args.set(5, new BlockScope());
        varRename = new variables();
        retList.addLast(TNLManip.traverseList(this, args, node.getMembers()));

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

        // skip the template declarations
        if (node.getParTypeNames() != null && !node.getParTypeNames().isEmpty()) {
            return retList;
        }

        // non-functional

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

        // non-functional

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

        // skip the template declarations
        if (node.getParTypeNames() != null && !node.getParTypeNames().isEmpty()) {
            return retList;
        }

        // retList.addLast("/* netlist ");
        // retList.addLast(node.getName().accept(this, args));
        // retList.addLast(" */\n");
        String netlistName = node.getName().getIdent();
        args.set(0, new String("Netlist"));
        args.set(1, new String(netlistName));
        args.set(5, new BlockScope());

        Iterator iterator = (node.getMembers()).iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof ConstructorDeclNode) {
                ConstructorDeclNode constructor = (ConstructorDeclNode) object;
                retList.addLast(constructor.accept(this, args));
                break;
            }
        }
        // retList.addLast("/* netlist declaration end */\n\n");

        return retList;
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

        // non-functional
        // retList.addLast(node.getObject().accept(this, args));
        // retList.addLast(".");
        // retList.addLast(node.getName().accept(this, args));

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

        // non-functional
        // retList.addLast("super.");
        // retList.addLast(node.getName().accept(this, args));

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

        // non-functional
        // retList.addLast(node.getObject().accept(this, args));
        // retList.addLast(".");
        // retList.addLast(node.getName().accept(this, args));

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

        // non-functional
        // retList.addLast("super.");
        // retList.addLast(node.getName().accept(this, args));

        return retList;
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
        if (args == null)
            args = new LinkedList();
        args.addFirst(null);
        args.addFirst(null);
        args.addFirst(null);
        args.addFirst(null);
        args.addFirst(null);
        args.addFirst(null);
        args.addFirst(null);

        headList.addLast("#define true                1\n");
        headList.addLast("#define false                0\n\n");
        headList.addLast("#define NONBLOCK        0\n");
        headList.addLast("#define BLOCK                1\n\n");
        headList.addLast("#define UNBLOCKABLE        0\n");
        headList.addLast("#define BLOCKABLE        1\n\n");

        initList.addLast("\ninit{\n");

        indentLevel += indentOnce;
        // initList.addLast(indent(indentLevel));
        // initList.addLast("bool function_return;\n");

        // initList.addLast(indent(indentLevel));
        // initList.addLast("chan syncChan = [0] of {bool};\n");
        // initList.addLast(indent(indentLevel));
        // initList.addLast("bool syncReturn;\n\n");

        // package
        if (node.getPkg() != AbsentTreeNode.instance) {
            retList.addLast("/* package ");
            retList.addLast(node.getPkg().accept(this, args));
            retList.addLast("; */\n");
        }

        // import
        if (!node.getImports().isEmpty()) {
            retList.addLast("/* ");
            retList.addLast(TNLManip
                    .traverseList(this, args, node.getImports()));
            retList.addLast(" */\n");
        }

        // def types
        Iterator iterator = (node.getDefTypes()).iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof NetlistDeclNode) {
                NetlistDeclNode netlist = (NetlistDeclNode) object;
                retList.addLast(netlist.accept(this, args));
            } else if (object instanceof InterfaceDeclNode) {
                InterfaceDeclNode inter = (InterfaceDeclNode) object;
                retList.addLast(inter.accept(this, args));
            }
        }

        iterator = connections.iterator();
        while (iterator.hasNext()) {
            connection conn = (connection) iterator.next();
            Iterator iterEntity = entities.iterator();
            while (iterEntity.hasNext()) {
                entity en = (entity) iterEntity.next();
                if (en.name.equals(conn.src)) {
                    conn.srcDefType = en.defType;
                    break;
                }
            }

            iterEntity = entities.iterator();
            while (iterEntity.hasNext()) {
                entity en = (entity) iterEntity.next();
                if (en.name.equals(conn.des)) {
                    conn.desDefType = en.defType;
                    break;
                }
            }
        }

        // iterator = interfaces.iterator();
        // retList.addLast("\n/*** interfaces list***\n");
        /*
         * while (iterator.hasNext()) { inter tmpInter = (inter)iterator.next();
         * retList.addLast(tmpInter.name); retList.addLast("\n"); Iterator
         * iterMembers = (tmpInter.members).iterator(); while
         * (iterMembers.hasNext()) { Object object = iterMembers.next(); if
         * (object instanceof MethodDeclNode) {
         * retList.addLast(Modifier.toString(((MethodDeclNode)object).getModifiers()));
         * retList.addLast(Effect.toString(((MethodDeclNode)object).getEffect()));
         * retList.addLast(((MethodDeclNode)object).getReturnType().accept(this,
         * args));
         * retList.addLast(((MethodDeclNode)object).getName().accept(this,
         * args)); retList.addLast("(");
         * retList.addLast(_commaList(TNLManip.traverseList(this, args,
         * ((MethodDeclNode)object).getParams()))); retList.addLast(")\n"); } }
         * retList.addLast("\n"); }
         */
        // retList.addLast(" *** interfaces list ***/\n\n");
        iterator = node.getDefTypes().iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof ProcessDeclNode) {
                ProcessDeclNode netList = (ProcessDeclNode) object;
                retList.addLast(netList.accept(this, args));
            }
        }

        iterator = node.getDefTypes().iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof MediumDeclNode) {
                MediumDeclNode netList = (MediumDeclNode) object;
                retList.addLast(netList.accept(this, args));
            }
        }

        initList.addLast("\n");
        initList.addLast(initList2);
        initList.addLast("\n");
        initList.addLast(initRunList);
        indentLevel -= indentOnce;
        initList.addLast(indent(indentLevel));
        initList.addLast("};\n");

        // inline all the functions
        iterator = functions.iterator();
        while (iterator.hasNext()) {

            function f = (function) iterator.next();
            if (!f.name.equals("thread"))
                continue;

            String methodString = f.decl;
            int begin;
            while ((begin = methodString.indexOf("%local%")) != -1) {
                StringBuffer buf = new StringBuffer(methodString);
                buf.replace(begin, begin + 7, "");
                methodString = buf.toString();
            }

            callLevel = 0;
            suffix = new int[100];
            suffix[0] = 0;

            Iterator iterCalls = f.functionCalls.iterator();
            while (iterCalls.hasNext()) {
                begin = methodString.indexOf("%call%");
                StringBuffer buf = new StringBuffer(methodString);
                buf
                        .replace(begin, begin + 6, "_"
                                + Integer.toString(suffix[0]));
                methodString = buf.toString();

                function func = (function) iterCalls.next();
                inlining(func);
                suffix[0]++;
            }

            threadList.addLast(methodString);
        }

        retList.addLast(headList);
        retList.addLast("\n");
        retList.addLast(headList2);
        retList.addLast("\n");
        retList.addLast(headList3);
        retList.addLast("\n");
        retList.addLast("/***          inline functions          ***/\n");
        retList.addLast(inlineList);
        retList.addLast("\n");
        retList
                .addLast("/***          concurrent running threads          ***/\n");
        retList.addLast(threadList);
        retList.addLast("\n");
        retList.addLast(initList);

        // retList.addLast("\n/*** function declarations ***\n");
        /*
         * iterator = functions.iterator(); while (iterator.hasNext()) {
         * function f = (function)iterator.next(); if (f.src != null)
         * retList.addLast("source : " + f.src.name + "\n"); else
         * retList.addLast("source : null\n"); retList.addLast("destination : " +
         * f.des.name + "\n"); retList.addLast("name : " + f.name + "\n");
         * retList.addLast("declaration : \n"); retList.addLast(f.decl);
         * retList.addLast("calls :\n"); Iterator iterCalls =
         * f.functionCalls.iterator(); while (iterCalls.hasNext()) { function
         * call = (function)iterCalls.next(); if (call.src != null)
         * retList.addLast("\t" + call.src.defType + "_" + call.src.name + "_" +
         * call.des.defType + "_" + call.des.name + "_" + call.name + "\n");
         * else retList.addLast("\t" + call.des.defType + "_" + call.des.name +
         * "_" + call.name + "\n"); }
         * retList.addLast("--------------------------------------------------------------\n");
         * retList.addLast("\n"); }
         */
        // retList.addLast(" *** function declarations ***/\n");
        // retList.addLast("/*** processes and medium list***\n");
        /*
         * iterator = entities.iterator(); while (iterator.hasNext()) { entity
         * tmp = (entity)iterator.next(); retList.addLast(tmp.type);
         * retList.addLast(" "); retList.addLast(tmp.name); retList.addLast("
         * "); retList.addLast(tmp.ident); retList.addLast("\n"); }
         */
        // retList.addLast(" *** processes and medium list***/\n\n");
        // retList.addLast("/*** connections list***\n");
        /*
         * iterator = connections.iterator(); while (iterator.hasNext()) {
         * connection tmp = (connection)iterator.next();
         * retList.addLast(tmp.src); retList.addLast(" ");
         * retList.addLast(tmp.port); retList.addLast(" ");
         * retList.addLast(tmp.des); retList.addLast(" ");
         * retList.addLast(tmp.inter); retList.addLast(" ");
         * retList.addLast(tmp.srcDefType); retList.addLast(" ");
         * retList.addLast(tmp.desDefType); retList.addLast("\n"); }
         */
        // retList.addLast(" *** connections list***/\n\n");
        Enumeration keys = parameterList.keys();

        // retList.addLast("/*** Parameter List ***\n");
        /*
         * while (enum.hasMoreElements()) { String key =
         * (String)enum.nextElement(); iterator =
         * ((List)parameterList.get(key)).iterator(); retList.addLast(key + " --
         * "); while (iterator.hasNext()) {
         * retList.addLast(((ParameterNode)iterator.next()).getName().getIdent());
         * retList.addLast("\t"); } retList.addLast("\n"); }
         * retList.addLast("\n");
         */

        // search parameter list, resolve parameter names and replace all the %%
        // symbols
        String retString = _stringListToString(retList);
        retList.clear();

        keys = parameterList.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            int index = 1;
            iterator = ((List) parameterList.get(key)).iterator();
            while (iterator.hasNext()) {
                String paramName = ((ParameterNode) iterator.next()).getName()
                        .getIdent();
                String src = "arg" + key + "_%%" + (index++);
                String des = "arg" + key + "_" + paramName;
                int begin;
                // retList.addLast(src + " -> " + des + " (First index of
                // replace " + retString.indexOf(src) + ")\n");

                // replaceAll is not compatible with before java 1.4
                // retString = retString.replaceAll(src,des);

                while ((begin = retString.indexOf(src)) != -1) {
                    StringBuffer buf = new StringBuffer(retString);
                    buf.replace(begin, begin + src.length(), des);
                    retString = buf.toString();
                }
            }
        }

        retString = retString.replace('$', '_');

        // retList.addLast(" *** Parameter List ***/\n");
        retList.addFirst(retString);

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

        // skip the template declarations
        if (node.getParTypeNames() != null && !node.getParTypeNames().isEmpty()) {
            return retList;
        }

        // non-functional

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

        // skip the template declarations
        if (node.getParTypeNames() != null && !node.getParTypeNames().isEmpty()) {
            return retList;
        }

        inter thisInterface = new inter();

        // visit name node
        String interName = node.getName().getIdent();
        thisInterface.name = new String(interName);

        // retList.addLast("/*** Interface *** \n");
        // retList.addLast(interName);

        // visit interfaces
        List retValue = TNLManip.traverseList(this, args, node.getInterfaces());
        if (!retValue.isEmpty()) {
            // retList.addLast(" extends ");
            // retList.addLast(_commaList(retValue));
            // retList.addLast(" ");
        }

        // retList.addLast("\n");

        // visit members
        args.set(0, new String("Interface"));
        args.set(1, new String(interName));
        thisInterface.members = node.getMembers();
        interfaces.addLast(thisInterface);
        retValue = TNLManip.traverseList(this, args, node.getMembers());

        // retValue is the original code that can be output for debug
        // retList.addLast(retValue);
        // retList.addLast(" *** Interface ***/\n\n");

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
        String type = args.get(0).toString();

        if (type.equals("Interface")) {
            String name = node.getName().getIdent();
            String interName = args.get(1).toString();
            String methodName = interName + "_" + name;

            retList.addLast(Modifier.toString(node.getModifiers()));
            retList.addLast(Effect.toString(node.getEffect()));
            retList.addLast(name);
            retList.addLast(" ");
            retList.addLast(node.getName().accept(this, args));
            retList.addLast("(");
            LinkedList par = _commaList(TNLManip.traverseList(this, args, node
                    .getParams()));
            retList.addLast(par);
            retList.addLast(")\n");

            parameterList.put(methodName, node.getParams());
        } else if (type.equals("Process")) {
            Iterator iterEntity = entities.iterator();
            while (iterEntity.hasNext()) {
                entity en = (entity) iterEntity.next();
                if (en.defType.equals(((ProcessDeclNode) node.getParent())
                        .getName().getIdent())) {
                    String methodName = en.defType + "_" + en.name + "_"
                            + node.getName().getIdent();
                    args.set(2, new String(en.name));
                    args.set(3, new String(node.getName().getIdent()));
                    ((BlockScope) args.get(5)).level = 1;

                    bodyList = new LinkedList();
                    methodList = new LinkedList();

                    function func = new function(en, node.getName().getIdent());
                    functions.addLast(func);

                    // localVarCounter = 0;
                    awaitFlagCounter = 0;

                    if (node.getName().getIdent().equals("thread")) {

                        headList.addLast("bool\tsync" + methodName + ";\n");
                        initRunList.addLast(indent(indentLevel));
                        initRunList.addLast("run " + methodName + "();\n");

                        continueCounter = 0;
                        exitPoint = "main_exit%local%";
                        methodList.addLast("\nproctype " + methodName + "()\n");
                        if (node.getBody() == AbsentTreeNode.instance)
                            methodList.addLast("{};\n");
                        else {
                            methodList.addLast("{\n");
                            methodIndent += indentOnce;
                            bodyIndent += indentOnce;

                            // methodList.addLast(indent(methodIndent));
                            // methodList.addLast("bool syncReturn =
                            // NONBLOCK;\n");
                            // methodList.addLast(indent(methodIndent));
                            // methodList.addLast("bool sync" + methodName +
                            // ";\n");

                            methodList.addLast(indent(methodIndent));
                            methodList
                                    .addLast("bool function_return%local%;\n");
                            bodyList.addLast(indent(bodyIndent));
                            bodyList.addLast("sync" + methodName
                                    + " = NONBLOCK;\n");
                            bodyList.addLast(indent(bodyIndent));
                            bodyList
                                    .addLast("function_return%local% = false;\n");
                            methodList.addLast(node.getBody()
                                    .accept(this, args));

                            bodyList.addLast("\nmain_exit%local%:\n");
                            bodyList.addLast(indent(bodyIndent));
                            bodyList.addLast("skip;\n");
                            bodyIndent -= indentOnce;
                            methodList.addLast("\n");
                            methodList.addLast(bodyList);
                            methodIndent -= indentOnce;
                            methodList.addLast("};\n");

                            // threadList.addLast(methodList);
                        }
                    } else {
                        headList.addLast("bool\tsync" + methodName + ";\n");

                        // return value
                        String returnType = _stringListToString((List) node
                                .getReturnType().accept(this, args));
                        if (!returnType.equals("void")) {
                            headList.addLast(returnType);
                            headList.addLast("\tr" + methodName + ";\n");
                        }

                        // parameters
                        parameterList.put(methodName, node.getParams());
                        Iterator iterParam = node.getParams().iterator();
                        int paramCount = 0;
                        while (iterParam.hasNext()) {
                            ParameterNode param = (ParameterNode) iterParam
                                    .next();
                            if (param.getDefType() instanceof ArrayTypeNode) {
                                String paramName = param.getName().getIdent();
                                if (!argRename.containsKey(paramName))
                                    argRename
                                            .put(
                                                    paramName,
                                                    new String(
                                                            "param_array%"
                                                                    + Integer
                                                                            .toString(paramCount)));
                            } else {
                                String paramType = _stringListToString((List) param
                                        .getDefType().accept(this, args));
                                String paramName = param.getName().getIdent();
                                if (!paramType.equals("void")) {
                                    headList.addLast(paramType + "\t");
                                    headList.addLast("arg" + methodName + "_"
                                            + paramName + ";\n");
                                    if (!argRename.containsKey(paramName)) {
                                        argRename.put(paramName, new String(
                                                "parameter"));
                                        // argRename.put(paramName,new
                                        // String("argument%" +
                                        // Integer.toString(paramCount)));
                                    }
                                }
                            }
                        }

                        continueCounter = 0;
                        exitPoint = "main_exit%local%";

                        // method body
                        methodList.addLast("\ninline " + methodName
                                + "%local%()\n");
                        methodList.addLast("{\n");
                        methodIndent += indentOnce;
                        bodyIndent += indentOnce;

                        // methodList.addLast(indent(methodIndent));
                        // methodList.addLast("bool syncReturn = NONBLOCK;\n");
                        // methodList.addLast(indent(methodIndent));
                        // methodList.addLast("bool isBlockable;\n");

                        methodList.addLast(indent(methodIndent));
                        methodList.addLast("bool function_return%local%;\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("function_return%local% = false;\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("sync" + args.get(1).toString() + "_"
                                + args.get(2).toString()
                                + "_thread = NONBLOCK;\n");
                        // bodyList.addLast("\nend:\n");
                        // bodyList.addLast(indent(bodyIndent));
                        // bodyList.addLast("do\n");
                        // bodyList.addLast(indent(bodyIndent));
                        // bodyList.addLast("::s" + methodName +
                        // "?isBlockable;\n\n");

                        if (node.getBody() != AbsentTreeNode.instance)
                            methodList.addLast(node.getBody()
                                    .accept(this, args));

                        bodyList.addLast("\nmain_exit%local%:\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("skip;\n");

                        // bodyList.addLast(indent(bodyIndent));
                        // bodyList.addLast("s" + methodName +
                        // "!syncReturn;\n");
                        // bodyList.addLast(indent(bodyIndent));
                        // bodyList.addLast("od;\n");

                        methodList.addLast(bodyList);
                        methodIndent -= indentOnce;
                        bodyIndent -= indentOnce;
                        methodList.addLast("};\n");

                        // mainList.addLast(methodList);
                    }
                    func.decl = _stringListToString(methodList);
                    headList.addLast("\n");
                }
            }
            argRename.clear();
            ((BlockScope) args.get(5)).level = 0;
        } else if (type.equals("Medium")) {
            Iterator iterator = interfaces.iterator();
            String interName = new String();
            boolean found = false;

            while (iterator.hasNext() && !found) {
                inter in = (inter) iterator.next();
                Iterator iterMember = in.members.iterator();
                while (iterMember.hasNext()) {
                    String methodName = ((MethodDeclNode) iterMember.next())
                            .getName().getIdent();
                    if (methodName.equals(node.getName().getIdent())) {
                        interName = in.name;
                        found = true;
                        break;
                    }
                }
            }
            // the found interface name should also be compared
            // to the interfaces this medium implements

            iterator = entities.iterator();
            while (iterator.hasNext()) {
                entity en = (entity) iterator.next();
                if (!en.defType.equals(args.get(1)))
                    continue;

                String instanceName = en.name;
                Iterator iterConn = connections.iterator();
                while (iterConn.hasNext()) {
                    connection conn = (connection) iterConn.next();
                    if (!conn.des.equals(instanceName))
                        continue;

                    String srcName = conn.src;
                    String srcDefType = conn.srcDefType;
                    String methodName = srcDefType + "_" + srcName + "_"
                            + args.get(1) + "_" + instanceName + "_"
                            + node.getName().getIdent();
                    continueCounter = 0;
                    exitPoint = "main_exit%local%";
                    args.set(2, new String(instanceName));
                    args.set(3, new String(node.getName().getIdent()));
                    args.set(4, srcDefType + "_" + srcName);
                    ((BlockScope) args.get(5)).level = 1;

                    bodyList = new LinkedList();
                    methodList = new LinkedList();

                    Iterator iterSrc = entities.iterator();
                    entity src = null;
                    while (iterSrc.hasNext()) {
                        src = (entity) iterSrc.next();
                        if (src.name.equals(srcName)
                                && src.defType.equals(srcDefType))
                            break;
                    }

                    function func = new function(src, en, node.getName()
                            .getIdent());
                    functions.addLast(func);

                    // localVarCounter = 0;
                    awaitFlagCounter = 0;

                    headList.addLast("bool\tsync" + methodName + ";\n");

                    // return value
                    String returnType = _stringListToString((List) node
                            .getReturnType().accept(this, args));
                    if (!returnType.equals("void")) {
                        headList.addLast(returnType);
                        headList.addLast("\trtn" + methodName + ";\n");
                    }

                    // parameters
                    parameterList.put(methodName, node.getParams());
                    Iterator iterParam = node.getParams().iterator();
                    int paramCount = 0;
                    while (iterParam.hasNext()) {
                        ParameterNode param = (ParameterNode) iterParam.next();
                        if (param.getDefType() instanceof ArrayTypeNode) {
                            String paramName = param.getName().getIdent();
                            if (!argRename.containsKey(paramName))
                                argRename
                                        .put(
                                                paramName,
                                                new String(
                                                        "param_array%"
                                                                + Integer
                                                                        .toString(paramCount)));
                        } else {
                            String paramType = _stringListToString((List) param
                                    .getDefType().accept(this, args));
                            if (!paramType.equals("void")) {
                                String paramName = param.getName().getIdent();
                                headList.addLast(paramType + "\t");
                                headList.addLast("arg" + methodName + "_");
                                headList.addLast(paramName + ";\n");
                                if (!argRename.containsKey(paramName)) {
                                    argRename.put(paramName, new String(
                                            "parameter"));
                                    // argRename.put(paramName, new
                                    // String("argument%" +
                                    // Integer.toString(paramCount)));
                                }
                            }
                        }
                        paramCount++;
                    }

                    // method declaration
                    methodList
                            .addLast("\ninline " + methodName + "%local%()\n");
                    methodList.addLast("{\n");

                    methodIndent += indentOnce;
                    bodyIndent += indentOnce;

                    // methodList.addLast(indent(methodIndent));
                    // methodList.addLast("bool isBlockable;\n");
                    // methodList.addLast(indent(methodIndent));
                    // methodList.addLast("bool syncReturn = NONBLOCK;\n");

                    methodList.addLast(indent(methodIndent));
                    methodList.addLast("bool function_return%local%; \n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("function_return%local% = false;\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("sync" + args.get(4).toString()
                            + "_thread = NONBLOCK;\n");

                    // bodyList.addLast("\nend:\n");
                    // bodyList.addLast(indent(bodyIndent));
                    // bodyList.addLast("do\n");
                    // bodyList.addLast(indent(bodyIndent));
                    // bodyList.addLast("::s" + methodName + "?isBlockable;\n");

                    if (interName.equals(""))
                        bodyList.addLast("\n");
                    else {
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("if\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("::(sync" + methodName
                                + " == BLOCKABLE) -> ");
                        bodyList.addLast("atomic{(" + args.get(1).toString()
                                + "_" + instanceName + "_" + interName);
                        bodyList.addLast("_state[1] == 0 ) -> ");
                        bodyList.addLast(args.get(1).toString() + "_"
                                + instanceName + "_" + interName);
                        bodyList.addLast("_state[0]++;}\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("::else -> atomic{\n");
                        bodyIndent += indentOnce;
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("if\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("::(" + args.get(1).toString() + "_"
                                + instanceName + "_" + interName);
                        bodyList.addLast("_state[1] == 0 ) -> ");
                        bodyList.addLast(args.get(1).toString() + "_"
                                + instanceName + "_" + interName);
                        bodyList.addLast("_state[0]++;\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("::else -> sync"
                                + args.get(4).toString()
                                + "_thread = BLOCK; goto quit%local%;\n");
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("fi;}\n");
                        bodyIndent -= indentOnce;
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("fi;\n\n");

                        // bodyList.addLast(indent(bodyIndent));
                        // bodyList.addLast("atomic{(" + args.get(1).toString()
                        // + "_" + instanceName + "_" + interName);
                        // bodyList.addLast("_state[1] == 0 ) -> ");
                        // bodyList.addLast(args.get(1).toString() + "_" +
                        // instanceName + "_" + interName);
                        // bodyList.addLast("_state[0]++;}\n");
                    }

                    if (node.getBody() != AbsentTreeNode.instance)
                        methodList.addLast(node.getBody().accept(this, args));

                    bodyList.addLast("\nmain_exit%local%:\n");

                    // bodyList.addLast(indent(bodyIndent));
                    // bodyList.addLast("s" + methodName + "!syncReturn;\n");

                    if (!interName.equals("")) {
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast(args.get(1).toString() + "_"
                                + instanceName + "_" + interName);
                        bodyList.addLast("_state[0]--;\n");
                    } else {
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast("skip;\n");
                    }

                    bodyList.addLast("quit%local%:\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("skip;\n");

                    // bodyList.addLast(indent(bodyIndent));
                    // bodyList.addLast("od;\n");

                    methodList.addLast(bodyList);
                    methodIndent -= indentOnce;
                    bodyIndent -= indentOnce;
                    methodList.addLast("};\n");

                    func.decl = _stringListToString(methodList);
                    headList.addLast("\n");
                }
            }
            argRename.clear();
            ((BlockScope) args.get(5)).level = 0;
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

        if (args.get(0).equals("Netlist")) {
            methodList = new LinkedList();
            retList.addLast(node.getBody().accept(this, args));
        } else {

            Iterator iterEntity = entities.iterator();
            while (iterEntity.hasNext()) {
                entity en = (entity) iterEntity.next();
                if (!en.defType.equals(((NetworkNodeDeclNode) node.getParent())
                        .getName().getIdent()))
                    continue;

                String methodName = en.defType + "_" + en.name + "_"
                        + node.getName().getIdent();
                args.set(2, new String(en.name));
                args.set(3, new String(node.getName().getIdent()));
                ((BlockScope) args.get(5)).level = 1;

                bodyList = new LinkedList();
                methodList = new LinkedList();

                // function func = new function(en, node.getName().getIdent());
                // functions.addLast(func);

                // localVarCounter = 0;
                awaitFlagCounter = 0;
                continueCounter = 0;
                exitPoint = "main_exit%local%";

                // parameters
                int count = 0;
                methodList.addLast("\ninline " + methodName + "(");
                Iterator iterParam = node.getParams().iterator();
                while (iterParam.hasNext()) {
                    ParameterNode param = (ParameterNode) iterParam.next();
                    if (count > 1)
                        methodList.addLast(",");
                    if (count > 0)
                        methodList.addLast(param.getName().getIdent());
                    count++;
                }
                methodList.addLast(")\n");
                // method body

                methodList.addLast("{\n");
                methodIndent += indentOnce;
                bodyIndent += indentOnce;

                methodList.addLast(indent(methodIndent));
                methodList.addLast("bool function_return%local%;\n");

                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("function_return%local% = false;\n");

                methodList.addLast(node.getBody().accept(this, args));

                bodyList.addLast("\nmain_exit%local%:\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("skip;\n");

                methodList.addLast(bodyList);
                methodIndent -= indentOnce;
                bodyIndent -= indentOnce;
                methodList.addLast("}\n");

                // replace %local% with some suffix

                String methodString = _stringListToString(methodList);
                int begin;
                while ((begin = methodString.indexOf("%local%")) != -1) {
                    StringBuffer buf = new StringBuffer(methodString);
                    buf.replace(begin, begin + 7, "_" + methodName);
                    methodString = buf.toString();
                }

                inlineList.addLast(methodString);
                ((BlockScope) args.get(5)).level = 0;
            }
        }

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

        // non-functional
        // retList.addLast(indent(indentLevel));
        // retList.addLast("this(");
        // retList.addLast(_commaList(TNLManip.traverseList(this, args,
        // node.getArgs())));
        // retList.addLast(");\n");

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

        // non-functional
        // retList.addLast(_commaList
        // (TNLManip.traverseList(this, args, node.getArgs())));

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

        LinkedList retList = new LinkedList();
        String defType = _stringListToString((List) node.getDefType().accept(
                this, args));
        String name = _stringListToString((List) node.getName().accept(this,
                args));

        if (args.get(0).equals("Netlist")) {

            // netlist member variable is not supported yet

        } else if (args.get(0).equals("Process")
                || args.get(0).equals("Medium")) {
            String entityType = ((NetworkNodeDeclNode) node.getParent())
                    .getName().getIdent();
            Iterator iterator = entities.iterator();
            while (iterator.hasNext()) {
                entity en = (entity) iterator.next();
                if (entityType.equals(en.defType)) {
                    String var = "v" + entityType + "_" + en.name + "_" + name;
                    headList3.addLast(defType);
                    // headList3.addLast(" ");
                    // headList3.addLast(node.getDefType().getClass().getName());
                    headList3.addLast("\t" + var);
                    if (node.getDefType() instanceof ArrayTypeNode) {
                        if (node.getInitExpr() instanceof AllocateArrayNode
                                && ((AllocateArrayNode) node.getInitExpr())
                                        .getDimExprs().get(0) instanceof IntLitNode) {
                            headList3.addLast(""
                                    + ((IntLitNode) ((AllocateArrayNode) node
                                            .getInitExpr()).getDimExprs()
                                            .get(0)).accept(this, args) + "");
                        } else
                            headList3.addLast("[" + arrayDimension + "]");

                        // headList3.addLast(((AllocateArrayNode)node.getInitExpr()).getDimExprs().get(0).getClass().getName()
                        // + "\n");
                    } else if (node.getInitExpr() != AbsentTreeNode.instance) {
                        initList2.addLast(indent(indentLevel));
                        initList2.addLast(var);
                        initList2.addLast(" = ");
                        initList2
                                .addLast(node.getInitExpr().accept(this, args));
                        initList2.addLast(";\n");
                    }
                    headList3.addLast(";\n");

                    varRename.addLast(name, new BlockScope((BlockScope) args
                            .get(5)));
                    // initList.addLast("\n/*** test " + name + " renamed as " +
                    // rename.name + " ***/\n");
                }
            }
        }

        return retList;
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
        LinkedList retList = new LinkedList();
        String defType = _stringListToString((List) node.getDefType().accept(
                this, args));
        String name = _stringListToString((List) node.getName().accept(this,
                args));

        if (args.get(0).equals("Netlist")) {

            entity en = new entity();
            en.defType = defType; // _stringListToString((List)node.getDefType().accept(this,
            // args));
            en.name = name; // _stringListToString((List)node.getName().accept(this,
            // args));
            en.initExpr = (AllocateNode) node.getInitExpr();
            entities.addLast(en);

            if (node.getInitExpr() != AbsentTreeNode.instance) {
                AllocateNode initNode = (AllocateNode) node.getInitExpr();
                int count = 0;
                Iterator iterator = initNode.getArgs().iterator();
                initList.addLast(indent(indentLevel));
                initList.addLast(defType + "_" + name + "_");
                initList.addLast(initNode.getDtype().accept(this, args));
                initList.addLast("(");
                while (iterator.hasNext()) {
                    TreeNode tmpNode = (TreeNode) iterator.next();
                    if (count > 1)
                        initList.addLast(",");
                    if (count > 0)
                        initList.addLast(tmpNode.accept(this, args));
                    count++;
                }
                initList.addLast(");\n");
            }
            // retList.addLast("/*InitExpr: ");
            // retList.addLast(node.getInitExpr().getClass().getName());
            // retList.addLast(node.getInitExpr().accept(this, args));
            // retList.addLast("*/\n");

        } else {

            String var = name + "%local%";
            methodList.addLast(indent(methodIndent));
            methodList.addLast(defType + " " + var);
            // methodList.addLast(" ");
            // methodList.addLast(node.getDefType().getClass().getName());
            if (node.getDefType() instanceof ArrayTypeNode) {
                if (node.getInitExpr() instanceof AllocateArrayNode
                        && ((AllocateArrayNode) node.getInitExpr())
                                .getDimExprs().get(0) instanceof IntLitNode) {
                    methodList.addLast(""
                            + ((IntLitNode) ((AllocateArrayNode) node
                                    .getInitExpr()).getDimExprs().get(0))
                                    .accept(this, args) + "");
                } else
                    methodList.addLast("[" + arrayDimension + "]");

                // methodList.addLast(((AllocateArrayNode)node.getInitExpr()).getDimExprs().get(0).getClass().getName()
                // + "\n");
            } else if (node.getInitExpr() != AbsentTreeNode.instance) {
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast(var);
                bodyList.addLast(" = ");
                bodyList.addLast(node.getInitExpr().accept(this, args));
                bodyList.addLast(";\n");
            }
            methodList.addLast(";\n");
            varRename.addLast(name, new BlockScope((BlockScope) args.get(5)));
        }

        return retList;
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

        // non-functional
        // retList.addLast("{");
        // retList.addLast(_commaList(TNLManip.traverseList
        // (this, args, node.getInitializers())));
        // retList.addLast("}");

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

        ((BlockScope) args.get(5)).goIn();
        retList.addLast(TNLManip.traverseList(this, args, node.getStmts()));
        ((BlockScope) args.get(5)).goOut();

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

        // non-functional
        // retList.addLast(Modifier.toString(node.getModifiers()));
        // retList.addLast(node.getDefType().accept(this, args));
        // retList.addLast(" ");
        // retList.addLast(node.getName().accept(this, args));

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

        if (args.get(0).equals("Netlist")) {
            retList.addLast("/* ");
            retList.addLast(node.getMethod().accept(this, args));
            retList.addLast("(");
            retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                    .getArgs())));
            retList.addLast(")  ");
            retList.addLast("*/\n");
        } else if (args.get(0).equals("Process")) {
            Iterator iterArg = node.getArgs().iterator();

            if (node.getMethod() instanceof ObjectFieldAccessNode) {
                String port = _stringListToString((List) ((ObjectFieldAccessNode) node
                        .getMethod()).getObject().accept(this, args));
                String name = _stringListToString((List) ((ObjectFieldAccessNode) node
                        .getMethod()).getName().accept(this, args));
                String medInstance = null;
                String medName = null;
                String interName = null;

                Iterator iterator = connections.iterator();
                while (iterator.hasNext()) {
                    connection conn = (connection) iterator.next();
                    if (conn.src.equals(args.get(2)) && conn.port.equals(port)) {
                        medInstance = conn.des;
                        interName = conn.inter;
                        break;
                    }
                }

                iterator = entities.iterator();
                entity des = null;
                while (iterator.hasNext()) {
                    des = (entity) iterator.next();
                    if (des.name.equals(medInstance)) {
                        medName = des.defType;
                        break;
                    }
                }

                iterator = entities.iterator();
                entity src = null;
                while (iterator.hasNext()) {
                    src = (entity) iterator.next();
                    if (src.name.equals(args.get(2))) {
                        break;
                    }
                }

                function f = new function(src, des, name);
                ((function) functions.getLast()).functionCalls.addLast(f);

                // should resolve the arguments as in the same way
                String methodName = args.get(1).toString() + "_"
                        + args.get(2).toString() + "_" + medName + "_"
                        + medInstance + "_" + name;
                iterator = ((List) parameterList.get(interName + "_" + name))
                        .iterator();
                while (iterator.hasNext()) {
                    String arg = _stringListToString((List) ((TreeNode) iterArg
                            .next()).accept(this, args));
                    ParameterNode param = (ParameterNode) iterator.next();
                    String paramType = _stringListToString((List) param
                            .getDefType().accept(this, args));
                    if (paramType.equals("void"))
                        break;
                    f.args.addLast(arg);
                    if (param.getDefType() instanceof ArrayTypeNode)
                        continue;
                    // String paramName = param.getName().getIdent();
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("arg" + methodName + "_");
                    bodyList.addLast(param.getName().accept(this, args));
                    bodyList.addLast(" = ");
                    bodyList.addLast(arg + ";\n");
                }

                if (args.get(6) == null) {
                    bodyList.addLast(indent(bodyIndent));
                    if (args.get(3).equals("thread"))
                        bodyList.addLast("sync" + methodName
                                + " = BLOCKABLE;\n");
                    else
                        bodyList.addLast("sync" + methodName + " = sync"
                                + args.get(1).toString() + "_"
                                + args.get(2).toString() + "_"
                                + args.get(3).toString() + ";\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast(methodName + "%call%();\n");
                    retList.addLast("rtn" + methodName);
                } else {
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("sync" + methodName + " = UNBLOCKABLE;\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast(methodName + "%call%();\n");
                    retList.addLast("rtn" + methodName);
                }
            } else if (node.getMethod() instanceof ThisFieldAccessNode) {
                int index = 1;
                String name = ((ThisFieldAccessNode) node.getMethod())
                        .getName().getIdent();
                String methodName = args.get(1).toString() + "_"
                        + args.get(2).toString() + "_" + name;
                Iterator iterator = entities.iterator();
                entity des = null;

                while (iterator.hasNext()) {
                    des = (entity) iterator.next();
                    if (des.name.equals(args.get(2))) {
                        break;
                    }
                }

                function f = new function(des, name);
                ((function) functions.getLast()).functionCalls.addLast(f);

                while (iterArg.hasNext()) {
                    String arg = _stringListToString((List) ((TreeNode) iterArg
                            .next()).accept(this, args));
                    f.args.addLast(arg);
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("arg" + methodName + "_");
                    bodyList.addLast("%%" + (index++));
                    bodyList.addLast(" = ");
                    bodyList.addLast(arg + ";\n");
                }

                if (args.get(6) == null) {
                    bodyList.addLast(indent(bodyIndent));
                    if (args.get(3).equals("thread"))
                        bodyList.addLast("sync" + methodName
                                + " = BLOCKABLE;\n");
                    else
                        bodyList.addLast("sync" + methodName + " = sync"
                                + args.get(1).toString() + "_"
                                + args.get(2).toString() + "_"
                                + args.get(3).toString() + ";\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast(methodName + "%call%();\n");
                    retList.addLast("rtn" + methodName);
                } else {
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("sync" + methodName + " = UNBLOCKABLE;\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast(methodName + "%call%();\n");
                    retList.addLast("rtn" + methodName);
                }

            }
        } else if (args.get(0).equals("Medium")) {
            Iterator iterArg = node.getArgs().iterator();

            if (node.getMethod() instanceof ThisFieldAccessNode) {
                int index = 1;
                String name = ((ThisFieldAccessNode) node.getMethod())
                        .getName().getIdent();
                String methodName = args.get(4).toString() + "_"
                        + args.get(1).toString() + "_" + args.get(2).toString()
                        + "_" + name;

                Iterator iterator = entities.iterator();
                entity src = null;
                while (iterator.hasNext()) {
                    src = (entity) iterator.next();
                    if (args.get(4).equals(src.defType + "_" + src.name)) {
                        break;
                    }
                }

                iterator = entities.iterator();
                entity des = null;
                while (iterator.hasNext()) {
                    des = (entity) iterator.next();
                    if (des.name.equals(args.get(2))) {
                        break;
                    }
                }

                function f = new function(src, des, name);
                ((function) functions.getLast()).functionCalls.addLast(f);

                while (iterArg.hasNext()) {
                    String arg = _stringListToString((List) ((TreeNode) iterArg
                            .next()).accept(this, args));
                    f.args.addLast(arg);
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("arg" + methodName + "_");
                    bodyList.addLast("%%" + (index++));
                    bodyList.addLast(" = ");
                    bodyList.addLast(arg + ";\n");

                }

                if (args.get(6) == null) {
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("sync" + methodName + " = sync"
                            + args.get(4).toString() + "_"
                            + args.get(1).toString() + "_"
                            + args.get(2).toString() + "_"
                            + args.get(3).toString() + ";\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast(methodName + "%call%();\n");
                    retList.addLast("rtn" + methodName);
                } else {
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast("sync" + methodName + " = UNBLOCKABLE;\n");
                    bodyList.addLast(indent(bodyIndent));
                    bodyList.addLast(methodName + "%call%();\n");
                    retList.addLast("rtn" + methodName);
                }

            } else if (node.getMethod() instanceof ObjectFieldAccessNode) {

                // medium-to-medium connection is not supported yet
            }
        }

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
        List exprList = (List) node.getExpr().accept(this, args);
        if (exprList.size() > 0 && !(node.getExpr() instanceof MethodCallNode)) {
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast(exprList);
            bodyList.addLast(";");
            bodyList.addLast("\n\n");
        } else
            bodyList.addLast("\n");

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
        // non-functional
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
        LinkedList condition = (LinkedList) node.getCondition().accept(this,
                args);

        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("if\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("::(");
        bodyList.addLast(condition);
        bodyList.addLast(") ->\n");
        bodyIndent += 2;
        bodyList.addLast(thenPart.accept(this, args));
        bodyList.addLast(indent(bodyIndent - 2));
        bodyList.addLast("::else ->");

        if (elsePart != AbsentTreeNode.instance) {
            bodyList.addLast("\n");
            bodyList.addLast(elsePart.accept(this, args));
        } else {
            bodyList.addLast(" skip;\n");
        }
        bodyIndent -= 2;
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("fi;\n");

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
        // non-functional
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
        // non-functional
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

        // non-functional
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

        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("break;\n");
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

        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("goto continue_" + currentContinue + "%local%;\n");
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
        bodyList.addLast("\n");

        if (node.getForeStmt().classID() == EMPTYSTMTNODE_ID) {
            // while loop
            int lastContinue = currentContinue;
            currentContinue = continueCounter;
            continueCounter++;

            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("do\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::skip;\n");
            bodyIndent += 2;
            String test = _stringListToString((List) node.getTest().accept(
                    this, args));

            bodyList.addLast(indent(bodyIndent - 2));
            bodyList.addLast("continue_" + currentContinue + "%local%:\n");
            if (!test.equals("true")) {
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("if\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("::(" + test + ") -> skip;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("::else -> break;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("fi;\n");
            }
            bodyList.addLast(node.getAftStmt().accept(this, args));
            bodyIndent -= 2;
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("od;\n");
            currentContinue = lastContinue;

        } else {
            // do loop
            int lastContinue = currentContinue;
            currentContinue = continueCounter;
            continueCounter++;

            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("do\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::skip;\n");
            bodyIndent += 2;
            bodyList.addLast(node.getForeStmt().accept(this, args));
            String test = _stringListToString((List) node.getTest().accept(
                    this, args));
            bodyList.addLast(indent(bodyIndent - 2));
            bodyList.addLast("continue_" + currentContinue + "%local%:\n");
            if (!test.equals("true")) {
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("if\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("::(" + test + ") -> skip;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("::else -> break;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("fi;\n");
            }
            bodyIndent -= 2;
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("od;\n");
            currentContinue = lastContinue;
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

        // bodyList.addLast(indent(bodyIndent));
        // bodyList.addLast("{\n");
        int length = node.getInit().size();
        if (length > 0) {
            Iterator iterator = node.getInit().iterator();
            TreeNode firstNode = (TreeNode) node.getInit().get(0);
            if (firstNode.classID() == LOCALVARDECLNODE_ID) {
                // a list of local variables, with the same type and modifier
                LocalVarDeclNode varDeclNode = (LocalVarDeclNode) firstNode;
                while (iterator.hasNext()) {
                    LocalVarDeclNode declNode = (LocalVarDeclNode) iterator
                            .next();
                    // bodyList.addLast(indent(bodyIndent));
                    // bodyList.addLast(varDeclNode.getDefType().accept(this,
                    // args));
                    // bodyList.addLast(" ");
                    // bodyList.addLast(declNode.getName().getIdent());
                    // bodyList.addLast(";\n");

                    TreeNode initExpr = declNode.getInitExpr();
                    if (initExpr != AbsentTreeNode.instance) {
                        List init = (List) initExpr.accept(this, args);
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast(varDeclNode.getDefType().accept(this,
                                args));
                        bodyList.addLast(" ");
                        bodyList.addLast(declNode.getName().getIdent());
                        bodyList.addLast(" = ");
                        bodyList.addLast(init);
                        bodyList.addLast(";\n");
                    } else {
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast(varDeclNode.getDefType().accept(this,
                                args));
                        bodyList.addLast(" ");
                        bodyList.addLast(declNode.getName().getIdent());
                        bodyList.addLast(";\n");
                    }
                }
            } else {
                while (iterator.hasNext()) {
                    TreeNode expr = (TreeNode) iterator.next();
                    List retExpr = (List) expr.accept(this, args);
                    if (retExpr.size() > 0 && !(expr instanceof MethodCallNode)) {
                        bodyList.addLast(indent(bodyIndent));
                        bodyList.addLast(retExpr);
                        bodyList.addLast(";\n");
                    }
                    // bodyList.addLast(expr.getClass().getName());
                    // bodyList.addLast("\n");
                }
            }
        }

        int lastContinue = currentContinue;
        currentContinue = continueCounter;
        continueCounter++;

        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("do\n");
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("::skip;\n");
        bodyIndent += 2;

        String test = _stringListToString((List) node.getTest().accept(this,
                args));

        bodyList.addLast(indent(bodyIndent - 2));
        bodyList.addLast("continue_" + currentContinue + "%local%:\n");
        if (!test.equals("true")) {
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("if\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::(" + test + ") -> skip;\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("::else -> break;\n");
            bodyList.addLast(indent(bodyIndent));
            bodyList.addLast("fi;\n");
        }

        bodyList.addLast(node.getStmt().accept(this, args));

        Iterator iterator = node.getUpdate().iterator();
        while (iterator.hasNext()) {
            TreeNode update = (TreeNode) iterator.next();
            List retUpdate = (List) update.accept(this, args);
            if (retUpdate.size() > 0 && !(update instanceof MethodCallNode)) {
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast(retUpdate);
                bodyList.addLast(";\n");
            }
            // bodyList.addLast(update.getClass().getName());
            // bodyList.addLast("\n");
        }
        bodyIndent -= 2;
        bodyList.addLast(indent(bodyIndent));
        bodyList.addLast("od;\n\n");
        currentContinue = lastContinue;

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

        // retList.addLast(node.getExpr2().getClass().getName());
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
        LinkedList retList = new LinkedList();
        String name = node.getName().getIdent();
        String type = args.get(0).toString();

        // retList.addLast("@@@");
        // only arguments need to be checked for renaming

        BlockScope s;
        if ((s = varRename.find(name, (BlockScope) args.get(5))) != null) {
            if (s.level < 0) {
                retList.addLast("v" + args.get(1).toString() + "_"
                        + args.get(2).toString() + "_" + name);
            } else {
                retList.addLast(name + "%local%");
            }
        } else if (argRename.containsKey(name)) {
            if (!argRename.get(name).equals("parameter")) {
                retList.addLast(argRename.get(name));
            } else if (type.equals("Process")) {
                retList.addLast("arg" + args.get(1).toString() + "_"
                        + args.get(2).toString() + "_" + args.get(3).toString()
                        + "_" + name);
            } else if (type.equals("Medium")) {
                retList.addLast("arg" + args.get(4).toString() + "_"
                        + args.get(1).toString() + "_" + args.get(2).toString()
                        + "_" + args.get(3).toString() + "_" + name);
            }
        } else {
            retList.addLast(name);
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
        String type = args.get(0).toString();

        if (type.equals("Process")) {
            TreeNode expr = node.getExpr();
            if (expr instanceof AbsentTreeNode) {
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("function_return%local% = true;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("goto " + exitPoint + ";\n");
            } else {
                LinkedList tmpList = (LinkedList) expr.accept(this, args);
                bodyList.addLast(indent(bodyIndent));
                bodyList
                        .addLast("rtn" + args.get(1).toString() + "_"
                                + args.get(2).toString() + "_"
                                + args.get(3).toString());
                bodyList.addLast(" = ");
                bodyList.addLast(tmpList);
                bodyList.addLast(";\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("function_return%local% = true;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("goto " + exitPoint + ";\n");
            }
        } else if (type.equals("Medium")) {
            TreeNode expr = node.getExpr();
            if (expr instanceof AbsentTreeNode) {
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("function_return%local% = true;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("goto " + exitPoint + ";\n");
            } else {
                LinkedList tmpList = (LinkedList) expr.accept(this, args);
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("rtn" + args.get(4).toString() + "_"
                        + args.get(1).toString() + "_" + args.get(2).toString()
                        + "_" + args.get(3).toString());
                bodyList.addLast(" = ");
                bodyList.addLast(tmpList);
                bodyList.addLast(";\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("function_return%local% = true;\n");
                bodyList.addLast(indent(bodyIndent));
                bodyList.addLast("goto " + exitPoint + ";\n");
            }
        }

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

        // non-functional
        /*
         * LinkedList exprStringList = (LinkedList) node.getExpr().accept(this,
         * args);
         *
         * retList.addLast("("); retList.addLast(node.getDtype().accept(this,
         * args)); retList.addLast(") ");
         * retList.addLast(_parenExpr(node.getExpr(), exprStringList));
         */

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

        // non-functional
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

        // non-functional
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
        String name = _stringListToString((LinkedList) node.getArray().accept(
                this, args));
        String type = args.get(0).toString();

        BlockScope s;
        if ((s = varRename.find(name, (BlockScope) args.get(5))) != null) {
            if (s.level < 0) {
                retList.addLast("v" + args.get(1).toString() + "_"
                        + args.get(2).toString() + "_" + name);
            } else {
                retList.addLast(name + "%local%");
            }
        } else if (argRename.containsKey(name)) {
            if (type.equals("Process")) {
                retList.addLast("arg" + args.get(1).toString() + "_"
                        + args.get(2).toString() + "_" + args.get(3).toString()
                        + "_" + name);
            }

            else if (type.equals("Medium")) {
                retList.addLast("arg" + args.get(4).toString() + "_"
                        + args.get(1).toString() + "_" + args.get(2).toString()
                        + "_" + args.get(3).toString() + "_" + name);
            }
        } else {
            retList.addLast(name);
        }
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
        //String name = node.getName().getIdent().toString();

        if (node.getParent() instanceof MethodCallNode
                && args.get(0).equals("Process")) {

        }

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

        // non-functional
        // retList.addLast("super.");
        // retList.addLast(node.getName().accept(this, args));

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
        String type = args.get(0).toString();
        if (type.equals("Process")) {
            retList.addLast("v" + args.get(1).toString() + "_"
                    + args.get(2).toString() + "_");
            retList.addLast(node.getName().accept(this, args));
        }
        if (type.equals("Medium")) {
            retList.addLast("v" + args.get(1).toString() + "_"
                    + args.get(2).toString() + "_");
            retList.addLast(node.getName().accept(this, args));
        }
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

        // non-functional
        // retList.addLast(node.getFType().accept(this, args));
        // retList.addLast(".class");

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

        // non-functional
        // retList.addLast(node.getType().accept(this, args));
        // retList.addLast(".this");

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

        // non-functional
        // retList.addLast(node.getType().accept(this, args));
        // retList.addLast(".super");

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
        return TNLManip.addFirst("bool");
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
        // retList.addLast(" *");

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
        return _visitBinaryOpNode(node, args, ">>>");
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

        // bodyList.addLast(indent(bodyIndent));
        // bodyList.addLast();
        /*
         * LinkedList e1StringList = (LinkedList) node.getExpr1().accept(this,
         * args); LinkedList e2StringList = (LinkedList)
         * node.getExpr2().accept(this, args); LinkedList e3StringList =
         * (LinkedList) node.getExpr3().accept(this, args);
         *
         * e1StringList = _parenExpr(node.getExpr1(), e1StringList);
         * e2StringList = _parenExpr(node.getExpr2(), e2StringList);
         * e3StringList = _parenExpr(node.getExpr3(), e3StringList);
         *
         * retList.addLast(e1StringList); retList.addLast(" ? ");
         * retList.addLast(e2StringList); retList.addLast(" : ");
         * retList.addLast(e3StringList);
         */

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
        return _visitBinaryOpAssignNode(node, args, "<<=");
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
        return _visitBinaryOpAssignNode(node, args, ">>>=");
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
     * Separate items in the list with a semicolon.
     *
     * @param stringList
     *            a list in which items should be separated by a semicolon
     * @return a list of items separated by a semicolon
     */
    protected static LinkedList _semicolonList(List stringList) {
        return _separateList(stringList, "; ");
    }

    /**
     * Separate items in the list with the argument separator.
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
        String defType = _stringListToString((List) node.getDefType().accept(
                this, args));
        String name = _stringListToString((List) node.getName().accept(this,
                args));

        if (args.get(0).equals("Netlist")) {

            entity en = new entity();
            en.defType = defType; // _stringListToString((List)node.getDefType().accept(this,
            // args));
            en.name = name; // _stringListToString((List)node.getName().accept(this,
            // args));
            en.initExpr = (AllocateNode) node.getInitExpr();
            entities.addLast(en);

            if (node.getInitExpr() != AbsentTreeNode.instance) {
                AllocateNode initNode = (AllocateNode) node.getInitExpr();
                int count = 0;
                Iterator iterator = initNode.getArgs().iterator();
                initList.addLast(indent(indentLevel));
                initList.addLast(defType);
                initList.addLast("_");
                initList.addLast(name);
                initList.addLast("_");
                initList.addLast(initNode.getDtype().accept(this, args));
                initList.addLast("(");
                while (iterator.hasNext()) {
                    if (count > 1)
                        initList.addLast(",");
                    if (count > 0)
                        initList.addLast(((TreeNode) iterator.next()).accept(
                                this, args));
                }
                initList.addLast(");\n");
            }
            // retList.addLast("/*InitExpr: ");
            // retList.addLast(node.getInitExpr().getClass().getName());
            // retList.addLast(node.getInitExpr().accept(this, args));
            // retList.addLast("*/\n");

        }

        else if ((args.get(0).equals("Process") && node.getParent() instanceof ProcessDeclNode)
                || (args.get(0).equals("Medium") && node.getParent() instanceof MediumDeclNode)) {
            String entityType = ((NetworkNodeDeclNode) node.getParent())
                    .getName().getIdent();
            Iterator iterator = entities.iterator();
            while (iterator.hasNext()) {
                entity en = (entity) iterator.next();
                if (entityType.equals(en.defType)) {
                    String var = "v" + entityType + "_" + en.name + "_" + name;
                    headList3.addLast(defType);
                    headList3.addLast("\t" + var + ";\n");
                    if (node.getInitExpr() != AbsentTreeNode.instance) {
                        initList2.addLast(indent(indentLevel));
                        initList2.addLast(var);
                        initList2.addLast(" = ");
                        initList2
                                .addLast(node.getInitExpr().accept(this, args));
                        initList2.addLast(";\n");
                    }

                    // varRename.put(name, new String("member variable"));
                    // initList.addLast("\n/*** test " + name + " renamed as " +
                    // rename.name + " ***/\n");
                }
            }
        } else {
            String var = name + "%local%";
            retList.addLast(indent(methodIndent));
            retList.addLast(defType + " " + var + ";\n");
            if (node.getInitExpr() != AbsentTreeNode.instance) {
                retList.addLast(indent(bodyIndent));
                retList.addLast(var);
                retList.addLast(" = ");
                retList.addLast(node.getInitExpr().accept(this, args));
                retList.addLast(";\n");
            }

            // varRename.put(name, new String("local variable"));
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
     * Convert a list of strings to a string.
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
                // return new String("WRONG HERE");
            }
        }

        // System.out.println(sb);
        return sb.toString();
    }

    /**
     * Return the code represented by a for init statement.
     *
     * @param list
     *            a list of declarations or statement expressions for the init
     *            part of a for loop
     * @param args
     *            the arguments the get passed to accept()
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
            return _separateList(TNLManip.traverseList(this, args, list), ", ");
        }
    }

    /**
     * Return the code represented by a <code>SingleExprNode</code>.
     *
     * @param node
     *            a node that contains the expression
     * @param args
     *            the arguments the get passed to accept()
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
     *            the arguments the get passed to accept()
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
     *            a node that contains the expressions of the assignment
     * @param args
     *            the arguments the get passed to accept()
     * @param opString
     *            a string that represents the operation assignment
     * @return a list of strings that represents <code> EXPR1 OP= EXPR2 </code>
     */
    protected LinkedList _visitBinaryOpAssignNode(BinaryOpAssignNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        List e1StringList = (List) node.getExpr1().accept(this, args);
        List e2StringList = (List) node.getExpr2().accept(this, args);

        opString = opString.trim();
        retList.addLast(e1StringList);
        retList.addLast(" = ");
        retList.addLast(e1StringList);
        retList.addLast(opString.substring(0, opString.length() - 1));
        retList.addLast("(");
        retList.addLast(e2StringList);
        retList.addLast(")");

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

    /**
     * Return the code represented by an <code>QuantifiedActionNode</code>.
     *
     * @param node
     *            the node that is being visited
     * @param args
     *            the arguments the get passed to accept()
     * @param opString
     *            the string that represents the quantified action
     * @return a list of strings that represents
     *         <code> QUANTIFIER VARS: SUBFORM </code>
     */
    public Object _visitQuantifiedActionNode(QuantifiedActionNode node,
            LinkedList args, String opString) {
        LinkedList retList = new LinkedList();

        retList.addLast(opString);
        retList.addLast(" ");
        retList.addLast(_commaList(TNLManip.traverseList(this, args, node
                .getVars())));
        retList.addLast(": ");
        retList.addLast(node.getSubform().accept(this, args));

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

    // /////////////////////////////////////////////////////////////////
    // // private variables ////
    /** number of spaces for the current indent level */
    private int indentLevel = 0;

    private int methodIndent = 0;

    private int bodyIndent = 0;

    /** number of spaces for the previous indent level */
    // private int lastIndentLevel = 0;
    /** number of spaces for each indentation */
    private final int indentOnce = 4;

    /** port-portType pairs defined in process or medium */
    //private Hashtable ports;
    private Hashtable parameterList = new Hashtable();

    private variables varRename = new variables();

    // private Hashtable varRename = new Hashtable();
    private Hashtable argRename = new Hashtable();

    /** interface-interface declarations pairs defined * */
    private LinkedList interfaces = new LinkedList();

    private LinkedList entities = new LinkedList();

    private LinkedList connections = new LinkedList();

    private LinkedList functions = new LinkedList();

    private LinkedList headList = new LinkedList(); // functions

    private LinkedList headList2 = new LinkedList(); // semaphores

    private LinkedList headList3 = new LinkedList(); // global variables

    private LinkedList initList = new LinkedList(); // semaphore

    private LinkedList initList2 = new LinkedList(); // global variables

    private LinkedList initRunList = new LinkedList();

    private LinkedList threadList = new LinkedList();

    private LinkedList inlineList = new LinkedList();

    private LinkedList methodList = new LinkedList();

    private LinkedList bodyList = new LinkedList();

    /** counter used to name temporary local variables and await flags */
    // private int localVarCounter;
    private int awaitFlagCounter;

    private int continueCounter;

    private int currentContinue;

    /** for return statememts within await statements */
    private String exitPoint;

    private final String arrayDimension = new String("100");

    /** inline funciton * */
    int callLevel;

    int[] suffix;

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

    private void inlining(function func) {

        Iterator iterator = functions.iterator();
        function f = null;
        String methodName = null;
        while (iterator.hasNext()) {
            function tmp = (function) iterator.next();
            if (func.src == null) {
                if (func.des.equals(tmp.des) && func.name.equals(tmp.name)) {
                    f = tmp;
                    methodName = func.des.defType + "_" + func.des.name + "_"
                            + func.name;
                    break;
                }
            } else {
                if (func.src.equals(tmp.src) && func.des.equals(tmp.des)
                        && func.name.equals(tmp.name)) {
                    f = tmp;
                    methodName = func.src.defType + "_" + func.src.name + "_"
                            + func.des.defType + "_" + func.des.name + "_"
                            + func.name;
                    break;
                }
            }
        }

        if (f == null) {
            throw new NullPointerException("Unexpected function call:"
                    + func.name);
            // System.out.println(func.des.defType + "_" + func.des.name + "_" +
            // func.name + "\n");
        }

        String methodString = f.decl;
        String index = new String();
        StringBuffer buf;
        int begin;
        int i;

        for (i = 0; i <= callLevel; i++)
            index = index + "_" + Integer.toString(suffix[i]);

        while ((begin = methodString.indexOf("%local%")) != -1) {
            buf = new StringBuffer(methodString);
            buf.replace(begin, begin + 7, index);
            methodString = buf.toString();
        }

        iterator = f.functionCalls.iterator();
        callLevel++;
        suffix[callLevel] = 0;
        while (iterator.hasNext()) {
            begin = methodString.indexOf("%call%");
            buf = new StringBuffer(methodString);
            buf.replace(begin, begin + 6, index + "_"
                    + Integer.toString(suffix[callLevel]));
            methodString = buf.toString();

            function tmp = (function) iterator.next();
            inlining(tmp);
            suffix[callLevel]++;
        }

        if (parameterList.containsKey(methodName)) {
            Iterator paramIter = ((List) parameterList.get(methodName))
                    .iterator();
            Iterator argIter = func.args.iterator();
            int counter = 0;
            while (paramIter.hasNext()) {
                ParameterNode param = (ParameterNode) paramIter.next();
                String arg = (String) argIter.next();
                String replace = new String("param_array%"
                        + Integer.toString(counter));
                if (param.getDefType() instanceof ArrayTypeNode) {
                    while ((begin = methodString.indexOf(replace)) != -1) {
                        buf = new StringBuffer(methodString);
                        buf.replace(begin, begin + replace.length(), arg);
                        methodString = buf.toString();
                    }
                }
                counter++;
            }
        }

        while ((begin = methodString.indexOf("%local%")) != -1) {
            buf = new StringBuffer(methodString);
            buf.replace(begin, begin + 7, "");
            methodString = buf.toString();
        }

        inlineList.addLast(methodString);
        callLevel--;
    }
}
