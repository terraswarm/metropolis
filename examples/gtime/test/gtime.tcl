# Tests for gtime
#
# @Author: Christopher Brooks
#
# @Version: $Id: gtime.tcl,v 1.8 2005/11/22 20:05:00 allenh Exp $
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
test gtime-1.1 {Create run.x and constrN.check} {
    cd ..	
    set errMsg {}
    if [catch {exec -stderrok make -k} errMsg] {
	cd test
	error "gtime-1.1 failed: $errMsg"
    }

    if [catch {set results [exec -stderrok .[java::field java.io.File separator]run.x]}] {
	cd test
	error "gtime-1.1 failed: $errMsg"
    }
    cd $TESTDIR

    regsub {^ *SystemC 2\..*$} $results {} results2
    regsub {^ *Copyright.*$} $results2 {} results3

    equalsOneOf $results3 \
    [list \
{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
- quantities to statemedia
- quantities to quantities
- statemedia to quantities
In process p1: cycle=1 j=29
GlobalTime @ beg =0
GlobalTime @ end =1
In process p1: cycle=1 j=28
GlobalTime @ beg =1
GlobalTime @ end =2
In process p2: cycle=2 j=29
GlobalTime @ beg =0
GlobalTime @ end =2
In process p1: cycle=1 j=27
GlobalTime @ beg =2
GlobalTime @ end =3
In process p1: cycle=1 j=26
GlobalTime @ beg =3
GlobalTime @ end =4
In process p2: cycle=2 j=28
GlobalTime @ beg =2
GlobalTime @ end =4
In process p1: cycle=1 j=25
GlobalTime @ beg =4
GlobalTime @ end =5
In process p1: cycle=1 j=24
GlobalTime @ beg =5
GlobalTime @ end =6
In process p2: cycle=2 j=27
GlobalTime @ beg =4
GlobalTime @ end =6
In process p1: cycle=1 j=23
GlobalTime @ beg =6
GlobalTime @ end =7
In process p1: cycle=1 j=22
GlobalTime @ beg =7
GlobalTime @ end =8
In process p2: cycle=2 j=26
GlobalTime @ beg =6
GlobalTime @ end =8
In process p1: cycle=1 j=21
GlobalTime @ beg =8
GlobalTime @ end =9
In process p1: cycle=1 j=20
GlobalTime @ beg =9
GlobalTime @ end =10
In process p2: cycle=2 j=25
GlobalTime @ beg =8
GlobalTime @ end =10
In process p1: cycle=1 j=19
GlobalTime @ beg =10
GlobalTime @ end =11
In process p1: cycle=1 j=18
GlobalTime @ beg =11
GlobalTime @ end =12
In process p2: cycle=2 j=24
GlobalTime @ beg =10
GlobalTime @ end =12
In process p1: cycle=1 j=17
GlobalTime @ beg =12
GlobalTime @ end =13
In process p1: cycle=1 j=16
GlobalTime @ beg =13
GlobalTime @ end =14
In process p2: cycle=2 j=23
GlobalTime @ beg =12
GlobalTime @ end =14
In process p1: cycle=1 j=15
GlobalTime @ beg =14
GlobalTime @ end =15
In process p1: cycle=1 j=14
GlobalTime @ beg =15
GlobalTime @ end =16
In process p2: cycle=2 j=22
GlobalTime @ beg =14
GlobalTime @ end =16
In process p1: cycle=1 j=13
GlobalTime @ beg =16
GlobalTime @ end =17
In process p1: cycle=1 j=12
GlobalTime @ beg =17
GlobalTime @ end =18
In process p2: cycle=2 j=21
GlobalTime @ beg =16
GlobalTime @ end =18
In process p1: cycle=1 j=11
GlobalTime @ beg =18
GlobalTime @ end =19
In process p1: cycle=1 j=10
GlobalTime @ beg =19
GlobalTime @ end =20
In process p2: cycle=2 j=20
GlobalTime @ beg =18
GlobalTime @ end =20
In process p1: cycle=1 j=9
GlobalTime @ beg =20
GlobalTime @ end =21
In process p1: cycle=1 j=8
GlobalTime @ beg =21
GlobalTime @ end =22
In process p2: cycle=2 j=19
GlobalTime @ beg =20
GlobalTime @ end =22
In process p1: cycle=1 j=7
GlobalTime @ beg =22
GlobalTime @ end =23
In process p1: cycle=1 j=6
GlobalTime @ beg =23
GlobalTime @ end =24
In process p2: cycle=2 j=18
GlobalTime @ beg =22
GlobalTime @ end =24
In process p1: cycle=1 j=5
GlobalTime @ beg =24
GlobalTime @ end =25
In process p1: cycle=1 j=4
GlobalTime @ beg =25
GlobalTime @ end =26
In process p2: cycle=2 j=17
GlobalTime @ beg =24
GlobalTime @ end =26
In process p1: cycle=1 j=3
GlobalTime @ beg =26
GlobalTime @ end =27
In process p1: cycle=1 j=2
GlobalTime @ beg =27
GlobalTime @ end =28
In process p2: cycle=2 j=16
GlobalTime @ beg =26
GlobalTime @ end =28
In process p1: cycle=1 j=1
GlobalTime @ beg =28
GlobalTime @ end =29
In process p1: cycle=1 j=0
GlobalTime @ beg =29
GlobalTime @ end =30
In process p2: cycle=2 j=15
GlobalTime @ beg =28
GlobalTime @ end =30
In process p2: cycle=2 j=14
GlobalTime @ beg =30
GlobalTime @ end =32
In process p2: cycle=2 j=13
GlobalTime @ beg =32
GlobalTime @ end =34
In process p2: cycle=2 j=12
GlobalTime @ beg =34
GlobalTime @ end =36
In process p2: cycle=2 j=11
GlobalTime @ beg =36
GlobalTime @ end =38
In process p2: cycle=2 j=10
GlobalTime @ beg =38
GlobalTime @ end =40
In process p2: cycle=2 j=9
GlobalTime @ beg =40
GlobalTime @ end =42
In process p2: cycle=2 j=8
GlobalTime @ beg =42
GlobalTime @ end =44
In process p2: cycle=2 j=7
GlobalTime @ beg =44
GlobalTime @ end =46
In process p2: cycle=2 j=6
GlobalTime @ beg =46
GlobalTime @ end =48
In process p2: cycle=2 j=5
GlobalTime @ beg =48
GlobalTime @ end =50
In process p2: cycle=2 j=4
GlobalTime @ beg =50
GlobalTime @ end =52
In process p2: cycle=2 j=3
GlobalTime @ beg =52
GlobalTime @ end =54
In process p2: cycle=2 j=2
GlobalTime @ beg =54
GlobalTime @ end =56
In process p2: cycle=2 j=1
GlobalTime @ beg =56
GlobalTime @ end =58
In process p2: cycle=2 j=0
GlobalTime @ beg =58
GlobalTime @ end =60
Finished or Deadlocked!
SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED} \
{Set execution order of processes
Begin setting up connections
- processes to media
- media to media
- processes to statemedia
- media to statemedia
- quantities to statemedia
- quantities to quantities
- statemedia to quantities
In process p1: cycle=1 j=29
GlobalTime @ beg =0
GlobalTime @ end =1
In process p2: cycle=2 j=29
GlobalTime @ beg =0
GlobalTime @ end =2
In process p1: cycle=1 j=28
GlobalTime @ beg =1
GlobalTime @ end =2
In process p1: cycle=1 j=27
GlobalTime @ beg =2
GlobalTime @ end =3
In process p2: cycle=2 j=28
GlobalTime @ beg =2
GlobalTime @ end =4
In process p1: cycle=1 j=26
GlobalTime @ beg =3
GlobalTime @ end =4
In process p1: cycle=1 j=25
GlobalTime @ beg =4
GlobalTime @ end =5
In process p2: cycle=2 j=27
GlobalTime @ beg =4
GlobalTime @ end =6
In process p1: cycle=1 j=24
GlobalTime @ beg =5
GlobalTime @ end =6
In process p1: cycle=1 j=23
GlobalTime @ beg =6
GlobalTime @ end =7
In process p2: cycle=2 j=26
GlobalTime @ beg =6
GlobalTime @ end =8
In process p1: cycle=1 j=22
GlobalTime @ beg =7
GlobalTime @ end =8
In process p1: cycle=1 j=21
GlobalTime @ beg =8
GlobalTime @ end =9
In process p2: cycle=2 j=25
GlobalTime @ beg =8
GlobalTime @ end =10
In process p1: cycle=1 j=20
GlobalTime @ beg =9
GlobalTime @ end =10
In process p1: cycle=1 j=19
GlobalTime @ beg =10
GlobalTime @ end =11
In process p2: cycle=2 j=24
GlobalTime @ beg =10
GlobalTime @ end =12
In process p1: cycle=1 j=18
GlobalTime @ beg =11
GlobalTime @ end =12
In process p1: cycle=1 j=17
GlobalTime @ beg =12
GlobalTime @ end =13
In process p2: cycle=2 j=23
GlobalTime @ beg =12
GlobalTime @ end =14
In process p1: cycle=1 j=16
GlobalTime @ beg =13
GlobalTime @ end =14
In process p1: cycle=1 j=15
GlobalTime @ beg =14
GlobalTime @ end =15
In process p2: cycle=2 j=22
GlobalTime @ beg =14
GlobalTime @ end =16
In process p1: cycle=1 j=14
GlobalTime @ beg =15
GlobalTime @ end =16
In process p1: cycle=1 j=13
GlobalTime @ beg =16
GlobalTime @ end =17
In process p2: cycle=2 j=21
GlobalTime @ beg =16
GlobalTime @ end =18
In process p1: cycle=1 j=12
GlobalTime @ beg =17
GlobalTime @ end =18
In process p1: cycle=1 j=11
GlobalTime @ beg =18
GlobalTime @ end =19
In process p2: cycle=2 j=20
GlobalTime @ beg =18
GlobalTime @ end =20
In process p1: cycle=1 j=10
GlobalTime @ beg =19
GlobalTime @ end =20
In process p1: cycle=1 j=9
GlobalTime @ beg =20
GlobalTime @ end =21
In process p2: cycle=2 j=19
GlobalTime @ beg =20
GlobalTime @ end =22
In process p1: cycle=1 j=8
GlobalTime @ beg =21
GlobalTime @ end =22
In process p1: cycle=1 j=7
GlobalTime @ beg =22
GlobalTime @ end =23
In process p2: cycle=2 j=18
GlobalTime @ beg =22
GlobalTime @ end =24
In process p1: cycle=1 j=6
GlobalTime @ beg =23
GlobalTime @ end =24
In process p1: cycle=1 j=5
GlobalTime @ beg =24
GlobalTime @ end =25
In process p2: cycle=2 j=17
GlobalTime @ beg =24
GlobalTime @ end =26
In process p1: cycle=1 j=4
GlobalTime @ beg =25
GlobalTime @ end =26
In process p1: cycle=1 j=3
GlobalTime @ beg =26
GlobalTime @ end =27
In process p2: cycle=2 j=16
GlobalTime @ beg =26
GlobalTime @ end =28
In process p1: cycle=1 j=2
GlobalTime @ beg =27
GlobalTime @ end =28
In process p1: cycle=1 j=1
GlobalTime @ beg =28
GlobalTime @ end =29
In process p2: cycle=2 j=15
GlobalTime @ beg =28
GlobalTime @ end =30
In process p1: cycle=1 j=0
GlobalTime @ beg =29
GlobalTime @ end =30
In process p2: cycle=2 j=14
GlobalTime @ beg =30
GlobalTime @ end =32
In process p2: cycle=2 j=13
GlobalTime @ beg =32
GlobalTime @ end =34
In process p2: cycle=2 j=12
GlobalTime @ beg =34
GlobalTime @ end =36
In process p2: cycle=2 j=11
GlobalTime @ beg =36
GlobalTime @ end =38
In process p2: cycle=2 j=10
GlobalTime @ beg =38
GlobalTime @ end =40
In process p2: cycle=2 j=9
GlobalTime @ beg =40
GlobalTime @ end =42
In process p2: cycle=2 j=8
GlobalTime @ beg =42
GlobalTime @ end =44
In process p2: cycle=2 j=7
GlobalTime @ beg =44
GlobalTime @ end =46
In process p2: cycle=2 j=6
GlobalTime @ beg =46
GlobalTime @ end =48
In process p2: cycle=2 j=5
GlobalTime @ beg =48
GlobalTime @ end =50
In process p2: cycle=2 j=4
GlobalTime @ beg =50
GlobalTime @ end =52
In process p2: cycle=2 j=3
GlobalTime @ beg =52
GlobalTime @ end =54
In process p2: cycle=2 j=2
GlobalTime @ beg =54
GlobalTime @ end =56
In process p2: cycle=2 j=1
GlobalTime @ beg =56
GlobalTime @ end =58
In process p2: cycle=2 j=0
GlobalTime @ beg =58
GlobalTime @ end =60
Finished or Deadlocked!
SystemC: simulation stopped by user.



                    ALL RIGHTS RESERVED}]
} {1}

