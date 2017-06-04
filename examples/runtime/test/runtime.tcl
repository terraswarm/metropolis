# Tests for runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: runtime.tcl,v 1.9 2005/11/22 20:02:20 allenh Exp $
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set TESTDIR [pwd]
######################################################################
####
#
test runtime-1.1 {Run the runtime test in the directory above} {
    cd ..	
    set errMsg {}
    if [catch {set results [exec -stderrok make -k all]} errMsg] {
	cd test
	error "loc_checker-1.1 failed: $errMsg"
    }
    cd $TESTDIR

    # The output is just too complex to compare because the order
    # of everything changes between runs.  So, we just look for a few
    # strings.
    list \
	[regexp {Top-Level netlist is top_level_netlist} $results] \
	[regexp {process runtime.XX} $results] \
	[regexp {medium runtime.IntM} $results] \
	[regexp {medium runtime.dummyw} $results] \
	[regexp {netlist runtime.system} $results] \
	[regexp {netlist runtime.dummyNet} $results] \
	[regexp {process runtime.dummyScope} $results] \
	[regexp {medium runtime.dummyr} $results]
} {1 1 1 1 1 1 1 1}
