/********************
     parser.y
 ********************/

%{
/*
@Version: $Id: parser.y,v 1.13 2005/11/18 01:31:06 allenh Exp $

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
#include <cstring>
#include <string>
#include <map>
#include <vector>
#include <list>
#include <stdio.h>
#include "term_type.h"

void yyerror(char* _mesg);
int yylex();

namespace Parser 
{
  void ProcessLOC(char * loc);
  int  ProcessEvent(char * annot, char * eventName, term_type * index);
  void GenerateCode();
  void CheckAnnot();
  string Indent(int level);
  
  /** \brief  Converts a number from 'int' to 'string' type.
   *  \param number  number to convert.
   */
  string ToString(int n);
}

using namespace std;
using namespace Parser;
extern 	char* yytext;
extern 	unsigned int yylineno;

/****************** global variables *****************/
ostream * outFile;
string final_loc;
string orig_loc;
string trace_format;
vector<annot_type> annot_list;
vector<event_type> event_list;
vector<string> term_list;
%}

%left OR_OP
%left AND_OP
%left NOT_OP
%left BINARY_RELATION
%left '+' '-'
%left '*' '/'
%left NEG
%token LEFT_PAREN RIGHT_PAREN
%token LEFT_BRACKET RIGHT_BRACKET
%token ID_TKN
%token VARIABLE
%token INT_CONST
%token FLOAT_CONST

%union
{
  int    i;
  float  f;
  char   c;
  char*  str;
  term_type * t;
}

%%
loc:	term BINARY_RELATION term {
             $<str>$ = strdup(($<t>1->expr + $<str>2 + $<t>3->expr).c_str()); 
	     Parser::ProcessLOC($<str>$);
	     delete $<t>1;
	     delete $<t>3;
          }
	| term BINARY_RELATION float_term {
	     $<str>$ = strdup(($<t>1->expr + $<str>2 + $<str>3).c_str()); 
	     Parser::ProcessLOC($<str>$);
	     delete $<t>1;
	     delete $<str>3;
	  }
        | float_term BINARY_RELATION term {
	     string tmp($<str>1); 
	     $<str>$ = strdup((tmp + $<str>2 + $<t>3->expr).c_str()); 
	     Parser::ProcessLOC($<str>$);
	     delete $<str>1;
	     delete $<t>3;
	  }
        | float_term BINARY_RELATION float_term {
	     string tmp($<str>1); 
	     $<str>$ = strdup((tmp + $<str>2 + $<str>3).c_str()); 
	     Parser::ProcessLOC($<str>$);
	     delete $<str>1;
	     delete $<str>3;
	  }
        | loc AND_OP loc {
	     string tmp = $<str>1;
	     tmp += "&&";
	     tmp += $<str>3; 
	     $<str>$ = strdup(tmp.c_str()); 
	     Parser::ProcessLOC($<str>$);
	     delete $<str>1;
	     delete $<str>3;
	  }
	| loc OR_OP loc {
	     string tmp = $<str>1;
	     tmp += "||";
	     tmp += $<str>3; 
	     $<str>$ = strdup(tmp.c_str()); 
	     Parser::ProcessLOC($<str>$);
             delete $<str>1;
	     delete $<str>3;}
	| NOT_OP loc {
	     string tmp = "!";
	     tmp += $<str>2; 
	     $<str>$ = strdup(tmp.c_str());
	     Parser::ProcessLOC($<str>$);
	     delete $<str>2;
	  }
	| LEFT_PAREN loc RIGHT_PAREN {
	     string tmp = "(";
	     tmp += $<str>2;
	     tmp += ")";
	     $<str>$ = strdup(tmp.c_str());
	     Parser::ProcessLOC($<str>$);
	     delete $<str>2;
	  }
;

term:  	VARIABLE {
	    $<t>$ = new term_type; 
	    $<t>$->type = 0; 
	    $<t>$->a = 1; 
	    $<t>$->b = 0; 
	    $<t>$->expr = "glbl_index";
	    $<t>$->orig_expr = "i";
	    //cout<< $<t>$->expr<<endl;
          }		
	| INT_CONST {
            $<t>$ = new term_type; 
	    $<t>$->type = 0; 
	    $<t>$->a = 0; 
	    $<t>$->b = $<i>1; 
	    $<t>$->expr = ToString($<i>1);
	    $<t>$->orig_expr = ToString($<i>1);
	  }
        | '-' term  %prec NEG {
	    string tmp("-");
	    if ($<t>2->type == 0) 
	    {
	      $<t>2->a = -$<t>2->a;  
	      $<t>2->b = -$<t>2->b; 
	      if ($<t>2->a == 0)	
		$<t>2->expr = ToString($<t>2->b);
	      else {
		if ($<t>2->a == 1)
		  $<t>2->expr = "glbl_index";
		else if ($<t>2->a == -1)
		  $<t>2->expr = "-glbl_index";
		else
		  $<t>2->expr = ToString($<t>2->a) + "*glbl_index";
		
		if ($<t>2->b > 0) {
		  $<t>2->expr += '+';
		  $<t>2->expr += ToString($<t>2->b);
		}
	      }
	    }
	    else
	      $<t>2->expr = tmp+$<t>2->expr;

	    $<t>2->orig_expr = tmp + $<t>2->orig_expr;
	    $<t>$ = $<t>2;
	    //cout<< $<t>$->expr<<endl;
	  }
        | term '+' term {
            if ($<t>1->type == 0 && $<t>3->type == 0) {
               $<t>1->a += $<t>3->a;
               $<t>1->b += $<t>3->b;
               if ($<t>1->a == 0)	
                  $<t>1->expr = ToString($<t>1->b);
	       else {
		  if ($<t>1->a == 1)
		    $<t>1->expr = "glbl_index";
		  else if ($<t>1->a == -1)
		    $<t>1->expr = "-glbl_index";
		  else
		    $<t>1->expr = ToString($<t>1->a) + "*glbl_index";

                  if ($<t>1->b > 0) {
                     $<t>1->expr += '+';
                     $<t>1->expr += ToString($<t>1->b);
                  }
		  else if ($<t>1->b < 0) {
                     $<t>1->expr += ToString($<t>1->b);
                  }
               }  
	    }
            else {
		    $<t>1->expr += $<c>2;
		    $<t>1->expr += $<t>3->expr;
	    }

	    $<t>1->orig_expr += $<c>2;
	    $<t>1->orig_expr += $<t>3->orig_expr;
	    $<t>$ = $<t>1;
	    delete $<t>3;
	    //cout<< $<t>$->expr<<endl;
    	  }
        | term '-' term {
            if ($<t>1->type == 0 && $<t>3->type == 0) {
               $<t>1->a -= $<t>3->a;
               $<t>1->b -= $<t>3->b;
	       if ($<t>1->a == 0)	
                  $<t>1->expr = ToString($<t>1->b);
	       else {
		  if ($<t>1->a == 1)
		    $<t>1->expr = "glbl_index";
		  else if ($<t>1->a == -1)
		    $<t>1->expr = "-glbl_index";
		  else
		    $<t>1->expr = ToString($<t>1->a) + "*glbl_index";

                  if ($<t>1->b > 0) {
                     $<t>1->expr += '+';
                     $<t>1->expr += ToString($<t>1->b);
                  }
		  else if ($<t>1->b < 0) {
                     $<t>1->expr += ToString($<t>1->b);
                  }
               }  
            }  
	    else {
	       $<t>1->expr += $<c>2;
	       $<t>1->expr += $<t>3->expr;
	    }

	    $<t>1->orig_expr += $<c>2;
	    $<t>1->orig_expr += $<t>3->orig_expr;
	    $<t>$ = $<t>1;
	    delete $<t>3;
	    //cout<< $<t>$->expr<<endl;
          }  
        | term '*' term { 
              if ($<t>1->type == 0 && $<t>3->type == 0) {
                 int product;
         	 product = $<t>1->a * $<t>3->a;
                 if (product == 0) {
                    $<t>1->a = $<t>1->a * $<t>3->b + $<t>1->b * $<t>3->a;
                    $<t>1->b = $<t>1->b * $<t>3->b;
		    if ($<t>1->a == 0)	
		       $<t>1->expr = ToString($<t>1->b);
		    else {
		       if ($<t>1->a == 1)
			 $<t>1->expr = "glbl_index";
		       else if ($<t>1->a == -1)
			 $<t>1->expr = "-glbl_index";
		       else
			 $<t>1->expr = ToString($<t>1->a) + "*glbl_index";

		       if ($<t>1->b > 0) {
			 $<t>1->expr += '+';
			 $<t>1->expr += ToString($<t>1->b);
		       }
		       else if ($<t>1->b < 0) {
			 $<t>1->expr += ToString($<t>1->b);
		       }
		    }  
                 }
                 else {
                    $<t>1->type = 1;
                    $<t>1->expr = $<t>1->expr + '*' + $<t>3->expr;
                 }
              }
	      else {
		 $<t>1->expr += $<c>2;
                 $<t>1->expr += $<t>3->expr;
	      }

	      $<t>1->orig_expr += $<c>2;
	      $<t>1->orig_expr += $<t>3->orig_expr;
	      $<t>$ = $<t>1;
	      delete $<t>3;
	      //cout<< $<t>$->expr<<endl;
    	  } 		 
        | term '/' term {         	
              $<t>1->type = 1;
	      $<t>1->expr += '/';
	      $<t>1->expr += $<t>3->expr;

	      $<t>1->orig_expr += '/';
	      $<t>1->orig_expr += $<t>3->orig_expr;

	      $<t>$ = $<t>1;
	      delete $<t>3;
	      //cout<< $<t>$->expr<<endl;
    	  }
        | LEFT_PAREN term RIGHT_PAREN {
	      string tmp = "(";
	      $<t>2->expr = tmp + $<t>2->expr + ')';
	      $<t>2->orig_expr = tmp + $<t>2->orig_expr + ')';
	      $<t>$ = $<t>2;
	      //cout<< $<t>$->expr<<endl;
      	  }
	| ID_TKN LEFT_PAREN ID_TKN LEFT_BRACKET term RIGHT_BRACKET RIGHT_PAREN{
	      int event_index =  Parser::ProcessEvent($<str>1, $<str>3, $<t>5);
	      $<t>$ = new term_type;
	      $<t>$->type = 2;
	      $<t>$->event = $<str>3;
	      $<t>$->annot = $<str>1;
	      $<t>$->expr = "((event_";
	      $<t>$->expr += $<str>3;
	      $<t>$->expr += "*)events[";
	      $<t>$->expr += ToString(event_index) + "])->getAnnot_" + $<str>1 + "(" + $<t>5->expr + ")";

	      $<t>$->orig_expr = $<str>1;
	      $<t>$->orig_expr += "(";
	      $<t>$->orig_expr += $<str>3;
	      $<t>$->orig_expr += "[";
	      $<t>$->orig_expr += $<t>5->orig_expr + "])"; 
	      term_list.push_back($<t>$->orig_expr);
	      term_list.push_back($<t>$->expr);
	      delete $<str>1;
	      delete $<str>3;
	      delete $<t>5;
	      //cout<< $<t>$->expr<<endl;
	  }
; 

float_term: FLOAT_CONST  {$<str>$ = strdup($<str>1); delete $<str>1;}
| '-' float_term  %prec NEG {
  string tmp("-");
  tmp += $<str>2;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>2;
  }
| LEFT_PAREN float_term RIGHT_PAREN{
  string tmp("(");
  tmp += $<str>2;
  tmp += ")";
  $<str>$ = strdup(tmp.c_str());
  delete $<str>2;
  }	    
| float_term '+' float_term {
  string tmp($<str>1);
  tmp += "+";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<str>3;
  }
| float_term '-' float_term {
  string tmp($<str>1);
  tmp += "-";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<str>3;
  }
| float_term '*' float_term {
  string tmp($<str>1);
  tmp += "*";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<str>3;
  }
| float_term '/' float_term {
  string tmp($<str>1);
  tmp += "/";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<str>3;
  }
| float_term '+' term {
  string tmp($<str>1);
  tmp += "+";
  tmp += $<t>3->expr;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<t>3;
  }
| float_term '-' term {
  string tmp($<str>1);
  tmp += "-";
  tmp += $<t>3->expr;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<t>3;
  }
| float_term '*' term {
  string tmp($<str>1);
  tmp += "*";
  tmp += $<t>3->expr;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<t>3;
  }
| float_term '/' term {
  string tmp($<str>1);
  tmp += "/";
  tmp += $<t>3->expr;
  $<str>$ = strdup(tmp.c_str());
  delete $<str>1;
  delete $<t>3;
  }
| term '+' float_term {
  string tmp($<t>1->expr);
  tmp += "+";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<t>1;
  delete $<str>3;
  }
| term '-' float_term {
  string tmp($<t>1->expr);
  tmp += "-";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<t>1;
  delete $<str>3;
  }
| term '*' float_term {
  string tmp($<t>1->expr);
  tmp += "*";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<t>1;
  delete $<str>3;
  }
| term '/' float_term {
  string tmp($<t>1->expr);
  tmp += "/";
  tmp += $<str>3;
  $<str>$ = strdup(tmp.c_str());
  delete $<t>1;
  delete $<str>3;
  }
;
                      	
%%

extern FILE *yyin;

void yyerror(char* _mesg)
{
  cout << "Error at line " << yylineno << " before " 
	    << yytext << " : " << _mesg << std::endl;
}

namespace Parser 
{
  void ProcessLOC(char * loc)
  {
    final_loc = loc;
  }

  int ProcessEvent(char *  annot, char * eventName, term_type * index)
  {
    unsigned event_index;
    unsigned annot_index;
    unsigned i;

    for (event_index = 0; event_index < event_list.size(); event_index++)
    {
      if (event_list[event_index].event_name == eventName)
	break;
    }
    
    if (event_index == event_list.size())
    {
      event_type newEvent;
      newEvent.event_name = eventName;
      event_list.push_back(newEvent);
    }

    event_type & thisEvent = event_list[event_index];

    for (annot_index = 0; annot_index < thisEvent.annot_list.size(); annot_index++)
    {
      if (thisEvent.annot_list[annot_index].annot_name == annot)
	break;
    }

    if (annot_index == thisEvent.annot_list.size())
    {
      annot_type newAnnot;
      newAnnot.annot_name = annot;
      thisEvent.annot_list.push_back(newAnnot);
    }
    
    annot_type & thisAnnot = thisEvent.annot_list[annot_index];

    if (index->type == 0)
    {
      for (i = 0; i<thisAnnot.term_list.size(); i++)
      {
	if (thisAnnot.term_list[i].type == 0 && thisAnnot.term_list[i].a == index -> a && thisAnnot.term_list[i].b == index -> b)
	  break;
      }

      if (i == thisAnnot.term_list.size())
	thisAnnot.term_list.push_back(*index);
    }
    else
      	thisAnnot.term_list.push_back(*index);
    
    return event_index;
  }
  
  void GenerateCode()
  {
    unsigned i, j;
    string comment(60, '*');
    string code1, code2, code3;
    char * data_type[] = {"int", "float", "unsigned"};

/**** debug output ****
    for (i = 0; i<event_list.size(); i++)
      event_list[i].print();
    cout<<trace_format<<endl;
    for (i = 0; i<annot_list.size(); i++)
      annot_list[i].print();
    cout<<"************************************************"<<endl;
 **********************/
    
    cout << "Parsing LOC formula and generating code .";
    CheckAnnot();
    cout << "..";
/**** debug output ****    
    for (i = 0; i<event_list.size(); i++)
      event_list[i].print();
    cout<<trace_format<<endl;
    for (i = 0; i<annot_list.size(); i++)
      annot_list[i].print();
 **********************/
    
    code1 = "#include <stdio.h>\n";
    code1 += "#include <stdlib.h>\n";
    code1 += "#include <time.h>\n";
    code1 += "#include <string>\n";
    code1 += "#include <iostream>\n";
    code1 += "#include <deque.h>\n\n";
    code1 += "typedef long long locgen_index_t;\n\n";
    code1 += "typedef unsigned long long size_type;\n\n";
    code1 += "/**** For exception handling ***/\n";
    code1 += "class OutOfRangeException\n";
    code1 += "{\npublic:\n";
    code1 += Indent(1) + "OutOfRangeException(int t):type(t){};\n";
    code1 += Indent(1) + "int whatType(){return type;};\n\n";
    code1 += "private:\n";
    code1 += Indent(1) + "int type;\n};\n\n";
    code1 += "/**** Base event class from which all events are derived ***/\n";
    code1 += "class event\n";
    code1 += "{\npublic:\n";
    code1 += Indent(1) + "char * name;\t\t\t/* event name */\n";
    code1 += Indent(1) + "locgen_index_t index;\t\t/* local index of annotations of the event */\n\n";
    code1 += Indent(1) + "event(char * name):index(0){this->name = strdup(name);};\n";
    code1 += Indent(1) + "virtual ~event(){if (name) free(name);};\n";
    code1 += Indent(1) + "virtual void removeRedundant(locgen_index_t) = 0;\n";
    code1 += Indent(1) + "virtual size_type size() = 0;\n};\n\n";
    cout << "..";

    for (i = 0; i < event_list.size();i++)
    {
      code2 += "/*** event '";
      code2 += event_list[i].event_name + "' ***/\n"; 
      code2 += "class event_";
      code2 += event_list[i].event_name + " : public event\n";
      code2 += "{\n";
      code2 += "private:\n";
      for (j = 0; j < event_list[i].annot_list.size(); j++)
      {	
	code2 += Indent(1) + "deque<";
	code2 += data_type[event_list[i].annot_list[j].data_type];
	code2 += ">  annot_";
	code2 += event_list[i].annot_list[j].annot_name + ";\t\t/* data list for annotation '";
	code2 += event_list[i].annot_list[j].annot_name + "' */\n";
      }
      
      code2 += "\npublic:\n";
      for (j = 0; j < event_list[i].annot_list.size(); j++)
      {	
	code2 += Indent(1);
	code2 += data_type[event_list[i].annot_list[j].data_type];
	code2 += "  getAnnot_";
	code2 += event_list[i].annot_list[j].annot_name;
	code2 += "(locgen_index_t);\n";
      }
      cout << "..";

      code2 += Indent(1) + "void addAnnot(";
      for (j = 0; j < event_list[i].annot_list.size() - 1; j++)
      {	
	code2 += data_type[event_list[i].annot_list[j].data_type];
	code2 += ",";
      }
      code2 += data_type[event_list[i].annot_list[j].data_type];
      code2 += ");\t\t/* store a set of annotations to the annotation lists */\n\n";
      code2 += Indent(1) + "event_" + event_list[i].event_name + "(char * name):event(name){};\n";
      code2 += Indent(1) + "~event_" + event_list[i].event_name + "();\n";
      code2 += Indent(1) + "void removeRedundant(locgen_index_t);\t/* memory space recycling */\n";
      code2 += Indent(1) + "size_type size();\t\t\t\t/* return the current memory usage */\n";
      code2 += "};\n\n";
      
      code2 += "/*** recycle memory space used for annotation lists ***/\n";
      code2 += "event_" + event_list[i].event_name + ":: ~event_" + event_list[i].event_name + "()\n";
      code2 += "{\n";
      for (j = 0; j < event_list[i].annot_list.size(); j++)
	code2 += Indent(1) + "annot_" +  event_list[i].annot_list[j].annot_name + ".clear();\n";
      code2 += "}\n\n";
      
      for (j = 0; j < event_list[i].annot_list.size(); j++)
      {
	code2 += "/*** return an annotation '";
	code2 += event_list[i].annot_list[j].annot_name + "' with global index i ***/\n";
	code2 += data_type[event_list[i].annot_list[j].data_type];
	code2 += " event_";
	code2 += event_list[i].event_name + "::getAnnot_" + event_list[i].annot_list[j].annot_name + "(locgen_index_t i)\n";
	code2 += "{\n";
	code2 += Indent(1) + "if (i >= index)\n";
	code2 += Indent(2) + "throw OutOfRangeException(1);\t\t\t/* unavailable index */\n";
	code2 += Indent(1) + "if (i < index - annot_" + event_list[i].annot_list[j].annot_name + ".size())\n";
	code2 += Indent(2) + "throw OutOfRangeException(0);\t\t\t/* invalid index */\n\n";
	code2 += Indent(1) + "return annot_" +  event_list[i].annot_list[j].annot_name + "[i-(index - annot_";
	code2 += event_list[i].annot_list[j].annot_name + ".size())];\n";
	code2 += "}\n\n";
      }
      cout << "..";

      code2 += "/*** store a set of annotations and increment the local index by 1 ***/\n";
      code2 += "void event_"; 
      code2 += event_list[i].event_name + "::addAnnot(";
      for (j = 0; j < event_list[i].annot_list.size() - 1; j++)
      {
	code2 += data_type[event_list[i].annot_list[j].data_type];
	code2 += " ";
	code2 += event_list[i].annot_list[j].annot_name + ",";
      }

      code2 += data_type[event_list[i].annot_list[j].data_type];
      code2 += " ";
      code2 += event_list[i].annot_list[j].annot_name + ")\n";
      code2 += "{\n";
      for (j = 0; j < event_list[i].annot_list.size(); j++)
      {
	code2 += Indent(1) + "annot_";
	code2 += event_list[i].annot_list[j].annot_name + ".push_back(";
	code2 += event_list[i].annot_list[j].annot_name + ");\n";
      }
      code2 += Indent(1) + "index++;\n}\n\n";
      code2 += "/*** annotations expected useless any more are removed ***/\n";
      code2 += "void event_"; 
      code2 += event_list[i].event_name + "::removeRedundant(locgen_index_t glbl_i)\n";
      code2 += "{\n";
      for (j = 0; j < event_list[i].annot_list.size(); j++)
      {
	if (event_list[i].annot_list[j].type() == 0)
	{
	  code2 += Indent(1) + "while (";
	  for (unsigned k = 0; k < event_list[i].annot_list[j].term_list.size(); k++)
	  {
	    term_type & thisTerm =  event_list[i].annot_list[j].term_list[k];
	    code2 += "(";
	    code2 += "index - annot_";
	    code2 += event_list[i].annot_list[j].annot_name + ".size() < ";
	    code2 += ToString(thisTerm.a) + "*glbl_i";
	    if (thisTerm.b < 0)
	      code2 += ToString(thisTerm.b);
	    else if (thisTerm.b > 0)
	    {
	      code2 += "+";
	      code2 += ToString(thisTerm.b);
	    }
	    code2 += ")";
	    if (k < event_list[i].annot_list[j].term_list.size()-1)
	      code2 += "&&";
	  }
	  code2 += ")\n";
	  code2 += Indent(2) +  "annot_" + event_list[i].annot_list[j].annot_name + ".pop_front();\n";
	}
	cout << ".";
      }
      code2 += "}\n\n";
       
      code2 += "/*** return the current memory usage ***/\n";
      code2 += "size_type event_" + event_list[i].event_name + "::size()\n";
      code2 += "{\n";
      code2 += Indent(1) + "return ";
      for (j = 0; j < event_list[i].annot_list.size()-1; j++)
      {
	code2 += "sizeof(";
	code2 += data_type[event_list[i].annot_list[j].data_type];
	code2 += ")*annot_";
	code2 += event_list[i].annot_list[j].annot_name + ".size() + ";
      }
      code2 += "sizeof(";
      code2 += data_type[event_list[i].annot_list[j].data_type];
      code2 += ")*annot_";
      code2 += event_list[i].annot_list[j].annot_name + ".size();\n";
      code2 += "}\n\n";
      cout << ".";
    }

    code3 = "event * events[";
    code3 += ToString(event_list.size()) + "];\t\t/* ";
    code3 += ToString(event_list.size()) + " events are mentioned in LOC formla*/\n";
    code3 += "locgen_index_t errors;\t\t\t/* total number of errors found */\n";
    code3 += "locgen_index_t glbl_index;\t\t/* global index i */\n";
    code3 += "time_t begin_time;\t\t/* the beginning time of running */\n";
    code3 += "time_t current_time;\t\t/* the current time of running */\n";
    code3 += "time_t minutes;\t\t\t/* the number of minutes elapsed */\n\n";

    code3 += "/*** for report use ***/\n";
    code3 += "#define LMAX 300\n";
    code3 += "locgen_index_t line;\n";
    code3 += "int current_file;\n";
    code3 += "char output[LMAX];\n";
    code3 += "size_type maxSpace;\n\n";
    code3 += "int whichEvent(char *);\t\t/* return the sequence number for a particular event */\n";
    code3 += "void check();\t\t\t/* check the actual loc formula */\n";
    code3 += "void recycle();\t\t\t/* memory space recycling */\n\n";

    code3 += "int whichEvent(char * eventname)\n";
    code3 += "{\n";
    
    for (i = 0; i<event_list.size();i++)
    {  
      code3 += Indent(1) + "if (strcmp(events[" + ToString(i) + "]->name, eventname) == 0)\n";
      code3 += Indent(2) + "return " + ToString(i) + ";\n\n";
    }
    
    code3 += Indent(1) + "return -1;\n}\n\n";
    code3 += "int main(int argc, char * argv[])\n{\n";
    code3 += Indent(1) + "if (argc < 2)\n";
    code3 += Indent(1) + "{\n";
    code3 += Indent(2) + "cerr << \"err: no trace file specified\" << endl; \n";
    code3 += Indent(2) + "cerr << \"usage: command file_name [file_name ...]\" << endl << endl;\n";
    code3 += Indent(2) + "exit(0);\n";
    code3 += Indent(1) + "}\n\n";
    
    code3 += Indent(1) + "/* initialization and opening trace files */\n";
    code3 += Indent(1) + "int j, k;\n";
    code3 += Indent(1) + "int total_files = argc - 1;\n";
    code3 += Indent(1) + "FILE * fptr = NULL;\n\n";

    //code3 += Indent(1) + "if (argc > 2)\n";
    //code3 += Indent(2) + "total_files = atoi(argv[1]);\n";
    //code3 += Indent(1) + "else\n";
    //code3 += Indent(2) + "total_files = 1;\n\n";

    //code3 += Indent(1) + "if (total_files > argc - 2 && argc > 2)\n";
    //code3 += Indent(1) + "{\n";
    //code3 += Indent(2) + "cerr << \"err: inconsistent command line arguments\" <<endl;\n";
    //code3 += Indent(2) + "cerr << \"usage: command [total_files] file_name [file_name ...]\" << endl << endl;\n";
    //code3 += Indent(2) + "exit(0);\n";
    //code3 += Indent(1) + "}\n\n";
   
    code3 += Indent(1) + "int * current_line = new int[total_files];\n";
    code3 += Indent(1) + "for (j = 0; j < total_files; j++)\n";
    code3 += Indent(2) + "current_line[j] = 0;\n\n";

    code3 += Indent(1) + "FILE ** fileptr = new FILE *[total_files];\n"; 
    //code3 += Indent(1) + "if (argc > 2)\n";
    //code3 += Indent(1) + "{\n";
    code3 += Indent(1) + "for (j = 0; j<total_files; j++)\n";
    code3 += Indent(1) + "{\n";
    code3 += Indent(2) + "if ((fileptr[j] = fopen(argv[j+1], \"r\")) == NULL )\n";
    code3 += Indent(2) + "{\n";  
    code3 += Indent(3) + "cerr << \"err: fail to open file \\\"\" << argv[j+1] << \"\\\"\" << endl << endl;\n";
    code3 += Indent(3) + "exit(1);\n";
    code3 += Indent(2) + "}\n";
    code3 += Indent(1) + "}\n\n"; 
    code3 += Indent(1) + "cout << \"\\n=========================================\\n            LOC Checker v0.1\\n\";\n";
    code3 += Indent(1) + "cout << \"   University of California, Riverside\\n=========================================\\n\\n\";\n\n";
    code3 += Indent(1) + "cout << \"Reading from trace file(s): \";\n";
    code3 += Indent(1) + "for (j = 0; j<total_files;j++)\n";
    code3 += Indent(2) + "cout <<\"#\" << j+1 << \"-\\\"\" << argv[j+1] << \"\\\"\";\n";
    code3 += Indent(1) + "cout << endl << endl;\n\n";
    //code3 += Indent(1) + "}\n";

    //code3 += Indent(1) + "else\n";
    //code3 += Indent(1) + "{\n";
    //code3 += Indent(2) + "if ((fileptr[0] = fopen(argv[1], \"r\")) == NULL )\n";
    //code3 += Indent(2) + "{\n";  
    //code3 += Indent(3) + "cerr << \"err: fail to open file \\\"\" << argv[1] << \"\\\"\" << endl << endl;\n";
    //code3 += Indent(3) + "exit(1);\n"; 
    //code3 += Indent(2) + "}\n\n";
    //code3 += Indent(2) + "cout << \"\\n=========================================\\n            LOC Checker v1.1\\n\";\n";
    //code3 += Indent(2) + "cout << \"   University of California, Riverside\\n=========================================\\n\\n\";\n\n";
    //code3 += Indent(2) + "cout << \"Reading from trace file #1-\\\"\" << argv[1] << \"\\\"\" << endl << endl;\n";
    //code3 += Indent(1) + "}\n\n";

    code3 += Indent(1) + "current_file = total_files - 1;\n";

    code3 += Indent(1) + "glbl_index = 0;\n";
    //code3 += Indent(1) + "line = 0;\n";

    for (i = 0; i<event_list.size();i++)
      code3 += Indent(1) + "events[" + ToString(i) + "] = new event_" + event_list[i].event_name + "(\"" + event_list[i].event_name + "\");\n";
    
    //code3 += Indent(1) + "printf(\"Reading from trace file \\\"%s\\\"...\\n\\n\", argv[1]);\n\n";
    //code3 += Indent(1) + "FILE * fptr = fopen(argv[1], \"r\");\n";
    code3 += "\n";
    code3 += Indent(1) + "bool needCheck;\n";
    code3 += Indent(1) + "size_type space;\n";
    code3 += Indent(1) + "char in_event[100];\n";
    for (i = 0; i<annot_list.size(); i++)
    {
      if (annot_list[i].annot_name != "event")
      {
	switch(annot_list[i].data_type)
	{
	case 0:
	  code3 += Indent(1) + "int\tin_" + annot_list[i].annot_name + ";\n";
	  break;
	case 1:
	  code3 += Indent(1) + "float\tin_" + annot_list[i].annot_name + ";\n";
	  break;
	case 2:
	  code3 += Indent(1) + "unsigned\tin_" + annot_list[i].annot_name + ";\n";
	  break;
	case 3:
	  code3 += Indent(1) + "char\tin_" + annot_list[i].annot_name + "[100];\n";
	  break;
	}
      }
    }
    cout << "..";

    code3 += "\n";
    code3 += Indent(1) + "begin_time = time(NULL);\n\n";
    code3 += Indent(1) + "while (1)\n";
    code3 += Indent(1) + "{\n";
    code3 += Indent(2) + "/* read in one line of a trace file each time */\n";
    code3 += Indent(2) + "needCheck = 1;\n\n";
    //code3 += Indent(2) + "line ++;\n";
    
    code3 += Indent(2) + "/* switch to next file to scan using round-robin */\n";
    code3 += Indent(2) + "for (k = 0; k < total_files; k++)\n";
    code3 += Indent(2) + "{\n";
    code3 += Indent(3) + "current_file = (current_file + 1)%total_files;\n";
    code3 += Indent(3) + "fptr = fileptr[current_file];\n";
    code3 += Indent(3) + "if (feof(fptr) == 0)\n";
    code3 += Indent(3) + "{\n";
    code3 += Indent(4) + "current_line[current_file] ++;\n";
    code3 += Indent(4) + "line = current_line[current_file];\n";
    code3 += Indent(4) + "break;\n";
    code3 += Indent(3) + "}\n";
    code3 += Indent(2) + "}\n";

    //cerr << "current_file:\t" << current_file << "\tcurrent_line:\t" << current_line[current_file] << "\tk:\t"<< k << endl;

    code3 += Indent(2) + "if (k == total_files)\n";
    code3 += Indent(3) + "break;\n\n";

    code3 += Indent(2) + "/* read in one line from the current file */\n";
    code3 += Indent(2) + "for (k = 0; k < LMAX && feof(fptr) == 0; k++) \n";
    code3 += Indent(3) + "if ((output[k] = fgetc(fptr)) == '\\n')\n";
    code3 += Indent(4) + "break;\n\n";
      
    code3 += Indent(2) + "if (k == LMAX || output[k-1] == EOF)\n";
    code3 += Indent(3) + "output[k-1] = '\\0';\n";
    code3 += Indent(2) + "else\n";
    code3 += Indent(3) + "output[k] = '\\0';\n\n";

    code3 += Indent(2) + "if (output[0] == '#')\n";
    code3 += Indent(3) + "continue;\n\n";

    //code3 += Indent(2) + "if (fscanf(fptr, \"" + trace_format + "\"";
    code3 += Indent(2) + "if (sscanf(output, \"" + trace_format + "\"";
    for (i = 0; i<annot_list.size(); i++)
    {
      if (annot_list[i].data_type != 3)
	code3 += ", &in_";
      else
	code3 += ", in_";

      code3 += annot_list[i].annot_name; 
    }
    code3 += ") < ";
    code3 += ToString(i) + ")\n";
    code3 += Indent(3) + "continue;\n\n";
    
    //code3 += Indent(2) + "sprintf(output, \"" + trace_format + "\"";
    //for (i = 0; i<annot_list.size(); i++)
    //{
    //  code3 += ", in_";
    //  code3 += annot_list[i].annot_name; 
    //}
    //code3 += ");\n\n";
    code3 += Indent(2) + "/* recognize which event it is and store its annotations */\n";
    code3 += Indent(2) + "switch(whichEvent(in_event))\n";
    code3 += Indent(2) + "{\n";
    
    for (i = 0; i<event_list.size();i++)
    {  
      code3 += Indent(2) + "case " + ToString(i) + ":\n";
      code3 += Indent(3) + "((event_" + event_list[i].event_name + "*)events[" + ToString(i) + "])->addAnnot(";
      for (j = 0; j<event_list[i].annot_list.size()-1;j++)
      {
	code3 += "in_";
	code3 += event_list[i].annot_list[j].annot_name + ",";
      }
      code3 += "in_";
      code3 += event_list[i].annot_list[j].annot_name + ");\n";
      code3 += Indent(3) + "break;\n";
    }

    code3 += Indent(2) + "default:\n";
    code3 += Indent(3) + "needCheck = 0;\n";
    code3 += Indent(3) + "break;\n";
    code3 += Indent(2) + "}\n\n";
    code3 += Indent(2) + "space = ";
    
    for (i = 0; i<event_list.size()-1;i++)
      code3 += "events[" + ToString(i) + "]->size() + ";
    code3 += "events[" + ToString(i) + "]->size();\n";
    code3 += Indent(2) + "if (maxSpace < space)\n";
    code3 += Indent(3) + "maxSpace = space;\n";
   
    code3 += Indent(2) + "if (needCheck)\n";
    code3 += Indent(3) + "check();\n\n";
    code3 += Indent(2) + "current_time = time(NULL);\n";
    code3 += Indent(2) + "if (current_time == begin_time + (minutes+1)*60)\n";
    code3 += Indent(2) + "{\n";
    code3 += Indent(3) + "minutes += 1;\n";
    code3 += Indent(3) + "cout << minutes << \" minute(s) elapsed ... \\n\";\n";
    code3 += Indent(2) + "}\n";
    code3 += Indent(1) + "}\n\n";
    code3 += Indent(1) + "current_time = time(NULL);\n\n";
    code3 += Indent(1) + "if (glbl_index > 1)\n";
    code3 += Indent(2) + "cout << \"The LOC formula '";
    code3 += orig_loc + "' with index i from 0 to \" << glbl_index - 1 << \" is evaluated.\\n\";\n";
    code3 += Indent(1) + "else if (glbl_index == 1)\n";
    code3 += Indent(2) + "cout << \"The LOC formula '";
    code3 += orig_loc + "' with index i of 0 is evaluated.\\n\";\n";
    code3 += Indent(1) + "else\n";
    code3 += Indent(2) + "cout << \"However, no LOC formula index is valid to be evaluated.\\n\";\n";
    code3 += Indent(1) + "if (errors> 0)\n";
    code3 += Indent(2) + "cout<<\"Totally \"<< errors <<\" error(s) found.\\n\\n\";\n";
    code3 += Indent(1) + "else\n";
    code3 += Indent(2) + "cout<< \"No error!\\n\\n\";\n\n";
    code3 += Indent(1) + "cout << \"Maximum memory usage: \" << maxSpace << \" Bytes (Approximately)\\n\";\n";
    code3 += Indent(1) + "cout << \"Time elapsed: \" << (current_time-begin_time)/3600 << \":\"<< (current_time-begin_time)/60<<\":\"<<(current_time-begin_time)%60 << \" -- \"<< current_time-begin_time <<\" second(s)\\n\\n\";\n";
    
    code3 += "\n"; 
    for (i = 0; i<event_list.size();i++)
      code3 += Indent(1) + "delete events[" + ToString(i) + "];\n"; 
    
    code3 += Indent(1) + "return 0;\n"; 
    code3 += "}\n\n"; 

    code3 += "/* after reading in a line of trace file, try to *\n";
    code3 += " * evaluate the LOC formula with the next index  */\n";
    code3 += "void check()\n";
    code3 += "{\n";
    code3 += Indent(1) + "try\n";
    code3 += Indent(1) + "{\n";  
    code3 += Indent(2) + "if (!(";
    code3 += final_loc + "))\n";
    code3 += Indent(2) + "{\n";  
    code3 += Indent(3) + "cout << \"Constraint violated at trace file# \" << current_file+1 << \" line# \" <<  line << \": \" <<  output << endl<<endl;\n";
    code3 += Indent(3) + "cout << \"Formula '" + orig_loc + "' is not satisfied at i = \" << glbl_index <<\", where\"<<endl;\n";
    
    for (i = 0; i < term_list.size(); i+=2)
    {
      code3 += Indent(3) + "cout << \"-- ";
      code3 += term_list[i] + "\" << \" = \" << " + term_list[i+1] + "<<endl;\n";
    }
    code3 += Indent(3) + "cout << endl;\n";
    //code3 += Indent(3) + "cout << \"Maximum memory usage: \" << maxSpace << \" Bytes (Approximately)\\n\\n\";\n";
    code3 += Indent(3) + "errors ++;\n";
    code3 += Indent(2) + "}\n";
    //code3 += Indent(2) + "else\n";
    //code3 += Indent(2) + "{\n";
    code3 += Indent(2) + "glbl_index ++;\n";
    code3 += Indent(2) + "recycle();\n";
    //code3 += Indent(2) + "}\n";
    code3 += Indent(1) + "}\n";
    code3 += Indent(1) + "catch (OutOfRangeException e)\n";
    code3 += Indent(1) + "{\n";
    code3 += Indent(2) + "if (e.whatType() == 0)\n";
    code3 += Indent(2) + "{\n";
    code3 += Indent(3) + "glbl_index ++;\n";
    code3 += Indent(3) + "recycle();\n";
    code3 += Indent(2) + "}\n";
    code3 += Indent(1) + "}\n";
    code3 += "}\n\n";

    code3 += "void recycle()\n";
    code3 += "{\n";
    for (i = 0; i<event_list.size();i++) 
      code3 += Indent(1) + "events[" + ToString(i) + "]->removeRedundant(glbl_index);\n";
    code3 += "}\n";
    cout << ".. done!\n\n";

    (*outFile)<<"/"<<comment<<endl;
    (*outFile)<<" LOC Trace Checker Generation v0.1\n\n";

    (*outFile)<<" @Copyright (c) 2004 The Regents of the University of California.\n";
    (*outFile)<<" All rights reserved.\n\n";
 
    (*outFile)<<" Permission is hereby granted, without written agreement and without\n";
    (*outFile)<<" license or royalty fees, to use, copy, modify, and distribute this\n";
    (*outFile)<<" software and its documentation for any purpose, provided that the\n";
    (*outFile)<<" above copyright notice and the following two paragraphs appear in all\n";
    (*outFile)<<" copies of this software and that appropriate acknowledgments are made\n";
    (*outFile)<<" to the research of the Metropolis group.\n";
    (*outFile)<<"\n";
    (*outFile)<<" IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY\n";
    (*outFile)<<" FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES\n";
    (*outFile)<<" ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF\n";
    (*outFile)<<" THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF\n";
    (*outFile)<<" SUCH DAMAGE.\n\n";

    (*outFile)<<" THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,\n";
    (*outFile)<<" INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF\n";
    (*outFile)<<" MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE\n";
    (*outFile)<<" PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF\n";
    (*outFile)<<" CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,\n";
    (*outFile)<<" ENHANCEMENTS, OR MODIFICATIONS.\n\n";

    (*outFile)<<" Checked LOC Formula:\n";
    (*outFile)<<"\t"<<orig_loc<<"\n\n";
    (*outFile)<<" "<<comment<<"/\n\n";
    (*outFile)<<code1+code2+code3;
  }

  void CheckAnnot()
  {
    /*parse the trace format defined in input file*/
    unsigned int pos = 0;
    for (unsigned i = 0; i<annot_list.size(); i++)
    { 
      while (1)
      {
	pos = trace_format.find("%",pos);
	pos ++;
	
	if (trace_format[pos]=='s' || trace_format[pos]=='f' || trace_format[pos]=='d' || trace_format[pos]=='u' || trace_format[pos]=='x')
	  break;
	else if (trace_format[pos] != '%')
	{
	  cout<<"Error: unknown data type %"<< trace_format[pos] <<" in trace format.\nAbort!\n";
	  exit(0);
	}
      }
      
      if (annot_list[i].annot_name == "event") 
      {	
	if (trace_format[pos] != 's')
	{
	  cout<<"Error: trace format. The data type associated with 'event' has to be %s.\nAbort!\n";
	  exit(0);
	}
        else
	  annot_list[i].data_type = 3;
      }
      else
      {
	switch(trace_format[pos])
	{
	case 'd':
	  annot_list[i].data_type = 0;
	  break;
	case 'f':
	  annot_list[i].data_type = 1;
	  break;
        case 'u':
	  annot_list[i].data_type = 2;
	  break;
	case 's':
	  annot_list[i].data_type = 3;
	  break;
        case 'x':
	  annot_list[i].data_type = 2;
	  break;
        default:
	  cout<<"Error: trace format. The data type has to be %d, %x, %f, %u or %s.\nAbort!\n";
	  exit(0);
	}
      }
    }
    
    /*check the data type of annotations*/
    unsigned i, j, k;
    for (i = 0; i < event_list.size(); i++)
    {
      for (j = 0; j < event_list[i].annot_list.size(); j++)
      {
	annot_type & thisAnnot = event_list[i].annot_list[j];
	
        for (k = 0; k< annot_list.size();k++)
	  if (annot_list[k].annot_name == thisAnnot.annot_name)
	    break;
	
        if (k == annot_list.size())
	{
	  cout<<"Error: annotation '"<<thisAnnot.annot_name<<"' is not defined.\nAbort!\n";
	  exit(0);
	}

	if (annot_list[k].data_type == 3)
	{
	  cout<<"Error: the data type of an annotation cannot be %s.\nAbort!\n";
	  exit(0);
	}
	
	thisAnnot.data_type = annot_list[k].data_type;

	if (thisAnnot.type() == 2)
	{
	  unsigned m,n;
	  for (m = 0; m < thisAnnot.term_list.size();m++)
	  {
	    if (thisAnnot.term_list[m].type == 2)
	    {
	      for (n = 0; n < annot_list.size();n ++)
		if (annot_list[n].annot_name == thisAnnot.term_list[m].annot)
		  break;
	      if (n == annot_list.size())
	      {
		cout<<"Error: index annotation '"<<thisAnnot.term_list[m].annot<<"' is not defined.\nAbort!\n";
		exit(0);
	      }
	      else if (annot_list[n].data_type == 3 || annot_list[n].data_type == 1 )
	      {
		cout<<"Error: the data type of index annotation has to be int(%d) or unsigned int(%u).\nAbort!\n";
		exit(0);
	      }
	    }
	  }  
	}


      }
    }
  }

  string ToString(int n) 
  {
    char *buf = new char[20];
    sprintf(buf, "%d", n);
    std::string return_value(buf);
    delete buf;

    return return_value;
  }

  string Indent(int level)
  {
    if (level <0)
      level = 0;
    string tmp;
    for (int i = 0; i< level; i++)
      tmp += "   ";
    return tmp;
  }
}

#define USAGE "Usage: locgen [[[-d] input_file] output file]\n"

int main(int argc, char *argv[]) 
{
  if (argc == 1 || argc > 3) {
     fprintf(stderr, USAGE);
     return 4;
  }

  int argCount = 1;
  if (argc >= 1 && strcmp(argv[1], "-d") == 0) {
#if YYDEBUG
     yydebug = 1;
#else
     fprintf(stderr, "Warning -d flag ignored, recompile locgen with\n \"make CXX_USERFLAGS=-DYYDEBUG\"\n");
#endif 
     argCount = 2;
  }

  if (argc <= argCount) {
     fprintf(stderr, "Missing input file.\n%s", USAGE);
     return 5;
  }

  yyin = fopen(argv[argCount], "r");

  if (yyin ==  NULL) {
     fprintf(stderr, "Failed to open \"%s\"\n", argv[argCount]);
     perror("Failed to open.");
  }

  if (argc > argCount + 1) {
    outFile = new ofstream(argv[argCount + 1]);
    if (outFile == NULL) {
     fprintf(stderr, "Failed to open \"%s\"\n", argv[argCount]);
    }
  } else {
    outFile = &cout;
  }

  yyparse();
  Parser::GenerateCode();
  outFile->flush();
  return 0;
}
