The Metropolis meta-model compiler developer's note

API DOCUMENTS
-------------
The documents of packages, classes, and methods used in the meta-model
compiler can be obtained by typing
    make docs
at $METRO/metropolis/metamodel.  Use an HTML browser to look at
    $METRO/metropolis/metamodel/doc/codeDoc/index.html
for specific information.


FILES
-----
This is a brief description of the directories in the meta-model compiler:

metamodel:               Common files shared by the front-end and back-end.
metamodel/frontend:      Meta-model frontend (parser + lexical analysis)
metamodel/backends:      Root directory for meta-model back-ends.
metamodel/nodetypes:     Description of the nodes in the AST.
metamodel/templates:     Templates for new java files, makefiles, etc.
metamodel/doc:           General documentation about the meta-model compiler.
metamodel/doc/codeDoc:   Documentation of the code of the compiler.


HOW TO...?
----------
1. How to add a new Java source file to a directory?
     - Use a template to write the file:
            metamodel/template/ClassTemplate.java
	    metamodel/template/InterfaceTemplate.java
     - Add the filename to variable JSRCS in the Makefile.

2. How to add a new non-Java source file to a directory?
     - Add new rules to the variable EXTRA_TARGETS in the Makefile.
     - IMPORTANT: Add your rules at the end of the file.
     - See "metamodel/Makefile" for an example

3. How to create a new source directory?
     - Create the directory in with 'mkdir'.
     - Copy the file metamodel/templates/Makefile to the directory.
     - Set the variables:
          ME   = frontend/parser   # Relative path FROM the root dir
          ROOT = ../..             # Relative path TO the root dir
	  SRC_DIRS =               # Any source subdirectory
	  JSRCS =                  # Java sources to be compiled
          EXTRA_TARGETS =          # Other kind of targets
     - Add the directory in the variable SRC_DIRS in the Makefile
       of the parent directory.   

4. How to add a new backend?
     - ./backends/README explains the steps.

5. How to add a backend into the metroshell?
     - ./shell/README explains it.

6. How to add a new library package?
     - read $METRO/../lib/README.
