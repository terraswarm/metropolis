# Tests for SimulateCmd
#
# @Author: Christopher Brooks
#
# @Version: $Id: SimulateCmd.tcl,v 1.12 2005/11/22 20:10:47 allenh Exp $
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

if {[info procs jdkCaptureErr] == "" } then {
    source [ file join $METRO util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test SimulateCmd-1.1 {simulate, not enough args} {
    catch {simulate} errMsg
    list $errMsg
} {{wrong # args: should be "simulate option ?simflags?"}}

######################################################################
####
#
test SimulateCmd-1.2 {simulate, bad sub command} {
    catch {simulate foo bar bif } errMsg
    list $errMsg
} {{bad option 'foo': must be java, promela or systemc}}

set producers_consumer ../../../../../examples/producers_consumer


######################################################################
####
#

# JavasimBackend.java is not always present, so we have two
# tests, one for when it is present, one for when it is not.

if [ file exists [file join $METRO src metropolis metamodel backends javasim JavasimBackend.java ]] {

    test SimulateCmd-2.1a {simulate java} {
	catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errmsg

	catch {eval file delete -force [glob -nocomplain $producers_consumer/*.java ]} errmsg
        set result0  \
	    [list [file exists $producers_consumer/C.java] \
	         [file exists $producers_consumer/M.java] \
	         [file exists $producers_consumer/P.java] \
	         [file exists $producers_consumer/Reader.java] \
	         [file exists $producers_consumer/Writer.java] \
	         [file exists $producers_consumer/system.java]]

        classpath add ../../../../../examples
        metroload pkg -semantics producers_consumer
        simulate java

        set result1 \
    	    [list [file exists $producers_consumer/C.java] \
	         [file exists $producers_consumer/M.java] \
	         [file exists $producers_consumer/P.java] \
	         [file exists $producers_consumer/Reader.java] \
	         [file exists $producers_consumer/Writer.java] \
	         [file exists $producers_consumer/system.java]]

        list $result0 $result1
    } {{0 0 0 0 0 0} {1 1 1 1 1 1}}

} else {

    test SimulateCmd-2.1b {simulate java} {
        catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errmsg

        catch {eval file delete -force [glob -nocomplain $producers_consumer/*.java ]} errmsg
        set result0  \
   	    [list [file exists $producers_consumer/C.java] \
	         [file exists $producers_consumer/M.java] \
	         [file exists $producers_consumer/P.java] \
	         [file exists $producers_consumer/Reader.java] \
	         [file exists $producers_consumer/Writer.java] \
	         [file exists $producers_consumer/system.java]]

        classpath add ../../../../../examples

        metroload pkg -semantics producers_consumer

	jdkCaptureErr {
	        catch {simulate java} errMsg
	} stderr

       list $errMsg	
   } {{Failed to instantiate 'metropolis.metamodel.backends.simulator.SimulatorBackend'. Perhaps that package is not currently in your tree}}

}

######################################################################
####
#
test SimulateCmd-3.1 {simulate systemc} {
    catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errmsg

    catch {eval file delete -force systemc_sim.mk}
    set result0  \
	[list [file exists systemc_sim.mk]]

    classpath add ../../../../../examples
    metroload pkg -semantics producers_consumer
    # Check for the error message
    catch {simulate systemc} errMsg

    # We could call 
    # elaborate producers_consumer.IwIr
    # but instead we pass in the top-level list name
    simulate systemc producers_consumer.IwIr

    set result1  \
	[list [file exists systemc_sim.mk]]

    list $result0 $result1 $errMsg
} {0 1 {Since SystemC code generation depends on the elaboration result, 
please do elaboration first or give the top-level netlist name 
after 'simulate systemc'.}}

######################################################################
####
#
test SimulateCmd-4.1 {simulate promela producers_consumer} {

    # Delete the pml files
    set producers_consumer_dir $METRO/examples/producers_consumer
    set files {C M P Reader Writer system}
    foreach file $files {
	catch {eval file delete -force $producers_consumer_dir/$file.pml} \
	    errmsg
    }	
    # Make sure they are deleted
    set results1 {}	 
    foreach file $files {
	lappend results1 \
	    [file exists $producers_consumer_dir/$file.pml]
    }	
    classpath add ../../../../../examples
    metroload pkg -semantics producers_consumer

    # This used to throw a NPE   
    simulate promela

    # Make sure the files are created.
    set results2 {}	 
    foreach file $files {
	lappend results2 \
	    [file exists $producers_consumer_dir/$file.pml]
    }	
    list $results1 $results2
} {{0 0 0 0 0 0} {1 1 1 1 1 1}} 



######################################################################
####
#
test SimulateCmd-4.2 {simulate promela SimpleByte.mmm} {
    set pmlFile	$METRO/examples/promela/SimpleByte.pml
    catch {eval file delete -force $pmlFile} errmsg
    set result1 [file exists $pmlFile]
    metroload file -semantics $METRO/examples/promela/SimpleByte.mmm
    simulate promela
    # FIXME:
    list $result1 [file exists $pmlFile]
} {0 1}
