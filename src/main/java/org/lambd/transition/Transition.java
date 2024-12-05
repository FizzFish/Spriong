package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.jimple.Stmt;

public interface Transition {
    void apply(SpMethod method, SpStmt stmt);
}
