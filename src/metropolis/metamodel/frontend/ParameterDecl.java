/* A declaration of a parameter of a process, medium or scheduler.

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

import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.TypeNode;

// ////////////////////////////////////////////////////////////////////////
// // ParameterDecl
/**
 * A declaration of a parameter of a process, medium or scheduler. Fields have
 * modifiers, a type, a name, a source AST node and a container class.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: ParameterDecl.java,v 1.11 2006/10/12 20:33:59 cxh Exp $
 */
public class ParameterDecl extends MemberDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a parameter declaration with a given name, type, modifiers, source
     * node in the AST and container class.
     *
     * @param name
     *            Identifier of the parameter.
     * @param type
     *            Type of the parameter.
     * @param modifiers
     *            Modifiers of this parameter declaration.
     * @param source
     *            Source node of the AST.
     * @param container
     *            Declaration of the class where the parameter is defined.
     */
    public ParameterDecl(String name, TypeNode type, int modifiers,
            TreeNode source, MetaModelDecl container) {
        super(name, CG_PARAMETER, type, modifiers, source, container);
    }

}
