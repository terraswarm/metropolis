# Tests for CompileUnit
#
# @Author: Christopher Brooks
#
# @Version: $Id: CompileUnit.tcl,v 1.8 2005/11/22 20:12:08 allenh Exp $
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
test Test-CompileUnit-1.1 {Construct at CompileUnit, call get and set} {
    set systemCCodegenVisitor \
	[java::new metropolis.metamodel.backends.systemc.SystemCCodegenVisitor]
    set headerCodegen \
	[java::new metropolis.metamodel.backends.systemc.HeaderCodegen \
	     $systemCCodegenVisitor]
    set compileUnit \
	[java::new metropolis.metamodel.backends.systemc.CompileUnit \
	     $headerCodegen [java::null] ]

    $compileUnit setFilename compileUnitFile
    $compileUnit setMMMFilename compileUnitMMMFile

    list \
	[$compileUnit getBaseName] \
	[$compileUnit getCppBaseName] \
	[$compileUnit getCppTargetName] \
	[$compileUnit getHBaseName] \
	[$compileUnit getHTargetName] \
	[$compileUnit getMMMFileName] \
	[$compileUnit getHasDebuggerInfo]

} {compileUnitFile compileUnitFile.cpp compileUnitFile.cpp compileUnitFile.cpp compileUnitFile.h compileUnitMMMFile 0}

######################################################################
####
#
test Test-CompileUnit-1.2 { getHeaderCache} {
    # Uses setup in 1.1 above
    list [$headerCodegen equals [$compileUnit getHeaderCache]]
} {1}

######################################################################
####
#
test Test-CompileUnit-1.3 { save} {
    # Uses setup in 1.1 above

    catch {
	file delete -force 	[$compileUnit getCppTargetName]
	file delete -force 	[$compileUnit getHTargetName]
    }
    $compileUnit save true "//includes go here\n"	 
    set cppFile [open [$compileUnit getCppTargetName]]
    set hFile [open [$compileUnit getHTargetName]]
    set cppFileContents [read $cppFile]
    set hFileContents [read $hFile]
    close $cppFile
    close $hFile
    list $cppFileContents "\n" $hFileContents
} {{//includes go here
// begin static variables
// end static variables


} {
} {#ifndef MMSCS_COMPILEUNITFILE_H
#define MMSCS_COMPILEUNITFILE_H
#include "library.h"

#endif
}}
