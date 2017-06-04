/***** ltl2ba : ltl2ba.h *****/

/* Written by Denis Oddoux, LIAFA, France                                 */
/*                                                                        */
/* This program is free software; you can redistribute it and/or modify   */
/* it under the terms of the GNU General Public License as published by   */
/* the Free Software Foundation; either version 2 of the License, or      */
/* (at your option) any later version.                                    */
/*                                                                        */
/* This program is distributed in the hope that it will be useful,        */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of         */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          */
/* GNU General Public License for more details.                           */
/*                                                                        */
/* You should have received a copy of the GNU General Public License      */
/* along with this program; if not, write to the Free Software            */
/* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA*/
/*                                                                        */
/* Based on the translation algorithm by Gastin and Oddoux,               */
/* presented at the CAV Conference, held in 2001, Paris, France 2001.     */
/* Send bug-reports and/or questions to: Denis.Oddoux@liafa.jussieu.fr    */
/* or to Denis Oddoux                                                     */
/*       LIAFA, UMR 7089, case 7014                                       */
/*       Universite Paris 7                                               */
/*       2, place Jussieu                                                 */
/*       F-75251 Paris Cedex 05                                           */
/*       FRANCE                                                           */

/* Some of the code in this file was taken from the Spin software         */
/* Written by Gerard J. Holzmann, Bell Laboratories, U.S.A.               */

#include <stdio.h>
#include <string.h>


#if (defined(__WIN32__) || defined(_WIN32)) && !defined(__CYGWIN32__)
/* Windows Visual C++ */
#else
#ifndef __MINGW32__
#define LTL_GENERATE_STATS
#endif
#endif

#ifdef LTL_GENERATE_STATS
#include <sys/times.h>
#endif


typedef struct Symbol {
char                *name;
        struct Symbol        *next;        /* linked list, symbol table */
} Symbol;

typedef struct Node {
        short                ntyp;        /* node type */
        struct Symbol        *sym;
        struct Node        *lft;        /* tree */
        struct Node        *rgt;        /* tree */
        struct Node        *nxt;        /* if linked list */
} Node;

typedef struct Graph {
        Symbol                *name;
        Symbol                *incoming;
        Symbol                *outgoing;
        Symbol                *oldstring;
        Symbol                *nxtstring;
        Node                *New;
        Node                *Old;
        Node                *Other;
        Node                *Next;
        unsigned char        isred[64], isgrn[64];
        unsigned char        redcnt, grncnt;
        unsigned char        reachable;
        struct Graph        *nxt;
} Graph;

typedef struct Mapping {
        char        *from;
        Graph        *to;
        struct Mapping        *nxt;
} Mapping;

typedef struct ATrans {
  int *to;
  int *pos;
  int *neg;
  struct ATrans *nxt;
} ATrans;

typedef struct AProd {
  int astate;
  struct ATrans *prod;
  struct ATrans *trans;
  struct AProd *nxt;
  struct AProd *prv;
} AProd;


typedef struct GTrans {
  int *pos;
  int *neg;
  struct GState *to;
  int *final;
  struct GTrans *nxt;
} GTrans;

typedef struct GState {
  int id;
  int incoming;
  int *nodes_set;
  struct GTrans *trans;
  struct GState *nxt;
  struct GState *prv;
} GState;

typedef struct BTrans {
  struct BState *to;
  int *pos;
  int *neg;
  struct BTrans *nxt;
} BTrans;

typedef struct BState {
  struct GState *gstate;
  int id;
  int dist;
  int incoming;
  int final;
  struct BTrans *trans;
  struct BState *nxt;
  struct BState *prv;
} BState;

typedef struct GScc {
  struct GState *gstate;
  int rank;
  int theta;
  struct GScc *nxt;
} GScc;

typedef struct BScc {
  struct BState *bstate;
  int rank;
  int theta;
  struct BScc *nxt;
} BScc;

enum {
        ALWAYS=257,
        AND,                /* 258 */
        EQUIV,                /* 259 */
        EVENTUALLY,        /* 260 */
        LTL_FALSE,                /* 261 */
        IMPLIES,        /* 262 */
        NOT,                /* 263 */
        OR,                /* 264 */
        PREDICATE,        /* 265 */
        LTL_TRUE,                /* 266 */
        U_OPER,                /* 267 */
        V_OPER                /* 268 */
#ifdef NXT
        , NEXT                /* 269 */
#endif
};

Node        *Canonical(Node *);
Node        *canonical(Node *);
Node        *cached(Node *);
Node        *dupnode(Node *);
Node        *getnode(Node *);
Node        *in_cache(Node *);
Node        *push_negation(Node *);
Node        *right_linked(Node *);
Node        *tl_nn(int, Node *, Node *);

Symbol        *tl_lookup(char *);
Symbol        *getsym(Symbol *);
Symbol        *DoDump(Node *);

char        *emalloc(int);

int        anywhere(int, Node *, Node *);
int        dump_cond(Node *, Node *, int);
int        isequal(Node *, Node *);
int        tl_Getchar(void);

void        *tl_emalloc(int);
ATrans  *emalloc_atrans();
void    free_atrans(ATrans *, int);
void    free_all_atrans();
GTrans  *emalloc_gtrans();
void    free_gtrans(GTrans *, GTrans *, int);
BTrans  *emalloc_btrans();
void    free_btrans(BTrans *, BTrans *, int);
void        a_stats(void);
void        addtrans(Graph *, char *, Node *, char *);
void        cache_stats(void);
void        dump(Node *);
/* MSVC, CYGWIN: declare exit(int); */
#include <stdlib.h>
//void        exit(int);
void        Fatal(char *, char *);
void        fatal(char *, char *);
void        fsm_print(void);
void        releasenode(int, Node *);
void        tfree(void *);
void        tl_explain(int);
void        tl_UnGetchar(void);
void        tl_parse(void);
void        tl_yyerror(char *);
void        trans(Node *);

void    mk_alternating(Node *);
void    mk_generalized();
void    mk_buchi();

ATrans *dup_trans(ATrans *);
ATrans *merge_trans(ATrans *, ATrans *);
void do_merge_trans(ATrans **, ATrans *, ATrans *);

int  *new_set(int);
int  *clear_set(int *, int);
int  *make_set(int , int);
void copy_set(int *, int *, int);
int  *dup_set(int *, int);
void merge_sets(int *, int *, int);
void do_merge_sets(int *, int *, int *, int);
int  *intersect_sets(int *, int *, int);
void add_set(int *, int);
void rem_set(int *, int);
void spin_print_set(int *, int*);
void print_set(int *, int);
int  empty_set(int *, int);
int  empty_intersect_sets(int *, int *, int);
int  same_sets(int *, int *, int);
int  included_set(int *, int *, int);
int  in_set(int *, int);
int  *list_set(int *, int);

#define ZN        (Node *)0
#define ZS        (Symbol *)0
#define Nhash        255
#define True        tl_nn(LTL_TRUE,  ZN, ZN)
#define False        tl_nn(LTL_FALSE, ZN, ZN)
#define Not(a)        push_negation(tl_nn(NOT, a, ZN))
#define rewrite(n)        canonical(right_linked(n))

typedef Node        *Nodeptr;
#define YYSTYPE         Nodeptr

#define Debug(x)        { if (0) printf(x); }
#define Debug2(x,y)        { if (tl_verbose) printf(x,y); }
#define Dump(x)                { if (0) dump(x); }
#define Explain(x)        { if (tl_verbose) tl_explain(x); }

#define Assert(x, y)        { if (!(x)) { tl_explain(y); \
                          Fatal(": assertion failed\n",(char *)0); } }
#ifndef ltl2bamin
#define ltl2bamin(x,y)        ((x<y)?x:y)
#endif

extern char        uform[];
extern int        hasuform;
extern BState *bstates;
extern FILE * tl_out;
extern char ** sym_table;
extern int sym_id;
// MSVC cannot have a variable named accept, so we use lt_accept.
extern int lt_accept;
