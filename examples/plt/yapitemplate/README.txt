This example is used to test and demonstrate the usage of the TTL and yapi 
template library located under $(METRO)/lib/metamodel/plt/yapitemplate
and $(METRO)/lib/metamodel/plt/TTLtemplate. For more details about the
two set of library modules, please see the documentation in $(METRO)/doc.

To run the yapitemplate test case, under current directory

1) type "make", test template resolution all the way down to
   the simulation
   
2) type "make elaborator", test template resolution and its elaboration

3) type "make metamodel", resolve templates and generate equivalent
   mmm code for both template declarations and instantiated template
   objects

4) type "make checker", generate an executable checker for the LOC constraint
in ProdCons.mmm. This has to be done after step 1 or 2. After simulation, 
a hidden trace file (.trace) is generated. To run the checker to check
the trace,  type command "make run_checker".

5) type "make demo", compile the example, run the simulation and go all 
the way down to the LOC constraint checking.