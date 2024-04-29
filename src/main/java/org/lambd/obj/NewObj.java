package org.lambd.obj;

import org.lambd.SpMethod;
import soot.Type;

public class NewObj extends Obj {
    public NewObj(Type type, SpMethod method) {
        super(type, method);
    }
    public String toString() {
        return String.format("NewObj: %s@%s", type, container.getName());
    }

}
