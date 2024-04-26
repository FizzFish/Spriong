package org.lambd.pointer;

public class ArrayIndex implements Pointer {
    private Obj base;
    public ArrayIndex(Obj base) {
        this.base = base;
    }
}
