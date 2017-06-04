# Tests for loc_checker
#
# @Author: Christopher Brooks
#
# @Version: $Id: loc_checker.tcl,v 1.20 2005/11/22 20:02:14 allenh Exp $
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set TESTDIR [pwd]
######################################################################
####
#
test loc_checker-1.1 {Create run.x and constrN.check} {
    cd ..	
    set errMsg {}
    if [catch {exec -stderrok make -k all run_runx checker} errMsg] {
	cd test
	error "loc_checker-1.1 failed: $errMsg"
    }
    if [catch {exec -stderrok make -k checker} errMsg] {
	cd test
	error "loc_checker-1.1 failed: $errMsg"
    }
    #puts $errMsg
    cd "$TESTDIR"
    set executableOrReadable executable
    if {[java::field java.io.File separator] == "\\"} {
	# In Jacl, Under Windows, only files that end in .exe, .bat or .com
	# are executable, so we check for readable instead
	set executableOrReadable readable
    }
    list \
	[file $executableOrReadable ../run.x] \
	[file $executableOrReadable ../constr0.check] \
	[file $executableOrReadable ../constr1.check] \
	[file $executableOrReadable ../constr2.check] \
	[file $executableOrReadable ../constr3.check] 
} {1 1 1 1 1}

######################################################################
####
#
test loc_checker-2.0 {constr0.check} {
    cd ..
    if [catch {set results [exec .[java::field java.io.File separator]constr0.check .trace]} errMsg] {
	cd test
        error "loc_checker-2.0: failed: $errMsg"
    }
    cd "$TESTDIR"
    list [regexp {No error!} $results] \
	[regexp {Maximum memory usage: (28|36|116) Bytes \(Approximately\)} $results]
} {1 1} 


######################################################################
####
#
test loc_checker-2.1 {constr1.check} {
    cd ..
    if [catch {set results [exec .[java::field java.io.File separator]constr1.check .trace]} errMsg] {
	cd test
        error "loc_checker-2.1: failed: $errMsg"
    }
    cd "$TESTDIR"
    #puts $results
    list [regexp {Totally 30 error\(s\) found} $results] \
	[regexp {Maximum memory usage: (52|68|116|136) Bytes \(Approximately\)} $results]
} {1 1}


######################################################################
####
#
test loc_checker-2.2 {constr2.check} {
    cd ..
    if [catch {set results [exec .[java::field java.io.File separator]constr2.check .trace]} errMsg] {
	cd test
        error "loc_checker-2.2: failed: $errMsg"
    }
    cd "$TESTDIR"
    #puts $results
    list [regexp {Totally 30 error\(s\) found} $results] \
	[regexp {Maximum memory usage: (136|116|84|68|64|56|52|48|44|40|32) Bytes \(Approximately\)} $results]
} {1 1} 

######################################################################
####
#
test loc_checker-2.3 {constr3.check} {
    cd ..
    if [catch {set results [exec .[java::field java.io.File separator]constr3.check .trace]} errMsg] {
	cd test
        error "loc_checker-2.3: failed: $errMsg"
    }
    cd "$TESTDIR"
    puts $results
    list [regexp {Totally 30 error\(s\) found} $results] \
	[regexp {Maximum memory usage: (116|36|28|24|20|16) Bytes \(Approximately\)} $results]
} {1 1} 


