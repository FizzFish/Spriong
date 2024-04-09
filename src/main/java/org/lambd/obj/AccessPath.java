package org.lambd.obj;

import soot.Local;
import soot.SootField;

import java.util.List;

public class AccessPath implements Location {
    private Obj obj;
    private List<SootField> fields;
    private int weight = 3;
    public AccessPath(Obj obj, List<SootField> fields) {
        this.obj = obj;
        this.fields = fields;
    }

    @Override
    public Location combineWith(Location other) {
        if (other.getWeight() >= this.weight)
            return other;
        return this;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
