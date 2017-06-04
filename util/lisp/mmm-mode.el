`;; Metropolis Meta Model coding Style
;; $Id: mmm-mode.el,v 1.8 2004/09/30 22:30:00 cxh Exp $

;; To use this file, uncomment the following block
;;  and place into your $HOME/.emacs file
;;(setq load-path (append
;;                 (list
;;                  (expand-file-name 
;;		   (concat (getenv "METRO") "/util/lisp")))
;;                 load-path
;;                 ))
;;(autoload 'mmm-mode "mmm-mode" "Metropolis Meta Model (.mmm) mode" t)
;;(setq auto-mode-alist (cons '("\\.mmm$" . mmm-mode) auto-mode-alist))


;; Shamelessly copied from Emacs cc-mode.el cc-lang.el and others, so
;; we include:

;; GNU Emacs is free software; you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 2, or (at your option)
;; any later version.

;; GNU Emacs is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with this program; see the file COPYING.  If not, write to
;; the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
;; Boston, MA 02111-1307, USA.

;; One way to test this file is to do M-x load-file;
;; then change the mode of a .mmm file to java with M-x java-mode;
;; then change the mode of the .mmm file to mmm with M-x mmm-mode.
;; If there are errors, you will see them in the status line.

;; FIXME: we should conditionally load this
(load-file (expand-file-name (concat 
			      (getenv "METRO") 
			      "/util/lisp/ptjavastyle.el")))


(load-library "cc-mode")
;;(autoload 'mmm-mode  "cc-mode" "C++ Editing Mode" t)

;;; Helpers for building regexps.
(defmacro c-identifier-re (re)
  `(concat "\\<\\(" ,re "\\)\\>[^_]"))

(defun c-make-inherited-keymap ()
  (let ((map (make-sparse-keymap)))
    (cond
     ;; XEmacs 19 & 20
     ((fboundp 'set-keymap-parents)
      (set-keymap-parents map c-mode-base-map))
     ;; Emacs 19
     ((fboundp 'set-keymap-parent)
      (set-keymap-parent map c-mode-base-map))
     ;; incompatible
     (t (error "CC Mode is incompatible with this version of Emacs")))
    map))

;; Menu setup
(defvar c-mmm-menu nil)
(defvar mmm-mode-abbrev-table nil
  "Abbreviation table used in mmm-mode buffers.")
(define-abbrev-table 'mmm-mode-abbrev-table
  '(("else" "else" c-electric-continued-statement 0)
    ("while" "while" c-electric-continued-statement 0)
    ("catch" "catch" c-electric-continued-statement 0)
    ("finally" "finally" c-electric-continued-statement 0)))

(defvar mmm-mode-map ()
  "Keymap used in mmm-mode buffers.")
(if mmm-mode-map
    nil
  (setq mmm-mode-map (c-make-inherited-keymap))
  ;; add bindings which are only useful for Mmm
  )

(easy-menu-define c-mmm-menu mmm-mode-map "Mmm Mode Commands"
		  (c-mode-menu "Mmm"))

;; Define mmm-mode.
(defun mmm-mode ()
  "Major mode for editing Metropolis Meta Model (MMM) code.
To submit a problem report, enter `\\[c-submit-bug-report]' from a
mmm-mode buffer.  This automatically sets up a mail buffer with
version information already added.  You just need to add a description
of the problem, including a reproducible test case and send the
message.

To see what version of CC Mode you are running, enter `\\[c-version]'.

The hook variable `mmm-mode-hook' is run with no args, if that value
is bound and has a non-nil value.  Also the common hook
`c-mode-common-hook' is run first.  Note that this mode automatically
sets the \"mmm\" style before calling any hooks so be careful if you
set styles in `c-mode-common-hook'.

Key bindings:
\\{mmm-mode-map}"
  (interactive)
  (c-initialize-cc-mode)
  (kill-all-local-variables)
  (set-syntax-table mmm-mode-syntax-table)
  (setq major-mode 'mmm-mode
 	mode-name "Mmm"
 	local-abbrev-table mmm-mode-abbrev-table
	abbrev-mode t
	c-append-paragraph-start c-Mmm-mmmdoc-paragraph-start)
  (use-local-map mmm-mode-map)
  (c-common-init)
  (setq comment-start "// "
 	comment-end   ""
	c-keywords (c-identifier-re c-Mmm-keywords)
 	c-conditional-key c-Mmm-conditional-key
 	c-comment-start-regexp c-Mmm-comment-start-regexp
  	c-class-key c-Mmm-class-key
	c-method-key nil
 	c-baseclass-key nil
	c-recognize-knr-p nil
	c-inexpr-class-key c-Mmm-inexpr-class-key
	;defun-prompt-regexp c-Mmm-defun-prompt-regexp
	c-special-brace-lists c-Mmm-special-brace-lists
	)
  (cc-imenu-init cc-imenu-mmm-generic-expression)
  (run-hooks 'c-mode-common-hook)
  (run-hooks 'mmm-mode-hook)
  (c-update-modeline))

;; FIXME: most of mmm-mode is an extension of the corresponding
;; java mode constants defined in lisp/progmodes/cc-langs.el.
;; Ideally, we would use those constants here, but we don't seem
;; to have access.

(defconst c-Mmm-primitive-type-kwds
  "boolean\\|byte\\|char\\|double\\|float\\|int\\|long\\|short\\|void")
(defconst c-Mmm-specifier-kwds
  ;; Note: `const' is not used, but it's still a reserved keyword.
;; MMM_ADDITION: eval update
  (concat "abstract\\|const\\|final\\|native\\|private\\|protected\\|"
	  "public\\|static\\|synchronized\\|transient\\|volatile\\|"
	  "eval\\|update"))

;; Class/struct declaration keywords.
;; MMM_ADDITION: Added netlist
(defconst c-Mmm-class-kwds "class\\|interface\\|netlist")

;; Keywords introducing other declaration-level blocks.
(defconst c-Mmm-other-decl-kwds "import\\|package")

;; Keywords that occur in declaration-level constructs.
(defconst c-Mmm-decl-level-kwds "extends\\|implements\\|throws")

;; Statement keywords followed directly by a block.
;; MMM_ADDITION: Added await
(defconst c-Mmm-block-stmt-1-kwds "do\\|else\\|finally\\|try\\|await")

;; Statement keywords followed by a paren sexp and then by a block.
;; MMM_ADDITION: Added blackbox
(defconst c-Mmm-block-stmt-2-kwds 
    "for\\|if\\|switch\\|while\\|catch\\|synchronized\\|blackbox")

;; Keywords defining protection levels
(defconst c-protection-key "\\<\\(public\\|protected\\|private\\)\\>")

;; Regex describing a `symbol' in all languages.  We cannot use just
;; `word' syntax class since `_' cannot be in word class.  Putting
;; underscore in word class breaks forward word movement behavior that
;; users are familiar with.  Besides, this runs counter to Emacs
;; convention.
;;
;; I suspect this definition isn't correct in light of Java's
;; definition of a symbol as being Unicode.  I know so little about
;; I18N (except how to sound cool and say I18N :-) that I'm willing to
;; punt on this for now.
(defconst c-symbol-key "[_a-zA-Z]\\(\\w\\|\\s_\\)*")

(defconst c-Mmm-class-key
  (concat
   "\\(" c-protection-key "\\s +\\)?"
   "\\(" c-Mmm-class-kwds "\\)\\s +"
   c-symbol-key				      ;name of the class
   "\\(\\s *extends\\s *" c-symbol-key "\\)?" ;maybe followed by superclass
   ;;"\\(\\s *implements *[^{]+{\\)?"	      ;maybe the adopted protocols list
   ))


;; Statement keywords followed by an expression or nothing.
(defconst c-Mmm-simple-stmt-kwds "break\\|continue\\|goto\\|return\\|throws")

;; Keywords introducing labels in blocks.
(defconst c-Mmm-label-kwds "case\\|default")

;; Keywords that can occur anywhere in expressions.
(defconst c-Mmm-expr-kwds "instanceof\\|new\\|super\\|this")

;; All keywords.
(defconst c-Mmm-keywords
  (concat c-Mmm-primitive-type-kwds "\\|" c-Mmm-specifier-kwds
	  "\\|" c-Mmm-class-kwds
	  ;; "\\|" c-Mmm-extra-toplevel-kwds
	  "\\|" c-Mmm-other-decl-kwds "\\|" c-Mmm-decl-level-kwds
	  ;; "\\|" c-Mmm-protection-kwds
	  "\\|" c-Mmm-block-stmt-1-kwds "\\|" c-Mmm-block-stmt-2-kwds
	  "\\|" c-Mmm-simple-stmt-kwds "\\|" c-Mmm-label-kwds
	  "\\|" c-Mmm-expr-kwds))

;; keywords introducing conditional blocks
(let ((all-kws "for\\|if\\|do\\|else\\|while\\|switch")
      (exc-kws "\\|try\\|catch")
      (thr-kws "\\|finally\\|synchronized")
      (front   "\\<\\(")
      (back    "\\)\\>[^_]"))
  (setq c-Mmm-conditional-key (concat front all-kws exc-kws thr-kws back)))

;; Regexp that may be followed by an anonymous class in expressions.
(defconst c-Mmm-inexpr-class-key "\\<new\\>")

;; comment starter definitions for various languages.
;; We need to match all 3 Java style comments
;; 1) Traditional C block; 2) javadoc /** ...; 3) C++ style
(defconst c-Mmm-comment-start-regexp "/\\(/\\|[*][*]?\\)")

;; Regexp to append to paragraph-start.
(defconst c-Mmm-mmmdoc-paragraph-start
  "\\(@[a-zA-Z]+\\>\\|$\\)")

;; List of open- and close-chars that makes up a pike-style brace
;; list, ie for a `([ ])' list there should be a cons (?\[ . ?\]) in
;; this list.
(defconst c-Mmm-special-brace-lists '((?{ . ?})
				      (?% . ?%)))

(defvar mmm-mode-syntax-table nil
  "Syntax table used in mmm-mode buffers.")
(if mmm-mode-syntax-table
    ()
  (setq mmm-mode-syntax-table (make-syntax-table))
  (c-populate-syntax-table mmm-mode-syntax-table))


(defun cc-imenu-init (mode-generic-expression)
  (setq imenu-generic-expression mode-generic-expression
	imenu-case-fold-search nil))

(defvar cc-imenu-mmm-generic-expression
  `((nil
     ,(concat
       "^\\([ \t]\\)*"
       "\\([.A-Za-z0-9_-]+[ \t]+\\)?"	      ; type specs; there can be
       "\\([.A-Za-z0-9_-]+[ \t]+\\)?"	      ; more than 3 tokens, right?
       "\\([.A-Za-z0-9_-]+[ \t]*[[]?[]]?\\)"
       "\\([ \t]\\)"
       "\\([A-Za-z0-9_-]+\\)"		      ; the string we want to get
       "\\([ \t]*\\)+("
       "[][a-zA-Z,_1-9\n \t]*"		      ; arguments
       ")[ \t]*"
;       "[^;(]"
       "[,a-zA-Z_1-9\n \t]*{"               
       ) 6))
  "Imenu generic expression for Java mode.  See `imenu-generic-expression'.")


(provide 'mmm-mode)

