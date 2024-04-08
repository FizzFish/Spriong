package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class Obj {
    public Type type;
    public SpMethod method;
    public Obj(Type type, SpMethod method) {
        this.type = type;
        this.method = method;
    }
    public String toString() {
        return String.format("Obj%s@%s", type, method.getName());
    }
}
