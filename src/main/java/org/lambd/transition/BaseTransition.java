package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.obj.Fraction;
import org.lambd.obj.Relation;
import soot.jimple.Stmt;

public record BaseTransition(int from, int to, int kind) implements Transition {
    public String toString() {
        return "(" + from + ", " + to + ", " + kind +")";
    }

    @Override
    public void apply(SpMethod method, Stmt stmt) {
        boolean update = kind == 1;
        Relation relation = new Relation(Fraction.one(), update);
        method.accept(stmt, from, to, relation);
    }

}
