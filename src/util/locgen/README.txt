*************************************************************
* locgen: A program to convert LOC formula into trace checker.
* author: Xi Chen and Harry Hsieh
*         xichen@cs.ucr.edu, harry@cs.ucr.edu
*************************************************************


locgen takes a standard LOC formula specification (see [1]), an event and 
annotation specification, a trace format specification, and generate a c++ 
program to analyze traces for LOC formula.   Currently, the trace file must
have one event per line along with value for all annotations.  Other trace 
format will need to be converted into this base format.


Compilation:
------------

To compile locgen, type 'make'.


Source files:
-------------

loc.flex - tokenize the input file (Flex file)
parser.y - parse the LOC formula and generate C++ code (Bison file)
term_type.h - main data structure defined for parsing and code generation.


Input format file (*.loc):
--------------------------

loc: LOC formula to be checked
annotation: name of event and annotation from the trace file 
(must includ keyword "event")
trace: c-style trace file format (data type allowed: %s %d %f and %u)

E.g.

loc: t(Stimuli[i] - t(Display[i-5]) == 45
annotation: event val t
trace: "%s : %d at time %d"

'Stimuli' and 'Display' are event names.  't' and 'val' are name of annotations 
('val' is not used).   'i' is the only index variable and must be a 
non-negative integer.


Usage:
------

1) LOC Checker Generator (locgen) takes the *.loc file as input, parses 
   the LOC formula and the format of target trace file, generates source file
   (e.g. checker.cc) for the checker of this particular LOC formula and the 
   trace file format.
2) checker.cc is compiled to generate an executable checker.
3) Run the checker with the name of trace file as parameter.
   With one trace file:
	locgen filename
   With multiple trace files:
	locgen filenumber filename1 filename2 ... 


Main algorithm and data structures (for checker.cc) :
-----------------------------------------------------

In the main function, the trace file is read line by line. Event name is
extracted from each line and the value of annotations are stored accordingly.

The formula is evaluated with i from 0.  If, with the current value of i, all 
annotations are available, the formula will be evaluated.  Otherwise, the 
checker continue to read trace file in until all the annotations becomes 
available with value of i.  Whenever a invalid index is encountered, the 
formula with this index will be skipped and no error will be reported. 

The checker terminates when either the entire trace file is read or a constraint
violation is encountered. If a violation is encountered, a detailed error report
will be printed.

A simple memory recycling algorithm is applied, which checks the local index of
events and the value of i and deletes the annotations that is no longer needed.

Data structure:

/**** Base event class from which all events are derived ***/
class event
{
public:
   char * name;			/* event name */
   index_t index;		/* local index of annotations of the event */

   event(char * name):index(0){this->name = strdup(name);};
   virtual ~event(){if (name) free(name);};
   virtual void removeRedundant(index_t) = 0;
   virtual size_type size() = 0;
};

/* for each particular event, there is a derived class associated with it */
/*** event 'Stimuli' ***/
class event_Stimuli : public event
{
private:
   deque<int>  annot_t;			/* data list for annotation 't' */

public:   
   /* return annotation 't' with a particular index*/               
   int  getAnnot_t(index_t);   
   /* store a set of annotations to the annotation lists */          
   void addAnnot(int);			

   event_Stimuli(char * name):event(name){};
   ~event_Stimuli();
   void removeRedundant(index_t);	/* memory space recycling */
   size_type size();			/* return the current memory usage */
};


Current and Future Work:
------------------------

Some of the future capabilities that we are considering includes:

1) Trading off memory usage versus analysis time
2) More than one LOC in one monitor.
3) Evaluate formula containing undefined annotations.
4) An option to evaluate the entire trace in the presence of error.
5) Preprocessing arbitrary trace file to conform to the specific format i.e.
   event-specific annotation, multiple event on one line, vcd,... 
6) Memory recycling for non simple indexed event (simple index are ax+b a>0)
7) Interactive LOC checking.

Reference:
----------
[1]Felice Balarin, Jerry Burch, Luciano Lavagno, and Yosinori Watanabe.
Constraints Specification at Higher Levels of Abstraction,
Proceedings of the Sixth IEEE International High-Level Design
Validation and Test Workshop (HLDVT'01)
