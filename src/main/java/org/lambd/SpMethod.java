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
    private int id = 0;
    private SpMethod caller;
    public SpCallGraph cg;
    public SpMethod(SootMethod sootMethod, int id, SpMethod caller) {
        this.id = id;
        this.caller = caller;
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new Summary(this);
        ptset = new PointerToSet(this);
        manager = new OneObjManager(this, ptset);
        cg = new SpCallGraph();
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
            ptset.update(l1, l2, w, stmt);
    }

    /**
     * get the var of arg index, and var.fields exists and can hold string
     * @param stmt current invoke stmt
     * @param sink sink description
     * @param index arg index can transfer to sink
     * @param w fields of arg can transfer to sink
     */
    public void handleSink(Stmt stmt, String sink, int index, Weight w, int calleeID) {
        Value var = getParameter(stmt, index);
        if (var instanceof Local l)
            ptset.genSink(sink, w, l, calleeID);
    }
    public void handleReturn(Local retVar, Stmt stmt) {
        ptset.genReturn(retVar, stmt);
    }

    public String getName() {
        return name;
    }
    public SootMethod getSootMethod() {
        return sootMethod;
    }
    public String toString() {
        return String.format("%s@%d", sootMethod, id);
//        return String.format("%s@%d %s(%s)", sootMethod, id, caller.name, caller.paramTypes);
    }
    public ObjManager getManager() {
        return manager;
    }
    public Summary getSummary() {
        return summary;
    }
    public SpMethod getCaller() {
        return caller;
    }
    public PointerToSet getPtset() {
        return ptset;
    }
    public SpCallGraph getCg() {
        return cg;
    }
}
