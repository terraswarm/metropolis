examples/producers_consumer/README.txt
Version: $Id: README.txt,v 1.1 2006/08/18 00:24:09 guyang Exp $

This directory contains a small meta-model files for a single
netlist with two writers and a single reader.
The point of this example is to use LTL constraints to ensure
the mutual exclusions between read and write operations.
Compare this example with the one in ../producers_consumer
to understand the distinction.

===========================================================
SystemC 2.0 simulation
* Makefile
 - type make from the command line; this will create run.x in the
   current directory.
 - type ./run.x -ltl

Note: You may see an interleaving of two writeInt operations
initiated by Producer1 and Producer2. e.g.
monitor> Producer1: write 0 BEGIN_RT   (1)
monitor> Producer2: write 10 BEGIN_RT  (2)
monitor> Producer1: write END_RT       (3)
In fact, this is not a violation to the mutex LTL constraints, 
because (2) and (3) are scheduled to run at exactly the same time, 
but due to the SystemC kernel, (2) happens to be scheduled before (3).

