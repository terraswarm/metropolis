
Description:

  The elaborator backend computes the static structure of the network 
of processes and media described by a set of meta-model files, resolves
the values of structural keywords and extracts event and annotation
information from constraints at compile time. The elaborator backend 
translates the meta-model files into java files, register all the user 
defined object types and invokes the constructor of the top level 
netlist. The constructor of the top level netlist then instantiates 
and initializes the whole network structure. In elaboration the network 
structure is built by running the Java code and stored in a set of 
data structures in runtime library. The classes in runtime library 
are used to represent and manipulate the elaborated network structure 
for other backends.
  
  The elaborated network structure includes following information that
is accessible through runtime library APIs by other backends.

1. All the instances of network objects such as processes, media, ports 
   and etc.
2. The constraint instances in the network objects, event instances 
   and annotations from the constraints.
3. The values of resolved structural keywords such as getnthconnectionsrc,
   getconnectionnum and etc.
