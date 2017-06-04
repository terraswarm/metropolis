$Id: README.txt,v 1.3 2005/04/07 23:56:56 cxh Exp $

GME Metamodel of Metropolis Netlist Creation

Contents
========
README.txt					This file.
examples/Producer-Consumer-exported.xme		ProducersConsumer example.
gme-metropolis.ppt				GME Screenshots
meta/Metropolis.mta
meta/Metropolis.xmp
meta/icons					GME Icons
meta/mmm-meta-export.xme			Metropolis/GME metamodel
						(Used in configuring GME to
						 support Metropolis)

Requirements for operation
==========================
 - WindowsXP Professional (not tested for other Windows versions)
 - Installation of GME (r4.11.10 or later) Download from:
         http://www.isis.vanderbilt.edu/Projects/gme/

Instructions
============
1. Start GME
    a) File -> New Project
    b) Choose paradigm "MetaGME" and select "Create new"
    c) "Create Project File" will be selected, click on Next
    d) Browse to the meta/ directory 
       (so that you can see the "icons" directory)
	create a new file called "mmm.mga"
    e) File -> Import XML...
    f) Select the "mmm-meta-export.xme" file
    g) Save the project (it should successfully import)
    h) Close the project with File -> Close Project 
2. Register the Metropolis Meta-Model paradigm
    a) File -> Register Paradigm
    b) In the "Select Paradigm" window, choose "Add from File"
    c) Browse to the Metropolis.xmp in the meta/ directory
    d) Back in "Select Paradigm", select Close.
3. Open the Producers Consumer example
    a) File -> Import XML
    b) Browse to ../examples/  
    c) In the "Import to new project" window,
       "Create Project File" will be selected, click on Next
    d) In the "Open" file browser, "Root Folder" will be selected,
       Click on Open
    e) On the right, in the Aggregate tab,
       expand "Root Folder", then double click on NewNetList


(Based on HSIF readme.txt by Andras Lang)
