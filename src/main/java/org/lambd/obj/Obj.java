package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transition.RetTrans;
import soot.Type;
import soot.jimple.Stmt;

public class Obj {
    public Type type;
    public SpMethod container;
    public Obj(Type type, SpMethod container) {
        this.type = type;
        this.container = container;
    }
    public SpMethod getContainer() {
        return container;
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

    public String getFields() {
        return null;
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
