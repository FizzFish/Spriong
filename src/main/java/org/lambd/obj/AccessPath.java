package org.lambd.obj;

import org.lambd.utils.PrimeGenerator;
import soot.Local;
import soot.SootField;

import java.util.ArrayList;
import java.util.List;

public class AccessPath implements Location {
    private Obj obj;
    private int weight = 3;
    private long pushNum = 1;
    private long popNum = 1;
    public AccessPath(Location location, Object field, boolean pushOrPop) {
        long num = PrimeGenerator.v().getPrime(field);
        if (location instanceof Obj obj) {
            this.obj = obj;
            if (pushOrPop)
                pushNum *= num;
            else
                popNum *= num;
        } else if (location instanceof AccessPath other) {
            this.obj = other.obj;
            long numerator = other.pushNum;
            long denominator = other.popNum;
            if (pushOrPop) {
                if (denominator % num == 0)
                    denominator /= num;
                else
                    numerator *= num;
            } else {
                if (numerator % num == 0)
                    numerator /= num;
                else
                    denominator *= num;
            }
            pushNum = numerator;
            popNum = denominator;
        }
    }
    public Obj getObj() {
        return obj;
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
