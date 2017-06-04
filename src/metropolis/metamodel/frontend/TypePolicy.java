/* A class that encapsulates a type policy, used to perform type checking and
 conversions.

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
import metropolis.metamodel.Modifier;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.BoolTypeNode;
import metropolis.metamodel.nodetypes.ByteTypeNode;
import metropolis.metamodel.nodetypes.CharTypeNode;
import metropolis.metamodel.nodetypes.DoubleTypeNode;
import metropolis.metamodel.nodetypes.EventTypeNode;
import metropolis.metamodel.nodetypes.ExprNode;
import metropolis.metamodel.nodetypes.FloatTypeNode;
import metropolis.metamodel.nodetypes.IntTypeNode;
import metropolis.metamodel.nodetypes.LiteralNode;
import metropolis.metamodel.nodetypes.LongTypeNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.NullTypeNode;
import metropolis.metamodel.nodetypes.ShortTypeNode;
import metropolis.metamodel.nodetypes.TemplateParametersNode;
import metropolis.metamodel.nodetypes.TemplateTypeNode;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.VoidTypeNode;

import java.util.Iterator;
import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TypePolicy
/**
 * A class that implements methods to compare types, perform widening of types
 * (i.e. find the least common type between two types) and check type
 * conversions. This methods are used to perform type checking, identify method
 * overriding, and resolve polymorphism in method and constructor invocation.
 * <p>
 * Types inside this class are identified by integers. The mapping between types
 * and its integer identifiers is established by the class TypeIdentifier.
 * <p>
 * There are two types in the Meta model that did not appear in Java: pcval for
 * program counter values and type parameters of templates. The first one is
 * considered a new primitive type of the language, while the latter is a
 * special type, which can be assigned to variables of any kind, operated with
 * types of any kind or converted to types of any kind.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: TypePolicy.java,v 1.31 2006/10/12 20:34:27 cxh Exp $
 */
public class TypePolicy implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new type policy, with the default mapping between types and
     * integer ids.
     */
    public TypePolicy() {
        this(new TypeIdentifier());
    }

    /**
     * Create a new type policy with a given mapping between types and integer
     * ids.
     *
     * @param typeID
     *            Mapping between types and integer ids.
     */
    public TypePolicy(TypeIdentifier typeID) {
        _typeID = typeID;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Get the mapping between types and integer ids used in this node.
     *
     * @return The TypeIdentifier used to map types to integers.
     */
    public TypeIdentifier typeIdentifier() {
        return _typeID;
    }

    /**
     * Return true iff type is a type parameter of a template.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is a type parameter of a template.
     */
    public boolean isTypeParameter(TypeNode type) {
        // GY: handle both array and scalar types
        TypeNode t = type;
        while (t instanceof ArrayTypeNode) {
            t = ((ArrayTypeNode) t).getBaseType();
        }
        // GY

        return (_typeID.kind(t) == TypeIdentifier.TYPE_KIND_TEMPLATE);
    }

    /**
     * Return true iff type is a primitive type. Primitive types include Java
     * primitive types (int, short, ...) plus two types added in the meta-model:
     * type parameters and program counters.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is a primitive type.
     */
    public boolean isPrimitiveType(TypeNode type) {
        int kind = _typeID.kind(type);
        return (kind > TypeIdentifier.TYPE_KIND_UNKNOWN)
                && (kind < TypeIdentifier.NUM_PRIMITIVE_TYPES);
    }

    /**
     * Return true iff type is a reference type. Arrays and objects are
     * reference type. Apart from these, there are two more reference types:
     * null type, which is a reference type that always matches with other
     * reference typs; and type parameters of templates, which can be replaced
     * by reference types, so they are also considered reference types.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is an array, object or type parameter.
     */
    public boolean isReferenceType(TypeNode type) {
        return ((type == NullTypeNode.instance) || isArrayType(type) || ((type instanceof TypeNameNode) && (!isTypeParameter(type))));
    }

    /**
     * Return true iff type is a floating point type.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is a floating point type.
     */
    public boolean isFloatType(TypeNode type) {
        return _isOneOf(type, _FLOAT_TYPES);
    }

    /**
     * Return true iff type is an integral type.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is an integral type.
     */
    public boolean isIntegralType(TypeNode type) {
        return _isOneOf(type, _INTEGRAL_TYPES);
    }

    /**
     * Return true iff type supports arithmetic operations.
     *
     * @param type
     *            Type being analyzed.
     * @return true if type is numeric.
     */
    public boolean isNumericType(TypeNode type) {
        return _isOneOf(type, _ARITH_TYPES);
    }

    /**
     * Return true iff type is boolean.
     *
     * @param type
     *            Type being analyzed.
     * @return true if type is boolean.
     */
    public boolean isBooleanType(TypeNode type) {
        return _typeID.kind(type) == TypeIdentifier.TYPE_KIND_BOOLEAN;
    }

    /**
     * Return true iff type is an interface.
     *
     * @param type
     *            Type being analyzed.
     * @return true if type is an interface.
     */
    public boolean isInterfaceType(TypeNode type) {
        return _typeID.kind(type) == TypeIdentifier.TYPE_KIND_INTERFACE;
    }

    /**
     * Return true iff type is a user defined type (process, class, interface,
     * ...), i.e. an object.
     *
     * @param type
     *            Type being analyzed.
     * @return true if type is a user defined type.
     */
    public boolean isUserType(TypeNode type) {
        switch (_typeID.kind(type)) {
        case TypeIdentifier.TYPE_KIND_CLASS:
        case TypeIdentifier.TYPE_KIND_INTERFACE:
        case TypeIdentifier.TYPE_KIND_NETLIST:
        case TypeIdentifier.TYPE_KIND_PROCESS:
        case TypeIdentifier.TYPE_KIND_MEDIUM:
        case TypeIdentifier.TYPE_KIND_SCHEDULER:
        case TypeIdentifier.TYPE_KIND_STATEMEDIUM:
        case TypeIdentifier.TYPE_KIND_QUANTITY:
            return true;
        }
        return false;
    }

    /**
     * Return true iff type is an array type.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is an array.
     */
    public boolean isArrayType(TypeNode type) {
        return (_typeID.kind(type) == TypeIdentifier.TYPE_KIND_ARRAY);
    }

    /**
     * Return true iff type is a String type.
     *
     * @param type
     *            Type being analyzed.
     * @return true iff type is a String type.
     */
    public boolean isStringType(TypeNode type) {
        return areEqual(type, MetaModelLibrary.STRING_TYPE);
    }

    /**
     * Return true if TypeNodes t1 and t2 are identical. By identical, we mean
     * that:
     * <ul>
     * <li> t1 and t2 are primitive types, and the same primitive type
     * <li> t1 and t2 are object types, and they refer to exactly the same
     * object and have same parameters
     * <li> t1 and t2 are arrays, and the base types of t1 and t2 are identical.
     * <li> if any one of the types is a type paramenter of a template
     * </ul>
     *
     * @param t1
     *            Type being compared.
     * @param t2
     *            Type being compared.
     * @return true iff type are identical.
     */
    public boolean areEqual(TypeNode t1, TypeNode t2) {

        // Compare basic types.
        // Basic types are singletons, so if two types have the same
        // primitive type, they must be exactly equal
        if (t1 == t2)
            return true;

        // Compare arrays
        // Check that both are arrays and both have the same base type.
        // FIXME: Assume T is a type parameter of a template, then types
        // int[][] and T[] are considered equal or not? How about T[] and T?
        // Maybe we need to consider this issue in canPassParameter().
        if (isArrayType(t1) && isArrayType(t2)) {
            ArrayTypeNode at1 = (ArrayTypeNode) t1;
            ArrayTypeNode at2 = (ArrayTypeNode) t2;
            TypeNode base1 = at1.getBaseType();
            TypeNode base2 = at2.getBaseType();
            return areEqual(base1, base2);
        } else if (isArrayType(t1) || isArrayType(t2))
            return false;

        /*
         * else if (isArrayType(t1)) { if (t2 instanceof TypeNameNode) {
         * MetaModelDecl decl = MetaModelDecl.getDecl(t2); if (decl instanceof
         * TypeParameterDecl) return true; else return false; } else return
         * false; } else if (isArrayType(t2)) { if (t1 instanceof TypeNameNode) {
         * MetaModelDecl decl = MetaModelDecl.getDecl(t1); if (decl instanceof
         * TypeParameterDecl) return true; else return false; } else return
         * false; }
         */

        // Compare classes
        // Check that both are classes and refer to the same class
        if (!(t1 instanceof TypeNameNode))
            return false;
        if (!(t2 instanceof TypeNameNode))
            return false;
        TypeNameNode tn1 = (TypeNameNode) t1;
        TypeNameNode tn2 = (TypeNameNode) t2;

        return areEqual(tn1, tn2);

        // GY: temporary change
        // If any one of the two is a template, they are equal.
        // In fact, finer comparison is possible. e.g.
        // if both are in the same scope, check whether their template types
        // are the same; or if one is a template array, the other is a primitive
        // type
        // they can not be equal.

    }

    /**
     * Return true if two TypeNameNodes are identical. This does not mean that
     * the names are exactly the same, but that they refer exactly to the same
     * class and their NameNodes have same parameters.
     *
     * @param tn1
     *            TypeNameNode being compared.
     * @param tn2
     *            TypeNameNode being compared.
     * @return true if the TypeNameNodes are identical.
     */
    public boolean areEqual(TypeNameNode tn1, TypeNameNode tn2) {
        MetaModelDecl decl1 = MetaModelDecl.getDecl((NamedNode) tn1);
        MetaModelDecl decl2 = MetaModelDecl.getDecl((NamedNode) tn2);

        // FIXME: is this possible?
        if (decl1 == null && decl2 == null)
            return false;

        // If any one of the two is a type parameter of a template ,
        // they are considered equal.
        if ((decl1 instanceof TypeParameterDecl)
                || (decl2 instanceof TypeParameterDecl))
            return true;

        // Two types are two different classes, or one is null and
        // the other is not.
        if (decl1 != decl2)
            return false;

        // Two types refer to the exactly same class, we need to check
        // if they have same parameters. E.g. templ-<int>- and
        // templ-<short>- are two different types, but templ-<int>-
        // and templ-<T>- are the same if T is a type paramenter of a template.
        TreeNode params1 = tn1.getName().getParameters();
        TreeNode params2 = tn2.getName().getParameters();
        if (params1 == AbsentTreeNode.instance
                && params2 == AbsentTreeNode.instance) {
            return true;
        } else if (params1 == AbsentTreeNode.instance
                || params2 == AbsentTreeNode.instance) {
            return false;
        }

        List paramTypes1 = ((TemplateParametersNode) params1).getTypes();
        List paramTypes2 = ((TemplateParametersNode) params2).getTypes();

        if (paramTypes1.size() != paramTypes2.size())
            return false;
        else {
            Iterator typeIter1 = paramTypes1.iterator();
            Iterator typeIter2 = paramTypes2.iterator();
            while (typeIter1.hasNext() && typeIter2.hasNext()) {
                TypeNode type1 = (TypeNode) typeIter1.next();
                TypeNode type2 = (TypeNode) typeIter2.next();
                if (!areEqual(type1, type2))
                    return false;
            }

            return true;
        }
    }

    /**
     * Return true if a type t1 is a subclass of type t2.
     *
     * @param t1
     *            Possible subclass.
     * @param t2
     *            Possible superclass.
     * @return true if t1 is a subclass of type t2.
     */
    public boolean isSubClass(TypeNode t1, TypeNode t2) {

        if (!isUserType(t2))
            return false;

        ObjectDecl subDecl = null;
        ObjectDecl superDecl = (ObjectDecl) MetaModelDecl
                .getDecl((NamedNode) t2);

        if (isArrayType(t1)) {
            subDecl = MetaModelLibrary.ARRAY_CLASS_DECL;
        } else if (isUserType(t1)) {
            subDecl = (ObjectDecl) MetaModelDecl.getDecl((NamedNode) t1);
        } else {
            return false;
        }

        return isSubClass(subDecl, superDecl);
    }

    /**
     * Return true iff decl1 corresponds to a class that is the same or a a
     * subclass of the class corresponding to decl2.
     *
     * @param decl1
     *            Declaration of the subclass.
     * @param decl2
     *            Declaration of the superclass.
     * @return true iff decl1 is a subclass of decl2.
     */
    public boolean isSubClass(ObjectDecl decl1, ObjectDecl decl2) {
        ObjectDecl obj = MetaModelLibrary.OBJECT_DECL;
        if (decl2 == obj)
            return true;
        if (decl1 == obj)
            return false;
        do {
            if (decl1 == null)
                return false;
            if (decl2 == null)
                return false;
            if (decl1 == decl2)
                return true;
            decl1 = decl1.getSuperClass();
        } while (decl1 != obj);
        return false;
    }

    /**
     * Return true iff type1 is a superinterface of type2. This means that type2
     * is the same interface as type1, or it is an object that implements
     * interface type1.
     *
     * @param type1
     *            Possible superinterface.
     * @param type2
     *            Possible implementing object.
     * @return true iff type1 is a superinterface of type2.
     */
    public boolean isSuperInterface(TypeNode type1, TypeNode type2) {
        if (!isUserType(type2))
            return false;
        int kind = _typeID.kind(type1);
        if (kind != TypeIdentifier.TYPE_KIND_INTERFACE)
            return false;
        ObjectDecl decl1 = (ObjectDecl) MetaModelDecl.getDecl(type1);
        ObjectDecl decl2 = (ObjectDecl) MetaModelDecl.getDecl(type2);
        return isSuperInterface(decl1, decl2);
    }

    /**
     * Return true iff decl1 is a superinterface of decl2. This means that decl2
     * is the same interface as decl1, or it is an object that implements
     * interface decl1.
     *
     * @param decl1
     *            Possible superinterface.
     * @param decl2
     *            Possible implementing object.
     * @return true iff decl1 is a superinterface of decl2.
     */
    public boolean isSuperInterface(ObjectDecl decl1, ObjectDecl decl2) {
        if (decl1 == decl2)
            return true;
        // Traverse the list of superinterfaces of decl2
        Iterator iter = decl2.getInterfaces().iterator();
        while (iter.hasNext()) {
            ObjectDecl impl = (ObjectDecl) iter.next();
            if (isSuperInterface(decl1, impl))
                return true;
        }
        return false;
    }

    /**
     * Return the unary numeric promotion of a single operand. Integral types
     * are promoted to 'int', other types are left the same.
     *
     * @param type
     *            Type to be promoted.
     * @return The promoted type.
     */
    public TypeNode arithPromoteType(TypeNode type) {
        return (isIntegralType(type) ? IntTypeNode.instance : type);
    }

    /**
     * Return the type obtained by arithmetic promotion of a pair of types
     * (binary operator). The resulting type will be the type obtained when
     * operating the two types with an arithmetic operator, e.g. + or *.
     *
     * @param type1
     *            Type to be promoted.
     * @param type2
     *            Type to be promoted.
     * @return The promoted type.
     */
    public TypeNode arithPromoteType(TypeNode type1, TypeNode type2) {
        int kind1 = _typeID.kind(type1);
        int kind2 = _typeID.kind(type2);

        if ((kind1 == TypeIdentifier.TYPE_KIND_TEMPLATE)
                || (kind2 == TypeIdentifier.TYPE_KIND_TEMPLATE))
            return TemplateTypeNode.instance;

        if ((kind1 == TypeIdentifier.TYPE_KIND_DOUBLE)
                || (kind2 == TypeIdentifier.TYPE_KIND_DOUBLE))
            return DoubleTypeNode.instance;

        if ((kind1 == TypeIdentifier.TYPE_KIND_FLOAT)
                || (kind2 == TypeIdentifier.TYPE_KIND_FLOAT))
            return FloatTypeNode.instance;

        if ((kind1 == TypeIdentifier.TYPE_KIND_LONG)
                || (kind2 == TypeIdentifier.TYPE_KIND_LONG))
            return LongTypeNode.instance;

        return IntTypeNode.instance;
    }

    /**
     * Return true iff the type cast from type src to type dst is valid. There
     * are several situations where the type conversion is not possible.
     *
     * @param src
     *            Type before the conversion.
     * @param dst
     *            Type after the conversion.
     * @return true iff the conversion is possible.
     */
    public boolean canCast(TypeNode src, TypeNode dst) {
        if (isTypeParameter(src))
            return true;
        if (isTypeParameter(dst))
            return true;

        if (_isIdentityCast(src, dst))
            return true;
        if (_isWideningCast(src, dst))
            return true;
        if (_isWideningRefCast(src, dst))
            return true;
        if (_isNarrowingCast(src, dst))
            return true;
        if (_isNarrowingRefCast(src, dst))
            return true;

        return false;
    }

    /**
     * Return true iff expression exp can be assigned to a value of the type
     * type without writing a explicit conversion. Return false if the type
     * cannot be assigned OR a explicit conversion is needed.
     *
     * @param exp
     *            Expression to be assigned.
     * @param dst
     *            Type of the variable being assigned.
     * @return true iff the expression can be assigned to a value of that type.
     */
    public boolean canAssign(ExprNode exp, TypeNode dst) {
        TypeNode src = (TypeNode) exp.getProperty(TYPE_KEY);

        if (isTypeParameter(src))
            return true;
        if (isTypeParameter(dst))
            return true;

        if (_isIdentityCast(src, dst))
            return true;
        if (_isWideningCast(src, dst))
            return true;
        if (_isWideningRefCast(src, dst))
            return true;
        if (_isNarrowingConstantCast(exp, dst))
            return true;

        return false;
    }

    /**
     * Return true iff an expression with type src can be passed as a parameter
     * on a method whose type for that formal parameter is dst.
     *
     * @param src
     *            Type of the expression used as parameter.
     * @param dst
     *            Type of the formal
     * @return true iff the type can be used as a parameter, given the type of
     *         this formal parameter.
     */
    public boolean canPassParameter(TypeNode src, TypeNode dst) {
        // GY: handle both array and scalar types
        if (isTypeParameter(src) && isTypeParameter(dst)) {
            TypeNode tsrc = src;
            int dsrc = 0;
            while (tsrc instanceof ArrayTypeNode) {
                tsrc = ((ArrayTypeNode) tsrc).getBaseType();
                dsrc++;
            }

            TypeNode tdst = dst;
            int ddst = 0;
            while (tdst instanceof ArrayTypeNode) {
                tdst = ((ArrayTypeNode) tdst).getBaseType();
                ddst++;
            }

            if (dsrc == ddst)
                return true;
            else
                return false;
        }
        // GY

        if (isTypeParameter(src))
            return true;
        if (isTypeParameter(dst))
            return true;

        if (_isIdentityCast(src, dst))
            return true;
        if (_isWideningCast(src, dst))
            return true;
        if (_isWideningRefCast(src, dst))
            return true;

        return false;
    }

    /**
     * Return true iff both methods have the same signature (i.e. same name,
     * same number of parameters and same type of the parameters).
     *
     * @param method1
     *            Declaration of first method.
     * @param method2
     *            Declaration of second method.
     * @return true iff both methods have the same signature.
     */
    public boolean haveSameSignature(MethodDecl method1, MethodDecl method2) {
        String name1 = method1.getName();
        String name2 = method2.getName();
        if (!name1.equals(name2))
            return false;

        List pars1 = method1.getParams();
        List pars2 = method2.getParams();
        if (pars1.size() != pars2.size())
            return false;

        Iterator iter1 = pars1.iterator();
        Iterator iter2 = pars2.iterator();
        while (iter1.hasNext()) {
            FormalParameterDecl par1 = (FormalParameterDecl) iter1.next();
            FormalParameterDecl par2 = (FormalParameterDecl) iter2.next();
            TypeNode type1 = par1.getType();
            TypeNode type2 = par2.getType();
            if (!areEqual(type1, type2))
                return false;
        }
        return true;
    }

    /**
     * Return true if both methods have the same return type.
     *
     * @param method1
     *            Declaration of first method.
     * @param method2
     *            Declaration of first method.
     * @return true iff both methods have the same return type.
     */
    public boolean haveSameReturnType(MethodDecl method1, MethodDecl method2) {
        TypeNode retType1 = method1.getType();
        TypeNode retType2 = method2.getType();

        return areEqual(retType1, retType2);
    }

    /**
     * Return a string representation of a declaration.
     *
     * @param decl
     *            Declaration being printed.
     * @param kind
     *            True if we want the kind of object to be printed.
     * @return A String representation of a type.
     */
    public String toString(MetaModelDecl decl, boolean kind) {
        String name = (decl.category == CG_TEMPLATE ? decl.getName() : decl
                .fullName());
        if (!kind)
            return name;
        String theKind = null;
        switch (decl.category) {
        case CG_TEMPLATE:
            theKind = "type parameter";
            break;
        case CG_PACKAGE:
            theKind = "package ";
            break;
        case CG_CLASS:
            theKind = "class ";
            break;
        case CG_INTERFACE:
            theKind = "interface ";
            break;
        case CG_NETLIST:
            theKind = "netlist ";
            break;
        case CG_PROCESS:
            theKind = "process ";
            break;
        case CG_MEDIUM:
            theKind = "medium ";
            break;
        case CG_SCHEDULER:
            theKind = "scheduler ";
            break;
        case CG_SM:
            theKind = "state medium ";
            break;
        case CG_QUANTITY:
            theKind = "quantity ";
            break;
        }
        return theKind + name;
    }

    /**
     * Return a string representation of a type name.
     *
     * @param type
     *            TypeNameNode being printed.
     * @param kind
     *            True if we want the kind of object to be printed.
     * @return A String representation of a type.
     */
    public String toString(TypeNameNode type, boolean kind) {
        MetaModelDecl decl = MetaModelDecl.getDecl(type.getName());
        return toString(decl, kind);
    }

    /**
     * Return a string representation of a type.
     *
     * @param type
     *            Type being printed.
     * @param kind
     *            True if we want the kind of object to be printed.
     * @return A String representation of a type.
     */
    public String toString(TypeNode type, boolean kind) {
        if (type instanceof TypeNameNode) {
            return toString((TypeNameNode) type, kind);
        }
        if (type instanceof ArrayTypeNode) {
            ArrayTypeNode array = (ArrayTypeNode) type;
            return toString(array.getBaseType(), kind) + "[]";
        }
        if (type == BoolTypeNode.instance)
            return "boolean";
        if (type == IntTypeNode.instance)
            return "int";
        if (type == ByteTypeNode.instance)
            return "byte";
        if (type == CharTypeNode.instance)
            return "char";
        if (type == ShortTypeNode.instance)
            return "short";
        if (type == LongTypeNode.instance)
            return "long";
        if (type == FloatTypeNode.instance)
            return "float";
        if (type == DoubleTypeNode.instance)
            return "double";
        if (type == NullTypeNode.instance)
            return "null";
        if (type == VoidTypeNode.instance)
            return "void";
        if (type == EventTypeNode.instance)
            return "event";
        if (type == TemplateTypeNode.instance)
            return "type parameter";
        return "<unknown>";
    }

    /**
     * Return a string representation of a method.
     *
     * @param decl
     *            Declaration of the method to be printed.
     * @param signature
     *            True if we only want to print the method signature.
     * @return A String representing the method declaration.
     */
    public String toString(MethodDecl decl, boolean signature) {
        String text = "";
        // Print return type
        if (!signature) {
            text = text + Effect.toString(decl.getEffect());
            text = text + toString(decl.getType(), false) + " ";
        }
        // Print method name
        text = text + decl.getName() + "(";

        // Print type of the parameters
        Iterator formals = decl.getParams().iterator();
        FormalParameterDecl formal = null;
        while (formals.hasNext()) {
            if (formal != null)
                text = text + ", ";
            formal = (FormalParameterDecl) formals.next();
            text = text + toString(formal.getType(), false);
        }
        text = text + ")";
        return text;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Mapping between types and integer ids used in the policy. */
    protected final TypeIdentifier _typeID;

    /**
     * Array of arithmetic types, i.e. types that support arithmetic operators
     * like '+' or '-'.
     */
    protected final TypeNode[] _ARITH_TYPES = new TypeNode[] {
            ByteTypeNode.instance, ShortTypeNode.instance,
            CharTypeNode.instance, IntTypeNode.instance, LongTypeNode.instance,
            FloatTypeNode.instance, DoubleTypeNode.instance };

    /** Array of floating point types. */
    protected static final TypeNode[] _FLOAT_TYPES = new TypeNode[] {
            FloatTypeNode.instance, DoubleTypeNode.instance };

    /** Array of integral types. */
    protected static final TypeNode[] _INTEGRAL_TYPES = new TypeNode[] {
            ByteTypeNode.instance, ShortTypeNode.instance,
            CharTypeNode.instance, IntTypeNode.instance, LongTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from byte type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_BOOL = new TypeNode[] {};

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from byte type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_BOOL = new TypeNode[] {};

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from byte type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_BYTE = new TypeNode[] {
            ShortTypeNode.instance, IntTypeNode.instance,
            LongTypeNode.instance, FloatTypeNode.instance,
            DoubleTypeNode.instance };

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from byte type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_BYTE = new TypeNode[] { CharTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from char type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_CHAR = new TypeNode[] {
            IntTypeNode.instance, LongTypeNode.instance,
            FloatTypeNode.instance, DoubleTypeNode.instance };

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from char type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_CHAR = new TypeNode[] {};

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from short type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_SHORT = new TypeNode[] {
            IntTypeNode.instance, LongTypeNode.instance,
            FloatTypeNode.instance, DoubleTypeNode.instance };

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from short type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_SHORT = new TypeNode[] {
            ByteTypeNode.instance, CharTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from int type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_INT = new TypeNode[] {
            LongTypeNode.instance, FloatTypeNode.instance,
            DoubleTypeNode.instance };

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from int type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_INT = new TypeNode[] {
            ByteTypeNode.instance, CharTypeNode.instance,
            ShortTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from long type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_LONG = new TypeNode[] {
            FloatTypeNode.instance, DoubleTypeNode.instance };

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from long type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_LONG = new TypeNode[] {
            ByteTypeNode.instance, CharTypeNode.instance,
            ShortTypeNode.instance, IntTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from float type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_FLOAT = new TypeNode[] { DoubleTypeNode.instance };

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from float type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_FLOAT = new TypeNode[] {
            ByteTypeNode.instance, CharTypeNode.instance,
            ShortTypeNode.instance, IntTypeNode.instance, LongTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from double type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_DOUBLE = new TypeNode[] {};

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from double type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_DOUBLE = new TypeNode[] {
            ByteTypeNode.instance, CharTypeNode.instance,
            ShortTypeNode.instance, IntTypeNode.instance,
            LongTypeNode.instance, FloatTypeNode.instance };

    /**
     * Array of primitive types that can be widened (extended with additional
     * precision) from pcval type.
     */
    protected static final TypeNode[] _TYPES_WIDENING_FROM_PCVAL = new TypeNode[] {};

    /**
     * Array of primitive types that can be narrowed (represented with less
     * precision) from pcval type.
     */
    protected static final TypeNode[] _TYPES_NARROWING_FROM_PCVAL = new TypeNode[] {};

    /**
     * An uneven matrix of primitive types that may be widened from a primitive
     * type, the kind of which is the first array index.
     */
    protected static final TypeNode[][] _TYPES_WIDENING_FROM = new TypeNode[][] {
            _TYPES_WIDENING_FROM_BOOL, _TYPES_WIDENING_FROM_BYTE,
            _TYPES_WIDENING_FROM_SHORT, _TYPES_WIDENING_FROM_CHAR,
            _TYPES_WIDENING_FROM_INT, _TYPES_WIDENING_FROM_LONG,
            _TYPES_WIDENING_FROM_FLOAT, _TYPES_WIDENING_FROM_DOUBLE,
            _TYPES_WIDENING_FROM_PCVAL };

    /**
     * An uneven matrix of primitive types that may narrowed from a primitive
     * type, the kind of which is the first array index.
     */
    protected static final TypeNode[][] _TYPES_NARROWING_FROM = new TypeNode[][] {
            _TYPES_NARROWING_FROM_BOOL, _TYPES_NARROWING_FROM_BYTE,
            _TYPES_NARROWING_FROM_SHORT, _TYPES_NARROWING_FROM_CHAR,
            _TYPES_NARROWING_FROM_INT, _TYPES_NARROWING_FROM_LONG,
            _TYPES_NARROWING_FROM_FLOAT, _TYPES_NARROWING_FROM_DOUBLE,
            _TYPES_NARROWING_FROM_PCVAL };

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Test if type is one of the types contained in typeArray. The comparison
     * is made between references only, so this only works for primitive types
     * (which are singletons).
     *
     * @param type
     *            Type being looked up.
     * @param typeArray
     *            Array of types.
     * @return true iff the type is contained in typeArray.
     */
    protected static final boolean _isOneOf(TypeNode type, TypeNode[] typeArray) {
        for (int i = 0; i < typeArray.length; i++) {
            if (typeArray[i] == type)
                return true;
        }
        return false;
    }

    /**
     * Return true iff the type declared by this type is declared final.
     *
     * @param type
     *            whose declaration we want to test.
     * @return true iff the type is declared final.
     */
    protected static final boolean _isFinal(TypeNode type) {
        MetaModelDecl decl = MetaModelDecl.getDecl(type);
        if (decl.hasModifiers()) {
            int modifiers = decl.getModifiers();
            if ((modifiers & Modifier.FINAL_MOD) != 0)
                return true;
        }
        return false;
    }

    /**
     * Return true iff both interfaces have a method with the same signature
     * (i.e. same name, same number of parameters and same type of the
     * parameters).
     *
     * @param intf1
     *            Declaration of the first interface.
     * @param intf2
     *            Declaration of the second interface.
     * @return true iff the two declarations have at least one method with the
     *         same signature.
     */
    protected boolean _haveConflictingMethods(InterfaceDecl intf1,
            InterfaceDecl intf2) {
        Iterator iter1 = intf1.getMethods();
        while (iter1.hasNext()) {
            MethodDecl m1 = (MethodDecl) iter1.next();
            Iterator iter2 = intf2.getMethods(m1.getName());
            while (iter2.hasNext()) {
                MethodDecl m2 = (MethodDecl) iter2.next();
                if (haveSameSignature(m1, m2) && (!haveSameReturnType(m1, m2)))
                    return true;
            }
        }
        return false;
    }

    /**
     * Return true iff the conversion specified is the identity conversion (i.e.
     * one type converted to the same type).
     *
     * @param src
     *            Initial type.
     * @param dst
     *            Final type.
     * @return true iff this is a identity conversion.
     */
    protected boolean _isIdentityCast(TypeNode src, TypeNode dst) {
        if (src == dst)
            return true;
        return areEqual(src, dst);
    }

    /**
     * Return true iff the conversion specified is a widening primitive
     * conversion. Widening primitive conversions are those that do not lose
     * information about the overall magnitude of a numeric value; precision may
     * be lost, however, when long are converted to double or float types. For
     * example, int to long.
     *
     * @param src
     *            Initial type.
     * @param dst
     *            Final type.
     * @return true iff the conversion from src to dst is a widening primitive
     *         conversion.
     */
    protected boolean _isWideningCast(TypeNode src, TypeNode dst) {
        if (!(isPrimitiveType(src) && isPrimitiveType(dst)))
            return false;
        int srcKind = _typeID.kind(src);
        return _isOneOf(dst, _TYPES_WIDENING_FROM[srcKind]);
    }

    /**
     * Return true iff the conversion specified is a narrowing primitive
     * conversion. Narrowing primitive types may result in a loss of
     * information, in addition to a loss of precision (float/double); also, the
     * sign of the converted value may differ from the sign of the input value.
     * For example, long to char.
     *
     * @param src
     *            Initial type
     * @param dst
     *            Final type.
     * @return true iff the conversion from src to dst is a narrowing primitive
     *         conversion.
     */
    protected boolean _isNarrowingCast(TypeNode src, TypeNode dst) {
        if (!(isPrimitiveType(src) && isPrimitiveType(dst)))
            return false;
        int srcKind = _typeID.kind(src);
        return _isOneOf(dst, _TYPES_NARROWING_FROM[srcKind]);
    }

    /**
     * Return true iff the conversion specified from type src to type dst is a
     * widening reference conversion, from a reference type to a more general
     * reference type.
     *
     * @param src
     *            Initial type.
     * @param dst
     *            Final type.
     * @return true iff the conversion from src to dst is a widening reference
     *         conversion.
     */
    protected boolean _isWideningRefCast(TypeNode src, TypeNode dst) {
        if (isTypeParameter(src))
            return true;
        if (isTypeParameter(dst))
            return true;

        if (!(isReferenceType(src) && isReferenceType(dst)))
            return false;

        // Null widening
        if ((src == NullTypeNode.instance) || (dst == NullTypeNode.instance))
            return true;

        // Widening from interfaces
        if (isInterfaceType(src)) {
            if (isArrayType(dst))
                return false;
            if (isInterfaceType(dst))
                return isSuperInterface(dst, src);
            if (isUserType(dst))
                return areEqual(dst, MetaModelLibrary.OBJECT_TYPE);
            // Never reached
            return false;
        }

        // Widening from classes
        if (isUserType(src)) {
            if (isInterfaceType(dst))
                return isSuperInterface(dst, src);
            if (isSubClass(src, dst))
                return true;
            return false;
        }

        // Widening from arrays
        if (isArrayType(src)) {
            if (isInterfaceType(dst))
                return false;
            if (isUserType(dst))
                return areEqual(dst, MetaModelLibrary.OBJECT_TYPE);
            if (isArrayType(dst)) {
                ArrayTypeNode at1 = (ArrayTypeNode) src;
                ArrayTypeNode at2 = (ArrayTypeNode) dst;
                TypeNode base1 = at1.getBaseType();
                TypeNode base2 = at2.getBaseType();
                return _isWideningRefCast(base1, base2);
            }
            return false;
        }

        // Never reached
        return false;
    }

    /**
     * Return true iff the conversion specified from type src to type dst is a
     * narrowing reference conversion, from a reference type to a more specific
     * reference type. This tests require a run-time check to verify if they are
     * valid.
     *
     * @param src
     *            Initial type.
     * @param dst
     *            Final type.
     * @return true iff the conversion from src to dst is a narrowing reference
     *         conversion.
     */
    protected boolean _isNarrowingRefCast(TypeNode src, TypeNode dst) {
        if (isTypeParameter(src))
            return true;
        if (isTypeParameter(dst))
            return true;

        if (!(isReferenceType(src) && isReferenceType(dst)))
            return false;

        // No null narrowing
        if ((src == NullTypeNode.instance) || (dst == NullTypeNode.instance))
            return false;

        // Interface narrowing
        if (isInterfaceType(src)) {
            if (isInterfaceType(dst)) {
                if (isSuperInterface(dst, src))
                    return false;
                InterfaceDecl decl1 = (InterfaceDecl) MetaModelDecl
                        .getDecl(src);
                InterfaceDecl decl2 = (InterfaceDecl) MetaModelDecl
                        .getDecl(dst);
                return (!_haveConflictingMethods(decl1, decl2));
            }
            if (isUserType(dst)) {
                if (!_isFinal(dst))
                    return true;
                return isSuperInterface(src, dst);
            }
            return false;
        }

        // Class narrowing
        if (isUserType(src)) {
            if (areEqual(src, MetaModelLibrary.OBJECT_TYPE))
                return (isArrayType(dst) || isInterfaceType(dst));
            if (isInterfaceType(dst))
                return (!_isFinal(src) && !isSuperInterface(dst, src));
            if (isUserType(dst))
                return isSubClass(dst, src);
            return false;
        }

        // Array narrowing
        if (isArrayType(src)) {
            if (!isArrayType(dst))
                return false;
            ArrayTypeNode at1 = (ArrayTypeNode) src;
            ArrayTypeNode at2 = (ArrayTypeNode) dst;
            TypeNode base1 = at1.getBaseType();
            TypeNode base2 = at2.getBaseType();
            return _isNarrowingRefCast(base1, base2);
        }

        // Never reached
        return false;
    }

    /**
     * Return true iff the cast from the source expression to the target type is
     * a narrowing primitive conversion for constants.
     *
     * @param exp
     *            The expression being converted.
     * @param dst
     *            The target type of the conversion.
     * @return true iff the cast is a narrrowing primitive conversion for
     *         constants.
     */
    protected boolean _isNarrowingConstantCast(ExprNode exp, TypeNode dst) {
        // Expression must be a constant
        if (!(exp instanceof LiteralNode))
            return false;

        // Type of constant must be byte, short, char or int
        TypeNode src = (TypeNode) exp.getProperty(TYPE_KEY);
        switch (_typeID.kind(src)) {
        case TypeIdentifier.TYPE_KIND_BYTE:
        case TypeIdentifier.TYPE_KIND_SHORT:
        case TypeIdentifier.TYPE_KIND_CHAR:
        case TypeIdentifier.TYPE_KIND_INT:
            break;
        default:
            return false;
        }

        // Type of target expression must be byte, short or char
        // The value of the expression must be representable in
        // the type of the variable.
        switch (_typeID.kind(dst)) {
        case TypeIdentifier.TYPE_KIND_BYTE:
            return ExprUtility.isIntConstant(exp, Byte.MIN_VALUE,
                    Byte.MAX_VALUE);
        case TypeIdentifier.TYPE_KIND_SHORT:
            return ExprUtility.isIntConstant(exp, Short.MIN_VALUE,
                    Short.MAX_VALUE);
        case TypeIdentifier.TYPE_KIND_CHAR:
            return ExprUtility.isIntConstant(exp, Character.MIN_VALUE,
                    Character.MAX_VALUE);
        }
        return false;
    }

}
