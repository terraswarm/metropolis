# Tests for Help
#
# @Author: Christopher Brooks
#
# @Version: $Id: HelpCmd.tcl,v 1.13 2005/11/22 20:10:51 allenh Exp $
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
test test-Help-1.1 {help} {
    help
} {
metropolis-shell - General information about the metropolis shell

The Metropolis shell is intended to provide an interactive tool that
provides access to the tools in the Metropolis framework. It provides
commands to load meta-model specifications and perform some tasks
like simulation, code generation, etc.

A typical session begins by using the 'metroload' command to get a meta-model
specification in memory. Then, commands like 'simulate' or 'elaborate'
can be used to handle this specification.

Use 'help' followed by a topic name to get information about the topic.
Type 'help topics' to get the list of help topics available. Use 
'help tcl-commands' to get help on the Tcl commands supported by the
metropolis shell. Finally, you can learn about how the metropolis shell
can be invoked by typing 'help metropolis-invocation'.

SEE ALSO
  help  topics  tcl-commands  metropolis-invocation  
}

test test-Help-1.2 {help topics} {
    help topics
} {
elaborate              - Get compile-time information about the network used in a Meta-Model specification
metroload              - Parse and load a Meta-Model specification.
classpath              - Display or change the classpath variable.
network                - Display/modify the elaborated network
help                   - Print a help message for a given topic
metropolis-shell       - General information about the metropolis shell
metrolist              - List the classes/files/packages currently loaded
simulate               - Generate simulateable code for a specification.
tcl-commands           - Summary of the Tcl commands available in the Metropolis shell
metropolis-invocation  - Options of the invocation of the Metropolis shell
}

test test-Help-2.1 {help elaborate} {
    help elaborate
} {
elaborate - Get compile-time information about the network used in a Meta-Model specification

USAGE
    elaborate toplevelnetlist

Elaboration provides compile-time information about the structure of the
network in a Meta-Model specification. This is done by translating part
of the specification to Java code that is executed to obtain the 
structure of the network.

The top-level netlist is specified by providing its fully qualified name.
A class qualifies to be a top-level netlist if and only if:
- it has been previously loaded using the 'metroload' command.
- it is a public and non abstract netlist.
- it has a public constructor with exactly 0 arguments.

If a top-level netlist does not satisfy this three properties, a 
compile-time error will occur.

Some backends such as simulators might need that the elaborator phase is
performed before being invoked. See the help on the specific backend you
are using to check if it needs elaboration before running.
If Tcl global variables named 'java' or 'javac' exist, then their
values are passed to the backend as values of -java or -javac
arguments.

SEE ALSO
  load  simulate  
}

test test-Help-2.2 {help classpath} {
    help classpath
} {
classpath - Display or change the classpath variable.

USAGE
    classpath [ get | show | reset ]
    classpath [ add | remove ] dir ?dir ...?

The meta-model compiler keeps a list of directories where the top-level
meta-model packages (including system libraries) can be found. This list,
called 'classpath' is used whenever the compiler is looking for a
top-level package.

The classpath is initialized with the environment variable
'METRO_CLASSPATH'. This command allows us to view and change the
list of directories in the classpath. The options are:
- add: Add a new directory to the classpath list.
- get: Get the classpaths as a Tcl list.
- remove: Remove a directory from the classpath list
- reset: Clear the contents of the classpath. This command should 
  be handled with care
- show: Display the directories in the classpath list
If the user specifies a path that does not exist, is not a directory or
a directory that is (add) or is not (remove) in the classpath, that
directory is ignored

SEE ALSO
  load  
}

test test-Help-2.3 {help network} {
    help network
} {
network - Display/modify the elaborated network

USAGE
    network show ?node?
    network flatten
    network connect src port dst
    network refine node netlist
    network refineconnect netlist src port dst
    network redirectconnect netlist src srcport dst dstport
    network setscope node port netlist
    network addcomponent node netlist

This commands provides a mechanism to display/modify a network after
it is elaborated using the 'elaborate' command. All the subcommands
check that the operation that is performed abides by the semantics of
the meta-model.
The available subcommands are:
- show:            display information about a Node of the network;
                   by default, it displays the top-level netlist of
                   the network.
- flatten:         remove all refinements from the network; all
                   refined nodes disappear from the netlist, while
                   the remaining nodes become a component of the
                   top-level netlist.
- connect:         equivalent to a meta-model connect statement.
- refine:          equivalent to a meta-model refine statement.
- refineconnect:   equivalent to a meta-model refineconnect statement.
- redirectconnect: equivalent to a meta-model redirectconnect statement.
- setscope:        equivalent to a meta-model setscope statement.
- addcomponent:    equivalent to a meta-model addcomponent statement.

SEE ALSO
  elaborate  
}

test test-Help-2.4 {help network} {
    help network
} {
network - Display/modify the elaborated network

USAGE
    network show ?node?
    network flatten
    network connect src port dst
    network refine node netlist
    network refineconnect netlist src port dst
    network redirectconnect netlist src srcport dst dstport
    network setscope node port netlist
    network addcomponent node netlist

This commands provides a mechanism to display/modify a network after
it is elaborated using the 'elaborate' command. All the subcommands
check that the operation that is performed abides by the semantics of
the meta-model.
The available subcommands are:
- show:            display information about a Node of the network;
                   by default, it displays the top-level netlist of
                   the network.
- flatten:         remove all refinements from the network; all
                   refined nodes disappear from the netlist, while
                   the remaining nodes become a component of the
                   top-level netlist.
- connect:         equivalent to a meta-model connect statement.
- refine:          equivalent to a meta-model refine statement.
- refineconnect:   equivalent to a meta-model refineconnect statement.
- redirectconnect: equivalent to a meta-model redirectconnect statement.
- setscope:        equivalent to a meta-model setscope statement.
- addcomponent:    equivalent to a meta-model addcomponent statement.

SEE ALSO
  elaborate  
}

test test-Help-2.5 {help metropolis-shell} {
    help network
} {
network - Display/modify the elaborated network

USAGE
    network show ?node?
    network flatten
    network connect src port dst
    network refine node netlist
    network refineconnect netlist src port dst
    network redirectconnect netlist src srcport dst dstport
    network setscope node port netlist
    network addcomponent node netlist

This commands provides a mechanism to display/modify a network after
it is elaborated using the 'elaborate' command. All the subcommands
check that the operation that is performed abides by the semantics of
the meta-model.
The available subcommands are:
- show:            display information about a Node of the network;
                   by default, it displays the top-level netlist of
                   the network.
- flatten:         remove all refinements from the network; all
                   refined nodes disappear from the netlist, while
                   the remaining nodes become a component of the
                   top-level netlist.
- connect:         equivalent to a meta-model connect statement.
- refine:          equivalent to a meta-model refine statement.
- refineconnect:   equivalent to a meta-model refineconnect statement.
- redirectconnect: equivalent to a meta-model redirectconnect statement.
- setscope:        equivalent to a meta-model setscope statement.
- addcomponent:    equivalent to a meta-model addcomponent statement.

SEE ALSO
  elaborate  
}

test test-Help-2.6 {help metrolist} {
    help metrolist
} {
metrolist - List the classes/files/packages currently loaded

USAGE
    metrolist [ classes | files | pkgs ] ?

This command lists the classes, files or packages that are currently
loaded (in memory) in the metamodel compiler. Other classes that are
reachable through the classpath but have not been loaded yet will not
be shown with this command

SEE ALSO
  metroload  
}

test test-Help-2.7 {help simulate} {
    help simulate
} {
simulate - Generate simulateable code for a specification.

USAGE
    simulate [ java | promela | systemc ] [top-level-netlist] [regenerate]

This command invokes a simulation backend on the loaded specification
(must be loaded previously using the metroload command). The arguments after
selecting the simulator are passed directly to the appropriate simulator
backend.

 The simulator backends currently available are:
- java : Java based simulation.
- promela: Spin/Promela simulation and verification.
- systemc: System C based simulation.
top-level-netlist is the same as in command 'elaborate'
'regenerate' switch forces regenerate all the code, which is necessary after changing mapping information in synch.

SEE ALSO
  metroload  
}

test test-Help-2.8 {help metroload} {
    help metroload
} {
metroload - Parse and load a Meta-Model specification.

USAGE
    metroload [ file | class | pkg ] [ -classes | -expressions | -semantics ]
           name ?name ...?

The metroload command provides the mechanism to load meta-model specifications.
Users can specify: which specification will be loaded and which will be 
the level of semantic analysis used to analyze this command.
The specification can be selected using three formats:
- file + flag + filename: Loads the specified file. For example,
      metroload file -classes metro/myClass.mmm
- class + flag + classname: Loads a given class inside a package, given
the fully qualified name of the class. For example,
      metroload class -expressions metro.lib.Writer
- pkg + flag + packagename: Loads all classes inside a package, given the
fully qualified name of the package. For example,
      metroload pkg -expressions metro.lib
The level of semantic analysis can be specified using 3 flags:
- classes: Checks class declarations and inheritance.
- expressions: In addition to checking everything checked in 'classes',
checks variable names inside expressions. This also includes checking 
field accesses and method calls. After this pass every name in the ASTs
is linked to the proper declaration.
- semantics: In addition to everything checked in 'expressions',performs
type checking and performs meta-model specific checks.

SEE ALSO
  classpath  
}


test test-Help-2.9 {help tcl-commands} {
    help tcl-commands
} {
tcl-commands - Summary of the Tcl commands available in the Metropolis shell

All tcl built-in commands are available in the metropolis shell. This 
list is not exhaustive:
- exit/quit       leave the Metropolis shell.
- exec COMMAND    execute a command in the system shell.
- pwd             show the current working directory.
- cd DIR          change the working directory.
- set NAME ?VAL?  define the value of a variable. The value can be
                  accessed in any context as $NAME.
- expr EXPR       compute a mathematical expression that can contain 
                  variables defined using 'set'.
- glob/ls/dir     list all files that match a given pattern
- #COMMENT        comment until the end of this line
Other features that would be to long to describe here include defining
procedures, loops, conditionals, reading from file... These commands
are described in detail in any Tcl manual.

SEE ALSO
  metropolis-shell  metropolis-invocation  
}

test test-Help-2.10 {help metropolis-invocation} {
    help metropolis-invocation
} {
metropolis-invocation - Options of the invocation of the Metropolis shell

USAGE
    metroshell [-classpath path [ : paths]] [scriptfiles] [-ni]

The Metropolis shell invocation mechanism proceeds as follows:
- First, the classpath from the command line is read. The path to the
  Meta-Model library and the paths in METRO_CLASSPATH are automatically
  included in the classpath, and therefore they should not be added by
  the user.
- Then, the meta-model library classes are loaded.
- Then, the shell tests if the user has a file '.metroshrc' in their
  home directory. If it exists, it is executed. This file can change
  the classpath, define useful variables or even load meta-model files.
  All commands available in the shell can be used in this script.
- After that, the shell executes all the commands contained in the
  script files specified in the command line. Again, they can use all
  the commands available in the shell, and this can be used to
  automate tasks (e.g. loading a set of packages).
- Finally, if the flag '-ni' (no interactive) does not appear in the
  command line, the shell enters in interactive mode, until the command
  'exit' or 'quit' is used.

Users are encouraged to use '.metroshrc' and script files to automate
tasks and simplify the use of the Metropolis shell. It should be
emphasized that the entire shell can work non-interactively if the flag
'-ni' is provided and the script file to be executed is passed to the
command line

SEE ALSO
  metropolis-shell  
}

test test-Help-3.1 {Test out cmdProc} {
    catch {[help foo]} errmsg
    set errmsg
} {help about topic 'foo' not available}


test test-Help-3.2 {Test out cmdProc} {
    catch {[help foo bar]} errmsg
    set errmsg
} {wrong # args: should be "help ?topic?"}

test test-Help-3.3 {Test out cmdProc} {
    catch {[help foo bar bif]} errmsg
    set errmsg
} {wrong # args: should be "help ?topic?"}


