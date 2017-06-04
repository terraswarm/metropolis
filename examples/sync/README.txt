$Id: README.txt,v 1.4 2004/08/29 18:52:16 guyang Exp $

This example demonstrates the usage and simulation of synch
constraints. It is the basic construct to specify behavior and
architecture mapping.

The basic syntax of synch is
ltl synch (event1, event2, ..., eventN : 
  variable1@(event1, i) == variable2@(event2, i), 
  variable3@(event2, i) == variable4@event3, i), ...)
All events {event1, event2, ..., eventN} will execute simulatenously
only. At the same time, variable1 in the scope of event1 must be
equal to variable2 in the scope of event2; variable3 in the scope
of event2 must be equal to variable4 in the scope of event3; and
so on. The variable i is the global execution index. In synch, 
it should always be i. When a variable is defined Nondet 
(nondeterministic), synch could assign the deterministic value of
the variable in the other side of == to this nondeterministic variable.
Please refer to metamodel document for the complete specification 
of synch.

A variation of synch is 
ltl synch (eventL1 || eventL2 || ... || eventLN => 
  eventR1 || eventR2 || ... || eventRM :
  variableL1@(eventL1, i) == variableR1@(eventR1, i), 
  variableL2@(eventL2, i) == variableR2@(eventR2, i), ...)
Now, it is not necessary that all events appearing in synch 
must execute all together. As long as any event on the left hand 
side excutes, at least one event on the right hand side must
execute simultenously. This implication helps to model multiple
events being mapped to a single event or multiple events. 
The semantics of variable comparison part is the same as in 
previous basic syntax.

In this example, behavior.mmm represents processes in the behavior
part of the system. architecture.mmm represents a process in the
architecture part of the system. The behavior netlist bnet.mmm creates
instances of behavior process. The architecture netlist anet.mmm
creates an instance of architecture process. The top level netlist
wnet.mmm then encapsulates both behavior netlist and architecture
netlist. In the top level netlist, there are both kinds of synch 
statements. They either specify the simultaneous execution of two 
events in behavior and architecture parts of the system, or specify
a set of events being mapped to a single event. Associated with
some pairs of events, there are variable comparisons. Note that 
variable n and m in architecture process are of type Nondet. 
See what the simulation produces to better understand synch statement.

