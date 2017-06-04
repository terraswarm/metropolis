/*
 * $Id: NetworkTest.java,v 1.5 2005/11/22 18:13:31 allenh Exp $
 */
package metropolis.metamodel.runtime.test;

import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.MMType;

public class NetworkTest {
    public NetworkTest(MMType type, Object userObject, String name, int objectID) {
        __pointer_myMMPort7_5_2 = new INode(type, userObject, name
                + "__pointer_myMMPort7_5_2", objectID);

        myMMPort20_1 = new INode(type, userObject, name + "myMMPort20_1",
                objectID + 1);

        _privateMMPort20_1 = new INode(type, userObject, name
                + "_privateMMPort20_1", objectID + 3);

    }

    // Used to test Network._connectPortPointer() and
    // Network.connect(Object, Object, Object)
    public INode __pointer_myMMPort7_5_2;

    // Used to test Network.getNthConnectionPort(Object, String, int)
    public INode myMMPort20_1;

    // Used to test Network.getNthConnectionPort(Object, String, int)
    private INode _privateMMPort20_1;
}
