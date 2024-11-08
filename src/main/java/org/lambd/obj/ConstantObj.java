package org.lambd.obj;

import soot.Type;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

public class ConstantObj extends Obj {
    private Constant constant;

    public ConstantObj(Type type, Stmt stmt, Constant constant)
    {
        super(type, stmt);
        this.constant = constant;
    }
    public Constant getVal() {
        return constant;
    }
    public String getString() {
        if (constant instanceof StringConstant sc)
            return sc.value;
        return constant.toString();
    }
    public String toString() {
        return String.format("ConstantObj: %s", constant);
    }
    public boolean isFormat() {
        return false;
    }
}
