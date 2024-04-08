package org.lambd.transition;

import soot.Local;

public class FieldTransition implements Transition {
    private String fieldName;
    private Local from;
    private Local to;
    private boolean pushOrPop;
    public FieldTransition(Local from, Local to, String fieldName, boolean pushOrPop) {
        this.from = from;
        this.to = to;
        this.fieldName = fieldName;
        this.pushOrPop = pushOrPop;
    }
    public String toString() {
        return "FieldTransition: " + from + " -> " + to + " " + fieldName + " " + pushOrPop;
    }
}
