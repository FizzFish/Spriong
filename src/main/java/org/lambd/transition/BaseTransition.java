package org.lambd.transition;

import org.lambd.SpMethod;
import soot.jimple.Stmt;

public record BaseTransition(int from, int to, int kind) implements Transition {
    public String toString() {
        return "(" + from + ", " + to + ", " + kind +")";
    }

    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.accept(stmt, from, to, kind);
    }

}
