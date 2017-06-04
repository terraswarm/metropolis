**************************************************
*
* A simple tutorial on using LOC checker generator
* Author: Xi Chen and Harry Hsieh
*         xichen@cs.ucr.edu, harry@cs.ucr.edu
* Version: $Id: README.txt,v 1.8 2004/09/14 14:59:13 cxh Exp $
*
**************************************************

../locgen: executable to convert .loc file into a checker
testFile.loc: specification of formula, annotation, and trace format
fir_trace.trc: trace file obtained from FIR example in SystemC 2.0 distribution

More example:
     fir_violation.loc (use trace "fir_trace.trc")
     fir_no_violation.loc (use trace "fir_trace.trc")
     equal_performance.loc (use trace "np1.trc" and "np2.trc")

Input format file (*.loc):
--------------------------

loc: LOC formula to be checked
annotation: names of event and annotation from the trace file 
(must include keyword "event")
trace: c-style trace file format (data type allowed: %s %d %f and %u)

E.g.

loc: t(Stimuli[i] - t(Display[i-5]) <= 45
annotation: event val t
trace: "%s : %d at time %d"

'Stimuli' and 'Display' are event names.  't' and 'val' are names of 
annotations ('val' is not used).   'i' is the only index variable and must be 
a non-negative integer.


Tutorial:
---------

1. type "make" to generate the loc_checker corresponding to 
   fir_violation.loc and fir_no_violation.loc

2. type "fir_violation.check fir_trace.trc" to check the trace

3. You should see a reporting of the violation of this latency constraint, 
   including detail error location and maximum data-structure memory usage.

4. fir_no_violation.loc has the violation fixed by changing the
   loc line to:

   loc: t(Stimuli[i]) - t(Stimuli[i-1]) == 10

5. type "fir_no_violation.check fir_trace.trc"

6. You should see the reporting of constraint satisfaction, along with maximum
   data-structure memory usage.

7. To test checking two trace files with one formula at the same time, use 
   the example in equal_performance.loc. After compilation, type:
           equal_performance.check np1.trc np2.trc
