# Tests for EqualVars class in the runtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: EqualVars.tcl,v 1.4 2005/11/22 20:11:58 allenh Exp $
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
#                                                 PT_COPYRIGHT_VERSION_2
#                                                 COPYRIGHTENDKEY
#######################################################################

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs makeINode] == "" } then {
    source makeINode.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test EqualVars-1.1 {constructor} {
    set event1 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent]
    # makeINode is defined in makeINode.tcl
    set        iNode [makeINode]
    set event2 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    $iNode "null" "myEvent2"]

    set equalVars [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" $event1 \
        [java::field metropolis.metamodel.runtime.EqualVars STRINGTYPE] \
        "foo" $event2 \
        [java::field metropolis.metamodel.runtime.EqualVars VARTYPE]]

    list \
        [[$equalVars getEvent1] equals $event1] \
        [[$equalVars getEvent2] equals $event2] \
        [$equalVars getType1] \
        [$equalVars getType2] \
        [$equalVars getValue1] \
        [$equalVars getValue2]
} {1 1 6 7 const1 foo}

######################################################################
####
#
test EqualVars-1.2 {constructor with bad type} {
    # Uses 1.1 above.
    catch {
        set equalVars [java::new metropolis.metamodel.runtime.EqualVars \
            "const1" $event1 \
            -1 \
            "foo" $event2 \
            9999]
    } errMsg
    list $errMsg
} {{java.lang.RuntimeException: type '-1' was out of range, it must be >= EqualVars.BOOLTYPE (0) and <= EqualVars.VARTYPE (7)}}

######################################################################
####
#
test EqualVars-2.1 {set* methods} {
    # Uses 1.1 above.
    set equalVars2_1 [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" $event1 \
        [java::field metropolis.metamodel.runtime.EqualVars BOOLTYPE] \
        "foo" $event2 \
        [java::field metropolis.metamodel.runtime.EqualVars INTTYPE]]

    set event3 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent3]
    $equalVars2_1 setEvent1 $event3

    set event4 [java::call metropolis.metamodel.runtime.Event newEvent \
                   [java::field metropolis.metamodel.runtime.Event NONE] \
                    "null" "null" myEvent4]
    $equalVars2_1 setEvent2 $event4

    $equalVars2_1 setType1 \
        [java::field metropolis.metamodel.runtime.EqualVars FLOATTYPE]
    $equalVars2_1 setType2 \
        [java::field metropolis.metamodel.runtime.EqualVars DOUBLETYPE]


    $equalVars2_1 setValue1 "newValue1"
    $equalVars2_1 setValue2 "newValue2"
    list \
        [[$equalVars2_1 getEvent1] equals $event3] \
        [[$equalVars2_1 getEvent2] equals $event4] \
        [$equalVars2_1 getType1] \
        [$equalVars2_1 getType2] \
        [$equalVars2_1 getValue1] \
        [$equalVars2_1 getValue2]

} {1 1 3 2 newValue1 newValue2}

######################################################################
####
#
test EqualVars-3.1 {show} {
    # Uses 1.1 above.
    $equalVars show
} {"const1" == foo@(none(myINode), i)}

######################################################################
####
#
test EqualVars-3.2 {show} {
    # Uses 2.1 above.
    $equalVars2_1 show
} {newValue1 == newValue2}

######################################################################
####
#
test EqualVars-3.3 {show with null events} {
    set equalVars3_3 [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" [java::null] \
        [java::field metropolis.metamodel.runtime.EqualVars VARTYPE] \
        "foo" [java::null] \
        [java::field metropolis.metamodel.runtime.EqualVars VARTYPE]]
    $equalVars3_3 show
} {const1 == foo}

######################################################################
####
#
test EqualVars-3.4 {show with VARTYPE and nonnull events} {
    # Uses event1 and event2 from 1.1 
    set equalVars3_4 [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" $event1 \
        [java::field metropolis.metamodel.runtime.EqualVars VARTYPE] \
        "foo" $event2 \
        [java::field metropolis.metamodel.runtime.EqualVars VARTYPE]]
    $equalVars3_4 show
} {const1@(none(all), i) == foo@(none(myINode), i)}

######################################################################
####
#
test EqualVars-3.5 {show with CHARTYPE and nonnull events} {
    # Uses event1 and event2 from 1.1 
    set equalVars3_5 [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" $event1 \
        [java::field metropolis.metamodel.runtime.EqualVars CHARTYPE] \
        "foo" $event2 \
        [java::field metropolis.metamodel.runtime.EqualVars CHARTYPE]]
    $equalVars3_5 show
} {'const1' == 'foo'}

######################################################################
####
#
test EqualVars-3.6 {show with STRINGTYPE and nonnull events} {
    # Uses event1 and event2 from 1.1 
    set equalVars3_6 [java::new metropolis.metamodel.runtime.EqualVars \
        "const1" $event1 \
        [java::field metropolis.metamodel.runtime.EqualVars STRINGTYPE] \
        "foo" $event2 \
        [java::field metropolis.metamodel.runtime.EqualVars STRINGTYPE]]
    $equalVars3_6 show
} {"const1" == "foo"}

