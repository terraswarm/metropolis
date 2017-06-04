# Tests for TrackedPropertyMap
#
# @Author: Christopher Brooks
#
# @Version: $Id: TrackedPropertyMap.tcl,v 1.5 2005/11/22 20:05:16 allenh Exp $
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software and that appropriate acknowledgments are made
# to the research of the Metropolis group.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test TrackedPropertyMap-1.1.1 {adding and removing a visitor } {
    set tracked [java::new metropolis.metamodel.TrackedPropertyMap]
    set objectClass [java::call java.lang.Class forName "java.lang.Object"]
    set results1 [$tracked wasVisitedBy $objectClass]
    set results2 [$tracked addVisitor $objectClass]
    set results3 [$tracked wasVisitedBy $objectClass]
    set results4 [$tracked removeVisitor $objectClass]
    set results5 [$tracked wasVisitedBy $objectClass]
    list $results1 $results2 $results3 $results4 $results5
} {0 1 1 1 0}

######################################################################
####
#
test TrackedPropertyMap-1.1.2 {removing a visitor} {
    set tracked [java::new metropolis.metamodel.TrackedPropertyMap]
    set objectClass [java::call java.lang.Class forName "java.lang.Object"]

    # Try removing the visitor before adding it so as to incr. code coverage.
    set results1 [$tracked removeVisitor $objectClass]

    set results2 [$tracked addVisitor $objectClass]
    
    # Remove the visitor twice
    set results3 [$tracked removeVisitor $objectClass]
    set results4 [$tracked removeVisitor $objectClass]

    list $results1 $results2 $results3 $results4
} {0 1 1 0}
######################################################################
####
#
test TrackedPropertyMap-1.2 {add a visitor twice } {
    set tracked [java::new metropolis.metamodel.TrackedPropertyMap]
    set objectClass [java::call java.lang.Class forName "java.lang.Object"]
    set results1 [$tracked addVisitor $objectClass]
    set results2 [$tracked addVisitor $objectClass]
    set results3 [$tracked wasVisitedBy $objectClass]
    set results4 [$tracked removeVisitor $objectClass]
    # We only ever added one visitor
    set results5 [$tracked wasVisitedBy $objectClass]
    list $results1 $results2 $results3 $results4 $results5
} {1 0 1 1 0}


######################################################################
####
#
test TrackedPropertyMap-1.3 {clearVisitors} {
    set tracked [java::new metropolis.metamodel.TrackedPropertyMap]
    # Get coverage on the false branche
    $tracked clearVisitors

    # Add a visitory
    set objectClass [java::call java.lang.Class forName "java.lang.Object"]
    set results1 [$tracked addVisitor $objectClass]
    set results2 [$tracked wasVisitedBy $objectClass]

    $tracked clearVisitors

    # Should not be present
    set results3 [$tracked wasVisitedBy $objectClass]

    list $results1 $results2 $results3
} {1 1 0}

######################################################################
####
#
test TrackedPropertyMap-1.4 {visitorIterator} {
    set tracked [java::new metropolis.metamodel.TrackedPropertyMap]

    set iterator [$tracked visitorIterator]

    # Nothing has been added
    set results1 [$iterator hasNext]

    # Add something
    set objectClass [java::call java.lang.Class forName "java.lang.Object"]
    set results2 [$tracked addVisitor $objectClass]
    set iterator2 [$tracked visitorIterator]
    set results3 [$objectClass equals [$iterator2 next]]

    # Should not be anything
    set results4 [$iterator2 hasNext]
    list $results1 $results2 $results3 $results4
} {0 1 1 0}

