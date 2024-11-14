package org.lambd;

import org.lambd.obj.*;
import org.lambd.pointer.InstanceField;
import org.lambd.pointer.PointerToSet;
import org.lambd.pointer.VarPointer;
import org.lambd.utils.Utils;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Visitor模式访问不同的Stmts，关键的几种Statement:
 * 1. AssignStmt: 主要的数据流传播方式，具体还需要考虑域的问题
 * 2. InvokeStmt: 调用其他函数时也产生了数据流传播，精准的callee选择很重要（CHA、VTA）
 */
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

    /**
     * invoke的处理：
     * 1.系统调用不进入处理
     * 2.如果是之前自定义的函数signature，则直接应用该函数的效果：quickMethodRef
     * 3.通过ptset(base)来找到精确的callee，然后应用该函数的效果：apply(callee, stmt)
     * (1)ptset(base)为空，增加一个Obj对象；剪枝invoke.getArgCount() == 0的情况
     * (2)SpecialInvoke
     * (3)InterfaceInvoke || base is abstract：处理所有callee
     * (4)VirtualInvoke: resolve(invoke, obj.type)
     * (5)StaticInvoke:
     */
    private void handleInvoke(Stmt stmt, InvokeExpr invoke) {
        String signature = invoke.getMethodRef().getSignature();
        boolean isSystemCallee = !invoke.getMethodRef().getDeclaringClass().isApplicationClass();
        SootWorld world = SootWorld.v();
        if (!world.quickMethodRef(signature, container, stmt)) {
            if (isSystemCallee) {
                // 由于不进入真正的函数内部，而且不在已有知识库中，因此需要为lhs创建一个Obj对象
                if (stmt instanceof AssignStmt) {
                    Type type = invoke.getMethodRef().getReturnType();
                    if (type instanceof RefType rt)
                        container.handleReturn(stmt, rt);
                }
                return;
            }
            SpHierarchy cg = SpHierarchy.v();
            PointerToSet ptset = container.getPtset();
            Map<Integer, Set<SootClass>> mayClassMap = handleArguments(invoke);
            // there are a lot of compromises here
            if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
                Local base = (Local) instanceInvokeExpr.getBase();
                VarPointer vp = ptset.getVarPointer(base);
                if (vp.isEmpty()) {
                    // base = Cls.field
                    vp.add(new TypeObj((RefType) base.getType(), stmt));
//                    System.err.println("vp is empty");
                }
                // special, virtual, interface
                if (invoke instanceof SpecialInvokeExpr) {
                     SootMethod callee = cg.resolve(invoke, invoke.getMethodRef().getDeclaringClass());
                     apply(callee, stmt, mayClassMap);
                } else if (vp.allInterface()) {
                    if (base.getType() instanceof RefType rt) {
                        cg.getCallee(invoke, rt).forEach(callee -> {
                            apply(callee, stmt, mayClassMap);
                        });
                    }
                } else {
                     // 找到所有可能得类型
                     Set<SootClass> possibleSootClasses = new HashSet<>();
                     vp.objs().forEach(obj -> {
                         if (obj.getType() instanceof RefType rt) {
                             SootClass sc = rt.getSootClass();
                             if (!sc.isInterface()) {
                                 possibleSootClasses.add(sc);
                                 if (obj.isMayMultiple() && obj instanceof FormatObj formatObj)
                                     possibleSootClasses.addAll(container.getMayClass(formatObj.getIndex()));
                             }
                         }
                     });
                     Set<SootMethod> calleeSet = possibleSootClasses.stream()
                             .map(sc-> cg.resolve(invoke, sc)).collect(Collectors.toSet());
                     for (SootMethod callee: calleeSet) {
                         if (callee != null)
                            apply(callee, stmt, mayClassMap);
                     }
                }
            } else {
                // static prune
                if (invoke.getArgCount() == 0)
                    return;
                SootMethod callee = cg.resolve(invoke, null);
                apply(callee, stmt, mayClassMap);
            }
        }
    }
    private Map<Integer, Set<SootClass>> handleArguments(InvokeExpr invoke) {
        PointerToSet ptset = container.getPtset();
        Map<Integer, Set<SootClass>> mayClassMap = new HashMap<>();
        for (int i = 0; i < invoke.getArgCount(); ++i) {
            Value arg = invoke.getArg(i);
            if (arg instanceof Local local) {
                for (Obj obj: ptset.getLocalObjs(local))
                    if (obj.getType() instanceof RefType rt && !rt.getSootClass().isInterface())
                        mayClassMap.computeIfAbsent(i, n -> new HashSet<>()).add(rt.getSootClass());
            }
        }
        return mayClassMap;
    }

    /**
     * 如果已经访问过callee，则之间应用该函数的摘要效果；
     * 否则先访问该函数，再应用其摘要效果
     * 这里想解决callgraph中的环问题，但没有处理好[TODO]
     */
    private void apply(SootMethod callee, Stmt stmt, Map<Integer, Set<SootClass>> mayClassMap) {
        SootWorld world = SootWorld.v();
        if (container.getSootMethod() == callee)
            return;
        if (world.getVisited().contains(callee)) {
            world.addActiveEdge(callee, container);
            world.quickCallee(callee, container, stmt);
        } else if (callee.getDeclaringClass().isApplicationClass()) {
            // first visit callee
            world.getMethod(callee).caller = container;
            world.visitMethod(callee, mayClassMap);
            world.quickCallee(callee, container, stmt);
        }
        // update neo4j callgraph
        world.updateNeo4jRelation(container.getSootMethod(), callee);
    }

    /**
     * x=y
     * x=y.f; x=c.f; x=y[i]
     * x=cast(T)y: 共享相同的ptset: getSameVP
     * x.f = y; c.f = y; x[i] = y
     */
    public void visit(AssignStmt stmt) {
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        PointerToSet pts = container.getPtset();

        if (rhs instanceof InvokeExpr invoke) {
            handleInvoke(stmt, invoke);
            return;
        }
        ObjManager objManager = container.getManager();
        if (lhs instanceof Local lvar) {
            if (rhs instanceof Local rvar) {
                objManager.copy(rvar, lvar);
            } else if (rhs instanceof AnyNewExpr newExpr) {
                pts.addLocal(lvar, new TypeObj(newExpr.getType(), stmt));
            } else if (rhs instanceof Constant constant) {
                pts.addLocal(lvar, new ConstantObj(rhs.getType(), stmt, constant));
            } else if (rhs instanceof FieldRef fieldRef) {
                // x = y.f
                if (fieldRef instanceof InstanceFieldRef instanceFieldRef)
                    objManager.loadField(lvar, (Local) instanceFieldRef.getBase(), instanceFieldRef.getField(), stmt);
                else    // x = c.f
                    objManager.loadStaticField(lvar, fieldRef.getClass(), fieldRef.getField());
            } else if (rhs instanceof ArrayRef arrayRef) {
                // x = y[i]
                objManager.loadArray(lvar, (Local) arrayRef.getBase(), stmt);
            } else if (rhs instanceof BinopExpr) {
            } else if (rhs instanceof UnopExpr) {
            } else if (rhs instanceof InstanceOfExpr) {
            } else if (rhs instanceof CastExpr castExpr) {
                // StmpManager::add: ((Log4jLogEvent)event).makeMessageImmutable();
                // maybe need turn obj type
                Value op = castExpr.getOp();
                if (op instanceof Local rvar && lvar.getType() instanceof RefType rt)
                    container.getPtset().handleCast(rvar, lvar, rt, stmt);
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
            Local base = (Local) arrayRef.getBase();
            if (rhs instanceof Local rvar) {
                objManager.storeArray(base, rvar, stmt);
            } else if (rhs instanceof Constant constant) {
                ConstantObj constantObj = new ConstantObj(rhs.getType(), stmt, constant);
                pts.addArray(base, constantObj);
            }
        } else {
            System.out.println("unsupported assignment lhs: " + stmt);
        }
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
