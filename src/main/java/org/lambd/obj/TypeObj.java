package org.lambd.obj;

import soot.RefType;
import soot.Type;
import soot.jimple.Stmt;

import javax.annotation.Nullable;

public class TypeObj implements Obj {
    private final Type type;
    private final Stmt stmt;
    public TypeObj(Type type, Stmt stmt) {
        this.type = type;
        this.stmt = stmt;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Obj castClone(Stmt stmt, @Nullable Type newType) {
        if (newType == null || newType.equals(type))
            return this;
        return new TypeObj(newType, stmt);
    }
}
