# Tests for NetworkCmd
#
# @Author: Christopher Brooks
#
# @Version: $Id: NetworkCmd.tcl,v 1.7 2005/11/22 20:10:52 allenh Exp $
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
test NetworkCmd-1.1 {network, not enough args} {
    catch {network} errMsg
    list $errMsg
} {{wrong # args: should be "network option optargs"}}

######################################################################
####
#
test NetworkCmd-1.2 {network, bad sub command} {
    catch {network foo bar bif } errMsg
    list $errMsg
} {{bad option 'foo': must be addcomponent, connect, flatten,  redirectconnect, refine, refineconnect, setscope or show}}

# Load a demo
catch {file delete -force [glob -nocomplain *.tmp *.bin *.o *.cpp]} errmsg
classpath add ../../../../../examples
metroload pkg -semantics producers_consumer
elaborate producers_consumer.IwIr

######################################################################
####
#
test NetworkCmd-2.1 {network addcomponent} {
    catch {network addcomponent} errMsg
    list $errMsg
} {{wrong # args: should be "network addcomponent node netlist [component_name]"}}

######################################################################
####
#
test NetworkCmd-3.1 {network connect} {
    catch {network connect} errMsg
    list $errMsg
} {{wrong # args: should be "network connect srcnode port dstnode"}}

######################################################################
####
#
test NetworkCmd-4.1 {network flatten} {
    catch {network flatten} errMsg
    list $errMsg
} {{}}

######################################################################
####
#
test NetworkCmd-5.1 {network redirectconnect} {
    catch {network redirectconnect} errMsg
    list $errMsg
} {{wrong # args: should be "network redirectconnect netlist src srcport dst dstport"}}

######################################################################
####
#
test NetworkCmd-6.1 {network refine} {
    catch {network refine} errMsg
    list $errMsg
} {{wrong # args: should be "network refine node netlist"}}


######################################################################
####
#
test NetworkCmd-6.2 {network refine} {
    set results [network show]
    set result1 [regexp {o Refines} $results]

    # Not sure what this does, but network show does change
    network refine DummyR top_level_netlist
    set results [network show]
    set result2 [regexp {o Refines} $results]
    list $result1 $result2
} {0 1}

######################################################################
####
#
test NetworkCmd-7.1 {network refineconnect} {
    catch {network refineconnect} errMsg
    list $errMsg
} {{wrong # args: should be "network refineconnect netlist src port dst"}}

######################################################################
####
#
test NetworkCmd-8.1 {network setscope} {
    catch {network setscope} errMsg
    list $errMsg
} {{wrong # args: should be "network setscope node port netlist"}}

######################################################################
####
#
test NetworkCmd-9.1 {network show} {
    set results [network show]
    # The order is non-deterministic, so we just check for whether
    # the pieces occur
    list \
	[regexp {DummyWriter  \(medium instance name: DummyW\)} $results] \
	[regexp {MEDIUM  \(medium instance name: InstIntM\)} $results] \
	[regexp {Producer1  \(process instance name: Producer1\)} $results] \
	[regexp {Consumer  \(process instance name: Consumer\)} $results] \
	[regexp {Producer0  \(process instance name: Producer0\)} $results] \
	[regexp {DummyReader  \(medium instance name: DummyR\)} $results]
} {1 1 1 1 1 1}




