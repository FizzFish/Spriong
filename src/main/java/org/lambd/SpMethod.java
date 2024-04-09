package org.lambd;

import org.lambd.obj.*;
import soot.*;
import soot.jimple.*;

import java.util.*;

public class SpMethod {
    private final List<Type> paramTypes;
    private final SootClass clazz;
    private final Type returnType;
    public final String name;
    private final List<Local> paramters;
    private final Local thisVar;
    private ObjManager objManager;
    public SpMethod(SootMethod sootMethod) {
        List<Local> paramters1;
        this.paramTypes = sootMethod.getParameterTypes();
        this.clazz = sootMethod.getDeclaringClass();
        this.returnType = sootMethod.getReturnType();
        this.name = sootMethod.getName();
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
            return getParameter(invokeStmt, i);
        }
        return null;
    }
    public void accept(Stmt stmt, int from, int to, int update) {
        Value fromVal = getParameter(stmt, from);
        Value toVal = getParameter(stmt, to);
        if (fromVal instanceof Local local) {
            copy(local, (Local) toVal, update);
        } else if (fromVal instanceof Constant constant) {
            ConstantObj constantObj = new ConstantObj(toVal.getType(), this, constant);
            objManager.addObj((Local) toVal, constantObj);
        }
    }

    public ObjManager getObjManager() {
        return objManager;
    }
    public String getName() {
        return name;
    }
    public void addObj(Local local, Obj obj) {
        objManager.addObj(local, obj);
    }
    public void copy(Local from, Local to) {
        // field ?
        copy(from, to, 0);
    }
    public void copy(Local from, Local to, int update) {
        // field ?
        // deep or shallow ?
        objManager.copy(from, to, update);
    }

}
