package org.lambd;

import org.lambd.obj.*;
import org.lambd.pointer.PointerToSet;
import org.lambd.transition.*;
import soot.*;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpMethod {
    private SootMethod sootMethod;
    public final String name;
    private Summary summary;
    private ObjManager manager;
    private PointerToSet ptset;
    private List<Annotion> annotionList = new ArrayList<>();
    private int id = 0;
    private State state;
    public SpMethod caller;
    public SpMethod(SootMethod sootMethod, int id) {
        this.id = id;
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new Summary(this);
        SootClass sc = id == 0 ? SootWorld.v().entryClass : null;
        ptset = new PointerToSet(this, sc);
        manager = new OneObjManager(this, ptset);
        state = State.VISITED;
    }

    public void addAnnotation(Annotion an) {
        annotionList.add(an);
    }
    public boolean checkAnnotation() {
        List<String> conditions = annotionList.stream().map(Annotion::getAnnotationType).toList();
        if (conditions.contains("Ljavax/ws/rs/POST;") && conditions.contains("Ljavax/ws/rs/Path;"))
            return true;
        if (conditions.contains("Lorg/springframework/shell/standard/ShellMethod;"))
            return true;
        return false;
    }
    public String getAnnotation() {
        return annotionList.stream().map(Annotion::getAnnotationType).collect(Collectors.joining());
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
    public void handleLoadTransition(Stmt stmt) {
        // packages(String[])($7)
        Local arg0 = (Local) getParameter(stmt,0);
        SootWorld.v().analyzePackage(ptset.getArrayString(arg0));
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
            ptset.updateLhs(l, type, stmt);
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
    public PointerToSet getPtset() {
        return ptset;
    }
    public void finish() {
        state = State.FINISHED;
    }
    public boolean isFinished() {
        return state == State.FINISHED;
    }
}
enum State {
    STRANGE,
    VISITED,
    FINISHED,
}