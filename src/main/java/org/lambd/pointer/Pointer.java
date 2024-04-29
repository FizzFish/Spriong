package org.lambd.pointer;

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
                .filter(obj -> obj instanceof FormatObj)
                .map(obj -> (FormatObj) obj);
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
}
