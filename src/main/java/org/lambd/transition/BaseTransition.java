package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import soot.jimple.Stmt;

public record BaseTransition(int from, int to, int kind) implements Transition {
    public String toString() {
        return "(" + from + ", " + to + ", " + kind +")";
    }

    @Override
    public void apply(SpMethod method, Stmt stmt) {
        boolean update = kind == 1;
        Weight relation = new Weight(Fraction.ONE, update);
        method.handleTransition(stmt, from, to, relation);
    }

}
