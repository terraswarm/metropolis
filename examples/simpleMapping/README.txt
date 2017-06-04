"simple" system example
July 21, 2004
Abhijit Davare
==============================================================================

Files
-----
README.txt: This file
func.mmm: Functional network
arch.mmm: Architectural network
mapper.mmm: Mapping network

The functional network contains a base class - "SimpleProcess" - and a number 
of subclasses. The subclasses (Source, Sink, Source, Sink, and Connect) are 
named based on the number of I/O ports they have. The processes initially 
communicate using yapi channels, these can either be refined to use TTL
or shared memory.

The architectural network is trivial and consists of a number of tasks 
executing concurrently. There are no shared media or quantity managers.
The services provided by the tasks are read, write, and execute.

The mapping network instantiates the functional network and gets an array
of processes, instantiates the arch. network with the required number of
tasks, and then retreives the array of tasks. From each functional process,
the mapping netlist gathers a list of storage media that are accessible
to this process, and proceeds to extract the necessary events and map them.
