# Tests for PropertyMap
#
# @Author: Christopher Brooks
#
# @Version: $Id: PropertyMap.tcl,v 1.7 2005/11/22 20:05:19 allenh Exp $
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

######################################################################
####
#
test PropertyMap-1.1 { defineProperty, clone, getDefinedProperty } {
    set property [java::new metropolis.metamodel.PropertyMap]

    # Define null properties for the three predefined values
    set results1 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results2 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY]]
    set results3 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]

    # clone it
    set property2 [java::cast metropolis.metamodel.PropertyMap \
		       [$property clone]]

    # Get those null properties back
    set object1 [$property2 getDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]] 
    set results4 [$object1 equals \
		      [java::field metropolis.metamodel.NullValue instance]]


    set object2 [$property2 getDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY]]
    set results5 [$object2 equals \
		      [java::field metropolis.metamodel.NullValue instance]]


    set object3 [$property2 getDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]
    set results6 [$object3 equals \
		      [java::field metropolis.metamodel.NullValue instance]]

    list $results1 $results2 $results3 $results4 $results5 $results6 
} {1 1 1 1 1 1}

######################################################################
####
#
test PropertyMap-2.1 { defineProperty twice} {
    set property [java::new metropolis.metamodel.PropertyMap]
    set results1 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results2 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    list $results1 $results2
} {1 0}


######################################################################
####
#
test PropertyMap-3.1 { getDefinedProperty on a non existent Property} {
    set property [java::new metropolis.metamodel.PropertyMap]
    catch {$property getDefinedProperty \
		[java::field metropolis.metamodel.PropertyMap \
		     NUMBER_KEY]} errMsg1
    set results1 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    catch {$property getDefinedProperty \
		[java::field metropolis.metamodel.PropertyMap \
		     NUMBER_KEY]} errMsg2
    list $errMsg1 $results1 $errMsg2
} {{java.lang.RuntimeException: Property -1 not defined, Property Map is empty.} 1 {java.lang.RuntimeException: Property -1 not defined}}


######################################################################
####
#
test PropertyMap-4.1 { getProperty on a non existent Property} {
    set property [java::new metropolis.metamodel.PropertyMap]
    set results1 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]
    set results2 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results3 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]

    # Get a defined property
    set object1 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results4 [$object1 equals \
		      [java::field metropolis.metamodel.NullValue instance]]

    list [java::isnull $results1] $results2 [java::isnull $results3] \
	$results4
} {1 1 1 1}

######################################################################
####
#
test PropertyMap-5.1 { setDefinedProperty when no properties are defined} {
    set property [java::new metropolis.metamodel.PropertyMap]
    catch {$property setDefinedProperty \
	       [java::field metropolis.metamodel.PropertyMap \
		    NUMBER_KEY] \
	       [java::null]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Property -1 not defined, Property Map is empty.}}

######################################################################
####
#
test PropertyMap-5.2 { setDefinedProperty} {
    set property [java::new metropolis.metamodel.PropertyMap]

    set results1 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]

    set object2 [$property setDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY] \
		      [java::null]]

    set results2 [$object2 equals \
		      [java::field metropolis.metamodel.NullValue instance]]

    set object3 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]
    set results3 [$object3 equals \
		      [java::field metropolis.metamodel.NullValue instance]]


    list $results1 $results2 $results3
} {1 1 1}

######################################################################
####
#
test PropertyMap-5.2.1 { setDefinedProperty with a non-null object} {
    set object4 [$property setDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY] \
		      "ObjectFour"]

    set results4 [$object4 equals \
		      [java::field metropolis.metamodel.NullValue instance]]


    # Set the NUMBER_KEY to a different Object, get back the old object
    set object5 [$property setDefinedProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY] \
		      "ObjectFive"]

    list $results5 $object5
} {1 ObjectFour}

######################################################################
####
#
test PropertyMap-5.3 { setDefinedProperty with wrong property} {
    set property [java::new metropolis.metamodel.PropertyMap]
    set results1 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]
    catch {$property setDefinedProperty \
	       [java::new Integer 666] \
	       [java::null]} errMsg
    list $errMsg
} {{java.lang.RuntimeException: Property 666 not defined}}

######################################################################
####
#
test PropertyMap-6.1 { keySet } {
    set property [java::new metropolis.metamodel.PropertyMap]
    set keySet0 [$property keySet]

    # Define null properties for the three predefined values
    set results1 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results2 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY]]
    set results3 [$property defineProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]
    set keySet [$property keySet]
    list [$keySet0 size] [$keySet size]
} {0 3}

######################################################################
####
#
test PropertyMap-7.1 { setProperty } {
    set property [java::new metropolis.metamodel.PropertyMap]

    # Define null properties for the three predefined values
    set results1 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY] \
		      CHILD_RETURN_VALUES_KEY]
    set results2 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY] \
		      RETURN_VALUE_AS_ELEMENT_KEY]
    set results3 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY] \
		      [java::null]]


    set results4 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results5 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY]]
    set results6 [$property getProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]

    list [java::isnull $results1] \
	[java::isnull $results2] \
	[java::isnull $results3] \
	$results4 $results5 \
	[$results6 equals \
	     [java::field metropolis.metamodel.NullValue instance]]
} {1 1 1 CHILD_RETURN_VALUES_KEY RETURN_VALUE_AS_ELEMENT_KEY 1}

######################################################################
####
#
test PropertyMap-8.1 { removeProperty } {
    set property [java::new metropolis.metamodel.PropertyMap]

    set results1 [$property removeProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]

    set results2 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY] \
		      CHILD_RETURN_VALUES_KEY]

    set results3 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY] \
		      CHILD_RETURN_VALUES_KEY2]

    set results4 [$property removeProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]

    set results5 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY] \
		      CHILD_RETURN_VALUES_KEY3]

    set results6 [$property removeProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    list [java::isnull $results1] \
	[java::isnull $results2] \
	$results3 $results4 \
	[java::isnull $results5] \
	$results6 
} {1 1 CHILD_RETURN_VALUES_KEY CHILD_RETURN_VALUES_KEY2 1 CHILD_RETURN_VALUES_KEY3}

######################################################################
####
#
test PropertyMap-9.1 { hasProperty } {
    set property [java::new metropolis.metamodel.PropertyMap]


    set results0 [$property hasProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]

    set results1 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY] \
		      CHILD_RETURN_VALUES_KEY]
    set results2 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY] \
		      RETURN_VALUE_AS_ELEMENT_KEY]
    set results3 [$property setProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY] \
		      [java::null]]

    set results4 [$property hasProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]

    set results5 [$property hasProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   RETURN_VALUE_AS_ELEMENT_KEY]]

    set results6 [$property hasProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   NUMBER_KEY]]

    list $results0 \
	[java::isnull $results1] \
	[java::isnull $results2] \
	[java::isnull $results3] \
	$results4 $results5 $results6 
} {0 1 1 1 1 1 1}

####
#
test PropertyMap-10.1 { values } {
    # Uses 9.1 above
    set values [$property values]
    set results1 [$values size]
    set results2 [$property removeProperty \
		      [java::field metropolis.metamodel.PropertyMap \
			   CHILD_RETURN_VALUES_KEY]]
    set results3 [$values size]

    list $results1 $results2 $results3
} {3 CHILD_RETURN_VALUES_KEY 2}


####
#
test PropertyMap-10.2 { values with a property that has not yet been set} {
    set property10 [java::new metropolis.metamodel.PropertyMap]
    set values [$property10 values]
    list [$values size]
} {0}
