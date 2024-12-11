package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.Type;
import soot.jimple.Stmt;

import javax.annotation.Nullable;

public class RealObj extends Obj {
    public RealObj(Type type, SpStmt stmt) {
        super(type, stmt);
    }

    @Override
    public Obj castClone(SpStmt stmt, @Nullable Type newType) {
        return this;
    }

    @Override
    public boolean real() {
        return true;
    }
}
