package org.lambd.obj;

import soot.SootField;
import soot.Type;
import soot.Value;
import soot.jimple.Stmt;

public class ConstantObj extends Obj {
    private Value val;

    public ConstantObj(Type type, Stmt stmt, Value val)
    {
        super(type, stmt);
        this.val = val;
    }
    public Value getVal() {
        return val;
    }
    public String toString() {
        return String.format("ConstantObj: %s", val);
    }
    public boolean isFormat() {
        return false;
    }
}
