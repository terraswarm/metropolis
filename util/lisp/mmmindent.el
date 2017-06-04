;; mmmindent.el, used by $METRO/util/testsuite/mmmindent to
;; indent Metropolis Meta Model files to the Metropolis style
;; Version: $Id: mmmindent.el,v 1.2 2004/09/30 23:09:21 cxh Exp $

(load (expand-file-name
       (concat (getenv "METRO") "/util/lisp/ptjavastyle.el")))
(load (expand-file-name
       (concat (getenv "METRO") "/util/lisp/mmm-mode.el")))

(setq auto-mode-alist (cons '("\\.mmm$" . mmm-mode) auto-mode-alist))

(defun mmmindent ()
  (interactive)
  (mmm-mode)
  (indent-region (point-min) (point-max) 'nil)
  (save-buffer)
)
