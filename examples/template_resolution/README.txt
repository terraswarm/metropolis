README.txt - Template Resolution 

1. OVERVIEW:

The template resolution is extended from the existing template
resolution place holder written by Robert Clariso in 2001. All
provided files work with the Metropolis frontend, and need only be
included in the frontend makefile for correct compilation. To allow
Metropolis to implement template resolution, an updated
"FileLoader.java" file has also been provided. This file is nearly
identical to the original, with the small change that the
_resolvePass2() function is now called, and a call to the
TemplateEliminationVisitor is included in that function.

The algorithm used follows that described in the VISITORS.txt readme
file included in the main metro/src/metropolis/metamodel directory
relatively closely. TemplateEliminationVisitor is called from
_resolvePass2 of the FileLoader. The TemplateEliminationVisitor uses
the TemplateDeclarationVisitor and TemplateInstanceVisitors to detect
every instance and declaration of a template contained in the same
CompileUnitNode. If the declaration of a template instance cannot be
found in the same CompileUnitNode, then it will search the current
package, and then the visible part of the whole source tree including
imported libraries.

TemplateEliminationVisitor walks through the list of template
instances in a CompileUnitNode, uses TemplateHandler to instantiate a
new instance declaration for each unique combination of template
parameters, and adds the new instance into the CompileUnitNode as a
child node. For each template instance, a new declaration structure
needs to be created and added to the scopes appropriately. The various
fields of the new instance treenode and declaration structure need to
be updated and/or set correctly. The template instance names are then
replaced with the resolved names.

2. IMPORTANT NOTES FOR BACKENDS PROGRAMMING: 

For a newly instantiated object node, we use a mapped property (mapped
with TEMPLDECL_KEY) to store a pointer to its prototype declaration.
Then a backend can easily tell it is an instantiated template and get
its original definition. At last, the unresolved template trees and
their associated declarations won't be removed from the
CompileUnitNodes and scopes.  Therefore, if a backend (e.g. systemc
backend) wants to handle templates by itself, it needs to skip the
template instantiations (the ones that have property
TEMPLDECL_KEY). Otherwise, it needs to skip the template prototype
declarations (elaborator backend is such an example). The metamodel
backend takes both and generate MMM code for both template prototype
declarations and template instantiations. So it is a good testing tool.

As for scope, once a template instantiation is added to where it is
instantiated, its declaration will be added to the corresponding file
scope and the direct package scope.

3. TESTING:

This test case tests the situations where template prototypes and
template instantiations are in the same files, in the different files
within the same package, and in different packages.

The templates are defined and instantiated in main/system.mmm,
main/templProc.mmm, and templ/templProc1.mmm.

To run the test case, go to directory main:

1) type "make", test template resolution all the way down to
   simulation (this test needs the support from systemc backend)

2) type "make elaborator", test template resolution and its elaboration

3) type "make metamodel", resolve templates and generate equivalent
   mmm code for both template declarations and instantiated template
   objects

To test standard template library, one has to add/change template
prototypes in the libraries. One example is to change class Nondet in
metro/lib/metamodel/lang/Nondet.mmm to a template prototype using the
code attached at the end of this file. Uncomment line 59-61 in
main/system.mmm, to instantiate the Nondet template.

4. LIMITATIONS AND COMMENTS:

1) Inner templates are not supported, i.e. template prototypes cannot
   be defined within other objects.

2) Template resolution is currently done at pass 2 (see FileLoader.java). 


5. TEMPLATE RELATED FILES:

InstantiateTemplateDeclVisitor.java
InstantiateTemplateUseVisitor.java
TemplateChecksVisitor.java
TemplateDeclarationVisitor.java
TemplateEliminationVisitor.java
TemplateHandler.java
TemplateInstanceVisitor.java
FileLoader.java

/*****************************************************************************/
/* replace the content of Nondet.mmm using the following code as a test case */

package metamodel.lang;
 
template (T)
public class Nondet extends Object {
        T data;
        boolean nondet;
 
        public Nondet() {
                data = 0;
                nondet = true;
        }
 
        public Nondet(T i) {
                data = i;
                nondet = false;
        }
 
        public void set(T i) {
                data = i;
                nondet = false;
        }
 
        public void setAny() {
                nondet = true;
        }
 
        public int get() {
                return data;
        }
 
        public boolean isNondet() {
                return nondet;
        }
}

====================================
Original author:  Xi Chen, May 15th, 2004
Last Update: $Id: README.txt,v 1.2 2004/07/03 20:08:45 xichen Exp $
