" Vim syntax file
" Language:	Metropolis MetaModel (mmm)
" Maintainer:	Doug Densmore <densmore@eecs.berkeley.edu>
" Last change:	2002 Oct 11

" Built on top of the java syntax highlighter
" For version 5.x: Clear all syntax items
" For version 6.x: Quit when a syntax file was already loaded
if version < 600
  syntax clear
elseif exists("b:current_syntax")
  finish
endif

" Read the java syntax to start with
if version < 600
  so <sfile>:p:h/java.vim
else
  runtime! syntax/java.vim
  unlet b:current_syntax
endif

" MMM extentions
syn keyword mmmObject		process medium scheduler statemedium 
" syn keyword mmmIntfc		interface extends implements
syn keyword mmmIntfcFunc	eval update constant
syn keyword mmmPort		port useport
syn keyword mmmPara		parameter
syn keyword mmmNet		netlist scope
syn keyword mmmList		addcomponent connect setscope
syn keyword mmmRefine		refine refineconnect
syn keyword mmmThread		thread
syn keyword mmmBlkBx		blackbox %
syn keyword mmmSysC		SystemCSim
syn keyword mmmAwait		await all
syn keyword mmmNonD		nondeterminism

syn match mmmBox "%<>%?"


" Default highlighting
if version >= 508 || !exists("did_java_syn_inits")
  if version < 508
    let did_java_syn_inits = 1
    command -nargs=+ HiLink hi link <args>
  else
    command -nargs=+ HiLink hi def link <args>
  endif
  HiLink mmmObject		Structure
"  HiLink mmmIntfc		Operator
  HiLink mmmIntfcFunc		Constant
  HiLink mmmPort		Type
  HiLink mmmNet			Statement
  HiLink mmmList		Function
  HiLink mmmRefine		Function
  HiLink mmmThread		Typedef
  HiLink mmmBlkBx		Function
  HiLink mmmSysC	        Type	
  HiLink mmmAwait		Conditional
  HiLink mmmNonD		Function
  HiLink mmmPara		Type
  delcommand HiLink
endif

let b:current_syntax = "mmm"

" vim: ts=8
