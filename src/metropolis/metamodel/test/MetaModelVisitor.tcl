# Tests for the MetaModelVisitor class
#
# @Author: Christopher Brooks
#
# @Version: $Id: MetaModelVisitor.tcl,v 1.4 2005/11/22 20:05:17 allenh Exp $
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs iterToObjects] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test MetaModelVisitor-1.1 {nullary constructor} {
    set visitor [java::new metropolis.metamodel.MetaModelVisitor]
    set TM_CHILDREN_FIRST \
	[java::field metropolis.metamodel.IVisitor TM_CHILDREN_FIRST]
    expr {[$visitor traversalMethod] == $TM_CHILDREN_FIRST}
} {1}

######################################################################
####
#
test MetaModelVisitor-2.1 {constructor traversalMethod TM_SELF_FIRST} {
    set TM_SELF_FIRST \
	[java::field metropolis.metamodel.IVisitor TM_SELF_FIRST]
    set visitor [java::new metropolis.metamodel.MetaModelVisitor \
		     $TM_SELF_FIRST]

    expr {[$visitor traversalMethod] == $TM_SELF_FIRST}
} {1}

######################################################################
####
#
test MetaModelVisitor-2.2 {constructor traversalMethod TM_CUSTOM} {
    set TM_CUSTOM \
	[java::field metropolis.metamodel.IVisitor TM_CUSTOM]
    set visitor [java::new metropolis.metamodel.MetaModelVisitor \
		     $TM_CUSTOM]

    expr {[$visitor traversalMethod] == $TM_CUSTOM}
} {1}

######################################################################
####
#
test MetaModelVisitor-2.2 {constructor traversalMethod Illegal traversal} {
    catch {
	set visitor [java::new metropolis.metamodel.MetaModelVisitor \
			 666]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Illegal traversal method}}

######################################################################
####
#

# Get the list of all of the visit* methods
set metaModelVisitorMethods \
    [java::info methods metropolis.metamodel.MetaModelVisitor]
foreach metaModelVisitorMethod $metaModelVisitorMethods {
    if [regexp {^visit} $metaModelVisitorMethod] {
    lappend visitMethods [lindex $metaModelVisitorMethod 0]
    }
}

# Call each visit* method, it should return null
set visitor [java::new metropolis.metamodel.MetaModelVisitor]
foreach visitMethod $visitMethods {
    test MetaModelVisitor-$visitMethod "Auto generated test for $visitMethod" {
	set r [$visitor $visitMethod [java::null] [java::null]]
	list [java::isnull $r]
    } {1}
}
