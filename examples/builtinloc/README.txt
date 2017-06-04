builtinloc README.txt
$Id: README.txt,v 1.1 2004/11/12 23:46:05 guyang Exp $

This example is used to demonstrate the built-in LOC constraints.

QuantityManager
===============
As described in the metamodel document, this interface defines four
functions (request, resolve, stable, postcond) to do quantity 
annotation and resolution. Please refer to the document for the 
semantics details. 

QuantityManagerLOC
==================
In addition to the four functions defined in QuantityManager,
QuantityManagerLOC interface defines three additional functions 
(getQuantity, registerLOC, unregisterLOC) to handle built-in LOC
constraints. Since users write their own quantity managers in an
imperative way, it is not possible for tools to automatically
handle the built-in constraints without integrating with the
quantity managers. The functions defined in QuantityManagerLOC
are for this purpose.

If a quantity manager wants to support built-in LOC constraints 
(maxrate, minrate, maxdelta, mindelta, period), it must implement
this interface. The semantics of each function is described below.

- elaborate eval Quantity getQuantity(int id);
LOC constraints are defined over events, quantities, variables etc. 
Each built-in LOC constraint must be associated with one quantity
manager, which resolves the quantity. getQuantity(int) function 
returns the quantity manager itself. So, typically, it would 
have 'return this' in the body of the quantity manager. 
Note that this static structure information is resolved at elaboration
time, so the keyword 'elaborate' is necessary for it to function
properly. The argument 'int id' is provided for resolving more
complex situations like distinguishing multiple quantity managers.

- update void registerLOC(int type, event e1, event e2, RequestClass r);
Built-in LOC constraints declare the formal properties that events
vectors must obey. e.g.
loc period(port2QM.getQuantity(0), one_event, 10);
requires that the event 'one_event' must occur every 10 units which
is governed by the quantity manager returned by port2QM.getQuantity(0).
Function registerLOC registers the built-in constraints to the
quantity manager. In the arguments, 'type' specifies one of the five 
types of built-in constraints. See metro/lib/metamodel/lang/
BuiltInLOCType.mmm. 'e1' (and 'e2') are the events being constrained.
'r' is a generic way to wrap up different kinds of values specified
in the constraints. It is the same as in quantity annotation.
registerLOC function is executed automatically right before event
e1 (and e2) to update the possible dynamic change of value.

- update void unregisterLOC(process p);
This function is used to remove all the built-in constraints on the
events made by process p. It is automatically executed when the 
process p finishes it thread function. This is necessary because
otherwise the built-in constraints might block the execution 
of other processes governed by the same quantity manager.
However, it is still open whether we should allow withdraw of a
particular built-in constraint, when and how.

Specify Built-in LOC Constraints
================================
Since constraints need to be elaborated to gather static information, 
e.g. quantity manager information for built-in LOC constraints, 
it must be specified in postElaborate function, which corresponds to
a special elaboration phase. As an alternative, constraints can also
be written in any function with elaborate modifier and called from
within postElaborate function.
For details, please refer to the metamodel document (version 0.6) 
in the Metropolis release 1.0.

GlobalTime
============
GlobalTime is one special built-in quantity manager. It is used 
to model "time". The most important property of GlobalTime is 
the non-decreasing time quantity. It supports all types of built-in
LOC constraints.
SystemC-based simulator provides a default implementation of it.
More precisely,
- maxrate(GlobalTime, event, value): 
  event occurs every 1/value seconds or longer
- minrate(GlobalTime, event, value): 
  event occurs every 1/value seconds or shorter
- maxdelta(GlobalTime, event1, event2, value)
  event2 occurs only if the most recent event1 occurred within the
  past value seconds
- mindelta(GlobalTime, event1, event2, value)
  event2 occurs only if the most recent event1 occurred before the
  past value seconds
- period(GlobalTime, event, value)
  event occurs every value seconds


Run the Test
============
1. Type "make" to generate executable run.x for simulation.
2. Type "./run.x". It should print out the absolute global time 
annotated to the events specified in the built-in LOC constraints.
If an event is not annotated, it prints out 'Not_Annotated'.

