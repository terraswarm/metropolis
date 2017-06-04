# flags.mk
# $Id: flags.mk,v 1.1 2003/11/06 23:30:24 cxh Exp $
#
# This file is here so that we can test MakefileCodegen.
# MakefileCodegen will read in a Makefile or makefile if it is
# present in the current directory and look for any makefile include
# directives and include them in the output.

MAKEFILE_CODEGEN_MESSAGE="This is the MakefileCodegen message in flags.mk"
testMakefileCodegen:
	@echo $(MAKEFILE_CODEGEN_MESSAGE)


