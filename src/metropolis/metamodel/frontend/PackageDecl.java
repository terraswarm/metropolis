/* A declaration of a meta-model package.

 Metropolis: Design Environment for Heterogeneus Systems.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

package metropolis.metamodel.frontend;

import metropolis.metamodel.Decl;
import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;

// ////////////////////////////////////////////////////////////////////////
// // PackageDecl
/**
 * This class describes the declaration of a package. There is only one
 * PackageDecl instance for each package in the program. All packages have a
 * container and a scope.
 * <p>
 * There are three special package declarations: the SYSTEM package, the UNNAMED
 * package and the LANG package. The SYSTEM package is the root of the
 * hierarchy: all packages that are not contained by another package are
 * contained by the SYSTEM package. The UNNAMED package is the place where we
 * will store ALL classes that have not provided the name of an enclosing
 * package. Notice that UNNAMED package is different from SYSTEM: they are both
 * used when no enclosing package is provided, but SYSTEM is for packages and
 * UNNAMED is for classes. Finally, the LANG package is the place where all the
 * meta-model classes related to the meta-model language will be stored. By
 * default, all meta-model files import all names from the LANG package.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: PackageDecl.java,v 1.32 2006/10/12 20:33:57 cxh Exp $
 */
public class PackageDecl extends MetaModelDecl {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create the package with the given name and container. The name of the
     * package should be unique in scope in the container, i.e. no other
     * containers or classes can have the same name
     *
     * @param name
     *            Name of the package.
     * @param container
     *            Package that contains current package. If the package is 'top
     *            level' this should be the SYSTEM_PACKAGE.
     */
    public PackageDecl(String name, MetaModelDecl container) {
        super(name, CG_PACKAGE);
        _container = container;
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Re-override equals() from Decl so that equality is defined as being the
     * same object. This is necessary to ensure that a Decl named z for x.y.z
     * does not equal another Decl named z for x.z.
     *
     * @param obj
     *            Object being compared with this.
     * @return true if the objects are the same object.
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * Return a description of the package declaration.
     *
     * @return The description.
     */
    public String description() {
        // FIXME: this is incomplete
        StringBuffer description = new StringBuffer(" PackageDecl: "
                + toString() + "\n name: " + getName() + "\n path: "
                + FileLoader.packagePath(this) + "\n userTypes: ");
        ScopeIterator userTypes = getUserTypes();
        while (userTypes.hasNext()) {
            Decl decl = (Decl) userTypes.next();
            description.append(" " + decl);
        }

        description.append("\n subpackages: ");
        ScopeIterator subPackages = getSubPackages();
        while (subPackages.hasNext()) {
            Decl decl = (Decl) subPackages.next();
            description.append(" " + decl);
        }
        return description.toString();
    }

    /**
     * Override method hasContainer() from MetaModelDecl to return always true,
     * as all packages have an enclosing declaration.
     *
     * @return true, as described.
     */
    public final boolean hasContainer() {
        return true;
    }

    /**
     * Override method hasScope() from MetaModelDecl to return always true, as
     * all packages have a scope.
     *
     * @return true, as described.
     */
    public final boolean hasScope() {
        return true;
    }

    /**
     * Set the scope of this package.
     *
     * @param scope
     *            Scope of this package.
     * @see #getScope()
     */
    public void setScope(Scope scope) {
        _scope = scope;
    }

    /**
     * Set the container of this package.
     *
     * @param container
     *            Enclosing package.
     * @see #getContainer()
     */
    public void setContainer(MetaModelDecl container) {
        _container = container;
    }

    /**
     * Get the scope of this package, or create and empty scope for this package
     * if it has not been created before. This method should be used during
     * initialization, by the classes that are going to load the members of this
     * package.
     *
     * @return The scope of this package, possibly empty if it was not created
     *         previously.
     */
    public Scope initGetScope() {
        if (_scope == null) {
            Scope parent;
            if (_container == null)
                parent = null;
            else {
                PackageDecl parentPkg = (PackageDecl) _container;
                parent = parentPkg.initGetScope();
            }
            _scope = new Scope(parent);
        }
        return _scope;
    }

    /**
     * Get the scope of this package. Load subpackages of current package and
     * types defined in the package, because they could be accessed in the
     * scope.
     *
     * @return The scope of this package.
     * @see #setScope(Scope)
     */
    public Scope getScope() {
        _initScope();
        return _scope;
    }

    /**
     * Get the enclosing package of this package. If the package is the
     * SYSTEM_PACKAGE (no parent) return NULL.
     *
     * @return The enclosing package.
     * @see #setContainer(MetaModelDecl)
     */
    public MetaModelDecl getContainer() {
        return _container;
    }

    /**
     * Get all the subpackages of the current package.
     *
     * @return A ScopeIterator that will generate all the subpackages of this
     *         package.
     */
    public ScopeIterator getSubPackages() {
        _initScope();
        return _scope.allLocalDecls(CG_PACKAGE);
    }

    /**
     * Get a subpackage of this package with a given name.
     *
     * @param name
     *            The name of the subpackage to find.
     * @exception RuntimeException
     *                if there is more than one subpackage with this name in
     *                this package.
     * @return The subpackage of the current package.
     */
    public PackageDecl getSubPackage(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        PackageDecl decl = (PackageDecl) _scope.lookupLocal(name, CG_PACKAGE,
                more);
        if (more[0]) {
            throw new RuntimeException("Subpackage name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of user-defined types in this package.
     *
     * @return A ScopeIterator that will generate all declarations of
     *         user-defined types in this package.
     */
    public ScopeIterator getUserTypes() {
        _initScope();
        return _scope.allLocalDecls(CG_USERTYPE);
    }

    /**
     * Get the declaration of a user-defined type in this package that has a
     * given name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the user-defined type, or null if the
     *         declaration does not exist.
     * @exception RuntimeException
     *                if there is more than one user-defined type with this name
     *                in this package.
     */
    public ObjectDecl getUserType(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        ObjectDecl decl = (ObjectDecl) _scope.lookupLocal(name, CG_USERTYPE,
                more);
        if (more[0]) {
            throw new RuntimeException("User-defined type name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of interfaces in current package.
     *
     * @return A iterator that will generate all declarations of interfaces in
     *         the current package.
     */
    public ScopeIterator getInterfaces() {
        _initScope();
        return _scope.allLocalDecls(CG_INTERFACE);
    }

    /**
     * Get the declaration of an interface in this package that has a given
     * name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the interface, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one interface with this name in this
     *                package.
     */
    public InterfaceDecl getInterface(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        InterfaceDecl decl = (InterfaceDecl) _scope.lookupLocal(name,
                CG_INTERFACE, more);
        if (more[0]) {
            throw new RuntimeException("Interface name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of classes in current package.
     *
     * @return A iterator that will generate all declarations of classes in the
     *         current package.
     */
    public ScopeIterator getClasses() {
        _initScope();
        return _scope.allLocalDecls(CG_CLASS);
    }

    /**
     * Get the declaration of an class in this package that has a given name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the class, or null if the declaration does not
     *         exist.
     * @exception RuntimeException
     *                if there is more than one class with this name in this
     *                package.
     */
    public ClassDecl getClass(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        ClassDecl decl = (ClassDecl) _scope.lookupLocal(name, CG_CLASS, more);
        if (more[0]) {
            throw new RuntimeException("Class name '" + name + "' in package "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of quantities in current package.
     *
     * @return A iterator that will generate all declarations of quantities in
     *         the current package.
     */
    public ScopeIterator getQuantities() {
        _initScope();
        return _scope.allLocalDecls(CG_QUANTITY);
    }

    /**
     * Get the declaration of an quantity in this package that has a given name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the quantity, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one quantity with this name in this
     *                package.
     */
    public QuantityDecl getQuantity(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        QuantityDecl decl = (QuantityDecl) _scope.lookupLocal(name,
                CG_QUANTITY, more);
        if (more[0]) {
            throw new RuntimeException("Quantity name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of processes in current package.
     *
     * @return A iterator that will generate all declarations of processes in
     *         the current package.
     */
    public ScopeIterator getProcesses() {
        _initScope();
        return _scope.allLocalDecls(CG_PROCESS);
    }

    /**
     * Get the declaration of an process in this package that has a given name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the process, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one process with this name in this
     *                package.
     */
    public ProcessDecl getProcess(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        ProcessDecl decl = (ProcessDecl) _scope.lookupLocal(name, CG_PROCESS,
                more);
        if (more[0]) {
            throw new RuntimeException("Process name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of netlists in current package.
     *
     * @return A iterator that will generate all declarations of netlists in the
     *         current package.
     */
    public ScopeIterator getNetlists() {
        _initScope();
        return _scope.allLocalDecls(CG_NETLIST);
    }

    /**
     * Get the declaration of an netlist in this package that has a given name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the netlist, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one netlist with this name in this
     *                package.
     */
    public NetlistDecl getNetlist(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        NetlistDecl decl = (NetlistDecl) _scope.lookupLocal(name, CG_NETLIST,
                more);
        if (more[0]) {
            throw new RuntimeException("Netlist name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of media in current package.
     *
     * @return A iterator that will generate all declarations of media in the
     *         current package.
     */
    public ScopeIterator getMedia() {
        _initScope();
        return _scope.allLocalDecls(CG_MEDIUM);
    }

    /**
     * Get the declaration of an medium in this package that has a given name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the medium, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one medium with this name in this
     *                package.
     */
    public MediumDecl getMedium(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        MediumDecl decl = (MediumDecl) _scope
                .lookupLocal(name, CG_MEDIUM, more);
        if (more[0]) {
            throw new RuntimeException("Medium name '" + name + "' in package "
                    + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of schedulers in current package.
     *
     * @return A iterator that will generate all declarations of schedulers in
     *         the current package.
     */
    public ScopeIterator getSchedulers() {
        _initScope();
        return _scope.allLocalDecls(CG_SCHEDULER);
    }

    /**
     * Get the declaration of an scheduler in this package that has a given
     * name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the scheduler, or null if the declaration does
     *         not exist.
     * @exception RuntimeException
     *                if there is more than one scheduler with this name in this
     *                package.
     */
    public SchedulerDecl getScheduler(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        SchedulerDecl decl = (SchedulerDecl) _scope.lookupLocal(name,
                CG_SCHEDULER, more);
        if (more[0]) {
            throw new RuntimeException("Scheduler name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Get all declarations of state media in current package.
     *
     * @return A iterator that will generate all declarations of state media in
     *         the current package.
     */
    public ScopeIterator getStateMedia() {
        _initScope();
        return _scope.allLocalDecls(CG_SM);
    }

    /**
     * Get the declaration of an state medium in this package that has a given
     * name.
     *
     * @param name
     *            Name to be found.
     * @return The declaration of the state medium, or null if the declaration
     *         does not exist.
     * @exception RuntimeException
     *                if there is more than one state medium with this name in
     *                this package.
     */
    public StateMediumDecl getStateMedium(String name) {
        _initScope();
        boolean more[] = new boolean[1];
        StateMediumDecl decl = (StateMediumDecl) _scope.lookupLocal(name,
                CG_SM, more);
        if (more[0]) {
            throw new RuntimeException("State medium name '" + name
                    + "' in package " + getName() + " redeclared.");
        }
        return decl;
    }

    /**
     * Test if the members of the package have been loaded.
     *
     * @return true iff the members of the package have been loaded.
     */
    public boolean membersLoaded() {
        if (this == MetaModelLibrary.SYSTEM_PACKAGE)
            return true;
        if (this == MetaModelLibrary.UNNAMED_PACKAGE)
            return true;
        return _membersLoaded;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Enclosing package of this package. */
    protected MetaModelDecl _container = null;

    /** Scope in this package. */
    protected Scope _scope = null;

    /**
     * Flag indicating if members of this package have been loaded. Subpackages
     * and user-defined types are loaded on demand.
     */
    protected boolean _membersLoaded = false;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Check if the members of the package have been loaded. If they haven't,
     * load them now.
     */
    protected void _initScope() {
        // Special packages are loaded differently
        if (this == MetaModelLibrary.SYSTEM_PACKAGE)
            return;
        if (this == MetaModelLibrary.UNNAMED_PACKAGE)
            return;

        if (_membersLoaded)
            return;

        _membersLoaded = true;
        FileLoader.loadPackageMembers(this);
    }

}
