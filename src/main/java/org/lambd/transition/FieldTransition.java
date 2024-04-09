package org.lambd.transition;

import org.lambd.SpMethod;
import soot.SootField;
import soot.jimple.Stmt;

public record FieldTransition(int from, int to, SootField field, boolean direction, int kind) implements Transition {
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.accept(stmt, from, to, kind);
    }

}