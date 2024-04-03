package org.lambd;

import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.Iterator;

public class InvokeTransition implements Transition {
    private SootMethod callee;
    public InvokeTransition(Unit unit) {
        Edge edge = Scene.v().getCallGraph().edgesOutOf(unit).next();
        this.callee = edge.getTgt().method();
    }
    public String toString() {
        return "InvokeTransition: " + callee.getSignature();
    }
}
