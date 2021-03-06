# Makefile for frontend directory
#
# @Version: $Id: makefile,v 1.19 2005/10/03 22:57:20 allenh Exp $
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2003-2004 The Regents of the University of California.
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
# 						METRO_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################


ME =		src/metropolis/metamodel/frontend/

# Root of the Metropolis directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
# lexer must be compiled first, parser/MetaModelParser.java depends on Lexer
DIRS =  lexer parser test

# Source Java files
JSRCS = \
	ASTCache.java \
	SyntaxChecker.java	\
	ClassDecl.java \
	ExprUtility.java \
	FieldDecl.java \
	FileLoader.java \
	FileRepository.java \
	FormalParameterDecl.java \
	InstantiateTemplateDeclVisitor.java \
	InstantiateTemplateUseVisitor.java \
	InterfaceDecl.java \
	LocalVarDecl.java \
	MediumDecl.java \
	MemberDecl.java \
	MetaModelDecl.java \
	MetaModelLibrary.java \
	MethodDecl.java \
	NetlistDecl.java \
	ObjectDecl.java \
	PackageDecl.java \
	PackageResolutionVisitor.java \
	ParameterDecl.java \
	PortDecl.java \
	ProcessDecl.java \
        RepeatInitLibrariesException.java \
	ReplacementVisitor.java \
	Repository.java \
	ResolutionVisitor.java \
	ResolvePackageVisitor.java \
	ResolveImportsVisitor.java \
	SchedulerDecl.java \
	SourceFileNameVisitor.java \
	StateMediumDecl.java \
	StmtLblDecl.java \
	SyntaxRepository.java \
	TemplateChecksVisitor.java \
	TemplateDeclarationVisitor.java \
	TemplateEliminationVisitor.java \
	TemplateHandler.java \
	TemplateInstanceVisitor.java \
	TypeDecl.java \
	TypeIdentifier.java \
	TypeParameterDecl.java \
	TypePolicy.java \
	TypeVisitor.java \
	TypedDecl.java \
	PackageResolutionVisitor.java \
	QuantityDecl.java \
	Repository.java \
	ResolveClassVisitor.java \
	ResolveFieldVisitor.java \
	ResolveInheritanceVisitor.java \
	ResolveNameVisitor.java \
	ResolvePackageVisitor.java \
	ResolveImportsVisitor.java \
	ResolveTypesVisitor.java

# Extra rules that should be generated
EXTRA_TARGETS =  

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	package.html

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'ResolveFieldVisitor$$FieldContext.class' \
	'ResolveNameVisitor$$NameContext.class'

all: jclass suball $(EXTRA_SRCS) $(EXTRA_TARGETS)

install: subinstall

SyntaxChecker.class: SyntaxChecker.java parser/Grammar.y
	cd parser; make MetaModelParser.java

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
