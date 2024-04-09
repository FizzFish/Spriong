package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class Obj implements Location {
    public Type type;
    public SpMethod method;
    protected int weight;
    private final Fraction fraction = Fraction.one;
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
    public boolean exposed() { return false; }
    public int getIndex() { return -5; }
    public Fraction getFraction() {
        return fraction;
    }
    public SpMethod getMethod() {
        return method;
    }
    @Override
    public Location combineWith(Location other) {
        if (other.getWeight() >= this.weight)
            return other;
        return this;
    }
}
