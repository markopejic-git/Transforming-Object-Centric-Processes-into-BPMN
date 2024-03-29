package org.bpmn.bpmn_elements.gateway;

import org.bpmn.bpmn_elements.BPMNElement;
import org.bpmn.bpmn_elements.collaboration.participant.Lane;
import org.bpmn.bpmn_elements.transition.SequenceFlow;
import org.bpmn.randomidgenerator.RandomIdGenerator;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;

import static org.bpmn.transformation.FlowsToBpmn.doc;

public class ExclusiveGateway implements BPMNElement {

    String id;

    boolean marked;

    ArrayList<SequenceFlow> incomings = new ArrayList<>();

    ArrayList<SequenceFlow> outgoings = new ArrayList<>();

    ArrayList<BPMNElement> beforeElements = new ArrayList<>();

    ArrayList<BPMNElement> afterElements = new ArrayList<>();

    Element elementExclusiveGateway;

    BPMNElement beforeElement;

    BPMNElement afterElement;

    boolean eventBased;

    boolean parallelGate = false;

    Lane lane;

    HashSet<SequenceFlow> incomingMarker;

    HashSet<SequenceFlow> outgoingMarker;

    String name;

    public ExclusiveGateway() {
        this.id = "Gateway_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.incomingMarker = new HashSet<>();
        this.outgoingMarker = new HashSet<>();
        this.elementExclusiveGateway = doc.createElement("bpmn:exclusiveGateway");
        setElementExclusiveGateway();
    }

    public boolean isParallelGate(){
        return this.parallelGate;
    }

    public ExclusiveGateway(boolean eventBased) {
        this.eventBased = eventBased;
        this.incomingMarker = new HashSet<>();
        this.outgoingMarker = new HashSet<>();
        this.id = "EventGateway_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.elementExclusiveGateway = doc.createElement("bpmn:eventBasedGateway");
        setElementExclusiveGateway();
    }

    public void setEventBased() {
        this.eventBased = true;
        this.id = "EventGateway_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.elementExclusiveGateway = doc.createElement("bpmn:eventBasedGateway");
        setElementExclusiveGateway();
    }

    public boolean getEventBased(){
        return this.eventBased;
    }

    public void setUser(Lane lane) {
        this.lane = lane;
    }

    @Override
    public Lane getUser() {
        return lane;
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public Double getCreateId() {
        return null;
    }

    @Override
    public void setOutgoing(SequenceFlow outgoing) {
        addOutgoing(outgoing);
    }

    @Override
    public void setIncoming(SequenceFlow incoming) {
        addIncoming(incoming);
    }

    @Override
    public SequenceFlow getOutgoing() {
        return null;
    }

    @Override
    public SequenceFlow getIncoming() {
        return null;
    }

    public void setEventBased(String id) {
        this.eventBased = true;
        this.id = id;
        this.elementExclusiveGateway = doc.createElement("bpmn:eventBasedGateway");
        setElementExclusiveGateway();
    }

    public void setExclusive(){
        this.eventBased = true;
        this.id = "Gateway_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.elementExclusiveGateway = doc.createElement("bpmn:exclusiveGateway");
        setElementExclusiveGateway();
    }

    private void setElementExclusiveGateway() {
        this.elementExclusiveGateway.setAttribute("id", this.id);
    }

    public Element getElementExclusiveGateway() {
        return this.elementExclusiveGateway;
    }

    public String getId() {
        return id;
    }

    public void addBeforeElement(BPMNElement element) {
        beforeElements.add(element);
    }

    public void addAfterElement(BPMNElement element) {
        afterElements.add(element);
    }

    public ArrayList<BPMNElement> getAfterElements() {
        return afterElements;
    }

    public ArrayList<BPMNElement> getBeforeElements() {
        return beforeElements;
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
        return beforeElement;
    }

    @Override
    public BPMNElement getAfterElement() {
        return afterElement;

    }

    @Override
    public void setBeforeElement(BPMNElement element) {
        this.beforeElement = element;
    }

    @Override
    public void setAfterElement(BPMNElement element) {
        this.afterElement = element;
    }

    @Override
    public Element getElement() {
        return null;
    }

    @Override
    public void setElement() {

    }

    public ArrayList<SequenceFlow> getIncomings() {
        return incomings;
    }

    public ArrayList<SequenceFlow> getOutgoings() {
        return outgoings;
    }

    public void addIncoming(SequenceFlow incoming) {
        if(!incomingMarker.contains(incoming)) {
            incomingMarker.add(incoming);
            Element temp = doc.createElement("bpmn:incoming");
            temp.setTextContent(incoming.getId());
            elementExclusiveGateway.appendChild(temp);
        }
    }

    public void addOutgoing(SequenceFlow outgoing) {
        if(!outgoingMarker.contains(outgoing)) {
            outgoingMarker.add(outgoing);
            Element temp = doc.createElement("bpmn:outgoing");
            temp.setTextContent(outgoing.getId());
            elementExclusiveGateway.appendChild(temp);
        }
    }

    public void setMarked() {
        this.marked = true;
    }


    public boolean getMarked() {
        return this.marked;
    }
    @Override
    public String toString() {
        return this.id;
    }

}