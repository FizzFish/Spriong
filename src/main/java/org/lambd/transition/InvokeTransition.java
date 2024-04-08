package org.lambd.transition;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.Edge;

public class InvokeTransition implements Transition {
    private SootMethod callee;
    private static final Logger logger = LogManager.getLogger(InvokeTransition.class);
    public InvokeTransition(Unit unit) {
        Edge edge = Scene.v().getCallGraph().edgesOutOf(unit).next();

        if (edge.getTgt() == null) {
            logger.error("Cannot find callee: " + unit);
            callee = null;
        } else {
            this.callee = edge.getTgt().method();
        }
    }

    public SootMethod getCallee() {
        return callee;
    }

    public String toString() {
        if (callee == null)
            return "InvokeTransition: cannot find callee";
        return "InvokeTransition: " + callee.getSignature();
    }
}
