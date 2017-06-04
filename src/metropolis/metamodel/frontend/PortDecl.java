/* A declaration of a port in a process, medium or scheduler.

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
// // PortDecl
/**
 * A declaration of a port of a Node. Port have modifiers, a type, a name, a
 * source AST node and a container class. The type of the port must be the type
 * of an interface, and the container class must be a process, medium or
 * scheduler.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: PortDecl.java,v 1.11 2006/10/12 20:33:45 cxh Exp $
 */
public class PortDecl extends MemberDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a port declaration with a given name, type, modifiers, source node
     * in the AST and container Node.
     *
     * @param name
     *            Identifier of the port.
     * @param type
     *            Type of the port.
     * @param modifiers
     *            Modifiers of this port declaration.
     * @param source
     *            Source node of the AST.
     * @param container
     *            Declaration of the Node where the port is defined.
     */
    public PortDecl(String name, TypeNode type, int modifiers, TreeNode source,
            MetaModelDecl container) {
        super(name, CG_PORT, type, modifiers, source, container);
    }

}
