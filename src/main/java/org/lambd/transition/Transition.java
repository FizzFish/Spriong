package org.lambd.transition;

import org.lambd.SpMethod;
import soot.jimple.Stmt;

public interface Transition {
    void apply(SpMethod method, Stmt stmt);
    default boolean debugTrans() {
        return false;
    }
}
