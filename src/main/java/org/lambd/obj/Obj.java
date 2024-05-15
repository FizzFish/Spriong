package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transition.RetTrans;
import soot.Type;
import soot.jimple.Stmt;

public class Obj {
    public Type type;
    public Stmt stmt;
    public Obj(Type type, Stmt stmt) {
        this.type = type;
        this.stmt = stmt;
    }
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
    public boolean isFormat() {
        return false;
    }

    public int hashCode() {
        return type.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj instanceof Obj other)
            return type.equals(other.type);
        return false;
    }
}
