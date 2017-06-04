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

#include "LinkedList.h"

LinkedList::LinkedList() {
}

LinkedList::~LinkedList() {
}

Object *LinkedList::clone(process *caller) {
    LinkedList *newLinkedList = new LinkedList();

    for (int i = 0; i < size(caller); i++)
        newLinkedList->add(caller, get(caller, i));

    return newLinkedList;
}

int LinkedList::size(process *caller) {
    return (int) _list.size();
}

void LinkedList::add(process *caller, int index, Object *ob) {
    list_t::iterator it = getItrFromIndex(index);
    if (it != _list.end()) _list.insert(it, ob);
}

bool LinkedList::add(process *caller, Object *ob) {
    _list.push_back(ob);
    return true;
}

bool LinkedList::add(Object *ob) {
    _list.push_back(ob);
    return true;
}

void LinkedList::addFirst(process *caller, Object *ob) {
    _list.push_front(ob);
}

void LinkedList::addLast(process *caller, Object *ob) {
    _list.push_back(ob);
}

Object *LinkedList::getFirst(process *caller) {
    return _list.front();
}

Object *LinkedList::getLast(process *caller) {
    return _list.back();
}

void LinkedList::removeRange(process *caller, int fromIndex, int toIndex) {
    if (toIndex >= size(caller) || toIndex < fromIndex) return;
    _list.erase(getItrFromIndex(fromIndex), getItrFromIndex(toIndex));
}

Object *LinkedList::remove(process *caller, int index) {
    list_t::iterator it = getItrFromIndex(index);
    if (it == _list.end()) return NULL;
    Object *prevOb = *it;
    _list.erase(it);
    return prevOb;
}

Object *LinkedList::removeFirst(process *caller) {
    Object *prevOb = _list.front();
    _list.pop_front();
    return prevOb;
}

Object *LinkedList::removeLast(process *caller) {
    Object *prevOb = _list.back();
    _list.pop_back();
    return prevOb;
}

void LinkedList::clear(process *caller) {
    _list.clear();
}

bool LinkedList::contains(process *caller, Object *ob) {
    return (indexOf(caller, ob) >= 0);
}

int LinkedList::indexOf(process *caller, Object *ob) {
    if (!ob) {
        for (int i = 0; i < size(caller); i++) {
            if (get(caller, i) == NULL) return i;
        } // end for
    } else {
        for (int i = 0; i < size(caller); i++) {
            Object *o = get(caller, i);
            if (o && o == ob) return i;
        } // end for
    } // end if

    return -1;
}

int LinkedList::lastIndexOf(process *caller, Object *ob) {
    if (!ob) {
        for (int i = size(caller) - 1; i >= 0; i--) {
            if (get(caller, i) == NULL) return i;
        } // end for
    } else {
        for (int i = size(caller) - 1; i >= 0; i--) {
            Object *o = get(caller, i);
            if (o && o == ob) return i;
        } // end for
    } // end if

    return -1;
}

Object *LinkedList::get(process *caller, int index) {
    list_t::iterator it = getItrFromIndex(index);
    if (it == _list.end()) return NULL;
    return *it;
}

Object *LinkedList::set(process *caller, int index, Object *ob) {
    Object *prevOb = NULL;
    if ((prevOb = remove(caller, index)) == NULL) return NULL;
    add(caller, index, ob);
    return prevOb;
}

LinkedList::list_t::iterator LinkedList::getItrFromIndex(int index) {
    int i;
    list_t::iterator it;
    for (i = 0, it = _list.begin(); i < index && it != _list.end(); i++, it++);
    return it;
}

LinkedListItr::LinkedListItr(LinkedList LinkedList) {
    _LinkedList = LinkedList;
    _currentItr = _LinkedList._list.begin();
}

bool LinkedListItr::hasNext(process *caller) {
    return _currentItr != _LinkedList._list.end();
}

Object *LinkedListItr::next(process *caller) {
    Object *result = (*_currentItr);
    _currentItr++;

    return result;
}

void LinkedListItr::remove(process *caller) {
    list_t::iterator currentItr = _currentItr++;
    _LinkedList._list.erase(currentItr);
}
