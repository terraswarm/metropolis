examples/producers_consumer/README.txt
Version: $Id: README.txt,v 1.3 2004/06/02 18:16:46 cxh Exp $

This directory contains a set of small meta-model files for a single
netlist with multiple writers and a single reader.

===========================================================
SystemC 2.0 simulation
* Makefile
 - type make from the command line; this will create run.x in the
   current directory.
 - type ./run.x

* metroshell
 - type $METRO/bin/metroshell 
 - type the following commands at metropolis>
   metropolis> classpath add ..
   metropolis> metroload pkg -semantics producers_consumer
   metropolis> simulate systemc producers_consumer.IwIr
   metropolis> exec make
   metropolis> quit
 - type ./run.x
 - from metropolis> command, once the simulate command is invoked, 
   you may optionally type the network command to see the network 
   structure of the example.  For example
    metropolis> network show

 Alternatively, you may call the metroshell in a batch mode:
 - type from the unix command prompt, metroshell -ni shellcmd.
===========================================================


Under Microsoft Visual C, you might see:
metro\examples\producers_consumer\sc_main.cpp(114) : error C2143: syntax error : missing ';' before '['
metro\examples\producers_consumer\sc_main.cpp(117) : error C2143: syntax error : missing ';' before '['

The solution is to change
  _sb.enabledSynchEventPC = new (ProgramCounter**)[0];
to
  _sb.enabledSynchEventPC = new (ProgramCounter**[0]);
and
  _sb.enabledSynchEventPC[i] = new (ProgramCounter*)[_sb.numSynchEventEachGroup[i]];
to
  _sb.enabledSynchEventPC[i] = new (ProgramCounter*[_sb.numSynchEventEachGroup[i]]); 
