# Tests for ElaborateCmd
#
# @Author: Christopher Brooks
#
# @Version: $Id: ElaborateCmd.tcl,v 1.10 2005/11/22 20:10:50 allenh Exp $
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
test ElaborateCmd-1.1 {elaborator, not enough args} {
    catch {elaborate} errMsg
    list $errMsg
} {{wrong # args: should be "elaborate netinitiator"}}

######################################################################
####
#
test ElaborateCmd-1.2 {elaborator} {
    catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errMsg

    classpath add ../../../../../examples
    metroload pkg -semantics producers_consumer
    elaborate producers_consumer.IwIr
    regexp producers_consumer [metrolist pkgs]
} {1}


######################################################################
####
#
test ElaborateCmd-1.3 {elaborator with bogus values for javac and java} {
    catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errMsg

    set java not_java
    set javac not_javac
    jdkCaptureErr {
	catch {elaborate producers_consumer.IwIr} errMsg
    } stderr
    #puts $errMsg	
    string range $errMsg 0 80
} {Elaboration error
Using Java compiler:	not_javac
Using Java interpreter:	not_java}

######################################################################
####
#
test ElaborateCmd-1.4 {elaborator with ok values for javac and java} {
    catch {eval file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errMsg

    set java java
    set javac javac
    elaborate producers_consumer.IwIr
} {}
