package org.lambd.transition;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.jimple.Stmt;

public class LoadTransition implements Transition {
    private String code;
    public LoadTransition(String code) {
        this.code = code;
    }
    public String toString() {
        return "(" + code +")";
    }
    @Override
    public void apply(SpMethod method, SpStmt stmt) {
        if (code.equals("loadClasses")) {
            method.handleLoadTransition(stmt);
        }
    }
    // Getters and Setters
}

