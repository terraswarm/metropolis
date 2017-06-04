/*
Copyright (c) 2001-2005 The Regents of the University of California.
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
#include "defs.h"

void transitive_closure(unsigned *R, int n)
{
    int rowsize;
    unsigned mask;
    unsigned *rowj;
    unsigned *rp;
    unsigned *rend;
    unsigned *ccol;
    unsigned *relend;
    unsigned *cword;
    unsigned *rowi;

    rowsize = WORDSIZE(n);
    relend = R + n * rowsize;

    cword = R;
    mask = 1;
    rowi = R;
    while (rowi < relend) {
        ccol = cword;
        rowj = R;

        while (rowj < relend) {
            if (*ccol & mask) {
                rp = rowi;
                rend = rowj + rowsize;
                while (rowj < rend)
                    *rowj++ |= *rp++;
            } else {
                rowj += rowsize;
            }

            ccol += rowsize;
        }

        mask <<= 1;
        if (mask == 0) {
            mask = 1;
            cword++;
        }

        rowi += rowsize;
    }
}

void reflexive_transitive_closure(unsigned *R, int n)
{
    int rowsize;
    unsigned mask;
    unsigned *rp;
    unsigned *relend;

    transitive_closure(R, n);

    rowsize = WORDSIZE(n);
    relend = R + n * rowsize;

    mask = 1;
    rp = R;
    while (rp < relend) {
        *rp |= mask;
        mask <<= 1;
        if (mask == 0) {
            mask = 1;
            rp++;
        }

        rp += rowsize;
    }
}
