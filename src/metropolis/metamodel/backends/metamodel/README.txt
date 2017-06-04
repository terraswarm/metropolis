================================================================
   Title : Metamodel Code Generator
   Author: Guang Yang
   Date  : Sep. 13, 2004
   $Id: README.txt,v 1.1 2004/09/14 01:49:47 guyang Exp $
================================================================
1. Introduction
================================================================
This backend tool takes the abstract syntax tree (AST) generated
by the compiler frontend, and then regenerate metamodel code
for the input metamodel code. The output files have the extension
'.out'. Semantically, the output files are the exactly the same
as the input files. There could be a bit difference of the file
format (e.g. tab, space, indentation, etc.) and some language
components (e.g. a class field 'x' could be changed to 'this.x').

This backend is usually used for checking syntax at the early
stage of design cycles. It is also able to generate a textual
view of the AST, which is extremely helpful for backend tool
developers to understand the AST structure.

================================================================
2. User's Guide
================================================================
This backend takes the following arguments:

  metacomp [-classpath <classpath>] [-dumpast]

-classpath <classpath>: Specify where to find the top level packages
-dumpast: Print out a textual copy of the AST


