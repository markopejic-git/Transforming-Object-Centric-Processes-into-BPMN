package org.bpmn.flows_entities;

import java.util.ArrayList;

public class RelationTypeData extends AbstractRelationship {

    public RelationTypeData(String type, String id_, String methodname_, ArrayList<Object> parameters_, Double createdActorId_) {

        // __type = type;
        // Id = id_;
        MethodName = methodname_;
        Parameters = parameters_;
        CreatedActorId = createdActorId_;
    }

    @Override
    public String toString() {
        return "Object { " + MethodName + " " + Parameters + " " + CreatedActorId + " }";
    }

}
