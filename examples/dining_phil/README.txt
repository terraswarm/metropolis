DEADLOCKING DINING PHILOSOPHERS EXAMPLE 
-------------------------------------------------------------

Example Description:

The Dining Philosophers example is a classic multiple process
synchronization problem.  This problem consists of N philosophers
sitting at a table who do nothing but think and eat. Between each
philosopher, there is a single chopstick (i.e. N philosophers will
share N chopsticks). In order to eat, a philosopher must have both
chopsticks. A problem can arise if each philosopher grabs one
chopstick either on his/her left or right, then waits for the other
chopstick next to him/her.  In this case a deadlock has occurred, and
all philosophers will starve. In this case the philosophers all eat 
and think at the same rate.

This example has no mechanism for preventing deadlock or for
recovering from it. The deadlock occurs if all philosophers have 
exactly one chopstick.

Compile and Execute:
------------------------------------------------------------

To compile example, at your system's prompt, type "make".  See example 
below:

yourPrompt> make

To run this example after compiling it, at your system's prompt, type 
"./run.x".  See example below:

yourPrompt> ./run.x

To randomize the simulation include the "-r" option after "./run.x"
