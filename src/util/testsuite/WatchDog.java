/* WatchDog timer for tests

 @Copyright (c) 2003-2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 @ProposedRating Red (cxh@eecs.berkeley.edu)
 @AcceptedRating Red (cxh@eecs.berkeley.edu)

 */

package util.testsuite;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

// ////////////////////////////////////////////////////////////////////////
// // WatchDog
/**
 * This class creates a Timer that calls System.exit() after a certain amount of
 * time.
 *
 * @author Christopher Brooks
 * @version $Id: WatchDog.java,v 1.8 2006/10/12 20:31:52 cxh Exp $
 * @since Ptolemy II 2.2
 */
public class WatchDog {
    /**
     * Create a timer that will go off after timeToDie milliseconds.
     */
    public WatchDog(final long timeToDie) {

        // Timers are new in JDK1.3
        // For information about Timers, see
        // http://java.sun.com/docs/books/tutorial/essential/threads/timer.html

        if (timeToDie <= 0) {
            return;
        }
        // Make a record of the time when the WatchDog was set
        // final long startTime = (new Date()).getTime();

        TimerTask doTimeToDie = new TimerTask() {
            public void run() {
                try {
                    System.err.println("*** util.testsuite.WatchDog went "
                            + "off after " + timeToDie + "ms.");

                    // Get the root ThreadGroup
                    ThreadGroup parent, rootGroup;

                    parent = Thread.currentThread().getThreadGroup();
                    do {
                        rootGroup = parent;
                        parent = parent.getParent();
                    } while (parent != null);

                    // Display all the threads
                    Thread threads[] = new Thread[rootGroup.activeCount()];
                    rootGroup.enumerate(threads);
                    for (int i = 0; i < threads.length; i++) {
                        System.err.println(i + ". " + threads[i]);
                        // It would be nice to display the stack traces,
                        // but this is hard to do. Thread.dumpStack()
                        // only dumps the stack trace for the current thread.
                        // For an idea using Thread.stop(), see
                        // http://forum.java.sun.com/thread.jsp?forum=4&thread=178641&start=15&range=15&hilite=false&q=
                    }

                } catch (Exception e) {
                    System.err.println(e);
                } finally {
                    System.out.println("util.testsuite.WatchDog went off");
                    watchDogWentOff = true;
                    if (_exitOnTimeOut) {
                        System.out.println("The string below is so that "
                                + "the nightly build will notice");
                        System.out.println("Failed: 666  Total Tests: 0 "
                                + "((Passed: 0, Newly Passed: 0)  "
                                + "Known Failed: 0) "
                                + "util.testsuite.WatchDog went off");
                        // Do not pass go, do not collect $200
                        System.exit(4);
                    }
                }
            }
        };
        if (_timer == null) {
            // Create the timer as a Daemon.. This way it won't prevent
            // the compiler from exiting if an exception occurs.
            _timer = new Timer(true);
        }
        _timer.schedule(doTimeToDie, timeToDie);
    }

    public void cancel() {
        System.out.println("util.testsuite.WatchDog.cancel(): canceling "
                + (new Date()));
        if (_timer == null) {
            System.out.println("util.testsuite.WatchDog.cancel(): "
                    + "Warning: cancel called twice?");
        } else {
            _timer.cancel();
            _timer = null;
        }
    }

    /**
     * Determine whether the JVM will exit when the time interval has passed.
     * This method is used for testing this class.
     */
    public void setExitOnTimeOut(boolean exitOnTimeOut) {
        _exitOnTimeOut = exitOnTimeOut;
    }

    /**
     * Set to true if the watch time timer interval has passed. Used primarily
     * for testing.
     */
    public boolean watchDogWentOff = false;

    private Timer _timer = null;

    // If true, then exit if the interval passes
    private boolean _exitOnTimeOut = true;
}
