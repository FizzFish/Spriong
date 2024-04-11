package org.lambd.transition;

import org.lambd.SpMethod;
import soot.Type;
import soot.jimple.Stmt;

public class GenType implements Transition {
    private Type type;
    public GenType(Type type) {
        this.type = type;
    }
    @Override
    public void apply(SpMethod method, Stmt stmt) {
    }
    public Type getType() {
        return type;
    }
    public String toString() {
        return "GenType(" + type + ")";
    }
}
