package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class Obj implements Location {
    public Type type;
    public SpMethod method;
    protected int weight;
    public Obj(Type type, SpMethod method) {
        this.type = type;
        this.method = method;
        this.weight = 1;
    }
    public String toString() {
        return String.format("Obj%s@%s", type, method.getName());
    }
    public int getWeight() {
        return weight;
    }
    @Override
    public Location combineWith(Location other) {
        if (other.getWeight() >= this.weight)
            return other;
        return this;
    }
}
