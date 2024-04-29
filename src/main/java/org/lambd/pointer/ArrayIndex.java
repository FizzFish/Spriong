package org.lambd.pointer;

import org.lambd.obj.Obj;

public class ArrayIndex extends Pointer {
    private Obj base;
    public ArrayIndex(Obj base) {
        this.base = base;
    }
}
