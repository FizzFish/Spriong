package org.lambd.transition;

import org.lambd.SpMethod;
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
    public void apply(SpMethod method, Stmt stmt) {
        if (code.equals("loadClasses")) {
           System.out.println("Loading Classes");
            method.handleLoadTransition(stmt);
        }
    }
    // Getters and Setters
}

