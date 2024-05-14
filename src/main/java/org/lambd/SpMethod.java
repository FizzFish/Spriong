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
    public final String name;
    private Summary summary;
    private ObjManager manager;
    private PointerToSet ptset;
    private int id = 0;
    private SpMethod caller;
    public SpCallGraph cg;
    private State state;
    private Map<SpMethod, Stmt> updateCaller = new HashMap<>();
    public SpMethod(SootMethod sootMethod, int id, SpMethod caller) {
        this.id = id;
        this.caller = caller;
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new Summary(this);
        ptset = new PointerToSet(this);
        manager = new OneObjManager(this, ptset);
        cg = new SpCallGraph();
        state = State.VISITED;
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
    public void handleSink(Stmt stmt, String sink, int index, Weight w) {
        Value var = getParameter(stmt, index);
        if (var instanceof Local l)
            ptset.genSink(sink, w, l, stmt);
    }
    public void handleReturn(Stmt stmt, RefType type) {
        Value lhs = getParameter(stmt, -2);
        if (lhs instanceof Local l)
            ptset.updateLhs(l, type);
    }
    public String getName() {
        return name;
    }
    public SootMethod getSootMethod() {
        return sootMethod;
    }
    public String toString() {
        return String.format("%s@%d: %d", sootMethod, id, caller==null? -1: caller.id);
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
    public void finish() {
        state = State.FINISHED;
//        updateCaller.forEach((c, s) -> {
//            summary.apply(c, s);
//        });
    }
    public void addUpdateCaller(SpMethod caller, Stmt stmt) {
        this.updateCaller.put(caller, stmt);
    }
}
enum State {
    STRANGE,
    VISITED,
    FINISHED,
}