locgen README.txt
$Id: README.txt,v 1.4 2004/06/03 20:38:27 cxh Exp $

This example is used to test trace checker generation for LOC constraints.

Run the Test
============

1. Type "make": elaborate the network, generate and compile SystemC
   simulation code. An executable "run.x" will be created.

2. Type "./run.x". During the elaboration, for each LOC constraint
   defined in the network, a formula definition file ".constrN.loc"
   will be generated. The execution of "run.x" will generate a hidden
   trace file called ".trace".

2. Type "make checker": generate and compile trace checker files from
   formula definition files. For each ".constrN.loc" file, a
   executable checker "constrN.check" will be generated.

   NOTE: Checker generation needs the tool "locgen".
   The sources are in metro/src/util/locgen.
   Running "make checker" will build in that directory and install
   locgen in $METRO/bin if necessary.

3. To run the trace checking, run:
	./constr0.check .trace
	./constr1.check .trace
	./constr2.check .trace
	./constr3.check .trace

4. Type "make elaborate": elaborate the network only, print out the
   elaborated network, and generate LOC constraint specifications 
   (.constrN.loc files) only.


Trace Checker Generation
========================

The procedure of trace checker generation for LOC constraints is
illustrated in README.ps.


Limitations
===========

The LOC constraint can only includes the events like beg(process,
object.label) and end(process, object.label). The label could be from
either labeled statements or labeled blocks.

1. The built-in constraints are not supported. 

2. LOC constraints can only contain integer variables and arrays are
   not supported (e.g. data[a@(event1, i)] cannot be handled yet).

3. In LOC constraints, the index variable has to be i.

---------------------
Xi Chen & Harry Hsieh

