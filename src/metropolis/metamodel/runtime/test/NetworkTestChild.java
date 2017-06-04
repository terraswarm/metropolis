// $Id: NetworkTestChild.java,v 1.3 2005/11/22 18:13:31 allenh Exp $

package metropolis.metamodel.runtime.test;

import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.MMType;

public class NetworkTestChild extends NetworkTest {
    public NetworkTestChild(MMType type, Object userObject, String name,
            int objectID) {
        super(type, userObject, name, objectID);
        myMMPort20_4 = new INode(type, userObject, name + "myMMPort20_4",
                objectID + 5);
    }

    // Used to test Network.getNthConnectionPort(Object, String, int)
    public INode myMMPort20_4;

}
