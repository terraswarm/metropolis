# Tests for the MetamodelParser.tcl
#
# @Author: Christopher Brooks
#
# @Version: $Id: MetaModelParser.tcl,v 1.4 2005/11/22 20:12:07 allenh Exp $
#
# @Copyright (c) 2004-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software and that appropriate acknowledgments are made
# to the research of the Metropolis group.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}


# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
test MetaModelParser-1.1 {Parse the producers_consumer Reader} {
    set parser [java::new metropolis.metamodel.frontend.parser.MetaModelParser]
    $parser init [file join $METRO examples producers_consumer Reader.mmm]
    $parser parse
    set ast [$parser getAST]
    equalsOneOf [list [$ast toString]] \
	[list \
{{ {CompileUnitNode { 
  {Pkg {NameNode { 
        {Ident producers_consumer} 
        {Qualifier {AbsentTreeNode {leaf}}} 
        {Parameters {AbsentTreeNode {leaf}}} 
      }}} 
  {Imports  {}} 
  {DefTypes { 
   {InterfaceDeclNode { 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident IntReader} 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Parameters {AbsentTreeNode {leaf}}} 
         }}} 
    {Interfaces { 
     {TypeNameNode { 
      {Name {NameNode { 
             {Ident Port} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
    }}}} 
    {Members { 
     {MethodDeclNode { 
      {ReturnType {IntTypeNode {leaf}}} 
      {Effect 128} 
      {Body {AbsentTreeNode {leaf}}} 
      {UsePorts  {}} 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident readInt} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
    }}     {MethodDeclNode { 
      {ReturnType {IntTypeNode {leaf}}} 
      {Effect 64} 
      {Body {AbsentTreeNode {leaf}}} 
      {UsePorts  {}} 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident num} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
    }}}} 
    {ParTypeNames  {}} 
  }}}} 
}}}} \
    {{ {CompileUnitNode { 
  {Pkg {NameNode { 
        {Ident producers_consumer} 
        {Qualifier {AbsentTreeNode {leaf}}} 
        {Parameters {AbsentTreeNode {leaf}}} 
      }}} 
  {Imports  {}} 
  {DefTypes { 
   {InterfaceDeclNode { 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident IntReader} 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Parameters {AbsentTreeNode {leaf}}} 
         }}} 
    {Interfaces { 
     {TypeNameNode { 
      {Name {NameNode { 
             {Ident Port} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
    }}}} 
    {Members { 
     {MethodDeclNode { 
      {ReturnType {IntTypeNode {leaf}}} 
      {Effect 256} 
      {Body {AbsentTreeNode {leaf}}} 
      {UsePorts  {}} 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident readInt} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
    }}     {MethodDeclNode { 
      {ReturnType {IntTypeNode {leaf}}} 
      {Effect 128} 
      {Body {AbsentTreeNode {leaf}}} 
      {UsePorts  {}} 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident num} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
    }}}} 
    {ParTypeNames  {}} 
  }}}} 
}}}} \
    {{ {CompileUnitNode { 
  {Pkg {NameNode { 
        {Ident producers_consumer} 
        {Qualifier {AbsentTreeNode {leaf}}} 
        {Parameters {AbsentTreeNode {leaf}}} 
      }}} 
  {Imports  {}} 
  {DefTypes { 
   {InterfaceDeclNode { 
    {Members { 
     {MethodDeclNode { 
      {Effect 256} 
      {UsePorts  {}} 
      {ReturnType {IntTypeNode {leaf}}} 
      {Body {AbsentTreeNode {leaf}}} 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident readInt} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
    }}     {MethodDeclNode { 
      {Effect 128} 
      {UsePorts  {}} 
      {ReturnType {IntTypeNode {leaf}}} 
      {Body {AbsentTreeNode {leaf}}} 
      {Modifiers 0} 
      {Name {NameNode { 
             {Ident num} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
      {Params  {}} 
    }}}} 
    {ParTypeNames  {}} 
    {Modifiers 0} 
    {Name {NameNode { 
           {Ident IntReader} 
           {Qualifier {AbsentTreeNode {leaf}}} 
           {Parameters {AbsentTreeNode {leaf}}} 
         }}} 
    {Interfaces { 
     {TypeNameNode { 
      {Name {NameNode { 
             {Ident Port} 
             {Qualifier {AbsentTreeNode {leaf}}} 
             {Parameters {AbsentTreeNode {leaf}}} 
           }}} 
    }}}} 
  }}}} 
}}}}]

} {1}

######################################################################
####
test MetaModelParser-2.1 {Parse a bogus non mmm file } {
    set parser [java::new metropolis.metamodel.frontend.parser.MetaModelParser]
    $parser init makefile
    catch {$parser parse} errMsg
    list $errMsg	
} {{java.lang.RuntimeException: Lexical error : Line 1 Unmatched input: #}}


######################################################################
####
test MetaModelParser-2.2 {Parse a bogus non mmm file: .class file } {
    set parser [java::new metropolis.metamodel.frontend.parser.MetaModelParser]
    $parser init ../MetaModelParser.class
    catch {$parser parse} errMsg
    list $errMsg	
} {{java.lang.ArrayIndexOutOfBoundsException: 65533}} {Poor error message from Lexer}
