package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.obj.Fraction;
import org.lambd.obj.Relation;
import soot.SootField;
import soot.jimple.Stmt;

public record FieldTransition(int from, int to, Relation relation) implements Transition {
    @Override
    public void apply(SpMethod method, Stmt stmt) {
        method.accept(stmt, from, to, relation);
    }
    public String toString() {
        return String.format("FieldTransition{ %d =%s=> %d", from, relation, to);
    }

}