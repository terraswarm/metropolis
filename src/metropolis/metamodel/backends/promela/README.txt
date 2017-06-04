src/metropolis/metamodel/backends/promela/README.txt
Version: $Id: README.txt,v 1.4 2004/09/12 19:09:55 xichen Exp $

MMM to Promela Translator

Note: Post-processing requires SPIN or XSPIN, see B. below

General Deception:

   The promela backend is essentially a translator from Metropolis designs 
to Promela, a formal verification language of the model checker Spin. Spin 
is a formal verification tool for asynchronous software systems and is chosen 
as a backend verification engine.  
   The MMM description is automatically translated into Promela description, 
and the properties are checked using SPIN model checker. The designer may perform 
any synthesis step (e.g. composition, decomposition, constraint addition, 
scheduler assignment) and a new Promela code can be automatically generated 
to verify the property.  If it does not pass, the error trace may be used to 
help designers figure out whether the design needs to be altered.  If the 
verification session runs too long, approximate verification can be used to 
explore a subset of the state space and report the probability that the property 
will pass.  Obviously, a partial exploration can not prove that a property holds.  
However, it is our experience that a lot of ``easy'' bugs can be found within a 
relatively small amount of time and memory usage. If a SPIN verification session 
continues to run after a long time, it is highly likely that the property will 
eventually pass.


A.     Translator
B.     Verification and Simulation using xspin (a tutorial)
C.     Simulation and Verification using only command-line spin

*** A. Translator ***

1) Usage

Use -promela switch to invoke the Promela backend and code generator:

   Under the working directory, type 
	 metacomp -classpath .. -promela SimpleByte.mmm
   A Promela file "SimpleByte.pml" will be created.

2) Restriction

   The input .mmm file should include the whole network description, i.e. 
netlist, interface, process and medium declarations should be put together.

   The MMM language elements NOT currently supported are:
   (1)  scheduler 
   (2)  statemedium
   (3)  Multiple-level netlist, method declared in netlist
   (3)  Inheritance of process or medium
   (4)  Constructor of process or medium
   (5)  medium-medium connection
   (6)  setscope, refine, refineconnect 	
   (7)	getnthconnectionsrc, getnthconnectionport, getconnectionnum,  
        redirectconnect
   (8)  Compound data types defined in Java and supported in MMM: Array, List, 
        Set, Stack and String
   (9)  template
   (10)	General constructs and operators: switch-case and conditional express
        (a?b:c)
   (11) constraint, boundedloop, nondeterminisim(always returns a 0 now), label,
        blackbox
   (12) Primitive data types: long, float, double, char
   (13) Inner classes
   (14)	await statement: keyword all

   
*** B. Verification and Simulation using xspin (a tutorial) ***

spin and xspin are freely available for download from:

  http://netlib.bell-labs.com/netlib/spin/whatispin.html

You may download the binary version for linux and windows, or compile your
own from the provided source.  Executables for icw.eecs.berkeley.edu has been
placed under:

/projects/hwsw/hwsw/sol2/bin/{xspin,spin}

You may set your DISPLAY appropriately and run xspin from icw.  Simply type
"xspin", and then open SimpleByte.pml from the GUI.

An example is found in metro/examples/promela.


*** C. Simulation and Verification using Spin ***
1) To simulate the model, 
   a) spin -p SimpleByte.pml
   b) To see the output from simulation,  type spin SimpleByte.pml > output
  
2) Formal verification
   a) spin -a SimpleByte.pml
   b) gcc [-DSAFETY] [-DMA=376] [-DVECTORSZ=100000] -o run pan.c
   c) run -m400000
