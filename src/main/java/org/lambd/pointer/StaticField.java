package org.lambd.pointer;

import soot.SootField;

public class StaticField implements Pointer {
    private final SootField field;
    public StaticField(SootField field) {
        this.field = field;
    }
}
