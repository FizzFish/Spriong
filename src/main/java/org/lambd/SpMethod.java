package org.lambd;

import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.lambd.obj.*;
import org.lambd.pointer.PointerToSet;
import org.lambd.transition.*;
import soot.*;
import soot.jimple.*;

import java.util.*;

public class SpMethod {
    private SootMethod sootMethod;
    private final List<Type> paramTypes;
    private final SootClass clazz;
    public final String name;
    private Summary summary;
    private ObjManager manager;
    private PointerToSet ptset;
    public SpMethod(SootMethod sootMethod) {
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new Summary(this);
        ptset = new PointerToSet(this);
        manager = new OneObjManager(this, ptset);
    }

    private Value getParameter(Stmt stmt, int i) {
        if (stmt instanceof AssignStmt assignStmt) {
            if (i == -2)
                return assignStmt.getLeftOp();
            Value rhs = assignStmt.getRightOp();
            if (rhs instanceof InvokeExpr invoke) {
                if (i == -1 && invoke instanceof InstanceInvokeExpr instanceInvoke) {
                    return instanceInvoke.getBase();
                }
                try {
                    return invoke.getArg(i);
                } catch (Exception e) {
                    return null;
                }
            }
        } else if (stmt instanceof InvokeStmt invokeStmt) {
            if (i == -2)
                return null;
            InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
            if (i == -1 && invokeExpr instanceof InstanceInvokeExpr instanceInvoke) {
                return instanceInvoke.getBase();
            }
            return invokeExpr.getArg(i);
        }
        return null;
    }
    public void handleTransition(Stmt stmt, int from, int to, Weight w) {
        Value fromVar = getParameter(stmt, from);
        Value toVar = getParameter(stmt, to);
        if (fromVar == toVar)
            return;
        if (fromVar instanceof Local l1 && toVar instanceof Local l2)
            ptset.update(l1, l2, w);
    }
    public void handleSink(Stmt stmt, String sink, int index, Weight w) {
        Value var = getParameter(stmt, index);
        if (var instanceof Local l)
            ptset.genSink(sink, w, l);
    }
    public void handleReturn(Local retVar) {
        ptset.genReturn(retVar);
    }

    public String getName() {
        return name;
    }
    public SootMethod getSootMethod() {
        return sootMethod;
    }
    public String toString() {
        return sootMethod.toString();
    }
    public ObjManager getManager() {
        return manager;
    }
    public Summary getSummary() {
        return summary;
    }
}
