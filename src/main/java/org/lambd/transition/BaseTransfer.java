package org.lambd.transition;

import java.util.List;

public class BaseTransfer {
    public String method;
    public List<Transition> transitions;
    public String toString() {
        return method + ": " + transitions;
    }
}