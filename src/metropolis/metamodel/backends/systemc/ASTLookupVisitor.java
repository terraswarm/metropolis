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

import metropolis.metamodel.StringManip;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.frontend.PackageDecl;
import metropolis.metamodel.nodetypes.ActionLabelStmtNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.LabeledBlockNode;
import metropolis.metamodel.nodetypes.LabeledStmtNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.Iterator;
import java.util.LinkedList;

// ////////////////////////////////////////////////////////////////////////
// // ASTLookupVisitor
/**
 * Lookup the AST to find a particular event position and return the AST node
 * representing that event.
 *
 * @author Guang Yang
 * @version $Id: ASTLookupVisitor.java,v 1.21 2006/10/12 20:32:57 cxh Exp $
 */
public class ASTLookupVisitor extends TraverseStmtsVisitor {

    /**
     * Construct a default instance.
     */
    public ASTLookupVisitor() {
        super();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /*
     * label{@ statement; @} @param node the node being visited @param args
     * visitor arguments
     */
    public Object visitActionLabelStmtNode(ActionLabelStmtNode node,
            LinkedList args) {
        if (node.getLabel().getName().getIdent().equals(_eventName)) {
            _ast = node;
        }
        TNLManip.traverseList(this, args, node.getStmts());
        return null;
    }

    /*
     * Root node of an AST @param node the node being visited @param args
     * visitor arguments
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        String objFullName = (String) args.get(0);
        _objName = StringManip.partAfterLast(objFullName, '.');
        _pkgName = StringManip.partBeforeLast(objFullName, '.');
        if (_pkgName.equals(_objName)) {
            _pkgName = "";
        }
        if (_objName.indexOf("_$") > 0)
            _objName = _objName.substring(0, _objName.indexOf("_$"));

        _eventName = (String) args.get(1);

        PackageDecl pkg = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);
        if (!pkg.fullName().equals(_pkgName)) {
            return null;
        }

        Iterator objIter = node.getDefTypes().iterator();
        UserTypeDeclNode obj = null;
        while (objIter.hasNext() && obj == null) {
            TreeNode tnode = (TreeNode) objIter.next();
            if (!(tnode instanceof UserTypeDeclNode))
                continue;
            UserTypeDeclNode oneObj = (UserTypeDeclNode) tnode;
            if (oneObj.getName().getIdent().equals(_objName)) {
                obj = oneObj;
            }
        }
        if (obj == null)
            return null;

        if (obj instanceof ProcessDeclNode) {
            _ast = ((ProcessDeclNode) obj).getSuperClass();
        } else if (obj instanceof MediumDeclNode) {
            _ast = ((MediumDeclNode) obj).getSuperClass();
        } else {
            throw new RuntimeException("Object " + objFullName
                    + " is neither a process nor a medium. "
                    + "Only processes or media can be used to specify events.");
        }

        obj.accept(this, null);

        return _ast;
    }

    /*
     * block(label){ statements } @param node the node being visited @param args
     * visitor arguments
     */
    public Object visitLabeledBlockNode(LabeledBlockNode node, LinkedList args) {
        if (node.getLabel().getName().getIdent().equals(_eventName)) {
            _ast = node;
        }
        TNLManip.traverseList(this, args, node.getStmts());
        return null;
    }

    /*
     * label: statement; @param node the node being visited @param args visitor
     * arguments
     */
    public Object visitLabeledStmtNode(LabeledStmtNode node, LinkedList args) {
        if (node.getLabel().getName().getIdent().equals(_eventName)) {
            _ast = node;
        }
        node.getStmt().accept(this, args);
        return null;
    }

    /*
     * Visit a method declaration ast node @param node the node being visited
     * @param args visitor arguments
     */
    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        if (node.getName().getIdent().equals(_eventName)) {
            _ast = node;
        }
        node.getBody().accept(this, args);
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /* The AST node that represents the event */
    private TreeNode _ast = null;

    /* Event Name (interface function name or label name) */
    private String _eventName = null;

    /* Object Name */
    private String _objName = null;

    /* Package Name */
    private String _pkgName = null;
}
