package org.lambd.pointer;

public class Obj {
    private boolean exposed;
    private int index;
    public Obj(boolean exposed, int index) {
        this.exposed = exposed;
        this.index = index;
    }
    public boolean exposed() {
        return exposed;
    }
    public int getIndex() {
        return index;
    }
}
