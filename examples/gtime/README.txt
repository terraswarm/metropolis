locgen README.txt
$Id: README.txt,v 1.2 2004/05/27 06:21:00 guyang Exp $

This example is used to demonstrate the built-in GlobalTime quantity
and in general the quantity annotation mechanism.

GlobalTime
============
1. GlobalTime is just one quantity. It is used to model "time".
   SystemC-based simulator provides a built-in implementation of it.
	 The GlobalTime::request function adds the request into a list.
	 The GlobalTime::resolve function picks from the list the smallest
	 time requested, and disables all requests larger than the smallest.
	 The GlobalTime::stable returns false if at least one request was
	 disabled in GlobalTime::resolve.
	 The GlobalTime::postcond updates the resolution result.
2. Other quantities work in a similar way. Users need to provide 
   all the request, resolve, stable and postcond functions for
	 the quantities.

Run the Test
============

1. Type "make": elaborate the network, generate and compile SystemC
   simulation code. An executable "run.x" will be created.

2. Type "./run.x". It should print out the absolute global time for
   the beginning and end event of 'labela'. Especially, since the
	 time requested for the end of lablea is the time annotated to 
	 beginning of 'labela' plus cycle, it simply means the operation
	 'labela' takes cycle amount of time.

