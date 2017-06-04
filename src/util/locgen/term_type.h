/*
  @Version: $Id: term_type.h,v 1.9 2005/11/22 20:16:09 allenh Exp $

  @Copyright (c) 2004-2005 The Regents of the University of California.
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
*/

#ifndef TERM_TYPE_H
#define TERM_TYPE_H

#include <vector>
using namespace std;

struct term_type{
    int type;                /* 0: simple term                                */
                        /* 1: complex term                                     */
                        /* 2: annotation term                           */

    int  a;                  /* for simple term a*i + b                        */
    int  b;                /* either a or b can be 0                        */

    string expr;          /* for complex term                                */

    string event;         /* for annotation term                          */

    string annot;

    string orig_expr;     /* for error reporting                          */

    void print(){cout<<type<<" "<<a<<"i+"<<b<<" -"<<event<<"."<<annot<<"- "<<"("<<expr<<") ";}
};

struct annot_type{
    string annot_name;
    int data_type;        /* 0: int            1: double                  */
    /* 2: unsigned       3: char *                  */
    vector<term_type> term_list;

    annot_type():data_type(0){};
    /* 0: all simple index; 1: only complex terms;                       */
    /* 2: only annotation term; 3: both complex and annotation term      */

    int type() {
        int type  = 0;
        for (unsigned i = 0 ; i< term_list.size(); i++)
            {
                if (type == 0)
                    type = term_list[i].type;
                else if (type == 1)
                    if (term_list[i].type == 2) return 3;
                    else
                        if (term_list[i].type == 1) return 3;
            }
        return type;
    }

    void print() {
        cout<<"annot:"<<annot_name<<" -- "<<"data_type:"<<data_type<<" "<<"annot_type:"<<type()<<endl;
        for (unsigned i = 0 ; i< term_list.size(); i++)
            { term_list[i].print(); cout<<",";}
        cout<<endl;
    }
};

struct event_type{
    string event_name;
    vector<annot_type> annot_list;

    void print() {
        cout<<event_name<<endl;
        for (unsigned i = 0 ; i< annot_list.size(); i++)
            annot_list[i].print();
        cout<<endl;
    }
};
#endif
