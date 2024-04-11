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
    private Map<Local, SpVar> spVars = new HashMap<>();
    private MethodSummary summary;
    private List<Type> genTypes;
    private ObjManager manager;
    public SpMethod(SootMethod sootMethod) {
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        summary = new MethodSummary(this);
        manager = new OneObjManager(this);
        initParam();
    }
    private void initParam() {
        Body body;
        try {
            body = sootMethod.getActiveBody();
        } catch (Exception e) {
            return;
        }
        Set<Type> typeSet = new HashSet<>();
        body.getUnits().forEach(stmt -> {
            if (stmt instanceof AssignStmt assignStmt) {
                Value rhs = assignStmt.getRightOp();
                if (rhs instanceof AnyNewExpr newExpr)
                    typeSet.add(newExpr.getType());
                else if (rhs instanceof InvokeExpr invokeExpr)
                    typeSet.add(invokeExpr.getMethod().getReturnType());
            }
        });
        genTypes = typeSet.stream().toList();

        List<Local> parameters = body.getParameterLocals();
        for (int i = 0; i < paramTypes.size(); i++) {
            Local var = parameters.get(i);
            spVars.put(var, new SpVar(this, var, i));
        }
        if (!sootMethod.isStatic()) {
            Local thisVar = sootMethod.getActiveBody().getThisLocal();
            spVars.put(thisVar, new SpVar(this, thisVar, -1));
        }
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
    public SpVar getVar(Value val) {
        if (!(val instanceof Local))
            return null;
        if (spVars.containsKey(val))
            return spVars.get(val);
        SpVar spvar = new SpVar(this, (Local) val);
        spVars.put((Local) val, spvar);
        return spvar;
    }
    private SpVar getParamVar(Stmt stmt, int i) {
        return getVar(getParameter(stmt, i));
    }
    public void handleTransition(Stmt stmt, int from, int to, Weight w) {
        SpVar fromVar = getParamVar(stmt, from);
        SpVar toVar = getParamVar(stmt, to);
        if (fromVar == null || toVar == null || fromVar == toVar)
            return;
        toVar.update(fromVar, w);

    }
    public void copy(Local from, Local to, Weight w) {
        SpVar fromVar = getVar(from);
        SpVar toVar = getVar(to);
        if (fromVar == null || toVar == null)
            return;
        toVar.copy(fromVar, w);
    }
    public void handleSink(Stmt stmt, SinkTrans sink) {
        int index = sink.getIndex();
        SpVar var = getParamVar(stmt, index);
        if (var == null)
            return;
        var.genSink(sink);
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
    public List<Type> getGenTypes() {
        return genTypes;
    }
    public ObjManager getManager() {
        return manager;
    }
    public MethodSummary getSummary() {
        return summary;
    }
}
