/* A base class for all repositories of precompiled information.

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

import metropolis.metamodel.MetaModelStaticSemanticConstants;

import java.io.File;
import java.io.Serializable;

// ////////////////////////////////////////////////////////////////////////
// // Repository
/**
 * A class of objects that store information obtained in previous compilations.
 * Serialization is used to store information about declarations and abstract
 * syntax trees. If an object is serialized more than once in the same file,
 * only one instance of this object is stored; on the other hand, if an object
 * is stored in several files, several instances of this object are stored. As
 * many of the objects that we are handling are singletons (e.g. nodes of the
 * trees, declarations), a special effort will be made to avoid cross-references
 * that could result in the creation of several instances of the same singleton
 * object.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: Repository.java,v 1.22 2006/10/12 20:34:03 cxh Exp $
 */
public abstract class Repository implements Serializable,
        MetaModelStaticSemanticConstants {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new repository with information on the sources in a given source
     * path.
     *
     * @param src
     *            Path where the sources are stored.
     */
    public Repository(File src) {
        _src = src;
        _rep = _repositoryPath(src);
        _loaded = false;
        if (!src.exists()) {
            throw new RuntimeException("Path " + src + " does not exist");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Load this repository from disk, or rebuild it if it does not exist. If
     * the repository exists but it is not loaded, then load it
     */
    public final void loadContent() {
        boolean modified = false;
        if (exists()) {
            if (isValid()) {
                try {
                    load();
                } catch (Exception ex) {
                    System.out.println("Warning, failed to load repository '"
                            + _rep + "' for sources '" + _src
                            + "', about to rebuild.  Exception was:");
                    ex.printStackTrace();
                    rebuild();
                    modified = true;
                }
            } else {
                if (canUpdate()) {
                    load();
                    update();
                    modified = true;
                } else {
                    rebuild();
                    modified = true;
                }
            }
        } else {
            rebuild();
            modified = true;
        }
        if (modified) {
            store();
        }
    }

    /** Load this repository from disk. */
    public void load() {
        if (_loaded) {
            return;
        }
        _loaded = true;
    }

    /**
     * Modify this loaded repository to bring it up-to-date. Re-use as much
     * information as possible from previous compilations.
     *
     * @exception RuntimeException
     *                if the repository has not been loaded.
     */
    public void update() {
        if (!_loaded) {
            throw new RuntimeException("Trying to update the repository"
                    + " before it is loaded");
        }
    }

    /**
     * Check if it is possible to update the repository instead of rebuilding it
     * from scratch.
     *
     * @return true iff the repository can be updated.
     */
    public abstract boolean canUpdate();

    /**
     * Store this repository to disk.
     *
     * @exception RuntimeException
     *                if the repository has not been loaded.
     */
    public void store() {
        if (!_loaded) {
            throw new RuntimeException("Trying to store the repository"
                    + " before it is loaded");
        }
    }

    /**
     * Rebuild the information in this repository completely from scratch. All
     * information available should be modified.
     */
    public void rebuild() {
        _loaded = true;
    }

    /**
     * Return the path where the sources of this repository are stored.
     *
     * @return The path where the sources are stored.
     */
    public File getSourcePath() {
        return _src;
    }

    /**
     * Return the path where the repository is stored.
     *
     * @return The path where the repository is stored.
     */
    public File getRepositoryPath() {
        return _rep;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /** Path where the sources are stored. */
    protected File _src;

    /** Path where the repository is stored. */
    protected File _rep;

    /** Flag indicating if the repository has been loaded from disk. */
    protected boolean _loaded;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Compute the path where a repository of a set of sources will be stored.
     * The path of the repository depends on the path where the sources e.g.
     * file(s), are stored.
     *
     * @param src
     *            Path where the sources are stored
     * @return The path where the repository file will be stored.
     */
    protected abstract File _repositoryPath(File src);

    /**
     * Check if this repository exists.
     *
     * @return true iff the repository file exists.
     */
    protected boolean exists() {
        return _rep.exists();
    }

    /**
     * Check if this repository exists and is completely up-to-date.
     *
     * @return true iff the repository exists and the sources have not changed
     *         since the repository was created.
     */
    protected abstract boolean isValid();
}
