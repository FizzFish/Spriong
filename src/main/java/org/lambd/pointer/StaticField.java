package org.lambd.pointer;

import soot.SootField;

public class StaticField extends Pointer {
    private final SootField field;
    public StaticField(SootField field) {
        this.field = field;
    }
}
