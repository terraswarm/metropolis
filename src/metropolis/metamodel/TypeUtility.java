/* Methods dealing with types.

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

import metropolis.metamodel.nodetypes.ArrayTypeNode;
import metropolis.metamodel.nodetypes.TypeNode;

// ////////////////////////////////////////////////////////////////////////
// // TypeUtility
/**
 * Utility methods dealing with array types. Theses methods allow us to create
 * arrays of a given dimension and base type, get the base type of an array or
 * get the number of dimensions of an array.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Titanium project, under funding from the DARPA, DoE, and Army Research
 * Office.
 *
 * @author Jeff Tsay
 * @version $Id: TypeUtility.java,v 1.13 2006/10/12 20:32:09 cxh Exp $
 */
public class TypeUtility implements MetaModelStaticSemanticConstants {

    /**
     * Public constructor allows inheritance of methods although this class has
     * no instance members.
     */
    public TypeUtility() {
    }

    // /** Return the base type of the array type, which is not itself an array
    // * type. If the type is not an array type, return the type.
    // * @param type Array type.
    // * @return The base type of the array, or the type itself if it was not
    // * as array.
    // */
    // public static TypeNode arrayBaseType(TypeNode type) {
    // if (type instanceof ArrayTypeNode) {
    // return arrayBaseType(((ArrayTypeNode) type).getBaseType());
    // }
    // return type;
    // }

    // /** Return the dimension of the array, which is the number of contiguous
    // * bracket pairs required after the base type.
    // * @param type Array type.
    // * @return The number of dimensions of the array (0 for non-array types).
    // */
    // public static int arrayDimension(TypeNode type) {
    // if (type instanceof ArrayTypeNode) {
    // return 1 + arrayDimension(((ArrayTypeNode) type).getBaseType());
    // }
    // return 0;
    // }

    /**
     * Create an array type with given element type and dimensions. If dims is
     * 0, return the element type.
     *
     * @param elementType
     *            Type of each element of the array.
     * @param dims
     *            Number of dimensions of the array
     * @return The array type, of base type elementType, with and dims
     *         dimensions.
     */
    public static TypeNode makeArrayType(TypeNode elementType, int dims) {
        for (int i = 0; i < dims; i++) {
            elementType = new ArrayTypeNode(elementType);
        }
        return elementType;
    }
}
