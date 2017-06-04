# Tests for StringManip
#
# @Author: Christopher Brooks
#
# @Version: $Id: StringManip.tcl,v 1.4 2005/11/22 20:05:23 allenh Exp $
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

######################################################################
####
#
test StringManip-1.1 {baseFileName} {
    # [file join Foo bar.biz] returns "Foo/bar.biz" under Windows,
    # which will not work
    java::call metropolis.metamodel.StringManip baseFilename \
	"Foo[java::field java.io.File separatorChar]bar.biz" 
} {bar.biz}

######################################################################
####
#
test StringManip-1.2 {partAfterLast} {
    java::call metropolis.metamodel.StringManip partAfterLast Foo/bar.biz /
} {bar.biz}

######################################################################
####
#
test StringManip-1.2.1 {partAfterLast - char does not exist} {
    java::call metropolis.metamodel.StringManip partAfterLast Foo/bar.biz @
} {Foo/bar.biz}

######################################################################
####
#
test StringManip-1.2.2 {partAfterLast - First char} {
    java::call metropolis.metamodel.StringManip partAfterLast Foo/bar.biz F
} {oo/bar.biz}

######################################################################
####
#
test StringManip-1.2.3 {partAfterLast - Last char} {
    java::call metropolis.metamodel.StringManip partAfterLast Foo/bar.biz z
} {}

######################################################################
####
#
test StringManip-1.3 {partBeforeLast} {
    java::call metropolis.metamodel.StringManip partBeforeLast Foo/bar.biz /
} {Foo}
######################################################################
####
#
test StringManip-1.3.1 {partBeforeLast - char does not exist} {
    java::call metropolis.metamodel.StringManip partBeforeLast Foo/bar.biz @
} {Foo/bar.biz}

######################################################################
####
#
test StringManip-1.3.2 {partBeforeLast - First char} {
    java::call metropolis.metamodel.StringManip partBeforeLast Foo/bar.biz F
} {}

######################################################################
####
#
test StringManip-1.3.3 {partBeforeLast - Last char} {
    java::call metropolis.metamodel.StringManip partBeforeLast Foo/bar.biz z
} {Foo/bar.bi}

######################################################################
####
#
test StringManip-1.4 {unqualifiedPart} {
    java::call metropolis.metamodel.StringManip unqualifiedPart Foo/bar.biz
} {biz}

