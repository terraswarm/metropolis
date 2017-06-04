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
#ifndef MMSCS_PROGRAMCOUNTER_H
#define MMSCS_PROGRAMCOUNTER_H

#include "global.h"
#include "node.h"

#include <vector>
#include <string>
#include <map>

class event;
class scoreboard;

class ProgramCounter {
    public:
ProgramCounter(process *pp);

public:
        process* p;
        scoreboard *_pScoreboard;
        node obj;
        char * stLabel;
        // Why is this capitalized?
        int SchedState;

        eventList_t _eventList;
        eventMap_t _pendingEvents;
        eventMap_t _oldPendingEvents;

        sc_event invoke;
        int funcType;
        int mode;

        bool * cond;

        int selected;
        _2DListStack_t testList;
        _2DListStack_t setList;
        _2DList_t preventList;
        simpleList_t nodeList;
        strList_t labelList;
        intList_t SchedStateList;
        intList_t funcTypeList;
        // vector of critical sections ordered according to the selection
        // priority
        intVect_t _csOrder;

        // vector of program counters blocked by this program counter
        pcList_t _pcBlocked;
        bool _inSynch;

        // potentially used media that appear in test/set lists in await
        MediaSet_t * _potentialUsedMedia;
        MediaSetList_t _potentialUsedMediaList;

        // Used in metamodel debugging:
        // A stack of lists of next executable metamodel source-code line #'s
        strList_t nextMMLines;
        int mmLineNumberIndex;
        String *currentLineNumber;
        String *eventTag;
        static String *BEG_TAG;
        static String *END_TAG;
        static String *NO_EVENT_TAG;

        // FIXME: These should be protected, after this is debugged:
        // Use in metamodel debugging:
        int awaitTestDepth; // We want to be able to make await test sections
                            // opaque to the debugger.
        _2DStrListStack_t *nextLineContextStack;
        _2DStrList_t *getCurrentNextLineContext();

protected:
        typedef std::vector<eventMap_t> eventMapStack_t;
        eventMapStack_t _pendingEventsStack;

public:
        scoreboard *getScoreboard() const { return _pScoreboard; }
        void setScoreboard(scoreboard *sc) { _pScoreboard = sc; }

        void initAwait(int numCs, bool nondet);
        void addPendingEvent(event *e, int csNumber);
        void freeAllPendingEvents();
        void savePendingEvents();
        void loadPendingEvents();
        bool containsPendingEvent(event *e);
        bool setMustDo(event *e);
        bool setMustNotDo(event *e);
        void setState(sc_object * pp, const char *intfc, const char *ll, int ss);
        void saveState();
        void restoreState();
        void setPotentialUsedMedia();
        void resetPotentialUsedMedia();
        // For metamodel debugging:
        void enterAwaitTest();
        void exitAwaitTest();
        bool inAwaitTest();
        void pushNextLineList(strList_t* list);
        void pushNextLine(const char* nextLine);
        void addNextLine(const char* nextLine);

        void pushNewNextLineContext();
        void popNextLineContext();
        // Debugging versions:
        void pushNewNextLineContext(const char *msg1, const char *msg2);
        void popNextLineContext(const char *msg1, const char *msg2);

        strList_t* getNextLines();
        void popNextLines();
        void dumpNextLines();
        // void addNextMMLineNumber(String *s);
        // void clearNextMMLineNumbers();
        // strList_t getNextMMLineNumbers();
        String* getCurrentLineNumber();
        void setCurrentLineNumber(String* lineNumberString);
        void setCurrentLineNumber(const char* lineNumberCharStar);
};

#endif
