package org.lambd.obj;

import org.lambd.SpMethod;
import org.lambd.transition.RetTrans;
import soot.Type;
import soot.jimple.Stmt;

import java.util.Objects;

/**
 * Obj 代表一个抽象的Java对象，例如：o = New Object()
 */
public class Obj {
    public Type type;
    public Stmt stmt;
    public Obj(Type type, Stmt stmt) {
        this.type = type;
        this.stmt = stmt;
    }
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
    public boolean isFormat() {
        return false;
    }
    public Obj castClone(Stmt stmt, Type type) {
        return new Obj(type, stmt);
    }
    public int hashCode() {
        return Objects.hash(type, stmt);
    }
    public boolean equals(Object obj) {
        if (obj instanceof Obj other) {
            if (stmt == null) {
                return type.equals(other.type) && other.stmt == null;
            }
            return type.equals(other.type) && stmt.equals(other.stmt);
        }
        return false;
    }
}
