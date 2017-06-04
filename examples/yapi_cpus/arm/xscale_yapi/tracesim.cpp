/*
  @Copyright (c) 2004 The Regents of the University of California.
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

// OVERVIEW: A list of Trace Instructions for use with the Metropolis
///          microarchitectural models of the Strongarm and XScale microprocessors
//
// AUTHOR: Trevor Meyerowitz (tcm@eecs.berkeley.edu)
//


#include "tracesim.h"

bool verbose=0, trace=0;

TraceList::TraceList(const char *filename) {
    ifstream fin;
    char name[10];
    unsigned int i, j, x, temp;



    curr_inst_num=0; // start from the bottom
    temp = getNumLines(filename);
    fin.open(filename);

    trace_list = new CTraceInst*[temp];



    // Read in the trace information
    x=0;
    while (fin.peek() != EOF) {
        fin >> name >> std::hex >> i >> std::hex >> j;
        //printf("%s %08x %08x\n", name, i, j);

        // read valid instructions
        if (strcmp(name, "NO") != 0) {
            trace_list[x] = new CTraceInst(i, j, (strcmp(name, "EX")==0));
            x++;
        }
    }

    size = x;


    fin.close();
}

TraceList::~TraceList() {
    for (int x=0; x<temp; x++)
        delete trace_list[x];
    delete trace_list;
}


unsigned int TraceList::getNumLines(const char *filename) {
    const static unsigned int MAXCHAR = 1024; // max line length in characters
    std::ifstream file(filename);
    if (!file) return 0;     // Some error message here, file not found
    unsigned int kount = 0;
    while (file.peek() != EOF) {
        ++kount;
        file.ignore(MAXCHAR, '\n');
    }
    file.close();

    return kount;
}

unsigned int TraceList::getSize() {
    return size;
}

CTraceInst* TraceList::getInst(int i) {
    //curr_inst_num++;

    return trace_list[i];
}

/*
  unsigned int TraceList::getCurrInstNum() {
  return curr_inst_num;
  }
*/

bool TraceList::isFinished() {
    return (curr_inst_num >= size);
}

void TraceList::print() {
    for (int x=0; x<size; x++) {
        printf("inst %d: ", x);
        trace_list[x]->print();
    }
}


/*

int main(int argc, char* argv[]) {
TraceList *tlist;

if (argc < 2) {
cerr << argc << endl << "Usage: tracesim <trace_file_name>\n";
exit(-1);
}

tlist = new TraceList(argv[1]);
tlist->print();

delete tlist;

return 0;
}
*/
