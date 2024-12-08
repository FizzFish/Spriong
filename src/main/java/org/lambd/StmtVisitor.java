package org.lambd;

import com.google.common.collect.HashBasedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lambd.condition.Condition;
import org.lambd.condition.Constraint;
import org.lambd.obj.*;
import org.lambd.pointer.Pointer;
import org.lambd.pointer.PointerToSet;
import org.lambd.pointer.VarPointer;
import org.lambd.transformer.SpStmt;
import org.lambd.transition.Effect;
import soot.*;
import soot.jimple.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Visitor模式访问不同的Stmts，关键的几种Statement:
 * 1. AssignStmt: 主要的数据流传播方式，具体还需要考虑域的问题
 * 2. InvokeStmt: 调用其他函数时也产生了数据流传播，精准的callee选择很重要（CHA、VTA）
 */
public class StmtVisitor {
    private static final Logger logger = LogManager.getLogger(StmtVisitor.class);
    private SpMethod container;
    Map<Stmt, Condition> conditionMap = new HashMap<>();
    private Condition currentCondition;

    public StmtVisitor(SpMethod method) {
        this(method, Condition.ROOT);
    }
    public StmtVisitor(SpMethod method, Condition context) {
        this.container = method;
        currentCondition = context;
    }
    public void visit(SpStmt spStmt) {
        Stmt stmt = spStmt.getStmt();
        if (conditionMap.containsKey(stmt))
            currentCondition = conditionMap.get(stmt);
        if (stmt instanceof IdentityStmt) {
            visitIdentity(spStmt);
        } else if (stmt instanceof AssignStmt) {
            visitAssign(spStmt);
        } else if (stmt instanceof InvokeStmt) {
            visitInvoke(spStmt);
        } else if (stmt instanceof ReturnStmt) {
            visitReturn(spStmt);
        } else if (stmt instanceof IfStmt ifStmt) {
            String repr = ifStmt.getCondition().toString();
            Condition cond = new Condition(repr, currentCondition);
            currentCondition = cond;
            Stmt target = ifStmt.getTarget();
            conditionMap.put(target, cond.not());
        } else if (stmt instanceof GotoStmt) {
            // TODO
        } else if (stmt instanceof SwitchStmt switchStmt) {
            // case(var)
            Unit defaultTarget = switchStmt.getDefaultTarget();
            if (switchStmt instanceof LookupSwitchStmt lookupSwitchStmt) {
                int size = lookupSwitchStmt.getLookupValues().size();
                for (int i = 0; i < size; i++) {
                    int v = lookupSwitchStmt.getLookupValue(i);
                    conditionMap.put((Stmt) switchStmt.getTarget(i), new Condition(String.valueOf(v), currentCondition));
                }
            } else if (stmt instanceof TableSwitchStmt tableSwitchStmt) {
                int low = tableSwitchStmt.getLowIndex();
                int high = tableSwitchStmt.getHighIndex();
                for (int i = low; i <= high; i++)
                    conditionMap.put((Stmt) switchStmt.getTarget(i), new Condition(String.valueOf(i), currentCondition));
            }
            conditionMap.put((Stmt) defaultTarget, new Condition("default", currentCondition));
        }
    }

    /**
     * source := @parameter0
     * this := @this: org.apache.commons.text.StringSubstitutor
     * @param spStmt
     */
    public void visitIdentity(SpStmt spStmt) {
        IdentityStmt stmt = (IdentityStmt) spStmt.getStmt();
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
    private void visitInvoke(SpStmt spStmt) {
        InvokeStmt stmt = (InvokeStmt) spStmt.getStmt();
        InvokeExpr invoke = stmt.getInvokeExpr();
        handleInvoke(spStmt, invoke);
    }
    private void analyzeCallee(InvokeExpr invoke, Set<SootMethod> calleeSet, PointerToSet ptset) {
        SpHierarchy cg = SpHierarchy.v();
        // there are a lot of compromises here
        if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
            Local base = (Local) instanceInvokeExpr.getBase();
            VarPointer vp = ptset.getVarPointer(base);
            Type type = base.getType();
            // special, virtual, interface
            if (invoke instanceof SpecialInvokeExpr) {
                // 因为SpecialInvoke不涉及动态绑定，所以只需要在methodRef声明的class中即可找到
                 SootMethod callee = cg.resolve(invoke, invoke.getMethodRef().getDeclaringClass());
                 calleeSet.add(callee);
            } else if (vp.isEmpty()) {
                logger.warn("No base for invoke: {}", invoke);
            } else if (type instanceof RefType rt && rt.getSootClass().isInterface() && vp.allFake()) {
                // 某些情况下可能没有实际的变量，例如Cls.field或者由某些未分析的代码实例化
                // 检查是否需要创建Condition
                // 这里先不分析
                cg.getCallee(invoke, rt, calleeSet);
            } else {
                 // 找到所有可能得类型
                // RealObj直接进入分析
                // FormatObj需要查看callSite的Obj，还需要判断是否需要增加Constraint
                 SpHierarchy.calleeSetFromPointer(invoke, vp, calleeSet);
                 if (calleeSet.isEmpty())
                    logger.debug("{} cannot resolve {}, obj are {}", container.name, invoke, vp.getObjs());
                 else {
                     Constraint constraint = new Constraint(vp.paramRelations(), invoke, calleeSet);
                     container.getSummary().addConstraint(constraint);
                 }
            }
        } else {
            // static prune
            if (invoke.getArgCount() == 0)
                return;
            SootMethod callee = cg.resolve(invoke, null);
            calleeSet.add(callee);
        }
    }
    private void handleInvoke(SpStmt spStmt, InvokeExpr invoke) {
        SootWorld world = SootWorld.v();
        boolean quickInvoke = world.quickMethodRef(invoke.getMethodRef(), container, spStmt);
        boolean systemInvoke = !invoke.getMethodRef().getDeclaringClass().isApplicationClass();
        if (!quickInvoke && !systemInvoke) {

            PointerToSet ptset = container.getPtset();
            Set<SootMethod> calleeSet = new HashSet<>();
            analyzeCallee(invoke, calleeSet, ptset);
            for (SootMethod callee : calleeSet) {
                 if (callee != null)
                     apply(callee, spStmt);
             }
        }
    }

    private void handleArguments(InvokeExpr invoke, SpMethod callee) {
        PointerToSet ptset = container.getPtset();
        PointerToSet calleePtset = callee.getPtset();
        for (int i = -1; i < invoke.getArgCount(); ++i) {
            Value arg;
            if (i == -1) {
                if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
                    arg = instanceInvokeExpr.getBase();
                } else
                    continue;
            } else {
                arg = invoke.getArg(i);
            }
            if (arg instanceof Local local && local.getType() instanceof RefType) {
                Set<RealObj> objs = ptset.getVarPointer(local).getRealObjs();
                calleePtset.setParamObjMap(i, objs);
            }
        }
    }
    private Effect matchEffect(InvokeExpr invoke, SpMethod callee) {
        Map<Integer, Local> varMap = new HashMap<>();
        for (int i = -1; i < invoke.getArgCount(); ++i) {
            Value arg;
            if (i == -1) {
                if (invoke instanceof InstanceInvokeExpr instanceInvokeExpr) {
                    arg = instanceInvokeExpr.getBase();
                } else
                    continue;
            } else {
                arg = invoke.getArg(i);
            }
            if (arg instanceof Local local) {
                varMap.put(i, local);
            }
        }
        return callee.getSummary().match(varMap);
    }

    /**
     * 如果已经访问过callee，则之间应用该函数的摘要效果；
     * 否则先访问该函数，再应用其摘要效果
     * 这里想解决callgraph中的环问题，但没有处理好[TODO]
     */
    private void apply(SootMethod callee, SpStmt stmt) {
        if (callee == null)
            return;
        applyInternal(callee, stmt);
        // update neo4j callgraph
        SootMethod sm = container.getSootMethod();
        SootWorld.v().getGraph().updateNeo4jRelation(sm.getName(), sm.getSignature(), callee.getName(), callee.getSignature());
    }
    private void applyInternal(SootMethod callee, SpStmt stmt) {
        SootWorld world = SootWorld.v();
        if (container.getSootMethod() == callee)
            return;
        InvokeExpr invoke = stmt.getStmt().getInvokeExpr();
        if (!callee.getDeclaringClass().isApplicationClass()) {
            return;
        }
        SpMethod spCallee = world.getMethod(callee);
        if (spCallee.visited()) {
            Effect effect = matchEffect(invoke, spCallee);
            if (effect != null) {
                effect.apply(container, stmt);
                return;
            }
        } else {
            // first visited
            spCallee.caller = container;
        }
        // 第一次访问，或者没有合适的函数摘要
        world.visitMethod(spCallee, method -> handleArguments(invoke, method));
        spCallee.getSummary().applyLastEffect(container, stmt);
    }

    /**
     * x=y
     * x=y.f; x=c.f; x=y[i]
     * x=cast(T)y: 共享相同的ptset: getSameVP
     * x.f = y; c.f = y; x[i] = y
     */
    public void visitAssign(SpStmt spStmt) {
        AssignStmt stmt = (AssignStmt) spStmt.getStmt();
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        PointerToSet pts = container.getPtset();

        if (rhs instanceof InvokeExpr invoke) {
            handleInvoke(spStmt, invoke);
            return;
        }
        ObjManager objManager = container.getManager();
        if (lhs instanceof Local lvar) {
            if (rhs instanceof Local rvar) {
                // x = y
                objManager.copy(rvar, lvar);
            } else if (rhs instanceof AnyNewExpr newExpr) {
                // x = new T
                pts.addLocal(lvar, new RealObj(newExpr.getType(), spStmt));
            } else if (rhs instanceof Constant constant) {
                // x = (T) y
                pts.addLocal(lvar, new ConstantObj(rhs.getType(), spStmt, constant));
            } else if (rhs instanceof FieldRef fieldRef) {
                SootField field = fieldRef.getField();
                // x = y.f
                if (fieldRef instanceof InstanceFieldRef instanceFieldRef) {
                    Local base = (Local) instanceFieldRef.getBase();

                    objManager.loadField(lvar, base, field);
                } else    // x = c.f
                    objManager.loadStaticField(lvar, fieldRef.getClass(), field);
            } else if (rhs instanceof ArrayRef arrayRef) {
                // x = y[i]
                objManager.loadArray(lvar, (Local) arrayRef.getBase(), spStmt);
            } else if (rhs instanceof BinopExpr) {
            } else if (rhs instanceof UnopExpr) {
            } else if (rhs instanceof InstanceOfExpr) {
            } else if (rhs instanceof CastExpr castExpr) {
                // StmpManager::add: ((Log4jLogEvent)event).makeMessageImmutable();
                // maybe need turn obj type
                Value op = castExpr.getOp();
                if (op instanceof Local rvar && lvar.getType() instanceof RefType rt)
                    container.getPtset().handleCast(rvar, lvar, rt, spStmt);
            } else {
                System.out.println("unsupported assignment rhs: " + stmt);
            }
        } else if (lhs instanceof FieldRef fieldRef) {
            SootField field = fieldRef.getField();
            if (rhs instanceof Local rvar) {
                // x.f = y
                if (fieldRef instanceof InstanceFieldRef instanceFieldRef) {
                    Local base = (Local) instanceFieldRef.getBase();
                    objManager.storeField(base, field, rvar, spStmt);
                } else {
                    // c.f = y
                    objManager.storeStaticField(fieldRef.getClass(), field, rvar);
                }
            }
        } else if (lhs instanceof ArrayRef arrayRef) {
            // x[i] = y
            Local base = (Local) arrayRef.getBase();
            if (rhs instanceof Local rvar) {
                objManager.storeArray(base, rvar, spStmt);
            } else if (rhs instanceof Constant constant) {
                ConstantObj constantObj = new ConstantObj(rhs.getType(), spStmt, constant);
                pts.addArray(base, constantObj);
            }
        } else {
            System.out.println("unsupported assignment lhs: " + stmt);
        }
    }

    public void visitReturn(SpStmt spStmt) {
        ReturnStmt stmt = (ReturnStmt) spStmt.getStmt();
        Value retVal = stmt.getOp();
        if (retVal instanceof Local local) {
            container.getPtset().genReturn(local, spStmt);
        }
    }
}
