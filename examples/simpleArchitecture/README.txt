simple architecture model example
Sept 6, 2004
Haibo Zeng
==============================================================================

This example is to show how an architecture works by "simulating" some 
functionality in the architecture. To do this, the thread() fucntion in 
each process is modified to have a deterministic behavior instead of 
non-determinism as in a normal architecture. 

Please refer to the pip design tutorial (?) chapter 3 for the detailed description of this architecture. 

-----
The architecture is initialized composing of one CPU/RTOS, one bus, one
memory and two software tasks.

