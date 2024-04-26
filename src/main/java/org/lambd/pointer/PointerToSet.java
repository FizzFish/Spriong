package org.lambd.pointer;

import com.google.common.collect.HashBasedTable;
import org.lambd.SpMethod;
import org.lambd.transition.Weight;
import soot.Body;
import soot.Local;
import soot.SootField;

import java.util.*;

public class PointerToSet {
    private Map<Pointer, Set<Obj>> map = new HashMap<>();
    private Map<Local, VarPointer> vars = new HashMap<>();
    private HashBasedTable<Obj, SootField, InstanceField> fields = HashBasedTable.create();
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
            addLocal(var, new Obj(true, i));
        }
        if (!container.getSootMethod().isStatic()) {
            Local thisVar = body.getThisLocal();
            addLocal(thisVar, new Obj(true, -1));
        }
    }
    public void add(Pointer pointer, Obj obj) {
        if (!map.containsKey(pointer)) {
            Set<Obj> set = new HashSet<>();
            set.add(obj);
            map.put(pointer, set);
        } else {
            Set<Obj> objSet = map.get(pointer);
            objSet.add(obj);
        }
    }
    public void add(Pointer pointer, Set<Obj> objs) {
        if (!map.containsKey(pointer)) {
            Set<Obj> set = new HashSet<>(objs);
            map.put(pointer, set);
        } else {
            Set<Obj> objSet = map.get(pointer);
            objSet.addAll(objs);
        }
    }
    public void addLocal(Local var, Obj obj) {
        VarPointer vp = getVarPointer(var);
        add(vp, obj);
    }
    public void copy(Pointer from, Pointer to) {
        add(from, map.get(to));
    }
    public VarPointer getVarPointer(Local var) {
        return vars.computeIfAbsent(var,
                f -> new VarPointer(var));
    }
    public InstanceField getInstanceField(Obj base, SootField field) {
        if (fields.contains(base, field))
            return fields.get(base, field);
        InstanceField ifield = new InstanceField(base, field);
        return ifield;
    }
    public ArrayIndex getArrayIndex(Obj base) {
        return arrays.computeIfAbsent(base,
                f -> new ArrayIndex(base));
    }
    public StaticField getStaticField(SootField field) {
        return statics.computeIfAbsent(field,
                f -> new StaticField(field));
    }
    public Set<Obj> getObjs(Pointer pointer) {
        return map.get(pointer);
    }
    public Set<Obj> getLocalObjs(Local var) {
        return map.get(getVarPointer(var));
    }
}
