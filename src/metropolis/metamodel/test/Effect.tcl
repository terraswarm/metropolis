# Tests for the Effect class
#
# @Author: Christopher Brooks
#
# @Version: $Id: Effect.tcl,v 1.7 2005/11/22 20:05:33 allenh Exp $
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
test Effect-1.1 {Get all the _EFFECT variables} {
    # We do this so that if someone adds and effect to 
    # MetaModelStaticSemanticConstants, then they will update
    # Effects.java
    set results {}
    set allEffects [java::info fields -static \
		  metropolis.metamodel.MetaModelStaticSemanticConstants]
    foreach effect $allEffects {
	if [regexp {_EFFECT$} $effect] {
	    # FIXME: Should we include ANY_EFFECT?
	    if { "$effect" != "ANY_EFFECT"} {
		set effectValue [java::field \
				     metropolis.metamodel.MetaModelStaticSemanticConstants \
				     $effect]
		lappend results [java::call \
				     metropolis.metamodel.Effect \
				     toString $effectValue]
	    }
	}
    }
    list $results
} {{{} {eval } {update } {constant }}}


######################################################################
####
#
test Effect-2.1 {Bad effect} {
    catch {java::call metropolis.metamodel.Effect toString -1} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Wrong effect '-1' of method.}}
