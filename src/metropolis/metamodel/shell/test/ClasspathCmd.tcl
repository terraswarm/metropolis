# Tests for ClasspathCmd
#
# @Author: Christopher Brooks
#
# @Version: $Id: ClasspathCmd.tcl,v 1.9 2005/11/23 22:56:26 allenh Exp $
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
####
#
test ClasspathCmd-1.1 {classpath reset} {
    classpath reset
    classpath show
} {   0 dirs in classpath}

######################################################################
####
#
test ClasspathCmd-1.2 {classpath add} {
    classpath reset
    # Doesn't work under Cygwin:
    # set dir "[java::field java.io.File separator]tmp"
    set dir [pwd]
    classpath add $dir
    # regsub {C:\\} [classpath show] {/} results
    regsub -all {\\} [classpath show] {/} results
    list $results
} "\{   1 dirs in classpath
   [pwd]\}"

######################################################################
####
#
test ClasspathCmd-1.3 {classpath add, multiple args, but same arg} {
    classpath reset
    # Doesn't work under Cygwin:
    # set dir "[java::field java.io.File separator]tmp"
    set dir [pwd]
    classpath add $dir $dir
    # regsub {C:\\} [classpath show] {/} results
    regsub -all {\\} [classpath show] {/} results
    list $results
} "\{   1 dirs in classpath
   [pwd]\}"

######################################################################
####
#
test ClasspathCmd-1.4 {classpath add, not enough args } {
    catch {classpath add} errMsg
    list $errMsg
} {{wrong # args: should be "classpath add file ?file ...?"}}

######################################################################
####
#
test ClasspathCmd-1.5 {classpath add, directory does not exist} {
    catch {classpath add notafile} errMsg
    list $errMsg
} {{Path 'notafile' does not exist}}

######################################################################
####
#
test ClasspathCmd-2.1 {classpath get on an empty classpath} {
    classpath reset
    classpath get
} {}

######################################################################
####
#
test ClasspathCmd-2.2 {classpath get} {
    classpath reset
    #set dir "[java::field java.io.File separator]tmp"
    #classpath add $dir
    #regsub {C:\\} [classpath get] {/} results
    #list $results

    # Unfortunately, when we add to the classpath, what we get back
    # is not exactly what went in.
    set dir [java::call System getProperty user.dir]
    set file [java::new java.io.File $dir]
    set canonicalFile [$file getCanonicalFile]
    classpath add $dir
    expr {"[lindex [classpath get] 0]" == "[$canonicalFile toString]"}
} {1}

######################################################################
####
#
test ClasspathCmd-2.3 {classpath get, too many args} {
    catch {classpath get foo} errMsg
    list $errMsg
} {{wrong # args: should be "classpath get"}}


######################################################################
####
#
test ClasspathCmd-3.1 {classpath remove} {
    classpath reset
    # Doesn't work under Cygwin:
    # set dir "[java::field java.io.File separator]tmp"
    set dir [pwd]
    classpath add $dir
    set classpaths [classpath get]
    classpath remove [lindex $classpaths 0]
    classpath show
} {   0 dirs in classpath}

######################################################################
####
#
test ClasspathCmd-3.2 {classpath remove, multiple args that are the same} {
    classpath reset
    # set dir "[java::field java.io.File separator]tmp"
    set dir [pwd]
    classpath add $dir
    set classpaths [classpath get]
    classpath remove [lindex $classpaths 0] [lindex $classpaths 0]
    classpath show
} {   0 dirs in classpath}


######################################################################
####
#
test ClasspathCmd-3.3 {classpath remove, not enough args} {
    catch {classpath remove} errMsg
    list $errMsg
} {{wrong # args: should be "classpath remove file ?file ...?"}}

######################################################################
####
#
test ClasspathCmd-3.4 {classpath remove, not in classpath} {
    classpath reset
    classpath remove foo
    classpath show
} {   0 dirs in classpath}

######################################################################
####
#
test ClasspathCmd-4.1 {classpath reset, wrong number of args} {
    catch {classpath reset foo} errMsg
    list $errMsg
} {{wrong # args: should be "classpath reset"}}


######################################################################
####
#
test ClasspathCmd-5.1 {classpath show, wrong number of args} {
    catch {classpath show foo} errMsg
    list $errMsg
} {{wrong # args: should be "classpath show"}}


######################################################################
####
#
test ClasspathCmd-6.1 {classpath error: not enough args} {
    catch {classpath} errMsg
    list $errMsg
} {{wrong # args: should be "classpath option ?arg ...?"}}

######################################################################
####
#
test ClasspathCmd-6.2 {classpath error: bogus subcommand} {
    catch {classpath foo} errMsg
    list $errMsg
} {{bad option "foo": must be add, get, remove, reset or show.}}
