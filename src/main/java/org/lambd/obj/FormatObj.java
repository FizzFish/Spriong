package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class FormatObj extends Obj {
    private int index;
    public FormatObj(Type type, SpMethod method, int index)
    {
        super(type, method);
        this.index = index;
    }
    public int getIndex() {
        return index;
    }
    public String toString() {
        return String.format("FormatObj:%s@%s", type, method.getName());
    }
}
