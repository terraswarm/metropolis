/* A LinkedList to hold generated SystemC code, with optional #line directives

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
import metropolis.metamodel.NullValue;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AwaitGuardNode;
import metropolis.metamodel.nodetypes.BlockNode;
import metropolis.metamodel.nodetypes.EmptyStmtNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

// ////////////////////////////////////////////////////////////////////////
// // LineList
/**
 * A <code>LinkedList</code> that knows how to add "<code>#line &lt;line_number&gt; &lt;file_name&gt;</code>"
 * preprocessor directives to the generated code, taking that information from
 * the <code>TreeNode</code> that it is instantiated with.
 * <p>
 *
 * @author Daniele Gasperini, Allen Hopkins
 * @version $Id: LineList.java,v 1.37 2006/10/12 20:33:22 cxh Exp $
 */

public class LineList extends LinkedList implements
        MetaModelStaticSemanticConstants {

    // ////////////////////////////////////////////////////////////////////////
    // / Constructors

    /**
     * Create a non-debugging <code>LineList</code>.
     *
     * @param node
     *            The TreeNode whose generated source code this list will
     *            contain.
     */
    public LineList(TreeNode node) {
        this(node, false, false);
    }

    /**
     * Create a <code>LineList</code>, with control over whether it creates
     * code for metamodel debugging.
     * <p>
     * Setting the <code>emittingLineDirectives</code> parameter to
     * <code>true</code> causes "<code>#line &lt;line_number&gt; &lt;file_name&gt;</code>"
     * preprocessor directives to be added to the generated source code for use
     * by the metamodel debugger.
     *
     * @param node
     *            The TreeNode whose generated source code this list will
     *            contain.
     * @param emittingLineDirectives
     *            If <code>true</code>, generated code is to be used with the
     *            metamodel debugger.
     */
    public LineList(TreeNode node, boolean emittingLineDirectives) {
        this(node, emittingLineDirectives, false);
    }

    /**
     * Create a <code>LineList</code>, with control over whether it creates
     * code for metamodel debugging, and whether next-line information is added
     * automatically, or only by explicitly calling
     * <code>addBeginningLineInfo()</code>.
     * <p>
     * Setting the <code>emittingLineDirectives</code> parameter to
     * <code>true</code> causes "<code>#line &lt;line_number&gt; &lt;file_name&gt;</code>"
     * preprocessor directives to be added to the generated source code for use
     * by the metamodel debugger.
     *
     * @param node
     *            The TreeNode whose generated source code this list will
     *            contain.
     * @param emittingLineDirectives
     *            If <code>true</code>, generated code is to be used with the
     *            metamodel debugger.
     * @param nextLinesAreAddedExplicitly
     *            If <code>true</code>, the user intends to call
     *            <code>addBeginningLineInfo()</code> explicitly. The default
     *            is for it to be called automatically on the first invocation
     *            of <code>addLast()</code> for this <code>node</code>.
     */
    public LineList(TreeNode node, boolean emittingLineDirectives,
            boolean nextLinesAreAddedExplicitly) {
        _node = node;
        _fileName = (String) _node.getProperty(IDENT_KEY);
        // XXX Use short filename, until it turns out we need the full one.
        // We need the full one.
        /*
         * if (_fileName != null) { _fileName = DebugUtil.getNodeFileName(node); }
         */
        // try {
        Object lineNumberObj = _node.getProperty(LINENUMBER_KEY);
        if (lineNumberObj == NullValue.instance) {
            _lineNumber = null;
        } else {
            _lineNumber = (Integer) lineNumberObj;
        }
        // } catch (Exception ex) {
        // throw new RuntimeException(DebugUtil.getNodeFileName(node)
        // + ":" + DebugUtil.getNodeKind(node), ex);
        // }
        _nextLineNumbersList = (List) _node.getProperty(NEXT_LINENUMBERS_KEY);
        //_blockEndLineNumber = (Integer) _node
        //       .getProperty(BLOCK_END_LINENUMBER_KEY);
        _emittingLineDirectives = emittingLineDirectives;
        _nextLinesAreAddedExplicitly = nextLinesAreAddedExplicitly;

        Object noPushObj = node.getProperty(NO_LINENUMBER_PUSH_KEY);
        if (noPushObj != null) {
            _noPush = ((Boolean) noPushObj).booleanValue();
        }

        Object noPopObj = node.getProperty(NO_LINENUMBER_POP_KEY);
        if (noPopObj != null) {
            _noPop = ((Boolean) noPopObj).booleanValue();
        }

        if (_lineNumber != null) {
            if (_lineNumber.intValue() == -1)
                _lineNumber = null;
        }

        if (_fileName != null) {
            _shortFileName = _fileName.substring(_fileName
                    .lastIndexOf(File.separatorChar) + 1);
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    // / public methods

    /**
     * Add an await guard setup.
     *
     * @param awaitGuardNode
     *            The await guard node to be added.
     */
    public void addAwaitGuardSetup(AwaitGuardNode awaitGuardNode) {
        TreeNode guardStatement = (TreeNode) awaitGuardNode.getStmt();
        if (guardStatement != null) {
            Object stmtLineNumberObj = guardStatement
                    .getProperty(LINENUMBER_KEY);
            if (stmtLineNumberObj != null
                    && !(stmtLineNumberObj instanceof NullValue)) {
                //Integer stmtLineNumber = (Integer) stmtLineNumberObj;
                super.addLast("pc->setCurrentLineNumber(\""
                        + DebugUtil.getCurrentLineNumberString(guardStatement)
                        + "\");\n");
                StringBuffer buf = new StringBuffer();
                List lineNumberList = new Vector();
                lineNumberList.add(guardStatement.getProperty(LINENUMBER_KEY));
                _appendNextLinePush(buf, awaitGuardNode, lineNumberList);
                // (List)guardStatement.getProperty(NEXT_LINENUMBERS_KEY));
                super.addLast(buf.toString());
            }
        } else {
            // ??? How can an AwaitGuardNode's statement be null?
            throw new MMDebugException(awaitGuardNode.getParent(),
                    "Null AwaitGuardNode.");
        }
        // addLast("if (pc != gCurrentPC) {");
        // addLast("DebuggerInfo::processSwitched(pc);}\n");
        addLast("DebuggerInfo::processSwitched(pc);\n");
        addLast("pc->eventTag=pc->NO_EVENT_TAG; "
                + "gAllEventTags = gMmdbSB->getAllEventTags();\n");
    }

    /**
     * Add the code to set this node's current line number in the
     * <code>ProgramCounter</code>.
     */
    public void addCurrentLineInfo() {
        StringBuffer buf = new StringBuffer();
        _appendCurrentLineInfo(buf);
        super.addLast(buf.toString());
    }

    /**
     * Insert a dummy statement to be able to set a breakpoint at, which
     * corresponds to the exit from a method.
     * <p>
     * If the last statement in a method is an if-statement, its next-line may
     * be in its own then-clause, or it may fall through. Both cases must be
     * accounted for when providing the next-line info to the debugger. For this
     * reason, the fall-through case must be in the same context as "then" case.
     * (A new next-line context is pushed onto the stack on entry to a method,
     * and popped off on exit.) The only source-code line available as the
     * fall-through candidate for this is the closing brace. So we insert a
     * dummy statement, claiming to have the line number of the closing brace,
     * for the debugger to set a breakpoint at.
     *
     * @param block
     *            the <code>BlockNode</code> whose ending brace linenumber is
     *            used as the line number of the dummy statement.
     */
    public void addBreakpointDummyStatement(BlockNode block) {
        Integer bodyEndLineNumber = (Integer) block
                .getProperty(BLOCK_END_LINENUMBER_KEY);
        if (bodyEndLineNumber == null) {
            throw new MMDebugException(block,
                    "Expected block-end line number not found.");
        } else {
            addBreakpointDummyStatement(bodyEndLineNumber);
        }
    }

    /**
     * Insert a dummy statement to be able to set a breakpoint at, using the
     * given line number as its designated source-code line number.
     *
     * @param lineNumber
     *            the line number to use.
     */
    public void addBreakpointDummyStatement(Integer lineNumber) {
        addLineDirective(lineNumber);
        addLast("gMGDBDummyVar = 0; // Here for debugger breakpoint.\n");
    }

    /**
     * Add the code to push an empty next-line value onto the
     * <code>ProgramCounter</code>'s next-line stack.
     */
    public void addEmptyNextLinePush() {
        super.addLast("pc->pushNextLine(\"\");\n");
    }

    /**
     * Add some code to the list. If we are generating code for the metamodel
     * debugger, and the string we are adding contains a newline, also add
     * debugging line-number information.
     *
     * @param o
     *            An object, which may be a <code>String</code>, or another
     *            <code>List</code>.
     */
    public void addLast(Object o) {
        if (!_emittingLineDirectives || !(o instanceof java.lang.String)
                || o.toString().indexOf("\n") == -1
                || _node instanceof EmptyStmtNode) {
            super.addLast(o);
        } else {
            StringBuffer lineBuffer = new StringBuffer();

            if (!_lineInfoHasBeenOutput) {
                if (!_nextLinesAreAddedExplicitly) {
                    _appendNextLineInfo(lineBuffer);

                    // _appendCurrentLineInfo(lineBuffer);

                    if (!_lineNumbersAreAddedExplicitly) {
                        _appendLineDirective(lineBuffer);
                    }
                }
                if (lineBuffer.length() > 0) {
                    super.add(_placeholder, lineBuffer.toString());
                }
                _lineInfoHasBeenOutput = true;
            }
            super.addLast(o);

            _placeholder = size();
        }
    }

    /**
     * Explicitly add a <code>#line</code> directive to the generated C++
     * code, taking the line number and file name from the <code>TreeNode</code>
     * this was constructed with.
     */
    public void addLineDirective() {
        addLineDirective(_lineNumber);
    }

    /**
     * Explicitly add a <code>#line</code> directive to the generated C++
     * code, taking the line number from the given argument and the file name
     * from the <code>TreeNode</code> this was constructed with.
     *
     * @param lineNumber
     *            The <code>Integer</code> line number to use.
     */
    public void addLineDirective(Integer lineNumber) {
        if (lineNumber == null) {
            throw new MMDebugException(_node,
                    "addLineDirective() given null lineNumber.");
        }
        String fileName = DebugUtil.getNodeFileName(_node);
        StringBuffer buf = new StringBuffer();

        if (System.getProperty("file.separator").equals("\\")) {
            fileName = DebugUtil.flipBackslashes(fileName);
        }
        buf.append("#line ");
        buf.append(lineNumber);
        buf.append(" \"");
        buf.append(fileName);
        buf.append("\"\n");
        super.addLast(buf.toString());
    }

    /**
     * Add code to pop the next-line context at the end of a method body, unless
     * the method ends with a return statement, in which case that code has
     * already been added.
     *
     * @exception MMDebugException
     *                if the node this <code>LineList</code> was constructed
     *                with is not a <code>MethodDeclNode</code>.
     */
    public void addMethodEndFallthroughCode() {
        if (!(_node instanceof MethodDeclNode)) {
            throw new MMDebugException(_node,
                    "addMethodEndFallthroughCode(): Node is not a "
                            + "MethodDeclNode.");
        }
        addLast("pc->popNextLineContext();\n");
    }

    /**
     * Add the code to pop the top-most next-lines off the
     * <code>ProgramCounter</code>'s next-line stack.
     */
    public void addNextLinePop() {
        super.addLast("pc->popNextLines();\n");
    }

    /**
     * Add the code to push this node's next-lines onto the
     * <code>ProgramCounter</code>'s next-line stack.
     */
    public void addNextLinePush() {
        addNextLinePush(false);
    }

    /**
     * Add the code to push this node's next-lines onto the
     * <code>ProgramCounter</code>'s next-line stack. As a special case, add
     * this node's own line number to the next-line stack.
     *
     * @param addOwnLineNumber
     *            If <code>true</code> add this node's own line number to the
     *            next-line stack.
     */
    /*
     * FIXME: "special case", above should really read "hideous kludge", but
     * desperate times call for desperate action.
     */
    public void addNextLinePush(boolean addOwnLineNumber) {
        StringBuffer buf = new StringBuffer();
        _appendNextLinePush(buf, _node, _nextLineNumbersList);
        if (addOwnLineNumber) {
            buf.append("pc->addNextLine(\"");
            buf.append(DebugUtil.getCurrentLineNumberString(_node));
            buf.append("\");\n");
        }
        super.addLast(buf.toString());
    }

    /**
     * Set the line number to be used by addEndingLineInfo(). The default is to
     * use the BLOCK_END_LINENUMBER_KEY property of the node this LineList was
     * constructed with.
     *
     * @param blockEndLineNumber
     *            The line number to use.
     */
    public void setBlockEndLineNumber(Integer blockEndLineNumber) {
        //_blockEndLineNumber = blockEndLineNumber;
    }

    /**
     * Control whether <code>#line</code> directives are to be added
     * explicitly. Calling this with an argument of <code>true</code>
     * effectively turns off the output of <code>#line</code> directives,
     * except when the <code>addLineDirective()</code> method is called
     * explicitly.
     * <p>
     * <em>Note:</em> This is <em>static</em>. It toggles explicit line
     * numbering for all instances of <code>LineList</code>. Don't forget to
     * turn it off if you turn it on.
     *
     * @param isExplicit
     *            If <code>true</code>, turn off automatic generation of
     *            <code>#line</code> directives. If
     *            <code>false</codes>, turns it on.
     */
    public static void setExplicitLineNumbering(boolean isExplicit) {
        _lineNumbersAreAddedExplicitly = isExplicit;
    }

    /**
     * Explicitly set the line number to use in <code>#line</code> directives.
     *
     * @param lineNumber
     *            The number to set.
     */
    public void setLineNumber(Integer lineNumber) {
        _lineNumber = lineNumber;
    }

    // ////////////////////////////////////////////////////////////////////////
    // / private methods

    /**
     * Append C++ code about the current line number to the given
     * <code>StringBuffer</code>, taking the line number and file name from
     * the <code>TreeNode</code> this was constructed with.
     *
     * @param buf
     *            The <code>StringBuffer</code> to append to.
     */
    private void _appendCurrentLineInfo(StringBuffer buf) {
        if (_lineNumber != null) {
            // buf.append("delete pc->currentLineNumber; ");
            // buf.append("pc->currentLineNumber=new String(\"");
            buf.append("pc->setCurrentLineNumber(\"");
            buf.append(_shortFileName);
            buf.append(":");
            buf.append(_lineNumber);
            buf.append("\");\n");
            if (_DEBUGGING) {
                buf.append("gMmdbSB=pc->getScoreboard();");
                buf.append("gAllCurrentLines=gMmdbSB->getAllCurrentLines();");
                buf.append("for (int i=0;i<gAllCurrentLines.size();i++){");
                buf.append("cerr<<gAllCurrentLines[i]->data()<<endl;}");
                buf.append("cerr<<pc->p->name()<<endl;");
            }
        }
    }

    /**
     * Append a <code>#line</code> directive to the given
     * <code>StringBuffer</code>, taking the line number and file name from
     * the <code>TreeNode</code> this was constructed with.
     *
     * @param buf
     *            The <code>StringBuffer</code> to append to.
     */
    private void _appendLineDirective(StringBuffer buf) {
        String fileName = _fileName;

        if (System.getProperty("file.separator").equals("\\")) {
            fileName = DebugUtil.flipBackslashes(fileName);
        }

        if (_lineNumber != null) {
            if (_fileName != null) {
                buf.append("#line ");
                buf.append(_lineNumber);
                buf.append(" \"");
                buf.append(fileName);
                buf.append("\"\n");
            } else {
                buf.append("#line ");
                buf.append(_lineNumber);
                buf.append("\n");
            }
        }
    }

    /**
     * Add code to the list to push the given <code>List</code> of
     * <code>Integer</code> line numbers on the <code>ProgramCounter</code>'s
     * stack of next-lines. If the given <code>List</code> is null, then
     * generate code to just push a single empty string, which has special
     * meaning. (See ProgramCounter.cpp).
     *
     * @param node
     *            the <code>TreeNode</code> that the given line number belong
     *            to, to get the source-code file name from.
     * @param nextLineNumbersList
     *            the <code>List</code> of <code>Integer</code> next-line
     *            numbers.
     */
    private void _appendNextLinePush(StringBuffer buf, TreeNode node,
            List nextLineNumbersList) {
        if (nextLineNumbersList == null) {
            buf.append("pc->pushNextLine(\"\");\n");
        } else {
            String cmd = "pc->pushNextLine";
            for (int i = 0; i < nextLineNumbersList.size(); i++) {
                buf.append(cmd);
                buf.append("(\"");
                buf.append(DebugUtil.getCurrentLineNumberString(node,
                        (Integer) nextLineNumbersList.get(i)));
                buf.append("\");\n");
                cmd = "pc->addNextLine";
            }
        }
    }

    /**
     * Append C++ code about the next line numbers to the given
     * <code>StringBuffer</code>.
     *
     * @param buf
     *            The <code>StringBuffer</code> to append to.
     */
    private void _appendNextLineInfo(StringBuffer buf) {

        if (_nextLineNumbersList != null && _nextLineNumbersList.size() > 0) {
            if (!_noPop) {
                buf.append("pc->popNextLines();\n");
            }
            if (!_noPush) {
                _appendNextLinePush(buf, _node, _nextLineNumbersList);
            }
        }
    }

    //    private String _debugSayClearingLines() {
    //        if (_DEBUGGING) {
    //            StringBuffer buf = new StringBuffer();
    //            buf.append("/*");
    //            buf.append(DebugUtil.getNodeKind(_node));
    //            buf.append("*/");
    //            buf.append("cerr<<pc->p->name()<<\" clearing.\"");
    //            // String f = _fileName.substring(_fileName.lastIndexOf('/')+1);
    //            return buf.toString();
    //            // return new String("cerr<<\"" + f + " clearing.\"");
    //            // return new String("cerr<<\"" + f + " clearing.\"<<endl;");
    //        } else {
    //            return "";
    //        }
    //    }

    //    private String _debugSayAddingLine(String lineNumber) {
    //        if (_DEBUGGING) {
    //            String f = _fileName.substring(_fileName.lastIndexOf('/') + 1);
    //            return new String("cerr<<pc->p->name()<<\" adding line " + f + ":"
    //                    + lineNumber + ".\"<<endl;");
    //        } else {
    //            return "";
    //        }
    //    }

    //    private String _debugNewline() {
    //        if (_DEBUGGING) {
    //            return new String("<<endl;");
    //        } else {
    //            return "";
    //        }
    //    }
    //
    //    private String _debugSayThatsAll() {
    //        if (_DEBUGGING) {
    //            String f = _fileName.substring(_fileName.lastIndexOf('/') + 1);
    //            return new String("<<\" @" + f + ":" + _lineNumber + "\"<<endl;");
    //        } else {
    //            return "";
    //        }
    //    }

    // ////////////////////////////////////////////////////////////////////////
    // / private members

    private static final boolean _DEBUGGING = false;

    private static boolean _lineNumbersAreAddedExplicitly = false;

    //private Integer _blockEndLineNumber = null;

    private boolean _emittingLineDirectives = false;

    private String _fileName = null;

    private boolean _lineInfoHasBeenOutput = false;

    private Integer _lineNumber = null;

    private List _nextLineNumbersList = null;

    private boolean _nextLinesAreAddedExplicitly = false;

    private TreeNode _node = null;

    private boolean _noPop = false;

    private boolean _noPush = false;

    private int _placeholder = 0;

    private String _shortFileName = null;
}
