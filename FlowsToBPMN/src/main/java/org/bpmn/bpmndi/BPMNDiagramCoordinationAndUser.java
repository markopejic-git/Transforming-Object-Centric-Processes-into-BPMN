package org.bpmn.bpmndi;

import org.bpmn.bpmn_elements.BPMNElement;
import org.bpmn.bpmn_elements.transition.Loop;
import org.bpmn.bpmn_elements.association.DataInputAssociation;
import org.bpmn.bpmn_elements.collaboration.Collaboration;
import org.bpmn.bpmn_elements.collaboration.participant.Participant;
import org.bpmn.bpmn_elements.collaboration.participant.Lane;
import org.bpmn.bpmn_elements.dataobject.DataObject;
import org.bpmn.bpmn_elements.event.IntermediateCatchEvent;
import org.bpmn.bpmn_elements.transition.MessageFlow;
import org.bpmn.bpmn_elements.transition.SequenceFlow;
import org.bpmn.bpmn_elements.task.Step;
import org.bpmn.bpmn_elements.task.Task;
import org.bpmn.process.FlowsProcessObject;
import org.bpmn.randomidgenerator.RandomIdGenerator;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bpmn.bpmn_elements.collaboration.Collaboration.pools;
import static org.bpmn.transformation.FlowsToBpmn.doc;

public class BPMNDiagramCoordinationAndUser {

    final double participantX = 70.0;

    double participantWidth = 2000.0;

    double participantHeight = 800.0;

    double participantYInc;

    final double startEventYInc = 200.0;

    final double loopOffset = 180.0;

    final double multipleLoopOffset = 10.0;

    final double startEventX = 200.0;

    final double eventWidth = 36.0;

    final double eventHeight = 36.0;

    final double activityWidth = 100.0;

    final double activityHeight = 80.0;

    final double gatewayWidth = 50.0;

    final double gatewayHeight = 50.0;

    final double flowsLength = 52.0;

    final double flowsHeight = 150.0;

    final double dataObjectWidth = 88.0;

    final double dataObjectHeight = 97.0;

    final double subProcessHeight = 300.0;

    final double subProcessWidthOffset = 130.0;

    double poolHeight;

    final double poolHeightOffset = 20;

    final double xTaskOffset = 160;

    final double subProcessOffsetY = 110.0;

    double poolWidth;

    ArrayList<Shape> temp = new ArrayList<>();

    HashSet<String> printMark = new HashSet<>();

    HashSet<String> targetMark = new HashSet<>();

    HashSet<Shape> shapes = new HashSet<>();

    HashSet<Shape> stepShapes = new HashSet<>();

    HashSet<Shape> allShapes = new HashSet<>();

    HashMap<Shape, BPMNElement> shapeBPMNElementHashMap = new HashMap<>();
    double participantStartY = 100.0;

    double startEventY;

    public void f(double x, double y, String e, Participant object, ArrayList<SequenceFlow> flows, String previous, boolean expandedSubprocess) {

        ArrayList<String> list = new ArrayList<>();
        FlowsProcessObject fp = object.getProcessRef();

        // extract in method to recognize if activity, event or gateway
        Pattern activityPattern = Pattern.compile("Activity_+");
        Pattern eventPattern = Pattern.compile("Event_+");
        Pattern gatewayPattern = Pattern.compile(".*Gateway_+");
        Matcher activityMatcher = activityPattern.matcher(e);
        Matcher eventMatcher = eventPattern.matcher(e);
        Matcher gatewayMatcher = gatewayPattern.matcher(e);
        Matcher activityMatcherPrev = null;
        Matcher eventMatcherPrev = null;
        Matcher gatewayMatcherPrev = null;
        Bounds tempBounds = null;

        if (previous != null) {
            activityMatcherPrev = activityPattern.matcher(previous);
            eventMatcherPrev = eventPattern.matcher(previous);
            gatewayMatcherPrev = gatewayPattern.matcher(previous);
        }

        for (SequenceFlow sf : flows) {

            BPMNElement sourceElement = sf.getSourceRef();
            BPMNElement targetElement = sf.getTargetRef();

            String source = sourceElement.getId();
            String target = targetElement.getId();

            if (e.equals(source)) {

                if (!printMark.contains(e)) {

                    if (previous != null) {
                        if (activityMatcher.find()) {
                            if (expandedSubprocess && fp.getTaskById(previous) != null && fp.getTaskById(previous).getIsSubprocess()) {
                                x += 145 + fp.getTaskById(previous).getSteps().size() * subProcessWidthOffset - activityWidth;
                            } else {
                                x += 145;
                            }
                            if (eventMatcherPrev.find()) {
                                x -= 67;
                                y -= 22;
                            } else if (gatewayMatcherPrev.find()) {
                                x -= 55;
                                y -= 15;
                            }
                            tempBounds = new Bounds(x, y, activityWidth, activityHeight);
                        } else if (eventMatcher.find()) {

                            x += 90;
                            y += 3;
                            if (activityMatcherPrev.find()) {
                                if (expandedSubprocess && fp.getTaskById(previous) != null && fp.getTaskById(previous).getIsSubprocess()) {
                                    x += 50 - fp.getTaskById(previous).getSteps().size() * subProcessWidthOffset + activityWidth;
                                } else {
                                    x += 50;
                                }
                                y += 20;
                            }
                            tempBounds = new Bounds(x, y, eventWidth, eventHeight);
                        } else if (gatewayMatcher.find()) {
                            x += 160;
                            if (activityMatcherPrev.find()) {
                                if (expandedSubprocess && fp.getTaskById(previous) != null && fp.getTaskById(previous).getIsSubprocess()) {
                                    x -= 20 - fp.getTaskById(previous).getSteps().size() * subProcessWidthOffset + activityWidth;
                                } else {
                                    x -= 20;
                                }
                                y += 15;
                            } else if (eventMatcherPrev.find()) {
                                x -= 87;
                                y -= 7;
                            }
                            tempBounds = new Bounds(x, y, gatewayWidth, gatewayHeight);
                        }
                    } else {
                        if (activityMatcher.find()) {
                            tempBounds = new Bounds(x, y, activityWidth, activityHeight);
                        } else if (eventMatcher.find()) {
                            tempBounds = new Bounds(x, y, eventWidth, eventHeight);
                        } else if (gatewayMatcher.find()) {
                            tempBounds = new Bounds(x, y, gatewayWidth, gatewayHeight);
                        }
                    }

                    Shape tempShape;
                    if (expandedSubprocess && fp.getTaskById(e) != null && fp.getTaskById(e).getIsSubprocess()) {
                        ArrayList<Step> steps = fp.getTaskById(e).getSteps();
                        Double subProcessWidth = steps.size() * subProcessWidthOffset;
                        tempBounds = new Bounds(x, y - subProcessOffsetY, subProcessWidth, subProcessHeight);
                        tempShape = new Shape(e, expandedSubprocess, tempBounds);
                    } else {
                        tempShape = new Shape(e, expandedSubprocess, tempBounds);
                    }

                    tempShape.setBounds();

                    tempShape.setShapeParticipant();

                    shapes.add(tempShape);
                    shapeBPMNElementHashMap.put(tempShape, sourceElement);
                    printMark.add(e);
                }

                if (!targetMark.contains(target)) {
                    list.add(target);
                    targetMark.add(target);
                }

            }

        }
        //x += flowsLength;
        int cntElements = list.size();
        if (cntElements > 1) {
            for (int t = cntElements - 1; t >= 0; t--) {
                f(x, y, list.get(t), object, flows, e, expandedSubprocess);
                y -= 100;
            }
        } else if (cntElements == 1) {
            f(x, y, list.get(0), object, flows, e, expandedSubprocess);
        } else if (!printMark.contains(e)) {
            //double tempX = x - flowsLength;
            //TODO: MAYBE BUGGY
            String end = fp.getEndEvent().getId();
            printMark.add(end);

            if (gatewayMatcherPrev.find()) {
                x += 86;
                y += 6;
            } else if (activityMatcherPrev.find()) {
                x += 141;
                y += 21;
            }
            tempBounds = new Bounds(x, y, eventWidth, eventHeight);
            Shape tempShape = new Shape(e, tempBounds);
            shapes.add(tempShape);
            tempShape.setShapeParticipant();
            shapeBPMNElementHashMap.put(tempShape, fp.getEndEvent());
        }

    }

    public void parseFlows(Element rootElement, Participant object, double x, double y, boolean expandedSubprocess, boolean lane) {

        //bring elements of pool in order according to flows

        FlowsProcessObject fp = object.getProcessRef();
        String start = fp.getStartEvent().getId();
        ArrayList<SequenceFlow> flows = fp.getFlows();
        printMark.clear();
        targetMark.clear();


        f(x, y, start, object, flows, null, expandedSubprocess);
        if (lane) {
            q(object, rootElement, expandedSubprocess);
        }
        for (Shape shape : shapes) {
            shape.setBounds();
            rootElement.appendChild(shape.getBpmnElement());
        }

        addFlowsEdge(rootElement, flows, fp);

        //addDataObjects(rootElement, flows);
        //addDataObjectsOutput(rootElement, tasks, expandedSubprocess);

        allShapes.addAll(shapes);
        shapes.clear();

    }

    private void q(Participant object, Element rootElement, boolean expandedSubprocess) {

        for (Map.Entry<Shape, BPMNElement> entry : shapeBPMNElementHashMap.entrySet()) {

            Shape shape = entry.getKey();
            BPMNElement bpmnElement = entry.getValue();
            Lane lane = bpmnElement.getUser();

            for (Map.Entry<Lane, org.bpmn.process.Lane> laneMap : object.getLanes().entrySet()) {

                org.bpmn.process.Lane entryLane = laneMap.getValue();

                if (lane.getId().equals(laneMap.getKey().getId()) && !shape.getMarked()) {
                    Double yOffSet;
                    if (expandedSubprocess && bpmnElement.getClass().equals(Task.class) && ((Task) bpmnElement).getIsSubprocess() && !shape.getMarked()) {
                        yOffSet = shape.getBounds().getY() - entryLane.getParticipantMiddleY() + entryLane.getMiddleY() - subProcessHeight / 4 + activityHeight * 0.96;
                    } else {
                        yOffSet = shape.getBounds().getY() - entryLane.getParticipantMiddleY() + entryLane.getMiddleY();
                    }
                    shape.getBounds().setY(yOffSet);
                    //TODO: Steps ohne XOR print
                    if (expandedSubprocess && bpmnElement.getClass().equals(Task.class) && ((Task) bpmnElement).getIsSubprocess() && !shape.getMarked()) {

                        Task subprocess = (Task) bpmnElement;
                        ArrayList<Step> steps = subprocess.getSteps();
                        Double subProcessWidth = steps.size() * subProcessWidthOffset;

                        Double stepOffset = (subProcessWidth - steps.size() * activityWidth) / (steps.size() + 1);
                        for (int i = 0; i < steps.size(); i++) {
                            Step step = steps.get(i);
                            Bounds stepBounds = new Bounds(shape.getBounds().getX() + stepOffset + (stepOffset + activityWidth) * i, shape.getBounds().getY() - subProcessOffsetY + subProcessHeight / 2, activityWidth, activityHeight);
                            Shape stepShape = new Shape(step.getId(), stepBounds);
                            stepShape.setBounds();
                            stepShape.setShapeParticipant();
                            stepShapes.add(stepShape);
                            rootElement.appendChild(stepShape.getBpmnElement());

                        }
                    }
                    shape.setMarked();
                }
            }
        }
    }

    public void fillBPMNDI(String id, Element rootElement, Collaboration collaboration, boolean visibleMessageFlows,
                           boolean expandedSubprocess, boolean lane) {

        Element bpmnDiagram = doc.createElement("bpmndi:BPMNDiagram");
        bpmnDiagram.setAttribute("id", id);
        rootElement.appendChild(bpmnDiagram);

        Element bpmnLane = doc.createElement("bpmndi:BPMNPlane");
        bpmnLane.setAttribute("id", "BPMNlane_" + RandomIdGenerator.generateRandomUniqueId(6));
        bpmnLane.setAttribute("bpmnElement", collaboration.getId());
        bpmnDiagram.appendChild(bpmnLane);

        if (lane){
            participantWidth = 1700.0;
            participantHeight = 3000.0;
            participantYInc = participantHeight + 50.0;
        }

        for (Participant object : pools) {

            // add pools
            addParticipantsShape(bpmnLane, object, this.participantStartY);

            parseFlows(bpmnLane, object, startEventX, this.startEventY, expandedSubprocess, lane);


        }


        for (Participant object : pools) {
            addDataObjectsInput(bpmnLane, object.getProcessRef().getIntermediateCatchEvents());
        }


        if (visibleMessageFlows) {
            setMessageFlows(bpmnLane, collaboration);
        }
    }

    private void setMessageFlows(Element rootElement, Collaboration collaboration) {

        HashSet<MessageFlow> messageFlows = collaboration.getMessageFlows();

        for (MessageFlow mf : messageFlows) {

            Edge messageFlow = new Edge(mf.getId());

            Shape bsSource = getBPMNShapeByFlowAllShapes(mf.getSourceRef().getId());
            Shape bsTarget = getBPMNShapeByFlowAllShapes(mf.getTargetRef().getId());

            // JSON BUG FINDING :
            // System.out.println((mf.getSourceRef()).getName() + " , " + bsSource + " , " + bsSource.getBounds());
            double xStart = bsSource.getBounds().getX() + activityHeight / 2;
            double yStart = bsSource.getBounds().getY();

            double xEnd = bsTarget.getBounds().getX() + activityWidth / 2;
            double yEnd = bsTarget.getBounds().getY();

            Element waypointStart = doc.createElement("di:waypoint");
            waypointStart.setAttribute("x", String.valueOf(xStart));
            waypointStart.setAttribute("y", String.valueOf(yStart));

            Element waypointEnd = doc.createElement("di:waypoint");
            waypointEnd.setAttribute("x", String.valueOf(xEnd));
            waypointEnd.setAttribute("y", String.valueOf(yEnd));

            messageFlow.getBpmnElement().appendChild(waypointStart);
            messageFlow.getBpmnElement().appendChild(waypointEnd);
            rootElement.appendChild(messageFlow.getBpmnElement());
        }

    }

    public void addParticipantsShape(Element rootElement, Participant p, double participantY) {

        if (p.getLanes().size() == 1) {
            this.participantHeight = 600.0;
            this.startEventY = this.participantHeight / 2 - 20 + this.participantStartY;
        } else if (p.getLanes().size() == 2) {
            this.participantHeight = 900.0;
            this.startEventY = this.participantHeight / 2 - 20 + this.participantStartY;
        } else {
            this.startEventY = this.participantHeight / 2 - 20 + this.participantStartY;
        }

        Bounds bounds = new Bounds(this.participantX, participantY, this.participantWidth, this.participantHeight);
        Shape shape = new Shape(p.getId(), "true", bounds);
        shape.setShapePool();
        shape.setBounds();
        poolHeight = shape.getBounds().getY();
        rootElement.appendChild(shape.getBpmnElement());

        int yOff = 0;

        // add Lanes
        for (Map.Entry<Lane, org.bpmn.process.Lane> lane : p.getLanes().entrySet()) {

            org.bpmn.process.Lane laneEntry = lane.getValue();

            Element temp = doc.createElement("bpmndi:BPMNShape");
            temp.setAttribute("bpmnElement", laneEntry.getId());
            temp.setAttribute("isHorizontal", "true");

            Element bnd = doc.createElement("dc:Bounds");

            Double x = this.participantX + 30;
            Double width = this.participantWidth - 30;

            Double height = this.participantHeight / p.getLanes().size();
            Double y = participantY + (this.participantHeight / p.getLanes().size()) * yOff;

            laneEntry.setParticipantMiddleY(participantY + (this.participantHeight / 2));
            laneEntry.setX(x);
            laneEntry.setY(y);
            laneEntry.setHeight(height);
            laneEntry.setWidth(width);
            laneEntry.setMiddleX(x + 30);
            laneEntry.setMiddleY(y + height / 2);

            bnd.setAttribute("x", String.valueOf(x));
            bnd.setAttribute("y", String.valueOf(y));
            bnd.setAttribute("width", String.valueOf(width));
            bnd.setAttribute("height", String.valueOf(height));

            temp.appendChild(bnd);
            rootElement.appendChild(temp);

            yOff++;
        }

        // adapt positions for next participant/pool
        this.participantStartY += this.participantHeight + 50.0;
    }

    private Shape getBPMNShapeByFlow(String sfId) {

        for (Shape bs : shapes) {

            if (bs.getElementId().equals(sfId)) {
                return bs;
            }

        }

        return null;

    }

    private Shape getBPMNShapeByFlowAllShapes(String sfId) {

        for (Shape bs : allShapes) {

            if (bs.getElementId().equals(sfId)) {
                return bs;
            }

        }

        return null;

    }

    private Shape getBPMNShapeByTask(String taskId) {

        for (Shape bs : shapes) {

            if (bs.getElementId().equals(taskId)) {
                return bs;
            }

        }


        return null;

    }

    public void addDataObjectsOutput(Element rootElement, ArrayList<Task> tasks, boolean expandedSubprocess) {

        Double xBoundOffset = 0d;
        for (int i = 0; i < tasks.size(); i++) {

            Task task = tasks.get(i);
            DataObject d = task.getDataObject();
            Shape bs = getBPMNShapeByTask(task.getId());

            if (d != null) {

                Element dataObject = doc.createElement("bpmndi:BPMNShape");
                dataObject.setAttribute("id", d.getRefId() + "_di");
                dataObject.setAttribute("bpmnElement", d.getRefId());

                Double xBound = bs.getBounds().getX() + xBoundOffset;
                Double yBound = bs.getBounds().getY() - 150;

                Element dataObjectBounds = doc.createElement("dc:Bounds");
                dataObjectBounds.setAttribute("x", String.valueOf(xBound));
                dataObjectBounds.setAttribute("y", String.valueOf(yBound));

                d.setX(xBound + dataObjectWidth / 2);
                d.setY(yBound + dataObjectHeight);

                dataObjectBounds.setAttribute("width", String.valueOf(dataObjectWidth));
                dataObjectBounds.setAttribute("height", String.valueOf(dataObjectHeight));
                dataObject.appendChild(dataObjectBounds);

                Edge dataObjectFlowOutput = new Edge(task.getDataOutputAssociation().getId());

                Element waypointOutStart = doc.createElement("di:waypoint");
                Element waypointOutEnd = doc.createElement("di:waypoint");

                String waypointOutStartX = String.valueOf(xBound + activityWidth / 2);
                String waypointOutStartY = String.valueOf(bs.getBounds().getY());
                String waypointOutEndX = String.valueOf(xBound + activityWidth / 2);
                String waypointOutEndY = String.valueOf(yBound + dataObjectHeight);

                waypointOutStart.setAttribute("x", waypointOutStartX);
                waypointOutStart.setAttribute("y", waypointOutStartY);
                waypointOutEnd.setAttribute("x", waypointOutEndX);
                waypointOutEnd.setAttribute("y", waypointOutEndY);

                dataObjectFlowOutput.getBpmnElement().appendChild(waypointOutStart);
                dataObjectFlowOutput.getBpmnElement().appendChild(waypointOutEnd);
                rootElement.appendChild(dataObjectFlowOutput.getBpmnElement());

                rootElement.appendChild(dataObject);

            }

            if (expandedSubprocess && task.getIsSubprocess()) {
                ArrayList<Step> steps = task.getSteps();
                for (Step step : steps) {

                    Shape bsStep = getStepShapeByTask(step.getId());
                    DataObject dStep = step.getDataObject();

                    Element dataObject = doc.createElement("bpmndi:BPMNShape");
                    dataObject.setAttribute("id", dStep.getRefId() + "_di");
                    dataObject.setAttribute("bpmnElement", dStep.getRefId());

                    Double xBound = bsStep.getBounds().getX();
                    Double yBound = bsStep.getBounds().getY() + 100;

                    Element dataObjectBounds = doc.createElement("dc:Bounds");
                    dataObjectBounds.setAttribute("x", String.valueOf(xBound));
                    dataObjectBounds.setAttribute("y", String.valueOf(yBound));

                    dStep.setX(xBound + dataObjectWidth / 2);
                    dStep.setY(yBound + dataObjectHeight);

                    dataObjectBounds.setAttribute("width", String.valueOf(dataObjectWidth));
                    dataObjectBounds.setAttribute("height", String.valueOf(dataObjectHeight));
                    dataObject.appendChild(dataObjectBounds);

                    if (step.getDataOutputAssociation() != null) {

                        Edge dataObjectFlowOutput = new Edge(step.getDataOutputAssociation().getId());

                        Element waypointOutStart = doc.createElement("di:waypoint");
                        Element waypointOutEnd = doc.createElement("di:waypoint");

                        String waypointOutStartX = String.valueOf(xBound + activityWidth / 2);
                        String waypointOutStartY = String.valueOf(yBound - 100 + activityHeight);
                        String waypointOutEndX = String.valueOf(xBound + activityWidth / 2);
                        String waypointOutEndY = String.valueOf(yBound);

                        waypointOutStart.setAttribute("x", waypointOutStartX);
                        waypointOutStart.setAttribute("y", waypointOutStartY);
                        waypointOutEnd.setAttribute("x", waypointOutEndX);
                        waypointOutEnd.setAttribute("y", waypointOutEndY);

                        dataObjectFlowOutput.getBpmnElement().appendChild(waypointOutStart);
                        dataObjectFlowOutput.getBpmnElement().appendChild(waypointOutEnd);

                        rootElement.appendChild(dataObjectFlowOutput.getBpmnElement());
                    } else {

                        for (DataInputAssociation in : step.getDataInputAssociations()) {

                            Edge dataObjectFlowInput = new Edge(in.getId());

                            Element waypointOutStart = doc.createElement("di:waypoint");
                            Element waypointOutEnd = doc.createElement("di:waypoint");

                            String waypointOutStartX = String.valueOf(step.getDataObject().getX());
                            String waypointOutStartY = String.valueOf(step.getDataObject().getY() - dataObjectHeight);
                            String waypointOutEndX = String.valueOf(step.getDataObject().getX());
                            String waypointOutEndY = String.valueOf(yBound - 100 + activityHeight);

                            waypointOutStart.setAttribute("x", waypointOutStartX);
                            waypointOutStart.setAttribute("y", waypointOutStartY);
                            waypointOutEnd.setAttribute("x", waypointOutEndX);
                            waypointOutEnd.setAttribute("y", waypointOutEndY);

                            dataObjectFlowInput.getBpmnElement().appendChild(waypointOutStart);
                            dataObjectFlowInput.getBpmnElement().appendChild(waypointOutEnd);
                            rootElement.appendChild(dataObjectFlowInput.getBpmnElement());
                        }
                    }
                    rootElement.appendChild(dataObject);
                }
            }

        }
    }

    private Shape getStepShapeByTask(String taskId) {

        for (Shape bs : stepShapes) {

            if (bs.getElementId().equals(taskId)) {
                return bs;
            }

        }

        return null;

    }

    public void addDataObjectsInput(Element rootElement, HashSet<IntermediateCatchEvent> events) {

        for (IntermediateCatchEvent event : events) {

            Shape shapeEvent = getBPMNShapeByFlowAllShapes(event.getId());

            for (DataObject d : event.getDataObjects()) {

                Element dataObject = doc.createElement("bpmndi:BPMNShape");
                dataObject.setAttribute("id", d.getRefId() + "_di");
                dataObject.setAttribute("bpmnElement", d.getRefId());

                Double xBound = shapeEvent.getBounds().getX();
                Double yBound = shapeEvent.getBounds().getY() + 150;

                Element dataObjectBounds = doc.createElement("dc:Bounds");
                dataObjectBounds.setAttribute("x", String.valueOf(xBound));
                dataObjectBounds.setAttribute("y", String.valueOf(yBound));

                d.setX(xBound + dataObjectWidth / 2);
                d.setY(yBound + dataObjectHeight);

                dataObjectBounds.setAttribute("width", String.valueOf(dataObjectWidth));
                dataObjectBounds.setAttribute("height", String.valueOf(dataObjectHeight));
                dataObject.appendChild(dataObjectBounds);
                rootElement.appendChild(dataObject);

                for (DataInputAssociation in : d.getDataInputAssociations()) {

                    Edge dataObjectFlowInput = new Edge(in.getId());

                    Element waypointOutStart = doc.createElement("di:waypoint");
                    Element waypointOutEnd = doc.createElement("di:waypoint");

                    String waypointOutStartX = String.valueOf(d.getX());
                    String waypointOutStartY = String.valueOf(d.getY() - dataObjectHeight);
                    String waypointOutEndX = String.valueOf(shapeEvent.getBounds().getX() + activityWidth / 2);
                    String waypointOutEndY = String.valueOf(shapeEvent.getBounds().getY() + activityHeight);

                    waypointOutStart.setAttribute("x", waypointOutStartX);
                    waypointOutStart.setAttribute("y", waypointOutStartY);
                    waypointOutEnd.setAttribute("x", waypointOutEndX);
                    waypointOutEnd.setAttribute("y", waypointOutEndY);

                    dataObjectFlowInput.getBpmnElement().appendChild(waypointOutStart);
                    dataObjectFlowInput.getBpmnElement().appendChild(waypointOutEnd);
                    rootElement.appendChild(dataObjectFlowInput.getBpmnElement());
                }
            }
        }

    }


    public void addFlowsEdge(Element rootElement, ArrayList<SequenceFlow> flows, FlowsProcessObject fp) {

        int cntLoops = 0;
        for (SequenceFlow sf : flows) {

            boolean elementsAreLoop = false;
            BPMNElement source = sf.getSourceRef();
            BPMNElement target = sf.getTargetRef();

            for (Loop loop : fp.getLoops()) {
                if (loop.getFirstGate().getId().equals(source.getId()) && loop.getSecondGate().getId().equals(target.getId())
                        || loop.getFirstGate().getId().equals(target.getId()) && loop.getSecondGate().getId().equals(source.getId())) {
                    cntLoops++;
                    elementsAreLoop = true;
                }
            }

            Shape bsSource = getBPMNShapeByFlow(source.getId());
            Shape bsTarget = getBPMNShapeByFlow(target.getId());

            Edge sequenceFlow = new Edge(sf.getId());

            // JSON BUG FINDING :
            // System.out.println(sf);
            if (bsSource != null && bsTarget != null) {
                double xStart = bsSource.getBounds().getX() + bsSource.getBounds().getWidth();
                double yStart = bsSource.getBounds().getY() + bsSource.getBounds().getHeight() / 2;

                double xEnd = bsTarget.getBounds().getX();
                double yEnd = bsTarget.getBounds().getY() + bsTarget.getBounds().getHeight() / 2;

                sf.setXStart(xStart);
                sf.setYStart(yStart);
                sf.setXEnd(xEnd);
                sf.setYEnd(yEnd);

                Element waypointStart = doc.createElement("di:waypoint");
                waypointStart.setAttribute("x", String.valueOf(xStart));
                waypointStart.setAttribute("y", String.valueOf(yStart));

                Element waypointEnd = doc.createElement("di:waypoint");
                waypointEnd.setAttribute("x", String.valueOf(xEnd));
                waypointEnd.setAttribute("y", String.valueOf(yEnd));

                sequenceFlow.getBpmnElement().appendChild(waypointStart);
                sequenceFlow.getBpmnElement().appendChild(waypointEnd);

                Element waypointAngleStart;
                Element waypointAngleEnd;
                if (elementsAreLoop) {
                    waypointAngleStart = doc.createElement("di:waypoint");
                    waypointAngleStart.setAttribute("x", String.valueOf(xStart));
                    waypointAngleStart.setAttribute("y", String.valueOf(yStart + loopOffset + (multipleLoopOffset * (cntLoops - 1))));

                    waypointAngleEnd = doc.createElement("di:waypoint");
                    waypointAngleEnd.setAttribute("x", String.valueOf(xEnd));
                    waypointAngleEnd.setAttribute("y", String.valueOf(yEnd + loopOffset + (multipleLoopOffset * (cntLoops - 1))));

                    sequenceFlow.getBpmnElement().appendChild(waypointStart);
                    sequenceFlow.getBpmnElement().appendChild(waypointAngleStart);
                    sequenceFlow.getBpmnElement().appendChild(waypointAngleEnd);
                    sequenceFlow.getBpmnElement().appendChild(waypointEnd);

                } else {
                    sequenceFlow.getBpmnElement().appendChild(waypointStart);
                    sequenceFlow.getBpmnElement().appendChild(waypointEnd);
                }

                rootElement.appendChild(sequenceFlow.getBpmnElement());

            }
        }


    }
    /*
    public void addDataObjects(Element rootElement, ArrayList<SequenceFlow> flows) {

        for (SequenceFlow flow : flows) {
            for (Association association : flow.getAssociations()) {
                String id = flow.getSourceRef().getId();
                Shape temp = null;
                for(Shape shape : shapes){
                    if(shape.getElementId().equals(id)){
                        temp = shape;
                    }
                }
                setDataObject(rootElement, flow, association, temp);
            }
        }

    }


    public void setDataObject(Element rootElement, SequenceFlow flow, Association association, Shape temp) {

        DataObject d = association.getDataObject();
        String associationId = association.getAssociationId();

        Element dataObject = doc.createElement("bpmndi:BPMNShape");
        dataObject.setAttribute("id", d.getRefId() + "_di");
        dataObject.setAttribute("bpmnElement", d.getRefId());

        Double xBound = temp.getBounds().getX() + activityWidth/2;
        Double yBound = poolHeight + poolHeightOffset;

        Element dataObjectBounds = doc.createElement("dc:Bounds");
        dataObjectBounds.setAttribute("x", String.valueOf(xBound));
        dataObjectBounds.setAttribute("y", String.valueOf(yBound));

        d.setX(String.valueOf(xBound + dataObjectWidth / 2));
        d.setY(String.valueOf(poolHeight + poolHeightOffset + dataObjectHeight));

        dataObjectBounds.setAttribute("width", String.valueOf(dataObjectWidth));
        dataObjectBounds.setAttribute("height", String.valueOf(dataObjectHeight));
        dataObject.appendChild(dataObjectBounds);

        Element dataObjectFlowOutput = doc.createElement("bpmndi:BPMNEdge");
        //TODO: POTENZIELL BUGGY
        dataObjectFlowOutput.setAttribute("id", associationId + "_di");
        dataObjectFlowOutput.setAttribute("bpmnElement", associationId);

        Element waypointOutStart = doc.createElement("di:waypoint");
        Element waypointOutEnd = doc.createElement("di:waypoint");

        String waypointOutStartX = String.valueOf(xBound + dataObjectWidth / 2);
        String waypointOutStartY = String.valueOf(flow.getYEnd());
        String waypointOutEndX = String.valueOf(xBound + dataObjectWidth / 2);
        String waypointOutEndY = String.valueOf(poolHeight + poolHeightOffset + dataObjectHeight);

        waypointOutStart.setAttribute("x", waypointOutStartX);
        waypointOutStart.setAttribute("y", waypointOutStartY);
        waypointOutEnd.setAttribute("x", waypointOutEndX);
        waypointOutEnd.setAttribute("y", waypointOutEndY);

        dataObjectFlowOutput.appendChild(waypointOutStart);
        dataObjectFlowOutput.appendChild(waypointOutEnd);
        rootElement.appendChild(dataObjectFlowOutput);

        rootElement.appendChild(dataObject);

    }

     */

}


