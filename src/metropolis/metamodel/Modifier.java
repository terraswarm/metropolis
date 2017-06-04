/* Helper functions to check/print modifiers.

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

// ////////////////////////////////////////////////////////////////////////
// // Modifier
/**
 * Helper functions used to handle modifiers such as 'public' or 'private'.
 * These functions are used to check that the modifiers used in each declaration
 * are legal.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: Modifier.java,v 1.23 2006/10/12 20:32:01 cxh Exp $
 */
public class Modifier implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check the modifiers used in a class declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the class uses an invalid modifier, or if the class
     *                declares several visibility modifiers.
     */
    public static void checkClassModifiers(int modifiers) {
        int validModifiers = _validClassModifiers;
        check(modifiers, validModifiers, "class declaration");
    }

    /**
     * Check the modifiers used in a declaration of a constant field inside an
     * interface.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the constant field uses an invalid modifier, or if the
     *                field declares several visibility modifiers.
     */
    public static final void checkConstantFieldModifiers(int modifiers) {
        int validModifiers = _validConstantFieldModifiers;
        check(modifiers, validModifiers, "constant field declaration");
    }

    /**
     * Check the modifiers used in a constructor declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the constructor uses an invalid modifier, or if the
     *                constructor declares several visibility modifiers.
     */
    public static final void checkConstructorModifiers(int modifiers) {
        int validModifiers = _validConstructorModifiers;
        check(modifiers, validModifiers, "constructor declaration");
    }

    /**
     * Check the modifiers used in a field declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the field uses an invalid modifier, or if the field
     *                declares several visibility modifiers.
     */
    public static final void checkFieldModifiers(int modifiers) {
        int validModifiers = _validFieldModifiers;
        check(modifiers, validModifiers, "field declaration");
    }

    /**
     * Check the modifiers used in a declaration of a formal parameter of a
     * method.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the parameter uses an invalid modifier, or if the
     *                parameter declares several visibility modifiers.
     */
    public static final void checkFormalModifiers(int modifiers) {
        int validModifiers = _validFormalModifiers;
        check(modifiers, validModifiers, "formal parameter declaration");
    }

    /**
     * Check the modifiers used in an interface method declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the method uses an invalid modifier, or if the method
     *                declares several visibility modifiers.
     */
    public static final void checkInterfaceMethodModifiers(int modifiers) {
        int validModifiers = _validIfMethodModifiers;
        check(modifiers, validModifiers, "method declaration");
    }

    /**
     * Check the modifiers used in an interface declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the interface uses an invalid modifier, or if the
     *                interface declares several visibility modifiers.
     */
    public static void checkInterfaceModifiers(int modifiers) {
        int validModifiers = _validInterfaceModifiers;
        check(modifiers, validModifiers, "interface declaration");
        if ((modifiers & ABSTRACT_MOD) != 0) {
            System.out.println("Warning: Using 'abstract' modifier in the "
                    + "declaration of an interface.");
            System.out.println("         Interface is abstract by default,"
                    + "use of the modifier is discouraged.");
        }
    }

    /**
     * Check the modifiers used in a local variable declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the local uses an invalid modifier, or if the local
     *                declares several visibility modifiers.
     */
    public static final void checkLocalVarModifiers(int modifiers) {
        int validModifiers = _validLocalVarModifiers;
        check(modifiers, validModifiers, "local variable declaration");
    }

    /**
     * Check the modifiers used in a communication medium declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the communication medium uses an invalid modifier, or
     *                if the communication medium declares several visibility
     *                modifiers.
     */
    public static final void checkMediumModifiers(int modifiers) {
        int validModifiers = _validMediumModifiers;
        check(modifiers, validModifiers, "communication medium declaration");
    }

    /**
     * Check the modifiers used in non-interface method declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the method uses an invalid modifier, or if the method
     *                declares several visibility modifiers.
     */
    public static final void checkMethodModifiers(final int modifiers) {
        int validModifiers = _validMethodModifiers;
        check(modifiers, validModifiers, "method declaration");
    }

    /**
     * Check the modifiers used in a declaration of a method signature inside an
     * interface.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the method uses an invalid modifier, or if the method
     *                declares several visibility modifiers.
     */
    public static final void checkMethodSignatureModifiers(int modifiers) {
        int validModifiers = _validMethodSignatureModifiers;
        check(modifiers, validModifiers, "method signature declaration");
        if ((modifiers & PUBLIC_MOD) != 0) {
            System.out.println("Warning: Using 'public' modifier in the "
                    + "declaration of an interface method.");
            System.out.println("         Method is public by default,"
                    + "use of the modifier is discouraged.");
        }
        if ((modifiers & ABSTRACT_MOD) != 0) {
            System.out.println("Warning: Using 'abstract' modifier in the "
                    + "declaration of an interface method.");
            System.out.println("         Method is abstract by default,"
                    + "use of the modifier is discouraged.");
        }
    }

    /**
     * Check the modifiers used in a netlist declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the netlist uses an invalid modifier, or if the netlist
     *                declares several visibility modifiers.
     */
    public static final void checkNetlistModifiers(int modifiers) {
        int validModifiers = _validNetlistModifiers;
        check(modifiers, validModifiers, "netlist declaration");
    }

    /**
     * Check the modifiers used in class parameter declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the parameter uses an invalid modifier, or if the
     *                parameter declares several visibility modifiers.
     */
    public static final void checkParameterModifiers(int modifiers) {
        int validModifiers = _validParameterModifiers;
        check(modifiers, validModifiers, "class parameter declaration");
    }

    /**
     * Check the modifiers used in a port declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the port uses an invalid modifier, or if the port
     *                declares several visibility modifiers.
     */
    public static final void checkPortModifiers(int modifiers) {
        int validModifiers = _validPortModifiers;
        check(modifiers, validModifiers, "port declaration");
    }

    /**
     * Check the modifiers used in a process declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the process uses an invalid modifier, or if the process
     *                declares several visibility modifiers.
     */
    public static final void checkProcessModifiers(int modifiers) {
        int validModifiers = _validProcessModifiers;
        check(modifiers, validModifiers, "process declaration");
    }

    /**
     * Check the modifiers used in a quantity declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the quantity uses an invalid modifier, or if the
     *                quantity declares several visibility modifiers.
     */
    public static void checkQuantityModifiers(int modifiers) {
        int validModifiers = _validQuantityModifiers;
        check(modifiers, validModifiers, "quantity declaration");
    }

    /**
     * Check the modifiers used in a state medium declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the process uses an invalid modifier, or if the process
     *                declares several visibility modifiers.
     */
    public static final void checkSMModifiers(int modifiers) {
        int validModifiers = _validSMModifiers;
        check(modifiers, validModifiers, "state medium declaration");
    }

    /**
     * Check the modifiers used in a scheduler declaration.
     *
     * @param modifiers
     *            The set of modifiers.
     * @exception RuntimeException
     *                If the scheduler uses an invalid modifier, or if the
     *                scheduler declares several visibility modifiers.
     */
    public static final void checkSchedulerModifiers(int modifiers) {
        int validModifiers = _validSchedulerModifiers;
        check(modifiers, validModifiers, "scheduler declaration");
    }

    /**
     * Return a string representation of a set of modifiers.
     *
     * @param modifier
     *            The bitwise or of the set of modifier
     * @return A string with all the modifiers.
     */
    public static final String toString(final int modifier) {
        StringBuffer modString = new StringBuffer();
        if (modifier == NO_MOD)
            return "";
        if ((modifier & PUBLIC_MOD) != 0)
            modString.append("public ");
        if ((modifier & PROTECTED_MOD) != 0)
            modString.append("protected ");
        if ((modifier & PRIVATE_MOD) != 0)
            modString.append("private ");
        if ((modifier & ABSTRACT_MOD) != 0)
            modString.append("abstract ");
        if ((modifier & FINAL_MOD) != 0)
            modString.append("final ");
        if ((modifier & STATIC_MOD) != 0)
            modString.append("static ");
        if ((modifier & ELABORATE_MOD) != 0)
            modString.append("elaborate ");
        return modString.toString();
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Valid modifiers in a class declaration. */
    protected static final int _validClassModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a quantity declaration. */
    protected static final int _validQuantityModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in an interface declaration. */
    protected static final int _validInterfaceModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a netlist declaration. */
    protected static final int _validNetlistModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a process declaration. */
    protected static final int _validProcessModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a communication medium declaration. */
    protected static final int _validMediumModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a scheduler declaration. */
    protected static final int _validSchedulerModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a state medium declaration. */
    protected static final int _validSMModifiers = PUBLIC_MOD | PROTECTED_MOD
            | PRIVATE_MOD | FINAL_MOD | ABSTRACT_MOD;

    /** Valid modifiers in a declaration of a field in an interface. */
    protected static final int _validConstantFieldModifiers = PUBLIC_MOD
            | FINAL_MOD;

    /** Valid modifiers in a declaration of a method in an interface. */
    protected static final int _validMethodSignatureModifiers = PUBLIC_MOD
            | ABSTRACT_MOD | ELABORATE_MOD;

    /** Valid modifiers in a field declaration. */
    protected static final int _validFieldModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | FINAL_MOD | STATIC_MOD;

    /** Valid modifiers in a parameter declaration. */
    protected static final int _validParameterModifiers = PRIVATE_MOD
            | FINAL_MOD;

    /** Valid modifiers in a port declaration. */
    protected static final int _validPortModifiers = PUBLIC_MOD;

    /** Valid modifiers in an interface method declaration. */
    protected static final int _validIfMethodModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | ABSTRACT_MOD | FINAL_MOD
            | STATIC_MOD | ELABORATE_MOD;

    /** Valid modifiers in a method declaration. */
    protected static final int _validMethodModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD | ABSTRACT_MOD | FINAL_MOD
            | STATIC_MOD | ELABORATE_MOD;

    /** Valid modifiers in a constructor declaration. */
    protected static final int _validConstructorModifiers = PUBLIC_MOD
            | PROTECTED_MOD | PRIVATE_MOD;

    /** Valid modifiers in a local variable declaration. */
    protected static final int _validLocalVarModifiers = FINAL_MOD;

    /** Valid modifiers in a declaration of a parameter of a method. */
    protected static final int _validFormalModifiers = FINAL_MOD;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Check that the modifiers are correct. There are three checks to perform:
     * first, that all modifiers used in a declaration are valid in that
     * declaration; second that there is only one v visibility modifier in the
     * declaration; and third, that there aren't invalid combinations of
     * modifiers.
     *
     * @param modifiers
     *            The set of used modifiers.
     * @param valids
     *            The set of valid modifiers.
     * @param where
     *            Short description of the kind of declaration.
     * @exception RuntimeException
     *                If the declaration uses an invalid modifier, or if the
     *                declaration declares uses visibility modifiers.
     */
    protected static final void check(int modifiers, int valids, String where) {
        // Check validity in the declaration
        if ((modifiers & (~valids)) != 0) {
            throw new RuntimeException("Illegal modifier in  " + where + " : "
                    + toString(modifiers));
        }
        // Check visibility modifiers
        int visibility = 0;
        if ((modifiers & PUBLIC_MOD) != 0)
            visibility++;
        if ((modifiers & PROTECTED_MOD) != 0)
            visibility++;
        if ((modifiers & PRIVATE_MOD) != 0)
            visibility++;
        if (visibility > 1) {
            throw new RuntimeException("Different visibilities defined in "
                    + where + ":" + toString(modifiers));
        }
        // Check invalid modifier combinations
        if ((modifiers & ABSTRACT_MOD) != 0) {
            if ((modifiers & STATIC_MOD) != 0) {
                throw new RuntimeException("Error in " + where + " : "
                        + "cannot use static and abstract simultaneously");
            }
            if ((modifiers & PRIVATE_MOD) != 0) {
                throw new RuntimeException("Error in " + where + " : "
                        + "cannot use private and abstract simultaneously");
            }
            if ((modifiers & FINAL_MOD) != 0) {
                throw new RuntimeException("Error in " + where + " : "
                        + "cannot use final and abstract simultaneously");
            }
        }
    }

}
