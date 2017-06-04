/* A class that stores abstract syntax trees in disk using binary format.
 Abstract syntax trees can be retrieved in this way without parsing a text
 file.

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

import metropolis.metamodel.Effect;
import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // ASTCache
/**
 * A class that stores and retrieves abstract syntax trees using a binary
 * format. Abstract syntax tress can be retrieved from this format without
 * parsing the source file again. This class is intended to be used as a caching
 * mechanism for parsed ASTs.
 * <p>
 * This class does not use Serialization of abstract syntax trees because of its
 * efficiency: parsing a text file is much faster than reading a serialized AST!
 * Therefore, this class does not use Serializable or Externalizable.
 * <p>
 * The format used to store ASTs tries to use the minimum amount of disk space
 * for each TreeNode. Basic data types (integer, string, etc). are
 * stored/retrieved using the methods in DataInput and DataOutput. The following
 * information is stored for a compile unit:
 * <ul>
 * <li> A format version identifier string, "MMM_AST_ver_xxx.yyy", where "xxx"
 * is the major version number and "yyy" the minor version number. Whenever the
 * format changes in a way that renders the previous format incompatible,
 * <code>MAJOR_VERSION_NUMBER</code> must be incremented. For changes to the
 * format that do not render the previous version incompatible, increment
 * <code>MINOR_VERSION_NUMBER</code>
 * <li> A char M with the number of object declarations created in this file.
 * Using char to specify this number means that we assume that there will be at
 * most 65535 object declarations inside one file.
 * <li> A char N with the number of unique identifiers in the compile unit.
 * Using char to specify this number means that we assume that there will be at
 * most 65535 different unique identifiers in a compile unit.
 * <li> A sequence of N Strings, containing the N unique identifiers. Each
 * String is stored in UTF format.
 * <li> An int P with the number of unique property values in the compile unit.
 * <li> A sequence of P serialized Objects, containing all the TreeNode property
 * values stored with the AST.
 * <li> Information about the CompileUnitNode, which recursively contains
 * information about its subnodes.
 * </ul>
 *
 * Each node of the abstract syntax tree is stored in the following binary
 * format:
 * <ul>
 * <li> A special byte NODE_BEGIN that designates the beginning of node
 * <li> A short (2 bytes) indicating the kind of node being stored, as defined
 * in NodeClassID.java
 * <li> An optional byte OBJ_DECL indicating that this node contains an object
 * declaration, followed by a char (2 bytes) indicating the position of this
 * declaration in the array of object declarations.
 * <li> The values of the attributes of this node (see below)
 * <li> An optional byte PROPERTIES indicating that node properties follow, in
 * the following format:
 * <ul>
 * <li> A char <i>n</i>, specifying how many properties follow.
 * <li> A list of <i>n</i> properties, each in the following form:
 * <ul>
 * <li> The property key value, converted to a char.
 * <li> An <code>int</code> index into the list of unique property values that
 * points to the current property value.
 * </ul>
 * </ul>
 * <li> The children of this node, with two possible situations.
 * <ul>
 * <li> If this children of the node is another tree node, its contents are
 * printed using this rules recursively.
 * <li> If this children of the node is a list of tree ndoes, we print a special
 * byte _LIST_BEGIN, then the contents of the list using this rules recursively,
 * and then a special byte _LIST_END
 * </ul>
 * <li> A special byte _NODE_END that designates the end of a node.
 * </ul>
 * Each node is considered to contain attributes of 3 kinds:
 * <ul>
 * <li> an integer
 * <li> a String which is not necessarily unique
 * <li> a unique identifier, of type String
 * </ul>. The last kind of attribute tries to take advantage of names of local
 * variables/ports/etc., which might be repeated many times in the same file.
 * <p>
 * For each attribute of the node, we print the byte specifying its kind and
 * then its value. Its value is stored in the following format:
 * <ul>
 * <li> for integers, the byte _FIELD_INT and then 4 bytes of the integer value
 * <li> for strings, the byte _FIELD_STRING and then the UTF representation of
 * the String
 * <li> for identifiers, the byte _FIELD_ID and then a char (2 bytes) indicating
 * the position of the id in the sequence of Strings stored at the beginning of
 * the file.
 * </ul>
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ASTCache.java,v 1.41 2006/10/12 20:33:38 cxh Exp $
 */
public class ASTCache implements NodeClassID, MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Retrieve an abstract syntax tree from a file with a given name.
     *
     * @param repository
     *            Repository where the AST should be loaded.
     */
    public static void load(SyntaxRepository repository) {
        _repository = repository;
        CompileUnitNode node = null;
        File file = repository.getRepositoryPath();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileInputStream);
            node = _loadCompileUnit(in);
        } catch (java.io.InvalidClassException ex) {
            throw new RuntimeException("Error retrieving cache file '"
                    + file.getPath() + "'.\n This can happen when the "
                    + ".class files has changed since the .*.ast cache file "
                    + "was created.\n The solution is to remove the .*.ast "
                    + "file when the .class file is modified.\n See the "
                    + "NullValue.class rule in "
                    + "metro/src/metropolis/metamodel/makefile for details.",
                    ex);

        } catch (Exception e) {
            int available = -1;
            try {

                if (fileInputStream != null) {
                    available = fileInputStream.available();
                    fileInputStream.close();
                }
            } catch (Exception ex2) {
                // Ignore
            }
            throw new RuntimeException("Error retrieving cache file "
                    + file.getPath()
                    + (available == -1 ? "" : " roughly " + available
                            + " bytes from the end"), e);
        }
        _repository._ast = node;
    }

    /**
     * Store an abstract syntax tree in a file with a given name, including only
     * the LINENUMBER_KEY and BLOCK_END_LINENUMBER_KEY properties. All other
     * properties are ignored, and will not exist in the AST when it is loaded
     * from the store created here.
     *
     * @param repository
     *            Repository where the abstract syntax tree is stored.
     */
    public static void store(SyntaxRepository repository) {
        List defaultKeyList = new ArrayList();
        defaultKeyList.add(LINENUMBER_KEY);
        defaultKeyList.add(BLOCK_END_LINENUMBER_KEY);
        store(repository, defaultKeyList);
    }

    /**
     * Store an abstract syntax tree in a file with a given name, and include a
     * the given properties. All other properties are ignored, and will not
     * exist in the AST when it is loaded from the store created here.
     *
     * @param repository
     *            Repository where the abstract syntax tree is stored.
     * @param propertyKeyList
     *            a <code>List</code> of the keys of properties to be stored.
     *            All values of these specified properties must implement
     *            <code>Serializable</code>.
     */
    public static void store(SyntaxRepository repository, List propertyKeyList) {
        _propertyKeyList = new ArrayList();
        for (int i = 0; i < propertyKeyList.size(); i++) {
            _propertyKeyList.add(propertyKeyList.get(i));
        }
        _repository = repository;
        CompileUnitNode tree = repository.getSyntaxTree();
        File file = repository.getRepositoryPath();
        try {
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(f);
            _storeCompileUnit(tree, out);
        } catch (IOException e) {
            throw new RuntimeException("Error storing cache file "
                    + file.getName() + ". " + e.toString());
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /**
     * The major version number. Files with different major versions are
     * incompatible.
     *
     * N.B: If you make a change to this class that makes the format
     * incompatible with earlier versions, be sure to increment this number!
     */
    private static byte _MAJOR_VERSION_NUMBER = 2;

    /**
     * The minor version number. Files with lower minor versions from this can
     * be used, but may lack features of this version.
     */
    private static byte _MINOR_VERSION_NUMBER = 0;

    /** Delimiter of the begining of a node of the AST. */
    private static byte _NODE_BEGIN = 0;

    /** Delimiter of the end of a node of the AST. */
    private static byte _NODE_END = 1;

    /** Delimiter of the begining of a list of nodes of the AST. */
    private static byte _LIST_BEGIN = 2;

    /** Delimiter of the end of a list of nodes of the AST. */
    private static byte _LIST_END = 3;

    /** Marker for an object declaration. */
    private static byte _OBJ_DECL = 4;

    /** Marker for an integer field. */
    private static byte _FIELD_INT = 5;

    /** Marker for a string field. */
    private static byte _FIELD_STRING = 6;

    /** Marker for a unique identifier string. */
    private static byte _FIELD_ID = 7;

    /** Marker for a unique property value. */
    private static byte _PROPERTIES = 8;

    /** List of unique identifier strings in current file. */
    private static ArrayList _names = null;

    /** Repository where the file should be loaded/stored. */
    private static SyntaxRepository _repository = null;

    /** List of unique property values in current file. */
    private static List _properties = null;

    /** List of keys of properties to be stored. */
    private static List _propertyKeyList = null;

    // /////////////////////////////////////////////////////////////////
    // // private methods ////

    /**
     * Build a node of the tree with a given kind. The object returned has been
     * built using the constructor with 0 arguments of the node with a kind
     * equal to the one passed as a parameter. However, if the kind corresponds
     * to a singleton node, the singleton instance for that node is returned
     * rather than creating a new node of that class.
     *
     * @param kind
     *            Kind of the node to be created.
     * @return An empty node of the given kind.
     * @exception IOException
     *                if the kind provided is not a valid kind of an object of a
     *                node.
     */
    private static TreeNode _buildNodeByKind(int kind) throws IOException {
        switch (kind) {
        case ABSENTTREENODE_ID:
            return AbsentTreeNode.instance;
        case BOOLTYPENODE_ID:
            return BoolTypeNode.instance;
        case CHARTYPENODE_ID:
            return CharTypeNode.instance;
        case BYTETYPENODE_ID:
            return ByteTypeNode.instance;
        case SHORTTYPENODE_ID:
            return ShortTypeNode.instance;
        case INTTYPENODE_ID:
            return IntTypeNode.instance;
        case FLOATTYPENODE_ID:
            return FloatTypeNode.instance;
        case LONGTYPENODE_ID:
            return LongTypeNode.instance;
        case DOUBLETYPENODE_ID:
            return DoubleTypeNode.instance;
        case EVENTTYPENODE_ID:
            return EventTypeNode.instance;
        case VOIDTYPENODE_ID:
            return VoidTypeNode.instance;
        case NULLTYPENODE_ID:
            return NullTypeNode.instance;
        case TYPENAMENODE_ID:
            return new TypeNameNode();
        case ARRAYTYPENODE_ID:
            return new ArrayTypeNode();
        case ARRAYINITTYPENODE_ID:
            return ArrayInitTypeNode.instance;
        case DECLARATORNODE_ID:
            return new DeclaratorNode();
        case COMPILEUNITNODE_ID:
            return new CompileUnitNode();
        case IMPORTNODE_ID:
            return new ImportNode();
        case IMPORTONDEMANDNODE_ID:
            return new ImportOnDemandNode();
        case FIELDDECLNODE_ID:
            return new FieldDeclNode();
        case LOCALVARDECLNODE_ID:
            return new LocalVarDeclNode();
        case CONSTRUCTORDECLNODE_ID:
            return new ConstructorDeclNode();
        case THISCONSTRUCTORCALLNODE_ID:
            return new ThisConstructorCallNode();
        case SUPERCONSTRUCTORCALLNODE_ID:
            return new SuperConstructorCallNode();
        case PARAMETERNODE_ID:
            return new ParameterNode();
        case BLOCKNODE_ID:
            return new BlockNode();
        case EMPTYSTMTNODE_ID:
            return new EmptyStmtNode();
        case LABELEDSTMTNODE_ID:
            return new LabeledStmtNode();
        case IFSTMTNODE_ID:
            return new IfStmtNode();
        case SWITCHNODE_ID:
            return new SwitchNode();
        case CASENODE_ID:
            return new CaseNode();
        case SWITCHBRANCHNODE_ID:
            return new SwitchBranchNode();
        case LOOPNODE_ID:
            return new LoopNode();
        case EXPRSTMTNODE_ID:
            return new ExprStmtNode();
        case USERTYPEDECLSTMTNODE_ID:
            return new UserTypeDeclStmtNode();
        case FORNODE_ID:
            return new ForNode();
        case BREAKNODE_ID:
            return new BreakNode();
        case CONTINUENODE_ID:
            return new ContinueNode();
        case RETURNNODE_ID:
            return new ReturnNode();
        case INTLITNODE_ID:
            return new IntLitNode();
        case LONGLITNODE_ID:
            return new LongLitNode();
        case FLOATLITNODE_ID:
            return new FloatLitNode();
        case DOUBLELITNODE_ID:
            return new DoubleLitNode();
        case BOOLLITNODE_ID:
            return new BoolLitNode();
        case CHARLITNODE_ID:
            return new CharLitNode();
        case STRINGLITNODE_ID:
            return new StringLitNode();
        case NULLPNTRNODE_ID:
            return new NullPntrNode();
        case THISNODE_ID:
            return new ThisNode();
        case ARRAYINITNODE_ID:
            return new ArrayInitNode();
        case ARRAYACCESSNODE_ID:
            return new ArrayAccessNode();
        case OBJECTNODE_ID:
            return new ObjectNode();
        case OBJECTFIELDACCESSNODE_ID:
            return new ObjectFieldAccessNode();
        case TYPEFIELDACCESSNODE_ID:
            return new TypeFieldAccessNode();
        case SUPERFIELDACCESSNODE_ID:
            return new SuperFieldAccessNode();
        case THISFIELDACCESSNODE_ID:
            return new ThisFieldAccessNode();
        case TYPECLASSACCESSNODE_ID:
            return new TypeClassAccessNode();
        case OUTERTHISACCESSNODE_ID:
            return new OuterThisAccessNode();
        case OUTERSUPERACCESSNODE_ID:
            return new OuterSuperAccessNode();
        case METHODCALLNODE_ID:
            return new MethodCallNode();
        case ALLOCATENODE_ID:
            return new AllocateNode();
        case ALLOCATEARRAYNODE_ID:
            return new AllocateArrayNode();
        case ALLOCATEANONYMOUSCLASSNODE_ID:
            return new AllocateAnonymousClassNode();
        case POSTINCRNODE_ID:
            return new PostIncrNode();
        case POSTDECRNODE_ID:
            return new PostDecrNode();
        case UNARYPLUSNODE_ID:
            return new UnaryPlusNode();
        case UNARYMINUSNODE_ID:
            return new UnaryMinusNode();
        case PREINCRNODE_ID:
            return new PreIncrNode();
        case PREDECRNODE_ID:
            return new PreDecrNode();
        case COMPLEMENTNODE_ID:
            return new ComplementNode();
        case NOTNODE_ID:
            return new NotNode();
        case CASTNODE_ID:
            return new CastNode();
        case MULTNODE_ID:
            return new MultNode();
        case DIVNODE_ID:
            return new DivNode();
        case REMNODE_ID:
            return new RemNode();
        case PLUSNODE_ID:
            return new PlusNode();
        case MINUSNODE_ID:
            return new MinusNode();
        case LEFTSHIFTLOGNODE_ID:
            return new LeftShiftLogNode();
        case RIGHTSHIFTLOGNODE_ID:
            return new RightShiftLogNode();
        case RIGHTSHIFTARITHNODE_ID:
            return new RightShiftArithNode();
        case LTNODE_ID:
            return new LTNode();
        case GTNODE_ID:
            return new GTNode();
        case LENODE_ID:
            return new LENode();
        case GENODE_ID:
            return new GENode();
        case INSTANCEOFNODE_ID:
            return new InstanceOfNode();
        case EQNODE_ID:
            return new EQNode();
        case NENODE_ID:
            return new NENode();
        case BITANDNODE_ID:
            return new BitAndNode();
        case BITORNODE_ID:
            return new BitOrNode();
        case BITXORNODE_ID:
            return new BitXorNode();
        case CANDNODE_ID:
            return new CandNode();
        case CORNODE_ID:
            return new CorNode();
        case IFEXPRNODE_ID:
            return new IfExprNode();
        case ASSIGNNODE_ID:
            return new AssignNode();
        case MULTASSIGNNODE_ID:
            return new MultAssignNode();
        case DIVASSIGNNODE_ID:
            return new DivAssignNode();
        case REMASSIGNNODE_ID:
            return new RemAssignNode();
        case PLUSASSIGNNODE_ID:
            return new PlusAssignNode();
        case MINUSASSIGNNODE_ID:
            return new MinusAssignNode();
        case LEFTSHIFTLOGASSIGNNODE_ID:
            return new LeftShiftLogAssignNode();
        case RIGHTSHIFTLOGASSIGNNODE_ID:
            return new RightShiftLogAssignNode();
        case RIGHTSHIFTARITHASSIGNNODE_ID:
            return new RightShiftArithAssignNode();
        case BITANDASSIGNNODE_ID:
            return new BitAndAssignNode();
        case BITXORASSIGNNODE_ID:
            return new BitXorAssignNode();
        case BITORASSIGNNODE_ID:
            return new BitOrAssignNode();
        case NAMENODE_ID:
            return new NameNode();
        case TEMPLATEPARAMETERSNODE_ID:
            return new TemplateParametersNode();
        case CIFNODE_ID:
            return new CifNode();
        case CIFFNODE_ID:
            return new CiffNode();
        case PCTYPENODE_ID:
            return PCTypeNode.instance;
        case LOCALLABELNODE_ID:
            return new LocalLabelNode();
        case GLOBALLABELNODE_ID:
            return new GlobalLabelNode();
        case LABELEDBLOCKNODE_ID:
            return new LabeledBlockNode();
        case CONSTRAINTBLOCKNODE_ID:
            return new ConstraintBlockNode();
        case BOUNDEDLOOPNODE_ID:
            return new BoundedLoopNode();
        case NONDETERMINISMNODE_ID:
            return new NonDeterminismNode();
        case EXECINDEXNODE_ID:
            return new ExecIndexNode();
        case EXPRLTLNODE_ID:
            return new ExprLTLNode();
        case FUTURELTLNODE_ID:
            return new FutureLTLNode();
        case GLOBALLYLTLNODE_ID:
            return new GloballyLTLNode();
        case NEXTLTLNODE_ID:
            return new NextLTLNode();
        case UNTILLTLNODE_ID:
            return new UntilLTLNode();
        case FORALLACTIONNODE_ID:
            return new ForallActionNode();
        case EXISTSACTIONNODE_ID:
            return new ExistsActionNode();
        case EXPRACTIONNODE_ID:
            return new ExprActionNode();
        case BEGINPCNODE_ID:
            return new BeginPCNode();
        case ENDPCNODE_ID:
            return new EndPCNode();
        case PCNODE_ID:
            return new PCNode();
        case AWAITSTATEMENTNODE_ID:
            return new AwaitStatementNode();
        case AWAITGUARDNODE_ID:
            return new AwaitGuardNode();
        case AWAITLOCKNODE_ID:
            return new AwaitLockNode();
        case INTERFACEDECLNODE_ID:
            return new InterfaceDeclNode();
        case CLASSDECLNODE_ID:
            return new ClassDeclNode();
        case NETLISTDECLNODE_ID:
            return new NetlistDeclNode();
        case PROCESSDECLNODE_ID:
            return new ProcessDeclNode();
        case MEDIUMDECLNODE_ID:
            return new MediumDeclNode();
        case SCHEDULERDECLNODE_ID:
            return new SchedulerDeclNode();
        case SMDECLNODE_ID:
            return new SMDeclNode();
        case METHODDECLNODE_ID:
            return new MethodDeclNode();
        case REFINENODE_ID:
            return new RefineNode();
        case CONNECTNODE_ID:
            return new ConnectNode();
        case ADDCOMPONENTNODE_ID:
            return new AddComponentNode();
        case SETSCOPENODE_ID:
            return new SetScopeNode();
        case REFINECONNECTNODE_ID:
            return new RefineConnectNode();
        case REDIRECTCONNECTNODE_ID:
            return new RedirectConnectNode();
        case GETCONNECTIONNUMNODE_ID:
            return new GetConnectionNumNode();
        case GETNTHCONNECTIONSRCNODE_ID:
            return new GetNthConnectionSrcNode();
        case GETNTHCONNECTIONPORTNODE_ID:
            return new GetNthConnectionPortNode();
        case PORTDECLNODE_ID:
            return new PortDeclNode();
        case PARAMETERDECLNODE_ID:
            return new ParameterDeclNode();
        case TEMPLATETYPENODE_ID:
            return TemplateTypeNode.instance;
        case BLACKBOXNODE_ID:
            return new BlackboxNode();
        case OBJECTPORTACCESSNODE_ID:
            return new ObjectPortAccessNode();
        case THISPORTACCESSNODE_ID:
            return new ThisPortAccessNode();
        case SUPERPORTACCESSNODE_ID:
            return new SuperPortAccessNode();
        case OBJECTPARAMACCESSNODE_ID:
            return new ObjectParamAccessNode();
        case THISPARAMACCESSNODE_ID:
            return new ThisParamAccessNode();
        case SUPERPARAMACCESSNODE_ID:
            return new SuperParamAccessNode();
        case SPECIALLITNODE_ID:
            return new SpecialLitNode();
        case GETCOMPONENTNODE_ID:
            return new GetComponentNode();
        case GETCONNECTIONDESTNODE_ID:
            return new GetConnectionDestNode();
        case GETNTHPORTNODE_ID:
            return new GetNthPortNode();
        case GETPORTNUMNODE_ID:
            return new GetPortNumNode();
        case GETSCOPENODE_ID:
            return new GetScopeNode();
        case GETTHREADNODE_ID:
            return new GetThreadNode();
        case ACTIONLABELSTMTNODE_ID:
            return new ActionLabelStmtNode();
        case ACTIONLABELEXPRNODE_ID:
            return new ActionLabelExprNode();
        case ANNOTATIONNODE_ID:
            return new AnnotationNode();
        case BEGINANNOTATIONNODE_ID:
            return new BeginAnnotationNode();
        case ENDANNOTATIONNODE_ID:
            return new EndAnnotationNode();
        case LTLSYNCHNODE_ID:
            return new LTLSynchNode();
        case EQUALVARSNODE_ID:
            return new EqualVarsNode();
        case ACTIONNODE_ID:
            return new ActionNode();
        case EVENTNODE_ID:
            return new EventNode();
        case BEGINEVENTNODE_ID:
            return new BeginEventNode();
        case ENDEVENTNODE_ID:
            return new EndEventNode();
        case NONEEVENTNODE_ID:
            return new NoneEventNode();
        case OTHEREVENTNODE_ID:
            return new OtherEventNode();
        case IMPLYNODE_ID:
            return new ImplyNode();
        case LTLCONSTRAINTNODE_ID:
            return new LTLConstraintNode();
        case LOCCONSTRAINTNODE_ID:
            return new LOCConstraintNode();
        case ELOCCONSTRAINTNODE_ID:
            return new ELOCConstraintNode();
        case CONSTRAINTDECLNODE_ID:
            return new ConstraintDeclNode();
        case ELOCCONSTRAINTDECLNODE_ID:
            return new ELOCConstraintDeclNode();
        case LOCCONSTRAINTDECLNODE_ID:
            return new LOCConstraintDeclNode();
        case LTLCONSTRAINTDECLNODE_ID:
            return new LTLConstraintDeclNode();
        case ELOCCONSTRAINTCALLNODE_ID:
            return new ELOCConstraintCallNode();
        case LOCCONSTRAINTCALLNODE_ID:
            return new LOCConstraintCallNode();
        case LTLCONSTRAINTCALLNODE_ID:
            return new LTLConstraintCallNode();
        case QUANTITYDECLNODE_ID:
            return new QuantityDeclNode();
        case EXCLLTLNODE_ID:
            return new ExclLTLNode();
        case MUTEXLTLNODE_ID:
            return new MutexLTLNode();
        case SIMULLTLNODE_ID:
            return new SimulLTLNode();
        case PRIORITYLTLNODE_ID:
            return new PriorityLTLNode();
        case MINRATENODE_ID:
            return new MinRateNode();
        case MAXRATENODE_ID:
            return new MaxRateNode();
        case PERIODNODE_ID:
            return new PeriodNode();
        case MINDELTANODE_ID:
            return new MinDeltaNode();
        case MAXDELTANODE_ID:
            return new MaxDeltaNode();
        case GETINSTNAMENODE_ID:
            return new GetInstNameNode();
        case GETCOMPNAMENODE_ID:
            return new GetCompNameNode();
        case ISCONNECTIONREFINEDNODE_ID:
            return new IsConnectionRefinedNode();
        case GETTYPENODE_ID:
            return new GetTypeNode();
        case GETPROCESSNODE_ID:
            return new GetProcessNode();
        case VARINEVENTREFNODE_ID:
            return new VarInEventRefNode();

        default:
            throw new IOException("Cache file corrupted, unknown kind: " + kind);
        }
    }

    /**
     * Read in the file format version identifier string, and check that its
     * major version number matches _MAJOR_VERSION_NUMBER. If not, throw a
     * <code>RuntimeException</code> about it.
     *
     * @param in
     *            The <code>ObjectInput</code> to read from.
     * @exception <code>RuntimeException</code> if version number doesn't match,
     *                or can't be parsed.
     * @exception <code>IOException</code> on I/O problems.
     */
    private static void _checkVersionIdentifier(ObjectInput in)
            throws IOException {
        String idString = in.readUTF();
        String majorString = null;

        try {
            if (!idString.substring(0, 12).equals("MMM_AST_ver_")) {
                throw new RuntimeException("Unknown AST format version.");
            }
            majorString = idString.substring(12, 15);
            try {
                byte majorNumber = Byte.parseByte(majorString, 10);
                if (majorNumber != _MAJOR_VERSION_NUMBER) {
                    throw new RuntimeException("Incompatible AST file format. "
                            + "Major version is " + majorNumber
                            + ", expecting " + _MAJOR_VERSION_NUMBER + ".");
                }
            } catch (NumberFormatException ex) {
                throw new RuntimeException(
                        "Can't parse AST file's major version number from \""
                                + majorString + "\".");
            }
        } catch (StringIndexOutOfBoundsException ex) {
            throw new RuntimeException("Unknown AST format version '"
                    + (idString == null ? "null" : idString) + "'", ex);
        }
    }

    /**
     * Find the list of unique ids of the compile unit recursively.
     *
     * @param node
     *            The current node being visited.
     */
    private static void _findUniqueIds(TreeNode node) {
        // Add current id if necessary
        String ident = null;
        if (node instanceof BlackboxNode)
            ident = ((BlackboxNode) node).getIdent();
        else if (node instanceof NameNode)
            ident = ((NameNode) node).getIdent();
        if (ident != null) {
            if (!_names.contains(ident))
                _names.add(ident);
        }
        // Find unique ids recursively
        Iterator iter = node.children().iterator();
        while (iter.hasNext()) {
            Object elem = iter.next();
            if (elem instanceof TreeNode)
                _findUniqueIds((TreeNode) elem);
            else {
                List list = (List) elem;
                Iterator iter2 = list.iterator();
                while (iter2.hasNext()) {
                    _findUniqueIds((TreeNode) iter2.next());
                }
            }
        }
    }

    /**
     * Find the list of unique property values in the compile unit recursively.
     *
     * @param node
     *            The current node being visited.
     */
    private static void _findUniqueProperties(TreeNode node) {
        Iterator desiredKeys = _propertyKeyList.iterator();
        while (desiredKeys.hasNext()) {
            Integer key = (Integer) desiredKeys.next();
            if (node.hasProperty(key)) {
                Object value = node.getProperty(key);
                if (!_properties.contains(value)) {
                    _properties.add(value);
                }
            }
        }

        Iterator children = node.children().iterator();
        while (children.hasNext()) {
            Object elem = children.next();
            if (elem instanceof TreeNode)
                _findUniqueProperties((TreeNode) elem);
            else {
                List list = (List) elem;
                Iterator nodes = list.iterator();
                while (nodes.hasNext()) {
                    _findUniqueProperties((TreeNode) nodes.next());
                }
            }
        }
    }

    /**
     * Load the list of children of a node. Retrieve the list from disk and
     * store it inside the node.
     *
     * @param lastByteRead
     *            Last marker/delimiter read from the node.
     * @param node
     *            Node where the list of children will be stored.
     * @param in
     *            InputStream where the list of children is stored.
     */
    private static void _loadChildren(byte lastByteRead, TreeNode node,
            ObjectInput in) throws IOException {
        LinkedList children = new LinkedList();
        try {
            while (lastByteRead != _NODE_END) {
                if (lastByteRead == _NODE_BEGIN) {
                    // Read a single child
                    children.add(_loadNode(in));
                } else if (lastByteRead == _LIST_BEGIN) {
                    // Read a list of children
                    LinkedList others = new LinkedList();
                    lastByteRead = in.readByte();
                    while (lastByteRead != _LIST_END) {
                        others.add(_loadNode(in));
                        lastByteRead = in.readByte();
                    }
                    children.add(others);
                }
                lastByteRead = in.readByte();
            }
        } catch (Exception ex) {
            StringBuffer childrenInfo = new StringBuffer();
            try {
                Iterator childrenIterator = children.iterator();
                while (childrenIterator.hasNext()) {
                    if (childrenInfo.length() > 0) {
                        childrenInfo.append(", ");
                    }
                    childrenInfo.append(childrenIterator.next().getClass()
                            .getName());
                }
            } catch (Exception ex2) {
                // ignore
            }
            IOException io = new IOException("Failed to load children "
                    + "into " + node.getClass().getName()
                    + ". Children seen were: " + childrenInfo);
            io.initCause(ex);
            throw io;
        }
        node.setChildren(children);
    }

    /**
     * Load a compile unit from disk. First load the number of unique ids in the
     * program, then load the sequence of unique ids and finally load all nodes
     * of the abstract syntax tree recursively.
     *
     * @param in
     *            InputStream from where the compile unit has to be retrieved.
     * @exception IOException
     *                if there is an IO error while retrieving the compile unit.
     */
    private static CompileUnitNode _loadCompileUnit(ObjectInput in)
            throws IOException, ClassNotFoundException {

        // Check that we understand this file's format:
        _checkVersionIdentifier(in);

        // Read number of object declarations
        // Adjust the size of 'sources' in the repository
        int numObjects = in.readChar();
        _repository._sources = new TreeNode[numObjects];

        // Read number of unique strings
        char numStrings = in.readChar();
        _names = new ArrayList(numStrings);
        // Read unique strings
        while (numStrings-- > 0)
            _names.add(in.readUTF().intern());

        // Read number of unique property values
        int numProperties = in.readInt();
        _properties = new ArrayList(numProperties);
        // Read unique property values
        while (numProperties-- > 0)
            _properties.add(in.readObject());

        // Read the classes of the AST
        in.readByte(); // Consume the _NODE_BEGIN byte!
        TreeNode node = _loadNode(in);
        _names = null;
        return (CompileUnitNode) node;
    }

    /**
     * Load the fields of an node, i.e. retrieve the information from disk and
     * store it in the object in memory. Keep track of the last marker/delimiter
     * read during this process. Also, read the integer id of the object
     * declaration for this node, if any.
     *
     * @param tree
     *            The node of the tree being read.
     * @param in
     *            InputStream where the fields of the object are stored.
     * @return The last marker/delimiter byte being read, which is not
     *         _FIELD_INT, _FIELD_ID or _FIELD_STRING.
     */
    private static byte _loadFields(TreeNode tree, ObjectInput in)
            throws IOException {

        // Read the values of the fields
        int intVal = 0;
        String stringVal = null;
        String idVal = null;
        boolean moreFields = true;
        byte lastByteRead;
        do {
            lastByteRead = in.readByte();
            if (lastByteRead == _FIELD_INT) {
                intVal = in.readInt();
            } else if (lastByteRead == _FIELD_STRING) {
                stringVal = in.readUTF();
            } else if (lastByteRead == _FIELD_ID) {
                char pos = in.readChar();
                idVal = (String) _names.get(pos);
            } else if (lastByteRead == _OBJ_DECL) {
                char pos = in.readChar();
                _repository._sources[pos] = tree;
            } else {
                moreFields = false;
            }
        } while (moreFields);

        // Store the values of the fields in the node
        if (tree instanceof NameNode) {
            NameNode node = (NameNode) tree;
            node.setIdent(idVal);
        } else if (tree instanceof LiteralNode) {
            LiteralNode node = (LiteralNode) tree;
            node.setLiteral(stringVal);
        } else if (tree instanceof BlackboxNode) {
            BlackboxNode node = (BlackboxNode) tree;
            node.setIdent(idVal);
            node.setCode(stringVal);
        } else if (tree instanceof AllocateArrayNode) {
            AllocateArrayNode node = (AllocateArrayNode) tree;
            node.setDims(intVal);
        } else if (tree instanceof DeclaratorNode) {
            DeclaratorNode node = (DeclaratorNode) tree;
            node.setDims(intVal);
        } else if (tree instanceof ModifiedNode) {
            if (tree instanceof MethodDeclNode) {
                MethodDeclNode node = (MethodDeclNode) tree;
                int modifiers = intVal & Effect.ANY_MOD;
                int effect = intVal & Effect.ANY_EFFECT;
                node.setModifiers(modifiers);
                node.setEffect(effect);
            } else {
                ModifiedNode node = (ModifiedNode) tree;
                node.setModifiers(intVal);
            }
        }

        return lastByteRead;
    }

    /**
     * Load a node from disk. The _NODE_BEGIN has already been consumed
     * previously. Read the kind of node being retrieved, create an instance of
     * an AST of this kind and load all fields of this node. Then, load all
     * children of this node recursively.
     *
     * @param in
     *            InputStream where the node is retrieved.
     * @return The abstract syntax tree of the compile unit.
     * @exception IOException
     *                if there is an IO error while retrieving the node of the
     *                abstract syntax tree.
     */
    private static TreeNode _loadNode(ObjectInput in) throws IOException {
        int kind = in.readShort();

        // Build a node of the kind being retrieved
        TreeNode node = _buildNodeByKind(kind);

        // Load the field of the node
        byte lastByteRead = _loadFields(node, in);

        // Load the properties, if any:
        if (lastByteRead == _PROPERTIES) {
            lastByteRead = _loadProperties(node, in);
        }

        // Load the children of the node, if any
        if (lastByteRead != _NODE_END)
            _loadChildren(lastByteRead, node, in);
        return node;
    }

    /**
     * Read the property key and index values for this node, and for each
     * key-index pair, set its property with the given key to the value found in
     * the <code>_properties</code> list at the given index.
     *
     * @param node
     *            The TreeNode to set properties in.
     * @param in
     *            the ObjectInput to read from.
     * @return The next byte read after the property information has been read.
     */
    private static byte _loadProperties(TreeNode node, ObjectInput in)
            throws IOException {
        int numberOfProperties = in.readChar();
        while (numberOfProperties-- > 0) {
            Integer key = new Integer((int) in.readChar());
            int valueIndex = in.readInt();
            node.setProperty(key, _properties.get(valueIndex));
        }
        return in.readByte();
    }

    /**
     * Store an abstract syntax tree that contains information on an entire
     * compile unit to disk. Store information about the identifiers of the
     * tree, and the nodes of the tree.
     *
     * @param node
     *            The compile unit being stored.
     * @param out
     *            File where the node must be stored.
     * @exception IOException
     *                if there is an IO error while storing the file.
     */
    private static void _storeCompileUnit(CompileUnitNode node, ObjectOutput out)
            throws IOException {

        // Write the version identifier:
        _writeVersionIdentifier(out);

        // Write the number of object declarations in this compile unit
        out.writeChar((char) _repository.getObjectDecls().length);

        // Build a list with the unique ids in the compile unit
        _names = new ArrayList();
        _findUniqueIds(node);
        // Write number of unique ids in the compile unit
        out.writeChar((char) _names.size());
        // Write the sequence of unique ids
        Iterator iter = _names.iterator();
        while (iter.hasNext()) {
            String value = (String) iter.next();
            out.writeUTF(value);
        }

        // Build a list with the unique property values.
        _properties = new ArrayList();
        _findUniqueProperties(node);
        // Write number of unique ids in the compile unit
        out.writeInt(_properties.size());

        // Write the sequence of unique property values:
        iter = _properties.iterator();
        while (iter.hasNext())
            out.writeObject(iter.next());

        // Write the nodes of the AST recursively
        _storeNode(node, out);
        out.flush();
        _names = null;
    }

    /**
     * Write the fields of a node of an abstact syntax tree to disk.
     *
     * @param tree
     *            Node whose fields are to be stored.
     * @param out
     *            File where the fields have to be stored.
     * @exception RuntimeException
     *                if there is an error while storing these fields.
     */
    private static void _storeFields(TreeNode tree, ObjectOutput out)
            throws IOException {
        if (tree instanceof NameNode) {
            NameNode node = (NameNode) tree;
            _storeID(node.getIdent(), out);
        } else if (tree instanceof LiteralNode) {
            LiteralNode node = (LiteralNode) tree;
            _storeString(node.getLiteral(), out);
        } else if (tree instanceof BlackboxNode) {
            BlackboxNode node = (BlackboxNode) tree;
            _storeID(node.getIdent(), out);
            _storeString(node.getCode(), out);
        } else if (tree instanceof AllocateArrayNode) {
            AllocateArrayNode node = (AllocateArrayNode) tree;
            _storeInt(node.getDims(), out);
        } else if (tree instanceof DeclaratorNode) {
            DeclaratorNode node = (DeclaratorNode) tree;
            _storeInt(node.getDims(), out);
        } else if (tree instanceof ModifiedNode) {
            ModifiedNode node = (ModifiedNode) tree;
            int modifiers = node.getModifiers();
            if (tree instanceof MethodDeclNode) {
                MethodDeclNode method = (MethodDeclNode) node;
                modifiers = modifiers | method.getEffect();
            }
            _storeInt(modifiers, out);
        }
    }

    /**
     * Store a unique identifier.
     *
     * @param value
     *            Identifier to be stored.
     * @param out
     *            Place where the field has to be stored.
     * @exception IOException
     *                if there is an IO error.
     */
    private static void _storeID(String value, ObjectOutput out)
            throws IOException {
        out.writeByte(_FIELD_ID);
        // Find the position of this id in the list of ids
        char pos = (char) _names.indexOf(value);
        if (pos == -1)
            throw new RuntimeException("Id not found!");
        out.writeChar(pos);
    }

    /**
     * Store an integer field of a node.
     *
     * @param value
     *            Integer to be stored.
     * @param out
     *            Place where the field has to be stored.
     * @exception IOException
     *                if there is an IO error.
     */
    private static void _storeInt(int value, ObjectOutput out)
            throws IOException {
        out.writeByte(_FIELD_INT);
        out.writeInt(value);
    }

    /**
     * Write a list of children nodes of an abstract syntax tree.
     *
     * @param list
     *            List of children to be written to disk.
     * @param out
     *            Place where the list has to be stored.
     * @exception RuntimeException
     *                if there is an error while storing these fields.
     */
    private static void _storeList(List list, ObjectOutput out)
            throws IOException {
        // Write the "begin" delimiter
        out.writeByte(_LIST_BEGIN);
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            _storeNode((TreeNode) iter.next(), out);
        }
        // Write the "end" delimiter
        out.writeByte(_LIST_END);
    }

    /**
     * Store a node of an abstract syntax tree to disk. Store the identifier of
     * the class of nodes, the fields of this node and (recursively) the
     * children of this node.
     *
     * @param node
     *            The node being stored.
     * @param out
     *            File where the node must be stored.
     * @exception IOException
     *                if there is an IO error while storing the file.
     */
    private static void _storeNode(TreeNode node, ObjectOutput out)
            throws IOException {
        // Write the "begin" delimiter
        out.writeByte(_NODE_BEGIN);

        // Write the kind of this node
        out.writeShort((short) node.classID());

        // If necessary, write the object declaration for this node
        if (node instanceof UserTypeDeclNode) {
            out.writeByte(_OBJ_DECL);
            // Find the position of this object declaration in
            // the array of declarations of this compile unit
            UserTypeDeclNode declNode = (UserTypeDeclNode) node;
            ObjectDecl decl = (ObjectDecl) MetaModelDecl.getDecl(declNode
                    .getName());
            ObjectDecl[] decls = _repository.getObjectDecls();
            int i;
            for (i = 0; i < decls.length; i++)
                if (decls[i] == decl)
                    break;
            out.writeChar((char) i);
        }

        // Write the fields of this node
        _storeFields(node, out);

        // Write out the properties of this node, if it has any that we are
        // saving.
        Iterator desiredKeys = _propertyKeyList.iterator();
        while (desiredKeys.hasNext()) {
            if (node.hasProperty((Integer) desiredKeys.next())) {
                out.writeByte(_PROPERTIES);
                _storeProperties(node, out);
                break;
            }
        }

        // Write the children of this node
        Iterator iter = node.children().iterator();
        while (iter.hasNext()) {
            Object child = iter.next();
            boolean isList = child instanceof List;
            if (isList)
                _storeList((List) child, out);
            else
                _storeNode((TreeNode) child, out);
        }
        // Write the "end" delimiter
        out.writeByte(_NODE_END);
    }

    /**
     * For each of the given TreeNode's properties that is to be stored, find
     * the index of its value in the list of unique properties, and write out
     * the property's key as a <code>char</code>, and the index as an
     * <code>int</code>.
     */
    private static void _storeProperties(TreeNode node, ObjectOutput out)
            throws IOException {
        Iterator desiredKeys = _propertyKeyList.iterator();
        // First count them and write the count.
        int count = 0;
        while (desiredKeys.hasNext()) {
            if (node.hasProperty((Integer) desiredKeys.next())) {
                count++;
            }
        }
        out.writeChar((char) count);

        // Now write the properties.
        desiredKeys = _propertyKeyList.iterator();
        while (desiredKeys.hasNext()) {
            Integer key = (Integer) desiredKeys.next();
            if (node.hasProperty(key)) {
                int valueIndex = _properties.indexOf(node.getProperty(key));
                if (valueIndex == -1) {
                    throw new IllegalStateException("Property value not found "
                            + "in list of unique values. " + "Node is a "
                            + node.getClass().getName() + ", from source file "
                            + node.getProperty(IDENT_KEY) + ", line #"
                            + node.getProperty(LINENUMBER_KEY) + ".");
                }
                out.writeChar((char) key.intValue());
                out.writeInt(valueIndex);
            }
        }
    }

    /**
     * Store a non-unique string of a node.
     *
     * @param value
     *            String to be stored.
     * @param out
     *            Place where the field has to be stored.
     * @exception IOException
     *                if there is an IO error.
     */
    private static void _storeString(String value, ObjectOutput out)
            throws IOException {
        out.writeByte(_FIELD_STRING);
        out.writeUTF(value);
    }

    /**
     * Write out the version identifier string that appears at the top of the
     * file. The string is "MMM_AST_ver_xxx.yyy", where "xxx" is
     * _MAJOR_VERSION_NUMBER, and "yyy" is _MINOR_VERSION_NUMBER.
     *
     * @param out
     *            The <code>ObjectOutput</code> to write to.
     * @exception IOException
     *                on I/O problems.
     */
    private static void _writeVersionIdentifier(ObjectOutput out)
            throws IOException {
        StringBuffer buf = new StringBuffer("MMM_AST_ver_");
        StringBuffer majorBuf = new StringBuffer("000");
        StringBuffer minorBuf = new StringBuffer("000");

        majorBuf.append(String.valueOf((int) _MAJOR_VERSION_NUMBER));
        minorBuf.append(String.valueOf((int) _MINOR_VERSION_NUMBER));

        buf.append(majorBuf.substring(majorBuf.length() - 3));
        buf.append(".");
        buf.append(minorBuf.substring(minorBuf.length() - 3));

        out.writeUTF(buf.toString());
    }
}
