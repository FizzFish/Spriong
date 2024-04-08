package org.lambd.transition;

import soot.Local;

public class OutSideTransition implements Transition{
    private int parameter;
    private boolean direction;
    private Local var;
    // direction: true -> identity, false -> return
    public OutSideTransition(int parameter, boolean direction, Local var) {
        this.parameter = parameter;
        this.direction = direction;
        this.var = var;
    }
    public String toString() {
        return "OutSideTransition(" + parameter + ", " + direction + ", " + var + ")";
    }
}
