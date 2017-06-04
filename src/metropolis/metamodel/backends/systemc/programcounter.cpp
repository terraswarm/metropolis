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
#include "programcounter.h"
#include "event.h"
#include "scoreboard.h"
#include "GlobalTime.h"
#include "assert.h"
#include <algorithm>

String *ProgramCounter::NO_EVENT_TAG = new String("");
String *ProgramCounter::BEG_TAG = new String("begin");
String *ProgramCounter::END_TAG = new String("end");

ProgramCounter::ProgramCounter(process *pp): p(pp) {
    obj.object = (sc_object *) pp;
    obj.intfcName = (char *) Empty_String;
    _pScoreboard = NULL;
    stLabel = (char *) Empty_String;
    SchedState = SchedStateVal::RUN;
    cond = NULL;
    selected = -1;
    funcType = 0;
    awaitTestDepth = 0;
    mode = RunType::EXEC;
    _inSynch = false;
    _potentialUsedMedia = new MediaSet_t;
    currentLineNumber = (String*)0;
    eventTag = ProgramCounter::NO_EVENT_TAG;
    nextLineContextStack = new _2DStrListStack_t();
}

void ProgramCounter::initAwait(int numCs, bool nondet) {
    _csOrder.clear();
    for (int j = 0; j < numCs; j++) _csOrder.push_back(j);

    if (nondet) {
        for (int j = 0; j < numCs; j++) {
            int pos = (int)(random() % numCs);
            int swap = _csOrder[pos];
            _csOrder[pos] = _csOrder[j];
            _csOrder[j] = swap;
        } // end for
    } // end if
} // end initAwait

void ProgramCounter::addPendingEvent(event *e, int csNumber) {
    if (!e) return;
    _pendingEvents.insert(eventMap_t::value_type(e, csNumber));
    e->inPendingList = true;
} // end addPendingEvent

void ProgramCounter::freeAllPendingEvents() {
    eventMap_t::iterator it = _pendingEvents.begin();
    for(; it != _pendingEvents.end(); it++)
        it->first->inPendingList = false;
    _pendingEvents.clear();
} // end addPendingEvent

void ProgramCounter::savePendingEvents() {
    _oldPendingEvents = _pendingEvents;
} // end savePendingEvents

void ProgramCounter::loadPendingEvents() {
    _pendingEvents = _oldPendingEvents;
    eventMap_t::iterator it = _pendingEvents.begin();
    for(; it != _pendingEvents.end(); it++)
        it->first->inPendingList = true;
} // end loadPendingEvents

bool ProgramCounter::containsPendingEvent(event *e) {
    /*
      eventMap_t::iterator it = _pendingEvents.find(e);
      return (it != _pendingEvents.end());
    */
    if (e->getProcess()->pc != this) return false;
    return (e->inPendingList);
} // end containsPendingEvent

bool ProgramCounter::setMustDo(event *e) {
    if (! e->inPendingList) return false;

    if (_pendingEvents.size() > 1) {
        eventMap_t::iterator it = _pendingEvents.find(e);
        int csNumber = 0;
        //if (it == _pendingEvents.end()) return false;
        csNumber = it->second;
        freeAllPendingEvents();
        addPendingEvent(e, csNumber);
    }

    return true;
} // end setMustDo

bool ProgramCounter::setMustNotDo(event *e) {
    if (! e->inPendingList) return false;
    eventMap_t::iterator it = _pendingEvents.find(e);
    //if (it == _pendingEvents.end()) return false;

    if (SchedState == SchedStateVal::RUN && selected == it->second)
        if (testSchedFlag) SchedState = SchedStateVal::TESTSCHEDULING;
        else  SchedState = SchedStateVal::UNKNOW;
    _pendingEvents.erase(it);
    e->inPendingList = false;
    selected = -1;
    return true;
} // end setMustNotDo

void ProgramCounter::setState(sc_object * pp, const char *intfc, const char *ll, int ss) {
    obj.object = pp;
    if (intfc!=Keep_Unchanged_String)
        obj.intfcName = (char *) intfc;
    if (ll!=Keep_Unchanged_String) stLabel = (char *) ll;
    SchedState = ss;
}

void ProgramCounter::saveState() {
    labelList.insert(labelList.begin(), new String(stLabel));
    nodeList.insert(nodeList.begin(), new node(obj));
    SchedStateList.insert(SchedStateList.begin(), new int(SchedState));
    funcTypeList.insert(funcTypeList.begin(), new int(funcType));

    _pendingEventsStack.insert(_pendingEventsStack.begin(), _pendingEvents);
    _pendingEvents.clear();

    _potentialUsedMediaList.push_back(_potentialUsedMedia);
    _potentialUsedMedia = new MediaSet_t;

    pushNewNextLineContext();
} // end saveState

void ProgramCounter::restoreState() {
    node * n = nodeList.front();
    String * l = labelList.front();
    int * s = SchedStateList.front();
    int * f = funcTypeList.front();

    popNextLineContext();

    nodeList.erase(nodeList.begin());
    labelList.erase(labelList.begin());
    SchedStateList.erase(SchedStateList.begin());
    funcTypeList.erase(funcTypeList.begin());
    obj = n;
    stLabel = (char *) l->c_str();
    SchedState = *s;
    funcType = *f;
    delete n;
    delete l;
    delete s;
    delete f;

    if (!_pendingEventsStack.empty()) {
        _pendingEvents = _pendingEventsStack.front();
        _pendingEventsStack.erase(_pendingEventsStack.begin());
    } // end if

    delete _potentialUsedMedia;
    _potentialUsedMedia = _potentialUsedMediaList.back();
    _potentialUsedMediaList.pop_back();
} // end restoreState

void ProgramCounter::setPotentialUsedMedia() {
    for (MediaSet_t::iterator it=_potentialUsedMedia->begin(); it!=_potentialUsedMedia->end(); it++) {
        medium * m = (medium*) (*it);
        m->_potentialUserPCs.insert(this);
    }
} // end setPotentialUsedMedia

void ProgramCounter::resetPotentialUsedMedia() {
    for (MediaSet_t::iterator it=_potentialUsedMedia->begin(); it!=_potentialUsedMedia->end(); it++) {
        medium * m = (medium*) (*it);
        m->_potentialUserPCs.erase(this);
    }
} // end resetPotentialUsedMedia

/**
 * Methods used for metamodel debugging:
 */
void ProgramCounter::enterAwaitTest() {
    awaitTestDepth++;
}

void ProgramCounter::exitAwaitTest() {
    awaitTestDepth--;
}

bool ProgramCounter::inAwaitTest() {
    return (awaitTestDepth > 0);
}

/**
 * Push the given single next-line string onto this PC's nextLineStack,
 * as a strList_t containing one element.  An empty string has special
 * meaning; if the string is empty, push an empty list.
 */
void ProgramCounter::pushNextLine(const char* nextLine) {
    strList_t* list = new strList_t();
    if (strcmp(nextLine, "") != 0) {
        list->push_back(new String(nextLine));
    }
    pushNextLineList(list);
}

/**
 * Add the given next-line string to the list on top of this PC's nextLineStack.
 */
void ProgramCounter::addNextLine(const char* nextLine) {
    strList_t* list = getNextLines();
    // list->push_back(new String(nextLine));
    String* s = new String(nextLine);
    list->push_back(s);
}

/**
 * Push the given list of next-line Strings onto this PC's nextLineStack.
 */
void ProgramCounter::pushNextLineList(strList_t* list) {
    _2DStrList_t *nextLineStack = getCurrentNextLineContext();
    nextLineStack->push_back(list);
}

/**
 * Pop the top list of next-line Strings off this PC's nextLineStack,
 * and destroy it.
 */
void ProgramCounter::popNextLines() {
    void destroyListContents(strList_t*);
    _2DStrList_t *nextLineStack = getCurrentNextLineContext();
    assert(nextLineStack->size() > 0);
    strList_t* list = nextLineStack->back();
    destroyListContents(list);
    delete list;
    nextLineStack->pop_back();
}

// /**
//  * Return the top list of next-line Strings currently on this PC's
//  * nextLineStack.
//  */
// strList_t* ProgramCounter::getNextLines() {
//     _2DStrList_t *nextLineStack = getCurrentNextLineContext();
//     int i = nextLineStack->size() - 1;
//     assert(i>=0);
//     return (nextLineStack->at(i));
// }

/**
 * Return the top non-empty list of next-line Strings currently on this PC's
 * nextLineStack, or null if there is no non-empty list.
 */
strList_t* ProgramCounter::getNextLines() {
    _2DStrList_t *nextLineStack;
    strList_t *list;
    _2DStrListStack_t::reverse_iterator cIter;
    _2DStrList_t::reverse_iterator      lIter;

    for (cIter = nextLineContextStack->rbegin();
         cIter < nextLineContextStack->rend(); cIter++) {
        nextLineStack = *cIter;
        for (lIter = nextLineStack->rbegin();
             lIter < nextLineStack->rend(); lIter++) {
            list = *lIter;
            if (list->size() > 0) {
                return (list);
            }
        }
    }

    return ((strList_t *)0);
}

/**
 *
 */
void ProgramCounter::dumpNextLines() {
    _2DStrList_t *nextLineStack;
    strList_t *list;
    _2DStrListStack_t::reverse_iterator cIter;
    _2DStrList_t::reverse_iterator      lIter;
    strList_t::iterator                 sIter;

    cerr << "< ";
    for (cIter = nextLineContextStack->rbegin();
         cIter < nextLineContextStack->rend(); cIter++) {
        nextLineStack = *cIter;
        cerr << "{ ";
        for (lIter = nextLineStack->rbegin();
             lIter < nextLineStack->rend(); lIter++) {
            list = *lIter;
            cerr << "[ ";
            for (sIter = list->begin();
                 sIter != list->end(); sIter++) {
                String *s = *sIter;
                if (sIter != list->begin()) {
                    cerr << ", ";
                }
                cerr << s->c_str();
            }
            cerr << " ]";
        }
        cerr << " }";
    }
    cerr << " >" << endl;
}

/**
 * Return the top next-line context on the next-line context stack.
 */
_2DStrList_t* ProgramCounter::getCurrentNextLineContext() {
    int i = nextLineContextStack->size() - 1;
    assert(i>=0);
    return (nextLineContextStack->back());
}

/**
 * Debugging version of getCurrentnextLineContext().
 */
void ProgramCounter::pushNewNextLineContext(const char* msg1, const char* msg2) {
    cerr << msg1 << " -- " << msg2 << ": ";
    pushNewNextLineContext();
}

/**
 * Add a new next-line context stack to nextLineContextStack.
 */
void ProgramCounter::pushNewNextLineContext() {
    _2DStrList_t* newContext = new _2DStrList_t();
    nextLineContextStack->push_back(newContext);
}

/**
 * Debugging version of popnextLineContext().
 */
void ProgramCounter::popNextLineContext(const char* msg1, const char* msg2) {
    cerr << msg1 << " -- " << msg2 << ": ";
    popNextLineContext();
}

/**
 * Remove and destroy the top-most next-line context stack from
 * nextLineContextStack.
 */
void ProgramCounter::popNextLineContext() {
    void destroyListContents(_2DStrList_t*);
    _2DStrList_t* topStack = getCurrentNextLineContext();
    destroyListContents(topStack);
    delete topStack;
    nextLineContextStack->pop_back();
}

/**
 * Return the current currentLineNumber value.
 */
String* ProgramCounter::getCurrentLineNumber() {
    return currentLineNumber;
}

/**
 * Set this PC's currentLineNumber String to the one given.
 * As a side-effect, have the scoreboard update its list of all
 * processes' current-line strings, for access from the debugger.
 */
void ProgramCounter::setCurrentLineNumber(const char* lineNumber) {
    delete currentLineNumber;
    currentLineNumber = new String(lineNumber);
    getScoreboard()->updateCurrentLines();
}

/*
 * Memory management for String vectors, and String vector vectors,
 * and String vector vector vectors :-).
 */
void destroyListContents(strList_t *theList) {
    if (theList == (strList_t *)0) {
        return;
    }
    for (strList_t::iterator it = theList->begin();
         it != theList->end(); it++) {
        String* element = *it;
        delete element;
    }
    theList->clear();
}

void destroyListContents(_2DStrList_t *theList) {
    if (theList == (_2DStrList_t *)0) {
        return;
    }
    for (_2DStrList_t::iterator it = theList->begin();
         it != theList->end(); it++) {
        strList_t* element = *it;
        destroyListContents(element);
        delete element;
    }
    theList->clear();
}

void destroyListContents(_2DStrListStack_t *theList) {
    if (theList == (_2DStrListStack_t *)0) {
        return;
    }
    for (_2DStrListStack_t::iterator it = theList->begin();
         it != theList->end(); it++) {
        _2DStrList_t* element = *it;
        destroyListContents(element);
        delete element;
    }
    theList->clear();
}
