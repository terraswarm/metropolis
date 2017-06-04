/*
Copyright (c) 2002-2005 The Regents of the University of California.
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

#ifndef __UTIL_HASHMAP_H__
#define __UTIL_HASHMAP_H__

#include <map>

#include "systemc.h"
#include "object.h"
#include "global.h"

//using namespace std;

class MapEntry;
class HashMapItr;

class HashMap : public Object {
public:
  HashMap(DUMMY_CTOR_ARG _arg, int *index) {}

        HashMap();
  HashMap(HashMap *map);
  virtual ~HashMap();

private:
        typedef std::map<Object*, Object*> map_t;
        map_t _map;

        friend class HashMapItr;

public:
        virtual int size(process *caller);
        virtual bool isEmpty(process *caller);

        virtual MapEntry *findValue(process *caller, Object *value);
        virtual bool containsValue(process *caller, Object *value);
        virtual MapEntry *findKey(process *caller, Object *key);
        virtual bool containsKey(process *caller, Object *key);

        virtual Object *get(process *caller, Object *key);
        virtual Object *put(process *caller, Object *key, Object *value);
        virtual Object *put(Object *key, Object *value);
        virtual Object *remove(process *caller, Object *key);
        virtual void putAll(process *caller, HashMap *map);
        virtual void clear(process *caller);
        virtual Object *clone(process *caller);
};

class MapEntry : public Object {
public:
        MapEntry() {}
        MapEntry(const MapEntry *map) { _it = map->_it; _key = map->_key; _value = map->_value; }
        MapEntry(Object *key, Object *value) { _key = key; _value = value; }
        virtual ~MapEntry() {};

private:
        typedef std::map<Object *, Object *> map_t;

        map_t::iterator _it;
        Object *_key;
        Object *_value;

        friend class HashMap;

        map_t::iterator getItr() const { return _it; }

private:
        MapEntry(map_t::iterator it) { _it = it; _key = it->first; _value = it->second; }

public:
        virtual Object *getKey(process *caller) const { return _key; }
        virtual Object *getValue(process *caller) const { return _value; }
};

class HashMapItr {
public:
        HashMapItr(DUMMY_CTOR_ARG _arg, int *index) {}
        HashMapItr() {}
        HashMapItr(HashMap hashMap);
        virtual ~HashMapItr() {}

private:
        HashMap _hashMap;

        typedef std::map<Object *, Object *> map_t;
        map_t::iterator _currentItr;

public:
        virtual bool hasNext(process *caller);
        virtual MapEntry *next(process *caller);
        virtual void remove(process *caller);
};

#endif
