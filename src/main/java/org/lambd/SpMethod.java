package org.lambd;

import org.lambd.obj.*;
import org.lambd.transition.SinkTransition;
import soot.*;
import soot.jimple.*;

import java.util.*;

public class SpMethod {
    private SootMethod sootMethod;
    private final List<Type> paramTypes;
    private final SootClass clazz;
    public final String name;
    private final List<Local> paramters;
    private final Local thisVar;
    private ObjManager objManager;
    public SpMethod(SootMethod sootMethod) {
        List<Local> paramters1;
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.name = sootMethod.getName();
        this.sootMethod = sootMethod;
        objManager = new OneObjManager(this);
        try {
            paramters1 = sootMethod.getActiveBody().getParameterLocals();
        } catch (Exception e) {
            paramters1 = new ArrayList<>();
        }

        this.paramters = paramters1;
        for (int i = 0; i < paramTypes.size(); i++) {
            objManager.addObj(paramters.get(i), new FormatObj(paramTypes.get(i), this, i));
        }
        if (!sootMethod.isStatic()) {
            this.thisVar = sootMethod.getActiveBody().getThisLocal();
            objManager.addObj(thisVar, new FormatObj(clazz.getType(), this, -1));
        } else {
            this.thisVar = null;
        }
    }
    private Value getParameter(AssignStmt stmt, int i) {
        if (i == -2)
            return stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (rhs instanceof InvokeExpr invoke) {
            if (i == -1 && invoke instanceof InstanceInvokeExpr instanceInvoke) {
                return instanceInvoke.getBase();
            }
            return invoke.getArg(i);
        }
        return null;
    }
    private Value getParameter(InvokeStmt stmt, int i) {
        InvokeExpr invokeExpr = stmt.getInvokeExpr();
        if (i == -1 && invokeExpr instanceof InstanceInvokeExpr instanceInvoke) {
            return instanceInvoke.getBase();
        }
        return invokeExpr.getArg(i);
    }
    private Value getParameter(Stmt stmt, int i) {
        if (stmt instanceof AssignStmt assignStmt) {
            return getParameter(assignStmt, i);
        } else if (stmt instanceof InvokeStmt invokeStmt) {
            if (i == -2)
                return null;
            return getParameter(invokeStmt, i);
        }
        return null;
    }
    public void handleTransition(Stmt stmt, int from, int to, Relation relation) {
        Value fromVal = getParameter(stmt, from);
        Value toVal = getParameter(stmt, to);
        if (fromVal == null || toVal == null)
            return;;
        if (fromVal instanceof Local local) {
            objManager.copy(local, (Local) toVal, relation);
        }
//      else if (fromVal instanceof Constant constant) {
//            ConstantObj constantObj = new ConstantObj(toVal.getType(), this, constant);
//            objManager.addObj((Local) toVal, constantObj);
//        }
    }
    public void handleSink(Stmt stmt, SinkTransition sinkRelation) {
        int index = sinkRelation.getIndex();
        Local arg = (Local) getParameter(stmt, index);
        Location location = objManager.getObj(arg);
        assert location != null;
        if (location == null)
            System.out.println("null");
        Fraction fraction = Fraction.multiply(location.getFraction(), sinkRelation.getFraction());
        int formatIndex = location.getIndex();
        SinkTransition newRelation = new SinkTransition(formatIndex, fraction, sinkRelation.getSink());
        if (formatIndex > -2) {
            SootWorld.v().addTransition(sootMethod, newRelation);
            System.out.printf(String.format("sink %s@%d: %s\n", sootMethod.getSignature(), formatIndex, newRelation));
        }

    }

    public ObjManager getObjManager() {
        return objManager;
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

}
