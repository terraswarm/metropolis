/*
  Copyright (c) 2002-2004 The Regents of the University of California.
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
//package metamodel.util;

#include "ArrayList.h"

ArrayList::ArrayList() {
}

ArrayList::~ArrayList() {
}

Object *ArrayList::clone(process *caller) {
    ArrayList *newArrayList = new ArrayList();

    for (int i = 0; i < size(caller); i++)
        newArrayList->add(caller, get(caller, i));

    return newArrayList;
}

int ArrayList::size(process *caller) {
    return (int) _list.size();
}

void ArrayList::add(process *caller, int index, Object *ob) {
    if (index < 0 || index > size(caller)) return;

    _list.resize(size(caller) + 1);

    for (int i = size(caller) - 1; i >= index; i--)
        _list[i + 1] = _list[i];

    _list[index] = ob;
}

bool ArrayList::add(Object *ob) {
    _list.push_back(ob);
    return true;
}

bool ArrayList::add(process *caller, Object *ob) {
    _list.push_back(ob);
    return true;
}

void ArrayList::removeRange(process *caller, int fromIndex, int toIndex) {
    if (toIndex >= size(caller) || toIndex < fromIndex) return;
    _list.erase(_list.begin() + fromIndex, _list.begin() + toIndex);
}

Object *ArrayList::remove(process *caller, int index) {
    if (index < 0 || index > size(caller)) return NULL;
    Object *prevOb = _list[index];
    _list.erase(_list.begin() + index);
    return prevOb;
}

void ArrayList::clear(process *caller) {
    _list.clear();
}

bool ArrayList::contains(process *caller, Object *ob) {
    return (indexOf(caller, ob) >= 0);
}

int ArrayList::indexOf(process *caller, Object *ob) {
    if (!ob) {
        for (int i = 0; i < size(caller); i++) {
            if (_list[i] == NULL) return i;
        } // end for
    } else {
        for (int i = 0; i < size(caller); i++) {
            if (_list[i] != NULL && _list[i] == ob) return i;
        } // end for
    } // end if

    return -1;
}

int ArrayList::lastIndexOf(process *caller, Object *ob) {
    if (!ob) {
        for (int i = size(caller) - 1; i >= 0; i--) {
            if (_list[i] == NULL) return i;
        } // end for
    } else {
        for (int i = size(caller) - 1; i >= 0; i--) {
            if (_list[i] != NULL && _list[i] == ob) return i;
        } // end for
    } // end if

    return -1;
}

Object *ArrayList::get(process *caller, int index) {
    if (index < 0 || index > size(caller)) return NULL;

    return _list[index];
}

Object *ArrayList::set(process *caller, int index, Object *ob) {
    if (index < 0) return NULL;

    Object *prevOb = NULL;

    if (index > size(caller))
        _list.resize(index);
    else
        prevOb = _list[index];

    _list[index] = ob;

    return prevOb;
}

ArrayListItr::ArrayListItr(ArrayList arrayList) {
    _arrayList = arrayList;
    _currentItr = _arrayList._list.begin();
}

bool ArrayListItr::hasNext(process *caller) {
    return _currentItr != _arrayList._list.end();
}

Object *ArrayListItr::next(process *caller) {
    Object *result = (*_currentItr);
    _currentItr++;

    return result;
}

void ArrayListItr::remove(process *caller) {
    list_t::iterator currentItr = _currentItr++;
    _arrayList._list.erase(currentItr);
}
