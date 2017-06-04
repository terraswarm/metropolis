/*
 Copyright (c) 2003-2005 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software and that appropriate acknowledgments are made
 to the research of the Metropolis group.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 METROPOLIS_COPYRIGHT_VERSION_1
 COPYRIGHTENDKEY

 */

package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.Modifier;
import metropolis.metamodel.TNLManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.nodetypes.AbsentTreeNode;
import metropolis.metamodel.nodetypes.BlackboxNode;
import metropolis.metamodel.nodetypes.ClassDeclNode;
import metropolis.metamodel.nodetypes.ConstructorDeclNode;
import metropolis.metamodel.nodetypes.InterfaceDeclNode;
import metropolis.metamodel.nodetypes.MediumDeclNode;
import metropolis.metamodel.nodetypes.MethodDeclNode;
import metropolis.metamodel.nodetypes.NetlistDeclNode;
import metropolis.metamodel.nodetypes.ParameterDeclNode;
import metropolis.metamodel.nodetypes.PortDeclNode;
import metropolis.metamodel.nodetypes.ProcessDeclNode;
import metropolis.metamodel.nodetypes.QuantityDeclNode;
import metropolis.metamodel.nodetypes.SMDeclNode;
import metropolis.metamodel.nodetypes.SchedulerDeclNode;
import metropolis.metamodel.nodetypes.VarInitDeclNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Generate header file.
 *
 * @author Daniele Gasperini
 * @version $Id: HeaderCodegen.java,v 1.48 2006/10/12 20:33:00 cxh Exp $
 * @since Created on May 5, 2003, 2:06 PM
 */
public class HeaderCodegen {

    /**
     * Creates a new instance of HeaderCodegen.
     *
     * @param sysC
     *            the associated SystemC code generator
     */
    public HeaderCodegen(SystemCCodegenVisitor sysC) {
        _sysC = sysC;
    }

    /**
     * Check whether or not have a super process.
     *
     * @return whether there is a super process
     */
    public boolean hasSuperProcess() {
        return _hasSuperProcess;
    }

    /**
     * Check whether or not have an SC process.
     *
     * @return whether there is an SC process
     */
    public boolean hasSCProcess() {
        return _hasSCProcess;
    }

    /**
     * Set the file name.
     *
     * @param filename
     *            file name
     * @see #getFilename
     */
    public void setFilename(String filename) {
        _filename = filename;
    }

    /**
     * Set the Metropolis Metamodel (mmm) file name.
     *
     * @param filename
     *            mmm file name
     * @see #getMMMFilename
     */
    public void setMMMFilename(String filename) {
        _mmmFileName = filename;
    }

    /**
     * Get the file name.
     *
     * @return file name
     * @see #setFilename
     */
    public String getFilename() {
        return _filename;
    }

    /**
     * Get the Metropolis Metamodel file name.
     *
     * @return mmm file name
     * @see #setMMMFilename
     */
    public String getMMMFilename() {
        return _mmmFileName;
    }

    /**
     * Check whether or not contain the class declaration.
     *
     * @param name
     *            Class name
     * @return whether contain class declaration
     */
    public boolean containsClassDeclaration(String name) {
        for (int i = 0; i < _declaredClasses.size(); i++) {
            if (((String) _declaredClasses.get(i)).equals(name))
                return true;
        }
        return false;
    }

    /**
     * Get the code generated.
     *
     * @return the generated code
     */
    public LinkedList getCode() {
        return _code;
    }

    /**
     * Get the extern declarations.
     *
     * @return external declaration
     */
    public String getExternDeclString() {
        String result = "";
        Iterator iter = _externDecl.iterator();
        while (iter.hasNext()) {
            result += "extern " + iter.next() + ";\n";
        }
        return result;
    }

    /**
     * Get forward declarations.
     *
     * @return forward declaration
     */
    public String getForwardDeclString() {
        String result = "";
        String one;
        String sep;
        int numTypes;
        for (int i = 0; i < _forwardDecl.size(); i++) {
            one = (String) _forwardDecl.get(i);
            numTypes = ((Integer) _forwardDeclNumTypes.get(i)).intValue();
            one = one.trim();
            sep = "";
            if (numTypes > 0) {
                result += "template <";
                for (int j = 0; j < numTypes; j++) {
                    result += sep + "typename __T" + j;
                    sep = ", ";
                }
                result += "> ";
            }
            result += "class " + one + ";\n";
        }
        return result;
    }

    /**
     * Get include declarations.
     *
     * @return include declarations
     */
    public Vector getIncludeDecl() {
        return _includeDecl;
    }

    /** Eliminate files in metamodel lang library from include declarations. */
    public void EliminateMetamodelLangFromIncludeDecl() {
        String inc;
        for (int i = 0; i < _includeDecl.size(); i++) {
            inc = (String) _includeDecl.get(i);
            // Check for both forward and backward slashes here in case
            // we are under Windows.
            if (inc.indexOf("/lib/metamodel/lang/") > -1
                    || inc.indexOf("\\lib\\metamodel\\lang\\") > -1) {
                _includeDecl.remove(i);
                i--;
            }
        }
    }

    /**
     * Get include declarations.
     *
     * @return include declaration in String
     */
    public String getIncludeDeclString() {
        String result = "";
        for (int i = 0; i < _includeDecl.size(); i++)
            result += "#include \"" + _includeDecl.get(i) + "\"\n";

        return result;
    }

    /**
     * Get static declarations.
     *
     * @return static declaration
     */
    public String getStaticDeclString() {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < _staticDecl.size(); i++) {
            result.append(_staticDecl.get(i) + ";\n");
        }
        return result.toString();
    }

    /**
     * Replace a particular include declaration.
     *
     * @param index
     *            The index of the include declaration
     * @param classFile
     *            The replacement include declaration
     */
    public void replaceIncludeDecl(int index, String classFile) {
        _includeDecl.remove(index);
        if (classFile != null) {
            _includeDecl.add(index, classFile);
        }
    }

    /**
     * Compact the include declarations.
     *
     * @param hFileName
     *            The header file name
     */
    public void compactIncludeDecl(String hFileName) {
        for (int i = 0; i < _includeDecl.size();) {
            String s1 = (String) _includeDecl.get(i);
            if (s1.equals(hFileName)) {
                _includeDecl.remove(i);
                continue;
            }
            for (int j = i + 1; j < _includeDecl.size();) {
                String s2 = (String) _includeDecl.get(j);
                if (s1.equals(s2)) {
                    _includeDecl.remove(j);
                } else {
                    j++;
                }
            }
            i++;
        }
    }

    /**
     * Add classes declared in this file.
     *
     * @param l
     *            The declared class
     */
    public void addDeclaredClass(LinkedList l) {
        addDeclaredClass(SystemCCodegenVisitor._stringListToString(l));
    }

    /**
     * Add declared class.
     *
     * @param s
     *            The name of the declared class
     */
    public void addDeclaredClass(String s) {
        _declaredClasses.add(s);
    }

    /**
     * Add external declaration.
     *
     * @param s
     *            The name of the external declaration
     */
    public void addExternDecl(String s) {
        _externDecl.add(s);
    }

    /**
     * Add forward declaration.
     *
     * @param s
     *            The forward declaration
     * @param numTypes
     *            The number of type parameters if it is a template
     */
    public void addForwardDecl(LinkedList s, int numTypes) {
        addForwardDecl(SystemCCodegenVisitor._stringListToString(s), numTypes);
    }

    /**
     * Add forward declaration.
     *
     * @param s
     *            The name of the external declaration
     * @param numTypes
     *            The number of type parameters if it is a template
     */
    public void addForwardDecl(String s, int numTypes) {
        for (int i = 0; i < _includeDecl.size(); i++) {
            if (s.equals(_includeDecl.get(i))) {
                return;
            }
        }

        for (int i = 0; i < _forwardDecl.size(); i++) {
            if (s.equals(_forwardDecl.get(i))) {
                return;
            }
        }
        _forwardDecl.add(s);
        _forwardDeclNumTypes.add(new Integer(numTypes));
    }

    /**
     * Add include declaration.
     *
     * @param s
     *            The name of the include declaration
     */
    public void addIncludeDecl(String s) {
        for (int i = 0; i < _forwardDecl.size(); i++) {
            if (s.equals(_forwardDecl.get(i))) {
                _forwardDecl.remove(i);
                _forwardDeclNumTypes.remove(i);
                break;
            }
        }

        for (int i = 0; i < _includeDecl.size(); i++) {
            if (s.equals(_includeDecl.get(i))) {
                return;
            }
        }
        _includeDecl.add(s);
    }

    /**
     * Add template implementation code.
     *
     * @param tmpl
     *            The template implementation code
     */
    public void addTemplateImplCode(LinkedList tmpl) {
        _code.addLast(tmpl);
    }

    /**
     * Add the code in BlackboxNode.
     *
     * @param node
     *            The BlackboxNode being visited
     * @param args
     *            The arguments
     */
    public void visitBlackboxNode(BlackboxNode node, LinkedList args) {
        _code.addLast(node.getCode());
    }

    /**
     * Visit PortDeclNode.
     *
     * @param node
     *            The PortDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitPortDeclNode(PortDeclNode node, LinkedList args) {
        indent();
        _code.addLast("sc_port <");
        LinkedList defType = (LinkedList) node.getDefType().accept(_sysC, args);
        LinkedList portName = (LinkedList) node.getName().accept(_sysC, args);
        String portType = SystemCCodegenVisitor._stringListToString(defType)
                .trim();
        String pointer = "";
        int index = portType.lastIndexOf('>');
        if (index == -1) {
            index = portType.indexOf("*");
        } else {
            int subindex = portType.substring(index).indexOf('*');
            if (subindex == -1)
                index = -1;
            else
                index += subindex;
        }

        if (index > 0) {
            pointer = portType.substring(index);
            portType = portType.substring(0, index);
        }
        int dim = 0;
        for (int i = 0; i < pointer.length(); i++) {
            if (pointer.charAt(i) == '*') {
                dim++;
            }
        }

        _code.addLast(portType);
        _code.addLast(" > ");

        for (int i = 0; i < dim; i++) {
            _code.addLast("*");
        }
        _code.addLast(" ");
        _code.addLast(portName);
        _code.addLast(";\n");

    }

    /**
     * Generate code for the beginning of ProcessDeclNode.
     *
     * @param node
     *            The ProcessDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitProcessDeclNodeBegin(ProcessDeclNode node, LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);
        if (node.getSuperClass() != AbsentTreeNode.instance) {
            _code.addLast(" : public ");
            _sysC._inObjectDef = true;
            LinkedList superClassName = (LinkedList) node.getSuperClass()
                    .accept(_sysC, args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
            _hasSuperProcess = true;
        } else {
            _code.addLast(" : public process ");
            _hasSuperProcess = false;
        }

        _code.addLast(" {\n");
        indent();
        _code.addLast("public:\n");

        _hasSCProcess = false;
    }

    /**
     * Generate code for the end of ProcessDeclNode.
     *
     * @param node
     *            The ProcessDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitProcessDeclNodeEnd(ProcessDeclNode node, LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the beginning of MediumDeclNode.
     *
     * @param node
     *            The MediumDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitMediumDeclNodeBegin(MediumDeclNode node, LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);
        LinkedList superClassName = null;

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);
        if (node.getSuperClass() != AbsentTreeNode.instance) {
            _code.addLast(" : public ");
            _sysC._inObjectDef = true;
            superClassName = (LinkedList) node.getSuperClass().accept(_sysC,
                    args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
        } else {
            _code.addLast(" : public medium");
        }

        _sysC._inObjectDef = true;
        List retValue = TNLManip
                .traverseList(_sysC, args, node.getInterfaces());
        _sysC._inObjectDef = false;
        if (!retValue.isEmpty()) {
            _code.addLast(", virtual public ");
            Iterator stringListItr = retValue.iterator();

            while (stringListItr.hasNext()) {
                LinkedList intfcClassName = (LinkedList) stringListItr.next();
                _code.addLast(intfcClassName);
                if (stringListItr.hasNext()) {
                    _code.addLast(", virtual public ");
                }
            }
        }

        _code.addLast("{\n");
        indent();
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of MediumDeclNode.
     *
     * @param node
     *            The MediumDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitMediumDeclNodeEnd(MediumDeclNode node, LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the beginning of SchedulerDeclNode.
     *
     * @param node
     *            The SchedulerDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitSchedulerDeclNodeBegin(SchedulerDeclNode node,
            LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);
        if (node.getSuperClass() != AbsentTreeNode.instance) {
            _code.addLast(" : public ");
            _sysC._inObjectDef = true;
            LinkedList superClassName = (LinkedList) node.getSuperClass()
                    .accept(_sysC, args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
        } else {
            _code.addLast(" : public scheduler");
        }

        _code.addLast(" {\n");
        indent();
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of SchedulerDeclNode.
     *
     * @param node
     *            The SchedulerDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitSchedulerDeclNodeEnd(SchedulerDeclNode node,
            LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the beginning of SMDeclNode.
     *
     * @param node
     *            The SMDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitSMDeclNodeBegin(SMDeclNode node, LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            _code.addLast(" : public ");
            _sysC._inObjectDef = true;
            LinkedList superClassName = (LinkedList) node.getSuperClass()
                    .accept(_sysC, args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
        } else {
            _code.addLast(" : public statemedium ");
        }

        _sysC._inObjectDef = true;
        List retValue = TNLManip
                .traverseList(_sysC, args, node.getInterfaces());
        _sysC._inObjectDef = false;
        if (!retValue.isEmpty()) {
            _code.addLast(", virtual public ");

            Iterator stringListItr = retValue.iterator();

            while (stringListItr.hasNext()) {
                LinkedList superClassName = (LinkedList) stringListItr.next();
                _code.addLast(superClassName);
                if (stringListItr.hasNext()) {
                    _code.addLast(", virtual public ");
                }
            }
        }

        _code.addLast(" {\n");
        indent();
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of SMDeclNode.
     *
     * @param node
     *            The SMDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitSMDeclNodeEnd(SMDeclNode node, LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the beginning of NetlistDeclNode.
     *
     * @param node
     *            The NetlistDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitNetlistDeclNodeBegin(NetlistDeclNode node, LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);

        if (node.getSuperClass() != AbsentTreeNode.instance) {
            _code.addLast(" : public ");
            _sysC._inObjectDef = true;
            LinkedList superClassName = (LinkedList) node.getSuperClass()
                    .accept(_sysC, args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
        } else {
            _code.addLast(" : public netlist");
        }

        _code.addLast(" {\n");
        indent();
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of NetlistDeclNode.
     *
     * @param node
     *            The NetlistDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitNetlistDeclNodeEnd(NetlistDeclNode node, LinkedList args) {
        // force one constructor with args (sc_module_name)
        indent();
        _code.addLast(node.getName().accept(_sysC, args));
        _code.addLast("(sc_module_name _name) : ");
        if (node.getSuperClass() instanceof AbsentTreeNode) {
            _code.addLast("netlist(_name) {};\n");
        } else {
            _sysC._inObjectDef = true;
            _code.addLast(node.getSuperClass().accept(_sysC, args));
            _sysC._inObjectDef = false;
            _code.addLast("(_name) {};\n");
        }

        indent();
        _code.addLast("};\n");
    }

    /**
     * Generate code for the beginning of ClassDeclNode.
     *
     * @param node
     *            The ClassDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitClassDeclNodeBegin(ClassDeclNode node, LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);

        _code.addLast(" ");

        TreeNode superClass = node.getSuperClass();
        _code.addLast(" : public ");
        if (superClass != AbsentTreeNode.instance) {
            _sysC._inObjectDef = true;
            LinkedList superClassName = (LinkedList) node.getSuperClass()
                    .accept(_sysC, args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
        } else {
            _code.addLast("Object");
        }

        _code.addLast(" ");

        // visit interfaces
        _sysC._inObjectDef = true;
        List retValue = TNLManip
                .traverseList(_sysC, args, node.getInterfaces());
        _sysC._inObjectDef = false;
        if (!retValue.isEmpty()) {
            if (superClass == AbsentTreeNode.instance)
                _code.addLast(" : virtual public ");
            else
                _code.addLast(", virtual public ");

            Iterator stringListItr = retValue.iterator();

            while (stringListItr.hasNext()) {
                LinkedList superClassName = (LinkedList) stringListItr.next();
                _code.addLast(superClassName);
                if (stringListItr.hasNext())
                    _code.addLast(", virtual public ");
            }
        }

        _code.addLast("{\n");
        indent();
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of ClassDeclNode.
     *
     * @param node
     *            The ClassDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitClassDeclNodeEnd(ClassDeclNode node, LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the beginning of InterfaceDeclNode.
     *
     * @param node
     *            The InterfaceDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitInterfaceDeclNodeBegin(InterfaceDeclNode node,
            LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);
        _code.addLast(" : ");

        // visit interfaces
        _sysC._inObjectDef = true;
        List retValue = TNLManip
                .traverseList(_sysC, args, node.getInterfaces());
        _sysC._inObjectDef = false;
        if (!retValue.isEmpty()) {
            Iterator ListItr = retValue.iterator();
            boolean hasParent = false;

            while (ListItr.hasNext()) {
                String str = (String) ((LinkedList) (ListItr.next()))
                        .getFirst();
                if (!str.equals("Port")) {
                    if (hasParent) {
                        _code.addLast(", ");
                    }
                    _code.addLast("virtual ");
                    _code.addLast("public " + str);
                    hasParent = true;
                }
            }
            if (!hasParent) {
                _code.addLast("virtual public sc_interface");
            }
        } else {
            _code.addLast("virtual public sc_interface");
        }

        _code.addLast("{\n");
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of InterfaceDeclNode.
     *
     * @param node
     *            The InterfaceDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitInterfaceDeclNodeEnd(InterfaceDeclNode node,
            LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the beginning of QuantityDeclNode.
     *
     * @param node
     *            The QuantityDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitQuantityDeclNodeBegin(QuantityDeclNode node,
            LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);

        args.set(1, className);

        List template = TNLManip.traverseList(_sysC, args, node
                .getParTypeNames());
        int numTypes = 0;
        if (!template.isEmpty()) {
            numTypes = template.size();
            _templateTypes = "<"
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", ")) + ">";
            _template = "template <typename "
                    + SystemCCodegenVisitor
                            ._stringListToString(SystemCCodegenVisitor
                                    ._separateList(template, ", typename "))
                    + "> ";
            _code.addLast("\n");
            indent();
            _code.addLast(_template);
            _hasTemplates = true;
        } else {
            _template = "";
            _templateTypes = "";
        }

        addForwardDecl(className, numTypes);
        addDeclaredClass(className);

        _code.addLast("\n");
        indent();
        _code.addLast("class ");
        _code.addLast(className);
        if (node.getSuperClass() != AbsentTreeNode.instance) {
            _code.addLast(" : public ");
            _sysC._inObjectDef = true;
            LinkedList superClassName = (LinkedList) node.getSuperClass()
                    .accept(_sysC, args);
            _sysC._inObjectDef = false;
            _code.addLast(superClassName);
        } else {
            _code.addLast(" : public quantity ");
        }

        _sysC._inObjectDef = true;
        List retValue = TNLManip
                .traverseList(_sysC, args, node.getInterfaces());
        _sysC._inObjectDef = false;
        if (!retValue.isEmpty()) {
            Iterator stringListItr = retValue.iterator();

            while (stringListItr.hasNext()) {
                String intfcClassName = SystemCCodegenVisitor
                        ._stringListToString((LinkedList) stringListItr.next());
                if (!intfcClassName.equals("QuantityManager")) {
                    _code.addLast(", virtual public ");
                    _code.addLast(intfcClassName);
                }
            }
        }

        _code.addLast(" {\n");
        indent();
        _code.addLast("public:\n");
    }

    /**
     * Generate code for the end of QuantityDeclNode.
     *
     * @param node
     *            The QuantityDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitQuantityDeclNodeEnd(QuantityDeclNode node, LinkedList args) {
        indent();
        _code.addLast("\n};\n");
    }

    /**
     * Generate code for the MethodDeclNode.
     *
     * @param node
     *            The MethodDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitMethodDeclNode(MethodDeclNode node, LinkedList args) {
        String type = args.get(0).toString();
        boolean parent_was_object = wasParentObject(node.getParent().classID());

        _code.addLast("\n");

        boolean isStatic = false;
        String mod = Modifier.toString(node.getModifiers());
        StringTokenizer st = new StringTokenizer(mod);
        while (st.hasMoreTokens()) {
            String modifier = st.nextToken();
            if (modifier.equals("final")) {
                if (parent_was_object)
                    _code.addLast("const ");
            } else if (modifier.equals("static")) {
                isStatic = true;
                if (parent_was_object)
                    _code.addLast(modifier + " ");
            }
            // public, protected, private, abstract ignored
        }

        if (!isStatic) {
            if (parent_was_object)
                _code.addLast("virtual ");
        }

        _code.addLast(node.getReturnType().accept(_sysC, args));

        _code.addLast(" ");

        if (parent_was_object) {
            _code.addLast(node.getName().accept(_sysC, args));
            _code.addLast("(");
        }

        LinkedList par = SystemCCodegenVisitor._commaList(TNLManip
                .traverseList(_sysC, args, node.getParams()));
        if (!(type.equals("Process") && node.getName().getIdent().toString()
                .equals("thread"))) {
            _code.addLast("process * caller");
            if (SystemCCodegenVisitor._stringListToString(par).trim().length() > 0) {
                _code.addLast(", ");
            }
        }

        if (parent_was_object) {
            _code.addLast(par);
            _code.addLast(")");
            if (node.getBody() instanceof AbsentTreeNode) {
                if (type.equals("Interface"))
                    _code.addLast("= 0");
            }
            _code.addLast(";\n");
        }
    }

    /**
     * Generate code for the ConstraintBlockNode.
     *
     * @param node
     *            The ConstraintBlockNode being visited
     * @param args
     *            The arguments
     */
    // ***** Old code to handle synch ******
    // public void visitConstraintBlockNode(ConstraintBlockNode node,
    // LinkedList args) {
    // TreeNode parent = node.getParent();
    // while (!(parent instanceof ConstructorDeclNode
    // || parent instanceof MethodDeclNode)) {
    // parent = parent.getParent();
    // }
    // if (parent instanceof ConstructorDeclNode) {
    // parent = ((ConstructorDeclNode)parent).getParent();
    // } else if (parent instanceof MethodDeclNode) {
    // parent = ((MethodDeclNode)parent).getParent();
    // }
    // boolean parent_was_object = wasParentObject(parent.classID());
    // if (parent_was_object) {
    // _code.add("\n");
    // indent();
    // _code.add("virtual void synch(int);\n\n");
    // indent();
    // _code.add("virtual void ltl();\n");
    // }
    // }
    /**
     * Generate code for the ConstructorDeclNode.
     *
     * @param node
     *            The ConstructorDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitConstructorDeclNode(ConstructorDeclNode node,
            LinkedList args) {
        boolean parent_was_object = wasParentObject(node.getParent().classID());
        LinkedList constructorName = (LinkedList) node.getName().accept(_sysC,
                args);

        if (!_hasSCProcess && args.get(0).equals("Process")) {
            indent();
            _code.addLast("SC_HAS_PROCESS(");
            _code.addLast(constructorName);
            _code.addLast(");\n");
            _hasSCProcess = true;
        }

        if (parent_was_object && !_hasSCProcess) {
            _code.addLast("\n");
        }

        // constructors for classes
        if (parent_was_object)
            _code.addLast(constructorName);

        LinkedList par = SystemCCodegenVisitor._commaList(TNLManip
                .traverseList(_sysC, args, node.getParams()));
        if (args.get(0).equals("Class")) {
            if (parent_was_object) {
                _code.addLast("(");
            }
        } else {
            if (parent_was_object) {
                _code.addLast("(sc_module_name _name");
            }
            if (SystemCCodegenVisitor._stringListToString(par).trim().length() > 0) {
                if (parent_was_object) {
                    _code.addLast(", ");
                }

            }
        }

        if (parent_was_object) {
            _code.addLast(par);
            _code.addLast(");\n");
        }
    }

    /**
     * Generate code for the VarInitDeclNode.
     *
     * @param node
     *            The VarInitDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitVarInitDeclNode(VarInitDeclNode node, LinkedList args) {
        boolean parent_was_object = wasParentObject(node.getParent().classID());

        //TreeNode baseType = node.getDefType();

        if (parent_was_object) {
            boolean bStatic = false;
            boolean bConst = false;

            indent();

            LinkedList staticDecl = new LinkedList();

            String mod = Modifier.toString(node.getModifiers());
            StringTokenizer st = new StringTokenizer(mod);
            while (st.hasMoreTokens()) {
                String modifier = st.nextToken();
                if (modifier.equals("final")) {
                    _code.addLast("const ");
                    staticDecl.addLast("const ");
                    bConst = true;
                } else if (modifier.equals("static")) {
                    _code.addLast(modifier + " ");
                    bStatic = true;
                } else if (modifier.equals("abstract")) {
                    _code.addLast(modifier + " ");
                }
                // public, protected, private ignored
            }
            if (bConst & !bStatic) {
                _code.addLast("static ");
                bStatic = true;
            }

            LinkedList typeName = (LinkedList) node.getDefType().accept(_sysC,
                    args);
            _code.addLast(typeName);

            staticDecl.addLast(typeName);

            _code.addLast(" " + node.getName().getIdent());
            if (bConst) {
                if (node.getInitExpr() != AbsentTreeNode.instance) {
                    _code.addLast(" = ");
                    _code.addLast(node.getInitExpr().accept(_sysC, args));
                }
            } else if (bStatic) {
                staticDecl.addLast(" ");
                staticDecl.addLast(args.get(1));
                staticDecl.addLast("::" + node.getName().getIdent() + " = ");
                if (node.getInitExpr() != AbsentTreeNode.instance) {
                    staticDecl.addLast(node.getInitExpr().accept(_sysC, args));
                } else {
                    staticDecl.addLast("0");
                }
                staticDecl.addLast(";\n");

                _staticDecl.add(SystemCCodegenVisitor
                        ._stringListToString(staticDecl));
            }
            _code.addLast(";\n");
        }
    }

    /**
     * Generate code for the ParameterDeclNode.
     *
     * @param node
     *            The ParameterDeclNode being visited
     * @param args
     *            The arguments
     */
    public void visitParameterDeclNode(ParameterDeclNode node, LinkedList args) {
        boolean parent_was_object = wasParentObject(node.getParent().classID());

        if (parent_was_object) {
            indent();
            _code.addLast(Modifier.toString(node.getModifiers()));
            _code.addLast(node.getDefType().accept(_sysC, args));
            //TreeNode baseType = node.getDefType();
            _code.addLast(" ");
            _code.addLast(node.getName().accept(_sysC, args));
            _code.addLast(";\n");
        }
    }

    /**
     * Generate a dummy constructor.
     *
     * @param node
     *            The class being visited
     * @param args
     *            The arguments
     */
    public void dummy_ctor_gen(ClassDeclNode node, LinkedList args) {
        LinkedList className = (LinkedList) node.getName().accept(_sysC, args);
        if (args.get(0).equals("Class")) {
            _code.addLast("\n");
            _code.addLast(className);
            _code.addLast("(DUMMY_CTOR_ARG _arg, int *index);");

            // provide default constructor
            List mem = node.getMembers();
            Iterator memIter = mem.iterator();
            boolean flag = false;
            while (memIter.hasNext()) {
                TreeNode tnode = (TreeNode) memIter.next();
                if (tnode instanceof ConstructorDeclNode) {
                    List par = ((ConstructorDeclNode) tnode).getParams();
                    if (par == null) {
                        flag = true;
                        break;
                    } else if (par.isEmpty()) {
                        flag = true;
                        break;
                    }
                }
            }
            if (!flag) {
                _code.addLast("\n");
                indent();
                _code.addLast(className);
                _code.addLast("() {}\n");
            }
        } else {
            _code.addLast("\n");
            _code.addLast(className);
            _code
                    .addLast("(sc_module_name _name, DUMMY_CTOR_ARG _arg, int *index); ");
        }
    }

    /**
     * Check if the parent node is an object.
     *
     * @param classID
     *            class ID
     * @return whether parent is a class object
     */
    public boolean wasParentObject(int classID) {
        switch (classID) {
        case MetaModelStaticSemanticConstants.QUANTITYDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.CLASSDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.PROCESSDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.MEDIUMDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.SCHEDULERDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.SMDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.INTERFACEDECLNODE_ID:
            return true;
        case MetaModelStaticSemanticConstants.NETLISTDECLNODE_ID:
            return true;
        default:
        }

        return false;
    }

    private void indent() {
        String blk = "";
        for (int i = 0; i < _indentLevel; i++)
            blk += " ";
        _code.addLast(blk);
    }

    /**
     * Get the template code.
     *
     * @return template
     */
    public String getTemplate() {
        return _template;
    }

    /**
     * Get the template types.
     *
     * @return template types
     */
    public String getTemplateTypes() {
        return _templateTypes;
    }

    /**
     * Check if it has a template.
     *
     * @return whether has templates
     */
    public boolean hasTemplate() {
        return _hasTemplates;
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    private LinkedList _code = new LinkedList();

    private Vector _declaredClasses = new Vector();

    private Set _externDecl = new HashSet();

    private String _filename = null;

    private Vector _forwardDecl = new Vector();

    private Vector _forwardDeclNumTypes = new Vector();

    private boolean _hasSuperProcess;

    private boolean _hasSCProcess;

    private boolean _hasTemplates = false;

    private int _indentLevel = 0;

    private Vector _includeDecl = new Vector();

    private String _mmmFileName = null;

    private Vector _staticDecl = new Vector(); // static variables

    private SystemCCodegenVisitor _sysC = null;

    private String _template = "";

    private String _templateTypes = "";
}
