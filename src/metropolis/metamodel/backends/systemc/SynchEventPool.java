/*
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
package metropolis.metamodel.backends.systemc;

import metropolis.metamodel.MetaModelStaticSemanticConstants;
import metropolis.metamodel.Scope;
import metropolis.metamodel.ScopeIterator;
import metropolis.metamodel.StringManip;
import metropolis.metamodel.TreeNode;
import metropolis.metamodel.frontend.TypedDecl;
import metropolis.metamodel.nodetypes.CompileUnitNode;
import metropolis.metamodel.nodetypes.NodeClassID;
import metropolis.metamodel.nodetypes.TypeNameNode;
import metropolis.metamodel.nodetypes.TypeNode;
import metropolis.metamodel.nodetypes.UserTypeDeclNode;
import metropolis.metamodel.runtime.EqualVars;
import metropolis.metamodel.runtime.Event;
import metropolis.metamodel.runtime.INode;
import metropolis.metamodel.runtime.LTLSynch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A pool that stores all the synch event groups.
 *
 * @author Guang Yang
 * @version $Id: SynchEventPool.java,v 1.28 2006/10/12 20:33:12 cxh Exp $
 */
public class SynchEventPool implements NodeClassID {
    /**
     * All synch events stored (classified) due to their process IDs. Keys:
     * processIDs Objects: HashSet Objects in HashSet: OneSynchEvent
     */
    protected Hashtable _eventsOfProcesses;

    /**
     * All synch events stored (classified) due to their object IDs. Keys:
     * objectIDs Objects: HashSet Objects in HashSet: OneSynchEvent
     */
    protected Hashtable _eventsOfObjects;

    /**
     * All synch events stored (classified) due to their synch event group IDs.
     * Array element: HashSet for one synch event group Objects in HashSet:
     * OneSynchEvent
     */
    protected ArrayList _synchEventsGroup;

    public SynchEventPool() {
        _eventsOfProcesses = new Hashtable();
        _eventsOfObjects = new Hashtable();
        _synchEventsGroup = new ArrayList();
    }

    /**
     * Add one synch event group.
     *
     * @return the unique synch event group ID
     */
    private int addSynchEventsGroup() {
        for (int i = 0; i < _synchEventsGroup.size(); i++) {
            if (_synchEventsGroup.get(i) == null) {
                _synchEventsGroup.set(i, new HashSet());
                return i;
            }
        }
        _synchEventsGroup.add(new HashSet());
        return (_synchEventsGroup.size() - 1);
    }

    /**
     * Add one synch event in the corresponding synch event group.
     *
     * @param se
     *            the synch event
     */
    private void addSynchEvent(OneSynchEvent se) {
        Event e = se.getEvent();
        Integer processID = new Integer(e.getProcess().getObjectID());
        Integer objectID = new Integer(e.getNodeObject().getObjectID());
        HashSet ph = (HashSet) _eventsOfProcesses.get(processID);
        if (ph == null) {
            ph = new HashSet();
            _eventsOfProcesses.put(processID, ph);
        }
        ph.add(se);
        HashSet oh = (HashSet) _eventsOfObjects.get(objectID);
        if (oh == null) {
            oh = new HashSet();
            _eventsOfObjects.put(objectID, oh);
        }
        oh.add(se);
    }

    /**
     * Add one LTLSynch instance (from elaboration) into the synch event pool.
     *
     * @param ltlSynch
     *            one LTLSynch instance
     */
    public void addLTLSynch(LTLSynch ltlSynch) {
        String typeString[] = { "bool", "char", "double", "float", "int",
                "long", "String" };
        LinkedList events = ltlSynch.getEvents();
        if (events.size() != 2)
            throw new RuntimeException(
                    "Only two events in synch statement are permitted currently.");
        Event e1 = null;
        Event e2 = null;
        String v1 = null;
        String v2 = null;
        String type1 = null;
        boolean isNondet1 = false;
        String type2 = null;
        boolean isNondet2 = false;
        EqualVars eqv = ltlSynch.getEqualVars();

        boolean hasEqualVars = (eqv != null);

        if (hasEqualVars) {
            v1 = eqv.getValue1();
            if (eqv.getType1() == EqualVars.VARTYPE) {
                e1 = eqv.getEvent1();
                if (e1 == null)
                    throw new RuntimeException(
                            "The first event in variable comparison part is null."
                                    + ltlSynch.show());
                try {
                    type1 = getVarType(e1, v1);
                } catch (NoSuchElementException nse) {
                    System.err.println("No variable with name '" + v1
                            + "' exists in the scope of event " + e1);
                    throw nse;
                }
                isNondet1 = type1.startsWith("Nondet");
                type1 = StringManip.partAfterLast(type1, '.');
            } else {
                type1 = "constant." + typeString[eqv.getType1()];
            }

            v2 = eqv.getValue2();
            if (eqv.getType2() == EqualVars.VARTYPE) {
                e2 = eqv.getEvent2();
                if (e2 == null)
                    throw new RuntimeException(
                            "The second event in variable comparison part is null."
                                    + ltlSynch.show());
                try {
                    type2 = getVarType(e2, v2);
                } catch (NoSuchElementException nse) {
                    System.err.println("No variable with name '" + v2
                            + "' exists in the scope of event " + e2);
                    throw nse;
                }
                isNondet2 = type2.startsWith("Nondet");
                type2 = StringManip.partAfterLast(type2, '.');
            } else {
                type2 = "constant." + typeString[eqv.getType2()];
            }

            if (e1 == null)
                if ((ltlSynch.getEvents().get(0)).equals(e2))
                    e1 = (Event) ltlSynch.getEvents().get(1);
                else
                    e1 = (Event) ltlSynch.getEvents().get(0);

            if (e2 == null)
                if ((ltlSynch.getEvents().get(1)).equals(e1))
                    e2 = (Event) ltlSynch.getEvents().get(0);
                else
                    e2 = (Event) ltlSynch.getEvents().get(1);
        } else {
            e1 = (Event) ltlSynch.getEvents().get(0);
            e2 = (Event) ltlSynch.getEvents().get(1);
        }

        int[] groupIDs = lookupSynchEventsGroup(e1, e2);
        int groupID;
        if (groupIDs.length == 0) { // Not synched with any existing group
            groupID = addSynchEventsGroup();
        } else if (groupIDs.length == 1) { // synched with only one existing
            // group
            groupID = groupIDs[0];
        } else { // synched with more than one existing group, which implies
            // all these groups need to be merged.
            groupID = mergeSynchEventsGroup(groupIDs);
        }

        HashSet synchGroup = (HashSet) _synchEventsGroup.get(groupID);

        Iterator iter = synchGroup.iterator();
        OneSynchEvent se1 = null;
        OneSynchEvent se2 = null;
        while (iter.hasNext() && (se1 == null || se2 == null)) {
            OneSynchEvent se = (OneSynchEvent) iter.next();
            if (se.getEvent().equals(e1))
                se1 = se;
            if (se.getEvent().equals(e2))
                se2 = se;
        }

        if (se1 == null)
            se1 = new OneSynchEvent(e1);
        VarAttribute va1 = null;
        if (hasEqualVars)
            va1 = se1.addSynchVar(v1, type1, isNondet1);
        se1.setSynchGroupID(groupID);
        synchGroup.add(se1);

        if (se2 == null)
            se2 = new OneSynchEvent(e2);
        VarAttribute va2 = null;
        if (hasEqualVars)
            va2 = se2.addSynchVar(v2, type2, isNondet2);
        se2.setSynchGroupID(groupID);
        synchGroup.add(se2);

        addSynchEvent(se1);
        addSynchEvent(se2);

        if (hasEqualVars) {
            if (isInEqualVarSet(va1, va2))
                return;
            VarAttribute va1next = va1.getEqualVar();
            VarAttribute va2next = va2.getEqualVar();
            va1.setEqualVar(va2next);
            va2.setEqualVar(va1next);
        }
    }

    /**
     * Compact the inner data structure (_synchEventsGroup). Reorder the synch
     * event groups and reassign their synch event group IDs if needed.
     */
    public void compactSynchEventsStorage() {
        for (int i = 0; i < _synchEventsGroup.size();) {
            HashSet hs = (HashSet) _synchEventsGroup.get(i);
            if (hs == null)
                _synchEventsGroup.remove(i);
            else {
                Iterator iter = hs.iterator();
                while (iter.hasNext()) {
                    OneSynchEvent se = (OneSynchEvent) iter.next();
                    se.setSynchGroupID(i);
                }
                i++;
            } // end if
        } // end for
        _synchEventsGroup.trimToSize();
    }

    /**
     * Get the type of the variable.
     *
     * @param e
     *            Event in whose scope the variable is defined
     * @param var
     *            Variable name
     * @return the type of the variable. If the variable is nondeterministic,
     *         return "Nondet."+variable type
     */
    public static String getVarType(Event e, String var) {
        // Fix Me: var is regarded as simple variable.
        // If it is an array element, this part should be refined more
        // carefully.
        INode obj = e.getNodeObject();
        String fullName = obj.getType().getName();
        String eventName = e.getName();
        int kind = obj.getType().getKind();
        UserTypeDeclNode node = SystemCBackend._findObjectAST(fullName, kind);
        LinkedList args = new LinkedList();
        args.addLast(fullName);
        args.addLast(eventName);
        TreeNode tnode = node;
        while (!(tnode instanceof CompileUnitNode))
            tnode = tnode.getParent();
        tnode = (TreeNode) tnode.accept(new ASTLookupVisitor(), args);

        Scope s = (Scope) tnode
                .getDefinedProperty(MetaModelStaticSemanticConstants.SCOPE_KEY);
        ScopeIterator siter = s.lookupFirst(var,
                MetaModelStaticSemanticConstants.CG_FIELD
                        | MetaModelStaticSemanticConstants.CG_PARAMETER
                        | MetaModelStaticSemanticConstants.CG_LOCALVAR
                        | MetaModelStaticSemanticConstants.CG_FORMAL, false);
        TypedDecl decl;
        try {
            decl = (TypedDecl) siter.nextDecl();
        } catch (NoSuchElementException ex) {
            e.toString();
            System.err.println("There is no variable '" + var
                    + "' in the scope of " + e.toString());
            throw ex;
        }
        TypeNode tn = decl.getType();
        String tnName = "";
        switch (tn.classID()) {
        case BOOLTYPENODE_ID:
            tnName = "bool";
            break;
        case CHARTYPENODE_ID:
            tnName = "char";
            break;
        case BYTETYPENODE_ID:
            tnName = "byte";
            break;
        case SHORTTYPENODE_ID:
            tnName = "short";
            break;
        case INTTYPENODE_ID:
            tnName = "int";
            break;
        case FLOATTYPENODE_ID:
            tnName = "float";
            break;
        case LONGTYPENODE_ID:
            tnName = "long";
            break;
        case DOUBLETYPENODE_ID:
            tnName = "double";
            break;
        case TYPENAMENODE_ID:
            String t = ((TypeNameNode) tn).getName().getIdent();
            if (t.equals("Nondet"))
                tnName = "Nondet." + "int";
            else
                tnName = t;
        }
        return tnName;

        /*
         * Object userObj = obj.getUserObject(); Class cls = userObj.getClass();
         * String clsName = cls.getName();
         *
         * while (cls!=null && !cls.getName().equals("java.lang.Object")) {
         * Field f[] = cls.getDeclaredFields();
         * AccessibleObject.setAccessible(f, true); for (int i=0; i<f.length;
         * i++) { //if ((f[i].getModifiers() &
         * java.lang.reflect.Modifier.FINAL)!=0) continue; if
         * (f[i].getName().equals(var)) { Class typeClass = f[i].getType(); if
         * (typeClass.getName().equals("metamodel.lang.Nondet")) { return
         * "Nondet."+"int"; } else { return typeClass.getName(); } } } cls =
         * cls.getSuperclass(); if (cls.getName().startsWith("metamodel.lang"))
         * cls=null; }
         */

        // return "";
    }

    //    /**
    //     * Find the OneSynchEvent instance which represents the event.
    //     *
    //     * @param e
    //     *            an event
    //     * @return the OneSynchEvent instance which represents the event
    //     */
    //    private OneSynchEvent lookupEvent(Event e) {
    //        int processID = e.getProcess().getObjectID();
    //        HashSet pEvents = (HashSet) _eventsOfProcesses.get(new Integer(
    //                processID));
    //        if (pEvents == null)
    //            return null;
    //        if (pEvents.isEmpty())
    //            return null;
    //        Iterator pEventsIter = pEvents.iterator();
    //        while (pEventsIter.hasNext()) {
    //            OneSynchEvent se = (OneSynchEvent) pEventsIter.next();
    //            Event e1 = se.getEvent();
    //            if (e1.equals(e))
    //                return se;
    //        }
    //        return null;
    //    }

    /**
     * During adding events into the synch event pool, it is possible to have
     * more than one OneSynchEvent instances. Find all such OneSynchEvent
     * instances indicated by their positions in the ArrayList
     * _synchEventsGroup.
     *
     * @param e1
     *            one synched event
     * @param e2
     *            the other synched event
     * @return integer array indicating the positions in the ArrayList where the
     *         two events belongs to the synch event groups
     */
    private int[] lookupSynchEventsGroup(Event e1, Event e2) {
        int totalNumGroup = _synchEventsGroup.size();
        LinkedList found = new LinkedList();

        for (int i = 0; i < totalNumGroup; i++) {
            HashSet ithGroup = (HashSet) _synchEventsGroup.get(i);
            if (ithGroup == null)
                continue;
            Iterator iter = ithGroup.iterator();
            while (iter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                if (se.getEvent().equals(e1) || se.getEvent().equals(e2)) {
                    found.addLast(new Integer(i));
                    break;
                }
            }
        }

        int[] ret = new int[found.size()];
        for (int i = 0; i < found.size(); i++)
            ret[i] = ((Integer) found.get(i)).intValue();

        return ret;
    }

    /**
     * Merge the synch event groups.
     *
     * @param groupIDs
     *            synch event group IDs
     * @return the new unique synch event group ID after merging
     */
    private int mergeSynchEventsGroup(int[] groupIDs) {
        int destGroupID = groupIDs[0];
        HashSet destGroup = (HashSet) _synchEventsGroup.get(destGroupID);

        for (int i = 1; i < groupIDs.length; i++) {
            int srcGroupID = groupIDs[i];
            HashSet srcGroup = (HashSet) _synchEventsGroup.get(srcGroupID);
            Iterator iter = srcGroup.iterator();
            while (iter.hasNext()) {
                OneSynchEvent se = (OneSynchEvent) iter.next();
                se.setSynchGroupID(destGroupID);
            }
            destGroup.addAll(srcGroup);
            _synchEventsGroup.set(srcGroupID, null);
        }

        return destGroupID;
    }

    /**
     * Test whether var is in the equal variable set (organized as a loop of
     * VarAttribute) starting from source.
     *
     * @param source
     *            Indicates a set of equal variables organized as a loop
     * @param var
     *            The variable under test
     * @return in or out of the equal variable loop
     */
    private boolean isInEqualVarSet(VarAttribute source, VarAttribute var) {
        if (source == var)
            return true;
        VarAttribute v = source.getEqualVar();
        while (v != source) {
            if (v == var)
                return true;
            v = v.getEqualVar();
        }
        return false;
    }

    /**
     * Add one synch event in a synch event group.
     *
     * @param synchEventGroupID
     *            synch event group ID
     * @param e
     *            the event being added
     */
    public void addEvent(int synchEventGroupID, Event e) {
        HashSet group = (HashSet) _synchEventsGroup.get(synchEventGroupID);
        group.add(e);
    }

    /**
     * Get the number of synch event groups.
     *
     * @return the number of synch event groups
     */
    public int getNumSynchEventsGroups() {
        int i = 0;
        int j = 0;
        int size = _synchEventsGroup.size();
        while (j < size) {
            if (_synchEventsGroup.get(j) != null)
                i++;
            j++;
        }
        return i;
    }

    /**
     * Get synch events classified due to their object IDs.
     *
     * @return Iterator to synch events classified due to their object IDs
     */
    public Iterator getSynchEventsOfObjects() {
        return _eventsOfObjects.values().iterator();
    }

    /**
     * Get synch events classified due to their process IDs.
     *
     * @return Iterator to synch events classified due to their process IDs
     */
    public Iterator getSynchEventsOfProcesses() {
        return _eventsOfProcesses.values().iterator();
    }

    /**
     * Get all groups of synch events.
     *
     * @return An ArrayList having all groups of synch events
     */
    public ArrayList getSynchEventsGroups() {
        return _synchEventsGroup;
    }

    /**
     * Get the i'th synch event group.
     *
     * @param ith
     *            synch event group index
     * @return A HashSet having the i'th synch event group
     */
    public HashSet getIthSynchEventsGroup(int ith) {
        return (HashSet) _synchEventsGroup.get(ith);
    }
}
