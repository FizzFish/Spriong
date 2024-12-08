package org.lambd.pointer;

import com.google.common.collect.HashBasedTable;
import org.lambd.SpMethod;
import org.lambd.condition.Condition;
import org.lambd.obj.*;
import org.lambd.transformer.SpStmt;
import org.lambd.transition.Summary;
import org.lambd.transition.Weight;
import org.lambd.utils.Utils;
import soot.*;

import java.util.*;
import java.util.stream.Collectors;

public class PointerToSet {
    private Map<Local, VarPointer> vars = new HashMap<>();
    private HashBasedTable<Obj, SootField, InstanceField> fields = HashBasedTable.create();
    private Map<SootField, StaticField> statics = new HashMap<>();
    private SpMethod container;
    private Map<Integer, FormatObj> paramMap = new HashMap<>();
    public PointerToSet(SpMethod container) {
        this.container = container;
        Body body;
        try {
            body = container.getSootMethod().getActiveBody();
        } catch (Exception e) {
            return;
        }
        SpStmt start = new SpStmt(container, null, -1);
        // 生成FormatObj抽象参数对象
        List<Local> parameters = body.getParameterLocals();
        for (int i = 0; i < parameters.size(); i++) {
            Local var = parameters.get(i);
            FormatObj obj = new FormatObj(var.getType(), start, i);
            addLocal(var, obj);
            paramMap.put(i, obj);
        }
        if (!container.getSootMethod().isStatic()) {
            Local thisVar = body.getThisLocal();
            Type type = container.getSootMethod().getDeclaringClass().getType();
            FormatObj obj = new FormatObj(type, start, -1);
            addLocal(thisVar, obj);
            paramMap.put(-1, obj);
        }
    }

    /**
     * 场景：(1) callee参数 (2) NewExpr
     */
    public void addLocal(Local var, Obj obj) {
        VarPointer vp = getVarPointer(var);
        vp.add(obj);
    }
    public void setParamObjMap(int i, Set<RealObj> objs) {
        paramMap.get(i).setRealObj(objs);
    }

    /**
     * pointer(to) merge pointer(from) objects
     */
    public void copy(Pointer from, Pointer to) {
        if (from == null || to == null)
            return;
        to.copyFrom(from);
    }

    /**
     * 对于一个有update属性的Weight关系来说，field写需要进一步更新FormatObj对象之间的Weight关系
     * 对于to.w2 = from.w1，如果to和from均包含参数对象，且w1/w2具有update属性，则生成新的transition
     * 这里感觉需要对w2进行判断，即w2不能为空，需要进一步测试[TODO]
     */
    public void update(Local from, Local to, Weight weight, SpStmt stmt, Type type) {
        // to.w2 = from.w1
        Set<Pointer> fields = varFields(from, weight.getFromFields());
        Set<Pointer> tFields = varFields(to, weight.getToFields());
        if (fields.isEmpty() || tFields.isEmpty())
            return;
        // handle from objs
        Set<Obj> objs = fields.stream()
                .flatMap(Pointer::objs)
                .map(o -> o.castClone(stmt, type))
                .collect(Collectors.toSet());
        Set<FormatObj> formatObjs = objs.stream().filter(FormatObj.class::isInstance)
                .map(FormatObj.class::cast).collect(Collectors.toSet());

        tFields.forEach(toPointer -> {
            if (weight.isUpdate()) {
                toPointer.formatObjs().forEach(tfObj -> {
                    formatObjs.forEach(ffObj -> {
                        if (ffObj.getIndex() != tfObj.getIndex()) {
                            Weight w = new Weight(ffObj.getFields(), tfObj.getFields());
                            w.setUpdate();
                            container.getSummary().addTransition(ffObj.getIndex(), tfObj.getIndex(), w, stmt);
                        }
                    });
                });
            }
            toPointer.addAll(objs);
        });
    }

    /**
     * 场景x.f = y 或 x[i] = y
     * 这里貌似base不会是GenObj，需要进一步测试[TODO]
     */
    public void storeAlias(VarPointer from, FormatObj base, SootField field, SpStmt stmt) {
        // x.f = y
        from.getObjs().forEach(f -> {
            if (f instanceof FormatObj fObj) {
                if (base.getIndex() != fObj.getIndex()) {
                    List<SootField> toFields = new ArrayList<>(base.getFields());
                    toFields.add(field);
                    Weight w = new Weight(fObj.getFields(), toFields);
                    w.setUpdate();
                    container.getSummary().addTransition(fObj.getIndex(), base.getIndex(), w, stmt);
                }
            }
        });

    }

    /**
     * 在sink反向跟踪时，仅关注String类型，可以大幅减少分析
     * 但是在Java Web中的Request请求分析时，可能需要完善[TODO]
     */
    public void genSink(String sink, Weight weight, Local var, SpStmt stmt) {
        // var.w => sink
        varFields(var, weight.getFromFields()).stream().
                flatMap(Pointer::formatObjs)
                .forEach(o -> {
                    int i = o.getIndex();
                    if (canHoldSink(o.getType()) && i != -1) {
                        Weight w = new Weight(o.getFields());
                        container.getSummary().addSink(sink, i, w, stmt);
                    }
                });
    }

    public VarPointer getVarPointer(Local var) {
        return vars.computeIfAbsent(var,
                f -> new VarPointer(var));
    }
    public Set<Obj> getLocalObjs(Local var) {
        return getVarPointer(var).getObjs();
    }
    public void getSameVP(Local from, Local to) {
        if (vars.containsKey(from)) {
            vars.put(to, vars.get(from));
        }
    }
    public InstanceField getInstanceField(Obj base, SootField field) {
        return base.fieldPointer(field);
    }
    public Set<Pointer> varFields(Local var, List<SootField> fields) {
        Set<Pointer> pointers = Set.of(getVarPointer(var));
        for (SootField field : fields) {
            pointers = pointers.stream().flatMap(Pointer::objs)
                    .map(o -> getInstanceField(o, field))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (pointers.isEmpty())
                return pointers;
        }
        return pointers;
    }
    public InstanceField getArrayIndex(Obj base) {
        return base.fieldPointer(Utils.arrayField);
    }
    public StaticField getStaticField(SootField field) {
        return statics.computeIfAbsent(field,
                f -> new StaticField(field));
    }
    private boolean canHoldSink(Type type) {
        /**
        String typeStr = type.toString();
        if (typeStr.equals("java.lang.String") || typeStr.equals("java.lang.StringBuilder")
                || typeStr.equals("java.lang.CharSequence")
                || typeStr.equals("char[]"))
            return true;
        if (typeStr.startsWith("org.glassfish.jersey") || typeStr.startsWith("org.eclipse.jetty")
                || typeStr.equals("javax.servlet.http.HttpServletRequest"))
            return true;
        System.err.println("Unsupported sink type: " + type);
         */
        return true;
    }



    /**
     * return如果包含FormatObj，则需要形成Transition
     * 注意FormatObj.field的情况也需要体现出来
     * 对于NewObj需要返回到lhs，以形成实际的类型对象
     * @param retVar
     * @param stmt
     */
    public void genReturn(Local retVar, SpStmt stmt) {
        Summary summary = container.getSummary();
        getVarPointer(retVar).getObjs().forEach(obj -> {
            if (obj instanceof FormatObj fobj) {
                Weight w = new Weight(fobj.getFields());
                summary.addTransition(fobj.getIndex(), -2, w, stmt);
            } else {
                if (obj.getType() instanceof RefType rt)
                    summary.addReturn(rt);
            }
//            fields.row(obj).forEach((field, ifield) -> {
//                ifield.getObjs().forEach(o -> {
//                    if (o instanceof FormatObj fobj) {
//                        // ret.field = fobj
//                        Weight w = new Weight(fobj.getFields(), field);
//                        summary.addTransition(fobj.getIndex(), -2, w, stmt);
//                    }
//                });
//            });
        });
    }

    public void handleCast(Local from, Local to, RefType type, SpStmt stmt) {
        VarPointer fromPointer = getVarPointer(from);
        VarPointer toPointer = getVarPointer(to);
        fromPointer.getObjs().forEach(obj -> {
            toPointer.add(obj.castClone(stmt, type));
        });
    }
    public int getVarSize() {
        return vars.size();
    }
    public int getObjSize() {
        int size = 0;
        for (Pointer p : vars.values())
            size += p.getObjs().size();
        return size;
    }
    // 为了统计某个数组可能得Constant值
    public void addArray(Local base, Obj obj) {
        getLocalObjs(base).forEach(o -> {
            getArrayIndex(o).add(obj);
        });
    }
    public Set<String> getArrayString(Local base) {
        return getLocalObjs(base).stream()
                .flatMap(obj -> getArrayIndex(obj).constantObjs())
                .map(ConstantObj::getString)
                .collect(Collectors.toSet());
    }
}
