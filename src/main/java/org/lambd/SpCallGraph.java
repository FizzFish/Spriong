package org.lambd;

import com.google.common.collect.HashBasedTable;
import soot.*;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.NumberedString;

import java.util.*;

public class SpCallGraph {
    public SpCallGraph() {
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
    private HashBasedTable<SootClass, NumberedString, SootMethod> dispatchTable = HashBasedTable.create();
    public SootMethod resolve(InvokeExpr invoke, SootClass cls) {
        SootMethodRef subsignature = invoke.getMethodRef();
        if (invoke instanceof StaticInvokeExpr sie)
            return subsignature.resolve();
        if (invoke instanceof InstanceInvokeExpr iie)
            return dispatch(cls, subsignature);
        return null;
    }
    public SootMethod dispatch(SootClass receiverClass, SootMethodRef methodRef) {
        // check the subclass relation between the receiver class and
        // the class of method reference to avoid the unexpected method found
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();

        if (!hierarchy.isClassSubclassOfIncluding(receiverClass, methodRef.getDeclaringClass()))
            return null;
        NumberedString subsignature = methodRef.getSubSignature();
        SootMethod target = dispatchTable.get(receiverClass, subsignature);
        if (target == null) {
            target = lookupMethod(receiverClass, subsignature, false);
            if (target != null) {
                dispatchTable.put(receiverClass, subsignature, target);
            }
        }
        return target;
    }
    private SootMethod lookupMethod(SootClass jclass, NumberedString subsignature,
                                 boolean allowAbstract) {
        // JVM Spec. (11 Ed.), 5.4.3.3 Method Resolution
        // 1. If C is an interface, method resolution throws
        // an IncompatibleClassChangeError. TODO: what does this mean???

        // 2. Otherwise, method resolution attempts to locate the
        // referenced method in C and its superclasses
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        for (SootClass c = jclass; c != null; c = c.getSuperclass()) {
            SootMethod method = c.getMethodUnsafe(subsignature);
            if (method != null && (allowAbstract || !method.isAbstract())) {
                return method;
            }
        }
        // 3. Otherwise, method resolution attempts to locate the
        // referenced method in the superinterfaces of the specified class C
        for (SootClass c = jclass; c != null; c = c.getSuperclass()) {
            for (SootClass iface : c.getInterfaces()) {
                SootMethod method = lookupMethodFromSuperinterfaces(
                        iface, subsignature, allowAbstract);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
        // TODO:
        //  1. check accessibility
        //  2. handle phantom methods
        //  3. double-check correctness
    }

    private SootMethod lookupMethodFromSuperinterfaces(
            SootClass jclass, NumberedString subsignature, boolean allowAbstract) {
        SootMethod method = jclass.getMethodUnsafe(subsignature);
        if (method != null && (allowAbstract || !method.isAbstract())) {
            return method;
        }
        for (SootClass iface : jclass.getInterfaces()) {
            method = lookupMethodFromSuperinterfaces(
                    iface, subsignature, allowAbstract);
            if (method != null) {
                return method;
            }
        }
        return null;
    }
}
