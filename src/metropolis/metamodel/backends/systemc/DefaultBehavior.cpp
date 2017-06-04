/*
  Copyright (c) 2003-2004 The Regents of the University of California.
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

*/
#include "DefaultBehavior.h"
#include "programcounter.h"
#include "scoreboard.h"

/*
 * resolve
 *
 * this simple resolve function select the process that blocks as fewer processes as
 * possible
 */
void DefaultBehavior::resolve(ProgramCounter * pc1, ProgramCounter * pc2) {
    if (pc1->_pcBlocked.size() < pc2->_pcBlocked.size())
        pc2->getScoreboard()->set2dontrun(pc2);
    else if (pc1->_pcBlocked.size() > pc2->_pcBlocked.size())
        pc1->getScoreboard()->set2dontrun(pc1);
    else {
        int choice = (int)(random() & 0x1 );
        if (choice)
            pc2->getScoreboard()->set2dontrun(pc2);
        else
            pc1->getScoreboard()->set2dontrun(pc1);
    } // end if
} // end resolve
