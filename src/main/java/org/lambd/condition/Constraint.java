package org.lambd.condition;

import org.lambd.StmtVisitor;
import org.lambd.obj.Obj;
import org.lambd.obj.RealObj;
import org.lambd.pointer.Pointer;
import org.lambd.pointer.PointerToSet;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.jimple.InvokeExpr;

import java.util.*;
import java.util.stream.Collectors;

// 描述某个函数摘要中的关键约束，例如第i个参数的类型，或者第i个参数的field的类型
// 解析出来的的<callee>
// argi.fields -> <callee>
public class Constraint {
    private Map<Integer, List<SootField>> paramRelations = new HashMap<>();
    private InvokeExpr invoke;
    private Set<SootMethod> calleeSet = new HashSet<>();
    public Constraint(Map<Integer, List<SootField>> paramRelations, InvokeExpr invoke, Set<SootMethod> calleeSet) {
        this.paramRelations = paramRelations;
        this.invoke = invoke;
        this.calleeSet.addAll(calleeSet);
    }
    public boolean satisfy(Map<Integer, Local> varMap, PointerToSet pts) {
        for (Map.Entry<Integer, Local> entry: varMap.entrySet()) {
            int index = entry.getKey();
            // 有的arg没有要求
            if (!paramRelations.containsKey(index))
                continue;
            if (!satisfy(entry.getValue(), index, pts))
                return false;
        }
        return true;
    }
    public boolean satisfy(Local var, int index, PointerToSet pts) {
        Set<SootMethod> newSet = new HashSet<>();
        List<SootField> fields = paramRelations.get(index);
        if (fields==null)
            System.out.println();
        pts.varFields(var, fields).forEach(p -> {
            newSet.addAll(StmtVisitor.calleeSetFromPointer(invoke, p));
        });
        return calleeSet.containsAll(newSet);
    }
}
