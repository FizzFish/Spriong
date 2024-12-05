package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transformer.SpStmt;
import soot.Type;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

public class ConstantObj extends RealObj {
    private Constant constant;
    public ConstantObj(Type type, SpStmt stmt, Constant constant)
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
}
