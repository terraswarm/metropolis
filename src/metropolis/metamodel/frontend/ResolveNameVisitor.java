/* A visitor that resolves names of local variables; formal parameters;
 fields, ports and parameters accesses; method calls; and statement labels.
 Perform only resolutions that do not need the type of expressions (this will
 be done later).

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

import metropolis.metamodel.Decl;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.NullValue;
import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ActionFormulaNode;
import metropolis.metamodel.nodetypes.ActionLabelStmtNode;
import metropolis.metamodel.nodetypes.ActionNode;
import metropolis.metamodel.nodetypes.ArrayAccessNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.AwaitLockNode;
import metropolis.metamodel.nodetypes.AwaitStatementNode;
import metropolis.metamodel.nodetypes.BeginEventNode;
import metropolis.metamodel.nodetypes.BlockNode;
import metropolis.metamodel.nodetypes.BoundedLoopNode;
import metropolis.metamodel.nodetypes.BreakNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.ConnectNode;
import metropolis.metamodel.nodetypes.ConstraintBlockNode;
import metropolis.metamodel.nodetypes.ConstraintDeclNode;
import metropolis.metamodel.nodetypes.ConstructorCallNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.ContinueNode;
import metropolis.metamodel.nodetypes.ELOCConstraintDeclNode;
import metropolis.metamodel.nodetypes.EndEventNode;
import metropolis.metamodel.nodetypes.EqualVarsNode;
import metropolis.metamodel.nodetypes.EventTypeNode;
import metropolis.metamodel.nodetypes.ExistsActionNode;
import metropolis.metamodel.nodetypes.ExprNode;
import metropolis.metamodel.nodetypes.ForNode;
import metropolis.metamodel.nodetypes.ForallActionNode;
import metropolis.metamodel.nodetypes.FormulaNode;
import metropolis.metamodel.nodetypes.GetConnectionDestNode;
import metropolis.metamodel.nodetypes.GetNthPortNode;
import metropolis.metamodel.nodetypes.GetScopeNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.IterationNode;
import metropolis.metamodel.nodetypes.JumpStmtNode;
import metropolis.metamodel.nodetypes.LOCConstraintDeclNode;
import metropolis.metamodel.nodetypes.LTLConstraintDeclNode;
import metropolis.metamodel.nodetypes.LTLSynchNode;
import metropolis.metamodel.nodetypes.LabeledBlockNode;
import metropolis.metamodel.nodetypes.LabeledStmtNode;
import metropolis.metamodel.nodetypes.LocalLabelNode;
import metropolis.metamodel.nodetypes.LocalVarDeclNode;
import metropolis.metamodel.nodetypes.LoopNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodCallNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ObjectFieldAccessNode;
import metropolis.metamodel.nodetypes.ObjectNode;
import metropolis.metamodel.nodetypes.ObjectParamAccessNode;
import metropolis.metamodel.nodetypes.ObjectPortAccessNode;
import metropolis.metamodel.nodetypes.ParameterNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantifiedActionNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.RedirectConnectNode;
import metropolis.metamodel.nodetypes.RefineConnectNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.SetScopeNode;
import metropolis.metamodel.nodetypes.StatementNode;
import metropolis.metamodel.nodetypes.SuperFieldAccessNode;
import metropolis.metamodel.nodetypes.SuperParamAccessNode;
import metropolis.metamodel.nodetypes.SuperPortAccessNode;
import metropolis.metamodel.nodetypes.SwitchNode;
import metropolis.metamodel.nodetypes.ThisFieldAccessNode;
import metropolis.metamodel.nodetypes.ThisNode;
import metropolis.metamodel.nodetypes.ThisParamAccessNode;
import metropolis.metamodel.nodetypes.ThisPortAccessNode;
import metropolis.metamodel.nodetypes.TypeFieldAccessNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.nodetypes.VarInEventRefNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ResolveNameVisitor
/**
 * A visitor that performs name resolution of fields, labels, methods, ports,
 * etc. in expressions and statements. Each name will be labeled with the
 * declaration of the port/field/label/method. The declaration in methods might
 * be wrong, as the types of expressions are not available yet to select among
 * different signatures. Overloading resolution is done later, in
 * ResolveFieldsVisitor. Further resolution that needs types will also be
 * performed in ResolveFieldsVisitor.
 * <p>
 * Each node returns a subtree that should be used to replace the node. So, if a
 * node does not have to be replaced, it should return 'this'. ObjectNodes
 * should return the correct kind of node to be used instead, e.g.
 * ObjectPortAccessNode. The only ObjectNodes that remain in the tree will be
 * the accesses to local variables and parameters.
 * <p>
 * Each node in this visitor gets as a parameter a NameContext, used to resolve
 * the names in expressions and statements.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ResolveNameVisitor.java,v 1.41 2006/10/12 20:34:10 cxh Exp $
 */
public class ResolveNameVisitor extends ReplacementVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a new visitor that will traverse completely a compile unit,
     * creating declarations for labels and local variables and setting the
     * right declaration in each name used in the program. Set the traversal
     * method to TM_CUSTOM;
     */
    public ResolveNameVisitor() {
        super(TM_CUSTOM);
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    // Nodes that should be not be replaced and that don't contain
    // any children that is relevant to us

    public Object visitTypeNameNode(TypeNameNode node, LinkedList args) {
        return node;
    }

    public Object visitArrayTypeNode(ArrayTypeNode node, LinkedList args) {
        return node;
    }

    // Visit the classes in the compile unit

    /**
     * Visit a compile unit node of a meta-model program, resolving names in
     * expressions and statements.
     *
     * @param node
     *            The compile unit being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return This node.
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // Create a dummy context to resolve names
        NameContext ctx = new NameContext();

        // Traverse the classes passing the context as an argument
        LinkedList childArgs = TNLManip.addFirst(ctx);
        TNLManip.traverseList(this, childArgs, node.getDefTypes());

        return node;
    }

    /**
     * Visit a class, resolving names in expressions and statements.
     *
     * @param node
     *            The class declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a quantity, resolving names in expressions and statements.
     *
     * @param node
     *            The quantity declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a interface, resolving names in expressions and statements.
     *
     * @param node
     *            The interface declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a process, resolving names in expressions and statements.
     *
     * @param node
     *            The process declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a medium, resolving names in expressions and statements.
     *
     * @param node
     *            The medium declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a scheduler, resolving names in expressions and statements.
     *
     * @param node
     *            The scheduler declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a state medium, resolving names in expressions and statements.
     *
     * @param node
     *            The state medium declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    /**
     * Visit a netlist, resolving names in expressions and statements.
     *
     * @param node
     *            The netlist declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDeclNode(node, args);
    }

    // Relevant nodes to be visited inside object declarations:
    // - those that declare a new name
    // - those that use a name
    // - those that declare a new sub-scope

    /**
     * Visit a local variable declaration. Create a new variable declaration,
     * and check that this declaration does not shadow a formal parameter, or
     * that the name of the variable has not been used before. Add the variable
     * name to the scope of the current context. Then, visit the expression that
     * initializes the variable.
     *
     * @param node
     *            The variable declaration being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     * @exception RuntimeException
     *                if this declaration shadows a parameter or there is
     *                already a variable with that name.
     */
    public Object visitLocalVarDeclNode(LocalVarDeclNode node, LinkedList args) {
        // Get the current context
        NameContext ctx = (NameContext) args.get(0);
        Scope scope = ctx.scope;

        // Get the variable name
        NameNode name = node.getName();
        String varName = name.getIdent();

        // Check that the variable name has not been already
        // declared, and that the variable does not shadow a
        // parameter name.
        if (scope.lookup(varName, CG_FORMAL) != null) {
            _error(ctx, "local variable '" + varName + "' shadows a "
                    + "parameter of the method");
        } else if (scope.lookupLocal(varName, CG_LOCALVAR) != null) {
            _error(ctx, "local variable " + varName + " already declared");
        }
        LocalVarDecl decl = new LocalVarDecl(varName, node.getDefType(), node
                .getModifiers(), node);
        scope.add(decl);
        name.setProperty(DECL_KEY, decl);

        // Visit + replace the initialization of the variable
        // IMPORTANT: The Java Language Specificacion explictly says
        // that a variable name can be used INSIDE its initialization
        TreeNode init = node.getInitExpr();
        init = (TreeNode) init.accept(this, args);
        node.setInitExpr(init);

        return node;
    }

    /**
     * Visit a method declaration. Create a new context to be used to resolve
     * names inside this method, that has the current scope as a parent. Add the
     * declarations of the parameters of the method to the scope, and visit the
     * method body.
     *
     * @param node
     *            The declaration of the method being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        // Build inner context
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.encLoop = null;
        subCtx.breakTarget = null;
        MethodDecl method = (MethodDecl) MetaModelDecl.getDecl(node.getName());
        subCtx.currentMethod = method;

        // Build child scope + add parameters
        Scope newScope1 = new Scope(ctx.scope);
        Iterator iter = method.getParams().iterator();
        while (iter.hasNext()) {
            FormalParameterDecl par = (FormalParameterDecl) iter.next();
            newScope1.add(par);
        }
        subCtx.scope = new Scope(newScope1);

        // Save scope for this MethodDeclNode
        node.setProperty(SCOPE_KEY, newScope1);

        // Traverse the body of the method
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        TreeNode body = node.getBody();
        if (body != AbsentTreeNode.instance) {
            node.setBody((TreeNode) body.accept(this, childArgs));
        }

        return node;
    }

    /**
     * Visit a constructor declaration. Create a new context to be used to
     * resolve names inside this constructor, that has the current scope as a
     * parent. Add the declarations of the parameters of the constructor to the
     * scope, and visit the super-constructor call and the constructor body.
     *
     * @param node
     *            The declaration of the constructor being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        // Build inner context
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        subCtx.encLoop = null;
        subCtx.breakTarget = null;
        MethodDecl method = (MethodDecl) MetaModelDecl.getDecl(node.getName());
        subCtx.currentMethod = method;

        // Build child scope + add parameters
        Scope newScope1 = new Scope(ctx.scope);
        Iterator iter = method.getParams().iterator();
        while (iter.hasNext()) {
            FormalParameterDecl par = (FormalParameterDecl) iter.next();
            newScope1.add(par);
        }
        subCtx.scope = new Scope(newScope1);

        // Traverse the super-constructor call
        ConstructorCallNode call = node.getConstructorCall();
        call = (ConstructorCallNode) call.accept(this, childArgs);
        node.setConstructorCall(call);

        // Traverse the body of the constructor
        BlockNode body = node.getBody();
        body = (BlockNode) body.accept(this, childArgs);
        node.setBody(body);

        return node;
    }

    /**
     * Visit a block of code. Create a new context with a scope that has current
     * scope as a parent. Traverse the list of statements in the block using the
     * new context.
     *
     * @param node
     *            The block of code being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitBlockNode(BlockNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = new Scope(ctx.scope);

        // Traverse statements of the block
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        List stmts = node.getStmts();
        stmts = TNLManip.traverseList(this, childArgs, stmts);
        node.setStmts(stmts);

        return node;
    }

    /**
     * Visit an await statement. Create a new context with a scope that has
     * current scope as a parent. Traverse the list of guards in the await using
     * the new context.
     *
     * @param node
     *            The await statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitAwaitStatementNode(AwaitStatementNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = new Scope(ctx.scope);

        // Traverse the guards of the await
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        List guards = node.getGuards();
        guards = TNLManip.traverseList(this, childArgs, guards);
        node.setGuards(guards);

        return node;
    }

    /**
     * Visit a block of constraints. Traverse the statements inside this class
     * using a new context, that has the scope of the current context as the
     * parent scope.
     *
     * @param node
     *            The block of constraints being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitConstraintBlockNode(ConstraintBlockNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = new Scope(ctx.scope);

        // Traverse statements of the block
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        List stmts = node.getStmts();
        stmts = TNLManip.traverseList(this, childArgs, stmts);
        node.setStmts(stmts);

        return node;
    }

    /**
     * Visit a labeled statement of code. Create a new context with a scope that
     * has current scope as a parent. Traverse the list of statements in the
     * block using the new context.
     *
     * @param node
     *            The labeled statement of code being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        Scope newScope = new Scope(ctx.scope);
        subCtx.scope = newScope;

        // Get the declaration of the label and add it to the scope
        NameNode name = node.getLabel().getName();
        StmtLblDecl d = (StmtLblDecl) name.getProperty(DECL_KEY);
        newScope.add(d);

        // Save scope for this LabeledStmtNode
        node.setProperty(SCOPE_KEY, newScope);

        // Traverse instructions of the block
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        List stmts = node.getStmts();
        stmts = TNLManip.traverseList(this, childArgs, stmts);
        node.setStmts(stmts);

        return node;
    }

    /**
     * Visit a labeled block of code. Create a new context with a scope that has
     * current scope as a parent. Traverse the list of statements in the block
     * using the new context.
     *
     * @param node
     *            The labeled block of code being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        Scope newScope = new Scope(ctx.scope);
        subCtx.scope = newScope;

        // Get the declaration of the label and add it to the scope
        NameNode name = node.getLabel().getName();
        StmtLblDecl d = (StmtLblDecl) name.getProperty(DECL_KEY);
        newScope.add(d);

        // Save scope for this LabeledStmtNode
        node.setProperty(SCOPE_KEY, newScope);

        // Traverse instructions of the block
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        List stmts = node.getStmts();
        stmts = TNLManip.traverseList(this, childArgs, stmts);
        node.setStmts(stmts);

        return node;
    }

    /**
     * Visit a labeled statement. Create a new context to be used inside this
     * labeled statement, that has the label in the scope.
     *
     * @param node
     *            The labeled statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        Scope newScope = new Scope(ctx.scope);
        subCtx.scope = newScope;

        // Get the declaration of the label and add it to the scope
        NameNode name = node.getLabel().getName();
        StmtLblDecl d = (StmtLblDecl) name.getProperty(DECL_KEY);
        newScope.add(d);

        // Save scope for this LabeledStmtNode
        node.setProperty(SCOPE_KEY, newScope);

        // Traverse the statement
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        StatementNode stmt = node.getStmt();
        stmt = (StatementNode) stmt.accept(this, childArgs);
        node.setStmt(stmt);

        return node;
    }

    /**
     * Visit a switch statement node. Traverse the statement with the context
     * received as a parameter. Traverse the statements inside the list of
     * statements, registering that this is the target of all break statements
     * used inside.
     *
     * @param node
     *            The switch statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSwitchNode(SwitchNode node, LinkedList args) {
        // Traverse the expression in the 'switch'
        ExprNode expr = node.getExpr();
        expr = (ExprNode) expr.accept(this, args);
        node.setExpr(expr);

        // Traverse the list of blocks
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.breakTarget = node;
        subCtx.scope = new Scope(ctx.scope);
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        List blocks = node.getSwitchBlocks();
        blocks = TNLManip.traverseList(this, childArgs, blocks);
        node.setSwitchBlocks(blocks);

        return node;
    }

    /**
     * Visit a loop node (while-do or do-while). Traverse the test of the loop,
     * and then traverse the statements in the loop with a new context.
     *
     * @param node
     *            The loop being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitLoopNode(LoopNode node, LinkedList args) {
        // Traverse the test of the loop
        ExprNode test = node.getTest();
        test = (ExprNode) test.accept(this, args);
        node.setTest(test);

        // Traverse the statements of the loop using a new context
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.breakTarget = node;
        subCtx.encLoop = node;
        subCtx.scope = new Scope(ctx.scope);
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        TreeNode stmt = node.getForeStmt();
        stmt = (TreeNode) stmt.accept(this, childArgs);
        node.setForeStmt(stmt);

        stmt = node.getAftStmt();
        stmt = (TreeNode) stmt.accept(this, childArgs);
        node.setAftStmt(stmt);

        return node;
    }

    /**
     * Visit a for loop. Traverse the init, test and update expressions using a
     * new context; traverse the statements in the loop using that same new
     * context.
     *
     * @param node
     *            The for loop being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitForNode(ForNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = new Scope(ctx.scope);
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Traverse the init expression
        List init = node.getInit();
        init = TNLManip.traverseList(this, childArgs, init);
        node.setInit(init);

        subCtx.breakTarget = node;
        subCtx.encLoop = node;

        // Traverse the test expression
        ExprNode test = node.getTest();
        test = (ExprNode) test.accept(this, childArgs);
        node.setTest(test);

        // Traverse the update expression
        List update = node.getUpdate();
        update = TNLManip.traverseList(this, childArgs, update);
        node.setUpdate(update);

        // Traverse statement of the loop
        StatementNode stmt = node.getStmt();
        stmt = (StatementNode) stmt.accept(this, childArgs);
        node.setStmt(stmt);

        return node;
    }

    /**
     * Visit a bounded loop node. Check that the variable in the bounded loop
     * has been declared, and set its declaration. Traverse the inner statements
     * with a new context.
     *
     * @param node
     *            The bounded loop being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitBoundedLoopNode(BoundedLoopNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        // Get the declaration of the iterator of the loop
        NameNode var = node.getVar();
        String ident = var.getIdent();
        Scope scope = ctx.scope;
        int category = CG_LOCALVAR | CG_FORMAL | CG_FIELD;
        Decl decl = scope.lookup(ident, category);
        if (decl == null) {
            _error(ctx, "iterator '" + ident + "' of bounded loop undeclared");
        }
        var.setProperty(DECL_KEY, decl);

        // Traverse the statement with a new context
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.scope = new Scope(ctx.scope);
        subCtx.encLoop = node;
        subCtx.breakTarget = node;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        StatementNode stmt = node.getStmt();
        stmt = (StatementNode) stmt.accept(this, childArgs);
        node.setStmt(stmt);

        return node;
    }

    /**
     * Visit a break node. Check that, if the break is unlabeled, we are inside
     * a switch statement or a loop. If the break is labeled, set the
     * declaration of the label. In any case, set the destination of the jump in
     * the property JUMP_DESTINATION_KEY.
     *
     * @param node
     *            The break node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return node The node that is passed in as an argument.
     */
    public Object visitBreakNode(BreakNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        if ((node.getLabel() == AbsentTreeNode.instance)
                && (ctx.breakTarget == null)) {
            _error(ctx, "unlabeled 'break' statement is only allowed in "
                    + "loops or switch statements");
        }
        _resolveJump(node, ctx);
        return node;
    }

    /**
     * Visit a continue node. Check that, if the continue statement is
     * unlabeled, we are inside a loop. If the continue is labeled, set the
     * declaration of the label. In any case, set the destination of the jump in
     * the property JUMP_DESTINATION_KEY.
     *
     * @param node
     *            The break node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return node The node that is passed in as an argument.
     */
    public Object visitContinueNode(ContinueNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        if (ctx.encLoop == null) {
            _error(ctx, "'continue' statement used outside a loop");
        }
        _resolveJump(node, ctx);
        if (node.hasProperty(JUMP_DESTINATION_KEY)) {
            StatementNode dest = (StatementNode) node
                    .getDefinedProperty(JUMP_DESTINATION_KEY);
            if (!(dest instanceof IterationNode)) {
                LocalLabelNode label = (LocalLabelNode) node.getLabel();
                String ident = label.getName().getIdent();
                _error(ctx, "target label '" + ident + "' in continue "
                        + "statement does not refer to a loop");
            }
        }
        return node;
    }

    /**
     * Visit a this node. Label the node with the type declared in current
     * class.
     *
     * @param node
     *            The 'this' node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitThisNode(ThisNode node, LinkedList args) {
        return _visitThisRefNode(node, args);
    }

    /**
     * Visit an access to a field of the current object. Label the node with the
     * type declared in the current class.
     *
     * @param node
     *            The 'this.field' node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitThisFieldAccessNode(ThisFieldAccessNode node,
            LinkedList args) {
        return _visitThisRefNode(node, args);
    }

    /**
     * Visit an access to a port of the current object. Label the node with the
     * type declared in the current class.
     *
     * @param node
     *            The 'this.port' node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitThisPortAccessNode(ThisPortAccessNode node,
            LinkedList args) {
        return _visitThisRefNode(node, args);
    }

    /**
     * Visit an access to a parameter of the current object. Label the node with
     * the type declared in the current class.
     *
     * @param node
     *            The 'this.param' node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitThisParamAccessNode(ThisParamAccessNode node,
            LinkedList args) {
        return _visitThisRefNode(node, args);
    }

    /**
     * Visit an acces to a field of the superclass. Label this node with the
     * type declared in the superclass of current class.
     *
     * @param node
     *            The field of 'super' being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSuperFieldAccessNode(SuperFieldAccessNode node,
            LinkedList args) {
        return _visitSuperRefNode(node, args);
    }

    /**
     * Visit an acces to a port of the superclass. Label this node with the type
     * declared in the superclass of current class.
     *
     * @param node
     *            The port of 'super' being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSuperPortAccessNode(SuperPortAccessNode node,
            LinkedList args) {
        return _visitSuperRefNode(node, args);
    }

    /**
     * Visit an acces to a parameter of the superclass. Label this node with the
     * type declared in the superclass of current class.
     *
     * @param node
     *            The parameter of 'super' being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSuperParamAccessNode(SuperParamAccessNode node,
            LinkedList args) {
        return _visitSuperRefNode(node, args);
    }

    /**
     * Visit an node which contains a name of an object; look for the
     * declaration of the given object and set the property. Re
     *
     * @param node
     *            The name of an object being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return The correct kind of node (ObjectFieldAccessNode,
     *         ObjectPortAccessNode, ...) that will replace this node.
     * @exception RuntimeException
     *                if the name is not declared.
     */
    public Object visitObjectNode(ObjectNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameNode name = node.getName();

        if (ctx.resolveAsMethod) {
            TreeNode retVal = _resolveMethodName(ctx, name);
            return retVal;
        } else {
            TreeNode retVal = _resolveName(ctx, name, CG_FIELD | CG_PARAMETER
                    | CG_PORT | CG_LOCALVAR | CG_FORMAL);
            return retVal;
        }
    }

    /**
     * Visit an access to a field of an object. Record that from this moment we
     * are not looking for more names of methods.
     *
     * @param node
     *            The access to a field that is being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitObjectFieldAccessNode(ObjectFieldAccessNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.resolveAsMethod = false;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Visit the object whose field is accessed
        TreeNode object = node.getObject();
        object = (TreeNode) object.accept(this, childArgs);
        node.setObject(object);

        return node;
    }

    /**
     * Visit an access to a port of an object. Record that from this moment we
     * are not looking for more names of methods.
     *
     * @param node
     *            The access to a port that is being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitObjectPortAccessNode(ObjectPortAccessNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.resolveAsMethod = false;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Visit the object whose field is accessed
        TreeNode object = node.getObject();
        object = (TreeNode) object.accept(this, childArgs);
        node.setObject(object);

        return node;
    }

    /**
     * Visit an access to a parameterof an object. Record that from this moment
     * we are not looking for more names of methods.
     *
     * @param node
     *            The access to a parameter that is being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitObjectParamAccessNode(ObjectParamAccessNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.resolveAsMethod = false;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Visit the object whose field is accessed
        TreeNode object = node.getObject();
        object = (TreeNode) object.accept(this, childArgs);
        node.setObject(object);

        return node;
    }

    /**
     * Visit a method call node. Record that the name that next name that is
     * going to be resolved will be resolved as a method. Store this information
     * in the context, and resolve the name of the object using this context.
     *
     * @param node
     *            The method call being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitMethodCallNode(MethodCallNode node, LinkedList args) {
        // Traverse the arguments of the call
        List callArgs = node.getArgs();
        callArgs = TNLManip.traverseList(this, args, callArgs);
        node.setArgs(callArgs);

        // Build new context registering that we expect a method name
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.resolveAsMethod = true;
        LinkedList childArgs = TNLManip.addFirst(subCtx);

        // Traverse the method name
        ExprNode expr = node.getMethod();
        expr = (ExprNode) expr.accept(this, childArgs);
        node.setMethod(expr);

        return node;
    }

    /**
     * Visit a lock inside an await statement. Set the declaration of the port
     * declared in the lock (if a port is used) in its property DECL_KEY. If the
     * lock reference 'this' instead, check that current object is a medium.
     *
     * @param node
     *            The await lock being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitAwaitLockNode(AwaitLockNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        TreeNode lockNode = node.getNode();

        // Check the port declared in the lock
        if (lockNode instanceof ThisNode) {
            if ((ctx.currentObject.category != CG_MEDIUM)
                    && (ctx.currentObject.category != CG_SM)) {
                _error(ctx, "'this' used in the lock of an await statement,"
                        + " although current object is not a medium");
            }
            // Visit the lockNode
            lockNode = (TreeNode) lockNode.accept(this, args);
            node.setNode(lockNode);
        } else if (lockNode instanceof ObjectNode) {
            NameNode name = ((ObjectNode) lockNode).getName();
            String ident = name.getIdent();
            Decl decl = ctx.scope.lookup(ident, CG_PORT);
            if (decl == null) {
                _error(ctx, "port '" + ident + "' used in the lock of an await"
                        + " statement is undeclared");
            }
            name.setProperty(DECL_KEY, decl);
            // Replace the ObjectNode by a ThisPortAccessNode
            ThisPortAccessNode port = new ThisPortAccessNode(name);
            node.setNode(port);
        }

        return node;
    }

    /**
     * Visit a forall action node. Check that the variables used in the
     * quantifier are declared, and set their DECL_KEY property. Traverse the
     * subformula in the quantifier.
     *
     * @param node
     *            The forall actions node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     * @exception RuntimeException
     *                if any of the quantified variables is not declared.
     */
    public Object visitForallActionNode(ForallActionNode node, LinkedList args) {
        return _visitQuantifiedActionNode(node, args);
    }

    /**
     * Visit a exists action node. Check that the variables used in the
     * quantifier are declared, and set their DECL_KEY property. Traverse the
     * subformula in the quantifier.
     *
     * @param node
     *            The exists actions node being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     * @exception RuntimeException
     *                if any of the quantified variables is not declared.
     */
    public Object visitExistsActionNode(ExistsActionNode node, LinkedList args) {
        return _visitQuantifiedActionNode(node, args);
    }

    /**
     * Visit a connect node. Check the names in the source object and
     * destination object and set their DECL_KEY. If the port is defined as a
     * single name, or an array access with a simple name, this name refers to a
     * port of the source in the source object. In this case, the name will be
     * resolved by ResolveFieldVisitor, because the type of the source object is
     * needed.
     *
     * @param node
     *            The connect statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitConnectNode(ConnectNode node, LinkedList args) {
        // Traverse the source object
        ExprNode src = node.getSrcObject();
        src = (ExprNode) src.accept(this, args);
        node.setSrcObject(src);

        // Traverse the destination objects
        ExprNode dst = node.getDstObject();
        dst = (ExprNode) dst.accept(this, args);
        node.setDstObject(dst);

        // Check if the port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        ExprNode port = node.getPort();
        try {
            // port is a reference pointed by local variables
            port = (ExprNode) port.accept(this, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(true));
        } catch (RuntimeException e) {
            // port is defined in src
            port = _resolvePortAccess(port, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(false));
        }
        node.setPort(port);

        return node;
    }

    /**
     * Visit a getscope node. Check the names in the node object and set
     * itsDECL_KEY. If the port is defined as a single name, or an array access
     * with a simple name, this name refers to a port of the node in the node
     * object. In this case, the name will be resolved by ResolveFieldVisitor,
     * because the type of the node object is needed.
     *
     * @param node
     *            The getscope statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitGetScopeNode(GetScopeNode node, LinkedList args) {
        // Traverse the source object
        ExprNode src = node.getNode();
        src = (ExprNode) src.accept(this, args);
        node.setNode(src);

        // Check if the port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        ExprNode port = node.getPort();
        try {
            // port is a reference pointed by local variables
            port = (ExprNode) port.accept(this, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(true));
        } catch (RuntimeException e) {
            // port is defined in src
            port = _resolvePortAccess(port, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(false));
        }
        node.setPort(port);

        return node;
    }

    /**
     * Visit a set scope node. Check the names in the node and netlist, and set
     * their DECL_KEY. If the port is defined as a single name, or an array
     * access with a simple name, this name refers to a port of the source in
     * the node. In this case, the name will be resolved by ResolveFieldVisitor,
     * because the type of the node is needed.
     *
     * @param node
     *            The set scope statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitSetScopeNode(SetScopeNode node, LinkedList args) {
        // Traverse the node
        ExprNode srcNode = node.getNode();
        srcNode = (ExprNode) srcNode.accept(this, args);
        node.setNode(srcNode);

        // Traverse the netlist
        ExprNode netlist = node.getNetlist();
        netlist = (ExprNode) netlist.accept(this, args);
        node.setNetlist(netlist);

        // Check if the port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        ExprNode port = node.getPort();
        node.setPort(_resolvePortAccess(port, args));

        return node;
    }

    /**
     * Visit a refine connection node. Check the names in the netlist, source
     * object and component object, and set their DECL_KEY. If the port is
     * defined as a single name, or an array access with a simple name, this
     * name refers to a port of the source in the node. In this case, the name
     * will be resolved by ResolveFieldVisitor, because the type of the node is
     * needed.
     *
     * @param node
     *            The refine connect statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitRefineConnectNode(RefineConnectNode node, LinkedList args) {

        // Traverse the netlist
        ExprNode netlist = node.getNetlist();
        netlist = (ExprNode) netlist.accept(this, args);
        node.setNetlist(netlist);

        // Traverse the source object
        ExprNode srcObject = node.getSrcObject();
        srcObject = (ExprNode) srcObject.accept(this, args);
        node.setSrcObject(srcObject);

        // Traverse the component object
        ExprNode component = node.getComponent();
        component = (ExprNode) component.accept(this, args);
        node.setComponent(component);

        // Check if the port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        ExprNode port = node.getPort();
        try {
            // port is a reference pointed by local variables
            port = (ExprNode) port.accept(this, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(true));
        } catch (RuntimeException e) {
            // port is defined in src
            port = _resolvePortAccess(port, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(false));
        }
        node.setPort(port);

        return node;
    }

    /**
     * Visit a redirect connection node. Check the names in the netlist, source
     * object and component object, and set their DECL_KEY. If the ports are
     * defined as a single name, or an array access with a simple name, this
     * name refers to a port of the source in the node. In this case, the name
     * will be resolved by ResolveFieldVisitor, because the type of the node is
     * needed.
     *
     * @param node
     *            The redirect connect statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitRedirectConnectNode(RedirectConnectNode node,
            LinkedList args) {

        // Traverse the netlist
        ExprNode netlist = node.getNetlist();
        netlist = (ExprNode) netlist.accept(this, args);
        node.setNetlist(netlist);

        // Traverse the source object
        ExprNode srcObject = node.getSrcObject();
        srcObject = (ExprNode) srcObject.accept(this, args);
        node.setSrcObject(srcObject);

        // Traverse the component object
        ExprNode component = node.getComponent();
        component = (ExprNode) component.accept(this, args);
        node.setComponent(component);

        // Check if the source port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        ExprNode port = node.getSrcPort();
        try {
            // port is a reference pointed by local variables
            port = (ExprNode) port.accept(this, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(true));
        } catch (RuntimeException e) {
            // port is defined in src
            port = _resolvePortAccess(port, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(false));
        }
        node.setSrcPort(port);

        // Check if the new port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        port = node.getNewPort();
        try {
            // port is a reference pointed by local variables
            port = (ExprNode) port.accept(this, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(true));
        } catch (RuntimeException e) {
            // port is defined in src
            port = _resolvePortAccess(port, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(false));
        }
        node.setNewPort(port);

        return node;
    }

    /**
     * Visit a getconnectiondest node. Check the names in the source object and
     * set their DECL_KEY. If the port is defined as a single name, or an array
     * access with a simple name, this name refers to a port of the source in
     * the source object. In this case, the name will be resolved by
     * ResolveFieldVisitor, because the type of the source object is needed.
     *
     * @param node
     *            The getconnectiondest statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitGetConnectionDestNode(GetConnectionDestNode node,
            LinkedList args) {
        // Traverse the source object
        ExprNode src = node.getNode();
        src = (ExprNode) src.accept(this, args);
        node.setNode(src);

        // Check if the port has to be traversed.
        // If the port is defined as a simple name, it will not be traversed.
        // Traversal will be performed in ResolveFieldName.
        ExprNode port = node.getPort();
        try {
            // port is a reference pointed by local variables
            port = (ExprNode) port.accept(this, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(true));
        } catch (RuntimeException e) {
            // port is defined in src
            port = _resolvePortAccess(port, args);
            port.setProperty(LOCALPORTREF_KEY, new Boolean(false));
        }
        node.setPort(port);

        return node;
    }

    /**
     * Visit a getnthport node. Check the names in the source object and set
     * their DECL_KEY. Check the index of the port.
     *
     * @param node
     *            The getnthport statement being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitGetNthPortNode(GetNthPortNode node, LinkedList args) {
        // Traverse the source object
        ExprNode src = node.getNode();
        src = (ExprNode) src.accept(this, args);
        node.setNode(src);

        ExprNode num = node.getNum();
        num = (ExprNode) num.accept(this, args);
        node.setNum(num);

        return node;
    }

    /**
     * Visit a loc constraint formula declaration. Create a new context to be
     * used to resolve names inside this method, that has the current scope as a
     * parent. Add the declarations of the parameters of the formula to the
     * scope, and visit the formula body.
     *
     * @param node
     *            The declaration of the method being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitLOCConstraintDeclNode(LOCConstraintDeclNode node,
            LinkedList args) {
        return _visitConstraintDeclNode(node, args);
    }

    /**
     * Visit a eloc constraint formula declaration. Create a new context to be
     * used to resolve names inside this method, that has the current scope as a
     * parent. Add the declarations of the parameters of the formula to the
     * scope, and visit the formula body.
     *
     * @param node
     *            The declaration of the method being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitELOCConstraintDeclNode(ELOCConstraintDeclNode node,
            LinkedList args) {
        return _visitConstraintDeclNode(node, args);
    }

    /**
     * Visit a ltl constraint formula declaration. Create a new context to be
     * used to resolve names inside this method, that has the current scope as a
     * parent. Add the declarations of the parameters of the formula to the
     * scope, and visit the formula body.
     *
     * @param node
     *            The declaration of the method being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitLTLConstraintDeclNode(LTLConstraintDeclNode node,
            LinkedList args) {
        return _visitConstraintDeclNode(node, args);
    }

    /**
     * Visit a ltl synch node.
     *
     * @param node
     *            The declaration of the synch being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitLTLSynchNode(LTLSynchNode node, LinkedList args) {
        List events = node.getEvents();
        events = TNLManip.traverseList(this, args, events);
        node.setEvents(events);

        List eqvars = node.getEqualVars();
        eqvars = TNLManip.traverseList(this, args, eqvars);
        node.setEqualVars(eqvars);

        return node;
    }

    /**
     * Visit a EqualVarsNode.
     *
     * @param node
     *            The declaration of the equal variables.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitEqualVarsNode(EqualVarsNode node, LinkedList args) {
        ExprNode var = node.getVar1();
        var = (ExprNode) var.accept(this, args);
        node.setVar1(var);

        var = node.getVar2();
        var = (ExprNode) var.accept(this, args);
        node.setVar2(var);

        return node;
    }

    /**
     * Visit a VarInEventRefNode.
     *
     * @param node
     * The declaration of the variable @ event.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitVarInEventRefNode(VarInEventRefNode node, LinkedList args) {
        // The variable part can not be easily resolved
        // because it is in the scope of event,
        // which is not available at compile time.

        ExprNode event = node.getEvent();
        event = (ExprNode) event.accept(this, args);
        node.setEvent(event);

        /**
         * check index expression. ExprNode index = node.getIndex(); boolean
         * correctindex = true; if (!(index instanceof ObjectNode)) correctindex =
         * false; if (correctindex) if
         * (!((ObjectNode)index).getName().getIdent().equals("i")) correctindex =
         * false; if (!correctindex) throw new RuntimeException( "The event
         * occurence index in synch statement must be 'i'.");
         */

        return node;
    }

    /**
     * Visit a BeginEventNode.
     *
     * @param node
     *            The declaration of the beg()
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitBeginEventNode(BeginEventNode node, LinkedList args) {
        TreeNode proc = node.getProcess();
        proc = (TreeNode) proc.accept(this, args);
        node.setProcess(proc);

        ActionNode act = node.getAction();
        act = (ActionNode) act.accept(this, args);
        node.setAction(act);

        return node;
    }

    /**
     * Visit a EndEventNode.
     *
     * @param node
     *            The declaration of the end()
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitEndEventNode(EndEventNode node, LinkedList args) {
        TreeNode proc = node.getProcess();
        proc = (TreeNode) proc.accept(this, args);
        node.setProcess(proc);

        ActionNode act = node.getAction();
        act = (ActionNode) act.accept(this, args);
        node.setAction(act);

        return node;
    }

    /**
     * Visit a ActionNode.
     *
     * @param node
     *            The declaration of the action
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object visitActionNode(ActionNode node, LinkedList args) {
        TreeNode obj = node.getObject();
        obj = (TreeNode) obj.accept(this, args);
        node.setObject(obj);

        // name can not be easily resolved
        // because it is in the scope of object,

        return node;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    // We use the defaultVisit method defined in ReplacementJavaVisitor

    /**
     * Visit a user-defined type. Resolve the names in expressions and
     * statements using a naming context.
     *
     * @param node
     *            The user-defined type declaration being visited.
     * @param args
     *            List of arguments of the visit, with the naming context as the
     *            first argument.
     * @return This node.
     */
    public Object _visitUserTypeDeclNode(UserTypeDeclNode node, LinkedList args) {
        // Initialize the naming context to be used in this class
        NameContext ctx = new NameContext();
        ObjectDecl decl = (ObjectDecl) MetaModelDecl.getDecl(node.getName());
        ctx.scope = decl.getScope();
        ctx.currentObject = decl;

        // Traverse members of this class with this naming context
        // Members cannot be replaced by this visitor, so don't modify them
        LinkedList childArgs = TNLManip.addFirst(ctx);
        TNLManip.traverseList(this, childArgs, node.getMembers());

        return node;
    }

    /**
     * Visit a node that contains a reference to 'this' object. Set property
     * THIS_CLASS_KEY to refer to the type of this object.
     *
     * @param node
     *            The node being visited.
     * @param args
     *            List of arguments of the visit, with the naming context as the
     *            first argument.
     * @return This node.
     */
    public Object _visitThisRefNode(TreeNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        node.setProperty(THIS_CLASS_KEY, ctx.currentObject.getDefType());
        return node;
    }

    /**
     * Visit a node that contains a reference to 'super' object. Set property
     * THIS_CLASS_KEY to refer to the type of the superclass of current object.
     *
     * @param node
     *            The node being visited.
     * @param args
     *            List of arguments of the visit, with the naming context as the
     *            first argument.
     * @return This node.
     */
    public Object _visitSuperRefNode(TreeNode node, LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        ObjectDecl superClass = ctx.currentObject.getSuperClass();
        node.setProperty(THIS_CLASS_KEY, superClass.getDefType());
        return node;
    }

    /**
     * Visit quantified formula node. Check that the variables used in the
     * quantifier are declared, and set their DECL_KEY property. Traverse the
     * subformula in the quantifier.
     *
     * @param node
     *            The quantified formula being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     * @exception RuntimeException
     *                if any of the quantified variables is not declared.
     */
    public Object _visitQuantifiedActionNode(QuantifiedActionNode node,
            LinkedList args) {
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.encLoop = null;
        subCtx.breakTarget = null;
        subCtx.currentMethod = null;

        Scope newScope = new Scope(ctx.scope);
        HashSet names = new HashSet();

        Iterator iter = node.getVars().iterator();
        while (iter.hasNext()) {
            ParameterNode pnode = (ParameterNode) iter.next();

            // Create the parameter declaration
            NameNode namenode = pnode.getName();
            String ident = namenode.getIdent();
            int modifiers = pnode.getModifiers();
            TypeNode type = pnode.getDefType();

            // Check that the name of the parameter is unique
            if (names.contains(ident)) {
                throw new RuntimeException("Quantified parameter " + ident
                        + " redeclared");
            } else {
                FormalParameterDecl decl = new FormalParameterDecl(ident, type,
                        modifiers, pnode);
                names.add(ident);
                namenode.setProperty(DECL_KEY, decl);
                newScope.add(decl);
            }
        }
        subCtx.scope = new Scope(newScope);

        // Traverse the body of the method
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        TreeNode formula = node.getSubform();
        if (formula != AbsentTreeNode.instance) {
            node
                    .setSubform((ActionFormulaNode) formula.accept(this,
                            childArgs));
        }

        return node;

        /***********************************************************************
         * Old Implementation ******************* // Set declarations of
         * quantified variables Iterator vars = node.getVars().iterator(); while
         * (vars.hasNext()) { NameNode name = (NameNode)
         * ((ParameterNode)vars.next()).getName(); String ident =
         * name.getIdent(); Decl decl = ctx.scope.lookup(ident, CG_LOCALVAR); if
         * (decl == null) { _error(ctx,"variable '" + ident + "' used in
         * quantified" + " action formula is undeclared."); }
         * name.setProperty(DECL_KEY, decl); }
         *  // Traverse quantified formula ActionFormulaNode subform =
         * node.getSubform(); subform = (ActionFormulaNode) subform.accept(this,
         * args); node.setSubform(subform);
         *
         * return node;
         **********************************************************************/
    }

    /**
     * Visit a constraint formula (loc, eloc or ltl) declaration. Create a new
     * context to be used to resolve names inside this method, that has the
     * current scope as a parent. Add the declarations of the parameters of the
     * formula to the scope, and visit the formula body.
     *
     * @param node
     *            The declaration of the method being visited.
     * @param args
     *            List of arguments, with the context used to resolve names in
     *            the first position.
     * @return This node.
     */
    public Object _visitConstraintDeclNode(ConstraintDeclNode node,
            LinkedList args) {
        // Build inner context
        NameContext ctx = (NameContext) args.get(0);
        NameContext subCtx = (NameContext) ctx.clone();
        subCtx.encLoop = null;
        subCtx.breakTarget = null;
        MethodDecl method = (MethodDecl) MetaModelDecl.getDecl(node.getName());
        subCtx.currentMethod = method;

        // Build child scope + add parameters
        Scope newScope1 = new Scope(ctx.scope);
        Iterator iter = method.getParams().iterator();
        while (iter.hasNext()) {
            FormalParameterDecl par = (FormalParameterDecl) iter.next();
            newScope1.add(par);
        }
        subCtx.scope = new Scope(newScope1);

        // Traverse the body of the method
        LinkedList childArgs = TNLManip.addFirst(subCtx);
        TreeNode formula = node.getFormula();
        if (formula != AbsentTreeNode.instance) {
            node.setFormula((FormulaNode) formula.accept(this, childArgs));
        }

        return node;
    }

    /**
     * Find the target of a jump statement (break or continue) with a given
     * context. Set this target in the property JUMP_DESTINATION_KEY of the
     * node. If the jump has a label, set the declaration of the label.
     *
     * @param node
     *            Jump statement being resolved.
     * @param ctx
     *            Name context to be used to find the label.
     */
    public void _resolveJump(JumpStmtNode node, NameContext ctx) {
        TreeNode label = node.getLabel();
        TreeNode defTarget = ctx.breakTarget;
        if (label == AbsentTreeNode.instance) {
            // Set target of the jump
            node.setProperty(JUMP_DESTINATION_KEY, defTarget);
        } else {
            // Set declaration of the label
            NameNode labelName = ((LocalLabelNode) label).getName();
            String labelString = labelName.getIdent();
            StmtLblDecl dest = (StmtLblDecl) ctx.scope.lookup(labelString,
                    CG_STMTLABEL);
            if (dest == null) {
                _error(ctx, "label '" + labelString + "' not found");
            }
            labelName.setProperty(DECL_KEY, dest);
            // Set target of the jump
            TreeNode source = dest.getSource();
            TreeNode target;
            if (source instanceof LabeledStmtNode) {
                target = (TreeNode) ((LabeledStmtNode) source).getStmt();
            } else {
                LabeledBlockNode block = (LabeledBlockNode) source;
                List stmts = block.getStmts();
                target = (stmts.size() > 0 ? (TreeNode) stmts.get(0) : block);
            }
            node.setProperty(JUMP_DESTINATION_KEY, target);
        }
    }

    /**
     * Resolve a name of a method. Replace the ObjectNode with the correct kind
     * of Node. Don't set the DECL_KEY of the method yet, as we still have to
     * check overloading.
     *
     * @param ctx
     *            Context used to resolve names
     * @param name
     *            NameNode to be resolved
     * @return The subtree that should be used to replace the NameNode in the
     *         resolved tree.
     */
    protected TreeNode _resolveMethodName(NameContext ctx, NameNode name) {
        TreeNode retVal = null;
        ObjectDecl current = ctx.currentObject;
        Scope scope = ctx.scope;
        TreeNode qualifier = name.getQualifier();
        String ident = name.getIdent();
        if (qualifier == AbsentTreeNode.instance) {
            // Local name
            MethodDecl decl = (MethodDecl) scope.lookup(ident, CG_METHOD);
            if (decl == null)
                _error(ctx, "in this type there are no methods with "
                        + "name '" + ident + "'");
            retVal = new ThisFieldAccessNode(name);
            name.setProperty(DECL_KEY, decl);
            retVal.setProperty(THIS_CLASS_KEY, current.getDefType());
        } else {
            NameNode qualName = (NameNode) qualifier;
            int mask = CG_LOCALVAR | CG_FORMAL | CG_PORT | CG_FIELD
                    | CG_PARAMETER | CG_USERTYPE;
            qualifier = _resolveName(ctx, qualName, mask);
            MetaModelDecl container = MetaModelDecl.getDecl(qualifier);
            if (container instanceof ObjectDecl) {
                ObjectDecl object = (ObjectDecl) container;
                MethodDecl decl = (MethodDecl) object.getScope().lookup(ident,
                        CG_METHOD);
                if (decl == null)
                    _error(ctx, _policy.toString(object, true) + " has no "
                            + " static methods with name '" + ident + "'");
                TypeNameNode type = new TypeNameNode((NameNode) qualifier);
                name.setQualifier(AbsentTreeNode.instance);
                name.setProperty(DECL_KEY, decl);
                retVal = new TypeFieldAccessNode(name, type);
            } else if (container instanceof TypedDecl) {
                TypedDecl typed = (TypedDecl) container;
                TypeNode type = typed.getType();
                if (type instanceof ArrayTypeNode) {
                    if (!ident.equals("clone")) {
                        _error(ctx, "trying to invoke a method '" + ident
                                + "' of an array - arrays only have a "
                                + "'clone()' method");
                    }
                } else if (type instanceof TypeNameNode) {
                    TypeNameNode typeNam = (TypeNameNode) type;
                    MetaModelDecl object = (MetaModelDecl) MetaModelDecl
                            .getDecl(typeNam.getName());
                    if (!(object instanceof TypeParameterDecl)) { // xichen
                        MethodDecl decl = (MethodDecl) ((ObjectDecl) object)
                                .getScope().lookup(ident, CG_METHOD);
                        if (decl == null) {
                            _error(
                                    ctx,
                                    "trying to access method '"
                                            + ident
                                            + "' of variable '"
                                            + typed.getName()
                                            + "' of type '"
                                            + _policy.toString(type, true)
                                            + " - this class does not have a method with"
                                            + " that name");
                        }
                    }
                } else {
                    _error(ctx, "trying to invoke a method '" + ident
                            + "' of something not an object or class");
                }
                name.setQualifier(AbsentTreeNode.instance);
                retVal = new ObjectFieldAccessNode(name, qualifier);
            } else {
                _error(ctx, "Container of method '" + name + "'is not an "
                        + "object");
            }
        }
        return retVal;
    }

    /**
     * Resolve a name of an type, field or variable (i.e. this names cannot be a
     * method). Set the DECL_KEY of the name being resolved and return the node
     * that should be used to replace this name.
     *
     * @param ctx
     *            Context used to resolve types.
     * @param name
     *            NameNode to be resolved.
     * @param mask
     *            Set of of valid categories of this name.
     * @return The subtree that should be used to replace the NameNode in the
     *         resolved tree.
     */
    protected TreeNode _resolveName(NameContext ctx, NameNode name, int mask) {
        String ident = name.getIdent();
        ScopeIterator possibles = null;

        // Find the declaration of the name
        TreeNode qualifier = name.getQualifier();
        if (qualifier == AbsentTreeNode.instance) {
            possibles = ctx.scope.lookupFirst(ident, mask);
        } else {
            int newCategories = 0;

            // If this name can be a package, then the qualifier can
            // as well
            if ((mask & CG_PACKAGE) != 0) {
                newCategories |= CG_PACKAGE;
            }

            // Nodes cannot be inner classes, so if this name can be a
            // node, then the qualifier can also be a package
            if ((mask & CG_NODE) != 0) {
                newCategories |= CG_PACKAGE;
            }

            // If we are looking for a class or interface, then
            // we can be looking for other classes/interfaces,
            // a package or a node
            if ((mask & (CG_CLASS | CG_INTERFACE)) != 0) {
                newCategories |= CG_PACKAGE | CG_USERTYPE;
            }

            // If we are looking for a field, then the superclass
            // can be another object
            if ((mask & CG_FIELD) != 0) {
                newCategories |= CG_FIELD | CG_LOCALVAR | CG_FORMAL
                        | CG_PARAMETER | CG_CLASS;
            }

            NameNode qualName = (NameNode) qualifier;
            TreeNode resolved = _resolveName(ctx, qualName, newCategories);
            name.setQualifier(resolved);

            MetaModelDecl container = MetaModelDecl.getDecl(resolved);

            if (container.hasScope()) {
                if ((mask & CG_USERTYPE) != 0) {
                    Scope scope = container.getTypeScope();
                    possibles = scope.lookupFirstLocal(ident, mask);
                } else {
                    Scope scope = container.getScope();
                    possibles = scope.lookupFirstLocal(ident, mask);
                }
            } else if (container instanceof TypedDecl) {
                TypedDecl typed = (TypedDecl) container;
                TypeNode type = typed.getType();
                if (type instanceof ArrayTypeNode) {
                    Scope scope = MetaModelLibrary.ARRAY_CLASS_DECL.getScope();
                    possibles = scope.lookupFirstLocal(ident, mask
                            & (CG_FIELD | CG_METHOD));
                } else if (type instanceof TypeNameNode) {
                    TypeNameNode typeName = (TypeNameNode) type;
                    NameNode theName = typeName.getName();
                    ObjectDecl decl = (ObjectDecl) MetaModelDecl
                            .getDecl(theName);
                    Scope scope = decl.getScope();
                    possibles = scope.lookupFirstLocal(ident, mask
                            & (CG_FIELD | CG_METHOD | CG_PORT | CG_PARAMETER
                                    | CG_CLASS | CG_INTERFACE));
                } else if (type instanceof EventTypeNode) { // GY
                    name.setProperty(DECL_KEY, NullValue.instance);
                    return new ObjectNode(name);
                } else {
                    _error(ctx, "cannot select " + ident + " from a "
                            + "non-reference type represented by "
                            + _policy.toString(type, true));
                }
            }
        }

        // Check if the declaration has been found
        if (!possibles.hasNext()) {
            String msg = " of variable/parameter/field/port";
            if ((mask & CG_USERTYPE) != 0)
                msg = msg + "/class";
            if ((mask & CG_PACKAGE) != 0)
                msg = msg + "/package";
            _error(ctx, "name" + msg + " '" + ident + "' is undeclared");
        }

        MetaModelDecl decl = null;
        if ((mask & (CG_USERTYPE | CG_PACKAGE)) == 0) {
            decl = (MetaModelDecl) possibles.nonConflictingDecl();
            if (decl == null)
                _error(ctx, "ambiguous reference to '" + ident
                        + "', which is not the name of a type or package");
        } else {
            decl = (MetaModelDecl) possibles.peek();
            if (possibles.moreThanOne()
                    && ((decl.category & (CG_USERTYPE | CG_PACKAGE)) == 0)) {
                String msg = "of variable/parameter/field/port";
                _error(ctx, "ambiguous reference to name " + msg + " '" + ident
                        + "', which is not the name of a "
                        + "which is not the name of a type, package or method");
            }
        }

        name.setProperty(DECL_KEY, decl);

        switch (decl.category) {
        case CG_CLASS:
        case CG_INTERFACE:
        case CG_NETLIST:
        case CG_MEDIUM:
        case CG_PROCESS:
        case CG_SM:
        case CG_SCHEDULER:
            ObjectDecl objectDecl = (ObjectDecl) decl;
            if (!ctx.currentObject.hasAccess(objectDecl)) {
                _error(ctx, _policy.toString(objectDecl, true)
                        + " is visible here, but it cannot be accessed "
                        + " because it is non-public");
            }
            return name;
        case CG_FIELD:
            FieldDecl field = (FieldDecl) decl;
            if (!ctx.currentObject.hasAccess(field)) {
                _error(ctx, "field " + field.getName() + " of "
                        + _policy.toString(field.getContainer(), true)
                        + " is visible here, but it cannot be accessed");
            }
            TreeNode qual = name.getQualifier();
            if (qual == AbsentTreeNode.instance) {
                TreeNode res = new ThisFieldAccessNode(name);
                res.setProperty(THIS_CLASS_KEY, ctx.currentObject);
                return res;
            } else {
                name.setQualifier(AbsentTreeNode.instance);
                MetaModelDecl qualDecl = MetaModelDecl.getDecl(qual);
                if (qualDecl.category == CG_CLASS) {
                    TypeNameNode type = new TypeNameNode((NameNode) qual);
                    return new TypeFieldAccessNode(name, type);
                } else
                    return new ObjectFieldAccessNode(name, qual);
            }
        case CG_PORT:
            PortDecl port = (PortDecl) decl;
            if (!ctx.currentObject.hasAccess(port)) {
                _error(ctx, "port " + port.getName() + " of "
                        + _policy.toString(port.getContainer(), true)
                        + " is visible here, but it cannot be accessed");
            }
            qual = name.getQualifier();
            if (qual == AbsentTreeNode.instance) {
                TreeNode res = new ThisPortAccessNode(name);
                res.setProperty(THIS_CLASS_KEY, ctx.currentObject);
                return res;
            } else {
                name.setQualifier(AbsentTreeNode.instance);
                return new ObjectPortAccessNode(name, qual);
            }
        case CG_PARAMETER:
            ParameterDecl param = (ParameterDecl) decl;
            if (!ctx.currentObject.hasAccess(param)) {
                _error(ctx, "param " + param.getName() + " of "
                        + _policy.toString(param.getContainer(), true)
                        + " is visible here, but it cannot be accessed");
            }
            qual = name.getQualifier();
            if (qual == AbsentTreeNode.instance) {
                TreeNode res = new ThisParamAccessNode(name);
                res.setProperty(THIS_CLASS_KEY, ctx.currentObject);
                return res;
            } else {
                name.setQualifier(AbsentTreeNode.instance);
                return new ObjectParamAccessNode(name, qual);
            }
        case CG_LOCALVAR:
        case CG_FORMAL:
            return new ObjectNode(name);
        default:
            // Packages and others
            return name;
        }
    }

    /**
     * Resolve an access to a port of an object. If the access of the port is
     * made through an ObjectNode (e.g. simple name of the scalar port) or
     * through an array access (e.g. simple name of an array of ports), the name
     * should be resolved in the context of the type of the object being
     * accessed, e.g. in 'connect(a,p,b)', port 'p' should be resolved in the
     * context of the type of 'a'.
     *
     * @param expr
     *            Expression holding the port access.
     * @param args
     *            Arguments to be used to resolve this access.
     * @return The node that should be used to replace the original port access.
     */
    protected ExprNode _resolvePortAccess(ExprNode expr, LinkedList args) {
        boolean traverse;
        if (expr instanceof ObjectNode) {
            ObjectNode portName = (ObjectNode) expr;
            NameNode name = portName.getName();
            // If the name of the array is a simple name, it will be
            // resolved in ResolveFieldVisitor. Otherwise, resolve it here.
            traverse = (name.getQualifier() != AbsentTreeNode.instance);
            if (traverse)
                expr = (ExprNode) expr.accept(this, args);
        } else if (expr instanceof ArrayAccessNode) {
            ArrayAccessNode portAccess = (ArrayAccessNode) expr;
            // Resolve the index of the array
            ExprNode index = portAccess.getIndex();
            index = (ExprNode) index.accept(this, args);
            portAccess.setIndex(index);
            // If the name of the array is a simple name, it will be
            // resolved in ResolveFieldVisitor. Otherwise, resolve it here.
            ExprNode array = portAccess.getArray();
            traverse = true;
            if (array instanceof ObjectNode) {
                ObjectNode arrayName = (ObjectNode) array;
                NameNode name = arrayName.getName();
                traverse = (name.getQualifier() != AbsentTreeNode.instance);
            }
            if (traverse) {
                array = (ExprNode) array.accept(this, args);
                portAccess.setArray(array);
            }
        } else {
            expr = (ExprNode) expr.accept(this, args);
        }
        return expr;
    }

    /**
     * Emit an error message and produce a run-time exception.
     *
     * @param ctx
     *            Context where the error is found.
     * @param msg
     *            Error message.
     * @exception RuntimeException
     *                notifying the error.
     */
    protected void _error(NameContext ctx, String msg) {
        String errorMsg = "Error in ";
        if (ctx.currentMethod != null) {
            errorMsg = errorMsg + "method "
                    + _policy.toString(ctx.currentMethod, true) + " of ";
        }
        if (ctx.currentObject != null) {
            errorMsg += _policy.toString(ctx.currentObject, true) + ", ";
        }
        errorMsg = errorMsg + msg;
        throw new RuntimeException(errorMsg);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /** Type policy used to print error messages. */
    TypePolicy _policy = new TypePolicy();

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    // /////////////////////////////////////////////////////////////////
    // // inner classes ////

    /**
     * Class used to check names inside expressions and statements. This class
     * keeps track of scope information inside expressions and statements
     * (remember, we don't store such a information). The NameContext is not
     * stored after the analysis, i.e. we check names and when we finish using
     * the NameContext, we discard it.
     */
    protected static class NameContext implements Cloneable {

        /** Create a new NameContext. */
        public NameContext() {
        }

        /**
         * Create a shallow copy of a NameContext. Information inside the
         * NameContext is NOT cloned as well.
         */
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new InternalError("clone of NameContext not supported");
            }
        }

        /** The last scope in this context. */
        public Scope scope = null;

        /** The current method. */
        public MethodDecl currentMethod = null;

        /** The declaration of current object. */
        public ObjectDecl currentObject = null;

        /**
         * Node that will be reached if a <code> break </code> statement is
         * invoked in the current context.
         */
        public TreeNode breakTarget = null;

        /** The enclosing loop. null if this context is not in a loop. */
        public TreeNode encLoop = null;

        /**
         * Flag referring to the kind of name that will be resolved next. It is
         * false if we expect the name of an object (local variable, formal
         * parameter, field, port, etc.), true if we expect the name of a
         * method.
         */
        boolean resolveAsMethod = false;
    }

}
