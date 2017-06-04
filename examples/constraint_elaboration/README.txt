The structural keywords getconnectionnum, getnthconnectionport and 
getnthconnectionsrc in constraints now can be resolved during elaboration.
To run the example, please go to
 
 metro/examples/test_constraint_elaboration, and type make.
 
In the output, the results of keyword resolution are printed out
under the items "Structural values", for example:
          o Structural values: 
            - getnthconnectionsrc(y2bf,yapioutinterface,0) = datagen1


Limitations
===========

The LOC constraint can only inculdes the events like beg(process,
object.label) and end(process, object.label). The label could be from
either labeled statements or labeled blocks.

1. The built-in constraints are not supported. 

2. LOC constraints can only contain integer vairables and arrays are
   not supported
   (e.g. data[a@(event1, i)]) cannot be handled.

3. In LOC constraints, the index variable has to be i.

---------------------
Xi Chen & Harry Hsieh
03/29/2003
