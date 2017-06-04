/*
YACC grammar for the meta-model. 

This file was based on the solution written by Paul N. Hilfinger for a
class project for CS164, with changes from Jeff Tsay to support Java 1.1
features.

Copyright (c) 1998-2004 The Regents of the University of California.
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

Meta-Model Parser $Id: Grammar.y,v 1.63 2006/02/20 22:18:23 guyang Exp $ */

/* KEYWORDS */

%token ABSTRACT ADDCOMPONENT ALL AT AWAIT
%token BEG BLACKBOX BLOCK BOOLEAN BOUNDEDLOOP BREAK BYTE
%token CASE CHAR CLASS CONNECT CONSTANT CONSTRAINT CONTINUE
%token DEFAULT DO DOUBLE
%token ELABORATE ELOC ELSE END EVAL EVENT EXCL EXECINDEX EXISTS EXTENDS
%token F FINAL FLOAT FOR FORALL
%token G GETCOMPNAME GETCONNECTIONNUM GETNTHCONNECTIONSRC
%token GETNTHCONNECTIONPORT GETCOMPONENT GETCONNECTIONDEST GETINSTNAME
%token GETNTHPORT GETPORTNUM GETPROCESS GETSCOPE GETTHREAD GETTYPE
%token IF IMPLEMENTS IMPORT INSTANCEOF INT INTERFACE ISCONNECTIONREFINED
%token LABEL LAST LFO LOC LONG LTL
%token MAXDELTA MINDELTA MAXRATE MINRATE PERIOD MEDIUM MUTEX
%token NETLIST NEW NONDETERMINISM NONE _NULL

%token OTHER
%token PACKAGE PARAMETER PC PCVAL PORT PRIORITY PRIVATE PROCESS PROTECTED PUBLIC
%token QUANTITY
%token REDIRECTCONNECT REFINE REFINECONNECT RETVAL RETURN
%token SCHEDULER SETSCOPE SHORT SIMUL STATEMEDIUM STATIC SUPER SWITCH SYNCH
%token TEMPLATE THIS
%token U UPDATE USEPORT
%token VOID
%token WHILE
%token X

/* KEYWORDS RESERVED (ILLEGAL AS IDENTIFIERS) BUT NOT USED */

%token CATCH CONST
%token FINALLY
%token GOTO
%token NATIVE
%token STRICTFP SYNCHRONIZED
%token THROW THROWS TRANSIENT TRY
%token VOLATILE

/* IDENTIFIERS, BLACKBOXES AND LITERALS */

%token TRUE FALSE

%token<sval> IDENTIFIER
%token<sval> BLACKBOX_CONTENT

%token<sval> INT_LITERAL LONG_LITERAL
%token<sval> FLOAT_LITERAL DOUBLE_LITERAL
%token<sval> CHARACTER_LITERAL
%token<sval> STRING_LITERAL

%token<sval> BLACKBOX_CONTENT

         /* SEPARATORS */

%token '(' ')' '{' '}' '[' ']' ',' '.' ';'

%token BEGIN_TEMPLATE END_TEMPLATE
%token BEGIN_LABEL END_LABEL
%token BEGIN_ANNOTATION END_ANNOTATION

/* The following represents '[' ']' with arbitrary intervening whitespace. */
/* It is reasonably cheap to have the lexer recognize this, and it helps */
/* resolve at least one awkward LALR(1) lookahead problem. */
%token EMPTY_DIM

         /* OPERATORS */

%token '=' '>' '<'  '!' '~' '?' ':'
%token '+' '-' '*' '/' '&' '|' '^' '%'

%token IMPLY  /* =>: expr1 imply expr2*/
%token CAND  /* &&: conditional and */
%token COR  /* ||: conditional or */
%token CIF /* ->: conditional if */
%token CIFF /* <->: conditional if and only if */

%token EQ    /* == */
%token NE    /* != */
%token LE    /* <= */
%token GE    /* >= */

%token LSHIFTL  /* << */
%token ASHIFTR  /* >> */
%token LSHIFTR  /* >>> */

%token PLUS_ASG  /* += */
%token MINUS_ASG  /* -= */
%token MULT_ASG  /* *= */
%token DIV_ASG  /* /= */
%token REM_ASG  /* %= */
%token LSHIFTL_ASG  /* <<= */
%token ASHIFTR_ASG  /* >>= */
%token LSHIFTR_ASG  /* >>>= */
%token AND_ASG  /* &= */
%token XOR_ASG  /* ^= */
%token OR_ASG  /* |= */

%token PLUSPLUS  /* ++ */
%token MINUSMINUS  /* -- */


        /* PRECEDENCES */

/* LOWEST */

%right ELSE
%right FORALL EXISTS
%left '=' PLUS_ASG MINUS_ASG MULT_ASG DIV_ASG REM_ASG LSHIFTL_ASG LSHIFTR_ASG ASHIFTR_ASG AND_ASG OR_ASG XOR_ASG
%nonassoc U
%nonassoc CIFF
%nonassoc CIF
%nonassoc IMPLY
%right '?' ':'
%left COR
%left CAND
%left '|'
%left '^'
%left '&'
%left EQ NE
%left '<' '>' LE GE INSTANCEOF
%left LSHIFTL LSHIFTR ASHIFTR
%left '+' '-'
%left '*' '/' '%'
%nonassoc PLUSPLUS MINUSMINUS

/* HIGHEST */


/* Artificial precedence rules: The rule for '.' resolves conflicts
 * with QualifiedNames, FieldAccesses, and MethodAccesses.  The result
 * is that FieldAccesses and MethodAccesses that look syntactically
 * like QualifiedNames are parsed as QualifiedNames (see the
 * production for Name from QualifiedName).  The ambiguity must be
 * resolved with static semantic information at a later stage.  The
 * rule for ')' resolves conflicts between Casts and ComplexPrimaries.
 */

%right '.' ')'

/* Artificial precedence rule to resolve conflicts with
 * InterfaceModifiers and ClassModifiers */

/*
%right ABSTRACT FINAL PUBLIC
*/

%{
/*
Meta-model parser produced by BYACC/J, with input file Grammar.y

Copyright (c) 1998-2004 The Regents of the University of California.
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

package metropolis.metamodel.frontend.parser;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;
import java.io.IOException;
import java.io.FileInputStream;

import metropolis.metamodel.*;
import metropolis.metamodel.nodetypes.*;
import metropolis.metamodel.frontend.lexer.Lexer;

%}

%type<obj> Literal
%type<obj> SpecialLiteral
%type<obj> Blackbox

%type<obj> Type PrimitiveType ReferenceType
%type<obj> TypeName DerivedType ArrayType

%type<obj> Name SimpleName QualifiedName
%type<obj> NameOfTemplate SimpleNameOfTemplate QualifiedNameOfTemplate
%type<obj> TypeParameters

%type<obj> CompilationUnit
%type<obj> PackageDeclarationOpt ImportStatementsOpt TypeDeclarationsOpt
%type<obj> ImportStatement TypeImportStatement TypeImportOnDemandStatement
%type<obj> TypeDeclaration NonTemplateDeclaration TemplateDeclaration
%type<obj> ClassTemplateDeclaration InterfaceTemplateDeclaration
%type<obj> ClassDeclaration InterfaceDeclaration ProcessDeclaration
%type<obj> MediumDeclaration SchedulerDeclaration NetlistDeclaration
%type<obj> StateMediumDeclaration

%type<obj> TypeParameterDecl TypeParameterDeclList ValidTypeList

%type<ival> ModifiersOpt Modifiers Modifier Effect
%type<ival> DimsOpt Dims

%type<obj> FormalDeclarator FormalDeclarators
%type<obj> VariableDeclarator VariableDeclarators
%type<obj> VariableDeclaratorInit VariableInitializer

%type<obj> InnerClassDeclaration
%type<obj> FieldDeclaration ConstantFieldDeclaration
%type<obj> ParameterDeclaration PortDeclaration
%type<obj> ConstructorDeclaration MethodSignatureDeclaration
%type<obj> JavaMethodDeclaration InterfaceMethodDeclaration
%type<obj> NonInterfaceMethodDeclaration MethodBody
%type<obj> Void FormalParamListOpt FormalParamList FormalParameter
%type<obj> UsePortsOpt UsePorts PortNameList ExplicitConstructorCallStatement

%type<obj> SuperOpt InterfacesOpt ExtendsInterfacesOpt TypeNameList

%type<obj> InterfaceBody
%type<obj> InterfaceMemberDeclarationsOpt InterfaceMemberDeclaration

%type<obj> ClassBody
%type<obj> ClassMemberDeclarationsOpt ClassMemberDeclaration

%type<obj> ProcessBody
%type<obj> ProcessMemberDeclarationsOpt ProcessMemberDeclaration

%type<obj> MediumBody
%type<obj> MediumMemberDeclarationsOpt MediumMemberDeclaration

%type<obj> SchedulerBody
%type<obj> SchedulerMemberDeclarationsOpt SchedulerMemberDeclaration

%type<obj> StateMediumBody
%type<obj> StateMediumMemberDeclarationsOpt StateMediumMemberDeclaration

%type<obj> NetlistBody
%type<obj> NetlistMemberDeclarationsOpt NetlistMemberDeclaration

%type<obj> ArrayInitializer ElementInitializers Element

%type<obj> Block BlockStatementsOpt BlockStatements BlockStatement
%type<obj> LocalVariableDeclarationStatement
%type<obj> Statement EmptyStatement NetlistStatement
%type<obj> LabeledStatement ExpressionStatement SelectionStatement
%type<obj> IterationStatement JumpStatement ConstraintBlock AwaitStatement

%type<obj> SwitchBlock SwitchBlockStatementsOpt SwitchLabels SwitchLabel

%type<obj> IterationStatement ForInit ForUpdateOpt
%type<obj> ExpressionStatementsOpt ExpressionStatements

%type<obj> ConstraintBlockStatementsOpt ConstraintBlockStatements
%type<obj> ConstraintStatement
%type<obj> LTLFormula BuiltInLTLFormula
%type<obj> ActionFormula BuiltInLOCFormula

%type<obj> AwaitGuardsOpt AwaitGuard Guard
%type<obj> AwaitLockList AwaitLocks AwaitLock

%type<obj> LabelOpt Label LocalLabel GlobalLabel LabeledExpression LabeledExpressionFirst

%type<obj> Expression NormalExpression ExpressionOpt
%type<obj> UnaryExpression Assignment PrimaryExpression NotJustName
%type<obj> AllocationExpression ComplexPrimary ArrayAccess FieldAccess
%type<obj> MethodCall ArgumentListOpt ArgumentList PostfixExpression
%type<obj> PostIncrement PostDecrement PreIncrement PreDecrement
%type<obj> UnaryExpressionNotPlusMinus CastExpression
%type<obj> DimExprs DimExpr ConstantExpression
%type<obj> EqualVarsOpt EqualVars EqualVarsList
%type<obj> GetComponent GetConnectionDest GetNthConnectionSrc GetThread
%type<obj> Action BeginEvent EndEvent NoneEvent OtherEvent
%type<obj> ConstraintParamList ConstraintParameter
%type<obj> QuantityDeclaration QuantityBody
%type<obj> QuantityMemberDeclarationsOpt QuantityMemberDeclaration
%type<obj> VariableInEventReference 
/* %type<obj> SetInMethod2True VariableInEventReference  */

%start Start

%%


        /* GOAL */

Start :
    CompilationUnit
    { _theAST = (CompileUnitNode) $1; }
  ;


       /* MISCELLANEOUS */

empty :
      ;


        /* LITERALS */
  
Literal :
    INT_LITERAL
    { $$ = new IntLitNode($1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | LONG_LITERAL
    { $$ = new LongLitNode($1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | FLOAT_LITERAL
    { $$ = new FloatLitNode($1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | DOUBLE_LITERAL
    { $$ = new DoubleLitNode($1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | TRUE
    { $$ = new BoolLitNode("true");
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | FALSE
    { $$ = new BoolLitNode("false");
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | CHARACTER_LITERAL
    { $$ = new CharLitNode($1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | STRING_LITERAL
    { $$ = new StringLitNode($1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | SpecialLiteral
          { $$ = $1; }
  ;

SpecialLiteral :
    LAST
                { $$ = new SpecialLitNode("LAST");
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | RETVAL
          { $$ = new SpecialLitNode("retval");
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;
        /* BLACK BOXES */

Blackbox :
    BLACKBOX { pushCurrentLineNumber("Blackbox"); } '(' IDENTIFIER ')'
    BLACKBOX_CONTENT
    { $$ = new BlackboxNode($4,$6);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("Blackbox")); }
  ;

        /* TYPES */

Type :
    PrimitiveType
  | ReferenceType
  ;

ReferenceType :
    TypeName
  | DerivedType
  | ArrayType
  ;

/* Primitive types */

PrimitiveType :
    BOOLEAN
    { $$ = BoolTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | CHAR
    { $$ = CharTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | BYTE
    { $$ = ByteTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SHORT
    { $$ = ShortTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | INT
    { $$ = IntTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | FLOAT
    { $$ = FloatTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | LONG
    { $$ = LongTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | DOUBLE
    { $$ = DoubleTypeNode.instance;
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | EVENT
          { $$ = EventTypeNode.instance;
                ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  ;

/* Type name */

TypeName :
    NameOfTemplate      %prec ')'
    { $$ = new TypeNameNode((NameNode) $1);
        ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Derived types */

DerivedType :
    MEDIUM
    { NameNode name = new NameNode(AbsentTreeNode.instance,"metamodel",
                                   AbsentTreeNode.instance);
      name = new NameNode(name,"lang",AbsentTreeNode.instance);
      name = new NameNode(name,"Medium",AbsentTreeNode.instance);
      $$ = new TypeNameNode((NameNode) name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | NETLIST
    { NameNode name = new NameNode(AbsentTreeNode.instance,"metamodel",
                                   AbsentTreeNode.instance);
      name = new NameNode(name,"lang",AbsentTreeNode.instance);
      name = new NameNode(name,"Netlist",AbsentTreeNode.instance);
      $$ = new TypeNameNode((NameNode) name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | PROCESS
    { NameNode name = new NameNode(AbsentTreeNode.instance,"metamodel",
                                   AbsentTreeNode.instance);
      name = new NameNode(name,"lang",AbsentTreeNode.instance);
      name = new NameNode(name,"Process",AbsentTreeNode.instance);
      $$ = new TypeNameNode((NameNode) name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SCHEDULER
    { NameNode name = new NameNode(AbsentTreeNode.instance,"metamodel",
                                   AbsentTreeNode.instance);
      name = new NameNode(name,"lang",AbsentTreeNode.instance);
      name = new NameNode(name,"Scheduler",AbsentTreeNode.instance);
      $$ = new TypeNameNode((NameNode) name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | STATEMEDIUM
    { NameNode name = new NameNode(AbsentTreeNode.instance,"metamodel",
                                   AbsentTreeNode.instance);
      name = new NameNode(name,"lang",AbsentTreeNode.instance);
      name = new NameNode(name,"StateMedium",AbsentTreeNode.instance);
      $$ = new TypeNameNode((NameNode) name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | PCVAL
    { $$ = PCTypeNode.instance;
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  ;

/* Array type */

ArrayType :
    Type EMPTY_DIM
    { $$ = new ArrayTypeNode((TypeNode) $1);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  ;

        
        /* NAMES */

Name :
    SimpleName
    { $$ = $1; }
  | QualifiedName
    { $$ = $1; }
  ;

SimpleName :
    IDENTIFIER
    { $$ = new NameNode(AbsentTreeNode.instance, $1,
                        AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

QualifiedName :
    Name '.' IDENTIFIER
    { $$ = new NameNode((NameNode) $1, $3, AbsentTreeNode.instance);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  ;

NameOfTemplate :
    SimpleNameOfTemplate
    { $$ = $1; }
  | QualifiedNameOfTemplate
    { $$ = $1; }
  ;

SimpleNameOfTemplate :
    IDENTIFIER
    { $$ = new NameNode(AbsentTreeNode.instance,$1,AbsentTreeNode.instance);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  | IDENTIFIER BEGIN_TEMPLATE TypeParameters END_TEMPLATE
    { $$ = new NameNode(AbsentTreeNode.instance,$1,
                        new TemplateParametersNode((List) $3));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

QualifiedNameOfTemplate :
    NameOfTemplate '.' IDENTIFIER
    { $$ = new NameNode((NameNode) $1,$3,AbsentTreeNode.instance);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  | NameOfTemplate '.' IDENTIFIER BEGIN_TEMPLATE TypeParameters END_TEMPLATE
    { $$ = new NameNode((NameNode) $1,$3,
                        new TemplateParametersNode((List)$5));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

TypeParameters :
    Type
    { $$ = cons($1); }
  | Type ';' TypeParameters
    { $$ = cons($1,(List) $3); }
  ;

        /* PROGRAM STRUCTURE */

CompilationUnit :
    PackageDeclarationOpt ImportStatementsOpt TypeDeclarationsOpt
    { $$ = new CompileUnitNode((TreeNode) $1, (List) $2, (List) $3); }
  | error
    { yyerror("Confused by previous errors - halting."); }
  ;

/* Package declaration */

PackageDeclarationOpt :
    PACKAGE Name ';'
    { $$ = $2; }
  | PACKAGE Name '.' error ';'
    { yyerror("Incorrect package name in package declaration, after '"
              + qualifiedName((NameNode) $2) + "'."); }
  | PACKAGE Name error
    { yyerror("Missing semicolon (;) after package declaration."); }
  | PACKAGE ';'
    { yyerror("Missing package name in package declaration."); }
  | PACKAGE error ';'
    { yyerror("Incorrect package name in package declaration.");  }
  | empty
    { $$ = AbsentTreeNode.instance; }
  ;

/* Import statements */

ImportStatementsOpt :
    empty
    { $$ = new LinkedList(); }
  | ImportStatement ImportStatementsOpt
    { $$ = cons($1, (List) $2); }
  | ImportStatement PACKAGE error
    { yyerror("Package name must be declared before import statements."); }
  ;

ImportStatement :
    TypeImportStatement
  | TypeImportOnDemandStatement
  | IMPORT Name '.' error
    { yyerror("Incorrect type name in import statement, after '" +
              qualifiedName((NameNode)$2) + "'."); }
  | IMPORT error ';'
    { yyerror("Incorrect type name in import statement."); }
  | IMPORT ';'
    { yyerror("Missing type name in import statement."); }
  | IMPORT Name error
    { yyerror("Missing semicolon (;) after import statement."); }
  | IMPORT Name '.' '*' error
    { yyerror("Missing semicolon (;) after import statement."); }
  ;

TypeImportStatement :
    IMPORT Name ';'
    { $$ = new ImportNode((NameNode) $2); }
  ;

TypeImportOnDemandStatement :
    IMPORT Name '.' '*' ';'
    { $$ = new ImportOnDemandNode((NameNode) $2); }
  ;

/* User-defined type declarations */


TypeDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | TypeDeclaration TypeDeclarationsOpt
    { $$ = cons($1, (List) $2); }
  | ';' TypeDeclarationsOpt
    { $$ = $2; }
  ;

TypeDeclaration :
    Blackbox
    { $$ = $1; }
  | TemplateDeclaration
    { $$ = $1; }
  | NonTemplateDeclaration
    { $$ = $1; }
  ;

NonTemplateDeclaration :
    ClassDeclaration
    { $$ = $1; }
  | InterfaceDeclaration
    { $$ = $1; }
  | ProcessDeclaration
    { $$ = $1; }
  | MediumDeclaration
    { $$ = $1; }
  | SchedulerDeclaration
    { $$ = $1; }
  | StateMediumDeclaration
    { $$ = $1; }
  | NetlistDeclaration
    { $$ = $1; }
        | QuantityDeclaration
    { $$ = $1; }
  ;


      /* TEMPLATE DECLARATIONS */

TemplateDeclaration :
    TEMPLATE '(' TypeParameterDeclList ')' NonTemplateDeclaration
    { UserTypeDeclNode decl = (UserTypeDeclNode) $5;
      List params = (List) $3;
      decl.setParTypeNames(params);
      $$ = decl; }
  ;

ClassTemplateDeclaration :

    TEMPLATE '(' TypeParameterDeclList ')' ClassDeclaration
    { UserTypeDeclNode decl = (UserTypeDeclNode) $5;
      List params = (List) $3;
      decl.setParTypeNames(params);
      $$ = decl; }
  ;

InterfaceTemplateDeclaration:
    TEMPLATE '(' TypeParameterDeclList ')' InterfaceDeclaration
    { UserTypeDeclNode decl = (UserTypeDeclNode) $5;
      List params = (List) $3;
      decl.setParTypeNames(params);
      $$ = decl; }
  ;

TypeParameterDeclList :
    TypeParameterDecl
    { $$ = cons($1); }
  | TypeParameterDecl ';' TypeParameterDeclList
    { $$ = cons($1, (List) $3); }
  ;

TypeParameterDecl :
    SimpleName
    { // FIXME: Use valid type list
      $$ = $1; }
  | SimpleName ':' ValidTypeList
    { // FIXME: Use valid type list
      $$ = $1; }
  ;
     
ValidTypeList :
    Type
    { $$ = cons($1); }
  | Type ',' ValidTypeList
    { $$ = cons($1, (List) $3); }
  ;



      /* MODIFIERS AND EFFECTS */

/* Modifiers */

ModifiersOpt :
    Modifiers
    { $$ = $1; }
  | empty
    { $$ = Modifier.NO_MOD; }
  ;

Modifiers :
    Modifier
    { $$ = $1; }
  | Modifier Modifiers
    { $$ = $1 | $2; }
  ;

Modifier :
    PUBLIC
    { $$ = Modifier.PUBLIC_MOD; }
  | PROTECTED
    { $$ = Modifier.PROTECTED_MOD; }
  | PRIVATE
    { $$ = Modifier.PRIVATE_MOD; }
  | ABSTRACT
    { $$ = Modifier.ABSTRACT_MOD; }
  | FINAL
    { $$ = Modifier.FINAL_MOD; }
  | STATIC
    { $$ = Modifier.STATIC_MOD; }
  | ELABORATE
    { $$ = Modifier.ELABORATE_MOD; }
  ;

/* Effects on state */

Effect :
    CONSTANT
    { $$ = Effect.CONSTANT_EFFECT; }
  | EVAL
    { $$ = Effect.EVAL_EFFECT; }
  | UPDATE
    { $$ = Effect.UPDATE_EFFECT; }
  ;

      /* VARIABLE DECLARATORS */

FormalDeclarator :
    SimpleName DimsOpt
    { $$ = new DeclaratorNode($2, (NameNode) $1, AbsentTreeNode.instance);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  ;

FormalDeclarators :
    FormalDeclarator
    { $$ = cons($1); }
  | FormalDeclarator ',' FormalDeclarators
    { $$ = cons($1, (List) $3); }
  ;

VariableDeclaratorInit:
    SimpleName DimsOpt '=' VariableInitializer
    { $$ = new DeclaratorNode($2, (NameNode) $1, (ExprNode) $4);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  ;

VariableInitializer :
    Expression
    { $$ = $1; }
  | ArrayInitializer
    { $$ = $1; }
  ;

VariableDeclarator :
    FormalDeclarator
    { $$ = $1; }
  | VariableDeclaratorInit
    { $$ = $1; }
  ;

VariableDeclarators :
    VariableDeclarator
    { $$ = cons($1); }
  | VariableDeclarator ',' VariableDeclarators
    { $$ = cons($1,(List) $3); }
  ;


      /* MEMBER DECLARATIONS */

/* Inner class declarations */
InnerClassDeclaration :
    ClassDeclaration
    { $$ = $1; }
  | InterfaceDeclaration
    { $$ = $1; }
  | ClassTemplateDeclaration
    { $$ = $1; }
  | InterfaceTemplateDeclaration
    { $$ = $1; }
  ;

/* Field declarations */

FieldDeclaration :
    ModifiersOpt Type VariableDeclarators ';'
    { Modifier.checkFieldModifiers($1);
      List result = new LinkedList();
      List varDecls = (List) $3;
      Iterator itr = varDecls.iterator();
      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        int dims      = decl.getDims();
        NameNode name = decl.getName();
        TreeNode init = decl.getInitExpr();
        TypeNode type = TypeUtility.makeArrayType((TypeNode) $2, dims);
        FieldDeclNode node = new FieldDeclNode($1,type,name,init);
        node.setProperty(_LINENUMBER_KEY,
             decl.getProperty(_LINENUMBER_KEY));
        result = cons(node,result);
      }
      $$ = result;
      }
  ;

ConstantFieldDeclaration :
    ModifiersOpt Type VariableDeclarators ';'
    { Modifier.checkConstantFieldModifiers($1);
      List result = new LinkedList();
      List varDecls = (List) $3;
      Iterator itr = varDecls.iterator();
      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        int dims      = decl.getDims();
        NameNode name = decl.getName();
        TreeNode init = decl.getInitExpr();
        TypeNode type = TypeUtility.makeArrayType((TypeNode) $2, dims);
        FieldDeclNode node = new FieldDeclNode($1,type,name,init);
        node.setProperty(_LINENUMBER_KEY,
             decl.getProperty(_LINENUMBER_KEY));
        result = cons(node,result);
      }
      $$ = result;
        }
  ;

/* Method declarations */
Void :
    VOID
    { $$ = VoidTypeNode.instance; }
  ;

FormalParamListOpt :
     empty
    { $$ = new LinkedList(); }
  | FormalParamList
    { $$ = $1; }
  ;

FormalParamList :
    FormalParameter
    { $$ = cons($1); }
  | FormalParameter ',' FormalParamList
    { $$ = cons($1,(List) $3); }
  ;

FormalParameter :
    ModifiersOpt Type FormalDeclarator
    { Modifier.checkFormalModifiers($1);
      DeclaratorNode decl = (DeclaratorNode) $3;
      int dims      = decl.getDims();
      NameNode name = decl.getName();
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      $$ = new ParameterNode($1,type,name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

UsePortsOpt :
    UsePorts
    { $$ = $1; }
  | empty
    { $$ = new LinkedList(); }
  ;

UsePorts :
    USEPORT PortNameList
    { $$ = $2; }
  ;

PortNameList :
    SimpleName
    { $$ = cons($1); }
  | SimpleName ',' PortNameList
    { $$ = cons($1, (List) $3); }
  ;

MethodSignatureDeclaration :
    ModifiersOpt Effect Type IDENTIFIER '(' FormalParamListOpt ')' DimsOpt ';'
    { Modifier.checkMethodSignatureModifiers($1);
      int modifier  = $1;
      int effect    = $2;
      int dims      = $8;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $3,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$4,
                                   AbsentTreeNode.instance);
      List params   = (List) $6;
      TreeNode body = AbsentTreeNode.instance;
      List ports    = new LinkedList();
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ModifiersOpt Effect Void IDENTIFIER '(' FormalParamListOpt ')' DimsOpt ';'
    { Modifier.checkMethodSignatureModifiers($1);
      int modifier  = $1;
      int effect    = $2;
      int dims      = $8;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $3,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$4,
                                   AbsentTreeNode.instance);
      List params   = (List) $6;
      TreeNode body = AbsentTreeNode.instance;
      List ports    = new LinkedList();
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ModifiersOpt Type IDENTIFIER '(' FormalParamListOpt ')' DimsOpt ';'
    { Modifier.checkMethodSignatureModifiers($1);
      int modifier  = $1;
      int effect    = Effect.NO_EFFECT;
      int dims      = $7;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$3,
                                   AbsentTreeNode.instance);
      List params   = (List) $5;
      TreeNode body = AbsentTreeNode.instance;
      List ports    = new LinkedList();
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ModifiersOpt Void IDENTIFIER '(' FormalParamListOpt ')' DimsOpt ';'
    { Modifier.checkMethodSignatureModifiers($1);
      int modifier  = $1;
      int effect    = Effect.NO_EFFECT;
      int dims      = $7;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$3,
                                   AbsentTreeNode.instance);
      List params   = (List) $5;
      TreeNode body = AbsentTreeNode.instance;
      List ports    = new LinkedList();
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

JavaMethodDeclaration :
    ModifiersOpt Type IDENTIFIER '(' FormalParamListOpt ')' DimsOpt
    { pushCurrentLineNumber("JavaMethodDeclaration"); _inMethod = true; }
    MethodBody { _inMethod = false; }
         { Modifier.checkMethodModifiers($1);
      int modifier  = $1;
      int effect    = Effect.NO_EFFECT;
      int dims      = $7;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$3,
                                   AbsentTreeNode.instance);
      List params   = (List) $5;
      TreeNode body = (TreeNode) $9;
      List ports    = new LinkedList();
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("JavaMethodDeclaration")); }
  | ModifiersOpt Void IDENTIFIER '(' FormalParamListOpt ')' DimsOpt
    { pushCurrentLineNumber("JavaMethodDeclaration"); _inMethod = true; }
    MethodBody { _inMethod = false; }
          { Modifier.checkMethodModifiers($1);
      int modifier  = $1;
      int effect    = Effect.NO_EFFECT;
      int dims      = $7;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$3,
                                   AbsentTreeNode.instance);
      List params   = (List) $5;
      TreeNode body = (TreeNode) $9;
      List ports    = new LinkedList();
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("JavaMethodDeclaration")); }
  ;

InterfaceMethodDeclaration :
    ModifiersOpt Effect Type IDENTIFIER '(' FormalParamListOpt ')' DimsOpt
    UsePortsOpt
    {pushCurrentLineNumber("InterfaceMethodDeclaration"); _inMethod = true;}
    MethodBody {_inMethod = false;}
    { Modifier.checkInterfaceMethodModifiers($1);
      int modifier  = $1;
      int effect    = $2;
      int dims      = $8;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $3,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$4,
                                   AbsentTreeNode.instance);
      List params   = (List) $6;
      TreeNode body = (TreeNode) $11;
      List ports    = (List) $9;
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("InterfaceMethodDeclaration")); }
  | ModifiersOpt Effect Void IDENTIFIER '(' FormalParamListOpt ')' DimsOpt
    UsePortsOpt
    {pushCurrentLineNumber("InterfaceMethodDeclaration"); _inMethod = true;}
    MethodBody {_inMethod = false;}
    { Modifier.checkInterfaceMethodModifiers($1);
      int modifier  = $1;
      int effect    = $2;
      int dims      = $8;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $3,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$4,
                                   AbsentTreeNode.instance);
      List params   = (List) $6;
      TreeNode body = (TreeNode) $11;
      List ports    = (List) $9;
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("InterfaceMethodDeclaration")); }
  ;

NonInterfaceMethodDeclaration :
    ModifiersOpt Type IDENTIFIER '(' FormalParamListOpt ')' DimsOpt UsePortsOpt
    {pushCurrentLineNumber("NonInterfaceMethodDeclaration"); _inMethod = true;}
    MethodBody {_inMethod = false;}
    { Modifier.checkMethodModifiers($1);
      int modifier  = $1;
      int effect    = Effect.NO_EFFECT;
      int dims      = $7;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$3,
                                   AbsentTreeNode.instance);
      List params   = (List) $5;
      TreeNode body = (TreeNode) $10;
      List ports    = (List) $8;
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("NonInterfaceMethodDeclaration")); }
  | ModifiersOpt Void IDENTIFIER '(' FormalParamListOpt ')' DimsOpt UsePortsOpt
    {pushCurrentLineNumber("NonInterfaceMethodDeclaration"); _inMethod = true;}
    MethodBody {_inMethod = false;}
    { Modifier.checkMethodModifiers($1);
      int modifier  = $1;
      int effect    = Effect.NO_EFFECT;
      int dims      = $7;
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $2,dims);
      NameNode name = new NameNode(AbsentTreeNode.instance,$3,
                                   AbsentTreeNode.instance);
      List params   = (List) $5;
      TreeNode body = (TreeNode) $10;
      List ports    = (List) $8;
      $$ = new MethodDeclNode(modifier,name,params,body,type,effect,ports);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("NonInterfaceMethodDeclaration")); }
  ;

MethodBody :
                Block
                { $$ = $1; }
  | ';'
    { $$ = AbsentTreeNode.instance; }
  ;

/* Constructor declarations */

ConstructorDeclaration :
    ModifiersOpt IDENTIFIER '(' FormalParamListOpt ')'
    '{' PushConstructorLineNumberAndSetInMethod ExplicitConstructorCallStatement
                BlockStatementsOpt '}'
    { _inMethod = false;
                  Modifier.checkConstructorModifiers($1);
      int modifier  = $1;
      NameNode name = new NameNode(AbsentTreeNode.instance,$2,
                                   AbsentTreeNode.instance);
      List params   = (List) $4;
      if (!_inClass && _nestLevel==1) {
        if (params.size()==0) {
          yyerror("A name (String) must be provided to constructor "
                  + $2
                  + ", and it must be the first argument of the constructor.");
        }
        ParameterNode prmnode = (ParameterNode) params.get(0);
        if (!(prmnode.getDefType() instanceof TypeNameNode)) {
          yyerror("A name (String) must be provided to constructor "
                  + $2
                  + ", and it must be the first argument of the constructor.");
        }
        TypeNameNode tnnode = (TypeNameNode) prmnode.getDefType();
        if (!tnnode.getName().getIdent().equals("String")) {
          yyerror("A name (String) must be provided to constructor "
                  + $2
                  + ", and it must be the first argument of the constructor.");
        }
      }
      BlockNode block = new BlockNode((List) $9);
      ConstructorCallNode call = (ConstructorCallNode) $8;
      $$ = new ConstructorDeclNode(modifier,name,params,block,call);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstructorDeclaration")); }
  | ModifiersOpt IDENTIFIER '(' FormalParamListOpt ')'
    '{' PushConstructorLineNumberAndSetInMethod BlockStatementsOpt '}'
    {_inMethod = false;
                  Modifier.checkConstructorModifiers($1);
      int modifier  = $1;
      NameNode name = new NameNode(AbsentTreeNode.instance,$2,
                                   AbsentTreeNode.instance);
      List params   = (List) $4;
      if (!_inClass && _nestLevel==1) {
          if (params.size()==0)
              yyerror("A name (String) must be provided to constructor "
                  + $2
                  + ", and it must be the first argument of the constructor.");
          ParameterNode prmnode = (ParameterNode) params.get(0);
          if (!(prmnode.getDefType() instanceof TypeNameNode))
              yyerror("A name (String) must be provided to constructor "
                  + $2
                  + ", and it must be the first argument of the constructor.");
          TypeNameNode tnnode = (TypeNameNode) prmnode.getDefType();
          if (!tnnode.getName().getIdent().equals("String"))
              yyerror("A name (String) must be provided to constructor "
                  + $2
                  + ", and it must be the first argument of the constructor.");
      }
      BlockNode block = new BlockNode((List) $8);
      LinkedList par = new LinkedList();
      if (!_inClass && _nestLevel==1 && ((List)$4).size() > 0) {
          ParameterNode param = (ParameterNode) ((List)$4).get(0);
          try {
              par.add(new ObjectNode((NameNode)param.getName().clone()));
          } catch (CloneNotSupportedException ex) {
              throw new RuntimeException("Clone of '"
                      + (NameNode)param.getName()
                      + "' not supported.", ex);
          }
      }
      ConstructorCallNode call =
                        new SuperConstructorCallNode(par);
      $$ = new ConstructorDeclNode(modifier,name,params,block,call);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstructorDeclaration")); }
  ;

PushConstructorLineNumberAndSetInMethod :
    { pushCurrentLineNumber("ConstructorDeclaration");
      _inMethod = true; }

/****
SetInMethod2True :
    { _inLineNumber = _lexer.lineNumber();
      _inMethod = true; }
****/

ExplicitConstructorCallStatement :
    THIS '(' ArgumentListOpt ')' ';'
    { $$ = new ThisConstructorCallNode((List) $3);
        ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        new Integer(_lexer.lineNumber())); }
  | SUPER '(' ArgumentListOpt ')' ';'
          { $$ = new SuperConstructorCallNode((List) $3);
                ((TreeNode)$$).setProperty(
                                _LINENUMBER_KEY,
                                new Integer(_lexer.lineNumber())); }
  ;

/* Parameter declarations  */

ParameterDeclaration :
    PARAMETER ModifiersOpt Type FormalDeclarators ';'
    { Modifier.checkParameterModifiers($2);
      List result = new LinkedList();
      List varDecls = (List) $4;
      Iterator itr = varDecls.iterator();
      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        int dims      = decl.getDims();
        NameNode name = decl.getName();
        TypeNode type = TypeUtility.makeArrayType((TypeNode) $3, dims);
        ParameterDeclNode node = new ParameterDeclNode($2,type,name);
        result = cons(node,result);
      }
      $$ = result;
      }
  ;

/* Port declarations */

PortDeclaration :
    PORT ModifiersOpt Type FormalDeclarators ';'
    { Modifier.checkPortModifiers($2);
      List result = new LinkedList();
      List varDecls = (List) $4;
      Iterator itr = varDecls.iterator();
      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        int dims      = decl.getDims();
        NameNode name = decl.getName();
        TypeNode type = TypeUtility.makeArrayType((TypeNode) $3, dims);
        PortDeclNode node = new PortDeclNode($2,type,name);
        result = cons(node,result);
      }
      $$ = result;
      }
  ;


      /* SUPERCLASSES AND SUPERINTERFACES */

SuperOpt :
    EXTENDS TypeName
    { $$ = $2; }
  | empty
    { // This will be fixed later by class resolution
      $$ = AbsentTreeNode.instance; }
  ;

InterfacesOpt:
    IMPLEMENTS TypeNameList
    { $$ = $2; }
  | empty
    { $$ = new LinkedList(); }
  ;

ExtendsInterfacesOpt :
    EXTENDS TypeNameList
    { $$ = $2; }
  | empty
    { $$ = new LinkedList(); }
  ;

TypeNameList :
    TypeName
    { $$ = cons($1); }
  | TypeName ',' TypeNameList
    { $$ = cons($1, (List) $3); }
  ;


      /* INTERFACE DECLARATION */

InterfaceDeclaration :
    ModifiersOpt INTERFACE SimpleName ExtendsInterfacesOpt
    { pushCurrentLineNumber("InterfaceDeclaration"); _nestLevel++; }
                InterfaceBody
    { _nestLevel--;
                  Modifier.checkInterfaceModifiers($1);
      $$ = new InterfaceDeclNode($1,(NameNode) $3,(List) $4,(List) $6,
                                 new LinkedList());
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("InterfaceDeclaration")); }
  ;

InterfaceBody :
    '{' InterfaceMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

InterfaceMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | InterfaceMemberDeclaration InterfaceMemberDeclarationsOpt
    { $$ = appendLists((List) $1, (List) $2); }
  ;

InterfaceMemberDeclaration :
    ConstantFieldDeclaration
    { $$ = $1; }
  | MethodSignatureDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  ;


      /* QUANTITY DECLARATION */

QuantityDeclaration :
    ModifiersOpt QUANTITY SimpleName SuperOpt InterfacesOpt
                { pushCurrentLineNumber("QuantityDeclaration");
                        if (_nestLevel == 0) _inClass = true;
                  _nestLevel++; }
                QuantityBody
    { _nestLevel--;
                  if (_nestLevel == 0) _inClass = false;
                        Modifier.checkClassModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;
      TreeNode sup   = (TreeNode) $4;
      List ifnames   = (List) $5;
      List members   = (List) $7;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, !_inClass && _nestLevel==0);
      $$ = new QuantityDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("QuantityDeclaration")); }
  ;

QuantityBody :
    '{' QuantityMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

QuantityMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | QuantityMemberDeclaration QuantityMemberDeclarationsOpt
    { $$ = appendLists((List)$1,(List)$2); }
  ;

QuantityMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
        | ParameterDeclaration
          { $$ = $1; }
  | PortDeclaration
    { $$ = $1; }
  | JavaMethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InterfaceMethodDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;


      /* CLASS DECLARATION */

ClassDeclaration :
    ModifiersOpt CLASS SimpleName SuperOpt InterfacesOpt
                { pushCurrentLineNumber("ClassDeclaration");
                        if (_nestLevel == 0) _inClass = true;
                  _nestLevel++;
                }
                ClassBody
    { _nestLevel--;
                  if (_nestLevel == 0) _inClass = false;
                        Modifier.checkClassModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;
      TreeNode sup   = (TreeNode) $4;
      List ifnames   = (List) $5;
      List members   = (List) $7;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, false);
      $$ = new ClassDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ClassDeclaration")); }
  ;

ClassBody :
    '{' ClassMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

ClassMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | ClassMemberDeclaration ClassMemberDeclarationsOpt
    { $$ = appendLists((List)$1,(List)$2); }
  ;

ClassMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
  | JavaMethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;


      /* PROCESS DECLARATION */

ProcessDeclaration :
    ModifiersOpt PROCESS SimpleName SuperOpt
                { pushCurrentLineNumber("ProcessDeclaration"); _nestLevel++; }
                ProcessBody
    { _nestLevel--;
                  Modifier.checkProcessModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;

      TreeNode sup   = (TreeNode) $4;
      List ifnames   = new LinkedList();
      List members   = (List) $6;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, !_inClass && _nestLevel==0);
      $$ = new ProcessDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ProcessDeclaration")); }
  ;

ProcessBody :
    '{' ProcessMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

ProcessMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | ProcessMemberDeclaration ProcessMemberDeclarationsOpt
    { $$ = appendLists((List) $1,(List) $2); }
  ;

ProcessMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
  | ParameterDeclaration
    { $$ = $1; }
  | PortDeclaration
    { $$ = $1; }
  | InterfaceMethodDeclaration
    { $$ = cons($1); }
  | NonInterfaceMethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;


      /* MEDIUM DECLARATION */

MediumDeclaration :
    ModifiersOpt MEDIUM SimpleName SuperOpt InterfacesOpt
                { pushCurrentLineNumber("MediumDeclaration"); _nestLevel++; }
                MediumBody
    { _nestLevel--;
                  Modifier.checkMediumModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;
      TreeNode sup   = (TreeNode) $4;
      List ifnames   = (List) $5;
      List members   = (List) $7;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, !_inClass && _nestLevel==0);
      $$ = new MediumDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("MediumDeclaration")); }
  ;

MediumBody :
    '{' MediumMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

MediumMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList();  }
  | MediumMemberDeclaration MediumMemberDeclarationsOpt
    { $$ = appendLists((List) $1, (List) $2); }
  ;

MediumMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
  | ParameterDeclaration
    { $$ = $1; }
  | PortDeclaration
    { $$ = $1; }
  | InterfaceMethodDeclaration
    { $$ = cons($1); }
  | NonInterfaceMethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;


      /* SCHEDULER DECLARATION */

SchedulerDeclaration :
    ModifiersOpt SCHEDULER SimpleName SuperOpt InterfacesOpt
                { pushCurrentLineNumber("SchedulerDeclaration"); _nestLevel++; }
                SchedulerBody
    { _nestLevel--;
                  Modifier.checkSchedulerModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;
      TreeNode sup   = (TreeNode) $4;
      List ifnames   = (List) $5;
      List members   = (List) $7;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, !_inClass && _nestLevel==0);
      $$ = new SchedulerDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("SchedulerDeclaration")); }
  ;

SchedulerBody :
    '{' SchedulerMemberDeclarationsOpt '}'
     { $$ = $2; }
  ;

SchedulerMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList();  }
  | SchedulerMemberDeclaration SchedulerMemberDeclarationsOpt
    { $$ = appendLists((List) $1,(List) $2); }
  ;

SchedulerMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
  | ParameterDeclaration
    { $$ = $1; }
  | PortDeclaration
    { $$ = $1; }
  | NonInterfaceMethodDeclaration
    { $$ = cons($1); }
        | InterfaceMethodDeclaration
          { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;


      /* STATE MEDIUM DECLARATION */

StateMediumDeclaration :
    ModifiersOpt STATEMEDIUM SimpleName SuperOpt InterfacesOpt
    { pushCurrentLineNumber("StateMediumDeclaration"); _nestLevel++; }
    StateMediumBody
    { _nestLevel--;
      Modifier.checkSMModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;
      TreeNode sup   = (TreeNode) $4;
      List ifnames   = (List) $5;
      List members   = (List) $7;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, !_inClass && _nestLevel==0);
      $$ = new SMDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("StateMediumDeclaration")); }
  ;

StateMediumBody :
    '{' StateMediumMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

StateMediumMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | StateMediumMemberDeclaration StateMediumMemberDeclarationsOpt
    { $$ = appendLists((List) $1, (List) $2); }
  ;

StateMediumMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
  | PortDeclaration
    { $$ = $1; }
  | InterfaceMethodDeclaration
    { $$ = cons($1); }
  | NonInterfaceMethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;

      /* NETLIST DECLARATION */

NetlistDeclaration :
    ModifiersOpt NETLIST SimpleName SuperOpt InterfacesOpt
    { pushCurrentLineNumber("NetListDeclaration");
                        _inNetlist = true;
                  _nestLevel++; }
    NetlistBody
    { _nestLevel--;
                  _inNetlist = false;
      Modifier.checkNetlistModifiers($1);
      int modifier   = $1;
      NameNode name  = (NameNode) $3;
      TreeNode sup   = (TreeNode) $4;
      List ifnames   = (List) $5;
      List members   = (List) $7;
      List pars      = new LinkedList();
      forceOneConstructor(name, members, !_inClass && _nestLevel==0);
      $$ = new NetlistDeclNode(modifier,name,ifnames,members,pars,sup);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("NetListDeclaration")); }
  ;

NetlistBody :
    '{' NetlistMemberDeclarationsOpt '}'
    { $$ = $2; }
  ;

NetlistMemberDeclarationsOpt :
    empty
    { $$ = new LinkedList(); }
  | NetlistMemberDeclaration NetlistMemberDeclarationsOpt
    { $$ = appendLists((List) $1,(List) $2); }
  ;

NetlistMemberDeclaration :
    FieldDeclaration
    { $$ = $1; }
  | JavaMethodDeclaration
    { $$ = cons($1); }
  | ConstructorDeclaration
    { $$ = cons($1); }
  | InterfaceMethodDeclaration
    { $$ = cons($1); }
  | InnerClassDeclaration
    { $$ = cons($1); }
  | ConstraintBlock
    { $$ = cons($1); }
  | Blackbox
    { $$ = cons($1); }
  ;

      /* ARRAYS */

ArrayInitializer :
   '{' ElementInitializers '}'
    { $$ = new ArrayInitNode((List) $2);
        ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                   new Integer(_lexer.lineNumber())); }
  | '{' ElementInitializers ',' '}'
    { $$ = new ArrayInitNode((List) $2);
        ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                   new Integer(_lexer.lineNumber())); }
  | '{' '}'
    { $$ = new ArrayInitNode(new LinkedList());
        ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                   new Integer(_lexer.lineNumber())); }
  ;
/* Note: I'm going to assume that they didn't intend to allow "{,}". */

ElementInitializers :
    Element
    { $$ = cons($1); }
  | ElementInitializers ',' Element
    { $$ = append((List) $1, $3); }
  ;

Element :
    Expression
    { $$ = $1; }
  | ArrayInitializer
    { $$ = $1; }
  ;


      /* BLOCKS AND STATEMENTS */

/* Blocks */

Block :
    '{' BlockStatementsOpt '}' { pushBlockEndLineNumber("Block"); }
    { $$ = new BlockNode((List) $2);
      /*
       * ((TreeNode)$$).setProperty(
       *           _LINENUMBER_KEY,
       *           popCurrentLineNumber("Block"));
       */
      List statements = (List)$2;
      // The LINENUMBER of a block is that of its first non-empty statement,
      // or else null if it has no non-empty statement.
      Integer myLineNumber     = null;
      Integer secondLineNumber = null;
      if (statements != null && ! statements.isEmpty()) {
        for (int i = 0; i < statements.size(); i++) {
            TreeNode stmt = (TreeNode)statements.get(i);
            if (stmt instanceof EmptyStmtNode) {
                continue;
            }
            Object lineNumberObj = stmt.getProperty(_LINENUMBER_KEY);
            if (myLineNumber == null) {
                if (lineNumberObj instanceof Integer) {
                    myLineNumber = (Integer)lineNumberObj;
                }
            } else {
                // Block's line number is set.  Now set the
                // 2nd-statement-line number.
                if (lineNumberObj instanceof Integer
                    && ((Integer)lineNumberObj).intValue()
                        > myLineNumber.intValue()) {
                    secondLineNumber = (Integer)lineNumberObj;
                    break;
                }
            }
        }
      }
      ((TreeNode)$$).setProperty(_LINENUMBER_KEY, myLineNumber);
      ((TreeNode)$$).setProperty(_BLOCK_END_LINENUMBER_KEY,
                                 popBlockEndLineNumber("Block"));
      if (secondLineNumber != null) {
          ((TreeNode)$$).setProperty(_BLOCK_SECOND_LINENUMBER_KEY,
                                     secondLineNumber);
      }
    }
  ;

/***
SetLineNumber :
    { _inLineNumber = _lexer.lineNumber(); }
***/

BlockStatementsOpt :
    BlockStatements
    { $$ = $1; }
  | empty
    { $$ = new LinkedList(); }
  ;

BlockStatements :
    BlockStatement
    { $$ = $1; }
  | BlockStatements BlockStatement
    { $$ = appendLists((List) $1, (List) $2); }
  ;

BlockStatement :
    LocalVariableDeclarationStatement
    { $$ = $1; }
  | Statement
    { $$ = cons($1); }
  ;

/* Local variable declarations */

LocalVariableDeclarationStatement :
    Modifiers Type
    { pushCurrentLineNumber("LocalVariableDeclarationStatement"); }
    VariableDeclarators ';'
    { Modifier.checkLocalVarModifiers($1);
      List result = new LinkedList();
      List varDecls = (List) $4;
      Integer myLineNumber =
            popCurrentLineNumber("LocalVariableDeclarationStatement");
      Iterator itr = varDecls.iterator();
      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        int dims      = decl.getDims();
        NameNode name = decl.getName();
        TreeNode init = decl.getInitExpr();
        TypeNode type = TypeUtility.makeArrayType((TypeNode) $2, dims);
        LocalVarDeclNode node = new LocalVarDeclNode($1,type,name,init);
        node.setProperty(_LINENUMBER_KEY, myLineNumber);
        result = cons(node,result);
      }
      $$ = result; }
  | Type
    { pushCurrentLineNumber("LocalVariableDeclarationStatement"); }
    VariableDeclarators ';'
    { List result = new LinkedList();
      List varDecls = (List) $3;
      Integer myLineNumber =
            popCurrentLineNumber("LocalVariableDeclarationStatement");
      Iterator itr = varDecls.iterator();
      while (itr.hasNext()) {
        DeclaratorNode decl = (DeclaratorNode) itr.next();
        int modifier  = Modifier.NO_MOD;
        int dims      = decl.getDims();
        NameNode name = decl.getName();
        TreeNode init = decl.getInitExpr();
        TypeNode type = TypeUtility.makeArrayType((TypeNode) $1, dims);
        LocalVarDeclNode node = new LocalVarDeclNode(modifier,type,name,init);
        node.setProperty(_LINENUMBER_KEY, myLineNumber);
        result = cons(node,result);
          }
      $$ = result;
      }
  ;

/* Statements */

Statement :
    EmptyStatement
    { $$ = $1; }
  | LabeledStatement
    { $$ = $1; }
  | ExpressionStatement ';'
    { $$ = new ExprStmtNode((ExprNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SelectionStatement
    { $$ = $1; }
  | IterationStatement
    { $$ = $1; }
  | JumpStatement
    { $$ = $1; }
  | Block
    { $$ = $1; }
  | ConstraintBlock
    { $$ = $1; }
  | AwaitStatement
    { $$ = $1; }
  | Blackbox
    { $$ = $1; }
  | NetlistStatement
    { $$ = $1; }
  ;

/* Empty statement */

EmptyStatement :
    ';'
    { $$ = new EmptyStmtNode();
      ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                 new Integer(_lexer.lineNumber())); }
  ;

/* Labeled statement */

LabeledStatement :
    IDENTIFIER { pushCurrentLineNumber("LabeledStatement"); } ':' Statement
    { if (_inConstraint)
         yyerror("Labels cannot be used inside 'constraint' block");
      //$$ = new LabeledStmtNode(new LocalLabelNode((NameNode) $1),
      $$ = new LabeledStmtNode(new LocalLabelNode(
                                     new NameNode(AbsentTreeNode.instance, $1,
                                                  AbsentTreeNode.instance)),
                               (StatementNode) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledStatement")); }
  | BLOCK { pushCurrentLineNumber("LabeledStatement"); } '(' SimpleName ')' '{'
        BlockStatementsOpt '}' { pushBlockEndLineNumber("LabeledBlock"); }
    { if (_inConstraint)
      yyerror("Labels cannot be used inside 'constraint' block");
      $$ = new LabeledBlockNode((List) $7,
                                new LocalLabelNode((NameNode) $4));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledStatement"));
      ((TreeNode)$$).setProperty(
                _BLOCK_END_LINENUMBER_KEY,
                popBlockEndLineNumber("LabeledBlock")); }
  | SimpleName { pushCurrentLineNumber("LabeledStatement"); }
        BEGIN_LABEL BlockStatementsOpt END_LABEL
        { pushBlockEndLineNumber("ActionLabel"); }
    { if (_inConstraint)
      yyerror("Labels cannot be used inside 'constraint' block");
      List stmts = (List) $4;
      if (stmts.size() == 0) { 
        yyerror("Can not label an empty statement.");
      } else if (stmts.size() == 1) { 
        if (stmts.get(0) instanceof AnnotationNode)
          yyerror("Can not annotate an empty statement.");
      } else if (stmts.size() == 2) { 
        if (!((stmts.get(0) instanceof AnnotationNode)
             && (stmts.get(1) instanceof  StatementNode)) 
           && !((stmts.get(1) instanceof AnnotationNode)
                && (stmts.get(0) instanceof  StatementNode))) {
        yyerror("Labels can enclose at most one statement and one annotation.");
        }
      } else {
        yyerror("Labels can enclose at most one statement and one annotation.");
      }
      $$ = new ActionLabelStmtNode(new LocalLabelNode((NameNode)$1), (List) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledStatement")); 
      ((TreeNode)$$).setProperty(
                _BLOCK_END_LINENUMBER_KEY,
                popBlockEndLineNumber("ActionLabel")); }
  | BEGIN_ANNOTATION 
          { pushCurrentLineNumber("LabeledStatement"); _inAnnotation = true; }
                BlockStatementsOpt END_ANNOTATION
    { _inAnnotation = false;
      $$ = new AnnotationNode((List) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
      popCurrentLineNumber("LabeledStatement")); }
  | BEG '{' { pushCurrentLineNumber("LabeledStatement"); } BlockStatementsOpt
        '}'
    { if (!_inAnnotation) {
         yyerror("beg{...} cannot be used outside annotation ({$...$}) block");
      }
      $$ = new BeginAnnotationNode((List) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledStatement")); }
  | END '{' { pushCurrentLineNumber("LabeledStatement"); } BlockStatementsOpt
        '}'
    { if (!_inAnnotation) {
         yyerror("end{...} cannot be used outside annotation ({$...$}) block");
      }
      $$ = new EndAnnotationNode((List) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledStatement")); }
  ;

/* Expression statements */

ExpressionStatement :
    Assignment
    { $$ = $1; }
  | PreIncrement
    { $$ = $1; }
  | PreDecrement
    { $$ = $1; }
  | PostIncrement
    { $$ = $1; }
  | PostDecrement
    { $$ = $1; }
  | MethodCall
    { $$ = $1; }
  | AllocationExpression
    { $$ = $1; }
  ;

/* Selection statements */

SelectionStatement :
    IF PushSelectionStatementLineNumber '(' Expression ')' Statement %prec ELSE
    { $$ = new IfStmtNode((ExprNode) $4,
                          (StatementNode) $6,
                          AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                                 popCurrentLineNumber("SelectionStatement")); }
  | IF PushSelectionStatementLineNumber '(' Expression ')' Statement ELSE
        Statement
    { $$ = new IfStmtNode((ExprNode) $4,
                          (StatementNode) $6,
                          (TreeNode) $8);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                                 popCurrentLineNumber("SelectionStatement")); }
  | SWITCH PushSelectionStatementLineNumber '(' Expression ')' SwitchBlock
    { $$ = new SwitchNode((ExprNode) $4, (List) $6);
      ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                 popCurrentLineNumber("SelectionStatement")); }
  ;

PushSelectionStatementLineNumber :
    { pushCurrentLineNumber("SelectionStatement"); }

SwitchBlock :
    '{' SwitchBlockStatementsOpt '}'
    { $$ = $2; }
  ;

SwitchBlockStatementsOpt :
    empty
    { $$ = new LinkedList(); }
  | SwitchLabels BlockStatements SwitchBlockStatementsOpt
    { $$ = cons(new SwitchBranchNode((List) $1, (List) $2),
                (List) $3); }
    /* Handle labels at the end without any statements */
  | SwitchLabels
    { $$ = cons(new SwitchBranchNode((List) $1, new LinkedList())); }
  ;

SwitchLabels :
    SwitchLabel
    { $$ = cons($1); }
  | SwitchLabel SwitchLabels
    { $$ = cons($1, (List) $2); }
  ;

SwitchLabel :
    CASE { pushCurrentLineNumber("SwitchLabel"); } ConstantExpression ':'
    { $$ = new CaseNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("SwitchLabel")); }
  | DEFAULT { pushCurrentLineNumber("SwitchLabel"); } ':'
    { $$ = new CaseNode(AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("SwitchLabel")); }
  ;

/* Iteration statements */

IterationStatement :
    WHILE PushIterationStatementLineNumber '(' Expression ')' Statement
    { $$ = new LoopNode(new EmptyStmtNode(), (ExprNode) $4, (TreeNode) $6);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("IterationStatement")); }
  /*
   This only works if Statement is not empty (so its LINENUMBER is the
   LINENUMBER of its first statement, if it's a block):
   */
  | DO Statement WHILE '(' 
        Expression { pushDoLoopTestLineNumber("DoLoop"); } ')' ';'
    { $$ = new LoopNode((TreeNode) $2, (ExprNode) $5, new EmptyStmtNode());
      Integer loopTestLineNumber = popDoLoopTestLineNumber("DoLoop");
      Object blockLineNumber = ((TreeNode)$2).getProperty(_LINENUMBER_KEY);
      if (blockLineNumber == null || blockLineNumber instanceof NullValue) {
          ((TreeNode)$$).setProperty(_LINENUMBER_KEY, loopTestLineNumber);
      } else {
          ((TreeNode)$$).setProperty(_LINENUMBER_KEY, (Integer)blockLineNumber);
      }
      ((TreeNode)$$).setProperty(
                _DOLOOP_TEST_LINENUMBER_KEY, loopTestLineNumber); }
  | FOR PushIterationStatementLineNumber '(' ForInit Expression ';'
        ForUpdateOpt ')' Statement
    { $$ = new ForNode((List) $4, (ExprNode) $5,
      (List) $7, (StatementNode) $9);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("IterationStatement")); }
  | FOR PushIterationStatementLineNumber '(' ForInit ';' ForUpdateOpt ')'
        Statement
    { $$ = new ForNode((List) $4, new BoolLitNode("true"), (List) $6,
      (StatementNode) $8);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("IterationStatement")); }
  | BOUNDEDLOOP PushIterationStatementLineNumber '(' SimpleName ','
        INT_LITERAL ')' Statement
    { $$ = new BoundedLoopNode((NameNode)$4,new IntLitNode($6),
                               (StatementNode) $8);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("IterationStatement")); }
  ;

/* This exists to eliminate reduce/reduce conflicts for IterationStatement. */
PushIterationStatementLineNumber :
    { pushCurrentLineNumber("IterationStatement"); }

ForInit :
    ExpressionStatementsOpt ';'
    { $$ = $1; }
  | LocalVariableDeclarationStatement
    { $$ = $1; }
  ;

ForUpdateOpt :
    ExpressionStatements
    { $$ = $1; }
  | empty
    { $$ = new LinkedList(); }
  ;

ExpressionStatementsOpt :
    ExpressionStatements
    { $$ = $1; }
  | empty
    { $$ = new LinkedList(); }
  ;

ExpressionStatements :
    ExpressionStatement
    { $$ = cons($1); }
  | ExpressionStatement ',' ExpressionStatements
    { $$ = cons($1, (List) $3); }
  ;

/* Jump statements */

JumpStatement :
    BREAK LabelOpt ';'
    { $$ = new BreakNode((TreeNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | CONTINUE LabelOpt ';'
    { $$ = new ContinueNode((TreeNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | RETURN ExpressionOpt ';'
    { $$ = new ReturnNode((TreeNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

LabelOpt :
    SimpleName
    { $$ = new LocalLabelNode((NameNode) $1); }
  | empty
    { $$ = AbsentTreeNode.instance; }
  ;

/* Constraint statements */

ConstraintBlock :
    CONSTRAINT { pushCurrentLineNumber("ConstraintBlock"); } '{'
    { if (_inConstraint)
         yyerror("Cannot declare a 'constraint' block inside " +
                 "another 'constraint' block");
      _inConstraint = true;  }
    ConstraintBlockStatementsOpt
    { _inConstraint = false; }
    '}'
    { $$ = new ConstraintBlockNode((List) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintBlock")); }
  ;

ConstraintBlockStatementsOpt :
    empty
    { $$ = new LinkedList(); }
  | ConstraintBlockStatements ConstraintBlockStatementsOpt
    { $$ = appendLists((List) $1,(List) $2);}
  ;

ConstraintBlockStatements :
    BlockStatement
    { $$ = $1; }
  | ConstraintStatement
    { $$ = cons($1); }
  ;

ConstraintStatement :
    LTL
    { pushCurrentLineNumber("ConstraintStatement"); _inLTLFormula = true;
      _inActionFormula = true; }
                BuiltInLTLFormula
    { _inLTLFormula = false;
      _inActionFormula = false; }
                ';'
    {
      if (!_inConstraint && !($3 instanceof LTLSynchNode)) {
         yyerror("You must provide a name [a list of arguments] for "
                 + "defining ltl formulas outside constraints.");
      }
      LTLFormulaNode exp;
      if ($3 instanceof LTLFormulaNode) {
          exp = (LTLFormulaNode) $3;
      } else {
          exp = new ExprLTLNode((ExprNode)$3);
      }
      $$ = new LTLConstraintNode( (LTLFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LTL '('
    { pushCurrentLineNumber("ConstraintStatement"); _inLTLFormula = true;
      _inActionFormula = true; }
                Expression
    { _inLTLFormula = false;
      _inActionFormula = false; }
                ')' ';'
    {
      if (!_inConstraint && !($4 instanceof LTLSynchNode)) {
             yyerror("You must provide a name [a list of arguments] for "
                     + "defining ltl formulas outside constraints.");
      }
      LTLFormulaNode exp;
      if ($4 instanceof LTLFormulaNode) {
          exp = (LTLFormulaNode) $4;
      } else {
          exp = new ExprLTLNode((ExprNode)$4);
      }
      $$ = new LTLConstraintNode( (LTLFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LTL SimpleName '(' ')' '('
    { pushCurrentLineNumber("ConstraintStatement"); _inLTLFormula = true;
      _inActionFormula = true; }
                Expression ')'
    { _inLTLFormula = false;
      _inActionFormula = false; }
    ';'
    {
      LTLFormulaNode exp;
      if ($7 instanceof LTLFormulaNode) {
          exp = (LTLFormulaNode) $7;
      } else {
          exp = new ExprLTLNode((ExprNode)$7);
      }
      $$ = new LTLConstraintDeclNode((NameNode) $2,
                           (List) new LinkedList(), (LTLFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                        _LINENUMBER_KEY,
                        popCurrentLineNumber("ConstraintStatement")); }
  | LTL SimpleName '(' ConstraintParamList ')' '('
    { pushCurrentLineNumber("ConstraintStatement"); _inLTLFormula = true;
      _inActionFormula = true; }
                Expression ')'
    { _inLTLFormula = false;
      _inActionFormula = false; }
    ';'
    {
                        LTLFormulaNode exp;
      if ($8 instanceof LTLFormulaNode)
          exp = (LTLFormulaNode) $8;
      else
                exp = new ExprLTLNode((ExprNode)$8);
      $$ = new LTLConstraintDeclNode((NameNode) $2,
                           (List) $4, (LTLFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LTL SYNCH '(' ArgumentListOpt ':' EqualVarsOpt ')' ';'
    { pushCurrentLineNumber("ConstraintStatement"); 
      if (!_inConstraint) {
        yyerror("'synch()' used outside a 'ltl()' statement.");
      }
      if (!_inNetlist) {
        yyerror("'synch()' used outside a netlist.");
      }
			boolean hasEventImpl = false;
      Iterator eiter = ((List)$4).iterator();
      while (eiter.hasNext()) {
        ExprNode expr = (ExprNode) eiter.next();
				if (expr instanceof ImplyNode) {
				   hasEventImpl = true;
				} else if ( !((expr instanceof EventNode)
               || (expr instanceof ObjectNode)
               || (expr instanceof FieldAccessNode))) {
          yyerror("Only events and event implication are allowed in synch().");
        }
      }
			if (hasEventImpl) {
					 if (((List)$4).size()>1)
							yyerror("Event implication can not co-exist with other events or event implications in one synch().");
			} else if (((List)$4).size() < 2) {
          yyerror("There should be at least two event references in synch().");
      }
      $$ = new LTLSynchNode((List)$4, (List)$6);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LTL SYNCH '(' ArgumentListOpt ')' ';'
    { pushCurrentLineNumber("ConstraintStatement"); 
        if (!_inConstraint) {
          yyerror("'synch()' used outside a 'ltl()' statement.");
        }
        if (!_inNetlist) {
          yyerror("'synch()' used outside a netlist.");
        }
			  boolean hasEventImpl = false;
        Iterator eiter = ((List)$4).iterator();
        while (eiter.hasNext()) {
          ExprNode expr = (ExprNode) eiter.next();
					if (expr instanceof ImplyNode) {
						 hasEventImpl = true;
					} else if ( !((expr instanceof EventNode)
                 || (expr instanceof ObjectNode)
                 || (expr instanceof ArrayAccessNode)
                 || (expr instanceof FieldAccessNode))) {
             yyerror("There are non-event arguments in synch().");
          }
        }
			if (hasEventImpl) {
					 if (((List)$4).size()>1)
							yyerror("Event implication can not co-exist with other events or event implications in one synch().");
			} else if (((List)$4).size() < 2) {
          yyerror("There should be at least two event references in synch().");
      }
      $$ = new LTLSynchNode((List)$4, new LinkedList());
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LOC
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;
      _inActionFormula = true; }
                BuiltInLOCFormula
    { _inLTLFormula = false;
      _inActionFormula = false; }
                ';'
    {
      if (!_inConstraint) {
             yyerror("You must provide a name [a list of parameters] for "
                     + "defining loc formulas outside constraints.");
      }
      ActionFormulaNode exp;
      if ($3 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $3;
      } else {
          exp = new ExprActionNode((ExprNode)$3);
      }
      $$ = new LOCConstraintNode((ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LOC '('
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;
      _inActionFormula = true; }
                Expression
    { _inLTLFormula = false;
      _inActionFormula = false; }
                ')' ';'
    {
      if (!_inConstraint) {
         yyerror("You must provide a name [a list of parameters] for "
                 + "defining loc formulas outside constraints.");
      }
      ActionFormulaNode exp;
      if ($4 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $4;
      } else {
          exp = new ExprActionNode((ExprNode)$4);
      }
      $$ = new LOCConstraintNode((ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LOC SimpleName '(' ')' '('
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;

      _inActionFormula = true; }
                Expression ')'
    { _inLTLFormula = false;
      _inActionFormula = false; }
    ';'
    {
      ActionFormulaNode exp;
      if ($7 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $7;
      } else {
          exp = new ExprActionNode((ExprNode)$7);
      }
      $$ = new LOCConstraintDeclNode((NameNode) $2,
                           (List) new LinkedList(), (ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | LOC SimpleName '(' ConstraintParamList ')' '('
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;
      _inActionFormula = true; }
                Expression ')'
    { _inLTLFormula = false;
      _inActionFormula = false; }
    ';'
    {
      ActionFormulaNode exp;
      if ($8 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $8;
      } else {
          exp = new ExprActionNode((ExprNode)$8);
      }
      $$ = new LOCConstraintDeclNode((NameNode) $2,
                           (List) $4, (ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | ELOC '('
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;
      _inActionFormula = true; }
                Expression
    { _inLTLFormula = false;
      _inActionFormula = false; }
                ')' ';'
    {
      if (!_inConstraint) {
         yyerror("You must provide a name [a list of arguments] for "
                 + "defining eloc formulas outside constraints.");
      }
      ActionFormulaNode exp;
      if ($4 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $4;
      } else {
          exp = new ExprActionNode((ExprNode)$4);
      }
      $$ = new ELOCConstraintNode((ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | ELOC SimpleName '(' ')' '('
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;
      _inActionFormula = true; }
                Expression ')'
    { _inLTLFormula = false;
      _inActionFormula = false; }
    ';'
    {
      ActionFormulaNode exp;
      if ($7 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $7;
      } else {
          exp = new ExprActionNode((ExprNode)$7);
      }
      $$ = new ELOCConstraintDeclNode((NameNode) $2,
                           (List) new LinkedList(), (ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  | ELOC SimpleName '(' ConstraintParamList ')' '('
    { pushCurrentLineNumber("ConstraintStatement"); 
        _inLTLFormula = true;
      _inActionFormula = true; }
                Expression ')'
    { _inLTLFormula = false;
      _inActionFormula = false; }
    ';'
    {
      ActionFormulaNode exp;
      if ($8 instanceof ActionFormulaNode) {
          exp = (ActionFormulaNode) $8;
      } else {
          exp = new ExprActionNode((ExprNode)$8);
      }
      $$ = new ELOCConstraintDeclNode((NameNode) $2,
                           (List) $4, (ActionFormulaNode) exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("ConstraintStatement")); }
  ;

ConstraintParamList :
    ConstraintParameter
          { $$ = cons($1); }
        | ConstraintParamList ',' ConstraintParameter
          { $$ = cons($3, (List) $1); }
        ;

ConstraintParameter :
    Type FormalDeclarator
          { DeclaratorNode decl = (DeclaratorNode) $2;
      int dims      = decl.getDims();
      NameNode name = decl.getName();
      TypeNode type = TypeUtility.makeArrayType((TypeNode) $1,dims);
      $$ = new ParameterNode(Modifier.NO_MOD,type,name);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

EqualVarsOpt :
    empty
                { $$ = new LinkedList(); }
        | EqualVarsList
          { $$ = $1; }
        ;

EqualVarsList :
    EqualVars
                { $$ = cons($1); }
        | EqualVars ',' EqualVarsList
          { $$ = cons($1, (List)$3); }
        ;

EqualVars :
    Expression EQ Expression
                {$$ = new EqualVarsNode((ExprNode)$1, (ExprNode)$3); }
  ;

/* Await statement */

AwaitStatement :
    AWAIT { pushCurrentLineNumber("AwaitStatement"); }
        '{' AwaitGuardsOpt '}'
    { $$ = new AwaitStatementNode((List) $4);
     ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                popCurrentLineNumber("AwaitStatement"));  }
  | AWAIT { pushCurrentLineNumber("AwaitStatement"); } AwaitGuard
    { $$ = new AwaitStatementNode(cons($3));
     ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                popCurrentLineNumber("AwaitStatement"));  }
  ;

AwaitGuardsOpt :
    empty
    { $$ = new LinkedList(); }
  | AwaitGuard AwaitGuardsOpt
    { $$ = cons($1,(List) $2); }
  ;

AwaitGuard :
    '(' Guard { pushCurrentLineNumber("AwaitGuard"); }
        ';' AwaitLockList ';' AwaitLockList ')' Statement
    { $$ = new AwaitGuardNode((TreeNode) $2, (List) $5, (List) $7,
                              (StatementNode) $9);
     ((TreeNode)$$).setProperty(_LINENUMBER_KEY,
                                popCurrentLineNumber("AwaitGuard")); }
  ;

Guard :
    Expression
    { $$ = $1; }
  | DEFAULT
    { $$ = AbsentTreeNode.instance; }
  ;

AwaitLockList :
    empty
    { $$ = new LinkedList(); }
  | AwaitLocks
    { $$ = $1; }
  ;

AwaitLocks :
    AwaitLock
    { $$ = cons($1);  }
  | AwaitLock ',' AwaitLocks
    { $$ = cons($1, (List) $3); }
  ;
 
AwaitLock :
    ALL
    { $$ = new AwaitLockNode(AbsentTreeNode.instance,
                             AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SimpleName '.' SimpleNameOfTemplate
    { $$ = new AwaitLockNode(new ObjectNode((NameNode)$1),(TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SimpleName '.' ALL
    { $$ = new AwaitLockNode(new ObjectNode((NameNode)$1),
                             AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | THIS '.' SimpleNameOfTemplate
    { $$ = new AwaitLockNode(new ThisNode(),(TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | THIS '.' ALL
    { $$ = new AwaitLockNode(new ThisNode(),AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Statements that can only appear inside a netlist */

NetlistStatement :
    REFINE '(' Expression ',' Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'refine' used outside a netlist.");
      $$ = new RefineNode((ExprNode) $3,(ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | CONNECT '(' Expression ',' Expression ',' Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'connect' used outside a netlist.");
      $$ = new ConnectNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | REFINECONNECT '(' Expression ',' Expression ',' Expression ','
    Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'refineconnect' used outside a netlist.");
      $$ = new RefineConnectNode((ExprNode) $3,(ExprNode) $5,(ExprNode) $7,
                                   (ExprNode) $9);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | REDIRECTCONNECT '(' Expression ',' Expression ',' Expression ','
    Expression ',' Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'redirectconnect' used outside a netlist.");
      $$ = new RedirectConnectNode((ExprNode) $3,(ExprNode) $5,
                                   (ExprNode) $7,(ExprNode) $9,
                                   (ExprNode) $11);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ADDCOMPONENT '(' Expression ',' Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'addcomponent' used outside a netlist.");
                        StringLitNode sln = new StringLitNode(new String(""));
      $$ = new AddComponentNode((ExprNode) $3, (ExprNode) $5, (ExprNode) sln);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ADDCOMPONENT '(' Expression ',' Expression ',' Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'addcomponent' used outside a netlist.");
      $$ = new AddComponentNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SETSCOPE '(' Expression ',' Expression ',' Expression ')' ';'
    { if (!_inNetlist)
        yyerror("'setscope' used outside a netlist.");
      $$ = new SetScopeNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;


        /* FORMULAS */

/* Labels */

Label :
    LocalLabel
    { $$ = $1; }
  | GlobalLabel
    { $$ = $1;  }
  ;

LocalLabel :
    SimpleName
    { $$ = new LocalLabelNode((NameNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

GlobalLabel :
    LABEL '(' Expression ',' LocalLabel ')'
    { $$ = new GlobalLabelNode((ExprNode) $3, (LocalLabelNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* LTL formulas */

LTLFormula :
    F '(' Expression ')'
    { if (!_inLTLFormula)
          yyerror("LTL operator 'F()' used outside a 'ltl()' statement.");
      LTLFormulaNode exp;
      if ($3 instanceof LTLFormulaNode)
          exp = (LTLFormulaNode) $3;
      else
          exp = new ExprLTLNode((ExprNode)$3);
      $$ = new FutureLTLNode(exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | G '(' Expression ')'
    { if (!_inLTLFormula)
          yyerror("LTL operator 'G()' used outside a 'ltl()' statement.");
      LTLFormulaNode exp;
      if ($3 instanceof LTLFormulaNode)
          exp = (LTLFormulaNode) $3;
      else
          exp = new ExprLTLNode((ExprNode)$3);
      $$ = new GloballyLTLNode(exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | X '(' Expression ')'
    { if (!_inLTLFormula)
          yyerror("LTL operator 'X()' used outside a 'ltl()' statement.");
      LTLFormulaNode exp;
      if ($3 instanceof LTLFormulaNode)
          exp = (LTLFormulaNode) $3;
      else
          exp = new ExprLTLNode((ExprNode)$3);
      $$ = new NextLTLNode(exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression U Expression
    { if (!_inLTLFormula)
          yyerror("LTL operator 'U' used outside a 'ltl()' statement.");
      LTLFormulaNode exp1, exp2;
      if ($1 instanceof LTLFormulaNode)
          exp1 = (LTLFormulaNode) $1;
      else
          exp1 = new ExprLTLNode((ExprNode) $1);
      if ($3 instanceof LTLFormulaNode)
          exp2 = (LTLFormulaNode) $3;
      else
          exp2 = new ExprLTLNode((ExprNode) $3);
      $$ = new UntilLTLNode(exp1,exp2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | BuiltInLTLFormula
          { $$ = $1; }
  | LTL SimpleName '(' ')' ';'
    {
      if (!_inConstraint)
         yyerror("Named loc formulas can only be used inside a " +
                                 "constraint statement");
      $$ = new LTLConstraintCallNode((NameNode) $2, (List) new LinkedList());
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | LTL SimpleName '(' ArgumentList ')' ';'
    {
      if (!_inConstraint)
         yyerror("Named loc formulas can only be used inside a " +
                                 "constraint statement");
      $$ = new LTLConstraintCallNode((NameNode) $2, (List) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }

  ;

BuiltInLTLFormula :
  EXCL  '(' Expression ',' Expression ')'
    { if (!_inLTLFormula) {
        yyerror("LTL formula 'excl' used outside a 'ltl()' statement.");
      }
      if ( !(($3 instanceof EventNode)
             || ($3 instanceof ObjectNode)
             || ($3 instanceof FieldAccessNode))
           || !(($5 instanceof EventNode)
                || ($5 instanceof ObjectNode)
                || ($5 instanceof FieldAccessNode)) ) {
         yyerror("Both expressions in excl() must be event references.");
      }
      $$ =  new ExclLTLNode((ExprNode) $3, (ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | MUTEX '(' Expression ',' Expression ')'
    { if (!_inLTLFormula) {
        yyerror("LTL formula 'mutex' used outside a 'ltl()' statement.");
      }
      if ( !(($3 instanceof EventNode)
             || ($3 instanceof ObjectNode)
             || ($3 instanceof FieldAccessNode))
           || !(($5 instanceof EventNode)
                || ($5 instanceof ObjectNode)
                || ($5 instanceof FieldAccessNode)) ) {
         yyerror("Both expressions in mutex() must be event references.");
      }
      $$ =  new MutexLTLNode((ExprNode) $3, (ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SIMUL '(' Expression ',' Expression ')'
    { if (!_inLTLFormula) {
          yyerror("LTL formula 'simul' used outside a 'ltl()' statement.");
      }
      if ( !(($3 instanceof EventNode)
             || ($3 instanceof ObjectNode)
             || ($3 instanceof FieldAccessNode))
           || !(($5 instanceof EventNode)
                || ($5 instanceof ObjectNode)
                || ($5 instanceof FieldAccessNode))) {
         yyerror("Both expressions in simul() must be event references.");
      }
      $$ =  new SimulLTLNode((ExprNode) $3, (ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | PRIORITY '(' Expression ',' Expression ',' Expression ')'
    { if (!_inLTLFormula) {
        yyerror("LTL formula 'priority' used outside a 'ltl()' statement.");
      }
      if ( !(($3 instanceof EventNode)
             || ($3 instanceof ObjectNode)
             || ($3 instanceof FieldAccessNode))
           || !(($5 instanceof EventNode)
                || ($5 instanceof ObjectNode)
                || ($5 instanceof FieldAccessNode))) {
         yyerror("The first two expressions in priority() must be "
                 + "event references.");
      }
      $$ =  new PriorityLTLNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7 );
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Action formulas */

ActionFormula :
  FORALL '(' FormalParamListOpt ')' Expression
    { if (!_inActionFormula) {
        yyerror("Action formula 'forall' used outside a 'lfo()', "
                + "'loc()' and 'eloc()' statetments.");
      }
      ActionFormulaNode exp;
      if ($5 instanceof ActionFormulaNode)
            exp = (ActionFormulaNode) $5;
      else
              exp = new ExprActionNode((ExprNode) $5);
      $$ = new ForallActionNode((List) $3,exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | EXISTS '(' FormalParamListOpt ')' Expression
    { if (!_inActionFormula) {
        yyerror("Action formula 'exists' used outside a 'lfo()', "
                + "'loc()' and 'eloc()' statetments.");
      }
      ActionFormulaNode exp;
      if ($5 instanceof ActionFormulaNode)
              exp = (ActionFormulaNode) $5;
      else
              exp = new ExprActionNode((ExprNode) $5);
      $$ = new ExistsActionNode((List) $3,exp);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | BuiltInLOCFormula
          { $$ = $1; }
  | LOC SimpleName '(' ')'
    {
      if (!_inConstraint)
         yyerror("Named loc formulas can only be used inside a " +
                                 "constraint statement");
      $$ = new LOCConstraintCallNode((NameNode) $2, (List) new LinkedList());
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | LOC SimpleName '(' ArgumentList ')'
    {
      if (!_inConstraint)
         yyerror("Named loc formulas can only be used inside a " +
                                 "constraint statement");
      $$ = new LOCConstraintCallNode((NameNode) $2, (List) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ELOC SimpleName '(' ')'
    {
      if (!_inConstraint)
         yyerror("Named eloc formulas can only be used inside a " +
                                 "constraint statement");
      $$ = new ELOCConstraintCallNode((NameNode) $2, (List) new LinkedList());
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ELOC SimpleName '(' ArgumentList ')'
    {
      if (!_inConstraint)
         yyerror("Named eloc formulas can only be used inside a " +
                                 "constraint statement");
      $$ = new ELOCConstraintCallNode((NameNode) $2, (List) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

BuiltInLOCFormula :
    MAXDELTA '(' Expression ',' Expression ',' Expression ',' Expression ')'
      { if (!_inActionFormula) {
          yyerror("'maxdelta' used outside a 'loc' statetment.");
        }
        if (!(($5 instanceof EventNode)
              || ($5 instanceof ObjectNode)
              || ($5 instanceof FieldAccessNode))
            || !(($7 instanceof EventNode)
                 || ($7 instanceof ObjectNode)
                 || ($7 instanceof FieldAccessNode)) ) {
           yyerror("The 2nd and 3rd arguments in maxdelta() must be "
                   + "event references.");
        }
        $$ = new MaxDeltaNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7,
                             (ExprNode) $9);
        ((TreeNode)$$).setProperty(
                  _LINENUMBER_KEY,
                  new Integer(_lexer.lineNumber())); }
    | MINDELTA '(' Expression ',' Expression ',' Expression ',' Expression ')'
      { if (!_inActionFormula) {
           yyerror("'mindelta' used outside a 'loc' statetment.");
        }
        if ( !(($5 instanceof EventNode)
               || ($5 instanceof ObjectNode)
               || ($5 instanceof FieldAccessNode))
             || !(($7 instanceof EventNode)
                  || ($7 instanceof ObjectNode)
                  || ($7 instanceof FieldAccessNode))) {
           yyerror("The 2nd and 3rd arguments in mindelta() must be "
                   + "event references.");
        }
        $$ = new MinDeltaNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7,
                             (ExprNode) $9);
        ((TreeNode)$$).setProperty(
                  _LINENUMBER_KEY,
                  new Integer(_lexer.lineNumber())); }
    | MAXRATE '(' Expression ',' Expression ',' Expression ')'
      { if (!_inActionFormula) {
           yyerror("'maxrate' used outside a 'loc' statetment.");
        }
        if (!(($5 instanceof EventNode)
              || ($5 instanceof ObjectNode)
              || ($5 instanceof FieldAccessNode))) {
           yyerror("The 2nd argument in maxrate() must be an event reference.");
        }
        $$ = new MaxRateNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7);
        ((TreeNode)$$).setProperty(
                  _LINENUMBER_KEY,
                  new Integer(_lexer.lineNumber())); }
        | MINRATE '(' Expression ',' Expression ',' Expression ')'
    { if (!_inActionFormula) {
         yyerror("'minrate' used outside a 'loc' statetment.");
      }
      if (!(($5 instanceof EventNode)
            || ($5 instanceof ObjectNode)
            || ($5 instanceof FieldAccessNode))) {
         yyerror("The 2nd argument in minrate() must be an event reference.");
      }
      $$ = new MinRateNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | PERIOD '(' Expression ',' Expression ',' Expression ')'
    { if (!_inActionFormula) {
         yyerror("'period' used outside a 'loc' statetment.");
      }
      if (!(($5 instanceof EventNode)
            || ($5 instanceof ObjectNode)
            || ($5 instanceof FieldAccessNode))) {
         yyerror("The 2nd argument in minrate() must be an event reference.");
      }
      $$ = new PeriodNode((ExprNode) $3, (ExprNode) $5, (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Action */
/* all, <object>.<name> */

Action :
    ALL
    { $$ = new ActionNode((TreeNode) AbsentTreeNode.instance,
                          (TreeNode) AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | THIS '.' SimpleName
    { $$ = new ActionNode((TreeNode) new ThisNode(), (NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | SimpleName '.' SimpleName
    { $$ = new ActionNode((TreeNode) $1, (NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GetComponent '.' SimpleName
    { $$ = new ActionNode((TreeNode) $1, (NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GetConnectionDest '.' SimpleName
    { $$ = new ActionNode((TreeNode) $1, (NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GetNthConnectionSrc '.' SimpleName
    { $$ = new ActionNode((TreeNode) $1, (NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GetThread '.' SimpleName
    { $$ = new ActionNode((TreeNode) $1, (NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

        /* EXPRESSIONS */

/* Relation with formulas */

Expression :
    NormalExpression
    { $$ = $1; }
  | ActionFormula
    { $$ = $1; }
  | LTLFormula
    { $$ = $1; }
  ;

/* Non formula expressions */

ExpressionOpt :
    Expression
    { $$ = $1; }
  | empty
    { $$ = AbsentTreeNode.instance; }
  ;

NormalExpression :
    UnaryExpression
    { $$ = $1; }
  | Expression '*' Expression
    { $$ = new MultNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '/' Expression
    { $$ = new DivNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '%' Expression
    { $$ = new RemNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '+' Expression
    { $$ = new PlusNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '-' Expression
    { $$ = new MinusNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression LSHIFTL Expression
    { $$ = new LeftShiftLogNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression LSHIFTR Expression
    { $$ = new RightShiftLogNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression ASHIFTR Expression
    { $$ = new RightShiftArithNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '<' Expression
    { $$ = new LTNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '>' Expression
    { $$ = new GTNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression LE Expression
    { $$ = new LENode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression GE Expression
    { $$ = new GENode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression INSTANCEOF ReferenceType
    { $$ = new InstanceOfNode((ExprNode) $1, (TypeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression EQ Expression
    { $$ = new EQNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression NE Expression
    { $$ = new NENode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '&' Expression
    { $$ = new BitAndNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '|' Expression
    { $$ = new BitOrNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '^' Expression
    { $$ = new BitXorNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression CAND Expression
    { $$ = new CandNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression COR Expression
    { $$ = new CorNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression CIF Expression
    { $$ = new CifNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression CIFF Expression
    { $$ = new CiffNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Expression '?' Expression ':' Expression
    { $$ = new IfExprNode((ExprNode) $1, (ExprNode) $3, (ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Assignment
    { $$ = $1; }
        | LabeledExpression
          { $$ = $1; }
        | Expression IMPLY Expression
          { //if (!(($1 instanceof EventNode) || ($1 instanceof ObjectNode)
             // || ($1 instanceof FieldAccessNode)))
             //yyerror("Expression before a '=>' must be an event reference.");
                        $$ = new ImplyNode((ExprNode)$1, (ExprNode)$3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;


/* Labeled Expression, e.g. {@ y+x-1 @} */
LabeledExpression :
    LabeledExpressionFirst END_LABEL
    { $$ = $1; }
  | LabeledExpressionFirst 
    BEGIN_ANNOTATION 
          { pushCurrentLineNumber("LabeledExpression"); _inAnnotation = true; }
                BlockStatementsOpt END_ANNOTATION END_LABEL
    { _inAnnotation = false;
      AnnotationNode anode = new AnnotationNode((List) $4);
      ((TreeNode)anode).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledExpression"));
      ((ActionLabelExprNode)$1).setAnode((TreeNode) anode);
      $$ = $1;
     }
  ;

LabeledExpressionFirst :
    SimpleName BEGIN_LABEL
          { pushCurrentLineNumber("LabeledExpressionFirst");
            _inExprLabel++;
            if (_inExprLabel>1) {
                yyerror("Nested expression labels are disallowed.");
            }
          }
                Expression
    { _inExprLabel--;
                  if (_inConstraint)
       yyerror("Labels cannot be used inside 'constraint' block");
      $$ = new ActionLabelExprNode(new LocalLabelNode((NameNode)$1),
                                   (ExprNode) $4,
                                   AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledExpressionFirst")); }
  | SimpleName BEGIN_LABEL
          { pushCurrentLineNumber("LabeledExpressionFirst"); _inExprLabel++;
                  if (_inExprLabel>1)
                         yyerror("Nested expression labels are disallowed.");
                }
    BEGIN_ANNOTATION 
          { _inLineNumber1 = _lexer.lineNumber(); _inAnnotation = true; }
                BlockStatementsOpt END_ANNOTATION 
                Expression 
    { _inAnnotation = false;
      AnnotationNode anode = new AnnotationNode((List) $6);
      ((TreeNode)anode).setProperty(
                _LINENUMBER_KEY,
                new Integer(_inLineNumber1));
      _inExprLabel--;
                  if (_inConstraint)
       yyerror("Labels cannot be used inside 'constraint' block");
      $$ = new ActionLabelExprNode(new LocalLabelNode((NameNode)$1),
                                   (ExprNode) $8,
                                   (TreeNode) anode);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                popCurrentLineNumber("LabeledExpressionFirst")); }
  ;

/* Assingments */

Assignment :
    UnaryExpression '=' Expression
    { $$ = new AssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression MULT_ASG Expression
    { $$ = new MultAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression DIV_ASG Expression
    { $$ = new DivAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression REM_ASG Expression
    { $$ = new RemAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression PLUS_ASG Expression
    { $$ = new PlusAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression MINUS_ASG Expression
    { $$ = new MinusAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression LSHIFTL_ASG Expression
    { $$ = new LeftShiftLogAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression LSHIFTR_ASG Expression
    { $$ = new RightShiftLogAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression ASHIFTR_ASG Expression
    { $$ = new RightShiftArithAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression AND_ASG Expression
    { $$ = new BitAndAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression XOR_ASG Expression
    { $$ = new BitXorAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpression OR_ASG Expression
    { $$ = new BitOrAssignNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Primary expressions (basics) */

PrimaryExpression :
    NameOfTemplate      %prec ')'
    { $$ = new ObjectNode((NameNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | NotJustName
    { $$ = $1; }
  | NameOfTemplate '.' CLASS
    { $$ = new TypeClassAccessNode(new TypeNameNode((NameNode) $1));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | PrimitiveType '.' CLASS
    { $$ = new TypeClassAccessNode((TypeNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | Void '.' CLASS
    { $$ = new TypeClassAccessNode((TypeNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | NameOfTemplate '.' THIS
    { $$ = new OuterThisAccessNode(new TypeNameNode((NameNode) $1));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | NameOfTemplate '.' SUPER
    { $$ = new OuterSuperAccessNode(new TypeNameNode((NameNode) $1));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

NotJustName :
    AllocationExpression
    { $$ = $1; }
  | ComplexPrimary
    { $$ = $1; }
  ;

ComplexPrimary :
    Literal
    { $$ = $1; }
  | _NULL
    { $$ = new NullPntrNode();
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | THIS
    { $$ = new ThisNode();
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | '(' Expression ')'
    { $$ = $2; }
  | '(' NameOfTemplate ')'
    { $$ = new ObjectNode((NameNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ArrayAccess
    { $$ = $1; }
  | FieldAccess
    { $$ = $1; }
  | MethodCall
    { $$ = $1; }
  | ArrayType '.' CLASS
    { $$ = new TypeClassAccessNode((TypeNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | BeginEvent
          { $$ = (EventNode) $1; }
        | EndEvent
          { $$ = (EventNode) $1; }
        | NoneEvent
          { $$ = (EventNode) $1; }
        | OtherEvent
          { $$ = (EventNode) $1; }
  ;
/* Note: The fifth production above is redundant, but helps resolve a  */
/* LALR(1) lookahead conflict arising in cases like "(T) + x" (Do we reduce */
/* Name T to TypeName on seeing the ")"?). See also  */
/* CastExpression in Unary expressions. */

/* Array accesses */

ArrayAccess :
    NameOfTemplate '[' Expression ']'
    { $$ = new ArrayAccessNode(new ObjectNode((NameNode) $1),
                               (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ComplexPrimary '[' Expression ']'
    { $$ = new ArrayAccessNode((ExprNode) $1, (ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Field accesses */

FieldAccess :
    /* The following never matches Name '.' IDENTIFIER */
    PrimaryExpression '.' SimpleNameOfTemplate
    { $$ = new ObjectFieldAccessNode((NameNode) $3, (ExprNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | SUPER '.' SimpleNameOfTemplate
    { $$ = new SuperFieldAccessNode((NameNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Method calls */

MethodCall :
    NameOfTemplate '(' ArgumentListOpt ')'
    { $$ = new MethodCallNode(new ObjectNode((NameNode) $1), (List) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | FieldAccess '(' ArgumentListOpt ')'
    { $$ = new MethodCallNode((FieldAccessNode) $1, (List) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

ArgumentListOpt :
    ArgumentList
    { $$ = $1; }
  | empty
    { $$ = new LinkedList(); }
  ;

ArgumentList :
    Expression
    { $$ = cons($1); }
  | Expression ',' ArgumentList
    { $$ = cons($1, (List) $3); }
  ;

/* Postfix expressions */

PostfixExpression :
    PrimaryExpression
  | PostIncrement
  | PostDecrement
  ;

PostIncrement :
    PostfixExpression PLUSPLUS
    { $$ = new PostIncrNode((ExprNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

PostDecrement :
    PostfixExpression MINUSMINUS
    { $$ = new PostDecrNode((ExprNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

/* Unary expressions */

UnaryExpression :
    PreIncrement
  | PreDecrement
  | '+' UnaryExpression
    { $$ = new UnaryPlusNode((ExprNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | '-' UnaryExpression
    { $$ = new UnaryMinusNode((ExprNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | UnaryExpressionNotPlusMinus
    { $$ = $1; }
  ;

PreIncrement :
    PLUSPLUS UnaryExpression
    { $$ = new PreIncrNode((ExprNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

PreDecrement :
    MINUSMINUS UnaryExpression
    { $$ = new PreDecrNode((ExprNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;

UnaryExpressionNotPlusMinus :
    PostfixExpression
    { $$ = $1; }
  | '~' UnaryExpression
    { $$ = new ComplementNode((ExprNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | '!' UnaryExpression
    { $$ = new NotNode((ExprNode) $2);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | CastExpression
    { $$ = $1;  }
  | EXECINDEX '(' Expression  ')'
    { $$ = new ExecIndexNode((ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | NONDETERMINISM '(' PrimitiveType ')'
    { $$ = new NonDeterminismNode((PrimitiveTypeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | PC '(' Expression ')'
    { if (!_inConstraint)
         yyerror("'pc(PROCESS)' can only be used inside a constraint " +
                 "statement");
      $$ = new PCNode((ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | BEG '(' Label ')'
    { if (!_inConstraint)
         yyerror("'beg(LABEL)' can only be used inside a constraint " +
                 "statement");
      $$ = new BeginPCNode((LabelNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | END '(' Label ')'
    { if (!_inConstraint)
         yyerror("'end(LABEL)' can only be used inside a constraint " +
                 "statement");
      $$ = new EndPCNode((LabelNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETCONNECTIONNUM '(' Expression ',' SimpleNameOfTemplate ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
        yyerror("'getconnectionnum' should be used inside a netlist"
                                +", a constraint block or an annotation.");
      $$ = new GetConnectionNumNode((ExprNode)$3,(NameNode)$5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETCONNECTIONNUM '(' Expression ',' ALL ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
        yyerror("'getconnectionnum' should be used inside a netlist"
                                +", a constraint block or an annotation.");
      $$ = new GetConnectionNumNode((ExprNode)$3,AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETNTHCONNECTIONPORT '(' Expression ',' SimpleNameOfTemplate ',' Expression ')'
    {  if (!_inNetlist && !_inConstraint && !_inAnnotation)
        yyerror("'getnthconnectionport' should be used inside a netlist"
                                +", a constraint block or an annotation.");
      $$ = new GetNthConnectionPortNode((ExprNode)$3,(NameNode)$5,(ExprNode)$7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETNTHPORT '(' Expression ',' SimpleNameOfTemplate ',' Expression ')'
          { if (!_inNetlist && !_inConstraint && !_inAnnotation)
                   yyerror("'getnthport' should be used inside a netlist"
                         +", a constraint block or an annotation.");
      $$ = new GetNthPortNode((ExprNode) $3, (NameNode) $5, (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETPORTNUM '(' Expression ',' SimpleNameOfTemplate ')'
          { if (!_inNetlist && !_inConstraint && !_inAnnotation)
                   yyerror("'getportnum' should be used inside a netlist"
                         +", a constraint block or an annotation.");
      $$ = new GetPortNumNode((ExprNode) $3, (NameNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETSCOPE '(' Expression ',' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
        yyerror("'getnthconnectionsrc' should be used inside a netlist"
                                +", a constraint block or an annotation.");
      $$ = new GetScopeNode((ExprNode)$3, (ExprNode)$5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | GETCOMPNAME '(' Expression ',' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
       yyerror("'getcompname' should be used inside a netlist"
       +", a constraint block or an annotation.");
      $$ = new GetCompNameNode((ExprNode) $3, (ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | GETINSTNAME '(' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
       yyerror("'getinstname' should be used inside a netlist"
       +", a constraint block or an annotation.");
      $$ = new GetInstNameNode((ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | ISCONNECTIONREFINED '(' Expression ',' Expression ',' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
               yyerror("'isconnectionrefined' should be used inside a netlist"
                     +", a constraint block or an annotation.");
      $$ = new IsConnectionRefinedNode((ExprNode) $3, (ExprNode) $5,
                                       (ExprNode) $7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETTYPE '(' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
       yyerror("'gettype' should be used inside a netlist"
       +", a constraint block or an annotation.");
      $$ = new GetTypeNode((ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | GETPROCESS '(' Expression ')'
    { if (!_inConstraint && !_inAnnotation && !_inMethod)
         yyerror("'getprocess' should be used inside a constraint, "
                 + "annotation or a method.");
      if ( !(($3 instanceof EventNode)
             || ($3 instanceof ObjectNode)
             || ($3 instanceof FieldAccessNode)) ) {
         yyerror("The argument in getprocess() must be an event reference.");
      }
      $$ = new GetProcessNode((ExprNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | VariableInEventReference
    { $$ = $1; }
  | GetComponent
    { $$ = $1; }
  | GetNthConnectionSrc
    { $$ = $1; }
  | GetConnectionDest
    { $$ = $1; }
  | GetThread
    { $$ = $1; }
  ;

VariableInEventReference :
                ArrayAccess AT '(' Expression ',' Expression ')' 
    { if (!_inConstraint)
         yyerror("'var@(Event, index)' can only be used inside a "
                 + "constraint statement");
      $$ = new VarInEventRefNode((ExprNode) $1, 
                                (ExprNode) $4, (ExprNode) $6);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | SimpleName AT '(' Expression ',' Expression ')' 
    { if (!_inConstraint)
         yyerror("'var@(Event, index)' can only be used inside a "
                 + "constraint statement");
      $$ = new VarInEventRefNode((ExprNode)(new ObjectNode((NameNode)$1)), 
                                (ExprNode) $4, (ExprNode) $6);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | Literal AT '(' Expression ',' Expression ')' 
    { if (!_inConstraint)
         yyerror("'var@(Event, index)' can only be used inside a "
                 + "constraint statement");
      $$ = new VarInEventRefNode((ExprNode) $1, 
                                (ExprNode) $4, (ExprNode) $6);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

GetComponent :
    GETCOMPONENT '(' Expression ',' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
             yyerror("'getcomponent' should be used inside a netlist"
                   +", a constraint block or an annotation.");
      $$ = new GetComponentNode((ExprNode) $3, (ExprNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

GetNthConnectionSrc :
    GETNTHCONNECTIONSRC '(' Expression ',' SimpleNameOfTemplate ',' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
        yyerror("'getnthconnectionsrc' should be used inside a netlist"
                                +", a constraint block or an annotation.");
      $$ = new GetNthConnectionSrcNode((ExprNode)$3,(NameNode)$5,
                                       (ExprNode)$7);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

GetConnectionDest :
    GETCONNECTIONDEST '(' Expression ',' Expression ')'
    { if (!_inNetlist && !_inConstraint && !_inAnnotation)
        yyerror("'getnthconnectionsrc' should be used inside a netlist"
                                +", a constraint block or an annotation.");
      $$ = new GetConnectionDestNode((ExprNode)$3, (ExprNode)$5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

GetThread :
    GETTHREAD '(' ')'
    { /* if (!_inConstraint && !_inAnnotation)
             yyerror("'getthread' should be used inside "
                   +"a constraint block or an annotation."); */
      $$ = new GetThreadNode();
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

/* Event expression */

BeginEvent :
    BEG '(' ALL ',' Action ')'
          { $$ = new BeginEventNode((TreeNode) AbsentTreeNode.instance,
                                          (ActionNode) $5); }
        | BEG '(' SimpleName ',' Action ')'
          { $$ = new BeginEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | BEG '(' GetComponent ',' Action ')'
          { $$ = new BeginEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | BEG '(' GetNthConnectionSrc ',' Action ')'
          { $$ = new BeginEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | BEG '(' GetThread ',' Action ')'
          { $$ = new BeginEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | BEG '(' THIS ',' Action ')'
          { $$ = new BeginEventNode((TreeNode) new ThisNode(), (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

EndEvent :
    END '(' ALL ',' Action ')'
          { $$ = new EndEventNode((TreeNode) AbsentTreeNode.instance,
                                        (ActionNode) $5); }
        | END '(' SimpleName ',' Action ')'
          { $$ = new EndEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | END '(' GetComponent ',' Action ')'
          { $$ = new EndEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | END '(' GetNthConnectionSrc ',' Action ')'
          { $$ = new EndEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | END '(' GetThread ',' Action ')'
          { $$ = new EndEventNode((TreeNode) $3, (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | END '(' THIS ',' Action ')'
          { $$ = new EndEventNode((TreeNode) new ThisNode(), (ActionNode) $5);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

NoneEvent :
    NONE '(' ALL ')'
          { $$ = new NoneEventNode((TreeNode) AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | NONE '(' SimpleName ')'
          { $$ = new NoneEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | NONE '(' GetComponent ')'
          { $$ = new NoneEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | NONE '(' GetNthConnectionSrc ')'
          { $$ = new NoneEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | NONE '(' GetThread ')'
          { $$ = new NoneEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

OtherEvent :
    OTHER '(' ALL ')'
          { $$ = new OtherEventNode((TreeNode) AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | OTHER '(' SimpleName ')'
          { $$ = new OtherEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | OTHER '(' GetComponent ')'
          { $$ = new OtherEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | OTHER '(' GetNthConnectionSrc ')'
          { $$ = new OtherEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        | OTHER '(' GetThread ')'
          { $$ = new OtherEventNode((TreeNode) $3);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
        ;

CastExpression :
    '(' PrimitiveType ')' UnaryExpression
    { $$ = new CastNode((TypeNode) $2, (ExprNode) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | '(' ReferenceType ')' UnaryExpressionNotPlusMinus
    { $$ = new CastNode((TypeNode) $2, (ExprNode) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  | '(' NameOfTemplate ')' UnaryExpressionNotPlusMinus
    { $$ = new CastNode(new TypeNameNode((NameNode) $2), (ExprNode) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
  ;
/* Note: The last production is redundant, but helps resolve a LALR(1) */
/* lookahead conflict arising in cases like "(T) + x" (Do we reduce Name  */
/* T to TypeName on seeing the ")"?). */

/* Array size expressions */

DimExprs :
    DimExpr
    { $$ = cons($1); }
  | DimExpr DimExprs
    { $$ = cons($1, (List) $2); }
  ;

DimExpr :
  '[' Expression ']'
  { $$ = $2; }
  ;

DimsOpt :
    Dims  { }
  | empty
    { $$ = 0; }
  ;

Dims :
    EMPTY_DIM
    { $$ = 1; }
  | Dims EMPTY_DIM
    { $$ = $1 + 1; }
  ;


/* Constant expressions */

ConstantExpression :
    Expression
    { $$ = $1; }
  ;

/* Allocation expressions */

AllocationExpression :
   NEW TypeName '(' ArgumentListOpt ')'
   { $$ = new AllocateNode((TypeNameNode) $2, (List) $4,
               AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* NEW: Java 1.1 : D.2.1 Anonymous classes */
 | NEW TypeName '(' ArgumentListOpt ')' ClassBody
   { $$ = new AllocateAnonymousClassNode((TypeNameNode) $2,
               (List) $4, (List) $6, AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
 | NEW TypeName DimExprs DimsOpt
   { $$ = new AllocateArrayNode((TypeNode) $2, (List) $3, $4,
               AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* NEW: Java 1.1 : D.2.1 Anonymous arrays */
 | NEW TypeName DimsOpt ArrayInitializer
   { $$ = new AllocateArrayNode((TypeNode) $2, new LinkedList(), $3,
               (TreeNode) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
 | NEW DerivedType DimExprs DimsOpt
   { $$ = new AllocateArrayNode((TypeNode) $2, (List) $3, $4,
               AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
 | NEW PrimitiveType DimExprs DimsOpt
   { $$ = new AllocateArrayNode((TypeNode) $2, (List) $3, $4,
               AbsentTreeNode.instance);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* NEW: Java 1.1 : D.2.1 Anonymous arrays */
 | NEW PrimitiveType DimsOpt ArrayInitializer
   { $$ = new AllocateArrayNode((TypeNode) $2, new LinkedList(), $3,
               (TreeNode) $4);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* NEW: Java 1.1 : qualified class creation */
 | PrimaryExpression '.' NEW IDENTIFIER '(' ArgumentListOpt ')'
   { $$ = new AllocateNode( new TypeNameNode(new NameNode(
               AbsentTreeNode.instance, $4, AbsentTreeNode.instance)),
               (List) $6, (ExprNode) $1);
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* NEW: Java 1.1 : qualified anonymous class creation */
 | PrimaryExpression '.' NEW IDENTIFIER '(' ArgumentListOpt ')' ClassBody
   { $$ = new AllocateAnonymousClassNode(
               new TypeNameNode(new NameNode(AbsentTreeNode.instance,$4,
               AbsentTreeNode.instance)), (List) $6, (List) $8,
               (ExprNode) $1);

      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* Redundant productions to handle Name . new */
   /* NEW: Java 1.1 : qualified class creation */
 | NameOfTemplate '.' NEW IDENTIFIER '(' ArgumentListOpt ')'
   { $$ = new AllocateNode(new TypeNameNode(new NameNode(
               AbsentTreeNode.instance, $4, AbsentTreeNode.instance)),
               (List) $6, new ObjectNode((NameNode) $1));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
   /* NEW: Java 1.1 : qualified anonymous class creation */
 | NameOfTemplate '.' NEW IDENTIFIER '(' ArgumentListOpt ')' ClassBody
   { $$ = new AllocateAnonymousClassNode(new TypeNameNode(
               new NameNode(AbsentTreeNode.instance, $4,
               AbsentTreeNode.instance)), (List) $6, (List) $8, new
               ObjectNode((NameNode) $1));
      ((TreeNode)$$).setProperty(
                _LINENUMBER_KEY,
                new Integer(_lexer.lineNumber())); }
 ;

%%

/** Initialize the lexical analyzer.
 *  @param filename The file to parse, which must be suitable
 *  for FileInputStream(). 
 *  @exception IOException If creating a FileInputStream throws it.
 */
public void init(String filename) throws IOException {
  _filename = filename;
  _lexer = new Lexer(new FileInputStream(_filename));
}

/** Parse the input.
 *  @return 1 if there is a problem, 0 if there is no problem.
 */
public int parse() {
  _numErrors = 0;
  int result = yyparse();
  if (_numErrors > 0)
    throw new RuntimeException("There are syntactic errors, check messages " +
                               "above.");
  return result;
}

/** Get the next token.
 *  @return The next token.
 */
protected int yylex() {
  int _retval;

  try {
    _retval = _lexer.yylex();

    yylval = _lexer.getMetaModelParserval();

  } catch (IOException ex) {
    throw new RuntimeException("lexical error", ex);
  }

  return _retval;
}

/** Create a new list with the obj as the contents of the list.
 * @param obj The object to insert into the list.
 * @return The new list.
 */
protected static final List cons(Object obj) {
  return cons(obj, new LinkedList());
}

/** Insert an object to the head of a list.
 *  The object must be non-null and must not be
 *  an instance of AbsentTreeNode.  If it is null or
 *  an instance of AbsentTreeNode, then the list is unchanged
 *  @param obj The Object to insert.
 *  @param list The list to which the object is inserted.
 *  @return the list
 */
protected static final List cons(Object obj, List list) {
  if ((obj != null) && (obj != AbsentTreeNode.instance)) {
     list.add(0, obj);
  }

  return list;
}

/** Append an object to the end of a preexisting list.
 *  @param obj The Object to append
 *  @param list The list to which the object is appended.
 *  @return the list
 */ 
protected static final List append(List list, Object obj) {
  list.add(obj);
  return list;
}


/** Append list2 on list1.
 *  @param list1 The list that gets appended to.
 *  @param list2 The list that gets appended.
 *  @return list1
 */
protected static final List appendLists(List list1, List list2) {
  list1.addAll(list2);
  return list1;
}

/** Return the qualified name of a node.
 *  @param node The NameNode we are looking up.
 *  @return The qualified name.
 */
protected static final String qualifiedName(NameNode node) {
  String result;
  String ident = node.getIdent();
  TreeNode qualifier = node.getQualifier();
  if (qualifier == AbsentTreeNode.instance) {
    result = ident;
  } else {
    result = qualifiedName((NameNode) qualifier) + "." + ident;
  }
  return result;
}

/** Check that all classes have at least one constructor. If no constructor
 *  is found, create one constructor with no arguments that calls the
 *  default constructor in the superclass.
 *  @param name    The name of the type.
 *  @param members The list of members of the type.
 *  @param hasNameInCntr True if there is no constructor that takes a
 *  name argument.
 */
protected static final void forceOneConstructor(NameNode name,
        List members, boolean hasNameInCntr) {
    Iterator memberItr = members.iterator();
    while (memberItr.hasNext()) {
        Object member = memberItr.next();
        if (member instanceof ConstructorDeclNode) {
            // Constructor found
            return;
        }
    }

    // There is no constructor.
    // We will create a public constructor with no arguments and a call
    // to the superclass constructor, also with no arguments
    if (!hasNameInCntr) {
        try {
            name = (NameNode) name.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone of '"
                    + name
                    + "' not supported.", ex);
        }
        int modifier  = Modifier.PUBLIC_MOD;
        List params   = new LinkedList();
        BlockNode block = new BlockNode(new LinkedList());
        LinkedList par = new LinkedList();
        SuperConstructorCallNode call =
            new SuperConstructorCallNode(par);

        ConstructorDeclNode constr =
            new ConstructorDeclNode(modifier,name,params,block,call);

        // Add the constructor to the body of the list of members
        members.add(constr);
    }

    // If this is for process, medium, scheduler or
    // netlist, we will create another public constructor
    // with an "String name" arguments and a call to the
    // superclass constructor, also with the name arguments.

    if (hasNameInCntr) {
        try {
            name = (NameNode) name.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone of '"
                    + (NameNode)name
                    + "' not supported.", ex);
        }
        int modifier  = Modifier.PUBLIC_MOD;
        List params   = new LinkedList();
        BlockNode block = new BlockNode(new LinkedList());
        LinkedList par = new LinkedList();
        NameNode type = new NameNode(AbsentTreeNode.instance,
                new String("String"), AbsentTreeNode.instance);
        NameNode nm = new NameNode(AbsentTreeNode.instance,
                new String("name"), AbsentTreeNode.instance);
        TypeNameNode typeName = new TypeNameNode(type);
        ParameterNode param = new ParameterNode(0, typeName, nm);
        params.add(param);
        try {
            par.add(new ObjectNode((NameNode)nm.clone()));
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Clone of '"
                    + (NameNode)nm
                    + "' not supported.", ex);
        }
        SuperConstructorCallNode call =
            new SuperConstructorCallNode(par);

        ConstructorDeclNode constr =
            new ConstructorDeclNode(modifier, name, params,
                    block, call);

        // Add the constructor to the body of the list
        // of members

        members.add(constr);
    }
}

/** Store _lexer.lineNumber for later retrieval after returning to the same
 *  level in the parse tree.
 *  @param tag a String to use to verify that your pushes & pops haven't
 *  gotten out of sync.
 */
protected void pushCurrentLineNumber(String tag) {
    _lineNumberStack.push(new Integer(_lexer.lineNumber()));
    _lineNumberTagStack.push(tag);
    // For backwards compatability with parts of this code that use
    // _inLineNumber.
    _inLineNumber = _lexer.lineNumber();
}

/** Pop a line number off the stack (as an Integer), previously stored there
 *  by {@link #pushCurrentLineNumber(String)}.
 *  @param tag a String to use to verify that your pushes & pops haven't
 *  gotten out of sync.
 *  @return The current line number.
 */
protected Integer popCurrentLineNumber(String tag) {
    String check = (String)_lineNumberTagStack.pop();
    if ((check == null && tag != null) ||
        !check.equals(tag)) {
        _lineNumberStack.pop();
        yyerror("Internal grammar error: tag \"" + tag
                + "\" does not equal check string on line-number stack (\""
                + check + "\")");
        return new Integer(0);
    } else {
        return (Integer)_lineNumberStack.pop();
    }
}

/** A separate version of {@link #pushCurrentLineNumber(String)} for
 *  the do-while-loop, which needs not only the line number of the "do"
 *  line, but also that of the "while" test.
 *  @param tag a String to use to verify that your pushes & pops haven't
 *  gotten out of sync.
 */
protected void pushDoLoopTestLineNumber(String tag) {
    _doLoopLineNumberStack.push(new Integer(_lexer.lineNumber()));
    _doLoopLineNumberTagStack.push(tag);
}

/** A separate version of {@link #popCurrentLineNumber(String)} for the
 *  do-while-loop, which needs not only the line number of the "do"
 *  line, but also that of the "while" test.
 *  @param tag a String to use to verify that your pushes & pops haven't
 *  gotten out of sync.
 *  @return The line number off the stack that was previously stored there
 *  by {@link #pushCurrentLineNumber(String)}
 */
protected Integer popDoLoopTestLineNumber(String tag) {
    String check = (String)_doLoopLineNumberTagStack.pop();
    if ((check == null && tag != null) ||
        !check.equals(tag)) {
        _doLoopLineNumberStack.pop();
        yyerror("Internal grammar error: tag \"" + tag
                + "\" does not equal check string on line-number stack (\""
                + check + "\")");
        return new Integer(0);
    } else {
        return (Integer)_doLoopLineNumberStack.pop();
    }
}

/** Store _lexer.lineNumber for later retrieval after returning to the same
 *  level in the parse tree.
 *  @param tag a String to use to verify that your pushes & pops haven't
 *  gotten out of sync.
 */
protected void pushBlockEndLineNumber(String tag) {
    _blockEndLineNumberStack.push(new Integer(_lexer.lineNumber()));
    _blockEndLineNumberTagStack.push(tag);
}

/** Pop a line number off the stack (as an Integer), previously stored there
 *  by {@link #pushBlockEndLineNumber(String)}.
 *  @param tag a String to use to verify that your pushes & pops haven't
 *  gotten out of sync.
 *  @return the line number off the stack.
 */
protected Integer popBlockEndLineNumber(String tag) {
    String check = (String)_blockEndLineNumberTagStack.pop();
    if ((check == null && tag != null) ||
        !check.equals(tag)) {
        _blockEndLineNumberStack.pop();
        yyerror("Internal grammar error: tag \"" + tag
                + "\" does not equal check string on block-end line-number "
                + "stack (\""
                + check + "\")");
        return new Integer(0);
    } else {
        return (Integer)_blockEndLineNumberStack.pop();
    }
}

/** The key indicating the end of a block. */
private static Integer _BLOCK_END_LINENUMBER_KEY =
        MetaModelStaticSemanticConstants.BLOCK_END_LINENUMBER_KEY;

private static Integer _BLOCK_SECOND_LINENUMBER_KEY =
        MetaModelStaticSemanticConstants.BLOCK_SECOND_LINENUMBER_KEY;

/** The key indicating a do loop test. */  
private static Integer _DOLOOP_TEST_LINENUMBER_KEY =
        MetaModelStaticSemanticConstants.DOLOOP_TEST_LINENUMBER_KEY;

/** The key indicating a line number. */
private static Integer _LINENUMBER_KEY =
        MetaModelStaticSemanticConstants.LINENUMBER_KEY;

/** Used to detect whether we are inside a LTL formula or not. */
private boolean _inLTLFormula = false;

/** Used to detect whether we are inside an action formula or not. */
private boolean _inActionFormula = false;

/** Used to detect whether we are inside a netlist or not. */
private boolean _inNetlist = false;

/** Used to detect whether we are inside a constraint block or not. */
private boolean _inConstraint = false;

/** Used to detect whether we are inside a annotation block or not. */
private boolean _inAnnotation = false;

/** Used to detect whether we are inside a top level class or not. */
private boolean _inClass = false;

/** Used to detect whether there are nested expression labels. */
private int _inExprLabel = 0;

/** Used to detect whether we are inside a method or not. */
private boolean _inMethod = false;

/** Used to detect line number information */
private int _inLineNumber = 0;
private int _inLineNumber1 = 0;
private Stack _lineNumberStack    = new Stack();
private Stack _lineNumberTagStack = new Stack();
private Stack _blockEndLineNumberStack    = new Stack();
private Stack _blockEndLineNumberTagStack = new Stack();
private Stack _doLoopLineNumberStack    = new Stack();
private Stack _doLoopLineNumberTagStack = new Stack();

/** Used to detect whether we are inside a Node declaration or not. */
/** Node type includes Process, Medium, StateMedium, Scheduler and Netlist. */
/** _nestLevel=1: in a top level declaration */
/** _nestLevel>1: in an inner class declaration */
private int _nestLevel = 0;

/** Place to put the finished Abstract Syntax Tree (AST). */
protected CompileUnitNode _theAST;

/** Number of syntactic erros in the AST. */
protected int _numErrors;

/** Return the Abstract Syntax Tree (AST).
 *  @return the AST
 */
public CompileUnitNode getAST() { return _theAST; }

/** Handle an error message by incrementing the error count and printing
 *  a message to stdout.
 *  @param msg The error message to print.
 */
protected void yyerror(String msg) {
  _numErrors++;
  String errMsg = _filename;
  String line = "";
  if (_lexer != null) {
     line = ":" + _lexer.lineNumber();
  }
  System.out.println(errMsg + line + ": " +  msg);
}

/** The filename to parse. */
protected String _filename = null;

/** The lexer to use. */
protected Lexer  _lexer = null;
