package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class FormatObj extends Obj {
    private int index;
    private int update;
    public FormatObj(Type type, SpMethod method, int index)
    {
        super(type, method);
        this.index = index;
        update = index;
        this.weight = 2;
    }
    public int getIndex() {
        return index;
    }
    public boolean exposed() { return true; }
//    public void deepCopy(Location other) {
//        if (other instanceof FormatObj formatObj)
//            update = formatObj.getIndex();
//    }
    public String toString() {
        return String.format("FormatObj:%s@%s", type, method.getName());
    }
}
