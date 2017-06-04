/* A visitor that visits object class declarations checking inheritance.

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
import metropolis.metamodel.MetaModelVisitor;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.Scope;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NameNode;
import metropolis.metamodel.nodetypes.NamedNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// ////////////////////////////////////////////////////////////////////////
// // ResolveInheritanceVisitor
/**
 * A visitor that adds inherited class and interface members to the respective
 * scopes. Additional checks are performed to make sure that inheritance of the
 * classes is semantically correct:
 * <ul>
 * <li> a class cannot declare two methods with the same signature
 * <li> a non-abstract class must implement all abstract methods, and all
 * methods specified in implemented interfaces
 * </ul>
 * <p>
 * This visitor will not return any value in its traversal, so null can be
 * returned safely in each node. Also, parameter list will not be used, at all,
 * so we can pass null as the list of arguments safely.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ResolveInheritanceVisitor.java,v 1.26 2004/10/19 08:21:21
 *          guyang Exp $
 */
public class ResolveInheritanceVisitor extends MetaModelVisitor implements
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Creates a new visitor that will traverse object declarations checking
     * inheritance. Set the default visit method to TM_CUSTOM;
     */
    public ResolveInheritanceVisitor() {
        super(TM_CUSTOM);
        _typePolicy = new TypePolicy();
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Visit a compile unit of a meta-model program. Traverse all the
     * user-defined types in this file checking inheritance, and filling the
     * class scope with inherited fields, methods, ports, etc.
     *
     * @param node
     *            The compile unit being visited
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        // Traverse classes of the compile unit
        TNLManip.traverseList(this, null, node.getDefTypes());
        return null;
    }

    // Visit all kinds of user-defined types, checking inheritance

    /**
     * Visit the declaration of an interface. Add all fields and methods from
     * superinterfaces to the scope of the interface. Check that the interface
     * does not declare or inherit several methods with the same signature (same
     * name and number of parameters), and check method overriding and hiding.
     *
     * @param node
     *            Interface declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a class. Add all fields and methods from
     * superinterfaces and the superclass to the scope of the class. Check that
     * the class does not declare or inherit several methods with the same
     * signature (same name and number of parameters), and check method
     * overriding and hiding.
     *
     * @param node
     *            Class declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a quantity. Add all fields and methods from
     * superinterfaces and the superclass to the scope of the quantity. Check
     * that the quantity does not declare or inherit several methods with the
     * same signature (same name and number of parameters), and check method
     * overriding and hiding.
     *
     * @param node
     *            Quantity declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitQuantityDeclNode(QuantityDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a process. Add all fields, methods, ports and
     * parameters from superinterfaces and the superclass to the scope of the
     * process. Check that the process does not declare or inherit several
     * methods with the same signature (same name and number of parameters), and
     * check method overriding and hiding.
     *
     * @param node
     *            Process declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitProcessDeclNode(ProcessDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a medium. Add all fields, methods, ports and
     * parameters from superinterfaces and the superclass to the scope of the
     * medium. Check that the medium does not declare or inherit several methods
     * with the same signature (same name and number of parameters), and check
     * method overriding and hiding.
     *
     * @param node
     *            Medium declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitMediumDeclNode(MediumDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a netlist. Add all fields and methods from
     * superinterfaces and the superclass to the scope of the netlist. Check
     * that the netlist does not declare or inherit several methods with the
     * same signature (same name and number of parameters), and check method
     * overriding and hiding.
     *
     * @param node
     *            Netlist declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitNetlistDeclNode(NetlistDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a state medium. Add all fields and methods from
     * superinterfaces and the superclass to the scope of the state medium.
     * Check that the state medium does not declare or inherit several methods
     * with the same signature (same name and number of parameters), and check
     * method overriding and hiding.
     *
     * @param node
     *            State medium declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitSMDeclNode(SMDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    /**
     * Visit the declaration of a scheduler. Add all fields, methods and port
     * from superinterfaces and the superclass to the scope of the scheduler.
     * Check that the scheduler does not declare or inherit several methods with
     * the same signature (same name and number of parameters), and check method
     * overriding and hiding.
     *
     * @param node
     *            Scheduler declaration being visited.
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    public Object visitSchedulerDeclNode(SchedulerDeclNode node, LinkedList args) {
        return _visitUserTypeDecl(node);
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Type policy used to compare method signatures. */
    protected TypePolicy _typePolicy;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Default visit method. Ignore all children and return null.
     *
     * @param node
     *            Node of the AST being visited (not used).
     * @param args
     *            List of arguments of the visit (not used).
     * @return null (not used).
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        return null;
    }

    /**
     * Fill in inherited members in a user-defined type, checking that
     * inheritance is correct.
     *
     * @param node
     *            Node where the type is declared.
     * @return Always return null.
     */
    protected Object _visitUserTypeDecl(UserTypeDeclNode node) {
        ObjectDecl thisDecl = (ObjectDecl) MetaModelDecl
                .getDecl((NamedNode) node);

        // Record the fact that we are modifying the Decl.
        // Exit if we have already done inheritance resolution.
        if (!thisDecl.addVisitor(_myClass))
            return null;

        // Check that the interface does not declare two methods with
        // the same signature.
        _checkDeclaredMethods(thisDecl);

        // Get members inherited from the superclass.
        // The same member can be inherited from different superclasses
        // without producing an error. This is why we use a Set for
        // inherited members.
        ObjectDecl superClass = thisDecl.getSuperClass();

        // Get the members inherited from the superclass
        if (superClass != null) {
            // Make sure that the superclass has been resolved so that
            // its inherited members are available
            TreeNode source = superClass.getSource();
            source.accept(this, null);
        }

        Set inheritedMembers = (superClass == null ? new HashSet()
                : _getInheritedMembers(superClass));

        // Get members inherited from the superinterfaces.
        Iterator iter = thisDecl.getInterfaces().iterator();
        while (iter.hasNext()) {
            ObjectDecl intf = (ObjectDecl) iter.next();
            TreeNode source = intf.getSource();
            source.accept(this, null);
            inheritedMembers.addAll(_getInheritedMembers(intf));
        }

        // Check that this type does not inherit two methods
        // with the same signature and different return types
        // or effects.
        _checkInheritedMembers(thisDecl, inheritedMembers);

        // Add to the scope of the object those inherited members
        // which are not overriden nor hidden. Check that
        // overriding and hiding is semantically correct.
        _addNonOverridenMembers(thisDecl, inheritedMembers);

        // Check that non-abstract objects do not contain any
        // abstract method, declared or inherited.
        _checkAbstractMethods(thisDecl);

        // Add use-port information to all method declarations,
        // now that all the names of the ports are available.
        _addUsePortInformation(thisDecl);

        // Check that all methods that have an effect are interface
        // methods or they implement a method from an interface
        // that has an effect.
        // This check should only be performed if the language
        // library 'metamodel.lang' is completely loaded.
        if (MetaModelLibrary.PORT_DECL != null)
            _checkEffects(thisDecl);

        // Set the list of labels that the type declares/inherits.
        // Check that there aren't duplicate labels in this object.
        _setLabels(thisDecl);

        // Traverse inner classes.
        TNLManip.traverseList(this, null, node.getMembers());

        // FIXME: Code should be added to deal with anonymous classes.

        return null;
    }

    /**
     * Check that an ObjectDecl does not contain two methods with the same
     * signature. It is an error if an object in the meta-model declares two
     * methods like this, although an object can inherit two methods with the
     * same signature.
     *
     * @param decl
     *            Declaration of the object.
     * @exception RuntimeException
     *                if the object declares two methods with the same
     *                signature.
     */
    protected void _checkDeclaredMethods(ObjectDecl decl) {
        Iterator methods = decl.getMethods();
        while (methods.hasNext()) {
            MethodDecl current = (MethodDecl) methods.next();

            // If the method has one or more formal parameters that are
            // type parameters, postpone the checking until the template
            // is resolved.
            if (_hasTypeParameter(current))
                continue;

            // Traverse the rest of the methods
            Iterator rest = decl.getMethods();
            _forward(rest, current);
            MethodDecl other;
            // Compare method signatures
            while (rest.hasNext()) {
                other = (MethodDecl) rest.next();
                if (_typePolicy.haveSameSignature(current, other)) {
                    String object = _typePolicy.toString(decl, true);
                    String proto = _typePolicy.toString(current, true);
                    throw new RuntimeException(object + " declares two "
                            + " methods with the same signature: " + proto);
                }
            }
        }
    }

    /**
     * Check if a method declaration has one or more formal parameters that are
     * type parameters.
     *
     * @param method
     *            Declaration of a method.
     * @return true if if a method declaration has one or more formal parameters
     *         that are type parameters; otherwise, false.
     */
    protected boolean _hasTypeParameter(MethodDecl method) {
        List pars = method.getParams();
        Iterator iter = pars.iterator();

        while (iter.hasNext()) {
            FormalParameterDecl par = (FormalParameterDecl) iter.next();
            TypeNode type = par.getType();
            if (_typePolicy.isTypeParameter(type))
                return true;
        }

        return false;
    }

    /**
     * Get the set of members that is inherited from an object declaration. For
     * example, private members are not inherited.
     *
     * @param object
     *            Declaration of the superclass/superinterface.
     * @return The set of members that is inherited from the object declaration.
     */
    protected Set _getInheritedMembers(ObjectDecl object) {
        Set members = new HashSet();

        // Inspect all local declarations in this object
        Iterator iter = object.getScope().allLocalDecls();

        while (iter.hasNext()) {
            MetaModelDecl member = (MetaModelDecl) iter.next();

            // Check if this declaration is inheritable
            int inheritableMask = (CG_FIELD | CG_METHOD | CG_PORT
                    | CG_PARAMETER | CG_USERTYPE);
            if ((member.category & inheritableMask) == 0)
                continue;

            // Check is this declaration is visible
            int modifiers = member.getModifiers();
            if ((modifiers & PRIVATE_MOD) != 0)
                continue;

            // The member is inherited by the subclass.
            // We still have to check if it is hidden or overriden
            // by other members.
            members.add(member);
        }

        return members;
    }

    /**
     * Check that a set of inherited members does not contain two methods with
     * the same signature and different return types or effects. Several fields,
     * ports of parameters can be inherited with the same name, so they won't be
     * checked.
     *
     * @param object
     *            Declaration of the user-defined type.
     * @param members
     *            Set of inherited members.
     * @exception RuntimeException
     *                if the set of inherited members contains two or more
     *                methods with the same signature and different return types
     *                or effects.
     */
    protected void _checkInheritedMembers(ObjectDecl object, Set members) {
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            MetaModelDecl decl = (MetaModelDecl) iter.next();
            if (!(decl instanceof MethodDecl))
                continue;
            MethodDecl m1 = (MethodDecl) decl;
            Iterator rest = members.iterator();
            _forward(rest, m1);
            while (rest.hasNext()) {
                MetaModelDecl decl2 = (MetaModelDecl) rest.next();
                if (!(decl2 instanceof MethodDecl))
                    continue;
                MethodDecl m2 = (MethodDecl) decl2;
                if (!_typePolicy.haveSameSignature(m1, m2))
                    continue;
                if (_hasTypeParameter(m1) || _hasTypeParameter(m2))
                    continue;
                int effect1 = m1.getEffect();
                int effect2 = m2.getEffect();
                if (effect1 != effect2) {
                    String e1 = Effect.toString(effect1);
                    String e2 = Effect.toString(effect2);
                    String c1 = _typePolicy.toString(m1.getContainer(), true);
                    String c2 = _typePolicy.toString(m2.getContainer(), true);
                    String proto = _typePolicy.toString(m1, true);
                    String obj = _typePolicy.toString(object, true);
                    throw new RuntimeException(obj + " inherits "
                            + "two methods with the same signature - " + proto
                            + " - and different effects: '" + e1 + "' (from "
                            + c1 + ") and '" + e2 + "' (from " + c2 + ")");
                }
                if (!_typePolicy.haveSameReturnType(m1, m2)) {
                    String t1 = _typePolicy.toString(m1.getType(), false);
                    String t2 = _typePolicy.toString(m2.getType(), false);
                    String c1 = _typePolicy.toString(m1.getContainer(), true);
                    String c2 = _typePolicy.toString(m2.getContainer(), true);
                    String proto = _typePolicy.toString(m1, true);
                    String obj = _typePolicy.toString(object, true);
                    throw new RuntimeException(obj + " inherits "
                            + "two methods with the same signature - " + proto
                            + " - and different return types: " + t1
                            + " (from " + c1 + ") and " + t2 + " (from " + c2
                            + ")");
                }
            }
        }
    }

    /**
     * Add to the scope of the object declaration those inherited members that
     * are not hidden or overriden by any member of object. Check that
     * overriding and hiding is done according to Java semantics.
     *
     * @param object
     *            Object being checked.
     * @param members
     *            Set of members inherited by the object.
     * @exception RuntimeException
     *                if overriding is incorrect.
     */
    protected void _addNonOverridenMembers(ObjectDecl object, Set members) {
        // Remove from the set of inherited members those members
        // that are hidden or overriden.
        Iterator iter = members.iterator();
        while (iter.hasNext()) {
            MetaModelDecl member = (MetaModelDecl) iter.next();
            if (_isOverriden(object, member))
                iter.remove();
        }

        // Check for inherited methods that are overriding methods
        // inherited from an interface.
        LinkedList inherited = new LinkedList();
        iter = members.iterator();
        while (iter.hasNext()) {
            MetaModelDecl member = (MetaModelDecl) iter.next();
            if (member.category != CG_METHOD)
                continue;
            if (member.getContainer().category == CG_INTERFACE)
                continue;
            inherited.add(member);
        }
        iter = members.iterator();
        while (iter.hasNext()) {
            MetaModelDecl member = (MetaModelDecl) iter.next();
            if (member.category != CG_METHOD)
                continue;
            if (member.getContainer().category != CG_INTERFACE)
                continue;
            MethodDecl m1 = (MethodDecl) member;
            Iterator iter2 = inherited.iterator();
            while (iter2.hasNext()) {
                MethodDecl m2 = (MethodDecl) iter2.next();
                if (_typePolicy.haveSameSignature(m1, m2)) {
                    _checkValidOverriding(object, m1, m2);
                    iter.remove();
                }
            }
        }

        // Add to the scope of the class the elements that remain
        // in the set of inherited members, i.e. those which have
        // not been overriden nor hidden.
        Scope scope = object.getScope();
        iter = members.iterator();
        while (iter.hasNext()) {
            MetaModelDecl member = (MetaModelDecl) iter.next();
            scope.add(member);
        }
    }

    /**
     * Return true if the inherited declaration 'member' is hidden or overriden
     * by any declaration in 'object'.
     *
     * @param object
     *            Declaration of the object that inherits the member.
     * @param member
     *            Declaration of the member of the object.
     * @return true iff the member is overridden or hidden by any declaration of
     *         the object.
     */
    protected boolean _isOverriden(ObjectDecl object, MetaModelDecl member) {
        String memberName = member.getName();
        switch (member.category) {
        case CG_FIELD:
        case CG_PORT:
        case CG_PARAMETER:
            // Fieds, ports and parameters hide fields, ports and
            // parameters from outer scopes.
            MemberDecl attrib = object.getAttribute(memberName);
            return (attrib != null);
        case CG_METHOD:
            // Methods override inherited methods if they have the same
            // signature. Some overridings (e.g. same signature but
            // different return type) cause an error.
            MethodDecl m2 = (MethodDecl) member;
            Iterator iter = object.getMethods(memberName);
            while (iter.hasNext()) {
                MethodDecl m1 = (MethodDecl) iter.next();
                if (!_typePolicy.haveSameSignature(m1, m2))
                    continue;
                _checkValidOverriding(object, m2, m1);
                return true;
            }
            return false;
        case CG_CLASS:
        case CG_INTERFACE:
            // Inner classes hide those in outer scope.
            ObjectDecl innerClass = object.getInnerClass(memberName);
            return (innerClass != null);
        default:
            throw new RuntimeException("Wrong kind of member");
        }
    }

    /**
     * Check that the overriding of an inherited method by a declared method is
     * correct according to meta-model semantics. If the declared method is
     * OVERRIDING (not hiding) the inherited method, update the information
     * available in the MethodDecl about overriding.
     *
     * @param object
     *            Object that declares one method and inherits the other.
     * @param inherited
     *            Declaration of the inherited method.
     * @param declared
     *            Declaration of the method declared in the object.
     * @exception RuntimeException
     *                if the overriding of the method is not correct.
     */
    protected void _checkValidOverriding(ObjectDecl object,
            MethodDecl inherited, MethodDecl declared) {

        // Check that the inherited method is not 'final'.
        int inheritedMods = inherited.getModifiers();
        if ((inheritedMods & FINAL_MOD) != 0) {
            String obj = _typePolicy.toString(object, true);
            String sup = _typePolicy.toString(inherited.getContainer(), true);
            String proto = _typePolicy.toString(declared, true);
            throw new RuntimeException(obj + " tries to override a final "
                    + "method " + proto + " inherited from " + sup);
        }

        // Check that the declared method is not static, with the
        // inherited method being static. If that is the case, cause
        // an error.
        int declaredMods = declared.getModifiers();
        if (((declaredMods & STATIC_MOD) != 0)
                && ((inheritedMods & STATIC_MOD) == 0)) {
            String obj = _typePolicy.toString(object, true);
            String sup = _typePolicy.toString(inherited.getContainer(), true);
            String proto = _typePolicy.toString(declared, true);
            throw new RuntimeException(obj + " tries to override a "
                    + "non-static method - " + proto + " - inherited from "
                    + sup + " with a static method with the same signature");
        }

        // Check that both methods have the same effect.
        int effect1 = inherited.getEffect();
        int effect2 = declared.getEffect();
        if (effect1 != effect2) {
            String e1 = "'" + Effect.toString(effect1) + "'";
            String e2 = "'" + Effect.toString(effect2) + "'";
            String obj = _typePolicy.toString(object, true);
            String sup = _typePolicy.toString(inherited.getContainer(), true);
            String proto = _typePolicy.toString(declared, true);
            throw new RuntimeException(obj + " tries to override method - "
                    + proto + " - from " + sup + ", that has effect " + e1
                    + " with a method with effect " + e2);
        }

        // Check that both methods have the same return type.
        if (!_typePolicy.haveSameReturnType(inherited, declared)) {
            String t1 = _typePolicy.toString(inherited.getType(), false);
            String t2 = _typePolicy.toString(declared.getType(), false);
            String obj = _typePolicy.toString(object, true);
            String sup = _typePolicy.toString(inherited.getContainer(), true);
            String proto = _typePolicy.toString(declared, true);
            throw new RuntimeException(obj + " tries to override method "
                    + proto + " from " + sup + ", with return type " + t1
                    + " with a method with return type " + t2);
        }

        // Check that the declared method provides at least as much
        // access as the method that we try to override.
        boolean declPublic = ((declaredMods & PUBLIC_MOD) != 0);
        boolean declProtected = ((declaredMods & PROTECTED_MOD) != 0);
        boolean declDefault = ((declaredMods & PRIVATE_MOD) == 0)
                && !declPublic && !declProtected;
        boolean inhPublic = ((inheritedMods & PUBLIC_MOD) != 0);
        boolean inhProtected = ((inheritedMods & PROTECTED_MOD) != 0);
        //boolean inhDefault = ((inheritedMods & PRIVATE_MOD) == 0)
        //        && !declPublic && !declProtected;
        boolean lessVisibility = !(declPublic
                || (declProtected && (!inhPublic)) || (declDefault && !(inhPublic || inhProtected)));
        if (lessVisibility) {
            int visibilityMask = PUBLIC_MOD | PROTECTED_MOD | PRIVATE_MOD;
            int mod1 = declaredMods & visibilityMask;
            int mod2 = inheritedMods & visibilityMask;
            String vis1 = (mod1 == 0 ? "default" : Modifier.toString(mod1));
            String vis2 = (mod2 == 0 ? "default" : Modifier.toString(mod2));
            String obj = _typePolicy.toString(object, true);
            String sup = _typePolicy.toString(inherited.getContainer(), true);
            String proto = _typePolicy.toString(declared, true);
            throw new RuntimeException(obj + " tries to override method "
                    + proto + " from " + sup + ", with visibility '" + vis2
                    + "', using a method with less visibility ('" + vis1 + "')");
        }

        // If the method has been overriden, and not hidden, keep
        // that information in the MethodDecl.
        boolean overriden = ((declaredMods & STATIC_MOD) == 0);
        if (overriden) {
            MetaModelDecl container = inherited.getContainer();
            if (container.category == CG_INTERFACE) {
                // 'inherited' is an interface method being
                // implemented by method 'declared'
                declared.addImplement(inherited);
            } else {
                // 'inherited' is a method of a class or a node
                // that is overrided by method 'declared'
                declared.setOverrides(inherited);
                inherited.addOverrider(declared);
            }
        }
    }

    /**
     * Check that an object, if it is non-abstract, does not contain any
     * abstract methods declared or inherited.
     *
     * @param object
     *            Declaration of the object being checked.
     * @exception RuntimeException
     *                if the object is not abstract and contains any abstract
     *                methods.
     */
    protected void _checkAbstractMethods(ObjectDecl object) {
        if ((object.getModifiers() & ABSTRACT_MOD) != 0)
            return;

        MethodDecl method = _hasAbstractMethods(object);
        if (method != null) {
            ObjectDecl where = (ObjectDecl) method.getContainer();
            String proto = _typePolicy.toString(method, true);
            String obj = _typePolicy.toString(object, true);
            if (where == object) {
                throw new RuntimeException("non-abstract " + obj
                        + " cannot declare an abstract method like " + proto);
            } else {
                String obj2 = _typePolicy.toString(where, true);
                throw new RuntimeException("non-abstract " + obj
                        + " inherits abstract method " + proto + " from "
                        + obj2 + " but it does not override it");
            }
        }
    }

    /**
     * Return an abstract method inside a declaration of an object. If these
     * object does not have any abstract methods, return null.
     *
     * @param object
     *            Object declaration where the abstract method should be found.
     * @return The declaration of an abstract method declared or inherited (and
     *         not overriden) by the object, or null if the only does not have
     *         abstract methods.
     */
    protected MethodDecl _hasAbstractMethods(ObjectDecl object) {
        Iterator methods = object.getMethods();
        while (methods.hasNext()) {
            MethodDecl method = (MethodDecl) methods.next();
            int modifiers = method.getModifiers();
            if ((modifiers & ABSTRACT_MOD) != 0)
                return method;
        }
        return null;
    }

    /**
     * Add the list of PortDecls referenced in the useport list in each method
     * to the MethodDecl of the method.
     *
     * @param object
     *            Declaration of the object
     * @exception RuntimeException
     *                if any of the names in the list of useports is not a name
     *                of a port of the object.
     */
    protected void _addUsePortInformation(ObjectDecl object) {
        Iterator methods = object.getMethods();
        while (methods.hasNext()) {
            MethodDecl method = (MethodDecl) methods.next();
            if (method.category == CG_CONSTRUCTOR)
                continue;
            if (method.category == CG_CNSTFORMULA)
                continue;
            // Add useport information for this method
            MethodDeclNode node = (MethodDeclNode) method.getSource();
            Iterator usePorts = node.getUsePorts().iterator();
            while (usePorts.hasNext()) {
                NameNode name = (NameNode) usePorts.next();
                String ident = name.getIdent();
                PortDecl port = object.getPort(ident);
                if (port != null) {
                    method.addUsePort(port);
                } else {
                    String obj = _typePolicy.toString(object, true);
                    String proto = _typePolicy.toString(method, false);
                    throw new RuntimeException("In " + obj + ", method "
                            + proto + " uses a name (" + ident
                            + ") in its list"
                            + " of useports which does not match with the"
                            + " name of any port of the object");
                }
            }
        }
    }

    /**
     * Check that if a method of an object has an effect, one of two things
     * happen: either the object is an interface, or the method implements a
     * method from a Port interface.
     *
     * @param object
     *            Object declaration being checked.
     * @exception RuntimeException
     *                if the object declares a method with an effect and it is
     *                not an interface method, nor it implements an interface
     *                method.
     */
    protected void _checkEffects(ObjectDecl object) {
        // Check all methods in the object
        Iterator iter = object.getMethods();
        while (iter.hasNext()) {
            MethodDecl method = (MethodDecl) iter.next();
            boolean mustHaveEffect = false;
            boolean cannotHaveEffect = false;
            ObjectDecl container = (ObjectDecl) method.getContainer();
            if (container.category == CG_INTERFACE) {
                mustHaveEffect = container.isPortInterface();
                cannotHaveEffect = !mustHaveEffect;
            }
            boolean hasEffect = (method.getEffect() != NO_EFFECT);
            if (hasEffect && cannotHaveEffect) {
                throw new RuntimeException(_typePolicy.toString(object, true)
                        + " declares a method - "
                        + _typePolicy.toString(method, true)
                        + " - with effect '"
                        + Effect.toString(method.getEffect()) + "', but it is "
                        + "not a Port interface, so it shouldn't have effect");
            } else if (!hasEffect && mustHaveEffect) {
                throw new RuntimeException(_typePolicy.toString(object, true)
                        + " declares a method - "
                        + _typePolicy.toString(method, true)
                        + " - without effect"
                        + ", but it is a Port interface, so it should have "
                        + "an effect");
            } else if (hasEffect && !method.isInterfaceMethod()) {
                throw new RuntimeException(_typePolicy.toString(object, true)
                        + " declares a method - "
                        + _typePolicy.toString(method, true)
                        + " - with effect '"
                        + Effect.toString(method.getEffect()) + "', but this "
                        + "method does not implement an interface method,"
                        + " so it shouldn't have effect");
            }
        }
    }

    /**
     * Set the label declarations that are declared/inherited in a given object.
     * Check that there are no duplicate labels.
     *
     * @param object
     *            The declaration of the current object.
     * @exception RuntimeException
     *                if this object declares/inherits two labels with the given
     *                name.
     */
    protected void _setLabels(ObjectDecl object) {
        // Get a list with all the methods before adding labels
        // We cannot add labels to the scope of the object while
        // we traverse the scope looking for more methods.
        Iterator methods = object.getConstructors();
        List allMethods = new LinkedList();
        while (methods.hasNext()) {
            MethodDecl method = (MethodDecl) methods.next();
            allMethods.add(method);
        }
        methods = object.getMethods();
        while (methods.hasNext()) {
            MethodDecl method = (MethodDecl) methods.next();
            allMethods.add(method);
        }
        // Add labels of all methods in the object
        methods = allMethods.iterator();
        while (methods.hasNext()) {
            MethodDecl method = (MethodDecl) methods.next();
            _addLabels(method, object);
        }
    }

    /**
     * Add the labels of a given method to the scope of the class. Check that
     * there are no duplicate labels.
     *
     * @param method
     *            Declaration of the method whose labels are added to the class.
     * @param object
     *            Declaration of the object.
     * @exception RuntimeException
     *                if the object/declares inherits the same label twice.
     */
    protected void _addLabels(MethodDecl method, ObjectDecl object) {
        Iterator labels = method.allLabelDecls();
        while (labels.hasNext()) {
            StmtLblDecl label = (StmtLblDecl) labels.next();
            // Check that the label is unique
            StmtLblDecl label2 = object.getLabel(label.getName());
            if (label2 != null) {
                // Print error message
                MethodDecl method1 = method;
                MethodDecl method2 = label2.getMethod();
                ObjectDecl super1 = (ObjectDecl) method1.getContainer();
                ObjectDecl super2 = (ObjectDecl) method2.getContainer();
                String error = "Error in " + _typePolicy.toString(object, true)
                        + ", label '" + label.getName()
                        + "' declared/inherited" + " twice. ";
                String m1 = "First it is defined in method "
                        + _typePolicy.toString(method1, true);
                if (super1 != object) {
                    m1 = m1 + " inherited from "
                            + _typePolicy.toString(super1, true);
                } else {
                    m1 = m1 + ", declared in this object";
                }
                String m2 = ". Then it is defined in method "
                        + _typePolicy.toString(method2, true);
                if (super2 != object) {
                    m2 = m2 + " inherited from "
                            + _typePolicy.toString(super2, true);
                } else {
                    m2 = m2 + ", declared in this object";
                }
                throw new RuntimeException(error + m1 + m2);
            }
            // Add label to Scope
            object.getScope().add(label);
        }
    }

    /**
     * Advance an iterator so that the last item generated is exactly 'current'.
     * It is assumed that current is a member that can be generated by the
     * iterator.
     *
     * @param iter
     *            Iterator to be advanced.
     * @param current
     *            Last item to be generated.
     */
    protected void _forward(Iterator iter, Object current) {
        Object other;
        do {
            other = iter.next();
        } while (other != current);
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /** The Class object for this visitor. */
    private static Class _myClass = new ResolveInheritanceVisitor().getClass();

}
