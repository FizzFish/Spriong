package org.lambd.pointer;

import soot.SootField;

public class InstanceField implements Pointer {
    private final Obj base;
    private final SootField field;
    public InstanceField(Obj base, SootField field) {
        this.base = base;
        this.field = field;
    }
}
