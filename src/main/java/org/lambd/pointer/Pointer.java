package org.lambd.pointer;

import org.lambd.condition.Condition;
import org.lambd.obj.*;
import soot.SootField;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Pointer {
    // 理论上同一个Condition下只应该有一个Obj，保持强更新和流敏感
    private Map<Condition, Obj> objMap = new HashMap<>();
    public Pointer() {
    }
    public Set<Obj> getObjs() {
        return objMap.values().stream().collect(Collectors.toSet());
    }
    public Set<RealObj> getRealObjs() {
        Set<RealObj> result = new HashSet<>();
        objMap.values().forEach(o -> {
            if (o instanceof FormatObj fObj)
                result.addAll(fObj.getRealObjs());
            else
                result.add((RealObj) o);
        });
        return result;
    }
    public Stream<FormatObj> formatObjs() {
        return objMap.values().stream()
                .filter(FormatObj.class::isInstance)
                .map(FormatObj.class::cast);
    }
    public Stream<ConstantObj> constantObjs() {
        return objMap.values().stream()
                .filter(ConstantObj.class::isInstance)
                .map(ConstantObj.class::cast);
    }
    public Stream<Obj> objs() {
        return objMap.values().stream();
    }
    public Stream<RealObj> realObjs() {
        return getRealObjs().stream();
    }

    public Map<Integer, List<SootField>> paramRelations() {
        Map<Integer, List<SootField>> result = new HashMap<>();
        for (Obj obj : objMap.values()) {
            if (obj instanceof FormatObj fObj) {
                if (fObj.getFields() == null)
                    System.out.println();
                result.put(fObj.getIndex(), fObj.getFields());
            }
        }
        return result;
    }
    public void add(Obj obj) {
        objMap.put(obj.getCondition(), obj);
    }
    public void addAll(Set<Obj> objs) {
        objs.forEach(this::add);
    }
    public void copyFrom(Pointer pointer) {
        objMap.putAll(pointer.objMap);
    }
    public boolean allInterface() {
        return false;
//        return objs.stream().allMatch(Obj::isInterface);
    }
    public boolean isEmpty() {
        return objMap.isEmpty();
    }
}
