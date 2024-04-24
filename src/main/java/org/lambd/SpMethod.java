package org.lambd;

import org.lambd.obj.*;
import org.lambd.transition.*;
import soot.*;
import soot.jimple.*;

import java.util.*;

public class SpMethod {
    private SootMethod sootMethod;
    private final List<Type> paramTypes;
    private final SootClass clazz;
    public final String name;
    private VarRelation varRelation;
    private MethodSummary summary;
    private ObjManager manager;
    public SpMethod(SootMethod sootMethod) {
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new MethodSummary(this);
        manager = new OneObjManager(this);
        varRelation = new VarRelation(this);
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
            varRelation.update(l1, l2, w);
    }
    public void copy(Local from, Local to, Weight w) {
        if (from == null || to == null || from == to)
            return;
        varRelation.update(from, to, w);
    }
    public void handleSink(Stmt stmt, String sink, int index, Weight w) {
        Value var = getParameter(stmt, index);
        if (var instanceof Local l)
            varRelation.genSink(sink, w, l);
    }
    public void handleReturn(Local retVar) {
        varRelation.genReturn(retVar);
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
    public MethodSummary getSummary() {
        return summary;
    }
}
