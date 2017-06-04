/* Simple Java Test Program

Copyright (c) 1998-2005 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software and that appropriate acknowledgments are made
to the research of the Metropolis group.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


METROPOLIS_COPYRIGHT_VERSION_1
COPYRIGHTENDKEY


@ProposedRating Green (cxh@eecs.berkeley.edu)
@AcceptedRating Red
*/

/** Simple class used by configure to test whether the javac
    compiler is working.
    @author Christopher Brooks
    @version $Id: confTest.java,v 1.8 2005/11/22 19:58:50 allenh Exp $
    @since Ptolemy II 0.2
*/
public class confTest {

    public static void main(String args[]) {
        System.out.print(System.getProperty("java.version"));
    }
}
