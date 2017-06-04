# Makefile of a source directory of Metropolis 
#
# @Version: $Id: makefile,v 1.23 2005/10/31 22:41:27 allenh Exp $
# @Author: Christopher Brooks (makefile only)
#
# Copyright (c) 2003-2004 The Regents of the University of California.
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
#
# 						METROPOLIS_COPYRIGHT_VERSION_1
# 						COPYRIGHTENDKEY
#
##########################################################################

ME =		src/metropolis/metamodel

# Root of the Metropolis directory
ROOT =		../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
# PTJACL_DIR is set to shell by configure in metro.mk if ptjacl is found.
DIRS = frontend backends nodetypes runtime $(PTJACL_DIR) test

# Source Java files
JSRCS = \
	ChildList.java \
	Compiler.java \
	Decl.java \
	Effect.java \
	GenerateVisitor.java \
	ITreeNode.java \
	IVisitor.java \
	MetaModelStaticSemanticConstants.java \
	MetaModelVisitor.java \
        Modifier.java \
	NullValue.java \
	PropertyMap.java \
	Scope.java \
	ScopeIterator.java \
	StringManip.java \
	TNLManip.java \
	TrackedPropertyMap.java \
	TreeNode.java \
	TypeUtility.java

# Extra rules that should be generated
EXTRA_TARGETS =  nodetypes/.lastgen

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	BUGS.txt \
	NodeTypes.def \
	README.txt \
	README_DEV.txt \
	SCOPE_HIERARCHY.pdf \
	VISITORS.txt \
	package.html

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'GenerateVisitor$$ClassField.class' \
	'GenerateVisitor$$MethodSignature.class'

# nodetypes/.lastgen needs to made before going in to frontend
all: $(EXTRA_TARGETS) $(EXTRA_SRCS) jclass suball 

install: all subinstall

# .*.ast files that depend on NullValue.class
ASTS_THAT_DEPEND_ON_NULL_VALUE = \
	$(ROOT)/lib/metamodel/lang/.GlobalTime.ast \
	$(ROOT)/lib/metamodel/lang/.Process.ast \
	$(ROOT)/lib/metamodel/lang/.Quantity.ast \
	$(ROOT)/lib/metamodel/lang/.SchedulingNetlist.ast \
	$(ROOT)/lib/metamodel/lang/.String.ast \
	$(ROOT)/lib/metamodel/plt/yapi/.yapi.ast \
	$(ROOT)/lib/metamodel/plt/yapi/.yapicosim.ast

NullValue.class: NullValue.java
	rm -f `basename $< .java`.class
	CLASSPATH="$(CLASSPATH)" "$(JAVAC)" $(JAVA_FLAGS) $<
	@echo "NullValue.class changed, so we need to remove certain"
	@echo "  .*.ast files in \$METRO/lib/metamodel/lang"
	rm -f $(ASTS_THAT_DEPEND_ON_NULL_VALUE)

nodetypes/.lastgen MetaModelVisitor.java: NodeTypes.def GenerateVisitor.java
	@echo "Generate the MetaModelVistor.java file"	
	${MAKE} GenerateVisitor.class
	"$(JAVA)" -classpath "$(CLASSPATH)" \
		metropolis.metamodel.GenerateVisitor \
		NodeTypes.def MetaModelVisitor TreeNode nodetypes
	pwd
	ls -l
	if [ -d jsoriginal ]; then \
		echo "# jsoriginal/ dir found, about to run:"; \
		echo "# $(JSINSTR) $(JSINSTRFLAGS) MetaModelVisitor.java"; \
		$(JSINSTR) $(JSINSTRFLAGS) MetaModelVisitor.java; \
	fi
	date > nodetypes/.lastgen

# "make clean" removes files and directories named in $(KRUFT)
KRUFT = $(ASTS_THAT_DEPEND_ON_NULL_VALUE)

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
