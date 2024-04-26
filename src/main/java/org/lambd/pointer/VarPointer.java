package org.lambd.pointer;

import soot.Local;

public class VarPointer implements Pointer {
    private final Local var;
    public VarPointer(Local var) {
        this.var = var;
    }
}
