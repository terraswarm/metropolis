/**********************
	loc.flex
        Tokenizer

 **********************/

%option yylineno

%{
/* 
@Version: $Id: loc.flex,v 1.5 2004/09/13 17:00:11 cxh Exp $

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
*/

#include <iostream>
#include <fstream>
#include <string>
#include <stdlib.h>
#include "term_type.h"
#include "parser.tab.h"

// flex 2.5.31 seems to require defining YY_SKIP_YYWRAP, or else we get
// lex.yy.c:643:29: macro "yywrap" passed 1 arguments, but takes just 0
#define YY_SKIP_YYWRAP
#define YY_NO_UNPUT
#define yywrap() 1

bool yyerrorOccurred = false;

extern YYSTYPE yylval;
extern ostream * outFile;
extern string orig_loc;
extern string trace_format;
extern vector<annot_type> annot_list;

namespace Parser {
  /* defined in parser.y */
}
 
%}

%x LOC
%x TRACE
%x ANNOT

DIGIT   [0-9]
ID      [a-zA-Z_][a-zA-Z0-9_]*
FLOAT   ([0-9]+\.[0-9]*)|([0-9]*\.[0-9]+)

%%
loc\:  { BEGIN LOC;    }
trace\: {BEGIN TRACE;  }
annotation\: {BEGIN ANNOT;}

<LOC>"<="     { yylval.str = "<="; orig_loc += yytext; return  BINARY_RELATION;       }
<LOC>">="     { yylval.str = ">="; orig_loc += yytext; return  BINARY_RELATION;       }
<LOC>"=="     { yylval.str = "=="; orig_loc += yytext; return  BINARY_RELATION;       }
<LOC>"<"      { yylval.str = "<"; orig_loc += yytext; return  BINARY_RELATION;       }
<LOC>">"      { yylval.str = ">"; orig_loc += yytext; return  BINARY_RELATION;       }
<LOC>"+"      { yylval.c = '+'; orig_loc += yytext; return '+';  }
<LOC>"-"      { yylval.c = '-'; orig_loc += yytext; return '-';  }
<LOC>"*"      { yylval.c = '*'; orig_loc += yytext; return '*';  }
<LOC>"/"      { yylval.c = '/'; orig_loc += yytext; return '/';  }
<LOC>"("      { orig_loc += yytext; return LEFT_PAREN;    }
<LOC>")"      { orig_loc += yytext; return RIGHT_PAREN;   }
<LOC>"["      { orig_loc += yytext; return LEFT_BRACKET;  }
<LOC>"]"      { orig_loc += yytext; return RIGHT_BRACKET; }
<LOC>"&&"     { orig_loc += yytext; return AND_OP; 	  }
<LOC>"||"     { orig_loc += yytext; return OR_OP;         }
<LOC>"!"      { orig_loc += yytext; return NOT_OP;        }
<LOC>"i"      { orig_loc += yytext; return VARIABLE;      }
<LOC>{FLOAT}  { yylval.str = strdup(yytext); orig_loc += yytext; return FLOAT_CONST;} 
<LOC>{DIGIT}+ { yylval.i = ::atoi(yytext); orig_loc += yytext; return INT_CONST; }
<LOC>{ID}     { yylval.str = strdup(yytext); orig_loc += yytext; return ID_TKN; }
<LOC>[ \t]+   { /*eat up blank spaces */  } 
<LOC>[ \n\r]+ { /*eat up new lines */     } 
<LOC>trace\:  { BEGIN TRACE; }
<LOC>annotation\: {BEGIN ANNOT;}
<LOC>.        { std::cerr << "Error: Unknown char '" << yytext 
	                  << "' at " << yylineno << std::endl; }

<TRACE>loc\:  	     {BEGIN LOC;    	}
<TRACE>annotation\:  {BEGIN ANNOT;	}
<TRACE>\"[^"]*       {
       			if (yytext[yyleng-1] == '\\')
                            yymore();
                        else
			{
			    yyinput();
                            trace_format = yytext + 1;
			}
		     }
<TRACE>[ \t]+   { /*eat up blank spaces */  } 
<TRACE>[ \n\r]+ { /*eat up new lines */     } 
<TRACE>.        { std::cerr << "Error: Unknown char '" << yytext 
	                  << "' at " << yylineno << std::endl; }

<ANNOT>loc\:  	     {BEGIN LOC;    	}
<ANNOT>trace\:       {BEGIN TRACE;  	}
<ANNOT>{ID}	     {annot_type annot;	annot.annot_name = yytext; annot_list.push_back(annot);}
<ANNOT>[ \t]+   { /*eat up blank spaces */  } 
<ANNOT>[ \n\r]+ { /*eat up new lines */     } 
<ANNOT>.        { std::cerr << "Error: Unknown char '" << yytext 
	                  << "' at " << yylineno << std::endl; }
%%
