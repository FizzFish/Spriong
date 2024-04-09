package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transition.FieldTransition;
import org.lambd.transition.Transition;
import org.lambd.utils.PrimeGenerator;
import soot.Local;
import soot.SootField;

import java.util.ArrayList;
import java.util.List;

public class AccessPath implements Location {
    private Obj obj;
    private int weight = 3;
    private Fraction fraction = new Fraction(1, 1);
    public AccessPath(Location location, Object field, boolean pushOrPop) {
        long num = PrimeGenerator.v().getPrime(field);
        if (location instanceof Obj obj) {
            this.obj = obj;
            if (pushOrPop)
                fraction.push(num);
            else
                fraction.pop(num);
        } else if (location instanceof AccessPath other) {
            this.obj = other.obj;
            Fraction otherFraction = other.fraction;
            if (pushOrPop)
                fraction.multiply(otherFraction);
            else
                fraction.divide(otherFraction);
        }
    }
    public Obj getObj() {
        return obj;
    }
    public boolean exposed() {
        return obj.exposed();
    }
    public SpMethod getMethod() { return obj.getMethod(); }
    public int getIndex() { return obj.getIndex(); }
    public Fraction getFraction() { return fraction; }
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
