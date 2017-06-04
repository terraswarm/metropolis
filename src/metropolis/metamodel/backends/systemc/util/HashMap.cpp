/*
  Copyright (c) 2002-2004 The Regents of the University of California.
  All rights reserved.

  Permission is hereby granted, without written agreement and without
  license or royalty fees, to use, copy, modify, and distribute this
  software and its documentation for any purpose, provided that the
  above copyright notice and the following two paragraphs appear in all
  copies of this software.

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

#include "HashMap.h"

HashMap::HashMap() {
}

HashMap::HashMap(HashMap *hmap) {
    for (map_t::iterator it = hmap->_map.begin(); it != hmap->_map.end(); it++)
        _map.insert(map_t::value_type(it->first, it->second));
}

HashMap::~HashMap() {
}

int HashMap::size(process *caller) {
    return _map.size();
}

bool HashMap::isEmpty(process *caller) {
    return _map.empty();
}

MapEntry *HashMap::findValue(process *caller, Object *value) {
    for (map_t::iterator it = _map.begin(); it != _map.end(); it++) {
        if (it->second == value) return new MapEntry(it->first, it->second);
    } // end for
    return NULL;
}

bool HashMap::containsValue(process *caller, Object *value) {
    for (map_t::iterator it = _map.begin(); it != _map.end(); it++) {
        if (it->second == value) return true;
    } // end for
    return false;
}

MapEntry *HashMap::findKey(process *caller, Object *key) {
    map_t::iterator it = _map.find(key);
    if (it == _map.end()) return NULL;
    return new MapEntry(it);
}

bool HashMap::containsKey(process *caller, Object *key) {
    map_t::iterator it = _map.find(key);
    if (it == _map.end()) return false;
    return true;
}

Object *HashMap::get(process *caller, Object *key) {
    MapEntry *e = findKey(caller,  key);
    if (e == NULL) return NULL;
    Object *result = e->getValue(caller);
    delete e;
    return result;
}

Object *HashMap::put(process *caller, Object *key, Object *value) {
    MapEntry *e = findKey(caller, key);
    Object *result;
    if (e == NULL) {
        result = NULL;
        _map.insert(map_t::value_type(key,value));
    }
    else {
        result = e->getValue(caller);
        _map.erase(e->getItr());
        delete e;
        _map.insert(map_t::value_type(key, value));
    }
    return result;
}

Object *HashMap::put(Object *key, Object *value) {
    process *caller = NULL;
    int mode = 0;
    String *intfcName = NULL;
    MapEntry *e = findKey(caller, key);
    if (e == NULL) return NULL;
    Object *result = e->getValue(caller);
    _map.erase(e->getItr());
    delete e;
    _map.insert(map_t::value_type(key, value));
    return result;
}

Object *HashMap::remove(process *caller, Object *key) {
    MapEntry *e = findKey(caller, key);
    if (e == NULL) return NULL;
    Object *result = e->getValue(caller);
    _map.erase(e->getItr());
    delete e;
    return result;
}

void HashMap::putAll(process *caller, HashMap *hmap) {
    if (!hmap) return;

    for (map_t::iterator it = hmap->_map.begin(); it != hmap->_map.end(); it++)
        put(caller, it->first, it->second);
}

void HashMap::clear(process *caller) {
    _map.clear();
}

Object *HashMap::clone(process *caller) {
    HashMap *newHashMap = new HashMap();
    newHashMap->putAll(caller, this);

    return newHashMap;
}

HashMapItr::HashMapItr(HashMap hashMap) {
    _hashMap = hashMap;
    _currentItr = _hashMap._map.begin();
}

bool HashMapItr::hasNext(process *caller) {
    return _currentItr != _hashMap._map.end();
}

MapEntry *HashMapItr::next(process *caller) {
    MapEntry *result = new MapEntry(_currentItr->first, _currentItr->second);
    _currentItr++;

    return result;
}

void HashMapItr::remove(process *caller) {
    map_t::iterator currentItr = _currentItr++;
    _hashMap._map.erase(currentItr);
}
