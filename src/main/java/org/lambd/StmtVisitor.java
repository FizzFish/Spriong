package org.lambd;

import org.lambd.obj.ConstantObj;
import org.lambd.obj.Obj;
import org.lambd.obj.ObjManager;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StmtVisitor {
    private SpMethod methodContext;
    public StmtVisitor(SpMethod method) {
        this.methodContext = method;
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
    private void handleInvoke(Stmt stmt, InvokeExpr invoke) {
        String signature = invoke.getMethodRef().getSignature();
        SootWorld world = SootWorld.v();
        if (!world.quickMethodRef(signature, methodContext, stmt)) {
            Set<SootMethod> callees = getCallee(stmt, invoke);
            for (SootMethod callee : callees) {
                if (world.getVisited().contains(callee))
                    continue;
                if (!world.quickCallee(callee, methodContext, stmt)) {
                    SootWorld.v().visitMethod(callee);
                    world.quickCallee(callee, methodContext, stmt);
                }
            }
            // check methodRef again
            world.quickMethodRef(signature, methodContext, stmt);
        }
    }
    public void visit(AssignStmt stmt) {
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (rhs instanceof InvokeExpr invoke) {
            handleInvoke(stmt, invoke);
            return;
        }
        ObjManager objManager = methodContext.getObjManager();
        if (lhs instanceof Local lvar) {
            if (rhs instanceof Local rvar) {
                objManager.copy(rvar, lvar);
            } else if (rhs instanceof AnyNewExpr newExpr) {
                Obj obj = new Obj(newExpr.getType(), methodContext);
                objManager.addObj(lvar, obj);
            } else if (rhs instanceof Constant) {
            } else if (rhs instanceof FieldRef fieldRef) {
                // x = y.f
                if (fieldRef instanceof InstanceFieldRef instanceFieldRef)
                    objManager.loadField(lvar, (Local) instanceFieldRef.getBase(), instanceFieldRef.getField());
                else    // x = c.f
                    objManager.loadStaticField(lvar, fieldRef.getClass(), fieldRef.getField());
            } else if (rhs instanceof ArrayRef arrayRef) {
                // x = y[i]
                objManager.loadArray(lvar, (Local) arrayRef.getBase());
            } else if (rhs instanceof BinopExpr) {
            } else if (rhs instanceof UnopExpr) {
            } else if (rhs instanceof InstanceOfExpr) {
            } else if (rhs instanceof CastExpr castExpr) {
                Value op = castExpr.getOp();
                if (op instanceof Local local)
                    objManager.copy(local, lvar);
            } else {
                System.out.println("unsupported assignment rhs: " + stmt);
            }
        } else if (lhs instanceof FieldRef fieldRef) {
            // x.f = y
            if (fieldRef instanceof InstanceFieldRef instanceFieldRef) {
                if (rhs instanceof Local rvar)
                    objManager.storeField((Local) instanceFieldRef.getBase(), instanceFieldRef.getField(), rvar);
            } else {
                // c.f = y
                if (rhs instanceof Local rvar)
                    objManager.storeStaticField(fieldRef.getClass(), fieldRef.getField(), rvar);
            }
        } else if (lhs instanceof ArrayRef arrayRef) {
            // x[i] = y
            if (rhs instanceof Local rvar)
                objManager.storeArray((Local) arrayRef.getBase(), rvar);
        } else {
            System.out.println("unsupported assignment lhs: " + stmt);
        }
    }
    private Set<SootMethod> getCallee(Unit unit, InvokeExpr invoke) {
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        CallGraph cg = Scene.v().getCallGraph();
        Set<SootMethod> methods = new HashSet();
        for (Iterator<Edge> it = cg.edgesOutOf(unit); it.hasNext();) {
            Edge edge = it.next();
            SootMethod sootMethod = edge.getTgt().method();
//            if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
//                RefType baseType = (RefType) instanceInvokeExpr.getBase().getType();
//
//                SootClass declaringClass = sootMethod.getDeclaringClass();
//                SootClass baseClass = baseType.getSootClass();
//                if (!hierarchy.isClassSubclassOfIncluding(declaringClass, baseClass))
//                    continue;
//            }
            methods.add(sootMethod);
        }

        return methods;
    }
    public void visit(InvokeStmt stmt) {
        InvokeExpr invoke = stmt.getInvokeExpr();
        handleInvoke(stmt, invoke);
    }
    public void visit(ReturnStmt stmt) {
        Value retVal = stmt.getOp();
        if (retVal instanceof Local local) {
//            Transition transition = new OutSideTransition(-2, false, local);
//            method.addTransition(transition);
        } else if (retVal instanceof Constant constant){
            // Todo
            ConstantObj constantObj = new ConstantObj(retVal.getType(), methodContext, constant);

        } else {
            System.out.println("unsupported return type: " + retVal.getClass().getName());
        }
    }
}
