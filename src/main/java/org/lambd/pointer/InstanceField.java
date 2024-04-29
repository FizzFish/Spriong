package org.lambd.pointer;

import org.lambd.obj.Obj;
import soot.SootField;

public class InstanceField extends Pointer {
    private final Obj base;
    private final String field;
    public InstanceField(Obj base, String field) {
        this.base = base;
        this.field = field;
    }
}
