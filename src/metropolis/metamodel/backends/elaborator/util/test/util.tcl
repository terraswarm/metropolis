# Tests for backends.elaborator.util
#
# @Author: Christopher Brooks
#
# @Version: $Id: util.tcl,v 1.6 2005/11/22 20:15:34 allenh Exp $
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

######################################################################
####
#
set classes [list ArrayList BitSet HashMap HashSet Hashtable LinkedList \
		 TreeMap TreeSet Vector]

foreach class $classes {
    test util-$class {Testing $class} { 
	
	# The interesting thing is that elaborator.util classes override
	# hashCode and equals so that elaborator.util classes that have
	# equal elements are not themselves equal.

	# Here, we test the nullary constructor.

	# These two objects are not equal.
	set metroObject [java::new \
		     metropolis.metamodel.backends.elaborator.util.$class]
	set metroObject2 [java::new \
		     metropolis.metamodel.backends.elaborator.util.$class]

	# These two objects are equal.
	set javaObject [java::new \
		     java.util.$class]
	set javaObject2 [java::new \
		     java.util.$class]
	list \
	    [list \
		 [expr {[$metroObject hashCode] == [$metroObject2 hashCode]}] \
		 [$metroObject equals $metroObject] \
		 [$metroObject equals $metroObject2]] \
	    [list \
		 [expr {[$javaObject hashCode] == [$javaObject2 hashCode]}] \
		 [$javaObject equals $javaObject] \
		 [$javaObject equals $javaObject2]] \
	} {{0 1 0} {1 1 1}}
} 

