package org.bpmn.bpmn_elements.event;

import org.bpmn.bpmn_elements.BPMNElement;
import org.bpmn.bpmn_elements.association.DataInputAssociation;
import org.bpmn.bpmn_elements.association.DataOutputAssociation;
import org.bpmn.bpmn_elements.collaboration.participant.Lane;
import org.bpmn.bpmn_elements.dataobject.DataObject;
import org.bpmn.bpmn_elements.transition.SequenceFlow;
import org.bpmn.bpmn_elements.task.Task;
import org.bpmn.randomidgenerator.RandomIdGenerator;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.bpmn.transformation.FlowsToBpmn.doc;

public class IntermediateCatchEvent implements BPMNElement{

    String id;

    ArrayList<BPMNElement> before = new ArrayList<>();

    ArrayList<BPMNElement> after = new ArrayList<>();

    Element elementCatchEvent;

    SequenceFlow outgoing;

    Element elementOutgoing;

    SequenceFlow incoming;

    Element elementIncoming;

    BPMNElement beforeElement;

    BPMNElement afterElement;

    DataOutputAssociation dataOutputAssociation;

    boolean parallelMultiple = false;

    HashSet<DataObject> dataObjects = new HashSet<>();

    String name;

    Lane lane;

    HashSet<DataInputAssociation> dataInputAssociations = new HashSet<>();

    HashMap<Task, DataObject> associatedTasks = new HashMap<Task, DataObject>();

    public IntermediateCatchEvent(String name, Lane lane) {
        this.id = "ReceiveActivity_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.name = name;
        this.lane = lane;
        if(this.lane != null) {
            lane.getElements().add(this);
        }
        this.elementCatchEvent = doc.createElement("bpmn:receiveTask");
        setElement();
    }

    public IntermediateCatchEvent(Lane lane) {
        this.id = "Event_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.lane = lane;
        if(this.lane != null) {
            lane.getElements().add(this);
        }
        this.elementCatchEvent = doc.createElement("bpmn:intermediateCatchEvent");
        setElement();
        this.elementIncoming = doc.createElement("bpmn:incoming");
        this.elementCatchEvent.appendChild(this.elementIncoming);
        this.elementOutgoing = doc.createElement("bpmn:outgoing");
        this.elementCatchEvent.appendChild(this.elementOutgoing);
    }

    public IntermediateCatchEvent(boolean parallelMultiple, Lane lane) {
        this.id = "Event_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.lane = lane;
        if(this.lane != null) {
            lane.getElements().add(this);
        }
        this.elementCatchEvent = doc.createElement("bpmn:intermediateCatchEvent");
        this.parallelMultiple = true;
        setElementMultiple();
        this.elementIncoming = doc.createElement("bpmn:incoming");
        this.elementCatchEvent.appendChild(this.elementIncoming);
        this.elementOutgoing = doc.createElement("bpmn:outgoing");
        this.elementCatchEvent.appendChild(this.elementOutgoing);
    }

    public boolean getParallelMultiple(){
        return this.parallelMultiple;
    }

    public HashMap<Task, DataObject> getAssociatedTasks() {
        return associatedTasks;
    }

    public HashSet<DataInputAssociation> getDataInputAssociations() {
        return dataInputAssociations;
    }

    public String getName() {
        return name;
    }

    @Override
    public Double getCreateId() {
        return null;
    }

    public void setName(String name) {
        this.name = name;
        this.elementCatchEvent.setAttribute("name", this.name);

    }

    public HashSet<DataObject> getDataObjects() {
        return dataObjects;
    }

    public Lane getUser() {
        return lane;
    }

    public void setUser(Lane lane) {
        this.lane = lane;
    }

    private void setElementMultiple() {
        this.elementCatchEvent.setAttribute("id", this.id);
        this.elementCatchEvent.setAttribute("name", this.name);
        this.elementCatchEvent.setAttribute("parallelMultiple", "true");
        Element cancelEventDefinition = doc.createElement("bpmn:cancelEventDefinition");
        cancelEventDefinition.setAttribute("id", "CancelEventDefinition_" + RandomIdGenerator.generateRandomUniqueId(6));
        Element terminateEventDefinition = doc.createElement("bpmn:terminateEventDefinition");
        terminateEventDefinition.setAttribute("id", "TerminateEventDefinition_" + RandomIdGenerator.generateRandomUniqueId(6));
        this.elementCatchEvent.appendChild(cancelEventDefinition);
        this.elementCatchEvent.appendChild(terminateEventDefinition);
    }

    public void setElement() {
        this.elementCatchEvent.setAttribute("id", this.id);

        Element messageEventDefinition = doc.createElement("bpmn:messageEventDefinition");
        messageEventDefinition.setAttribute("id", "MessageEventDefinition_" + RandomIdGenerator.generateRandomUniqueId(6));
        this.elementCatchEvent.appendChild(messageEventDefinition);
    }

    public void setIncoming(SequenceFlow incoming) {
        this.incoming = incoming;
        if (incoming != null) {
            this.elementIncoming.setTextContent(incoming.getId());
        }
    }

    @Override
    public SequenceFlow getOutgoing() {
        return this.outgoing;
    }

    @Override
    public SequenceFlow getIncoming() {
        return this.incoming;
    }

    public void setOutgoing(SequenceFlow outgoing) {
        this.outgoing = outgoing;
        if (outgoing != null) {
            this.elementOutgoing.setTextContent(outgoing.getId());
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public ArrayList<BPMNElement> getBefore() {
        return null;
    }

    @Override
    public ArrayList<BPMNElement> getAfter() {
        return null;
    }

    @Override
    public BPMNElement getBeforeElement() {
        return null;
    }

    @Override
    public BPMNElement getAfterElement() {
        return null;
    }

    @Override
    public void setBeforeElement(BPMNElement element) {

    }

    @Override
    public void setAfterElement(BPMNElement element) {

    }

    public Element getElement() {
        return elementCatchEvent;
    }

    public void setElement(Element elementCatchEvent) {
        this.elementCatchEvent = elementCatchEvent;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
