#Tests for mgdb
#
# @Author: Christopher Brooks
#
# @Version: $Id: mgdb.tcl,v 1.17 2005/11/22 20:02:26 allenh Exp $
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
test mgdb-1.l {verify the version number of the underlying gdb} {
    set results [exec $METRO/bin/mgdb --version]
    set firstLine [string range $results 0 [expr {[string first {Copyright} [split $results]] -2 }]]
    if [regexp { 5.} $firstLine] {
	error "mgdb requires GDB 6.0 or later, you are running $firstLine"
    }
    
} {}

######################################################################
####
#
test mgdb-2.l {Run ../test_pnext} {
    cd ..	
    puts "mgdb-2.l: make started"
    set errMsg {}
    if [catch {exec -stderrok make -k} errMsg] {
	cd test
	error "mgdb-2.l failed: $errMsg"
    }
    puts "mgdb-2.l: make complete"
    set seconds 15
    puts "mgdb-2.l: running test for $seconds seconds"
    if [catch {set results [exec -stderrok \
		[file join $METRO util testsuite run-watchdog] \
				-n $seconds \
				./test_pnext]} errMsg] {
	cd test
	error "mgdb-2.l failed: $errMsg"
    }
    cd $TESTDIR
    list $results
} {{  proc     line     event state
  -------- -------- ----- -----
* Consumer C.mmm:36       RUN  
  proc      line     event state   
  --------- -------- ----- --------
  Consumer  C.mmm:39 begin EVALUATE
  Producer0 P.mmm:39 begin DONTRUN 
* Producer1 P.mmm:39 begin RUNNING 
monitor> Producer1: write 0 BEGIN_RT
monitor> Producer1: write 10 END_RT
  proc      line     event state  
  --------- -------- ----- -------
* Consumer  C.mmm:39 begin RUNNING
  Producer0 P.mmm:39 begin DONTRUN
  Producer1 M.mmm:80 end   RUN    
  proc      line     event state  
  --------- -------- ----- -------
  Consumer  M.mmm:82 begin UNKNOWN
  Producer0 P.mmm:39 begin DONTRUN
* Producer1 M.mmm:74 end   RUNNING
  proc      line     event state  
  --------- -------- ----- -------
* Consumer  M.mmm:82 begin RUNNING
  Producer0 P.mmm:39 begin DONTRUN
  Producer1 P.mmm:39 begin RUN    
  proc      line     event state   
  --------- -------- ----- --------
  Consumer  M.mmm:85 begin EVALUATE
  Producer0 P.mmm:39 begin DONTRUN 
* Producer1 P.mmm:39 begin RUNNING 
-
run-watchdog: Done}}

######################################################################
####
#
test mgdb-3.l {Run ../test_next_c} {
    cd ..	
    set seconds 15
    puts "mgdb-3.l: running test for $seconds seconds"
    if [catch {set results [exec -stderrok \
		[file join $METRO util testsuite run-watchdog] \
				-n $seconds \
				./test_next_c]} errMsg] {
	cd test
	error "mgdb-3.l failed: $errMsg"
    }
    cd $TESTDIR
    list $results
} {{38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 0 BEGIN_RT
monitor> ProducerX: write 10 END_RT
41	                    r = port0.readInt();
monitor> Consumer: read 10 BEGIN_RT
monitor> Consumer: read 20 END_RT
38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 20 BEGIN_RT
monitor> ProducerX: write 30 END_RT
41	                    r = port0.readInt();
monitor> Consumer: read 30 BEGIN_RT
monitor> Consumer: read 40 END_RT
38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 40 BEGIN_RT
monitor> ProducerX: write 50 END_RT
41	                    r = port0.readInt();
monitor> Consumer: read 50 BEGIN_RT
monitor> Consumer: read 60 END_RT
38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 60 BEGIN_RT
monitor> ProducerX: write 70 END_RT
41	                    r = port0.readInt();
monitor> Consumer: read 70 BEGIN_RT
monitor> Consumer: read 80 END_RT
38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 80 BEGIN_RT
monitor> ProducerX: write 90 END_RT
41	                    r = port0.readInt();
monitor> Consumer: read 90 BEGIN_RT
monitor> Consumer: read 100 END_RT
38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 100 BEGIN_RT
monitor> ProducerX: write 110 END_RT
41	                    r = port0.readInt();
monitor> Consumer: read 110 BEGIN_RT
monitor> Consumer: read 120 END_RT
38	        while (r < 30) 
39	            await 
monitor> ProducerX: write 120 BEGIN_RT
monitor> ProducerX: write 130 END_RT
41	                    r = port0.readInt();
-
run-watchdog: Done}}

######################################################################
####
#
test mgdb-4.l {Run ../test_next_p} {
    cd ..	
    set seconds 15
    puts "mgdb-4.l: running test for $seconds seconds"
    if [catch {set results [exec -stderrok \
		[file join $METRO util testsuite run-watchdog] \
				-n $seconds \
				./test_next_p]} errMsg] {
	cd test
	error "mgdb-4.l failed: $errMsg"
    }
    cd $TESTDIR
    list $results
} {{38	        while (w < 30) 
39	            await 
41	                    port1.writeInt(w);
monitor> ProducerX: write 0 BEGIN_RT
monitor> ProducerX: write 10 END_RT
38	        while (w < 30) 
39	            await 
41	                    port1.writeInt(w);
monitor> Consumer: read 10 BEGIN_RT
monitor> Consumer: read 20 END_RT
monitor> ProducerX: write 20 BEGIN_RT
monitor> ProducerX: write 30 END_RT
38	        while (w < 30) 
39	            await 
41	                    port1.writeInt(w);
monitor> Consumer: read 30 BEGIN_RT
monitor> Consumer: read 40 END_RT
monitor> ProducerX: write 40 BEGIN_RT
monitor> ProducerX: write 50 END_RT
38	        while (w < 30) 
39	            await 
monitor> Consumer: read 50 BEGIN_RT
monitor> Consumer: read 60 END_RT
monitor> ProducerX: write 60 BEGIN_RT
monitor> ProducerX: write 70 END_RT
41	                    port1.writeInt(w);
monitor> Consumer: read 70 BEGIN_RT
monitor> Consumer: read 80 END_RT
monitor> ProducerX: write 80 BEGIN_RT
monitor> ProducerX: write 90 END_RT
38	        while (w < 30) 
39	            await 
41	                    port1.writeInt(w);
monitor> Consumer: read 90 BEGIN_RT
monitor> Consumer: read 100 END_RT
monitor> ProducerX: write 100 BEGIN_RT
monitor> ProducerX: write 110 END_RT
38	        while (w < 30) 
-
run-watchdog: Done}}
