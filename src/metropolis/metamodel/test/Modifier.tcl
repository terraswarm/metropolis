# Tests for Modifier
#
# @Author: Christopher Brooks
#
# @Version: $Id: Modifier.tcl,v 1.7 2005/11/22 20:05:25 allenh Exp $
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

if {[info procs iterToObjects] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# We run makeModifierAuto.tcl to generate Modifier.tcl


test test-Modifier-1.1 {Two visibilities} {
    set public_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			PUBLIC_MOD]
    set protected_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			PROTECTED_MOD]
    catch {
	java::call metropolis.metamodel.Modifier checkClassModifiers \
	    [expr {$public_mod | $protected_mod}]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Different visibilities defined in class declaration:public protected }}

####
#
test test-Modifier-2.1 {Abstract and static} {
    set abstract_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			ABSTRACT_MOD]
    set static_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			STATIC_MOD]
    catch {
	java::call metropolis.metamodel.Modifier \
	    checkInterfaceMethodModifiers \
	    [expr {$abstract_mod | $static_mod}]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Error in method declaration : cannot use static and abstract simultaneously}}

####
#
test test-Modifier-2.2 {Abstract and private} {
    set abstract_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			ABSTRACT_MOD]
    set private_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			PRIVATE_MOD]
    catch {
	java::call metropolis.metamodel.Modifier \
	    checkInterfaceMethodModifiers \
	    [expr {$abstract_mod | $private_mod}]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Error in method declaration : cannot use private and abstract simultaneously}}

####
#
test test-Modifier-2.2 {Abstract and private} {
    set abstract_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			ABSTRACT_MOD]
    set final_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			FINAL_MOD]
    catch {
	java::call metropolis.metamodel.Modifier \
	    checkInterfaceMethodModifiers \
	    [expr {$abstract_mod | $final_mod}]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: Error in method declaration : cannot use final and abstract simultaneously}}

####
#
test test-Modifier-3.1 {toString NO_MOD} {
    set no_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			NO_MOD]
    java::call metropolis.metamodel.Modifier toString $no_mod
} {}

####
#
test test-Modifier-3.2 {toString ANY_MOD} {
    set any_mod [java::field \
			metropolis.metamodel.MetaModelStaticSemanticConstants \
			ANY_MOD]
    java::call metropolis.metamodel.Modifier toString $any_mod
} {public protected private abstract final static elaborate }

####
#
test test-Modifier-3.3 {toString every *_MOD except ANY_MOD and NO_MOD} {
    # Get all the _MOD static fields in case one gets added
    set allModValue 0
    set allMods [java::info fields -static \
		  metropolis.metamodel.MetaModelStaticSemanticConstants]
    foreach mod $allMods {
	if [regexp {_MOD$} $mod] {
	    if { "$mod" != "ANY_MOD"
		 && "$mod" != "NO_MOD"} {
		set modValue [java::field \
				  metropolis.metamodel.MetaModelStaticSemanticConstants \
				  $mod]
		set allModValue [expr {$allModValue | $modValue}]
	    }
	}
    }
    java::call metropolis.metamodel.Modifier toString $allModValue
} {public protected private abstract final static elaborate }
