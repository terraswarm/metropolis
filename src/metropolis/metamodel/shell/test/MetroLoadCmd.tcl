# Tests for MetroLoad
#
# @Author: Christopher Brooks
#
# @Version: $Id: MetroLoadCmd.tcl,v 1.6 2005/11/22 20:10:48 allenh Exp $
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
test MetroLoadCmd-1.1 {metroload, not enough args} {
    catch {metroload} errMsg
    list $errMsg
} {{wrong # args: should be "metroload option compflag name ?name ...?"}}

######################################################################
####
#
test MetroloadCmd-1.2 {metroload, bad sub command} {
    catch {metroload foo bar bif } errMsg
    list $errMsg
} {{bad option 'foo': must be class, file or pkg}}


######################################################################
####
#
test MetroloadCmd-1.3 {metroload, bad option to legitimate subcommand} {
    catch {metroload pkg -barf bif } errMsg
    list $errMsg
} {{bad flag '-barf': must be -classes, -expressions or -semantics}}


######################################################################
####
#
test MetroloadCmd-2.1 {metroload classes} {

    classpath add ../../../../../examples
    metroload pkg -classes producers_consumer
    metroload pkg -expressions producers_consumer
    metroload pkg -semantics producers_consumer
} {}

######################################################################
####
#
test MetroloadCmd-2.2 {metroload pkg -semantics badpackagename} { 
    catch {metroload pkg -semantics badPackageName} errMsg
    list $errMsg
} {{top level package 'badPackageName' not found. Check that the package name and classpath are correct}}

######################################################################
####
#
test MetroloadCmd-3.1 {metroload file} {
    metroload file -classes ../../../../../examples/producers_consumer/M.mmm
    metroload file -expressions ../../../../../examples/producers_consumer/M.mmm
    metroload file -semantics ../../../../../examples/producers_consumer/M.mmm
} {}

######################################################################
####
#
test MetroloadCmd-4.1 {metroload class} {
    metroload class -classes producers_consumer.IwIr
    metroload class -expressions producers_consumer.IwIr
    metroload class -semantics producers_consumer.IwIr
} {}
