package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

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
    public boolean isFormat() {
        return false;
    }

    public String getFields() {
        return null;
    }
}
