# Tests for the Compiler class
#
# @Author: Christopher Brooks
#
# @Version: $Id: Compiler.tcl,v 1.29 2005/11/23 17:08:34 allenh Exp $
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs iterToObjects] == "" } then {
    source [ file join $METRO util testsuite enums.tcl]
}

if {[info procs jdkCapture] == "" } then {
    source [ file join $METRO util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


# Do not exit when help is called
java::call System setProperty "metro.doNotAutoExit" "true"

######################################################################
####
#
test Compiler-1.1 {-help} {
    jdkCaptureErr {
	set args [java::new {String[]} 1 [list "-help"]]
	java::call metropolis.metamodel.Compiler main $args
	#java::call metropolis.metamodel.Compiler printHelp
    } output
    list $output
} {{
class metropolis.metamodel.Compiler:
       A compiler for the meta-model language, the internal representation
       used in the Metropolis design framework

Usage: java metropolis.metamodel.Compiler [options] source_files
         
where options include:
   -? -help -h                Display this help message
   -classpath <classpath>     Specify where to find user class files
   -metamodel [-dumpast]      Use the metamodel back-end (default)
                              -dumpast prints a dump of the AST to STDOUT
   -javasim [-top <netinit>] [-java <path>/java] [-javac <path>/javac]
                              Generate java simulation code (semantics 3.0)
                              [javasim not shipped in Metropolis 1.0]
   -cpp                       Generate C++ simulation code
                              [cpp not shipped in Metropolis 1.0]
   -simulator                 Generate java simulation code
                              [simulator not shipped in Metropolis 1.0]
   -systemc [-top <netinit>] [-java <path>/java] [-javac <path>/javac]
            [-w] [-noic] [-mmdebug]
                              Generate SystemC simulation code
                              -top Fully qualified top level netlist name
                              -java/javac Java virtual machine and
                                 java compiler
                              -w Regenerate systemc code regardless the time
                                 stamps of mmm and systemc files
                              -noic No interleaving concurrent
                                 specific optimization
                              -mmdebug Support mmm level debugging
                                  in simulation
   -elaborator [-dir <dir>] [-java <path>/javac] <netinit>
                              Compute the structure of the network
                              -dir specify the directory to create elaboration
                                 products in (default is current directory)
                              -javac specify the java compiler
   -promela                   Generate Promela/Spin code
   -runtimetest runtime/constraint
                              runtime Test the runtime library
                              constraint Test the constraint
                              elaboration and show the results
   -cfa                       Generate a CFA; Visual, KISS,
                              and RML representations
                              [cfa not shipped in Metropolis 1.0]

}}

######################################################################
####
#
test Compiler-1.2 {no arguments} {
    # Uses 1.1 above
    jdkCaptureErr {
	set args [java::new {String[]} 0 {}]
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{
class metropolis.metamodel.Compiler:
       A compiler for the meta-model language, the internal representation
       used in the Metropolis design framework

Usage: java metropolis.metamodel.Compiler [options] source_files
         
where options include:
   -? -help -h                Display this help message
   -classpath <classpath>     Specify where to find user class files
   -metamodel [-dumpast]      Use the metamodel back-end (default)
                              -dumpast prints a dump of the AST to STDOUT
   -javasim [-top <netinit>] [-java <path>/java] [-javac <path>/javac]
                              Generate java simulation code (semantics 3.0)
                              [javasim not shipped in Metropolis 1.0]
   -cpp                       Generate C++ simulation code
                              [cpp not shipped in Metropolis 1.0]
   -simulator                 Generate java simulation code
                              [simulator not shipped in Metropolis 1.0]
   -systemc [-top <netinit>] [-java <path>/java] [-javac <path>/javac]
            [-w] [-noic] [-mmdebug]
                              Generate SystemC simulation code
                              -top Fully qualified top level netlist name
                              -java/javac Java virtual machine and
                                 java compiler
                              -w Regenerate systemc code regardless the time
                                 stamps of mmm and systemc files
                              -noic No interleaving concurrent
                                 specific optimization
                              -mmdebug Support mmm level debugging
                                  in simulation
   -elaborator [-dir <dir>] [-java <path>/javac] <netinit>
                              Compute the structure of the network
                              -dir specify the directory to create elaboration
                                 products in (default is current directory)
                              -javac specify the java compiler
   -promela                   Generate Promela/Spin code
   -runtimetest runtime/constraint
                              runtime Test the runtime library
                              constraint Test the constraint
                              elaboration and show the results
   -cfa                       Generate a CFA; Visual, KISS,
                              and RML representations
                              [cfa not shipped in Metropolis 1.0]

Error: source files expected
  Use -help to get help
}}

######################################################################
####
#
test Compiler-3.1 {null second arg} {
    set args [java::new {String[]} 2 \
	[list "-metamodel"]]	
    catch {java::call metropolis.metamodel.Compiler main $args} \
	output
    list $output
} {{java.lang.RuntimeException: Compiler: Internal error: argument #1 is null.}}


######################################################################
####
#
test Compiler-3.2 {empty argument} {
    set args [java::new {String[]} 1 {-}]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: empty flag '-' in command line
  Use -help to get help
}}

######################################################################
####
#
test Compiler-3.3 {filename before -arg} {
    set args [java::new {String[]} 2 {foo.mmm -bar }]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: all flags must appear before source files, arg was '-bar'
  Use -help to get help
}}

######################################################################
####
#
test Compiler-3.4 {no sources} {
    set args [java::new {String[]} 2 [list {-classpath} {$METRO/lib}]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: source files expected after class-paths
  Use -help to get help
}}

######################################################################
####
#
test Compiler-3.5 {unknown arg that starts with a -} {
    set args [java::new {String[]} 2 [list {-unknownArg} {$METRO/lib}]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: unknown flag '-unknownArg'
  Use -help to get help
}}

######################################################################
####
#
test Compiler-4.0 {classpath without a path} {
    set args [java::new {String[]} 1 [list {-classpath}]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: wrong classpath, path expected
  Use -help to get help
}}


######################################################################
####
#
test Compiler-4.1 {-classpath with non-existent path} {
    set args [java::new {String[]} 2 \
		  [list {-classpath} {/this/path/does/not/exist}]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: source files expected after class-paths
  Use -help to get help
}}

######################################################################
####
#
test Compiler-4.2 {-classpath does not exist + sourcefile} {
    set args [java::new {String[]} 3 \
		  [list {-classpath} "/doesnot/exist" foo.mmm]]
    catch {
	java::call metropolis.metamodel.Compiler main $args
    } errMsg
	jdkStackTrace
    regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
    list $errMsg2
} {{java.lang.RuntimeException: Failed to initialize libraries. metropolis classpath was:
}}

######################################################################
####
#
test Compiler-4.3 {-classpath with two element path} {
    set args [java::new {String[]} 4 \
		  [list {-classpath} $METRO/src ":" $METRO/lib]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: source files expected after class-paths
  Use -help to get help
}}

######################################################################
####
#
test Compiler-4.4 {-classpath with two : in a row} {
    set args [java::new {String[]} 4 \
		  [list {-classpath} $METRO/src ":" ":"]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: ':' used where a path was expected
  Use -help to get help
}}

#######################
# Backends below here 
#######################

######################################################################
####
#
if { [file exists "../backends/cfa"] } {
    test Compiler-10.0 {-cfa, non-existent file} {
        set args [java::new {String[]} 4 \
                      [list {-classpath} $METRO -cfa foo.mmm]]
        catch {
            java::call metropolis.metamodel.Compiler main $args
        } errMsg
        regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
        list $errMsg2
    } {{java.lang.RuntimeException: Path foo.mmm does not exist}}
}

######################################################################
####
#
if { [file exists "../backends/cpp"] } {
    test Compiler-11.0 {-cpp, non-existent file} {
        set args [java::new {String[]} 4 \
                      [list {-classpath} $METRO -cpp foo.mmm]]
        catch {
            java::call metropolis.metamodel.Compiler main $args
        } errMsg
        regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
        list $errMsg2
    } {{java.lang.RuntimeException: Path foo.mmm does not exist}}
}

######################################################################
####
#
test Compiler-12.0 {-elaborator} {
    set args [java::new {String[]} 3 \
		  [list {-classpath} $METRO -elaborator]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: elaborator needs the fully qualified name of the net-initiator netlist
  Use -help to get help
}}

######################################################################
####
#
if { [file exists "../backends/javasim"] } {
    test Compiler-13.0 {-javasim, non-existent file} {
        set args [java::new {String[]} 4 \
                      [list {-classpath} $METRO -javasim foo.mmm]]
        catch {
            java::call metropolis.metamodel.Compiler main $args
        } errMsg
        regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
        list $errMsg2
    } {{java.lang.RuntimeException: Path foo.mmm does not exist}}
}


######################################################################
####
#
test Compiler-14.0 {-metamodel, non-existent file} {
    set args [java::new {String[]} 4 \
		  [list {-classpath} $METRO -metamodel foo.mmm]]
    catch {
	java::call metropolis.metamodel.Compiler main $args
    } errMsg
    regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
    list $errMsg2
} {{java.lang.RuntimeException: Path foo.mmm does not exist}}

######################################################################
####
#
test Compiler-14.1 {-metamodel, -dumpast, non-existent file} {
    set args [java::new {String[]} 5 \
		  [list {-classpath} $METRO -metamodel -dumpast foo.mmm]]
    catch {
	java::call metropolis.metamodel.Compiler main $args
    } errMsg
    regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
    list $errMsg2
} {{java.lang.RuntimeException: Path foo.mmm does not exist}}

######################################################################
####
#
test Compiler-14.2 {-metamodel, -mmdebug, non-existent file} {
    set args [java::new {String[]} 5 \
		  [list {-classpath} $METRO -metamodel -mmdebug foo.mmm]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{"-mmdebug" must appear after "-systemc", only supported for "-systemc".  Ignored.
  Use -help to get help
}}


######################################################################
####
#
test Compiler-15.0 {-promela, no args} {
    set args [java::new {String[]} 1 \
		  [list {-promela}]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: source files expected
  Use -help to get help
}}

######################################################################
####
#
test Compiler-16.0 {-runtimetest, no args} {
    set args [java::new {String[]} 1 \
		  [list {-runtimetest}]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{Error: runtimetest needs an option and the fully qualified name of the net-initiator netlist
  Use -help to get help
}}

# We do not necessarily ship the simulator backend
if [file exists $METRO/src/metropolis/metamodel/backends/simulator] {

    ######################################################################
    ####
    #
    test Compiler-17.0 {-simulator, no args} {
	set args [java::new {String[]} 1 \
		      [list {-simulator}]]
	jdkCaptureErr {
	    java::call metropolis.metamodel.Compiler main $args
	} output
	list $output
    } {{Error: source files expected
  Use -help to get help
}}

    ######################################################################
    ####
    #
    test Compiler-17.1 {-simulator, null args} {
	set args [java::new {String[]} 20 \
		      [list {-simulator}]]
	catch {
	    java::call metropolis.metamodel.Compiler main $args
	} errMsg
	list $errMsg
    } {{java.lang.RuntimeException: Compiler: Internal error: argument #1 is null.}}

    ######################################################################
    ####
    #
    test Compiler-17.2 {-simulator, non-existent file} {
        set args [java::new {String[]} 4 \
                    [list {-classpath} $METRO -simulator foo.mmm]]
        catch {
            java::call metropolis.metamodel.Compiler main $args
        } errMsg
        regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
        list $errMsg2
    } {{java.lang.RuntimeException: Path foo.mmm does not exist}}
}


######################################################################
####
#
test Compiler-18.0 {-systemc, non-existent file} {
    set args [java::new {String[]} 4 \
		  [list {-classpath} $METRO -systemc foo.mmm]]
    catch {
	java::call metropolis.metamodel.Compiler main $args
    } errMsg
    regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
    list $errMsg2
} {{java.lang.RuntimeException: Path foo.mmm does not exist}}


######################################################################
####
#
test Compiler-18.1 {-systemc, -dumpast non-existent file} {
    set args [java::new {String[]} 5 		  [list {-classpath} $METRO -systemc -dumpast foo.mmm]]
    jdkCaptureErr {
	java::call metropolis.metamodel.Compiler main $args
    } output
    list $output
} {{"-dumpast" only supported for "-metamodel".  Ignored.
  Use -help to get help
}}


######################################################################
####
#
test Compiler-18.2 {-systemc, -dumpast non-existent file} {
    set args [java::new {String[]} 5 \
		  [list {-classpath} $METRO -systemc -mmdebug foo.mmm]]
    catch {
	java::call metropolis.metamodel.Compiler main $args
    } errMsg
    regsub { [^ ]*foo.mmm} $errMsg { foo.mmm} errMsg2
    list $errMsg2
} {{java.lang.RuntimeException: Path foo.mmm does not exist}}


######################################################################
####
#
test Compiler-19 {classPathToString} {
    set args [java::new {String[]} 4 \
		  [list {-classpath} /foo : /bar ]]
    # Ignore the error, we only want to set the path	
    catch {
	java::call metropolis.metamodel.Compiler main $args
    } errMsg
    list [java::call metropolis.metamodel.Compiler classPathToString]
} {{/foo, /bar}}


