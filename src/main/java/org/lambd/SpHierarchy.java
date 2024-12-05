package org.lambd;

import com.google.common.collect.HashBasedTable;
import org.lambd.condition.Constraint;
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
    // 为了决定是否生成条件，需要判断一个methodRef是否会有多个实现者
    // TODO:由于interface中的default实现不易判断，且出现的可能性非常小，暂不分析这种情况
    // 生成一个Constraint，即[recvClass，superClass]的区间
    public SootMethod dispatch(SootClass receiverClass, SootMethodRef methodRef) {
        // 判断receiverClass是否是declaringClass的子类或者实现者，但这个代价是比较大的
        // 我们假定程序没有错误，跳过这个判断
        /**
        if (!hierarchy.isClassSubclassOfIncluding(receiverClass, methodRef.getDeclaringClass()))
            return null;
         */
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
        // 1. Otherwise, method resolution attempts to locate the
        // referenced method in C and its superclasses
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        for (SootClass c = jclass; c != null && c.hasSuperclass(); c = c.getSuperclass()) {
            SootMethod method = c.getMethodUnsafe(subsignature);
            if (method != null && (allowAbstract || !method.isAbstract())) {
                return method;
            }
        }
        // 2. Otherwise, method resolution attempts to locate the
        // referenced method in the superinterfaces of the specified class C
        // 可能函数存在于interface的default实现中，这种情况比较少
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
