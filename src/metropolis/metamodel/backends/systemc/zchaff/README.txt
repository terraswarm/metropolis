[2003.7.22]
NOTE: If your code is in C instead of C++, please use SAT_C.h instead of SAT.h
as the header file. 

This is a new (as of June, 2003)  release of zchaff, a SAT solver from
Princeton University. The main difference between this one and the previous
(2001.2.17) version are listed in the following:
1. This version of zchaff has incremental SAT solving capability
    In practice, many SAT instances are related in the sense that they only
  differ in a small number of clauses. Zchaff can solve a set of
  such instances incrementally, leveraging the knowledge (clauses) learned
  from previous runs to help current runs. This feature can only be invoked
  through the functional call interface. Please read SAT.h for more
  information about assigning clauses with Group IDs and how to delete
  clause or add clauses by groups.
                                                                                                                    
2. This version of zchaff is certifiable
    Now zchaff can produce a verifiable trace that can be checked by a third
  party checker. To invoke this, modify zchaff_solver.cpp and uncomment
  #define VERIFY_ON and compile again. Now zchaff will produce a trace
  called resolution_trace after each run and this can be checked by
  zverify_bf or zverify_df, which are two checkers based on breadth-first and
  depth-first search.
                                                                                                                    
3. This version of zchaff can produce an unsatisfiable core from an
   unsatisfiable formula
    Unsatisfiable core extraction can be useful for some applications. This
  version of zchaff implement the idea presented in our SAT 2003 paper about
  extracting unsat cores.
                                                                                                                    
4. This version can compile under gcc 3.x.
                                                                                                                    
5. This version fixed a couple of serious bugs in the previous version. (But
may have introduced other bugs :().
                                                                                                                    
How to Install:
  Use "make" to compile, or "make all" to compile zchaff with extra utilities
(e.g. core extractor, verifier). It should work without any problem under
Linux, Cygwin or Solaris.
                                                                                                                    
  To compile a native Windows executable, Open Visual Studio .Net, create
a project, and add these files into the project:
  zchaff_base.cpp, 
  zchaff_cpp_wrapper.cpp (this can be obtained by rename zchaff_wrapper.wrp to 
         zchaff_cpp_wrapper.cpp and delete all occurrences of "EXTERN" in
         the file.)
  zchaff_dbase.cpp
  zchaff_solver.cpp
  zchaff_utils.cpp
  sat_solver.cpp
  Also, modify zchaff_header.h and comment out #define WORD_SIZE 4, and uncomment
  #define WORD_SIZE MSVC. Also, modify SAT.h and change the definition
  of long64 into long (instead of the default long long). 
    MSVC will report a lot of warnings. Hopefully none is serious. :(. As you
  suspected, zchaff is not tested under native Windows environment. But anyway, 
  this is the hack. 

How to use: 
  the main executable is zchaff. The command line is 
		zchaff CNF_FILE [TimeLimit]
  Other executables will print out a help info when executed with no argument.

  run_till_fix can obtain a small core by iteratively run core extraction. Do turn
  VERIFY_ON in zchaff_solver.cpp when compile. 

For any questions or bug reports, please send email to lintaoz@ee.princeton.edu

Thanks.  

