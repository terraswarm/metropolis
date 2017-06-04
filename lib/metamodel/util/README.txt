Java Collections 
by Alexandre Smirnov

---

This directory contains an implementation of the collection classes from the
Java package 'java.util'. The author is Alexandre Smirnov.  

The complete set of implemented classes is: AbstractCollection, AbstractList,
AbstractMap, AbstractSequentialList, AbstractSet, ArrayList, BitSet,
Collection, Comparable, Comparator, Dictionary, Enumeration, HashMap,
HashSet, Hashtable, Iterator, LinkedList, List, ListIterator, Map, Set,
SortedMap, SortedSet, TreeMap, TreeSet, Vector.

The implementation is based on the API of Java 1.3, which can be found at
'http://java.sun.com/j2se/1.3/docs/api'. This address also provides
documentation about the API and semantics of the methods provided by these 
classes. 

This implementation is not 100% equivalent to the Java semantics in two
aspects: 
  - exceptions: metamodel does not support exceptions. In any situation where
    Java would throw an exception, the behavior is undefined.
  - synchronized methods: there are synchronized methods in these library,
    as there should be no concurrent modification of collections.

All these differences are documented in the comments of the code. For other
information about these classes, it is recommended that you visit
'http://java.sun.com/j2se/1.3/docs/api'.
