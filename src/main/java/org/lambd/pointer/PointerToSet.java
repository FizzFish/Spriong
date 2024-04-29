package org.lambd.pointer;

import com.google.common.collect.HashBasedTable;
import org.lambd.SpMethod;
import org.lambd.obj.FormatObj;
import org.lambd.obj.GenObj;
import org.lambd.obj.Obj;
import org.lambd.transition.Weight;
import soot.Body;
import soot.Local;
import soot.SootField;

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
        to.addAll(from.getObjs());
    }
    public void update(Local from, Local to, Weight weight) {
        // to.w2 = from.w1
        Set<Obj> objs = varFields(from, weight.getFromFields())
                .flatMap(Pointer::objs).collect(Collectors.toSet());
        varFields(to, weight.getToFields()).forEach(toPointer -> {
            if (weight.isUpdate()) {
                toPointer.formatObjs().forEach(tfObj -> {
                    objs.stream().filter(Obj::isFormat).map(obj -> (FormatObj) obj).forEach(ffObj -> {
                        Weight w = new Weight(ffObj.getFields(), tfObj.getFields());
                        w.setUpdate();
                        container.getSummary().addTransition(ffObj.getIndex(), tfObj.getIndex(), w);
                    });
                });
            }
            toPointer.addAll(objs);
        });

    }
    public void storeAlias(VarPointer from, FormatObj base, String field) {
        // x.f = y
        from.getObjs().forEach(f -> {
            if (f instanceof FormatObj fObj) {
                if (base.getIndex() != fObj.getIndex()) {
                    String toStr = base.getFields().isEmpty() ? field : String.format("%s.%s", base.getFields(), field);
                    Weight w = new Weight(fObj.getFields(), toStr);
                    w.setUpdate();
                    container.getSummary().addTransition(fObj.getIndex(), base.getIndex(), w);
                }
            }
        });

    }
    public void genSink(String sink, Weight weight, Local var) {
        // var.w => sink
        varFields(var, weight.getFromFields()).
                flatMap(Pointer::formatObjs)
                .forEach(o -> {
                    Weight w = new Weight(o.getFields());
                    container.getSummary().addSink(o.getIndex(), w, sink);
                });
    }
    public VarPointer getVarPointer(Local var) {
        return vars.computeIfAbsent(var,
                f -> new VarPointer(var));
    }
    public InstanceField getInstanceField(Obj base, String field) {
        if (fields.contains(base, field))
            return fields.get(base, field);
        InstanceField ifield = new InstanceField(base, field);
        if (base instanceof FormatObj fobj) {
            GenObj gobj = new GenObj(fobj, field);
            ifield.add(gobj);
        }
        fields.put(base, field, ifield);
        return ifield;
    }
    public Stream<Pointer> varFields(Local var, List<String> fields) {
        Stream<Pointer> pointers = Stream.of(getVarPointer(var));
        for (String field : fields) {
            pointers = pointers.flatMap(Pointer::objs)
                    .map(o -> getInstanceField(o, field));
//            if (pointers.noneMatch(x -> true))
//                break;
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

    public void genReturn(Local retVar) {
        getVarPointer(retVar).getObjs().forEach(obj -> {
            if (obj instanceof FormatObj fobj) {
                Weight w = new Weight(fobj.getFields());
                container.getSummary().addTransition(fobj.getIndex(), -2, w);
            }
        });
    }
}
