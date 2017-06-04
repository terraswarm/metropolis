/*
  Copyright (c) 2003-2004 The Regents of the University of California.
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
// LABEL

label{@
 {$
      beg{ beg_statement }
 end{ end_statement }
 $}
 statement;
 @} -->

//check if labeled statement can start to run.
pc->saveState();

if (flag) {
    beg_statement;
    pc->funcType = FunctionType::LABEL | FunctionType::ANNOTATION;
}
else
pc->funcType = FunctionType::LABEL;

pc->_currentEvent = beg(pc->p, this, "label");

bool stuck_flag = false, return_flag = false;

if (mode == RunType::EXEC)
{
    pc->setState(this, Keep_Unchanged_String, "label", SchedStateVal::UNKNOW);
    notify(DeltaCycle, _mng.pc->invoke);
    wait(pc->invoke);
}
else
{
    pc->setState(this, Keep_Unchanged_String, "label", SchedStateVal::TESTSCHEDULING);
    notify(DeltaCycle, _mng.pc->invoke);
    wait(pc->invoke);
}

if (pc->SchedState == SchedStateVal::RUN) {
    pc->SchedState = SchedStateVal::RUNNING;
    try {
        statement;
    } catch (STUCK) { stuck_flag = true; }
    catch (RETURN) { return_flag = true; }
}
else stuck_flag = true;

pc->restoreState();
if (stuck_flag) throw STUCK();
if (return_flag) throw RETURN();
} catch (STUCK) { throw; }
catch (RETURN) {};


if (flag) {
end_statement;
pc->funcType = FunctionType::LABEL | FunctionType::ANNOTATION;
}
else
pc->funcType = FunctionType::LABEL;

pc->_currentEvent = end(pc->p, this, "label");

if (mode == RunType::EXEC)
{
pc->setState(this, Keep_Unchanged_String, "label", SchedStateVal::UNKNOW);
notify(DeltaCycle, _mng.pc->invoke);
wait(pc->invoke);
}
else
{
    pc->setState(this, Keep_Unchanged_String, "label", SchedStateVal::TESTSCHEDULING);
    notify(DeltaCycle, _mng.pc->invoke);
    wait(pc->invoke);
}

// INTERFACE CALL FUNCTION

type functionName(args) {
    {$
         beg{ beg_statement }
    end{ end_statement }
    $}
    statement;
} -->

type functionName(args) {
    //check if labeled statement can start to run.
    pc->saveState();

    if (flag) {
        beg_statement;
        pc->funcType = pc->funcType = FunctionType::INTFCFUNC | FunctionType::ANNOTATION;
    }
    else
        pc->funcType = FunctionType::INTFCFUNC

            pc->_currentEvent = beg(pc->p, this, "functionName");

    bool stuck_flag = false, return_flag = false;

    if (mode == RunType::EXEC)
        {
            pc->setState(this, Keep_Unchanged_String, "functionName", SchedStateVal::UNKNOW);
            notify(DeltaCycle, _mng.pc->invoke);
            wait(pc->invoke);
        }
    else
        {
            pc->setState(this, Keep_Unchanged_String, "functionName", SchedStateVal::TESTSCHEDULING);
            notify(DeltaCycle, _mng.pc->invoke);
            wait(pc->invoke);
        }

    if (pc->SchedState == SchedStateVal::RUN) {
        pc->SchedState = SchedStateVal::RUNNING;
        try {
            statement;
        } catch (STUCK) { stuck_flag = true; }
        catch (RETURN) { return_flag = true; }
    }
    else stuck_flag = true;

    pc->restoreState();
    if (stuck_flag) throw STUCK();
    if (return_flag) throw RETURN();
} catch (STUCK) { throw; }
catch (RETURN) {};


if (flag) {
    end_statement;
    pc->funcType = FunctionType::INTFCFUNC | FunctionType::ANNOTATION;
}
else
pc->funcType = FunctionType::INTFCFUNC;

pc->_currentEvent = end(pc->p, this, "functionName");

if (mode == RunType::EXEC)
{
    pc->setState(this, Keep_Unchanged_String, "functionName", SchedStateVal::UNKNOW);
    notify(DeltaCycle, _mng.pc->invoke);
    wait(pc->invoke);
}
else
{
    pc->setState(this, Keep_Unchanged_String, "functionName", SchedStateVal::TESTSCHEDULING);
    notify(DeltaCycle, _mng.pc->invoke);
    wait(pc->invoke);
}

}

// AWAIT

await{
(cond1; testList1; setList1) { CS1; }
(cond2; testList2; setList2) { CS2; }
}

// await statement
//inner variables for constucting await
simpleList * list;
_2DList * tslist;
bool * condition;
int * preCsOrder;
bool flag = true, stuck_flag = false, return_flag = false;
bool mask[2]={true, true};

//save label, node, SchedState, funcType
pc->saveState();

//begin constructing await
pc->funcType = FunctionType::AWAIT;

//set testList for await statement
tslist = new _2DList;
list = new simpleList;
addPortIntfcEntry(list, "port", "interfaceName"); //testList1
tslist->push_front(list);
list = new simpleList;
addPortIntfcEntry(list, "port", "interfaceName"); //testList2
tslist->push_front(list);
pc->testList.push_front(tslist);

//set setList for await statement
tslist = new _2DList;
list = new simpleList;
addPortIntfcEntry(list, "port", "interfaceName"); //setList1
tslist->push_front(list);
list = new simpleList;
addPortIntfcEntry(list, "port", "interfaceName"); //setList2
tslist->push_front(list);
pc->setList.push_front(tslist);

condition = new bool[2];
do {
do {
preCsOrder = pc->csOrder;
pc->csOrder = intArrayGen(2, NONDET);
if (mode==RunType::EXEC) {
pc->setState(this, intfcName, Keep_Unchanged_String, SchedStateVal::EVALUATE);
notify(DeltaCycle, _mng.pc->invoke);
wait(pc->invoke);
}
pc->setState(this, intfcName, Keep_Unchanged_String, SchedStateVal::UNKNOW);
try {
if (mask[0]) {
condition[0] = (cond1);
} else condition[0] = false;
} catch (STUCK) {condition[0] = false; }
try {
if (mask[1]) {
condition[1] = (cond2);
} else condition[1] = false;
} catch (STUCK) {condition[1] = false; }
pc->cond = condition;

if (mode == RunType::EXEC)
{
notify(DeltaCycle, _mng.pc->invoke);
wait(pc->invoke);
}
else
{
    pc->setState(this, Keep_Unchanged_String, Empty_String, SchedStateVal::TESTSCHEDULING);
    notify(DeltaCycle, _mng.pc->invoke);
    wait(pc->invoke);
}


delete[] pc->csOrder;
pc->csOrder = preCsOrder;
} while (pc->SchedState != SchedStateVal::RUN && mode==RunType::EXEC);

if (pc->SchedState == SchedStateVal::RUN) {
    //start critical section within await
    pc->SchedState = SchedStateVal::RUNNING;

    sc_plist_iter<simpleList *> temp(tslist, 1);
    for (int i=0; i<tslist->size(); i++)
        {
            list = temp.get();
            if (i==pc->selected) break;
            temp--;
        }
    pc->preventList.push_front(list);

    switch (pc->selected)
        {
        case 0:
            try {
                CS1;
            } catch (STUCK) {
                mask[0] = false;
                continue;
            } catch (RETURN) { return_flag = true; }
            break;
        case 1:
            try {
                CS2;
            } catch (STUCK) {
                mask[1] = false;
                continue;
            } catch (RETURN) { return_flag = true; }
            break;
        }
    pc->preventList.pop_front();
}
else stuck_flag = true;
flag = false;
} while (flag);

//await ends
tslist = pc->setList.pop_front();
for (int i=0, j=tslist->size(); i<j; i++) {
    list = tslist->pop_front();
    for (int m=0, n=list->size(); m<n; m++) delete list->pop_front();
    delete list;
}
delete tslist;
tslist = pc->testList.pop_front();
for (int i=0, j=tslist->size(); i<j; i++) {
    list = tslist->pop_front();
    for (int m=0, n=list->size(); m<n; m++) delete list->pop_front();
    delete list;
}
delete tslist;
delete[] condition;
pc->restoreState();
if (stuck_flag) throw STUCK();
if (return_flag) throw RETURN();
} catch (STUCK) { throw; }
catch (RETURN) {};



constraint{
ltl sync(e1, e2 : e1.u==e2.u);
} -->

void synch() {
if (e1->getProcess()->pc->_currentEvent == e1 && e2->getProcess()->pc->_currentEvent == e2) {
if (e1->getAction()->getObject()->u != e2->getAction()->getObject()->u ) {
e1->getStatemedium()->setSchedState(SchedStateVal::DONTRUN);
e2->getStatemedium()->setSchedState(SchedStateVal::DONTRUN);
} else {
if (e1->getStatemedium()->getSchedState() == SchedStateVal::DONTRUN ||
e2->getStatemedium()->getSchedState() == SchedStateVal::DONTRUN ) {
e1->getStatemedium()->setSchedState(SchedStateVal::DONTRUN);
e2->getStatemedium()->setSchedState(SchedStateVal::DONTRUN);
}
}
}
}
