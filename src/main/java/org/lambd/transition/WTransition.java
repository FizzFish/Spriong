package org.lambd.transition;

import org.lambd.SpMethod;
import soot.jimple.Stmt;

public record WTransition(int from, int to, Weight w) implements Transition {
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.handleTransition(stmt, from, to, w);
    }
    public String toString() {
        return String.format("FieldTransition{ %d =%s=> %d", from, w, to);
    }

}