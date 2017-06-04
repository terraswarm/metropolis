lib/metamodel/README.txt
Version: $Id: README.txt,v 1.2 2003/10/29 00:48:07 cxh Exp $ 

CONTENTS and USAGE
------------------
This directory contains a set of meta-model packages:
lang	       : definitions of meta-model built-in classes
util	       : utility packages that can be used in the meta-model.
plt	       : platform libraries

Add $METRO/lib to your $METRO_CLASSPATH so that the compiler
can access these libraries.

You do not need to import lang and util packages by the import
statements, because these two packages are automatically loaded.


DEVELOPMENT
-----------
When you add a new package in this library, please make sure the
following:
 - In each meta-model file, specify the package name by using the
   package statement.  The name of the package should be relative
   to $METRO/lib, i.e. for a file in ./lang package, it must be
      package metamodel.lang;

 - For each package, create a file named "API.txt" that explains the
   classes and methods that can be accessed by the user of the
   package.  The format is the following.

	     Package <it's path from the top package>
	     =========================================
	     Class Summary:
	      <class name> : brief explanation
	       ...

             =========================================
	     Class <class name>

	     extends ... which implements ...

	     -----------------------------------------
	     Version: ...
	     Author: ...

	     -----------------------------------------
	     Constructors:

	     <class name>(... )
	       explanation
	     <class name>(... )
	       explanation

	     -----------------------------------------
	     Methods:

	     foo(...)
	       explanation
	     ...

	     =========================================
	     Class <class name>
	     ...

 - A README.txt file may be put for further remarks or notes.
