package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class NewObj {
    public Type type;
    public SpMethod method;
    public NewObj(Type type, SpMethod method) {
        this.type = type;
        this.method = method;
    }
    public String toString() {
        return String.format("Obj: %s@%s", type, method.getName());
    }
    public boolean exposed() { return false; }
    public int getIndex() { return -5; }
    public SpMethod getMethod() {
        return method;
    }
}
