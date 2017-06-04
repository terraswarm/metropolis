# Makefile of a source directory of Metropolis 
#
# @Version: $Id: makefile,v 1.13 2005/11/22 18:58:38 allenh Exp $
# @Author: Christopher Brooks (makefile only)
#
# Copyright (c) 2003-2005 The Regents of the University of California.
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

ME =		src/metropolis/metamodel/nodetypes

# Root of the Metropolis directory
ROOT =		../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

# Subdirectories that contain source code
DIRS = 

# Source Java files
JSRCS    =  AbsentTreeNode.java \
	ActionFormulaNode.java \
	ActionLabelExprNode.java \
	ActionLabelStmtNode.java \
	ActionNode.java \
	AddComponentNode.java \
	AllocateAnonymousClassNode.java \
	AllocateArrayNode.java \
	AllocateNode.java \
	AnnotationNode.java \
	ArrayAccessNode.java \
	ArrayInitNode.java \
	ArrayInitTypeNode.java \
	ArrayTypeNode.java \
	AssignNode.java \
	AwaitGuardNode.java \
	AwaitLockNode.java \
	AwaitStatementNode.java \
	BeginAnnotationNode.java \
	BeginEventNode.java \
	BeginPCNode.java \
	BinaryArithAssignNode.java \
	BinaryArithNode.java \
	BinaryOpAssignNode.java \
	BinaryOpNode.java \
	BitAndAssignNode.java \
	BitAndNode.java \
	BitOrAssignNode.java \
	BitOrNode.java \
	BitXorAssignNode.java \
	BitXorNode.java \
	BitwiseAssignNode.java \
	BitwiseNode.java \
	BlackboxNode.java \
	BlockNode.java \
	BoolLitNode.java \
	BoolTypeNode.java \
	BoundedLoopNode.java \
	BreakNode.java \
	BuiltInActionNode.java \
	BuiltInLTLNode.java \
	ByteTypeNode.java \
	CandNode.java \
	CaseNode.java \
	CastNode.java \
	CharLitNode.java \
	CharTypeNode.java \
	CifNode.java \
	CiffNode.java \
	ClassDeclNode.java \
	CompileUnitNode.java \
	ComplementNode.java \
	ConnectNode.java \
	ConstraintBlockNode.java \
	ConstraintDeclNode.java \
	ConstraintStatementNode.java \
	ConstructorCallNode.java \
	ConstructorDeclNode.java \
	ContinueNode.java \
	CorNode.java \
	DeclaratorNode.java \
	DivAssignNode.java \
	DivNode.java \
	DoubleExprNode.java \
	DoubleLTLFormulaNode.java \
	DoubleLitNode.java \
	DoubleTypeNode.java \
	ELOCConstraintCallNode.java \
	ELOCConstraintDeclNode.java \
	ELOCConstraintNode.java \
	EQNode.java \
	EmptyStmtNode.java \
	EndAnnotationNode.java \
	EndEventNode.java \
	EndPCNode.java \
	EqualVarsNode.java \
	EqualityNode.java \
	EventNode.java \
	EventTypeNode.java \
	ExclLTLNode.java \
	ExecIndexNode.java \
	ExistsActionNode.java \
	ExprActionNode.java \
	ExprLTLNode.java \
	ExprNode.java \
	ExprStmtNode.java \
	FieldAccessNode.java \
	FieldDeclNode.java \
	FloatLitNode.java \
	FloatTypeNode.java \
	ForNode.java \
	ForallActionNode.java \
	FormulaNode.java \
	FutureLTLNode.java \
	GENode.java \
	GTNode.java \
	GenericNameNode.java \
	GetCompNameNode.java \
	GetComponentNode.java \
	GetConnectionDestNode.java \
	GetConnectionNumNode.java \
	GetInstNameNode.java \
	GetNthConnectionPortNode.java \
	GetNthConnectionSrcNode.java \
	GetNthPortNode.java \
	GetPortNumNode.java \
	GetProcessNode.java \
	GetScopeNode.java \
	GetThreadNode.java \
	GetTypeNode.java \
	GlobalLabelNode.java \
	GloballyLTLNode.java \
	IfExprNode.java \
	IfStmtNode.java \
	ImplyNode.java \
	ImportNode.java \
	ImportOnDemandNode.java \
	IncrDecrNode.java \
	InstanceOfNode.java \
	IntLitNode.java \
	IntTypeNode.java \
	InterfaceDeclNode.java \
	InvokableDeclNode.java \
	IsConnectionRefinedNode.java \
	IterationNode.java \
	JumpStmtNode.java \
	LENode.java \
	LOCConstraintCallNode.java \
	LOCConstraintDeclNode.java \
	LOCConstraintNode.java \
	LTLConstraintCallNode.java \
	LTLConstraintDeclNode.java \
	LTLConstraintNode.java \
	LTLFormulaNode.java \
	LTLSynchNode.java \
	LTNode.java \
	LabelNode.java \
	LabelPCExprNode.java \
	LabeledBlockNode.java \
	LabeledStmtNode.java \
	LeftShiftLogAssignNode.java \
	LeftShiftLogNode.java \
	LiteralNode.java \
	LocalLabelNode.java \
	LocalVarDeclNode.java \
	LogCondNode.java \
	LongLitNode.java \
	LongTypeNode.java \
	LoopNode.java \
	MaxDeltaNode.java \
	MaxRateNode.java \
	MediumDeclNode.java \
	MetaModelNode.java \
	MethodCallNode.java \
	MethodDeclNode.java \
	MinDeltaNode.java \
	MinRateNode.java \
	MinusAssignNode.java \
	MinusNode.java \
	ModifiedNode.java \
	MultAssignNode.java \
	MultNode.java \
	MutexLTLNode.java \
	NENode.java \
	NameNode.java \
	NamedNode.java \
	NetlistDeclNode.java \
	NetworkNodeDeclNode.java \
	NextLTLNode.java \
	NodeClassID.java \
	NonDeterminismNode.java \
	NoneEventNode.java \
	NotNode.java \
	NullPntrNode.java \
	NullTypeNode.java \
	ObjectFieldAccessNode.java \
	ObjectNode.java \
	ObjectParamAccessNode.java \
	ObjectPortAccessNode.java \
	OtherEventNode.java \
	OuterClassAccessNode.java \
	OuterSuperAccessNode.java \
	OuterThisAccessNode.java \
	PCExprNode.java \
	PCNode.java \
	PCTypeNode.java \
	ParamAccessNode.java \
	ParameterDeclNode.java \
	ParameterNode.java \
	PeriodNode.java \
	PlusAssignNode.java \
	PlusNode.java \
	PortAccessNode.java \
	PortDeclNode.java \
	PostDecrNode.java \
	PostIncrNode.java \
	PreDecrNode.java \
	PreIncrNode.java \
	PrimitiveTypeNode.java \
	PriorityLTLNode.java \
	ProcessDeclNode.java \
	QuantifiedActionNode.java \
	QuantityDeclNode.java \
	RedirectConnectNode.java \
	ReferenceTypeNode.java \
	RefineConnectNode.java \
	RefineNode.java \
	RelationNode.java \
	RemAssignNode.java \
	RemNode.java \
	ReturnNode.java \
	RightShiftArithAssignNode.java \
	RightShiftArithNode.java \
	RightShiftLogAssignNode.java \
	RightShiftLogNode.java \
	SMDeclNode.java \
	SchedulerDeclNode.java \
	SetScopeNode.java \
	ShiftAssignNode.java \
	ShiftNode.java \
	ShortTypeNode.java \
	SimulLTLNode.java \
	SingleExprNode.java \
	SingleLTLFormulaNode.java \
	SingleOpNode.java \
	SpecialLitNode.java \
	StatementNode.java \
	StringLitNode.java \
	SuperConstructorCallNode.java \
	SuperFieldAccessNode.java \
	SuperParamAccessNode.java \
	SuperPortAccessNode.java \
	SwitchBranchNode.java \
	SwitchNode.java \
	TemplateParametersNode.java \
	TemplateTypeNode.java \
	ThisConstructorCallNode.java \
	ThisFieldAccessNode.java \
	ThisNode.java \
	ThisParamAccessNode.java \
	ThisPortAccessNode.java \
	TypeClassAccessNode.java \
	TypeFieldAccessNode.java \
	TypeNameNode.java \
	TypeNode.java \
	UnaryArithNode.java \
	UnaryMinusNode.java \
	UnaryPlusNode.java \
	UntilLTLNode.java \
	UserTypeDeclNode.java \
	UserTypeDeclStmtNode.java \
	VarDeclNode.java \
	VarInEventRefNode.java \
	VarInitDeclNode.java \
	VoidTypeNode.java

# Extra rules that should be generated
EXTRA_TARGETS = 

JCLASS = $(JSRCS:%.java=%.class)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	package.html

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

# nodetypes/.lastgen needs to made before going in to frontend
all: jclass $(EXTRA_TARGETS) suball $(EXTRA_SRCS) 

install: subinstall

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
