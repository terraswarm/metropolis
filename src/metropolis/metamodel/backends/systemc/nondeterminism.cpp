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
#include "nondeterminism.h"
#include "assert.h"
#include "global.h" // get the random() declaration
#include <stdlib.h>

//generate an integer array which is a permutation of
//integers from 0 to size-1
//array : the array
//size  : the length of the array
//nondet: true -- randomize the order of the integers
//                                false-- keep the increasing order of the integers
int* intArrayGen(int size, bool nondet)
{
    assert(size>0);
    int * array = new int[size];
    for (int j=0; j<size; j++) array[j]=j;

    if (nondet) {
        for (int j=0; j<size; j++) {
            int pos = (int)(random()%size);
            int swap = array[pos];
            array[pos] = array[j];
            array[j] = swap;
        }
    }
    return array;
}

//return a nondeterministic int number of 'size' bytes
int nondeterminism(unsigned short size)
{
    unsigned short _size;
    if (size>sizeof(RAND_MAX)) _size = sizeof(RAND_MAX);
    else _size = size;
    return rand()>>((sizeof(RAND_MAX) - _size)<<3);
}
