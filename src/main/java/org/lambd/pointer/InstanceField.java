package org.lambd.pointer;

import org.lambd.obj.Obj;
import soot.SootField;

public class InstanceField extends Pointer {
    private final Obj base;
    private final SootField field;
    public InstanceField(Obj base, SootField field) {
        this.base = base;
        this.field = field;
    }
}
