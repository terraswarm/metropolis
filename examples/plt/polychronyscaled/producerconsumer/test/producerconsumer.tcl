# Tests for the producerconsumer Metropolis SystemC simulator 
#
# @Author: Christopher Brooks
#
# @Version: $Id: producerconsumer.tcl,v 1.24 2005/11/28 18:06:38 allenh Exp $
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

set TESTDIR [pwd]

puts "This test takes a few minutes to complete, it will print out"
puts "the test number below as it proceeds:"
puts -nonewline "1 "
######################################################################
####
#
test ProducerConsumer-1.1 {run make demo1} {
    cd ..
    if [catch {exec -stderrok make demo1} errMsg] {
    	cd $TESTDIR
        error "ProducersConsumer-1.1 failed:  $errMsg"
    }
    cd $TESTDIR

    if {! [ file exists ..[java::field java.io.File separator]run.x]} {
	error "[pwd]..[java::field java.io.File separator]run.x does not exist"
    }

    set results [exec -stderrok make run_x_head]
    #set results [exec -stderrok -sh \
    #		     [file join $METRO util testsuite run-watchdog] \
    #		     -n 2 \
    #		     ..[java::field java.io.File separator]run.x]

    #puts $results
    regsub {^ *SystemC 2\..*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # Substitute Producer? for Producer0 and Producer1
    regsub -all {Producer0} $results3 {Producer?} results4
    regsub -all {Producer1} $results4 {Producer?} results5

    # Get rid of all lines that do not start with C or P
    regsub -all {^[^CP]*$} $results5 {} results6

   # This hack is necessary because of problems with crnl under windows
   regsub -all [java::call System getProperty "line.separator"] \
       $results6 "\n" results7
   set results8 [string range $results7 0 \
		     [expr {[string length $results7] - 2}]]

    # Under Solaris, this might start with "Producer is executing"
    # Under Linux, it might start with Consumer is executing"
    # So, if the first line is Producer is executing, we remove it
    set pattern {Producer is executing}
    if [ regexp "^\n$pattern" $results8] {
	set results9 [list [string range $results8 \
		[expr {1 + [string length $pattern]}] \
				[expr {513 + [string length $pattern]}]]]
    } else {
	set results9 [list [string range $results6 0 512]]
    }
    equalsOneOf $results9 \
	[list \
{{
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Produc}} \
    {{
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consumer is executing
Producer is executing
Consum}} \
    ]
} {1}

# Sleep for five seconds, don't print dots, try to remove run.x
sleep 5 0
file delete -force ..[java::field java.io.File separator]run.x]

puts -nonewline "2 "
######################################################################
####
#
test ProducerConsumer-1.2 {run make demo2} {
    cd ..
    if [catch {exec -stderrok make demo2} errMsg] {
	cd $TESTDIR
        error "ProducersConsumer-1.2 failed:  $errMsg"
    }
    cd $TESTDIR

    if {! [ file exists ..[java::field java.io.File separator]run.x]} {
	error "[pwd]..[java::field java.io.File separator]run.x does not exist"
    }

    set results [exec -stderrok make run_x_head]
#     set results0 [exec -stderrok -sh \
# 		     [file join $METRO util testsuite run-watchdog] \
# 		     -n 2 \
# 		     ..[java::field java.io.File separator]run.x]
#    set results [list [string range $results0 0 1024]]
    #puts $results
    regsub {^ *SystemC 2\..*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # Substitute Producer? for Producer0 and Producer1
    regsub -all {Producer0} $results3 {Producer?} results4
    regsub -all {Producer1} $results4 {Producer?} results5

    # Get rid of all lines that do not start with C,N, or P
    regsub -all {^[^CNP]*$} $results5 {} results6

    set results7 [list [string range $results6 0 512]]

    # equalsOnOf is defined in $METRO/util/testsuite/testDefs.tcl
    # It returns 1 if the first arg matches any of the elements of the 2nd arg
    equalsOneOf $results7 \
	[list \
{{
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 0
Producer is executing
    Now Sched}} \
{{
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Co}} \
{{
Producer is executing
Consumer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 1
Producer is executing
Consumer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 1
Producer is executing
Consumer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 1
Producer is executing
Consumer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 1
Producer is executing
Consumer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 1
Pr}} \
    {{
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduling Process id: 0
Consumer is executing
Producer is executing
Now Scheduling Process id: 1
Now Scheduli}} \
]
} {1}


# Sleep for five seconds, don't print dots, try to remove run.x
sleep 5 0
file delete -force ..[java::field java.io.File separator]run.x]

puts -nonewline "3 "
######################################################################
####
#
test ProducerConsumer-1.3 {Run make demo3} {
    cd ..
    if [catch {exec -stderrok make demo3} errMsg] {
	cd $TESTDIR
        error "ProducersConsumer-1.3 failed:  $errMsg"
    }
    cd $TESTDIR

    if {! [ file exists ..[java::field java.io.File separator]run.x]} {
	error "[pwd]..[java::field java.io.File separator]run.x does not exist"
    }

    #set results [exec -stderrok -sh \
    #		     [file join $METRO util testsuite run-watchdog] \
    #		     -n 2 \
    #		     ..[java::field java.io.File separator]run.x]


    set results [exec -stderrok make run_x_head]
    
    #puts $results
    regsub {^ *SystemC 2\..*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # Substitute Producer? for Producer0 and Producer1
    regsub -all {Producer0} $results3 {Producer?} results4
    regsub -all {Producer1} $results4 {Producer?} results5

    # Get rid of all lines that do not start with N
    regsub -all {^[^N]*$} $results5 {} results6

    set results7 [list [string range $results6 0 512]]

    # equalsOnOf is defined in $METRO/util/testsuite/testDefs.tcl
    # It returns 1 if the first arg matches any of the elements of the 2nd arg
    equalsOneOf $results7 \
	[list \
{{
Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now S}} \
{{
Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Sc}} \
{{
Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Proce}} \
{{
Now Scheduling Process id: 0
Now Scheduling Process id: 1

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0
Now Scheduling Process id: 1

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0
Now Scheduling Process id: 1

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now S}} \
{{
Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Proc}} \
{{
Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 1
Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Process id: 0

Now Scheduling Proce}}
]
} {1}

# Sleep for five seconds, don't print dots, try to remove run.x
sleep 5 0
file delete -force ..[java::field java.io.File separator]run.x]

puts -nonewline "4 "
######################################################################
####
#
test ProducerConsumer-1.4 {Run make demo4} {
    cd ..
    if [catch {exec -stderrok make demo4} errMsg] {
	cd $TESTDIR
        error "ProducersConsumer-1.4 failed:  $errMsg"
    }
    cd $TESTDIR

    if {! [ file exists ..[java::field java.io.File separator]run.x]} {
	error "[pwd]..[java::field java.io.File separator]run.x does not exist"
    }

    set results [exec -stderrok make run_x_head]

    # set results0 [exec -stderrok -sh \
# 		     [file join $METRO util testsuite run-watchdog] \
# 		     -n 2 \
# 		     ..[java::field java.io.File separator]run.x]
#    set results [string range $results0 0 1024] 
    regsub {^ *SystemC 2\..*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # Substitute Producer? for Producer0 and Producer1
    regsub -all {Producer0} $results3 {Producer?} results4
    regsub -all {Producer1} $results4 {Producer?} results5

    # Get rid of all lines that do not start with C, N or P
    regsub -all {^[^CNP]*$} $results5 {} results6

    set results7 [list [string range $results6 0 512]]

    # equalsOnOf is defined in $METRO/util/testsuite/testDefs.tcl
    # It returns 1 if the first arg matches any of the elements of the 2nd arg
    equalsOneOf $results7 \
	[list \
{{
Consumer is executing
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 1
Consumer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Sched}} \
{{
Producer is executing
Consumer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 1
Consumer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Sched}} \
{{
Consumer is executing
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 1
Consumer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is }} \
{{
Consumer is executing
Producer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 1
Consumer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Pr}} \
{{
Consumer is executing
Producer is executing
Now Scheduling Process id: 0
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 1
Consumer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduling Process id: 0
Producer is executing
Now Scheduli}}
]
} {1}

puts " Done."

# Reset everything back to the original state
file copy -force ../ProdConsScheduled.mmm_1 ../ProdConsScheduled.mmm
