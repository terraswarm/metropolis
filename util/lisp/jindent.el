;; jindent.el, used by $METRO/util/testsuite/jindent to
;; indent Java files to the Ptolemy II standard
;; Version: $Id: jindent.el,v 1.2 2004/05/18 17:49:19 cxh Exp $

(load (expand-file-name
       (concat (getenv "METRO") "/util/lisp/ptjavastyle.el")))

(defun jindent ()
  (java-mode)
  (indent-region (point-min) (point-max) 'nil)
  (save-buffer)
)
