package org.lambd.transition;

import soot.Local;
import soot.Type;

public class NewTransition implements Transition {
    private Local local;
    private Type type;
    public NewTransition(Local local, Type type) {
        this.local = local;
        this.type = type;
    }
    public String toString() {
        return "NewTransition: " + local + " " + type;
    }
}
