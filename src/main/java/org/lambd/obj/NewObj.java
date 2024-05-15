package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;
import soot.jimple.Stmt;

public class NewObj extends Obj {
    public NewObj(Type type, Stmt stmt) {
        super(type, stmt);
    }
    public String toString() {
        return String.format("NewObj: %s@%s", type, stmt);
    }

}
