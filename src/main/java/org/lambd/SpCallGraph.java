package org.lambd;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SpCallGraph {
    private CallGraph cg;
    public SpCallGraph(CallGraph cg) {
        this.cg = cg;
    }
//    public Set<SpMethod> resolve(RefType type, Unit unit) {
//        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
//        Set<SpMethod> methods = new HashSet();
//        for (Iterator<Edge> it = cg.edgesOutOf(unit); it.hasNext();) {
//            Edge edge = it.next();
//            SootMethod sootMethod = edge.getTgt().method();
//            SootClass declaringClass = sootMethod.getDeclaringClass();
//            SootClass baseClass = type.getSootClass();
//            if (hierarchy.isClassSubclassOf(declaringClass, baseClass))
//                methods.add(SootWorld.v().getMethod(sootMethod));
//        }
//
//        return methods;
//    }

}
