package org.lambd.transition;

import org.apache.commons.math3.fraction.Fraction;
import org.lambd.SpMethod;
import soot.jimple.Stmt;

public record BaseTrans(int from, int to, int kind) implements Transition {
    public String toString() {
        return "(" + from + ", " + to + ", " + kind +")";
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        Weight relation = Weight.ONE;
        if (kind == 1)
            relation = Weight.COPY;
        method.handleTransition(stmt, from, to, relation);
    }

}
