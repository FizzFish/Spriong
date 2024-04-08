package org.lambd.transition;

import soot.SootField;

public record Transition(int from, int to) {
    public String toString() {
        return "(" + from + ", " + to + ")";
    }
}
