# Tests for the ElaborationException
#
# @Author: Christopher Brooks, Based on SDFReceiver by Brian K. Vogel
#
# @Version: $Id: ElaborationException.tcl,v 1.6 2005/11/22 20:16:07 allenh Exp $
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software and that appropriate acknowledgments are made
# to the research of the Metropolis group.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

test ElaborationException-1.1 {} {
    set tmpRoot [java::new java.io.File /tmp/elaboratorExceptionTest]
    set cause [java::new Exception "Test Exception"]
    set ex [java::new \
	metropolis.metamodel.backends.elaborator.ElaborationException \
	"Message" \
	"/path/to/javac" \
	"/path/to/java" \
	"class:path" \
	$tmpRoot \
	$cause]
    set results [$ex getMessage]

    # Windows: Convert backslashes to forward slashes
    regsub -all {\\} $results {/} results2

    list $results2
} {{Elaboration error
Using Java compiler:	/path/to/javac
Using Java interpreter:	/path/to/java
Using class-path:	class:path
Temporary directory:	/tmp/elaboratorExceptionTest

Message}}
