package org.lambd;

import org.lambd.obj.Obj;
import org.lambd.obj.ObjManager;
import org.lambd.pointer.PointerToSet;
import org.lambd.pointer.VarPointer;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
        SootWorld world = SootWorld.v();
        if (!world.quickMethodRef(signature, container, stmt)) {
            if (isSystemCallee)
                return;
            SpHierarchy cg = SpHierarchy.v();
            PointerToSet ptset = container.getPtset();
            // there are a lot of compromises here
            if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
                Local base = (Local) instanceInvokeExpr.getBase();
                VarPointer vp = ptset.getVarPointer(base);
                // prune
                if (!ptset.hasFormatObj(vp) && invoke.getArgCount() == 0)
                    return;
//                if (vp.isEmpty() && base.getType() instanceof RefType rt && rt.getSootClass().getShortName().equals("ReusableSimpleMessage")) {
                if (vp.isEmpty()) {
                    vp.add(new Obj(base.getType(), stmt));
                }
                 if (invoke instanceof SpecialInvokeExpr) {
                    SootMethod callee = cg.resolve(invoke, invoke.getMethodRef().getDeclaringClass());
                    apply(callee, stmt);
                } else if (instanceInvokeExpr instanceof InterfaceInvokeExpr
                        || vp.isEmpty() // sometimes there are no objs
                        || ptset.hasAbstractObj(vp)) {
                    getCallee(stmt).forEach(callee -> {
                        apply(callee, stmt);
                    });
                } else if (instanceInvokeExpr instanceof VirtualInvokeExpr) {
                    vp.getObjs().forEach(obj -> {
                        if (obj.getType() instanceof RefType rt) {
                            SootMethod callee = cg.resolve(invoke, rt.getSootClass());
                            if (callee != null)
                                apply(callee, stmt);
                        }
                    });
                }
            } else {
                // static prune
                if (invoke.getArgCount() == 0)
                    return;
                SootMethod callee = cg.resolve(invoke, null);
                apply(callee, stmt);
            }
        }
    }
    private void apply(SootMethod callee, Stmt stmt) {
        SootWorld world = SootWorld.v();
        if (world.getVisited().contains(callee)) {
            if (container.getSootMethod() != callee) {
                world.addActiveEdge(callee, container);
                world.quickCallee(callee, container, stmt);
            }
        } else {
            world.visitMethod(callee);
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
                container.getPtset().addLocal(lvar, new Obj(newExpr.getType(), stmt));
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
                // StmpManager::add: ((Log4jLogEvent)event).makeMessageImmutable();
                // maybe need turn obj type
                Value op = castExpr.getOp();
                if (op instanceof Local rvar)
                    container.getPtset().getSameVP(rvar, lvar);
//                if (op instanceof Local local && lvar.getType() instanceof RefType rtl) {
//                    container.getPtset().handleCast(lvar, local, rtl);
//                }
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
            container.getPtset().genReturn(local, stmt);
        }
    }
}
