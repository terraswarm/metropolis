/* Constants used in the semantic analysis of the meta-model.

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

package metropolis.metamodel;

import metropolis.metamodel.nodetypes.NodeClassID;

// ////////////////////////////////////////////////////////////////////////
// // MetaModelStaticSemanticConstants
/**
 * Constants used in the semantic analysis of the meta-model. These constants
 * describe the kind of declarations, modifiers, and the values in the
 * PropertyMap in each tree node.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: MetaModelStaticSemanticConstants.java,v 1.41 2004/11/12
 *          11:00:42 guyang Exp $
 */
public interface MetaModelStaticSemanticConstants extends NodeClassID {

    // /////////////////////////////////////////////////////////////////
    // // public constants ////

    // MODIFIERS

    /** No modifier. */
    public static final int NO_MOD = 0;

    /** The 'public' modifier. */
    public static final int PUBLIC_MOD = 0x1;

    /** The 'protected' modifier. */
    public static final int PROTECTED_MOD = 0x2;

    /** The 'private' modifier. */
    public static final int PRIVATE_MOD = 0x4;

    /** The 'abstract' modifier. */
    public static final int ABSTRACT_MOD = 0x8;

    /** The 'final' modifier. */
    public static final int FINAL_MOD = 0x10;

    /** The 'static' modifier. */
    public static final int STATIC_MOD = 0x20;

    /** The 'elaborate' modifier. */
    public static final int ELABORATE_MOD = 0x40;

    /** Any modifier. */
    public static final int ANY_MOD = PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD
            | ABSTRACT_MOD | FINAL_MOD | STATIC_MOD | ELABORATE_MOD;

    // EFFECTS ON STATE

    /** Effect not specified. */
    public static final int NO_EFFECT = 0;

    /** The 'eval' effect. */
    public static final int EVAL_EFFECT = 0x80;

    /** The 'update' effect. */
    public static final int UPDATE_EFFECT = 0x100;

    /** The 'constant' effect. */
    public static final int CONSTANT_EFFECT = 0x200;

    /** Any effect. */
    public static final int ANY_EFFECT = EVAL_EFFECT | UPDATE_EFFECT
            | CONSTANT_EFFECT;

    // TYPES OF DECLARATIONS

    /** Declaration of a class. */
    public static final int CG_CLASS = 0x1;

    /**
     * Declaration of an interface.
     *
     * @see metropolis.metamodel.frontend.InterfaceDecl
     */
    public static final int CG_INTERFACE = 0x2;

    /** Declaration of a netlist. */
    public static final int CG_NETLIST = 0x4;

    /** Declaration of a process. */
    public static final int CG_PROCESS = 0x8;

    /** Declaration of a communication medium. */
    public static final int CG_MEDIUM = 0x10;

    /** Declaration of a scheduler. */
    public static final int CG_SCHEDULER = 0x20;

    /** Declaration of a state medium. */
    public static final int CG_SM = 0x40;

    /** Declaration of a field. */
    public static final int CG_FIELD = 0x80;

    /** Declaration of a class parameter. */
    public static final int CG_PARAMETER = 0x100;

    /** Declaration of a port. */
    public static final int CG_PORT = 0x200;

    /** Declaration of a method. */
    public static final int CG_METHOD = 0x400;

    /** Declaration of a constructor. */
    public static final int CG_CONSTRUCTOR = 0x800;

    /** Declaration of a local variable. */
    public static final int CG_LOCALVAR = 0x1000;

    /** Declaration of a formal parameter. */
    public static final int CG_FORMAL = 0x2000;

    /** Declaration of a package. */
    public static final int CG_PACKAGE = 0x4000;

    /** Declaration of a statement label. */
    public static final int CG_STMTLABEL = 0x8000;

    /** Declaration of type parameter (template). */
    public static final int CG_TEMPLATE = 0x10000;

    /** Declaration of a quantity. */
    public static final int CG_QUANTITY = 0x20000;

    /** Declaration of a constraint formula. */
    public static final int CG_CNSTFORMULA = 0x40000;

    /** A constant used to search for any single node declarations. */
    public static final int CG_SINGLENODE = CG_PROCESS | CG_MEDIUM
            | CG_SCHEDULER | CG_SM;

    /** A constant used to search for any node declaration. */
    public static final int CG_NODE = CG_NETLIST | CG_SINGLENODE;

    /** A constant used to search for any object class declaration . */
    public static final int CG_USERTYPE = CG_CLASS | CG_QUANTITY | CG_INTERFACE
            | CG_NODE;

    // KEYS FOR PROPERTY MAPS

    /**
     * The key that retrieves the canonical filename of the parsed meta-model
     * file. This property is used in CompileUnitNode.
     */
    public static final Integer IDENT_KEY = new Integer(0);

    /**
     * The key that retrieves the package declaration that the compilation unit
     * belongs to. This property is used in CompileUnitNode.
     */
    public static final Integer PACKAGE_KEY = new Integer(1);

    /**
     * The key that retrieves the List of packages that a compile unit imports.
     * This property is used in CompileUnitNode.
     */
    public static final Integer IMPORTED_PACKAGES_KEY = new Integer(2);

    /** The key that retrieves the declaration associated with a TreeNode. */
    public static final Integer DECL_KEY = new Integer(3);

    /** The key that retrieves the Scope associated with a TreeNode. */
    public static final Integer SCOPE_KEY = new Integer(4);

    /**
     * The key that retrieves the TypeNameNode for the class associated with a
     * ThisNode. This property is used in ThisNode.
     */
    public static final Integer THIS_CLASS_KEY = new Integer(5);

    /**
     * The key that retrieves the ClassDecl associated with the superclass. This
     * property is used in ClassDeclNode.
     */
    public static final Integer SUPERCLASS_KEY = new Integer(6);

    /**
     * The key that retrieves the statement that is the destination of a jump.
     * This property is used in nodes that jump (break, continue).
     */
    public static final Integer JUMP_DESTINATION_KEY = new Integer(7);

    /**
     * The key that retrieves the resolved type of the node of an expression.
     * This property is used in ExprNode.
     */
    public static final Integer TYPE_KEY = new Integer(8);

    /***************************************************************************
     * Metamodel debugging properties:
     */

    /**
     * The key that retrieves the line number of an instruction. This is used in
     * metamodel debugging.
     */
    public static final Integer LINENUMBER_KEY = new Integer(9);

    /**
     * The key that retrieves the <code>List</code> of line numbers of the
     * lines in a metamodel source code file that may execute next.
     * <p>
     * For example, for an <code>await</code> statement, it is a list of the
     * line numbers of the first lines of the <code>await</code> statement's
     * critical sections. For the last statement in the body of a while-loop, it
     * is the line number of the while-loop's test expression. For a simple
     * statement in a block, it is just line number of the next statement to
     * follow it. Etc.
     * <p>
     * This is used in metamodel debugging.
     */
    public static final Integer NEXT_LINENUMBERS_KEY = new Integer(10);

    /**
     * The key that retrieves the line number of the next sibling statement
     * during AST analysis for metamodel debugging.
     */
    public static final Integer NEXT_SIBLING_LINENUMBER_KEY = new Integer(11);

    /**
     * The key that retrieves the line number of the closing brace of a block.
     * This is used in metamodel debugging.
     */
    public static final Integer BLOCK_END_LINENUMBER_KEY = new Integer(12);

    /**
     * The value at this key is the <code>Integer</code> line number of the
     * first non-empty statement, in the <code>BlockNode</code> in which the
     * property is set, that is on a different line from the block's first
     * statement.
     */
    public static final Integer BLOCK_SECOND_LINENUMBER_KEY = new Integer(13);

    /**
     * The key that retrieves the line number of the test statement for a
     * do-while loop. This is used in metamodel debugging.
     */
    public static final Integer DOLOOP_TEST_LINENUMBER_KEY = new Integer(14);

    /**
     * This property is a flag indicating that the <code>TreeNode</code>'s
     * source-code line numbers should not be popped off the
     * <code>ProgramCounter</code>'s next-line-number stack at run time.
     */
    public static final Integer NO_LINENUMBER_POP_KEY = new Integer(15);

    /**
     * This property is a flag indicating that the <code>TreeNode</code>'s
     * source-code line numbers found in its <code>NEXT_LINENUMBERS_KEY</code>
     * property should not be pushed onto the <code>ProgramCounter</code>'s
     * next-line-number stack at run time.
     */
    public static final Integer NO_LINENUMBER_PUSH_KEY = new Integer(16);

    /**
     * This property is a flag indicating that the tree of nodes rooted at this
     * <code>TreeNode</code> does not get debugging info added to its
     * generated code.
     */
    public static final Integer DONT_DEBUG_ME_KEY = new Integer(17);

    /***************************************************************************
     * End metamodel debugging proerties.
     **************************************************************************/

    /**
     * If port in connect(), refineconnect(), redirectconnect() is a reference
     * defined locally or one in the object itself.
     */
    public static final Integer LOCALPORTREF_KEY = new Integer(18);

    /**
     * The key that retrieves from a medium a list of interfaces that is
     * referred by test lists and/or set lists in awaits either in the medium or
     * a process/medium connected to it. This property is associated with
     * MediumDeclNode.
     */
    public static final Integer REFINTFC_KEY = new Integer(19);

    /**
     * The key that indicates whether the current event (including interface
     * function call and labeled statement) is refered in LTL constraints.
     */
    public static final Integer LTLEVENT_KEY = new Integer(20);

    /**
     * Assign a unique ID to synched event in a process or medium.
     */
    public static final Integer LTLSYNCHEVENT_KEY = new Integer(21);

    /**
     * The key that indicates whether the current event (including interface
     * function call and labeled statement) is refered in LOC constraints.
     */
    public static final Integer LOCEVENT_KEY = new Integer(22);

    /**
     * The key that indicates whether the current event (including interface
     * function call and labeled statement) is refered in ELOC constraints.
     */
    public static final Integer ELOCEVENT_KEY = new Integer(23);

    /**
     * The key that indicates whether the current event (including interface
     * function call and labeled statement) is refered in ltl synch implication
     * constraints.
     */
    public static final Integer LTLSYNCHIMPLYEVENT_KEY = new Integer(24);

    /**
     * The key that indicates whether the current event (including interface
     * function call and labeled statement) is refered by built-in loc
     * constraints.
     */
    public static final Integer BUILTINLOCEVENT_KEY = new Integer(25);

    /**
     * The key that maps to the property that points to the prototype of the
     * template definition if a UserTypeDeclNode is a template instance. The
     * property is a pointer to a UserTypeDeclNode and associated only with a
     * UserTypeDeclNode.
     */
    public static final Integer TEMPLDECL_KEY = new Integer(26);

    /**
     * The key that maps to the property that points to the original definition
     * of a TypeNameNode whose NameNode has parameters (i.e. a template instance
     * name) after the node is resolved. The property is a pointer to a
     * TypeNameNode and associated only with a TypeNameNode.
     */
    public static final Integer ORIGTYPE_KEY = new Integer(27);

    /**
     * The key that indicates whether an action is interleaving concurrent
     * atomic or not. Actions are limited to functions, critical sections of an
     * await, an await, and the entire interface that a medium implements.
     */
    public static final Integer ICATOMIC_KEY = new Integer(28);

    /**
     * The number of properties reserved for static resolution in the meta-model
     * language. This number can be used to start numbering extended properties.
     */
    public static final int RESERVED_PROPERTIES = ICATOMIC_KEY.intValue() + 1;

    // Miscellaneous constants

    /**
     * The package name to assign to compile units that do not have package
     * names specified explicitly.
     */
    public static final String DEFAULT_PACKAGE_NAME = "metropolis.default";
}
