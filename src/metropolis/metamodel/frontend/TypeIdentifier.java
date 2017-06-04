/* A class used to identify types and relation between types.

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

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.nodetypes.BoolTypeNode;
import metropolis.metamodel.nodetypes.ByteTypeNode;
import metropolis.metamodel.nodetypes.CharTypeNode;
import metropolis.metamodel.nodetypes.DoubleTypeNode;
import metropolis.metamodel.nodetypes.EventTypeNode;
import metropolis.metamodel.nodetypes.FloatTypeNode;
import metropolis.metamodel.nodetypes.IntTypeNode;
import metropolis.metamodel.nodetypes.LongTypeNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.PCTypeNode;
import metropolis.metamodel.nodetypes.ShortTypeNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;

// ////////////////////////////////////////////////////////////////////////
// // TypeIdentifier
/**
 * A class that identifies special types. This class provides methods used
 * during type checking.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TypeIdentifier.java,v 1.21 2006/10/12 20:34:25 cxh Exp $
 */
public class TypeIdentifier implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new TypeIdentifier. This object will be used by a TypePolicy to
     * check types.
     */
    public TypeIdentifier() {
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    // Primitive types

    /** Identifier of an unknown type. */
    public static final int TYPE_KIND_UNKNOWN = -1;

    /** Identifier of boolean type. */
    public static final int TYPE_KIND_BOOLEAN = 0;

    /** Identifier of byte integer type. */
    public static final int TYPE_KIND_BYTE = 1;

    /** Identifier of short integer type. */
    public static final int TYPE_KIND_SHORT = 2;

    /** Identifier of character type. */
    public static final int TYPE_KIND_CHAR = 3;

    /** Identifier of integer type. */
    public static final int TYPE_KIND_INT = 4;

    /** Identifier of long integer type. */
    public static final int TYPE_KIND_LONG = 5;

    /** Identifier of floating point type. */
    public static final int TYPE_KIND_FLOAT = 6;

    /** Identifier of double precision floating point type. */
    public static final int TYPE_KIND_DOUBLE = 7;

    /** Identifier of event type. */
    public static final int TYPE_KIND_EVENT = 8;

    /** Identifier of values of a program counter. */
    public static final int TYPE_KIND_PCVAL = 9;

    // Types which are not considered primitive

    /** Identifier of a void type (for return types). */
    public static final int TYPE_KIND_VOID = 10;

    /** Identifier of the type of type parameters. */
    public static final int TYPE_KIND_TEMPLATE = 11;

    /** Identifier of the type of NULL. */
    public static final int TYPE_KIND_NULL = 12;

    /** Identifier of an array type. */
    public static final int TYPE_KIND_ARRAY = 13;

    /** Identifier of the type of an array initializer. */
    public static final int TYPE_KIND_ARRAYINIT = 14;

    /** Identifier of a class type. */
    public static final int TYPE_KIND_CLASS = 15;

    /** Identifier of an interface type. */
    public static final int TYPE_KIND_INTERFACE = 16;

    /** Identifier of a process type. */
    public static final int TYPE_KIND_PROCESS = 17;

    /** Identifier of a scheduler type. */
    public static final int TYPE_KIND_SCHEDULER = 18;

    /** Identifier of a medium type. */
    public static final int TYPE_KIND_MEDIUM = 19;

    /** Identifier of a state medium type. */
    public static final int TYPE_KIND_STATEMEDIUM = 20;

    /** Identifier of a netlist type. */
    public static final int TYPE_KIND_NETLIST = 21;

    /** Identifier of a quantity type. */
    public static final int TYPE_KIND_QUANTITY = 22;

    /** Number of primitive types in the meta-model. */
    public static final int NUM_PRIMITIVE_TYPES = TYPE_KIND_PCVAL + 1;

    /** Total number of types in the meta-model. */
    public static final int TYPE_KINDS = TYPE_KIND_QUANTITY + 1;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get an integer that identifies a type. This integer is called kind and
     * the values for it are defined in this class.
     *
     * @param type
     *            Type whose kind is requested.
     * @return an integer that identifies the type. This integer has a different
     *         value for each kind of primitive type and for each kind of class
     *         of objects. For example, a class and a process have different
     *         kind, but two classes have the same kind.
     */
    public int kind(TypeNode type) {
        switch (type.classID()) {
        case ARRAYTYPENODE_ID:
            return TYPE_KIND_ARRAY;
        case NULLTYPENODE_ID:
            return TYPE_KIND_NULL;
        case TEMPLATETYPENODE_ID:
            return TYPE_KIND_TEMPLATE;
        case BOOLTYPENODE_ID:
            return TYPE_KIND_BOOLEAN;
        case BYTETYPENODE_ID:
            return TYPE_KIND_BYTE;
        case CHARTYPENODE_ID:
            return TYPE_KIND_CHAR;
        case SHORTTYPENODE_ID:
            return TYPE_KIND_SHORT;
        case INTTYPENODE_ID:
            return TYPE_KIND_INT;
        case LONGTYPENODE_ID:
            return TYPE_KIND_LONG;
        case FLOATTYPENODE_ID:
            return TYPE_KIND_FLOAT;
        case DOUBLETYPENODE_ID:
            return TYPE_KIND_DOUBLE;
        case ARRAYINITTYPENODE_ID:
            return TYPE_KIND_ARRAYINIT;
        case VOIDTYPENODE_ID:
            return TYPE_KIND_VOID;
        case EVENTTYPENODE_ID:
            return TYPE_KIND_EVENT;
        case PCTYPENODE_ID:
            return TYPE_KIND_PCVAL;
        case TYPENAMENODE_ID:
            return kindOfName((TypeNameNode) type);
        default:
            throw new RuntimeException("Unknown type found");
        }
    }

    /**
     * Get the primitive type corresponding to the argument kind.
     *
     * @param kind
     *            Identifier being looked up.
     * @return The primitive type corresponding to this type.
     * @exception RuntimeException
     *                if the kind does not correspond to a primitive type.
     */
    public TypeNode primitiveKindToType(int kind) {
        if (kind < 0) {
            throw new RuntimeException("Unknown type kind '" + kind
                    + "' used as primitive type, type kind must be > 0.");
        }
        if (kind > NUM_PRIMITIVE_TYPES) {
            throw new RuntimeException("Type kind '" + kind
                    + "' is not primitive, type must be < "
                    + NUM_PRIMITIVE_TYPES + ".");
        }
        return _PRIMITIVE_KIND_TO_TYPE[kind];
    }

    /**
     * Get the kind of the user type, which can be either a class type,
     * interface type, process type, netlist type, medium type, scheduler type,
     * state medium type or type parameter type, related to a type name node.
     *
     * @param type
     *            Name of the type.
     * @return The kind of this type.
     */
    public int kindOfName(TypeNameNode type) {
        NamedNode name = (NamedNode) type;
        MetaModelDecl decl = MetaModelDecl.getDecl(name);
        return kindOfDecl(decl);
    }

    /**
     * Get the kind of the user type, which can be either a class type,
     * interface type, process type, netlist type, medium type, scheduler type,
     * state medium type or type parameter type, related to a metamodel
     * declaration defining a type.
     *
     * @param decl
     *            Declaration of the type.
     * @return The kind of this type.
     */
    public int kindOfDecl(MetaModelDecl decl) {
        if (decl == null)
            return TYPE_KIND_UNKNOWN;
        if (!decl.hasDefType())
            throw new RuntimeException("Declaration '" + decl.getName()
                    + "' does not declare a type.");
        switch (decl.category) {
        case CG_TEMPLATE:
            return TYPE_KIND_TEMPLATE;
        case CG_CLASS:
            return TYPE_KIND_CLASS;
        case CG_INTERFACE:
            return TYPE_KIND_INTERFACE;
        case CG_NETLIST:
            return TYPE_KIND_NETLIST;
        case CG_PROCESS:
            return TYPE_KIND_PROCESS;
        case CG_MEDIUM:
            return TYPE_KIND_MEDIUM;
        case CG_SCHEDULER:
            return TYPE_KIND_SCHEDULER;
        case CG_SM:
            return TYPE_KIND_STATEMEDIUM;
        case CG_QUANTITY:
            return TYPE_KIND_QUANTITY;
        default:
            throw new RuntimeException("Unknown kind of declaration");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * An array, indexed by kind, of the primitive type corresponding to the
     * kind.
     */
    protected static final TypeNode[] _PRIMITIVE_KIND_TO_TYPE = new TypeNode[] {
            BoolTypeNode.instance, ByteTypeNode.instance,
            ShortTypeNode.instance, CharTypeNode.instance,
            IntTypeNode.instance, LongTypeNode.instance,
            FloatTypeNode.instance, DoubleTypeNode.instance,
            EventTypeNode.instance, PCTypeNode.instance };

}
