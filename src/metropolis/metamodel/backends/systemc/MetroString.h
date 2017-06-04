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
#ifndef MMSCS_STRING_H
#define MMSCS_STRING_H

#include "global.h"
#include "object.h"
#include <string>

using namespace std;

class process;
class String;
class Object;

class String : public string, public Object {
    public:
String() : string() { }
    String(char * c) : string(c) { }
    String(const char * c) : string((char*)c) { }
    String(String * s) : string(s->c_str()) { }
    String(String &s) : string(s.c_str()) { }
    String(const String &s) : string(((string)s).c_str()) { }

    bool equals(String * cmpStr) {
        return (strcmp(c_str(), cmpStr->c_str()) == 0);
    }

    bool equals(process* caller, String * cmpStr) {
        return (strcmp(c_str(), cmpStr->c_str()) == 0);
    }

    bool equals(process* caller, int mode, String*intfcName, String * cmpStr) {
        return (strcmp(c_str(), cmpStr->c_str()) == 0);
    }

    const char* c_str() {                //return char* form of this String
        return string::c_str();
    }

    virtual int hashCode(process * caller) {
        const char * s = c_str();
        int hashcode = 0;
        int weight = 1;
        for (; (*s) != 0; s++) {
            hashcode += ((int) (*s))*weight;
            weight *= 31;
        }
        return hashcode;
    }

};

#endif

