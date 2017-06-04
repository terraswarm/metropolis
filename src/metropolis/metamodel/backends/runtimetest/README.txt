Author(s): Xi Chen

Description:
  RuntimeTestBackend is essentially a testing backend added for testing, 
runtime library, constraint elaboration and other elaborator features. 
Similar to SystemCBackend, RuntimeTestBackend is also a subclass of 
ElaboratorBackend, and the only thing it does is to elaborate the 
design, manipulate the elaborated network and print out the results. 
Users are supposed to modify this backed and use it to test their own
added features of runtime library and elaborator.

Usage:

1. To test runtime library, under the test case directory metro/examples/runtime, 
   type 'make' to compile the meta-model and print out the testing information.

2. To test constraint elaboration, under metro/examples/constraint_elaboration, 
   type 'make' to compile the meta-model and print out the testing information.

3. To test a particular aspect of runtime library or elaborator, one needs to
   modify RuntimeTestBackend.java and recompile the backend. For example, to 
   test a method of metropolis.metamodel.runtime.Network object, invoke it in 
   RuntimeTestBackend.java after elaboration is done and print the results by
   calling method Network.show(). And then recompile the whole package.
