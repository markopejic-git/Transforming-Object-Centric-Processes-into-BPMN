package org.bpmn.flows_entities.deserialize;

import java.util.ArrayList;

public class FlowsObjectName extends AbstractFlowsObjectName {

    public FlowsObjectName(ArrayList<Double> Parameters_) {
        Parameters = Parameters_;
    }

    @Override
    public String toString() {
        return "Object { " + Parameters + " }";
    }

}