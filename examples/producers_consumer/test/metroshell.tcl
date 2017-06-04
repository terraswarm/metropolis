# Tests for the test_new Metropolis SystemC simulator using metroshell
#
# @Author: Christopher Brooks
#
# @Version: $Id: metroshell.tcl,v 1.16 2005/11/22 20:01:11 allenh Exp $
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
test metroshell-1.1 {Initial setup} {
    file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]
    file delete -force systemc_sim.mk run.x 

    classpath add ../..
    metroload pkg -semantics producers_consumer
    elaborate producers_consumer.IwIr
    simulate systemc producers_consumer.IwIr
    list \
	    [file exists sc_main.cpp ] \
	    [file exists systemc_sim.mk]
} {1 1}

test metroshell-1.2 {Run make} {
    puts "Running 'make -f systemc_sim.mk' - this could take awhile"
    exec make -f systemc_sim.mk
    puts "... Done running make"
    # file executable run.x does not work under Windows?
    file exists run.x
} {1}

test metroshell-1.3 {Run the binary} {
    set results [exec -stderrok ".[java::field java.io.File separator]run.x"]
    regsub {^ *SystemC 2\.0.*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3
    # Substitute Producer? for Producer0 and Producer1
    regsub -all {Producer0} $results3 {Producer?} results4
    regsub -all {Producer1} $results4 {Producer?} results5

    # This hack is necessary because of problems with crnl under windows
    set changes [regsub -all [java::call System getProperty "line.separator"] \
		     $results5 "\n" results6]

    list [string range $results6 0 2048]
} {{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
monitor> Producer?: write 0 BEGIN_RT
monitor> Producer?: write 10 END_RT
monitor> Consumer: read 10 BEGIN_RT
monitor> Consumer: read 20 END_RT
monitor> Producer?: write 20 BEGIN_RT
monitor> Producer?: write 30 END_RT
monitor> Consumer: read 30 BEGIN_RT
monitor> Consumer: read 40 END_RT
monitor> Producer?: write 40 BEGIN_RT
monitor> Producer?: write 50 END_RT
monitor> Consumer: read 50 BEGIN_RT
monitor> Consumer: read 60 END_RT
monitor> Producer?: write 60 BEGIN_RT
monitor> Producer?: write 70 END_RT
monitor> Consumer: read 70 BEGIN_RT
monitor> Consumer: read 80 END_RT
monitor> Producer?: write 80 BEGIN_RT
monitor> Producer?: write 90 END_RT
monitor> Consumer: read 90 BEGIN_RT
monitor> Consumer: read 100 END_RT
monitor> Producer?: write 100 BEGIN_RT
monitor> Producer?: write 110 END_RT
monitor> Consumer: read 110 BEGIN_RT
monitor> Consumer: read 120 END_RT
monitor> Producer?: write 120 BEGIN_RT
monitor> Producer?: write 130 END_RT
monitor> Consumer: read 130 BEGIN_RT
monitor> Consumer: read 140 END_RT
monitor> Producer?: write 140 BEGIN_RT
monitor> Producer?: write 150 END_RT
monitor> Consumer: read 150 BEGIN_RT
monitor> Consumer: read 160 END_RT
monitor> Producer?: write 160 BEGIN_RT
monitor> Producer?: write 170 END_RT
monitor> Consumer: read 170 BEGIN_RT
monitor> Consumer: read 180 END_RT
monitor> Producer?: write 180 BEGIN_RT
monitor> Producer?: write 190 END_RT
monitor> Consumer: read 190 BEGIN_RT
monitor> Consumer: read 200 END_RT
monitor> Producer?: write 200 BEGIN_RT
monitor> Producer?: write 210 END_RT
monitor> Consumer: read 210 BEGIN_RT
monitor> Consumer: read 220 END_RT
monitor> Producer?: write 220 BEGIN_RT
monitor> Producer?: write 230 END_RT
monitor> Consumer: read 230 BEGIN_RT
monitor> Consumer: read 240 END_RT
monitor> Producer?: write 240 BEGIN_RT
monitor> Producer?: write 250 END_RT
monitor> Consumer: read 250 BEGIN_RT
monitor> Consumer: read 260 END_}}

test metroshell-2.1 {network show} {
    # Uses setup from metroshell-1.1 above
    set results [network show]
    # Unfortunately, the order of Components varies
    list \
	    [list \
	    [regexp {Top-level netlist:} $results] \
	    [regexp {netlist producers_consumer.IwIr} $results] \
	    [regexp {o Instance name: top_level_netlist} $results] \
	    [regexp {o Component name:} $results] \
	    [regexp {  o Components:} $results]] \
	    [list \
	    [regexp {MEDIUM  \(medium instance name: InstIntM\)} $results] \
	    [regexp {DummyWriter  \(medium instance name: DummyW\)} $results] \
	    [regexp {DummyReader  \(medium instance name: DummyR\)} $results] \
	    [regexp {Producer1  \(process instance name: Producer1\)} $results] \
	    [regexp {Producer0  \(process instance name: Producer0\)} $results] \
	    [regexp {Consumer  \(process instance name: Consumer\)} $results] \
	    ] \
	    [list \
	    [regexp {o Not refined by a netlist} $results] \
	    [regexp {o Does not refine any node} $results] \
	    [regexp {o No constraints} $results]]

} {{1 1 1 1 1} {1 1 1 1 1 1} {1 1 1}}
