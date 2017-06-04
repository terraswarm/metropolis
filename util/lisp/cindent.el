;; cindent.el, used by $METRO/util/testsuite/cindent to
;; indent C++ files to the Metropolis
;; Version: $Id: cindent.el,v 1.2 2004/09/30 00:07:46 cxh Exp $

(load (expand-file-name
       (concat (getenv "METRO") "/util/lisp/ptjavastyle.el")))

(defun cindent ()
  (c++-mode)
  (indent-region (point-min) (point-max) 'nil)
  (save-buffer)
)
