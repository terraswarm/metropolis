/* A base class for all repositories that store information about a single
 file.

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

import metropolis.metamodel.StringManip;

import java.io.File;

// ////////////////////////////////////////////////////////////////////////
// // FileRepository
/**
 * A base class for all repositories that store information about a single
 * source file. It provides utility methods to check if a repository is still
 * valid, and methods to get the name of the repository.
 * <p>
 * Portions of this code were derived from sources developed under the auspices
 * of the Ptolemy II project.
 *
 * @author Robert Clariso
 * @version $Id: FileRepository.java,v 1.21 2006/10/12 20:33:43 cxh Exp $
 */
public abstract class FileRepository extends Repository {

    // /////////////////////////////////////////////////////////////////
    // // constructors ////

    /**
     * Create a new repository with information on the sources in a given source
     * path. The source path must be a Metropolis meta-model file.
     *
     * @param src
     *            Path where the source file is stored.
     */
    public FileRepository(File src) {
        super(src);
        if (!src.isFile()) {
            throw new RuntimeException(src + " is not a source file");
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * Check if it is possible to update the repository instead of rebuilding it
     * from scratch. In the case of files, if the file has changed we will have
     * to parse it again, so we cannot reuse any information. repositories
     * based.
     *
     * @return true iff the repository can be updated.
     */
    public final boolean canUpdate() {
        return false;
    }

    /**
     * Modify this loaded repository to bring it up-to-date. Re-use as much
     * information as possible from previous compilations. As we cannot re-use
     * anything, we will have to rebuild it.
     *
     * @exception RuntimeException
     *                if the repository has not been loaded.
     */
    public final void update() {
        rebuild();
    }

    /**
     * Indicate whether the repository must be rebuilt. If set to true, then
     * isValid() will return return <code>false</code>, forcing the
     * repository to be recompiled.
     *
     * @param mustRebuild
     *            <code>true</code> if this repository is always to be rebuilt
     *            instead of using its saved compiled state. <code>false</code>
     *            to leave it in its default state.
     */
    public final void setMustRebuild(boolean mustRebuild) {
        _mustRebuild = mustRebuild;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * True if this repository is always to be rebuilt instead of being used
     * using its saved compiled state.
     */
    protected boolean _mustRebuild = false;

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

    /**
     * Compute the path where a repository of a set of sources will be stored.
     * The path of the repository depends on the path where the sources e.g.
     * file(s), are stored.
     *
     * @param src
     *            Path where the sources are stored.
     * @return The path where the repository file will be stored.
     */
    protected File _repositoryPath(File src) {
        // The name of the repository will be:
        // (source) path / file . mmm
        // (repository) path / file . _extension
        String path = src.getParent();
        String base = StringManip.partBeforeLast(src.getName(), '.');
        String name = "." + base + "." + _getRepositoryExtension();
        return new File(path, name);
    }

    /**
     * Check if this repository exists and is completely up-to-date.
     *
     * @return true iff the repository exists and the sources have not changed
     *         since the repository was created.
     */
    protected boolean isValid() {
        if (_mustRebuild)
            return false;
        if (!_rep.exists())
            return false;
        return (_rep.lastModified() > _src.lastModified());
    }

    /**
     * Get the extension used by repositories of this file.
     *
     * @return the Extension used by repositories of this file.
     */
    protected abstract String _getRepositoryExtension();
}
