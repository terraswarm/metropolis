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
#include <stdio.h>
#include <stdlib.h>
// #include <malloc.h>
#include <string.h>

#include "defs.h"


/* TABLE_SIZE is the number of entries in the symbol table. */
/* TABLE_SIZE must be a power of two.                            */

#define        TABLE_SIZE 1024


bucket **symbol_table;
bucket *first_symbol;
bucket *last_symbol;


int hash(char *name)
{
    char *s;
    int c, k;

    assert(name && *name);
    s = name;
    k = *s;
    while ((c = (int) *(++s)) != 0)
        k = (31 * k + c) & (TABLE_SIZE - 1);
    return (k);
}


bucket *make_bucket(char *name)
{
    bucket *bp;

    assert(name);
    bp = (bucket *) MALLOC(sizeof(bucket));
    if (bp == 0)
        no_space();
    bp->link = 0;
    bp->next = 0;
    bp->name = MALLOC(strlen(name) + 1);
    if (bp->name == 0)
        no_space();
    bp->tag = 0;
    bp->value = UNDEFINED;
    bp->index = 0;
    bp->prec = 0;
    bp->class = UNKNOWN;
    bp->assoc = TOKEN;
    if (bp->name == 0)
        no_space();
    strcpy(bp->name, name);
    return (bp);
}


bucket *lookup(char *name)
{
    bucket *bp, **bpp;

    bpp = symbol_table + hash(name);
    bp = *bpp;

    while (bp) {
        if (strcmp(name, bp->name) == 0)
            return (bp);
        bpp = &bp->link;
        bp = *bpp;
    }

    *bpp = bp = make_bucket(name);
    last_symbol->next = bp;
    last_symbol = bp;

    return (bp);
}


void create_symbol_table(void)
{
    int i;
    bucket *bp;

    symbol_table = (bucket **) MALLOC(TABLE_SIZE * sizeof(bucket *));
    if (symbol_table == 0)
        no_space();
    for (i = 0; i < TABLE_SIZE; i++)
        symbol_table[i] = 0;

    bp = make_bucket("error");
    bp->index = 1;
    bp->class = TERM;

    first_symbol = bp;
    last_symbol = bp;
    symbol_table[hash("error")] = bp;
}


void free_symbol_table(void)
{
    FREE(symbol_table);
    symbol_table = 0;
}


void free_symbols(void)
{
    bucket *p, *q;

    for (p = first_symbol; p; p = q) {
        q = p->next;
        FREE(p);
    }
}
