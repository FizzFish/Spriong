package org.lambd;

import org.lambd.obj.ConstantObj;
import org.lambd.obj.FormatObj;
import org.lambd.obj.Obj;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.Edge;

public class StmtVisitor {
    private SpMethod method;
    public StmtVisitor(SpMethod method) {
        this.method = method;
    }
    public void visit(Stmt stmt) {
        if (stmt instanceof IdentityStmt) {
            visit((IdentityStmt) stmt);
        } else if (stmt instanceof AssignStmt) {
            visit((AssignStmt) stmt);
        } else if (stmt instanceof InvokeStmt) {
            visit((InvokeStmt) stmt);
        } else if (stmt instanceof ReturnStmt) {
            visit((ReturnStmt) stmt);
        }
    }

    /**
     * source := @parameter0
     * this := @this: org.apache.commons.text.StringSubstitutor
     * @param stmt
     */
    public void visit(IdentityStmt stmt) {
        Local lhs = (Local) stmt.getLeftOp();
        Value val = stmt.getRightOp();
        if (val instanceof ParameterRef parameterRef) {
            int index = parameterRef.getIndex();
        } else if (val instanceof ThisRef thisRef) {
        }
    }
    public void visit(AssignStmt stmt) {
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (rhs instanceof InvokeExpr) {
            SootMethod callee = getCallee(stmt);
            if (callee !=null) {
                SootWorld.v().visitMethod(callee);
            }
            return;
        }
        if (lhs instanceof Local lvar) {
            if (rhs instanceof Local rvar) {
                method.copy(rvar, lvar);
            } else if (rhs instanceof AnyNewExpr newExpr) {
                Obj obj = new Obj(newExpr.getType(), method);
                method.addObj(lvar, obj);
            } else if (rhs instanceof Constant) {
            } else if (rhs instanceof FieldRef fieldRef) {
                // x = y.f
                if (fieldRef instanceof InstanceFieldRef instanceFieldRef)
                    method.loadField(lvar, (Local) instanceFieldRef.getBase(), instanceFieldRef.getField());
                else    // x = c.f
                    method.loadStaticField(lvar, fieldRef.getClass(), fieldRef.getField());
            } else if (rhs instanceof ArrayRef arrayRef) {
                // x = y[i]
                method.loadArray(lvar, (Local) arrayRef.getBase());
            } else if (rhs instanceof BinopExpr) {
            } else if (rhs instanceof UnopExpr) {
            } else if (rhs instanceof InstanceOfExpr) {
            } else if (rhs instanceof CastExpr castExpr) {
                method.copy((Local) castExpr.getOp(), lvar);
            } else {
                System.out.println("unsupported assignment rhs: " + stmt);
            }
        } else if (lhs instanceof FieldRef fieldRef) {
            // x.f = y
            if (fieldRef instanceof InstanceFieldRef instanceFieldRef)
                method.storeField((Local) instanceFieldRef.getBase(), instanceFieldRef.getField(), rhs);
            else    // c.f = y
                method.storeStaticField(fieldRef.getClass(), fieldRef.getField(), rhs);
        } else if (lhs instanceof ArrayRef arrayRef) {
            // x[i] = y
            method.storeArray((Local) arrayRef.getBase(), (Local) rhs);
        } else {
            System.out.println("unsupported assignment lhs: " + stmt);
        }
    }
    private SootMethod getCallee(Unit unit) {
        Edge edge = Scene.v().getCallGraph().edgesOutOf(unit).next();
        if (edge.getTgt() != null)
            return edge.getTgt().method();
        return null;
    }
    public void visit(InvokeStmt stmt) {
        SootMethod callee = getCallee(stmt);
        if (callee !=null) {
            SootWorld.v().visitMethod(callee);
        }
    }
    public void visit(ReturnStmt stmt) {
        Value retVal = stmt.getOp();
        if (retVal instanceof Local local) {
//            Transition transition = new OutSideTransition(-2, false, local);
//            method.addTransition(transition);
        } else if (retVal instanceof Constant constant){
            // Todo
            ConstantObj constantObj = new ConstantObj(retVal.getType(), method, constant);

        } else {
            System.out.println("unsupported return type: " + retVal.getClass().getName());
        }
    }
}
