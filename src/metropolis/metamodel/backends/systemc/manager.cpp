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
#include "manager.h"
#include "scheduler.h"
#include "scoreboard.h"

manager::manager(sc_module_name name) : process(name) {
    SC_THREAD(run);
    end_module();
}

void manager::run() {
    wait(DeltaCycle);
    cntl->initialize();
    cntl->invoke();

    while (true) {

        if (debug_flag) {
            cerr<<endl<<"Initial Scoreboard"<<endl;
            cntl->showSB();
        }

        cntl->eval();
        if (debug_flag && debug_level>0) {
            cerr<<endl<<"After eval "<<endl;
            cntl->showSB();
        }

        cntl->preSet();
        if (debug_flag && debug_level>1) {
            cerr<<endl<<"after preSet"<<endl;
            cntl->showSB();
        }

        cntl->schedule();
        if (debug_flag) {
            cerr<<endl<<"after scheduling"<<endl;
            cntl->showSB();
        }

        cntl->postSet();
        if (debug_flag && debug_level>1) {
            cerr<<endl<<"after postSet"<<endl;
            cntl->showSB();
        }

        cntl->invoke();
    } //end while
}
