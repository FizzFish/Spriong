package org.lambd.obj;
import org.lambd.SpMethod;
import soot.Type;
import soot.Value;

public class ConstantObj extends Obj {
    private Value constant;
    public ConstantObj(Type type, SpMethod method, Value constant)
    {
        super(type, method);
        this.constant = constant;
    }
    public String toString() {
        return String.format("ConstantObj%s@%s", type, method.getName());
    }
}
