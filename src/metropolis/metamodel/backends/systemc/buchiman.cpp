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
#include "global.h"
#include "buchiman.h"

void pre(BState * root, BState_set_t& s, BState_set_t& p) {
    p.clear();
    for (BState_set_t::iterator it = s.begin(); it != s.end(); it++) {
        for (BState *b = root->nxt; b != root; b=b->nxt) {
            for (BTrans *t=b->trans->nxt; t!=b->trans; t = t->nxt) {
                if (t->to == (*it)) {
                    p.insert(b);
                    break;
                }// end if
            }// end for
        }// end for
    }// end for
}

void post(BState * root, BState_set_t& s, BState_set_t& p) {
    p.clear();
    for (BState_set_t::iterator it = s.begin(); it != s.end(); it++) {
        for (BTrans *t=(*it)->trans->nxt; t!=(*it)->trans; t = t->nxt)
            p.insert(t->to);
    }// end for
}

void transToPost(BState * root, BState_set_t& s, int *pos, int *neg, bool decided, BTrans_set_t& tr) {
    tr.clear();
    bool sat;
    for (BState_set_t::iterator it = s.begin(); it != s.end(); it++) {
        for (BTrans *t=(*it)->trans->nxt; t!=(*it)->trans; t = t->nxt) {
            sat = true;
            for (int i=0; i<=SEG(sym_id-1) && sat; i++)
                sat = (((decided ? pos[i] : ~neg[i]) & t->pos[i]) == t->pos[i]) &&
                    (((decided ? neg[i] : ~pos[i]) & t->neg[i]) == t->neg[i]);
            if (sat)  tr.insert(t);
        } // end for
    }// end for
}

void conditionalPost(BState * root, BState_set_t& s, int *pos, int *neg, bool decided, BState_set_t& p) {
    p.clear();
    bool sat;
    for (BState_set_t::iterator it = s.begin(); it != s.end(); it++) {
        for (BTrans *t=(*it)->trans->nxt; t!=(*it)->trans; t = t->nxt) {
            sat = true;
            for (int i=0; i<=SEG(sym_id-1) && sat; i++)
                sat = (((decided ? pos[i] : ~neg[i]) & t->pos[i]) == t->pos[i]) &&
                    (((decided ? neg[i] : ~pos[i]) & t->neg[i]) == t->neg[i]);
            if (sat)  p.insert(t->to);
        } // end for
    }// end for
}

void strictPre(BState * root, BState_set_t& s, BState_set_t& q) {
    BState_set_t p;
    q.clear();
    pre(root, s, p);
    //not supported by g++ 2.96?
    //set_difference(p.begin(), p.end(), s.begin(), s.end(), inserter(q, q.begin()));
    for (BState_set_t::iterator it=p.begin(); it!=p.end(); it++)
        if (s.find(*it)==s.end()) q.insert(*it);
}

void strictPost(BState * root, BState_set_t& s, BState_set_t& q) {
    BState_set_t p;
    q.clear();
    post(root, s, p);
    //not supported by g++ 2.96?
    //set_difference(p.begin(), p.end(), s.begin(), s.end(), inserter(q, q.begin()));
    for (BState_set_t::iterator it=p.begin(); it!=p.end(); it++)
        if (s.find(*it)==s.end()) q.insert(*it);
}

void annotateDist2FS(BState * root) {
    BState_set_t f, tmp;
    int stat;
    int counter = 1;
    for (BState *b = root->nxt; b != root; b=b->nxt) {
        b->dist = -1;
        stat = 0;
        if (b->final == lt_accept) {
            stat |= FINAL_STATE;
            f.insert(b);
            b->dist = 0;
        }
        if (b->id == -1) {
            stat |= INIT_STATE;
            b->id = 0;
        } else {
            b->id = counter;
        }
        b->final = stat;
        counter ++;
    }// end for

    if (f.empty()) return;
    counter = 1;
    while (! f.empty()) {
        strictPre(root, f, tmp);
        f = tmp;
        for (BState_set_t::iterator i=f.begin(); i!=f.end(); i++)
            if ((*i)->dist != -1)
                f.erase(i);
            else
                (*i)->dist = counter;
        counter++;
    } // end while
}

void getFinalStates(BState * root, BState_set_t& f) {
    f.clear();
    for (BState *b = root->nxt; b != root; b=b->nxt)
        if (b->final & FINAL_STATE) f.insert(b);
}

void getInitStates(BState * root, BState_set_t& f) {
    f.clear();
    for (BState *b = root->nxt; b != root; b=b->nxt)
        if (b->final & INIT_STATE) f.insert(b);
}

void outputBuchi(BState * root) {
    for (BState *b = root->nxt; b != root; b=b->nxt) {
        for (BTrans *t=b->trans->nxt; t!=b->trans; t = t->nxt) {
            if (b->final & FINAL_STATE) cerr<<"F";
            if (b->final & INIT_STATE)  cerr<<"I";
            cerr<<"S"<<b->id<<"-"<<b->dist<<" ";
            if (t->to->final & FINAL_STATE) cerr<<"F";
            if (t->to->final & INIT_STATE)  cerr<<"I";
            cerr<<"S"<<t->to->id<<"-"<<t->to->dist<<" ";
            for (int i=0; i<=sym_id; i++) {
                if (t->pos[SEG(i)] & (1<<BIT(i))) cerr<<sym_table[i]<<" ";
                else if (t->neg[SEG(i)] & (1<<BIT(i))) cerr<<"!"<<sym_table[i]<<" ";
            }
            cerr<<endl;
        } // end for
    }// end for
}

void outputBuchiStates(BState_set_t& ss) {
    for(BState_set_t::iterator sit = ss.begin(); sit != ss.end(); sit ++) {
        if ((*sit)->final & FINAL_STATE) cerr<<"F";
        if ((*sit)->final & INIT_STATE)  cerr<<"I";
        cerr<<"S"<<(*sit)->id<<"-"<<(*sit)->dist<<" ";
    }
}
