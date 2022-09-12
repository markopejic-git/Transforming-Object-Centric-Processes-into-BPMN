package org.bpmn.bpmndi;

import org.w3c.dom.Element;

import static org.bpmn.steps.BPMN.doc;

public class Bounds {

    private final Element elementBounds;
    private double x;
    private double y;
    private double width;
    private double height;

    public Bounds(double x, double y, double width, double height) {

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.elementBounds = doc.createElement("dc:Bounds");
        setElement();

    }

    private void setElement() {
        this.elementBounds.setAttribute("height", String.valueOf(height));
        this.elementBounds.setAttribute("width", String.valueOf(width));
        this.elementBounds.setAttribute("x", String.valueOf(x));
        this.elementBounds.setAttribute("y", String.valueOf(y));
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Element getElementBounds() {
        return elementBounds;
    }

}