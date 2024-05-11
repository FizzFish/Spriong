package org.lambd;

import org.lambd.obj.NewObj;
import org.lambd.obj.Obj;
import org.lambd.obj.ObjManager;
import org.lambd.transition.Weight;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        boolean isSystemCallee = !invoke.getMethodRef().getDeclaringClass().isApplicationClass();
        if (signature.contains("getConnection") || signature.contains("getDefaultManager") || signature.contains("close"))
            return;
        SootWorld world = SootWorld.v();
        if (!world.quickMethodRef(signature, methodContext, stmt)) {
            if (isSystemCallee)
                return;
            Set<SootMethod> callees;
            if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
                Local base = (Local) instanceInvokeExpr.getBase();
//                methodContext.cg.resolve(invoke, cls);
                Set<Type> types = methodContext.getPtset().getVarPointer(base)
                        .getObjs().stream().map(Obj::getType).collect(Collectors.toSet());
                callees = getCallee(stmt, types);
            } else {
                callees = getCallee(stmt, null);
            }
            for (SootMethod callee : callees) {
                if (callee.getDeclaringClass().hasOuterClass() && !callee.getDeclaringClass().getOuterClass().getShortName().equals("PatternLayout"))
                    continue;
                if (world.getVisited().contains(callee)) {
                    if (methodContext.getSootMethod() != callee)
                        world.quickCallee(callee, methodContext, stmt);
                } else {
                    SootWorld.v().visitMethod(callee, methodContext);
                    world.quickCallee(callee, methodContext, stmt);
                }
            }
        }
    }
    private boolean isSystemCallee(String signature) {
        return signature.contains("java.lang.System");
    }
    public void visit(AssignStmt stmt) {
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (rhs instanceof InvokeExpr invoke) {
            handleInvoke(stmt, invoke);
            return;
        }
        ObjManager objManager = methodContext.getManager();
        if (lhs instanceof Local lvar) {
            if (rhs instanceof Local rvar) {
                objManager.copy(rvar, lvar);
            } else if (rhs instanceof AnyNewExpr newExpr) {
                objManager.handleNew(lvar, newExpr.getType());
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
                    objManager.storeField((Local) instanceFieldRef.getBase(), instanceFieldRef.getField(), rvar, stmt);
            } else {
                // c.f = y
                if (rhs instanceof Local rvar)
                    objManager.storeStaticField(fieldRef.getClass(), fieldRef.getField(), rvar);
            }
        } else if (lhs instanceof ArrayRef arrayRef) {
            // x[i] = y
            if (rhs instanceof Local rvar)
                objManager.storeArray((Local) arrayRef.getBase(), rvar, stmt);
        } else {
            System.out.println("unsupported assignment lhs: " + stmt);
        }
    }
    private Set<SootMethod> getCallee(Unit unit, Set<Type> types) {
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        CallGraph cg = Scene.v().getCallGraph();
        Set<SootMethod> methods = new HashSet();
        for (Iterator<Edge> it = cg.edgesOutOf(unit); it.hasNext();) {
            Edge edge = it.next();
            SootMethod sootMethod = edge.getTgt().method();
            if (types != null) {
                Type type = sootMethod.getDeclaringClass().getType();
                if (!types.contains(type))
                    continue;
            }
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
            methodContext.handleReturn(local, stmt);
        }
    }
}
