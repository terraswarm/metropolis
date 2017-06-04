/*
  Copyright (c) 2003-2005 The Regents of the University of California.
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
#ifndef MMSCS_GLOBALTIMEMANAGER_H
#define MMSCS_GLOBALTIMEMANAGER_H

#include "global.h"
#include "process.h"
#include "event.h"
#include "object.h"
#include "quantitymanager.h"
#include "GlobalTimeRequestClass.h"

class GlobalTimeManager : virtual public QuantityManagerLOC {
    public:

    virtual double sub(process *caller, double t1, double t2)= 0;
    virtual bool equal(process *caller, double t1, double t2)= 0;
    virtual bool less(process *caller, double t1, double t2)= 0;
    virtual double A(process * caller, event *e, int i)= 0;
    virtual double getCurrentTime(process *caller)= 0;
    virtual void requestI(process *caller, event *e, GlobalTimeRequestClass* rc)= 0;
    /*
      virtual void request(process *caller, int mode, String *intfcName, event *e, GlobalTimeRequestClass* rc)= 0;
      virtual void resolve(process *caller, int mode, String *intfcName)= 0;
      virtual bool stable(process *caller, int mode, String *intfcName)= 0;
    */
    /*
      virtual double sub(double t1, double t2)= 0;
      virtual bool equal(double t1, double t2)= 0;
      virtual bool less(double t1, double t2)= 0;
      virtual double A(event *e, int i)= 0;
    */
    /*commented out by yw
      virtual void request(event *e, RequestClass* rc)= 0;
      virtual void resolve()= 0;
      virtual bool stable()= 0;
      commented out by yw */
};

#endif
