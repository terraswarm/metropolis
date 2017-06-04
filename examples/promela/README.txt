MMM to Promela Translator -- README

A.     Translator
B.     Verification and Simulation using xspin (a tutorial)
C.     Simulation and Verification using only command-line spin

*** A. Translator ***

1) Usage

Use -promela switch to invoke the Promela backend and code generator:

   Under the working directory, type 
	 ../bin/metacomp -classpath .. -promela SimpleByte.mmm
   A Promela file "SimpleByte.pml" will be created.

To run the examples under this directory, simply type "make". For each .mmm
file, there is a LTL property file (.ltl) for verification testing purposes.

2) Restriction

   The input .mmm file should include the whole network description, i.e. 
netlist, interface, process and medium declarations should be put together.

   The MMM language elements currently NOT supported include:
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
   (9)	General constructs and operators: switch-case and conditional express
        (a?b:c)
   (10) constraint, boundedloop, nondeterminisim(always returns a 0 now), label,
        blackbox
   (11) Primitive data types: long, float, double, char
   (12) Inner classes
   (13)	await statement: keyword all


*** B. Verification and Simulation using xspin (a tutorial) ***

Spin and xSpin are freely available for download from:

  http://netlib.bell-labs.com/netlib/spin/whatispin.html

You may download the binary version for linux and windows, or compile your
own from the provided source.  Executables for icw.eecs.berkeley.edu has been
placed under:

/projects/hwsw/hwsw/sol2/bin/{xspin,spin}

You may set your DISPLAY appropriately and run xspin from icw.  Simply type
"xspin", and then open SimpleByte.pml from the GUI.

1) Verification of SimpleByte example.
  a)  We check property: 
      "A write can not take place until there is nothing in the medium"

  a.1) Insert verification flag in Simplebyte.mmm of the following form

           boolean write = false;
      
       in ByteM declaration

             write = true;
             write = false;

       in beginning of block(writebyte) of the medium ByteM, resulting in file 
       Simplebyte1.mmm

  a.2) Generate Simplebyte1.pml through translator.

  a.3) Type "xspin Simplebyte1.pml"
      (1) [Run][Set Verification Parameter]
          (choose "Safety" "Apply Never Claim" "Exhaustive" "Block New Msgs")
      (2) [Set Advanced Options]
          (choose "Stop at Error Nr: 1")
	  (Maximum Search Depth set to 1000000)
	  (Also set Physical Memory appropriately (probably 1000)) [Set]
      (3) [Verify an LTL Property]
          Type in

	  [] ( p -> q )
	  
	  [Generate]
	  
	  #define p (vByteM_bytem0_write == true) 
	  #define q (vByteM_bytem0_n == 0)
	  

      (4) [Run Verification]
      (5) [Run]

  a.4) Verification pass within 1 minute.  
       LTL file is stored in SimpleByte1.pml.ltl
      

  b)  We check property: 
      "A read can not take place until there is no space in the medium"

  b.1) Insert verification flag in Simplebyte.mmm of the following form

           boolean read= false;

       in ByteM declaration

             read = true;
             read = false;

       in beginning of block(readbyte) of the medium ByteM, resulting in file 
       Simplebyte2.mmm

  b.2) Generate Simplebyte2.pml through translator.

  b.3) Type "xspin Simplebyte2.pml"
      (1) [Run][Set Verification Parameter]
          (choose "Safety" "Apply Never Claim" "Exhaustive" "Block New Msgs")
      (2) [Set Advanced Options]
          (choose "Stop at Error Nr: 1")
	  (Maximum Search Depth set to 1000000)
	  (Also set Physical Memory appropriately (probably 1000)) [Set]
      (3) [Verify an LTL Property]
          Type in

	  [] ( p -> q )

	  [Generate]

	  #define p (vByteM_bytem0_read == true) 
	  #define q (vByteM_bytem0_space == 0)

      (4) [Run Verification]
      (5) [Run]

  b.4) Verification pass within 1 minute, with 1.5 million state visited.
       LTL file is stored in SimpleByte2.pml.ltl

  c)  We check property: 
       "If p0 wants to write and it is always possible for c0 to read, then
        p0 gets to write"
      
  c.1) Insert verification flag in Simplebyte.mmm of the following form

           boolean read_start= false;
           boolean write_start= false;
           boolean write_end= false;

       in ByteX declaration

             read_start = true;
             read_start = false;

       immediately before read and

             write_start = true;
             write_start = false;

       immediately before write and

             write_end = true;
	     write_end = false;
  
       immediate after write, resulting in file Simplebyte3.mmm

  c.2) Generate Simplebyte3.pml through translator.

  c.3) Type "xspin Simplebyte3.pml"
      (1) [Run][Set Verification Parameter]
          (choose "Safety" "Apply Never Claim" "Exhaustive" "Block New Msgs")
      (2) [Set Advanced Options]
          (choose "Stop at Error Nr: 1")
	  (Maximum Search Depth set to 150)
	  (Also set Physical Memory appropriately (probably 1000)) [Set]
      (3) [Verify an LTL Property]
          Type in

	  [] ( (p && ( [] <> q) ) -> (<> r) )

	  [Generate]

	  #define p (vByteX_p0_write_start == true) 
	  #define q (vByteX_c0_read_start == true)
	  #define r (vByteX_p0_write_end == true)

      (4) [Run Verification]
      (5) [Run]

  c.4) Verification failed as expected because p0 can be block from ever 
       succeed in write by p1 keep writing and c0 keep reading (i.e. if
       p1 have higher priority and always want to write).  This property
       may be verified if a scheduler is added to ensure fairness.

  c.5) [Run Guided Simulation]
       [Single Step]

       You will see that p0 first attempt a write (but not complete), then
       p1 also attempt a write (complete) which leads the c0 reading it.
       The latter action forms a cycle and p0 never finish write.


*** C. Simulation and Verification using Spin ***

1) To simulate the model, 
   a) spin -p SimpleByte.pml
   b) To see the output from simulation,  type spin SimpleByte.pml > output
  
2) Formal verification
   a) spin -a SimpleByte.pml
   b) gcc [-DSAFETY] [-DMA=376] [-DVECTORSZ=100000] -o run pan.c
   c) run -m400000

-------------------------------------------------------------------------------
Last Update 04/30/2004
Xi Chen & Harry Hsieh
