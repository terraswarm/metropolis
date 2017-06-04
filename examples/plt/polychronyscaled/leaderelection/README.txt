$Id: README.txt,v 1.6 2004/06/02 18:16:46 cxh Exp $

LEADER ELECTION ALGORITHM 
---------------------------------------------------------------------

This example is the implementation of the traditional leader election
algorithm in its synchronous version.


Each process has rate equal to 1 and priority equal to 1. It means
that in each round, all processes are scheduled concurrently. So in
each round all processes reads the input, execute a function that
possibly generates some output and finally write the output. 

Compile by typing: 

make 

to generate the run.x executable simulation.

Example can be run by typing:

./run.x


The leader election algorithm will find the node with higher id and
will elect him to be the leader of the network.

The example network has 6 nodes and the simulation stops indicating
that node 6 (UID = 5) has been elected.

-------------------------------------------------------------------

This example shows how the platform works as a set of concurrent state
machines (priority and rate equal to one).

Please refer to producerconsumer example for another kind of application.


