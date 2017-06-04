# Tests for MetroList
#
# @Author: Christopher Brooks
#
# @Version: $Id: MetroListCmd.tcl,v 1.5 2005/11/22 20:10:49 allenh Exp $
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
test MetroListCmd-1.1 {metrolist, not enough args} {
    catch {metrolist} errMsg
    list $errMsg
} {{wrong # args: should be "metrolist option"}}

######################################################################
####
#
test MetroListCmd-1.2 {metrolist, bad sub commane} {
    catch {metrolist foo} errMsg
    list $errMsg
} {{bad option 'foo': must be classes, files or pkgs}}


######################################################################
####
#
test MetroListCmd-2.1 {metrolist classes} {

    classpath add ../../../../../examples
    metroload pkg -semantics producers_consumer

    set classes [metrolist classes]
    # Check to see that metamodel.lang.Port is defined
    list \
	[regexp {metamodel.lang.Port} $classes] \
	[regexp {producers_consumer.IwIr} $classes]
} {1 1}


######################################################################
####
#
test MetroListCmd-3.1 {metrolist pkgs} {
    set packages [metrolist pkgs]
    # Check to see that metamodel.lang.Port is defined
    list \
	[regexp {producers_consumer} $packages] \
	[regexp {metamodel.lang} $packages] \
} {1 1}

######################################################################
####
#
test MetroListCmd-4.1 {metrolist files} {
    set files [metrolist files]
    # Check to see that metamodel.lang.Port is defined
    regexp {Port.mmm} $files
} {1}
