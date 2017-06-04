/*
  Copyright (c) 2002-2005 The Regents of the University of California.
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

#ifndef __UTIL_LINKEDLIST_H__
#define __UTIL_LINKEDLIST_H__

#include <list>
#include "systemc.h"
#include "object.h"
#include "global.h"

using namespace std;

class LinkedList : public Object {
    public:
LinkedList(DUMMY_CTOR_ARG _arg, int *index) {}
    LinkedList();
    virtual ~LinkedList();

    private:
    typedef std::list<Object *> list_t;
    list_t _list;

    friend class LinkedListItr;

    public:
    virtual Object *clone(process *caller);
    virtual int size(process *caller);

    virtual void add(process *caller, int index, Object *ob);
    virtual bool add(process *caller, Object *ob);
    virtual bool add(Object *ob);
    virtual void addFirst(process *caller, Object *ob);
    virtual void addLast(process *caller, Object *ob);
    virtual Object *getFirst(process *caller);
    virtual Object *getLast(process *caller);

    virtual void removeRange(process *caller, int fromIndex, int toIndex);
    virtual Object *remove(process *caller, int index);
    virtual Object *removeFirst(process *caller);
    virtual Object *removeLast(process *caller);
    virtual void clear(process *caller);

    virtual bool contains(process *caller, Object *ob);
    virtual int indexOf(process *caller, Object *ob);
    virtual int lastIndexOf(process *caller, Object *ob);

    virtual Object *get(process *caller, int index);
    virtual Object *set(process *caller, int index, Object *ob);

    private:
    // helper functions
    list_t::iterator getItrFromIndex(int index);
};

class LinkedListItr {
    public:
LinkedListItr(DUMMY_CTOR_ARG _arg, int *index) {}
    LinkedListItr() {}
    LinkedListItr(LinkedList LinkedList);
    virtual ~LinkedListItr() {}

    private:
    LinkedList _LinkedList;

    typedef std::list<Object *> list_t;
    list_t::iterator _currentItr;

    public:
    virtual bool hasNext(process *caller);
    virtual Object *next(process *caller);
    virtual void remove(process *caller);
};

#endif
