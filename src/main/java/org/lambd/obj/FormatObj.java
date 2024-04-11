package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class FormatObj extends NewObj {
    private int index;
    public FormatObj(Type type, SpMethod method, int index)
    {
        super(type, method);
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
    public boolean exposed() { return true; }
    public String toString() {
        return String.format("FormatObj: %s@%s", type, method.getName());
    }
}
