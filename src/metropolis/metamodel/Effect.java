/* Helper functions to check/print effects.

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
// // Effect
/**
 * Helper functions used to handle method effects such as 'constant' or 'eval'.
 * This "effects" describe how the state of the system is affected by the
 * execution of methods.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: Effect.java,v 1.18 2006/10/12 20:31:57 cxh Exp $
 */
public class Effect implements MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Return a string representation of a method effect.
     *
     * @param effect
     *            An effect. See {@link MetaModelStaticSemanticConstants} for
     *            permissible values.
     * @return A string with the effect.
     *
     */
    public static final String toString(final int effect) {
        switch (effect) {
        case NO_EFFECT:
            return "";
        case EVAL_EFFECT:
            return "eval ";
        case UPDATE_EFFECT:
            return "update ";
        case CONSTANT_EFFECT:
            return "constant ";
        default:
            // The effect used is invalid.
            throw new RuntimeException("Wrong effect '" + effect
                    + "' of method.");
        }
    }

}
