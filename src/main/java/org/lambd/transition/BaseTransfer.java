package org.lambd.transition;

import java.util.List;

public class BaseTransfer {
    public String method;
    public List<BaseTransition> transitions;
    public String toString() {
        return method + ": " + transitions;
    }
}