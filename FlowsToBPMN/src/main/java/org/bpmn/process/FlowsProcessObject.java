package org.bpmn.process;

import org.bpmn.bpmn_elements.BPMNElement;
import org.bpmn.bpmn_elements.event.IntermediateThrowEvent;
import org.bpmn.bpmn_elements.transition.Loop;
import org.bpmn.bpmn_elements.association.DataInputAssociation;
import org.bpmn.bpmn_elements.dataobject.DataObject;
import org.bpmn.bpmn_elements.event.EndEvent;
import org.bpmn.bpmn_elements.event.IntermediateCatchEvent;
import org.bpmn.bpmn_elements.event.StartEvent;
import org.bpmn.bpmn_elements.transition.SequenceFlow;
import org.bpmn.bpmn_elements.gateway.ExclusiveGateway;
import org.bpmn.bpmn_elements.gateway.Predicate;
import org.bpmn.bpmn_elements.task.Permission;
import org.bpmn.bpmn_elements.task.Step;
import org.bpmn.bpmn_elements.task.Task;
import org.bpmn.flows_entities.AbstractFlowsEntity;
import org.bpmn.parse_json.Parser;
import org.bpmn.randomidgenerator.RandomIdGenerator;
import org.bpmn.bpmn_elements.collaboration.participant.Pool;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bpmn.transformation.FlowsToBpmn.doc;
import static org.bpmn.transformation.LifecycleTransformation.*;

public class FlowsProcessObject {

    static int countProcess = 0;

    String id;

    static String isExecutable = "true";

    Element elementFlowsProcess;

    Pool participant;
    StartEvent startEvent;
    ArrayList<Task> tasks = new ArrayList<>();

    EndEvent endEvent;

    HashSet<ExclusiveGateway> gateways = new HashSet<>();

    ArrayList<SequenceFlow> flows = new ArrayList<>();

    HashSet<DataObject> dataObjects = new HashSet<>();

    ArrayList<Task> subprocesses = new ArrayList<>();

    ArrayList<AbstractFlowsEntity> objects;

    ArrayList<Loop> loops = new ArrayList<>();

    HashSet<DataObject> finishedDataObjects = new HashSet<>();

    HashSet<IntermediateCatchEvent> intermediateCatchEvents = new HashSet<>();

    HashSet<IntermediateThrowEvent> intermediateThrowEvents = new HashSet<>();

    HashSet<Element> associationFlows = new HashSet<>();

    boolean adHoc;

    boolean expandedSubprocess;


    public FlowsProcessObject(Pool participant, HashMap<Double, ArrayList<AbstractFlowsEntity>> objectTypeObjects, boolean adHoc, boolean expandedSubprocess) {

        this.id = "Process_" + RandomIdGenerator.generateRandomUniqueId(6);
        this.participant = participant;
        this.objects = objectTypeObjects.get(participant.getKey());
        this.adHoc = adHoc;
        this.expandedSubprocess = expandedSubprocess;
        setFlowsProcess();

    }

    private void setFlowsProcess() {

        Parser parser = new Parser();

        this.tasks = parser.parseTasks(this.participant, objects, adHoc);
        this.loops = parser.parseLoops(this, objects);
        predicates = parser.parsePredicates(objects);

        this.startEvent = new StartEvent();
        this.endEvent = new EndEvent();
        this.flows = parser.parseFlows(this, objects);

        setDataObjects();
        setAssociations();
        sortProcess();
        setEndTasks();
        addEndEventFlows();
        setSubProcesses();
        setBeforeAndAfterElements();

        setLoops();
        setGatewaysMachine();
        addFlowsToTasks();
        setGateways();
        setBeforeAndAfterElements();
        trimSequenceFlows();
        setBeforeAndAfterElements();

    }

    private void trimSequenceFlows() {


        // trim excl --> excl

        HashSet<SequenceFlow> flowsToRemove = new HashSet<>();
        HashSet<ExclusiveGateway> gatewaysToRemove = new HashSet<>();
        HashSet<SequenceFlow> flowsToAdd = new HashSet<>();
        Pattern p = Pattern.compile("^Exclusive_+");

        for (int i = 0; i < flows.size()-1; i++) {

            BPMNElement source = flows.get(i).getSourceRef();
            BPMNElement target = flows.get(i).getTargetRef();
            Matcher matchSource = p.matcher(source.getId());
            Matcher matchTarget = p.matcher(target.getId());

            if(matchSource.find() && matchTarget.find()){

                gatewaysToRemove.add((ExclusiveGateway) source);
                gatewaysToRemove.add((ExclusiveGateway) target);

                for(int j = i + 1; j < flows.size(); j++) {

                }

            }


        }

    }

    public HashSet<IntermediateCatchEvent> getIntermediateCatchEvents() {
        return intermediateCatchEvents;
    }

    public HashSet<IntermediateThrowEvent> getIntermediateThrowEvents() {
        return intermediateThrowEvents;
    }

    private void setStartEventElement() {

        Element elementStartEvent = startEvent.getElement();
        this.elementFlowsProcess.appendChild(elementStartEvent);

    }

    private void setEndEventElement() {

        Element elementEndEvent = endEvent.getElement();
        this.elementFlowsProcess.appendChild(elementEndEvent);

    }


    public void setBeforeAndAfterElements() {

        for (SequenceFlow flow : flows) {

            BPMNElement source = flow.getSourceRef();
            BPMNElement target = flow.getTargetRef();

            source.setAfterElement(target);
            target.setBeforeElement(source);

            source.setOutgoing(flow);
            target.setIncoming(flow);

        }

    }

    private void setLoops() {

        for (Loop loop : loops) {

            Task source = loop.getSource();
            Task target = loop.getTarget();
            BPMNElement beforeSource = source.getBeforeElement();
            BPMNElement afterTarget = target.getAfterElement();
            ExclusiveGateway beforeGateway = new ExclusiveGateway();
            ExclusiveGateway afterGateway = new ExclusiveGateway();

            gateways.add(beforeGateway);
            loop.setFirstGate(beforeGateway);
            gateways.add(afterGateway);
            loop.setSecondGate(afterGateway);

            flows.remove(getFlowBySourceAndTarget(beforeSource, source));
            flows.remove(getFlowBySourceAndTarget(target, afterTarget));

            // sourceBefore --> gateBefore
            beforeSource.setAfterElement(beforeGateway);
            flows.add(new SequenceFlow(beforeSource, beforeGateway));

            beforeGateway.addBeforeElement(beforeSource);
            beforeGateway.addAfterElement(source);
            flows.add(new SequenceFlow(beforeGateway, source));

            // target --> afterGate
            target.setAfterElement(afterGateway);
            flows.add(new SequenceFlow(target, afterGateway));
            afterGateway.addBeforeElement(target);

            afterGateway.addAfterElement(afterTarget);
            flows.add(new SequenceFlow(afterGateway, afterTarget));

            // afterGate --> beforeGate
            afterGateway.addAfterElement(beforeGateway);
            flows.add(new SequenceFlow(afterGateway, beforeGateway));

            afterTarget.setBeforeElement(afterGateway);

            allLoops.add(loop);

        }

    }

    public ArrayList<Loop> getLoops() {
        return loops;
    }

    private SequenceFlow getFlowBySourceAndTarget(BPMNElement source, BPMNElement target) {
        for (SequenceFlow flow : flows) {
            if (flow.getSourceRef().getId().equals(source.getId()) && flow.getTargetRef().getId().equals(target.getId())) {
                return flow;
            }
        }
        return null;
    }

    public SequenceFlow getFlowBySource(SequenceFlow flowIn) {
        for (SequenceFlow flow : flows) {
            if (flowIn.getSourceRef().equals(flow.getSourceRef())) {
                return flow;
            }
        }
        return null;
    }

    public SequenceFlow getFlowBySource(BPMNElement source) {
        for (SequenceFlow flow : flows) {
            if (flow.getSourceRef().getId() != null && flow.getSourceRef().getId().equals(source.getId())) {
                return flow;
            }
        }
        return null;
    }

    public SequenceFlow getFlowByTarget(BPMNElement target) {
        for (SequenceFlow flow : flows) {
            if (flow.getTargetRef().getId() != null && flow.getTargetRef().getId().equals(target.getId())) {
                return flow;
            }
        }
        return null;
    }

    private void setGatewaysMachine() {

        setSplitGateways();
        setJoinGateways();

    }

    private void setSplitGateways() {

        ArrayList<SequenceFlow> flowsToAdd = new ArrayList<>();
        ArrayList<SequenceFlow> flowsToRemove = new ArrayList<>();
        // add split gateways
        for (int i = 0; i < flows.size() - 1; i++) {

            boolean duplicate = false;
            ExclusiveGateway splitGateway = new ExclusiveGateway();
            BPMNElement outerElement = flows.get(i).getSourceRef();

            for (int j = i + 1; j < flows.size(); j++) {

                Pattern p = Pattern.compile("(Activity*|EndEvent*)");
                BPMNElement innerElement = flows.get(j).getSourceRef();
                Matcher m = p.matcher(innerElement.getId());

                if (m.find() && outerElement.getId().equals(innerElement.getId())) {
                    flowsToRemove.add(flows.get(j));
                    flowsToAdd.add(new SequenceFlow(splitGateway, flows.get(j).getTargetRef()));
                    duplicate = true;
                }

            }
            if (duplicate) {
                flowsToRemove.add(flows.get(i));
                flowsToAdd.add(new SequenceFlow(splitGateway, flows.get(i).getTargetRef()));
                flowsToAdd.add(new SequenceFlow(flows.get(i).getSourceRef(), splitGateway));
                gateways.add(splitGateway);
            }

        }

        flows.addAll(flowsToAdd);
        flows.removeAll(flowsToRemove);

    }

    private void setJoinGateways() {

        ArrayList<SequenceFlow> flowsToAdd = new ArrayList<>();
        ArrayList<SequenceFlow> flowsToRemove = new ArrayList<>();
        // add split gateways
        for (int i = 0; i < flows.size() - 1; i++) {

            boolean duplicate = false;
            ExclusiveGateway joinGateway = new ExclusiveGateway();
            BPMNElement outerElement = flows.get(i).getTargetRef();

            for (int j = i + 1; j < flows.size(); j++) {

                Pattern p = Pattern.compile("(Activity*|EndEvent*)");
                BPMNElement innerElement = flows.get(j).getTargetRef();
                Matcher m = p.matcher(innerElement.getId());

                if (m.find() && outerElement.getId().equals(innerElement.getId())) {
                    flowsToRemove.add(flows.get(j));
                    flowsToAdd.add(new SequenceFlow(flows.get(j).getSourceRef(), joinGateway));
                    duplicate = true;
                }

            }
            if (duplicate) {
                flowsToRemove.add(flows.get(i));
                flowsToAdd.add(new SequenceFlow(joinGateway, flows.get(i).getTargetRef()));
                flowsToAdd.add(new SequenceFlow(flows.get(i).getSourceRef(), joinGateway));
                gateways.add(joinGateway);

            }

        }

        flows.addAll(flowsToAdd);
        flows.removeAll(flowsToRemove);

    }

    /*private void setSubProcesses() {

        for (Task task : tasks) {
            if (task.getIsSubprocess()) {
                task.setStartEvent();
                task.setEndEvent();
                subprocesses.add(task);
                ArrayList<Step> steps = task.getSteps();
                SequenceFlow sfStart = new SequenceFlow(task.getStart(), steps.get(0));
                flows.add(sfStart);
                task.getElement().appendChild(sfStart.getElementSequenceFlow());
                task.getFlows().add(sfStart);
                task.getStart().setOutgoing(sfStart);
                for (int i = 0; i < task.getSteps().size() - 1; i++) {
                    SequenceFlow sf = new SequenceFlow(steps.get(i), steps.get(i + 1));
                    flows.add(sf);
                    task.getElement().appendChild(sf.getElementSequenceFlow());
                    task.getFlows().add(sf);
                }
                SequenceFlow sfEnd = new SequenceFlow(steps.get(task.getSteps().size() - 1), task.getEnd());
                flows.add(sfEnd);
                task.getElement().appendChild(task.getEnd().getElement());
                task.getElement().appendChild(sfEnd.getElementSequenceFlow());
                task.getFlows().add(sfEnd);
                task.getEnd().setIncoming(sfEnd);
            }
        }

    }
     */

    private void setSubProcesses() {

        for (Task task : tasks) {
            if (task.getIsSubprocess()) {
                subprocesses.add(task);
                for (Step step : task.getSteps()) {
                    for (SequenceFlow flow : flows) {
                        if (flow.getSourceRef().getId().equals(step.getId())) {
                            task.getElement().appendChild(flow.getElementSequenceFlow());
                            task.getFlows().add(flow);
                        }
                    }
                }

                /*
                SequenceFlow sfEnd = new SequenceFlow(steps.get(task.getSteps().size() - 1), task.getEnd());
                flows.add(sfEnd);
                task.getElement().appendChild(task.getEnd().getElement());
                task.getElement().appendChild(sfEnd.getElementSequenceFlow());
                task.getFlows().add(sfEnd);
                task.getEnd().setIncoming(sfEnd);

                 */
            }
        }

    }


    public Task getTaskById(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    private void setEndTasks() {

        for (Task task : tasks) {
            if (task.getAfter().size() == 0) {
                task.setIsEndTask();
                task.getAfter().add(endEvent);
            }
        }

    }

    private void setAssociations() {

        /*
        startEvent.setDataOutputAssociation();
        startEvent.getDataOutputAssociation().setOutputAssociationTarget(tasks.get(0).getDataObject());

         */

        for (Task task : tasks) {
            // add data output association
            // task.setDataOutputAssociation(); erledigt im Konstruktor, maybe buggy
            if (task.getIsSubprocess()) {
                for (Step step : task.getSteps()) {
                    if (step.getPermission() == Permission.READ) {
                        DataInputAssociation in = new DataInputAssociation();
                        Element tempSource = doc.createElement("bpmn:sourceRef");
                        Element tempTarget = doc.createElement("bpmn:targetRef");
                        tempSource.setTextContent(step.getDataObject().getRefId());
                        tempTarget.setTextContent("_property_placeholder");
                        in.getElementDataInputAssociation().appendChild(tempSource);
                        in.getElementDataInputAssociation().appendChild(tempTarget);
                        step.getDataInputAssociations().add(in);
                        step.getElement().appendChild(in.getElementDataInputAssociation());
                    } else {
                        step.setDataOutputAssociation();
                        step.getDataOutputAssociation().setOutputAssociationTarget(step.getDataObject());
                    }
                }
            }
            task.getDataOutputAssociation().setOutputAssociationTarget(task.getDataObject());
        }

    }

    public void setFlowsElement() {

        for (SequenceFlow flow : flows) {
            this.elementFlowsProcess.appendChild(flow.getElementSequenceFlow());
        }

    }

    private void setDataObjects() {

        for (Task task : tasks) {
            if (task.getIsSubprocess()) {
                for (Step step : task.getSteps()) {
                    dataObjects.add(step.getDataObject());
                }
            }
            dataObjects.add(task.getDataObject());
        }
    }

    private void setTaskElement() {
        for (Task task : tasks) {
            this.elementFlowsProcess.appendChild(task.getElement());
        }
    }

    private void setDataObjectsElement() {

        for (DataObject dObj : dataObjects) {

            if (dObj.getAssociatedTask().getClass().equals(Step.class)) {

                Step step = (Step) dObj.getAssociatedTask();
                Task subprocess = step.getAssociatedTask();
                subprocess.getElement().appendChild(step.getDataObject().getElementDataObject());
                subprocess.getElement().appendChild(step.getDataObject().getElementDataObjectSingle());

            } else {

                this.elementFlowsProcess.appendChild(dObj.getElementDataObject());
                this.elementFlowsProcess.appendChild(dObj.getElementDataObjectSingle());
            }

        }

    }

    private void addEndEventFlows() {

        for (Task task : tasks) {

            if (task.getIsEndTask()) {
                SequenceFlow endFlow = new SequenceFlow(task, endEvent);
                flows.add(endFlow);
            }

        }

    }

    public AbstractFlowsEntity getObjectById(Double id, ArrayList<AbstractFlowsEntity> objectTypeObjects) {

        return objectTypeObjects.stream().filter(obj -> obj != null && obj.getCreatedEntityId() != null && obj.getCreatedEntityId().equals(id)).collect(Collectors.toList()).get(0);
    }

    public Task getTaskById(Double id) {

        for (Task task : this.tasks) {

            if (task.getCreatedEntityId().equals(id)) {
                return task;
            }

            for (Task subTask : task.getSteps()) {
                if (subTask.getCreatedEntityId().equals(id)) {
                    return subTask;
                }
            }

        }

        return null;
    }

    private void sortProcess() {

        for (SequenceFlow flow : flows) {

            BPMNElement source = flow.getSourceRef();
            BPMNElement target = flow.getTargetRef();

            source.getAfter().add(target);
            target.getBefore().add(source);

        }

    }

    public void setElementFlowsProcess() {
        // for step 3; reseting first
        this.elementFlowsProcess = null;
        this.elementFlowsProcess = doc.createElement("bpmn:process");
        this.elementFlowsProcess.setAttribute("id", this.id);
        if (countProcess == 0) {
            this.elementFlowsProcess.setAttribute("isExecutable", this.isExecutable);
            countProcess++;
        } else {
            isExecutable = "false";
            this.elementFlowsProcess.setAttribute("isExecutable", this.isExecutable);
        }
        setStartEventElement();
        setEndEventElement();
        setDataObjectsElement();
        setTaskElement();
        setFlowsElement();
        setGatewaysElement();
        setIntermediateEvent();
    }

    private void setIntermediateEvent() {
        for (IntermediateCatchEvent event : intermediateCatchEvents) {
            this.elementFlowsProcess.appendChild(event.getElement());
        }
        for (IntermediateThrowEvent event : intermediateThrowEvents) {
            this.elementFlowsProcess.appendChild(event.getElement());
        }
    }

    public static void resetCountProcess() {
        countProcess = 0;
        isExecutable = "true";
    }

    private void addFlowsToTasks() {

        for (Task task : tasks) {
            if (task.getIsSubprocess()) {
                for (Step step : task.getSteps()) {
                    for (SequenceFlow sf : task.getFlows()) {
                        if (step.getId().equals(sf.getSourceRef().getId())) {
                            step.setOutgoing(sf);
                        }
                        if (step.getId().equals(sf.getTargetRef().getId())) {
                            step.setIncoming(sf);
                        }
                    }
                }
            }
            for (SequenceFlow sf : flows) {
                if (task.getId().equals(sf.getSourceRef().getId())) {
                    task.setOutgoing(sf);
                }
                if (task.getId().equals(sf.getTargetRef().getId())) {
                    task.setIncoming(sf);
                }
            }
        }

        // add flow to endEvent
        for (SequenceFlow sf : flows) {
            if (sf.getTargetRef().getId().equals(endEvent.getId())) {
                endEvent.setIncoming(sf);
            }
        }
    }

    private void setGateways() {

        for (ExclusiveGateway gate : gateways) {
            allGateways.add(gate);
            for (SequenceFlow sf : flows) {
                if (sf.getSourceRef().getId().equals(gate.getId())) {

                    //set predicate
                    BPMNElement target = sf.getTargetRef();
                    Double stateCreatedEntityId = target.getCreateId();
                    for (AbstractFlowsEntity transitionObj : objects) {
                        if (transitionObj != null
                                && transitionObj.getMethodName().equals("AddTransitionType")
                                && transitionObj.getParameters().get(1).equals(stateCreatedEntityId)) {

                            Double predicateId = (Double) transitionObj.getParameters().get(0);

                            for (Predicate predicate : predicates) {
                                if (predicate.getCreatedEntityId().equals(predicateId)) {
                                    // edge case, if excl --> excl --> condition (Insurance example)
                                    Pattern p = Pattern.compile("Gateway_*");
                                    Matcher m;
                                    boolean edgeCase = false;
                                    for (SequenceFlow flow : flows) {
                                        m = p.matcher(flow.getSourceRef().getId());
                                        if (m.find() && flow.getTargetRef().getId().equals(sf.getSourceRef().getId())) {
                                            flow.setName(predicate.getCondition());
                                            flow.getElementSequenceFlow().setAttribute("name", flow.getName());
                                            edgeCase = true;
                                        }
                                    }
                                    if (!edgeCase) {
                                        sf.setName(predicate.getCondition());
                                        sf.getElementSequenceFlow().setAttribute("name", sf.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private void setGatewaysElement() {
        for (ExclusiveGateway gate : gateways) {
            this.elementFlowsProcess.appendChild(gate.getElementExclusiveGateway());
        }
    }

    public Element getElementFlowsProcess() {
        return this.elementFlowsProcess;
    }

    public HashSet<DataObject> getDataObjects() {
        return dataObjects;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public String getId() {
        return this.id;
    }

    public EndEvent getEndEvent() {
        return endEvent;
    }

    public StartEvent getStartEvent() {
        return startEvent;
    }

    public ArrayList<SequenceFlow> getFlows() {
        return flows;
    }

    public HashSet<ExclusiveGateway> getGateways() {
        return gateways;
    }
}
