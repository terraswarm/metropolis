/* Test class that extends ElaboratorBackend so we can test protected methods

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

 */

package metropolis.metamodel.backends.elaborator.test;

import metropolis.metamodel.backends.elaborator.ElaborationException;
import metropolis.metamodel.backends.elaborator.ElaboratorBackend;

import java.util.List;

// ////////////////////////////////////////////////////////////////////////
// // TestElaboratorBackend
/**
 * Test class that extends ElaboratorBackend so we can test protected methods.
 *
 * @author Christopher Brooks
 * @version $Id: TestElaboratorBackend.java,v 1.11 2006/10/12 20:32:28 cxh Exp $
 */
public class TestElaboratorBackend extends ElaboratorBackend {
    public TestElaboratorBackend() {
        super();
    }

    public void elaborationError(String msg) {
        // _elaborationError(msg);
        throw new ElaborationException(msg, _javacPath, _javaPath, _classPath,
                _tmpRoot);

    }

    public void execute(String command, boolean showError) {
        _execute(new String[] { command }, showError);
    }

    public List findTypes() {
        return _findTypes();
    }
}
