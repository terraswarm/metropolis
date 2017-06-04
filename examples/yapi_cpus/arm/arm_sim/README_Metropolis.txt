$Id: README_Metropolis.txt,v 1.5 2004/06/03 23:17:14 cxh Exp $
Metropolis modifications to the armulator by Trevor Meyerowitz

Sources
-------

The armulator is included in the GNU gdb toolchain, which can be found at:
http://www.gnu.org/software/gdb/
Look in gdb-N.M/sim/arm.

We also partially used the gnupro toolkit from redhat for the xscale
at:
http://www.intel.com/design/intelxscale/dev_tools/020523/

We modified code that from Christian Sauer (a visiting researcher
from Infineon) that made the armulator standalone (as opposed to attached
to gdb) like it is.

We had to do a few changes to make them run on standalone files
(i.e. no-os), and to spit out traces.

Christian Sauer probably originally got the code from the uClinux project's
version of the armulator, which is at:
http://www.uclinux.org/pub/uClinux/utilities/armulator/


Trace Types
-----------
trace trace_file_name - generates an instruction trace
memtrace trace_file_name - generates an instruction trace with memory addresses

To Do
-----
add operand value generation to traces
possibly re-work trace-file format to support new features more
elegantly
