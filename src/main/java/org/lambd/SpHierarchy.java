package org.lambd;

import com.google.common.collect.HashBasedTable;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.NumberedString;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 自己实现的hierarchy，主要对外的接口是resolve(invoke, cls)
 */
public class SpHierarchy {
    public SpHierarchy() {
    }
    private static SpHierarchy cg = null;
    public static SpHierarchy v() {
        if (cg == null) {
            cg = new SpHierarchy();
        }
        return cg;
    }
    private HashBasedTable<SootClass, NumberedString, SootMethod> dispatchTable = HashBasedTable.create();
    /**
     * 获取unit对应的所有可能得callees
     * @return
     */
    public Set<SootMethod> getCallee(InvokeExpr invoke, RefType type) {
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        Set<SootMethod> methods = new HashSet();
        SootClass interfaceClass = type.getSootClass();
        NumberedString signature = invoke.getMethodRef().getSubSignature();
        for (SootClass sc: hierarchy.getImplementersOf(interfaceClass)) {
            if (!sc.isPhantom() && sc.declaresMethod(signature))
                methods.add(sc.getMethod(signature));
        }
        return methods;
    }
    public SootMethod resolve(InvokeExpr invoke, SootClass cls) {
        SootMethodRef subsignature = invoke.getMethodRef();
        if (cls == null)
            return subsignature.resolve();
        return dispatch(cls, subsignature);
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
        for (SootClass c = jclass; c != null && c.hasSuperclass(); c = c.getSuperclass()) {
            SootMethod method = c.getMethodUnsafe(subsignature);
            if (method != null && (allowAbstract || !method.isAbstract())) {
                return method;
            }
        }
        // 3. Otherwise, method resolution attempts to locate the
        // referenced method in the superinterfaces of the specified class C
        for (SootClass c = jclass; c != null && c.hasSuperclass(); c = c.getSuperclass()) {
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
    public boolean isSubClass(SootClass jclass, SootClass baseClass) {
        for (SootClass c = jclass; c != null; c = c.getSuperclass()) {
            if (c == baseClass) {
                return true;
            }
            for (SootClass iface : c.getInterfaces()) {
                if (checkInterface(iface, baseClass))
                    return true;
            }
            if (!c.hasSuperclass())
                return false;
        }
        return false;
    }
    private boolean checkInterface(SootClass jclass, SootClass baseClass) {
        if (jclass == baseClass)
            return true;
        for (SootClass iface : jclass.getInterfaces()) {
            if (checkInterface(iface, baseClass))
                return true;
        }
        return false;
    }
}
