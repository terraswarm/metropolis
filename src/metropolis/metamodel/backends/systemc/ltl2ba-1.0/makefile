# LTL2BA - Version 1.0 - October 2001

# Written by Denis Oddoux, LIAFA, France                                 

# This program is free software; you can redistribute it and/or modify   
# it under the terms of the GNU General Public License as published by   
# the Free Software Foundation; either version 2 of the License, or      
# (at your option) any later version.                                    
#                                                                        
# This program is distributed in the hope that it will be useful,        
# but WITHOUT ANY WARRANTY; without even the implied warranty of         
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          
# GNU General Public License for more details.                           
#                                                                        
# You should have received a copy of the GNU General Public License      
# along with this program; if not, write to the Free Software            
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
#                                                                        
# Based on the translation algorithm by Gastin and Oddoux,               
# presented at the CAV Conference, held in 2001, Paris, France 2001.     
# Send bug-reports and/or questions to: Denis.Oddoux@liafa.jussieu.fr    
# or to Denis Oddoux                                                     
#       LIAFA, UMR 7089, case 7014                                       
#       Universite Paris 7                                               
#       2, place Jussieu                                                 
#       F-75251 Paris Cedex 05                                          
#       FRANCE                                                               

ME =		src/metropolis/metamodel/backends/systemc/ltl2ba-1.0

# Root of the Metropolis directory
ROOT =		../../../../../..

# Get configuration info
CONFIG =	$(ROOT)/mk/metro.mk
include $(CONFIG)

#CFLAGS= -O3 -ansi -DNXT
#CFLAGS= -g -DNXT
# -DNXT is necessary for compiling under Cygwin?
CXX_MAKEFILE_FLAGS=-DNXT

LIB_SRCS = \
	alternating.cpp \
	buchi.cpp \
	cache.cpp \
	generalized.cpp \
	lex.cpp \
	main.cpp \
	mem.cpp \
	parse.cpp \
	rewrt.cpp \
	set.cpp \
	trans.cpp


LIB_OBJS = $(LIB_SRCS:.cpp=.o)

# 'make fast' builds $(METRO_LIBS)
METRO_LIBS = ltl2ba.a

CCSRCS = $(LIB_SRCS)

SRCS = $(CCSRCS)	

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = $(DIRS) \
	LICENSE \
	README.txt \
	ltl2ba.dsp \
	ltl2ba.h

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

all: $(METRO_LIBS)
install: all
jclass: all

ltl2ba.a:	$(LIB_OBJS)
	$(METRO_AR) -rs libltl2ba.a $(LIB_OBJS)
	#$(CC) $(C_FLAGS) -o ltl2ba $(LIB_OBJS)


$(LIB_OBJS): ltl2ba.h

# Get the rest of the rules
include $(ROOT)/mk/metrocommon.mk
