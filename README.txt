$Id: README.txt,v 1.30 2006/10/12 00:45:28 cxh Exp $

# contents of this directory
README.txt : this file
bin	: scripts for the metroshell and some backend tools
doc	: documents for the Metropolis meta-model and their libraries
examples: Metropolis meta-model examples
lib	: Metropolis meta-model libraries
src	: Metropolis meta-model compiler and backend tools

Metropolis-1.1: Design Environment for Heterogeneous Systems
Send questions and bug reports to metro_comp@ic.eecs.berkeley.edu.
See also the Metropolis website: http://www.gigascale.org/metropolis

The Metropolis meta-model compiler
----------------------------------
The Metropolis meta-model consists of the frontend and a set of
backend tools.  The frontend parses meta-model files, creates
Abstract Syntax Trees (ASTs), and applies a series of passes on the
ASTs to make resolutions and semantic analysis. A Backend tool
browses and analyzes the ASTs to generate a model appropriate for the
backend. For example, a backend for a simulation tool in a particular
language generates files that describe the meta-model design in the
language.


INSTALLATION
------------
The meta-model compiler requires Java Development Kit (JDK) and a C
compiler for installation.  The compilation has been tested under the
following environments:

* Linux (Our primary in house development platform)
 - OS:		Fedora Core 2 Linux
 - JDK:		Java 1.4 or later
		We use exception chaining, which requires Java 1.4.
		JDK 1.4 under versions of RedHat earlier that RH 8.0
		hung while compiling code in 
		src/metropolis/metamodel/backends/elaborator/
		JDK 1.4.2_01 and later seems to work fine.
		Use the Sun JDK, not GNU gcj.
		If "/usr/bin/java --version" returns:
		  java version "1.4.2"
		  gij (GNU libgcj) version 4.1.1 20060525 (Red Hat 4.1.1-1)
		Then you might have problems loading asts from files.
		The solution is to use the Sun JDK,

 - C compiler:	gcc 3.2, gcc 3.3.3 is preferred because of problems
                setting breakpoints with gcc-3.2.
		gcc-4.1.1 also works.


* Solaris (The nightly build runs under Solaris)
 - OS:		Sun Solaris 2.8
 - JDK:		1.4 or later
		We use exception chaining, which requires Java 1.4.
		Locally, we use Java 1.4.2_06
 - C compiler:	gcc 3.2.2, gcc 3.3 is preferred because of debugger
                problems.  Locally, we use gcc-3.2.2
		gcc-2.95.2 will _not_ work because of template issues
                in backends/systemc

* Windows under Cygwin (SystemC-2.0.1 does not work under Cygwin,
	   use SystemC-2.1, see $METRO/doc/coding/cygwin.htm)
 - OS		Windows XP
 - JDK		1.4 or later
		We use exception chaining, which requires Java 1.4.
		Locally, we use Java 1.5
		Download the Windows JDK .exe
		 from http://java.sun.com/downloads
 - C compiler:  gcc 3.3.3

Note that many of the SystemC-2.0.1 regression tests fail under
Cygwin.  See doc/coding/cygwin.htm for details.  These failures have
nothing to do with Metropolis, they are problems with Cygwin and
SystemC-2.0.1.  The solution is to use SystemC-2.1

You can install all of Cygwin or else a subset that includes 
make, gcc and other programs.
Such a subset may be found at:
http://ptolemy.eecs.berkeley.edu/ptolemyII/ptIIlatest/cygwin.htm
See $METRO/doc/coding/cygwin.htm for details about SystemC installation.


* Windows under Microsoft VisualStudio 6.0 (Incomplete)
 - OS		Windows XP
 - JDK		1.4 or later
		We use exception chaining, which requires Java 1.4.
 - C compiler:  Microsoft VisualStudio 6.0 sp4
Note that support for MSVC is incomplete, it is best if you
read doc/coding/msvc.htm, follow the Cygwin installation and 
complete the msvc installation.



Take the following steps for installation. 

1. Download and install the Java Development Kit (JDK).
Most users will download the JDK from
http://java.sun.com/downloads.
Add the JDK bin directory to your path so that
<CODE>javac</CODE> and <CODE>java</CODE> are available.

2. Install System C 2.1 from http://www.systemc.org in 
either /usr/local/systemc-2.1 or ~/src/systemc-2.1

SystemC 2.0 will work, though we have tested primarily under
SystemC 2.1 and SystemC 2.0.1.

The following directories are searched for System C installations
in the following order

   1. $HOME/src/systemc-2.1
   2. $HOME/src/systemc-2.1-beta
   3. $HOME/src/systemc_2_1.oct_12_2004.beta
   4. $HOME/src/systemc-2.1beta11
   5. $HOME/src/systemc-2.0.1	
   6. $HOME/src/systemc-2.0
   7. /usr/local/systemc-2.1
   8. /usr/local/systemc-2.1-beta
   9. /usr/local/systemc_2_1.oct_12_2004.beta
   10. /usr/local/systemc-2.1beta11
   11. /usr/local/systemc-2.0.1
   12. /usr/local/systemc-2.0

Or, you may use the configure --with-systemc flag, for example:
    ./configure --with-systemc=/foo/systemc-2.1-beta

3. Define the METRO environment variable to the location
of the Metropolis directory
   
For example, under csh, you might do
  % setenv METRO /home/metro

METRO should point to the directory where this file is found.
i.e. the current directory should become $METRO

Note that under Cygwin bash, you should _not_ do
  % export METRO=`pwd`
Instead, do
  % export METRO=c:/src/metro
This is because under Cygwin bash, `pwd` is likely to return a path
that includes "cygdrive" and Java does not understand the
"cygdrive" notation.

METRO_CLASSPATH can be used to specify a set of directories for top-level
packages at which meta-model designs to be compiled are
located. Multiple directories are separated by the standard path-separator
character for your platform, that is, a colon (:) on Unix systems, or a
semicolon (;) on Windows systems.  It is wise to include $METRO/lib
and $METRO/examples by default.  For example,
   % setenv METRO_CLASSPATH "$METRO/examples:$METRO/lib"
or, in Cygwin bash on Windows:
   % export METRO_CLASSPATH="$METRO/examples;$METRO/lib"
(Note the semicolon).
If nothing is specified, set it as empty:
   % setenv METRO_CLASSPATH

4. Run configure
./configure

This will check for Java and the C/C++ compilers,
read in $METRO/mk/metro.mk.in and generate $METRO/mk/metro.mk

5. Compile:
make >& make.out

Instructions for Eclipse users can be found in $METRO/doc/coding/eclipse.htm


BASIC USAGE
-----------
The metroshell can be found at $METRO/bin/metroshell.
(The metroshell command created by running make in $METRO/bin)

It will give a metropolis> prompt, and type help
for available commands and their information.

The metroshell can be used in a batch mode.  To do this, prepare a
file, say myscript, which lists a sequence of metroshell commands, and
then type:
  metroshell -ni myscript
See $METRO/examples/test/shellcmd for an example of the file.

If metroshell commands are put in a file .metroshrc located at the home
directory, these commands are executed in the very beginning when the
metroshell starts up.  This can be used for initialization.  The
following is an example.

% more ~/.metroshrc
# comment: my initial set up for the Java locations
set JDK /usr/local/j2sdk1.4.2_06
set java  $JDK/bin/java
set javac $JDK/bin/javac
classpath add ..
%

Note that one cannot use Unix environment variables in the second
argument of the set command in .metroshrc.


Various backends can be invoked using another shell script
$METRO/bin/metacomp. The basic usage of this script is:

  metacomp [options] source_files

The option "-h" displays the help for the meta-model compiler, including the
complete list of options of the compiler. For example, the options allow the
user to specify the back-end module that will be invoked once the meta-model
file has been parsed.



EXAMPLES
--------
There is an example at $METRO/examples/producers_consumer.

README.txt file in the directory explains the example and how to use
the meta-model compiler and a System C simulation backend tool.


WRITING SPECIFICATIONS WITH THE META-MODEL 
------------------------------------------

Files written in the meta-model language should be text files with
extension "mmm" (initials for "Metropolis Meta-Model"). The user
invoking the compiler should have enough privileges to READ the
source files and CREATE files in the same directory where the source
files are stored.

Meta-model sources can be organized in packages, in the same way as
Java packages. All source files of a package are stored in a single
directory.  Subpackages of a package are stored in a subdirectory of
the parent package, thus creating a directory hierarchy.

The compiler has to know all the directories where it can find top
level packages (packages with no parent, like "java" in the Java
language).  This set of directories, known as the "classpath", can be
specified in the METRO_CLASSPATH environment variable.  Users can list
the directories in the classpath, separated by the standard
path-separator character for their system -- for example, a colon (:)
on Unix systems, or a semicolon (;) on Windows systems.  Some examples
of classpath variable (using csh syntax) would be:

 * If some meta-model packages that we need to use in the compilation are 
   stored in the paths "/hm/mysystem" and "/hm/research", then
   % setenv METRO_CLASSPATH "/hm/mysystem:/hm/research"

 * An example on a Windows platform might be:
   % setenv METRO_CLASSPATH "c:/metro/mysystem;e:/mylibs/sometree"

   (Here each directory specification is prefixed by its drive ID,
   andis separated from other directories in the class path by a
   semicolon.)

 * No user-defined package is available, then
   % setenv METRO_CLASSPATH

 * The only package available is in "/usr/local/mmm/pkg", then
   % setenv METRO_CLASSPATH /usr/local/mmm/pkg

If user-defined meta-model packages are not needed in the compilation,
the variable can be defined empty. Remember to use "" to enclose the
list of paths if you define more than one directory.  One can use the
classpath command of the metroshell to register new paths as
classpaths. These paths are recognized as valid classpaths until the
metroshell is quit.  For example, classpath add /usr/local/mmm/designs
/hm/myname/mydesigns will add the two paths as the classpath.


TEST
----
A regression test can be performed on some examples included in this 
release.  

The regression test can be performed by typing 
    cd $METRO
    make tests

The output is rather voluminous, and could take a few minutes to
run, even on a fast machine.
The output consists of quite a bit of text with lines like:

Failed: 0  Total Tests: 2  ((Passed: 2, Newly Passed: 0)  Known Failed: 0) /export/home2/bldmastr/metro/src/util/testsuite/test

interspersed.



