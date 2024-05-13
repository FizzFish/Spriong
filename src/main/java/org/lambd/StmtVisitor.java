package org.lambd;

import org.lambd.obj.Obj;
import org.lambd.obj.ObjManager;
import org.lambd.pointer.VarPointer;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class StmtVisitor {
    private SpMethod container;
    public StmtVisitor(SpMethod method) {
        this.container = method;
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
        if (!world.quickMethodRef(signature, container, stmt)) {
            if (isSystemCallee)
                return;
            SpCallGraph cg = container.getCg();
            // there are a lot of compromises here
            if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
                Local base = (Local) instanceInvokeExpr.getBase();
                VarPointer vp = container.getPtset().getVarPointer(base);
                if (instanceInvokeExpr instanceof InterfaceInvokeExpr
                        || vp.isEmpty()
                        || container.getPtset().hasAbstractObj(vp)) {
                    getCallee(stmt).forEach(callee -> {
                        apply(callee, stmt);
                    });
                } else if (instanceInvokeExpr instanceof VirtualInvokeExpr) {
                    vp.getObjs().forEach(obj -> {
                        if (obj.getType() instanceof RefType rt) {
                            SootMethod callee = cg.resolve(invoke, rt.getSootClass());
                            if (callee == null)
                                return;
                            apply(callee, stmt);
                        }
                    });
                } else if (invoke instanceof SpecialInvokeExpr) {
                    SootMethod callee = cg.resolve(invoke, invoke.getMethodRef().getDeclaringClass());
                    apply(callee, stmt);
                }
            } else {
                SootMethod callee = cg.resolve(invoke, null);
                apply(callee, stmt);
            }
        }
    }
    private void apply(SootMethod callee, Stmt stmt) {
        if (callee.getDeclaringClass().hasOuterClass() && !callee.getDeclaringClass().getOuterClass().getShortName().equals("PatternLayout"))
            return;
        SootWorld world = SootWorld.v();
        if (world.getVisited().contains(callee)) {
            if (container.getSootMethod() != callee)
                world.quickCallee(callee, container, stmt);
        } else {
            world.visitMethod(callee, container);
            world.quickCallee(callee, container, stmt);
        }
    }
    public void visit(AssignStmt stmt) {
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (rhs instanceof InvokeExpr invoke) {
            handleInvoke(stmt, invoke);
            return;
        }
        ObjManager objManager = container.getManager();
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
    private Set<SootMethod> getCallee(Unit unit) {
        Hierarchy hierarchy = Scene.v().getActiveHierarchy();
        CallGraph cg = Scene.v().getCallGraph();
        Set<SootMethod> methods = new HashSet();
        for (Iterator<Edge> it = cg.edgesOutOf(unit); it.hasNext();) {
            Edge edge = it.next();
            SootMethod sootMethod = edge.getTgt().method();
//            if (types != null) {
//                Type type = sootMethod.getDeclaringClass().getType();
//                if (!types.contains(type))
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
            container.handleReturn(local, stmt);
        }
    }
}
