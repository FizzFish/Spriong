package org.lambd.pointer;

import com.google.common.collect.HashBasedTable;
import org.lambd.SpCallGraph;
import org.lambd.SpMethod;
import org.lambd.obj.FormatObj;
import org.lambd.obj.GenObj;
import org.lambd.obj.Obj;
import org.lambd.transition.Summary;
import org.lambd.transition.Weight;
import org.lambd.utils.Utils;
import soot.*;
import soot.jimple.Stmt;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PointerToSet {
    private Map<Local, VarPointer> vars = new HashMap<>();
    private HashBasedTable<Obj, String, InstanceField> fields = HashBasedTable.create();
    private Map<Obj, ArrayIndex> arrays = new HashMap<>();
    private Map<SootField, StaticField> statics = new HashMap<>();
    private SpMethod container;
    public PointerToSet(SpMethod container) {
        this.container = container;
        Body body;
        try {
            body = container.getSootMethod().getActiveBody();
        } catch (Exception e) {
            return;
        }

        List<Local> parameters = body.getParameterLocals();
        for (int i = 0; i < parameters.size(); i++) {
            Local var = parameters.get(i);
            FormatObj obj = new FormatObj(var.getType(), container, i);
            addLocal(var, obj);
        }
        if (!container.getSootMethod().isStatic()) {
            Local thisVar = body.getThisLocal();
            FormatObj obj = new FormatObj(thisVar.getType(), container, -1);
            addLocal(thisVar, obj);
        }
    }
    public void addLocal(Local var, Obj obj) {
        VarPointer vp = getVarPointer(var);
        vp.add(obj);
    }
    public void copy(Pointer from, Pointer to) {
        if (from == null || to == null)
            return;
        to.addAll(from.getObjs());
    }
    public void update(Local from, Local to, Weight weight, Stmt stmt) {
        // to.w2 = from.w1
        Set<Pointer> fields = varFields(from, weight.getFromFields());
        if (fields.isEmpty())
            return;
        Set<Obj> objs = fields.stream()
                .flatMap(Pointer::objs)
                .collect(Collectors.toSet());

        varFields(to, weight.getToFields()).forEach(toPointer -> {
            if (weight.isUpdate()) {
                toPointer.formatObjs().forEach(tfObj -> {
                    objs.stream().filter(Obj::isFormat).map(obj -> (FormatObj) obj).forEach(ffObj -> {
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
    public void storeAlias(VarPointer from, FormatObj base, String field, Stmt stmt) {
        // x.f = y
        from.getObjs().forEach(f -> {
            if (f instanceof FormatObj fObj) {
                if (base.getIndex() != fObj.getIndex()) {
                    String toStr = base.getFields().isEmpty() ? field : String.format("%s.%s", base.getFields(), field);
                    Weight w = new Weight(fObj.getFields(), toStr);
                    w.setUpdate();
                    container.getSummary().addTransition(fObj.getIndex(), base.getIndex(), w, stmt);
                }
            }
        });

    }
    public void genSink(String sink, Weight weight, Local var, int calleeID) {
        // var.w => sink
        varFields(var, weight.getFromFields()).stream().
                flatMap(Pointer::formatObjs)
                .forEach(o -> {
                    int i = o.getIndex();
                    if (canHoldString(o.type) && i != -1) {
                        Weight w = new Weight(o.getFields());
                        container.getSummary().addSink(sink, i, w, calleeID);
                    }
                });
    }
    public void updateLhs(Local lhs, RefType type) {
        Obj obj = new Obj(type, container);
        getVarPointer(lhs).add(obj);
    }
    private boolean canHoldString(Type type) {
        String typeStr = type.toString();
        if (typeStr.equals("java.lang.String") || typeStr.equals("java.lang.StringBuilder")
                || typeStr.equals("java.lang.CharSequence")
                || typeStr.equals("char[]"))
            return true;
        return false;
    }
    public VarPointer getVarPointer(Local var) {
        return vars.computeIfAbsent(var,
                f -> new VarPointer(var));
    }
    public boolean hasAbstractObj(VarPointer vp) {
        for (Obj obj : vp.getObjs())
            if (obj instanceof FormatObj fo && fo.getType() instanceof RefType rt && rt.getSootClass().isAbstract())
                    return true;
        return false;
    }
    public void getSameVP(Local from, Local to) {
        if (vars.containsKey(from)) {
            vars.put(to, vars.get(from));
        }
    }
    public InstanceField getInstanceField(Obj base, String field) {
        if (fields.contains(base, field))
            return fields.get(base, field);
        InstanceField ifield = new InstanceField(base, field);
        if (base instanceof FormatObj fobj) {
            Type type = fieldType(fobj, field);
            if (type == null)
                return null;
            GenObj gobj = new GenObj(fobj, field, type);
            ifield.add(gobj);
        }
        fields.put(base, field, ifield);
        return ifield;
    }
    private Type fieldType(FormatObj base, String field) {
        Type baseType = base.type;
        if (!field.equals("[*]") && baseType instanceof RefType refType) {
            try {
                return refType.getSootClass().getFieldByName(field).getType();
            } catch (Exception e) {
                return null;
            }

        }
        return baseType;

    }
    public Set<Pointer> varFields(Local var, List<String> fields) {
        Set<Pointer> pointers = Set.of(getVarPointer(var));
        for (String field : fields) {
            pointers = pointers.stream().flatMap(Pointer::objs)
                    .map(o -> getInstanceField(o, field))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (pointers.isEmpty())
                return pointers;
        }
        return pointers;
    }
    public ArrayIndex getArrayIndex(Obj base) {
        return arrays.computeIfAbsent(base,
                f -> new ArrayIndex(base));
    }
    public StaticField getStaticField(SootField field) {
        return statics.computeIfAbsent(field,
                f -> new StaticField(field));
    }
    public Set<Obj> getLocalObjs(Local var) {
        return getVarPointer(var).getObjs();
    }

    public void genReturn(Local retVar, Stmt stmt) {
        Summary summary = container.getSummary();
        getVarPointer(retVar).getObjs().forEach(obj -> {
            if (obj instanceof FormatObj fobj) {
                Weight w = new Weight(fobj.getFields());
                summary.addTransition(fobj.getIndex(), -2, w, stmt);
            } else if (obj.getType() instanceof RefType rt){
                summary.addReturn(rt);
            }
            fields.row(obj).forEach((field, ifield) -> {
                ifield.getObjs().forEach(o -> {
                    if (o instanceof FormatObj fobj) {
                        // ret.field = fobj
                        Weight w = new Weight(fobj.getFields(), field);
                        container.getSummary().addTransition(fobj.getIndex(), -2, w, stmt);
                    }
                });
            });
        });
    }

    public void handleCast(Local lhs, Local rhs, RefType type) {
        VarPointer vp = getVarPointer(rhs);
        SpCallGraph cg = container.getCg();
        vars.put(lhs, vp);
        vp.getObjs().forEach(obj -> {
            if (obj.getType() instanceof RefType rt && cg.isSubClass(type.getSootClass(), rt.getSootClass()))
                obj.setType(type);
        });
    }
}
