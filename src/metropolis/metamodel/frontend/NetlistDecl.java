/* A declaration of a netlist.

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

import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // NetlistDecl
/**
 * A declaration of a netlist. Netlists cannot have parameters or ports. All
 * netlists inherit from a netlist called 'Netlist', defined in the meta-model
 * library.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: NetlistDecl.java,v 1.13 2006/10/12 20:33:55 cxh Exp $
 */
public class NetlistDecl extends ObjectDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Build a netlist with a given name, defined type, modifiers, source node
     * in the AST and enclosing declaration.
     *
     * @param name
     *            Name of the netlist.
     * @param defType
     *            Type defined by this declaration.
     * @param modifiers
     *            Modifiers of this netlist declaration.
     * @param source
     *            Node in the AST with the declaration.
     * @param container
     *            Enclosing declaration.
     * @param typeParams
     *            Declarations of type parameters of this class.
     */
    public NetlistDecl(String name, TypeNode defType, int modifiers,
            TreeNode source, MetaModelDecl container, List typeParams) {
        super(name, CG_NETLIST, defType, modifiers, source, container,
                typeParams);
    }

}
