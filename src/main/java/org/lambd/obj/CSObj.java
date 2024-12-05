package org.lambd.obj;

import org.lambd.condition.Condition;

public class CSObj {
    private final Obj obj;
    private final Condition context;
    public CSObj(Obj obj, Condition context) {
        this.obj = obj;
        this.context = context;
    }
    public Obj getObj() {
        return obj;
    }
    public Condition getContext() {
        return context;
    }
}
