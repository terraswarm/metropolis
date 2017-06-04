Author: Doug Desmore
UC Berkeley
Date: 7/29/04

This README file is an introduction to the vertical architecture model.

This model was developed based on the PiP architecture_v2 model. This initial
model provides the baseline architecture with a cpu, bus, and memory.

A vertical refinement as defined roughly in the Metropolis architecture
environment consists of adding media to the scheduled netlist. These
media represent new services that the architecture can make use of. In this
case they are:

Rtos.mmm - adds different task scheduling policies
and Cache.mmm - adds the notion of memory hierarchy and the execution time
savings they provide.

This model is provided not only so that this functionality can be used, but
also so that those looking do develop designs in Metropolis can see how to
develop vertical refinement models.

The files required are:
architecture_vert.mmm
Bus.mmm
Cache.mmm
Cpu.mmm
docs
InterfaceSched.mmm
InterfaceSchedReq.mmm
InterfaceScheduling.mmm
makefile
Makeflags
Mem.mmm
MyScheduler.mmm
piparchscheduling_vert.mmm
piparchsched_vert.mmm
ProcessAccount.mmm
ProcessRecord.mmm
README.txt
Rtos.mmm
SchedulerFIFO.mmm
SchedulerTimeSliceBased.mmm
SwTask.mmm
systemc_sim.mk
top_vert.mmm

This can be built simply by typing make in the directory. This follows the
tutorial given by the PiP tutorial document concerning mapping and simulation
of this architecture.

Please look at the document docs/ref.pdf for more information.
 
Questions can be sent to: densmore@eecs.berkeley.edu
