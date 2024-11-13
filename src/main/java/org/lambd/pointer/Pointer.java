package org.lambd.pointer;

import org.lambd.obj.ConstantObj;
import org.lambd.obj.FormatObj;
import org.lambd.obj.Obj;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class Pointer {
    protected Set<Obj> objs;
    public Pointer() {
        objs = new HashSet<>();
    }
    public Set<Obj> getObjs() {
        return objs;
    }
    public Stream<FormatObj> formatObjs() {
        return objs.stream()
                .filter(FormatObj.class::isInstance)
                .map(FormatObj.class::cast);
    }
    public Stream<ConstantObj> constantObjs() {
        return objs.stream()
                .filter(ConstantObj.class::isInstance)
                .map(ConstantObj.class::cast);
    }
    public Stream<Obj> abstractObjs() {
        return objs.stream()
                .filter(Obj::isMayMultiple);
    }
    public Stream<Obj> realObjs() {
        return objs.stream()
                .filter(o -> !o.isMayMultiple());
    }
    public Stream<Obj> objs() {
        return objs.stream();
    }
    public void add(Obj obj) {
        objs.add(obj);
    }
    public void addAll(Set<Obj> objs) {
        this.objs.addAll(objs);
    }
    public boolean isEmpty() {
        return objs.isEmpty();
    }
    public boolean allInterface() {
        return objs.stream().allMatch(Obj::isInterface);
    }
}
