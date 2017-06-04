# Tests for the ElaboratorBackend.tcl
#
# @Author: Christopher Brooks, Based on SDFReceiver by Brian K. Vogel
#
# @Version: $Id: ElaboratorBackend.tcl,v 1.10 2005/11/23 00:18:04 allenh Exp $
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

if {[string compare jdkCapture [info procs jdkCapture]] == 1} then {
    source [file join $METRO util testsuite jdktools.tcl]
} {}

if {[string compare objectsToStrings [info procs objectsToStrings]] == 1} then {
    source [file join $METRO util testsuite enums.tcl]
} {}


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set TESTDIR [pwd]
catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errMsg

#######################################################################
# NOTE: See also src/metropolis/metamodel/shell/test/ElaborateCmd.tcl #
#######################################################################


#######################################################################
####
#
test ElaboratorBackend-1.1 {invoke with no args or sources} {
    set elaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.ElaboratorBackend]
    set args [java::new java.util.LinkedList]
    set sources [java::new java.util.LinkedList]
    catch {$elaborator invoke $args $sources} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Wrong number of arguments (0), should be 1, 3, 5 or 7: [-javac <path>] [-java <path>] [-dir <dir>] <netlistName>}}

#######################################################################
####
#
test ElaboratorBackend-1.2 {invoke with first arg not -javac} {
    set elaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.ElaboratorBackend]
    set args [java::new java.util.LinkedList]
    $args add "-not-javac"
    $args add "javac"
    $args add "-java"
    $args add "java"
    $args add "netinitiator"
    set sources [java::new java.util.LinkedList]
    catch {$elaborator invoke $args $sources} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Wrong arguments for elaboration, should be: [-javac <path>] [-java <path>] [-dir <dir>] <netlistName>}}

#######################################################################
####
#
test ElaboratorBackend-1.3 {invoke with 3rd arg not -java} {
    set elaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.ElaboratorBackend]
    set args [java::new java.util.LinkedList]
    $args add "-not-javac"
    $args add "javac"
    $args add "-not-java"
    $args add "java"
    $args add "netinitiator"
    set sources [java::new java.util.LinkedList]
    catch {$elaborator invoke $args $sources} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Wrong arguments for elaboration, should be: [-javac <path>] [-java <path>] [-dir <dir>] <netlistName>}}

#######################################################################
####
#
test ElaboratorBackend-1.4 {nonexistant netinitiator} {
    set elaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.ElaboratorBackend]
    set args [java::new java.util.LinkedList]
    $args add "nonexistant_netinitiator"
    set sources [java::new java.util.LinkedList]
    catch {$elaborator invoke $args $sources} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Specified net-initiator 'nonexistant_netinitiator' not found in the unnamed package.}}


#######################################################################
####
#
test ElaboratorBackend-2.0 {invoke with legitimate netlist} {
    # See also src/metropolis/metamodel/shell/test/ElaborateCmd.tcl

    classpath add ../../../../../../examples
    metroload pkg -semantics producers_consumer

    # This is like the metroshell "elaborate producers_consumer.IwIr" command

    set elaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.ElaboratorBackend]
    set args [java::new java.util.LinkedList]
    $args add "producers_consumer.IwIr"
    set sources [java::new java.util.LinkedList]

    $elaborator invoke $args $sources
    regexp producers_consumer [metrolist pkgs]
} {1}

#######################################################################
####
#
test ElaboratorBackend-2.1 {invoke with -javac javac -java java legitimate netlist} {
    # See also src/metropolis/metamodel/shell/test/ElaborateCmd.tcl

    classpath add ../../../../../../examples
    metroload pkg -semantics producers_consumer

    # This is like the metroshell "elaborate producers_consumer.IwIr" command

    set testElaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.test.TestElaboratorBackend]
    set args [java::new java.util.LinkedList]
    $args add "-javac"
    $args add "javac"
    $args add "-java"
    $args add "java"
    $args add "producers_consumer.IwIr"

    set sources [java::new java.util.LinkedList]

    $testElaborator invoke $args $sources
    regexp producers_consumer [metrolist pkgs]
} {1}


#######################################################################
####
#
test ElaboratorBackend-3.1 {Cover _findTypes()} {
    # Uses 2.1 above
    set typeList [$testElaborator findTypes]
    # objectsToStrings is defined in $METRO/util/testsuite/enums.tcl
    set types [lsort [objectsToStrings [listToObjects $typeList]]]
    set results ""
    foreach type $types {
	set results "$results\n$type" 
    }
    list $results
} {{
{AbstractCollection, 1}
{AbstractEnumerator, 1}
{AbstractHashIterator, 1}
{AbstractList, 1}
{AbstractMap, 1}
{AbstractSequentialList, 1}
{AbstractSet, 1}
{AbstractTreeMapIterator, 1}
{AbstractTreeSet, 1}
{Action, 1}
{Array, 1}
{ArrayList, 1}
{ArrayListItr, 1}
{BitSet, 1}
{BuiltInLOCType, 1}
{C, 8}
{Collection, 2}
{Comparable, 2}
{Comparator, 2}
{Dictionary, 1}
{EmptyEnumerator, 1}
{EmptyHashIterator, 1}
{EmptyTreeIterator, 1}
{Entry, 1}
{Entry, 1}
{Entry, 1}
{Entry, 1}
{Entry, 2}
{Enumeration, 2}
{Enumerator, 1}
{GlobalTime, 131072}
{GlobalTimeManager, 2}
{GlobalTimeRequestClass, 1}
{HashIterator, 1}
{HashMap, 1}
{HashSet, 1}
{Hashtable, 1}
{HashtableSet, 1}
{IntM, 16}
{IntReader, 2}
{IntWriter, 2}
{Interface, 2}
{Iterator, 2}
{Itr, 1}
{Itr, 1}
{IwIr, 4}
{LinkedList, 1}
{List, 2}
{ListIterator, 2}
{ListItr, 1}
{ListItr, 1}
{Map, 2}
{Math, 1}
{Medium, 16}
{Netlist, 4}
{Node, 1}
{Nondet, 1}
{Object, 1}
{ObjectType, 1}
{P, 8}
{Port, 2}
{Process, 8}
{Quantity, 131072}
{QuantityManager, 2}
{QuantityManagerLOC, 2}
{RequestClass, 1}
{SchedHierarchyChild, 2}
{SchedHierarchyParent, 2}
{SchedProgramCounter, 1}
{SchedStateVal, 1}
{Scheduler, 32}
{SchedulingNetlist, 4}
{SchedulingNetlistIntfc, 2}
{Scope, 2}
{Set, 2}
{Sized, 2}
{SortedMap, 2}
{SortedSet, 2}
{StateMedium, 64}
{StateMediumProc, 2}
{StateMediumSched, 2}
{String, 1}
{SubList, 1}
{SubTreeSet, 1}
{SubVector, 1}
{TreeIterator, 1}
{TreeMap, 1}
{TreeSet, 1}
{Vector, 1}
{VectorEnumerator, 1}
{VectorItr, 1}
{dummyr, 16}
{dummyw, 16}
{n_intfc, 2}
{ri_intfc, 2}
{ro_intfc, 2}
{s_intfc, 2}
{wi_intfc, 2}
{wo_intfc, 2}}}

#######################################################################
####
#
test ElaboratorBackend-4.1 {Cover _elaboratorError()} {
    set testElaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.test.TestElaboratorBackend]
    catch {$testElaborator elaborationError "Test Message"} errMsg
    list [string range $errMsg 0 152]
} {{metropolis.metamodel.backends.elaborator.ElaborationException: Elaboration error
Using Java compiler:	javac
Using Java interpreter:	java
Using class-path}}

#######################################################################
####
#
test ElaboratorBackend-5.1 {Cover _execute()} {
    set testElaborator \
	[java::new \
	     metropolis.metamodel.backends.elaborator.test.TestElaboratorBackend]
    catch {$testElaborator execute "not-a-command" true} errMsg
    list \
	[string range $errMsg 0 153] \
	[string range $errMsg [expr {[string length $errMsg ] - 36}] end]
} {{metropolis.metamodel.backends.elaborator.ElaborationException: Elaboration error
Using Java compiler:	javac
Using Java interpreter:	java
Using class-path:} {

Execution of:
not-a-command
failed}}
